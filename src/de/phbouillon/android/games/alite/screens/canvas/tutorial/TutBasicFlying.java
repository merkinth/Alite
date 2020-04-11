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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.graphics.Rect;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
public class TutBasicFlying extends TutorialScreen {

	private static final int TUTORIAL_INDEX = 6;
	private static final String YELLOW_TARGET_ID = "yellow_target";
	private static final String BLUE_TARGET_ID = "blue_target";
	private static final String BUOY_ID = "buoy";
	private static final String DOCKING_BUOY_ID = "docking_buoy";

	private FlightScreen flight;
	private boolean resetShipPosition = true;
	private SpaceObject yellowTarget;
	private SpaceObject blueTarget;
	private SpaceObject dockingBuoy;

	protected TutBasicFlying(FlightScreen flight) {
		super(true);
		this.flight = flight;

		ObjectSpawnManager.SHUTTLES_ENABLED = false;
		ObjectSpawnManager.ASTEROIDS_ENABLED = false;
		ObjectSpawnManager.CONDITION_RED_OBJECTS_ENABLED = false;
		ObjectSpawnManager.THARGOIDS_ENABLED = false;
		ObjectSpawnManager.THARGONS_ENABLED = false;
		ObjectSpawnManager.TRADERS_ENABLED = false;
		ObjectSpawnManager.VIPERS_ENABLED = false;

		game.getCobra().clearEquipment();
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(SystemData.LAVE_SYSTEM_INDEX));
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(SystemData.ZAONCE_SYSTEM_INDEX));
		game.getPlayer().setLegalValue(0);
		game.getCobra().setFuel(70);
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
		initLine_26();
		initLine_27();
		initLine_28();
		initLine_29();
		initLine_30();
		initLine_31();
		initLine_32();
		initLine_33();
		initLine_34();
	}

	public TutBasicFlying(DataInputStream dis) throws IOException, ClassNotFoundException {
		this(FlightScreen.createScreen(dis));
		currentLineIndex = dis.readInt();
		resetShipPosition = dis.readBoolean();

		yellowTarget = (SpaceObject) flight.findObjectById(YELLOW_TARGET_ID);
		if (yellowTarget != null) {
			yellowTarget.setSaving(false);
		}
		blueTarget = (SpaceObject) flight.findObjectById(BLUE_TARGET_ID);
		if (blueTarget != null) {
			blueTarget.setSaving(false);
		}
		SpaceObject buoy = (SpaceObject) flight.findObjectById(DOCKING_BUOY_ID);
		if (buoy != null) {
			buoy.setSaving(false);
		}
		loadScreenState(dis);
	}

	private TutorialLine addTopLine(String text) {
		return addLine(TUTORIAL_INDEX, text).setX(250).setWidth(1420).setY(20).setHeight(140);
	}

	private void initLine_00() {
		addTopLine(L.string(R.string.tutorial_basic_flying_00)).
				setUpdateMethod(deltaTime -> {
					flight.getInGameManager().getShip().setSpeed(0);
					flight.getInGameManager().setPlayerControl(false);
				});

		if (flight == null) {
			flight = new FlightScreen(true);
		}
	}

	private void initLine_01() {
		addTopLine(L.string(R.string.tutorial_basic_flying_01))
			.setHeight(180);
	}

	private void initLine_02() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_02));

		line.setFinishHook(deltaTime -> {
			yellowTarget = getBuoy(YELLOW_TARGET_ID, R.string.tutorial_basic_flying_yellow_target,
				new Vector3f(-8000, -11000, 17000), 0xEFEF00);
			blueTarget = getBuoy(BLUE_TARGET_ID, R.string.tutorial_basic_flying_blue_target,
				new Vector3f(8000, 14000, -10000), 0x0000EF);
		});
	}

	private SpaceObject getBuoy(String id, int name, Vector3f relPos, int color) {
		Vector3f position = new Vector3f(0, 0, 0);
		Vector3f vec = new Vector3f(0, 0, 0);

		flight.getInGameManager().getShip().getPosition().copy(position);
		flight.getInGameManager().getShip().getRightVector().copy(vec);
		vec.scale(relPos.x);
		position.add(vec);
		flight.getInGameManager().getShip().getForwardVector().copy(vec);
		vec.scale(relPos.y);
		position.add(vec);
		flight.getInGameManager().getShip().getUpVector().copy(vec);
		vec.scale(relPos.z);
		position.add(vec);

		SpaceObject buoy = SpaceObjectFactory.getInstance().getObjectById(BUOY_ID);
		buoy.setId(id);
		buoy.setProperty(SpaceObject.Property.name, L.string(name));
		buoy.setPosition(position);
		buoy.setHudColor(color);
		buoy.scale(6.0f);
		flight.getInGameManager().addObject(buoy);
		return buoy;
	}

	private void initLine_03() {
		addTopLine(L.string(R.string.tutorial_basic_flying_03));
	}

	private void initLine_04() {
		addTopLine(L.string(R.string.tutorial_basic_flying_04));
	}

	private void initLine_05() {
		addTopLine(L.string(R.string.tutorial_basic_flying_05))
			.addHighlight(makeHighlight(1284, 640, 128, 128));
	}

	private void initLine_06() {
		addTopLine(L.string(R.string.tutorial_basic_flying_06));
	}

	private void initLine_07() {
		addTopLine(L.string(R.string.tutorial_basic_flying_07));
	}

	private void initLine_08() {
		addTopLine(L.string(R.string.tutorial_basic_flying_08))
			.setHeight(180);
	}

	private void initLine_09() {
		addTopLine(L.string(R.string.tutorial_basic_flying_09));
	}

	private void initLine_10() {
		addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_10,
			L.string(Settings.controlMode == ShipControl.ACCELEROMETER ||
			   Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER ?
				R.string.tutorial_basic_flying_10a :
			   Settings.controlMode == ShipControl.CONTROL_PAD ?
				   R.string.tutorial_basic_flying_10b : R.string.tutorial_basic_flying_10c)),
				Settings.controlMode == ShipControl.ACCELEROMETER ||
				Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER ? "a" :
				Settings.controlMode == ShipControl.CONTROL_PAD ? "b" : "c").
			setX(250).setWidth(1420).setY(20).setHeight(220);
	}

	private void initLine_11() {
		addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_11,
			L.string(Settings.controlMode == ShipControl.ACCELEROMETER ||
			   Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER ?
				R.string.tutorial_basic_flying_11a :
			   Settings.controlMode == ShipControl.CONTROL_PAD ?
			   R.string.tutorial_basic_flying_11b : R.string.tutorial_basic_flying_11c)),
				Settings.controlMode == ShipControl.ACCELEROMETER ||
				Settings.controlMode == ShipControl.ALTERNATIVE_ACCELEROMETER ? "a" :
				Settings.controlMode == ShipControl.CONTROL_PAD ? "b" : "c").
			setX(250).setWidth(1420).setY(20).setHeight(220);
	}

	private void initLine_12() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_12))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> targetToCrosshairs(line, blueTarget))
		.setFinishHook(deltaTime -> {
			flight.getInGameManager().setPlayerControl(false);
			setButtons(false);
			InGameManager.OVERRIDE_SPEED = false;
		});
	}

	private void targetToCrosshairs(TutorialLine line, SpaceObject target) {
		setButtons(true);
		InGameManager.OVERRIDE_SPEED = true;
		if (!flight.getInGameManager().isPlayerControl()) {
			flight.getInGameManager().calibrate();
			flight.getInGameManager().setPlayerControl(true);
		}
		if (flight.getInGameManager().getLaserManager().isUnderCross(target,
				flight.getInGameManager().getShip(),
				flight.getInGameManager().getViewDirection())) {
			SoundManager.play(Assets.identify);
			line.setFinished();
		}
	}

	private void setButtons(boolean b) {
		AliteButtons.OVERRIDE_HYPERSPACE = b;
		AliteButtons.OVERRIDE_INFORMATION = b;
		AliteButtons.OVERRIDE_MISSILE = b;
		AliteButtons.OVERRIDE_LASER = b;
	}

	private void initLine_13() {
		addTopLine(L.string(R.string.tutorial_basic_flying_13));
	}

	private void initLine_14() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_14))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> targetToCrosshairs(line, yellowTarget))
		.setFinishHook(deltaTime -> {
			setButtons(false);
			InGameManager.OVERRIDE_SPEED = false;
			flight.getInGameManager().setPlayerControl(false);
		});
	}

	private void initLine_15() {
		addTopLine(L.string(R.string.tutorial_basic_flying_15));
	}

	private void initLine_16() {
		addTopLine(L.string(R.string.tutorial_basic_flying_16));
	}

	private void initLine_17() {
		addTopLine(L.string(R.string.tutorial_basic_flying_17));
	}

	private void initLine_18() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_18))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> approachTarget(line, yellowTarget))
			.setFinishHook(deltaTime -> finishFlight());
	}

	private void approachTarget(TutorialLine line, SpaceObject target) {
		startFlightWithButtons();
		if (target.getPosition().distanceSq(flight.getInGameManager().getShip().getPosition()) < 360000000L) {
			SoundManager.play(Assets.identify);
			line.setFinished();
		}
	}

	private void startFlightWithButtons() {
		setButtons(true);
		startFlight();
	}

	private void startFlight() {
		if (!flight.getInGameManager().isPlayerControl()) {
			flight.getInGameManager().calibrate();
			flight.getInGameManager().setPlayerControl(true);
			flight.setHandleUI(true);
		}
	}

	private void finishFlight() {
		flight.getInGameManager().setPlayerControl(false);
		flight.getInGameManager().getShip().adjustSpeed(0);
		flight.getInGameManager().setNeedsSpeedAdjustment(true);
		flight.setHandleUI(false);
		setButtons(false);
	}

	private void initLine_19() {
		addTopLine(L.string(R.string.tutorial_basic_flying_19));
	}

	private void initLine_20() {
		addTopLine(L.string(R.string.tutorial_basic_flying_20));
	}

	private void initLine_21() {
		final TutorialLine line = addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_21))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = true;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = true;
			AliteButtons.OVERRIDE_LASER = false;
			startFlight();
			if (yellowTarget.getDestructionCallbacks().isEmpty()) {
				yellowTarget.addDestructionCallback(5, (IMethodHook) deltaTime1 -> {
					line.setFinished();
					if (flight.findObjectById(BLUE_TARGET_ID) == null) {
						currentLineIndex+= 5;
					}
				});
			}
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().getLaserManager().setAutoFire(false);
			finishFlight();
		});

	}

	private void initLine_22() {
		addTopLine(L.string(R.string.tutorial_basic_flying_22));
	}

	private void initLine_23() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_23))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> approachTarget(line, blueTarget))
			.setFinishHook(deltaTime -> finishFlight());
	}

	private void initLine_24() {
		final TutorialLine line = addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_24))
				.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = true;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = false;
			AliteButtons.OVERRIDE_LASER = true;
			startFlight();
			// collided with the target
			if (flight.findObjectById(BLUE_TARGET_ID) == null) {
				line.setFinished();
				currentLineIndex+= 2;
			}
			if (game.getCobra().isMissileTargetting()) {
				line.setFinished();
			} else if (isBlueTargetLocked()) {
				line.setFinished();
				currentLineIndex++;
			}
		}).setFinishHook(deltaTime -> finishFlight());
	}

	private boolean isBlueTargetLocked() {
		SpaceObject missileLock = flight.getInGameManager().getMissileLock();
		return missileLock != null && BLUE_TARGET_ID.equals(missileLock.getId());
	}

	private void initLine_25() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_25))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			AliteButtons.OVERRIDE_HYPERSPACE = true;
			AliteButtons.OVERRIDE_INFORMATION = true;
			AliteButtons.OVERRIDE_MISSILE = false;
			AliteButtons.OVERRIDE_LASER = true;
			startFlight();
			// collided with the target
			if (flight.findObjectById(BLUE_TARGET_ID) == null) {
				line.setFinished();
				currentLineIndex++;
			}
			if (game.getCobra().getMissiles() == 0) {
				line.setFinished();
				currentLineIndex+= 2;
			}
			if (isBlueTargetLocked()) {
				SoundManager.play(Assets.identify);
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> {
			flight.getInGameManager().setPlayerControl(false);
			flight.setHandleUI(false);
			setButtons(false);
		});
	}

	private void initLine_26() {
		final TutorialLine line = addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_26))
			.setHeight(180).setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(new IMethodHook() {
			private void writeObject(ObjectOutputStream out) throws IOException {
				try {
					AliteLog.d("Writing", "Writing");
					out.defaultWriteObject();
					AliteLog.d("DONEWriting", "DONE Writing");
				} catch(IOException e) {
					AliteLog.e("PersistenceException", "WriteObject!!", e);
					throw e;
				}
		    }
			@Override
			public void execute(float deltaTime) {
				AliteButtons.OVERRIDE_HYPERSPACE = true;
				AliteButtons.OVERRIDE_INFORMATION = true;
				AliteButtons.OVERRIDE_MISSILE = false;
				AliteButtons.OVERRIDE_LASER = true;
				startFlight();
				if (!blueTarget.getDestructionCallbacks().isEmpty()) {
					return;
				}
				// blueTarget is being serialized!!
				// So, the destruction callback is being serialized, _too_ (!).
				// The destruction callback must have the reference to the tutorial line,
				// which is not serializable. This causes problems, of course.
				// Solution: Delete the destruction callbacks before calling write object....
				blueTarget.addDestructionCallback(6, new IMethodHook() {
					transient TutorialLine tLine = line;

					private void writeObject(ObjectOutputStream out) throws IOException {
						try {
							AliteLog.d("Destruction Callback", "Destruction Callback");
							out.defaultWriteObject();
							AliteLog.d("DONEWriting", "DONE Writing Destruction Callback");
						} catch(IOException e) {
							AliteLog.e("PersistenceException", "WriteObject Destruction Callback!!", e);
							throw e;
						}
					}

					private void readObject(ObjectInputStream in) throws IOException {
						try {
							in.defaultReadObject();
							tLine = line;
						} catch (ClassNotFoundException e) {
							AliteLog.e("Error in Initializer", e.getMessage(), e);
						}
					}

					@Override
					public void execute(float deltaTime) {
						tLine.setFinished();
					}

				});
			}
		}).setFinishHook(deltaTime -> finishFlight());
	}

	private void initLine_27() {
		addTopLine(L.string(R.string.tutorial_basic_flying_27));
	}

	private void initLine_28() {
		addTopLine(L.string(R.string.tutorial_basic_flying_28));
	}

	private void initLine_29() {
		addTopLine(L.string(R.string.tutorial_basic_flying_29));
	}

	private void initLine_30() {
		addTopLine(L.string(R.string.tutorial_basic_flying_30));
	}

	private void initLine_31() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_31))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (dockingBuoy == null) {
				dockingBuoy = (SpaceObject)flight.findObjectById(DOCKING_BUOY_ID);
				if (dockingBuoy == null) {
					InGameManager man = flight.getInGameManager();
					dockingBuoy = SpaceObjectFactory.getInstance().getObjectById(BUOY_ID);
					Vector3f position = new Vector3f(0, 0, 0);
					man.getPlanet().getPosition().sub(man.getStation().getPosition(), position);
					position.normalize();
					position.scale(6000);
					position.add(man.getStation().getPosition());

					dockingBuoy.setPosition(position);
					dockingBuoy.setHudColor(0xEF0000);
					dockingBuoy.setId(DOCKING_BUOY_ID);
					dockingBuoy.setProperty(SpaceObject.Property.name, L.string(R.string.tutorial_basic_flying_docking_buoy));
					flight.getInGameManager().addObject(dockingBuoy);
				}
			}

			startFlightWithButtons();
			if (dockingBuoy.getPosition().distanceSq(flight.getInGameManager().getShip().getPosition()) < 40000) {
				SoundManager.play(Assets.identify);
				dockingBuoy.setRemove(true);
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> finishFlight());

	}

	private void initLine_32() {
		final TutorialLine line = addTopLine(L.string(R.string.tutorial_basic_flying_32))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			startFlightWithButtons();
			if (flight.getInGameManager().getLaserManager().isUnderCross(
					flight.getInGameManager().getStation(),
					flight.getInGameManager().getShip(),
					flight.getInGameManager().getViewDirection())) {
				line.setFinished();
			}
		}).setFinishHook(deltaTime -> {
			setButtons(false);
			flight.getInGameManager().setPlayerControl(false);
			flight.setHandleUI(false);
		});

	}

	private void initLine_33() {
		addTopLine(L.string(R.string.tutorial_basic_flying_33));
	}

	private void initLine_34() {
		final TutorialLine line = addLine(TUTORIAL_INDEX, L.string(R.string.tutorial_basic_flying_34))
			.setMustRetainEvents();

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			startFlightWithButtons();
			if (flight.getPostDockingHook() == null) {
				flight.setPostDockingHook(deltaTime1 -> dispose());
			}
			if (flight.getInGameManager().getActualPostDockingScreen() == null) {
				flight.getInGameManager().setPostDockingScreen(new TutorialSelectionScreen());
			}
		}).setFinishHook(deltaTime -> finishFlight());
	}

	@Override
	public void activate() {
		super.activate();
		Settings.resetButtonPosition();
		game.getCobra().clearEquipment();
		game.getGenerator().buildGalaxy(1);
		game.getGenerator().setCurrentGalaxy(1);
		game.getPlayer().setCurrentSystem(game.getGenerator().getSystem(SystemData.LAVE_SYSTEM_INDEX));
		game.getPlayer().setHyperspaceSystem(game.getGenerator().getSystem(SystemData.ZAONCE_SYSTEM_INDEX));
		game.getCobra().setFuel(70);
		game.getCobra().setMissiles(4);

		flight.activate();
		flight.getInGameManager().setPlayerControl(false);
		flight.getInGameManager().getShip().setSpeed(0);
		flight.setPause(false);

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
		if (yellowTarget != null) {
			yellowTarget.setSaving(true);
		}
		if (blueTarget != null) {
			blueTarget.setSaving(true);
		}
		if (dockingBuoy != null) {
			dockingBuoy.setSaving(true);
		}
		flight.saveScreenState(dos);
		dos.writeInt(currentLineIndex - 1);
		dos.writeBoolean(resetShipPosition);
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

	private void rotate(GraphicObject go, float x, float y, float z) {
		go.applyDeltaRotation((float) Math.toDegrees(x),
							  (float) Math.toDegrees(y),
							  (float) Math.toDegrees(z));
	}

	private void rotateBuoys(float deltaTime) {
		if (yellowTarget != null && yellowTarget.getHullStrength() > 0 && !yellowTarget.mustBeRemoved()) {
			rotate(yellowTarget, 3 * deltaTime, 5 * deltaTime, 2 * deltaTime);
		}
		if (blueTarget != null && blueTarget.getHullStrength() > 0 && !blueTarget.mustBeRemoved()) {
			rotate(blueTarget, 2 * deltaTime, 3 * deltaTime, 5 * deltaTime);
		}
		if (dockingBuoy != null && dockingBuoy.getHullStrength() > 0 && !dockingBuoy.mustBeRemoved()) {
			rotate(dockingBuoy, 5 * deltaTime, 2 * deltaTime, 3 * deltaTime);
		}
	}

	public void doUpdate(float deltaTime) {
		if (flight != null) {
			if (!flight.getInGameManager().isDockingComputerActive()) {
				game.getCobra().setRotation(0, 0);
			}
			flight.update(deltaTime);
			if (resetShipPosition) {
				resetShipPosition = false;
				// Move the ship 30000m to the right, so that the space station is out of our way.
				Vector3f offset = new Vector3f(0, 0, 0);
				flight.getInGameManager().getShip().getRightVector().copy(offset);
				offset.scale(30000.0f);
				offset.add(flight.getInGameManager().getShip().getPosition());
				flight.getInGameManager().getShip().setPosition(offset);
			}
		}
		rotateBuoys(deltaTime);
	}

	@Override
	public void dispose() {
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
		return ScreenCodes.TUT_BASIC_FLYING_SCREEN;
	}
}
