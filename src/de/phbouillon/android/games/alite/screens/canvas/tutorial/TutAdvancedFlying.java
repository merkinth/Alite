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

import android.opengl.GLES11;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.opengl.HyperspaceScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ScoopCallback;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
// TODO: pull common code base of classes TutBasicFlying and TutAdvancedFlying to new super class TutorialFlying
public class TutAdvancedFlying extends TutorialScreen {
	private FlightScreen flight;
	private HyperspaceScreen hyperspace;
	private TutAdvancedFlying switchScreen = null;
	private final Timer timer = new Timer().setAutoResetWithSkipFirstCall();
	private SpaceObject adder = null;
	private boolean scooped;

	TutAdvancedFlying(int lineIndex) {
		this();
		currentLineIndex = lineIndex - 1;
		flight = new FlightScreen(currentLineIndex == -1);
		AliteLog.d("TutAdvancedFlying", "Starting Advanced Flying: " + lineIndex);
	}

	private TutAdvancedFlying() {
		super(true);

		game.getCobra().clearEquipment();
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(SystemData.LAVE_SYSTEM_INDEX));
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(SystemData.ZAONCE_SYSTEM_INDEX));
		game.getCobra().setFuel(70);
		game.getPlayer().setLegalValue(0);
		game.getPlayer().setCondition(Condition.GREEN);
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
	}

	public TutAdvancedFlying(DataInputStream dis) throws IOException, ClassNotFoundException {
		this();
		boolean isFlight = dis.readBoolean();
		boolean isHyper = dis.readBoolean();
		flight = isFlight ? FlightScreen.createScreen(dis) : null;
		hyperspace = isHyper ? new HyperspaceScreen(dis) : null;
		int lineIndex = dis.readInt();
		AliteLog.d("TutAdvancedFlying", "Starting Advanced Flying: " + lineIndex);
		currentLineIndex = lineIndex - 1;
		timer.setTimer(dis.readLong());
		adder = (SpaceObject) flight.findObjectById("Adder");
		if (adder != null) {
			adder.setSaving(false);
		}
		loadScreenState(dis);
	}

	private TutorialLine addTopLine(String text) {
		return addLine(7, text).setX(250).setWidth(1420).setY(20).setHeight(140);
	}

	private void initLine_00() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_00)).setUpdateMethod(deltaTime -> {
			flight.getInGameManager().getShip().setSpeed(0);
			flight.getInGameManager().setPlayerControl(false);
		});
	}

	private void initLine_01() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_01));
	}

	private void initLine_02() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_advanced_flying_02)).
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
					hyperspace = new HyperspaceScreen(0);
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
					switchScreen = new TutAdvancedFlying(3);
					line.setFinished();
				});
			}
		});
	}

	private void initLine_03() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_03));
	}

	private void initLine_04() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_advanced_flying_04)).setHeight(180).
			setUnskippable().setMustRetainEvents();

		line.setUpdateMethod(deltaTime -> {
			overrideControlButtons(true);
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
		overrideControlButtons(false);
	}

	private void setPlayerControlOn() {
		if (flight != null && !flight.getInGameManager().isPlayerControl()) {
			flight.getInGameManager().calibrate();
			flight.getInGameManager().setPlayerControl(true);
			flight.setHandleUI(true);
		}
	}

	private void overrideControlButtons(boolean override) {
		AliteButtons.OVERRIDE_HYPERSPACE = override;
		AliteButtons.OVERRIDE_INFORMATION = override;
		AliteButtons.OVERRIDE_MISSILE = override;
		AliteButtons.OVERRIDE_LASER = override;
		AliteButtons.OVERRIDE_TORUS = override;
	}

	private void initLine_05() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_advanced_flying_05)).setUnskippable();
		line.setMustRetainEvents().setUpdateMethod(deltaTime -> {
			overrideControlButtons(true);
			setPlayerControlOn();
			if (flight.getInGameManager().getShip().getSpeed() <= -PlayerCobra.MAX_SPEED) {
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().setPlayerControl(false);
			flight.setHandleUI(false);
			overrideControlButtons(false);
		});
	}

	private void initLine_06() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_06));
	}

	private void initLine_07() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_07)).addHighlight(
						makeHighlight(1560, 150, 200, 200));
	}

	private void initLine_08() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_advanced_flying_08)).
			setMustRetainEvents().
			setUnskippable().
			addHighlight(makeHighlight(1560, 150, 200, 200));

		line.setUpdateMethod(deltaTime -> {
			overrideControlButtons(true);
			AliteButtons.OVERRIDE_TORUS = false;
			setPlayerControlOn();
			if (flight.getInGameManager().getShip().getSpeed() < -PlayerCobra.TORUS_TEST_SPEED) {
				setPlayerControlOff();
				if (timer.hasPassedSeconds(5)) {
					flight.getInGameManager().getSpawnManager().leaveTorus();
					line.setFinished();
				}
			}
		}).setFinishHook(deltaTime -> {
			// leaveTorus enables player control but does not change UI handler
			flight.setHandleUI(true);
		});
	}

	private void initLine_09() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_09));
	}

	private void initLine_10() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_10));
	}

	private void initLine_11() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_11));
	}

	private void initLine_12() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_12));
	}

	private void initLine_13() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_13));
	}

	private void initLine_14() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_14));
	}

	private void initLine_15() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_15));
	}

	private void initLine_16() {
		final TutorialLine line = addEmptyLine().setMustRetainEvents().setUnskippable();

		line.setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = true;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = false;
			AliteButtons.OVERRIDE_LASER = false;
			AliteButtons.OVERRIDE_TORUS = true;
			setPlayerControlOn();
			if (adder == null) {
				adder = (SpaceObject) flight.findObjectById("Adder");
				if (adder == null) {
					SoundManager.play(Assets.com_conditionRed);
					flight.getInGameManager().repeatMessage(L.string(R.string.com_condition_red), 3);
					Vector3f spawnPosition = flight.getInGameManager().getSpawnManager().getSpawnPosition();
					adder = SpaceObjectFactory.getInstance().getObjectById("adder");
					adder.getRepoHandler().setProperty(SpaceObject.Property.aggression_level, 4L);
					adder.getRepoHandler().setProperty(SpaceObject.Property.missiles, 0L);
					adder.setCargoCanisterCount(2);
					flight.getInGameManager().getSpawnManager().spawnEnemyAndAttackPlayer(adder, 0, spawnPosition);
					adder.addDestructionCallback(3, deltaTime1 -> line.setFinished());
				}
			}
			if (adder.getDestructionCallbacks().isEmpty()) {
				AliteLog.d("Adder DC is empty", "Adder DC is empty");
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
		addTopLine(L.string(R.string.tutorial_advanced_flying_17))
		.setFinishHook(deltaTime -> {
			if (scooped) currentLineIndex = 23;
			flight.getInGameManager().setScoopCallback(null);
		});
	}

	private void initLine_18() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_18));
	}

	private void initLine_19() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_19)).setHeight(180);
	}

	private void initLine_20() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_20)).setHeight(180);
	}

	private void initLine_21() {
		final TutorialLine line =
			addEmptyLine().setUnskippable().setMustRetainEvents();

		line.setUpdateMethod(deltaTime -> {
			overrideControlButtons(true);
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
			addTopLine(L.string(R.string.tutorial_advanced_flying_22)).setUnskippable();
		line.setUpdateMethod(deltaTime -> {
			if (line.getCurrentSpeechIndex() > 0) {
				line.setFinished();
				if (flight.findObjectById("Cargo Canister") != null) currentLineIndex = 19;
				else currentLineIndex++;
			}
		});
	}

	private void initLine_23() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_23));
	}

	private void initLine_24() {
		addTopLine(L.string(R.string.tutorial_advanced_flying_24));
	}

	private void initLine_25() {
		addEmptyLine().setUnskippable().setMustRetainEvents()
		.setUpdateMethod(deltaTime -> {
			overrideControlButtons(true);
			AliteButtons.OVERRIDE_TORUS = false;
			setPlayerControlOn();
			if (flight.getPostDockingHook() == null) {
				flight.setPostDockingHook(deltaTime1 -> {
					overrideControlButtons(false);
					dispose();
				});
			}
			if (flight.getInGameManager().getActualPostDockingScreen() == null) {
				flight.getInGameManager().setPostDockingScreen(new TutorialSelectionScreen());
			}
		});
	}

	@Override
	public void activate() {
		super.activate();
		Settings.resetButtonPosition();
		game.getCobra().clearEquipment();
		game.getCobra().addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP));
		game.getCobra().setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.get().getEquipmentById(EquipmentStore.PULSE_LASER));
		game.getCobra().setLaser(PlayerCobra.DIR_RIGHT, null);
		game.getCobra().setLaser(PlayerCobra.DIR_REAR, null);
		game.getCobra().setLaser(PlayerCobra.DIR_LEFT, null);
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(SystemData.LAVE_SYSTEM_INDEX));
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(SystemData.ZAONCE_SYSTEM_INDEX));
		game.getPlayer().setCondition(Condition.GREEN);
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
	public void renderGlPart(float deltaTime) {
		if (hyperspace != null) {
			hyperspace.initializeGl();
			hyperspace.present(deltaTime);
			if (flight != null) {
				flight.dispose();
				flight = null;
			}
		}
		if (flight != null) {
			flight.initializeGl();
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
