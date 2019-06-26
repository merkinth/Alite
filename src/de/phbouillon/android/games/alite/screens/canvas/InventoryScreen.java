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
import java.util.ArrayList;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.InventoryItem;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.model.Unit;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CargoCanister;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class InventoryScreen extends TradeScreen {
	static class InventoryPair {
		TradeGood good;
		InventoryItem item;

		InventoryPair(TradeGood good, InventoryItem item) {
			this.good = good;
			this.item = item;
		}
	}

	private static final float CARGO_CANISTER_EJECTION_DISTANCE = 800.0f;
	private final ArrayList<InventoryPair> inventoryList = new ArrayList<>();
	private Pixmap thargoidDocuments;
	private Pixmap unhappyRefugees;
	private String pendingSelection = null;
	private final Vector3f cargoVector = new Vector3f(0, 0, 0);

	public InventoryScreen(Alite game) {
		super(game, false);
		X_OFFSET = 50;
		GAP_X = 270;
		GAP_Y = 290;
		COLUMNS = 6;
	}

	@Override
	public void activate() {
		createButtons();
		if (pendingSelection != null) {
			for (Button[] bs: tradeButton) {
				if (bs == null) {
					continue;
				}
				for (Button b: bs) {
					if (b == null || b.getName() == null) {
						continue;
					}
					if (pendingSelection.equals(b.getName())) {
						selection = b;
						b.setSelected(true);
					}
				}
			}
			pendingSelection = null;
		}
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		InventoryScreen is = new InventoryScreen(alite);
		try {
			byte selectionLength = dis.readByte();
			if (selectionLength > 0) {
				is.pendingSelection = "";
				while (selectionLength > 0) {
					is.pendingSelection += dis.readChar();
					selectionLength--;
				}
			}
		} catch (IOException e) {
			AliteLog.e("Inventory Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(is);
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
		PlayerCobra cobra = game.getPlayer().getCobra();
		InventoryItem[] inventory = cobra.getInventory();
		inventoryList.clear();

		int counter = 0;
		for (InventoryItem item: inventory) {
			if (item.getWeight().getWeightInGrams() != 0) {
				inventoryList.add(new InventoryPair(TradeGoodStore.get().goods()[counter], item));
			}
			counter++;
		}
		tradeButton = new Button[COLUMNS][ROWS];
		for (int x = 0; x < COLUMNS; x++) {
			for (int y = 0; y < ROWS; y++) {
				tradeButton[x][y] = null;
			}
		}
		int y = 0;
		int x = 0;
		for (InventoryPair pair: inventoryList) {
			tradeButton[x][y] = Button.createOverlayButton(x * GAP_X + X_OFFSET, y * GAP_Y + Y_OFFSET, SIZE, SIZE,
					tradeGoods[pair.good.getId()], beam)
				.setName(pair.good.getName());
			x++;
			if (x == COLUMNS) {
				x = 0;
				y++;
			}
		}
		renderSpecialCargo(x, y);
	}

	private void renderSpecialCargo(int x, int y) {
		PlayerCobra cobra = game.getPlayer().getCobra();
		if (y >= ROWS) {
			return;
		}
		if (cobra.getSpecialCargo(TradeGoodStore.GOOD_THARGOID_DOCUMENTS) != null) {
			tradeButton[x][y] = Button.createOverlayButton(x * GAP_X + X_OFFSET, y * GAP_Y + Y_OFFSET, SIZE, SIZE, thargoidDocuments, beam);
			x++;
			if (x == COLUMNS) {
				x = 0;
				y++;
				if (y >= ROWS) {
					return;
				}
			}
		}
		if (cobra.getSpecialCargo(TradeGoodStore.GOOD_UNHAPPY_REFUGEES) != null) {
			tradeButton[x][y] = Button.createOverlayButton(x * GAP_X + X_OFFSET, y * GAP_Y + Y_OFFSET, SIZE, SIZE, unhappyRefugees, beam);
		}
	}

	private long cap(long value) {
        String binary = Long.toBinaryString(value);
        if (binary.length() > 32) {
            binary = binary.substring(binary.length() - 32);
            return Long.parseLong(binary, 2);
        }
        return value;
    }

    private long computePrice(InventoryPair pair) {
		long weightInGrams = pair.item.getWeight().getWeightInGrams();
		int factor = pair.good.getUnit().getValue();
        long tradeGoodPrice = game.getPlayer().getMarket().getPrice(pair.good) * 19 * 4 / 20;
        long price = (weightInGrams / factor + (weightInGrams % factor != 0 ? 1 : 0)) * tradeGoodPrice;
        price = cap(price) / 4;
        return price;
    }

	private String computeGainLossString(long gain) {
		return L.string(gain < 0 ? R.string.inventory_loss : R.string.inventory_gain,
			L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, Math.abs(gain))) + ". ";
	}

	@Override
	protected String getCost(int row, int column) {
		int index = row * COLUMNS + column;
		if (index >= inventoryList.size()) {
			return "";
		}
		InventoryPair pair = inventoryList.get(index);
		return pair.item.getWeight().getFormattedString();
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_inventory));

		if (inventoryList.isEmpty()) {
			if (!game.getCobra().containsSpecialCargo()) {
				g.drawText(L.string(R.string.inventory_empty_cargo), 180, 200, ColorScheme.get(ColorScheme.COLOR_WARNING_MESSAGE), Assets.regularFont);
			}
		}
		presentTradeGoods(deltaTime);
		presentTradeStatus();
	}

	@Override
	protected void presentSelection(int row, int column) {
		int index = row * COLUMNS + column;
		if (index >= inventoryList.size()) {
			if (index >= ROWS * COLUMNS) {
				return;
			}
			if (tradeButton[column][row].getPixmap() == thargoidDocuments) {
				game.getGraphics().drawText(L.string(R.string.goods_thargoid_documents), X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
				return;
			}
			if (tradeButton[column][row].getPixmap() == unhappyRefugees) {
				game.getGraphics().drawText(L.string(R.string.goods_unhappy_refugees), X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
			}
			return;
		}
		InventoryPair pair = inventoryList.get(index);
		long price = computePrice(pair);
		String worthText = L.string(R.string.inventory_worth, pair.good.getName(), L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, price));
		int width = game.getGraphics().getTextWidth(worthText, Assets.regularFont);
		game.getGraphics().drawText(worthText, X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		long gain = price - pair.item.getPrice();
		String gainText = computeGainLossString(gain);
		int widthComplete = width + game.getGraphics().getTextWidth(gainText, Assets.regularFont);
		game.getGraphics().drawText(gainText, X_OFFSET + width, 1050, ColorScheme.get(gain < 0 ?
			ColorScheme.COLOR_CONDITION_RED : ColorScheme.COLOR_CONDITION_GREEN), Assets.regularFont);
		game.getGraphics().drawText(L.string(game.getCurrentScreen() instanceof FlightScreen &&
			game.getCobra().isEquipmentInstalled(EquipmentStore.fuelScoop) ? R.string.inventory_eject : R.string.inventory_sell),
			X_OFFSET + widthComplete, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	protected void performTradeWhileInFlight(int row, int column) {
		if (!game.getCobra().isEquipmentInstalled(EquipmentStore.fuelScoop)) {
			super.performTradeWhileInFlight(row, column);
			return;
		}
		SoundManager.play(Assets.retroRocketsOrEscapeCapsuleFired);
		int index = row * COLUMNS + column;
		if (index >= inventoryList.size()) {
			return;
		}
		InventoryPair pair = inventoryList.get(index);
		TradeGood tradeGood = pair.good;
		Player player = game.getPlayer();
		Weight ejectedWeight;
		long ejectedPrice = pair.item.getPrice();
		if (pair.item.getWeight().compareTo(Weight.tonnes(4)) < 0) {
			ejectedWeight = pair.item.getWeight();
			player.getCobra().removeTradeGood(tradeGood);
		} else {
			ejectedWeight = Weight.tonnes(4);
			player.getCobra().setTradeGood(tradeGood, pair.item.getWeight().sub(ejectedWeight), pair.item.getPrice());
			player.getCobra().subUnpunishedTradeGood(tradeGood, ejectedWeight);
		}
		ejectPlayerCargoCanister(tradeGood, ejectedWeight, ejectedPrice);

		createButtons();
	}

	private void ejectPlayerCargoCanister(TradeGood tradeGood, Weight weight, long price) {
		final CargoCanister cargo = new CargoCanister(game);
		cargo.setContent(tradeGood, weight);
		cargo.setPrice(price);
		InGameManager inGame = ((FlightScreen) game.getCurrentScreen()).getInGameManager();
		final SpaceObject ship = inGame.getShip();
		ship.getForwardVector().copy(cargoVector);
		cargoVector.scale(CARGO_CANISTER_EJECTION_DISTANCE);
		cargoVector.add(ship.getPosition());
		inGame.getSpawnManager().spawnTumbleObject(cargo, cargoVector);
	}

	@Override
	public void performTrade(int row, int column) {
		int index = row * COLUMNS + column;
		if (index >= inventoryList.size()) {
			return;
		}
		InventoryPair pair = inventoryList.get(index);
		TradeGood tradeGood = pair.good;
		Player player = game.getPlayer();
		for (Mission m: player.getActiveMissions()) {
			if (m.performTrade(this, tradeGood)) {
				return;
			}
		}
		long price = computePrice(pair);
		int chanceInPercent = game.getPlayer().getLegalProblemLikelihoodInPercent();
		if (Math.random() * 100 < chanceInPercent) {
			game.getPlayer().setLegalValue(game.getPlayer().getLegalValue() +
				(int) (tradeGood.getLegalityType() * pair.item.getUnpunished().getQuantityInAppropriateUnit()));
		}
		player.getCobra().removeTradeGood(tradeGood);
		player.setCash(player.getCash() + price);
		cashLeft = L.getOneDecimalFormatString(R.string.cash_amount, player.getCash());
		SoundManager.play(Assets.kaChing);
		createButtons();
		try {
			game.autoSave();
		} catch (IOException e) {
			AliteLog.e("Auto saving failed", e.getMessage(), e);
		}
	}

	public String getCashLeft() {
		return cashLeft;
	}

	@Override
	public void loadAssets() {
		loadTradeGoodAssets();
		Graphics g = game.getGraphics();
		if (thargoidDocuments == null) {
			thargoidDocuments = g.newPixmap("trade_icons/thargoid_documents.png");
		}
		if (unhappyRefugees == null) {
			unhappyRefugees = g.newPixmap("trade_icons/unhappy_refugees.png");
		}
		super.loadAssets();
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeTradeGoodAssets();
		if (thargoidDocuments != null) {
			thargoidDocuments.dispose();
			thargoidDocuments = null;
		}
		if (unhappyRefugees != null) {
			unhappyRefugees.dispose();
			unhappyRefugees = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.INVENTORY_SCREEN;
	}
}
