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
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.generator.enums.Economy;
import de.phbouillon.android.games.alite.model.generator.enums.Government;

public class SystemData implements Serializable {
	private static final long serialVersionUID = 6117084866118128888L;

	public static final SystemData RAXXLA_SYSTEM = createRaxxlaSystem();
	public static final int LAVE_SYSTEM_INDEX = 7;
	public static final int ZAONCE_SYSTEM_INDEX = 129;


	private static final String[][] DESCRIPTION_TEXT_LIST = {
		/* 0x80 */	{" fabled", " notable", " well known", " famous", " noted"},
		/* 0x81 */	{" very", " mildly", " most", " reasonably", ""},
		/* 0x82 */	{" ancient", "%97%", " great", " vast", " pink"},
		/* 0x83 */	{"%9C%%9B% plantations", " mountains", "%9A%", "%a5% forests", " oceans"},
		/* 0x84 */	{"%A6%", " mountain", " edible", " tree", " spotted"},
		/* 0x85 */	{"%9D%", "%9E%", "%86%oid", "%A4%", "%A3%"},
		/* 0x86 */	{" walking%8D%", " crab", " bat", " lobst", " %RANDOM_NAME%"},
		/* 0x87 */	{" ancient", " exceptional", " eccentric", " ingrained", "%97%"},
		/* 0x88 */	{" shyness", " silliness", " mating traditions", " loathing of%89%", " love for%89%"},
		/* 0x89 */	{" food blenders", " tourists", " poetry", " discos", "%91%"},
		/* 0x8A */	{" its%82%%83%", " the %PLANET_NAME_IAN%%84%%85%", " its inhabitants'%87%%88%", "%9F%", " its%90%%91%"},
		/* 0x8B */	{" beset", " plagued", " ravaged", " cursed", " scourged"},
		/* 0x8C */	{"%96% civil war", "%8D%%84%%85%s", "%98% disease", "%96% earthquakes", "%96% solar activity"},
		/* 0x8D */	{" killer", " deadly", " evil", " lethal", " vicious"},
		/* 0x8E */	{" Juice", " Brandy", " Water", " Brew", " Gargle Blasters"},
		/* 0x8F */	{" %RANDOM_NAME%", " %PLANET_NAME_IAN%%85%", " %PLANET_NAME_IAN% %RANDOM_NAME%", " %PLANET_NAME_IAN%%99%", "%99% %RANDOM_NAME%"},
		/* 0x90 */	{" fabulous", " exotic", " hoopy", " unusual", " exciting"},
		/* 0x91 */	{" cuisine", " night life", " casinos", " sitcoms", "%9F%"},
		/* 0x92 */	{"%PLANET_NAME%", "The planet %PLANET_NAME%", "The world %PLANET_NAME%", "This planet", "This world"},
		/* 0x93 */	{"%81%%80% for%8A%", "%81%%80% for%8A% and%8A%", "%8B% by%8C%", "%81%%80% for%8A% but is%8B% by%8C%", "%94%%95%"},
		/* 0x94 */	{" an unremarkable", " a boring", " a dull", " a tedious", " a revolting"},
		/* 0x95 */	{" planet", " world", " place", " little planet", " dump"},
		/* 0x96 */	{" frequent", " occasional", " unpredictable", " dreadful", " deadly"},
		/* 0x97 */	{" funny", " weird", " unusual", " strange", " peculiar"},
		/* 0x98 */	{" a killer", " a deadly", " an evil", " a lethal", " a vicious"},
		/* 0x99 */	{" Killer", " Deadly", " Evil", " Lethal", " Vicious"},
		/* 0x9A */	{" parking meters", " dust clouds", " icebergs", " rock formations", " volcanoes"},
		/* 0x9B */	{" Plant", " Tulip", " Banana", " Corn", " Weed"},
		/* 0x9C */	{" %RANDOM_NAME%", " %PLANET_NAME_IAN% %RANDOM_NAME%", " %PLANET_NAME_IAN%%99%", " Inhabitant", " %PLANET_NAME_IAN% %RANDOM_NAME%"},
		/* 0x9D */	{" shrew", " beast", " bison", " snake", " wolf"},
		/* 0x9E */	{" leopard", " cat", " monkey", " goat", " fish"},
		/* 0x9F */	{"%8F%%8E%", " %PLANET_NAME_IAN%%A7%%A0%", " its%A8%%A9%%A0%", "%A1%%A2%", "%8F%%8E%"},
		/* 0xA0 */	{" Meat", " Cutlet", " Steak", " Burgers", " Soup"},
		/* 0xA1 */	{" ice", " mud", " zero-G", " vacuum", " %PLANET_NAME_IAN% ultra"},
		/* 0xA2 */	{" hockey", " cricket", " karate", " polo", " tennis"},
		/* 0xA3 */	{" wasp", " moth", " grub", " ant", " %RANDOM_NAME%"},
		/* 0xA4 */	{" poet", " arts graduate", " yak", " snail", " slug"},
		/* 0xA5 */	{" dense", " lush", " rain", " bamboo", " deciduous"},
		/* 0xA6 */	{" green", " black", " yellow stripey", " pinky grey", " white"},
		/* 0xA7 */	{" Shrew", " Beast", " Bison", " Snake", " Wolf"},
		/* 0xA8 */	{" Fabulous", " Exotic", " Hoopy", " Unusual", " Exciting"},
		/* 0xA9 */	{" Leopard", " Cat", " Monkey", " Goat", " Fish"},
	};

	private static String[] planetNameSyllable;
	private static Map<String,String> descriptionMap;

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
	private String descriptionCode;

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
			descriptionMap.put(description.substring(0, idIdx), description.substring(idIdx + 1));
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
		result.inhabitants = L.string(R.string.inhabitant_friendly_green_treeards);
		result.name = L.string(R.string.raxxla_name);
		result.description = L.string(R.string.raxxla_desc);
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
		result.name = result.generateRandomName(seed);
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
		descriptionCode = "";
		description = computeGoatSoup("%92% is%93%.", true);
		if (!descriptionMap.isEmpty()) {
			String localizedDescription = descriptionMap.get(descriptionCode);
			if (localizedDescription == null) {
				AliteLog.e("Planet description error", "Missing description for code " + descriptionCode + ": " + description);
				return;
			}
			description = L.getInstance().executeScript(computeGoatSoup(localizedDescription, false));
		}
	}

	// Goat soup description string generation
	private char generateRandomNumber() {
		char d0 = goatSoupSeedB;
		char d1 = goatSoupSeedA;
		goatSoupSeedA = d0;
		d0 += d1;
		goatSoupSeedB = d0;
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
		return StringUtil.capitalize(planetName);
	}

	private String replaceCommand(String id, boolean genCode) {
		switch (id.toLowerCase(L.getInstance().getCurrentLocale())) {
			case "planet_name":
				return StringUtil.capitalize(name);
			case "planet_name_ian":
				if (genCode) {
					char lastChar = name.charAt(name.length() - 1);
					return StringUtil.capitalize((lastChar == 'a' || lastChar == 'e' || lastChar == 'i' || lastChar == 'o' || lastChar == 'u' ?
						name.substring(0, name.length() - 1) : name) + "ian");
				}
				return id;
			case "random_name":
				SeedType localSeed = new SeedType(goatSoupSeedA, goatSoupSeedB, (char) (goatSoupSeedA ^ goatSoupSeedB));
				return generateRandomName(localSeed);
			default:
				if (genCode) {
					int rnd = generateRandomNumber() / 52; // [0..255] / 52 = [0..4]
					descriptionCode += rnd;
					return computeGoatSoup(DESCRIPTION_TEXT_LIST[Integer.parseInt(id, 16) - 0x80][rnd], true);
				}
				return id;
		}
	}

	private String computeGoatSoup(String source, boolean genCode) {
		if (source == null || source.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		int index = -1;
		int b = 0;
		do {
			int commandIdx = source.indexOf('%', index + 1);
			if (commandIdx >= 0) {
				if (b < commandIdx) {
					builder.append(source.substring(b, commandIdx));
					b = commandIdx;
				}
				index = source.indexOf("%", commandIdx + 1);
				if (index < 0) {
					if (b < source.length()) {
						builder.append(source.substring(b));
					}
					break;
				}
				builder.append(replaceCommand(source.substring(b + 1, index), genCode));
				b = index + 1;
			} else {
				index = source.length();
				if (b < index) {
					builder.append(source, b, index);
				}
			}
		} while (index < source.length());

		return builder.toString();
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
		return L.getOneDecimalFormatString(R.string.planet_gnp_value, productivity);
	}

	public String getPopulation() {
		return L.getOneDecimalFormatString(R.string.planet_population_value, population);
	}

	public String getDiameter() {
		return L.string(R.string.planet_diameter_value, diameter);
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
