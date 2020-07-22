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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {
	public static final Charset CHARSET = Charset.forName("UTF-8");

	private StringUtil() {
		// Prevent instantiation
	}

	public static void addSpaceAndStringToBuilder(String stringToAdd, StringBuilder builder) {
		if (stringToAdd != null && !stringToAdd.isEmpty()) {
			if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
				builder.append(" ");
			}
			builder.append(stringToAdd);
		}
	}

	public static String capitalize(String text) {
		int i = -1;
		while (true) {
			i = text.indexOf(' ', i + 1);
			if (i < 0 || i == text.length() - 1) {
				return toUpperFirstCase(text);
			}
			text = text.substring(0, i + 1) + toUpperFirstCase(text.substring(i + 1));
		}
	}

	public static String toUpperFirstCase(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		if (s.length() == 1) {
			return s;
		}
		return s.substring(0,1).toUpperCase(L.getInstance().getCurrentLocale()) + s.substring(1);
	}

	public static String format(String format, Object... args) {
		return String.format(L.getInstance().getCurrentLocale(), format, args);
	}

	public static String computeSHAString(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes(CHARSET));
			return getHexSHA(md);
		} catch (NoSuchAlgorithmException e) {
			AliteLog.e("[ALITE] computeSHAString", "No SHA-256 encryption!", e);
		}
		return "";
	}

	private static String getHexSHA(MessageDigest md) {
		byte[] mdbytes = md.digest();

		StringBuffer hexString = new StringBuffer();
		for (byte mdbyte : mdbytes) {
			hexString.append(Integer.toHexString(0xFF & mdbyte));
		}
		return hexString.toString();
	}

}
