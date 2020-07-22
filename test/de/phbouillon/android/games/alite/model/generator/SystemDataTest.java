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
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.TestLogger;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Locale;

public class SystemDataTest {
	private final GalaxyGenerator generator = new GalaxyGenerator();

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage:\n  SystemDataTest <path of desired non-US locale or empty> " +
				"<locale name (in form of lang_ctry)> <output file name with path>");
			return;
		}
		AliteLog.setInstance(new TestLogger());
		L.getInstance().addDefaultResource(new File("res\\values").getAbsolutePath(), FileInputStream::new, "");
		Locale locale = L.getLocaleOf(args[1]);
		if (!locale.equals(Locale.US)) {
			L.getInstance().addLocalizedResource(new File(args[0]).getAbsolutePath(), FileInputStream::new, "");
		}
		L.getInstance().setLocale(locale);

		Settings.maxGalaxies = GalaxyGenerator.EXTENDED_GALAXY_COUNT;
		SystemDataTest test = new SystemDataTest();
		test.buildGalaxies(args[2]);
	}

	private void buildGalaxies(String outputFile) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile));
		out.write("Galaxy\tIndex\tName\tx\ty\tTech level\tEconomy\tGovernment\tInhabitants\tGnp\tDiameter" +
			"\tPopulation\tDescription code\tDescription\tRoutes\n");
		long time = System.currentTimeMillis();
		for (int g = 1; g <= Settings.maxGalaxies; g++) {
			generator.buildGalaxy(g);
			SystemData[] systems = generator.getSystems();
			for (SystemData system : systems) {
				system.computeReachableSystems(systems);
				out.write(g + "\t" + formatSystemInfo(system));
			}
		}
		out.close();
		System.out.println("Total time of generation: " + (System.currentTimeMillis() - time) + " ms");
	}

	private String formatSystemInfo(SystemData system) {
		return system.getIndex() + "\t" +
			system.getName() + "\t" +
			system.getX() + "\t" +
			system.getY() + "\t" +
			system.getTechLevel() + "\t" +
			system.getEconomy().getDescription() + "\t" +
			system.getGovernment().getDescription() + "\t" +
			system.getInhabitants() + "\t" +
			system.getGnp() + "\t" +
			system.getDiameter() + "\t" +
			system.getPopulation() + "\t" +
			system.descriptionCode + "\t" +
			system.getDescription() + "\t" +
			(system.getReachableSystems().length - 1) + "\n";
	}

	@Test
	public void findPlanetTest() throws IOException {
		AliteLog.setInstance(new TestLogger());
		L.getInstance().addDefaultResource(new File("res\\values").getAbsolutePath(), FileInputStream::new, "");
		L.getInstance().setLocale(Locale.US);

		Settings.maxGalaxies = GalaxyGenerator.EXTENDED_GALAXY_COUNT;
		generator.buildGalaxy(8);
		Assert.assertEquals(6, generator.findGalaxyOfPlanet("Gearge"));
		Assert.assertEquals(244, generator.findGalaxyOfPlanet("estia"));
		Assert.assertEquals(245, generator.findGalaxyOfPlanet("ususaon"));
	}
}
