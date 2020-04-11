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

import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;

public class Equipment implements Serializable {
	private static final long serialVersionUID = -3769076685902433528L;

	private int id;
	private final int name;
	private final int cost;
	private final int shortName;
	private final String lostSoundFilename;

	public Equipment(int id, int name, int cost, int shortName) {
		this.id = id;
		this.name = name;
		this.cost = cost;
		this.shortName = shortName;
		this.lostSoundFilename = null;
	}

	public Equipment(int id, int name, int cost, int shortName, String lostSoundFilename) {
		this.id = id;
		this.name = name;
		this.cost = cost;
		this.shortName = shortName;
		this.lostSoundFilename = lostSoundFilename;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Equipment " + getName(), e);
			throw e;
		}
    }

	public int getId() {
		return id;
	}

	public String getName() {
		return L.string(name);
	}

	public int getCost() {
		return cost;
	}

	public String getShortName() {
		return L.string(shortName);
	}

	boolean canBeLost() {
		return lostSoundFilename != null;
	}
}
