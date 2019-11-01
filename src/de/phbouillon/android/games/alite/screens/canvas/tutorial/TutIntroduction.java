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

import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.screens.canvas.BuyScreen;
import de.phbouillon.android.games.alite.screens.canvas.GalaxyScreen;
import de.phbouillon.android.games.alite.screens.canvas.LocalScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
@SuppressWarnings("serial")
public class TutIntroduction extends TutorialScreen {
	private StatusScreen status;
	private BuyScreen buy;
	private GalaxyScreen galaxy;
	private int screenToInitialize = 0;
	private Pixmap quelo;

	public TutIntroduction() {
		super(Alite.get());

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
		initLine_11();
		initLine_12();
		initLine_13();
		initLine_14();
	}

	private void initLine_00() {
		addLine(1, L.string(R.string.tutorial_introduction_00))
			.setPostPresentMethod(deltaTime -> game.getGraphics().drawPixmap(quelo, 642, 200));

		status = new StatusScreen(game);
	}

	private void initLine_01() {
		addLine(1, L.string(R.string.tutorial_introduction_01));
	}

	private void initLine_02() {
		addLine(1, L.string(R.string.tutorial_introduction_02))
			.addHighlight(makeHighlight(30, 120, 750, 90));
	}

	private void initLine_03() {
		addLine(1, L.string(R.string.tutorial_introduction_03))
			.addHighlight(makeHighlight(30, 190, 750, 50));
	}

	private void initLine_04() {
		addLine(1, L.string(R.string.tutorial_introduction_04))
			.addHighlight(makeHighlight(30, 230, 750, 50));
	}

	private void initLine_05() {
		addLine(1, L.string(R.string.tutorial_introduction_05))
			.setY(700).setHeight(350).addHighlight(makeHighlight(30, 270, 850, 50));
	}

	private void initLine_06() {
		addLine(1, L.string(R.string.tutorial_introduction_06))
			.addHighlight(makeHighlight(30, 310, 750, 50));
	}

	private void initLine_07() {
		addLine(1, L.string(R.string.tutorial_introduction_07))
			.addHighlight(makeHighlight(30, 350, 750, 50));
	}

	private void initLine_08() {
		addLine(1, L.string(R.string.tutorial_introduction_08))
			.setY(750).addHighlight(makeHighlight(970, 350, 300, 50))
			.addHighlight(makeHighlight(1330, 540, 300, 50));
	}

	private void initLine_09() {
		final TutorialLine line = addLine(1, L.string(R.string.tutorial_introduction_09));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			updateNavBar();
			if (game.getNavigationBar().isAtBottom()) {
				line.setFinished();
			}
		}).addHighlight(makeHighlight(1740, 20, 160, 1040));
	}

	private void initLine_10() {
		addLine(1, L.string(R.string.tutorial_introduction_10));
	}

	private void initLine_11() {
		final TutorialLine line = addLine(1, L.string(R.string.tutorial_introduction_11));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (updateNavBar() instanceof BuyScreen) {
				status.dispose();
				status = null;
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_BUY);
				buy = new BuyScreen(game);
				buy.loadAssets();
				buy.activate();
				line.setFinished();
			}
		});
	}

	private void initLine_12() {
		final TutorialLine line = addLine(1, L.string(R.string.tutorial_introduction_12));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			Screen result = updateNavBar();
			if (result instanceof GalaxyScreen && !(result instanceof LocalScreen)) {
				buy.dispose();
				buy = null;
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_GALAXY);
				galaxy = new GalaxyScreen(game);
				galaxy.loadAssets();
				galaxy.activate();
				line.setFinished();
			}
		});
	}

	private void initLine_13() {
		addLine(1, L.string(R.string.tutorial_introduction_13));
	}

	private void initLine_14() {
		addLine(1, L.string(R.string.tutorial_introduction_14)).setPause(5000);
	}

	@Override
	public void activate() {
		super.activate();
		switch (screenToInitialize) {
			case 0:
				status.activate();
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
				break;
			case 1:
				status.dispose();
				status = null;
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_BUY);
				buy = new BuyScreen(game);
				buy.loadAssets();
				buy.activate();
				break;
			case 2:
				status.dispose();
				status = null;
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_GALAXY);
				galaxy = new GalaxyScreen(game);
				galaxy.loadAssets();
				galaxy.activate();
				break;
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		TutIntroduction ti = new TutIntroduction();
		try {
			ti.currentLineIndex = dis.readByte();
			ti.screenToInitialize = dis.readByte();
			ti.loadScreenState(dis);
		} catch (IOException e) {
			AliteLog.e("Tutorial Introduction Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(ti);
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeByte(currentLineIndex - 1);
		dos.writeByte(status != null ? 0 : buy != null ? 1 : 2);
		super.saveScreenState(dos);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		status.loadAssets();
		if (quelo != null) {
			quelo.dispose();
		}
		quelo = game.getGraphics().newPixmap("quelo.png");
	}

	@Override
	public void doPresent(float deltaTime) {
		if (status != null) {
			status.present(deltaTime);
		} else if (buy != null) {
			buy.present(deltaTime);
		} else if (galaxy != null) {
			galaxy.present(deltaTime);
		}
		renderText();
	}

	@Override
	public void dispose() {
		if (status != null) {
			status.dispose();
			status = null;
		}
		if (buy != null) {
			buy.dispose();
			buy = null;
		}
		if (galaxy != null) {
			galaxy.dispose();
			galaxy = null;
		}
		super.dispose();
		if (quelo != null) {
			quelo.dispose();
			quelo = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_INTRODUCTION_SCREEN;
	}
}
