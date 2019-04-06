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

import java.io.Serializable;

public class Timer implements Serializable {
	private static final long serialVersionUID = -5442756109577136730L;

	private static final float NANOS = 1;
	private static final float MICROS = 1000 * NANOS;
	private static final float MILLIS = 1000 * MICROS;
	private static final float SECONDS = 1000 * MILLIS;

	private long startTime = System.nanoTime();
	private long currentTime = startTime;
	private boolean autoReset;
	private boolean skipFirstCall;

	public Timer setAutoReset() {
		autoReset = true;
		return this;
	}
	public Timer setAutoResetWithImmediateAtFirstCall() {
		startTime = 0;
		return setAutoReset();
	}

	public Timer setAutoResetWithSkipFirstCall() {
		skipFirstCall = true;
		return setAutoReset();
	}

	public void reset() {
		getPassedNanos();
		startTime = currentTime;
	}

	public long getTimer() {
		return startTime;
	}

	public void setTimer(long startTime) {
		this.startTime = startTime;
	}

	public long getPassedNanos() {
		return (long)getPassedTime(NANOS);
	}

	public float getPassedSeconds() {
		return getPassedTime(SECONDS);
	}

	private float getPassedTime(float unit) {
		currentTime = System.nanoTime();
		return skipFirstCall ? 0 : (currentTime - startTime) / unit;
	}

	public boolean hasPassedNanos(float time) {
		return hasPassed(time, NANOS);
	}

	public boolean hasPassedMicros(float time) {
		return hasPassed(time, MICROS);
	}

	public boolean hasPassedMillis(float time) {
		return hasPassed(time, MILLIS);
	}

	public boolean hasPassedSeconds(float time) {
		return hasPassed(time, SECONDS);
	}

	private boolean hasPassed(float time, float unit) {
		boolean passed = getPassedTime(unit) > time;
		if (autoReset && (passed || skipFirstCall)) startTime = currentTime;
		if (skipFirstCall) {
			skipFirstCall = false;
			return false;
		}
		return passed;
	}

}
