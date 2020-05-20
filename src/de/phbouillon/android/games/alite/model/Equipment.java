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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import de.phbouillon.android.framework.Timer;

public class Equipment implements Serializable {
	private static final long serialVersionUID = -3769076685902433528L;

	private static final String EQUIPMENT_DEFAULT_ICON = "equipment_icons/default";

	private static final List<String> legacyEquipmentId = Arrays.asList("EQ_FUEL", "EQ_MISSILE",
		"EQ_CARGO_BAY", "EQ_ECM", "EQ_WEAPON_PULSE_LASER", "EQ_WEAPON_BEAM_LASER", "EQ_FUEL_SCOOPS",
		"EQ_ESCAPE_POD", "EQ_ENERGY_BOMB", "EQ_ENERGY_UNIT", "EQ_DOCK_COMP", "EQ_GAL_DRIVE",
		"EQ_WEAPON_MINING_LASER", "EQ_WEAPON_MILITARY_LASER", "EQ_RETRO_ROCKETS", "EQ_NAVAL_ENERGY_UNIT",
		"EQ_CLOAKING_DEVICE", "EQ_ECM_JAMMER");

	private final int id;
	private final int minTechLevel;
	private final int cost;
	private final Repository<Property> repoHandler = new Repository<>();
	private Timer timer;
	private Repository<WeaponProperty> weaponInfo;

	private enum Property {
		available_to_all,
		available_to_NPCs,
		available_to_player,
		condition_script,
		damage_probability,
		display_color,
		fast_affinity_defensive,
		fast_affinity_offensive,
		incompatible_with_equipment,
		installation_time,
		is_external_store,
		portable_between_ships,
		provides,
		repair_time,
		requires_any_equipment,
		requires_cargo_space,
		requires_clean,
		requires_empty_pylon,
		requires_equipment,
		requires_free_passenger_berth,
		requires_full_fuel,
		requires_mounted_pylon,
		requires_not_clean,
		requires_non_full_fuel,
		script,
		script_info,
		strict_mode_only,
		strict_mode_compatible,
		visible,
		weapon_info,
		icon, // only in Alite
		lost_sound, // only in Alite

		name, // for localization
		short_name // for localization
	}

	private enum WeaponProperty {
		range,
		energy,
		damage,
		recharge_rate,
		shot_temperature,
		color,
		threat_assessment,
		is_mining_laser,
		is_turret_laser,
		crosshairs, // only in Alite, format: texture_file atlas_name, planned format: texture_file [atlas_name | x y width height]
		beam_length // only in Alite
	}

	public Equipment(String equipmentName, String name, int minTechLevel, int cost, String shortName,
			NSObject properties) throws IOException {
		id = getEquipmentId(equipmentName);
		repoHandler.setLocalizedProperty(Property.name, name);
		this.minTechLevel = minTechLevel;
		this.cost = cost;
		repoHandler.setLocalizedProperty(Property.short_name, shortName);
		repoHandler.setProperty(Property.icon, EQUIPMENT_DEFAULT_ICON);
		if (equipmentName.startsWith("EQ_WEAPON_")) {
			timer = new Timer().setAutoResetWithImmediateAtFirstCall();
			weaponInfo = new Repository<>();
			weaponInfo.setProperty(WeaponProperty.range, 12500);
			weaponInfo.setProperty(WeaponProperty.energy, 0.8);
			weaponInfo.setProperty(WeaponProperty.damage, 15.0);
			weaponInfo.setProperty(WeaponProperty.shot_temperature, 7.0);
		}
		readProperties(properties);
	}

	private void readProperties(NSObject properties) throws IOException {
		NSDictionary dict = (NSDictionary) properties;
		for (Map.Entry<String, NSObject> p: dict.entrySet()) {
			if ("weapon_info".equals(p.getKey())) {
				if (weaponInfo == null) {
					throw new IOException("Invalid property '" + p.getKey() + "' for equipment.");
				}
				for (Map.Entry<String, NSObject> w: ((NSDictionary) p.getValue()).entrySet()) {
					weaponInfo.setProperty(WeaponProperty.valueOf(w.getKey()), w.getValue());
				}
			} else {
				repoHandler.setProperty(Equipment.Property.valueOf(p.getKey()), p.getValue());
			}
		}
	}

	static int getEquipmentId(String equipmentName) {
		int index = legacyEquipmentId.indexOf(equipmentName);
		return index < 0 && !equipmentName.isEmpty() ? -equipmentName.hashCode() : index;
	}

	public boolean isLaser() {
		return weaponInfo != null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return repoHandler.getResStringProperty(Property.name);
	}

	public int getMinTechLevel() {
		return minTechLevel;
	}

	public int getCost() {
		return cost;
	}

	public String getShortName() {
		return repoHandler.getResStringProperty(Property.short_name);
	}

	boolean canBeLost() {
		return repoHandler.getStringProperty(Property.lost_sound) != null;
	}

	public void setDefaultIcon() {
		repoHandler.setProperty(Property.icon, EQUIPMENT_DEFAULT_ICON);
	}

	public String getIcon() {
		return repoHandler.getStringProperty(Equipment.Property.icon);
	}

	public String getLostSound() {
		return repoHandler.getStringProperty(Property.lost_sound);
	}

	public boolean fire() {
		return timer.hasPassedSeconds(weaponInfo.getNumericProperty(WeaponProperty.recharge_rate));
	}
	public float getRangeSq() {
		Float range = weaponInfo.getNumericProperty(WeaponProperty.range);
		return range * range;
	}

	public float getEnergy() {
		return weaponInfo.getNumericProperty(WeaponProperty.energy);
	}

	public int getDamage() {
		return weaponInfo.getNumericProperty(WeaponProperty.damage).intValue();
	}

	public float getShotTemperature() {
		return weaponInfo.getNumericProperty(WeaponProperty.shot_temperature);
	}

	public int getColor() {
		return weaponInfo.getColorProperty(WeaponProperty.color);
	}

	public boolean isMining() {
		return weaponInfo.getNumericProperty(WeaponProperty.is_mining_laser) != 0;
	}

	public String[] getCrosshairs() {
		return weaponInfo.getStringProperty(WeaponProperty.crosshairs).split(" ");
	}

	public int getBeamLength() {
		return weaponInfo.getNumericProperty(WeaponProperty.beam_length).intValue();
	}

}
