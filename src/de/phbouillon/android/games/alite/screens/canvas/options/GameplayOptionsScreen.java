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
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Button;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class GameplayOptionsScreen extends OptionsScreen {
	private Button difficultyLevel;
	private Button keyboardLayout;
	private Button laserAutoFire;
	private Button dockingSpeed;
	private Button autoId;
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
		keyboardLayout    = createButton(5, getKeyboardButtonText());

		back              = createButton(6, "Back");
	}

	private String getKeyboardButtonText() {
		return "Keyboard: " + Settings.keyboardLayout;
	}

	private String getLaserButtonText() {
		return "Laser: " + (Settings.laserButtonAutoFire ? "Auto Fire" : "Single Shot");
	}

	private String getDockingComputerButtonText() {
		return "Docking Computer: " + (Settings.dockingComputerSpeed == 0 ? "Slow" :
			Settings.dockingComputerSpeed == 1 ? "Medium" : "Fast");
	}

	private String getAutoIdButtonText() {
		return "Auto Id: " + (Settings.autoId ? "On" : "Off");
	}

	private String getDifficultyButtonText() {
		return "Difficulty Level: " + getDifficultyString();
	}

	private String getDifficultyString() {
		switch (Settings.difficultyLevel) {
			case 0: return "Sedate";
			case 1: return "Very easy";
			case 2: return "Easy";
			case 3: return "Normal";
			case 4: return "Hard";
			case 5: return "Extremely Hard";
		}
		return "Normal";
	}

	private String getDifficultyDescription() {
		switch (Settings.difficultyLevel) {
			case 0: return "No enemies at all. Only Vipers attack if your legal status requires it.";
			case 1: return "Very few enemies. At most 2 at a time. No Thargoids.";
			case 2: return "Few enemies. At most 3 at a time. Thargoids are rare.";
			case 3: return "Normal number of enemies. At most 4 at a time. Some Thargoids.";
			case 4: return "Enemies appear frequently. At most 6 at a time. Thargoids are abundant.";
			case 5: return "Armageddon. Be warned: Frustration level is extremely high.";
		}
		return "";
	}

	@Override
	public void present(float deltaTime) {
		if (disposed) {
			return;
		}
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle("Gameplay Options");
		difficultyLevel.render(g);
		String text = getDifficultyDescription();
		centerText(text, 315, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		autoId.render(g);
		dockingSpeed.render(g);
		laserAutoFire.render(g);
		keyboardLayout.render(g);

		back.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (difficultyLevel.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.difficultyLevel++;
				if (Settings.difficultyLevel > 5) {
					Settings.difficultyLevel = 0;
				}
				difficultyLevel.setText(getDifficultyButtonText());
				Settings.save(game.getFileIO());
			} else if (autoId.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.autoId = !Settings.autoId;
				autoId.setText(getAutoIdButtonText());
				Settings.save(game.getFileIO());
			} else if (keyboardLayout.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				if ("QWERTY".equals(Settings.keyboardLayout)) {
					Settings.keyboardLayout = "QWERTZ";
				} else {
					Settings.keyboardLayout = "QWERTY";
				}
				keyboardLayout.setText(getKeyboardButtonText());
				Settings.save(game.getFileIO());
			} else if (dockingSpeed.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.dockingComputerSpeed++;
				if (Settings.dockingComputerSpeed > 2) {
					Settings.dockingComputerSpeed = 0;
				}
				dockingSpeed.setText(getDockingComputerButtonText());
				Settings.save(game.getFileIO());
			} else if (laserAutoFire.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.laserButtonAutoFire = !Settings.laserButtonAutoFire;
				laserAutoFire.setText(getLaserButtonText());
				Settings.save(game.getFileIO());
			} else if (back.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new OptionsScreen(game);
			}
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
