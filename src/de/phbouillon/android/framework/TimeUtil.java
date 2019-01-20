package de.phbouillon.android.framework;

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

public class TimeUtil {
	public static final long NANOS = 1;
	public static final long MICROS = 1000 * NANOS;
	public static final long MILLIS = 1000 * MICROS;
	public static final long SECONDS = 1000 * MILLIS;

	public static long getPassedTime(long from, long unit) {
		return (System.nanoTime() - from) / unit;
	}

	public static boolean hasPassed(long from, long time, long unit) {
		return getPassedTime(from, unit) > time;
	}

}
