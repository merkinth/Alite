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

import java.io.File;
import java.io.IOException;
import java.util.*;

import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.Mission;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class EquipmentScreen extends TradeScreen {
	private int mountLaserPosition = -1;
	private static Map<Integer,List<Pixmap>> equipment;
	private Equipment equippedEquipment = null;

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
		Iterator<Equipment> i = EquipmentStore.get().getIterator();
		int pos = 0;
		while (i.hasNext()) {
			Equipment e = i.next();
			if (techLevel <= e.getMinTechLevel()) {
				// Only show equipment items that are available on worlds with the given tech level.
				continue;
			}
			Button b = Button.createPictureButton(pos % COLUMNS * GAP_X + X_OFFSET,
				pos / COLUMNS* GAP_Y + Y_OFFSET, SIZE, SIZE, equipment.get(e.getId()).get(0))
				.setName(String.valueOf(e.getId()));
			tradeButton.add(b);
			if (equipment.get(e.getId()).size() > 1) {
				b.setAnimation(equipment.get(e.getId()).toArray(new Pixmap[0]));
			}
			pos++;
		}
	}

	@Override
	protected String getCost(int index) {
		int price = getEquipment(index).getCost();
		if (price == -1) { // variable price for fuel
			SystemData currentSystem = game.getPlayer().getCurrentSystem();
			return L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, currentSystem == null ? 10 : currentSystem.getFuelPrice());
		}
		return L.string(R.string.cash_int_amount_value_ccy, price / 10);
	}

	private Equipment getEquipment(int index) {
		return index < 0 ? null : EquipmentStore.get().getEquipmentByHash(getEquipmentId(index));
	}

	private int getEquipmentId(int index) {
		return Integer.parseInt(tradeButton.get(index).getName());
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
		Equipment equipment = getEquipment(index);
		game.getGraphics().drawText(L.string(R.string.equip_info, equipment.getName()),
			X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	void setLaserPosition(int laserPosition) {
		mountLaserPosition = laserPosition;
	}

	public Equipment getSelectedEquipment() {
		return getEquipment(selectionIndex);
	}

	private int maintainLaser(int pos, Equipment laser) {
		Equipment laserInPos = game.getPlayer().getCobra().getLaser(pos);
		return laserInPos == null ? 0 : laserInPos == laser ? -1 : 1;
	}

	@Override
	protected void performTrade(int index) {
		Equipment equipment = getEquipment(index);
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
		if (equipment.isLaser()) {
			if (mountLaserPosition == -1) {
				newScreen = new LaserPositionSelectionScreen(this,
					maintainLaser(PlayerCobra.DIR_FRONT, equipment),
					maintainLaser(PlayerCobra.DIR_RIGHT, equipment),
					maintainLaser(PlayerCobra.DIR_REAR, equipment),
					maintainLaser(PlayerCobra.DIR_LEFT, equipment), index);
				return;
			}
			if (mountLaserPosition == -2) {
				// Do nothing: User canceled.
				mountLaserPosition = -1;
				return;
			}
			where = mountLaserPosition;
			mountLaserPosition = -1;
			laserState = maintainLaser(where, equipment);
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
		if (!equipment.isLaser() && cobra.isEquipmentInstalled(equipment)) {
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

		if (equipment.isLaser()) {
			player.setCash(player.getCash() - price);
			cobra.setLaser(where, laserState < 0 ? null : equipment);
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
		Iterator<Pixmap> i = equipment.get(getEquipmentId(index)).iterator();
		if (i.hasNext()) {
			i.next(); // skip first item
			while (i.hasNext()) {
				i.next().dispose();
				i.remove();
			}
		}
	}

	public Equipment getEquippedEquipment() {
		return equippedEquipment;
	}

	@Override
	protected void loadSelectedAnimation() {
		loadEquipmentAnimation(selectionIndex);
		tradeButton.get(selectionIndex).setAnimation(equipment.get(getEquipmentId(selectionIndex)).toArray(new Pixmap[0]));
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

	private void loadEquipmentAnimation(int index) {
		int eqId = getEquipmentId(index);
		String path = EquipmentStore.get().getEquipmentByHash(eqId).getIcon();
		int i = 1;
		while(true) {
			Pixmap icon = newAssetPixmap(path + File.separator + i);
			if (icon == null) {
				break;
			}
			equipment.get(eqId).add(icon);
			i++;
		}
	}

	private Pixmap newAssetPixmap(String fileName) {
		try {
			fileName += ".png";
			return game.getGraphics().newPixmap(fileName, game.getFileIO().readAssetFile(fileName), 225, 225);
		} catch (IOException ignored) {
			return null;
		}
	}

	@Override
	public void loadAssets() {
		equipment = new HashMap<>();
		Iterator<Equipment> i = EquipmentStore.get().getIterator();
		while (i.hasNext()) {
			List<Pixmap> p = new ArrayList<>();
			Equipment e = i.next();
			if (!game.getFileIO().existsAssetFile(e.getIcon() + ".png")) {
				e.setDefaultIcon();
			}
			p.add(newAssetPixmap(e.getIcon()));
			equipment.put(e.getId(),p);
		}
		super.loadAssets();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (equipment != null) {
			for (List<Pixmap> ps: equipment.values()) {
				ps.get(0).dispose();
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.EQUIP_SCREEN;
	}
}
