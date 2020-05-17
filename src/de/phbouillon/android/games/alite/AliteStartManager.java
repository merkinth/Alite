package de.phbouillon.android.games.alite;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Messenger;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.vending.expansion.downloader.*;

import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.PluginManager;
import de.phbouillon.android.framework.PluginModel;
import de.phbouillon.android.framework.impl.PluginManagerImpl;
import de.phbouillon.android.framework.impl.AndroidFileIO;
import de.phbouillon.android.games.alite.io.AliteDownloaderService;
import de.phbouillon.android.games.alite.io.AliteFiles;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.oxp.OXPParser;
import de.phbouillon.android.games.alite.screens.canvas.PluginsScreen;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

public class AliteStartManager extends Activity implements IDownloaderClient {
	static final int ALITE_RESULT_CLOSE_ALL = 78615265;

	public static final String ALITE_STATE_FILE = "current_state.dat";

	private IStub downloaderClientStub;
	private String currentStatus = "Idle";
	private static FileIO fileIO;
	private IMethodHook onTaskCompleted;
	private boolean pluginUpdateCheck;

	private class ErrorHook implements IMethodHook {
		private static final long serialVersionUID = -3231920982342356254L;

		@Override
		public void execute(float deltaTime) {
			setContentView(R.layout.activity_start_manager);
			((TextView) findViewById(R.id.downloadTextView)).setText(StringUtil.format(L.string(R.string.obb_download_error),
				AliteConfig.GAME_NAME, AliteConfig.ALITE_WEBSITE));
		}
	}

	private boolean expansionFilesDelivered() {
	    String fileName = Helpers.getExpansionAPKFileName(this, true, BuildConfig.VERSION_CODE);
		File[] oldOBBs = fileIO.getFiles(Helpers.getSaveFilePath(this), ".*\\.obb");
		if (oldOBBs != null) {
			for (File obb : oldOBBs) {
				if (!fileName.equals(obb.getName())) {
					obb.delete();
					AliteLog.d("Delete old OBB", "Old OBB '" + obb.getName() + "' deleted.");
				}
			}
		}

		File fileForNewFile = new File(Helpers.generateSaveFileName(this, fileName));
	    AliteLog.d("Check for OBB", "OBB exists? " + fileForNewFile.getAbsolutePath());
	    return Helpers.doesFileExist(this, fileName, AliteConfig.EXTENSION_FILE_LENGTH, false);
	}

	private void setStatus(String status) {
		if (currentStatus.equals(status)) {
			return;
		}
		((TextView) findViewById(R.id.statusTextView)).setText(status);
		currentStatus = status;
	}

	private void checkDownload() {
		if (!expansionFilesDelivered()) {
			AliteLog.d("File does not exist!", "Expansion file does not exist. Downloading...");
			Intent notifierIntent = new Intent(this, getClass());
			notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			try {
				onTaskCompleted = deltaTime -> mountOBB();
				if (DownloaderClientMarshaller.startDownloadServiceIfRequired(this,
						PendingIntent.getActivity(this, 0, notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT),
						AliteDownloaderService.class) != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
					downloaderClientStub = DownloaderClientMarshaller.CreateStub(this, AliteDownloaderService.class);
					setContentView(R.layout.activity_start_manager);
					return;
				}
			} catch (NameNotFoundException e) {
				setStatus(L.string(R.string.notification_download_error, e.getMessage()));
				AliteLog.e("Error while Downloading Expansions", "Name not Found: " + e.getMessage(), e);
			}
		}
		mountOBB();
	}

	private void mountOBB() {
		AliteFiles.performMount(this, new IMethodHook() {
			private static final long serialVersionUID = -6200281383445218241L;

			@Override
			public void execute(float deltaTime) {
				upgradeAndLoadPlugins();
			}
		}, new ErrorHook());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (fileIO == null) {
			fileIO = new AndroidFileIO(this);
		}
		AliteLog.initialize(fileIO);
		AliteLog.d("AliteStartManager.onCreate", "onCreate begin");
		final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
			AliteLog.e("Uncaught Exception (AliteStartManager)",
				"Message: " + (paramThrowable == null ? "<null>" : paramThrowable.getMessage()), paramThrowable);
			if (oldHandler != null) {
				oldHandler.uncaughtException(paramThread, paramThrowable);
			} else {
				System.exit(2);
			}
		});
		AliteLog.d("Alite Start Manager", "Alite Start Manager has been created.");
		Settings.load(fileIO);
		L.getInstance(this).setLocale(Settings.locale);

		if (AliteConfig.HAS_EXTENSION_APK) {
			checkDownload();
		} else {
			upgradeAndLoadPlugins();
		}
		AliteLog.d("AliteStartManager.onCreate", "onCreate end");
	}

//	private void checkSHA() {
//	    String fileName = Helpers.getExpansionAPKFileName(this, true, BuildConfig.VERSION_CODE);
//	    AliteLog.e("SHA-Checksum", "Start SHA-Checksum calculation");
//	    File fileForNewFile = new File(Helpers.generateSaveFileName(this, fileName));
//	    AliteLog.e("SHA-Checksum", "Checksum for obb: " + FileUtils.computeSHAString(fileForNewFile));
//	}

	private void upgradeAndLoadPlugins() {
		pluginUpdateCheck = true;
		setContentView(R.layout.activity_start_manager);
		findViewById(R.id.downloadHeader).setVisibility(View.INVISIBLE);
		setStatus(L.string(R.string.notification_download_working));
		if (Settings.extensionUpdateMode == PluginManager.UPDATE_MODE_NO_UPDATE) {
			((TextView) findViewById(R.id.downloadTextView)).setText(L.string(R.string.notification_plugins_loading));
			AliteLog.d("Alite Start Manager", "Updating plugins is disabled");
			new PluginModel(fileIO, PluginModel.DIRECTORY_PLUGINS + PluginsScreen.PLUGINS_META_FILE).
				checkPluginFiles(null, new String[]{PluginModel.DIRECTORY_LOCALES, PluginModel.DIRECTORY_PLUGINS});
		} else {
			AliteLog.d("Alite Start Manager", "Checking plugins");
			((TextView) findViewById(R.id.downloadTextView)).setText(L.string(R.string.notification_plugin_checking));
			new PluginManagerImpl(this, fileIO, AliteConfig.ROOT_DRIVE_FOLDER, AliteConfig.GAME_NAME, this).
				checkPluginFiles(PluginModel.DIRECTORY_PLUGINS, PluginModel.DIRECTORY_LOCALES,
					PluginsScreen.PLUGINS_META_FILE, Settings.extensionUpdateMode);
		}
		onTaskCompleted = deltaTime -> startGame();
		((TextView) findViewById(R.id.downloadTextView)).setText(L.string(R.string.notification_plugins_loading));
		new PluginLoader(this, onTaskCompleted).execute(getResources().getAssets());
	}

	private void startGame() {
		AliteLog.d("Alite Start Manager", "Loading Alite State");
		loadCurrentGame();
	}

	static void loadLocaleDependentPlugins() {
		new PluginLoader(null, null).loadLocaleDependentPlugins();
	}

	private static class PluginLoader extends AsyncTask<AssetManager, String, Object> {
		@SuppressLint("StaticFieldLeak")
		private Activity activity;
		private IMethodHook onTaskCompleted;
		private boolean countMode;
		private int pluginTotal;
		private int pluginProgress;

		PluginLoader(Activity activity, IMethodHook onTaskCompleted) {
			this.activity = activity;
			this.onTaskCompleted = onTaskCompleted;
		}

		@Override
		protected Object doInBackground(AssetManager... assetManager) {
			countMode = true;
			countOrLoadPlugins(assetManager);
			countMode = false;
			countOrLoadPlugins(assetManager);
			return null;
		}

		private void countOrLoadPlugins(AssetManager[] assetManager) {
			if (assetManager.length > 0) {
				loadBundledPlugins(assetManager[0]);
				loadExternalPlugins(false);
			}
			loadLocaleDependentPlugins();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (activity != null) {
				((TextView) activity.findViewById(R.id.downloadTextView)).setText(L.string(R.string.notification_plugin_loading, values[0]));
				int progressInPercent = pluginTotal == 0 ? 100 : 100 * pluginProgress / pluginTotal;
				((TextView) activity.findViewById(R.id.downloadProgressPercentTextView)).setText(
					StringUtil.format("%d%%", progressInPercent));
				((ProgressBar) activity.findViewById(R.id.downloadProgressBar)).setProgress(progressInPercent);
			}
		}

		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			if (onTaskCompleted != null) {
				onTaskCompleted.execute(0);
			}
		}

		private void loadBundledPlugins(AssetManager assetManager) {
			try {
				String[] plugins = assetManager.list("plugins");
				if (plugins == null) {
					return;
				}
				if (countMode) {
					pluginTotal += plugins.length;
					return;
				}
				AliteLog.d("loadBundledPlugins", "Started");
				for (String pluginName : plugins) {
					new OXPParser(PluginModel.DIRECTORY_PLUGINS + pluginName,
						fileName -> assetManager.open(PluginModel.DIRECTORY_PLUGINS + pluginName +
							File.separatorChar + fileName),
						directory -> assetManager.list(PluginModel.DIRECTORY_PLUGINS + pluginName +
							File.separatorChar + directory)).isPlugged();
					pluginProgress++;
					publishProgress(pluginName);
				}
			} catch (IOException e) {
				AliteLog.e("Bundled plugin load error", "Failed to load bundled plugin.", e);
			}
		}

		// Load from plugin directory of the files of the currently selected locale
		void loadLocaleDependentPlugins() {
			SpaceObjectFactory.getInstance().clearLocaleDependentProperties();
			String[] plugins = L.getInstance().list(PluginModel.DIRECTORY_PLUGINS);
			if (plugins == null) {
				return;
			}
			if (countMode) {
				pluginTotal += plugins.length;
				loadExternalPlugins(true);
				return;
			}
			AliteLog.d("loadLocaleDependentPlugins", "Started");
			for (String pluginName : plugins) {
				try {
					new OXPParser(PluginsScreen.DIRECTORY_PLUGINS + pluginName,
							fileName -> L.raw(PluginsScreen.DIRECTORY_PLUGINS + pluginName + File.separatorChar, fileName)).
						setLocaleDependent().isPlugged();
					pluginProgress++;
					publishProgress(pluginName);
				} catch (IOException e) {
					AliteLog.e("Plugin localization error", "Failed to load localization of plugin.", e);
				}
			}
			loadExternalPlugins(true);
		}

		private void loadExternalPlugins(boolean localeDependent) {
			String directory;
			String fileNamePattern;
			if (localeDependent) {
				directory = PluginModel.DIRECTORY_LOCALES;
				fileNamePattern = L.getLanguageCountry(L.getInstance().getCurrentLocale()) + "_.*\\.zip";
			} else {
				directory = PluginModel.DIRECTORY_PLUGINS;
				fileNamePattern = ".*\\.oxz|.*\\.zip|.*\\.oxp";
			}
			AliteLog.d("loadExternalPlugins" + (countMode ? " (countMode)" : ""),
				"Search '" + fileNamePattern + "' in directory '" + directory + "'");
			File[] plugins = fileIO.getFiles(directory, fileNamePattern);
			if (plugins == null || plugins.length == 0) {
				AliteLog.d("loadExternalPlugins", "plugin not found.");
				return;
			}
			if (countMode) {
				pluginTotal += plugins.length;
				return;
			}
			List<OXPParser> pendingPlugins = new ArrayList<>();
			List<OXPParser> installedPlugins = new ArrayList<>();
			for (File f : plugins) {
				try {
					final OXPParser plugin = new OXPParser(fileIO, directory + f.getName(), installedPlugins);
					if (localeDependent) {
						plugin.setLocaleDependent();
					}
					if (!plugin.isPluginFile()) {
						// localization of program itself is already set by button languages on options screen
						continue;
					}
					if (plugin.isPlugged()) {
						pluginProgress++;
						publishProgress(f.getName());
						installedPlugins.add(plugin);
					} else {
						pendingPlugins.add(plugin);
					}
				} catch (IOException e) {
					AliteLog.e("Plugin load error", "Failed to load plugin " + f.getPath(), e);
				}
			}
			if (pendingPlugins.isEmpty()) {
				return;
			}
			boolean handled = false;
			do {
				for (OXPParser plugin : pendingPlugins) {
					try {
						if (plugin.isPlugged()) {
							publishProgress(plugin.getPluginName());
							installedPlugins.add(plugin);
							pendingPlugins.remove(plugin);
							handled = true;
						}
					} catch (IOException e) {
						pendingPlugins.remove(plugin);
						AliteLog.e("Plugin load error", "Failed to load plugin " + plugin.getIdentifier(), e);
					}
				}
			} while (handled);
		}
	}

	private void startAliteIntro() {
		AliteLog.d("Alite Start Manager", "Starting INTRO!");
		Intent intent = new Intent(this, AliteIntro.class);
		intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
		startActivityForResult(intent, 0);
		finish();
	}

	private void startAlite() {
		AliteLog.d("Alite Start Manager", "Starting Alite.");
		Intent intent = new Intent(this, Alite.class);
		intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
		startActivityForResult(intent, 0);
		finish();
	}

	private void loadCurrentGame() {
		try {
			if (fileIO.exists(ALITE_STATE_FILE)) {
				AliteLog.d("Alite Start Manager", "Alite state file exists. Opening it.");
				// State file exists, so open the first byte to check if the
				// Intro Activity or the Game Activity must be started.
				byte[] b = fileIO.readPartialFileContents(ALITE_STATE_FILE, 1);
				if (b == null || b.length != 1) {
					// Fallback in case of an error
					AliteLog.d("Alite Start Manager", "Reading screen code failed. b == " +
						(b == null ? "<null>" : b) + " -- " + (b == null ? "<null>" : b.length));
				} else {
					// We used to parse the first byte here, to determine if we want to resume the intro.
					// However, if the game crashes right after the intro (probably because of the
					// Activity change), we don't have a fallback. Hence, we simply prohibit resuming
					// the intro...
					AliteLog.d("Alite Start Manager", "Saved screen code == " + b[0] + " - starting game.");
				}
				startAlite();
				return;
			}
			AliteLog.d("Alite Start Manager", "No state file present: Starting intro.");
		} catch (IOException e) {
			// Default to Intro...
			AliteLog.e("Alite Start Manager", "Exception occurred. Starting intro.", e);
		}
		startAliteIntro();
	}

	@Override
	protected void onPause() {
		AliteLog.d("AliteStartManager.onPause", "onPause begin");
		super.onPause();
		AliteLog.d("AliteStartManager.onPause", "onPause end");
	}

	@Override
	protected void onResume() {
		AliteLog.d("AliteStartManager.onResume", "onResume begin");
		if (null != downloaderClientStub) {
	        downloaderClientStub.connect(this);
	    }
		super.onResume();
		AliteLog.d("AliteStartManager.onResume", "onResume end");
	}

	@Override
	protected void onStop() {
		AliteLog.d("AliteStartManager.onStop", "onStop begin");
		if (null != downloaderClientStub) {
	        downloaderClientStub.disconnect(this);
	    }
		super.onStop();
		AliteLog.d("AliteStartManager.onStop", "onStop end");
	}

	@Override
	public void onServiceConnected(Messenger m) {
		DownloaderServiceMarshaller.CreateProxy(m).onClientUpdated(downloaderClientStub.getMessenger());
	}

	@Override
	public void onDownloadStateChanged(int newState) {
		setStatus(L.string(pluginUpdateCheck ? R.string.notification_download_working :
			Helpers.getDownloaderStringResourceIDFromState(newState)));
		((TextView) findViewById(R.id.downloadProgressPercentTextView)).setText(StringUtil.format("%d%%", 100));
		((ProgressBar) findViewById(R.id.downloadProgressBar)).setProgress(100);
		((TextView) findViewById(R.id.downloadTextView)).setText(L.string(pluginUpdateCheck ?
			R.string.notification_plugin_checking : R.string.notification_download_complete));
	}

	@Override
	public void onDownloadProgress(DownloadProgressInfo progress) {
		pluginUpdateCheck = false;
		int progressInPercent = (int) (progress.mOverallProgress / (float) progress.mOverallTotal * 100.0f);

		((TextView) findViewById(R.id.downloadProgressPercentTextView)).setText(
			StringUtil.format("%d%%", progressInPercent));
		((ProgressBar) findViewById(R.id.downloadProgressBar)).setProgress(progressInPercent);

		((TextView) findViewById(R.id.downloadTextView)).setText(L.string(R.string.notification_downloading_info,
			(int) (progress.mOverallProgress / AliteLog.MB), (int) (progress.mOverallTotal / AliteLog.MB),
			progress.mCurrentSpeed / AliteLog.KB, (int) (progress.mTimeRemaining / 1000.0f)));

		AliteLog.d("Progress", "Current Speed: " + progress.mCurrentSpeed +
			", Overall progress: " + progress.mOverallProgress +
			", Total progress: " + progress.mOverallTotal +
			", Time Remaining: " + progress.mTimeRemaining);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (resultCode == ALITE_RESULT_CLOSE_ALL) {
	    setResult(ALITE_RESULT_CLOSE_ALL);
	    finish();
	  }
	  super.onActivityResult(requestCode, resultCode, data);
	}
}
