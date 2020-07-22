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
import java.util.HashSet;
import java.util.Set;

import android.graphics.Point;
import android.util.SparseIntArray;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.Button.TextPosition;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.SystemData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class GalaxyScreen extends AliteScreen {
	private static final int HALF_WIDTH = 760;
	private static final int HALF_HEIGHT = 460;
	private static final int CROSS_SIZE = 40;
	private static final int CROSS_DISTANCE = 2;
	private static final float SCALE_CONST = AliteConfig.DESKTOP_WIDTH / 256.0f;

	private float zoomFactor = 1;
	private float zoomFactorForFind = 1;
	private String title;
	private MappedSystemData[] systemData;
	private final Set<Integer> doubleLocations = new HashSet<>();
	private Button findButton;
	private Button homeButton;
	private int targetX = 0;
	private int targetY = 0;
	private final ScrollPane scrollPane = new ScrollPane(0, 80, AliteConfig.DESKTOP_WIDTH, 1000,
		() -> new Point((int) (AliteConfig.DESKTOP_WIDTH * zoomFactor), (int) (920 * zoomFactor)));
	private boolean zoom = false;
	private int scalingReferenceX = -1;
	private int scalingReferenceY;
	private boolean wasHomeButtonPressed;

	class MappedSystemData {
		SystemData system;
		int xDiff;

		MappedSystemData(SystemData system) {
			this.system = system;
		}

		int getLocationId() {
			return (system.getX() << 8) + system.getY();
		}

		public int x() {
			return transformX(system.getX());
		}

		public int y() {
			return transformY(system.getY());
		}
	}

	// default public constructor is required for navigation bar
	public GalaxyScreen() {
	}

	public GalaxyScreen(float zoomFactor, int centerX, int centerY) {
		this.zoomFactor = zoomFactor;
		scrollPane.position.x = centerX;
		scrollPane.position.y = centerY;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeFloat(zoomFactor);
		dos.writeInt(scrollPane.position.x);
		dos.writeInt(scrollPane.position.y);
	}

	private MappedSystemData findClosestSystem(int x, int y) {
		int minDist = -1;
		Player player = game.getPlayer();
		MappedSystemData closestSystem = null;
		for (MappedSystemData system: systemData) {
			int dist = (system.x() - x) * (system.x() - x) + (system.y() - y) * (system.y() - y);
			if (dist < minDist || closestSystem == null) {
				if (doubleLocations.contains(system.getLocationId()) && player.getHyperspaceSystem() == system.system) {
					continue;
				}
				minDist = dist;
				closestSystem = system;
			}
		}
		return closestSystem;
	}

	private String capitalize(String t) {
		return t == null || t.length() < 1 ? "" : t.length() < 2 ? t :
			Character.toUpperCase(t.charAt(0)) + t.substring(1).toLowerCase(L.getInstance().getCurrentLocale());
	}

	private void findSystem(String text) {
		if (text.trim().isEmpty()) {
			return;
		}
		for (MappedSystemData system: systemData) {
			if (system.system.getName().equalsIgnoreCase(text)) {
				game.getPlayer().setHyperspaceSystem(system.system);
				moveToCenter(system.system.getX(), system.system.getY());
				return;
			}
		}
		int galaxy = game.getGenerator().findGalaxyOfPlanet(text);
		if (galaxy == -1) {
			showMessageDialog(L.string(R.string.galaxy_unknown_planet, capitalize(text)));
			SoundManager.play(Assets.error);
		} else {
			showMessageDialog(L.string(R.string.galaxy_unknown_planet_in_galaxy, capitalize(text), galaxy,
				game.getGenerator().getCurrentGalaxy()));
			SoundManager.play(Assets.alert);
		}

	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_SCALE) {
			if (scalingReferenceX == -1) {
				scalingReferenceX = (int) ((touch.x2 + scrollPane.position.x) / SCALE_CONST / zoomFactor);
				scalingReferenceY = (int) ((touch.y2 + scrollPane.position.y - 100) / SCALE_CONST / zoomFactor);
			}
			zoomFactor = touch.zoomFactor;
			scrollPane.changePosition(transformX(scalingReferenceX), transformY(scalingReferenceY), touch.x2, touch.y2);
			zoom = true;
			targetX = 0;
			targetY = 0;
		}

		if (game.getInput().getTouchCount() > 1 ||
				touch.type == TouchEvent.TOUCH_DRAGGED && touch.pointer == 0 && zoom) {
			return;
		}
		scrollPane.handleEvent(touch);
		targetX = 0;
		targetY = 0;

		if (homeButton.isPressed(touch)) {
			SystemData homeSystem = game.getPlayer().getCurrentSystem();
			game.getPlayer().setHyperspaceSystem(homeSystem);
			if (homeSystem == null) {
				moveToCenter(game.getPlayer().getPosition().x, game.getPlayer().getPosition().y);
			} else {
				moveToCenter(homeSystem.getX(), homeSystem.getY());
			}
			wasHomeButtonPressed = true;
			return;
		}

		if (findButton.isPressed(touch)) {
			popupTextInput(L.string(R.string.galaxy_find_planet_name), "", 8);
			return;
		}

		if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0 && zoom) {
			zoom = false;
			scalingReferenceX = -1;
			return;
		}

		if (touch.type != TouchEvent.TOUCH_UP || scrollPane.isSweepingGesture(touch)) {
			return;
		}

		MappedSystemData closestSystem = findClosestSystem(touch.x, touch.y);
		if (!Rect.inside(closestSystem.x(), closestSystem.y(), 0, 20,
				AliteConfig.DESKTOP_WIDTH, AliteConfig.SCREEN_HEIGHT)) {
			return;
		}
		game.getPlayer().setHyperspaceSystem(closestSystem.system);
		SoundManager.play(Assets.click);
	}

	private int transformX(int x) {
		return (int) (x * SCALE_CONST * zoomFactor) - scrollPane.position.x;
	}

	private int transformY(int y) {
		return 100 + (int) (y * SCALE_CONST * zoomFactor) - scrollPane.position.y;
	}

	void moveToCenter(int x, int y) {
		if (zoomFactor > zoomFactorForFind) {
			float zoomRatio = (zoomFactorForFind - 1) / (zoomFactor - 1);
			scrollPane.position.x *= zoomRatio;
			scrollPane.position.y *= zoomRatio;
		}
		zoomFactor = zoomFactorForFind;
		game.getInput().setZoomFactor(zoomFactor);
		targetX = (AliteConfig.DESKTOP_WIDTH >> 1) - transformX(x);
		targetY = 80 + (AliteConfig.SCREEN_HEIGHT >> 1) - transformY(y);
	}

	private void renderName(MappedSystemData system) {
		Graphics g = game.getGraphics();
		int nameWidth = g.getTextWidth(system.system.getName(), Assets.regularFont);
		int positionX = (int) (3 * zoomFactor) + 2;
		int positionY = 40;
		if (system.x() + nameWidth > HALF_WIDTH << 1) {
			positionX = -positionX - nameWidth;
		}
		if (system.y() + 40 > HALF_HEIGHT << 1) {
			positionY = -40;
		}
		g.drawText(system.system.getName(), system.x() + positionX, system.y() + positionY,
			system.system.getEconomy().getColor(), Assets.regularFont);
		if (game.getPlayer().isPlanetVisited(system.system.getId())) {
			g.drawUnderlinedText(system.system.getName(), system.x() + positionX, system.y() + positionY,
				system.system.getEconomy().getColor(), Assets.regularFont);
		} else {
			g.drawText(system.system.getName(), system.x() + positionX, system.y() + positionY,
				system.system.getEconomy().getColor(), Assets.regularFont);
		}
	}

	public void updateMap() {
		if (targetX != 0 || targetY != 0) {
			int deltaX = targetX >> 4;
			int deltaY = targetY >> 3;
			if (deltaX == 0 && targetX != 0) {
				deltaX = targetX < 0 ? -1 : 1;
			}
			if (deltaY == 0 && targetY != 0) {
				deltaY = targetY < 0 ? -1 : 1;
			}
			scrollPane.setScrollingTarget(deltaX, deltaY);
			targetX -= deltaX;
			targetY -= deltaY;
		}
		scrollPane.scrollingFree();
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (messageResult == RESULT_YES) {
			findSystem(inputText);
		}
		messageResult = RESULT_NONE;
		updateMap();
	}

	private void renderCurrentPositionCross() {
		Graphics g = game.getGraphics();
		Player player = game.getPlayer();
		SystemData hyperspaceSystem = player.getHyperspaceSystem();

		int px = transformX(hyperspaceSystem == null ? player.getPosition().x : hyperspaceSystem.getX());
		int py = transformY(hyperspaceSystem == null ? player.getPosition().y : hyperspaceSystem.getY());
		g.drawLine(px, py - CROSS_SIZE - CROSS_DISTANCE, px, py - CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
		g.drawLine(px, py + CROSS_SIZE + CROSS_DISTANCE, px, py + CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
		g.drawLine(px - CROSS_SIZE - CROSS_DISTANCE, py, px - CROSS_DISTANCE, py, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
		g.drawLine(px + CROSS_SIZE + CROSS_DISTANCE, py, px + CROSS_DISTANCE, py, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
	}

	private void renderCurrentFuelCircle() {
		Graphics g = game.getGraphics();
		Player player = game.getPlayer();

		// 36 is half of 7.2 light years distance, instead of half of 7.0.
		// The real distance of planets could be bigger than the calculated one.
		// See also SystemData.computeDistance
		int r = (int) (36 * SCALE_CONST * zoomFactor) >> 1;
		SystemData hyperspaceSystem = player.getHyperspaceSystem();
		if (hyperspaceSystem != null) {
			int px = transformX(hyperspaceSystem.getX());
			int py = transformY(hyperspaceSystem.getY());
			g.drawDashedCircle(px, py, r, ColorScheme.get(ColorScheme.COLOR_DASHED_FUEL_CIRCLE));
		}

		SystemData currentSystem = player.getCurrentSystem();
		int px = transformX(currentSystem == null ? player.getPosition().x : player.getCurrentSystem().getX());
		int py = transformY(currentSystem == null ? player.getPosition().y : player.getCurrentSystem().getY());
		g.drawCircle(px, py, r * player.getCobra().getFuel() / player.getCobra().getMaxFuel(),
			ColorScheme.get(ColorScheme.COLOR_FUEL_CIRCLE));
	}

	private void renderDistance() {
		Player player = game.getPlayer();
		Graphics g = game.getGraphics();

		if (player.getHyperspaceSystem() != null) {
			int distance = player.computeDistance();
			g.drawText(L.string(R.string.galaxy_distance_info,
				player.getHyperspaceSystem().getName(), distance / 10, distance % 10),
				100, 1060, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();

		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(title);

		g.setClip(0, -1, -1, 1000);
		int r = (int) (3 * zoomFactor);
		for (MappedSystemData system: systemData) {
			g.fillCircle(system.x() + system.xDiff, system.y(), r, system.system.getEconomy().getColor());
			if (namesVisible() && Rect.inside(system.x(), system.y(), 0, 0,
					AliteConfig.SCREEN_WIDTH, AliteConfig.SCREEN_HEIGHT)) {
				renderName(system);
			} else if (game.getPlayer().isPlanetVisited(system.system.getId())) {
				g.drawLine(system.x() + system.xDiff - r, system.y() + r + 2,
					system.x() + system.xDiff + r, system.y() + r + 2, system.system.getEconomy().getColor());
			}
		}

		renderCurrentPositionCross();
		renderCurrentFuelCircle();
		renderDistance();

		g.setClip(-1, -1, -1, -1);

		homeButton.render(g);
		findButton.render(g);
	}

	void setupUi() {
		initializeSystems();

		findButton = Button.createGradientRegularButton(1375, 980, 320, 100, L.string(R.string.galaxy_btn_find))
			.setPixmap(pics.get("search_icon"))
			.setTextPosition(TextPosition.RIGHT);

		homeButton = Button.createGradientRegularButton(1020, 980, 320, 100, L.string(R.string.galaxy_btn_home))
			.setPixmap(pics.get("home_icon"))
			.setTextPosition(TextPosition.RIGHT);
	}

	@Override
	public void activate() {
		activateScreen(L.string(R.string.title_galaxy, game.getGenerator().getCurrentGalaxy()));
	}

	void initPosition(int centerX, int centerY, float zoomFactor) {
		this.zoomFactor = zoomFactor;
		zoomFactorForFind = zoomFactor;
		scrollPane.changePosition(transformX(centerX), transformY(centerY), AliteConfig.DESKTOP_WIDTH >> 1,
			80 + AliteConfig.SCREEN_HEIGHT >> 1);
	}

	void activateScreen(String title) {
		this.title = title;
		game.getInput().setZoomFactor(zoomFactor);
		setupUi();
	}

	private void initializeSystems() {
		int raxlaa = game.isRaxxlaVisible() ? 1 : 0;

		SystemData[] system = game.getGenerator().getSystems();
		systemData = new MappedSystemData[system.length + raxlaa];
		SparseIntArray doubleCounts = new SparseIntArray(system.length);
		for (int i = 0; i < system.length; i++) {
			systemData[i] = new MappedSystemData(system[i]);
			int key = systemData[i].getLocationId();
			int count = doubleCounts.get(key);
			doubleCounts.put(key, count + 1);
			if (count > 0) {
				doubleLocations.add(key);
				systemData[i].xDiff = count << 3;
			}
		}
		if (raxlaa == 1) {
			systemData[system.length] = new MappedSystemData(SystemData.RAXXLA_SYSTEM);
		}
	}

	public boolean namesVisible() {
		return zoomFactor >= 4.0f;
	}

	public boolean wasHomeButtonPressed() {
		return wasHomeButtonPressed;
	}

	public float getZoomFactor() {
		return zoomFactor;
	}

	@Override
	public void loadAssets() {
		addPictures("search_icon", "home_icon");
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.GALAXY_SCREEN;
	}

	private static Point toScreen(SystemData systemData, int centerX, int centerY, float zoomFactor) {
		int offsetX = (int) (centerX * zoomFactor * SCALE_CONST) - 400;
		int offsetY = (int) (centerY * zoomFactor * SCALE_CONST) - 550;
		Point p = new Point((int) (systemData.getX() * zoomFactor * SCALE_CONST + 900 - offsetX),
			(int) (systemData.getY() * zoomFactor * SCALE_CONST + 100 - offsetY));
		return Rect.inside(p.x, p.y, 900, 100,  1700, 1000) ? p : null;
	}

	private static void drawSystem(SystemData system, Point p, float zoomFactor, boolean targetSystem) {
		Graphics g = Alite.get().getGraphics();
		g.fillCircle(p.x, p.y, (int) (3 * zoomFactor), system.getEconomy().getColor());
		int nameWidth = g.getTextWidth(system.getName(), targetSystem ? Assets.regularFont : Assets.smallFont);
		int nameHeight = g.getTextHeight(system.getName(), targetSystem ? Assets.regularFont : Assets.smallFont);
		int positionX = (int) (3 * zoomFactor) + 2;
		int positionY = 40;
		if (p.x + nameWidth > HALF_WIDTH << 1) {
			positionX = -positionX - nameWidth;
		}
		if (p.y + 40 > HALF_HEIGHT << 1) {
			positionY = -40;
		}
		if (targetSystem) {
			g.fillRect(p.x + positionX, p.y + positionY - nameHeight, nameWidth, nameHeight,
				ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		}
		g.drawText(system.getName(), p.x + positionX, p.y + positionY, system.getEconomy().getColor(),
			targetSystem ? Assets.regularFont : Assets.smallFont);
	}

	public static void displayStarMap(SystemData targetSystem) {
		int centerX = targetSystem.getX();
		int centerY = targetSystem.getY();

		for (SystemData system:  Alite.get().getGenerator().getSystems()) {
			Point p = toScreen(system, centerX, centerY, 3.0f);
			if (p != null) {
				drawSystem(system, p, 3.0f, false);
			}
		}
		// Make sure the target system is rendered on top...
		Graphics g = Alite.get().getGraphics();
		Point p = toScreen(targetSystem, centerX, centerY, 3.0f);
		if (p != null) {
			drawSystem(targetSystem, p, 3.0f, true);
			g.drawLine(p.x, p.y - CROSS_SIZE - CROSS_DISTANCE, p.x, p.y - CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x, p.y + CROSS_SIZE + CROSS_DISTANCE, p.x, p.y + CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x - CROSS_SIZE - CROSS_DISTANCE, p.y, p.x - CROSS_DISTANCE, p.y, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x + CROSS_SIZE + CROSS_DISTANCE, p.y, p.x + CROSS_DISTANCE, p.y, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
		}
	}

}
