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

import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Laser;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.Mission;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class EquipmentScreen extends TradeScreen {
	private int mountLaserPosition = -1;
	private static Pixmap[][] equipment;
	private Equipment equippedEquipment = null;

	private static final String[] paths = {
			"equipment_icons/fuel",
			"equipment_icons/missiles",
			"equipment_icons/large_cargo_bay",
			"equipment_icons/ecm",
			"equipment_icons/pulse_laser",
			"equipment_icons/beam_laser",
			"equipment_icons/fuel_scoop",
			"equipment_icons/escape_capsule",
			"equipment_icons/energy_bomb",
			"equipment_icons/extra_energy_unit",
			"equipment_icons/docking_computer",
			"equipment_icons/galactic_hyperdrive",
			"equipment_icons/mining_laser",
			"equipment_icons/military_laser",
			"equipment_icons/retro_rockets"};

	// default public constructor is required for navigation bar
	public EquipmentScreen() {
		super(true, null);
	}

	public EquipmentScreen(String pendingSelection) {
		super(true, pendingSelection);
	}

	@Override
	protected void createButtons() {
		tradeButton.clear();
		SystemData currentSystem = game.getPlayer().getCurrentSystem();
		int techLevel = currentSystem == null ? 1 : currentSystem.getTechLevel();
		for (int i = 0; i < equipment.length; i++) {
			Button b = Button.createPictureButton(i % COLUMNS * GAP_X + X_OFFSET,
					i / COLUMNS* GAP_Y + Y_OFFSET, SIZE, SIZE, equipment[i][0])
				.setName(paths[i]);
			tradeButton.add(b);
			if (equipment[i].length > 1) {
				b.setAnimation(equipment[i]);
			}
			if (techLevel < 10 && i - techLevel > 1) {
				// Only show equipment items that are available on worlds with the given tech level.
				return;
			}
		}
	}

	@Override
	protected String getCost(int index) {
		int price = EquipmentStore.get().getEquipment(index).getCost();
		if (price == -1) { // variable price for fuel
			SystemData currentSystem = game.getPlayer().getCurrentSystem();
			return L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, currentSystem == null ? 10 : currentSystem.getFuelPrice());
		}
		return L.string(R.string.cash_int_amount_value_ccy, price / 10);
	}

	@Override
	public void present(float deltaTime) {
		game.getGraphics().clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_equip_ship));

		presentTradeGoods(deltaTime);
	}

	public void clearSelection() {
		selectionIndex = -1;
		equippedEquipment = null;
	}

	@Override
	protected void presentSelection(int index) {
		Equipment equipment = EquipmentStore.get().getEquipment(index);
		game.getGraphics().drawText(L.string(R.string.equip_info, equipment.getName()),
			X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	void setLaserPosition(int laserPosition) {
		mountLaserPosition = laserPosition;
	}

	public Equipment getSelectedEquipment() {
		if (selectionIndex < 0) {
			return null;
		}
		return EquipmentStore.get().getEquipment(selectionIndex);
	}

	private int maintainLaser(int pos, Laser laser) {
		Laser laserInPos = game.getPlayer().getCobra().getLaser(pos);
		return laserInPos == null ? 0 : laserInPos == laser ? -1 : 1;
	}

	@Override
	protected void performTrade(int index) {
		Equipment equipment = EquipmentStore.get().getEquipment(index);
		Player player = game.getPlayer();
		for (Mission mission: player.getActiveMissions()) {
			if (mission.performTrade(this, equipment)) {
				return;
			}
		}
		PlayerCobra cobra = player.getCobra();
		int price = equipment.getCost();
		int where = -1;
		int laserState = 0;
		if (equipment instanceof Laser) {
			if (mountLaserPosition == -1) {
				newScreen = new LaserPositionSelectionScreen(this,
					maintainLaser(PlayerCobra.DIR_FRONT, (Laser) equipment),
					maintainLaser(PlayerCobra.DIR_RIGHT, (Laser) equipment),
					maintainLaser(PlayerCobra.DIR_REAR, (Laser) equipment),
					maintainLaser(PlayerCobra.DIR_LEFT, (Laser) equipment), index);
				return;
			}
			if (mountLaserPosition == -2) {
				// Do nothing: User canceled.
				mountLaserPosition = -1;
				return;
			}
			where = mountLaserPosition;
			mountLaserPosition = -1;
			laserState = maintainLaser(where, (Laser) equipment);
			if (laserState != 0) {
				price = laserState < 0 ? -equipment.getCost() : equipment.getCost() - cobra.getLaser(where).getCost();
			}
		}
		if (player.getCash() < price) {
			showMessageDialog(L.string(R.string.trade_not_enough_money));
			SoundManager.play(Assets.error);
			return;
		}
		if (equipment.getCost() == -1) {
			// Fuel
			if (cobra.getFuel() == PlayerCobra.MAXIMUM_FUEL) {
				showMessageDialog(L.getOneDecimalFormatString(R.string.equip_fuel_full, PlayerCobra.MAXIMUM_FUEL));
				SoundManager.play(Assets.error);
				return;
			}
		}
		if (!(equipment instanceof Laser) && cobra.isEquipmentInstalled(equipment)) {
			showMessageDialog(L.string(R.string.equip_only_one_allowed, equipment.getShortName()));
			SoundManager.play(Assets.error);
			return;
		}
		if (cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT)) &&
				equipment.equals(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT))) {
			showMessageDialog(L.string(R.string.equip_only_one_allowed, equipment.getShortName()));
			SoundManager.play(Assets.error);
			return;
		}

		if (equipment instanceof Laser) {
			player.setCash(player.getCash() - price);
			cobra.setLaser(where, laserState < 0 ? null : (Laser) equipment);
			disposeSelectedAnimation(selectionIndex);
			selectionIndex = -1;
			cashLeft = getCashLeftString();

			SoundManager.play(Assets.kaChing);
			performAutoSave();
			return;
		}

		if (equipment.getCost() == -1) {
			// Fuel
			price = player.getCurrentSystem() == null ? 10 : player.getCurrentSystem().getFuelPrice();
			int fuelToBuy = PlayerCobra.MAXIMUM_FUEL - cobra.getFuel();
			int priceToPay = fuelToBuy * price / 10;
			if (priceToPay > player.getCash()) {
				showMessageDialog(L.string(R.string.trade_not_enough_money));
				SoundManager.play(Assets.error);
				return;
			}
			player.setCash(player.getCash() - priceToPay);
			player.getCobra().setFuel(PlayerCobra.MAXIMUM_FUEL);
			disposeSelectedAnimation(selectionIndex);
			selectionIndex = -1;
			cashLeft = getCashLeftString();
			SoundManager.play(Assets.kaChing);
			equippedEquipment = EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL);
			performAutoSave();
			return;
		}

		if (equipment == EquipmentStore.get().getEquipmentById(EquipmentStore.MISSILES)) {
			if (cobra.getMissiles() == PlayerCobra.MAXIMUM_MISSILES) {
				showMessageDialog(L.string(R.string.equip_max_missiles_reached, PlayerCobra.MAXIMUM_MISSILES));
				SoundManager.play(Assets.error);
				return;
			}
			player.setCash(player.getCash() - price);
			cobra.setMissiles(cobra.getMissiles() + 1);
			disposeSelectedAnimation(selectionIndex);
			selectionIndex = -1;
			cashLeft = getCashLeftString();
			SoundManager.play(Assets.kaChing);
			equippedEquipment = EquipmentStore.get().getEquipmentById(EquipmentStore.MISSILES);
			performAutoSave();
			return;
		}

		player.setCash(player.getCash() - price);
		cobra.addEquipment(equipment);
		if (equipment == EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS)) {
			cobra.setRetroRocketsUseCount(4 + (int) (Math.random() * 3));
		}
		SoundManager.play(Assets.kaChing);
		disposeSelectedAnimation(selectionIndex);
		selectionIndex = -1;
		cashLeft = getCashLeftString();
		equippedEquipment = equipment;
		performAutoSave();
	}

	private void performAutoSave() {
		try {
			game.autoSave();
		} catch (IOException e) {
			AliteLog.e("Auto saving failed", e.getMessage(), e);
		}
	}

	@Override
	protected void disposeSelectedAnimation(int index) {
		equippedEquipment = null;
		for (int i = 1; i <= 15; i++) {
			if (equipment[index][i] != null) {
				equipment[index][i].dispose();
			}
			equipment[index][i] = null;
		}
	}

	public Equipment getEquippedEquipment() {
		return equippedEquipment;
	}

	@Override
	protected void loadSelectedAnimation() {
		loadEquipmentAnimation(selectionIndex, paths[selectionIndex]);
		tradeButton.get(selectionIndex).setAnimation(equipment[selectionIndex]);
	}

	protected void performScreenChange() {
		if (inFlightScreenChange()) {
			return;
		}
		Screen oldScreen = game.getCurrentScreen();
		if (!(newScreen instanceof LaserPositionSelectionScreen)) {
			oldScreen.dispose();
		}
		game.setScreen(newScreen);
		game.getNavigationBar().performScreenChange();
		postScreenChange();
	}

	private void loadEquipmentAnimation(int offset, String path) {
	  for (int i = 1; i <= 15; i++) {
		equipment[offset][i] = game.getGraphics().newPixmap(path + "/" + i + ".png");
	  }
	}

	private void readEquipmentStill(int offset, String path) {
		equipment[offset] = new Pixmap[16];
		equipment[offset][0] = game.getGraphics().newPixmap(path + ".png");
	}

	@Override
	public void loadAssets() {
		equipment = new Pixmap[15][1];
		for (int i = 0; i < 15; i++) {
			readEquipmentStill(i, paths[i]);
		}
		super.loadAssets();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (equipment != null) {
			for (Pixmap[] ps: equipment) {
				ps[0].dispose();
			}
			equipment = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.EQUIP_SCREEN;
	}
}
