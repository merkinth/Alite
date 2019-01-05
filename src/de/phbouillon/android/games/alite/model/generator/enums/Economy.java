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

import de.phbouillon.android.games.alite.colors.ColorScheme;

public enum Economy {
	RICH_INDUSTRIAL("Rich Industrial", ColorScheme.COLOR_RICH_INDUSTRIAL),
	AVERAGE_INDUSTRIAL("Average Industrial", ColorScheme.COLOR_AVERAGE_INDUSTRIAL),
	POOR_INDUSTRIAL("Poor Industrial", ColorScheme.COLOR_POOR_INDUSTRIAL),
	MAINLY_INDUSTRIAL("Mainly Industrial", ColorScheme.COLOR_MAIN_INDUSTRIAL),
	MAINLY_AGRICULTURAL("Mainly Agricultural", ColorScheme.COLOR_MAIN_AGRICULTURAL),
	RICH_AGRICULTURAL("Rich Agricultural", ColorScheme.COLOR_RICH_AGRICULTURAL),
	AVERAGE_AGRICULTURAL("Average Agricultural", ColorScheme.COLOR_AVERAGE_AGRICULTURAL),
	POOR_AGRICULTURAL("Poor Agricultural", ColorScheme.COLOR_POOR_AGRICULTURAL);

	private String description;
	private int colorIndex;

	Economy(String desc, int colorIndex) {
		this.description = desc;
		this.colorIndex = colorIndex;
	}

	public String getDescription() {
		return description;
	}

	public int getColor() {
		return ColorScheme.get(colorIndex);
	}

	public String toString() {
		return description;
	}

}
