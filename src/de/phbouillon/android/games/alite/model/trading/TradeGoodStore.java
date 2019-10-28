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

public abstract class TradeGoodStore {
	public static final String GOOD_THARGOID_DOCUMENTS = "Thargoid Documents";
	public static final String GOOD_UNHAPPY_REFUGEES = "Unhappy Refugees";

	public static final int NUMBER_OF_GOODS = 18;

	static final int FOOD             =  0;
	static final int TEXTILES         =  1;
	static final int RADIOACTIVES     =  2;
	static final int SLAVES           =  3;
	static final int LIQUOR_WINES     =  4;
	static final int LUXURIES         =  5;
	static final int NARCOTICS        =  6;
	static final int COMPUTERS        =  7;
	static final int MACHINERY        =  8;
	static final int ALLOYS           =  9;
	static final int FIREARMS         = 10;
	static final int FURS             = 11;
	static final int MINERALS         = 12;
	static final int GOLD             = 13;
	static final int PLATINUM         = 14;
	static final int GEM_STONES       = 15;
	static final int ALIEN_ITEMS      = 16;
	static final int MEDICAL_SUPPLIES = 17;

	private static TradeGoodStore instance = null;
	protected final TradeGood [] goods = new TradeGood[NUMBER_OF_GOODS];

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

	public TradeGood[] goods() {
		return goods;
	}

	public TradeGood fromNumber(int number) {
		return goods[number];
	}

	public TradeGood getRandomTradeGoodForContainer() {
		int num;

		do {
			num = (int) (Math.random() * NUMBER_OF_GOODS);
		} while (num == ALIEN_ITEMS);

		return goods[num];
	}

	public TradeGood food()              { return goods[FOOD];             }
	public TradeGood textiles()          { return goods[TEXTILES];         }
	public TradeGood radioactives()      { return goods[RADIOACTIVES];     }
	public TradeGood slaves()            { return goods[SLAVES];           }
	public TradeGood liquorWines()       { return goods[LIQUOR_WINES];     }
	public TradeGood luxuries()          { return goods[LUXURIES];         }
	public TradeGood narcotics()         { return goods[NARCOTICS];        }
	public TradeGood computers()         { return goods[COMPUTERS];        }
	public TradeGood machinery()         { return goods[MACHINERY];        }
	public TradeGood alloys()            { return goods[ALLOYS];           }
	public TradeGood firearms()          { return goods[FIREARMS];         }
	public TradeGood furs()              { return goods[FURS];             }
	public TradeGood minerals()          { return goods[MINERALS];         }
	public TradeGood gold()              { return goods[GOLD];             }
	public TradeGood platinum()          { return goods[PLATINUM];         }
	public TradeGood gemStones()         { return goods[GEM_STONES];       }
	public TradeGood alienItems()        { return goods[ALIEN_ITEMS];      }
	public TradeGood medicalSupplies()   { return goods[MEDICAL_SUPPLIES]; }
}
