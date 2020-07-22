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

public class CursorKeys extends ShipController {
	private static final long serialVersionUID = 2357990555586247354L;

	private final Sprite[] cursorKeys = new Sprite[8];
	private final Rect[] buttonCoordinates = new Rect[4];
	private final int[] downPointer = new int[4];

	CursorKeys(boolean split) {
		int WIDTH = 192;
		int HEIGHT = 192;
		int GAP = 16;
		int CPX;
		int CPX2;
		int CPY;
		int CPY2;
		if (split) {
			int x1 = Settings.flatButtonDisplay ? 20 : 120;
			int x2 = Settings.flatButtonDisplay ? Settings.controlPosition == 1 ? 1900 - WIDTH : 1494 : 1394;
			CPX = Settings.controlPosition == 0 ? x1 : x2;
			CPX2 = Settings.controlPosition == 1 ? x1 : x2;
			CPY = Settings.flatButtonDisplay ? 350 : 740;
			buttonCoordinates[0] = new Rect(CPX + (Settings.flatButtonDisplay ? 0 : WIDTH >> 1), CPY - (HEIGHT >> 1), WIDTH, HEIGHT);
			buttonCoordinates[1] = new Rect(CPX2 + WIDTH + GAP, CPY, WIDTH, HEIGHT);
			buttonCoordinates[2] = new Rect(CPX + (Settings.flatButtonDisplay ? 0 : WIDTH >> 1), CPY - (HEIGHT >> 1) + GAP + HEIGHT, WIDTH, HEIGHT);
			buttonCoordinates[3] = new Rect(CPX2, CPY, WIDTH, HEIGHT);
		} else {
			CPX = Settings.controlPosition == 0 ? 30 : 1304;
			CPY = Settings.flatButtonDisplay ? 270 : 670;
			CPY2 = CPY + HEIGHT + GAP;
			buttonCoordinates[0] = new Rect(CPX + WIDTH + GAP, CPY, WIDTH, HEIGHT);
			buttonCoordinates[1] = new Rect(CPX + ((WIDTH + GAP) << 1), CPY2, WIDTH, HEIGHT);
			buttonCoordinates[2] = new Rect(CPX + WIDTH + GAP, CPY2, WIDTH, HEIGHT);
			buttonCoordinates[3] = new Rect(CPX, CPY2, WIDTH, HEIGHT);
		}
		cursorKeys[0] = genSprite("cu", buttonCoordinates[0]);
		cursorKeys[1] = genSprite("cr", buttonCoordinates[1]);
		cursorKeys[2] = genSprite("cd", buttonCoordinates[2]);
		cursorKeys[3] = genSprite("cl", buttonCoordinates[3]);
		cursorKeys[4] = genSprite("cuh", buttonCoordinates[0]);
		cursorKeys[5] = genSprite("crh", buttonCoordinates[1]);
		cursorKeys[6] = genSprite("cdh", buttonCoordinates[2]);
		cursorKeys[7] = genSprite("clh", buttonCoordinates[3]);
	}

	@Override
	void setDirections(boolean left, boolean right, boolean up, boolean down) {
		// 728 is an arbitrary value: Just use any value that is highly unlikely to be
		// used as a real touch pointer... Unless someone has 728 fingers (simultaneously)
		// on the device and said device supports 728 simultaneous touch inputs, we should
		// be safe :).
		downPointer[0] = up ? 728 : 0;
		downPointer[1] = right ? 728 : 0;
		downPointer[2] = down ? 728 : 0;
		downPointer[3] = left ? 728 : 0;
	}

	@Override
	protected boolean isDown() {
		return TouchEvent.isDown(downPointer[2]);
	}

	@Override
	protected boolean isUp() {
		return TouchEvent.isDown(downPointer[0]);
	}

	@Override
	protected boolean isRight() {
		return TouchEvent.isDown(downPointer[1]);
	}

	@Override
	protected boolean isLeft() {
		return TouchEvent.isDown(downPointer[3]);
	}

	@Override
	boolean handleUI(TouchEvent event) {
		boolean result = false;

		if (event.type == TouchEvent.TOUCH_DOWN) {
			for (int i = 0; i < 4; i++) {
				if (Rect.inside(event.x, event.y, buttonCoordinates[i].left, buttonCoordinates[i].top,
						buttonCoordinates[i].left + buttonCoordinates[i].right,
						buttonCoordinates[i].top + buttonCoordinates[i].bottom)) {
					result = true;
					downPointer[i] = TouchEvent.fingerDown(downPointer[i], event.pointer);
				}
			}
			return result;
		}
		if (event.type == TouchEvent.TOUCH_UP) {
			for (int i = 0; i < 4; i++) {
				int f = TouchEvent.fingerUp(downPointer[i], event.pointer);
				if (f != downPointer[i]) {
					downPointer[i] = f;
					result = true;
				}
			}
			return result;
		}
		if (event.type == TouchEvent.TOUCH_DRAGGED) {
			for (int i = 0; i < 4; i++) {
				if (TouchEvent.isDown(downPointer[i], event.pointer)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	void render() {
		float a = Settings.alpha * Settings.controlAlpha;
		Alite.get().getGraphics().setColor(AliteColor.argb(a, a, a, a));
		for (int i = 0; i < 4; i++) {
			cursorKeys[downPointer[i] > 0 ? i + 4 : i].justRender();
		}
		Alite.get().getGraphics().setColor(AliteColor.argb(Settings.alpha, 0.2f * Settings.alpha, Settings.alpha, Settings.alpha));
	}
}
