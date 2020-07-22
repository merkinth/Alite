package de.phbouillon.android.framework;

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

public interface PluginManager {

	int UPDATE_MODE_NO_UPDATE = 0;
	int UPDATE_MODE_CHECK_FOR_UPDATES_ONLY = 1;
	int UPDATE_MODE_AUTO_UPDATE_AT_ANY_TIME = 2;
	int UPDATE_MODE_AUTO_UPDATE_OVER_WIFI_ONLY = 3;

	boolean updateFile(String fileId, long size, String destinationFolder, String destinationFilename);
	void checkPluginFiles(String pluginDirectory, String localeDirectory, String pluginsMetaFile, int updateMode);
	void downloadFile(String fileId, long size, String destinationFolder, String destinationFilename);
}
