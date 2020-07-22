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

import de.phbouillon.android.games.alite.model.trading.TradeGood;
import org.json.JSONException;
import org.json.JSONObject;

public class InventoryItem {
	private final TradeGood good;
	private Weight weight = Weight.ZERO_GRAMS;
	private Weight unpunished = Weight.ZERO_GRAMS;
	// It is set if scooped object is not cargo
	// EscapeCapsule -> SLAVES, Thargon -> ALIEN_ITEMS, Alloy (Platlet) -> ALLOYS
	private int nonCargo;
	private long totalBuyPrice;

	public InventoryItem(TradeGood good) {
		this.good = good;
	}

	public void add(Weight w, long price) {
		totalBuyPrice += price;
		weight = weight.add(w);
	}

	public void set(Weight w, long price) {
		totalBuyPrice = price;
		weight = w;
	}

	public void addUnpunished(Weight weight, int nonCargoInTonne) {
		unpunished = unpunished.add(weight);
		nonCargo += nonCargoInTonne;
	}

	public void subUnpunished(Weight weight) {
		unpunished = unpunished.sub(weight);
		nonCargo = Math.max(nonCargo - weight.getQuantityInAppropriateUnit(), 0);
	}

	public void resetUnpunished() {
		unpunished = Weight.ZERO_GRAMS;
		nonCargo = 0;
	}

	public Weight getUnpunished() {
		return unpunished;
	}

	public int getNonCargo() {
		return nonCargo;
	}

	public long getPrice() {
		return totalBuyPrice;
	}

	public Weight getWeight() {
		return weight;
	}

	public TradeGood getGood() {
		return good;
	}

	public JSONObject toJson(JSONObject inventory) throws JSONException {
		return inventory
			.put("weight", (int) weight.getWeightInGrams())
			.put("price", totalBuyPrice)
			.put("unpunishedWeight", unpunished.getWeightInGrams())
			.put("nonCargoWeight", nonCargo);
	}

	public void fromJson(JSONObject inventory) throws JSONException {
		set(Weight.grams(inventory.getLong("weight")), inventory.getLong("price"));
		addUnpunished(Weight.grams(inventory.getLong("unpunishedWeight")), inventory.optInt("nonCargoWeight"));
	}

}
