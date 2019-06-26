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
	public static final String DIRECTORY_SOUND = "sound" + File.separator;
	public static final String DIRECTORY_MUSIC = "music" + File.separator;
	private static final String DIRECTORY_SOUND_COMPUTER = DIRECTORY_SOUND + "computer" + File.separator;

	public LoadingScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
	}

	private void loadSounds() {
		Assets.click = game.getAudio().newSound(DIRECTORY_SOUND + "guiclick.ogg");
		Assets.alert = game.getAudio().newSound(DIRECTORY_SOUND + "beep.ogg");
		Assets.kaChing = game.getAudio().newSound(DIRECTORY_SOUND + "buy.ogg");
		Assets.fireLaser = game.getAudio().newCombatSound(DIRECTORY_SOUND + "laser.ogg");
		Assets.laserHit = game.getAudio().newCombatSound(DIRECTORY_SOUND + "laserhit.ogg");
		Assets.enemyFireLaser = game.getAudio().newCombatSound(DIRECTORY_SOUND + "enemy_laser.ogg");
		Assets.hullDamage = game.getAudio().newCombatSound(DIRECTORY_SOUND + "enemy_laserhit.ogg");

		Assets.energyLow = game.getAudio().newSound(DIRECTORY_SOUND + "alert.ogg");
		Assets.altitudeLow = Assets.energyLow;
		Assets.temperatureHigh = Assets.energyLow;
		Assets.criticalCondition = Assets.energyLow;
		Assets.error = game.getAudio().newSound(DIRECTORY_SOUND + "witchabort.ogg");

		Assets.shipDestroyed = game.getAudio().newCombatSound(DIRECTORY_SOUND + "explosion.ogg");
		Assets.scooped = game.getAudio().newSound(DIRECTORY_SOUND + "scoop.ogg");
		Assets.fireMissile = game.getAudio().newSound(DIRECTORY_SOUND + "missile.ogg");
		Assets.missileLocked = Assets.alert;
		Assets.torus = game.getAudio().newSound(DIRECTORY_SOUND + "torus.ogg");
		Assets.ecm = game.getAudio().newSound(DIRECTORY_SOUND + "ecm.ogg");
		Assets.identify = game.getAudio().newSound(DIRECTORY_SOUND + "boop.ogg");
		Assets.retroRocketsOrEscapeCapsuleFired = game.getAudio().newSound(DIRECTORY_SOUND + "retros.ogg");
		Assets.hyperspace = game.getAudio().newSound(DIRECTORY_SOUND + "hyperspace.ogg");
	}

	private void loadLocalizedSounds() {
		Assets.com_aftShieldHasFailed = safeLoadSound(Assets.com_aftShieldHasFailed, "aft_shield_failed");
		Assets.com_frontShieldHasFailed = safeLoadSound(Assets.com_frontShieldHasFailed, "front_shield_failed");
		Assets.com_conditionRed = safeLoadSound(Assets.com_conditionRed, "condition_red");
		Assets.com_dockingComputerEngaged = safeLoadSound(Assets.com_dockingComputerEngaged, "docking_on");
		Assets.com_dockingComputerDisengaged = safeLoadSound(Assets.com_dockingComputerDisengaged, "docking_off");
		Assets.com_hyperdriveMalfunction = safeLoadSound(Assets.com_hyperdriveMalfunction, "hyperdrive_malfunction");
		Assets.com_hyperdriveRepaired = safeLoadSound(Assets.com_hyperdriveRepaired, "hyperdrive_repaired");
		Assets.com_incomingMissile = safeLoadSound(Assets.com_incomingMissile, "incoming_missile");
		Assets.com_laserTemperatureCritical = safeLoadSound(Assets.com_laserTemperatureCritical, "laser_temperature");
		Assets.com_cabinTemperatureCritical = safeLoadSound(Assets.com_cabinTemperatureCritical, "cabin_temperature");
		Assets.com_targetDestroyed = safeLoadSound(Assets.com_targetDestroyed, "target_destroyed");
		Assets.com_fuelSystemMalfunction = safeLoadSound(Assets.com_fuelSystemMalfunction, "fuel_malfunction");
		Assets.com_accessDeclined = safeLoadSound(Assets.com_accessDeclined, "access_declined");

		Assets.com_lostDockingComputer = safeLoadSound(Assets.com_lostDockingComputer, "lost_docking");
		Assets.com_lostEcm = safeLoadSound(Assets.com_lostEcm, "lost_ecm");
		Assets.com_lostEnergyBomb = safeLoadSound(Assets.com_lostEnergyBomb, "lost_bomb");
		Assets.com_lostEscapeCapsule = safeLoadSound(Assets.com_lostEscapeCapsule, "lost_escape");
		Assets.com_lostExtraEnergyUnit = safeLoadSound(Assets.com_lostExtraEnergyUnit, "lost_energy");
		Assets.com_lostFuelScoop = safeLoadSound(Assets.com_lostFuelScoop, "lost_fuel_scoop");
		Assets.com_lostGalacticHyperdrive = safeLoadSound(Assets.com_lostGalacticHyperdrive, "lost_galactic");
		Assets.com_lostRetroRockets = safeLoadSound(Assets.com_lostRetroRockets, "lost_retro_rockets");
		Assets.com_escapeMalfunction = safeLoadSound(Assets.com_escapeMalfunction, "escape_malfunction");
		Assets.com_lostCargo = safeLoadSound(Assets.com_lostCargo, "lost_cargo");
		Assets.com_launch_area_violation_1st = safeLoadSound(Assets.com_launch_area_violation_1st, "launch_viol1");
		Assets.com_launch_area_violation_2nd = safeLoadSound(Assets.com_launch_area_violation_2nd, "launch_viol2");
		Assets.com_launch_area_violation_3rd = safeLoadSound(Assets.com_launch_area_violation_3rd, "launch_viol3");
	}

	private Sound safeLoadSound(Sound current, String soundFileName) {
		Sound sound = game.getAudio().newSoundAsset(DIRECTORY_SOUND_COMPUTER + soundFileName + ".ogg");
		return sound != null ? sound : current;
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
	public void resume() {
	}

	@Override
	public int getScreenCode() {
		return -1;
	}
}
