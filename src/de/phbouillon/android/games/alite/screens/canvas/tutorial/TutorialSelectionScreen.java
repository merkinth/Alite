package de.phbouillon.android.games.alite.screens.canvas.tutorial;

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
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class TutorialSelectionScreen extends AliteScreen {
	private Button introduction;
	private Button trading;
	private Button equipment;
	private Button navigation;
	private Button hud;
	private Button basicFlying;
	private Button advancedFlying;

	TutorialSelectionScreen(Alite game) {
		super(game);
	}

	private Button createButton(int row, String text) {
		return Button.createGradientTitleButton(50, 130 * (row + 1), 1620, 100, text);
	}

	@Override
	public void activate() {
		introduction   = createButton(0, "Introduction");
		trading        = createButton(1, "Trading");
		equipment      = createButton(2, "Equipment");
		navigation     = createButton(3, "Navigation");
		hud            = createButton(4, "HUD");
		basicFlying    = createButton(5, "Basic Flying");
		advancedFlying = createButton(6, "Advanced Flying");
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle("Training Academy");
		introduction.render(g);
		trading.render(g);
		equipment.render(g);
		navigation.render(g);
		hud.render(g);
		basicFlying.render(g);
		advancedFlying.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (introduction.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutIntroduction(game);
			return;
		}
		if (trading.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutTrading(game);
			return;
		}
		if (equipment.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutEquipment(game);
			return;
		}
		if (navigation.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutNavigation(game);
			return;
		}
		if (hud.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutHud(game);
			return;
		}
		if (basicFlying.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutBasicFlying(game);
			return;
		}
		if (advancedFlying.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new TutAdvancedFlying(game, 0);
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUTORIAL_SELECTION_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new TutorialSelectionScreen(alite));
		return true;
	}
}
