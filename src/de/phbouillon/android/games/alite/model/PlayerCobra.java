package de.phbouillon.android.games.alite.model;

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
import java.util.Iterator;
import java.util.List;

import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.trading.TradeGood;

public class PlayerCobra {
	public static final int   DIR_FRONT = 0;
	public static final int   DIR_RIGHT = 1;
	public static final int   DIR_REAR  = 2;
	public static final int   DIR_LEFT  = 3;

	public static final int   MAXIMUM_FUEL          = 70;
	public static final int   MAXIMUM_MISSILES      = 4;
	public static final int   DEFAULT_MISSILES      = 3;
	public static final int   SPEED_UP_FACTOR       = 10;       // speed-up factor to use when torus can't be engaged
	public static final float MAX_SPEED             = 367.4f;   // m/s -- about 1320 km/h; just faster than sonic speed on Earth
	public static final float TORUS_SPEED           = 33400.0f; // torus drive speed
	public static final float TORUS_TEST_SPEED      = 10000.0f; // value to use to test if torus is engaged
	public static final int   MAX_SHIELD            = 24;
	public static final float MAX_FUEL              = 70;
	public static final float MAX_CABIN_TEMPERATURE = 30;
	public static final float MAX_LASER_TEMPERATURE = 48;
	public static final float MAX_ALTITUDE          = 30;
	public static final int   MAX_ENERGY_BANK       = 24;
	public static final int   MAX_ENERGY            = 96;

	private int fuel = MAXIMUM_FUEL;
	private int missiles = DEFAULT_MISSILES;
	private final Equipment[] lasers = new Equipment[] {EquipmentStore.get().getEquipmentById(EquipmentStore.PULSE_LASER), null, null, null};
	private final List<Equipment> equipmentInstalled = new ArrayList<>();
	private Weight maxCargoHold = Weight.tonnes(20);
	private final List<InventoryItem> inventory = new ArrayList<>();
	private int retroRocketsUseCount = 0;

	private int frontShield = MAX_SHIELD;
	private int rearShield = MAX_SHIELD;
	private int[] energyBank = new int[] {MAX_ENERGY_BANK, MAX_ENERGY_BANK, MAX_ENERGY_BANK, MAX_ENERGY_BANK};
	private int laserTemperature;
	private int cabinTemperature;
	private float altitude = MAX_ALTITUDE;
	private float pitch;
	private float roll;
	private boolean missileLocked = false;
	private boolean missileTargeting = false;

	public void setLaser(int where, Laser laser) {
		lasers[where] = laser;
	}

	public Laser getLaser(int where) {
		return (Laser) lasers[where];
	}

	public void addTradeGood(TradeGood good, Weight weight, long price) {
		getItem(good).add(weight, price);
	}

	private InventoryItem getItem(TradeGood good) {
		InventoryItem item = getInventoryItemByGood(good);
		if (item != null) {
			return item;
		}
		item = new InventoryItem(good);
		inventory.add(item);
		return item;
	}

	public void addUnpunishedTradeGood(TradeGood good, Weight weight) {
		getItem(good).addUnpunished(weight);
	}

	public void subUnpunishedTradeGood(TradeGood good, Weight weight) {
		getItem(good).subUnpunished(weight);
	}

	public void setTradeGood(TradeGood good, Weight weight, long price) {
		getItem(good).set(weight, price);
	}

	public void setUnpunishedTradeGood(TradeGood good, Weight unpunished) {
		getItem(good).resetUnpunished();
		getItem(good).addUnpunished(unpunished);
	}

	public void removeTradeGood(TradeGood good) {
		InventoryItem item = getInventoryItemByGood(good);
		if (item != null) {
			inventory.remove(item);
		}
	}

	public void addEquipment(Equipment equip) {
		if (equip != null && equipmentInstalled.indexOf(equip) < 0) {
			equipmentInstalled.add(equip);
			if (equip == EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY)) {
				maxCargoHold = Weight.tonnes(35);
			}
		}
	}

	public void removeEquipment(Equipment equip) {
		if (equipmentInstalled.remove(equip)) {
			if (equip == EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY)) {
				maxCargoHold = Weight.tonnes(20);
			}
		}
	}

	public void clearEquipment() {
		equipmentInstalled.clear();
		maxCargoHold = Weight.tonnes(20);
		lasers[0] = EquipmentStore.get().getEquipmentById(EquipmentStore.PULSE_LASER);
		for (int i = 1; i < 4; i++) {
			lasers[i] = null;
		}
	}

	public boolean isEquipmentInstalled(Equipment equip) {
		return equipmentInstalled.contains(equip);
	}

	public List<Equipment> getInstalledEquipment() {
		return equipmentInstalled;
	}

	public List<Equipment> getInstalledLosableEquipment() {
		List<Equipment> losableEquipments = new ArrayList<>();
		for (Equipment e : equipmentInstalled) {
			if (e.canBeLost()) {
				losableEquipments.add(e);
			}
		}
		return losableEquipments;
	}

	public Weight getFreeCargo() {
		Weight freeCargo = maxCargoHold;
		for (InventoryItem i: inventory) {
			freeCargo = freeCargo.sub(i.getWeight());
		}
		return freeCargo;
	}

	public List<InventoryItem> getInventory() {
		return inventory;
	}

	public boolean hasCargo() {
		return !inventory.isEmpty();
	}

	public InventoryItem getInventoryItemByGood(TradeGood good) {
		for (InventoryItem item : inventory) {
			if (item.getGood() == good) {
				return item;
			}
		}
		return null;
	}

	public void clearInventory() {
		inventory.clear();
	}

	public int getMissiles() {
		return missiles;
	}

	public void setMissiles(int newMissileCount) {
		missiles = Math.min(MAXIMUM_MISSILES, newMissileCount);
	}

	public int getFuel() {
		return fuel;
	}

	public void setFuel(int newFuel) {
		if (newFuel < 0) {
			newFuel = 0;
		}
		fuel = newFuel;
	}

	public void resetEnergy() {
		frontShield   = MAX_SHIELD;
		rearShield    = MAX_SHIELD;
		energyBank[0] = MAX_ENERGY_BANK;
		energyBank[1] = MAX_ENERGY_BANK;
		energyBank[2] = MAX_ENERGY_BANK;
		energyBank[3] = MAX_ENERGY_BANK;
	}

	public int getFrontShield() {
		return frontShield;
	}

	public int getRearShield() {
		return rearShield;
	}

	public int getEnergy() {
		return energyBank[0] + energyBank[1] + energyBank[2] + energyBank[3];
	}

	public int getEnergy(int idx) {
		return energyBank[idx];
	}

	public void setFrontShield(int newVal) {
		if (newVal > MAX_SHIELD + Settings.shieldPowerOverride) {
			newVal = MAX_SHIELD + Settings.shieldPowerOverride;
		}
		if (newVal < 0) {
			newVal = 0;
		}
		frontShield = newVal;
	}

	public void setRearShield(int newVal) {
		if (newVal > MAX_SHIELD + Settings.shieldPowerOverride) {
			newVal = MAX_SHIELD + Settings.shieldPowerOverride;
		}
		if (newVal < 0) {
			newVal = 0;
		}
		rearShield = newVal;
	}

	public void setEnergy(int newVal) {
		if (newVal > MAX_ENERGY) {
			newVal = MAX_ENERGY;
		}
		if (newVal < 0) {
			newVal = 0;
		}
		energyBank[0] = Math.max(newVal - 3 * MAX_ENERGY_BANK, 0);
		energyBank[1] = Math.max(newVal - energyBank[0] - 2 * MAX_ENERGY_BANK, 0);
		energyBank[2] = Math.max(newVal - energyBank[0] - energyBank[1] - MAX_ENERGY_BANK, 0);
		energyBank[3] = Math.max(newVal - energyBank[0] - energyBank[1] - energyBank[2], 0);
	}

	public void setLaserTemperature(int temp) {
		laserTemperature = temp < 0 ? 0 : Math.min(temp, 48);
	}

	public int getLaserTemperature() {
		return laserTemperature;
	}

	public int getCabinTemperature() {
		return cabinTemperature;
	}

	public void setCabinTemperature(int temp) {
		cabinTemperature = temp;
	}

	public float getAltitude() {
		return altitude;
	}

	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}

	public float getPitch() {
		return pitch;
	}

	public float getRoll() {
		return roll;
	}

	public void setRotation(float pitch, float roll) {
		this.pitch = pitch;
		this.roll = roll;
	}

	public boolean isMissileLocked() {
		return missileLocked;
	}

	public void setMissileLocked(boolean b) {
		if (b) {
			missileTargeting = false;
		}
		missileLocked = b;
	}

	public boolean isMissileTargetting() {
		return missileTargeting;
	}

	public void setMissileTargetting(boolean b) {
		if (b) {
			missileLocked = false;
		}
		missileTargeting = b;
	}

	public int getRetroRocketsUseCount() {
		return retroRocketsUseCount;
	}

	public void setRetroRocketsUseCount(int newCount) {
		retroRocketsUseCount = newCount;
		if (newCount == 0) {
			removeEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS));
		}
	}

	public void clearSpecialCargo() {
		final Iterator<InventoryItem> i = inventory.iterator();
		while (i.hasNext()) {
			if (i.next().getGood().isSpecialGood()) {
				i.remove();
			}
		}
	}
}
