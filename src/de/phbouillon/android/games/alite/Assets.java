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

import de.phbouillon.android.framework.Music;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.framework.impl.gl.font.GLText;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Assets {
	public static final String DIRECTORY_SOUND = "sound" + File.separatorChar;
	private static final String DIRECTORY_SOUND_COMPUTER = DIRECTORY_SOUND + "computer" + File.separatorChar;

	public static Pixmap launchIcon;
	public static Pixmap statusIcon;
	public static Pixmap buyIcon;
	public static Pixmap inventoryIcon;
	public static Pixmap equipIcon;
	public static Pixmap galaxyIcon;
	public static Pixmap localIcon;
	public static Pixmap planetIcon;
	public static Pixmap diskIcon;
	public static Pixmap optionsIcon;
	public static Pixmap academyIcon;
	public static Pixmap libraryIcon;
	public static Pixmap hackerIcon;
	public static Pixmap quitIcon;
	public static Pixmap aliteLogoSmall;

	public static Pixmap yesIcon;
	public static Pixmap noIcon;

	public static GLText regularFont;
	public static GLText boldFont;
	public static GLText italicFont;
	public static GLText boldItalicFont;
	public static GLText titleFont;
	public static GLText smallFont;

	public static Sound com_aftShieldHasFailed;
	public static Sound com_frontShieldHasFailed;
	public static Sound com_conditionRed;
	public static Sound com_dockingComputerEngaged;
	public static Sound com_dockingComputerDisengaged;
	public static Sound com_hyperdriveMalfunction;
	public static Sound com_hyperdriveRepaired;
	public static Sound com_incomingMissile;
	public static Sound com_laserTemperatureCritical;
	public static Sound com_cabinTemperatureCritical;
	public static Sound com_targetDestroyed;
	public static Sound com_fuelSystemMalfunction;
	public static Sound com_accessDeclined;
	public static Sound com_escapeMalfunction;
	public static Sound com_lostCargo;
	public static Sound com_launch_area_violation_1st;
	public static Sound com_launch_area_violation_2nd;
	public static Sound com_launch_area_violation_3rd;

	public static Sound click;
	public static Sound error;
	public static Sound alert;
	public static Sound kaChing;
	public static Sound fireLaser;
	public static Sound laserHit;
	public static Sound enemyFireLaser;
	public static Sound hullDamage;
	public static Sound shipDestroyed;
	public static Sound scooped;
	public static Sound fireMissile;
	public static Sound missileLocked;
	public static Sound torus;
	public static Sound energyLow;
	public static Sound altitudeLow;
	public static Sound temperatureHigh;
	public static Sound criticalCondition;
	public static Sound ecm;
	public static Sound retroRocketsOrEscapeCapsuleFired;
	public static Sound identify;
	public static Sound hyperspace;

	public static Music danube;

	private static Map<String,Sound> lostEquipmentSound = new HashMap<>();

	public static Sound getLostEquipmentSound(String equipmentName) {
		return lostEquipmentSound.get(equipmentName);
	}

	public static void setLostEquipmentSound(String equipmentName) {
		Assets.lostEquipmentSound.put(equipmentName, safeLoadSound(equipmentName));
	}

	public static Sound safeLoadSound(String soundFileName) {
		return Alite.get().getAudio().newSoundAsset(DIRECTORY_SOUND_COMPUTER + soundFileName + ".ogg");
	}

}
