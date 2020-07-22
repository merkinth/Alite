package de.phbouillon.android.games.alite;

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

public class TestLogger implements Loggable {
	@Override
	public void debug(String title, String message) {
		System.out.println("[DEBUG " + title + "] " + message);
	}

	@Override
	public void warning(String title, String message) {
		System.out.println("[WARNING " + title + "] " + message);
	}

	@Override
	public void error(String title, String message) {
		System.out.println("[ERROR " + title + "] " + message);
	}

	@Override
	public void error(String title, String message, Throwable cause) {
		System.out.println("[ERROR " + title + "] " + message + "\n" + cause.getMessage());
	}

	@Override
	public String getGlVendorData(int name) {
		return null;
	}

	@Override
	public String getMemoryData() {
		return null;
	}

	@Override
	public String getDeviceInfo() {
		return null;
	}
}
