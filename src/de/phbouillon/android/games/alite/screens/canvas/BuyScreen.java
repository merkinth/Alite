package de.phbouillon.android.games.alite.screens.canvas;

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

import android.graphics.Color;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.trading.Market;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class BuyScreen extends TradeScreen {
	private static final int[] availColors = new int[] {Color.RED, 0xFFFF3E00, 0xFFFF7D00,
		0xFFFFBD00, Color.YELLOW, 0xFFFFFF3B, 0xFFFFFF7D, 0xFFFFFFBF, Color.WHITE };
	private int currentAvailabilityColor;
	private int boughtAmount;
	private float elapsedTime;
	private TradeGood goodToBuy;

	// default public constructor is required for navigation bar
	public BuyScreen() {
		this(null);
	}

	public BuyScreen(String pendingSelection) {
		super(false, pendingSelection);
		X_OFFSET = 50;
		GAP_X = 270;
		GAP_Y = 290;
		COLUMNS = 6;
	}

	@Override
	protected void createButtons() {
		tradeButton.clear();
		int index = 0;
		for (TradeGood good : TradeGoodStore.get().goods()) {
			if (!good.isSpecialGood()) {
				tradeButton.add(Button.createOverlayButton(index % COLUMNS * GAP_X + X_OFFSET,
						index / COLUMNS * GAP_Y + Y_OFFSET, SIZE, SIZE, tradeGoods.get(good), beam)
					.setName(good.getName()));
				index++;
			}
		}
	}

	@Override
	protected String getCost(int index) {
		return L.getOneDecimalFormatString(R.string.cash_amount_value_ccy,
			game.getPlayer().getMarket().getPrice(TradeGoodStore.get().goods().get(index)));
	}

	public void resetSelection() {
		selectionIndex = -1;
	}

	public TradeGood getSelectedGood() {
		if (selectionIndex < 0) {
			return null;
		}
		return TradeGoodStore.get().goods().get(selectionIndex);
	}

	public TradeGood getGoodToBuy() {
		return goodToBuy;
	}

	public int getBoughtAmount() {
		return boughtAmount;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_buy_cargo));

		elapsedTime+= deltaTime;
		currentAvailabilityColor = (int) (elapsedTime % availColors.length);
		presentTradeGoods(deltaTime);
		presentTradeStatus();
	}

	@Override
	protected void drawAdditionalTradeGoodInformation(int index, float deltaTime) {
		TradeGood tradeGood = TradeGoodStore.get().goods().get(index);
		int avail = game.getPlayer().getMarket().getQuantity(tradeGood);
		if (avail > 0) {
			int height = SIZE * avail / 63 + 1;
			game.getGraphics().fillRect(index % COLUMNS * GAP_X + X_OFFSET + SIZE + 1,
				index / COLUMNS * GAP_Y + Y_OFFSET + SIZE - 1 - height, 20, height, availColors[currentAvailabilityColor]);
		}
	}

	@Override
	protected void presentSelection(int index) {
		TradeGood tradeGood = TradeGoodStore.get().goods().get(index);
		int avail = game.getPlayer().getMarket().getQuantity(tradeGood);
		String average = L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, game.getGenerator().getAveragePrice(tradeGood));
		game.getGraphics().drawText(L.string(R.string.trade_good_info, tradeGood.getName(), avail,
			tradeGood.getUnit().toUnitString(), average), X_OFFSET, 1050,
			ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	void setBoughtAmount(int amount) {
		boughtAmount = amount;
	}

    @Override
	public void performTrade(int index) {
		TradeGood tradeGood = TradeGoodStore.get().goods().get(index);
		Player player = game.getPlayer();
		Market market = player.getMarket();
		for (Mission mission: MissionManager.getInstance().getActiveMissions()) {
			if (mission.performTrade(this, tradeGood)) {
				return;
			}
		}
		int avail = market.getQuantity(tradeGood);
		if (avail == 0) {
			showMessageDialog(L.string(R.string.trade_out_of_stock));
			SoundManager.play(Assets.error);
			return;
		}
		if (boughtAmount == 0) {
			goodToBuy = tradeGood;
			newScreen = new QuantityPadScreen(this, avail, tradeGood.getUnit().toUnitString(),
				index % COLUMNS < 3 ? 1075 : 215, 200, index);
			return;
		}

		goodToBuy = null;
		long buyAmount = boughtAmount;
		boughtAmount = 0;
		if (buyAmount > avail) {
			SoundManager.play(Assets.error);
			showMessageDialog(L.string(R.string.trade_not_enough_in_stock));
			return;
		}
		long totalPrice = buyAmount * market.getPrice(tradeGood);
		if (totalPrice > player.getCash()) {
			SoundManager.play(Assets.error);
			showMessageDialog(L.string(R.string.trade_not_enough_money));
			return;
		}
		Weight buyWeight = Weight.unit(tradeGood.getUnit(), buyAmount);
		if (buyWeight.compareTo(player.getCobra().getFreeCargo()) > 0) {
			SoundManager.play(Assets.error);
			showMessageDialog(L.string(R.string.trade_not_enough_space));
			return;
		}
		market.setQuantity(tradeGood, (int) (market.getQuantity(tradeGood) - buyAmount));
		player.getCobra().addTradeGood(tradeGood, buyWeight, totalPrice);
		player.setCash(player.getCash() - totalPrice);
		SoundManager.play(Assets.kaChing);
		cashLeft = getCashLeftString();
		selectionIndex = -1;
		game.getPlayer().setLegalValueByContraband(tradeGood.getLegalityType(), (int) buyAmount);
		try {
			game.autoSave();
		} catch (IOException e) {
			AliteLog.e("Auto saving failed", e.getMessage(), e);
		}
	}

	@Override
	public void loadAssets() {
		loadTradeGoodAssets(TradeGoodStore.get().goods());
		super.loadAssets();
	}

	protected void performScreenChange() {
		if (inFlightScreenChange()) {
			return;
		}
		Screen oldScreen = game.getCurrentScreen();
		if (!(newScreen instanceof QuantityPadScreen)) {
			oldScreen.dispose();
		}
		game.setScreen(newScreen);
		game.getNavigationBar().performScreenChange();
		postScreenChange();
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeTradeGoodAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.BUY_SCREEN;
	}
}
