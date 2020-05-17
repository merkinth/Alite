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

import de.phbouillon.android.games.alite.R;

import java.util.*;

public class EquipmentStore {
	public static final String FUEL = "EQ_FUEL";
	public static final String MISSILES = "EQ_MISSILE";
	public static final String LARGE_CARGO_BAY = "EQ_CARGO_BAY";
	public static final String ECM_SYSTEM = "EQ_ECM";
	public static final String PULSE_LASER = "EQ_WEAPON_PULSE_LASER";
	public static final String BEAM_LASER = "EQ_WEAPON_BEAM_LASER";
	public static final String FUEL_SCOOP = "EQ_FUEL_SCOOPS";
	public static final String ESCAPE_CAPSULE = "EQ_ESCAPE_POD";
	public static final String ENERGY_BOMB = "EQ_ENERGY_BOMB";
	public static final String EXTRA_ENERGY_UNIT = "EQ_ENERGY_UNIT";
	public static final String DOCKING_COMPUTER = "EQ_DOCK_COMP";
	public static final String GALACTIC_HYPERDRIVE = "EQ_GAL_DRIVE";
	public static final String MINING_LASER = "EQ_WEAPON_MINING_LASER";
	public static final String MILITARY_LASER = "EQ_WEAPON_MILITARY_LASER";
	public static final String RETRO_ROCKETS = "EQ_RETRO_ROCKETS";
	public static final String NAVAL_ENERGY_UNIT = "EQ_NAVAL_ENERGY_UNIT";
	public static final String CLOAKING_DEVICE = "EQ_CLOAKING_DEVICE";
	public static final String ECM_JAMMER = "EQ_ECM_JAMMER";

	private static EquipmentStore instance;
	private final List<Equipment> equipments = new ArrayList<>();

	public static EquipmentStore get() {
		if (instance == null) {
			instance = new EquipmentStore();
		}
		return instance;
	}

	private EquipmentStore() {
	}

	public Equipment getEquipment(int index) {
		return index < 0 || index >= equipments.size() ? null : equipments.get(index);
	}

	public void addEquipment(Equipment equipment) {
		equipments.add(equipment);
	}

	public Equipment getEquipmentById(String name) {
		return getEquipmentByHash(Equipment.getEquipmentId(name));
	}

	public Equipment getEquipmentByHash(int id) {
		for(Equipment equipment : equipments) {
			if (equipment.getId() == id) {
				return equipment;
			}
		}
		return null;
	}

	public Iterator<Equipment> getIterator() {
		return equipments.iterator();
	}
}
