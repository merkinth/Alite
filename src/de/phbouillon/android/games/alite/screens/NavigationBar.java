package de.phbouillon.android.games.alite.screens;

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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.DiskScreen;
import de.phbouillon.android.games.alite.screens.canvas.QuitScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;

public class NavigationBar {
	public static final int SIZE = 200;

	private int position;
	private int activeIndex;
	private int pendingIndex = -1;
	private boolean active = true;
	private final Alite game;

	static class NavigationEntry {
		String title;
		Pixmap image;
		String navigationTarget;
		boolean visible;

		NavigationEntry(String title, Pixmap image, String navigationTarget) {
			this.title = title;
			this.image = image;
			this.navigationTarget = navigationTarget;
			visible = true;
		}
	}

	private final List <NavigationEntry> targets = new ArrayList<>();

	public NavigationBar(Alite game) {
		position = 0;
		this.game = game;

		Assets.launchIcon    = game.getGraphics().newPixmap("navigation_icons/launch_icon.png");
		Assets.statusIcon    = game.getGraphics().newPixmap("navigation_icons/status_icon.png");
		Assets.buyIcon       = game.getGraphics().newPixmap("navigation_icons/buy_icon.png");
		Assets.inventoryIcon = game.getGraphics().newPixmap("navigation_icons/inventory_icon.png");
		Assets.equipIcon     = game.getGraphics().newPixmap("navigation_icons/equipment_icon.png");
		Assets.localIcon     = game.getGraphics().newPixmap("navigation_icons/local_icon.png");
		Assets.galaxyIcon    = game.getGraphics().newPixmap("navigation_icons/galaxy_icon.png");
		Assets.planetIcon    = game.getGraphics().newPixmap("navigation_icons/planet_icon.png");
		Assets.diskIcon      = game.getGraphics().newPixmap("navigation_icons/disk_icon.png");
		Assets.optionsIcon   = game.getGraphics().newPixmap("navigation_icons/options_icon.png");
		Assets.libraryIcon   = game.getGraphics().newPixmap("navigation_icons/library_icon.png");
		Assets.academyIcon   = game.getGraphics().newPixmap("navigation_icons/academy_icon.png");
		Assets.hackerIcon    = game.getGraphics().newPixmap("navigation_icons/hacker_icon.png");
		Assets.quitIcon      = game.getGraphics().newPixmap("navigation_icons/quit_icon.png");
	}

	private void ensureVisible() {
		position = 0;
		int realSize = targets.size();
		for (NavigationEntry target : targets) {
			if (!target.visible) {
				realSize--;
			}
		}
		boolean found = (position + 1080) / SIZE > activeIndex + 1;
		while (!found) {
			position += SIZE;
			if (position > SIZE * realSize - 1080) {
				found = true;
				position = SIZE * realSize - 1080;
			} else {
				found = (position + 1080) / SIZE > activeIndex + 1;
			}
		}

	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public synchronized int add(String title, Pixmap image, String navigationTarget) {
		NavigationEntry entry = new NavigationEntry(title, image, navigationTarget);
		targets.add(entry);
		return targets.size()-1;
	}

	public void setFlightMode(boolean b) {
		targets.get(Alite.NAVIGATION_BAR_LAUNCH).title = b ? "Front" : "Launch";
		targets.get(Alite.NAVIGATION_BAR_DISK).visible = !b;
		targets.get(Alite.NAVIGATION_BAR_ACADEMY).visible = !b;
		targets.get(Alite.NAVIGATION_BAR_HACKER).visible = !b && game.isHackerActive();
	}

	public void setVisible(int index, boolean visible) {
		targets.get(index).visible = visible;
	}

	public boolean isVisible(int index) {
		return targets.get(index).visible;
	}

	public void render(Graphics g) {
		if (AndroidGame.resetting) {
			GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
			return;
		}
		int counter = 0;
		int positionCounter = 0;
		int selX = -1;
		int selY = -1;
		for (NavigationEntry entry: targets) {
			if (!entry.visible) {
				counter++;
				continue;
			}
			if (counter * SIZE + SIZE < position) {
				counter++;
				positionCounter++;
				continue;
			}
			int halfWidth  = g.getTextWidth(entry.title, Assets.regularFont) >> 1;
			int halfHeight = g.getTextHeight(entry.title, Assets.regularFont) >> 1;

			int y = positionCounter * SIZE - position + 1;
			int x = 1920 - SIZE;

			g.diagonalGradientRect(x + 5, y + 5, SIZE - 6, SIZE - 6,
				ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
			if (entry.image != null) {
				g.drawPixmap(entry.image, x + 5, y + 5);
			}
			if (counter == activeIndex) {
				selX = x;
				selY = y;
			}
			g.rec3d(x, y, SIZE, SIZE, 5, ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_COLORED_FRAME_LIGHT : ColorScheme.COLOR_FRAME_LIGHT),
					ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_COLORED_FRAME_DARK :ColorScheme.COLOR_FRAME_DARK));

			y = positionCounter * SIZE;
			int yPos = entry.image == null ? (int) (y + (SIZE >> 1) - halfHeight + Assets.regularFont.getSize() / 2) - position :
				                             (int) (y + SIZE - position - 10);

			g.drawText(entry.title, 1920 - (SIZE >> 1) - halfWidth, yPos,
				ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_TEXT : ColorScheme.COLOR_MESSAGE), Assets.regularFont);
			counter++;
			positionCounter++;
		}
		if (selX != -1 && selY != -1) {
			g.rec3d(selX, selY, SIZE, SIZE, 5, ColorScheme.get(ColorScheme.COLOR_SELECTED_COLORED_FRAME_LIGHT),
					ColorScheme.get(ColorScheme.COLOR_SELECTED_COLORED_FRAME_DARK));
		}
	}

	public void setActiveIndex(int newIndex) {
		activeIndex = newIndex;
		ensureVisible();
	}

	public void increasePosition(int delta) {
		position += delta;
		int realSize = targets.size();
		for (NavigationEntry target : targets) {
			if (!target.visible) {
				realSize--;
			}
		}

		if (position > SIZE * realSize - 1080) {
			position = SIZE * realSize - 1080;
		}
	}

	public void moveToTop() {
		position = 0;
	}

	public boolean isAtBottom() {
		int realSize = targets.size();
		for (NavigationEntry target : targets) {
			if (!target.visible) {
				realSize--;
			}
		}
		return position == SIZE * realSize - 1080;
	}

	public void decreasePosition(int delta) {
		position -= delta;
		if (position < 0) {
			position = 0;
		}
	}

	public Screen touched(Alite game, int x, int y) {
		if (x < 1920 - SIZE || !active) {
			return null;
		}

		int targetY = y + position;
		int index = targetY / SIZE;
		int realIndex = index;
		for (int i = 0; i <= realIndex; i++) {
			if (i >= targets.size()) {
				AliteLog.e("NavigationBar", "Index out of bounds: " + index);
				return null;
			}
			if (!targets.get(i).visible) {
				realIndex++;
			}
		}
		index = realIndex;

		if (index < 0 || index >= targets.size()) {
			AliteLog.e("NavigationBar", "Index out of bounds: " + index);
			return null;
		}
		if ((index == activeIndex || pendingIndex != -1) && index != Alite.NAVIGATION_BAR_DISK) {
			// Nothing to do...
			return null;
		}
		if (index == Alite.NAVIGATION_BAR_DISK && game.getCurrentScreen() instanceof DiskScreen) {
			// Nothing to do... Otherwise, if index is DiskScreen and
			// the current screen is _not_ instance of DiskScreen, we are in
			// a sub menu of the disk screen and want to return to the
			// disk screen. This feels like a hack. To much explanation
			// necessary... :(
			return null;
		}
		NavigationEntry entry = targets.get(index);
		Screen newScreen = null;
		if (entry.navigationTarget != null) {
			SoundManager.play(Assets.click);
			try {
				if (index == Alite.NAVIGATION_BAR_PLANET &&
						game.getPlayer().getCurrentSystem() == null && game.getPlayer().getHyperspaceSystem() == null) {
					SoundManager.play(Assets.error);
				}
				newScreen = (Screen) Class.forName(getClass().getPackage().getName() + ".canvas." + entry.navigationTarget).
					getConstructor(Alite.class).newInstance(game);
				pendingIndex = index;
			} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException |
				InvocationTargetException | ClassNotFoundException | InstantiationException e) {
				e.printStackTrace();
			}
		}
		else {
			switch (entry.title) {
				case "Launch":
					SoundManager.play(Assets.click);
					try {
						AliteLog.d("[ALITE]", "Performing autosave. [Launch]");
						game.autoSave();
					} catch (IOException e) {
						AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
					}
					InGameManager.safeZoneViolated = false;
					newScreen = new FlightScreen(game, true);
					break;
				case "Front":
					SoundManager.play(Assets.click);
					((FlightScreen) game.getCurrentScreen()).setForwardView();
					((FlightScreen) game.getCurrentScreen()).setInformationScreen(null);
					break;
				case "Quit":
					SoundManager.play(Assets.click);
					FlightScreen fs = game.getCurrentScreen() instanceof FlightScreen ? (FlightScreen) game.getCurrentScreen() : null;
					newScreen = new QuitScreen(game, fs);
					break;
			}
		}
		return newScreen;
	}

	public void resetPending() {
		pendingIndex = -1;
	}

	public void performScreenChange() {
		if (pendingIndex != -1) {
			activeIndex = pendingIndex;
			pendingIndex = -1;
		}
	}
}
