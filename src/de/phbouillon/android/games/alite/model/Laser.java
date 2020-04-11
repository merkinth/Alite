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
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.model.statistics.WeaponType;

public class Laser extends Equipment implements Serializable {
	private static final long serialVersionUID = -8132898632556359341L;

	private final WeaponType weaponType;
	private final Timer timer = new Timer().setAutoResetWithImmediateAtFirstCall();
	private final long delayTime;
	private final int power;
	private final int color;
	private final boolean beam;

	public Laser(int id, int name, int cost, int shortName, WeaponType weaponType, long delay, int power, int color, boolean beam) {
		super(id, name, cost, shortName);
		this.weaponType = weaponType;
		this.power = power;
		this.delayTime = delay;
		this.color = color;
		this.beam = beam;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Laser " + getName(), e);
			throw e;
		}
    }

	public WeaponType getWeaponType() {
		return weaponType;
	}

	public boolean fire() {
		return timer.hasPassedNanos(delayTime);
	}

	public int getPower() {
		return power;
	}

	public int getColor() {
		return color;
	}

	public boolean isBeam() {
		return beam;
	}

	public static int getLaserValue(PlayerCobra cobra, int id) {
		Laser laser = (Laser) EquipmentStore.get().getEquipmentById(id);
		return (cobra.getLaser(PlayerCobra.DIR_FRONT) == laser ? 1 : 0) +
			(cobra.getLaser(PlayerCobra.DIR_RIGHT) == laser ? 2 : 0) +
			(cobra.getLaser(PlayerCobra.DIR_REAR)  == laser ? 4 : 0) +
			(cobra.getLaser(PlayerCobra.DIR_LEFT)  == laser ? 8 : 0);
	}

	public static void equipLaser(int where, int id, PlayerCobra cobra) {
		Laser laser = (Laser) EquipmentStore.get().getEquipmentById(id);
		if ((where & 1) > 0) cobra.setLaser(PlayerCobra.DIR_FRONT, laser);
		if ((where & 2) > 0) cobra.setLaser(PlayerCobra.DIR_RIGHT, laser);
		if ((where & 4) > 0) cobra.setLaser(PlayerCobra.DIR_REAR,  laser);
		if ((where & 8) > 0) cobra.setLaser(PlayerCobra.DIR_LEFT,  laser);
	}
}
