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

public enum ShipControl {
	ACCELEROMETER,
	ALTERNATIVE_ACCELEROMETER,
	CONTROL_PAD,
	CURSOR_BLOCK,
	CURSOR_SPLIT_BLOCK;

	// This solution (instead of initialization from constructor) is required for
	// avoiding ExceptionInInitializerError due to early reference from class Settings.
	// Beside that, it is also required in case of all enums for changing the text immediately
	// without recreating the class if language changed
	public String getDescription() {
		switch (this) {
			case ACCELEROMETER: return L.string(R.string.ship_ctrl_acc);
			case ALTERNATIVE_ACCELEROMETER: return L.string(R.string.ship_ctrl_alt_acc);
			case CONTROL_PAD: return L.string(R.string.ship_ctrl_control_pad);
			case CURSOR_BLOCK: return L.string(R.string.ship_ctrl_cursor_block);
			case CURSOR_SPLIT_BLOCK: return L.string(R.string.ship_ctrl_cursor_split_block);
		}
		return "";
	}
}
