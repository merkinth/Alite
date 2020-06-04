package de.phbouillon.android.games.alite;

public class TestLogger implements Loggable {
	@Override
	public void debug(String title, String message) {
		System.out.println("[DEBUG " + title + "] " + message);
	}

	@Override
	public void warning(String title, String message) {
		System.out.println("[WARNING " + title + "] " + message);
	}

	@Override
	public void error(String title, String message) {
		System.out.println("[ERROR " + title + "] " + message);
	}

	@Override
	public void error(String title, String message, Throwable cause) {
		System.out.println("[ERROR " + title + "] " + message + "\n" + cause.getMessage());
	}

	@Override
	public String getGlVendorData(int name) {
		return null;
	}

	@Override
	public String getMemoryData() {
		return null;
	}

	@Override
	public String getDeviceInfo() {
		return null;
	}
}
