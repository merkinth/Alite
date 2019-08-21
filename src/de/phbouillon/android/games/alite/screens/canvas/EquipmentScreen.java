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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
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
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class EquipmentScreen extends TradeScreen {
	private int mountLaserPosition = -1;
	private int selectionIndex = -1;
	private static Pixmap[][] equipment;
	private Equipment equippedEquipment = null;
	private String pendingSelection = null;

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

	public EquipmentScreen(Alite game) {
		super(game, true);
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
						selectionIndex = getSelectionIndex();
						loadSelectedEquipmentAnimation();
					}
				}
			}
			pendingSelection = null;
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		ScreenBuilder.writeString(dos, selection == null ? null : selection.getName());
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		EquipmentScreen es = new EquipmentScreen(alite);
		try {
			es.pendingSelection = ScreenBuilder.readString(dis);
		} catch (IOException e) {
			AliteLog.e("Equipment Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(es);
		return true;
	}

	@Override
	protected void createButtons() {
		SystemData currentSystem = game.getPlayer().getCurrentSystem();
		int techLevel = currentSystem == null ? 1 : currentSystem.getTechLevel();
		tradeButton = new Button[COLUMNS][ROWS];
		int counter = 0;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (equipment[counter].length > 1) {
					tradeButton[x][y] = Button.createAnimatedButton(x * GAP_X + X_OFFSET,
						y * GAP_Y + Y_OFFSET, SIZE, SIZE, equipment[counter]);
				} else {
					tradeButton[x][y] = Button.createPictureButton(x * GAP_X + X_OFFSET,
						y * GAP_Y + Y_OFFSET, SIZE, SIZE, equipment[counter][0]);
				}
				tradeButton[x][y].setName(paths[counter]);
				counter++;
				if (techLevel < 10 && counter - techLevel > 1) {
					// Only show equipment items that are available on worlds with the given tech level.
					return;
				}
			}
		}
	}

	@Override
	protected String getCost(int row, int column) {
		Equipment equipment = game.getCobra().getEquipment(row * COLUMNS + column);
		int price = equipment.getCost();
		if (price == -1) { // variable price for fuel
			SystemData currentSystem = game.getPlayer().getCurrentSystem();
			return L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, currentSystem == null ? 10 : currentSystem.getFuelPrice());
		}
		return L.string(R.string.cash_int_amount_value_ccy, price / 10);
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_equip_ship));

		presentTradeGoods(deltaTime);
	}

	public void clearSelection() {
		selection = null;
		equippedEquipment = null;
	}

	@Override
	protected void presentSelection(int row, int column) {
		Equipment equipment = game.getCobra().getEquipment(row * COLUMNS + column);
		game.getGraphics().drawText(L.string(R.string.equip_info, equipment.getName()),
			X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	void setLaserPosition(int laserPosition) {
		mountLaserPosition = laserPosition;
	}

	public Equipment getSelectedEquipment() {
		int index = getSelectionIndex();
		if (index < 0) {
			return null;
		}
		return game.getCobra().getEquipment(index);
	}

	private int getNewLaserLocation(Laser laser, int row, int column) {
		Player player = game.getPlayer();
		PlayerCobra cobra = player.getCobra();
		boolean front = cobra.getLaser(PlayerCobra.DIR_FRONT) != laser;
		boolean right = cobra.getLaser(PlayerCobra.DIR_RIGHT) != laser;
		boolean rear  = cobra.getLaser(PlayerCobra.DIR_REAR)  != laser;
		boolean left  = cobra.getLaser(PlayerCobra.DIR_LEFT)  != laser;
		int freeSlots = (front ? 1 : 0) + (right ? 1 : 0) + (rear ? 1 : 0) + (left ? 1 : 0);

		if (freeSlots == 0) {
			return -1;
		}

		newScreen = new LaserPositionSelectionScreen(this, game, front, right, rear, left, row, column);
		return 0;
	}

	@Override
	protected void performTrade(int row, int column) {
		Equipment equipment = game.getCobra().getEquipment(row * COLUMNS + column);
		Player player = game.getPlayer();
		for (Mission mission: player.getActiveMissions()) {
			if (mission.performTrade(this, equipment)) {
				return;
			}
		}
		PlayerCobra cobra = player.getCobra();
		int price = equipment.getCost();
		int where = -1;
		if (equipment instanceof Laser) {
			if (mountLaserPosition == -1) {
				int result = getNewLaserLocation((Laser) equipment, row, column);
				if (result == -1) {
					showMessageDialog(L.string(R.string.equip_all_lasers_mounted));
					SoundManager.play(Assets.error);
				}
				return;
			}
			if (mountLaserPosition == -2) {
				// Do nothing: User canceled.
				mountLaserPosition = -1;
				return;
			}
			where = mountLaserPosition;
			mountLaserPosition = -1;
			price = cobra.getLaser(where) == null ? equipment.getCost() : equipment.getCost() - cobra.getLaser(where).getCost();
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
		if (cobra.isEquipmentInstalled(EquipmentStore.navalEnergyUnit) && equipment.equals(EquipmentStore.extraEnergyUnit)) {
			showMessageDialog(L.string(R.string.equip_only_one_allowed, equipment.getShortName()));
			SoundManager.play(Assets.error);
			return;
		}

		if (equipment instanceof Laser) {
			player.setCash(player.getCash() - price);
			cobra.setLaser(where, (Laser) equipment);
			disposeSelectedEquipmentAnimation();
			selection = null;
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
			disposeSelectedEquipmentAnimation();
			selection = null;
			cashLeft = getCashLeftString();
			SoundManager.play(Assets.kaChing);
			equippedEquipment = EquipmentStore.fuel;
			performAutoSave();
			return;
		}

		if (equipment == EquipmentStore.missiles) {
			if (cobra.getMissiles() == PlayerCobra.MAXIMUM_MISSILES) {
				showMessageDialog(L.string(R.string.equip_max_missiles_reached, PlayerCobra.MAXIMUM_MISSILES));
				SoundManager.play(Assets.error);
				return;
			}
			player.setCash(player.getCash() - price);
			cobra.setMissiles(cobra.getMissiles() + 1);
			disposeSelectedEquipmentAnimation();
			selection = null;
			cashLeft = getCashLeftString();
			SoundManager.play(Assets.kaChing);
			equippedEquipment = EquipmentStore.missiles;
			performAutoSave();
			return;
		}

		player.setCash(player.getCash() - price);
		cobra.addEquipment(equipment);
		if (equipment == EquipmentStore.retroRockets) {
			cobra.setRetroRocketsUseCount(4 + (int) (Math.random() * 3));
		}
		SoundManager.play(Assets.kaChing);
		disposeSelectedEquipmentAnimation();
		selection = null;
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

	private void disposeSelectedEquipmentAnimation() {
		if (selectionIndex != -1) {
			disposeEquipmentAnimation(selectionIndex);
			selectionIndex = -1;
		}
	}

	public Equipment getEquippedEquipment() {
		return equippedEquipment;
	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (tradeButton[x][y] == null || !tradeButton[x][y].isTouched(touch.x, touch.y)) {
					continue;
				}
				if (selection == tradeButton[x][y]) {
					if (game.getCurrentScreen() instanceof FlightScreen) {
						SoundManager.play(Assets.error);
						errorText = L.string(R.string.state_not_docked);
					} else {
						performTrade(y, x);
						SoundManager.play(Assets.click);
					}
					continue;
				}
				equippedEquipment = null;
				errorText = null;
				disposeSelectedEquipmentAnimation();
				selectionIndex = y * COLUMNS + x;
				selection = tradeButton[x][y];
				loadSelectedEquipmentAnimation();
				selectionTimer.reset();
				currentFrame = 0;
				cashLeft = null;
				SoundManager.play(Assets.click);
			}
		}
	}

	private void loadSelectedEquipmentAnimation() {
		if (Settings.animationsEnabled) {
			loadEquipmentAnimation(game.getGraphics(), selectionIndex, paths[selectionIndex]);
			selection.setAnimation(equipment[selectionIndex]);
		}
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

	private void loadEquipmentAnimation(final Graphics g, int offset, String path) {
	  for (int i = 1; i <= 15; i++) {
		equipment[offset][i] = g.newPixmap(path + "/" + i + ".png");
	  }
	}

	private void disposeEquipmentAnimation(int offset) {
		for (int i = 1; i <= 15; i++) {
			if (equipment[offset][i] != null) {
				equipment[offset][i].dispose();
			}
			equipment[offset][i] = null;
		}
	}

	private void readEquipmentStill(final Graphics g, int offset, String path) {
		equipment[offset] = new Pixmap[16];
		equipment[offset][0] = g.newPixmap(path + ".png");
	}

	private void readEquipment(final Graphics g) {
		equipment = new Pixmap[15][1];
		for (int i = 0; i < 15; i++) {
			readEquipmentStill(g, i, paths[i]);
		}
	}

	@Override
	public void loadAssets() {
		Graphics g = game.getGraphics();

		if (equipment == null) {
			readEquipment(g);
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
