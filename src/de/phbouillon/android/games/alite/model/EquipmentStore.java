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

import android.graphics.Color;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.statistics.WeaponType;

import java.util.ArrayList;
import java.util.List;

public class EquipmentStore {
	public static final int FUEL = 0;
	public static final int MISSILES = 1;
	public static final int LARGE_CARGO_BAY = 2;
	public static final int ECM_SYSTEM = 3;
	public static final int PULSE_LASER = 4;
	public static final int BEAM_LASER = 5;
	public static final int FUEL_SCOOP = 6;
	public static final int ESCAPE_CAPSULE = 7;
	public static final int ENERGY_BOMB = 8;
	public static final int EXTRA_ENERGY_UNIT = 9;
	public static final int DOCKING_COMPUTER = 10;
	public static final int GALACTIC_HYPERDRIVE = 11;
	public static final int MINING_LASER = 12;
	public static final int MILITARY_LASER = 13;
	public static final int RETRO_ROCKETS = 14;
	public static final int NAVAL_ENERGY_UNIT = 15;
	public static final int CLOAKING_DEVICE = 16;
	public static final int ECM_JAMMER = 17;

	private static EquipmentStore instance;
	private final List<Equipment> equipments = new ArrayList<>();

	public static EquipmentStore get() {
		if (instance == null) {
			instance = new EquipmentStore();
		}
		return instance;
	}

	private EquipmentStore() {
		equipments.add(new Equipment(FUEL, R.string.equip_fuel, -1, R.string.equip_short_fuel));
		equipments.add(new Equipment(MISSILES,R.string.equip_missiles, 300, R.string.equip_short_missiles));
		equipments.add(new Equipment(LARGE_CARGO_BAY, R.string.equip_large_cargo_bay, 4000, R.string.equip_short_large_cargo_bay));
		equipments.add(new Equipment(ECM_SYSTEM, R.string.equip_ecm_system, 6000, R.string.equip_short_ecm_system, "lost_ecm"));
		equipments.add(new Laser(PULSE_LASER, R.string.equip_pulse_laser, 4000, R.string.equip_short_pulse_laser, WeaponType.PulseLaser, 598802395L, 5, Color.YELLOW, false));
		equipments.add(new Laser(BEAM_LASER, R.string.equip_beam_laser, 10000, R.string.equip_short_beam_laser, WeaponType.BeamLaser, 359281437L, 9, Color.BLUE, true));
		equipments.add(new Equipment(FUEL_SCOOP, R.string.equip_fuel_scoop, 5250, R.string.equip_short_fuel_scoop, "lost_fuel_scoop"));
		equipments.add(new Equipment(ESCAPE_CAPSULE, R.string.equip_escape_capsule, 10000, R.string.equip_short_escape_capsule, "lost_escape"));
		equipments.add(new Equipment(ENERGY_BOMB, R.string.equip_energy_bomb, 9000, R.string.equip_short_energy_bomb, "lost_bomb"));
		equipments.add(new Equipment(EXTRA_ENERGY_UNIT, R.string.equip_extra_energy_unit, 15000, R.string.equip_short_extra_energy_unit, "lost_energy"));
		equipments.add(new Equipment(DOCKING_COMPUTER, R.string.equip_docking_computer, 15000, R.string.equip_short_docking_computer, "lost_docking"));
		equipments.add(new Equipment(GALACTIC_HYPERDRIVE, R.string.equip_galactic_hyperdrive, 50000, R.string.equip_short_galactic_hyperdrive, "lost_galactic"));
		equipments.add(new Laser(MINING_LASER, R.string.equip_mining_laser, 8000, R.string.equip_short_mining_laser, WeaponType.MiningLaser, 479041916L, 7, Color.GREEN, false));
		equipments.add(new Laser(MILITARY_LASER, R.string.equip_military_laser, 60000, R.string.equip_short_military_laser, WeaponType.MilitaryLaser, 179640718L, 11, Color.MAGENTA, true));
		equipments.add(new Equipment(RETRO_ROCKETS, R.string.equip_retro_rockets, 80000, R.string.equip_short_retro_rockets, "lost_retro_rockets"));

		equipments.add(new Equipment(NAVAL_ENERGY_UNIT, R.string.equip_naval_energy_unit, 15000, R.string.equip_short_naval_energy_unit));
		equipments.add(new Equipment(CLOAKING_DEVICE, R.string.equip_cloaking_device, 150000, R.string.equip_short_cloaking_device));
		equipments.add(new Equipment(ECM_JAMMER, R.string.equip_ecm_jammer, 150000, R.string.equip_short_ecm_jammer));
	}

	public Equipment getEquipment(int index) {
		return index < 0 || index >= equipments.size() ? null : equipments.get(index);
	}

	public void addEquipment(Equipment equipment) {
		equipments.add(equipment);
	}

	public Equipment getEquipmentById(int id) {
		if (id < 0) {
			return null;
		}
		for(Equipment equipment : equipments) {
			if (equipment.getId() == id) {
				return equipment;
			}
		}
		return null;
	}
}
