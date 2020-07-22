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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class TutorialSelectionScreen extends AliteScreen {
	private final Button[] buttons = new Button[7];

	private Button createButton(int row, String text) {
		return Button.createGradientTitleButton(50, 130 * (row + 1), 1620, 100, text);
	}

	@Override
	public void activate() {
		game.updateMedals();
		buttons[0] = createButton(0, L.string(R.string.tutorial_selection_introduction))
			.setEvent(b -> newScreen = new TutIntroduction());
		buttons[1] = createButton(1, L.string(R.string.tutorial_selection_trading))
			.setEvent(b -> newScreen = new TutTrading());
		buttons[2] = createButton(2, L.string(R.string.tutorial_selection_equipment))
			.setEvent(b -> newScreen = new TutEquipment());
		buttons[3] = createButton(3, L.string(R.string.tutorial_selection_navigation))
			.setEvent(b -> newScreen = new TutNavigation());
		buttons[4] = createButton(4, L.string(R.string.tutorial_selection_hud))
			.setEvent(b -> newScreen = new TutHud((FlightScreen) null));
		buttons[5] = createButton(5, L.string(R.string.tutorial_selection_basic_flying))
			.setEvent(b -> newScreen = new TutBasicFlying((FlightScreen) null));
		buttons[6] = createButton(6, L.string(R.string.tutorial_selection_advanced_flying))
			.setEvent(b -> newScreen = new TutAdvancedFlying(0));
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_training_academy));
		for (Button b : buttons) {
			b.render(g);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		for (Button b : buttons) {
			if (b.isPressed(touch)) {
				b.onEvent();
				return;
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUTORIAL_SELECTION_SCREEN;
	}

}
