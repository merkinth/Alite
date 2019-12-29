package de.phbouillon.android.games.alite.oxp;

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

import java.io.*;
import java.util.*;

import android.graphics.Color;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.dd.plist.*;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.ResourceStream;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.BuildConfig;
import de.phbouillon.android.games.alite.screens.opengl.ingame.EngineExhaust;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

public class OXPParser {
	private static final String DIRECTORY_CONFIG = "Config" + File.separatorChar;
	private static final String FILE_DEMO_SHIPS = DIRECTORY_CONFIG + "demoships.plist";
	private static final String FILE_SHIP_LIBRARY = DIRECTORY_CONFIG + "shiplibrary.plist";
	private static final String FILE_SHIP_DATA_OVERRIDES = DIRECTORY_CONFIG + "shipdata-overrides.plist";
	private static final String FILE_SHIP_DATA = DIRECTORY_CONFIG + "shipdata.plist";

	private String pluginName;
	private List<OXPParser> installedPlugins;

	private ResourceStream inputStreamMethod;
	private PListParser parser;
	private boolean manifestRequired;
	private boolean isPluginFile;
	private boolean plugged = true;
	private boolean localeDependent;
	private List<String> pendingRegistration = new ArrayList<>();

	private String identifier;
	private String minVersion;
	private String name;
	private String version;
	private String category;
	private String description;
	private String author;
	private String license;
	private String maxVersion;

	public OXPParser(FileIO fileIO, String pluginName, List<OXPParser> installedPlugins) throws IOException {
		this.pluginName = pluginName;
		this.installedPlugins = installedPlugins;
		manifestRequired = pluginName.toLowerCase(Locale.getDefault()).endsWith(".oxz") ||
			pluginName.toLowerCase(Locale.getDefault()).endsWith(".zip");
		if (manifestRequired) {
			final ZipResourceFile zip = new ZipResourceFile(fileIO.getFileName(pluginName));
			ZipResourceFile.ZipEntryRO[] entries = zip.getAllEntries();
			for (ZipResourceFile.ZipEntryRO entry : entries) {
				if (DIRECTORY_CONFIG.equals(entry.mFileName)) {
					inputStreamMethod = zip::getInputStream;
					isPluginFile = true;
					break;
				}
			}
		} else {
			if (fileIO.exists(pluginName + File.separatorChar + DIRECTORY_CONFIG)) {
				inputStreamMethod = fileName -> fileIO.readFile(pluginName + File.separatorChar + fileName);
				isPluginFile = true;
			}
		}
		if (!isPluginFile) {
			return;
		}
		parser = new PListParser(inputStreamMethod);
		name = pluginName;
	}

	public OXPParser(String pluginName, ResourceStream inputStreamMethod) {
		this.pluginName = pluginName;
		this.inputStreamMethod = inputStreamMethod;
		parser = new PListParser(inputStreamMethod);
		name = pluginName;
		isPluginFile = true;
	}

	public OXPParser setLocaleDependent() {
		localeDependent = true;
		return this;
	}

	private void plug() throws IOException {
		if (!isPluginFile) {
			return;
		}
		AliteLog.d("Loading plugin", "Loading plugin " + pluginName);
		try {
			readManifestFileProperties();
		} catch (IOException e) {
			if (manifestRequired) throw e;
		}

		try {
			if (isEmpty(minVersion) || isEmpty(maxVersion)) {
				readRequiresFileProperties();
			}
		} catch (IOException ignored) {
			// allowed to be missed
		}
		checkRequiredVersion();

		try {
			readDemoShipsFileProperties();
		} catch (IOException ignored) {
			// allowed to be missed
		}
		try {
			readShipLibraryFileProperties();
		} catch (IOException ignored) {
			// allowed to be missed
		}

		try {
			readShipDataFileProperties(FILE_SHIP_DATA, false);
			if (!pendingRegistration.isEmpty()) {
				readShipDataFileProperties(FILE_SHIP_DATA, true);
			}
		} catch (IOException ignored) {
			// allowed to be missed
		}

		try {
			readShipDataFileProperties(FILE_SHIP_DATA_OVERRIDES, false);
		} catch (IOException ignored) {
			// allowed to be missed
		}
	}

	private void readShipDataFileProperties(String fileName, boolean pending) throws IOException {
		NSDictionary shipList = parser.parseFile(fileName);
		if (shipList == null) {
			return;
		}
		for (String id : shipList.keySet()) {
			if (pending && !pendingRegistration.contains(id)) {
				continue;
			}
			SpaceObject spaceObject = SpaceObjectFactory.getInstance().getTemplateObject(id);
			boolean register = spaceObject == null;
			boolean shipData = FILE_SHIP_DATA.equals(fileName);
			AliteLog.d("readShipDataFileProperties " + (pending ? "(pending) " : "") + (shipData ? "shipdata" : ""), id);
			if (register) {
				if (shipData) {
					spaceObject = new SpaceObject(id);
					setDefaultPropertyValuesOfShip(spaceObject);
				} else {
					plugged = false;
					continue;
				}
			}
			NSDictionary propertyList = (NSDictionary) shipList.get(id);
			for (String p: propertyList.keySet()) {
				if (propertyList.get(p) instanceof NSString) {
					if (shipData || !localeDependent) {
						spaceObject.setProperty(SpaceObject.Property.valueOf(p), getString(propertyList, p));
					} else {
						spaceObject.setOverrideProperty(SpaceObject.Property.valueOf(p), getString(propertyList, p));
					}
				} else if (propertyList.get(p) instanceof NSNumber) {
					if (shipData || !localeDependent) {
						spaceObject.setProperty(SpaceObject.Property.valueOf(p), getNumber(propertyList, p));
					} else {
						spaceObject.setOverrideProperty(SpaceObject.Property.valueOf(p), getNumber(propertyList, p));
					}
				} else if (propertyList.get(p) instanceof NSArray) {
					ArrayList<String> list = new ArrayList<>();
					for (NSObject items : ((NSArray) propertyList.get(p)).getArray()) {
						if (items instanceof NSString) {
							list.add(((NSString) items).getContent());
						} else if ("escort_roles".equals(p)){
							list.add("\"" + getString((NSDictionary) items, "role") +
								"\" \"" + getNumber((NSDictionary) items, "min") +
								"\" \"" + getNumber((NSDictionary) items, "max") + "\"");
						} else if ("subentities".equals(p)){
							String subEntityKey = getString((NSDictionary) items, "subentity_key");
							SpaceObject subEntity = SpaceObjectFactory.getInstance().getObjectById(subEntityKey);
							if (subEntity == null) {
								if ("flasher".equals(getString((NSDictionary) items, "type"))) {
									continue;
								}
								if (!shipData || pending) {
									AliteLog.e("Ship data reading error", "Referred sub-entity '" + subEntityKey + "' not found.");
								} else {
									pendingRegistration.add(id);
								}
								break;
							}
							NSObject position = ((NSDictionary) items).get("position");
							NSObject orientation = ((NSDictionary) items).get("orientation");
							list.add(subEntityKey + "\t" +
								(position == null ? "0.0 0.0 0.0" : arrayToString((NSArray) position)) + "\t" +
								(orientation == null ? "1 0 0 0" : arrayToString((NSArray) orientation))); // no rotation
							spaceObject.addSubEntity(list.get(list.size() - 1));
						}
					}
					if (shipData || !localeDependent) {
						spaceObject.setProperty(SpaceObject.Property.valueOf(p), list);
					} else {
						spaceObject.setOverrideProperty(SpaceObject.Property.valueOf(p), list);
					}
				}
			}
			String likeShipId = setDependentProperties(spaceObject);
			if (likeShipId != null) {
				if (!shipData || pending) {
					AliteLog.e("Ship data reading error", "Referred object '" + likeShipId + "' not found.");
				} else {
					pendingRegistration.add(id);
				}
				continue;
			}
			boolean modelDefined = spaceObject.getStringProperty(SpaceObject.Property.like_ship) != null &&
				spaceObject.getProperty(SpaceObject.Property.model) == null;
			if (shipData && !modelDefined) {
				modelDefined = isModelDefined(spaceObject);
			}

			if (register && modelDefined) {
				SpaceObjectFactory.getInstance().registerSpaceObject(spaceObject);
			}
		}
	}

	private String arrayToString(NSArray array) {
		String result = "";
		for (NSObject a : array.getArray()) {
			result += (result.isEmpty() ? "" : " ") + getNumber((NSNumber) a);
		}
		return result;
	}

	private boolean isModelDefined(SpaceObject spaceObject) throws IOException {
		InputStream is = parser.getInputStream("Models" + File.separatorChar +
			spaceObject.getStringProperty(SpaceObject.Property.model));
		try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
			float[] vertices = null;
			float[] faces = null;
			int[] indices = null;
			String textureFilename = null;
			float[] textures = null;

			int vertexCounter = 0;
			int faceCounter = 0;
			int textureCounter = 0;
			int section = -1; // 0 - vertex, 1 - faces, 2 - textures

			do {
				String line = in.readLine();
				if (line == null || "END".equals(line)) {
					break;
				}
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				if (line.startsWith("NVERTS")) {
					vertices = new float[3 * Integer.parseInt(line.substring(7))];
					continue;
				}
				if (line.startsWith("NFACES")) {
					faces = new float[3 * Integer.parseInt(line.substring(7))];
					indices = new int[3 * Integer.parseInt(line.substring(7))];
					textures = new float[6 * Integer.parseInt(line.substring(7))];
					continue;
				}
				if (line.startsWith("VERTEX")) {
					section = 0;
					continue;
				}

				if (line.startsWith("FACES")) {
					section = 1;
					continue;
				}

				if (line.startsWith("TEXTURES")) {
					section = 2;
					continue;
				}

				if (section == 0) { // vertex
					if (vertices == null) {
							AliteLog.e("Model reading error", "Missing NVERTS line");
							break;
					}
					if (vertices.length <= vertexCounter + 2) {
						AliteLog.e("Model reading error", "Max. vertices (" + vertices.length + ") exceeded");
						break;
					}
					String[] v = line.split(",");
					vertices[vertexCounter] = Float.parseFloat(v[0].trim());
					vertices[vertexCounter+1] = Float.parseFloat(v[1].trim());
					vertices[vertexCounter+2] = Float.parseFloat(v[2].trim());
					vertexCounter += 3;
					continue;
				}

				if (section == 1) { // faces
					if (faces == null) {
						AliteLog.e("Model reading error", "Missing NFACES line");
						break;
					}
					if (faces.length <= faceCounter + 2) {
						AliteLog.e("Model reading error", "Max. faces (" + faces.length + ") exceeded");
						break;
					}
					String[] v = line.split(",");
					faces[faceCounter] = Float.parseFloat(v[3].trim());
					faces[faceCounter+1] = Float.parseFloat(v[4].trim());
					faces[faceCounter+2] = Float.parseFloat(v[5].trim());

					indices[faceCounter] = Integer.parseInt(v[7].trim());
					indices[faceCounter+1] = Integer.parseInt(v[8].trim());
					indices[faceCounter+2] = Integer.parseInt(v[9].trim());

					faceCounter += 3;
					continue;
				}

				if (section == 2) { // textures
					if (textures == null) {
						AliteLog.e("Model reading error", "Missing NFACES line");
						break;
					}
					if (textures.length <= textureCounter + 5) {
						AliteLog.e("Model reading error", "Max. textures (" + textures.length + ") exceeded");
						break;
					}
					String[] v = line.split("\\s");
					textureFilename = v[0];
					textures[textureCounter] = Float.parseFloat(v[3].trim());
					textures[textureCounter+1] = Float.parseFloat(v[4].trim());
					textures[textureCounter+2] = Float.parseFloat(v[5].trim());
					textures[textureCounter+3] = Float.parseFloat(v[6].trim());
					textures[textureCounter+4] = Float.parseFloat(v[7].trim());
					textures[textureCounter+5] = Float.parseFloat(v[8].trim());
					textureCounter += 6;
				}
			} while (true);
			if (vertices != null && faces != null && indices != null) {
				spaceObject.createFaces(vertices, faces, indices);
				spaceObject.setTexture("Textures" + File.separatorChar + textureFilename, textures, inputStreamMethod);
				return true;
			}
		}
		return false;
	}

	private void setDefaultPropertyValuesOfShip(SpaceObject spaceObject) {
		spaceObject.setProperty(SpaceObject.Property.missiles, 2L);
		spaceObject.setProperty(SpaceObject.Property.missile_load_time, 4.0d); // sec
		spaceObject.setProperty(SpaceObject.Property.laser_color, "orangeColor");
		spaceObject.setProperty(SpaceObject.Property.roles, "");
		spaceObject.setProperty(SpaceObject.Property.model_scale_factor, 1.0d);
		spaceObject.setProperty(SpaceObject.Property.max_energy, 1L);
		spaceObject.setProperty(SpaceObject.Property.cargo_carried, 0L);
		spaceObject.setProperty(SpaceObject.Property.max_cargo, 0L);
		spaceObject.setProperty(SpaceObject.Property.likely_cargo, 0L);
		spaceObject.setProperty(SpaceObject.Property.affected_by_energy_bomb, 1.0d);
		spaceObject.setProperty(SpaceObject.Property.proximity_warning, 1L);
		spaceObject.setProperty(SpaceObject.Property.aggression_level, 10L);
	}

	private String setDependentProperties(SpaceObject spaceObject) {
		String likeShipId = spaceObject.getStringProperty(SpaceObject.Property.like_ship);
		if (likeShipId != null) {
			SpaceObject likeShip = SpaceObjectFactory.getInstance().getObjectById(likeShipId);
			if (likeShip == null) {
				return likeShipId;
			}
			likeShip.copyPropertiesAndModelData(spaceObject);
		}
		if (spaceObject.getProperty(SpaceObject.Property.max_missiles) == null) {
			spaceObject.setProperty(SpaceObject.Property.max_missiles,
				spaceObject.getNumericProperty(SpaceObject.Property.missiles));
		}

		// escort_role is used instead of escort_ship
		if (spaceObject.getProperty(SpaceObject.Property.escort_ship) != null && spaceObject.getProperty(SpaceObject.Property.escort_role) == null) {
			spaceObject.setProperty(SpaceObject.Property.escort_role, "[" + spaceObject.getStringProperty(SpaceObject.Property.escort_ship) + "]");
		}

		if (spaceObject.getProperty(SpaceObject.Property.exhaust) != null) {
			List<EngineExhaust> exhausts = spaceObject.getExhausts();
			exhausts.clear();
			for (String exhaust: spaceObject.getArrayProperty(SpaceObject.Property.exhaust)) {
				String[] p = exhaust.split(" ");
				exhausts.add(new EngineExhaust(Float.parseFloat(p[3]), Float.parseFloat(p[4]), 10 * Float.parseFloat(p[5]),
					Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2])));
			}
			for (int i = 0; i < exhausts.size(); i++) {
				EngineExhaust e = exhausts.get(i);
				if (spaceObject.getProperty(SpaceObject.Property.exhaust_emissive_color) == null) {
					e.setColor(0.7f, 0.8f, 0.8f, 0.7f);
				} else {
					int color = spaceObject.getColor(spaceObject.getArrayProperty(SpaceObject.Property.exhaust_emissive_color).get(i));
					e.setColor(Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f);
				}
			}
		}

		if (spaceObject.getProperty(SpaceObject.Property.weapon_position_forward) != null) {
			List<Vector3f> laserHardpoint = spaceObject.getLaserHardpoint();
			laserHardpoint.clear();
			if (spaceObject.getProperty(SpaceObject.Property.weapon_position_forward) instanceof String) {
				String[] p = spaceObject.getStringProperty(SpaceObject.Property.weapon_position_forward).split(" ");
				laserHardpoint.add(new Vector3f(Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2])));
			} else {
				for (String hardpoint: spaceObject.getArrayProperty(SpaceObject.Property.weapon_position_forward)) {
					String[] p = hardpoint.split(" ");
					laserHardpoint.add(new Vector3f(Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2])));
				}
			}
		}
		return null;
	}

	private void readShipLibraryFileProperties() throws IOException {
		final String fileName = FILE_SHIP_LIBRARY;
		NSArray shipLibrary = parser.parseArrayFile(fileName);
		if (shipLibrary == null) {
			return;
		}
		for (NSObject id : shipLibrary.getArray()) {
			String objectClass = getString((NSDictionary) id, "class");
			if (getBoolean((NSDictionary) id, "ship_data",
				objectClass.isEmpty() || "ship".equals(objectClass.toLowerCase()))) {
				SpaceObjectFactory.getInstance().addToDemoList(getRequiredString((NSDictionary) id, "ship", fileName));
			}
		}
	}

	private void readDemoShipsFileProperties() throws IOException {
		NSArray demoShips = parser.parseArrayFile(FILE_DEMO_SHIPS);
		if (demoShips == null) {
			return;
		}
		for (NSObject id : demoShips.getArray()) {
			SpaceObjectFactory.getInstance().addToDemoList(((NSString) id).getContent());
		}
	}

	private void checkRequiredVersion() throws IOException {
		if (compareCurrentVersion(minVersion) < 0) {
			AliteLog.d("Plugin version check", "Program version (" + BuildConfig.VERSION_NAME +
				") is outdated, " + pluginName + " requires at least version " + minVersion + ".");
			throw new IOException("Plugin version check failed.");
		}
		if (compareCurrentVersion(maxVersion) > 0) {
			AliteLog.d("Plugin version check", "Program version (" + BuildConfig.VERSION_NAME +
				") is high, " + pluginName + " is limited to version " + maxVersion + ".");
			throw new IOException("Plugin version check failed.");
		}
	}

	private int compareCurrentVersion(String checkedVersion) {
		return compareVersions(checkedVersion, BuildConfig.VERSION_NAME);
	}

	private int compareVersions(String requiredVersion, String currentVersion) {
		if (isEmpty(requiredVersion)) {
			return 0;
		}
		int[] currentVersionParts = splitVersion(currentVersion);
		int[] requiredVersionParts = splitVersion(requiredVersion);
		for (int i = 0; i < 3; i++) {
			int c = Integer.compare(currentVersionParts[i], requiredVersionParts[i]);
			if (c != 0) {
				return c;
			}
		}
		return 0;
	}

	private int[] splitVersion(String versionName) {
		int b = versionName.indexOf('.');
		if (b < 0) {
			return new int[] { Integer.parseInt(versionName), 0, 0 };
		}
		int major = Integer.parseInt(versionName.substring(0, b));

		int e = versionName.indexOf('.', b + 1);
		if (e < 0) {
			return new int[] { major, Integer.parseInt(versionName.substring(b + 1)), 0 };
		}
		int minor = Integer.parseInt(versionName.substring(b + 1, e));

		return new int[] { major, minor, Integer.parseInt(versionName.substring(e + 1)) };
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private void readRequiresFileProperties() throws IOException {
		final String fileName = "requires.plist";
		NSDictionary requires = parser.parseFile(fileName);
		if (requires == null) {
			return;
		}
		if (isEmpty(minVersion)) {
			minVersion = getRequiredString(requires, "version", fileName);
		}
		if (isEmpty(maxVersion)) {
			maxVersion = getString(requires, "max_version");
		}
	}

	private void readManifestFileProperties() throws IOException {
		final String fileName = "manifest.plist";
		NSDictionary manifest = parser.parseFile(fileName);
		if (manifest == null) {
			return;
		}
		identifier  = getRequiredString(manifest, "identifier", fileName);
		name        = getRequiredString(manifest, "title", fileName);
		version     = getRequiredString(manifest, "version", fileName);
		category    = getString(manifest, "category");
		description = getString(manifest, "description");
		author      = getString(manifest, "author");
		license     = getString(manifest, "license");
		minVersion  = getString(manifest, "required_oolite_version");
		maxVersion  = getString(manifest, "maximum_oolite_version");

		NSArray conflictOxps = (NSArray) manifest.get("conflict_oxps");
		if (conflictOxps != null) {
			for (NSDictionary conflictOxp : (NSDictionary[]) conflictOxps.getArray()) {
				OXPParser installed = getInstalledPlugin(getRequiredString(conflictOxp, "identifier", fileName));
				String minVersion = getRequiredString(conflictOxp, "version", fileName);
				String maxVersion = getString(conflictOxp, "maximum_version");
				if (installed != null &&
						"0".equals(minVersion) || compareVersions(minVersion, installed.getVersion()) <= 0 &&
						(maxVersion.isEmpty() || compareVersions(maxVersion, installed.getVersion()) <= 0)) {
					throw new IOException("Conflict with plugin " + installed.getIdentifier() + " v" + installed.getVersion());
				}
			}
		}

		NSArray requiresOxps = (NSArray) manifest.get("requires_oxps");
		if (requiresOxps != null) {
			for (NSDictionary requireOxps : (NSDictionary[]) requiresOxps.getArray()) {
				OXPParser installed = getInstalledPlugin(getRequiredString(requireOxps, "identifier", fileName));
				String minVersion = getRequiredString(requireOxps, "version", fileName);
				String maxVersion = getString(requireOxps, "maximum_version");
				if (installed == null ||
					!"0".equals(minVersion) && compareVersions(minVersion, installed.getVersion()) <= 0 &&
						(maxVersion.isEmpty() || compareVersions(maxVersion, installed.getVersion()) <= 0)) {
					plugged = false;
					return;
				}

			}
		}
	}

	private OXPParser getInstalledPlugin(String id) {
		for (OXPParser plugin : installedPlugins) {
			if (id.equals(plugin.getIdentifier())) {
				return plugin;
			}
		}
		return null;
	}

	public boolean isPluginFile() {
		return isPluginFile;
	}

	public boolean isPlugged() throws IOException {
		plug();
		return plugged;
	}

	public String getPluginName() {
		return pluginName.substring(pluginName.lastIndexOf(File.separatorChar) + 1);
	}

	private String getRequiredString(NSDictionary dict, String property, String fileName) throws IOException {
		NSObject value = dict == null ? null : dict.get(property);
		if (value == null) {
			AliteLog.e(fileName + " error", "Missing property '" + property + "'.");
			throw new IOException("Missing property '" + property + "' in file '" + fileName + "'.");
		}
		return ((NSString) value).getContent();
	}

	private String getString(NSDictionary dict, String property) {
		NSObject value = dict == null ? null : dict.get(property);
		if (value == null) {
			return "";
		}
		return ((NSString) value).getContent();
	}

	private Object getNumber(NSDictionary dict, String property) {
		return getNumber(dict == null ? null : (NSNumber) dict.get(property));
	}

	private Object getNumber(NSNumber value) {
		if (value == null) {
			return "";
		}
		switch (value.type()) {
			case NSNumber.INTEGER: {
				return value.longValue();
			}
			case NSNumber.REAL: {
				return value.doubleValue();
			}
			case NSNumber.BOOLEAN: {
				return value.boolValue();
			}
		}
		return null;
	}

	private boolean getBoolean(NSDictionary dict, String property, boolean def) {
		NSObject value = dict == null ? null :  dict.get(property);
		return value == null ? def : ((NSNumber) value).boolValue();
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getAuthor() {
		return author;
	}

	public String getLicense() {
		return license;
	}
}
