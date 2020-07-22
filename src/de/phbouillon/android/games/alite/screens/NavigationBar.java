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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

public class NavigationBar {
	private int activeIndex;
	private int pendingIndex = -1;
	private boolean active = true;

	private final ScrollPane scrollPane = new ScrollPane(AliteConfig.DESKTOP_WIDTH, 0, AliteConfig.SCREEN_WIDTH,
		AliteConfig.SCREEN_HEIGHT, () -> new Point(AliteConfig.SCREEN_WIDTH, getHeight()));

	private static class NavigationEntry {
		int titleId;
		Pixmap image;
		String navigationTarget;
		boolean visible;
		int notificationNumber;

		NavigationEntry(int titleId, Pixmap image, String navigationTarget) {
			this.titleId = titleId;
			this.image = image;
			this.navigationTarget = navigationTarget;
			visible = true;
		}
	}

	private final List <NavigationEntry> targets = new ArrayList<>();

	public NavigationBar() {
		Alite game = Alite.get();
		Assets.launchIcon    = game.getGraphics().newPixmap("navigation_icons/launch_icon.png");
		Assets.statusIcon    = game.getGraphics().newPixmap("navigation_icons/status_icon.png");
		Assets.buyIcon       = game.getGraphics().newPixmap("navigation_icons/buy_icon.png");
		Assets.inventoryIcon = game.getGraphics().newPixmap("navigation_icons/inventory_icon.png");
		Assets.equipIcon     = game.getGraphics().newPixmap("navigation_icons/equipment_icon.png");
		Assets.localIcon     = game.getGraphics().newPixmap("navigation_icons/local_icon.png");
		Assets.galaxyIcon    = game.getGraphics().newPixmap("navigation_icons/galaxy_icon.png");
		Assets.planetIcon    = game.getGraphics().newPixmap("navigation_icons/planet_icon.png");
		Assets.diskIcon      = game.getGraphics().newPixmap("navigation_icons/disk_icon.png");
		Assets.achievementsIcon = game.getGraphics().newPixmap("navigation_icons/achievements_icon.png");
		Assets.optionsIcon   = game.getGraphics().newPixmap("navigation_icons/options_icon.png");
		Assets.libraryIcon   = game.getGraphics().newPixmap("navigation_icons/library_icon.png");
		Assets.academyIcon   = game.getGraphics().newPixmap("navigation_icons/academy_icon.png");
		Assets.hackerIcon    = game.getGraphics().newPixmap("navigation_icons/hacker_icon.png");
		Assets.quitIcon      = game.getGraphics().newPixmap("navigation_icons/quit_icon.png");
	}

	public void ensureVisible(int index) {
		scrollPane.position.y = 0;
		int height = getHeight();
		boolean found = (scrollPane.position.y + AliteConfig.SCREEN_HEIGHT) / AliteConfig.NAVIGATION_BAR_SIZE > index + 1;
		while (!found) {
			scrollPane.position.y += AliteConfig.NAVIGATION_BAR_SIZE;
			if (scrollPane.position.y > height - AliteConfig.SCREEN_HEIGHT) {
				found = true;
				scrollPane.position.y = height - AliteConfig.SCREEN_HEIGHT;
			} else {
				found = (scrollPane.position.y + AliteConfig.SCREEN_HEIGHT) / AliteConfig.NAVIGATION_BAR_SIZE > index + 1;
			}
		}

	}

	private int getHeight() {
		int height = targets.size();
		for (NavigationEntry target : targets) {
			if (!target.visible) {
				height--;
			}
		}
		return AliteConfig.NAVIGATION_BAR_SIZE * height;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public synchronized int add(int titleId, Pixmap image, String navigationTarget) {
		targets.add(new NavigationEntry(titleId, image, navigationTarget));
		return targets.size()-1;
	}

	public void setFlightMode(boolean b) {
		targets.get(Alite.NAVIGATION_BAR_LAUNCH).titleId = b ? R.string.navbar_front : R.string.navbar_launch;
		targets.get(Alite.NAVIGATION_BAR_DISK).visible = !b;
		targets.get(Alite.NAVIGATION_BAR_ACADEMY).visible = !b;
		targets.get(Alite.NAVIGATION_BAR_HACKER).visible = !b && Alite.get().isHackerActive();
	}

	public void setVisible(int index, boolean visible) {
		targets.get(index).visible = visible;
	}

	public boolean isVisible(int index) {
		return targets.get(index).visible;
	}

	public void setNotificationNumber(int index, int number) {
		targets.get(index).notificationNumber = number;
	}

	public int getNotificationNumber(int index) {
		return targets.get(index).notificationNumber;
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
			if ((counter + 1) * AliteConfig.NAVIGATION_BAR_SIZE < scrollPane.position.y) {
				counter++;
				positionCounter++;
				continue;
			}
			String title = L.string(entry.titleId);
			int halfWidth  = g.getTextWidth(title, Assets.regularFont) >> 1;
			int halfHeight = g.getTextHeight(title, Assets.regularFont) >> 1;

			int y = positionCounter * AliteConfig.NAVIGATION_BAR_SIZE - scrollPane.position.y + 1;
			int x = AliteConfig.DESKTOP_WIDTH;

			g.diagonalGradientRect(x + 5, y + 5,
				AliteConfig.NAVIGATION_BAR_SIZE - 6, AliteConfig.NAVIGATION_BAR_SIZE - 6,
				ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
			if (entry.image != null) {
				g.drawPixmap(entry.image, x + 5, y + 5);
			}
			if (counter == activeIndex) {
				selX = x;
				selY = y;
			}
			g.rec3d(x, y, AliteConfig.NAVIGATION_BAR_SIZE, AliteConfig.NAVIGATION_BAR_SIZE, 5,
				ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_COLORED_FRAME_LIGHT : ColorScheme.COLOR_FRAME_LIGHT),
				ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_COLORED_FRAME_DARK :ColorScheme.COLOR_FRAME_DARK));

			y = positionCounter * AliteConfig.NAVIGATION_BAR_SIZE - scrollPane.position.y;
			int yPos = entry.image == null ? (int) (y + (AliteConfig.NAVIGATION_BAR_SIZE >> 1) -
				halfHeight + Assets.regularFont.getSize() / 2) : y + AliteConfig.NAVIGATION_BAR_SIZE - 16;
			g.drawText(title, AliteConfig.DESKTOP_WIDTH + (AliteConfig.NAVIGATION_BAR_SIZE >> 1) - halfWidth, yPos,
				ColorScheme.get(counter == activeIndex ? ColorScheme.COLOR_SELECTED_TEXT : ColorScheme.COLOR_MESSAGE), Assets.regularFont);
			if (entry.notificationNumber > 0) {
				Pixmap number = g.getNotificationNumber(Assets.regularFont, entry.notificationNumber);
				g.drawPixmap(number, x + AliteConfig.NAVIGATION_BAR_SIZE - 8 - number.getWidth(), y + 10);
			}
			counter++;
			positionCounter++;
		}
		if (selX != -1 && selY != -1) {
			g.rec3d(selX, selY, AliteConfig.NAVIGATION_BAR_SIZE, AliteConfig.NAVIGATION_BAR_SIZE, 5,
				ColorScheme.get(ColorScheme.COLOR_SELECTED_COLORED_FRAME_LIGHT),
				ColorScheme.get(ColorScheme.COLOR_SELECTED_COLORED_FRAME_DARK));
		}
	}

	public void setActiveIndex(int newIndex) {
		activeIndex = newIndex;
		ensureVisible(activeIndex);
	}

	public Screen checkNavigationBar(TouchEvent event) {
		scrollPane.handleEvent(event);
		return event.type == TouchEvent.TOUCH_UP && active &&
			!scrollPane.isSweepingGesture(event) ? touched(event.x, event.y) : null;
	}

	public void moveToTop() {
		scrollPane.moveToTop();
	}

	public boolean isAtBottom() {
		return scrollPane.isAtBottom();
	}

	private Screen touched(int x, int y) {
		int index = (y + scrollPane.position.y) / AliteConfig.NAVIGATION_BAR_SIZE;
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
		if (index == activeIndex || pendingIndex != -1)  {
			// Nothing to do...
			return null;
		}

		NavigationEntry entry = targets.get(index);

		if (R.string.navbar_front == entry.titleId) {
			SoundManager.play(Assets.click);
			FlightScreen flightScreen = (FlightScreen) Alite.get().getCurrentScreen();
			flightScreen.setForwardView();
			flightScreen.setInformationScreen(null);
			return null;
		}

		if (entry.navigationTarget != null) {
			SoundManager.play(Assets.click);
			String name = getClass().getPackage().getName() + "." + entry.navigationTarget;
			try {
				Screen newScreen = (Screen) Class.forName(name).getConstructor().newInstance();
				pendingIndex = index;
				return newScreen;
			} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException |
				InvocationTargetException | ClassNotFoundException | InstantiationException e) {
				AliteLog.e("Navigation bar", "Screen " + name + " cannot be opened", e);
			}
		}
		return null;
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
