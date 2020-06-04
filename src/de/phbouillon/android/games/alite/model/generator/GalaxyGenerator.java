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

import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.trading.TradeGood;

import java.util.*;

public class GalaxyGenerator {
	private static final char[][] BASE_SEEDS = {
		{0x5A4a, 0x0248, 0xB753}, {0xB494, 0x0490, 0x6FA6}, {0x6929, 0x0821, 0xDE4D}, {0xD252, 0x1042, 0xBD9A},
		{0xA5A4, 0x2084, 0x7B35}, {0x4B49, 0x4009, 0xF66A}, {0x9692, 0x8012, 0xEDD4}, {0x2D25, 0x0124, 0xDBA9},
		{0x5B5B, 0x0359, 0xB864}, {0xB5A5, 0x05A1, 0x70B7}, {0x6A3A, 0x0932, 0xDF5E}, {0xD363, 0x1153, 0xBEAB},
		{0xA6B5, 0x2195, 0x7C46}, {0x4C5A, 0x411A, 0xF77B}, {0x97A3, 0x8123, 0xEEE5}, {0x2E36, 0x0235, 0xDCBA},
		{0x5C6C, 0x046A, 0xB975}, {0xB6B6, 0x06B2, 0x71C8}, {0x6B4B, 0x0A43, 0xE06F}, {0xD474, 0x1264, 0xBFBC},
		{0xA7C6, 0x22A6, 0x7D57}, {0x4D6B, 0x422B, 0xF88C}, {0x98B4, 0x8234, 0xEFF6}, {0x2F47, 0x0346, 0xDDCB},
		{0x5D7D, 0x057B, 0xBA86}, {0xB7C7, 0x07C3, 0x72D9}, {0x6C5C, 0x0B54, 0xE180}, {0xD585, 0x1375, 0xC0CD},
		{0xA8D7, 0x23B7, 0x7E68}, {0x4E7C, 0x433C, 0xF99D}, {0x99C5, 0x8345, 0xF107}, {0x3058, 0x0457, 0xDEDC},
		{0x5E8E, 0x068C, 0xBB97}, {0xB8D8, 0x08D4, 0x73EA}, {0x6D6D, 0x0C65, 0xE291}, {0xD696, 0x1486, 0xC1DE},
		{0xA9E8, 0x24C8, 0x7F79}, {0x4F8D, 0x444D, 0xFAAE}, {0x9AD6, 0x8456, 0xF218}, {0x3169, 0x0568, 0xDFED},
		{0x5F9F, 0x079D, 0xBCA8}, {0xB9E9, 0x09E5, 0x74FB}, {0x6E7E, 0x0D76, 0xE3A2}, {0xD7A7, 0x1597, 0xC2EF},
		{0xAAF9, 0x25D9, 0x808A}, {0x509E, 0x455E, 0xFBBF}, {0x9BE7, 0x8567, 0xF329}, {0x327A, 0x0679, 0xE0FE},
		{0x60B0, 0x08AE, 0xBDB9}, {0xBAFA, 0x0AF6, 0x760C}, {0x6F8F, 0x0E87, 0xE4B3}, {0xD8B8, 0x16A8, 0xC400},
		{0xAC0A, 0x26EA, 0x819B}, {0x51AF, 0x466F, 0xFCD0}, {0x9CF8, 0x8678, 0xF43A}, {0x338B, 0x078A, 0xE20F},
		{0x61C1, 0x09BF, 0xBECA}, {0xBC0B, 0x0C07, 0x771D}, {0x70A0, 0x0F98, 0xE5C4}, {0xD9C9, 0x17B9, 0xC511},
		{0xAD1B, 0x27FB, 0x82AC}, {0x52C0, 0x4780, 0xFDE1}, {0x9E09, 0x8789, 0xF54B}, {0x349C, 0x089B, 0xE320},
		{0x62D2, 0x0AD0, 0xBFDB}, {0xBD1C, 0x0D18, 0x782E}, {0x71B1, 0x10A9, 0xE6D5}, {0xDADA, 0x18CA, 0xC622},
		{0xAE2C, 0x290C, 0x83BD}, {0x53D1, 0x4891, 0xFEF2}, {0x9F1A, 0x889A, 0xF65C}, {0x35AD, 0x09AC, 0xE431},
		{0x63E3, 0x0BE1, 0xC0EC}, {0xBE2D, 0x0E29, 0x793F}, {0x72C2, 0x11BA, 0xE7E6}, {0xDBEB, 0x19DB, 0xC733},
		{0xAF3D, 0x2A1D, 0x84CE}, {0x54E2, 0x49A2, 0x0003}, {0xA02B, 0x89AB, 0xF76D}, {0x36BE, 0x0ABD, 0xE542},
		{0x64F4, 0x0CF2, 0xC1FD}, {0xBF3E, 0x0F3A, 0x7A50}, {0x73D3, 0x12CB, 0xE8F7}, {0xDCFC, 0x1AEC, 0xC844},
		{0xB04E, 0x2B2E, 0x85DF}, {0x55F3, 0x4AB3, 0x0114}, {0xA13C, 0x8ABC, 0xF87E}, {0x37CF, 0x0BCE, 0xE653},
		{0x6605, 0x0E03, 0xC30E}, {0xC04F, 0x104B, 0x7B61}, {0x74E4, 0x13DC, 0xEA08}, {0xDE0D, 0x1BFD, 0xC955},
		{0xB15F, 0x2C3F, 0x86F0}, {0x5704, 0x4BC4, 0x0225}, {0xA24D, 0x8BCD, 0xF98F}, {0x38E0, 0x0CDF, 0xE764},
		{0x6716, 0x0F14, 0xC41F}, {0xC160, 0x115C, 0x7C72}, {0x75F5, 0x14ED, 0xEB19}, {0xDF1E, 0x1D0E, 0xCA66},
		{0xB270, 0x2D50, 0x8801}, {0x5815, 0x4CD5, 0x0336}, {0xA35E, 0x8CDE, 0xFAA0}, {0x39F1, 0x0DF0, 0xE875},
		{0x6827, 0x1025, 0xC530}, {0xC271, 0x126D, 0x7D83}, {0x7706, 0x15FE, 0xEC2A}, {0xE02F, 0x1E1F, 0xCB77},
		{0xB381, 0x2E61, 0x8912}, {0x5926, 0x4DE6, 0x0447}, {0xA46F, 0x8DEF, 0xFBB1}, {0x3B02, 0x0F01, 0xE986},
		{0xC382, 0x137E, 0x7E94}, {0x7817, 0x170F, 0xED3B}, {0xE140, 0x1F30, 0xCC88}, {0xB492, 0x2F72, 0x8A23},
		{0x5A37, 0x4EF7, 0x0558}, {0xA580, 0x8F00, 0xFCC2}, {0x3C13, 0x1012, 0xEA97}, {0xC493, 0x148F, 0x7FA5},
		{0x7928, 0x1820, 0xEE4C}, {0xE251, 0x2041, 0xCD99}, {0x3D24, 0x1123, 0xEBA8}, {0xC5A4, 0x15A0, 0x80B6},
		{0x7A39, 0x1931, 0xEF5D}, {0xE362, 0x2152, 0xCEAA}, {0x3E35, 0x1234, 0xECB9}, {0xC6B5, 0x16B1, 0x81C7},
		{0x7B4A, 0x1A42, 0xF06E}, {0xE473, 0x2263, 0xCFBB}, {0x3F46, 0x1345, 0xEDCA}, {0xC7C6, 0x17C2, 0x82D8},
		{0x7C5B, 0x1B53, 0xF17F}, {0xE584, 0x2374, 0xD0CC}, {0x4057, 0x1456, 0xEEDB}, {0xC8D7, 0x18D3, 0x83E9},
		{0x7D6C, 0x1C64, 0xF290}, {0xE695, 0x2485, 0xD1DD}, {0x4168, 0x1567, 0xEFEC}, {0xC9E8, 0x19E4, 0x84FA},
		{0x7E7D, 0x1D75, 0xF3A1}, {0xE7A6, 0x2596, 0xD2EE}, {0x4279, 0x1678, 0xF0FD}, {0xCAF9, 0x1AF5, 0x860B},
		{0x7F8E, 0x1E86, 0xF4B2}, {0xE8B7, 0x26A7, 0xD3FF}, {0x438A, 0x1789, 0xF20E}, {0xCC0A, 0x1C06, 0x871C},
		{0x809F, 0x1F97, 0xF5C3}, {0xE9C8, 0x27B8, 0xD510}, {0x449B, 0x189A, 0xF31F}, {0xCD1B, 0x1D17, 0x882D},
		{0x81B0, 0x20A8, 0xF6D4}, {0xEAD9, 0x28C9, 0xD621}, {0x45AC, 0x19AB, 0xF430}, {0xCE2C, 0x1E28, 0x893E},
		{0x82C1, 0x21B9, 0xF7E5}, {0xEBEA, 0x29DA, 0xD732}, {0x46BD, 0x1ABC, 0xF541}, {0xCF3D, 0x1F39, 0x8A4F},
		{0x83D2, 0x22CA, 0xF8F6}, {0xECFB, 0x2AEB, 0xD843}, {0x47CE, 0x1BCD, 0xF652}, {0xD04E, 0x204A, 0x8B60},
		{0x84E3, 0x23DB, 0xFA07}, {0xEE0D, 0x2BFD, 0xD955}, {0x48DF, 0x1CDE, 0xF763}, {0xD15F, 0x215B, 0x8C71},
		{0x85F4, 0x24EC, 0xFB18}, {0xEF1D, 0x2D0D, 0xDA65}, {0x49F0, 0x1DEF, 0xF874}, {0x8705, 0x25FD, 0xFC29},
		{0xF02E, 0x2E1E, 0xDB76}, {0x4B01, 0x1F00, 0xF985}, {0x8816, 0x270E, 0xFD3A}, {0xF13F, 0x2F2F, 0xDC87},
		{0x8927, 0x281F, 0xFE4B}, {0xF251, 0x3041, 0xDD99}, {0x8A38, 0x2930, 0xFF5C}, {0xF361, 0x3151, 0xDEA9},
		{0x8B49, 0x2A41, 0x006D}, {0xF472, 0x3262, 0xDFBA}, {0x8C5A, 0x2B52, 0x017E}, {0xF583, 0x3373, 0xE0CB},
		{0x8D6B, 0x2C63, 0x028F}, {0xF695, 0x3485, 0xE1DD}, {0x8E7C, 0x2D74, 0x03A0}, {0xF7A5, 0x3595, 0xE2ED},
		{0x8F8D, 0x2E85, 0x04B1}, {0xF8B6, 0x36A6, 0xE3FE}, {0x909E, 0x2F96, 0x05C2}, {0xF9C7, 0x37B7, 0xE50F},
		{0x91AF, 0x30A7, 0x06D3}, {0xFAD9, 0x38C9, 0xE621}, {0x92C0, 0x31B8, 0x07E4}, {0xFBE9, 0x39D9, 0xE731},
		{0x93D1, 0x32C9, 0x08F5}, {0xFCFA, 0x3AEA, 0xE842}, {0x94E2, 0x33DA, 0x0A06}, {0xFE0B, 0x3BFB, 0xE953},
		{0x95F3, 0x34EB, 0x0B17}, {0xFF1D, 0x3D0D, 0xEA65}, {0x002D, 0x3E1D, 0xEB75}, {0x013E, 0x3F2E, 0xEC86},
		{0x024F, 0x403F, 0xED97}, {0x0361, 0x4151, 0xEEA9}, {0x0471, 0x4261, 0xEFB9}, {0x0582, 0x4372, 0xF0CA},
		{0x0693, 0x4483, 0xF1DB}, {0x07A5, 0x4595, 0xF2ED}, {0x08B5, 0x46A5, 0xF3FD}, {0x09C6, 0x47B6, 0xF50E},
		{0x0AD7, 0x48C7, 0xF61F}, {0x0BE8, 0x49D8, 0xF731}, {0x0CF9, 0x4AE9, 0xF841}, {0x0E0A, 0x4BFA, 0xF952},
		{0x0F1B, 0x4D0B, 0xFA63}, {0x102C, 0x4E1D, 0xFB74}, {0x113D, 0x4F2D, 0xFC85}, {0x124E, 0x503E, 0xFD96},
		{0x135F, 0x514F, 0xFEA7}, {0x1471, 0x5260, 0xFFB8}, {0x1581, 0x5371, 0x00C9}, {0x1692, 0x5482, 0x01DA},
		{0x17A3, 0x5593, 0x02EB}, {0x18B4, 0x56A5, 0x03FC}, {0x19C5, 0x57B5, 0x050D}, {0x1AD6, 0x58C6, 0x061E},
		{0x1BE7, 0x59D7, 0x072F}, {0x1CF8, 0x5AE8, 0x0841}, {0x1E09, 0x5BF9, 0x0951}, {0x1F1A, 0x5D0A, 0x0A62},
		{0x202B, 0x5E1B, 0x0B73}, {0x213D, 0x5F2E, 0x0C85}, {0x224D, 0x603D, 0x0D95}, {0x235E, 0x614E, 0x0EA6},
		{0x246F, 0x625F, 0x0FB7}, {0x2582, 0x6371, 0x10C9}, {0x2691, 0x6481, 0x11D9}, {0x27A3, 0x6592, 0x12EA},
		{0x28B3, 0x66A3, 0x13FB}, {0x29C4, 0x67B5, 0x150C}, {0x2AD5, 0x68C5, 0x161D}, {0x2BE6, 0x69D6, 0x172E},
		{0x2CF7, 0x6AE7, 0x183F}
	};

	// galaxy on upper 10 bit, index is on lower 10 bit
	private static final Set<Integer> ORPHAN_PLANETS =  new HashSet<>(Arrays.asList(
		(8 << 10) + 162, (10 << 10) + 34, (10 << 10) + 102, (14 << 10) + 50, (17 << 10) + 26, (17 << 10) + 116,
		(18 << 10) + 96, (19 << 10) + 230, (20 << 10) + 37, (22 << 10) + 90, (23 << 10) + 191, (28 << 10) + 116,
		(28 << 10) + 214, (30 << 10) + 196, (31 << 10) + 121, (31 << 10) + 133, (32 << 10) + 31, (34 << 10) + 24,
		(34 << 10) + 33, (34 << 10) + 65, (37 << 10) + 67, (45 << 10) + 43, (46 << 10) + 170, (48 << 10) + 1,
		(49 << 10) + 87, (52 << 10) + 15, (56 << 10) + 110, (58 << 10) + 237, (62 << 10) + 35, (62 << 10) + 141,
		(64 << 10) + 65, (64 << 10) + 145, (65 << 10) + 118, (67 << 10) + 70, (69 << 10) + 92, (71 << 10) + 186,
		(74 << 10) + 100, (75 << 10) + 232, (76 << 10) + 180, (77 << 10) + 212, (79 << 10) + 58, (81 << 10) + 177,
		(82 << 10) + 195, (85 << 10) + 178, (87 << 10) + 176, (87 << 10) + 198, (88 << 10) + 20, (89 << 10) + 212,
		(90 << 10) + 200, (94 << 10) + 171, (95 << 10) + 66, (97 << 10) + 97, (101 << 10) + 61, (106 << 10) + 188,
		(107 << 10) + 192, (108 << 10) + 60, (109 << 10) + 237, (111 << 10) + 249, (116 << 10) + 178, (120 << 10) + 171,
		(121 << 10) + 36, (124 << 10) + 198, (126 << 10) + 233, (131 << 10) + 218, (133 << 10) + 20, (133 << 10) + 245,
		(135 << 10) + 206, (138 << 10) + 2, (138 << 10) + 251, (139 << 10) + 68, (140 << 10) + 85, (142 << 10) + 251,
		(143 << 10) + 44, (145 << 10) + 234, (149 << 10) + 206, (158 << 10) + 161, (161 << 10) + 100, (162 << 10) + 100,
		(163 << 10) + 182, (164 << 10) + 197, (167 << 10) + 59, (167 << 10) + 241, (171 << 10) + 165, (173 << 10) + 20,
		(173 << 10) + 63, (176 << 10) + 12, (176 << 10) + 97, (177 << 10) + 201, (177 << 10) + 223, (181 << 10) + 121,
		(181 << 10) + 198, (182 << 10) + 29, (184 << 10) + 161, (186 << 10) + 239, (190 << 10) + 25, (191 << 10) + 56,
		(191 << 10) + 85, (191 << 10) + 90, (194 << 10) + 80, (194 << 10) + 132, (194 << 10) + 200, (197 << 10) + 167,
		(198 << 10) + 52, (201 << 10) + 228, (204 << 10) + 203, (205 << 10) + 15, (206 << 10) + 178, (208 << 10) + 0,
		(208 << 10) + 73, (208 << 10) + 214, (211 << 10) + 188, (212 << 10) + 156, (214 << 10) + 31, (215 << 10) + 28,
		(219 << 10) + 166, (219 << 10) + 248, (220 << 10) + 156, (221 << 10) + 22, (221 << 10) + 63, (223 << 10) + 43,
		(223 << 10) + 204, (225 << 10) + 85, (226 << 10) + 38, (226 << 10) + 54, (228 << 10) + 87, (228 << 10) + 147,
		(231 << 10) + 79, (233 << 10) + 93, (236 << 10) + 232, (237 << 10) + 67, (237 << 10) + 99, (239 << 10) + 29,
		(240 << 10) + 30, (240 << 10) + 138, (242 << 10) + 3, (242 << 10) + 201, (244 << 10) + 76, (244 << 10) + 134));

	public static final int GALAXY_COUNT = 8;
	public static final int EXTENDED_GALAXY_COUNT = BASE_SEEDS.length;
	static final int PLANET_COUNT = 256;

	// All systems in this galaxy
	private final SystemData[] system = new SystemData[PLANET_COUNT];
	private int currentGalaxy = 1;
	private int highestHashedGalaxyNumber = 1;
	private final Map<String,Integer> planetNames = new HashMap<>();
	private final Map<Integer,Map<Integer,String>> duplicates = new HashMap<>();

	public void buildGalaxy(int galaxyNumber) {
		currentGalaxy = galaxyNumber;
		buildGalaxyOf(galaxyNumber);
	}

	private void buildGalaxyOf(int galaxyNumber) {
		SystemData.initialize();
		long time = System.currentTimeMillis();
		// store not yet stored planet names
		if (highestHashedGalaxyNumber <= galaxyNumber) {
			for (int i = highestHashedGalaxyNumber; i <= galaxyNumber; i++) {
				generatePlanetsOfGalaxy(i);
			}
			highestHashedGalaxyNumber = galaxyNumber + 1;
		} else {
			generatePlanetsOfGalaxy(galaxyNumber);
		}
		AliteLog.d("Galaxy generation", "Galaxy #" + galaxyNumber + " generated in " +
			(System.currentTimeMillis() - time) + " ms");
	}

	// Generate all the planets in this galaxy
	private void generatePlanetsOfGalaxy(int galaxyNumber) {
		SeedType seed = getSeedOf(galaxyNumber);
		// Create the planet only if this is the target system
		if (galaxyNumber == currentGalaxy) {
			for (int systemCount = 0; systemCount < PLANET_COUNT; systemCount++) {
				system[systemCount] = SystemData.createSystem(systemCount, seed);
			}
		}
		if (highestHashedGalaxyNumber <= galaxyNumber) {
			Map<Integer,String> duplicatesInGalaxy = new LinkedHashMap<>();
			for (int systemCount = 0; systemCount < PLANET_COUNT; systemCount++) {
				String planetName = (galaxyNumber == currentGalaxy ? system[systemCount].getName() :
					// otherwise get the planet name only
					SystemData.generateRandomName(seed)).toLowerCase();
				Integer planet = planetNames.get(planetName);
				int planetId = (galaxyNumber << 8) + systemCount;
				if (planet == null) {
					planetNames.put(planetName, planetId);
				} else if (planet != planetId) {
					duplicatesInGalaxy.put(systemCount, planetName);
				}
			}
			duplicates.put(galaxyNumber, duplicatesInGalaxy);
		}
		// then check uniqueness of planet names
		Map<Integer,String> duplicatesInGalaxy = duplicates.get(galaxyNumber);
		for (int index : duplicatesInGalaxy.keySet()) {
//			String currentPlanetName = duplicatesInGalaxy.get(index);
			Integer existsPlanet; //= planetNames.get(currentPlanetName);
			String planetName;
			do {
				planetName = SystemData.generateRandomName(seed);
				// Logging rename info are commented out for performance reasons.
				// Seriously, it gains almost 20% (the worst case, on Galaxy S6: 11,5s -> 9,5s)!
//				AliteLog.d("Planet name duplication", "#" + galaxyNumber + " (" + index + "): " +
//					currentPlanetName + " exists in galaxy #" + (existsPlanet >> 8) + ", renamed to " + planetName);
//				currentPlanetName = planetName;
				existsPlanet = planetNames.get(planetName.toLowerCase());
			} while (existsPlanet != null && existsPlanet != (galaxyNumber << 8) + index);
			if (galaxyNumber == currentGalaxy) {
				system[index].changePlanetName(planetName);
			}
			planetNames.put(planetName.toLowerCase(), (galaxyNumber << 8) + index);
		}
	}

	public void rebuildGalaxy() {
		SystemData.changeLocale();
		buildGalaxy(currentGalaxy);
	}

	SeedType getSeedOf(int galaxy) {
		if (galaxy > Settings.maxGalaxies || galaxy > EXTENDED_GALAXY_COUNT || galaxy < 1) {
			galaxy = 1;
		}
		return new SeedType(BASE_SEEDS[galaxy - 1][0], BASE_SEEDS[galaxy - 1][1], BASE_SEEDS[galaxy - 1][2]);
	}

	public int getCurrentGalaxy() {
		return currentGalaxy;
	}

	public void setCurrentGalaxy(int galaxy) {
		currentGalaxy = galaxy;
	}

	public int getNextGalaxy() {
		if (currentGalaxy >= Settings.maxGalaxies || currentGalaxy < 1) {
			return 1;
		}
		return currentGalaxy + 1;
	}

	public SystemData[] getSystems() {
		return system;
	}

	public SystemData getSystem(int index) {
		return currentGalaxy == SystemData.RAXXLA_GALAXY && index == SystemData.RAXXLA_SYSTEM_INDEX ?
			SystemData.RAXXLA_SYSTEM : system[index % PLANET_COUNT];
	}

	public boolean isOrphan(int galaxy, int index) {
		return ORPHAN_PLANETS.contains((galaxy << 10) + index);
	}

	public int getAveragePrice(TradeGood tradeGood) {
		return tradeGood.getAveragePrice(currentGalaxy);
	}

	public int findGalaxyOfPlanet(String name) {
		int i = currentGalaxy;
		try {
			for (int j = 0; j < Settings.maxGalaxies - 1; j++) {
				Integer galaxy = planetNames.get(name.toLowerCase());
				if (galaxy != null) {
					return galaxy >> 8;
				}
				i = i % (Settings.maxGalaxies + 1) + 1;
				if (i <= highestHashedGalaxyNumber) {
					continue;
				}
				AliteLog.d("Analyzing Galaxy", "Analyzing Galaxy " + i);
				buildGalaxyOf(i);
			}
		} finally {
			if (i != currentGalaxy) {
				buildGalaxyOf(currentGalaxy);
			}
		}
		return -1;
	}
}
