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
import java.util.List;
import java.util.Locale;

import android.graphics.Color;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.Button.TextPosition;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.library.LibraryPage;
import de.phbouillon.android.games.alite.model.library.Toc;
import de.phbouillon.android.games.alite.model.library.TocEntry;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class LibraryScreen extends AliteScreen {
	private final List <Button> button = new ArrayList<>();
	private final List <TocEntryData> entries = new ArrayList<>();
	private final List <TocEntryData> filteredEntries = new ArrayList<>();
	private Pixmap buttonBackground;
	private Pixmap buttonBackgroundPushed;
	private Pixmap searchIcon;
	private Button searchButton;
	private int yPosition = 0;
	private int startY;
	private int startX;
	private int lastY;
	private int maxY;
	private float deltaY = 0.0f;
	private String currentFilter;

	private class TocEntryData {
		TocEntry entry;
		int level;

		TocEntryData(TocEntry entry, int level) {
			this.entry = entry;
			this.level = level;
		}
	}

	// public constructor(Alite) is required for navigation bar
	public LibraryScreen(Alite game) {
		this(game, null);
	}

	LibraryScreen(Alite game, String currentFilter) {
		super(game);
		this.currentFilter = currentFilter;
	}


	@Override
	public void activate() {
		searchButton = Button.createGradientRegularButton(1375, 980, 320, 100, "Search")
			.setPixmap(searchIcon)
			.setTextPosition(TextPosition.RIGHT);

		try {
			button.clear();
			entries.clear();
			filteredEntries.clear();
			Toc toc = Toc.read(game.getFileIO().readPrivateFile("library/toc.xml"), game.getFileIO());
			buildTocButtons(toc.getEntries(), 0);
			if (currentFilter != null) {
				filter();
			}
			Button last = button.get(button.size() - 1);
			maxY = last.getY() + last.getHeight() - 870;
			if (maxY < 0) {
				maxY = 0;
			}
		} catch (IOException e) {
			AliteLog.e("[ALITE] Library", "Error reading Library TOC file.", e);
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			int len = dis.readInt();
			String filter = null;
			if (len > 0) {
				filter = "";
				for (int i = 0; i < len; i++) {
					filter += dis.readChar();
				}
			}
			LibraryScreen ls = new LibraryScreen(alite, filter);
			ls.yPosition = dis.readInt();
			alite.setScreen(ls);
		} catch (IOException e) {
			AliteLog.e("Library Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(currentFilter == null ? 0 : currentFilter.length());
		if (currentFilter != null) {
			dos.writeChars(currentFilter);
		}
		dos.writeInt(yPosition);
	}

	private void filter() {
		filteredEntries.clear();
		button.clear();
		for (TocEntryData entry : entries) {
			LibraryPage libPage = entry.entry.getLinkedPage();
			if (libPage != null) {
				String[] highlights = currentFilter.toLowerCase(Locale.getDefault()).split(" ");
				String paragraph = libPage.getParagraphs();
				if (paragraph != null) {
					paragraph = paragraph.toLowerCase(Locale.getDefault());
					for (String h : highlights) {
						if (paragraph.contains(h)) {
							filteredEntries.add(entry);
							break;
						}
					}
				}
			}
		}
		if (!filteredEntries.isEmpty()) {
			buildTocButtons(filteredEntries.toArray(new TocEntryData[0]));
		}
	}

	private boolean checkCheat(String text) {
		String hash = StringUtil.computeSHAString(text);
		if ("3a6d64c24cf8b69ccda376546467e8266667b50cfd0b984beb3651b129ed7".equals(hash) ||
			"53b1fb446230b347c3f6406cca4b1ddbac60905ba4ab1977179f44b8fb134447".equals(hash)) { // Sara
			setMessage("Sorry, " + text + " does not work here, anymore.");
			return true;
		}
		if ("d2a66247a6fad77347b676dedf6755cedbfbf8aef67cae4dc18ba53346577b5".equals(hash) ||
				   "d3d291ec78221333acf4d79084efe49cebb42fc01ce9c681315745c50e1".equals(hash)) { // Suzanne
			setMessage("No, " + text + " does not work here, either. Sorry.");
			return true;
		}
		if ("5be5a31d90d073e11bd2362aa2336b7e902ea46dada1ec282ae6f4759a4d31ee".equals(hash)) { // Klaudia
			game.activateHacker();
			return cheater("Hacker activated.");
		}
		if ("6efe6cb9668dc015e79a8bf751b543de841f1e824dd4f42c59d80f39a0bd61".equals(hash)) { // Vianne
			Settings.shieldPowerOverride = 40;
			return cheater("Shield Power Booster activated.");
		}
		if ("67e082893e848c8706431c32dea6c3ca86c488ae24ee6b0661489cb8b3bb78a".equals(hash)) { // Christian
			Settings.unlimitedFuel = true;
			return cheater("Hyperdrive Booster activated.");
		}
		if ("dbadfc88144bc153a2d1bdf154681c857a237eb79d58df24e918bca6e17db5".equals(hash)) { // Alexander
			Settings.laserPowerOverride = 15;
			return cheater("Laser Power Booster activated.");
		}
		if ("86cc9b0303fd0b29a61b4b5a0fa78bb5c5fd64aa7346fb35267bcfe5f41".equals(hash)) {
			Settings.laserPowerOverride = 5000;
			return cheater("Maximum Laser Power.");
		}
		if ("87ad16d301e439a57c6296968f1ebbd889d4a5cf58bd0bf1dc97d4259803af8".equals(hash)) {
			Settings.invulnerable = true;
			return cheater("Maximum Shield Power.");
		}
		if ("3152a173cfd3efe739ba51b84937173cf3380fd6d39e49fb92623caa131b4f6".equals(hash)) { // Philipp
			Settings.freePath = true;
			return cheater("Clear Path Ahead.");
		}
		if ("8f9adc99291f20b6f4d91c2a4b6b834477b422c8e3d78b8f481c88f619694".equals(hash)) { // Franz Josef
			OptionsScreen.SHOW_DEBUG_MENU = true;
			return cheater("The Master Control Program is at your service.");
		}
		return false;
	}

	private boolean cheater(String message) {
		setMessage(message);
		game.getPlayer().setCheater(true);
		return true;
	}

	private void performSearch() {
		newScreen = new TextInputScreen(game, "Search Library", "Enter search text", "", this, new TextCallback() {
			@Override
			public void onOk(String text) {
				currentFilter = text;
				boolean messageSet = checkCheat(text);
				filteredEntries.clear();
				yPosition = 0;
				deltaY = 0;
				button.clear();
				if (!text.trim().isEmpty()) {
					filter();
				} else {
					currentFilter = null;
				}
				if (filteredEntries.isEmpty()) {
					if (!messageSet) {
						setMessage("No results found.");
					}
					currentFilter = null;
					Alite.setDefiningScreen(LibraryScreen.this);
					buildTocButtons(entries.toArray(new TocEntryData[0]));
				}
			}

			@Override
			public void onCancel() {
			}
		});
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		super.processTouch(touch);
		if (getMessage() != null) {
			return;
		}
		if (touch.type == TouchEvent.TOUCH_DOWN && touch.pointer == 0) {
			startX = touch.x;
			startY = lastY = touch.y;
			deltaY = 0;
		}
		if (touch.type == TouchEvent.TOUCH_DRAGGED && touch.pointer == 0) {
			if (touch.x > 1920 - NavigationBar.SIZE) {
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
		}
		if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0) {
			if (touch.x > 1920 - NavigationBar.SIZE) {
				return;
			}
			if (Math.abs(startX - touch.x) < 20 &&
				Math.abs(startY - touch.y) < 20) {
				if (searchButton.isTouched(touch.x, touch.y)) {
					SoundManager.play(Assets.click);
					performSearch();
				} else if (touch.y > 79) {
					int index = 0;
					for (Button b: button) {
						if (b.isTouched(touch.x, touch.y)) {
							newScreen = new LibraryPageScreen(game, currentFilter == null ? entries.get(index).entry : filteredEntries.get(index).entry, currentFilter);
							SoundManager.play(Assets.click);
						}
						index++;
					}
				}
			}
		}
		if (touch.type == TouchEvent.TOUCH_SWEEP && touch.x < 1920 - NavigationBar.SIZE) {
			deltaY = touch.y2;
		}
	}

	private void buildTocButtons(TocEntry[] entries, int level) {
		if (entries == null) {
			return;
		}
		for (TocEntry entry: entries) {
			Button b = Button.createPictureButton(20 + level * 100, 80 + button.size() * 140,
				1680 - level * 100, 120, buttonBackground)
				.setPushedBackground(buttonBackgroundPushed);
			if (level != 0) {
				b.setButtonEnd(50);
			}
			button.add(b);
			this.entries.add(new TocEntryData(entry, level));
			buildTocButtons(entry.getChildren(), level + 1);
		}
	}

	private void buildTocButtons(TocEntryData[] entries) {
		if (entries == null) {
			return;
		}
		for (TocEntryData entry: entries) {
			Button b = Button.createPictureButton(20 + entry.level * 100, 80 + button.size() * 140,
				1680 - entry.level * 100, 120, buttonBackground)
				.setPushedBackground(buttonBackgroundPushed);
			if (entry.level != 0) {
				b.setButtonEnd(50);
			}
			button.add(b);
		}
	}

	@Override
	public void present(float deltaTime) {
		if (disposed) {
			return;
		}
		Graphics g = game.getGraphics();
		g.clear(Color.BLACK);
		displayTitle("Library");

		searchButton.render(g);
		if (deltaY != 0) {
			boolean neg = deltaY < 0;
			deltaY += deltaY > 0 ? -8.0f * deltaTime : deltaY < 0 ? 8.0f * deltaTime : 0;
			if (neg && deltaY > 0 || !neg && deltaY < 0) {
				deltaY = 0;
			}
			yPosition -= deltaY;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
		}

		g.setClip(0, 100, -1, 1000);
		for (int i = 0, n = button.size(); i < n; i++) {
			TocEntryData ted = currentFilter == null ? entries.get(i) : filteredEntries.get(i);
			button.get(i)
				.setYOffset(-yPosition)
				.setTextData(computeTextDisplay(g, ted.entry.getName(), 30,
					30 + (120 - (int) Assets.regularFont.getSize() >> 1),
					1680, 0, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION)))
				.render(g);
		}
		g.setClip(-1, -1, -1, -1);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (buttonBackground != null) {
			buttonBackground.dispose();
			buttonBackground = null;
		}
		if (searchIcon != null) {
			searchIcon.dispose();
			searchIcon = null;
		}
		if (buttonBackgroundPushed != null) {
			buttonBackgroundPushed.dispose();
			buttonBackgroundPushed = null;
		}
	}

	@Override
	public void loadAssets() {
		buttonBackground = game.getGraphics().newPixmap("catalog_button.png");
		searchIcon = game.getGraphics().newPixmap("search_icon.png");
		buttonBackgroundPushed = game.getGraphics().newPixmap("catalog_button_pushed.png");
		super.loadAssets();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.LIBRARY_SCREEN;
	}
}
