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

public class Plugin {
	public static final String META_STATUS_NEW = "NEW";
	public static final String META_STATUS_DOWNLOADABLE = "DOWNLOADABLE";
	public static final String META_STATUS_INSTALLED = "INSTALLED";
	public static final String META_STATUS_UPGRADED = "UPGRADED";
	public static final String META_STATUS_OUTDATED = "OUTDATED";
	public static final String META_STATUS_REMOVED = "REMOVED";
	public static final String META_STATUS_NEW_OF_REMOVED = "NEW_OF_REMOVED";

	public String fileId;
	public String folder;
	public String filename;
	public long size;
	public long modifiedTime;
	public String description;
	public String status;
	public String removalReason;
	public long downloadTime;

	public Plugin(String fileId, String folder, String filename, long size, long modifiedTime,
		String description, String status, String removalReason, long downloadTime) {
		this.fileId = fileId;
		this.folder = folder;
		this.filename = filename;
		this.size = size;
		this.modifiedTime = modifiedTime;
		this.description = description;
		this.status = status;
		this.removalReason = removalReason;
		this.downloadTime = downloadTime;

		AliteLog.d("Meta file item creation",
			"fileId:" + fileId +
			", folder:" + folder +
			", filename:" + filename +
			", size:" + size +
			", modifiedTime:" + new DateTime(modifiedTime) +
			", description:" + description +
			", status:" + status +
			", removalReason:" + removalReason +
			", downloadTime:" + new DateTime(downloadTime));
	}

}
