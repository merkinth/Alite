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
import java.io.DataOutputStream;
import java.io.IOException;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class InFlightButtonsOptionsScreen extends AliteScreen {
	private ButtonConfigData[] uiButton = new ButtonConfigData[12];

	private Pixmap torusDriveDockingComputerPixmap;
	private Pixmap hyperspacePixmap;
	private Pixmap galacticHyperspacePixmap;
	private Pixmap statusPixmap;
	private Pixmap ecmPixmap;
	private Pixmap escapeCapsulePixmap;
	private Pixmap energyBombPixmap;
	private Pixmap retroRocketsPixmap;
	private Pixmap ecmJammerPixmap;
	private Pixmap cloakingDevicePixmap;
	private Pixmap missilePixmap;
	private Pixmap firePixmap;
	private Pixmap overlayPixmap;

	private Button selectionMode;
	private Button reset;
	private Button back;
	private ButtonConfigData selectedButton = null;
	private ButtonConfigGroup[] buttonGroups = new ButtonConfigGroup[4];
	private boolean groupSelectionMode = true;
	private boolean confirmReset = false;

	class ButtonConfigData {
		Button button;
		int groupIndex;
		int settingsPosition;
		String name;
	}

	class ButtonConfigGroup {
		ButtonConfigData[] buttons = new ButtonConfigData[3];
	}

	InFlightButtonsOptionsScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
		uiButton[Settings.FIRE] = createButton(Settings.buttonPosition[Settings.FIRE], firePixmap, Settings.FIRE, L.string(R.string.in_flight_button_fire));
		uiButton[Settings.MISSILE] = createButton(Settings.buttonPosition[Settings.MISSILE], missilePixmap, Settings.MISSILE, L.string(R.string.in_flight_button_missile));
		uiButton[Settings.ECM] = createButton(Settings.buttonPosition[Settings.ECM], ecmPixmap, Settings.ECM, L.string(R.string.in_flight_button_ecm));
		uiButton[Settings.RETRO_ROCKETS] = createButton(Settings.buttonPosition[Settings.RETRO_ROCKETS], retroRocketsPixmap, Settings.RETRO_ROCKETS, L.string(R.string.in_flight_button_retro_rockets));
		uiButton[Settings.ESCAPE_CAPSULE] = createButton(Settings.buttonPosition[Settings.ESCAPE_CAPSULE], escapeCapsulePixmap, Settings.ESCAPE_CAPSULE, L.string(R.string.in_flight_button_escape_capsule));
		uiButton[Settings.ENERGY_BOMB] = createButton(Settings.buttonPosition[Settings.ENERGY_BOMB], energyBombPixmap, Settings.ENERGY_BOMB, L.string(R.string.in_flight_button_energy_bomb));
		uiButton[Settings.STATUS] = createButton(Settings.buttonPosition[Settings.STATUS], statusPixmap, Settings.STATUS, L.string(R.string.in_flight_button_status));
		uiButton[Settings.TORUS] = createButton(Settings.buttonPosition[Settings.TORUS], torusDriveDockingComputerPixmap, Settings.TORUS, L.string(R.string.in_flight_button_drive_docking));
		uiButton[Settings.HYPERSPACE] = createButton(Settings.buttonPosition[Settings.HYPERSPACE], hyperspacePixmap, Settings.HYPERSPACE, L.string(R.string.in_flight_button_hyperspace));
		uiButton[Settings.GALACTIC_HYPERSPACE] = createButton(Settings.buttonPosition[Settings.GALACTIC_HYPERSPACE], galacticHyperspacePixmap, Settings.GALACTIC_HYPERSPACE, L.string(R.string.in_flight_button_galactic_hyperspace));
	    uiButton[Settings.CLOAKING_DEVICE] = createButton(Settings.buttonPosition[Settings.CLOAKING_DEVICE], cloakingDevicePixmap, Settings.CLOAKING_DEVICE, L.string(R.string.in_flight_button_cloaking_device));
	    uiButton[Settings.ECM_JAMMER] = createButton(Settings.buttonPosition[Settings.ECM_JAMMER], ecmJammerPixmap, Settings.ECM_JAMMER, L.string(R.string.in_flight_button_ecm_jammer));

		selectionMode = Button.createGradientTitleButton(50, 860, 1620, 100,
			L.string(R.string.options_in_flight_selection_mode, L.string(groupSelectionMode ? R.string.options_in_flight_selection_mode_group : R.string.options_in_flight_selection_mode_button)));
		back = Button.createGradientTitleButton(50, 970, 780, 100, L.string(R.string.options_back));
		reset = Button.createGradientTitleButton(890, 970, 780, 100, L.string(R.string.options_in_flight_reset_positions));
	}

	private ButtonConfigData createButton(int position, Pixmap pixmap, int settingsPosition, String name) {
		int xt;
		int yt;
		int groupIndex;
		int buttonIndex;
		if (position < 6) {
			xt = (position % 3 % 2 == 0 ? 0 : 150) + (position < 3 ? 0 : 300);
			groupIndex = position < 3 ? 0 : 1;
		} else {
			xt = (position % 3 % 2 == 0 ? 1500 : 1350) - (position < 9 ? 0 : 300);
			groupIndex = position < 9 ? 2 : 3;
		}
		yt = position % 3 * 150 + 200;
		buttonIndex = position % 3;

		Button result = Button.createPictureButton(xt, yt, 200, 200, pixmap);

		ButtonConfigData config = new ButtonConfigData();
		config.button = result;
		config.groupIndex = groupIndex;
		config.settingsPosition = settingsPosition;
		config.name = name;
		if (buttonGroups[groupIndex] == null) {
			buttonGroups[groupIndex] = new ButtonConfigGroup();
		}

		buttonGroups[groupIndex].buttons[buttonIndex] = config;

		return config;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_button_position_options));

		g.drawText(L.string(R.string.options_in_flight_primary), 20, 150, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.options_in_flight_secondary), 320, 150, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.options_in_flight_secondary), 1180, 150, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.options_in_flight_primary), 1480, 150, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		centerText(L.string(groupSelectionMode ?
			selectedButton == null ? R.string.options_in_flight_source_button_group : R.string.options_in_flight_target_button_group :
			selectedButton == null ? R.string.options_in_flight_source_button : R.string.options_in_flight_target_button),
			800, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));

		for (ButtonConfigData b: uiButton) {
			b.button.render(g);
			if (b.button.isSelected()) {
				g.drawPixmap(overlayPixmap, b.button.getX(), b.button.getY());
				if (!b.name.contains(";")) {
					centerText(b.name, b.button.getY() + 100, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
				} else {
					centerText(b.name.substring(0, b.name.indexOf(";")), b.button.getY() + 80, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
					centerText(b.name.substring(b.name.indexOf(";") + 1), b.button.getY() + 120, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
				}

			}
		}

		selectionMode.render(g);
		back.render(g);
		reset.render(g);
	}


	private void swapSingleButton(ButtonConfigData src, ButtonConfigData target) {
		AliteLog.d("Swapping Buttons", src.name + ", " + target.name + " => " + Settings.buttonPosition[src.settingsPosition] + ", " + Settings.buttonPosition[target.settingsPosition]);

		for (int i = 0; i < 3; i++) {
			if (buttonGroups[src.groupIndex].buttons[i] == src) {
				buttonGroups[src.groupIndex].buttons[i] = null;
			}
			if (buttonGroups[target.groupIndex].buttons[i] == target) {
				buttonGroups[target.groupIndex].buttons[i] = src;
			}
		}
		for (int i = 0; i < 3; i++) {
			if (buttonGroups[src.groupIndex].buttons[i] == null) {
				buttonGroups[src.groupIndex].buttons[i] = target;
			}
		}

		int srcValue = Settings.buttonPosition[src.settingsPosition];
		int tgtValue = Settings.buttonPosition[target.settingsPosition];
		int x = src.button.getX();
		int y = src.button.getY();
		int x2 = target.button.getX();
		int y2 = target.button.getY();
		target.button.move(x, y);
		src.button.move(x2, y2);
		int srcGrp = src.groupIndex;
		src.groupIndex = target.groupIndex;
		target.groupIndex = srcGrp;
		Settings.buttonPosition[src.settingsPosition] = tgtValue;
		Settings.buttonPosition[target.settingsPosition] = srcValue;

		AliteLog.d("Swapped Buttons", src.name + ", " + target.name + " => " + Settings.buttonPosition[src.settingsPosition] + ", " + Settings.buttonPosition[target.settingsPosition]);
	}

	private void debug() {
		int count = 1;
		for (ButtonConfigGroup bcg: buttonGroups) {
			AliteLog.d("Button Group " + count, "Button Group " + count);
			int bc = 1;
			if (bcg == null) {
				AliteLog.d("  BCG - " + count + " == null!!", "  BCG - " + count + " == null!!");
			}
			if (bcg != null && bcg.buttons == null) {
				AliteLog.d("  BCG - " + count + ".buttons == null!!", "  BCG - " + count + ".buttons == null!!");
			}
			if (bcg != null && bcg.buttons != null) {
				for (ButtonConfigData b: bcg.buttons) {
					AliteLog.d("  Button " + bc, "  Button " + bc + " => " + b.name + " => " + b.groupIndex + " => " + b.settingsPosition + " => " + Settings.buttonPosition[b.settingsPosition]);
					bc++;
				}
			}
			count++;
		}
	}

	private void swapButtons(ButtonConfigData target) {
		if (groupSelectionMode) {
			if (target.groupIndex != selectedButton.groupIndex) {
				int srcGI = selectedButton.groupIndex;
				int tGI = target.groupIndex;
				for (int i = 0; i < 3; i++) {
					swapSingleButton(buttonGroups[tGI].buttons[i], buttonGroups[srcGI].buttons[i]);
				}
			}
		} else {
			swapSingleButton(selectedButton, target);
		}
		for (ButtonConfigData b: uiButton) {
			b.button.setSelected(false);
		}
		selectedButton = null;
		debug();
		Settings.save(game.getFileIO());
	}

	private void selectGroup(ButtonConfigData b) {
		for (int j = 0; j < 3; j++) {
			buttonGroups[b.groupIndex].buttons[j].button.setSelected(true);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (confirmReset && messageResult != RESULT_NONE) {
			confirmReset = false;
			if (messageResult == RESULT_YES) {
				for (int i = 0; i < 12; i++) {
					Settings.buttonPosition[i] = i;
				}
				activate();
			}
			messageResult = RESULT_NONE;
			return;
		}
		messageResult = RESULT_NONE;
		if (back.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new ControlOptionsScreen(game, false);
			return;
		}
		if (selectionMode.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			groupSelectionMode = !groupSelectionMode;
			selectionMode.setText(L.string(R.string.options_in_flight_selection_mode, L.string(groupSelectionMode ? R.string.options_in_flight_selection_mode_group : R.string.options_in_flight_selection_mode_button)));
			selectedButton = null;
			for (ButtonConfigData b: uiButton) {
				b.button.setSelected(false);
			}
			return;
		}
		if (reset.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			showQuestionDialog(L.string(R.string.options_in_flight_reset_positions_confirm));
			confirmReset = true;
			return;
		}
		for (ButtonConfigData b: uiButton) {
			if (b.button.isTouched(touch.x, touch.y)) {
				if (selectedButton == null) {
					selectedButton = b;
					if (groupSelectionMode) {
						selectGroup(b);
					} else {
						b.button.setSelected(true);
					}
				} else {
					swapButtons(b);
				}
				break;
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (torusDriveDockingComputerPixmap != null) {
			torusDriveDockingComputerPixmap.dispose();
			torusDriveDockingComputerPixmap = null;
		}
		if (hyperspacePixmap != null) {
			hyperspacePixmap.dispose();
			hyperspacePixmap = null;
		}
		if (galacticHyperspacePixmap != null) {
			galacticHyperspacePixmap.dispose();
			galacticHyperspacePixmap = null;
		}
		if (statusPixmap != null) {
			statusPixmap.dispose();
			statusPixmap = null;
		}
		if (ecmPixmap != null) {
			ecmPixmap.dispose();
			ecmPixmap = null;
		}
		if (escapeCapsulePixmap != null) {
			escapeCapsulePixmap.dispose();
			escapeCapsulePixmap = null;
		}
		if (energyBombPixmap != null) {
			energyBombPixmap.dispose();
			energyBombPixmap = null;
		}
		if (retroRocketsPixmap != null) {
			retroRocketsPixmap.dispose();
			retroRocketsPixmap = null;
		}
		if (ecmJammerPixmap != null) {
			ecmJammerPixmap.dispose();
			ecmJammerPixmap = null;
		}
		if (cloakingDevicePixmap != null) {
			cloakingDevicePixmap.dispose();
			cloakingDevicePixmap = null;
		}
		if (missilePixmap != null) {
			missilePixmap.dispose();
			missilePixmap = null;
		}
		if (firePixmap != null) {
			firePixmap.dispose();
			firePixmap = null;
		}
		if (overlayPixmap != null) {
			overlayPixmap.dispose();
			overlayPixmap = null;
		}
	}

	@Override
	public void loadAssets() {
		Graphics g = game.getGraphics();

		torusDriveDockingComputerPixmap = g.newPixmap("buttons/torus_docking.png");
		hyperspacePixmap                = g.newPixmap("buttons/hyperspace.png");
		galacticHyperspacePixmap        = g.newPixmap("buttons/gal_hyperspace.png");
		statusPixmap                    = g.newPixmap("buttons/status.png");
		ecmPixmap                       = g.newPixmap("buttons/ecm.png");
		escapeCapsulePixmap             = g.newPixmap("buttons/escape_capsule.png");
		energyBombPixmap                = g.newPixmap("buttons/energy_bomb.png");
		retroRocketsPixmap              = g.newPixmap("buttons/retro_rockets.png");
		ecmJammerPixmap                 = g.newPixmap("buttons/ecm_jammer.png");
		cloakingDevicePixmap            = g.newPixmap("buttons/cloaking_device.png");
		missilePixmap                   = g.newPixmap("buttons/missile.png");
		firePixmap                      = g.newPixmap("buttons/fire.png");
		overlayPixmap                   = g.newPixmap("buttons/overlay.png");

		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.INFLIGHT_BUTTONS_OPTIONS_SCREEN;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeBoolean(groupSelectionMode);
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		InFlightButtonsOptionsScreen ifbos = new InFlightButtonsOptionsScreen(alite);
		try {
			ifbos.groupSelectionMode = dis.readBoolean();
		} catch (IOException e) {
			AliteLog.e("Inflight Button Configuration Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(ifbos);
		return true;
	}
}
