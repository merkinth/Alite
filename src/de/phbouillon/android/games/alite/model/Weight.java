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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;

public class Weight implements Comparable <Weight>, Serializable {
	private static final long serialVersionUID = -7041624303034666651L;

	private final long grams;

	private void writeObject(ObjectOutputStream out)
            throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Weight " + this, e);
			throw e;
		}
    }

	public static Weight unit(Unit u, long amount) {
		switch (u) {
			case TONNE: return tonnes(amount);
			case KILOGRAM: return kilograms(amount);
			case GRAM: return grams(amount);
		}
		return null;
	}

	public static Weight tonnes(long t) {
		return new Weight(t * Unit.TONNE.getValue());
	}

	public static Weight kilograms(long kg) {
		return new Weight(kg * Unit.KILOGRAM.getValue());
	}

	public static Weight grams(long g) {
		if (g < 0) {
			g = 0;
		}
		return new Weight(g);
	}

	private Weight(long grams) {
		this.grams = grams;
	}

	public Weight set(Weight weight) {
		return grams(weight.getWeightInGrams());
	}

	@Override
	public int compareTo(Weight another) {
		return another == null || grams > another.grams ? 1 : grams == another.grams ? 0 : -1;
	}

	public Weight add(Weight another) {
		return grams(grams + another.grams);
	}

	public Weight sub(Weight another) {
		return grams(grams - another.grams);
	}

	public Unit getAppropriateUnit() {
		return grams < Unit.KILOGRAM.getValue() ? Unit.GRAM : grams < Unit.TONNE.getValue() ? Unit.KILOGRAM : Unit.TONNE;
	}

	public int getQuantityInAppropriateUnit() {
		return (int) (grams / getAppropriateUnit().getValue());
	}

	public long getWeightInGrams() {
		return grams;
	}

	public String getStringWithoutUnit() {
		if (grams < Unit.KILOGRAM.getValue()) {
			return String.format(L.currentLocale, "%d", grams);
		}
		return L.getOneDecimalFormatString(R.string.cash_amount_only, 10 * grams /
			(grams < Unit.TONNE.getValue() ? Unit.KILOGRAM : Unit.TONNE).getValue());
	}

	public String getFormattedString() {
		return getStringWithoutUnit() + getAppropriateUnit().toUnitString();
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
