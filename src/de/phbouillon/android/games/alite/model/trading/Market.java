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

import java.util.HashMap;
import java.util.Map;

import de.phbouillon.android.games.alite.model.generator.SystemData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Market {
	protected SystemData system;
	protected int        fluct;
	protected final Map <TradeGood, Integer> quantity;
	protected final Map <TradeGood, Integer> price;
	protected final TradeGoodStore store;

	protected Market(TradeGoodStore store) {
		quantity   = new HashMap<>();
		price      = new HashMap<>();
		fluct      = 0;
		this.store = store;
	}

	public void setSystem(SystemData system) {
		this.system = system;
	}

	public int getFluct() {
		return fluct;
	}

	public void setFluct(int fluct) {
		this.fluct = fluct;
	}

	public abstract void generate();

	public int getPrice(TradeGood good) {
		Integer p = price.get(good);
		return p == null ? 0 : p;
	}

	public int getQuantity(TradeGood good) {
		Integer q = quantity.get(good);
		return q == null ? 0 : q;
	}

	public void setQuantity(TradeGood good, int newQuantity) {
		if (good != null) {
			quantity.put(good, newQuantity);
		}
	}

	public JSONObject toJson() throws JSONException {
		JSONArray goods = new JSONArray();
		for (TradeGood good : store.goods()) {
			goods.put(new JSONObject()
				.put("id", good.getId())
				.put("quantity", getQuantity(good))
				.put("traded", good.isTraded()));
		}
		return new JSONObject()
			.put("fluct", fluct)
			.put("goods", goods);
	}

	public void fromJson(JSONObject market) throws JSONException {
		fluct = market.getInt("fluct");
		JSONArray goods = market.getJSONArray("goods");
		for (int i = 0; i < goods.length(); i++) {
			JSONObject g = goods.getJSONObject(i);
			TradeGood good = TradeGoodStore.get().getGoodById(g.getInt("id"));
			setQuantity(good, g.getInt("quantity"));
			if (g.getBoolean("traded")) {
				good.traded();
			}
		}
	}
}
