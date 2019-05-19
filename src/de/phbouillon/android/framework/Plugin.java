package de.phbouillon.android.framework;

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
