package de.phbouillon.android.framework.impl;

/* Alite - Discover the Universe on your Favorite Android Device
 * Copyright (C) 2015 Philipp Bouillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful and
 * fun, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * http://http://www.gnu.org/licenses/gpl-3.0.txt.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.games.alite.AliteLog;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PluginManagerImpl implements PluginManager {

	private static final String MEDIA_TYPE_GOOGLE_DRIVE_FOLDER = "application/vnd.google-apps.folder";

	private Context context;
	private FileIO fileIO;
	private String rootDriveFolder;
	private String applicationName;
	private IDownloaderClient uiNotifier;

	private DownloadProgressInfo downloadProgressInfo;
	private Timer downloadTimer;
	private Worker worker;
	private int updateMode;
	private int downloadState;

	public PluginManagerImpl(Context context, FileIO fileIO, String rootDriveFolder, String applicationName, IDownloaderClient uiNotifier) {
		this.context = context;
		this.fileIO = fileIO;
		this.rootDriveFolder = rootDriveFolder;
		this.applicationName = applicationName;
		this.uiNotifier = uiNotifier;
	}

	@Override
	public void checkPluginFiles(String pluginDirectory, String localeDirectory, String pluginsMetaFile, int updateMode) {
		this.updateMode = updateMode;
		worker = new Worker(deltaTime -> new PluginModel(fileIO, pluginDirectory + pluginsMetaFile).
			setServerFiles(getPluginsList()).checkPluginFiles(this, new String[] { localeDirectory, pluginDirectory }));
		worker.execute();
	}

	private Map<String, Plugin> getPluginsList() {
		AliteLog.d("GoogleDrive", "Getting list of plugins from Google Drive");
		try {
			Drive service = getDriveService();
			FileList folderList = service.files().list()
				.setQ("'" + rootDriveFolder+ "' in parents and mimeType = '" + MEDIA_TYPE_GOOGLE_DRIVE_FOLDER + "' and trashed=false")
				.setSpaces("drive")
				.setFields("nextPageToken, files(id, name)")
				.setPageSize(1000).execute();
			Map<String,String> folders = new HashMap<>();
			String folderIds = "";
			for (com.google.api.services.drive.model.File f : folderList.getFiles()) {
				folderIds += " or '" + f.getId() + "' in parents";
				folders.put(f.getId(), f.getName());
			}
			if (folders.isEmpty()) {
				return null;
			}

			AliteLog.d("GoogleDrive", "Getting list of folders " + Arrays.toString(folders.values().toArray()));
			Drive.Files.List request = service.files().list()
				.setQ(folderIds.substring(4) + " and trashed=false")
				.setSpaces("drive")
				.setFields("nextPageToken, files(id, name, size, modifiedTime, description, parents)")
				.setPageSize(1000);

			Map<String, Plugin> result = new HashMap<>();
			do {
				try {
					FileList files = request.execute();
					AliteLog.d("GoogleDrive", "Count of plugin files on server = " + files.getFiles().size());
					for (com.google.api.services.drive.model.File f : files.getFiles()) {
						result.put(f.getName(), new Plugin(f.getId(), f.getParents().isEmpty() ? "" : folders.get(f.getParents().get(0)),
							f.getName(), f.getSize(), f.getModifiedTime().getValue(), f.getDescription(), "", "", 0));
					}
					request.setPageToken(files.getNextPageToken());
				} catch (IOException e) {
					AliteLog.e("GoogleDrive", "Error during getting the file list.", e);
					request.setPageToken(null);
				}
			} while (request.getPageToken() != null && !request.getPageToken().isEmpty());
			return result;
		} catch (IOException e) {
			AliteLog.e("GoogleDrive", "Google Drive is currently unavailable.", e);
		}
		return null;
	}

	private Drive getDriveService() throws IOException {
		GoogleCredential credential = GoogleCredential.fromStream(context.getAssets().open("Alite2020.json"))
			.createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY));
		return new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
			.setApplicationName(applicationName)
			.build();
	}

	@Override
	public boolean updateFile(String fileId, long size, String destinationFolder, String destinationFilename) {
		if (updateMode == PluginManager.UPDATE_MODE_CHECK_FOR_UPDATES_ONLY ||
			updateMode != PluginManager.UPDATE_MODE_AUTO_UPDATE_AT_ANY_TIME && !isWifiConnected()) {
			return false;
		}
		return getFileFromDrive(fileId, size, destinationFolder, destinationFilename);
	}

	@Override
	public void downloadFile(String fileId, long size, String destinationFolder, String destinationFilename) {
		worker = new Worker(deltaTime -> getFileFromDrive(fileId, size, destinationFolder, destinationFilename));
		worker.execute();
	}

	private boolean getFileFromDrive(String fileId, long size, String destinationFolder, String destinationFilename) {
		AliteLog.d("GoogleDrive", "Starting to download file to " + destinationFilename);
		try {
			Drive service = getDriveService();
			fileIO.mkDir(destinationFolder);
			destinationFilename = destinationFolder + File.separatorChar + destinationFilename;
			OutputStream outputStream = fileIO.writeFile(destinationFilename + ".tmp");
			Drive.Files.Get get = service.files().get(fileId);
			downloadProgressInfo = new DownloadProgressInfo(size, 0, -1, 0);
			downloadTimer = new Timer();
			get.getMediaHttpDownloader().setChunkSize(AliteLog.MB).setProgressListener(worker);
			get.executeMediaAndDownloadTo(outputStream);
			outputStream.close();
			new java.io.File(fileIO.getFileName(destinationFilename + ".tmp")).renameTo(
				new java.io.File(fileIO.getFileName(destinationFilename)));
			AliteLog.d("GoogleDrive", "File downloaded to " + destinationFilename + " successfully.");
			downloadState = IDownloaderClient.STATE_COMPLETED;
			return true;
		} catch (IOException e) {
			AliteLog.e("GoogleDrive", "Error during downloading file to " + destinationFilename, e);
			downloadState = IDownloaderClient.STATE_FAILED;
			return false;
		}
	}

	private boolean isWifiConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			AliteLog.d("GoogleDrive", "May not connected to Wi-Fi.");
			return false;
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		boolean wifi = networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
		AliteLog.d("GoogleDrive", wifi ? "Connected to Wi-Fi." : "Not connected to Wi-Fi.");
		return wifi;
	}

	private void updateDownloadProgressInfo(long totalBytesSoFar) {
		float timePassed = downloadTimer.getPassedMillis();
		downloadProgressInfo.mOverallProgress = totalBytesSoFar;
		downloadProgressInfo.mTimeRemaining = (long) (timePassed * downloadProgressInfo.mOverallTotal / totalBytesSoFar - timePassed);
		downloadProgressInfo.mCurrentSpeed = totalBytesSoFar / timePassed;
	}

	@SuppressLint("StaticFieldLeak")
	private class Worker extends AsyncTask<Void, DownloadProgressInfo, Object> implements MediaHttpDownloaderProgressListener {
		private IMethodHook backgroundTask;

		Worker(IMethodHook backgroundTask) {
			this.backgroundTask = backgroundTask;
		}

		@Override
		protected DownloadProgressInfo doInBackground(Void... params) {
			backgroundTask.execute(0);
			return null;
		}
		@Override
		protected void onProgressUpdate(DownloadProgressInfo... values) {
			super.onProgressUpdate(values);
			if (uiNotifier != null) {
				uiNotifier.onDownloadProgress(values[0]);
			}
		}
		@Override
		public void progressChanged(MediaHttpDownloader downloader) {
			updateDownloadProgressInfo(downloader.getNumBytesDownloaded());
			publishProgress(downloadProgressInfo);
		}

		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			if (uiNotifier != null) {
				uiNotifier.onDownloadStateChanged(downloadState);
			}
		}
	}

}
