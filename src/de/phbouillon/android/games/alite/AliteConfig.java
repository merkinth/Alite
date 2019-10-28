package de.phbouillon.android.games.alite;

public class AliteConfig {
	public static final boolean HAS_EXTENSION_APK = true;
	static final long EXTENSION_FILE_LENGTH = 206153281L;

	public static final String GAME_NAME = "Alite 2020";
	public static final String VERSION_STRING = BuildConfig.VERSION_NAME + " " + (HAS_EXTENSION_APK ? "OBB" : "SFI");
	public static final String ALITE_WEBSITE = "https://alite2020.iftopic.com"; // see also manifest file if change
	public static final String ALITE_MAIL = "alite.crash.report@gmail.com";
	public static final String ROOT_DRIVE_FOLDER = "1OtXzUbeHWrvN9j_JolgIMKEODHSQCmEK";

	public static final int SCREEN_WIDTH = 1920;
	public static final int SCREEN_HEIGHT = 1080;
	public static final int NAVIGATION_BAR_SIZE = 200;
	public static final int DESKTOP_WIDTH = SCREEN_WIDTH - NAVIGATION_BAR_SIZE;

	public static final int     ALITE_INTRO_B1920      = -1;//R.raw.alite_intro_b1920;
}
