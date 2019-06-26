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

import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.colors.ColorScheme;

public enum Condition {
	DOCKED(ColorScheme.COLOR_CONDITION_GREEN),
	GREEN(ColorScheme.COLOR_CONDITION_GREEN),
	YELLOW(ColorScheme.COLOR_CONDITION_YELLOW),
	RED(ColorScheme.COLOR_CONDITION_RED);

	private int colorIndex;

	Condition(int colorIndex) {
		this.colorIndex = colorIndex;
	}

	public String getName() {
		switch (this) {
			case DOCKED: return L.string(R.string.condition_docked);
			case GREEN: return L.string(R.string.condition_green);
			case YELLOW: return L.string(R.string.condition_yellow);
			case RED: return L.string(R.string.condition_red);
		}
		return "";
	}

	public int getColor() {
		return ColorScheme.get(colorIndex);
	}

}
