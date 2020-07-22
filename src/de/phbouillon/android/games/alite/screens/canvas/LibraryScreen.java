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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Point;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.Button.TextPosition;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.library.Toc;
import de.phbouillon.android.games.alite.model.library.TocEntry;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class LibraryScreen extends AliteScreen {
	private final List<Button> button = new ArrayList<>();
	private List<TocEntry> entries;
	private Button searchButton;
	private final ScrollPane scrollPane = new ScrollPane(0, 80, AliteConfig.DESKTOP_WIDTH, 980,
		() -> new Point(AliteConfig.SCREEN_WIDTH, getHeight()));
	private String currentFilter;
	private int entryIndexToCenter = -1;

	// default public constructor is required for navigation bar
	@SuppressWarnings("unused")
	public LibraryScreen() {
	}

	LibraryScreen(String currentFilter) {
		this.currentFilter = currentFilter;
	}

	public LibraryScreen(String currentFilter, int yPosition) {
		this.currentFilter = currentFilter;
		scrollPane.position.y = yPosition;
	}

	@Override
	public void activate() {
		searchButton = Button.createGradientRegularButton(1375, 980, 320, 100, L.string(R.string.library_btn_search))
			.setPixmap(pics.get("search_icon"))
			.setTextPosition(TextPosition.RIGHT);

		try {
			entries = Toc.read(L.raw(Toc.TOC_FILENAME)).getEntries(currentFilter);
			filter();
			buildTocButtons(entries);
			if (entryIndexToCenter >= 0) {
				scrollPane.position.y = entryIndexToCenter < 4 ? 0 : Math.min((entryIndexToCenter - 3) * 140, getHeight() - 900);
				entryIndexToCenter = -1;
			}
		} catch (IOException e) {
			AliteLog.e("[ALITE] Library", "Error reading Library TOC file.", e);
		}
	}

	public Screen ensureVisible(int index) {
		entryIndexToCenter = index;
		return this;
	}

	private boolean filter() throws IOException {
		if (currentFilter == null) {
			return false;
		}
		entries = Toc.read(L.raw(Toc.TOC_FILENAME)).getEntries(currentFilter);
		if (entries.isEmpty()) {
			currentFilter = null;
			entries = Toc.read(L.raw(Toc.TOC_FILENAME)).getEntries(currentFilter);
			return true;
		}
		return false;
	}

	private int getHeight() {
		if (button.isEmpty()) {
			return 0;
		}
		Button last = button.get(button.size() - 1);
		return last.getY() + last.getHeight();
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		ScreenBuilder.writeString(dos, currentFilter);
		dos.writeInt(scrollPane.position.y);
	}

	@Override
	public synchronized void update(float deltaTime) {
		super.update(deltaTime);
		if (messageResult == RESULT_YES) {
			find(inputText);
		}
		messageResult = RESULT_NONE;
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		scrollPane.handleEvent(touch);

		if (searchButton.isPressed(touch)) {
			popupTextInput(L.string(R.string.library_search_text), currentFilter, -1);
		} else if (!scrollPane.isSweepingGesture(touch)) {
			int index = 0;
			for (Button b: button) {
				if (b.isPressed(touch)) {
					newScreen = new LibraryPageScreen(entries, index, currentFilter);
					return;
				}
				index++;
			}
		}
	}

	private void find(String text) {
		boolean messageSet = checkCheat(text);
		scrollPane.moveToTop();
		try {
			if (!text.trim().isEmpty()) {
				currentFilter = text;
				boolean fullToc = filter();
				if (!messageSet && fullToc) {
					showMessageDialog(L.string(R.string.library_no_results));
				}
			} else {
				currentFilter = null;
				entries = Toc.read(L.raw(Toc.TOC_FILENAME)).getEntries(currentFilter);
			}
		} catch (IOException e) {
			AliteLog.e("[ALITE] Library", "Error reading Library TOC file.", e);
		}
		buildTocButtons(entries);
	}

	private boolean checkCheat(String text) {
		String hash = StringUtil.computeSHAString(text);
		if ("3a6d64c24cf8b69ccda376546467e8266667b50cfd0b984beb3651b129ed7".equals(hash) ||
			"53b1fb446230b347c3f6406cca4b1ddbac60905ba4ab1977179f44b8fb134447".equals(hash)) { // Sara
			showMessageDialog(L.string(R.string.library_cheat_code_obsoleted1, text));
			return true;
		}
		if ("d2a66247a6fad77347b676dedf6755cedbfbf8aef67cae4dc18ba53346577b5".equals(hash) ||
			"d3d291ec78221333acf4d79084efe49cebb42fc01ce9c681315745c50e1".equals(hash)) { // Suzanne
			showMessageDialog(L.string(R.string.library_cheat_code_obsoleted2, text));
			return true;
		}
		if ("5be5a31d90d073e11bd2362aa2336b7e902ea46dada1ec282ae6f4759a4d31ee".equals(hash)) { // Klaudia
			game.activateHacker();
			return cheater(L.string(R.string.library_cheat_code_hacker));
		}
		if ("6efe6cb9668dc015e79a8bf751b543de841f1e824dd4f42c59d80f39a0bd61".equals(hash)) { // Vianne
			Settings.shieldPowerOverride = 40;
			return cheater(L.string(R.string.library_cheat_code_shield_booster));
		}
		if ("67e082893e848c8706431c32dea6c3ca86c488ae24ee6b0661489cb8b3bb78a".equals(hash)) { // Christian
			Settings.unlimitedFuel = true;
			return cheater(L.string(R.string.library_cheat_code_hyperdrive_booster));
		}
		if ("dbadfc88144bc153a2d1bdf154681c857a237eb79d58df24e918bca6e17db5".equals(hash)) { // Alexander
			Settings.laserPowerOverride = 15;
			return cheater(L.string(R.string.library_cheat_code_laser_booster));
		}
		if ("86cc9b0303fd0b29a61b4b5a0fa78bb5c5fd64aa7346fb35267bcfe5f41".equals(hash)) {
			Settings.laserPowerOverride = 5000;
			return cheater(L.string(R.string.library_cheat_code_max_laser));
		}
		if ("87ad16d301e439a57c6296968f1ebbd889d4a5cf58bd0bf1dc97d4259803af8".equals(hash)) {
			Settings.invulnerable = true;
			return cheater(L.string(R.string.library_cheat_code_max_shield));
		}
		if ("3152a173cfd3efe739ba51b84937173cf3380fd6d39e49fb92623caa131b4f6".equals(hash)) { // Philipp
			Settings.freePath = true;
			return cheater(L.string(R.string.library_cheat_code_clear_path));
		}
		if ("8f9adc99291f20b6f4d91c2a4b6b834477b422c8e3d78b8f481c88f619694".equals(hash)) { // Franz Josef
			OptionsScreen.SHOW_DEBUG_MENU = true;
			return cheater(L.string(R.string.library_cheat_code_master_prg));
		}
		return false;
	}

	private boolean cheater(String message) {
		showMessageDialog(message);
		game.getPlayer().setCheater(true);
		return true;
	}

	private void buildTocButtons(List<TocEntry> entries) {
		button.clear();
		if (entries == null) {
			return;
		}
		for (TocEntry entry: entries) {
			GLText font = Settings.watchedTocEntries.contains(entry.getFileName()) ? Assets.regularFont : Assets.boldItalicFont;
			Button b = Button.createPictureButton(20 + entry.getLevel() * 100, 80 + button.size() * 140,
				1680 - entry.getLevel() * 100, 120, pics.get("catalog_button"))
				.setPushedBackground(pics.get("catalog_button_pushed"))
				.setTextData(computeTextDisplay(game.getGraphics(), entry.getName(), 30,
				30 + (120 - (int) font.getSize() >> 1), 1680, font,
					ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION)));
			if (entry.getLevel() != 0) {
				b.setButtonEnd(50);
			}
			button.add(b);
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(Color.BLACK);
		displayTitle(L.string(R.string.title_library));

		searchButton.render(g);
		scrollPane.scrollingFree();

		g.setClip(0, 100, -1, 1000);
		for (Button value : button) {
			value.setYOffset(-scrollPane.position.y).render(g);
		}
		g.setClip(-1, -1, -1, -1);
	}

	@Override
	public void loadAssets() {
		addPictures("catalog_button", "search_icon", "catalog_button_pushed");
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.LIBRARY_SCREEN;
	}
}
