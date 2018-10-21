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

import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ColorScheme {
	public static int COLOR_CONDITION_GREEN = 0;
	public static int COLOR_CONDITION_YELLOW = 1;
	public static int COLOR_CONDITION_RED = 2;
	public static int COLOR_BACKGROUND = 3;
	public static int COLOR_BASE_INFORMATION = 4;
	public static int COLOR_MAIN_TEXT = 5;
	public static int COLOR_INFORMATION_TEXT = 6;
	public static int COLOR_ADDITIONAL_TEXT = 7;
	public static int COLOR_MESSAGE = 8;
	public static int COLOR_WARNING_MESSAGE = 9;
	public static int COLOR_FUEL_CIRCLE = 10;
	public static int COLOR_DASHED_FUEL_CIRCLE = 11;
	public static int COLOR_INHABITANT_INFORMATION = 12;
	public static int COLOR_ARROW = 13;
	public static int COLOR_ECONOMY = 14;
	public static int COLOR_GOVERNMENT = 15;
	public static int COLOR_TECH_LEVEL = 16;
	public static int COLOR_POPULATION = 17;
	public static int COLOR_SHIP_DISTANCE = 18;
	public static int COLOR_DIAMETER = 19;
	public static int COLOR_GNP = 20;
	public static int COLOR_CURRENT_SYSTEM_NAME = 21;
	public static int COLOR_HYPERSPACE_SYSTEM_NAME = 22;
	public static int COLOR_LEGAL_STATUS = 23;
	public static int COLOR_REMAINING_FUEL = 24;
	public static int COLOR_BALANCE = 25;
	public static int COLOR_RATING = 26;
	public static int COLOR_MISSION_OBJECTIVE = 27;
	public static int COLOR_EQUIPMENT_DESCRIPTION = 28;
	public static int COLOR_RICH_INDUSTRIAL = 29;
	public static int COLOR_AVERAGE_INDUSTRIAL = 30;
	public static int COLOR_POOR_INDUSTRIAL = 31;
	public static int COLOR_MAIN_INDUSTRIAL = 32;
	public static int COLOR_MAIN_AGRICULTURAL = 33;
	public static int COLOR_RICH_AGRICULTURAL = 34;
	public static int COLOR_AVERAGE_AGRICULTURAL = 35;
	public static int COLOR_POOR_AGRICULTURAL = 36;
	public static int COLOR_BACKGROUND_LIGHT = 37;
	public static int COLOR_BACKGROUND_DARK = 38;
	public static int COLOR_FRAME_LIGHT = 39;
	public static int COLOR_FRAME_DARK = 40;
	public static int COLOR_SELECTED_COLORED_FRAME_LIGHT = 41;
	public static int COLOR_SELECTED_COLORED_FRAME_DARK = 42;
	public static int COLOR_PULSING_HIGHLIGHTER_DARK = 43;
	public static int COLOR_PULSING_HIGHLIGHTER_LIGHT = 44;
	public static int COLOR_TUTORIAL_BUBBLE_DARK = 45;
	public static int COLOR_TUTORIAL_BUBBLE_LIGHT = 46;
	public static int COLOR_SHIP_TITLE = 47;
	public static int COLOR_TEXT_AREA_BACKGROUND = 48;
	public static int COLOR_CURSOR = 49;
	public static int COLOR_PRICE = 50;
	public static int COLOR_SCROLLING_TEXT = 51;
	public static int COLOR_SELECTED_TEXT = 52;
	public static int COLOR_CREDITS_DESCRIPTION = 53;
	public static int COLOR_CREDITS_PERSON = 54;
	public static int COLOR_CREDITS_ADDITION = 55;
	public static int COLOR_FRONT_SHIELD = 56;
	public static int COLOR_AFT_SHIELD = 57;
	public static int COLOR_FUEL = 58;
	public static int COLOR_CABIN_TEMPERATURE = 59;
	public static int COLOR_LASER_TEMPERATURE = 60;
	public static int COLOR_ALTITUDE = 61;
	public static int COLOR_SPEED = 62;
	public static int COLOR_ENERGY_BANK_X = 63; // 0..3
	public static int COLOR_ENERGY_BANK_WHOLE = 67;
	public static int COLOR_INDICATOR_BAR = 68;
	public static int COLOR_HIGHLIGHT_COLOR = 69;

	private static int MAX_COLORS = 69;

	static final String ALITE_COLOR_SCHEME_EXTENSION = ".acs";

	public static final String COLOR_SCHEME_CLASSIC = "0";
	private static final String COLOR_SCHEME_MODERN = "1";

	private static int[][] classicColorSchemeColors = new int[][] {
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_CONDITION_GREEN
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_CONDITION_YELLOW
		{AliteColor.RED, AliteColor.RED}, // COLOR_CONDITION_RED
		{AliteColor.BLACK, AliteColor.BLACK}, // COLOR_BACKGROUND
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_BASE_INFORMATION
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_MAIN_TEXT
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_INFORMATION_TEXT
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_ADDITIONAL_TEXT
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_MESSAGE
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_WARNING_MESSAGE
		{AliteColor.RED, AliteColor.RED}, // COLOR_FUEL_CIRCLE
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_DASHED_FUEL_CIRCLE
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_INHABITANT_INFORMATION
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_ARROW
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_ECONOMY
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_GOVERNMENT
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_TECH_LEVEL
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_POPULATION
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_SHIP_DISTANCE
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_DIAMETER
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_GNP
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_CURRENT_SYSTEM_NAME
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_HYPERSPACE_SYSTEM_NAME
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_LEGAL_STATUS
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_REMAINING_FUEL
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_BALANCE
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_RATING
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_MISSION_OBJECTIVE
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_EQUIPMENT_DESCRIPTION
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_RICH_INDUSTRIAL
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_AVERAGE_INDUSTRIAL
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_POOR_INDUSTRIAL
		{AliteColor.RED, AliteColor.RED}, // COLOR_MAIN_INDUSTRIAL
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_MAIN_AGRICULTURAL
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_RICH_AGRICULTURAL
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_AVERAGE_AGRICULTURAL
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_POOR_AGRICULTURAL
		{AliteColor.LIGHT_BLUE, AliteColor.LIGHT_BLUE}, // COLOR_BACKGROUND_LIGHT
		{AliteColor.DARK_BLUE, AliteColor.DARK_BLUE}, // COLOR_BACKGROUND_DARK
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_FRAME_LIGHT
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_FRAME_DARK
		{AliteColor.LIGHT_ORANGE, AliteColor.LIGHT_ORANGE}, // COLOR_SELECTED_COLORED_FRAME_LIGHT
		{AliteColor.DARK_ORANGE, AliteColor.DARK_ORANGE}, // COLOR_SELECTED_COLORED_FRAME_DARK
		{AliteColor.DARK_RED_LOW_ALPHA, AliteColor.DARK_RED_LOW_ALPHA}, // COLOR_PULSING_HIGHLIGHTER_DARK
		{AliteColor.LIGHT_RED_LOW_ALPHA, AliteColor.LIGHT_RED_LOW_ALPHA}, // COLOR_PULSING_HIGHLIGHTER_LIGHT
		{AliteColor.DKGRAY_MED_ALPHA, AliteColor.DKGRAY_MED_ALPHA}, // COLOR_TUTORIAL_BUBBLE_DARK
		{AliteColor.GRAY_MED_ALPHA, AliteColor.GRAY_MED_ALPHA}, // COLOR_TUTORIAL_BUBBLE_LIGHT
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_SHIP_TITLE
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_TEXT_AREA_BACKGROUND
		{AliteColor.LIGHT_BLUE, AliteColor.LIGHT_BLUE}, // COLOR_CURSOR
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_PRICE
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_SCROLLING_TEXT
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_SELECTED_TEXT
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_CREDITS_DESCRIPTION
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_CREDITS_PERSON
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_CREDITS_ADDITION
		{AliteColor.MAGENTA, AliteColor.RED}, // COLOR_FRONT_SHIELD
		{AliteColor.MAGENTA, AliteColor.RED}, // COLOR_AFT_SHIELD
		{AliteColor.GREEN, AliteColor.RED}, // COLOR_FUEL
		{AliteColor.GREEN, AliteColor.RED}, // COLOR_CABIN_TEMPERATURE
		{AliteColor.GREEN, AliteColor.RED}, // COLOR_LASER_TEMPERATURE
		{AliteColor.GREEN, AliteColor.RED}, // COLOR_ALTITUDE
		{AliteColor.RED, AliteColor.GREEN}, // COLOR_SPEED
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_ENERGY_BANK_1
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_ENERGY_BANK_2
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_ENERGY_BANK_3
		{AliteColor.MAGENTA, AliteColor.RED}, // COLOR_ENERGY_BANK_4
		{AliteColor.TRANSPARENT, AliteColor.TRANSPARENT}, // COLOR_ENERGY_BANK_WHOLE
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_INDICATOR_BAR
		{AliteColor.DARK_RED_LOW_ALPHA, AliteColor.DARK_RED_LOW_ALPHA} // COLOR_HIGHLIGHT_COLOR
	};

	private static int[][] modernColorSchemeColors = new int[][]{
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CONDITION_GREEN
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CONDITION_YELLOW
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CONDITION_RED
		{AliteColor.BLACK, AliteColor.BLACK}, // COLOR_BACKGROUND
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_BASE_INFORMATION
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_MAIN_TEXT
		{AliteColor.DARK_ELECTRIC_BLUE, AliteColor.DARK_ELECTRIC_BLUE}, // COLOR_INFORMATION_TEXT
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_ADDITIONAL_TEXT
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_MESSAGE
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_WARNING_MESSAGE
		{AliteColor.RED, AliteColor.RED}, // COLOR_FUEL_CIRCLE
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_DASHED_FUEL_CIRCLE
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_INHABITANT_INFORMATION
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_ARROW
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_ECONOMY
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_GOVERNMENT
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_TECH_LEVEL
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_POPULATION
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_SHIP_DISTANCE
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_DIAMETER
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_GNP
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CURRENT_SYSTEM_NAME
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_HYPERSPACE_SYSTEM_NAME
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_LEGAL_STATUS
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_REMAINING_FUEL
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_BALANCE
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_RATING
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_MISSION_OBJECTIVE
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_EQUIPMENT_DESCRIPTION
		{AliteColor.GRAYISH_BLUE, AliteColor.GRAYISH_BLUE}, // COLOR_RICH_INDUSTRIAL
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_AVERAGE_INDUSTRIAL
		{AliteColor.MAGENTA, AliteColor.MAGENTA}, // COLOR_POOR_INDUSTRIAL
		{AliteColor.RED, AliteColor.RED}, // COLOR_MAIN_INDUSTRIAL
		{AliteColor.YELLOW, AliteColor.YELLOW}, // COLOR_MAIN_AGRICULTURAL
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_RICH_AGRICULTURAL
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_AVERAGE_AGRICULTURAL
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_POOR_AGRICULTURAL
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_BACKGROUND_LIGHT
		{AliteColor.DKGRAY, AliteColor.DKGRAY}, // COLOR_BACKGROUND_DARK
		{AliteColor.WHITE, AliteColor.WHITE}, // COLOR_FRAME_LIGHT
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_FRAME_DARK
		{AliteColor.LIGHT_ORANGE, AliteColor.LIGHT_ORANGE}, // COLOR_SELECTED_COLORED_FRAME_LIGHT
		{AliteColor.DARK_ORANGE, AliteColor.DARK_ORANGE}, // COLOR_SELECTED_COLORED_FRAME_DARK
		{AliteColor.DARK_GREEN_LOW_ALPHA, AliteColor.DARK_GREEN_LOW_ALPHA}, // COLOR_PULSING_HIGHLIGHTER_DARK
		{AliteColor.LIGHT_GREEN_LOW_ALPHA, AliteColor.LIGHT_GREEN_LOW_ALPHA}, // COLOR_PULSING_HIGHLIGHTER_LIGHT
		{AliteColor.DKGRAY_MED_ALPHA, AliteColor.DKGRAY_MED_ALPHA}, // COLOR_TUTORIAL_BUBBLE_DARK
		{AliteColor.GRAY_MED_ALPHA, AliteColor.GRAY_MED_ALPHA}, // COLOR_TUTORIAL_BUBBLE_LIGHT
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_SHIP_TITLE
		{AliteColor.GRAY, AliteColor.GRAY}, // COLOR_TEXT_AREA_BACKGROUND
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CURSOR
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_PRICE
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_SCROLLING_TEXT
		{AliteColor.ORANGE, AliteColor.ORANGE}, // COLOR_SELECTED_TEXT
		{AliteColor.DARK_ELECTRIC_BLUE, AliteColor.DARK_ELECTRIC_BLUE}, // COLOR_CREDITS_DESCRIPTION
		{AliteColor.ELECTRIC_BLUE, AliteColor.ELECTRIC_BLUE}, // COLOR_CREDITS_PERSON
		{AliteColor.DARK_ORANGE, AliteColor.DARK_ORANGE}, // COLOR_CREDITS_ADDITION
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_FRONT_SHIELD
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_AFT_SHIELD
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_FUEL
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_CABIN_TEMPERATURE
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_LASER_TEMPERATURE
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_ALTITUDE
		{AliteColor.RED, AliteColor.LIGHT_GREEN}, // COLOR_SPEED
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_ENERGY_BANK_1
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_ENERGY_BANK_2
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_ENERGY_BANK_3
		{AliteColor.LIGHT_GREEN, AliteColor.RED}, // COLOR_ENERGY_BANK_4
		{AliteColor.TRANSPARENT, AliteColor.TRANSPARENT}, // COLOR_ENERGY_BANK_WHOLE
		{AliteColor.LIGHT_GREEN, AliteColor.LIGHT_GREEN}, // COLOR_INDICATOR_BAR
		{AliteColor.DARK_GREEN_LOW_ALPHA, AliteColor.DARK_GREEN_LOW_ALPHA} // COLOR_HIGHLIGHT_COLOR
	};

	private static final String DIRECTORY_COLOR_SCHEMES = "color_schemes";

	private static int[][] colors = classicColorSchemeColors;
	private static List<String> colorSchemes = new ArrayList<>();
	private static int nextColorSchemeIndex = 0;

	public static String setColorScheme(FileIO f, String schemeContent, String schemeName) {
		switch (schemeName) {
			case COLOR_SCHEME_CLASSIC: colors = classicColorSchemeColors; break;
			case COLOR_SCHEME_MODERN: colors = modernColorSchemeColors; break;
			default:
				AliteLog.d("Load color scheme file", "file name: " + DIRECTORY_COLOR_SCHEMES + schemeName);
				try {
					return loadColorSchemeValues(schemeContent == null ?
						new String(f.readFileContents(DIRECTORY_COLOR_SCHEMES + File.separator + schemeName)) : schemeContent);
				} catch (IOException e) {
					return e.getMessage();
				}
		}
		return "";
	}

	private static String loadColorSchemeValues(String schemeContent) {
		colors = new int[MAX_COLORS + 1][2];
		for (int i = 0; i < colors.length; i++) {
			colors[i][0] = classicColorSchemeColors[i][0];
			colors[i][1] = classicColorSchemeColors[i][1];
		}
		String colorItem = "";
		String color = null;
		try {
			HashMap<String, Integer> colorNameMap = new HashMap<>();
			JSONObject scheme = new JSONObject(schemeContent);
			JSONObject colorItems = scheme.optJSONObject("colorDefinition");
			if (colorItems != null) {
				Iterator<String> i = colorItems.keys();
				while (i.hasNext()) {
					colorItem = i.next();
					color = colorItems.getString(colorItem);
					colorNameMap.put(colorItem.toLowerCase(), AliteColor.parseColor(color));
				}
			}

			colorItems = scheme.getJSONObject("colors");
			Iterator<String> i = colorItems.keys();
			while (i.hasNext()) {
				colorItem = i.next();
				int index = getColorItemIndex(colorItem);
				if (index == -1) {
					return "Unknown color item '" + colorItem + "'.";
				}
				Object value = colorItems.get(colorItem);
				if (value instanceof String) {
					color = (String) value;
					colors[index][0] = parseColor(color, colorNameMap);
				} else {
					color = ((JSONObject) value).getString("startColor");
					colors[index][0] = parseColor(color, colorNameMap);
					color = ((JSONObject) value).getString("endColor");
					colors[index][1] = parseColor(color, colorNameMap);
				}
			}
			return null;
		} catch (JSONException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) {
			return e.getMessage() + " (" + color + ") for color item '" + colorItem + "'.";
		}
	}

	private static int parseColor(String colorString, HashMap<String, Integer> localColorNameMap) {
		try {
			return AliteColor.parseColor(colorString);
		} catch (IllegalArgumentException ignored) {
			Integer color = localColorNameMap.get(colorString.toLowerCase());
			if (color != null) {
				return color;
			}
			throw new IllegalArgumentException("Unknown color");
		}
	}

	private static int getColorItemIndex(String name) {
		for (int i=0; i < schemeItemNameMap.length; i++) {
			if (schemeItemNameMap[i].equals(name.toLowerCase())) {
				return i;
			}
		}
		return -1;
	}

	public static String getSchemeDisplayName(String schemeName) {
		if (COLOR_SCHEME_CLASSIC.equals(schemeName)) return Alite.get().getResources().getString(R.string.color_scheme_classic);
		if (COLOR_SCHEME_MODERN.equals(schemeName)) return Alite.get().getResources().getString(R.string.color_scheme_modern);
		return schemeName.substring(0, schemeName.indexOf(ALITE_COLOR_SCHEME_EXTENSION));
	}

	public static void loadColorSchemeList(FileIO f, String schemeName) {
		try {
			nextColorSchemeIndex = 0;
			File[] colorSchemeFiles = f.getFiles(DIRECTORY_COLOR_SCHEMES, "(.*)\\" + ALITE_COLOR_SCHEME_EXTENSION);
			colorSchemes.clear();
			// if color_schemes directory does not exist
			if (colorSchemeFiles == null) {
				return;
			}
			AliteLog.d("ColorScheme.load", "Number of color scheme files in directory '" +
				DIRECTORY_COLOR_SCHEMES + "' found: " + colorSchemeFiles.length);
			for (int i = 0; i < colorSchemeFiles.length; i++) {
				colorSchemes.add(colorSchemeFiles[i].getName());
				if (colorSchemeFiles[i].getName().equals(schemeName)) {
					nextColorSchemeIndex = i+1;
				}
			}
		} catch (IOException ignored) { }
	}

	public static String getNextColorScheme(String schemeName) {
		if (COLOR_SCHEME_CLASSIC.equals(schemeName)) {
			return COLOR_SCHEME_MODERN;
		}
		if (nextColorSchemeIndex < colorSchemes.size()) {
			nextColorSchemeIndex++;
			return colorSchemes.get(nextColorSchemeIndex -1);
		}
		nextColorSchemeIndex = 0;
		return COLOR_SCHEME_CLASSIC;
	}

	public static int get(int index) {
		if (index < 0 || index > MAX_COLORS) {
			AliteLog.e("ColorScheme.get", "Required color " + index + " is out of bounds (0 - " + MAX_COLORS + ")");
			return AliteColor.TRANSPARENT;
		}
		return colors[index][0];
	}

	public static int get(int index, float value) {
		if (index < 0 || index > MAX_COLORS || colors[index][0] == colors[index][1]) {
			return get(index);
		}
		return getGradient(colors[index][1], colors[index][0], value);
	}

	// Color class is allowed only from API 26
	private static int getGradient(int startColor, int endColor, float mixRatio) {
		if (mixRatio <= 0) {
			return startColor;
		}
		if (mixRatio >= 1) {
			return endColor;
		}
		startColor = convertEntireColorLinearSRGBtoRGB(startColor);
		endColor = convertEntireColorLinearSRGBtoRGB(endColor);

		return AliteColor.rgb(convertLinearRGBtoSRGB(interpolate(mixRatio, AliteColor.red(startColor), AliteColor.red(endColor))),
			convertLinearRGBtoSRGB(interpolate(mixRatio, AliteColor.green(startColor), AliteColor.green(endColor))),
			convertLinearRGBtoSRGB(interpolate(mixRatio, AliteColor.blue(startColor), AliteColor.blue(endColor))));
	}

	private static int convertEntireColorLinearSRGBtoRGB(int c) {
		return AliteColor.rgb(convertSRGBtoLinearRGB(AliteColor.red(c)), convertSRGBtoLinearRGB(AliteColor.green(c)), convertSRGBtoLinearRGB(AliteColor.blue(c)));
	}

	private static int convertSRGBtoLinearRGB(int color) {
		return Math.round(color <= 10 ? color / 12.92f : 255.0f * (float)Math.pow((color / 255.0f + 0.055) / 1.055, 2.4));
	}

	private static int interpolate(float mixRatio, int start, int end) {
		return (int) (start * (1 - mixRatio) + end * mixRatio);
	}

	private static int convertLinearRGBtoSRGB(int c) {
		return c > 0 ? Math.round(255.0f * (1.055f * (float) Math.pow(c / 255.0f, 1.0 / 2.4) - 0.055f)) : 0;
	}

	private static final String[] schemeItemNameMap = {
		"conditiongreen",
		"conditionyellow",
		"conditionred",
		"background",
		"baseinformation",
		"maintext",
		"informationtext",
		"additionaltext",
		"message",
		"warningmessage",
		"fuelcircle",
		"dashedfuelcircle",
		"inhabitantinformation",
		"arrow",
		"economy",
		"government",
		"techlevel",
		"population",
		"shipdistance",
		"diameter",
		"gnp",
		"currentsystemname",
		"hyperspacesystemname",
		"legalstatus",
		"remainingfuel",
		"balance",
		"rating",
		"missionobjective",
		"equipmentdescription",
		"richindustrial",
		"averageindustrial",
		"poorindustrial",
		"mainindustrial",
		"mainagricultural",
		"richagricultural",
		"averageagricultural",
		"pooragricultural",
		"backgroundlight",
		"backgrounddark",
		"framelight",
		"framedark",
		"selectedcoloredframelight",
		"selectedcoloredframedark",
		"pulsinghighlighterdark",
		"pulsinghighlighterlight",
		"tutorialbubbledark",
		"tutorialbubblelight",
		"shiptitle",
		"textareabackground",
		"cursor",
		"price",
		"scrollingtext",
		"selectedtext",
		"creditsdescription",
		"creditsperson",
		"creditsaddition",
		"frontshield",
		"aftshield",
		"fuel",
		"cabintemperature",
		"lasertemperature",
		"altitude",
		"speed",
		"energybank1",
		"energybank2",
		"energybank3",
		"energybank4",
		"energybankwhole",
		"indicatorbar",
		"highlightcolor"
	};

}
