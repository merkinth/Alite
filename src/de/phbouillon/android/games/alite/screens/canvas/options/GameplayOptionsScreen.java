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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.PluginManager;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.SystemData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class GameplayOptionsScreen extends OptionsScreen {
	private final Button[] buttons = new Button[6];
	private Button back;

	@Override
	public void activate() {
		buttons[0] = createButton(0, getDifficultyButtonText()).setEvent(b -> {
			Settings.difficultyLevel = cycleFromZeroTo(Settings.difficultyLevel, 5);
			b.setText(getDifficultyButtonText());
		});
		buttons[1] = createSmallButton(2, true, getLaserButtonText()).setEvent(b -> {
			Settings.laserButtonAutoFire = !Settings.laserButtonAutoFire;
			b.setText(getLaserButtonText());
		});
		buttons[2] = createSmallButton(2, false, getExtendedGalaxyText()).setEvent(b -> {
			if (!game.getPlayer().isPlanetVisited(SystemData.RAXXLA_SYSTEM.getId())) {
				showLargeMessageDialog(L.string(R.string.options_gameplay_extended_galaxy_on_error));
				return;
			}
			if (game.getGenerator().getCurrentGalaxy() > GalaxyGenerator.GALAXY_COUNT) {
				showLargeMessageDialog(L.string(R.string.options_gameplay_extended_galaxy_off_error));
				return;
			}
			Settings.maxGalaxies = GalaxyGenerator.EXTENDED_GALAXY_COUNT + GalaxyGenerator.GALAXY_COUNT - Settings.maxGalaxies;
			b.setText(getExtendedGalaxyText());
		});
		buttons[3] = createButton(3, getAutoIdButtonText()).setEvent(b -> {
			Settings.autoId = !Settings.autoId;
			b.setText(getAutoIdButtonText());
		});
		buttons[4] = createButton(4, getDockingComputerButtonText()).setEvent(b -> {
			Settings.dockingComputerSpeed = cycleFromZeroTo(Settings.dockingComputerSpeed, 2);
			b.setText(getDockingComputerButtonText());
		});
		buttons[5] = createButton(5, getExtensionUpdateModeButtonText()).setEvent(b -> {
			Settings.extensionUpdateMode = cycleFromZeroTo(Settings.extensionUpdateMode, PluginManager.UPDATE_MODE_AUTO_UPDATE_OVER_WIFI_ONLY);
			b.setText(getExtensionUpdateModeButtonText());
		});
		back = createButton(6, L.string(R.string.options_back));
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

	private String getExtendedGalaxyText() {
		return L.string(R.string.options_gameplay_extended_galaxy, L.string(
			Settings.maxGalaxies == GalaxyGenerator.GALAXY_COUNT ? R.string.options_off : R.string.options_on));
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
		centerText(L.string(getDifficultyDescription()), 315, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		for (Button b : buttons) {
			b.render(g);
		}
		back.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		for (Button b : buttons) {
			if (b.isPressed(touch)) {
				b.onEvent();
				Settings.save(game.getFileIO());
				return;
			}
		}
		if (back.isPressed(touch)) {
			newScreen = new OptionsScreen();
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.GAMEPLAY_OPTIONS_SCREEN;
	}

}
