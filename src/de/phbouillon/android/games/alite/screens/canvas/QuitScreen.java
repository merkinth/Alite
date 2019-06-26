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

import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteConfig;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.AliteStartManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

import java.io.IOException;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class QuitScreen extends AliteScreen {
	private Screen callingScreen;
	private Screen mockStatusScreen;

	public QuitScreen(Alite game) {
		super(game);
		mockStatusScreen = new StatusScreen(game);
		mockStatusScreen.loadAssets();
		if (game.getCurrentScreen() instanceof FlightScreen) {
			callingScreen = game.getCurrentScreen();
		} else {
			callingScreen = mockStatusScreen;
		}
	}

	@Override
	public void activate() {
		mockStatusScreen.activate();
		setUpForDisplay(game.getGraphics().getVisibleArea());
		showModalQuestionDialog("Do you really want to quit " + AliteConfig.GAME_NAME + "?");
	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (messageResult == RESULT_NONE) {
			return;
		}
		if (messageResult == RESULT_YES) {
			try {
				AliteLog.d("[ALITE]", "Performing autosave. [Quit]");
				game.autoSave();
			} catch (IOException e) {
				AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
			}
			game.finishAffinity();
		}
	}

	@Override
	public void present(float deltaTime) {
		if (messageResult == RESULT_NO) {
			messageResult = RESULT_NONE;
			newScreen = callingScreen;
			if (callingScreen != mockStatusScreen) {
				((FlightScreen) callingScreen).setInformationScreen(null);
			}
			callingScreen.present(deltaTime);
		} else {
			mockStatusScreen.present(deltaTime);
		}
	}

	@Override
	public void dispose() {
		if (mockStatusScreen != null) {
			mockStatusScreen.dispose();
			mockStatusScreen = null;
		}
		super.dispose();
	}

	@Override
	public void loadAssets() {
		mockStatusScreen.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return callingScreen.getScreenCode();
	}
}
