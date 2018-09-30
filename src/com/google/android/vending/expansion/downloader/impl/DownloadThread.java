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

import com.google.android.vending.expansion.downloader.Constants;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;

import android.content.Context;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.*;

/**
 * Runs an actual download
 */
public class DownloadThread {

	private static final int HTTP_TEMPORARY_REDIRECT = 307;

	private Context mContext;
    private DownloadInfo mInfo;
    private DownloaderService mService;
    private final DownloadsDB mDB;
    private final DownloadNotification mNotification;

    DownloadThread(DownloadInfo info, DownloaderService service, DownloadNotification notification) {
        mContext = service;
        mInfo = info;
        mService = service;
        mNotification = notification;
        mDB = DownloadsDB.getDB(service);
    }

    /**
     * State for the entire run() and executeDownload() methods.
     */
    private class State {
        String mFilename;
        FileOutputStream mStream;
        boolean mCountRetry = false;
        int mRetryAfter = 0;
        int mRedirectCount;
        boolean mGotData = false;
        String mRequestUri;

		int mBytesSoFar = 0;
		int mBytesThisSession = 0;
		String mHeaderETag;
		boolean mContinuingDownload = false;
		String mHeaderContentLength;
		String mHeaderContentDisposition;
		String mHeaderContentLocation;
		int mBytesNotified = 0;
		long mTimeLastNotification = 0;

		public State(DownloadInfo info, DownloaderService service) {
            mRedirectCount = info.mRedirectCount;
            mRequestUri = info.mUri;
            mFilename = service.generateTempSaveFileName(info.mFileName);
        }
    }

    /**
     * Raised from methods called by run() to indicate that the current request
     * should be stopped immediately. Note the message passed to this exception
     * will be logged and therefore must be guaranteed not to contain any PII,
     * meaning it generally can't include any information about the request URI,
     * headers, or destination filename.
     */
    private class StopRequest extends Exception {
        private static final long serialVersionUID = 6338592678988347973L;
        int mFinalStatus;

        StopRequest(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }

    /**
     * Executes the download in a separate thread
     */
	public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        State state = new State(mInfo, mService);
        PowerManager.WakeLock wakeLock = null;
        int finalStatus = DownloaderService.STATUS_UNKNOWN_ERROR;

        try {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
            wakeLock.acquire();

            if (Constants.LOGV) {
                Log.v(Constants.TAG, "initiating download for " + mInfo.mFileName);
                Log.v(Constants.TAG, "  at " + mInfo.mUri);
            }

			do {
				if (Constants.LOGV) {
					Log.v(Constants.TAG, "initiating download for " + mInfo.mFileName);
					Log.v(Constants.TAG, "  at " + mInfo.mUri);
				}
			} while (!executeDownload(state));

			if (Constants.LOGV) {
                Log.v(Constants.TAG, "download completed for " + mInfo.mFileName);
                Log.v(Constants.TAG, "  at " + mInfo.mUri);
            }
            finalizeDestinationFile(state);
            finalStatus = DownloaderService.STATUS_SUCCESS;
        } catch (StopRequest error) {
            // remove the cause before printing, in case it contains PII
            Log.w(Constants.TAG, "Aborting request for download " + mInfo.mFileName + ": " + error.getMessage());
            error.printStackTrace();
            finalStatus = error.mFinalStatus;
            // fall through to finally block
        } catch (Throwable ex) { // sometimes the socket code throws unchecked exceptions
            Log.w(Constants.TAG, "Exception for " + mInfo.mFileName + ": " + ex);
            finalStatus = DownloaderService.STATUS_UNKNOWN_ERROR;
            // falls through to the code that reports an error
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
            cleanupDestination(state, finalStatus);
			updateDownloadDatabase(finalStatus, state.mCountRetry, state.mRetryAfter, state.mRedirectCount, state.mGotData);
		}
    }

    /**
     * Fully execute a single download request - setup and send the request,
     * handle the response, and transfer the data to the destination file.
	 * @return true if download done
     */
    private boolean executeDownload(State state) throws StopRequest {
        checkPausedOrCanceled();
        setupDestinationFile(state);

        // check just before sending the request to avoid using an invalid connection at all
        checkConnectivity();

		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(state.mRequestUri).openConnection();
			connection.setRequestMethod("GET");
			addRequestProperty(state, connection);

			mNotification.onDownloadStateChanged(IDownloaderClient.STATE_CONNECTING);
			connection.connect();

			if (handleExceptionalStatus(state, connection)) {
				return false;
			}

			if (Constants.LOGV) {
				Log.v(Constants.TAG, "received response for " + mInfo.mUri);
			}

			processResponseHeaders(state, connection);
			InputStream entityStream = openResponseEntity(state, connection);
			mNotification.onDownloadStateChanged(IDownloaderClient.STATE_DOWNLOADING);
			transferData(state, entityStream);
			return true;
		} catch (IOException e) {
			logNetworkState();
			throw new StopRequest(getFinalStatusForHttpError(state),
				"while trying to open connection / execute request: " + e, e);
		}
	}

    /**
     * Check if current connectivity is valid for this request.
     */
    private void checkConnectivity() throws StopRequest {
        switch (mService.getNetworkAvailabilityState(mDB)) {
            case DownloaderService.NETWORK_OK:
                return;
            case DownloaderService.NETWORK_NO_CONNECTION:
                throw new StopRequest(DownloaderService.STATUS_WAITING_FOR_NETWORK,
                        "waiting for network to return");
            case DownloaderService.NETWORK_TYPE_DISALLOWED_BY_REQUESTOR:
                throw new StopRequest(DownloaderService.STATUS_QUEUED_FOR_WIFI_OR_CELLULAR_PERMISSION,
                        "waiting for wifi or for download over cellular to be authorized");
            case DownloaderService.NETWORK_CANNOT_USE_ROAMING:
                throw new StopRequest(DownloaderService.STATUS_WAITING_FOR_NETWORK,
                        "roaming is not allowed");
            case DownloaderService.NETWORK_UNUSABLE_DUE_TO_SIZE:
                throw new StopRequest(DownloaderService.STATUS_QUEUED_FOR_WIFI, "waiting for wifi");
        }
    }

    /**
     * Transfer as much data as possible from the HTTP response to the
     * destination file.
     *
     * @param entityStream stream for reading the HTTP response entity
     */
    private void transferData(State state, InputStream entityStream) throws StopRequest {
		byte data[] = new byte[Constants.BUFFER_SIZE];
        for (;;) {
            int bytesRead = readFromResponse(state, data, entityStream);
            if (bytesRead == -1) { // success, end of stream already reached
                handleEndOfStream(state);
                return;
            }

            state.mGotData = true;
            writeDataToDestination(state, data, bytesRead);
			state.mBytesSoFar += bytesRead;
			state.mBytesThisSession += bytesRead;
            reportProgress(state);

            checkPausedOrCanceled();
        }
    }

    /**
     * Called after a successful completion to take any necessary action on the
     * downloaded file.
     */
    private void finalizeDestinationFile(State state) throws StopRequest {
        syncDestination(state);
        String tempFilename = state.mFilename;
        String finalFilename = Helpers.generateSaveFileName(mService, mInfo.mFileName);
        if (!state.mFilename.equals(finalFilename)) {
            File startFile = new File(tempFilename);
            File destFile = new File(finalFilename);
            if (mInfo.mTotalBytes != -1 && mInfo.mCurrentBytes == mInfo.mTotalBytes) {
                if (!startFile.renameTo(destFile)) {
                    throw new StopRequest(DownloaderService.STATUS_FILE_ERROR,
                            "unable to finalize destination file");
                }
            } else {
                throw new StopRequest(DownloaderService.STATUS_FILE_DELIVERED_INCORRECTLY,
                        "file delivered with incorrect size. probably due to network not browser configured");
            }
        }
    }

    /**
     * Called just before the thread finishes, regardless of status, to take any
     * necessary action on the downloaded file.
     */
    private void cleanupDestination(State state, int finalStatus) {
        closeDestination(state);
        if (state.mFilename != null && DownloaderService.isStatusError(finalStatus)) {
            new File(state.mFilename).delete();
            state.mFilename = null;
        }
    }

    /**
     * Sync the destination file to storage.
     */
    private void syncDestination(State state) {
        try(FileOutputStream downloadedFileStream = new FileOutputStream(state.mFilename, true)) {
            downloadedFileStream.getFD().sync();
        } catch (FileNotFoundException ex) {
            Log.w(Constants.TAG, "file " + state.mFilename + " not found: " + ex);
        } catch (SyncFailedException ex) {
            Log.w(Constants.TAG, "file " + state.mFilename + " sync failed: " + ex);
        } catch (IOException ex) {
            Log.w(Constants.TAG, "IOException trying to sync " + state.mFilename + ": " + ex);
        } catch (RuntimeException ex) {
            Log.w(Constants.TAG, "exception while syncing file: ", ex);
        }
    }

    /**
     * Close the destination output stream.
     */
    private void closeDestination(State state) {
        try {
            // close the file
            if (state.mStream != null) {
                state.mStream.close();
                state.mStream = null;
            }
        } catch (IOException ex) {
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "exception when closing the file after download : " + ex);
            }
            // nothing can really be done if the file can't be closed
        }
    }

    /**
     * Check if the download has been paused or canceled, stopping the request
     * appropriately if it has been.
     */
    private void checkPausedOrCanceled() throws StopRequest {
        if (mService.getControl() == DownloaderService.CONTROL_PAUSED) {
			if (mService.getStatus() == DownloaderService.STATUS_PAUSED_BY_APP) {
				throw new StopRequest(mService.getStatus(), "download paused");
			}
        }
    }

    /**
     * Report download progress through the database if necessary.
     */
    private void reportProgress(State state) {
        long now = System.currentTimeMillis();
        if (state.mBytesSoFar - state.mBytesNotified > Constants.MIN_PROGRESS_STEP
                && now - state.mTimeLastNotification > Constants.MIN_PROGRESS_TIME) {
            // we store progress updates to the database here
            mInfo.mCurrentBytes = state.mBytesSoFar;
            mDB.updateDownloadCurrentBytes(mInfo);

			state.mBytesNotified = state.mBytesSoFar;
			state.mTimeLastNotification = now;

            long totalBytesSoFar = state.mBytesThisSession + mService.mBytesSoFar;

            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "downloaded " + mInfo.mCurrentBytes + " out of " + mInfo.mTotalBytes);
                Log.v(Constants.TAG, "     total " + totalBytesSoFar + " out of " + mService.mTotalLength);
            }

            mService.notifyUpdateBytes(totalBytesSoFar);
        }
    }

    /**
     * Write a data buffer to the destination file.
     *
     * @param data buffer containing the data to write
     * @param bytesRead how many bytes to write from the buffer
     */
    private void writeDataToDestination(State state, byte[] data, int bytesRead) throws StopRequest {
		try {
			if (state.mStream == null) {
				state.mStream = new FileOutputStream(state.mFilename, true);
			}
			state.mStream.write(data, 0, bytesRead);
			// we close after every write --- this may be too inefficient
			closeDestination(state);
		} catch (IOException ex) {
			if (!Helpers.isExternalMediaMounted()) {
				throw new StopRequest(DownloaderService.STATUS_DEVICE_NOT_FOUND_ERROR,
						"external media not mounted while writing destination file");
			}

			long availableBytes = Helpers.getAvailableBytes(Helpers.getFilesystemRoot(state.mFilename));
			if (availableBytes < bytesRead) {
				throw new StopRequest(DownloaderService.STATUS_INSUFFICIENT_SPACE_ERROR,
						"insufficient space while writing destination file", ex);
			}
			throw new StopRequest(DownloaderService.STATUS_FILE_ERROR,
					"while writing destination file: " + ex, ex);
		}
	}

    /**
     * Called when we've reached the end of the HTTP response stream, to update
     * the database and check for consistency.
     */
    private void handleEndOfStream(State state) throws StopRequest {
        mInfo.mCurrentBytes = state.mBytesSoFar;
        // this should always be set from the market
        // if ( state.mHeaderContentLength == null ) {
        // mInfo.mTotalBytes = state.mBytesSoFar;
        // }
        mDB.updateDownload(mInfo);

        boolean lengthMismatched = state.mHeaderContentLength != null
                && state.mBytesSoFar != Integer.parseInt(state.mHeaderContentLength);
        if (lengthMismatched) {
            if (cannotResume(state)) {
                throw new StopRequest(DownloaderService.STATUS_CANNOT_RESUME,
                        "mismatched content length");
            }
			throw new StopRequest(getFinalStatusForHttpError(state),
					"closed socket before end of file");
		}
    }

    private boolean cannotResume(State state) {
        return state.mBytesSoFar > 0 && state.mHeaderETag == null;
    }

    /**
     * Read some data from the HTTP response stream, handling I/O errors.
     *
     * @param data buffer to use to read data
     * @param entityStream stream for reading the HTTP response entity
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached
     */
    private int readFromResponse(State state, byte[] data, InputStream entityStream) throws StopRequest {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            logNetworkState();
            mInfo.mCurrentBytes = state.mBytesSoFar;
            mDB.updateDownload(mInfo);
            if (cannotResume(state)) {
				throw new StopRequest(DownloaderService.STATUS_CANNOT_RESUME,
					"while reading response: " + ex + ", can't resume interrupted download with no ETag", ex);
            }
			throw new StopRequest(getFinalStatusForHttpError(state),
					"while reading response: " + ex, ex);
		}
    }

    /**
     * Open a stream for the HTTP response entity, handling I/O errors.
     * @return an InputStream to read the response entity
     */
    private InputStream openResponseEntity(State state, HttpURLConnection connection) throws StopRequest {
        try {
            return connection.getInputStream();
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while getting entity: " + ex, ex);
        }
    }

    private void logNetworkState() {
        if (Constants.LOGX) {
            Log.i(Constants.TAG, "Net " +
				(mService.getNetworkAvailabilityState(mDB) == DownloaderService.NETWORK_OK ? "Up" : "Down"));
        }
    }

    /**
     * Read HTTP response headers and take appropriate action, including setting
     * up the destination file and updating the database.
     */
    private void processResponseHeaders(State state, HttpURLConnection connection) throws StopRequest {
        if (state.mContinuingDownload) {
            // ignore response headers on resume requests
            return;
        }

        readResponseHeaders(state, connection);

        try {
            state.mFilename = mService.generateSaveFile(mInfo.mFileName, mInfo.mTotalBytes);
            state.mStream = new FileOutputStream(state.mFilename);
        } catch (DownloaderService.GenerateSaveFileError exc) {
            throw new StopRequest(exc.mStatus, exc.mMessage);
        } catch (FileNotFoundException exc) {
            // make sure the directory exists
            File pathFile = new File(Helpers.getSaveFilePath(mService));
			try {
				if (pathFile.mkdirs()) {
					state.mStream = new FileOutputStream(state.mFilename);
				}
			} catch (FileNotFoundException ignored) {
				throw new StopRequest(DownloaderService.STATUS_FILE_ERROR,
					"while opening destination file: " + exc, exc);
			}
        }
        if (Constants.LOGV) {
            Log.v(Constants.TAG, "writing " + mInfo.mUri + " to " + state.mFilename);
        }

        updateDatabaseFromHeaders(state);
        // check connectivity again now that we know the total size
        checkConnectivity();
    }

    /**
     * Update necessary database fields based on values of HTTP response headers
     * that have been read.
     */
    private void updateDatabaseFromHeaders(State state) {
        mInfo.mETag = state.mHeaderETag;
        mDB.updateDownload(mInfo);
    }

    /**
     * Read headers from the HTTP response and store them into local state.
     */
    private void readResponseHeaders(State state, HttpURLConnection connection) throws StopRequest {
        String header = connection.getHeaderField("Content-Disposition");
        if (header != null) {
			state.mHeaderContentDisposition = header;
        }
		header = connection.getHeaderField("Content-Location");
        if (header != null) {
			state.mHeaderContentLocation = header;
        }
        header = connection.getHeaderField("ETag");
        if (header != null) {
			state.mHeaderETag = header;
        }
        String headerTransferEncoding = null;
        header = connection.getHeaderField("Transfer-Encoding");
        if (header != null) {
            headerTransferEncoding = header;
        }
		header = connection.getHeaderField("Content-Type");
        if (header != null) {
			if (!header.equals("application/vnd.android.obb")) {
                throw new StopRequest(DownloaderService.STATUS_FILE_DELIVERED_INCORRECTLY,
                        "file delivered with incorrect Mime type");
            }
        }

        if (headerTransferEncoding == null) {
            header = connection.getHeaderField("Content-Length");
            if (header != null) {
				state.mHeaderContentLength = header;
                // this is always set from Market
                long contentLength = Long.parseLong(state.mHeaderContentLength);
                if (contentLength != -1 && contentLength != mInfo.mTotalBytes) {
                    // we're most likely on a bad wifi connection -- we should
                    // probably
                    // also look at the mime type --- but the size mismatch is
                    // enough
                    // to tell us that something is wrong here
                    Log.e(Constants.TAG, "Incorrect file size delivered.");
                }
            }
        } else {
            // Ignore content-length with transfer-encoding - 2616 4.4 3
            if (Constants.LOGVV) {
                Log.v(Constants.TAG,
                        "ignoring content-length because of xfer-encoding");
            }
        }
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Content-Disposition: " + state.mHeaderContentDisposition);
            Log.v(Constants.TAG, "Content-Length: " + state.mHeaderContentLength);
            Log.v(Constants.TAG, "Content-Location: " + state.mHeaderContentLocation);
            Log.v(Constants.TAG, "ETag: " + state.mHeaderETag);
            Log.v(Constants.TAG, "Transfer-Encoding: " + headerTransferEncoding);
        }

		if (state.mHeaderContentLength == null && !"chunked".equalsIgnoreCase(headerTransferEncoding)) {
            throw new StopRequest(DownloaderService.STATUS_HTTP_DATA_ERROR,
                    "can't know size of download, giving up");
        }
    }

    /**
     * Check the HTTP response status and handle anything unusual (e.g. not 200/206).
	 * @return true if retry download
     */
    private boolean handleExceptionalStatus(State state, HttpURLConnection connection) throws StopRequest {
        int statusCode = getResponseCode(connection);
        if (statusCode == HttpURLConnection.HTTP_UNAVAILABLE && mInfo.mNumFailed < Constants.MAX_RETRIES) {
            handleServiceUnavailable(state, connection);
        }
        if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP ||
				statusCode == HttpURLConnection.HTTP_SEE_OTHER || statusCode == HTTP_TEMPORARY_REDIRECT) {
			if (handleRedirect(state, connection)) {
				return true;
			}
        }

        int expectedStatus = state.mContinuingDownload ? HttpURLConnection.HTTP_PARTIAL : HttpURLConnection.HTTP_OK;
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, statusCode);
        } else {
            // no longer redirected
            state.mRedirectCount = 0;
        }
        return false;
    }

	private int getResponseCode(HttpURLConnection connection) throws StopRequest {
		try {
			return connection.getResponseCode();
		} catch (IOException e) {
			throw new StopRequest(DownloaderService.STATUS_UNKNOWN_ERROR,
				"while getting response code: " + e, e);
		}
	}

	/**
     * Handle a status that we don't know how to deal with properly.
     */
    private void handleOtherStatus(State state, int statusCode) throws StopRequest {
        int finalStatus;
        if (DownloaderService.isStatusError(statusCode)) {
            finalStatus = statusCode;
        } else if (isStatusRedirection(statusCode)) {
            finalStatus = DownloaderService.STATUS_UNHANDLED_REDIRECT;
        } else if (state.mContinuingDownload && statusCode == DownloaderService.STATUS_SUCCESS) {
            finalStatus = DownloaderService.STATUS_CANNOT_RESUME;
        } else {
            finalStatus = DownloaderService.STATUS_UNHANDLED_HTTP_CODE;
        }
        throw new StopRequest(finalStatus, "http error " + statusCode);
    }

	private boolean isStatusRedirection(int status) {
		return status >= 300 && status < 400;
	}

	/**
     * Handle a 3xx redirect status.
	 * @return true if retry download
     */
    private boolean handleRedirect(State state, HttpURLConnection connection) throws StopRequest {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "got HTTP redirect " + getResponseCode(connection));
        }
        if (state.mRedirectCount >= Constants.MAX_REDIRECTS) {
            throw new StopRequest(DownloaderService.STATUS_TOO_MANY_REDIRECTS, "too many redirects");
        }
        String location = connection.getHeaderField("Location");
        if (location == null) {
            return false;
        }
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Location :" + location);
        }

        String newUri;
        try {
            newUri = new URI(mInfo.mUri).resolve(new URI(location)).toString();
        } catch (URISyntaxException ignored) {
            if (Constants.LOGV) {
                Log.d(Constants.TAG, "Couldn't resolve redirect URI " + location + " for " + mInfo.mUri);
            }
            throw new StopRequest(DownloaderService.STATUS_HTTP_DATA_ERROR,
                    "Couldn't resolve redirect URI");
        }
        ++state.mRedirectCount;
        state.mRequestUri = newUri;
        return true;
    }

    /**
     * Add headers for this download to the HTTP request to allow for resume.
     */
    private void addRequestProperty(State state, HttpURLConnection connection) {
        if (state.mContinuingDownload) {
            if (state.mHeaderETag != null) {
				connection.setRequestProperty("If-Match", state.mHeaderETag);
            }
			connection.setRequestProperty("Range", "bytes=" + state.mBytesSoFar + "-");
        }
    }

    /**
     * Handle a 503 Service Unavailable status by processing the Retry-After
     * header.
     */
    private void handleServiceUnavailable(State state, HttpURLConnection connection) throws StopRequest {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "got HTTP response code " + HttpURLConnection.HTTP_UNAVAILABLE);
        }
        state.mCountRetry = true;
        String retryAfter = connection.getHeaderField("Retry-After");
        if (retryAfter != null) {
            try {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "Retry-After :" + retryAfter);
                }
                state.mRetryAfter = Integer.parseInt(retryAfter);
                if (state.mRetryAfter < 0) {
                    state.mRetryAfter = 0;
                } else {
                    if (state.mRetryAfter < Constants.MIN_RETRY_AFTER) {
                        state.mRetryAfter = Constants.MIN_RETRY_AFTER;
                    } else if (state.mRetryAfter > Constants.MAX_RETRY_AFTER) {
                        state.mRetryAfter = Constants.MAX_RETRY_AFTER;
                    }
                    state.mRetryAfter += Helpers.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1);
                    state.mRetryAfter *= 1000;
                }
            } catch (NumberFormatException ignored) {
                // ignored - retryAfter stays 0 in this case.
            }
        }
        throw new StopRequest(DownloaderService.STATUS_WAITING_TO_RETRY,
                "got 503 Service Unavailable, will retry later");
    }

	private int getFinalStatusForHttpError(State state) {
        if (mService.getNetworkAvailabilityState(mDB) != DownloaderService.NETWORK_OK) {
            return DownloaderService.STATUS_WAITING_FOR_NETWORK;
        }
		if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
			state.mCountRetry = true;
			return DownloaderService.STATUS_WAITING_TO_RETRY;
		}
		Log.w(Constants.TAG, "reached max retries for " + mInfo.mNumFailed);
		return DownloaderService.STATUS_HTTP_DATA_ERROR;
	}

    /**
     * Prepare the destination file to receive data. If the file already exists,
     * we'll set up appropriately for resumption.
     */
    private void setupDestinationFile(State state) throws StopRequest {
		// only true if we've already run a thread for this download
        if (state.mFilename != null) {
            if (!Helpers.isFilenameValid(state.mFilename)) {
                // this should never happen
                throw new StopRequest(DownloaderService.STATUS_FILE_ERROR,
                        "found invalid internal destination filename");
            }
            // We're resuming a download that got interrupted
            File f = new File(state.mFilename);
            if (f.exists()) {
                long fileLength = f.length();
                if (fileLength == 0) {
                    // The download hadn't actually started, we can restart from
                    // scratch
                    f.delete();
                    state.mFilename = null;
                } else if (mInfo.mETag == null) {
                    // This should've been caught upon failure
                    f.delete();
                    throw new StopRequest(DownloaderService.STATUS_CANNOT_RESUME,
                            "Trying to resume a download that can't be resumed");
                } else {
                    // All right, we'll be able to resume this download
                    try {
                        state.mStream = new FileOutputStream(state.mFilename, true);
                    } catch (FileNotFoundException exc) {
                        throw new StopRequest(DownloaderService.STATUS_FILE_ERROR,
                                "while opening destination for resuming: " + exc, exc);
                    }
					state.mBytesSoFar = (int) fileLength;
                    if (mInfo.mTotalBytes != -1) {
						state.mHeaderContentLength = Long.toString(mInfo.mTotalBytes);
                    }
					state.mHeaderETag = mInfo.mETag;
					state.mContinuingDownload = true;
                }
            }
        }

        if (state.mStream != null) {
            closeDestination(state);
        }
    }

	private void updateDownloadDatabase(int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData) {
        mInfo.mStatus = status;
        mInfo.mRetryAfter = retryAfter;
        mInfo.mRedirectCount = redirectCount;
        mInfo.mLastMod = System.currentTimeMillis();
        if (!countRetry) {
            mInfo.mNumFailed = 0;
        } else if (gotData) {
            mInfo.mNumFailed = 1;
        } else {
            mInfo.mNumFailed++;
        }
        mDB.updateDownload(mInfo);
    }

}
