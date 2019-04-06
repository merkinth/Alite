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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Color;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Button;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.trading.Market;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.model.trading.Unit;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class BuyScreen extends TradeScreen {
	private static final String MARKET_HINT = "(Tap again to buy)";
	private static final int[] availColors = new int[] {Color.RED, 0xFFFF3E00, 0xFFFF7D00,
		0xFFFFBD00, Color.YELLOW, 0xFFFFFF3B, 0xFFFFFF7D, 0xFFFFFFBF, Color.WHITE };
	private int currentAvailabiltyColor;
	private String boughtAmount = null;
	private float elapsedTime;
	private String pendingSelection = null;
	private TradeGood goodToBuy;

	public BuyScreen(Alite game) {
		super(game, false);
		X_OFFSET = 50;
		GAP_X = 270;
		GAP_Y = 290;
		COLUMNS = 6;
	}

	@Override
	public void activate() {
		if (tradeButton == null) {
			createButtons();
		}
		if (pendingSelection != null) {
			for (Button[] bs: tradeButton) {
				for (Button b: bs) {
					if (pendingSelection.equals(b.getName())) {
						selection = b;
						b.setSelected(true);
					}
				}
			}
			pendingSelection = null;
		}
	}

	static BuyScreen readScreen(Alite alite, final DataInputStream dis) {
		BuyScreen bs = new BuyScreen(alite);
		try {
			byte selectionLength = dis.readByte();
			if (selectionLength > 0) {
				bs.pendingSelection = "";
				while (selectionLength > 0) {
					bs.pendingSelection += dis.readChar();
					selectionLength--;
				}
			}
		} catch (IOException e) {
			AliteLog.e("Buy Screen Initialize", "Error in initializer.", e);
			return null;
		}
		return bs;
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		BuyScreen bs = readScreen(alite, dis);
		if (bs == null) {
			return false;
		}
		alite.setScreen(bs);
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeByte(selection == null ? 0 : selection.getName().length());
		if (selection != null) {
			dos.writeChars(selection.getName());
		}
	}

	@Override
	protected void createButtons() {
		tradeButton = new Button[COLUMNS][ROWS];

		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				tradeButton[x][y] = Button.createOverlayButton(x * GAP_X + X_OFFSET,
						y * GAP_Y + Y_OFFSET, SIZE, SIZE, tradeGoods[y * COLUMNS + x], beam)
					.setName(TradeGoodStore.get().goods()[y * COLUMNS + x].getName());
			}
		}
	}

	@Override
	protected String getCost(int row, int column) {
		return getOneDecimalFormatString("%d.%d Cr",
			game.getPlayer().getMarket().getPrice(TradeGoodStore.get().goods()[row * COLUMNS + column]));
	}

	public void resetSelection() {
		selection = null;
	}

	public TradeGood getSelectedGood() {
		int index = getSelectionIndex();
		if (index < 0) {
			return null;
		}
		return TradeGoodStore.get().goods()[index];
	}

	public TradeGood getGoodToBuy() {
		return goodToBuy;
	}

	public String getBoughtAmount() {
		return boughtAmount;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle("Buy Cargo");

		elapsedTime+= deltaTime;
		currentAvailabiltyColor = (int) (elapsedTime % availColors.length);
		presentTradeGoods(deltaTime);
		presentTradeStatus();
	}

	@Override
	protected void drawAdditionalTradeGoodInformation(int row, int column, float deltaTime) {
		TradeGood tradeGood = TradeGoodStore.get().goods()[row * COLUMNS + column];
		int avail = game.getPlayer().getMarket().getQuantity(tradeGood);
		if (avail > 0) {
			int height = SIZE * avail / 63 + 1;
			game.getGraphics().fillRect(column * GAP_X + X_OFFSET + SIZE + 1,
				row * GAP_Y + Y_OFFSET + SIZE - 1 - height, 20, height, availColors[currentAvailabiltyColor]);
		}
	}

	@Override
	protected void presentSelection(int row, int column) {
		TradeGood tradeGood = TradeGoodStore.get().goods()[row * COLUMNS + column];
		int avail = game.getPlayer().getMarket().getQuantity(tradeGood);
		String average = getOneDecimalFormatString("%d.%d Cr ", game.getGenerator().getAveragePrice(tradeGood));
		game.getGraphics().drawText(tradeGood.getName() + " - " + avail + tradeGood.getUnit().toUnitString() +
			" available. Average Price: " + average + MARKET_HINT, X_OFFSET, 1050,
			ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	void setBoughtAmountString(String s) {
		boughtAmount = s;
	}

    @Override
	public void performTrade(int row, int column) {
		TradeGood tradeGood = TradeGoodStore.get().goods()[row * COLUMNS + column];
		Player player = game.getPlayer();
		Market market = player.getMarket();
		for (Mission mission: player.getActiveMissions()) {
			if (mission.performTrade(this, tradeGood)) {
				return;
			}
		}
		int avail = market.getQuantity(tradeGood);
		if (avail == 0) {
			showMessageDialog("Sorry - that item is out of stock.");
			SoundManager.play(Assets.error);
			return;
		}
		String maxAmountString = avail + tradeGood.getUnit().toUnitString();
		if (boughtAmount == null) {
			goodToBuy = TradeGoodStore.get().goods()[row * COLUMNS + column];
			newScreen = new QuantityPadScreen(this, game,
				maxAmountString, column < 3 ? 1075 : 215, 200, row, column);
			return;
		}

		goodToBuy = null;
		long buyAmount;
		try {
			buyAmount = Long.parseLong(boughtAmount);
		} catch (NumberFormatException ignored) {
			boughtAmount = null;
			return;
		}
		boughtAmount = null;
		if (buyAmount > avail) {
			SoundManager.play(Assets.error);
			showMessageDialog("Sorry - we don't have that much in stock.");
			return;
		}
		long totalPrice = buyAmount * market.getPrice(tradeGood);
		if (totalPrice > player.getCash()) {
			SoundManager.play(Assets.error);
			showMessageDialog("Sorry - you don't have enough credits.");
			return;
		}
		Weight buyWeight = tradeGood.getUnit() == Unit.GRAM ? Weight.grams(buyAmount) :
						   tradeGood.getUnit() == Unit.KILOGRAM ? Weight.kilograms(buyAmount) :
						   Weight.tonnes(buyAmount);
		if (buyWeight.compareTo(player.getCobra().getFreeCargo()) > 0) {
			SoundManager.play(Assets.error);
			showMessageDialog("Sorry - you don't have enough room in your hold.");
			return;
		}
		market.setQuantity(tradeGood, (int) (market.getQuantity(tradeGood) - buyAmount));
		player.getCobra().addTradeGood(tradeGood, buyWeight, totalPrice);
		player.setCash(player.getCash() - totalPrice);
		SoundManager.play(Assets.kaChing);
		cashLeft = getCashLeftString();
		selection = null;
		int chanceInPercent = game.getPlayer().getLegalProblemLikelihoodInPercent();
		if (Math.random() * 100 < chanceInPercent) {
			game.getPlayer().setLegalValue(
				game.getPlayer().getLegalValue() + (int) (tradeGood.getLegalityType() * buyAmount));
		}
		try {
			game.autoSave();
		} catch (IOException e) {
			AliteLog.e("Auto saving failed", e.getMessage(), e);
		}
	}

	@Override
	public void loadAssets() {
		loadTradeGoodAssets();
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
