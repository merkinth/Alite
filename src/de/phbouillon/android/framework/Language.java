package de.phbouillon.android.framework;

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

import android.content.res.AssetFileDescriptor;
import androidx.annotation.ArrayRes;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public interface Language {
	void addDefaultResource(String path, ResourceStream stream, String resPrefix) throws IOException;
	void addDefaultResource(Integer key, String value);
	void addDefaultResource(Integer key, String[] value);
	void addLocalizedResource(String path, ResourceStream stream, String resPrefix) throws IOException;
	void addLocalizedResource(Integer key, String value);
	void addLocalizedResource(Integer key, String[] value);

	Locale getCurrentLocale();
	void setLocale(Locale locale);

	/**
	 * Executes all java scripts in the given statement param between &lt;js>&lt;/js> tags.
	 */
	String executeScript(String statement);

	/**
	 * Adds default locale and adds name of locale files from the given file list
	 * by calling addLocaleFile.
	 */
	void loadLocaleList(File[] localeFiles);

	/**
	 * Returns the name of the next locale on the list read by method {@link #loadLocaleList}
	 * Returns default locale (0. item) after the last locale name exceeded.
	 */
	Locale getNextLocale();

	String stringById(@StringRes int id, Object... formatArgs);
	String[] arrayById(@ArrayRes int id);
	String pluralsById(@PluralsRes int id, int quantity, Object... formatArgs);
	InputStream rawAssetsByFilename(String fileName) throws IOException;
	InputStream rawByFilename(String path, String fileName) throws IOException;
	AssetFileDescriptor rawDescriptorByFilename(String fileName) throws IOException;

	/**
	 * Returns list of file and sub-directory names only in the given path (but not deeper)
	 * within the currently selected locale file or null for default locale.
	 */
	String[] list(String path);
}
