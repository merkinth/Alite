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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class DiskScreen extends AliteScreen {
	private static final int SIZE     = 450;
	private static final int X_OFFSET = 150;
	private static final int X_GAP    =  50;
	private static final int Y_OFFSET = 315;

	private static Pixmap loadIcon;
	private static Pixmap saveIcon;
	private static Pixmap catalogIcon;

	private Button[] button = new Button[3];
	private final String[] text = new String[] {L.string(R.string.disk_menu_load), L.string(R.string.disk_menu_save), L.string(R.string.title_catalog)};

	@Override
	public void activate() {
		button[0] = Button.createPictureButton(X_OFFSET, Y_OFFSET, SIZE, SIZE, loadIcon);
		button[1] = Button.createPictureButton(X_OFFSET + X_GAP + SIZE, Y_OFFSET, SIZE, SIZE, saveIcon);
		button[2] = Button.createPictureButton(X_OFFSET + X_GAP * 2 + SIZE * 2, Y_OFFSET, SIZE, SIZE, catalogIcon);
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_disk_menu));

		int index = 0;
		for (Button b: button) {
			if (b != null) {
				b.render(g);
				int halfWidth = g.getTextWidth(text[index], Assets.regularFont) >> 1;
				g.drawText(text[index], b.getX() + (b.getWidth() >> 1) - halfWidth, b.getY() + b.getHeight() + 35,
					ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
			}
			index++;
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (button[0].isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new LoadScreen(L.string(R.string.title_cmdr_load));
			}
			if (button[1].isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new SaveScreen(L.string(R.string.title_cmdr_save));
			}
			if (button[2].isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new CatalogScreen(L.string(R.string.title_catalog));
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (loadIcon != null) {
			loadIcon.dispose();
			loadIcon = null;
		}
		if (saveIcon != null) {
			saveIcon.dispose();
			saveIcon = null;
		}
		if (catalogIcon != null) {
			catalogIcon.dispose();
			catalogIcon = null;
		}
	}

	@Override
	public void loadAssets() {
		Graphics g = game.getGraphics();
		if (loadIcon == null) {
			loadIcon = g.newPixmap("load_symbol.png");
		}
		if (saveIcon == null) {
			saveIcon = g.newPixmap("save_symbol.png");
		}
		if (catalogIcon == null) {
			catalogIcon = g.newPixmap("catalog_symbol.png");
		}
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.DISK_SCREEN;
	}
}
