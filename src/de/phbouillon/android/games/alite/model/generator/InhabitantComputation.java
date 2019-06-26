package de.phbouillon.android.games.alite.model.generator;

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

import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;

class InhabitantComputation {

	private static String getDescription(int index) {
		switch (index) {
			case 0: return L.string(R.string.inhabitant_desc_large);
			case 1: return L.string(R.string.inhabitant_desc_fierce);
			case 2: return L.string(R.string.inhabitant_desc_small);
		}
		return "";
	}

	private static String getColor(int index) {
		switch (index) {
			case 0: return L.string(R.string.inhabitant_color_green);
			case 1: return L.string(R.string.inhabitant_color_red);
			case 2: return L.string(R.string.inhabitant_color_yellow);
			case 3: return L.string(R.string.inhabitant_color_blue);
			case 4: return L.string(R.string.inhabitant_color_white);
			case 5: return L.string(R.string.inhabitant_color_harmless);
		}
		return "";
	}

	private static String getAppearance(int index) {
		switch (index) {
			case 0: return L.string(R.string.inhabitant_appearance_slimy);
			case 1: return L.string(R.string.inhabitant_appearance_bug_eyed);
			case 2: return L.string(R.string.inhabitant_appearance_horned);
			case 3: return L.string(R.string.inhabitant_appearance_bony);
			case 4: return L.string(R.string.inhabitant_appearance_fat);
			case 5: return L.string(R.string.inhabitant_appearance_furry);
			case 6: return L.string(R.string.inhabitant_appearance_mutant);
			case 7: return L.string(R.string.inhabitant_appearance_weird);
		}
		return "";
	}

	private static String getType(int index) {
		switch (index) {
			case 0: return L.string(R.string.inhabitant_type_rodent);
			case 1: return L.string(R.string.inhabitant_type_frog);
			case 2: return L.string(R.string.inhabitant_type_lizard);
			case 3: return L.string(R.string.inhabitant_type_lobster);
			case 4: return L.string(R.string.inhabitant_type_bird);
			case 5: return L.string(R.string.inhabitant_type_humanoid);
			case 6: return L.string(R.string.inhabitant_type_feline);
			case 7: return L.string(R.string.inhabitant_type_insect);
		}
		return "";
	}

	private static String computeHumanColonial(SeedType seed) {
		// This generates unique binary representations for all Human Colonials
		// in the 8 "official" galaxies, _except_ for exactly two planets who
		// have the exact same human inhabitant code... The plan was to create
		// a mission including these two planets.
		String inhabitantCode = Integer.toBinaryString(-seed.getWord(0) * 3 - seed.getWord(1) * 5 + seed.getWord(2) * 7);
		if (inhabitantCode.length() > 20) {
			inhabitantCode = inhabitantCode.substring(inhabitantCode.length() - 20);
		}
		while (inhabitantCode.length() < 20) {
			inhabitantCode = "0" + inhabitantCode;
		}
		inhabitantCode = "0000" + inhabitantCode + new StringBuilder(inhabitantCode.substring(11, 19)).reverse();
		if (seed.getLoByte(2) < 4) {
			inhabitantCode = inhabitantCode.substring(0, 31) + "1";
		}
		return inhabitantCode;
	}

	static String computeInhabitantCode(SeedType seed) {
		if (seed.getLoByte(2) < 128) {
			return computeHumanColonial(seed);
		}
		return null;
	}

	static String computeInhabitantString(SeedType seed) {
		if (seed.getLoByte(2) < 128) {
			return L.string(R.string.inhabitant_human_colonial);
		}
		StringBuilder inhabitantName = new StringBuilder();
		int descriptionFlag = seed.getHiByte(2) >> 2;
		int colorFlag = descriptionFlag;
		descriptionFlag &= 7;
		inhabitantName.append(getDescription(descriptionFlag));

		colorFlag >>= 3;
		colorFlag &= 7;
		StringUtil.addSpaceAndStringToBuilder(getColor(colorFlag), inhabitantName);

		int appearanceFlag = seed.getHiByte(0) ^ seed.getHiByte(1);
		int temp = appearanceFlag;
		appearanceFlag &= 7;
		StringUtil.addSpaceAndStringToBuilder(getAppearance(appearanceFlag), inhabitantName);

		int typeFlag = ((seed.getHiByte(2) & 3) + temp) & 7;
		StringUtil.addSpaceAndStringToBuilder(getType(typeFlag), inhabitantName);

		return StringUtil.toUpperFirstCase(inhabitantName.toString());
	}
}
