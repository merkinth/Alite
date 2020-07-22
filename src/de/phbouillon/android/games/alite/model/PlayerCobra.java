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
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerCobra {
	public static final int   DIR_FRONT = 0;
	public static final int   DIR_RIGHT = 1;
	public static final int   DIR_REAR  = 2;
	public static final int   DIR_LEFT  = 3;

	public static final int   MAXIMUM_MISSILES      = 4;
	public static final int   DEFAULT_MISSILES      = 3;
	public static final int   SPEED_UP_FACTOR       = 10;       // speed-up factor to use when torus can't be engaged
	public static final float MAX_SPEED             = 367.4f;   // m/s -- about 1320 km/h; just faster than sonic speed on Earth
	public static final float TORUS_SPEED           = 33400.0f; // torus drive speed
	public static final float TORUS_TEST_SPEED      = 10000.0f; // value to use to test if torus is engaged
	public static final int   MAX_SHIELD            = 24;
	public static final int   MAX_FUEL              = 70;
	public static final float MAX_CABIN_TEMPERATURE = 30;
	public static final float MAX_LASER_TEMPERATURE = 210;
	public static final float MAX_ALTITUDE          = 30;
	public static final int   MAX_ENERGY_BANK       = 24;
	public static final int   MAX_ENERGY            = 96;

	private int fuel = MAX_FUEL;
	private int missiles = DEFAULT_MISSILES;
	private final Equipment[] lasers = new Equipment[] {EquipmentStore.get().getEquipmentById(EquipmentStore.PULSE_LASER), null, null, null};
	private final List<Equipment> equipmentInstalled = new ArrayList<>();
	private Weight maxCargoHold = Weight.tonnes(20);
	private final List<InventoryItem> inventory = new ArrayList<>();
	private int retroRocketsUseCount;
	private int retroRocketsTotalCount;

	private float frontShield = MAX_SHIELD;
	private float rearShield = MAX_SHIELD;
	private final float[] energyBank = new float[] {MAX_ENERGY_BANK, MAX_ENERGY_BANK, MAX_ENERGY_BANK, MAX_ENERGY_BANK};
	private float laserTemperature;
	private int cabinTemperature;
	private float altitude = MAX_ALTITUDE;
	private float pitch;
	private float roll;
	private boolean missileLocked = false;
	private boolean missileTargeting = false;
	private boolean laserOverheated;
	private int travelledDistance;
	private int collisionAmount;
	private int hitByMissileCount;
	private int hitBySpaceStationCount;
	private int hitByLaserAmount;
	private long scoopAmount;
	private int scoopedTonnePlatlet;
	private int scoopedTonneAlien;
	private int scoopedTonneEscapeCapsule;
	private long ejectedAmount;
	private boolean wasLowEnergy;
	private int lowEnergyCount;
	private int maxEquipments;
	private int scoopedFuel;

	public void setLaser(int where, Equipment laser) {
		lasers[where] = laser;
		setMaxEquipments();
	}

	public Equipment getLaser(int where) {
		return lasers[where];
	}

	public InventoryItem addTradeGood(TradeGood good, Weight weight, long price) {
		InventoryItem item = getItem(good);
		item.add(weight, price);
		return item;
	}

	private InventoryItem getItem(TradeGood good) {
		InventoryItem item = getInventoryItemByGood(good);
		if (item == null) {
			item = new InventoryItem(good);
			inventory.add(item);
		}
		return item;
	}

	public InventoryItem setTradeGood(TradeGood good, Weight weight, long price) {
		InventoryItem item = getItem(good);
		item.set(weight, price);
		return item;
	}

	public void removeItem(InventoryItem item) {
		inventory.remove(item);
	}

	public void addEquipment(Equipment equip) {
		if (equip == null || equipmentInstalled.contains(equip)) {
			return;
		}
		equipmentInstalled.add(equip);
		if (equip == EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY)) {
			maxCargoHold = Weight.tonnes(35);
		}
		setMaxEquipments();
	}

	private void setMaxEquipments() {
		int count = getLaserCount(PlayerCobra.DIR_FRONT) + getLaserCount(PlayerCobra.DIR_RIGHT) +
			getLaserCount(PlayerCobra.DIR_REAR) + getLaserCount(PlayerCobra.DIR_LEFT) +
			equipmentInstalled.size();
		if (count > maxEquipments) {
			maxEquipments = count;
		}
	}

	private int getLaserCount(int where) {
		return lasers[where] == null ? 0 : 1;
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

	public int getMaxFuel() {
		return MAX_FUEL;
	}

	public void setFuel(int newFuel) {
		if (newFuel < 0) {
			newFuel = 0;
		}
		fuel = newFuel;
	}

	public void consumeFuel(int distance) {
		travelledDistance += distance;
		setFuel(fuel - (distance == 0 ? 1 : distance));
	}

	public int getTravelledDistance() {
		return travelledDistance / 10;
	}

	public void addCollision(int amount) {
		collisionAmount += amount;
	}

	public int getCollisionAmount() {
		return collisionAmount;
	}

	public void increaseHitByMissile() {
		hitByMissileCount++;
	}

	public int getHitByMissileCount() {
		return hitByMissileCount;
	}

	public void addHitByLaser(int amount) {
		hitByLaserAmount += amount;
	}

	public int getHitByLaserAmount() {
		return hitByLaserAmount;
	}

	public void increaseHitBySpaceStation() {
		hitBySpaceStationCount++;
	}

	public int getHitBySpaceStationCount() {
		return hitBySpaceStationCount;
	}

	public void changeScoopEjectCount(int goodId, long totalAmount, long realAmount, int specWeight) {
		scoopAmount += realAmount;
		switch (goodId) {
			case TradeGoodStore.ALLOYS:
				scoopedTonnePlatlet += specWeight;
				break;
			case TradeGoodStore.ALIEN_ITEMS:
				scoopedTonneAlien += specWeight;
				break;
			case TradeGoodStore.SLAVES:
				scoopedTonneEscapeCapsule += specWeight;
				break;
		}
		ejectedAmount += totalAmount + realAmount;
	}

	public long getScoopAmount() {
		return scoopAmount / Unit.TONNE.getValue();
	}

	public int getScoopedTonnePlatlet() {
		return scoopedTonnePlatlet;
	}

	public int getScoopedTonneAlien() {
		return scoopedTonneAlien;
	}

	public int getScoopedTonneEscapeCapsule() {
		return scoopedTonneEscapeCapsule;
	}

	public long getEjectedAmount() {
		return ejectedAmount / Unit.TONNE.getValue();
	}

	public void checkLowEnergy() {
		if (wasLowEnergy) {
			lowEnergyCount++;
			wasLowEnergy = false;
		}
	}

	public int getRetroRocketsTotalCount() {
		return retroRocketsTotalCount;
	}

	public int getLowEnergyCount() {
		return lowEnergyCount;
	}

	public int getMaxEquipments() {
		return maxEquipments;
	}

	public void addScoopedFuel(int fuel) {
		scoopedFuel += fuel;
	}

	public int getScoopedFuel() {
		return scoopedFuel;
	}

	public void resetEnergy() {
		frontShield   = MAX_SHIELD;
		rearShield    = MAX_SHIELD;
		energyBank[0] = MAX_ENERGY_BANK;
		energyBank[1] = MAX_ENERGY_BANK;
		energyBank[2] = MAX_ENERGY_BANK;
		energyBank[3] = MAX_ENERGY_BANK;
	}

	public float getFrontShield() {
		return frontShield;
	}

	public float getRearShield() {
		return rearShield;
	}

	public float getEnergy() {
		if (energyBank[3] < MAX_ENERGY_BANK / 2f) {
			wasLowEnergy = true;
		}
		return energyBank[0] + energyBank[1] + energyBank[2] + energyBank[3];
	}

	public float getEnergy(int idx) {
		return energyBank[idx];
	}

	public void setFrontShield(float newVal) {
		if (newVal > MAX_SHIELD + Settings.shieldPowerOverride) {
			newVal = MAX_SHIELD + Settings.shieldPowerOverride;
		}
		if (newVal < 0) {
			newVal = 0;
		}
		frontShield = newVal;
	}

	public void setRearShield(float newVal) {
		if (newVal > MAX_SHIELD + Settings.shieldPowerOverride) {
			newVal = MAX_SHIELD + Settings.shieldPowerOverride;
		}
		if (newVal < 0) {
			newVal = 0;
		}
		rearShield = newVal;
	}

	public void setEnergy(float newVal) {
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

	public void setLaserTemperature(float temp) {
		laserTemperature = temp < 0 ? 0 : Math.min(temp, MAX_LASER_TEMPERATURE);
		if (laserOverheated && !isLaserOverheated()) {
			laserOverheated = false;
		}
	}

	public float getLaserTemperature() {
		return laserTemperature;
	}

	public boolean isLaserJustOverheated() {
		if (!laserOverheated && isLaserOverheated()) {
			laserOverheated = true;
			return true;
		}
		return false;
	}

	private boolean isLaserOverheated() {
		return laserTemperature >= PlayerCobra.MAX_LASER_TEMPERATURE * 0.85;
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

	public void useRetroRockets() {
		setRetroRocketsUseCount(retroRocketsUseCount - 1);
		retroRocketsTotalCount++;
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

	public int getLaserValue(String name) {
		Equipment laser = EquipmentStore.get().getEquipmentById(name);
		return (getLaser(PlayerCobra.DIR_FRONT) == laser ? 1 : 0) +
			(getLaser(PlayerCobra.DIR_RIGHT) == laser ? 2 : 0) +
			(getLaser(PlayerCobra.DIR_REAR)  == laser ? 4 : 0) +
			(getLaser(PlayerCobra.DIR_LEFT)  == laser ? 8 : 0);
	}

	public void equipLaser(int where, String name) {
		Equipment laser = EquipmentStore.get().getEquipmentById(name);
		if ((where & 1) > 0) setLaser(PlayerCobra.DIR_FRONT, laser);
		if ((where & 2) > 0) setLaser(PlayerCobra.DIR_RIGHT, laser);
		if ((where & 4) > 0) setLaser(PlayerCobra.DIR_REAR, laser);
		if ((where & 8) > 0) setLaser(PlayerCobra.DIR_LEFT, laser);
	}

	private Integer getLaserId(int where) {
		return lasers[where] != null ? lasers[where].getId() : null;
	}

	JSONObject toJson() throws JSONException {
		JSONArray equipments = new JSONArray();
		for (Equipment e : equipmentInstalled) {
			equipments.put(new JSONObject().put("id", e.getId()));
		}

		JSONArray inventories = new JSONArray();
		for (InventoryItem i : inventory) {
			inventories.put(i.toJson(new JSONObject().put("goodId", i.getGood().getId())));
		}

		return new JSONObject()
			.put("fuel", fuel)
			.put("retroRocketsUseCount", retroRocketsUseCount)
			.put("retroRocketsTotalCount", retroRocketsTotalCount)
			.put("travelledDistance", travelledDistance)
			.put("collisionAmount", collisionAmount)
			.put("hitByMissileCount", hitByMissileCount)
			.put("hitBySpaceStationCount", hitBySpaceStationCount)
			.put("hitByLaserAmount", hitByLaserAmount)
			.put("scoopAmount", scoopAmount)
			.put("scoopedTonnePlatlet", scoopedTonnePlatlet)
			.put("scoopedTonneAlien", scoopedTonneAlien)
			.put("scoopedTonneEscapeCapsule", scoopedTonneEscapeCapsule)
			.put("ejectedAmount", ejectedAmount)
			.put("lowEnergyCount", lowEnergyCount)
			.put("maxEquipments", maxEquipments)
			.put("scoopedFuel", scoopedFuel)
			.put("missiles", missiles)
			.put("equipments", equipments)
			.putOpt("frontLaser", getLaserId(PlayerCobra.DIR_FRONT))
			.putOpt("rightLaser", getLaserId(PlayerCobra.DIR_RIGHT))
			.putOpt("rearLaser", getLaserId(PlayerCobra.DIR_REAR))
			.putOpt("leftLaser", getLaserId(PlayerCobra.DIR_LEFT))
			.put("inventories", inventories);
	}

	public void fromJson(JSONObject cobra) throws JSONException {
		clearInventory();
		clearEquipment();
		fuel = cobra.getInt("fuel");
		retroRocketsUseCount = cobra.getInt("retroRocketsUseCount");
		retroRocketsTotalCount = cobra.optInt("retroRocketsTotalCount");
		travelledDistance = cobra.getInt("travelledDistance");
		collisionAmount = cobra.optInt("collisionAmount");
		hitByMissileCount = cobra.optInt("hitByMissileCount");
		hitBySpaceStationCount = cobra.optInt("hitBySpaceStationCount");
		hitByLaserAmount = cobra.optInt("hitByLaserAmount");
		scoopAmount = cobra.optLong("scoopAmount");
		scoopedTonnePlatlet = cobra.optInt("scoopedTonnePlatlet");
		scoopedTonneAlien = cobra.optInt("scoopedTonneAlien");
		scoopedTonneEscapeCapsule = cobra.optInt("scoopedTonneEscapeCapsule");
		ejectedAmount = cobra.optLong("ejectedAmount");
		lowEnergyCount = cobra.optInt("lowEnergyCount");
		maxEquipments = cobra.optInt("maxEquipments");
		scoopedFuel = cobra.optInt("scoopedFuel");
		missiles = cobra.getInt("missiles");
		JSONArray equipments = cobra.getJSONArray("equipments");
		for (int i = 0; i < equipments.length(); i++) {
			JSONObject e = equipments.getJSONObject(i);
			addEquipment(EquipmentStore.get().getEquipmentByHash(e.getInt("id")));
		}
		lasers[PlayerCobra.DIR_FRONT] = EquipmentStore.get().getEquipmentByHash(cobra.optInt("frontLaser", -1));
		lasers[PlayerCobra.DIR_RIGHT] = EquipmentStore.get().getEquipmentByHash(cobra.optInt("rightLaser", -1));
		lasers[PlayerCobra.DIR_REAR] = EquipmentStore.get().getEquipmentByHash(cobra.optInt("rearLaser", -1));
		lasers[PlayerCobra.DIR_LEFT] = EquipmentStore.get().getEquipmentByHash(cobra.optInt("leftLaser", -1));

		JSONArray inventories = cobra.getJSONArray("inventories");
		for (int i = 0; i < inventories.length(); i++) {
			JSONObject inv = inventories.getJSONObject(i);
			TradeGood good = TradeGoodStore.get().getGoodById(inv.getInt("goodId"));
			if (good != null) {
				getItem(good).fromJson(inv);
			}
		}
	}

}
