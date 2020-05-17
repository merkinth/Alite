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

import com.google.api.client.util.DateTime;
import de.phbouillon.android.games.alite.AliteLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class PluginModel {
	public static final String DIRECTORY_LOCALES = "locales" + File.separatorChar;
	public static final String DIRECTORY_PLUGINS = "plugins" + File.separator;

	private FileIO fileIO;
	private String metaFileName;

	private Map<String, Plugin> serverFiles;
	private Map<String, Plugin> metaInfo;
	private Map<String, File> localFiles;

	public PluginModel(FileIO fileIO, String metaFileName) {
		this.fileIO = fileIO;
		this.metaFileName = metaFileName;

		metaInfo = readMetaFile();
	}

	public PluginModel setServerFiles(Map<String, Plugin> serverFiles) {
		this.serverFiles = serverFiles;
		return this;
	}

	public List<Plugin> getOrderedListOfPlugins() {
		List<Plugin> pluginList = new ArrayList<>(metaInfo.values());
		Collections.sort(pluginList, (c1, c2) -> c1.filename.compareTo(c2.filename));
		return pluginList;
	}

	private Map<String, Plugin> readMetaFile() {
		Map<String, Plugin> meta = new HashMap<>();
		try {
			JSONObject scheme = new JSONObject(new String(fileIO.readFileContents(metaFileName)));
			JSONArray files = scheme.getJSONArray("items");
			for (int i = 0; i < files.length(); i++) {
				JSONObject file = files.getJSONObject(i);
				meta.put(file.optString("filename"), new Plugin(
					file.optString("fileId"),
					file.optString("folder"),
					file.optString("filename"),
					file.optLong("size"),
					getModifiedTime(file.opt("modifiedTime")),
					file.optString("description"),
					file.optString("status"),
					file.optString("removalReason"),
					getModifiedTime(file.opt("downloadTime"))));
			}
		} catch (IOException | JSONException e) {
			AliteLog.e("Plugin meta info load", "Loading meta info of plugins failed.", e);
		}
		return meta;
	}

	private long getModifiedTime(Object modifiedTime) {
		if (modifiedTime instanceof String) {
			return DateTime.parseRfc3339((String) modifiedTime).getValue();
		}
		return modifiedTime instanceof Long ? (long) modifiedTime : 0;
	}

	public void checkPluginFiles(PluginManager pluginManager, String[] folders) {
		localFiles = new HashMap<>();
		for (String folder : folders) {
			int startCounter = localFiles.size();
			File[] files = fileIO.getFiles(folder, ".*\\.oxz|.*\\.zip|.*\\.oxp");
			if (files != null) {
				for (File f : files) {
					localFiles.put(f.getName(), f);
				}
				AliteLog.d("Plugins checking", "Checking locale files in folder '" + folder +
					"', found " + (localFiles.size() - startCounter) + " file(s).");
			}
		}
		checkLocalFiles(pluginManager);
		checkServerFiles();
		checkMetaFiles();
		saveRefreshedMetaFile();
	}

	/**
	 * Set meta status to installed if file exists locally.
	 * Upgrade local file if there is newer on the server.
	 * File names must be unique among all folders since file name is used for compare without folder name.
	 */
	private void checkLocalFiles(PluginManager pluginManager) {
		for (File l : localFiles.values()) {
			if (serverFiles != null) {
				Plugin s = serverFiles.get(l.getName());
				if (s != null) {
					upgradeFileIfNeeded(pluginManager, s, l);
					continue;
				}
			}
			updateMetaInfo(null, getLastSubPath(l.getParent()), l.getName(), l.length(), l.lastModified(), null, true);
		}
	}

	private String getLastSubPath(String filePath) {
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}


	/**
	 * Refresh meta info based on server info
	 * File names must be unique among all folders since file name is used for compare without folder name.
	 */
	private void checkServerFiles() {
		if (serverFiles == null) {
			return;
		}
		for (Plugin s : serverFiles.values()) {
			updateMetaInfo(s.fileId, s.folder, s.filename, s.size, s.modifiedTime,
				s.description == null ? "" : s.description, localFiles.containsKey(s.filename));
		}
	}

	/**
	 * Remove meta info if file exists neither on server nor locally.
	 */
	private void checkMetaFiles() {
		Iterator<Plugin> i = metaInfo.values().iterator();
		while (i.hasNext()) {
			Plugin m = i.next();
			if (!localFiles.containsKey(m.filename)) {
				// File is in the plugins meta file but neither on server nor in local folder.
				if (serverFiles != null) {
					if (!serverFiles.containsKey(m.filename)) {
						i.remove();
					}
				} else {
					updateMetaInfo(m.fileId, m.folder, m.filename, m.size, m.modifiedTime, m.description, false);
				}
			}
		}
	}

	/**
	 * If server file is newer than the local one try to upgrade.
	 * According to the result add to meta file or update the plugin file info with status upgraded / outdated.
	 * Set description and in case of success the modified time, as well.
	 */
	private void upgradeFileIfNeeded(PluginManager pluginManager, Plugin serverFile, File localFile) {
		Plugin file = metaInfo.get(serverFile.filename);
		if (serverFile.modifiedTime <= localFile.lastModified()) {
			if (file != null) {
				file.description = serverFile.description;
			}
			return;
		}
		boolean upgradeSucceeded = pluginManager.updateFile(serverFile.fileId,
			serverFile.size, getLastSubPath(localFile.getParent()), localFile.getName());
		String newStatus = upgradeSucceeded ? Plugin.META_STATUS_UPGRADED : Plugin.META_STATUS_OUTDATED;
		if (file == null) {
			metaInfo.put(serverFile.filename, new Plugin(serverFile.fileId, serverFile.folder, serverFile.filename,
				serverFile.size, upgradeSucceeded ? serverFile.modifiedTime : localFile.lastModified(),
				serverFile.description, newStatus, null, 0));
		} else {
			file.fileId = serverFile.fileId;
			file.size = serverFile.size;
			if (upgradeSucceeded) {
				file.modifiedTime = serverFile.modifiedTime;
				file.downloadTime = System.currentTimeMillis();
			}
			file.description = serverFile.description;
			file.status = newStatus;
		}
		AliteLog.d("Plugins checking", "Plugin file '" + serverFile.filename + "' is " + newStatus + ".");
	}

	/**
	 * Add plugin file info to meta file with as installed / new according to the file exists locally or not
	 * otherwise update meta info.
	 * Status changes to installed if it is removed currently but file exists locally.
	 * Status changes to removed if it is installed currently but file does not exist locally.
	 */
	private void updateMetaInfo(String fileId, String folder, String filename, long size, long modifiedTime,
			String description, boolean locallyExists) {
		Plugin file = metaInfo.get(filename);
		String newStatus;
		if (file == null) {
			newStatus = locallyExists ? Plugin.META_STATUS_INSTALLED : Plugin.META_STATUS_NEW;
			metaInfo.put(filename, new Plugin(fileId, folder, filename, size, modifiedTime, description, newStatus, null, 0));
		} else {
			if (fileId != null) file.fileId = fileId;
			file.folder = folder;
			file.size = size;
			if (description != null) file.description = description;
			if (Plugin.META_STATUS_REMOVED.equals(file.status) && modifiedTime > file.modifiedTime) {
				newStatus = Plugin.META_STATUS_NEW_OF_REMOVED;
				file.status = newStatus;
			} else if (Plugin.META_STATUS_NEW_OF_REMOVED.equals(file.status) && modifiedTime < file.modifiedTime) {
				newStatus = Plugin.META_STATUS_REMOVED;
				file.status = newStatus;
			} else {
				file.modifiedTime = modifiedTime;
				if (locallyExists && (Plugin.META_STATUS_REMOVED.equals(file.status) || Plugin.META_STATUS_DOWNLOADABLE.equals(file.status) ||
						Plugin.META_STATUS_NEW.equals(file.status)) || !locallyExists && (Plugin.META_STATUS_INSTALLED.equals(file.status) ||
						Plugin.META_STATUS_UPGRADED.equals(file.status) || Plugin.META_STATUS_OUTDATED.equals(file.status))) {
					// Manually added / deleted
					newStatus = locallyExists ? Plugin.META_STATUS_INSTALLED : Plugin.META_STATUS_REMOVED;
					file.status = newStatus;
				} else {
					newStatus = file.status + " (not changed)";
				}
			}
		}
		AliteLog.d("Plugins checking", "Plugin file '" + filename + "' is " + newStatus + ".");
	}

	private void saveRefreshedMetaFile() {
		try {
			JSONArray items = new JSONArray();
			for (Plugin m : metaInfo.values()) {
				DateTime modifiedTime = new DateTime(m.modifiedTime);
				AliteLog.d("Save meta file item", m.fileId + " " + m.filename + " " + m.size + " " +
					modifiedTime + " " + m.description + " " + m.status);
				items.put(new JSONObject()
					.putOpt("fileId", m.fileId)
					.put("folder", m.folder)
					.put("filename", m.filename)
					.put("size", m.size)
					.put("modifiedTime", modifiedTime)
					.putOpt("description", m.description)
					.put("status", m.status)
					.putOpt("removalReason", m.removalReason)
					.putOpt("downloadTime", new DateTime(m.downloadTime))
				);
			}
			fileIO.mkDir(new File(metaFileName).getParent());
			OutputStream metaFile = fileIO.writeFile(metaFileName);
			metaFile.write(new JSONObject().put("items", items).toString(2).getBytes());
			metaFile.close();
		} catch (IOException | JSONException e) {
			AliteLog.e("Plugin meta info save", "Saving meta info of plugins failed", e);
		}
	}

	public void pluginsVisited() {
		for (Plugin m : metaInfo.values()) {
			if (m.status.equals(Plugin.META_STATUS_NEW)) m.status = Plugin.META_STATUS_DOWNLOADABLE;
			else if (m.status.equals(Plugin.META_STATUS_UPGRADED)) m.status = Plugin.META_STATUS_INSTALLED;
		}
		saveRefreshedMetaFile();
	}

	public int countNewAndUpgraded() {
		int count = 0;
		for (Plugin m : metaInfo.values()) {
			if (m.status.equals(Plugin.META_STATUS_NEW) || m.status.equals(Plugin.META_STATUS_UPGRADED) ||
					m.status.equals(Plugin.META_STATUS_OUTDATED)) {
				count++;
			}
		}
		return count;
	}

	public void removePlugin(Plugin plugin, String removalReason) {
		if (fileIO.deleteFile((DIRECTORY_LOCALES.equals(plugin.folder + File.separator) ?
				DIRECTORY_LOCALES : DIRECTORY_PLUGINS) + plugin.filename)) {
			plugin.status = Plugin.META_STATUS_REMOVED;
			plugin.removalReason = removalReason;
			saveRefreshedMetaFile();
		}
	}

	public void downloadedPlugin(Plugin plugin) {
		plugin.status = Plugin.META_STATUS_INSTALLED;
		plugin.removalReason = null;
		plugin.downloadTime = System.currentTimeMillis();
		saveRefreshedMetaFile();
	}

}
