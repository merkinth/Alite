package de.phbouillon.android.games.alite.screens.canvas.tutorial;

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

import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.screens.canvas.GalaxyScreen;
import de.phbouillon.android.games.alite.screens.canvas.LocalScreen;
import de.phbouillon.android.games.alite.screens.canvas.PlanetScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
@SuppressWarnings("serial")
public class TutNavigation extends TutorialScreen {
	private StatusScreen status;
	private GalaxyScreen galaxy;
	private PlanetScreen planet;
	private LocalScreen  local;
	private int screenToInitialize = 0;

	TutNavigation(final Alite alite) {
		super(alite);

		initLine_00();
		initLine_01();
		initLine_02();
		initLine_03();
		initLine_04();
		initLine_05();
		initLine_06();
		initLine_07();
		initLine_08();
		initLine_09();
		initLine_10();
	}

	private void initLine_00() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_00));

		status = new StatusScreen(game);
		line.setUnskippable().setUpdateMethod(deltaTime -> {
			Screen screen = updateNavBar();
			if (screen instanceof GalaxyScreen && !(screen instanceof LocalScreen)) {
				line.setFinished();
				changeToGalaxyScreen();
			}
		});
	}

	private void initLine_01() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_01)).setY(150);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			galaxy.processAllTouches();
			if (galaxy.namesVisible()) {
				line.setFinished();
			}
		});
	}

	private void initLine_02() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_02));

		line.setUnskippable().setHeight(100).setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				galaxy.processTouch(event);
				if (event.type == TouchEvent.TOUCH_UP) {
					if (galaxy.getHomeButton().isTouched(event.x, event.y)) {
						line.setFinished();
					}
				}
			}
		}).addHighlight(makeHighlight(1020, 980, 320, 100)).setY(150);
	}

	private void initLine_03() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_03)).setY(150).setHeight(300);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (galaxy != null) {
				galaxy.updateMap();
			}
			Screen screen = updateNavBar();
			if (screen instanceof PlanetScreen) {
				galaxy.dispose();
				galaxy = null;
				changeToPlanetScreen();
				line.setFinished();
				currentLineIndex++;
			} else if (screen != null) {
				line.setFinished();
			}
		});
	}

	private void changeToPlanetScreen() {
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_PLANET);
		planet = new PlanetScreen(game);
		planet.loadAssets();
		planet.activate();
	}

	private void initLine_04() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_04));

		line.setHeight(100).setUnskippable().setUpdateMethod(deltaTime -> {
			Screen newScreen = updateNavBar();
			if (newScreen instanceof PlanetScreen) {
				galaxy.dispose();
				galaxy = null;
				changeToPlanetScreen();
				line.setFinished();
			} else if (newScreen != null) {
				line.setFinished();
				currentLineIndex--;
			}
		});
	}

	private void initLine_05() {
		addLine(4, L.string(R.string.tutorial_navigation_05)).setY(700).setHeight(350);
	}

	private void initLine_06() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_06)).setY(150);

		line.setHeight(150).setUnskippable().setUpdateMethod(deltaTime -> {
			Screen screen = updateNavBar();
			if (screen instanceof LocalScreen) {
				planet.dispose();
				planet = null;
				changeToLocalScreen();
				line.setFinished();
				currentLineIndex++;
			} else if (screen != null) {
				line.setFinished();
			}
		});
	}

	private void initLine_07() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_07)).setY(150);

		line.setHeight(150).setUnskippable().setUpdateMethod(deltaTime -> {
			Screen newScreen = updateNavBar();
			if (newScreen instanceof LocalScreen) {
				planet.dispose();
				planet = null;
				changeToLocalScreen();
				line.setFinished();
			} else if (newScreen != null) {
				line.setFinished();
				currentLineIndex--;
			}
		});
	}

	private void initLine_08() {
		final TutorialLine line = addLine(4, L.string(R.string.tutorial_navigation_08)).setY(150);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			local.processAllTouches();
			if (local.getZoomFactor() < 2.0f) {
				line.setFinished();
			}
		});
	}

	private void initLine_09() {
		addLine(4, L.string(R.string.tutorial_navigation_09)).setY(150);
	}

	private void initLine_10() {
		addLine(4, L.string(R.string.tutorial_navigation_10)).setY(150).setHeight(350).setPause(5000);
	}

	@Override
	public void activate() {
		super.activate();
		switch (screenToInitialize) {
			case 0:
				status.activate();
				game.getNavigationBar().moveToTop();
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
				break;
			case 1:
				changeToGalaxyScreen();
				break;
			case 2:
				status.dispose();
				status = null;
				changeToLocalScreen();
				break;
			case 3:
				status.dispose();
				status = null;
				changeToPlanetScreen();
				break;
		}
	}

	private void changeToLocalScreen() {
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_LOCAL);
		local = new LocalScreen(game);
		local.loadAssets();
		local.activate();
	}

	private void changeToGalaxyScreen() {
		status.dispose();
		status = null;
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_GALAXY);
		galaxy = new GalaxyScreen(game);
		galaxy.loadAssets();
		galaxy.activate();
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		TutNavigation tn = new TutNavigation(alite);
		try {
			tn.currentLineIndex = dis.readInt();
			tn.screenToInitialize = dis.readByte();
			tn.loadScreenState(dis);
		} catch (IOException e) {
			AliteLog.e("Tutorial Navigation Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(tn);
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(currentLineIndex - 1);
		dos.writeByte(status != null ? 0 : galaxy != null ? 1 : local != null ? 2 : 3);
		super.saveScreenState(dos);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		status.loadAssets();
	}

	@Override
	public void doPresent(float deltaTime) {
		if (status != null) {
			status.present(deltaTime);
		} else if (galaxy != null) {
			galaxy.present(deltaTime);
		} else if (planet != null) {
			planet.present(deltaTime);
		} else if (local != null) {
			local.present(deltaTime);
		}

		renderText();
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (planet != null) {
			planet.update(deltaTime);
		}
	}

	@Override
	public void dispose() {
		if (status != null) {
			status.dispose();
			status = null;
		}
		if (galaxy != null) {
			galaxy.dispose();
			galaxy = null;
		}
		if (local != null) {
			local.dispose();
			local = null;
		}
		if (planet != null) {
			planet.dispose();
			planet = null;
		}
		super.dispose();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_NAVIGATION_SCREEN;
	}
}
