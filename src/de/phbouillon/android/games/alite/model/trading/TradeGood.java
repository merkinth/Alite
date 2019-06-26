package de.phbouillon.android.games.alite.model.trading;

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
import de.phbouillon.android.games.alite.model.Unit;

public class TradeGood implements Serializable {
	private static final long serialVersionUID = 5358106266043560822L;

	private int id;
	private final int       basePrice;
	private final int       gradient;
	private final int       baseQuantity;
	private final int       maskByte;
	private final float     legalityType;
	private final Unit unit;
	private final int name;
	private final int    [] averagePrice;

	public TradeGood(int id, int basePrice, int gradient, int baseQuantity, int maskByte, Unit unit, int name, int ...averagePrice) {
		this.id = id;
		this.basePrice    = basePrice;
		this.gradient     = gradient;
		this.baseQuantity = baseQuantity;
		this.maskByte     = maskByte;
		this.unit         = unit;
		this.name         = name;
		this.legalityType = 0;
		this.averagePrice = averagePrice;
	}

	public TradeGood(int id, int basePrice, int gradient, int baseQuantity, int maskByte, float legalityType, Unit unit, int name, int ...averagePrice) {
		this.id = id;
		this.basePrice    = basePrice;
		this.gradient     = gradient;
		this.baseQuantity = baseQuantity;
		this.maskByte     = maskByte;
		this.unit         = unit;
		this.name         = name;
		this.legalityType = legalityType;
		this.averagePrice = averagePrice;
	}

	private void writeObject(ObjectOutputStream out)
            throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "TradeGood " + getName(), e);
			throw e;
		}
    }

	public int getId() {
		return id;
	}

	public final int getBasePrice() {
		return basePrice;
	}

	public final int getGradient() {
		return gradient;
	}

	public final int getBaseQuantity() {
		return baseQuantity;
	}

	public final int getMaskByte() {
		return maskByte;
	}

	public final Unit getUnit() {
		return unit;
	}

	public final String getName() {
		return L.string(name);
	}

	public float getLegalityType() {
		return legalityType;
	}

	public int getAveragePrice(int galaxyNumber) {
		return galaxyNumber > 0 && galaxyNumber < 9 ? averagePrice[galaxyNumber] : averagePrice[0];
	}

}
