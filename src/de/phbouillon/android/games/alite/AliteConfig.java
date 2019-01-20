package de.phbouillon.android.games.alite;

public class AliteConfig {
	public static final boolean HAS_EXTENSION_APK = true;
	public static final int EXTENSION_FILE_VERSION = 2201;
	static final long EXTENSION_FILE_LENGTH = 342663233L;

	public static final String GAME_NAME = "Alite 2020";
	private static final String PRG_VERSION = "1.5.8";
	public static final String VERSION_STRING = PRG_VERSION + " " + (HAS_EXTENSION_APK ? "OBB" : "SFI");
	public static final String ALITE_WEBSITE = "http://alite.mobi";

	public static final int SCREEN_WIDTH = 1920;
	public static final int SCREEN_HEIGHT = 1080;
	public static final int NAVIGATION_BAR_SIZE = 200;
	public static final int DESKTOP_WIDTH = SCREEN_WIDTH - NAVIGATION_BAR_SIZE;

	public static final int     ALITE_INTRO_B1920      = -1;//R.raw.alite_intro_b1920;
	public static final int     ALITE_INTRO_B1280      = -1;//R.raw.alite_intro_b1280;
	public static final int     ALITE_INTRO_B640       = -1;//R.raw.alite_intro_b640;
	public static final int     ALITE_INTRO_B320       = -1;//R.raw.alite_intro_b320;
	public static final int     ALITE_INTRO_288        = -1;//R.raw.alite_intro_288;
	public static final int     ALITE_INTRO_B240       = -1;//R.raw.alite_intro_b240;
}
