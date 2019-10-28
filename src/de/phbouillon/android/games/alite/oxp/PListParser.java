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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import com.dd.plist.*;
import de.phbouillon.android.framework.ResourceStream;
import org.xml.sax.SAXException;

import de.phbouillon.android.games.alite.AliteLog;

class PListParser {
	private ResourceStream stream;

	PListParser(ResourceStream stream) {
		this.stream = stream;
	}

	NSDictionary parseFile(String fileName) throws IOException {
		return (NSDictionary) parseFileInternal(fileName);
	}

	InputStream getInputStream(String fileName) throws IOException {
		return stream.getStream(fileName);
	}

	private NSObject parseFileInternal(String fileName) throws IOException {
		try {
			InputStream is = stream.getStream(fileName);
			NSObject dictionary = PropertyListParser.parse(is);
			is.close();
			return dictionary;
		} catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
			AliteLog.e("Error reading PList", e.getMessage(), e);
		}
		return null;
	}

	NSArray parseArrayFile(String fileName) throws IOException {
		return (NSArray) parseFileInternal(fileName);
	}

}
