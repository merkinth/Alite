package de.phbouillon.android.framework.impl;

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
import java.util.ArrayList;
import java.util.List;

import de.phbouillon.android.games.alite.AliteLog;

public class Pool <T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -5141492387845777888L;

	public interface PoolObjectFactory <T extends Serializable> extends Serializable {
		T createObject();
	}

	private final List<T> freeObjects;
	private final PoolObjectFactory <T> factory;
	private final int maxSize;

	public Pool(PoolObjectFactory <T> factory, int maxSize) {
		this.factory = factory;
		this.maxSize = maxSize;
		freeObjects = new ArrayList<>(maxSize);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Pool " + getClass().getName(), e);
			throw e;
		}
	}

	public synchronized T newObject() {
		if (freeObjects.isEmpty()) {
			return factory.createObject();
		}
		T object = freeObjects.get(0);
		freeObjects.remove(0);
		return object;
	}

	public void free(T object) {
		if (freeObjects.size() < maxSize) {
			freeObjects.add(object);
		}
	}

	public synchronized void reset() {
		freeObjects.clear();
	}
}
