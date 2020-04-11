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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutIntroduction;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ControlOptionsScreen extends OptionsScreen {
	private Button shipControlMode;
	private Button controlDisplaySide;
	private Button reverseDiveClimb;

	private Button radarTapZoom;
	private Button buttonPositionOptions;
	private Button linearLayout;
	private Button back;
	private boolean forwardToIntroduction;

	public ControlOptionsScreen(boolean forwardToIntroduction) {
		this.forwardToIntroduction = forwardToIntroduction;
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_OPTIONS);
	}

	@Override
	public void activate() {
		shipControlMode = createButton(0, L.string(R.string.options_ctrl_ship_control_mode, Settings.controlMode.getDescription()));
		controlDisplaySide = createButton(1, computeControlDisplaySideText())
			.setVisible(Settings.controlMode != ShipControl.ACCELEROMETER &&
				Settings.controlMode != ShipControl.ALTERNATIVE_ACCELEROMETER);

		buttonPositionOptions = createButton(2, L.string(R.string.options_ctrl_button_position_options))
			.setVisible(!forwardToIntroduction);
		reverseDiveClimb = createButton(3, L.string(R.string.options_ctrl_reverse_climb,
			L.string(Settings.reversePitch ? R.string.options_yes : R.string.options_no)));
		radarTapZoom = createButton(4, L.string(R.string.options_ctrl_radar_change_view,
			L.string(Settings.tapRadarToChangeView ? R.string.options_ctrl_radar_change_view_tap : R.string.options_ctrl_radar_change_view_slide)));
		linearLayout = createButton(5, L.string(R.string.options_ctrl_linear_layout,
			L.string(Settings.flatButtonDisplay ? R.string.options_yes : R.string.options_no)));

		back = createButton(6, L.string(forwardToIntroduction ? R.string.options_ctrl_forward_to_introduction : R.string.options_back));

	}

	private String computeControlDisplaySideText() {
		String controlPosition = L.string(Settings.controlPosition == 0 ? R.string.ship_ctrl_side_text_left_position : R.string.ship_ctrl_side_text_right_position);
		switch (Settings.controlMode) {
			case CONTROL_PAD: return L.string(R.string.ship_ctrl_side_text_control_pad, controlPosition);
			case CURSOR_BLOCK: return L.string(R.string.ship_ctrl_side_text_cursor_block, controlPosition);
			case CURSOR_SPLIT_BLOCK: return L.string(R.string.ship_ctrl_side_text_cursor_split_block, controlPosition);
		}
		return "";
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_control_options));
		shipControlMode.render(g);
		controlDisplaySide.render(g);
		buttonPositionOptions.render(g);
		reverseDiveClimb.render(g);
		radarTapZoom.render(g);
		linearLayout.render(g);

		back.render(g);
		if (forwardToIntroduction) {
			centerText(L.string(R.string.options_ctrl_forward_to_introduction_help),
				115, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		}
		if (Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER) {
			centerText(L.string(R.string.ship_ctrl_alt_acc_help_line1),
				275, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
			centerText(L.string(R.string.ship_ctrl_alt_acc_help_line2),
				315, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (shipControlMode.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				int val = Settings.controlMode.ordinal();
				val++;
				if (val >= ShipControl.values().length) {
					val = 0;
				}
				Settings.controlMode = ShipControl.values()[val];
				if (Settings.controlMode == ShipControl.ACCELEROMETER && game.getInput().isAlternativeAccelerometer()) {
					game.getInput().switchAccelerometerHandler();
				} else if (Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER && !game.getInput().isAlternativeAccelerometer()) {
					game.getInput().switchAccelerometerHandler();
				}
				shipControlMode.setText(L.string(R.string.options_ctrl_ship_control_mode, Settings.controlMode.getDescription()));
				controlDisplaySide.setVisible(Settings.controlMode != ShipControl.ACCELEROMETER &&
						Settings.controlMode != ShipControl.ALTERNATIVE_ACCELEROMETER)
					.setText(computeControlDisplaySideText());
				Settings.save(game.getFileIO());
			} else if (controlDisplaySide.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
                Settings.controlPosition = -Settings.controlPosition + 1;
                controlDisplaySide.setText(computeControlDisplaySideText());
				Settings.save(game.getFileIO());
			} else if (buttonPositionOptions.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new InFlightButtonsOptionsScreen();
			} else if (linearLayout.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.flatButtonDisplay = !Settings.flatButtonDisplay;
				linearLayout.setText(L.string(R.string.options_ctrl_linear_layout, L.string(Settings.flatButtonDisplay ? R.string.options_yes : R.string.options_no)));
				Settings.save(game.getFileIO());
			} else if (back.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = forwardToIntroduction ? new TutIntroduction() : new OptionsScreen();
			} else if (reverseDiveClimb.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.reversePitch = !Settings.reversePitch;
				reverseDiveClimb.setText(L.string(R.string.options_ctrl_reverse_climb,
					L.string(Settings.reversePitch ? R.string.options_yes : R.string.options_no)));
				Settings.save(game.getFileIO());
			} else if (radarTapZoom.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.tapRadarToChangeView = !Settings.tapRadarToChangeView;
				radarTapZoom.setText(L.string(R.string.options_ctrl_radar_change_view,
					L.string(Settings.tapRadarToChangeView ? R.string.options_ctrl_radar_change_view_tap : R.string.options_ctrl_radar_change_view_slide)));
				Settings.save(game.getFileIO());
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.CONTROL_OPTIONS_SCREEN;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeBoolean(forwardToIntroduction);
	}

}
