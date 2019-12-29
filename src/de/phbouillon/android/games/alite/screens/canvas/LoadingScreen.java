package de.phbouillon.android.games.alite.screens.canvas;

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

import java.io.File;
import java.io.IOException;

import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Settings;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class LoadingScreen extends AliteScreen {
	public static final String DIRECTORY_SOUND = "sound" + File.separatorChar;
	public static final String DIRECTORY_MUSIC = "music" + File.separatorChar;
	private static final String DIRECTORY_SOUND_COMPUTER = DIRECTORY_SOUND + "computer" + File.separatorChar;

	public LoadingScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
	}

	private void loadSounds() {
		Assets.click = newSound("guiclick");
		Assets.alert = newSound("beep");
		Assets.kaChing = newSound("buy");
		Assets.fireLaser = newCombatSound("laser");
		Assets.laserHit = newCombatSound("laserhit");
		Assets.enemyFireLaser = newCombatSound("enemy_laser");
		Assets.hullDamage = newCombatSound("enemy_laserhit");

		Assets.energyLow = newSound("alert");
		Assets.altitudeLow = Assets.energyLow;
		Assets.temperatureHigh = Assets.energyLow;
		Assets.criticalCondition = Assets.energyLow;
		Assets.error = newSound("witchabort");

		Assets.shipDestroyed = newCombatSound("explosion");
		Assets.scooped = newSound("scoop");
		Assets.fireMissile = newSound("missile");
		Assets.missileLocked = Assets.alert;
		Assets.torus = newSound("torus");
		Assets.ecm = newSound("ecm");
		Assets.identify = newSound("boop");
		Assets.retroRocketsOrEscapeCapsuleFired = newSound("retros");
		Assets.hyperspace = newSound("hyperspace");
	}

	private Sound newSound(String soundFileName) {
		return game.getAudio().newSound(DIRECTORY_SOUND + soundFileName + ".ogg");
	}

	private Sound newCombatSound(String soundFileName) {
		return game.getAudio().newCombatSound(DIRECTORY_SOUND + soundFileName + ".ogg");
	}

	private void loadLocalizedSounds() {
		Assets.com_aftShieldHasFailed = safeLoadSound("aft_shield_failed");
		Assets.com_frontShieldHasFailed = safeLoadSound("front_shield_failed");
		Assets.com_conditionRed = safeLoadSound("condition_red");
		Assets.com_dockingComputerEngaged = safeLoadSound("docking_on");
		Assets.com_dockingComputerDisengaged = safeLoadSound("docking_off");
		Assets.com_hyperdriveMalfunction = safeLoadSound("hyperdrive_malfunction");
		Assets.com_hyperdriveRepaired = safeLoadSound("hyperdrive_repaired");
		Assets.com_incomingMissile = safeLoadSound("incoming_missile");
		Assets.com_laserTemperatureCritical = safeLoadSound("laser_temperature");
		Assets.com_cabinTemperatureCritical = safeLoadSound("cabin_temperature");
		Assets.com_targetDestroyed = safeLoadSound("target_destroyed");
		Assets.com_fuelSystemMalfunction = safeLoadSound("fuel_malfunction");
		Assets.com_accessDeclined = safeLoadSound("access_declined");

		Assets.com_lostDockingComputer = safeLoadSound("lost_docking");
		Assets.com_lostEcm = safeLoadSound("lost_ecm");
		Assets.com_lostEnergyBomb = safeLoadSound("lost_bomb");
		Assets.com_lostEscapeCapsule = safeLoadSound("lost_escape");
		Assets.com_lostExtraEnergyUnit = safeLoadSound("lost_energy");
		Assets.com_lostFuelScoop = safeLoadSound("lost_fuel_scoop");
		Assets.com_lostGalacticHyperdrive = safeLoadSound("lost_galactic");
		Assets.com_lostRetroRockets = safeLoadSound("lost_retro_rockets");
		Assets.com_escapeMalfunction = safeLoadSound("escape_malfunction");
		Assets.com_lostCargo = safeLoadSound("lost_cargo");
		Assets.com_launch_area_violation_1st = safeLoadSound("launch_viol1");
		Assets.com_launch_area_violation_2nd = safeLoadSound("launch_viol2");
		Assets.com_launch_area_violation_3rd = safeLoadSound("launch_viol3");
	}

	private Sound safeLoadSound(String soundFileName) {
		return game.getAudio().newSoundAsset(DIRECTORY_SOUND_COMPUTER + soundFileName + ".ogg");
	}

	public void changeLocale() {
		new Thread(){
			public void run() {
				loadLocalizedSounds();
			}
		}.start();
	}

	@Override
	public void update(float deltaTime) {
		AliteLog.d("Starting LoadingScreen", "Starting loading screen");
		long m1 = System.currentTimeMillis();
		AliteLog.d("Debug-1", "Now loading Alite Logo");
		Assets.aliteLogoSmall = game.getGraphics().newPixmap("alite_logo_small.png");
		AliteLog.d("Debug-2", "Logo loaded; skipping sound load");
		new Thread(){
			public void run() {
				loadSounds();
				loadLocalizedSounds();
			}
		}.start();
		AliteLog.d("Debug-3", "Now loading Alite Settings");
		Settings.load(game.getFileIO());
		AliteLog.d("Debug-4", "Settings loaded");
		game.getGraphics().setClip(-1, -1, -1, -1);
		AliteLog.d("Debug-5", "Clip reset");

		AliteLog.d("End LoadingScreen", "End LoadingScreen. Resource load took: " + (System.currentTimeMillis() - m1));
		try {
			AliteLog.d("Debug-6", "Now loading Alite Game State (if present)");
			if (!game.readState()) {
				AliteLog.d("Debug-7", "No game state present, defaulting to SIS");
				game.setScreen(new ShipIntroScreen(game));
				AliteLog.d("Debug-8", "SIS set.");
			}
		} catch (IOException ignored) {
			AliteLog.d("Debug-9", "IO-Ex, defaulting to SIS");
			game.setScreen(new ShipIntroScreen(game));
			AliteLog.d("Debug-10", "SIS set.");
		}
	}

	@Override
	public void present(float deltaTime) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void loadAssets() {
	}

	@Override
	public void pause() {
	}

	@Override
	public int getScreenCode() {
		return -1;
	}
}
