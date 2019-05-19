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

import java.io.DataInputStream;
import java.io.IOException;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Button;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.SoundManager;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class SaveScreen extends CatalogScreen {
	private Button saveNewCommanderButton;
	private boolean confirmedSave = false;

	SaveScreen(Alite game, String title) {
		super(game, title);
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		alite.setScreen(new SaveScreen(alite, "Save Commander"));
		return true;
	}

	@Override
	public void activate() {
		super.activate();
		saveNewCommanderButton = Button.createGradientRegularButton(50, 950, 500, 100, "Save New Commander");
		deleteButton = null;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (messageResult == RESULT_YES) {
			try {
				game.saveCommander(inputText);
			} catch (IOException e) {
				AliteLog.e("[ALITE] SaveCommander", "Error while saving commander.", e);
			}
			showMessageDialog("Commander " + inputText + " saved successfully.");
			confirmedSave = true;
		}
		messageResult = RESULT_NONE;
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		super.processTouch(touch);
		if (confirmedSave) {
			newScreen = new StatusScreen(game);
			confirmedSave = false;
		}
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (saveNewCommanderButton.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				popupTextInput("Enter the new commander's name:",
					game.getPlayer().getName(), 16);
			}
		}
		if (selectedCommanderData.size() != 1) {
			return;
		}
		if (messageResult == RESULT_NONE) {
			showQuestionDialog(String.format("Are you sure you want to overwrite Commander %s?",
				selectedCommanderData.get(0).getName()));
			confirmDelete = false;
			SoundManager.play(Assets.alert);
			return;
		}
		if (messageResult == RESULT_YES) {
			try {
				if (selectedCommanderData.get(0).isAutoSaved()) {
					game.saveCommander(selectedCommanderData.get(0).getName());
				} else {
					game.saveCommander(selectedCommanderData.get(0).getName(), selectedCommanderData.get(0).getFileName());
				}
				showMessageDialog(String.format("Commander %s saved successfully.", selectedCommanderData.get(0).getName()));
				SoundManager.play(Assets.alert);
				confirmedSave = true;
			} catch (IOException e) {
				showMessageDialog(String.format("Error while saving commander %s: %s", selectedCommanderData.get(0).getName(), e.getMessage()));
			}
		}
		clearSelection();
		messageResult = RESULT_NONE;
	}

	@Override
	public void present(float deltaTime) {
		super.present(deltaTime);
		Graphics g = game.getGraphics();
		saveNewCommanderButton.render(g);
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.SAVE_SCREEN;
	}
}
