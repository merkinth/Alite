package de.phbouillon.android.framework;

public interface PluginManager {

	int UPDATE_MODE_NO_UPDATE = 0;
	int UPDATE_MODE_CHECK_FOR_UPDATES_ONLY = 1;
	int UPDATE_MODE_AUTO_UPDATE_AT_ANY_TIME = 2;
	int UPDATE_MODE_AUTO_UPDATE_OVER_WIFI_ONLY = 3;

	boolean updateFile(String fileId, long size, String destinationFilename);
	void checkPluginFiles(String pluginDirectory, String localeDirectory, String pluginsMetaFile, int updateMode);
	void downloadFile(String fileId, long size, String destinationFilename);
}
