package de.phbouillon.android.games.alite.colors;

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

import android.graphics.Color;

import java.lang.reflect.Field;
import java.util.HashMap;

public class AliteColor {

	// Further pre-defined color constants
	static final int GRAYISH_BLUE          = 0xFF8CAAEF;
	static final int LIGHT_GREEN           = 0xFF8CEF00;
	static final int ORANGE                = 0xFFEF6500;
	static final int ELECTRIC_BLUE         = 0xFF0892D0;
	static final int DARK_ELECTRIC_BLUE    = 0xFF085290;
	static final int LIGHT_ORANGE          = 0xFFEEACAC;
	static final int DARK_ORANGE           = 0xFF885555;
	static final int DKGRAY_MED_ALPHA      = 0xAA444444;
	static final int GRAY_MED_ALPHA        = 0xAA888888;
	static final int LIGHT_GREEN_LOW_ALPHA = 0x5500AA00;
	static final int DARK_GREEN_LOW_ALPHA  = 0x55007700;
	static final int LIGHT_BLUE            = 0xFFACACEE;
	static final int DARK_BLUE             = 0xFF555588;
	static final int LIGHT_RED_LOW_ALPHA   = 0x55AA0000;
	static final int DARK_RED_LOW_ALPHA    = 0x55770000;

	// Color.argb since API 26
	public static int argb(float alpha, float red, float green, float blue) {
		return (int) (alpha * 255.0f + 0.5f) << 24 |
			(int) (red   * 255.0f + 0.5f) << 16 |
			(int) (green * 255.0f + 0.5f) <<  8 |
			(int) (blue  * 255.0f + 0.5f);
	}

	public static int colorAlpha(int color, float alpha) {
		return (int) (alpha * 255.0f + 0.5f) << 24 | color & 0x00ffffff;
	}

	public static int lighten(int color, double percent) {
		return argb(color >> 24,
			(float) Math.min(1, Color.red(color) / 255.0 + percent),
			(float) Math.min(1, Color.green(color) / 255.0 + percent),
			(float) Math.min(1, Color.blue(color) / 255.0 + percent));
	}

	static {
	try {
		Field f = Color.class.getDeclaredField("sColorNameMap");
		f.setAccessible(true);
		HashMap<String, Integer> colorNameMap = (HashMap)f.get(new Color());
		// Add further pre-defined color constants
		colorNameMap.put("transparent", Color.TRANSPARENT);
		colorNameMap.put("grayishblue", GRAYISH_BLUE);
		colorNameMap.put("lightgreen", LIGHT_GREEN);
		colorNameMap.put("orange", ORANGE);
		colorNameMap.put("electricblue", ELECTRIC_BLUE);
		colorNameMap.put("darkelectricblue", DARK_ELECTRIC_BLUE);
		colorNameMap.put("lightorange", LIGHT_ORANGE);
		colorNameMap.put("darkorange", DARK_ORANGE);
		colorNameMap.put("dkgraymedalpha", DKGRAY_MED_ALPHA);
		colorNameMap.put("graymedalpha", GRAY_MED_ALPHA);
		colorNameMap.put("lightgreenlowalpha", LIGHT_GREEN_LOW_ALPHA);
		colorNameMap.put("darkgreenlowalpha", DARK_GREEN_LOW_ALPHA);
		colorNameMap.put("lightblue", LIGHT_BLUE);
		colorNameMap.put("darkblue", DARK_BLUE);
		colorNameMap.put("lightredlowalpha", LIGHT_RED_LOW_ALPHA);
		colorNameMap.put("darkredlowalpha", DARK_RED_LOW_ALPHA);
	} catch (NoSuchFieldException e) {
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	}
}
}
