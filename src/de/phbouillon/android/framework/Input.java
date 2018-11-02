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
import java.util.List;

public interface Input {
	class TouchEvent implements Serializable {
		private static final long serialVersionUID = 547212806154304758L;

		public static final int TOUCH_DOWN    = 0;
		public static final int TOUCH_UP      = 1;
		public static final int TOUCH_DRAGGED = 2;
		public static final int TOUCH_SCALE   = 3;
		public static final int TOUCH_SWEEP   = 4;

		public int type;
		public int x;
		public int y;
		public int x2;
		public int y2;
		public int pointer;
		public float zoomFactor;
	}

	boolean isTouchDown(int pointer);
	int getTouchCount();
	int getTouchX(int pointer);
	int getTouchY(int pointer);
	void setZoomFactor(float zoom);
	float getAccelX();
	float getAccelY();
	float getAccelZ();
	List<TouchEvent> getTouchEvents();
	List<TouchEvent> getAndRetainTouchEvents();
	void dispose();
	boolean isDisposed();
	void switchAccelerometerHandler();
	boolean isAlternativeAccelerometer();
}
