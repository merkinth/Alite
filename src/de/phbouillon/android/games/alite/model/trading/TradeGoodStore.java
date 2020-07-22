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

import java.util.ArrayList;
import java.util.List;

public abstract class TradeGoodStore {
	public static final int FOOD             =  0;
	public static final int TEXTILES         =  1;
	public static final int RADIOACTIVES     =  2;
	public static final int SLAVES           =  3;
	public static final int LIQUOR_WINES     =  4;
	public static final int LUXURIES         =  5;
	public static final int NARCOTICS        =  6;
	public static final int COMPUTERS        =  7;
	public static final int MACHINERY        =  8;
	public static final int ALLOYS           =  9;
	public static final int FIREARMS         = 10;
	public static final int FURS             = 11;
	public static final int MINERALS         = 12;
	public static final int GOLD             = 13;
	public static final int PLATINUM         = 14;
	public static final int GEM_STONES       = 15;
	public static final int ALIEN_ITEMS      = 16;
	public static final int MEDICAL_SUPPLIES = 17;

	public static final int THARGOID_DOCUMENTS = 100;
	public static final int UNHAPPY_REFUGEES = 101;

	private static TradeGoodStore instance = null;
	private final List<TradeGood> goods = new ArrayList<>();

	protected TradeGoodStore() {
		initialize();
	}

	public abstract void initialize();

	public static TradeGoodStore get() {
		if (instance == null) {
			instance = new AliteTradeGoodStore();
		}
		return instance;
	}

	public List<TradeGood> goods() {
		return goods;
	}

	public void addTradeGood(TradeGood tradeGood) {
		if (!goods.contains(tradeGood)) {
			goods.add(tradeGood);
		}
	}

	public TradeGood getGoodById(int id) {
		for(TradeGood good : goods) {
			if (good.getId() == id) {
				return good;
			}
		}
		return null;
	}

	public TradeGood getRandomTradeGoodForContainer() {
		TradeGood good;
		do {
			good = goods.get((int) (Math.random() * goods.size()));
		} while (good.getId() == ALIEN_ITEMS || good.isSpecialGood());
		return good;
	}

	public int getGoodsCount() {
		int count = 0;
		for(TradeGood good : goods) {
			if (!good.isSpecialGood()) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) goods.stream().filter(p -> !p.isSpecialGood()).count();
	}

	public int getTradedGoodsCount() {
		int count = 0;
		for(TradeGood good : goods) {
			if (!good.isSpecialGood() && good.isTraded()) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) goods.stream().filter(p -> !p.isSpecialGood() && p.isTraded()).count();
	}

	public void clearTraded() {
		for(TradeGood good : goods) {
			good.traded = false;
		}
	}

	public int hasTradedWithAllIllegalGoods() {
		for(TradeGood good : goods) {
			if (good.getLegalityType() > 0 && !good.isTraded()) {
				return 0;
			}
		}
		return 1;
	}
}
