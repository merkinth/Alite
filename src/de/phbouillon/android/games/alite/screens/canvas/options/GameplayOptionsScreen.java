package de.phbouillon.android.games.alite.screens.canvas.options;

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

import java.io.DataInputStream;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.PluginManager;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class GameplayOptionsScreen extends OptionsScreen {
	private Button difficultyLevel;
	private Button autoId;
	private Button dockingSpeed;
	private Button laserAutoFire;
	private Button extensionUpdateMode;
	private Button back;

	GameplayOptionsScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
		difficultyLevel   = createButton(0, getDifficultyButtonText());
		autoId            = createButton(2, getAutoIdButtonText());
		dockingSpeed      = createButton(3, getDockingComputerButtonText());
		laserAutoFire     = createButton(4, getLaserButtonText());
		extensionUpdateMode = createButton(5, getExtensionUpdateModeButtonText());
		back              = createButton(6, L.string(R.string.options_back));
	}

	private String getLaserButtonText() {
		return L.string(R.string.options_gameplay_laser, L.string(Settings.laserButtonAutoFire ? R.string.options_gameplay_laser_auto_fire : R.string.options_gameplay_laser_single_shot));
	}

	private String getDockingComputerButtonText() {
		return L.string(R.string.options_gameplay_docking_speed, L.string(Settings.dockingComputerSpeed == 0 ? R.string.options_gameplay_docking_normal_speed :
			Settings.dockingComputerSpeed == 1 ? R.string.options_gameplay_docking_time_drive_speed  : R.string.options_gameplay_docking_dock_immediate));
	}

	private String getAutoIdButtonText() {
		return L.string(R.string.options_gameplay_auto_id, L.string(Settings.autoId ? R.string.options_on : R.string.options_off));
	}

	private String getExtensionUpdateModeButtonText() {
		return L.string(R.string.options_gameplay_extension_update_mode, L.string(getExtensionUpdateModeString()));
	}

	private int getExtensionUpdateModeString() {
		switch (Settings.extensionUpdateMode) {
			case PluginManager.UPDATE_MODE_NO_UPDATE: return R.string.options_gameplay_extension_update_mode_no_update;
			case PluginManager.UPDATE_MODE_CHECK_FOR_UPDATES_ONLY: return R.string.options_gameplay_extension_update_mode_check_for_updates_only;
			case PluginManager.UPDATE_MODE_AUTO_UPDATE_AT_ANY_TIME: return R.string.options_gameplay_extension_update_mode_auto_update_at_any_time;
		}
		return R.string.options_gameplay_extension_update_mode_auto_update_over_wifi_only;
	}

	private String getDifficultyButtonText() {
		return L.string(R.string.options_gameplay_difficulty_level, L.string(getDifficultyString()));
	}

	private int getDifficultyString() {
		switch (Settings.difficultyLevel) {
			case 0: return R.string.options_gameplay_difficulty_sedate;
			case 1: return R.string.options_gameplay_difficulty_very_easy;
			case 2: return R.string.options_gameplay_difficulty_easy;
			case 3: return R.string.options_gameplay_difficulty_normal;
			case 4: return R.string.options_gameplay_difficulty_hard;
			case 5: return R.string.options_gameplay_difficulty_extremely_hard;
		}
		return R.string.options_gameplay_difficulty_normal;
	}

	private int getDifficultyDescription() {
		switch (Settings.difficultyLevel) {
			case 0: return R.string.options_gameplay_difficulty_sedate_desc;
			case 1: return R.string.options_gameplay_difficulty_very_easy_desc;
			case 2: return R.string.options_gameplay_difficulty_easy_desc;
			case 3: return R.string.options_gameplay_difficulty_normal_desc;
			case 4: return R.string.options_gameplay_difficulty_hard_desc;
			case 5: return R.string.options_gameplay_difficulty_extremely_hard_desc;
		}
		return R.string.options_gameplay_difficulty_normal_desc;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_gameplay_options));
		difficultyLevel.render(g);
		centerText(L.string(getDifficultyDescription()), 315, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		autoId.render(g);
		dockingSpeed.render(g);
		laserAutoFire.render(g);
		extensionUpdateMode.render(g);

		back.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (difficultyLevel.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.difficultyLevel = cycleFromZeroTo(Settings.difficultyLevel, 5);
			difficultyLevel.setText(getDifficultyButtonText());
			Settings.save(game.getFileIO());
			return;
		}
		if (autoId.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.autoId = !Settings.autoId;
			autoId.setText(getAutoIdButtonText());
			Settings.save(game.getFileIO());
			return;
		}
		if (dockingSpeed.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.dockingComputerSpeed = cycleFromZeroTo(Settings.dockingComputerSpeed, 2);
			dockingSpeed.setText(getDockingComputerButtonText());
			Settings.save(game.getFileIO());
			return;
		}
		if (laserAutoFire.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.laserButtonAutoFire = !Settings.laserButtonAutoFire;
			laserAutoFire.setText(getLaserButtonText());
			Settings.save(game.getFileIO());
			return;
		}
		if (extensionUpdateMode.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.extensionUpdateMode = cycleFromZeroTo(Settings.extensionUpdateMode, PluginManager.UPDATE_MODE_AUTO_UPDATE_OVER_WIFI_ONLY);
			extensionUpdateMode.setText(getExtensionUpdateModeButtonText());
			Settings.save(game.getFileIO());
			return;
		}
		if (back.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new OptionsScreen(game);
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.GAMEPLAY_OPTIONS_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new GameplayOptionsScreen(alite));
		return true;
	}
}
