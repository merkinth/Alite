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
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class L {

	public static final String DIRECTORY_LOCALES = "locales" + File.separator;
	private static Resources res;
	private static ZipResourceFile currentLanguagePack;
	private static SparseArray<String> currentResourceBundle;
	private static SparseArray<String[]> currentResourceArrayBundle;
	public static Locale currentLocale;

	public static void setLocale(Context context, String languagePackFileName) {
		res = context.getResources();

		AliteLog.d("Loading locale", "Loading language pack '" + languagePackFileName + "'");
		Map<String,String> currentResource = new HashMap<>();
		Map<String,String[]> currentArrayResource = new HashMap<>();
		currentLanguagePack = null;
		String localeFile = Settings.DEFAULT_LOCALE_FILE;

		boolean defaultLanguage = languagePackFileName == null || languagePackFileName.isEmpty() ||
			Settings.DEFAULT_LOCALE_FILE.equals(languagePackFileName.substring(languagePackFileName.lastIndexOf(File.separator) + 1));

		if (!defaultLanguage) {
			try {
				ZipResourceFile languagePack = new ZipResourceFile(languagePackFileName);
				InputStream is = languagePack.getInputStream("values" + File.separator + "strings.xml");
				if (is == null) {
					AliteLog.e("Error during loadLocale", "values/strings.xml not found, default locale is used.");
				} else {
					NodeList strings = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).
						getDocumentElement().getChildNodes();
					if (strings != null) {
						for (int i = 0; i < strings.getLength(); i++) {
							if (strings.item(i) instanceof Element) {
								if (strings.item(i).getChildNodes().getLength() == 1) {
									currentResource.put(((Element) strings.item(i)).getAttribute("name"),
										strings.item(i).getChildNodes().item(0).getNodeValue());
								} else {
									List<String> itemList = new ArrayList<>();
									NodeList items = strings.item(i).getChildNodes();
									for (int a = 0; a < items.getLength(); a++) {
										if (items.item(a) instanceof Element) {
											itemList.add(items.item(a).getChildNodes().item(0).getNodeValue());
										}
									}
									currentArrayResource.put(((Element) strings.item(i)).getAttribute("name"),
										itemList.toArray(new String[0]));
								}
							}
						}
					}
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
		for (Field field : R.string.class.getDeclaredFields()) {
			try {
				int id = field.getInt(field);
				String value = defaultLanguage ? null : currentResource.get(field.getName());
				if (value == null) {
					if (!defaultLanguage) {
						AliteLog.e("Missing resource", "Missing string resource '" + field.getName() + "'");
					}
					currentResourceBundle.put(id, res.getString(id));
				} else {
					currentResourceBundle.put(id, value);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		currentResourceArrayBundle = new SparseArray<>();
		for (Field field : R.array.class.getDeclaredFields()) {
			try {
				int id = field.getInt(field);
				String[] value = defaultLanguage ? null : currentArrayResource.get(field.getName());
				if (value == null) {
					if (!defaultLanguage) {
						AliteLog.e("Missing resource", "Missing array resource '" + field.getName() + "'");
					}
					currentResourceArrayBundle.put(id, res.getStringArray(id));
				} else {
					currentResourceArrayBundle.put(id, value);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setCurrentLocale(String languagePackFileName) {
		Settings.localeFileName = languagePackFileName.substring(languagePackFileName.lastIndexOf(File.separator) + 1);
		int languageIdx = Settings.localeFileName.indexOf('_');
		int countryIdx = Settings.localeFileName.indexOf('_', languageIdx + 1);
		if (countryIdx < 0) {
			countryIdx = Settings.localeFileName.length();
		}
		currentLocale = new Locale(Settings.localeFileName.substring(0, languageIdx),
			Settings.localeFileName.substring(languageIdx + 1, countryIdx).toUpperCase());
	}

	public static String string(@StringRes int id, Object... formatArgs) {
		return String.format(currentLocale, currentResourceBundle.get(id), formatArgs);
	}

	public static String[] stringArray(@ArrayRes int id) {
		return currentResourceArrayBundle.get(id);
	}

	public static InputStream raw(String fileName) throws IOException {
		if (currentLanguagePack == null) {
			return res.getAssets().open(fileName);
		}
		return currentLanguagePack.getInputStream("assets" + File.separator + fileName);
	}

	public static AssetFileDescriptor rawDescriptor(String fileName) throws IOException {
		if (currentLanguagePack == null) {
			return res.getAssets().openFd(fileName);
		}
		AssetFileDescriptor fd = currentLanguagePack.getAssetFileDescriptor("assets" + File.separator + fileName);
		// if compressed file
		if (fd == null) {
			AliteLog.d("Save temp file", "Saving 'assets" + File.separator + fileName + "' to temp.");
			return saveFile(currentLanguagePack.getInputStream("assets" + File.separator + fileName));
		}
		return fd;
	}

	private static AssetFileDescriptor saveFile(InputStream is) throws IOException {
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

