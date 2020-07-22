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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.*;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class InventoryScreen extends TradeScreen {
	private static final float CARGO_CANISTER_EJECTION_DISTANCE = 800.0f;
	private final Vector3f cargoVector = new Vector3f(0, 0, 0);
	private SpaceObject lastEjectedCargo;

	// default public constructor is required for navigation bar
	public InventoryScreen() {
		super(false, null);
		X_OFFSET = 50;
		GAP_X = 270;
		GAP_Y = 290;
		COLUMNS = 6;
	}

	public InventoryScreen(String pendingSelection) {
		super(false, pendingSelection);
	}

	@Override
	protected void createButtons() {
		tradeButton.clear();
		int index = 0;
		for (InventoryItem item: game.getCobra().getInventory()) {
			tradeButton.add(Button.createOverlayButton(index % COLUMNS * GAP_X + X_OFFSET,
				index / COLUMNS * GAP_Y + Y_OFFSET, SIZE, SIZE, tradeGoods.get(item.getGood()), beam)
				.setName(item.getGood().getName()));
			index++;
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

	private long computePrice(InventoryItem item) {
		long weightInGrams = item.getWeight().getWeightInGrams();
		int factor = item.getGood().getUnit().getValue();
		long tradeGoodPrice = game.getPlayer().getMarket().getPrice(item.getGood()) * 19 * 4 / 20;
		long price = (weightInGrams / factor + (weightInGrams % factor != 0 ? 1 : 0)) * tradeGoodPrice;
		price = cap(price) / 4;
		return price;
	}

	private String computeGainLossString(long gain) {
		return L.string(gain < 0 ? R.string.inventory_loss : R.string.inventory_gain,
			L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, Math.abs(gain))) + ". ";
	}

	@Override
	protected String getCost(int index) {
		return game.getCobra().getInventory().get(index).getWeight().getFormattedString();
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_inventory));

		if (!game.getCobra().hasCargo()) {
			g.drawText(L.string(R.string.inventory_empty_cargo), 180, 200, ColorScheme.get(ColorScheme.COLOR_WARNING_MESSAGE), Assets.regularFont);
		}
		presentTradeGoods(deltaTime);
		presentTradeStatus();
	}

	@Override
	protected void presentSelection(int index) {
		InventoryItem item = game.getCobra().getInventory().get(index);
		if (item.getGood().isSpecialGood()) {
			game.getGraphics().drawText(item.getGood().getName(), X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
			return;
		}
		long price = computePrice(item);
		String worthText = L.string(R.string.inventory_worth, item.getGood().getName(), L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, price));
		int width = game.getGraphics().getTextWidth(worthText, Assets.regularFont);
		game.getGraphics().drawText(worthText, X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		long gain = price - item.getPrice();
		String gainText = computeGainLossString(gain);
		int widthComplete = width + game.getGraphics().getTextWidth(gainText, Assets.regularFont);
		game.getGraphics().drawText(gainText, X_OFFSET + width, 1050, ColorScheme.get(gain < 0 ?
			ColorScheme.COLOR_CONDITION_RED : ColorScheme.COLOR_CONDITION_GREEN), Assets.regularFont);
		game.getGraphics().drawText(L.string(game.getCurrentScreen() instanceof FlightScreen &&
			game.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP)) ?
				R.string.inventory_eject : R.string.inventory_sell),
			X_OFFSET + widthComplete, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	public void performTradeWhileInFlight(int index) {
		if (!game.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP))) {
			super.performTradeWhileInFlight(index);
			return;
		}
		InventoryItem item = game.getCobra().getInventory().get(index);
		if (item.getGood().isSpecialGood()) {
			return;
		}
		SoundManager.play(Assets.retroRocketsOrEscapeCapsuleFired);
		PlayerCobra cobra = game.getCobra();
		long ejectedPrice = item.getPrice();
		final Weight fourTonnes = Weight.tonnes(4);
		Weight ejectedWeight = item.getWeight().compareTo(fourTonnes) <= 0 ? item.getWeight() : fourTonnes;
		Weight unpunishedWeight = item.getUnpunished().compareTo(fourTonnes) <= 0 ? item.getUnpunished() : fourTonnes;
		int specWeight = Math.min(item.getNonCargo(), 4);

		if (ejectedWeight == fourTonnes) {
			item.set(item.getWeight().sub(ejectedWeight), ejectedPrice);
			item.subUnpunished(unpunishedWeight);
		} else {
			cobra.removeItem(item);
		}
		ejectPlayerCargoCanister(item, ejectedWeight, unpunishedWeight, specWeight, ejectedPrice);
		cobra.changeScoopEjectCount(item.getGood().getId(), ejectedWeight.getWeightInGrams(),
			-unpunishedWeight.getWeightInGrams(), -specWeight);
		createButtons();
	}

	private void ejectPlayerCargoCanister(InventoryItem item, Weight weight, Weight unpunishedWeight, int specWeight, long price) {
		lastEjectedCargo = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.CargoPod);
		lastEjectedCargo.setCargoContent(item.getGood(), weight, unpunishedWeight);
		lastEjectedCargo.setCargoPrice(price);
		lastEjectedCargo.setCargoCanisterCount(specWeight);
		InGameManager inGame = ((FlightScreen) game.getCurrentScreen()).getInGameManager();
		final SpaceObject ship = inGame.getShip();
		ship.getForwardVector().copy(cargoVector);
		cargoVector.scale(CARGO_CANISTER_EJECTION_DISTANCE);
		cargoVector.add(ship.getPosition());
		inGame.getSpawnManager().spawnTumbleObject(lastEjectedCargo, cargoVector);
	}

	public SpaceObject getLastEjectedCargo() {
		return lastEjectedCargo;
	}

	@Override
	public void performTrade(int index) {
		InventoryItem item = game.getCobra().getInventory().get(index);
		if (item.getGood().isSpecialGood()) {
			return;
		}
		Player player = game.getPlayer();
		for (Mission m: MissionManager.getInstance().getActiveMissions()) {
			if (m.performTrade(this, item.getGood())) {
				return;
			}
		}
		long price = computePrice(item);
		game.getPlayer().setLegalValueByContraband(item.getGood().getLegalityType(), item.getUnpunished().getQuantityInAppropriateUnit());
		player.trade(item, price);
		SoundManager.play(Assets.kaChing);
		cashLeft = L.getOneDecimalFormatString(R.string.cash_amount, player.getCash());
		selectionIndex = -1;
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
		loadTradeGoodAssetsByInventory(game.getCobra().getInventory());
		super.loadAssets();
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeTradeGoodAssets();
		game.updateMedals();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.INVENTORY_SCREEN;
	}
}
