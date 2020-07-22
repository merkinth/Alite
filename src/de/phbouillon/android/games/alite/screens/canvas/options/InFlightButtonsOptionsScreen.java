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

import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.SpriteData;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class InFlightButtonsOptionsScreen extends AliteScreen {
	private final ButtonConfigData[] uiButton = new ButtonConfigData[12];

	private Button selectionMode;
	private Button reset;
	private Button back;
	private ButtonConfigData selectedButton = null;
	private final ButtonConfigGroup[] buttonGroups = new ButtonConfigGroup[4];
	private boolean groupSelectionMode = true;
	private boolean confirmReset = false;

	private static class ButtonConfigData {
		Button button;
		int groupIndex;
		int settingsPosition;
		String name;
	}

	private static class ButtonConfigGroup {
		ButtonConfigData[] buttons = new ButtonConfigData[3];
	}

	InFlightButtonsOptionsScreen() {
	}

	public InFlightButtonsOptionsScreen(boolean groupSelectionMode) {
		this.groupSelectionMode = groupSelectionMode;
	}

	@Override
	public void activate() {
		uiButton[Settings.FIRE] = createButton(Settings.buttonPosition[Settings.FIRE],
			"fire", Settings.FIRE, L.string(R.string.in_flight_button_fire));
		uiButton[Settings.MISSILE] = createButton(Settings.buttonPosition[Settings.MISSILE],
			"missile", Settings.MISSILE, L.string(R.string.in_flight_button_missile));
		uiButton[Settings.ECM] = createButton(Settings.buttonPosition[Settings.ECM],
			"ecm", Settings.ECM, L.string(R.string.in_flight_button_ecm));
		uiButton[Settings.RETRO_ROCKETS] = createButton(Settings.buttonPosition[Settings.RETRO_ROCKETS],
			"retro_rockets", Settings.RETRO_ROCKETS, L.string(R.string.in_flight_button_retro_rockets));
		uiButton[Settings.ESCAPE_CAPSULE] = createButton(Settings.buttonPosition[Settings.ESCAPE_CAPSULE],
			"escape_capsule", Settings.ESCAPE_CAPSULE, L.string(R.string.in_flight_button_escape_capsule));
		uiButton[Settings.ENERGY_BOMB] = createButton(Settings.buttonPosition[Settings.ENERGY_BOMB],
			"energy_bomb", Settings.ENERGY_BOMB, L.string(R.string.in_flight_button_energy_bomb));
		uiButton[Settings.STATUS] = createButton(Settings.buttonPosition[Settings.STATUS],
			"status", Settings.STATUS, L.string(R.string.in_flight_button_status));
		uiButton[Settings.TORUS] = createButton(Settings.buttonPosition[Settings.TORUS],
			"torus_docking", Settings.TORUS, L.string(R.string.in_flight_button_drive_docking));
		uiButton[Settings.HYPERSPACE] = createButton(Settings.buttonPosition[Settings.HYPERSPACE],
			"hyperspace", Settings.HYPERSPACE, L.string(R.string.in_flight_button_hyperspace));
		uiButton[Settings.GALACTIC_HYPERSPACE] = createButton(Settings.buttonPosition[Settings.GALACTIC_HYPERSPACE],
			"gal_hyperspace", Settings.GALACTIC_HYPERSPACE, L.string(R.string.in_flight_button_galactic_hyperspace));
		uiButton[Settings.CLOAKING_DEVICE] = createButton(Settings.buttonPosition[Settings.CLOAKING_DEVICE],
			"cloaking_device", Settings.CLOAKING_DEVICE, L.string(R.string.in_flight_button_cloaking_device));
		uiButton[Settings.ECM_JAMMER] = createButton(Settings.buttonPosition[Settings.ECM_JAMMER],
			"ecm_jammer", Settings.ECM_JAMMER, L.string(R.string.in_flight_button_ecm_jammer));

		selectionMode = Button.createGradientTitleButton(50, 860, 1620, 100,
			L.string(R.string.options_in_flight_selection_mode, L.string(groupSelectionMode ? R.string.options_in_flight_selection_mode_group : R.string.options_in_flight_selection_mode_button)));
		back = Button.createGradientTitleButton(50, 970, 780, 100, L.string(R.string.options_back));
		reset = Button.createGradientTitleButton(890, 970, 780, 100, L.string(R.string.options_in_flight_reset_positions));
	}

	private ButtonConfigData createButton(int position, String spriteName, int settingsPosition, String name) {
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

		Button result = Button.createPictureButton(xt, yt, 200, 200, pics.get(spriteName));

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
				g.drawPixmap(pics.get("overlay"), b.button.getX(), b.button.getY());
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
		if (back.isPressed(touch)) {
			newScreen = new ControlOptionsScreen(false);
			return;
		}
		if (selectionMode.isPressed(touch)) {
			groupSelectionMode = !groupSelectionMode;
			selectionMode.setText(L.string(R.string.options_in_flight_selection_mode,
				L.string(groupSelectionMode ? R.string.options_in_flight_selection_mode_group :
					R.string.options_in_flight_selection_mode_button)));
			selectedButton = null;
			for (ButtonConfigData b: uiButton) {
				b.button.setSelected(false);
			}
			return;
		}
		if (reset.isPressed(touch)) {
			showQuestionDialog(L.string(R.string.options_in_flight_reset_positions_confirm));
			confirmReset = true;
			return;
		}

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
	public void loadAssets() {
		addPicturesFromAliteButtonsTexture("torus_docking", "hyperspace", "gal_hyperspace",
			"status", "ecm", "escape_capsule", "energy_bomb", "retro_rockets", "ecm_jammer",
			"cloaking_device", "missile", "fire", "overlay");
		super.loadAssets();
	}

	private void addPicturesFromAliteButtonsTexture(String... spriteNames) {
		if (!pics.containsKey("buttons")) {
			game.getTextureManager().addTexture(AliteButtons.TEXTURE_FILE);
			pics.put("buttons", game.getGraphics().newPixmap(AliteButtons.TEXTURE_FILE));
		}
		for (String spriteName : spriteNames) {
			SpriteData data = game.getTextureManager().getSprite(AliteButtons.TEXTURE_FILE, spriteName);
			Pixmap pixmap = pics.get("buttons");
			pics.put(spriteName, game.getGraphics().newPixmap(Bitmap.createBitmap(pixmap.getBitmap(),
				(int) (data.x * pixmap.getWidth()), (int) (data.y * pixmap.getHeight()),
				(int) data.origWidth, (int) data.origHeight), spriteName));
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.INFLIGHT_BUTTONS_OPTIONS_SCREEN;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeBoolean(groupSelectionMode);
	}

}
