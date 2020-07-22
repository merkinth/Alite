package de.phbouillon.android.games.alite.screens.opengl.sprites;

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
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.framework.impl.gl.Sprite;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.colors.AliteColor;

public class ControlPad extends ShipController {
	private static final long serialVersionUID = -3050741925963060286L;

	private final int CPX = Settings.controlPosition == 0 ? Settings.flatButtonDisplay ? 50 : 150 : Settings.flatButtonDisplay ? 1524 : 1424;
	private final int CPY = Settings.flatButtonDisplay ? 300 : 680;
	private final int WIDTH = 350;
	private final int HEIGHT = 350;

	private final Sprite [] controlPad = new Sprite[9];

	private int fingerDown;
	private int activeIndex;

	ControlPad() {
		Rect r = new Rect(CPX, CPY, WIDTH, HEIGHT);
		controlPad[0] = genSprite("cpn", r);
		controlPad[1] = genSprite("cpu", r);
		controlPad[2] = genSprite("cpru", r);
		controlPad[3] = genSprite("cpr", r);
		controlPad[4] = genSprite("cprd", r);
		controlPad[5] = genSprite("cpd", r);
		controlPad[6] = genSprite("cpld", r);
		controlPad[7] = genSprite("cpl", r);
		controlPad[8] = genSprite("cplu", r);
	}

	private void calculateActiveIndex(int x, int y) {
		setDirections(x < 125, x > 225, y < 125, y > 225);
	}

	@Override
	protected boolean isDown() {
		return activeIndex == 4 || activeIndex == 5 || activeIndex == 6;
	}

	@Override
	protected boolean isUp() {
		return activeIndex == 1 || activeIndex == 2 || activeIndex == 8;
	}

	@Override
	protected boolean isRight() {
		return activeIndex > 1 && activeIndex < 5;
	}

	@Override
	protected boolean isLeft() {
		return activeIndex > 5;
	}

	@Override
	boolean handleUI(TouchEvent event) {
		boolean result = false;

		if (Rect.inside(event.x, event.y, CPX, CPY, CPX + WIDTH, CPY + HEIGHT)) {
			if (event.type == TouchEvent.TOUCH_DOWN) {
				fingerDown = TouchEvent.fingerDown(fingerDown, event.pointer);
				calculateActiveIndex(event.x - CPX, event.y - CPY);
			}
			if (event.type == TouchEvent.TOUCH_DRAGGED) {
				calculateActiveIndex(event.x - CPX, event.y - CPY);
			}
			result = true;
		}
		if (event.type == TouchEvent.TOUCH_UP) {
			int f = TouchEvent.fingerUp(fingerDown, event.pointer);
			if (f != fingerDown) {
				fingerDown = f;
				result = true;
			}
		}
		if (!TouchEvent.isDown(fingerDown)) {
			activeIndex = 0;
		}

		return result;
	}

	@Override
	void setDirections(boolean left, boolean right, boolean up, boolean down) {
		if (left) {
			if (up) {
				activeIndex = 8;
			} else if (down) {
				activeIndex = 6;
			} else {
				activeIndex = 7;
			}
		} else if (right) {
			if (up) {
				activeIndex = 2;
			} else if (down) {
				activeIndex = 4;
			} else {
				activeIndex = 3;
			}
		} else {
			if (up) {
				activeIndex = 1;
			} else if (down) {
				activeIndex = 5;
			} else {
				activeIndex = 0;
			}
		}
	}

	@Override
	void render() {
		float a = Settings.alpha * Settings.controlAlpha;
		Alite.get().getGraphics().setColor(AliteColor.argb(a, a, a, a));
		controlPad[activeIndex].justRender();
		Alite.get().getGraphics().setColor(AliteColor.argb(Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
	}
}
