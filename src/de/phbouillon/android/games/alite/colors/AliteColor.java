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

import java.util.HashMap;

// Class android.graphics.Color is since API26
public class AliteColor {

	// Constants of class Color
	public static final int BLACK       = 0xFF000000;
	public static final int DKGRAY      = 0xFF444444;
	public static final int GRAY        = 0xFF888888;
	public static final int LTGRAY      = 0xFFCCCCCC;
	public static final int WHITE       = 0xFFFFFFFF;
	public static final int RED         = 0xFFFF0000;
	public static final int GREEN       = 0xFF00FF00;
	public static final int BLUE        = 0xFF0000FF;
	public static final int YELLOW      = 0xFFFFFF00;
	public static final int CYAN        = 0xFF00FFFF;
	public static final int MAGENTA     = 0xFFFF00FF;
	public static final int TRANSPARENT = 0;

	// Further pre-defined color constants
	public static final int GRAYISH_BLUE          = 0xFF8CAAEF;
	public static final int LIGHT_GREEN           = 0xFF8CEF00;
	public static final int ORANGE                = 0xFFEF6500;
	public static final int ELECTRIC_BLUE         = 0xFF0892D0;
	public static final int DARK_ELECTRIC_BLUE    = 0xFF085290;
	public static final int LIGHT_ORANGE          = 0xFFEEACAC;
	public static final int DARK_ORANGE           = 0xFF885555;
	public static final int DKGRAY_MED_ALPHA      = 0xAA444444;
	public static final int GRAY_MED_ALPHA        = 0xAA888888;
	public static final int LIGHT_GREEN_LOW_ALPHA = 0x5500AA00;
	public static final int DARK_GREEN_LOW_ALPHA  = 0x55007700;
	public static final int LIGHT_BLUE            = 0xFFACACEE;
	public static final int DARK_BLUE             = 0xFF555588;
	public static final int LIGHT_RED_LOW_ALPHA   = 0x55AA0000;
	public static final int DARK_RED_LOW_ALPHA    = 0x55770000;

	public static int alpha(int color) {
		return color >>> 24;
	}

	public static int red(int color) {
		return color >> 16 & 0xFF;
	}

	public static int green(int color) {
		return color >> 8 & 0xFF;
	}

	public static int blue(int color) {
		return color & 0xFF;
	}

	public static int rgb(int red, int green, int blue) {
		return 0xff000000 | red << 16 | green << 8 | blue;
	}

	public static int argb(float alpha, float red, float green, float blue) {
		return (int) (alpha * 255.0f + 0.5f) << 24 |
			(int) (red   * 255.0f + 0.5f) << 16 |
			(int) (green * 255.0f + 0.5f) <<  8 |
			(int) (blue  * 255.0f + 0.5f);
	}

	public static int parseColor(String colorString) {
		if (!colorString.isEmpty() && colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color");
			}
			return (int)color;
		}
		Integer color = colorNameMap.get(colorString.toLowerCase());
		if (color != null) {
			return color;
		}
		throw new IllegalArgumentException("Unknown color");
	}

	private static final HashMap<String, Integer> colorNameMap;
	static {
		// Constants of class Color
		colorNameMap = new HashMap<>();
		colorNameMap.put("black", BLACK);
		colorNameMap.put("darkgray", DKGRAY);
		colorNameMap.put("gray", GRAY);
		colorNameMap.put("lightgray", LTGRAY);
		colorNameMap.put("white", WHITE);
		colorNameMap.put("red", RED);
		colorNameMap.put("green", GREEN);
		colorNameMap.put("blue", BLUE);
		colorNameMap.put("yellow", YELLOW);
		colorNameMap.put("cyan", CYAN);
		colorNameMap.put("magenta", MAGENTA);

		// Further pre-defined color constants
		colorNameMap.put("transparent", TRANSPARENT);
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
	}
}
