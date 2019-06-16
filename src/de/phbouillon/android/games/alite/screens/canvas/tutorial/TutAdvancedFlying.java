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
import android.opengl.GLES11;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.screens.opengl.HyperspaceScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ScoopCallback;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Adder;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
@SuppressWarnings("serial")
// TODO: pull common code base of classes TutBasicFlying and TutAdvancedFlying to new super class TutorialFlying
public class TutAdvancedFlying extends TutorialScreen {
	private FlightScreen flight;
	private HyperspaceScreen hyperspace;
	private TutAdvancedFlying switchScreen = null;
	private final Timer timer = new Timer().setAutoResetWithSkipFirstCall();
	private Adder adder = null;
	private boolean scooped;

	TutAdvancedFlying(final Alite alite, int lineIndex) {
		this(alite, lineIndex, null);
	}

	private TutAdvancedFlying(final Alite alite, int lineIndex, FlightScreen flight) {
		super(alite, true);

		this.flight = flight;
		AliteLog.d("TutAdvancedFlying", "Starting Advanced Flying: " + lineIndex);

		alite.getCobra().clearEquipment();
		alite.getGenerator().buildGalaxy(1);
		alite.getGenerator().setCurrentGalaxy(1);
		alite.getPlayer().setCurrentSystem(alite.getGenerator().getSystem(7)); // Lave
		alite.getPlayer().setHyperspaceSystem(alite.getGenerator().getSystem(129)); // Zaonce
		alite.getCobra().setFuel(70);
		alite.getPlayer().setLegalValue(0);
		Settings.resetButtonPosition();

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

		currentLineIndex = lineIndex - 1;
		if (this.flight == null) {
			this.flight = new FlightScreen(alite, currentLineIndex == -1);
		}
	}

	private TutorialLine addTopLine(String text) {
		return addLine(7, text).setX(250).setWidth(1420).setY(20).setHeight(140);
	}

	private void initLine_00() {
		addTopLine("Welcome back, Commander! A fair landing you produced " +
				"there. I'm proud of you!").setUpdateMethod(deltaTime -> {
					flight.getInGameManager().getShip().setSpeed(0);
					flight.getInGameManager().setPlayerControl(false);
				});
	}

	private void initLine_01() {
		addTopLine("You are now ready for more advanced controls, but we " +
				"need to be out in space for those.");
	}

	private void initLine_02() {
		final TutorialLine line =
			addTopLine("Therefore, when you activate the hyperspace, you will " +
				"reach a simulated space region a little off of Lave. Go " +
				"there, now. Activate the hyperspace drive.").
			setMustRetainEvents().addHighlight(makeHighlight(1710, 300, 200, 200)).
			setUnskippable();

		line.setHeight(180).setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = false;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = true;
			AliteButtons.OVERRIDE_LASER = true;
			setPlayerControlOn();

			if (flight != null && flight.getInGameManager().getHyperspaceHook() == null) {
				flight.getInGameManager().setHyperspaceHook(deltaTime1 -> {
					hyperspace = new HyperspaceScreen(game, false);
					hyperspace.setNeedsSoundRestart();
					hideCloseButton = true;
					line.clearHighlights();
					line.setText("");
					line.setWidth(0);
					line.setHeight(0);
					hyperspace.activate();
				});
			}
			if (hyperspace != null && hyperspace.getFinishHook() == null) {
				hyperspace.setNeedsSoundRestart();
				hideCloseButton = true;
				line.clearHighlights();
				line.setText("");
				line.setWidth(0);
				line.setHeight(0);
				hyperspace.setFinishHook(deltaTime1 -> {
					hyperspace.dispose();
					hyperspace = null;
					hideCloseButton = false;
					GLES11.glMatrixMode(GLES11.GL_TEXTURE);
					GLES11.glLoadIdentity();
					game.getTextureManager().clear();
					switchScreen = new TutAdvancedFlying(game, 3);
					line.setFinished();
				});
			}
		});
	}

	private void initLine_03() {
		addTopLine("You have now reached a space region outside Lave, you " +
				"can see clearly that you are outside the safe zone.");
	}

	private void initLine_04() {
		final TutorialLine line =
			addTopLine("Now, in order to reach Lave station, you have to fly " +
				"towards the planet. So bring the circle in the small radar " +
				"to your front. Remember: The circle is red once the planet " +
				"is in front of you.").setHeight(180).
			setUnskippable().setMustRetainEvents();

		line.setUpdateMethod(deltaTime -> {
			enableControlButtons(true);
			setPlayerControlOn();
			if (flight.getInGameManager().isTargetInCenter()) {
				SoundManager.play(Assets.identify);
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> setPlayerControlOff());
	}

	private void setPlayerControlOff() {
		flight.getInGameManager().setPlayerControl(false);
		flight.getInGameManager().getShip().adjustSpeed(0);
		flight.getInGameManager().setNeedsSpeedAdjustment(true);
		flight.setHandleUI(false);
		enableControlButtons(false);
	}

	private void setPlayerControlOn() {
		if (flight != null && !flight.getInGameManager().isPlayerControl()) {
			flight.getInGameManager().calibrate();
			flight.getInGameManager().setPlayerControl(true);
			flight.setHandleUI(true);
		}
	}

	private void enableControlButtons(boolean enable) {
		AliteButtons.OVERRIDE_HYPERSPACE = enable;
		AliteButtons.OVERRIDE_INFORMATION = enable;
		AliteButtons.OVERRIDE_MISSILE = enable;
		AliteButtons.OVERRIDE_LASER = enable;
	}

	private void initLine_05() {
		final TutorialLine line =
				addTopLine("Good. Now accelerate to maximum speed by " +
				"sliding your finger up.").setUnskippable();
		line.setMustRetainEvents().setUpdateMethod(deltaTime -> {
			enableControlButtons(true);
			setPlayerControlOn();
			if (game.getCobra().getSpeed() <= -PlayerCobra.MAX_SPEED) {
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().setPlayerControl(false);
			flight.setHandleUI(false);
			enableControlButtons(false);
		});
	}

	private void initLine_06() {
		addTopLine("Ok. If you traveled at this speed, it would take " +
				"forever to reach Lave, but did you notice that a new " +
				"button appeared once you reached maximum speed?");
	}

	private void initLine_07() {
		addTopLine("This is the torus drive button and it will allow you to " +
				"utilize the engine boosters.").addHighlight(
						makeHighlight(1560, 150, 200, 200));
	}

	private void initLine_08() {
		final TutorialLine line =
			addTopLine("It only works, however, if you are already flying " +
					"with maximum speed and no other ships are in the " +
					"vicinity. Try it. Activate the torus drive.").
			setMustRetainEvents().
			setUnskippable().
			addHighlight(makeHighlight(1560, 150, 200, 200));

		line.setUpdateMethod(deltaTime -> {
			enableControlButtons(true);
			AliteButtons.OVERRIDE_TORUS = false;
			setPlayerControlOn();
			if (game.getCobra().getSpeed() < -PlayerCobra.TORUS_TEST_SPEED) {
				if (timer.hasPassedSeconds(5)) {
					flight.getInGameManager().getSpawnManager().leaveTorus();
					line.setFinished();
				}
			}
		}).setFinishHook(deltaTime -> {
			setPlayerControlOff();
			AliteButtons.OVERRIDE_TORUS = false;
		});
	}

	private void initLine_09() {
		addTopLine("Wow... That was fast, wasn't it? Why did we stop?");
	}

	private void initLine_10() {
		addTopLine("Usually the torus drive is interrupted, if you are too " +
				"close to the planet or the star, or an enemy ship appears " +
				"waiting to get a shot at you.");
	}

	private void initLine_11() {
		addTopLine("In this simulation, I killed the torus drive.");
	}

	private void initLine_12() {
		addTopLine("And now, you're about to make a kill. An enemy ship " +
				"will appear soon, and I want you to destroy it.");
	}

	private void initLine_13() {
		addTopLine("It will attack you, but it is a relatively harmless and " +
				"slow Adder. There is a slim chance, though, that it might " +
				"destroy you.");
	}

	private void initLine_14() {
		addTopLine("In that case: You pay the drinks tonight and we'll try " +
				"again tomorrow.");
	}

	private void initLine_15() {
		addTopLine("Ready? Good. Go!");
	}

	private void initLine_16() {
		final TutorialLine line = addEmptyLine().setMustRetainEvents().
			setUnskippable();

		line.setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = true;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = false;
			AliteButtons.OVERRIDE_LASER = false;
			AliteButtons.OVERRIDE_TORUS = true;
			setPlayerControlOn();
			if (adder == null) {
				adder = (Adder) flight.findObjectByName("Adder");
				if (adder == null) {
					SoundManager.play(Assets.com_conditionRed);
					flight.getInGameManager().repeatMessage("Condition Red!", 3);
					Vector3f spawnPosition = flight.getInGameManager().getSpawnManager().getSpawnPosition();
					adder = new Adder(game);
					adder.setAggression(4);
					adder.setMissileCount(0);
					adder.setCargoCanisterCount(2);
					flight.getInGameManager().getSpawnManager().spawnEnemyAndAttackPlayer(adder, 0, spawnPosition);
					adder.addDestructionCallback(3, deltaTime1 -> line.setFinished());
				}
			}
			if (adder.getDestructionCallbacks().isEmpty()) {
				AliteLog.e("Adder DC is empty", "Adder DC is empty");
				adder.addDestructionCallback(4, deltaTime1 -> line.setFinished());
			}
			setScoopNotifier(line);
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().getLaserManager().setAutoFire(false);
			setPlayerControlOff();
			AliteButtons.OVERRIDE_TORUS = false;
		});
	}

	private void initLine_17() {
		addTopLine("Hey not bad... You destroyed the Adder. And it released " +
			"a couple of Cargo Canisters. You can see them on the radar as purple bars.")
		.setFinishHook(deltaTime -> {
			if (scooped) currentLineIndex = 23;
			flight.getInGameManager().setScoopCallback(null);
		});
	}

	private void initLine_18() {
		addTopLine("Remember: If you tap on a target on your screen, it " +
				"will be identified by the computer.");
	}

	private void initLine_19() {
		addTopLine("Your simulated Cobra is equipped with a fuel scoop, so " +
				"you may collect those Cargo Canisters and their contents " +
				"are then added to your inventory.").setHeight(180);
	}

	private void initLine_20() {
		addTopLine("To successfully do that, ram a Cargo Canister with " +
				"the lower part of your Cobra, where the Fuel Scoop is " +
				"installed. Go ahead, now.").setHeight(180);
	}

	private void initLine_21() {
		final TutorialLine line =
			addEmptyLine().setUnskippable().setMustRetainEvents();

		line.setUpdateMethod(deltaTime -> {
			enableControlButtons(true);
			AliteButtons.OVERRIDE_TORUS = true;
			setPlayerControlOn();
			setScoopNotifier(line);
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().getLaserManager().setAutoFire(false);
			setPlayerControlOff();
			AliteButtons.OVERRIDE_TORUS = false;
			if (scooped) currentLineIndex++;
		});
	}

	private void setScoopNotifier(TutorialLine line) {
		if (flight.getInGameManager().getScoopCallback() != null) {
			return;
		}
		flight.getInGameManager().setScoopCallback(new ScoopCallback() {
			@Override
			public void scooped(SpaceObject scoopedObject) {
				scooped = true;
				line.setFinished();
			}

			@Override
			public void rammed(SpaceObject rammedObject) {
				line.setFinished();
			}
		});
	}

	private void initLine_22() {
		final TutorialLine line =
			addTopLine("Oh no. You have destroyed the Cargo Canister. " +
			"Remember: You must touch it with the lower part of your Cobra.").setUnskippable();
		line.setUpdateMethod(deltaTime -> {
			if (line.getCurrentSpeechIndex() > 0) {
				line.setFinished();
				if (flight.findObjectByName("Cargo Canister") != null) currentLineIndex = 19;
				else currentLineIndex++;
			}
		});
	}

	private void initLine_23() {
		addTopLine("Excellent flying, Commander.");
	}

	private void initLine_24() {
		addTopLine("This is all I can teach you, really. As a last training " +
				"exercise, dock at Lave station. You are on your own. Good " +
				"luck out there, Commander.");
	}

	private void initLine_25() {
		addEmptyLine().setUnskippable().setMustRetainEvents()
		.setUpdateMethod(deltaTime -> {
			enableControlButtons(true);
			AliteButtons.OVERRIDE_TORUS = false;
			setPlayerControlOn();
			if (flight.getPostDockingHook() == null) {
				flight.setPostDockingHook(deltaTime1 -> {
					enableControlButtons(false);
					AliteButtons.OVERRIDE_TORUS = false;
					dispose();
				});
			}
			if (flight.getInGameManager().getActualPostDockingScreen() == null) {
				flight.getInGameManager().setPostDockingScreen(new TutorialSelectionScreen(game));
			}
		});
	}

	@Override
	public void activate() {
		super.activate();
		Settings.resetButtonPosition();
		game.getCobra().clearEquipment();
		game.getCobra().addEquipment(EquipmentStore.fuelScoop);
		game.getCobra().setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.pulseLaser);
		game.getCobra().setLaser(PlayerCobra.DIR_RIGHT, null);
		game.getCobra().setLaser(PlayerCobra.DIR_REAR, null);
		game.getCobra().setLaser(PlayerCobra.DIR_LEFT, null);
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(7)); // Lave
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(129)); // Zaonce
		game.getCobra().setFuel(70);
		game.getCobra().setMissiles(4);
		game.getCobra().clearInventory();

		if (flight != null) {
			flight.loadAssets();
			flight.activate();
			flight.getInGameManager().setPlayerControl(false);
			flight.getInGameManager().getShip().setSpeed(0);
			flight.setPause(false);
		}
		if (hyperspace != null) {
			hyperspace.loadAssets();
			hyperspace.activate();
		}

		Settings.disableTraders = true;
		Settings.disableAttackers = true;
		ObjectSpawnManager.SHUTTLES_ENABLED = false;
		ObjectSpawnManager.ASTEROIDS_ENABLED = false;
		ObjectSpawnManager.CONDITION_RED_OBJECTS_ENABLED = false;
		ObjectSpawnManager.THARGOIDS_ENABLED = false;
		ObjectSpawnManager.THARGONS_ENABLED = false;
		ObjectSpawnManager.TRADERS_ENABLED = false;
		ObjectSpawnManager.VIPERS_ENABLED = false;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			boolean flight = dis.readBoolean();
			boolean hyper = dis.readBoolean();
			FlightScreen fs = flight ? FlightScreen.createScreen(alite, dis) : null;
			HyperspaceScreen hs = hyper ? HyperspaceScreen.createScreen(alite, dis) : null;
			TutAdvancedFlying ta = new TutAdvancedFlying(alite, dis.readInt(), fs);
			ta.hyperspace = hs;
			ta.timer.setTimer(dis.readLong());
			ta.savedMarketFluct = dis.readInt();
			ta.adder = (Adder) ta.flight.findObjectByName("Adder");
			if (ta.adder != null) {
				ta.adder.setSaving(false);
			}
			ta.loadScreenState(dis);
			alite.setScreen(ta);
		} catch (IOException | ClassNotFoundException e) {
			AliteLog.e("Tutorial Advanced Flying Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
		if (adder != null) {
			adder.setSaving(true);
		}
		dos.writeBoolean(flight != null);
		dos.writeBoolean(hyperspace != null);
		if (flight != null) {
			flight.saveScreenState(dos);
		}
		if (hyperspace != null) {
			hyperspace.saveScreenState(dos);
		}
		dos.writeInt(currentLineIndex); // 1 is subtracted in the read object code, because it has to be passed to the constructor.
		dos.writeLong(timer.getTimer());
		super.saveScreenState(dos);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		flight.loadAssets();
	}

	@Override
	public void renderGlPart(float deltaTime, final Rect visibleArea) {
		if (hyperspace != null) {
			hyperspace.initializeGl(visibleArea);
			hyperspace.present(deltaTime);
			if (flight != null) {
				flight.dispose();
				flight = null;
			}
		}
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
		if (hyperspace != null) {
			hyperspace.update(deltaTime);
			if (flight != null) {
				flight.dispose();
				flight = null;
			}
		}
		if (flight != null) {
			if (!flight.getInGameManager().isDockingComputerActive()) {
				game.getCobra().setRotation(0, 0);
			}
			flight.update(deltaTime);
		}
		if (switchScreen != null) {
			newScreen = switchScreen;
			performScreenChange();
			postScreenChange();
		}
	}

	@Override
	public void dispose() {
		if (hyperspace != null) {
			hyperspace.dispose();
			hyperspace = null;
		}
		if (flight != null) {
			if (!flight.isDisposed()) {
				flight.dispose();
			}
			flight = null;
		}
		ObjectSpawnManager.SHUTTLES_ENABLED = true;
		ObjectSpawnManager.ASTEROIDS_ENABLED = true;
		ObjectSpawnManager.CONDITION_RED_OBJECTS_ENABLED = true;
		ObjectSpawnManager.THARGOIDS_ENABLED = true;
		ObjectSpawnManager.THARGONS_ENABLED = true;
		ObjectSpawnManager.TRADERS_ENABLED = true;
		ObjectSpawnManager.VIPERS_ENABLED = true;
		super.dispose();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_ADVANCED_FLYING_SCREEN;
	}
}
