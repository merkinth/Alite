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
import de.phbouillon.android.games.alite.model.generator.StringUtil;

public class Weight implements Comparable <Weight>, Serializable {
	private static final long serialVersionUID = -7041624303034666651L;

	public static final Weight ZERO_GRAMS = new Weight(0);

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
		return getWeight((u == null ? Unit.TONNE : u).getValue() * amount);
	}

	public static Weight tonnes(long t) {
		return getWeight(t * Unit.TONNE.getValue());
	}

	public static Weight kilograms(long kg) {
		return getWeight(kg * Unit.KILOGRAM.getValue());
	}

	public static Weight grams(long g) {
		return getWeight(Math.max(g, 0));
	}

	private static Weight getWeight(long grams) {
		return grams == 0 ? ZERO_GRAMS : new Weight(grams);
	}

	private Weight(long grams) {
		this.grams = grams;
	}

	private Weight getWeightInstance(long grams) {
		return this.grams == grams ? this : grams(grams);
	}

	@Override
	public int compareTo(Weight another) {
		return another == null || grams > another.grams ? 1 : grams == another.grams ? 0 : -1;
	}

	public Weight add(Weight another) {
		return getWeightInstance(grams + another.grams);
	}

	public Weight sub(Weight another) {
		return getWeightInstance(grams - another.grams);
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
		Unit unit = getAppropriateUnit();
		return unit == Unit.GRAM ? StringUtil.format("%d", grams) :
			L.getOneDecimalFormatString(R.string.cash_amount_only, 10 * grams / unit.getValue());
	}

	public String getFormattedString() {
		return getStringWithoutUnit() + getAppropriateUnit().toUnitString();
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
