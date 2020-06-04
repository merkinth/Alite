package de.phbouillon.android.games.alite.screens.opengl.ingame;

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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import android.graphics.Color;
import android.graphics.Rect;
import android.opengl.GLES11;
import android.opengl.Matrix;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.IntFunction;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.AccelerometerHandler;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.ShipIntroScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;
import de.phbouillon.android.games.alite.screens.opengl.HyperspaceScreen;
import de.phbouillon.android.games.alite.screens.opengl.objects.*;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.*;
import de.phbouillon.android.games.alite.screens.opengl.sprites.AliteHud;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

public class InGameManager implements Serializable {
	private static final long serialVersionUID = -7222644863845482563L;
	private static final boolean DEBUG_OBJECT_DRAW_ORDER = false;

	private static final float RADAR_CENTER_X = AliteHud.RADAR_X1 + ((AliteHud.RADAR_X2 - AliteHud.RADAR_X1) >> 1);
	private static final float RADAR_CENTER_Y = AliteHud.RADAR_Y1 + ((AliteHud.RADAR_Y2 - AliteHud.RADAR_Y1) >> 1);
	private static final float RADAR_RADIUS_X = AliteHud.RADAR_X2 - RADAR_CENTER_X;
	private static final float RADAR_RADIUS_Y = AliteHud.RADAR_Y2 - RADAR_CENTER_Y;

	private static final long  SAFE_ZONE_RADIUS_SQ     = 19752615936L; // 140544m
	private static final long  EXT_SAFE_ZONE_RADIUS_SQ = 21025000000L; // 145000m

	public static boolean     OVERRIDE_SPEED = false;
	public static boolean     playerInSafeZone = false;
	boolean extendedSafeZone;
	public static boolean     safeZoneViolated = false;

	private transient AliteScreen       postDockingScreen = null;
	private transient IMethodHook hyperspaceHook    = null;
	private transient String            feeText           = null;
	private transient Alite             alite;

	private final Vector3f              deltaYawRollPitch     = new Vector3f(0, 0, 0);
	private final Vector3f              tempVector            = new Vector3f(0, 0, -1);
	private final Vector3f              systemStationPosition = new Vector3f(0, 0, 0);
	private final Vector3f              zero                  = new Vector3f(0, 0, 0);
	private final Vector3f              deltaOrientation      = new Vector3f(0, 0, 0);

	private final float[][]            tempMatrix = new float[3][16];
	private final float[]              viewMatrix = new float[16];
	private final float[]              lightPosition;
	private final float                 aspectRatio;

	private final AliteButtons          buttons;
	private final DockingComputerAI     dockingComputerAI;
	private final ObjectPicker          objectPicker;
	private final SkySphereSpaceObject  skysphere;

	private List <AliteObject>          objectsToBeAdded = new ArrayList<>();
	private List <DepthBucket>          sortedObjectsToDraw = new ArrayList<>();
	private List <TimedEvent>           timedEvents = new ArrayList<>();
	private List <TimedEvent>           removedTimedEvents = new ArrayList<>();

	private InGameHelper                helper;
	private AliteHud                    hud;
	private SystemData                  initialHyperspaceSystem;
	private LaserManager                laserManager;
	private OnScreenMessage             message = new OnScreenMessage();
	private OnScreenMessage             oldMessage;
	private SpaceObject                 missileLock = null;
	private Screen                      newScreen = null;
	private ScrollingText               scrollingText = null;
	private ObjectSpawnManager          spawnManager;
	private StarDust                    starDust;
	private ViewingTransformationHelper viewingTransformationHelper = new ViewingTransformationHelper();
	private WitchSpaceRender            witchSpace = null;

	private final SpaceObject           ship;
	private AliteObject                 planet;
	private AliteObject                 sun;
	private AliteObject                 sunGlow;
	private SpaceObject                 station;

	public HyperspaceTimer              hyperspaceTimer = null;
	private TimedEvent                  cloakingEvent = null;
	private TimedEvent                  jammingEvent = null;

	private boolean                     calibrated = false;
	private boolean                     changingSpeed = false;
	private boolean                     destroyed = false;
	private boolean                     needsSpeedAdjustment = false;
	private boolean                     paused = false;
	private boolean                     planetWasSet = false;
	private boolean                     playerControl = true;
	private boolean                     targetMissile = false;
	private boolean                     viewDirectionChanged = false;
	private boolean                     vipersWillEngage = false;

	private int                         viewDirection = PlayerCobra.DIR_FRONT;
	private int                         lastX = -1;
	private int                         lastY = -1;
	private int                         hudIndex = 0;

	public InGameManager(AliteHud hud, String skyMap, float[] lightPosition, boolean fromStation, boolean initStarDust) {
		alite = Alite.get();
		alite.setInGame(this);
		helper = new InGameHelper(this);
		this.hud = hud;
		this.lightPosition = lightPosition;
		spawnManager = new ObjectSpawnManager(this);
		dockingComputerAI = new DockingComputerAI(this);
		alite.getPlayer().getCobra().resetEnergy();

		skysphere = new SkySphereSpaceObject("skysphere", 8000.0f, 16, 16, skyMap);
		ship = SpaceObjectFactory.getInstance().getObjectById("cobra_mk_iii");
		ship.setPlayer(true);
		ship.setId("Camera");

		MathHelper.getRandomPosition(FlightScreen.PLANET_POSITION, tempVector, 115000.0f, 20000.0f).copy(systemStationPosition);
		if (fromStation) {
			tempVector.scale(-1500.0f);
			tempVector.add(systemStationPosition);
			ship.setPosition(tempVector);
			ship.setForwardVector(new Vector3f(0.0f, 0.0f, 1.0f));
			ship.setUpVector(new Vector3f(0.0f, 1.0f, 0.0f));
			ship.setRightVector(new Vector3f(1.0f, 0.0f, 0.0f));
		} else {
			ship.setPosition(FlightScreen.SHIP_ENTRY_POSITION);
		}
		if (initStarDust && Settings.particleDensity > 0) {
			starDust = new StarDust(ship.getPosition());
		}
		buttons = hud != null ? new AliteButtons(this) : null;
		laserManager = new LaserManager(this);
		timedEvents.addAll(laserManager.registerTimedEvents());
		Rect visibleArea = alite.getGraphics().getVisibleArea();
		aspectRatio = visibleArea.width() / (float) visibleArea.height();
		spawnManager.startSimulation(alite.getPlayer().getCurrentSystem());
		alite.getCobra().setMissileLocked(false);
		objectPicker = new ObjectPicker(this, visibleArea);
		AccelerometerHandler.needsCalibration = true;
	}

	void initializeViperAction() {
		safeZoneViolated = false;
		SystemData currentSystem = alite.getPlayer().getCurrentSystem();
		if (alite.getPlayer().getLegalStatus() == LegalStatus.CLEAN) {
			vipersWillEngage = false;
			return;
		}
		if (currentSystem == null) {
			return;
		}
		vipersWillEngage = true;
		if (getStation() != null && getStation().getHitCount() > 1) {
			safeZoneViolated = true;
			return;
		}
		Government government = currentSystem.getGovernment();
		if (government == Government.ANARCHY || government == Government.FEUDAL) {
			vipersWillEngage = false;
			return;
		}
		int roll = (int) (Math.random() * 100);
		int legalProblemLikelihoodInPercent = alite.getPlayer().getLegalProblemLikelihoodInPercent();
		if (roll >= legalProblemLikelihoodInPercent) {
			vipersWillEngage = false;
		}
		AliteLog.d("Checking Viper Attack", "Roll: " + roll +
			", Likelihood: " + legalProblemLikelihoodInPercent + ", Result: " + vipersWillEngage);
	}

	public LaserManager getLaserManager() {
		return laserManager;
	}

	void clearObjectTransformations() {
		if (viewingTransformationHelper != null) {
			viewingTransformationHelper.clearObjects(sortedObjectsToDraw);
		}
	}

	float getAspectRatio() {
		return aspectRatio;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			in.defaultReadObject();
			alite = Alite.get();
			if (spawnManager != null && timedEvents != null) {
				spawnManager.initTimedEvents(this);
			}
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	public void setScoopCallback(ScoopCallback callback) {
		if (helper != null) {
			helper.setScoopCallback(callback);
		}
	}

	public ScoopCallback getScoopCallback() {
		if (helper != null) {
			return helper.getScoopCallback();
		}
		return null;
	}

	void preMissionCheck() {
		for (Mission m: MissionManager.getInstance().getMissions()) {
			if (m.willStartOnDock()) {
				TimedEvent te = m.getPreStartEvent(this);
				if (te != null) {
					addTimedEvent(te);
				}
			}
		}
	}

	AliteScreen getPostDockingScreen() {
		if (postDockingScreen == null) {
			postDockingScreen = new StatusScreen();
		}
		return postDockingScreen;
	}

	public void setPostDockingScreen(AliteScreen screen) {
		postDockingScreen = screen;
	}

	public Screen getActualPostDockingScreen() {
		return postDockingScreen;
	}

	public void setMessage(String text) {
		message.setText(text);
	}

	public void repeatMessage(String text, int times) {
		message.repeatText(text, 1, times, 1);
	}

	public AliteHud getHud() {
		return hud;
	}

	Vector3f getSystemStationPosition() {
		return systemStationPosition;
	}

	void initStarDust() {
		if (starDust != null) {
			starDust.setPosition(ship.getPosition());
		}
	}

	public boolean isDockingComputerActive() {
		return dockingComputerAI.isActive();
	}

	public void toggleDockingComputer(boolean playSound) {
		if (getStation().isAccessDenied()) {
			if (playSound) {
				SoundManager.play(Assets.com_accessDeclined);
				message.setText(L.string(R.string.com_access_declined));
			}
			return;
		}
		if (dockingComputerAI.isActive()) {
			if (playSound) {
				SoundManager.play(Assets.com_dockingComputerDisengaged);
				setMessage(L.string(R.string.com_docking_computer_disengaged));
			}
			if (hud != null) {
				hud.mapDirections(false, false, false, false);
			}
			dockingComputerAI.disengage();
		} else {
			if (playSound) {
				SoundManager.play(Assets.com_dockingComputerEngaged);
				setMessage(L.string(R.string.com_docking_computer_engaged));
			}
			dockingComputerAI.engage();
		}
	}

	public void toggleStationHandsDocking() {
		if (getStation().isAccessDenied()) {
			SoundManager.play(Assets.com_accessDeclined);
			message.setText(L.string(R.string.com_access_declined));
			return;
		}
		if (feeText != null || dockingComputerAI.isActive()) {
			// Once station hands initiated docking, there's nothing you can do to stop it again...
			return;
		}
		long dockingFee = getDockingFee();
		feeText = L.string(R.string.assisted_docking, L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, dockingFee));
	}

	private long getDockingFee() {
		return Math.max(alite.getPlayer().getCurrentSystem() == null ? 50 :
			alite.getPlayer().getCurrentSystem().getStationHandsDockingFee(), (long) (alite.getPlayer().getCash() * 0.1f));
	}

	private void initiateStationHandsDocking() {
		// TODO different sound ("Transaction has been received. Lean back and enjoy the flight.")
		SoundManager.play(Assets.com_dockingComputerEngaged);
		setMessage(L.string(R.string.com_docking_computer_engaged));
		dockingComputerAI.engage();
	}

	public void yesSelected() {
		if (feeText == null) {
			return;
		}
		feeText = null;
		SoundManager.play(Assets.click);
		long dockingFee = getDockingFee();
		alite.getPlayer().setCash(alite.getPlayer().getCash() - dockingFee);
		initiateStationHandsDocking();
	}

	public void noSelected() {
		if (feeText == null) {
			return;
		}
		SoundManager.play(Assets.click);
		feeText = null;
	}

	void setPlanet(AliteObject planet) {
		this.planet = planet;
	}

	public AliteObject getPlanet() {
		return planet;
	}

	void setSun(AliteObject sun) {
		this.sun = sun;
	}

	void setSunGlow(AliteObject sunGlow) {
		this.sunGlow = sunGlow;
	}

	public AliteObject getSun() {
		return sun;
	}

	public AliteObject getSunGlow() {
		return sunGlow;
	}

	void setStation(SpaceObject station) {
		this.station = station;
	}

	public SpaceObject getStation() {
		return station;
	}

	void yankOutOfTorus() {
		ship.setSpeed(-PlayerCobra.MAX_SPEED);
		setPlayerControl(true);
	}

	public void setPlayerControl(boolean playerControl) {
		this.playerControl = playerControl;
		deltaYawRollPitch.x = 0.0f;
		deltaYawRollPitch.y = 0.0f;
		deltaYawRollPitch.z = 0.0f;
	}

	public boolean isPlayerControl() {
		return playerControl;
	}

	public void killHud() {
		hud = null;
	}

	boolean isPlayerAlive() {
		return hud != null;
	}

	public ObjectSpawnManager getSpawnManager() {
		return spawnManager;
	}

	void addTimedEvent(final TimedEvent event) {
		timedEvents.add(event);
	}

	private float clamp(float val, float min, float max) {
		return val < min ? min : val > max ? max : val;
	}

	public SpaceObject getShip() {
		return ship;
	}

	public void calibrate() {
		// Ensures that a re-calibration occurs on the next frame.
		calibrated = false;
		AccelerometerHandler.needsCalibration = true;
	}

	private void getAlternativeAccelerometerData() {
		if (!calibrated) {
			zero.x = alite.getInput().getAccelX();
			zero.y = alite.getInput().getAccelY();
			zero.z = alite.getInput().getAccelZ();
			calibrated = true;
		} else {
			deltaOrientation.x = -clamp((int) ((alite.getInput().getAccelX() - zero.x) * 10.0f) / 4.0f, -2.0f, 2.0f);
			deltaOrientation.y =  clamp((int) ((alite.getInput().getAccelY() - zero.y) * 50.0f) / 10.0f, -2.0f, 2.0f);
			deltaOrientation.z = -clamp((int) ((alite.getInput().getAccelZ() - zero.z) * 50.0f) / 10.0f, -2.0f, 2.0f);

			deltaYawRollPitch.z = /*(float) (30.0f * Math.PI / 180.0f) **/ deltaOrientation.z;
			if (Settings.reversePitch) {
				deltaYawRollPitch.z = -deltaYawRollPitch.z;
			}
			deltaYawRollPitch.y = /*(float) (30.0f * Math.PI / 180.0f) **/ deltaOrientation.y;
		}
	}

	private void getAccelerometerData() {
		if (!calibrated) {
			calibrated = true;
		}
		float accelY = alite.getInput().getAccelY();
		float accelZ = alite.getInput().getAccelZ();

		deltaYawRollPitch.x = 0;
		deltaYawRollPitch.y = -clamp((int) (accelY * 50.0f) / 10.0f, -2.0f, 2.0f);
		deltaYawRollPitch.z =  clamp((int) (accelZ * 30.0f) / 10.0f, -2.0f, 2.0f);

		if (Settings.reversePitch) {
			deltaYawRollPitch.z = -deltaYawRollPitch.z;
		}
	}

	private void getHudControlData() {
		if (hud != null) {
			deltaYawRollPitch.z = hud.getZ();
			if (Settings.reversePitch) {
				deltaYawRollPitch.z = -deltaYawRollPitch.z;
			}
			deltaYawRollPitch.y = hud.getY();
		}
	}

	private void updateShipOrientation() {
		if (!playerControl) {
			return;
		}
		switch (Settings.controlMode) {
			case ACCELEROMETER: getAccelerometerData(); break;
			case ALTERNATIVE_ACCELEROMETER: getAlternativeAccelerometerData(); break;
			case CONTROL_PAD:
			case CURSOR_BLOCK:
			case CURSOR_SPLIT_BLOCK: getHudControlData(); break;
		}
	}

	void terminateToTitleScreen() {
		SoundManager.stopAll();
		witchSpace = null;
		message.clearRepetition();
		try {
			alite.autoLoad();
		} catch (IOException e) {
			AliteLog.e("Game Over", "Cannot reset commander to last autosave. Resetting.", e);
			alite.getPlayer().reset();
		}
		alite.getNavigationBar().setFlightMode(false);
		newScreen = new ShipIntroScreen();
	}

	public void terminateToStatusScreen() {
		SoundManager.stopAll();
		witchSpace = null;
		message.clearRepetition();
		alite.getNavigationBar().setFlightMode(false);
		try {
			AliteLog.d("[ALITE]", "Performing autosave. [Docked]");
			alite.autoSave();
		} catch (IOException e) {
			AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
		}
		newScreen = new StatusScreen();
	}

	public void addObject(AliteObject object) {
		objectsToBeAdded.add(object);
	}

	private synchronized void spawnMissile(SpaceObject so) {
		SpaceObject missile = helper.spawnMissile(so, ship);
		message.repeatText(L.string(R.string.com_incoming_missile), 2);
		missile.addDestructionCallback(7, new IMethodHook() {
			private static final long serialVersionUID = -4168441227358105959L;

			@Override
			public void execute(float deltaTime) {
				message.clearRepetition();
			}

		});
	}

	private synchronized void spawnObjects(SpaceObject ao) {
		for (ObjectType st: ao.getObjectsToSpawn()) {
			switch (st) {
				case Missile: spawnMissile(ao); break;
				case EscapeCapsule: helper.launchEscapeCapsule(ao); break;
				default: AliteLog.d("Unknown ShipType", "Supposed to spawn a " + st + " - but don't know how."); break;
			}
		}
		ao.clearObjectsToSpawn();
	}

	private synchronized void updatePlanet(AliteObject ao) {
		float distSq = ao.getPosition().distanceSq(ship.getPosition());
		if (distSq > EXT_SAFE_ZONE_RADIUS_SQ) {
			alite.getCobra().setAltitude(PlayerCobra.MAX_ALTITUDE);
		} else {
			alite.getCobra().setAltitude(PlayerCobra.MAX_ALTITUDE * (distSq / EXT_SAFE_ZONE_RADIUS_SQ));
		}
		playerInSafeZone = witchSpace == null && distSq < SAFE_ZONE_RADIUS_SQ;
		extendedSafeZone = witchSpace == null && distSq < EXT_SAFE_ZONE_RADIUS_SQ;
		if (extendedSafeZone && spawnManager.isInTorus()) {
			spawnManager.leaveTorus();
			message.setText(L.string(R.string.msg_mass_locked));
		}
	}

	private synchronized boolean removeObjectIfNecessary(Iterator <AliteObject> objectIterator, AliteObject ao) {
		if (ao.mustBeRemoved() || ao instanceof SpaceObject && ((SpaceObject) ao).getHullStrength() <= 0) {
			ao.executeDestructionCallbacks();
			objectIterator.remove();
			return true;
		}
		return false;
	}

	private synchronized void updateObjects(float deltaTime, List <AliteObject> allObjects) {
		helper.checkShipObjectCollision(allObjects);
		laserManager.update(deltaTime, allObjects);
		Iterator <AliteObject> objectIterator = allObjects.iterator();
		helper.checkProximity(allObjects);
		if (dockingComputerAI != null && dockingComputerAI.isActive()) {
			if (!dockingComputerAI.isOnFinalApproach()) {
				helper.checkShipStationProximity();
			}
			if (ship.getProximity() != null && ship.getProximity() != station) {
				float distanceSq = ship.getPosition().distanceSq(ship.getProximity().getPosition());
				if (distanceSq > InGameHelper.STATION_VESSEL_PROXIMITY_DISTANCE_SQ) {
					ship.setProximity(null);
				}
			}
		}

		while (objectIterator.hasNext()) {
			AliteObject ao = objectIterator.next();
			if ("Planet".equals(ao.getId())) {
				updatePlanet(ao);
			}
			if (ao instanceof SpaceObject) {
				spawnObjects((SpaceObject) ao);
			}
			if (removeObjectIfNecessary(objectIterator, ao)) {
				continue;
			}
			if (ao instanceof SpaceObject) {
				((SpaceObject) ao).update(deltaTime);
				if (!SpaceObjectAI.AI_STATE_FOLLOW_CURVE.equals(((SpaceObject) ao).getAIState())) {
					ao.moveForward(deltaTime);
				}
				if (((SpaceObject) ao).getType() == ObjectType.Missile) {
					helper.handleMissileUpdate((SpaceObject)ao, deltaTime);
					ao.getPosition().sub(ship.getPosition(), tempVector);
					if (tempVector.lengthSq() > AliteHud.MAX_DISTANCE_SQ) {
						objectIterator.remove();
					}
				}
			}
			ao.onUpdate(deltaTime);
		}
	}

	private synchronized void updateTimedEvents() {
		removedTimedEvents.clear();
		for (TimedEvent event: timedEvents) {
			event.perform();
			if (event.mustBeRemoved()) {
				removedTimedEvents.add(event);
			}
		}
		for (TimedEvent event: removedTimedEvents) {
			timedEvents.remove(event);
		}
	}

	private synchronized void handleStationAccessDeclined() {
		if (getStation().isAccessDenied()) {
			if (isDockingComputerActive()) {
				if (hud != null) {
					hud.mapDirections(false, false, false, false);
				}
				dockingComputerAI.disengage();
				SoundManager.playOnce(Assets.com_accessDeclined, 3000);
				message.setText(L.string(R.string.com_access_declined));
			}
		}
	}

	private synchronized void updateRetroRockets(float deltaTime) {
		if (ship.getSpeed() > 0) {
			// Retro rockets decaying
			float newSpeed = ship.getSpeed() * (1.0f - deltaTime + deltaTime / 1.6f);
			if (newSpeed < 100) {
				newSpeed = 0;
			}
			ship.setSpeed(newSpeed);
		}
	}

	public boolean isTorusDriveEngaged() {
		return ship.getSpeed() < -PlayerCobra.TORUS_TEST_SPEED;
	}

	synchronized void performUpdate(float deltaTime, List<AliteObject> allObjects) {
		if (paused || destroyed || helper == null) {
			return;
		}
		if (hud != null) {
			hud.update(deltaTime);
		}
		if (spawnManager != null && timedEvents != null && spawnManager.needsInitialization()) {
			spawnManager.initTimedEvents(this);
		}

		updateShipOrientation();
		ship.moveForward(deltaTime);
		ship.onUpdate(deltaTime);
		helper.updatePlayerCondition();
		handleStationAccessDeclined();
		updateRetroRockets(deltaTime);

		if (starDust != null) {
			starDust.update(ship.getPosition(), ship.getForwardVector(), isTorusDriveEngaged());
		}

		laserManager.performUpdate();

		updateTimedEvents();
		if (dockingComputerAI.isActive() && Settings.dockingComputerSpeed == 2 &&
				(alite.getPlayer().getLegalStatus() == LegalStatus.CLEAN || !vipersWillEngage ||
				(alite.getPlayer().getCurrentSystem() == null ||
				alite.getPlayer().getCurrentSystem().getGovernment() == Government.ANARCHY ||
				alite.getPlayer().getCurrentSystem().getGovernment() == Government.FEUDAL) && !safeZoneViolated)) {
			helper.automaticDockingSequence();
		}
		updateObjects(deltaTime, allObjects);

		allObjects.addAll(objectsToBeAdded);
		objectsToBeAdded.clear();
		if (!isPlayerControl() && dockingComputerAI.isActive() || needsSpeedAdjustment) {
			if (!isPlayerAlive()) {
				dockingComputerAI.disengage();
			}
			ship.update(deltaTime);
			if (Math.abs(ship.getTargetSpeed() - ship.getSpeed()) < 0.0001) {
				needsSpeedAdjustment = false;
			}
		}
	}

	public void setNeedsSpeedAdjustment(boolean b) {
		needsSpeedAdjustment = b;
	}

	public void setViewport(int newViewDirection) {
		if (viewDirection == newViewDirection) {
			toggleZoom();
		} else {
			laserManager.setAutoFire(false);
			viewDirection = newViewDirection;
		}
	}

	public void toggleZoom() {
		if (hud.getZoomFactor() > 3.5f) {
			hud.setZoomFactor(1.0f);
		} else {
			hud.zoomIn();
		}
	}

	private int computeNewViewport(int x, int y) {
		if (Math.abs(x - RADAR_CENTER_X) / RADAR_RADIUS_X > Math.abs(y - RADAR_CENTER_Y) / RADAR_RADIUS_Y) {
			return x > RADAR_CENTER_X ? PlayerCobra.DIR_RIGHT : PlayerCobra.DIR_LEFT;
		}
		return y > RADAR_CENTER_Y ? PlayerCobra.DIR_REAR : PlayerCobra.DIR_FRONT;
	}

	public void handleMissileIcons() {
		if (alite.getCobra().isMissileLocked()) {
			alite.getCobra().setMissileLocked(false);
			missileLock = null;
			targetMissile = false;
		} else {
			targetMissile = !targetMissile;
		}
		alite.getCobra().setMissileTargetting(targetMissile);
	}

	public void fireMissile() {
		if (alite.getCobra().isMissileLocked()) {
			if (missileLock == null) {
				// This should not happen, but just in case: Deactivate the missile now.
				alite.getCobra().setMissileLocked(false);
				missileLock = null;
				targetMissile = false;
				laserManager.handleTouchUp();
			} else {
				alite.getCobra().setMissileLocked(false);
				alite.getCobra().setMissiles(alite.getCobra().getMissiles() - 1);
				SoundManager.play(Assets.fireMissile);
				helper.spawnMissile(ship, missileLock);
				missileLock.sendAIMessage("INCOMING_MISSILE");
				missileLock = null;
			}
		}
	}

	private void handleSpeedChange(TouchEvent e) {
		if (lastX != -1 && lastY != -1) {
			int diffX = e.x - lastX;
			int diffY = e.y - lastY;
			int ady = Math.abs(diffY);
			int adx = Math.abs(diffX);
			if (adx > 10 || ady > 10 || changingSpeed) {
				if (ady > adx && playerControl && !OVERRIDE_SPEED && ship.getSpeed() <= 0) {
					// No speed change if retro rockets are being fired right now...
					changingSpeed = true;
					float newSpeed = ship.getSpeed() + diffY / 1.4f;
					if (diffY > 0) {
						if (newSpeed > 0.0f) {
							newSpeed = 0.0f;
						}
					} else if (newSpeed < -PlayerCobra.MAX_SPEED) {
						newSpeed = -PlayerCobra.MAX_SPEED;
					}
					ship.setSpeed(newSpeed);
					lastX = e.x;
					lastY = e.y;
				}
				if (adx > ady && playerControl) {
					if (!Settings.tapRadarToChangeView) {
						if (adx > 500) {
							if (diffX < 0) {
								setViewport(viewDirection < 3 ? viewDirection + 1 : 0);
							} else {
								int vd = viewDirection > 0 ? viewDirection - 1 : 3;
								setViewport(vd);
							}
							lastX = e.x;
							lastY = e.y;
							viewDirectionChanged = true;
						}
					} else {
						if (!OVERRIDE_SPEED && ship.getSpeed() <= 0) {
							changingSpeed = true;
							float newSpeed = ship.getSpeed() - diffX;
							if (newSpeed > 0.0f) {
								newSpeed = 0.0f;
							} else if (newSpeed < -PlayerCobra.MAX_SPEED) {
								newSpeed = -PlayerCobra.MAX_SPEED;
							}
							ship.setSpeed(newSpeed);
							lastX = e.x;
							lastY = e.y;
						}
					}
				}
			}
		}
	}

	public int handleExternalViewportChange(TouchEvent e) {
		if (Settings.tapRadarToChangeView) {
			if (e.type == TouchEvent.TOUCH_UP) {
				if (e.x >= AliteHud.RADAR_X1 && e.x <= AliteHud.RADAR_X2 &&
						e.y >= AliteHud.RADAR_Y1 && e.y <= AliteHud.RADAR_Y2 &&
						isPlayerAlive()) {
					return computeNewViewport(e.x, e.y);
				}
			}
		} else {
			if (e.type == TouchEvent.TOUCH_DOWN) {
				lastX = e.x;
				lastY = e.y;
			}
			if (e.type == TouchEvent.TOUCH_DRAGGED) {
				if (lastX != -1 && lastY != -1) {
					int diffX = e.x - lastX;
					int adx = Math.abs(diffX);
					if (adx > 500) {
						if (diffX < 0) {
							int vd = viewDirection + 1;
							if (vd == 4) {
								vd = 0;
							}
							lastX = e.x;
							return vd;
						}
						int vd = viewDirection - 1;
						if (vd < 0) {
							vd = 3;
						}
						lastX = e.x;
						return vd;
					}
				}
			}
			if (e.type == TouchEvent.TOUCH_UP) {
				if (e.x >= AliteHud.RADAR_X1 && e.x <= AliteHud.RADAR_X2 &&
						e.y >= AliteHud.RADAR_Y1 && e.y <= AliteHud.RADAR_Y2 &&
						isPlayerAlive()) {
					return 4;
				}
			}
		}
		return -1;
	}

	boolean handleUI(TouchEvent e) {
		if (hud == null) {
			return false;
		}
		buttons.checkFireRelease(e);
		if (isPlayerControl()) {
			if (hud.handleUI(e)) {
				return false;
			}
		}
		if (!buttons.handleTouch(e)) {
			if (e.type == TouchEvent.TOUCH_DOWN) {
				lastX = e.x;
				lastY = e.y;
			}
			if (e.type == TouchEvent.TOUCH_UP) {
				lastX = -1;
				lastY = -1;
				if (changingSpeed) {
					changingSpeed = false;
				} else {
					if (e.x >= AliteHud.RADAR_X1 && e.x <= AliteHud.RADAR_X2 &&
							e.y >= AliteHud.RADAR_Y1 && e.y <= AliteHud.RADAR_Y2 &&
							isPlayerAlive()) {
						if (Settings.tapRadarToChangeView) {
							setViewport(computeNewViewport(e.x, e.y));
						} else if (!viewDirectionChanged) {
							toggleZoom();
						}
					} else if (e.x >= AliteHud.ALITE_TEXT_X1 && e.x <= AliteHud.ALITE_TEXT_X2 &&
							   e.y >= AliteHud.ALITE_TEXT_Y1 && e.y <= AliteHud.ALITE_TEXT_Y2) {
						if (alite.getCurrentScreen() instanceof FlightScreen) {
							((FlightScreen) alite.getCurrentScreen()).setPause(true);
						}
					} else {
						SpaceObject picked = objectPicker.handleIdentify(e.x, e.y, sortedObjectsToDraw);
						if (picked != null) {
							SoundManager.play(Assets.identify);
							message.setText(picked.getName());
						}
					}
				}
				viewDirectionChanged = false;
				return true;
			}
			if (e.type == TouchEvent.TOUCH_DRAGGED) {
				handleSpeedChange(e);
			}
		} else {
			newScreen = null;
			return true;
		}
		return false;
	}

	public Screen getNewScreen() {
		return newScreen;
	}

	private void performViewTransformation(float deltaTime) {
		// Kudos to Quelo!!
		// Thanks for getting me started on OpenGL -- from simply
		// looking at this method, one cannot immediately grasp the
		// complexities of the ideas behind it.
		// And without Quelo, I certainly would not have understood!
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_POSITION, lightPosition, 0);

		calcViewTransformation(deltaTime);
		GLES11.glLoadMatrixf(viewMatrix, 0);
	}

	private void calcViewTransformation(float deltaTime) {
		if (!paused) {
			ship.applyDeltaRotation((float) Math.toDegrees(deltaYawRollPitch.z * deltaTime),
				(float) Math.toDegrees(deltaYawRollPitch.x * deltaTime),
				(float) Math.toDegrees(deltaYawRollPitch.y * deltaTime));
		}

		ship.orthoNormalize();
		Matrix.invertM(viewMatrix, 0, ship.getMatrix(), 0);
	}

	private void renderHud() {
		if (hud == null) {
			return;
		}
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPushMatrix();
		GLES11.glLoadIdentity();
		GlUtils.ortho(alite);

		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		if (playerControl) {
			alite.getCobra().setRotation(deltaYawRollPitch.z, deltaYawRollPitch.y);
		}
		hud.render();
		hud.clear();
	}

	private void renderButtons() {
		if (hud == null) {
			return;
		}
		buttons.render();

		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPopMatrix();
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
	}

	public boolean isTargetInCenter() {
		return hud != null && hud.isTargetInCenter();
	}

	private boolean isEnemy(AliteObject go) {
		if (!(go instanceof SpaceObject)) {
			return false;
		}
		SpaceObject so = (SpaceObject) go;
		ObjectType type = so.getType();
		return type == ObjectType.Pirate || type == ObjectType.Thargoid || type == ObjectType.Police ||
			so.isDrone() && so.hasLivingMother();
	}

	private void renderHudObject(float deltaTime, AliteObject go) {
		if (hud == null || go instanceof SpaceObject && ((SpaceObject) go).isCloaked()) {
			return;
		}
		if (go.isVisibleOnHud()) {
			Matrix.multiplyMM(tempMatrix[1], 0, viewMatrix, 0, go.getMatrix(), 0);
			hud.setObject(hudIndex++, tempMatrix[1][12], tempMatrix[1][13], tempMatrix[1][14], go.getHudColor(), isEnemy(go));
		}
		if (go instanceof SpaceObject && ObjectType.isSpaceStation(((SpaceObject) go).getType()) && !planetWasSet) {
			Matrix.multiplyMM(tempMatrix[1], 0, viewMatrix, 0, go.getMatrix(), 0);
			hud.setPlanet(tempMatrix[1][12], tempMatrix[1][13], tempMatrix[1][14]);
		}
		if ("Planet".equals(go.getId())) {
			if (!playerInSafeZone) {
				Matrix.multiplyMM(tempMatrix[1], 0, viewMatrix, 0, go.getMatrix(), 0);
				hud.setPlanet(tempMatrix[1][12], tempMatrix[1][13], tempMatrix[1][14]);
				planetWasSet = true;
			} else {
				helper.checkAltitudeLowAlert();
			}
		}
		if ("Sun".equals(go.getId())) {
			float distSq = go.getPosition().distanceSq(ship.getPosition());
			if (distSq > EXT_SAFE_ZONE_RADIUS_SQ || witchSpace != null) {
				alite.getCobra().setCabinTemperature(0);
			} else {
				if (spawnManager.isInTorus()) {
					spawnManager.leaveTorus();
					message.setText(L.string(R.string.msg_mass_locked));
				}
				alite.getCobra().setCabinTemperature(
					(int) (PlayerCobra.MAX_CABIN_TEMPERATURE - PlayerCobra.MAX_CABIN_TEMPERATURE
						* ((distSq - FlightScreen.SUN_SIZE * FlightScreen.SUN_SIZE) / EXT_SAFE_ZONE_RADIUS_SQ)));
				helper.checkCabinTemperatureAlert(deltaTime);
			}
		}
	}

	private void renderAllObjects(final float deltaTime) {
		if (witchSpace == null) {
			if (!isTorusDriveEngaged()) {
				GLES11.glPushMatrix();
				skysphere.setPosition(ship.getPosition());
				GLES11.glMultMatrixf(skysphere.getMatrix(), 0);
				skysphere.render();
				GLES11.glPopMatrix();
			} else {
				GLES11.glClearColor(0.1f, 0.1f, 0.1f, 0.7f);
			}

			if (starDust != null) {
				// Now render star dust...
				GLES11.glPushMatrix();
				GLES11.glMultMatrixf(starDust.getMatrix(), 0);
				starDust.render(isTorusDriveEngaged());
				GLES11.glPopMatrix();
			}
		}

		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glEnable(GLES11.GL_DEPTH_TEST);
		GLES11.glDepthFunc(GLES11.GL_LESS);
		GLES11.glClear(GLES11.GL_DEPTH_BUFFER_BIT);

		GLES11.glPushMatrix();
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(alite, 45.0f, 0.1f, 1.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glPopMatrix();

		if (DEBUG_OBJECT_DRAW_ORDER) {
			AliteLog.d("----Debugging Objects----", "--------Debugging Objects--------");
			for (DepthBucket bucket: sortedObjectsToDraw) {
				if (bucket.near <= 0 || bucket.far <= 0) {
					AliteLog.d("Bucket error", "[E] Bucket near: " + bucket.near + ", Bucket far: " + bucket.far);
				} else {
					AliteLog.d("Bucket ok", "[O] Bucket near: " + bucket.near + ", Bucket far: " + bucket.far);
				}
				for (AliteObject go: bucket.sortedObjects) {
					AliteLog.d("  OIB", "  Object: " + go.getId());
				}
			}
			AliteLog.d("----Debugging Objects End----", "--------Debugging Objects End--------");
		}
		for (DepthBucket bucket: sortedObjectsToDraw) {
			if (bucket.near > 0 && bucket.far > 0) {
				GLES11.glPushMatrix();
				GLES11.glMatrixMode(GLES11.GL_PROJECTION);
				GLES11.glLoadIdentity();
				GlUtils.gluPerspective(alite, 45.0f, bucket.near, bucket.far);
				GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
				GLES11.glPopMatrix();
			}
			GLES11.glClear(GLES11.GL_DEPTH_BUFFER_BIT);
			for (AliteObject go: bucket.sortedObjects) {
				if (go instanceof SpaceObject && ((SpaceObject) go).isCloaked()) {
					continue;
				}
				if (go instanceof LaserCylinder) {
					laserManager.renderLaser((LaserCylinder) go);
					continue;
				}
				if (go instanceof ExplosionBillboard) {
					((ExplosionBillboard) go).getExplosion().render();
					continue;
				}
				if (!isPlayerAlive() && go instanceof SpaceObject && ObjectType.isSpaceStation(((SpaceObject) go).getType())) {
					// If the player rams the space station, the station gets in the way of the game over sequence.
					// So we don't draw it.
					continue;
				}
				GLES11.glPushMatrix();
				MathHelper.copyMatrix(tempMatrix[0], viewMatrix);
				if (viewDirection == PlayerCobra.DIR_FRONT) {
					renderHudObject(deltaTime, go);
				}
				if (go.isDepthTest()) {
					GLES11.glEnable(GLES11.GL_DEPTH_TEST);
				} else {
					GLES11.glDisable(GLES11.GL_DEPTH_TEST);
				}
				float distSq = 1.0f;
				if (go instanceof SpaceObject) {
					distSq = ship.getPosition().distanceSq(go.getPosition());
				}
				if (go instanceof Billboard) {
					((Billboard) go).update(ship);
					GLES11.glEnable(GLES11.GL_BLEND);
					GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
					GLES11.glDisable(GLES11.GL_CULL_FACE);
				}
				float[] goMatrix;
				if (go instanceof SpaceObject && ObjectType.isSpaceStation(((SpaceObject) go).getType())) {
					float scale = distSq < EXT_SAFE_ZONE_RADIUS_SQ ? 1.0f : EXT_SAFE_ZONE_RADIUS_SQ / distSq;
					goMatrix = go.getScaledMatrix(scale);
					((SpaceObject) go).scaleBoundingBox(scale);
				} else {
					goMatrix = go.getMatrix();
				}
				GLES11.glMultMatrixf(goMatrix, 0);
				Matrix.multiplyMM(tempMatrix[2], 0, viewMatrix, 0, go.getMatrix(), 0);
				go.setDisplayMatrix(tempMatrix[2]);
				if (alite.getCobra().getLaser(viewDirection) != null) {
					if (go instanceof SpaceObject) {
						if (targetMissile && alite.getCobra().getMissiles() > 0 || Settings.autoId && !((SpaceObject) go).isIdentified()) {
							if (laserManager.isUnderCross((SpaceObject) go)) {
								if (targetMissile) {
									AliteLog.d("Targetted", "Targetted " + go.getId());
									setMessage(L.string(R.string.msg_missile_locked, go.getName()));
									alite.getCobra().setMissileLocked(true);
									missileLock = (SpaceObject) go;
									SoundManager.play(Assets.missileLocked);
									targetMissile = false;
								} else if (Settings.autoId && isPlayerAlive()) {
									SoundManager.play(Assets.identify);
									setMessage(go.getName());
								}
								((SpaceObject) go).setIdentified();
							}
						}
					}
				}
				go.render();
				if (go instanceof SpaceObject) {
					((SpaceObject) go).renderTargetBox(distSq);
				}
				if (go instanceof Billboard) {
					GLES11.glDisable(GLES11.GL_BLEND);
					GLES11.glEnable(GLES11.GL_CULL_FACE);
				}
				GLES11.glPopMatrix();
			}
		}
		GLES11.glDisable(GLES11.GL_DEPTH_TEST);
		GLES11.glPushMatrix();
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(alite, 45.0f, 1.0f, 900000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glPopMatrix();
		GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glDisableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glDisableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
	}

 	void renderScroller(final float deltaTime) {
 		GLES11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
 		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPushMatrix();
		GLES11.glLoadIdentity();
		GlUtils.ortho(alite);

		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		message.render();
		if (scrollingText != null) {
			scrollingText.render(deltaTime);
		}
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPopMatrix();
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
 	}

	public void render(final float deltaTime, final List <AliteObject> objects) {
		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);

		if (destroyed) {
			return;
		}
		if (laserManager == null) {
			return;
		}
		if (hud != null) {
			hud.setViewDirection(viewDirection);
		}
		performViewTransformation(deltaTime);
		viewingTransformationHelper.clearObjects(sortedObjectsToDraw);
		hudIndex = 0;
		planetWasSet = false;
		if (viewDirection != PlayerCobra.DIR_FRONT) {
			MathHelper.copyMatrix(viewMatrix, tempMatrix[0]);
			for (AliteObject go: objects) {
				MathHelper.copyMatrix(tempMatrix[0], viewMatrix);
				renderHudObject(deltaTime, go);
			}
			MathHelper.copyMatrix(tempMatrix[0], viewMatrix);
			viewingTransformationHelper.applyViewDirection(viewDirection, viewMatrix);
			GLES11.glLoadMatrixf(viewMatrix, 0);
		}
		MathHelper.copyMatrix(viewMatrix, tempMatrix[0]);

		viewingTransformationHelper.sortObjects(objects, viewMatrix, tempMatrix[2], laserManager.activeLasers,
			sortedObjectsToDraw, witchSpace != null, ship);
		try {
			renderAllObjects(deltaTime);
		} catch (ConcurrentModificationException ignored) {
			// This can happen if the game state is being paused while the current
			// screen is being rendered. Ignoring it is a bit of a hack, but gets
			// rid of the issue...
		}

		if (hud != null) {
			if (dockingComputerAI.isActive()) {
				hud.mapDirections(alite.getCobra().getRoll() > 0.1,
				alite.getCobra().getRoll() < -0.1,
				alite.getCobra().getPitch() < -0.1,
				alite.getCobra().getPitch() > 0.1);
			}
			renderHud();
			GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			if (message == null) {
				message = new OnScreenMessage();
			}
			message.render();
			if (scrollingText != null) {
				scrollingText.render(deltaTime);
			} else if (feeText != null) {
				alite.getGraphics().drawCenteredText(feeText, 960, 150,
					AliteColor.argb(0.6f, 0.94f, 0.94f, 0.0f), Assets.regularFont, 1.0f);
				GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				buttons.renderYesNoButtons();
			}
			if (Settings.displayFrameRate) {
				alite.getGraphics().drawText(String.format("FPS: %3.1f", AndroidGame.fps), 400, 10, 0xFFE6B300,
					Assets.regularFont, 1.0f);
				alite.getGraphics().setColor(Color.WHITE);
			}
			if (Settings.displayDockingInformation) {
				debugDocking();
			}
			renderButtons();
		} else {
			GLES11.glMatrixMode(GLES11.GL_PROJECTION);
			GLES11.glPushMatrix();
			GLES11.glLoadIdentity();
			GlUtils.ortho(alite);

			GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
			GLES11.glLoadIdentity();
			GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			message.render();
			if (scrollingText != null) {
				scrollingText.render(deltaTime);
			}
			GLES11.glMatrixMode(GLES11.GL_PROJECTION);
			GLES11.glPopMatrix();
			GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		}
	}

	private void debugDocking() {
		for (DepthBucket depthBucket: sortedObjectsToDraw) {
			for (AliteObject object: depthBucket.sortedObjects) {
				if (object instanceof SpaceObject && ObjectType.isSpaceStation(((SpaceObject) object).getType())) {
					float distanceSq = computeDistanceSq(object, ship);
					if (distanceSq < 64000000) {
						displayDockingAlignment((SpaceObject) object, distanceSq);
					}
					return;
				}
			}
		}
		alite.getGraphics().setColor(Color.WHITE);
	}

	float computeDistanceSq(AliteObject a, AliteObject b) {
		tempVector.x = b.getPosition().x - a.getPosition().x;
		tempVector.y = b.getPosition().y - a.getPosition().y;
		tempVector.z = b.getPosition().z - a.getPosition().z;
		tempVector.normalize();
		float aDistance = a.getDistanceFromCenterToBorder();
		float adx = aDistance * tempVector.x;
		float ady = aDistance * tempVector.y;
		float adz = aDistance * tempVector.z;
		tempVector.negate();
		float bDistance = b.getDistanceFromCenterToBorder();
		float bdx = bDistance * tempVector.x;
		float bdy = bDistance * tempVector.y;
		float bdz = bDistance * tempVector.z;

		return (a.getPosition().x + adx - b.getPosition().x - bdx) * (a.getPosition().x + adx - b.getPosition().x - bdx) +
			(a.getPosition().y + ady - b.getPosition().y - bdy) * (a.getPosition().y + ady - b.getPosition().y - bdy) +
			(a.getPosition().z + adz - b.getPosition().z - bdz) * (a.getPosition().z + adz - b.getPosition().z - bdz);
	}

	private void displayDockingAlignment(SpaceObject spaceStation, float distanceSq) {
		alite.getGraphics().drawText("Dist: " + distanceSq, 400, 50, Color.WHITE, Assets.regularFont, 1.0f);
		float fz = spaceStation.getDisplayMatrix()[10];
		alite.getGraphics().drawText("fz: " + fz, 400, 90, fz < 0.98f ? Color.RED : Color.GREEN,
			Assets.regularFont, 1.0f);

		float angle = ship.getForwardVector().angleInDegrees(spaceStation.getForwardVector());
		alite.getGraphics().drawText(String.format("%4.2f", angle), 400, 130,
			Math.abs(angle) > 15.0f ? Color.RED : Color.GREEN, Assets.regularFont, 1.0f);

		float ux = Math.abs(spaceStation.getDisplayMatrix()[4]);
		alite.getGraphics().setColor(ux < 0.95f ? Color.RED : Color.GREEN);
		alite.getGraphics().drawText("ux: " + ux, 400, 170,
			ux < 0.95f ? Color.RED : Color.GREEN, Assets.regularFont, 1.0f);
		alite.getGraphics().setColor(Color.WHITE);
	}

	void calcAllObjects(final float deltaTime, final List<AliteObject> objects) {
		if (destroyed) {
			return;
		}
		if (laserManager == null) {
			return;
		}
		calcViewTransformation(deltaTime);
		viewingTransformationHelper.clearObjects(sortedObjectsToDraw);
		hudIndex = 0;
		planetWasSet = false;
		if (viewDirection != PlayerCobra.DIR_FRONT) {
			viewingTransformationHelper.applyViewDirection(viewDirection, viewMatrix);
		}
		MathHelper.copyMatrix(viewMatrix, tempMatrix[0]);

		viewingTransformationHelper.sortObjects(objects, viewMatrix, tempMatrix[2], laserManager.activeLasers,
			sortedObjectsToDraw, witchSpace != null, ship);
		try {
			for (DepthBucket bucket: sortedObjectsToDraw) {
				for (AliteObject go: bucket.sortedObjects) {
					if (go instanceof SpaceObject && ((SpaceObject) go).isCloaked()) {
						continue;
					}
					if (!isPlayerAlive() && go instanceof SpaceObject && ObjectType.isSpaceStation(((SpaceObject) go).getType())) {
						// If the player rams the space station, the station gets in the way of the game over sequence.
						// So we don't draw it.
						continue;
					}
					MathHelper.copyMatrix(tempMatrix[0], viewMatrix);
					if (go instanceof Billboard) {
						((Billboard) go).update(ship);
					}
					Matrix.multiplyMM(tempMatrix[2], 0, viewMatrix, 0, go.getMatrix(), 0);
					go.setDisplayMatrix(tempMatrix[2]);
				}
			}
		} catch (ConcurrentModificationException ignored) {
			// This can happen if the game state is being paused while the current
			// screen is being rendered. Ignoring it is a bit of a hack, but gets
			// rid of the issue...
		}
	}

	public void destroy() {
		destroyed = true;
		if (message != null) {
			message.clearRepetition();
		}
		SoundManager.stopAll();
		if (skysphere != null) {
			skysphere.destroy();
		}
		if (laserManager != null) {
			laserManager.destroy();
		}
		laserManager = null;
		helper = null;
		viewingTransformationHelper = null;
	}

	public final boolean traverseObjects(SpaceObjectTraverser traverser) {
		// This method is called only from the AliteButtons update method and hence
		// sortedObjectsToDraw still contains the objects of the last rendered frame.
		for (DepthBucket db: sortedObjectsToDraw) {
			for (AliteObject eo: db.sortedObjects) {
				if (eo instanceof SpaceObject && !eo.mustBeRemoved()) {
					if (traverser.handle((SpaceObject) eo)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	void enterWitchSpace() {
		witchSpace = new WitchSpaceRender(this);
		witchSpace.enterWitchSpace();
	}

	public boolean isWitchSpace() {
		return witchSpace != null;
	}

	private void escapeWitchSpace() {
		witchSpace = null;
	}

	public boolean isHyperdriveMalfunction() {
		return witchSpace != null && witchSpace.isHyperdriveMalfunction();
	}

	public boolean isInExtendedSafeZone() {
		return extendedSafeZone;
	}

	int getNumberOfObjects(ObjectType type) {
		// This is called only from timed events; hence sortedObjectsToDraw still contains
		// the objects of the last rendered frame.
		int count = 0;
		for (DepthBucket db: sortedObjectsToDraw) {
			for (AliteObject eo: db.sortedObjects) {
				if (eo instanceof SpaceObject && ((SpaceObject) eo).getType().equals(type) && !eo.mustBeRemoved()) {
					count++;
				}
			}
		}
		return count;
	}

	public List<SpaceObject> getObjects(ObjectType type) {
		List<SpaceObject> objects = new ArrayList<>();
		for (DepthBucket db: sortedObjectsToDraw) {
			for (AliteObject eo: db.sortedObjects) {
				if (eo instanceof SpaceObject && ((SpaceObject) eo).getType().equals(type) && !eo.mustBeRemoved()) {
					objects.add((SpaceObject) eo);
				}
			}
		}
		return objects;
	}

	SpaceObject getShipInDockingBay() {
		// This is called only from timed events; hence sortedObjectsToDraw still contains
		// the objects of the last rendered frame.
		for (DepthBucket db: sortedObjectsToDraw) {
			for (AliteObject eo: db.sortedObjects) {
				if (eo instanceof SpaceObject && ((SpaceObject) eo).isInBay()) {
					return (SpaceObject) eo;
				}
			}
		}
		return null;
	}

	void setPaused(boolean p) {
		paused = p;
		spawnManager.setPaused(p);
		if (p) {
			if (oldMessage == null) {
				// If oldMessage is _not_ null, oldMessage already contains the
				// message before the pause; so, setPause is called twice with true.
				// (Can happen during state save/restore).
				oldMessage = message;
			}
			if (scrollingText == null) {
				scrollingText = new ScrollingText();
			}
			message = new OnScreenMessage();
			message.repeatText(L.string(R.string.msg_pause_game, AliteConfig.GAME_NAME), 5, -1, 3);
		} else {
			scrollingText = null;
			message.clearRepetition();
			message = oldMessage;
			oldMessage = null;
			calibrate();
		}
		for (TimedEvent te: timedEvents) {
			if (p) {
				te.pause();
			} else {
				te.resume();
			}
		}
		if (dockingComputerAI != null) {
			if (paused) {
				dockingComputerAI.pauseMusic();
			} else {
				dockingComputerAI.resumeMusic();
			}
		}
	}

	public void clearMessageRepetition() {
		message.clearRepetition();
	}

	public void forceForwardView() {
		viewDirection = PlayerCobra.DIR_FRONT;
		if (hud != null) {
			hud.setZoomFactor(1);
		}
	}

	private boolean killHyperspaceJump() {
		if (hyperspaceTimer != null) {
			hyperspaceTimer.pause();
			hyperspaceTimer.remove();
			hyperspaceTimer = null;
			return true;
		}
		return false;
	}

	public boolean toggleHyperspaceCountdown(boolean isIntergalactic) {
		if (killHyperspaceJump()) {
			message.setText(L.string(R.string.msg_hyperspace_jump_aborted));
			return false;
		}
		initialHyperspaceSystem = alite.getPlayer().getHyperspaceSystem();
		hyperspaceTimer = new HyperspaceTimer(this, isIntergalactic);
		timedEvents.add(hyperspaceTimer);
		return true;
	}

	public boolean isHyperspaceEngaged(boolean isIntergalactic) {
		return hyperspaceTimer != null && hyperspaceTimer.isIntergalactic() == isIntergalactic;
	}

	public void toggleCloaked() {
		ship.setCloaked(!ship.isCloaked());
		if (ship.isCloaked()) {
			cloakingEvent = new CloakingEvent(this);
			timedEvents.add(cloakingEvent);
		} else {
			cloakingEvent.pause();
			cloakingEvent.remove();
			cloakingEvent = null;
			message.clearRepetition();
		}
	}

	final void performHyperspaceJump(boolean isIntergalactic) {
		killHyperspaceJump();
		newScreen = new HyperspaceScreen(isIntergalactic);
		escapeWitchSpace();
		playerInSafeZone = false;
		alite.setTimeFactor(1);
		alite.getPlayer().setHyperspaceSystem(initialHyperspaceSystem);
	}

	public int getViewDirection() {
		return viewDirection;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void reduceShipEnergy(float i) {
		alite.getCobra().setEnergy(alite.getCobra().getEnergy() - i);
		laserManager.checkEnergyLow();
		if (alite.getCobra().getEnergy() <= 0) {
			gameOver();
		}
	}

	private void checkPromotion() {
		int score = alite.getPlayer().getScore();
		boolean promoted = false;
		while (score >= alite.getPlayer().getRating().getScoreThreshold() && alite.getPlayer().getRating().getScoreThreshold() > 0) {
			alite.getPlayer().setRating(Rating.values()[alite.getPlayer().getRating().ordinal() + 1]);
			promoted = true;
		}
		if (promoted) {
			message.setText(L.string(R.string.msg_right_on_commander));
		}
	}

	final void computeBounty(SpaceObject destroyedObject) {
		int bounty = destroyedObject.getBounty();
		alite.getPlayer().setCash(alite.getPlayer().getCash() + bounty);
		computeScore(destroyedObject);
		if (destroyedObject.getType() != ObjectType.CargoPod) {
			SoundManager.play(Assets.com_targetDestroyed);
			String bountyString = bounty == 0 ? L.string(R.string.bounty_none, destroyedObject.getName()) :
				L.string(R.string.bounty_amount, destroyedObject.getName(), L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, bounty));
			message.setDelayedText(bountyString);
		}
	}

	public final void computeScore(SpaceObject destroyedObject) {
		int points = destroyedObject.getScore();
		if (Settings.difficultyLevel == 0) {
			points >>= 1;
		} else if (Settings.difficultyLevel == 1) {
			points = (int) (points * 0.75f);
		} else if (Settings.difficultyLevel == 2) {
			points = (int) (points * 0.85f);
		} else if (Settings.difficultyLevel == 4) {
			points = (int) (points * 1.25f);
		} else if (Settings.difficultyLevel == 5) {
			points <<= 1;
		}
		AliteLog.d("Player kill", "Destroyed " + destroyedObject.getId() + " at Difficulty " + Settings.difficultyLevel + " for " + points + " points.");
		alite.getPlayer().setScore(alite.getPlayer().getScore() + points);
		checkPromotion();
		if (destroyedObject.getScore() > 0) {
			alite.getPlayer().increaseKillCount(1);
			if (alite.getPlayer().getKillCount() % 1024 == 0) {
				message.setText(L.string(R.string.msg_good_shooting_commander));
			} else if (alite.getPlayer().getKillCount() % 256 == 0) {
				message.setText(L.string(R.string.msg_right_on_commander));
			}
		}
	}

	public final void gameOver() {
		if (isDockingComputerActive()) {
			toggleDockingComputer(false);
		}
		killHyperspaceJump();
		message.clearRepetition();
		if (ship.getUpdater() instanceof GameOverUpdater) {
			return;
		}
		SoundManager.stop(Assets.energyLow);
		SoundManager.stop(Assets.criticalCondition);
		setPlayerControl(false);
		if (alite.getCurrentScreen() instanceof FlightScreen) {
			((FlightScreen) alite.getCurrentScreen()).setInformationScreen(null);
		}
		forceForwardView();
		killHud();
		ship.setUpdater(new GameOverUpdater(this, ship));
		alite.getPlayer().setCondition(Condition.DOCKED);
	}

	public void setHyperspaceHook(IMethodHook hyperspaceHook) {
		this.hyperspaceHook = hyperspaceHook;
	}

	public IMethodHook getHyperspaceHook() {
		return hyperspaceHook;
	}

	public void toggleECMJammer() {
		ship.setEcmJammer(!ship.isEcmJammer());
		if (ship.isEcmJammer()) {
			jammingEvent = new JammingEvent(this);
			timedEvents.add(jammingEvent);
		} else {
			jammingEvent.pause();
			jammingEvent.remove();
			jammingEvent = null;
			message.clearRepetition();
		}
	}

	void resetHud() {
		if (hud != null) {
			hud = new AliteHud(new IntFunction<Float>() {
				private static final long serialVersionUID = 1294719270286388523L;

				@Override
				public Float apply(int speed) {
					return ship.getSpeed();
				}
			});
			if (buttons != null) {
				buttons.reset();
			}
		}
	}

	WitchSpaceRender getWitchSpace() {
		return witchSpace;
	}

	void setNewScreen(AliteScreen screen) {
		newScreen = screen;
	}

	DockingComputerAI getDockingComputerAI() {
		return dockingComputerAI;
	}

	void clearMissileLock() {
		missileLock = null;
	}

	OnScreenMessage getMessage() {
		return message;
	}

	public SpaceObject getMissileLock() {
		return missileLock;
	}

	boolean isVipersWillEngage() {
		return vipersWillEngage;
	}

	void setVipersWillEngage() {
		vipersWillEngage = true;
	}
}