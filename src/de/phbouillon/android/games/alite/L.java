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
import android.util.SparseArray;
import androidx.annotation.ArrayRes;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;
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
 * <p>Concept of plug-in language resource files</p>
 * <p>---------------------------------------------</p>
 * The only real resource file is the values/strings.xml
 * any other language files are defined as a plain xml file not as a resource.
 * Although android resource support is not accessible in this way but new language files can be added as
 * a plug-in without recompiling the project which would be required since resource files are part of the APK.
 *<p></p>
 * <p>Implementation</p>
 * <p>-----------------</p>
 * Locale can be changed by calling of method setLocale.
 * One of its main task is to hash string, array and plural values from file values/strings.xml of
 * the given locale by method readLanguageResources.
 * Then values are mapped to their resource id's by examining declared fields of class R with reflexion.
 * Plurals are handled as an array in this stage where the value of each item is in form 'quantity attribute' $ 'value'.
 * If an item is missing from the language file the one in the resource file values/strings.xml will be used.
 *<p></p>
 * <p>Strings and arrays have static values they are returned by methods string and array.</p>
 *<p></p>
 * <p>Binary a.k.a raw files are handled by methods raw and rawDescriptor.
 * To access them by its descriptor has a trick: firstly, they have to be save from the zipped resource file
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
 * So method plurals can use it first to get the needed quantity item and find the underlying string formatter to use
 * or the 'other' item if that is not currently defined.
 */
public class L {

	public static final String DIRECTORY_LOCALES = "locales" + File.separator;
	private static final String DIRECTORY_ASSETS = "assets" + File.separator;

	private Resources res;
	private ZipResourceFile currentLanguagePack;
	private SparseArray<String> currentResourceBundle;
	private SparseArray<String[]> currentResourceArrayBundle;
	private static Locale currentLocale;
	private List<String> locales = new ArrayList<>();
	private int nextLocaleIndex;
	private boolean isDefaultLanguage;
	private ScriptEngine engine;
	private static L instance = new L();

	private L() {
	}

	public static L getInstance() {
		return instance;
	}

	public void loadLocaleList(FileIO f, String localeName) {
		nextLocaleIndex = 0;
		File[] localeFiles = f.getFiles(DIRECTORY_LOCALES, "(.*)\\.zip");
		locales.clear();
		locales.add(Settings.DEFAULT_LOCALE_FILE);
		// if locale directory does not exist
		if (localeFiles == null) {
			return;
		}
		AliteLog.d("loadLocaleList", "Number of locale files in directory '" +
			DIRECTORY_LOCALES + "' found: " + localeFiles.length);
		for (int i = 0; i < localeFiles.length; i++) {
			locales.add(localeFiles[i].getName());
			if (localeFiles[i].getName().equals(localeName)) {
				nextLocaleIndex = i+1;
			}
		}
	}

	public String getNextLocale() {
		nextLocaleIndex = OptionsScreen.cycleFromZeroTo(nextLocaleIndex, locales.size()-1);
		return locales.get(nextLocaleIndex);
	}

	public void setLocale(Context context, String languagePackFileName) {
		res = context.getResources();

		AliteLog.d("Loading locale", "Loading language pack '" + languagePackFileName + "'");
		Map<String,String> currentResource = new HashMap<>();
		Map<String,String[]> currentArrayResource = new HashMap<>();
		currentLanguagePack = null;
		String localeFile = Settings.DEFAULT_LOCALE_FILE;

		isDefaultLanguage = languagePackFileName == null || languagePackFileName.isEmpty() ||
			Settings.DEFAULT_LOCALE_FILE.equals(languagePackFileName.substring(languagePackFileName.lastIndexOf(File.separator) + 1));

		if (!isDefaultLanguage) {
			try {
				ZipResourceFile languagePack = readLanguageResources(languagePackFileName, currentResource, currentArrayResource);
				if (languagePack == null) {
					AliteLog.e("Error during loadLocale", "values/strings.xml not found, default locale is used.");
				} else {
					AliteLog.d("Loading locale", "Loading language pack " + languagePackFileName + " succeeded.");
					localeFile = languagePackFileName;
					currentLanguagePack = languagePack;
				}
			} catch (IOException | SAXException | ParserConfigurationException | DOMException e) {
				AliteLog.e("Error during loadLocale", "Loading language pack " +
					languagePackFileName + " failed, default locale is used.", e);
			}
		}

		setCurrentLocale(localeFile);
		currentResourceBundle = new SparseArray<>();
		collectResourceFields(R.string.class.getDeclaredFields(), currentResource, currentResourceBundle, value -> res.getString(value));
		currentResourceArrayBundle = new SparseArray<>();
		collectResourceFields(R.array.class.getDeclaredFields(), currentArrayResource, currentResourceArrayBundle, value -> res.getStringArray(value));

		currentArrayResource.put("plurals_pattern", new String[] {
			"zero$zero", "one$one", "two$two", "few$few", "many$many", "other$other" });
		collectResourceFields(R.plurals.class.getDeclaredFields(), currentArrayResource, currentResourceArrayBundle, null);
		engine = new ScriptEngineManager().getEngineByName("rhino");
		if (currentResourceBundle.get(R.string.js) != null) {
			try {
				engine.eval(currentResourceBundle.get(R.string.js));
			} catch (ScriptException e) {
				AliteLog.e("ScriptEvaluation", "Error during initialization java script", e);
			}
		}
	}

	private ZipResourceFile readLanguageResources(String languagePackFileName, Map<String, String> currentResource,
			Map<String, String[]> currentArrayResource) throws IOException, SAXException, ParserConfigurationException {
		ZipResourceFile languagePack = new ZipResourceFile(languagePackFileName);
		InputStream is = languagePack.getInputStream("values" + File.separator + "strings.xml");
		if (is == null) {
			return null;
		}
		NodeList strings = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).
			getDocumentElement().getChildNodes();
		if (strings == null) {
			return languagePack;
		}
		for (int i = 0; i < strings.getLength(); i++) {
			if (!(strings.item(i) instanceof Element)) {
				continue;
			}
			String name = ((Element) strings.item(i)).getAttribute("name");
			if (strings.item(i).getChildNodes().getLength() == 1) {
				currentResource.put(name, strings.item(i).getChildNodes().item(0).getNodeValue());
				continue;
			}
			List<String> itemList = new ArrayList<>();
			NodeList items = strings.item(i).getChildNodes();
			for (int a = 0; a < items.getLength(); a++) {
				if (!(items.item(a) instanceof Element)) {
					continue;
				}
				String quantity = ((Element) items.item(a)).getAttribute("quantity");
				itemList.add((quantity.isEmpty() ? "" : quantity + "$") + items.item(a).getChildNodes().item(0).getNodeValue());
			}
			currentArrayResource.put(name, itemList.toArray(new String[0]));
		}
		return languagePack;
	}

	public static String getOneDecimalFormatString(int resId, long value) {
		return string(resId, value / 10, value % 10);
	}

	// todo: Remove this copy of interface after min sdk will be at least 24
	@FunctionalInterface
	private interface IntFunction<R> {
		R apply(int value);
	}

	private <T> void collectResourceFields(Field[] fields, Map<String,T> current, SparseArray<T> destination, IntFunction<T> getter) {
		for (Field field : fields) {
			try {
				int id = field.getInt(field);
				T value = isDefaultLanguage && getter != null ? null : current.get(field.getName());
				if (value == null) {
					if (!isDefaultLanguage) {
						AliteLog.e("Missing resource", "Missing resource '" + field.getName() + "'");
					}
					value = getter != null ? getter.apply(id) : null;
				}
				destination.put(id, value);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				AliteLog.e("Collecting resource fields", "Error during collecting resource fields", e);
			}
		}
	}

	private void setCurrentLocale(String languagePackFileName) {
		Settings.localeFileName = languagePackFileName.substring(languagePackFileName.lastIndexOf(File.separator) + 1);
		int languageIdx = Settings.localeFileName.indexOf('_');
		int countryIdx = Settings.localeFileName.indexOf('_', languageIdx + 1);
		if (countryIdx < 0) {
			countryIdx = Settings.localeFileName.length();
		}
		currentLocale = new Locale(Settings.localeFileName.substring(0, languageIdx),
			Settings.localeFileName.substring(languageIdx + 1, countryIdx).toUpperCase());
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public static String string(@StringRes int id, Object... formatArgs) {
		return getInstance().executeScript(StringUtil.format(getInstance().currentResourceBundle.get(id), formatArgs));
	}

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

	public static String[] array(@ArrayRes int id) {
		return getInstance().currentResourceArrayBundle.get(id);
	}

	public static String plurals(@PluralsRes int id, int quantity, Object... formatArgs) {
		if (getInstance().isDefaultLanguage) {
			return getInstance().res.getQuantityString(id, quantity, formatArgs);
		}
		String quantityString = getInstance().res.getQuantityString(R.plurals.plurals_pattern, quantity);
		String[] items = getInstance().currentResourceArrayBundle.get(id);
		int otherIdx = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].startsWith(quantityString + "$")) {
				return StringUtil.format(items[i].substring(quantityString.length()+1), formatArgs);
			}
			if (items[i].startsWith("other$")) {
				otherIdx = i;
			}
		}
		return otherIdx < 0 ? "" : StringUtil.format(items[otherIdx].substring("other".length() + 1), formatArgs);
	}

	public static InputStream raw(String fileName) throws IOException {
		if (getInstance().currentLanguagePack == null) {
			return getInstance().res.getAssets().open(fileName);
		}
		return raw(DIRECTORY_ASSETS, fileName);
	}

	public static InputStream raw(String path, String fileName) throws IOException {
		InputStream inputStream = getInstance().currentLanguagePack.getInputStream(path + fileName);
		return inputStream != null ? inputStream : getInstance().res.getAssets().open(fileName);
	}

	public static AssetFileDescriptor rawDescriptor(String fileName) throws IOException {
		if (getInstance().currentLanguagePack == null) {
			return getInstance().res.getAssets().openFd(fileName);
		}
		AssetFileDescriptor fd = getInstance().currentLanguagePack.getAssetFileDescriptor(DIRECTORY_ASSETS + fileName);
		// if compressed file
		if (fd == null) {
			AliteLog.d("Save temp file", "Saving '" + DIRECTORY_ASSETS + fileName + "' to temp.");
			InputStream inputStream = getInstance().currentLanguagePack.getInputStream(DIRECTORY_ASSETS + fileName);
			return inputStream != null ? getInstance().saveFile(inputStream) : getInstance().res.getAssets().openFd(fileName);
		}
		return fd;
	}

	private AssetFileDescriptor saveFile(InputStream is) throws IOException {
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
		AssetFileDescriptor fd = new AssetFileDescriptor(ParcelFileDescriptor.open(f,
			ParcelFileDescriptor.MODE_READ_ONLY), 0, f.length());
		f.delete();
		return fd;
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

