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

public class EquipmentStore {
	static final int ENTRIES = 18;

	public static final Equipment fuel = new Equipment(0, R.string.equip_fuel, -1, R.string.equip_short_fuel, false);
	public static final Equipment missiles = new Equipment(1,R.string.equip_missiles, 300, R.string.equip_short_missiles, false);
	public static final Equipment largeCargoBay = new Equipment(2, R.string.equip_large_cargo_bay, 4000, R.string.equip_short_large_cargo_bay, false);
	public static final Equipment ecmSystem = new Equipment(3, R.string.equip_ecm_system, 6000, R.string.equip_short_ecm_system, true);
	public static final Laser pulseLaser = new Laser(4, R.string.equip_pulse_laser, 4000, R.string.equip_short_pulse_laser, 0, 598802395L, 5, Color.YELLOW, false);
	public static final Laser beamLaser = new Laser(5, R.string.equip_beam_laser, 10000, R.string.equip_short_beam_laser, 1, 359281437L, 9, Color.BLUE, true);
	public static final Equipment fuelScoop = new Equipment(6, R.string.equip_fuel_scoop, 5250, R.string.equip_short_fuel_scoop, true);
	public static final Equipment escapeCapsule = new Equipment(7, R.string.equip_escape_capsule, 10000, R.string.equip_short_escape_capsule, true);
	public static final Equipment energyBomb = new Equipment(8, R.string.equip_energy_bomb, 9000, R.string.equip_short_energy_bomb, true);
	public static final Equipment extraEnergyUnit = new Equipment(9, R.string.equip_extra_energy_unit, 15000, R.string.equip_short_extra_energy_unit, true);
	public static final Equipment dockingComputer = new Equipment(10, R.string.equip_docking_computer, 15000, R.string.equip_short_docking_computer, true);
	public static final Equipment galacticHyperdrive = new Equipment(11, R.string.equip_galactic_hyperdrive, 50000, R.string.equip_short_galactic_hyperdrive, true);
	public static final Laser miningLaser = new Laser(12, R.string.equip_mining_laser, 8000, R.string.equip_short_mining_laser, 2, 479041916L, 7, Color.GREEN, false);
	public static final Laser militaryLaser = new Laser(13, R.string.equip_military_laser, 60000, R.string.equip_short_military_laser, 3, 179640718L, 11, Color.MAGENTA, true);
	public static final Equipment retroRockets = new Equipment(14, R.string.equip_retro_rockets, 80000, R.string.equip_short_retro_rockets, true);

	public static final Equipment navalEnergyUnit = new Equipment(15, R.string.equip_naval_energy_unit, 15000, R.string.equip_short_naval_energy_unit, false);
	public static final Equipment cloakingDevice = new Equipment(16, R.string.equip_cloaking_device, 150000, R.string.equip_short_cloaking_device, false);
	public static final Equipment ecmJammer = new Equipment(17, R.string.equip_ecm_jammer, 150000, R.string.equip_short_ecm_jammer, false);

	public static Equipment fromInt(int val) {
		switch (val) {
			case 0: return fuel;
			case 1: return missiles;
			case 2: return largeCargoBay;
			case 3: return ecmSystem;
			case 4: return pulseLaser;
			case 5: return beamLaser;
			case 6: return fuelScoop;
			case 7: return escapeCapsule;
			case 8: return energyBomb;
			case 9: return extraEnergyUnit;
			case 10: return dockingComputer;
			case 11: return galacticHyperdrive;
			case 12: return miningLaser;
			case 13: return militaryLaser;
			case 14: return retroRockets;
			case 15: return navalEnergyUnit;
			case 16: return cloakingDevice;
			case 17: return ecmJammer;
		}
		return null;
	}
}
