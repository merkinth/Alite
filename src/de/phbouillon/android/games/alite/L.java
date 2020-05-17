package de.phbouillon.android.games.alite;

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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.os.ParcelFileDescriptor;
import androidx.annotation.ArrayRes;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import de.phbouillon.android.framework.IntFunction;
import de.phbouillon.android.framework.Language;
import de.phbouillon.android.framework.PluginModel;
import de.phbouillon.android.framework.ResourceStream;
import de.phbouillon.android.games.alite.oxp.OXPParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <pre>Concept of plug-in language resource files</pre>
 * The only real resource file is the values/strings.xml
 * any other language files are defined as a plain xml file not as a resource.
 * Although android resource support is not accessible in this way but new language files can be added as
 * a plug-in without recompiling the project which would be required since resource files are part of the APK.
 *<p></p>
 * <pre>Implementation</pre>
 * Locale can be changed by calling of method setLocale.
 * All previously added (see {@link #loadLocaleList} resource files of the given locale will be loaded.
 * One of its main task is to hash string, array and plural values from file values/strings.xml of
 * the given locale by method readLanguageResources.
 * Then values are mapped to their resource id's by examining declared fields of class R with reflexion.
 * Hash code is generated with negative value for each element in the additional resource files that
 * may not be in the R file. So these can be handled in the same way as real resource elements.
 * Plurals are handled as an array in this stage where the value of each item is in form 'quantity attribute' $ 'value'.
 * If an item is missing from the language file the one in the resource file values/strings.xml will be used.
 *<p></p>
 * <p>Strings and arrays have static values they are returned by methods string and array.</p>
 *<p></p>
 * <p>Binary a.k.a raw files are handled by methods raw and rawDescriptor.
 * To access them by its descriptor has a trick: firstly, they have to be saved from the zipped resource file
 * as a temporary file.</p>
 *<p></p>
 * <p>Handling plurals requires further explanation. Since the plural form that is currently being used
 * depends on the quantity involved, a dynamic definition is required instead of handling static values.
 * The form is determined by Android based on the given resource id.
 * This resource id is defined in resource file values/strings.xml with a fixed name and quantity items
 * in the following form:</p>
 * <pre>{@code
 *     <plurals name="plurals_pattern">
 *         <item quantity="zero">zero</item>
 *         <item quantity="one">one</item>
 *         <item quantity="two">two</item>
 *         <item quantity="few">few</item>
 *         <item quantity="many">many</item>
 *         <item quantity="other">other</item>
 *     </plurals>
 * }</pre>
 *
 * plurals_pattern is added artificially to the resource map of current language.
 * So method plurals can use it first to get the needed quantity item and find the underlying string formatter
 * to use or the 'other' item if that is not currently defined.
 */
public class L implements Language {
	public static final String DIRECTORY_ASSETS = "assets" + File.separatorChar;
	private static final Locale DEFAULT_LOCALE = Locale.US;

	private static Resources res;
	// "main" zip (the first which contains assets directory) of currently selected locale
	// or null for default locale
	private ZipResourceFile currentLanguagePack;
	private final Map<String,Integer> resourceBundleHash = new HashMap<>();
	private final Map<String,Integer> resourceArrayBundleHash = new HashMap<>();

	private final Map<Integer,String> defaultResourceBundle = new HashMap<>();
	private final Map<Integer,String[]> defaultResourceArrayBundle = new HashMap<>();
	private final Map<Integer,String> currentResourceBundle = new HashMap<>();
	private final Map<Integer,String[]> currentResourceArrayBundle = new HashMap<>();

	private final Map<String,String> tempResource = new HashMap<>();
	private final Map<String,String[]> tempArrayResource = new HashMap<>();

	private static Locale currentLocale;
	private final Map<Locale,List<String>> locales = new HashMap<>();
	private boolean isDefaultLanguage;
	private ScriptEngine engine;
	private static Language instance = new L();

	private L() {
		hashResourceFields(R.string.class.getDeclaredFields(), resourceBundleHash);
		hashResourceFields(R.array.class.getDeclaredFields(), resourceArrayBundleHash);
		hashResourceFields(R.plurals.class.getDeclaredFields(), resourceArrayBundleHash);
	}

	private void hashResourceFields(Field[] fields, Map<String,Integer> destHash) {
		for (Field field : fields) {
			try {
				destHash.put(field.getName(), field.getInt(field));
			} catch (IllegalAccessException | IllegalArgumentException e) {
				AliteLog.e("Resource field hashing error", "Error during hashing resource fields", e);
			}
		}
	}

	public static Language getInstance() {
		return instance;
	}

	public static Language getInstance(Context context) {
		res = context != null ? context.getResources() : null;
		return instance;
	}

	public static void setInstance(Language instance) {
		L.instance = instance;
	}

	@Override
	public void loadLocaleList(File[] localeFiles) {
		locales.clear();
		ArrayList<String> l = new ArrayList<>();
		l.add(getLanguageCountry(DEFAULT_LOCALE));
		locales.put(DEFAULT_LOCALE, l);
		// if locale directory does not exist
		if (localeFiles == null) {
			return;
		}
		AliteLog.d("loadLocaleList", "Locale file count: " + localeFiles.length);
		for (File localeFile : localeFiles) {
			addLocaleFile(getLocaleOf(localeFile.getName()), localeFile.getPath());
		}
	}

	private void addLocaleFile(Locale locale, String fileName) {
		List<String> files = locales.get(locale);
		if (files == null) {
			files = new ArrayList<>();
			locales.put(locale, files);
		}
		files.add(fileName);
	}

	public static Locale getLocaleOf(String languageCountry) {
		int languageIdx = languageCountry.indexOf('_');
		if (languageIdx < 2 || languageIdx > 8) {
			return null;
		}
		int countryIdx = languageCountry.indexOf('_', languageIdx + 1);
		if (countryIdx < 0) {
			countryIdx = languageCountry.indexOf('.');
			if (countryIdx < 0) {
				countryIdx = languageCountry.length();
			}
		}
		if (countryIdx - languageIdx < 2 || countryIdx - languageIdx > 3) {
			return null;
		}
		try {
			Locale locale = new Locale(languageCountry.substring(0, languageIdx),
				languageCountry.substring(languageIdx + 1, countryIdx).toUpperCase());
			if (!locale.getISO3Language().isEmpty() && !locale.getISO3Country().isEmpty()) {
				return locale;
			}
		} catch (MissingResourceException ignored) { }
		return null;
	}

	public static String getLanguageCountry(Locale locale) {
		return (locale.getLanguage() + '_' + locale.getCountry()).toLowerCase();

	}

	@Override
	public Locale getNextLocale() {
		boolean found = false;
		Locale first = null;
		for (Locale l : locales.keySet()) {
			if (first == null) {
				first = l;
			}
			if (l.equals(currentLocale)) {
				found = true;
			} else if (found) {
				return l;
			}
		}
		return first;
	}

	@Override
	public void setLocale(Locale locale) {
		if (locale == null) {
			return;
		}
		currentLanguagePack = null;
		isDefaultLanguage = locale.equals(DEFAULT_LOCALE);
		addDefaultResources();

		// res can be null if no context is defined, e.g. for test purposes
		if (res != null && !isDefaultLanguage) {
			List<String> languageFiles = locales.get(locale);
			if (languageFiles == null) {
				return;
			}
			try {
				for (String languageFile : languageFiles) {
					ZipResourceFile languagePack = new ZipResourceFile(languageFile);
					if (list(languagePack, DIRECTORY_ASSETS) != null) {
						currentLanguagePack = languagePack;
					}
					addResource("values", languagePack::getInputStream, "");
					String[] plugins = list(languagePack, PluginModel.DIRECTORY_PLUGINS);
					if (plugins == null) {
						continue;
					}
					for (String plugin : plugins) {
						addResource(PluginModel.DIRECTORY_PLUGINS + plugin + File.separator +
							OXPParser.DIRECTORY_CONFIG, languagePack::getInputStream, plugin);
					}
				}
				AliteLog.d("Loading locale", "Loading locale '" + locale + "' succeeded.");
			} catch (IOException e) {
				AliteLog.e("Error during load locale", "Loading locale '" + locale + "' failed.", e);
				return;
			}
		}

		currentLocale = locale;
		currentResourceBundle.clear();
		currentResourceArrayBundle.clear();
		Map<Integer,String> toJs = defaultResourceBundle;
		if (!isDefaultLanguage) {
			tempArrayResource.put("plurals_pattern", new String[] {
				"zero$zero", "one$one", "two$two", "few$few", "many$many", "other$other"});
			moveCollectedTexts(tempResource, resourceBundleHash, currentResourceBundle);
			moveCollectedTexts(tempArrayResource, resourceArrayBundleHash, currentResourceArrayBundle);
			toJs = currentResourceBundle;
		}

		engine = new ScriptEngineManager().getEngineByName("rhino");
		if (toJs.get(R.string.js) != null) {
			try {
				engine.eval(toJs.get(R.string.js));
			} catch (ScriptException e) {
				AliteLog.e("ScriptEvaluation", "Error during initialization java script", e);
			}
		}
	}

	@Override
	public void addDefaultResource(String path, ResourceStream stream, String resPrefix) throws IOException {
		addDefaultResources();
		addResource(path, stream, resPrefix);
		moveCollectedTexts(tempResource, resourceBundleHash, defaultResourceBundle);
		moveCollectedTexts(tempArrayResource, resourceArrayBundleHash, defaultResourceArrayBundle);
	}

	private void addDefaultResources() {
		if (res != null && defaultResourceBundle.isEmpty() && defaultResourceArrayBundle.isEmpty()) {
			addResourceTexts(value -> res.getString(value), resourceBundleHash, defaultResourceBundle);
			addResourceTexts(value -> res.getStringArray(value), resourceArrayBundleHash, defaultResourceArrayBundle);
		}
	}

	private <T> void addResourceTexts(IntFunction<T> sourceRes, Map<String,Integer> hash, Map<Integer,T> dest) {
		for (Integer field : hash.values()) {
			dest.put(field, sourceRes.apply(field));
		}
	}

	@Override
	public void addLocalizedResource(String path, ResourceStream stream, String resPrefix) throws IOException {
		addResource(path, stream, resPrefix);
	}

	private void addResource(String path, ResourceStream stream, String resPrefix) throws IOException {
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		try(InputStream is = stream.getStream(path + "strings.xml")) {
			if (is == null) {
				return; // File doesn't exist
			}
			readLanguageResources(is, tempResource, tempArrayResource, resPrefix.isEmpty() ? "" : resPrefix + "@");
			AliteLog.d("Locale resource", "Locale resource from " + path + " added successfully.");
		} catch (SAXException | ParserConfigurationException | DOMException e) {
			AliteLog.e("Locale resource error", "Adding locale resource from " + path + " failed.", e);
		}
	}

	private void readLanguageResources(InputStream is, Map<String, String> dest, Map<String, String[]> destArray,
			String resPrefix) throws IOException, SAXException, ParserConfigurationException {
		NodeList strings = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).
			getDocumentElement().getChildNodes();
		if (strings == null) {
			return;
		}
		for (int i = 0; i < strings.getLength(); i++) {
			if (!(strings.item(i) instanceof Element)) {
				continue;
			}
			String name = ((Element) strings.item(i)).getAttribute("name");
			if ("string".equals(strings.item(i).getNodeName())) {
				dest.put(resPrefix + name, strings.item(i).getChildNodes().item(0).getNodeValue());
				continue;
			}
			// string-array, plurals
			List<String> itemList = new ArrayList<>();
			NodeList items = strings.item(i).getChildNodes();
			for (int a = 0; a < items.getLength(); a++) {
				if (!(items.item(a) instanceof Element)) {
					continue;
				}
				String quantity = ((Element) items.item(a)).getAttribute("quantity");
				itemList.add((quantity.isEmpty() ? "" : quantity + "$") + items.item(a).getChildNodes().item(0).getNodeValue());
			}
			destArray.put(resPrefix + name, itemList.toArray(new String[0]));
		}
	}

	public static String getOneDecimalFormatString(int resId, long value) {
		return string(resId, value / 10, value % 10);
	}

	private <T> void moveCollectedTexts(Map<String,T> source, Map<String,Integer> hash, Map<Integer,T> dest) {
		for (Map.Entry<String, T> field : source.entrySet()) {
			Integer hashCode = hash.get(field.getKey());
			if (hashCode == null) {
				hashCode = - field.getKey().hashCode();
			}
			dest.put(hashCode, field.getValue());
		}
		source.clear();
	}

	@Override
	public void addDefaultResource(Integer key, String value) {
		defaultResourceBundle.put(key, value);
	}

	@Override
	public void addDefaultResource(Integer key, String[] value) {
		defaultResourceArrayBundle.put(key, value);
	}

	@Override
	public void addLocalizedResource(Integer key, String value) {
		currentResourceBundle.put(key, value);
	}

	@Override
	public void addLocalizedResource(Integer key, String[] value) {
		currentResourceArrayBundle.put(key, value);
	}

	@Override
	public Locale getCurrentLocale() {
		return currentLocale;
	}

	@Override
	public String stringById(@StringRes int id, Object... formatArgs) {
		String resString = currentResourceBundle.get(id);
		if (resString == null) {
			resString = defaultResourceBundle.get(id);
		}
		return resString == null ? null : executeScript(String.format(currentLocale, resString, formatArgs));
	}

	public static String string(@StringRes int id, Object... formatArgs) {
		return getInstance().stringById(id, formatArgs);
	}

	@Override
	public String executeScript(String statement) {
		try {
			while (true) {
				int b = statement.indexOf("<js>");
				if (b < 0) {
					return statement;
				}
				int e = statement.indexOf("</js>", b + 4);
				if (e < 0) {
					return statement;
				}
				String eval = (String) engine.eval(statement.substring(b + 4, e));
				statement = statement.replace(statement.substring(b, e + 5), eval);
			}
		} catch (ScriptException ex) {
			AliteLog.e("ScriptEvaluation", "Error during execution java script", ex);
			return statement;
		}
	}

	@Override
	public String[] arrayById(@ArrayRes int id) {
		String[] resArray = currentResourceArrayBundle.get(id);
		if (resArray == null) {
			return defaultResourceArrayBundle.get(id);
		}
		return resArray;
	}

	public static String[] array(@ArrayRes int id) {
		return getInstance().arrayById(id);
	}

	@Override
	public String pluralsById(@PluralsRes int id, int quantity, Object... formatArgs) {
		if (isDefaultLanguage) {
			return res.getQuantityString(id, quantity, formatArgs);
		}
		String quantityString = res.getQuantityString(R.plurals.plurals_pattern, quantity);
		String[] items = currentResourceArrayBundle.get(id);
		int otherIdx = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].startsWith(quantityString + "$")) {
				return String.format(currentLocale, items[i].substring(quantityString.length()+1), formatArgs);
			}
			if (items[i].startsWith("other$")) {
				otherIdx = i;
			}
		}
		return otherIdx < 0 ? "" : String.format(currentLocale, items[otherIdx].substring("other".length() + 1), formatArgs);
	}

	public static String plurals(@PluralsRes int id, int quantity, Object... formatArgs) {
		return getInstance().pluralsById(id, quantity, formatArgs);
	}

	@Override
	public String[] list(String path) {
		return list(currentLanguagePack, path);
	}

	private String[] list(ZipResourceFile languagePack, String path) {
		if (languagePack == null) {
			return null;
		}
		ZipResourceFile.ZipEntryRO[] entries = languagePack.getAllEntries();
		List<String> files = new ArrayList<>();
		int length = path.length();
		for (ZipResourceFile.ZipEntryRO entry : entries) {
			if (entry.mFileName.startsWith(path) && entry.mFileName.length() > length) {
				// files are not in the sub-directory of path
				int subDirIdx = entry.mFileName.indexOf(File.separatorChar, length);
				if (subDirIdx < 0) {
					files.add(entry.mFileName.substring(length));
				} else if (!files.contains(entry.mFileName.substring(length, subDirIdx))) {
					files.add(entry.mFileName.substring(length, subDirIdx));
				}
			}

		}
		return files.toArray(new String[0]);
	}

	@Override
	public InputStream rawAssetsByFilename(String fileName) throws IOException {
		if (currentLanguagePack == null) {
			return res.getAssets().open(fileName);
		}
		return raw(DIRECTORY_ASSETS, fileName);
	}

	public static InputStream raw(String fileName) throws IOException {
		return getInstance().rawAssetsByFilename(fileName);
	}

	@Override
	public InputStream rawByFilename(String path, String fileName) throws IOException {
		InputStream inputStream = currentLanguagePack.getInputStream(path + fileName);
		return inputStream != null ? inputStream : res.getAssets().open(fileName);
	}

	public static InputStream raw(String path, String fileName) throws IOException {
		return getInstance().rawByFilename(path, fileName);
	}

	@Override
	public AssetFileDescriptor rawDescriptorByFilename(String fileName) throws IOException {
		if (currentLanguagePack == null) {
			return res.getAssets().openFd(fileName);
		}
		AssetFileDescriptor fd = currentLanguagePack.getAssetFileDescriptor(DIRECTORY_ASSETS + fileName);
		// if compressed file
		if (fd == null) {
			AliteLog.d("Save temp file", "Saving '" + DIRECTORY_ASSETS + fileName + "' to temp.");
			InputStream inputStream = currentLanguagePack.getInputStream(DIRECTORY_ASSETS + fileName);
			return inputStream != null ? getDescriptorOfSavedFile(inputStream) : res.getAssets().openFd(fileName);
		}
		return fd;
	}

	public static AssetFileDescriptor rawDescriptor(String fileName) throws IOException {
		return getInstance().rawDescriptorByFilename(fileName);
	}

	private AssetFileDescriptor getDescriptorOfSavedFile(InputStream is) throws IOException {
		File f = saveFile(is);
		AssetFileDescriptor fd = new AssetFileDescriptor(ParcelFileDescriptor.open(f,
			ParcelFileDescriptor.MODE_READ_ONLY), 0, f.length());
		f.delete();
		return fd;
	}

	// Not locale dependent, but useful helper method to save to temporary file from stream
	static File saveFile(InputStream is) throws IOException {
		File f = File.createTempFile("alite", "");
		try(FileOutputStream fos = new FileOutputStream(f)) {
			byte[] buffer = new byte[20*1024];
			int length;
			do {
				length = is.read(buffer);
				if (length != -1) {
					fos.write(buffer, 0, length);
				}
			} while (length != -1);
		}
		return f;
	}


/*
	public static int getResId(String resName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(resName);
			return idField.getInt(idField);
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
			e.printStackTrace();
			return -1;
		}
	}
*/

}

