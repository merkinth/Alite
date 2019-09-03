package de.phbouillon.android.games.alite;

public interface Loggable {
	void debug(String title, String message);
	void warning(String title, String message);
	void error(String title, String message);
	void error(String title, String message, Throwable cause);

	String getGlVendorData(int name);
	String getMemoryData();
	String getDeviceInfo();
}
