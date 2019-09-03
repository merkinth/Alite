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
import de.phbouillon.android.games.alite.AliteLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;

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

	private static final HashMap<String, Integer> colorNameMap;

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

	// Copied from class Color
	public static int parseColor(String colorString) {
		if (colorString.charAt(0) == '#') {
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
		Integer color = colorNameMap.get(colorString.toLowerCase(Locale.ROOT));
		if (color != null) {
			return color;
		}
		throw new IllegalArgumentException("Unknown color");
	}

	static {
		colorNameMap = new HashMap<>();
		// Copied from class Color
		colorNameMap.put("black", Color.BLACK);
		colorNameMap.put("darkgray", Color.DKGRAY);
		colorNameMap.put("gray", Color.GRAY);
		colorNameMap.put("lightgray", Color.LTGRAY);
		colorNameMap.put("white", Color.WHITE);
		colorNameMap.put("red", Color.RED);
		colorNameMap.put("green", Color.GREEN);
		colorNameMap.put("blue", Color.BLUE);
		colorNameMap.put("yellow", Color.YELLOW);
		colorNameMap.put("cyan", Color.CYAN);
		colorNameMap.put("magenta", Color.MAGENTA);
		colorNameMap.put("aqua", 0xFF00FFFF);
		colorNameMap.put("fuchsia", 0xFFFF00FF);
		colorNameMap.put("darkgrey", Color.DKGRAY);
		colorNameMap.put("grey", Color.GRAY);
		colorNameMap.put("lightgrey", Color.LTGRAY);
		colorNameMap.put("lime", 0xFF00FF00);
		colorNameMap.put("maroon", 0xFF800000);
		colorNameMap.put("navy", 0xFF000080);
		colorNameMap.put("olive", 0xFF808000);
		colorNameMap.put("purple", 0xFF800080);
		colorNameMap.put("silver", 0xFFC0C0C0);
		colorNameMap.put("teal", 0xFF008080);
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
	}
}
