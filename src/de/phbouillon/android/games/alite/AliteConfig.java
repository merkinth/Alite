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

public class AliteConfig {
	public static final boolean HAS_EXTENSION_APK = true;
	static final long EXTENSION_FILE_LENGTH = 200693825L;

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
