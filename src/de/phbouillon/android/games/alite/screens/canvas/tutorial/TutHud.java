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
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
@SuppressWarnings("serial")
public class TutHud extends TutorialScreen {
	private FlightScreen flight;

	TutHud(final Alite alite) {
		this(alite, null);
	}

	private TutHud(final Alite alite, final FlightScreen flight) {
		super(alite, true);

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

	private void setInitialConditions() {
		for (int i = 0; i < Settings.buttonPosition.length; i++) {
			Settings.buttonPosition[i] = i;
		}
		game.getCobra().clearEquipment();
		game.getCobra().setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.pulseLaser);
		game.getCobra().setLaser(PlayerCobra.DIR_RIGHT, null);
		game.getCobra().setLaser(PlayerCobra.DIR_REAR, null);
		game.getCobra().setLaser(PlayerCobra.DIR_LEFT, null);
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(7)); // Lave
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(129)); // Zaonce
		game.getCobra().setFuel(70);
	}

	private TutorialLine addTopLine(String text) {
		return addLine(5, text).setX(250).setWidth(1420).setY(20).setHeight(140);
	}

	private void initLine_00() {
		addTopLine("Welcome Back, 'Commander'. Well? How does it feel to be " +
				"in the Cockpit of a Cobra, hm?").
				setUpdateMethod(deltaTime -> {
					flight.getInGameManager().getShip().setSpeed(0);
					flight.getInGameManager().setPlayerControl(false);
				});
		if (flight == null) {
			flight = new FlightScreen(game, true);
			flight.setHandleUI(false);
		}
	}

	private void initLine_01() {
		addTopLine("Don't worry, rookie, you are in the safe zone around " +
				"Lave space station, no one will attack you here.");
	}

	private void initLine_02() {
		addTopLine("The safe zone is a zone around a space station where " +
				"the police Vipers will watch over you.");
	}

	private void initLine_03() {
		addTopLine("You can see that you have reached a safe zone by the " +
				"large S in the bottom right corner of your radar.").
				addHighlight(makeHighlight(1284, 935, 128, 128));
	}

	private void initLine_04() {
		addTopLine("So now you can relax and enjoy the view of Lave right " +
				"in front of you. Isn't it beautiful?");
	}

	private void initLine_05() {
		addLine(5, "If you want to look around, you can change the display " +
				"to show the right, rear, or left view out of your ship. " +
				"To look right, "
				+ (Settings.tapRadarToChangeView ?
					"tap the right half of your radar." :
					"swipe your finger from right to left over the screen."),
					Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(180);
	}

	private void initLine_06() {
		final TutorialLine line = addTopLine("Why don't you try it?");

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
				addLine(5, "No. Look to your right. Remember: "
				+ (Settings.tapRadarToChangeView ?
					"tap the right half of your radar." :
					"swipe your finger from right to left over the screen."),
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
		addLine(5, "Yes. Just like that. You can now see that the green " +
				"indicator of your radar is on its right half: You are " +
				"looking right. To look behind you, "
				+ (Settings.tapRadarToChangeView ?
					"tap the bottom half of your radar." :
					"swipe your finger from right to left over the screen again."),
					Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(180);
	}

	private void initLine_09() {
		final TutorialLine line = addTopLine("Go ahead, try it...");

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
		final TutorialLine line =
				addLine(5, "No. Look behind you. Remember: "
				+ (Settings.tapRadarToChangeView ?
					"tap the bottom half of your radar." :
					"swipe your finger from right to left over the screen."),
					Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(180);

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
		addTopLine("Good. You are now looking at the Coriolis station in " +
				"orbit around Lave. That's where you'll eventually have to " +
				"dock, but we'll keep that for a later lesson.").
				setHeight(180);
	}

	private void initLine_12() {
		final TutorialLine line = addTopLine("You can look left, too, I " +
				"suppose you know how to do that on your own, don't you? Go " +
				"ahead, look left...");

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
		final TutorialLine line =
				addLine(5, "No. To look left, "
				+ (Settings.tapRadarToChangeView ?
					"tap the left half of your radar." :
					"swipe your finger from right to left over the screen again."),
					Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(180);

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
		addTopLine("Ok, so now you can look around. Good. The green area on " +
				"your radar tells you in which direction you are looking, " +
				"and the purple bar on the radar is the space station.").
				setHeight(180);
	}

	private void initLine_15() {
		addTopLine("It is now almost in the center of the radar, which " +
				"means that you are very close to it. In fact, the space " +
				"station is a little behind you. You can see that more " +
				"clearly, if you increase the zoom factor of the radar.").
				setHeight(180);
	}

	private void initLine_16() {
		final TutorialLine line =
				addLine(5, "To do that "
				+ (Settings.tapRadarToChangeView ?
					"tap the green area on your radar." :
					"tap the radar."),
					Settings.tapRadarToChangeView ? "a" : "b").
				setX(250).setWidth(1420).setY(20).setHeight(140);

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
		addTopLine("Good. You can enhance the zoom factor even more, by " +
				"tapping again. If you tap once more, the zoom factor will " +
				"be back to normal.");
	}

	private void initLine_18() {
		addTopLine("We will get back to the radar in more detail in the " +
				"next lesson, for now, let's focus on the gauges you see " +
				"next to your radar.").setUpdateMethod(deltaTime -> {
					if (flight.getInGameManager().getViewDirection() != 0) {
						flight.getInGameManager().setViewport(0);
					}
				});
	}

	private void initLine_19() {
		addTopLine("On the left hand side, you have your front and aft " +
				"shield gauges.").addHighlight(
						makeHighlight(150, 700, 350, 76));
	}

	private void initLine_20() {
		addTopLine("When they are depleted, the next laser hits are going " +
				"to damage your hull and the energy bars on the lower " +
				"right, numbered 1 through 4 are affected.").setHeight(180).
				addHighlight(makeHighlight(1420, 845, 350, 156));
	}

	private void initLine_21() {
		addTopLine("If you lose your last bit of energy, the Cobra is " +
				"destroyed, so you want to watch out who's in the vicinity, " +
				"kiddo.");
	}

	private void initLine_22() {
		addTopLine("Below the aft shield on the left hand side is the fuel " +
				"gauge. It only depletes in hyperspace travel and tells you " +
				"how far you can jump.").addHighlight(
						makeHighlight(150, 805, 350, 36));
	}

	private void initLine_23() {
		addTopLine("Next is the cabin temperature meter. If you go fuel " +
				"scooping near the sun, you want to watch that gauge " +
				"closely. If it gets too hot, your Cobra will explode.").
				setHeight(180).addHighlight(makeHighlight(150, 845, 350, 36));
	}

	private void initLine_24() {
		addTopLine("Next is the laser temperature. If you fire too much, " +
				"the laser becomes too hot and you will have to wait " +
				"until it cools down again.").
				addHighlight(makeHighlight(150, 885, 350, 36));
	}

	private void initLine_25() {
		addTopLine("Below that, there is the altitude meter. It shows, how " +
				"high you are above the planet. Do not try to land on alien " +
				"planets, kiddo, the locals don't appreciate that.").
				setHeight(180).addHighlight(makeHighlight(150, 925, 350, 36));
	}

	private void initLine_26() {
		addTopLine("Finally, at the bottom of the left hand side is your " +
				"missile indicator. Your Cobra has four missile slots. If a " +
				"slot is empty, the circle representing the slot is black.").
				setHeight(180).addHighlight(makeHighlight(150, 990, 350, 38));
	}

	private void initLine_27() {
		addTopLine("If a missile is in the slot, the circle is green, if " +
				"the missile is targetting, it is yellow and if it is " +
				"locked, it becomes red.").
				addHighlight(makeHighlight(150, 990, 350, 38));
	}

	private void initLine_28() {
		addTopLine("On the right hand side, you can see your current speed, " +
				"your current roll, and your current pitch.").
				addHighlight(makeHighlight(1420, 700, 350, 116));
	}

	private void initLine_29() {
		addLine(5, "Did you wonder about the four buttons on the top of " +
				"your screen? We won't touch them right now, but let me " +
				"tell you what they do:").
				addHighlight(makeHighlight(0, 0, 350, 350)).
				addHighlight(makeHighlight(1710, 0, 200, 500));
	}

	private void initLine_30() {
		addLine(5, "The button on the top left fires your laser. "
				+ (Settings.laserButtonAutoFire ?
					"Tap it once and the laser starts shooting. Tap it " +
					"again and the laser stops." :
					"Tap it once to shoot."),
					Settings.laserButtonAutoFire ? "a" : "b").
				addHighlight(makeHighlight(0, 0, 200, 200));
	}

	private void initLine_31() {
		addLine(5, "Of course, this only works if you have a laser equipped " +
				"in the current view.").addHighlight(
						makeHighlight(0, 0, 200, 200));
	}

	private void initLine_32() {
		addLine(5, "The other button on the left lets you target a " +
				"missile. Whatever target will be in your crosshairs, will " +
				"be targetted. Tap the button again to fire a locked " +
				"missile.").addHighlight(makeHighlight(150, 150, 200, 200));
	}

	private void initLine_33() {
		addLine(5, "The upper button on the right hand side will take you " +
				"to the station interface, you can use that to select a " +
				"hyperspace target or check local market prices.").
				addHighlight(makeHighlight(1710, 0, 200, 200));
	}

	private void initLine_34() {
		addLine(5, "Of course, you can't sell or buy anything while in space. You " +
				"need to dock first.").addHighlight(
					makeHighlight(1710, 0, 200, 200));
	}

	private void initLine_35() {
		addLine(5, "The lower button on the right takes you to hyperspace " +
				"and you will get out at your selected destination -- " +
				"unless you are hijacked by Thargoids, that is.").
				addHighlight(makeHighlight(1710, 300, 200, 200));
	}

	private void initLine_36() {
		addLine(5, "Some pretty heavy stuff, hm? I bet you have a lot " +
				"to think about. So let me dock this ship for you now " +
				"and you can then rest a little and come back when " +
				"you're ready for more...");
	}

	private void initLine_37() {
		final TutorialLine line = addEmptyLine().setUnskippable();
		line.setUpdateMethod(deltaTime -> {
			if (!flight.getInGameManager().isDockingComputerActive()) {
				flight.getInGameManager().toggleDockingComputer(false);
			}
			if (flight.getInGameManager().getActualPostDockingScreen() == null) {
				flight.getInGameManager().setPostDockingScreen(new TutorialSelectionScreen(game));
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


	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			TutHud th = new TutHud(alite, FlightScreen.createScreen(alite, dis));
			th.currentLineIndex = dis.readInt();
			th.loadScreenState(dis);
			alite.setScreen(th);
		} catch (IOException | ClassNotFoundException e) {
			AliteLog.e("Tutorial HUD Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
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
