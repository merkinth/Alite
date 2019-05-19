/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.vending.expansion.downloader.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Messenger;

import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;

import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;

/**
 * This class handles displaying the notification associated with the download
 * queue going on in the download manager. It handles multiple status types;
 * Some require user interaction and some do not. Some of the user interactions
 * may be transient. (for example: the user is queried to continue the download
 * on 3G when it started on WiFi, but then the phone locks onto WiFi again so
 * the prompt automatically goes away)
 * <p/>
 * The application interface for the downloader also needs to understand and
 * handle these transient states.
 */
public class DownloadNotification implements IDownloaderClient {

    private int mState;
    private final Context mContext;
    private final NotificationManager mNotificationManager;

    private IDownloaderClient mClientProxy;
	private CharSequence mLabel;
    private PendingIntent mContentIntent;
    private DownloadProgressInfo mProgressInfo;

    private static final String LOGTAG = "DownloadNotification";
    private static final int NOTIFICATION_ID = LOGTAG.hashCode();

	/**
	 * @param ctx The context to use to obtain access to the Notification Service
	 */
	DownloadNotification(Context ctx, CharSequence applicationLabel) {
		mState = -1;
		mContext = ctx;
		mLabel = applicationLabel;
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	void setClientIntent(PendingIntent mClientIntent) {
		mContentIntent = mClientIntent;
    }

    void resendState() {
        if (null != mClientProxy) {
            mClientProxy.onDownloadStateChanged(mState);
        }
    }

	@Override
    public void onDownloadStateChanged(int newState) {
        if (null != mClientProxy) {
            mClientProxy.onDownloadStateChanged(newState);
        }
        if (newState != mState) {
            mState = newState;
            if (newState == IDownloaderClient.STATE_IDLE || null == mContentIntent) {
                return;
            }
            int stringDownloadID;
            int iconResource;
            boolean ongoingEvent;

            // get the new title string and paused text
            switch (newState) {
                case 0:
                    iconResource = android.R.drawable.stat_sys_warning;
                    stringDownloadID = R.string.state_unknown;
                    ongoingEvent = false;
                    break;

                case IDownloaderClient.STATE_DOWNLOADING:
                    iconResource = android.R.drawable.stat_sys_download;
                    stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                    ongoingEvent = true;
                    break;

                case IDownloaderClient.STATE_FETCHING_URL:
                case IDownloaderClient.STATE_CONNECTING:
                    iconResource = android.R.drawable.stat_sys_download_done;
                    stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                    ongoingEvent = true;
                    break;

                case IDownloaderClient.STATE_COMPLETED:
                case IDownloaderClient.STATE_PAUSED_BY_REQUEST:
                    iconResource = android.R.drawable.stat_sys_download_done;
                    stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                    ongoingEvent = false;
                    break;

                case IDownloaderClient.STATE_FAILED:
                case IDownloaderClient.STATE_FAILED_CANCELED:
                case IDownloaderClient.STATE_FAILED_FETCHING_URL:
                case IDownloaderClient.STATE_FAILED_SDCARD_FULL:
                case IDownloaderClient.STATE_FAILED_UNLICENSED:
                    iconResource = android.R.drawable.stat_sys_warning;
                    stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                    ongoingEvent = false;
                    break;

                default:
                    iconResource = android.R.drawable.stat_sys_warning;
                    stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                    ongoingEvent = true;
                    break;
            }

			if (mProgressInfo != null) {
				mNotificationManager.notify(NOTIFICATION_ID, getCustomNotification(mContext, mContentIntent,
					mLabel, iconResource, mProgressInfo));
				return;
			}

			String mCurrentText = L.string(stringDownloadID);
			mNotificationManager.notify(NOTIFICATION_ID, new NotificationCompat.Builder(mContext, "")
				.setAutoCancel(!ongoingEvent)
				.setTicker(mLabel + ": " + mCurrentText)
				.setContentTitle(mLabel)
				.setContentText(mCurrentText)
				.setSmallIcon(iconResource)
				.setContentIntent(mContentIntent)
				.setOngoing(ongoingEvent)
				.build());
        }
    }

	@Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        mProgressInfo = progress;
        if (null != mClientProxy) {
            mClientProxy.onDownloadProgress(progress);
        }
        if (progress.mOverallTotal <= 0) {
            // we just show the text
			mNotificationManager.notify(NOTIFICATION_ID, new NotificationCompat.Builder(mContext, "")
				.setTicker(mLabel)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.build());
			return;
        }

		mNotificationManager.notify(NOTIFICATION_ID, getCustomNotification(mContext, mContentIntent,
			mLabel, android.R.drawable.stat_sys_download, mProgressInfo));
    }

	private Notification getCustomNotification(Context context, PendingIntent intent, CharSequence title,
		int icon, DownloadProgressInfo progress) {
		// Build the RemoteView object
		RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.status_bar_ongoing_event_progress_bar);
		expandedView.setTextViewText(R.id.title, title);
		// look at strings
		expandedView.setViewVisibility(R.id.description, View.VISIBLE);
		expandedView.setTextViewText(R.id.description,
			Helpers.getDownloadProgressString(progress.mOverallProgress, progress.mOverallTotal));
		expandedView.setViewVisibility(R.id.progress_bar_frame, View.VISIBLE);
		expandedView.setProgressBar(R.id.progress_bar,
			(int) (progress.mOverallTotal >> 8),
			(int) (progress.mOverallProgress >> 8),
			progress.mOverallTotal <= 0);
		expandedView.setViewVisibility(R.id.time_remaining, View.VISIBLE);
		expandedView.setTextViewText(R.id.time_remaining,
			context.getString(R.string.time_remaining_notification,
				Helpers.getTimeRemaining(progress.mTimeRemaining)));
		expandedView.setTextViewText(R.id.progress_text,
			Helpers.getDownloadProgressPercent(progress.mOverallProgress, progress.mOverallTotal));
		expandedView.setImageViewResource(R.id.appIcon, icon);

		return new NotificationCompat.Builder(context, "")
			.setSmallIcon(icon)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setContentIntent(intent)
			.setCustomContentView(expandedView)
			.build();
	}

	/**
     * Called in response to onClientUpdated. Creates a new proxy and notifies
     * it of the current state.
     *
     * @param msg the client Messenger to notify
     */
    void setMessenger(Messenger msg) {
        mClientProxy = DownloaderClientMarshaller.CreateProxy(msg);
        if (null != mProgressInfo) {
            mClientProxy.onDownloadProgress(mProgressInfo);
        }
        if (mState != -1) {
            mClientProxy.onDownloadStateChanged(mState);
        }
    }

    @Override
    public void onServiceConnected(Messenger m) {
    }

}
