package de.phbouillon.android.games.alite.model.generator;

import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.trading.TradeGood;

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

public class GalaxyGenerator {
	// Current seed of this galaxy
	private SeedType seed;

	// All 256 systems in this galaxy
	private SystemData[] system = new SystemData[256];
	private int currentGalaxy = 1;
	private String[] planetNameSyllable;
	private String[] planetDescription;

	public GalaxyGenerator() {
		planetNameSyllable = L.array(R.array.planet_name_syllable);
		planetDescription = L.array(R.array.planet_description);
	}

	GalaxyGenerator(String[] planetNameSyllable, String[] planetDescription) {
		this.planetNameSyllable = planetNameSyllable;
		this.planetDescription = planetDescription;
	}

	public void buildGalaxy(int galaxyNumber) {
		currentGalaxy = galaxyNumber;
		char[] currentSeed = getSeedOf(galaxyNumber);
		seed = new SeedType(currentSeed[0], currentSeed[1], currentSeed[2]);
		buildGalaxy();
	}

	public void rebuildGalaxy() {
		planetNameSyllable = L.array(R.array.planet_name_syllable);
		planetDescription = L.array(R.array.planet_description);
		buildGalaxy(currentGalaxy);
	}

	private char[] getSeedOf(int galaxy) {
		if (galaxy > Settings.maxGalaxies || galaxy < 1) {
			galaxy = 1;
		}
		return getBaseSeed(galaxy - 1);
	}

	private char[] getBaseSeed(int baseId) {
		// 0 - 7
		switch (baseId) {
			case 0: return new char[] {0x5A4a, 0x0248, 0xB753};
			case 1: return new char[] {0xB494, 0x0490, 0x6FA6};
			case 2: return new char[] {0x6929, 0x0821, 0xDE4D};
			case 3: return new char[] {0xD252, 0x1042, 0xBD9A};
			case 4: return new char[] {0xA5A4, 0x2084, 0x7B35};
			case 5: return new char[] {0x4B49, 0x4009, 0xF66A};
			case 6: return new char[] {0x9692, 0x8012, 0xEDD4};
			case 7: return new char[] {0x2D25, 0x0124, 0xDBA9};
		}
		return new char[] {0x5A4a, 0x0248, 0x0B753};
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

	private void buildGalaxy() {
		SystemData.initialize(planetNameSyllable, planetDescription);
		// Generate the 256 planets in this galaxy
		for (int systemCount = 0; systemCount < 256; systemCount++) {
			system[systemCount] = SystemData.createSystem(systemCount, seed);
		}
	}

	public SystemData[] getSystems() {
		return system;
	}

	public SystemData getSystem(int index) {
		return system[index];
	}

	public int getAveragePrice(TradeGood tradeGood) {
		return tradeGood.getAveragePrice(currentGalaxy);
	}

	public int findGalaxyOfPlanet(String name) {
		int oldGalaxy = currentGalaxy;

		int i = oldGalaxy;
		for (int j = 0; j < 7; j++) {
			i = i % Settings.maxGalaxies + 1;
			AliteLog.d("Analyzing Galaxy", "Analyzing Galaxy " + i);
			buildGalaxy(i);
			for (SystemData s: system) {
				if (s.getName().equalsIgnoreCase(name)) {
					buildGalaxy(oldGalaxy);
					return i;
				}
			}
		}
		buildGalaxy(oldGalaxy);
		return -1;
	}
}
