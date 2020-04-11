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

import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.Unit;

public class AliteTradeGoodStore extends TradeGoodStore {
	@Override
	public void initialize() {
		addTradeGood(new TradeGood(FOOD, 0x13, -0x02, 0x06, 0x01,
			Unit.TONNE, R.string.goods_food, "trade_icons/food.png", 48, 49, 47, 47, 48, 49, 49, 49, 48));
		addTradeGood(new TradeGood(TEXTILES, 0x14, -0x01, 0x0a, 0x03,
			Unit.TONNE, R.string.goods_textiles, "trade_icons/textiles.png", 71, 71, 70, 71, 71, 72, 72, 72, 71));
		addTradeGood(new TradeGood(RADIOACTIVES, 0x41, -0x03, 0x02, 0x07,
			Unit.TONNE, R.string.goods_radioactives, "trade_icons/radioactives.png", 228, 230, 228, 228, 229, 230, 231, 231, 229));
		addTradeGood(new TradeGood(SLAVES, 0x28, -0x05, 0xe2, 0x1f,
			2.5f, Unit.TONNE, R.string.goods_slaves, "trade_icons/slaves.png", 147, 149, 144, 145, 147, 149, 149, 151, 146));
		addTradeGood(new TradeGood(LIQUOR_WINES, 0x53, -0x05, 0xfb, 0x0f,
			Unit.TONNE, R.string.goods_liquor_wines, "trade_icons/liquor_wines.png", 286, 289, 285, 285, 287, 290, 290, 291, 286));
		addTradeGood(new TradeGood(LUXURIES, 0xc4, 0x08, 0x36, 0x03,
			Unit.TONNE, R.string.goods_luxuries, "trade_icons/luxuries.png", 913, 908, 914, 913, 910, 906, 906, 904, 911));
		addTradeGood(new TradeGood(NARCOTICS, 0xeb, 0x1d, 0x08, 0x78,
			2.5f, Unit.TONNE, R.string.goods_narcotics, "trade_icons/narcotics.png", 611, 1606, 1630, 1626, 1615, 1601, 1600, 1593, 1619));
		addTradeGood(new TradeGood(COMPUTERS, 0x9a, 0x0e, 0x38, 0x03,
			Unit.TONNE, R.string.goods_computers, "trade_icons/computers.png", 831, 828, 839, 837, 832, 825, 825, 822, 834));
		addTradeGood(new TradeGood(MACHINERY, 0x75, 0x06, 0x28, 0x07,
			Unit.TONNE, R.string.goods_machinery, "trade_icons/machinery.png", 569, 570, 575, 574, 572, 569, 569, 567, 573));
		addTradeGood(new TradeGood(ALLOYS, 0x4e, 0x01, 0x11, 0x1f,
			Unit.TONNE, R.string.goods_alloys, "trade_icons/alloys.png", 387, 89, 389, 389, 389, 389, 389, 388, 389));
		addTradeGood(new TradeGood(FIREARMS, 0x9c, 0x0d, 0x1d, 0x07,
			1.3f, Unit.TONNE, R.string.goods_firearms, "trade_icons/firearms.png", 830, 829, 840, 838, 833, 827, 827, 823, 835));
		addTradeGood(new TradeGood(FURS, 0xb0, -0x09, 0xdc, 0x3f,
			Unit.TONNE, R.string.goods_furs, "trade_icons/furs.png", 694, 698, 691, 692, 695, 700, 700, 702, 694));
		addTradeGood(new TradeGood(MINERALS, 0x18, -0x01, 0x60, 0x03,
			Unit.KILOGRAM, R.string.goods_minerals, "trade_icons/minerals.png", 87, 87, 87, 87, 87, 88, 88, 88, 87));
		addTradeGood(new TradeGood(GOLD, 0x61, -0x01, 0x42, 0x07,
			Unit.KILOGRAM, R.string.goods_gold, "trade_icons/gold.png", 385, 387, 386, 387, 387, 387, 387, 388, 387));
		addTradeGood(new TradeGood(PLATINUM, 0xab, -0x02, 0x37, 0x1f,
			Unit.KILOGRAM, R.string.goods_platinum, "trade_icons/platinum.png", 714, 717, 715, 715, 716, 717, 717, 717, 716));
		addTradeGood(new TradeGood(GEM_STONES, 0x2d, -0x01, 0xfa, 0x0f,
			Unit.GRAM, R.string.goods_gem_stones, "trade_icons/gem_stones.png", 94, 195, 195, 195, 195, 196, 196, 196, 195));
		addTradeGood(new TradeGood(ALIEN_ITEMS, 0x35, 0x0f, 0xf8, 0x07,
			Unit.TONNE, R.string.goods_alien_items, "trade_icons/alien_items.png", 47, 446, 458, 457, 451, 443, 443, 440, 453));
		addTradeGood(new TradeGood(MEDICAL_SUPPLIES, 0x7b, 0x10, 0x07, 0x78,
			Unit.TONNE, R.string.goods_medical_supplies, "trade_icons/medical_supplies.png", 965, 967, 980, 978, 972, 964, 964, 960, 974));

		addTradeGood(new TradeGood(THARGOID_DOCUMENTS, Unit.GRAM, R.string.goods_thargoid_documents, "trade_icons/thargoid_documents.png"));
		addTradeGood(new TradeGood(UNHAPPY_REFUGEES, Unit.TONNE, R.string.goods_unhappy_refugees, "trade_icons/unhappy_refugees.png"));
	}
}
