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

import java.util.ArrayList;
import java.util.List;

public class TestInput implements Input {
	@Override
	public boolean isTouchDown(int pointer) {
		return false;
	}

	@Override
	public int getTouchCount() {
		return 0;
	}

	@Override
	public int getTouchX(int pointer) {
		return 0;
	}

	@Override
	public int getTouchY(int pointer) {
		return 0;
	}

	@Override
	public void setZoomFactor(float zoom) {

	}

	@Override
	public float getAccelX() {
		return 0;
	}

	@Override
	public float getAccelY() {
		return 0;
	}

	@Override
	public float getAccelZ() {
		return 0;
	}

	@Override
	public List<TouchEvent> getTouchEvents() {
		return new ArrayList<>();
	}

	@Override
	public List<TouchEvent> getAndRetainTouchEvents() {
		return null;
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public void switchAccelerometerHandler() {

	}

	@Override
	public boolean isAlternativeAccelerometer() {
		return false;
	}
}
