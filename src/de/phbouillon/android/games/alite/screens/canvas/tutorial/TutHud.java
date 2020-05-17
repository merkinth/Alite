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

import android.graphics.Rect;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
public class TutHud extends TutorialScreen {
	private FlightScreen flight;

	protected TutHud(final FlightScreen flight) {
		super(true);
		this.flight = flight;

		setInitialConditions();
		game.getPlayer().setLegalValue(0);

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
		initLine_15();
		initLine_16();
		initLine_17();
		initLine_18();
		initLine_19();
		initLine_20();
		initLine_21();
		initLine_22();
		initLine_23();
		initLine_24();
		initLine_25();
		initLine_26();
		initLine_27();
		initLine_28();
		initLine_29();
		initLine_30();
		initLine_31();
		initLine_32();
		initLine_33();
		initLine_34();
		initLine_35();
		initLine_36();
		initLine_37();
	}

	public TutHud(DataInputStream dis) throws IOException, ClassNotFoundException {
		this(FlightScreen.createScreen(dis));
		currentLineIndex = dis.readInt();
		loadScreenState(dis);
	}

	private void setInitialConditions() {
		for (int i = 0; i < Settings.buttonPosition.length; i++) {
			Settings.buttonPosition[i] = i;
		}
		game.getCobra().clearEquipment();
		game.getCobra().setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.get().getEquipmentById(EquipmentStore.PULSE_LASER));
		game.getCobra().setLaser(PlayerCobra.DIR_RIGHT, null);
		game.getCobra().setLaser(PlayerCobra.DIR_REAR, null);
		game.getCobra().setLaser(PlayerCobra.DIR_LEFT, null);
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(SystemData.LAVE_SYSTEM_INDEX));
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(SystemData.ZAONCE_SYSTEM_INDEX));
		game.getCobra().setFuel(70);
	}

	private TutorialLine addTopLine(String text) {
		return addLine(5, text).setX(250).setWidth(1420).setY(20).setHeight(140);
	}

	private void initLine_00() {
		addTopLine(L.string(R.string.tutorial_hud_00))
			.setUpdateMethod(deltaTime -> {
					flight.getInGameManager().getShip().setSpeed(0);
					flight.getInGameManager().setPlayerControl(false);
				});
		if (flight == null) {
			flight = new FlightScreen(true);
			flight.setHandleUI(false);
		}
	}

	private void initLine_01() {
		addTopLine(L.string(R.string.tutorial_hud_01));
	}

	private void initLine_02() {
		addTopLine(L.string(R.string.tutorial_hud_02));
	}

	private void initLine_03() {
		addTopLine(L.string(R.string.tutorial_hud_03)).
				addHighlight(makeHighlight(1284, 935, 128, 128));
	}

	private void initLine_04() {
		addTopLine(L.string(R.string.tutorial_hud_04));
	}

	private void initLine_05() {
		addLine(5, L.string(R.string.tutorial_hud_05,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_05a: R.string.tutorial_hud_05b)),
				Settings.tapRadarToChangeView ? "a" : "b")
			.setX(250).setWidth(1420).setY(20).setHeight(180);
	}

	private void initLine_06() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_hud_06));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 1) {
					flight.getInGameManager().setViewport(1);
					currentLineIndex++;
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_07() {
		final TutorialLine line =
				addLine(5, L.string(R.string.tutorial_hud_07,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_05a : R.string.tutorial_hud_05b)),
				Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(180);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 1) {
					flight.getInGameManager().setViewport(1);
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					currentLineIndex--;
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_08() {
		addLine(5, L.string(R.string.tutorial_hud_08,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_08a : R.string.tutorial_hud_08b)),
				Settings.tapRadarToChangeView ? "a" : "b")
			.setX(250).setWidth(1420).setY(20).setHeight(180);
	}

	private void initLine_09() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_hud_09));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 2) {
					flight.getInGameManager().setViewport(2);
					currentLineIndex++;
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_10() {
		final TutorialLine line = addLine(5, L.string(R.string.tutorial_hud_10,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_10a : R.string.tutorial_hud_05b)),
				Settings.tapRadarToChangeView ? "a" : "b")
			.setX(250).setWidth(1420).setY(20).setHeight(180);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 2) {
					flight.getInGameManager().setViewport(2);
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					currentLineIndex--;
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_11() {
		addTopLine(L.string(R.string.tutorial_hud_11)).
				setHeight(180);
	}

	private void initLine_12() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_hud_12));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 3) {
					flight.getInGameManager().setViewport(3);
					currentLineIndex++;
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_13() {
		final TutorialLine line = addLine(5, L.string(R.string.tutorial_hud_13,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_13a : R.string.tutorial_hud_13b)),
				Settings.tapRadarToChangeView ? "a" : "b")
			.setX(250).setWidth(1420).setY(20).setHeight(180);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 3) {
					flight.getInGameManager().setViewport(3);
					line.setFinished();
					return;
				}
				if (viewPort >= 0) {
					currentLineIndex--;
					line.setFinished();
					return;
				}
			}
		});
	}

	private void initLine_14() {
		addTopLine(L.string(R.string.tutorial_hud_14))
			.setHeight(180);
	}

	private void initLine_15() {
		addTopLine(L.string(R.string.tutorial_hud_15))
			.setHeight(180);
	}

	private void initLine_16() {
		final TutorialLine line = addLine(5, L.string(R.string.tutorial_hud_16,
				L.string(Settings.tapRadarToChangeView ? R.string.tutorial_hud_16a : R.string.tutorial_hud_16b)),
				Settings.tapRadarToChangeView ? "a" : "b")
			.setX(250).setWidth(1420).setY(20).setHeight(140);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				int viewPort = flight.getInGameManager().handleExternalViewportChange(event);
				if (viewPort == 3 && Settings.tapRadarToChangeView) {
					flight.getInGameManager().setViewport(3);
					line.setFinished();
				} else if (viewPort == 4 && !Settings.tapRadarToChangeView) {
					flight.getInGameManager().toggleZoom();
					line.setFinished();
				}
			}
		});
	}

	private void initLine_17() {
		addTopLine(L.string(R.string.tutorial_hud_17));
	}

	private void initLine_18() {
		addTopLine(L.string(R.string.tutorial_hud_18)).setUpdateMethod(deltaTime -> {
			if (flight.getInGameManager().getViewDirection() != 0) {
				flight.getInGameManager().setViewport(0);
			}
		});
	}

	private void initLine_19() {
		addTopLine(L.string(R.string.tutorial_hud_19))
			.addHighlight(makeHighlight(150, 700, 350, 76));
	}

	private void initLine_20() {
		addTopLine(L.string(R.string.tutorial_hud_20))
			.setHeight(180).addHighlight(makeHighlight(1420, 845, 350, 156));
	}

	private void initLine_21() {
		addTopLine(L.string(R.string.tutorial_hud_21));
	}

	private void initLine_22() {
		addTopLine(L.string(R.string.tutorial_hud_22))
			.addHighlight(makeHighlight(150, 805, 350, 36));
	}

	private void initLine_23() {
		addTopLine(L.string(R.string.tutorial_hud_23))
			.setHeight(180).addHighlight(makeHighlight(150, 845, 350, 36));
	}

	private void initLine_24() {
		addTopLine(L.string(R.string.tutorial_hud_24))
			.addHighlight(makeHighlight(150, 885, 350, 36));
	}

	private void initLine_25() {
		addTopLine(L.string(R.string.tutorial_hud_25))
			.setHeight(180).addHighlight(makeHighlight(150, 925, 350, 36));
	}

	private void initLine_26() {
		addTopLine(L.string(R.string.tutorial_hud_26))
			.setHeight(180).addHighlight(makeHighlight(150, 990, 350, 38));
	}

	private void initLine_27() {
		addTopLine(L.string(R.string.tutorial_hud_27))
			.addHighlight(makeHighlight(150, 990, 350, 38));
	}

	private void initLine_28() {
		addTopLine(L.string(R.string.tutorial_hud_28))
			.addHighlight(makeHighlight(1420, 700, 350, 116));
	}

	private void initLine_29() {
		addLine(5, L.string(R.string.tutorial_hud_29))
			.addHighlight(makeHighlight(0, 0, 350, 350))
			.addHighlight(makeHighlight(1710, 0, 200, 500));
	}

	private void initLine_30() {
		addLine(5, L.string(R.string.tutorial_hud_30,
			L.string(Settings.laserButtonAutoFire ? R.string.tutorial_hud_30a : R.string.tutorial_hud_30b)),
			Settings.laserButtonAutoFire ? "a" : "b")
		.addHighlight(makeHighlight(0, 0, 200, 200));
	}

	private void initLine_31() {
		addLine(5, L.string(R.string.tutorial_hud_31))
			.addHighlight(makeHighlight(0, 0, 200, 200));
	}

	private void initLine_32() {
		addLine(5, L.string(R.string.tutorial_hud_32))
			.addHighlight(makeHighlight(150, 150, 200, 200));
	}

	private void initLine_33() {
		addLine(5, L.string(R.string.tutorial_hud_33))
			.addHighlight(makeHighlight(1710, 0, 200, 200));
	}

	private void initLine_34() {
		addLine(5, L.string(R.string.tutorial_hud_34))
			.addHighlight(makeHighlight(1710, 0, 200, 200));
	}

	private void initLine_35() {
		addLine(5, L.string(R.string.tutorial_hud_35))
			.addHighlight(makeHighlight(1710, 300, 200, 200));
	}

	private void initLine_36() {
		addLine(5, L.string(R.string.tutorial_hud_36));
	}

	private void initLine_37() {
		final TutorialLine line = addEmptyLine().setUnskippable();
		line.setUpdateMethod(deltaTime -> {
			if (!flight.getInGameManager().isDockingComputerActive()) {
				flight.getInGameManager().toggleDockingComputer(false);
			}
			if (flight.getInGameManager().getActualPostDockingScreen() == null) {
				flight.getInGameManager().setPostDockingScreen(new TutorialSelectionScreen());
			}
			if (flight.getPostDockingHook() == null) {
				flight.setPostDockingHook(deltaTime1 -> dispose());
			}
		});
	}

	@Override
	public void activate() {
		super.activate();
		setInitialConditions();

		flight.activate();
		flight.getInGameManager().setPlayerControl(false);
		flight.getInGameManager().getShip().setSpeed(0);
		flight.setPause(false);
	}


	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
		flight.saveScreenState(dos);
		dos.writeInt(currentLineIndex - 1);
		super.saveScreenState(dos);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		flight.loadAssets();
	}

	@Override
	public void renderGlPart(float deltaTime, final Rect visibleArea) {
		if (flight != null) {
			flight.initializeGl(visibleArea);
			flight.present(deltaTime);
		}
	}

	@Override
	public void doPresent(float deltaTime) {
		renderText();
	}

	public void doUpdate(float deltaTime) {
		if (flight != null) {
			if (!flight.getInGameManager().isDockingComputerActive()) {
				game.getCobra().setRotation(0, 0);
			}
			flight.update(deltaTime);
		}
	}

	@Override
	public void dispose() {
		if (flight != null) {
			if (!flight.isDisposed()) {
				flight.dispose();
			}
			flight = null;
		}
		super.dispose();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_HUD_SCREEN;
	}
}
