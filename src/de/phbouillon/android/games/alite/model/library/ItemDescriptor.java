package de.phbouillon.android.games.alite.model.library;

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

public class ItemDescriptor {
	private final String fileName;
	private final String text;
	private final boolean localized;
	private boolean watched;

	ItemDescriptor(String fileName, String text, boolean localized) {
		this.fileName = fileName;
		this.text = text;
		this.localized = localized;
	}

	public String getFileName() {
		return fileName;
	}

	public String getText() {
		return text;
	}

	public boolean isLocalized() {
		return localized;
	}

	public boolean isWatched() {
		return watched;
	}

	public void watched() {
		watched = true;
	}
}
