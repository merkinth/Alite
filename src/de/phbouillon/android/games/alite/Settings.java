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

import java.io.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.PluginManager;
import de.phbouillon.android.framework.PluginModel;
import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Medal;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;

public class Settings {
	private static final String SETUP_FILE_NAME = ".alite";
	private static final boolean ALWAYS_WRITE_LOG = false;
	public static boolean suppressOnlineLog = false;
	public static final boolean VIS_DEBUG = false;

	public static final int FIRE                   =   0;
	public static final int MISSILE                =   1;
	public static final int ECM                    =   2;
	public static final int RETRO_ROCKETS          =   3;
	public static final int ESCAPE_CAPSULE         =   4;
	public static final int ENERGY_BOMB            =   5;
	public static final int STATUS                 =   6;
	public static final int TORUS                  =   7;
	public static final int HYPERSPACE             =   8;
	public static final int GALACTIC_HYPERSPACE    =   9;
	public static final int CLOAKING_DEVICE        =  10;
	public static final int ECM_JAMMER             =  11;

	public static boolean animationsEnabled = true;
	private static boolean debugActive = false;
	public static boolean logToFile = ALWAYS_WRITE_LOG;
	public static boolean displayFrameRate = false;
	public static boolean displayDockingInformation = false;
	public static boolean memDebug = false;
	static boolean onlineMemDebug = false;
	public static int textureLevel = 1;
	public static int colorDepth = 1;
	public static float alpha = 0.75f;
	public static float controlAlpha = 0.5f;
	public static float[] volumes = {1.0f, 0.5f, 0.5f, 0.5f};
	public static float vibrateLevelOnDamage = 0.5f;
	public static float vibrateLevelOnHit;
	public static ShipControl controlMode = ShipControl.ACCELEROMETER;
	public static int controlPosition = 1;
	public static int introVideoQuality = 255;
	public static boolean unlimitedFuel = false;
	public static boolean enterInSafeZone = false;
	public static boolean disableAttackers = false;
	public static boolean disableTraders = false;
	public static boolean invulnerable = false;
	public static boolean laserDoesNotOverheat = false;
	public static int particleDensity = 2;
	public static boolean tapRadarToChangeView = true;
	public static boolean laserButtonAutoFire = true;
	public static boolean hasBeenPlayedBefore = false;
	public static int[] buttonPosition = new int[12];
    public static boolean engineExhaust = true;
    public static boolean targetBox = true;
    public static int lockScreen = 0;
    public static String colorScheme = ColorScheme.COLOR_SCHEME_CLASSIC;
    public static int laserPowerOverride = 0;
    public static int shieldPowerOverride = 0;
    public static boolean freePath = false;
    public static boolean autoId = true;
    public static boolean reversePitch = false;
    public static boolean flatButtonDisplay = false;
    public static int dockingComputerSpeed = 0;
    public static int difficultyLevel = 3;
	private static int restoredCommanderCount = 0;
    public static boolean navButtonsVisible = true;
	public static Locale locale = Locale.US;
	public static int extensionUpdateMode = PluginManager.UPDATE_MODE_AUTO_UPDATE_OVER_WIFI_ONLY;
	public static int maxGalaxies = GalaxyGenerator.GALAXY_COUNT;
	public static boolean continuousTutorialMode = true;
	public static Set<String> watchedTocEntries = new HashSet<>();

	private Settings() {
	}

	public static File[] getLocaleFiles(FileIO f) {
		return f.getFiles(PluginModel.DIRECTORY_LOCALES, ".*\\.zip");
	}

	public static void load(FileIO files) {
		L.getInstance().loadLocaleList(getLocaleFiles(files));
		resetButtonPosition();
		boolean fastDC = false;
		try(BufferedReader in = new BufferedReader(new InputStreamReader(files.readFile(SETUP_FILE_NAME)))) {
			animationsEnabled = Boolean.parseBoolean(in.readLine());
			String s = in.readLine();
			if (s != null) {
				locale = L.getLocaleOf(s);
			}
			debugActive = Boolean.parseBoolean(in.readLine());
			logToFile = Boolean.parseBoolean(in.readLine()) || ALWAYS_WRITE_LOG;
			displayFrameRate = Boolean.parseBoolean(in.readLine());
			displayDockingInformation = Boolean.parseBoolean(in.readLine());
			memDebug = Boolean.parseBoolean(in.readLine());
			textureLevel = Integer.parseInt(in.readLine());
			colorDepth = Integer.parseInt(in.readLine());
			alpha = Float.parseFloat(in.readLine());
			volumes[Sound.SoundType.MUSIC.getValue()] = Float.parseFloat(in.readLine());
			volumes[Sound.SoundType.SOUND_FX.getValue()] = Float.parseFloat(in.readLine());
			volumes[Sound.SoundType.VOICE.getValue()] = Float.parseFloat(in.readLine());
			volumes[Sound.SoundType.COMBAT_FX.getValue()] = volumes[Sound.SoundType.SOUND_FX.getValue()];
			controlMode = ShipControl.values()[Integer.parseInt(in.readLine())];
			controlPosition = Integer.parseInt(in.readLine());
			controlAlpha = Float.parseFloat(in.readLine());
			introVideoQuality = Integer.parseInt(in.readLine());
			unlimitedFuel = Boolean.parseBoolean(in.readLine());
			enterInSafeZone = Boolean.parseBoolean(in.readLine());
			disableAttackers = Boolean.parseBoolean(in.readLine());
			disableTraders = Boolean.parseBoolean(in.readLine());
			invulnerable = Boolean.parseBoolean(in.readLine());
			laserDoesNotOverheat = Boolean.parseBoolean(in.readLine());
			particleDensity = Integer.parseInt(in.readLine());
			tapRadarToChangeView = Boolean.parseBoolean(in.readLine());
			laserButtonAutoFire = Boolean.parseBoolean(in.readLine());
			hasBeenPlayedBefore = Boolean.parseBoolean(in.readLine());
			for (int i = 0; i < 12; i++) {
				buttonPosition[i] = Integer.parseInt(in.readLine());
			}
			fastDC = Boolean.parseBoolean(in.readLine()); // Dummy for fast DC
			engineExhaust = Boolean.parseBoolean(in.readLine());
			lockScreen = Integer.parseInt(in.readLine());
			colorScheme = in.readLine();
			targetBox = Boolean.parseBoolean(in.readLine());
			autoId = Boolean.parseBoolean(in.readLine());
			reversePitch = Boolean.parseBoolean(in.readLine());
			flatButtonDisplay = Boolean.parseBoolean(in.readLine());
			volumes[Sound.SoundType.COMBAT_FX.getValue()] = Float.parseFloat(in.readLine());
			vibrateLevelOnDamage = Float.parseFloat(in.readLine());
			dockingComputerSpeed = Integer.parseInt(in.readLine());
			difficultyLevel = Integer.parseInt(in.readLine());
			restoredCommanderCount = Integer.parseInt(in.readLine());
			navButtonsVisible = Boolean.parseBoolean(in.readLine());
			extensionUpdateMode = Integer.parseInt(in.readLine());
			continuousTutorialMode = Boolean.parseBoolean(in.readLine());
			Medal.setGameLevelMedals(in.readLine());
			setFromString(watchedTocEntries, in.readLine());
			String line = in.readLine();
			if (line != null) {
				vibrateLevelOnHit = Float.parseFloat(line);
			}
		} catch (IOException | NumberFormatException ignored) {
			dockingComputerSpeed = fastDC ? 2 : 0;
			continuousTutorialMode = true;
		}
		ColorScheme.setColorScheme(files, null, colorScheme);
	}

	private static void setFromString(Set<String> dest, String s) {
		dest.clear();
		String list = s.substring(1, s.length() - 1); // chop off brackets
		for (String token : list.split(",")) {
			dest.add(token.trim());
		}
	}

	public static void setOrientation(Activity activity) {
		switch (lockScreen) {
			case 0: activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); break;
			case 1: activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); break;
			case 2: activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); break;
		}
	}

	public static void setImmersion(View view) {
		if (view == null || navButtonsVisible) {
			return;
		}
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
			View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	public static void resetButtonPosition() {
		for (int i = 0; i < 12; i++) {
			buttonPosition[i] = i;
		}
	}

	public static void save(FileIO files) {
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(files.writeFile(SETUP_FILE_NAME)))) {
			out.write(animationsEnabled + "\n");
			out.write(L.getLanguageCountry(L.getInstance().getCurrentLocale()) + "\n");
			out.write(debugActive + "\n");
			out.write(logToFile + "\n");
			out.write(displayFrameRate + "\n");
			out.write(displayDockingInformation + "\n");
			out.write(memDebug + "\n");
			out.write(textureLevel + "\n");
			out.write(colorDepth + "\n");
			out.write(alpha + "\n");
			out.write(volumes[Sound.SoundType.MUSIC.getValue()] + "\n");
			out.write(volumes[Sound.SoundType.SOUND_FX.getValue()] + "\n");
			out.write(volumes[Sound.SoundType.VOICE.getValue()] + "\n");
			out.write(controlMode.ordinal() + "\n");
			out.write(controlPosition + "\n");
			out.write(controlAlpha + "\n");
			out.write(introVideoQuality + "\n");
			out.write(false + "\n");
			out.write(false + "\n");
			out.write(disableAttackers + "\n");
			out.write(disableTraders + "\n");
			out.write(false + "\n");
			out.write(false + "\n");
			out.write(particleDensity + "\n");
			out.write(tapRadarToChangeView + "\n");
			out.write(laserButtonAutoFire + "\n");
			out.write(hasBeenPlayedBefore + "\n");
			for (int i = 0; i < 12; i++) {
				out.write(buttonPosition[i] + "\n");
			}
			out.write(false + "\n"); // Backward compatibility
			out.write(engineExhaust + "\n");
			out.write(lockScreen + "\n");
			out.write(colorScheme + "\n");
			out.write(targetBox + "\n");
			out.write(autoId + "\n");
			out.write(reversePitch + "\n");
			out.write(flatButtonDisplay + "\n");
			out.write(volumes[Sound.SoundType.COMBAT_FX.getValue()] + "\n");
			out.write(vibrateLevelOnDamage + "\n");
			out.write(dockingComputerSpeed + "\n");
			out.write(difficultyLevel + "\n");
			out.write(restoredCommanderCount + "\n");
			out.write(navButtonsVisible + "\n");
			out.write(extensionUpdateMode + "\n");
			out.write(continuousTutorialMode + "\n");
			out.write(Medal.getGameLevelMedals() + "\n");
			out.write(watchedTocEntries + "\n");
			out.write(vibrateLevelOnHit + "\n");
		} catch (IOException ignored) { }
	}
}
