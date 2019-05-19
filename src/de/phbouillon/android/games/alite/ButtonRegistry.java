package de.phbouillon.android.games.alite;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;

public class ButtonRegistry {
	private static final ButtonRegistry instance = new ButtonRegistry();

	private final Map<Screen, Set<Button>> buttons = new HashMap<>();
	private final Set<Button> messageButtons = new HashSet<>();
	private boolean messageButton;

	private ButtonRegistry() {
	}

	public static ButtonRegistry get() {
		return instance;
	}

	public void setNextAsMessageButton() {
		messageButton = true;
	}

	public void clearMessageButtons() {
		messageButtons.clear();
	}

	public void addButton(Button button) {
		if (messageButton) {
			messageButtons.add(button);
			messageButton = false;
			return;
		}
		Set <Button> definedButtons = buttons.get(Alite.get().getCurrentScreen());
		if (definedButtons == null) {
			definedButtons = new HashSet<>();
		}
		definedButtons.add(button);
		buttons.put(Alite.get().getCurrentScreen(), definedButtons);
	}

	public void removeButtons(Screen definingScreen) {
		buttons.remove(definingScreen);
	}

	public void removeButton(Screen definingScreen, Button button) {
		Set <Button> definedButtons = buttons.get(definingScreen);
		if (definedButtons != null) {
			definedButtons.remove(button);
		}
	}

	public int processTouch(TouchEvent touch) {
		int result = Integer.MAX_VALUE;
		Set<Button> set = messageButtons.isEmpty() ? buttons.get(Alite.get().getCurrentScreen()) : messageButtons;
		if (set == null) {
			return result;
		}

		// To prevent the button from sticking in the pressed position.
		// This can occur when message dialog is quickly opened immediately after a button is pressed
		if (!messageButtons.isEmpty()) {
			Set<Button> screenButtons = buttons.get(Alite.get().getCurrentScreen());
			if (screenButtons != null) {
				for (Button b: screenButtons) {
					b.clearFingerDown();
				}
			}
		}

		if (touch.type == TouchEvent.TOUCH_DOWN) {
			for (Button b: set) {
				if (b.isTouched(touch.x, touch.y)) {
					b.fingerDown(touch.pointer);
				}
			}
			return result;
		}

		if (touch.type == TouchEvent.TOUCH_DRAGGED) {
			for (Button b : set) {
				if (b.isTouched(touch.x, touch.y)) {
					b.fingerDown(touch.pointer);
				} else {
					b.fingerUp(touch.pointer);
				}
			}
			return result;
		}

		if (touch.type == TouchEvent.TOUCH_UP) {
			for (Button b : set) {
				if (b.isTouched(touch.x, touch.y)) {
					b.fingerUp(touch.pointer);
					result = b.getCommand();
				}
			}
		}
		return result;
	}

}
