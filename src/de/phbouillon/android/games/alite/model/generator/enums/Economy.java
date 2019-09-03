package de.phbouillon.android.games.alite.model.generator.enums;

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

public enum Economy {
	RICH_INDUSTRIAL(ColorScheme.COLOR_RICH_INDUSTRIAL),
	AVERAGE_INDUSTRIAL(ColorScheme.COLOR_AVERAGE_INDUSTRIAL),
	POOR_INDUSTRIAL(ColorScheme.COLOR_POOR_INDUSTRIAL),
	MAINLY_INDUSTRIAL(ColorScheme.COLOR_MAIN_INDUSTRIAL),
	MAINLY_AGRICULTURAL(ColorScheme.COLOR_MAIN_AGRICULTURAL),
	RICH_AGRICULTURAL(ColorScheme.COLOR_RICH_AGRICULTURAL),
	AVERAGE_AGRICULTURAL(ColorScheme.COLOR_AVERAGE_AGRICULTURAL),
	POOR_AGRICULTURAL(ColorScheme.COLOR_POOR_AGRICULTURAL);

	private int colorIndex;

	Economy(int colorIndex) {
		this.colorIndex = colorIndex;
	}

	public String getDescription() {
		switch (this) {
			case RICH_INDUSTRIAL: return L.string(R.string.economy_rich_industrial);
			case AVERAGE_INDUSTRIAL: return L.string(R.string.economy_average_industrial);
			case POOR_INDUSTRIAL: return L.string(R.string.economy_poor_industrial);
			case MAINLY_INDUSTRIAL: return L.string(R.string.economy_mainly_industrial);
			case MAINLY_AGRICULTURAL: return L.string(R.string.economy_mainly_agricultural);
			case RICH_AGRICULTURAL: return L.string(R.string.economy_rich_agricultural);
			case AVERAGE_AGRICULTURAL: return L.string(R.string.economy_average_agricultural);
			case POOR_AGRICULTURAL: return L.string(R.string.economy_poor_agricultural);
		}
		return "";
	}

	public int getColor() {
		return ColorScheme.get(colorIndex);
	}

}
