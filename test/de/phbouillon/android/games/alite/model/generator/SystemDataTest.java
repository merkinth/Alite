package de.phbouillon.android.games.alite.model.generator;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SystemDataTest {
	private List<String> planetNameSyllable = new ArrayList<>();
	private List<String> planetDescription = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage:\n  SystemDataTest <strings.xml path> <output file name with path>");
			return;
		}
		SystemDataTest test = new SystemDataTest();
		test.readResources(args[0] + (args[0].isEmpty() || args[0].charAt(args[0].length()-1) == File.separatorChar ?
			"" : File.separatorChar) + "strings.xml");
		if (test.planetNameSyllable.size() != 32) {
			System.out.println("planet_name_syllable is not defined or corrupted, program halted.");
			System.exit(1);
		}
		test.buildGalaxies(args[1]);
	}

	private void readResources(String resourceName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(resourceName)));
		List<String> currentData = null;
		String line = in.readLine();
		while (line != null) {
			if (line.contains("planet_name_syllable")) currentData = planetNameSyllable;
			else if (line.contains("planet_description")) currentData = planetDescription;
			else if (currentData != null) {
				if (line.contains("</string-array>")){ currentData = null;}
				else currentData.add(line.substring(line.indexOf("<item>") + 6, line.indexOf("</item>")).replace("\\", ""));
			}
			line = in.readLine();
		}
		in.close();
	}

	private void buildGalaxies(String outputFile) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile));
		GalaxyGenerator generator = new GalaxyGenerator(planetNameSyllable.toArray(new String[0]), planetDescription.toArray(new String[0]));
		out.write("Galaxy\tName\tx\ty\tTech level\tEconomy\tGovernment\tInhabitants\tGnp\tDiameter\tPopulation\tDescription\n");
		for (int g=1;g<=8; g++) {
			generator.buildGalaxy(g);
			SystemData[] systems = generator.getSystems();
			for (SystemData system : systems) {
				out.write(g + "\t" + formatSystemInfo(system));
			}
		}
		out.close();
	}

	private String formatSystemInfo(SystemData system) {
		return system.getName() + "\t" +
			system.getX() + "\t" +
			system.getY() + "\t" +
			system.getTechLevel() + "\t" +
			system.getEconomy().getDescription() + "\t" +
			system.getGovernment().getDescription() + "\t" +
			system.getInhabitants() + "\t" +
			system.getGnp() + "\t" +
			system.getDiameter() + "\t" +
			system.getPopulation() + "\t" +
			system.getDescription() + "\n";
	}

}
