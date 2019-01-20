package de.phbouillon.android.games.alite.model.library;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.phbouillon.android.games.alite.L;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.phbouillon.android.games.alite.AliteLog;

public class Toc {
	public static final String DIRECTORY_LIBRARY = "library" + File.separator;

	private final TocEntry invisibleRoot;

	public static Toc read(InputStream is) {
		Toc toc = new Toc();

		try {
			Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
			root.normalize();
			getEntriesFromParent(root, toc.invisibleRoot);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return toc;
	}

	private static void getEntriesFromParent(Element parent, TocEntry parentEntry) {
		NodeList children = parent.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0, n = children.getLength(); i < n; i++) {
				if (children.item(i) instanceof Element) {
					Element e = (Element) children.item(i);
					if (e.getNodeName().equals("tocEntry")) {
						TocEntry newEntry = parseTocEntryNode(e, parentEntry);
						parentEntry.addChild(newEntry);
						getEntriesFromParent(e, newEntry);
					}
				}
			}
		}
	}

	private static String getString(Element element) {
		NodeList subList = element.getChildNodes();
        if (subList != null && subList.getLength() > 0) {
        	return subList.item(0).getNodeValue();
        }

        return null;
    }

	private static TocEntry parseTocEntryNode(Element tocEntryNode, TocEntry parent) {
		LibraryPage linkedPage = null;
		String fileName = tocEntryNode.getAttribute("file");
		try {
			linkedPage = LibraryPage.load(fileName, L.raw(DIRECTORY_LIBRARY + fileName + ".xml"));
		} catch (IOException ignored) {
			AliteLog.e("[ALITE] Toc", "Error reading library node " + fileName + ".");
		}
		String name = getString(tocEntryNode);
		name = name == null ? "" : name.replaceAll("\\s+", " ").trim();
		return new TocEntry(name, linkedPage, parent);
	}

	public TocEntry [] getEntries() {
		return invisibleRoot.getChildren();
	}

	private Toc() {
		invisibleRoot = new TocEntry("root", null, null);
	}
}
