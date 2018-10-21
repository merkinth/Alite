package de.phbouillon.android.games.alite.model;

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

import de.phbouillon.android.games.alite.colors.ColorScheme;

public enum Condition {
	DOCKED("Docked", ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN)),
	GREEN("Green",   ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN)),
	YELLOW("Yellow", ColorScheme.get(ColorScheme.COLOR_CONDITION_YELLOW)),
	RED("Red",       ColorScheme.get(ColorScheme.COLOR_CONDITION_RED));

	private String name;
	private int color;

	Condition(String name, int color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public static void update() {
		DOCKED.color = ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN);
		GREEN.color = ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN);
		YELLOW.color = ColorScheme.get(ColorScheme.COLOR_CONDITION_YELLOW);
		RED.color = ColorScheme.get(ColorScheme.COLOR_CONDITION_RED);
	}
}
