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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.phbouillon.android.games.alite.AliteLog;

public class LibraryPage {
	private List <String> paragraphs;
	private List <ItemDescriptor> objects;
	private List <ItemDescriptor> images;
	private ItemDescriptor backgroundImage;

	private static Element getPageText(Element root) {
		return (Element) ((Element) root.getElementsByTagName("Content").item(0)).
			getElementsByTagName("Text").item(0);
	}

	private static List <String> getParagraphsFromText(Element text) {
		ArrayList <String> result = new ArrayList<>();

		NodeList children = text.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0, n = children.getLength(); i < n; i++) {
				if (children.item(i) instanceof Element) {
					Element e = (Element) children.item(i);
					if (e.getNodeName().equals("p")) {
						result.add(e.getTextContent().replaceAll("\\s+", " "));
					} else if (e.getNodeName().equals("InlineImage")) {
						result.add("[G:" + e.getAttribute("name") + "]");
					} else if (e.getNodeName().equals("table")) {
						String table = "[T:" + e.getTextContent().replaceAll("\\s+", "[s]").replaceAll("\\n", "") + ":T]\n\n";
						result.add(table);
					}
				}
			}
		}
		if (result.isEmpty()) {
			result.add(text.getTextContent().replaceAll("\\s+", " "));
		}

		return result;
	}

	private static List <ItemDescriptor> extractItems(NodeList itemNodes) {
		ArrayList <ItemDescriptor> result = new ArrayList<>();
		if (itemNodes != null && itemNodes.getLength() > 0) {
			for (int i = 0, n = itemNodes.getLength(); i < n; i++) {
				Element object = (Element) itemNodes.item(i);
				NodeList texts = object.getElementsByTagName("Text");
				String text = "";
				if (texts != null && texts.getLength() > 0) {
					List <String> desc = getParagraphsFromText((Element) texts.item(0));
					if (!desc.isEmpty()) {
						for (String d: desc) {
							text += d + "\n\n";
						}
						text = text.trim();
					}
				}
				ItemDescriptor item = new ItemDescriptor(object.getAttribute("name"), text);
				result.add(item);
			}
		}
		return result;
	}

	private static List <ItemDescriptor> getObjects(Element root) {
		NodeList objects = ((Element) root.getElementsByTagName("Content").item(0)).getElementsByTagName("Object");
		return extractItems(objects);
	}

	private static List <ItemDescriptor> getImages(Element root) {
		NodeList images = ((Element) root.getElementsByTagName("Content").item(0)).getElementsByTagName("Image");
		return extractItems(images);
	}

	private static ItemDescriptor getBackgroundImage(Element root) {
		NodeList backgroundImage = ((Element) root.getElementsByTagName("Content").item(0)).getElementsByTagName("BackgroundImage");
		List<ItemDescriptor> itemDescriptors = extractItems(backgroundImage);
		return itemDescriptors.isEmpty() ? null : itemDescriptors.get(0);
	}

	public static LibraryPage load(String fileName, InputStream is) {
		LibraryPage result = new LibraryPage();

		try {
			Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
			root.normalize();
			result.paragraphs        = getParagraphsFromText(getPageText(root));
			result.objects           = getObjects(root);
			result.images            = getImages(root);
			result.backgroundImage   = getBackgroundImage(root);
		} catch (Throwable t) {
			AliteLog.e("Error reading Library Page " + fileName, "An error occurred while parsing page " + fileName + ".", t);
		}

		return result;
	}

	public String getParagraphs() {
		if (paragraphs == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (String s: paragraphs) {
			builder.append(s);
			builder.append("\n\n");
		}
		return builder.toString();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Content:\n");
		builder.append(paragraphs == null || paragraphs.isEmpty() ? "-" : getParagraphs());
		if (objects != null && !objects.isEmpty()) {
			builder.append("Objects:\n");
			for (ItemDescriptor id: objects) {
				builder.append("  Name: " + id.getFileName() + "\n");
				builder.append("  Desc: " + id.getText() + "\n");
			}
			builder.append("\n");
		}
		if (images != null && !images.isEmpty()) {
			builder.append("Images:\n");
			for (ItemDescriptor id: images) {
				builder.append("  Name: " + id.getFileName() + "\n");
				builder.append("  Desc: " + id.getText() + "\n");
			}
			builder.append("\n");
		}
		if (backgroundImage != null) {
			builder.append("backgroundImage:\n");
			builder.append("  Name: " + backgroundImage.getFileName() + "\n\n");
		}
		return builder.toString();
	}

	public List<ItemDescriptor> getImages() {
		return images;
	}

	public ItemDescriptor getBackgroundImage() {
		return backgroundImage;
	}
}
