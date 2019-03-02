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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.model.generator.enums.Economy;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.screens.canvas.TradeScreen;

public class SystemData implements Serializable {
	private static final long serialVersionUID = 6117084866118128888L;

	private static final char REFERENCE_NORMAL = '%';
	private static final char REFERENCE_CAPITALIZE = '!';
	private static final char REFERENCE_EVALUATE = '?';
	private static final char REFERENCE_ORDERED = '-';
	private static final char REFERENCE_USE = '+';
	private static final String REFERENCE_PREFIXES = String.valueOf(REFERENCE_NORMAL) +
		REFERENCE_CAPITALIZE + REFERENCE_EVALUATE + REFERENCE_ORDERED + REFERENCE_USE;

	public static final SystemData RAXXLA_SYSTEM = createRaxxlaSystem();

	private static String[] planetNameSyllable;
	private static Map<String,String[]> descriptionMap;

	private int index; // 0-255
	private int x; // 0-255
	private int y; // 0-255
	private Government govType; // 0-7
	private Economy economy; // 0-7
	private int techLevel; // 0-15 I think (original source says 0-16, which I doubt)
				           //      The value will be increased by 1 before printing,
	                       //      so 16 is the max tech level (and 1 the least).
	private int population; // 0-255
	private int productivity; // 0-65535
	private int diameter; // 0-65535
	private int fuelPrice; // 1-27 -- 32?!?!
 	private char goatSoupSeedA;
 	private char goatSoupSeedB;
	private String name;
	private String inhabitants;
	private String inhabitantCode;
	private String description;

	private int planetTexture; // Index of the pre-generated planet texture
	private int ringsTexture;  // Index of the ring texture or 0 for no rings
	private int cloudsTexture; // Index of the cloud texture or 0 for no clouds
	private int starTexture;   // 0-22 O-M class stars with 3 different luminosity settings, and two dwarf types.
	private int dockingFee;    // Fee for automatic docking, if no docking computer is installed. 400-3300.

	private final List <SystemData> reachableSystems = new ArrayList<>();

	static void initialize(String[] syllables, String[] planetDescription) {
		planetNameSyllable = syllables;
		descriptionMap = new HashMap<>();
		for (String description : planetDescription) {
			int idIdx = description.indexOf(':');
			if (idIdx < 0) {
				AliteLog.e("Planet description initializer", "Missing id prefix in description line '" + description + "'");
				continue;
			}
			descriptionMap.put(description.substring(0, idIdx).toLowerCase(), description.substring(idIdx + 1).split("\\|", 100));
		}
	}

	private static SystemData createRaxxlaSystem() {
		SystemData result = new SystemData();
		result.index = 256;
		result.x = 12;
		result.y = 127;
		result.govType = Government.CORPORATE_STATE;
		result.economy = Economy.RICH_INDUSTRIAL;
		result.techLevel = 22;
		result.population = 4;
		result.productivity = 63568;
		result.diameter = 42000;
		result.inhabitantCode = null;
		result.inhabitants = "Friendly Green Treeards";
		result.name = "Raxxla";
		result.description = "The fabled planet Raxxla is the home of all Elite pilots in the universe. " +
			"It provides retreat and peace for them along with a portal to another universe.";
		result.fuelPrice = 1;
		result.planetTexture = 1;
		result.ringsTexture = 16;
		result.cloudsTexture = 1;
		result.starTexture = 0;
		result.dockingFee = 0;
		return result;
	}

	static SystemData createSystem(int index, SeedType seed) {
		SystemData result = new SystemData();

		result.index = index;

		result.computePosition(seed);
		result.computeGovernment(seed);
		result.computeEconomy(seed);
		result.computeTechLevel(seed);
		result.computePopulation(seed);
		result.computeProductivity(seed);
		result.computeDiameter(seed);

		// Initialize goat soup seed:
		result.goatSoupSeedA = (char) (seed.getWord(0) ^ seed.getWord(1));
		result.goatSoupSeedB = (char) (result.goatSoupSeedA ^ seed.getWord(2));

		result.inhabitantCode = InhabitantComputation.computeInhabitantCode(seed);
		result.inhabitants = InhabitantComputation.computeInhabitantString(seed);
		result.name = StringUtil.capitalize(result.generateRandomName(seed));
		result.computeDescriptionString();

		// The fuel price is fixed for a given system and must be
		// computed AFTER the planet description (because the seed
		// is twisted four times during the description generation.
		// If computed earlier, the fuel prices won't match the Amiga version).
		result.computeFuelPrice(seed);

		result.computeTextures(seed);
		result.computeDockingFee();

		return result;
	}

	private void writeObject(ObjectOutputStream out)
            throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "SystemData " + this, e);
			throw e;
		}
    }

	private void computePosition(SeedType seed) {
		x = seed.getHiByte(1);
		y = seed.getHiByte(0) * 0x007F / 0x00FF;
	}

	private void computeGovernment(SeedType seed) {
		govType = Government.values()[seed.shiftRight(1, 3) & 7];
	}

	private void computeEconomy(SeedType seed) {
		int economyValue = seed.shiftRight(0, 8) & 7;
		if (govType.ordinal() <= 1) {
			economyValue |= 2;
		}
		economy = Economy.values()[economyValue];
	}

	private void computeTechLevel(SeedType seed) {
		techLevel = (seed.shiftRight(1, 8) & 3) + (economy.ordinal() ^ 7);
		techLevel += (govType.ordinal() >> 1) + 1;
		if ((govType.ordinal() & 1) == 1) {
			techLevel++;
		}
	}

	private void computePopulation(SeedType seed) {
		population  = seed.getLoByte(0) & 0x3f;
		population += 1;
	}

	private void computeProductivity(SeedType seed) {
		productivity  = seed.getLoByte(0) & 0x3f;
		productivity += 1;
		productivity *= 0x1b;
		productivity /= 5;
	}

	private void computeDiameter(SeedType seed) {
		diameter = (seed.getWord(0) & 0x7fff) + 0x3a98; // Min diameter = 15.000 (0x3A98)
	}

	private void computeFuelPrice(SeedType seed) {
		fuelPrice = (0x001F & seed.getLoByte(0)) + 1;
	}

	private void computeTextures(SeedType seed) {
		planetTexture = (seed.getLoByte(0) + seed.getHiByte(1)) % 64;
		ringsTexture  = seed.getLoByte(1) < 128 ? seed.getLoByte(1) % 15 + 1 : 0;
		cloudsTexture = seed.getHiByte(2) % 9;
		starTexture   = (seed.getLoByte(2) + seed.getHiByte(1)) % 21 +
			(seed.getHiByte(0) > 240 ? 2 : seed.getHiByte(0) > 220 ? 1 : 0);
	}

	private void computeDockingFee() {
		dockingFee = (8 - govType.ordinal()) * 50;
	}

	private void computeDescriptionString() {
		description = replaceGoatSoupString("starter", false);
	}

	// Goat soup description string generation

	private char generateRandomNumber(boolean set) {
		char d0 = goatSoupSeedB;
		char d1 = goatSoupSeedA;
		if (set) goatSoupSeedA = d0;
		d0 += d1;
		if (set) goatSoupSeedB = d0;
		d0 &= 0xFF;
		return d0;
	}

	private void tweakSeed(SeedType seed) {
		char temp;
		temp = (char) (seed.getWord(0) + seed.getWord(1) + seed.getWord(2));
		seed.setWord(0, seed.getWord(1));
		seed.setWord(1, seed.getWord(2));
		seed.setWord(2, temp);
	}

	private String generateRandomName(SeedType nameSeed) {
		String planetName = "";
		while (planetName.isEmpty()) {
			char longNameFlag = (char) (nameSeed.getWord(0) & 64);

			char [] pair = new char[4];
			for (int i = 0; i < 4; i++) {
				pair[i] = (char) (nameSeed.shiftRight(2, 8) & 31);
				tweakSeed(nameSeed);
			}

			StringBuilder resultStringBuilder = new StringBuilder();
			for (int i = 0; i < (longNameFlag > 0 ? 4 : 3); i++) {
				// Syllables for the planet name generation algorithm; . represents non-printable characters.
				resultStringBuilder.append(planetNameSyllable[pair[i]]);
			}
			planetName = resultStringBuilder.toString().replaceAll("\\.", "");
		}
		return planetName;
	}

	private String replaceGoatSoupString(String id, boolean evaluate) {
		String[] value = descriptionMap.get(id);
		if (value == null) {
			AliteLog.e("Missing planet description", "Invalid planet description reference '" + id + "'");
			return "";
		}
		int rnd = generateRandomNumber(!evaluate && Character.isDigit(id.charAt(0))) / (255 / value.length + 1);
		return computeGoatSoup(value[rnd]);
	}

	private String replaceCommand(String id) {
		switch (id) {
			case "planet_name":
				return name.toLowerCase();
			case "random_name":
			   SeedType localSeed = new SeedType(goatSoupSeedA, goatSoupSeedB, (char) (goatSoupSeedA ^ goatSoupSeedB));
				return generateRandomName(localSeed);
		    default:   // Whoops. Wrong op code. Issue error and continue.
				System.err.println("Invalid command code '" + id + "' -- ignoring.");
				return "";
		}
	}

	private String computeGoatSoup(String source) {
		if (source == null || source.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		int index = -1;
		int b = 0;
		Map<String,char[]> seed = null;
		do {
			int commandIdx = getFirstPrefixPos(source.substring(index + 1));
			if (commandIdx >= 0) {
				index += commandIdx + 1;
				char mode = source.charAt(index);
				if (b < index) {
					builder.append(source.substring(b, index));
				}
				b = index + 1;
				index = source.indexOf("%", b);
				if (index < 0) {
					index = source.length();
					if (b - 1 < index) {
						builder.append(source.substring(b - 1, index));
					}
					break;
				}
				String referenceName = source.substring(b, index).toLowerCase();
				char[] pSeed = null;
				if (mode == REFERENCE_ORDERED) {
					if (seed == null) {
						seed = new HashMap<>();
					}
					seed.put(referenceName, new char[] { goatSoupSeedA, goatSoupSeedB });
				} else if (mode == REFERENCE_USE && seed != null) {
					pSeed = seed.get(referenceName);
					if (pSeed != null) {
						seed.put("goatSoupSeed", new char[] { goatSoupSeedA, goatSoupSeedB });
						goatSoupSeedA = pSeed[0];
						goatSoupSeedB = pSeed[1];
					}
				}
				String referenceResult = getReferenceResult(referenceName, mode);
				// restore seed
				if (pSeed != null) {
					pSeed = seed.get("goatSoupSeed");
					goatSoupSeedA = pSeed[0];
					goatSoupSeedB = pSeed[1];
				}
				if (mode == REFERENCE_EVALUATE) {
					int trueIndex = source.indexOf(":", index + 1);
					int falseIndex = source.indexOf(":", trueIndex + 1);
					int endIndex = source.indexOf(":", falseIndex + 1);
					if (trueIndex >= 0 && falseIndex >= trueIndex && endIndex >= falseIndex) {
						// comma separated values
						if (Arrays.asList(source.substring(index + 1, trueIndex).split(",")).contains(referenceResult)) {
							referenceResult = computeGoatSoup(source.substring(trueIndex + 1, falseIndex));
						} else {
							referenceResult = computeGoatSoup(source.substring(falseIndex + 1, endIndex));
						}
						index = endIndex;
					}
				}
				if (mode != REFERENCE_ORDERED) {
					builder.append(referenceResult);
				}
				b = index + 1;
			} else {
				index = source.length();
				if (b < index) {
					builder.append(source.substring(b, index));
				}
			}
		} while (index < source.length());

		return builder.toString();
	}

	private String getReferenceResult(String command, char mode) {
		int subStart = command.indexOf('(');
		int[] subFromTo;
		if (subStart >= 0) {
			subFromTo = getFromTo(command.substring(subStart + 1));
			command = command.substring(0, subStart);
		} else {
			subFromTo = new int[] {1,0};
		}
		command = descriptionMap.containsKey(command) ? replaceGoatSoupString(command,
			mode == REFERENCE_EVALUATE) : replaceCommand(command);
		if (mode == REFERENCE_CAPITALIZE) command = StringUtil.capitalize(command);

		int len = command.length();
		subFromTo[0] = subFromTo[0] <= 0 ? len + subFromTo[0] : subFromTo[0] - 1;
		subFromTo[1] = subFromTo[1] <= 0 ? len + subFromTo[1] : subFromTo[1];
		if (subFromTo[0] >= 0 && subFromTo[1] <= len && subFromTo[1] - subFromTo[0] >= 0) {
			command = command.substring(subFromTo[0], subFromTo[1]);
		}
		return command;
	}

	private int getFirstPrefixPos(String source) {
		int first = -1;
		for (int i = 0; i < REFERENCE_PREFIXES.length(); i++) {
			int p = source.indexOf(REFERENCE_PREFIXES.charAt(i));
			if (p >= 0 && (first < 0 || p < first)) first = p;
		}
		return first;
	}

	/**
	 * Formats (p) and (b,e) are allowed. p'th char or [b,e] substring.
	 * Relative to the beginning must be a positive number starting with 1,
	 * relative to the end must be 0 or negative number.
	 * 1 is the 1st char, 2 is the 2nd, and so on.
	 * 0 is the last char, -1 is the char before the last one and so on.
	 * @param command for substring
	 * @return begin and end indices for substring
	 */
	private int[] getFromTo(String command) {
		int[] fullString = {1,0};
		int subEnd = command.indexOf(')');
		if (subEnd < 0) return fullString;

		command = command.substring(0, subEnd).trim().toLowerCase();
		int subTo = command.indexOf(',');
		if (subTo < 0) {
			try {
				fullString[0] = Integer.parseInt(command);
				if (fullString[0] > 0) {
					fullString[1] = fullString[0];
				} else {
					fullString[1] = fullString[0];
					fullString[0]--;
				}
			} catch (NumberFormatException ignored) { }
			return fullString;
		}

		String index = command.substring(0, subTo).trim();
		if (!index.isEmpty()) {
			try {
				fullString[0] = Integer.parseInt(index);
			} catch (NumberFormatException ignored) { }
		}
		index = command.substring(subTo + 1).trim();
		if (!index.isEmpty()) {
			try {
				fullString[1] = Integer.parseInt(index);
			} catch (NumberFormatException ignored) { }
		}
		return fullString;
	}

	public void computeReachableSystems(SystemData [] allSystems) {
		// Computes all reachable planets. (I.e. all planets with a
		// distance up to 7.0 light years).

		for (SystemData data: allSystems) {
			int dx = x - data.x;
			int dy = y - data.y;
			int dist = (int) Math.sqrt(dx * dx + dy * dy) << 2;

			if (dist <= 70) {
				reachableSystems.add(data);
			}
		}
	}

	public int computeDistance(SystemData targetSystem) {
		int dx = x - targetSystem.x;
		int dy = y - targetSystem.y;
		return (int) Math.sqrt(dx * dx + dy * dy) << 2;
	}

	// Pretty print the system data.
	public String toString() {
		String info = String.format(Locale.getDefault(), "%10s (%3d, %3d) TL: %2d %22s %22s %32s Prod: %8s Diameter: %8d km Pop: %8s %s",
			name,
			x,
			y,
			techLevel,
			economy,
			govType,
			inhabitants,
			getGnp(),
			diameter,
			getPopulation(),
			description);

		StringBuilder systems = new StringBuilder(" [");
		for (SystemData data: reachableSystems) {
			if (data.index == index) {
				continue;
			}
			systems.append(data.name + ", ");
		}
		if (systems.length() > 2) {
			systems.deleteCharAt(systems.length() - 1);
			systems.deleteCharAt(systems.length() - 1);
		} else {
			systems.append("--");
		}
		systems.append("]");

		return info + systems;
	}

	// Getter Methods

	public Economy getEconomy() {
		return economy;
	}

	public Government getGovernment() {
		return govType;
	}

	public int getIndex() {
		return index;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getName() {
		return name;
	}

	public String getInhabitants() {
		return inhabitants;
	}

	public int getTechLevel() {
		return techLevel;
	}

	public String getGnp() {
		return TradeScreen.getOneDecimalFormatString("%d.%d MCr", productivity);
	}

	public String getPopulation() {
		return TradeScreen.getOneDecimalFormatString("%d.%d bn", population);
	}

	public String getDiameter() {
		return String.format(Locale.getDefault(), "%,d km", diameter);
	}

	public String getDescription() {
		return description;
	}

	public SystemData [] getReachableSystems() {
		return reachableSystems.toArray(new SystemData[0]);
	}

	public int getFuelPrice() {
		return fuelPrice;
	}

	public int getPlanetTexture() {
		return planetTexture;
	}

	public int getRingsTexture() {
		return ringsTexture;
	}

	public int getCloudsTexture() {
		return cloudsTexture;
	}

	public String getInhabitantCode() {
		return inhabitantCode;
	}

	public int getStarTexture() {
	    return starTexture;
	}

	public long getStationHandsDockingFee() {
		return dockingFee;
	}
}
