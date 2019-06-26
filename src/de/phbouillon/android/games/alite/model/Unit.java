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

public enum Unit {
	TONNE(1000000),
	KILOGRAM(1000),
	GRAM(1);

	private int value;

	Unit(int value) {
		this.value = value;
	}

	public String toUnitString() {
		switch (this) {
			case TONNE: return L.string(R.string.unit_tonne);
			case KILOGRAM: return L.string(R.string.unit_kilogram);
			case GRAM: return L.string(R.string.unit_gram);
		}
		return "";
	}

	public int getValue() {
		return value;
	}
}
