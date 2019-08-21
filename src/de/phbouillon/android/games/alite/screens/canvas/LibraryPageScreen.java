package de.phbouillon.android.games.alite.screens.canvas;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Color;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.library.ItemDescriptor;
import de.phbouillon.android.games.alite.model.library.LibraryPage;
import de.phbouillon.android.games.alite.model.library.Toc;
import de.phbouillon.android.games.alite.model.library.TocEntry;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class LibraryPageScreen extends AliteScreen {
	private static final int PAGE_BEGIN = 120;

	private final List<TocEntry> entries;
	private int entryIndex;
	private int yPosition = 0;
	private int startY;
	private int startX;
	private int lastY;
	private int maxY;
	private int deltaY = 0;
	private final List <PageText> pageText = new ArrayList<>();
	private Button next;
	private Button prev;
	private Button toc;
	private Button backgroundImage;
	private final List <Button> images = new ArrayList<>();
	private String imageText = null;
	private Button largeImage = null;
	private String currentFilter;
	private GLText currentFont = Assets.regularFont;
	private GLText newFont = null;
	private int currentColor = ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT);
	private int newColor = -1;
	private boolean needsCr = false;
	private final List <Pixmap> inlineImages = new ArrayList<>();

	class PageText {
		private StyledText[] words;
		private int[] positions;
		private Pixmap pixmap = null;

		PageText(StyledText[] words, int[] positions) {
			this.words = words;
			this.positions = positions;
		}

		PageText(Pixmap pm) {
			pixmap = pm;
		}
	}

	class Sentence {
		private final List <StyledText> words;
		int width;
		boolean cr;

		Sentence() {
			words = new ArrayList<>();
			width = 0;
			cr = false;
		}

		void add(Graphics g, StyledText word) {
			words.add(word);
			if (word.pixmap != null) {
				return;
			}
			if (width != 0) {
				width += g.getTextWidth(" ", word.font);
			}
			width += g.getTextWidth(word.text, word.font);
		}

		int getWidth(Graphics g, StyledText word) {
			if (word.pixmap != null) {
				return 0;
			}
			if (width == 0) {
				return g.getTextWidth(word.text, word.font);
			}
			return width + g.getTextWidth(" ", word.font) + g.getTextWidth(word.text, word.font);
		}

		void terminate() {
			cr = true;
		}

		public String toString() {
			String result = "[";
			for (int i = 0, n = words.size(); i < n; i++) {
				if (words.get(i).text != null) {
					result += words.get(i).text;
				} else {
					result += "[pixmap]";
				}
				if (i < n - 1) {
					result += " ";
				}
			}
			return result + "]";
		}
	}

	class StyledText {
		private String text;
		private GLText font;
		private int color;
		private Pixmap pixmap = null;

		StyledText(Pixmap pm) {
			pixmap = pm;
		}

		StyledText(String text) {
			this(text, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		}

		StyledText(String text, GLText font, int color) {
			this.text = text;
			this.font = font;
			this.color = color;
		}
	}

	LibraryPageScreen(Alite game, List<TocEntry> entries, int entryIndex, String currentFilter) {
		super(game);
		this.entries = entries;
		this.entryIndex = entryIndex;
		this.currentFilter = currentFilter;
	}

	@Override
	public void activate() {
		Graphics g = game.getGraphics();
		toc = Button.createGradientRegularButton(624, 960, 500, 120, null)
			.setTextData(computeCenteredTextDisplay(g, L.string(R.string.library_btn_toc), 0, 50,
				500 - 2 * Button.BORDER_SIZE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION)));
		LibraryPage page = entries.get(entryIndex).getLinkedPage();
		if (page == null) {
			return;
		}
		computePageText(game.getGraphics(), page.getParagraphs());
		if (entryIndex < entries.size() - 1) {
			next = Button.createGradientRegularButton(1200, 960, 500, 120, null)
				.setTextData(computeCenteredTextDisplay(g, L.string(R.string.library_btn_next) +
					entries.get(entryIndex + 1).getName(),
				0, 50, 500 - 2 * Button.BORDER_SIZE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION)));
		}
		if (entryIndex > 0) {
			prev = Button.createGradientRegularButton(45, 960, 500, 120, null)
				.setTextData(computeCenteredTextDisplay(g, L.string(R.string.library_btn_prev) +
					entries.get(entryIndex - 1).getName(),
				0, 50, 500 - 2 * Button.BORDER_SIZE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION)));
		}
		ItemDescriptor bgImageDesc = page.getBackgroundImage();
		if (bgImageDesc != null) {
			Pixmap pixmap = game.getGraphics().newPixmap(Toc.DIRECTORY_LIBRARY + bgImageDesc.getFileName() + ".png");
			backgroundImage = Button.createPictureButton(AliteConfig.DESKTOP_WIDTH - pixmap.getWidth(), 955 - pixmap.getHeight(),
				pixmap.getWidth(), pixmap.getHeight(), pixmap, 0.6f);
		}

		int counter = 0;
		for (ItemDescriptor id: page.getImages()) {
			try {
				Pixmap pixmap = game.getGraphics().newPixmap(Toc.DIRECTORY_LIBRARY + id.getFileName() + ".png", 500, 250);
				Button b = Button.createGradientPictureButton(1100, PAGE_BEGIN + counter * 275,
					500 + 2 * Button.BORDER_SIZE, 250 + 2 * Button.BORDER_SIZE, pixmap);
				images.add(b);
				counter++;
			} catch (RuntimeException e) {
				// Catching the exception here, without adding pixmap or button, will throw the
				// indexing off (ItemDescriptors are referenced by the bitmap position in the bitmap
				// array list), so this will cause problems -- but once the library is completely in
				// place, this should not be an issue. Just keep in mind during testing.
				AliteLog.e("[ALITE] LibraryPageScreen", "Image " + id.getFileName() + " not found. Skipping.");
			}
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			int entryNo = dis.readInt();
			AliteLog.d("Entry Number", "Read Entry Number: " + entryNo);
			String currentFilter = ScreenBuilder.readString(dis);
			List<TocEntry> entries = Toc.read(L.raw(Toc.DIRECTORY_LIBRARY + "toc.xml")).getEntries(currentFilter);
			LibraryPageScreen lps = new LibraryPageScreen(alite, entries, entryNo >= entries.size() ? 0 : entryNo, currentFilter);
			lps.yPosition = dis.readInt();
			alite.setScreen(lps);
		} catch (IOException e) {
			AliteLog.e("Library Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(entryIndex);
		ScreenBuilder.writeString(dos, currentFilter);
		dos.writeInt(yPosition);

	}

	private void toggleBold(boolean end) {
		if (end) {
			if (newFont == null) {
				newFont = currentFont;
			}
			newFont = newFont == Assets.boldItalicFont ? Assets.italicFont : Assets.regularFont;
		} else {
			currentFont = currentFont == Assets.italicFont ? Assets.boldItalicFont : Assets.boldFont;
		}
	}

	private void toggleItalic(boolean end) {
		if (end) {
			if (newFont == null) {
				newFont = currentFont;
			}
			newFont = newFont == Assets.boldItalicFont ? Assets.boldFont : Assets.regularFont;
		} else {
			currentFont = currentFont == Assets.boldFont ? Assets.boldItalicFont : Assets.italicFont;
		}
	}

	private void toggleColor(int color, boolean end) {
		if (end) {
			newColor = ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT);
		} else {
			currentColor = color;
		}
	}

	private String loadInlineImage(String word) {
		int index = word.indexOf("[G:");
		String fileName = word.substring(index + 3, word.indexOf("]", index));
		inlineImages.add(game.getGraphics().newPixmap(Toc.DIRECTORY_LIBRARY + fileName + ".png"));
		return "@I+" + (inlineImages.size() - 1) + ";";
	}

	private String filterTags(String word) {
		int leftIndex = -1;
		boolean end = false;
		boolean containsTags = false;

		while ((leftIndex = word.indexOf("[", leftIndex + 1)) != -1) {
			containsTags = true;
			int rightIndex = word.indexOf("]", leftIndex);
			if (word.charAt(leftIndex + 1) == 'G') {
				word = loadInlineImage(word);
			} else {
				for (char c: word.substring(leftIndex + 1, rightIndex).toCharArray()) {
					switch (c) {
						case '/': end = true; break;
						case 'b': toggleBold(end); break;
						case 'i': toggleItalic(end); break;
						case 'p': toggleColor(ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), end); break;
						case 'o': toggleColor(ColorScheme.get(ColorScheme.COLOR_SELECTED_TEXT), end); break;
					}
				}
			}
		}
		return containsTags ? word.replaceAll("\\[.*?\\]", "") : word;
	}

	private StyledText getStyledText(String word) {
		needsCr = word.contains("\n");
		if (needsCr) {
			word = word.substring(0, word.indexOf("\n"));
		}
		word = filterTags(word.trim());
		StyledText result;
		if (word.startsWith("@I+")) {
			result = new StyledText(inlineImages.get(Integer.parseInt(word.substring(3, word.indexOf(";")))));
		} else {
			result = new StyledText(word, currentFont, currentColor);
		}
		if (newFont != null) {
			currentFont = newFont;
			newFont = null;
		}
		if (newColor != -1) {
			currentColor = newColor;
			newColor = -1;
		}
		return result;
	}

	private void formatTable(Graphics g, String table, int fieldWidth) {
		table = table.substring(3, table.lastIndexOf(":T]")); // cut off [T: and :T] tags
		String[] tableRows = table.split(";");
		List <Integer> columnWidths = new ArrayList<>();
		Set <Integer> rightAlign = new HashSet<>();
		boolean toggleColor = true;
		for (String row: tableRows) {
			int colIndex = 0;
			for (String col: row.split(",")) {
				col = col.replaceAll("\\[s\\]", " ").trim();
				if (col.contains("[r]")) {
					col.replaceAll("\\[r\\]", "");
					rightAlign.add(colIndex);
				}
				if (col.contains("[m]")) {
					col.replaceAll("\\[m\\]", "");
					toggleColor = false;
				}
				StyledText styledWord = getStyledText(col);
				int width = g.getTextWidth(styledWord.text, styledWord.font);
				if (colIndex >= columnWidths.size()) {
					columnWidths.add(width);
				} else {
					int currentWidth = columnWidths.get(colIndex);
					if (width > currentWidth) {
						columnWidths.set(colIndex, width);
					}
				}
				colIndex++;
			}
		}
		int numOfColumns = columnWidths.size();
		int totalWidth = 0;
		for (int w: columnWidths) {
			totalWidth += w;
		}
		List <Integer> originalWidths = new ArrayList<>();
		if (totalWidth < fieldWidth) {
			int buffer = (int) ((fieldWidth - (float) totalWidth) / numOfColumns);
			for (int i = 0; i < columnWidths.size(); i++) {
				originalWidths.add(columnWidths.get(i));
				columnWidths.set(i, columnWidths.get(i) + buffer);
			}
		} else {
			originalWidths = columnWidths;
		}

		int rowIndex = 0;
		for (String row: tableRows) {
			int colIndex = 0;
			int xPos = 50;
			String[] cols = row.split(",");
			int[] pos = new int[cols.length];
			StyledText[] words = new StyledText[cols.length];
			for (String col: cols) {
				col = col.replaceAll("\\[s\\]", " ").trim();
				col = col.replaceAll("\\[c\\]", ",");
				words[colIndex] = getStyledText(col);
				if (toggleColor) {
					if (rowIndex > 0 && rowIndex % 2 == 0) {
						words[colIndex].color = ColorScheme.get(ColorScheme.COLOR_SELECTED_TEXT);
					} else if (rowIndex > 0) {
						words[colIndex].color = ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT);
					}
				}
				pos[colIndex] = rightAlign.contains(colIndex) ? xPos + originalWidths.get(colIndex) - g.getTextWidth(words[colIndex].text, words[colIndex].font) : xPos;
				xPos += columnWidths.get(colIndex);
				colIndex++;
			}
			pageText.add(new PageText(words, pos));
			rowIndex++;
		}
	}

	private Sentence formatWord(String word, Sentence sentence, List <Sentence> sentences, int fieldWidth, Graphics g) {
		StyledText styledWord = getStyledText(word);
		if (styledWord.pixmap != null) {
			needsCr = true;
			if (sentence.width > 0) {
				sentences.add(sentence);
				sentence = new Sentence();
			}
		}
		int width = sentence.getWidth(g, styledWord);
		if (width > fieldWidth) {
			sentences.add(sentence);
			sentence = new Sentence();
		}
		sentence.add(g, styledWord);
		if (needsCr) {
			sentence.terminate();
			sentences.add(sentence);
			sentence = new Sentence();
		}
		return sentence;
	}

	private int formatSentences(List <Sentence> sentences, int x, int fieldWidth, int inlineImageHeight, Graphics g) {
		for (Sentence s: sentences) {
			int xPos = x;
			StyledText[] words = s.words.toArray(new StyledText[0]);
			if (words.length == 1 && words[0].pixmap != null) {
				pageText.add(new PageText(words[0].pixmap));
				inlineImageHeight += words[0].pixmap.getHeight();
				continue;
			}
			int spaces = words.length - 1;
			if (spaces == 0) {
				pageText.add(new PageText(words, new int[] {x}));
				if (s.cr) {
					pageText.add(new PageText(new StyledText[] {new StyledText("")}, new int[] {0}));
				}
				continue;
			}
			int sentenceWidth = s.width;
			int diff = fieldWidth - sentenceWidth;
			int enlarge = diff / spaces;
			int extraEnlarge = diff - spaces * enlarge;
			int[] pos = new int[words.length];
			int currentWord = 0;
			for (StyledText w: words) {
				pos[currentWord++] = xPos;
				if (w.pixmap != null) {
					pageText.add(new PageText(w.pixmap));
					inlineImageHeight += w.pixmap.getHeight();
					continue;
				}
				xPos += g.getTextWidth(w.text, w.font);
				xPos += g.getTextWidth(" ", w.font) + (s.cr ? 0 : extraEnlarge + enlarge);
				extraEnlarge = 0;
			}
			pageText.add(new PageText(words, pos));
			if (s.cr) {
				pageText.add(new PageText(new StyledText[] {new StyledText("")}, new int[] {0}));
			}
		}
		sentences.clear();
		return inlineImageHeight;
	}

	private void computePageText(Graphics g, String text) {
		int fieldWidth = entries.get(entryIndex).getLinkedPage().getImages().isEmpty() ? 1620 : 1000;
		int x = 50;
		int inlineImageHeight = 0;

		String[] textWords = text.split(" ");
		List <Sentence> sentences = new ArrayList<>();
		Sentence sentence = new Sentence();
		String nextWord;
		int index;
		for (String word: textWords) {
			nextWord = null;
			if ((index = word.indexOf("\n\n")) != -1) {
				if (word.length() > index) {
					nextWord = word.substring(index + 2);
				}
			}

			if (word.indexOf("[T:") > 0) {
				String table = word.substring(word.indexOf("[T:"));
				word = word.substring(0, word.indexOf("[T:"));
				sentence = formatWord(word, sentence, sentences, fieldWidth, g);
				if (sentence.width > 0) {
					sentences.add(sentence);
				}
				if (!sentences.isEmpty()) {
					inlineImageHeight = formatSentences(sentences, x, fieldWidth, inlineImageHeight, g);
				}
				int tableIndex = table.lastIndexOf(":T]");
				String tableString = table.substring(0, tableIndex + 3);
				if (table.length() > tableIndex + 3) {
					nextWord = table.substring(tableIndex + 3).trim();
				}
				AliteLog.e("Table", "Table == " + tableString);
				formatTable(g, tableString, fieldWidth);
			} else if (word.startsWith("[T:")) {
				if (sentence.width > 0) {
					sentences.add(sentence);
				}
				if (!sentences.isEmpty()) {
					inlineImageHeight = formatSentences(sentences, x, fieldWidth, inlineImageHeight, g);
				}
				int tableIndex = word.lastIndexOf(":T]");
				String table = word.substring(0, tableIndex + 3);
				if (word.length() > tableIndex + 3) {
					nextWord = word.substring(tableIndex + 3).trim();
				}
				AliteLog.e("Table", "Table == " + table);
				formatTable(g, table, fieldWidth);
			} else {
				sentence = formatWord(word, sentence, sentences, fieldWidth, g);
			}
			String tempWord = null;
			StyledText styledWord;
			int width;
			while (nextWord != null) {
				if ((index = nextWord.indexOf("\n\n")) != -1) {
					if (nextWord.length() > index) {
						tempWord = nextWord.substring(index + 2);
					}
				}
				styledWord = getStyledText(nextWord);
				nextWord = tempWord;
				tempWord = null;
				if (styledWord.pixmap != null) {
					Sentence s2 = new Sentence();
					s2.add(g, styledWord);
					s2.terminate();
					sentences.add(s2);
					continue;
				}
				width = sentence.getWidth(g, styledWord);
				if (width > fieldWidth) {
					sentences.add(sentence);
					sentence = new Sentence();
				}
				sentence.add(g, styledWord);
			}
		}
		if (sentence.width > 0) {
			sentences.add(sentence);
		}
		inlineImageHeight = formatSentences(sentences, x, fieldWidth, inlineImageHeight, g);
		maxY = Math.max(0, PAGE_BEGIN + 45 * pageText.size() - 890 + inlineImageHeight);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (imageText != null && largeImage != null) {
			if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0) {
				SoundManager.play(Assets.click);
				imageText = null;
				if (backgroundImage == null || largeImage.getPixmap() != backgroundImage.getPixmap()) {
					largeImage.getPixmap().dispose();
				}
				largeImage = null;
			}
			return;
		}
		if (touch.type == TouchEvent.TOUCH_DOWN && touch.pointer == 0) {
			startX = touch.x;
			startY = lastY = touch.y;
		}
		if (touch.type == TouchEvent.TOUCH_DRAGGED && touch.pointer == 0) {
			if (touch.x > AliteConfig.DESKTOP_WIDTH) {
				return;
			}
			yPosition += lastY - touch.y;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
			lastY = touch.y;
			deltaY = 0;
		}
		if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0) {
			if (touch.x > AliteConfig.DESKTOP_WIDTH) {
				return;
			}
			if (Math.abs(startX - touch.x) < 20 && Math.abs(startY - touch.y) < 20) {
				if (backgroundImage != null && backgroundImage.isTouched(touch.x, touch.y)) {
					fullScreenImage(backgroundImage.getPixmap(), "");
				}
				for (int i = 0; i < images.size(); i++) {
					Button b = images.get(i);
					if (b.isTouched(touch.x, touch.y)) {
						Pixmap pixmap = game.getGraphics().newPixmap(Toc.DIRECTORY_LIBRARY +
							entries.get(entryIndex).getLinkedPage().getImages().get(i).getFileName() + ".png");
						fullScreenImage(pixmap, entries.get(entryIndex).getLinkedPage().getImages().get(i).getText());
					}
				}
				if (next != null && next.isTouched(touch.x, touch.y)) {
					newScreen = new LibraryPageScreen(game, entries, entryIndex + 1, currentFilter);
					SoundManager.play(Assets.click);
				}
				if (prev != null && prev.isTouched(touch.x, touch.y)) {
					newScreen = new LibraryPageScreen(game, entries, entryIndex - 1, currentFilter);
					SoundManager.play(Assets.click);
				}
				if (toc.isTouched(touch.x, touch.y)) {
					newScreen = new LibraryScreen(game, currentFilter);
					SoundManager.play(Assets.click);
				}
			}
		}
		if (touch.type == TouchEvent.TOUCH_SWEEP && touch.x < AliteConfig.DESKTOP_WIDTH) {
			deltaY = touch.y2;
		}
	}

	private void fullScreenImage(Pixmap pixmap, String imageText) {
		this.imageText = imageText;
		largeImage = Button.createGradientPictureButton(50, 100, 1650, 920, pixmap)
			.setPixmapOffset(1650 - pixmap.getWidth() >> 1, 0)
			.setTextData(computeTextDisplay(game.getGraphics(), imageText, 0, 920 - 180,
				1650 - 2 * Button.BORDER_SIZE, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT)));
		SoundManager.play(Assets.alert);
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(Color.BLACK);
		displayTitle(entries.get(entryIndex).getName());

		if (deltaY != 0) {
			deltaY += deltaY > 0 ? -1 : 1;
			yPosition -= deltaY;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
		}

		if (next != null) {
			next.render(g);
		}
		if (prev != null) {
			prev.render(g);
		}
		toc.render(g);

		for (Button b: images) {
			b.render(g);
		}

		if (backgroundImage != null) {
			backgroundImage.render(g);
		}

		g.setClip(0, (int) (PAGE_BEGIN - Assets.regularFont.getSize() + 40), -1, 980);
		String[] highlights = new String[0];
		if (currentFilter != null) {
			highlights = currentFilter.toLowerCase(L.getInstance().getCurrentLocale()).split(" ");
		}
		int yOffset = 0;
		int hardClip = -1;
		for (int row = 0, rows = pageText.size(); row < rows; row++) {
			PageText pt = pageText.get(row);
			if (pt.pixmap != null) {
				int picYPos = PAGE_BEGIN - yPosition + row * 45 + 10 + yOffset;
				if (picYPos < PAGE_BEGIN) {
					picYPos = PAGE_BEGIN;
					hardClip = (int) (PAGE_BEGIN - Assets.regularFont.getSize() + 40 + pt.pixmap.getHeight());
				}
				g.drawPixmap(pt.pixmap, 50, picYPos);
				yOffset += pt.pixmap.getHeight() + 45;
				continue;
			}
			for (int x = 0, words = pt.words.length; x < words; x++) {
				int color = pt.words[x].color;
				for (String h: highlights) {
					if (pt.words[x].text.toLowerCase(L.getInstance().getCurrentLocale()).contains(h)) {
						color = ColorScheme.get(ColorScheme.COLOR_SELECTED_TEXT);
					}
				}
				int textY = PAGE_BEGIN - yPosition + row * 45 + 10 + yOffset;
				if (textY > hardClip) {
					g.drawText(pt.words[x].text, pt.positions[x], textY, color, pt.words[x].font);
				}
			}
		}
		g.setClip(-1, -1, -1, -1);

		if (largeImage != null) {
			largeImage.render(g);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		for (Pixmap pm: inlineImages) {
			pm.dispose();
		}
		inlineImages.clear();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.LIBRARY_PAGE_SCREEN;
	}
}
