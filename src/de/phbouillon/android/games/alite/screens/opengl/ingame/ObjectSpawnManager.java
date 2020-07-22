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
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.opengl.Matrix;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.BoxSpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.*;
import de.phbouillon.android.games.alite.screens.opengl.sprites.AliteHud;

public class ObjectSpawnManager implements Serializable {
	private static final long serialVersionUID = -6732126311025754393L;

	private static final int DAMAGE_SPACE_STATION_DEFENCE = 15;

	public static boolean ASTEROIDS_ENABLED             = true;
	public static boolean CONDITION_RED_OBJECTS_ENABLED = true;
	public static boolean TRADERS_ENABLED               = true;
	public static boolean SHUTTLES_ENABLED              = true;
	public static boolean VIPERS_ENABLED                = true;
	public static boolean THARGOIDS_ENABLED             = true;
	public static boolean THARGONS_ENABLED              = true;

	private transient int maxNumberOfTradeShips;
	private transient int maxNumberOfAsteroids;
	private transient int maxNumberOfShuttles;
	private transient int maxNumberOfThargoids;
	private transient int maxNumberOfEnemies;
	private transient int maxNumberOfVipers;

	private static final long LAUNCH_BREAK_TIMER  = 15; // seconds

	private static final float SPAWN_INTER_SHIP_DISTANCE            = 1500.0f;
	private static final float TRANSPORT_PLANET_DISTANCE_SQ         = 7225000000.0f;

	private static final float[] spawnMatrix = new float[] {0, 0,  1, 0,   -1, 0,   0, -1,   0, 1,   1, 1,
		                                                 -2, -2,  2, 0,   -2, 0,   0, -2,   0, 2,   2, 2};

	private static final float[] thargonSpawnMatrix = new float[] {0, -1,  0.7071067f, -0.7071067f,  1, 0,  0.7071067f, 0.7071067f,
																	 0,  1, -0.7071067f,  0.7071067f, -1, 0, -0.7071067f, -0.7071067f};
	private transient Alite alite;
	private InGameManager inGame;
	private SpawnTimer conditionRedTimer = new SpawnTimer();
	private SpawnTimer traderTimer = new SpawnTimer();
	private SpawnTimer asteroidTimer = new SpawnTimer();
	private SpawnTimer shuttleOrTransportTimer = new SpawnTimer();
	private SpawnTimer viperTimer = new SpawnTimer();
	private transient BoxSpaceObject gateWatcher;

	private final Timer launchTime = new Timer();

	private static final Vector3f vector = new Vector3f(0, 0, 0);
	private static final Vector3f vector2 = new Vector3f(0, 0, 0);
	private static final Vector3f vector3 = new Vector3f(0, 0, 0);
	private final float[] matrixCopy = new float[16];

	private boolean torus;
	private SystemData system;
	private boolean timedEventsMustBeInitialized = true;
	private int launchAreaViolations = 0;
	private final Timer lastLogTime = new Timer().setAutoReset();

	private class SpawnTimer implements Serializable {
		private static final long serialVersionUID = -4989102775350929292L;

		private transient TimedEvent event;
		private boolean paused = false;
		private long pauseTime = -1;
		private long lastExecutionTime = -1;

		private void clearTimes() {
			pauseTime         = -1;
			lastExecutionTime = -1;
		}

		private void setTimes() {
			if (event == null) {
				return;
			}
			pauseTime = event.pause();
			lastExecutionTime = event.getLastExecutionTime();
		}

		private void initialize(long delay, final IMethodHook method) {
			event = new TimedEvent(delay, lastExecutionTime, pauseTime);
			event.addAlarmEvent(method);
			clearTimes();
		}

		private void pause(boolean b) {
			if (event != null && paused != b) {
				if (b) {
					event.pause();
				} else {
					event.resume();
				}
				paused = b;
			}
		}
	}

	ObjectSpawnManager(final InGameManager inGame) {
		alite = Alite.get();
		this.inGame = inGame;
		initializeLimits();
	}

	boolean needsInitialization() {
		return timedEventsMustBeInitialized;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		conditionRedTimer.setTimes();
		traderTimer.setTimes();
		asteroidTimer.setTimes();
		shuttleOrTransportTimer.setTimes();
		viperTimer.setTimes();
		out.defaultWriteObject();
	}


	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "ObjectSpawnManager.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "ObjectSpawnManager.readObject I");
			alite = Alite.get();
			conditionRedTimer = new SpawnTimer();
			traderTimer = new SpawnTimer();
			asteroidTimer = new SpawnTimer();
			shuttleOrTransportTimer = new SpawnTimer();
			viperTimer = new SpawnTimer();
			timedEventsMustBeInitialized = true;
			gateWatcher = new BoxSpaceObject("GateWatcher", 510, 510, 100);
			AliteLog.d("readObject", "ObjectSpawnManager.readObject II");
			initializeLimits();
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	private void initializeLimits() {
		int d = Settings.difficultyLevel;
		maxNumberOfTradeShips = 2;
		maxNumberOfAsteroids = 4;
		maxNumberOfShuttles = 2;
		maxNumberOfThargoids = d == 0 ? 0 : d == 1 ? 0 : d == 2 ? 1 : d == 3 ? 4 : d == 4 ? 8 : 10;
		maxNumberOfEnemies = d == 0 ? 0 : d == 1 ? 2 : d == 2 ? 5 : d == 3 ? 8 : d == 4 ? 12 : 20;
		maxNumberOfVipers = d == 0 ? 1 : d == 1 ? 1 : d == 2 ? 3 : d == 3 ? 8 : d == 4 ? 12 : 20;
	}

	void initTimedEvents(InGameManager inGame) {
		if (conditionRedTimer == null || traderTimer == null || asteroidTimer == null || shuttleOrTransportTimer == null || viperTimer == null) {
			return;
		}
		timedEventsMustBeInitialized = false;
		alite = Alite.get();
		if (Alite.get().getPlayer() == null) {
			Alite.get().initialize();
		}
		if (alite.getPlayer().getCurrentSystem() == SystemData.RAXXLA_SYSTEM) {
			return;
		}
		if (conditionRedTimer.event != null) {
			conditionRedTimer.event.remove();
		}
		if (traderTimer.event != null) {
			traderTimer.event.remove();
		}
		if (asteroidTimer.event != null) {
			asteroidTimer.event.remove();
		}
		if (shuttleOrTransportTimer.event != null) {
			shuttleOrTransportTimer.event.remove();
		}
		if (viperTimer.event != null) {
			viperTimer.event.remove();
		}
		long delayToConditionRedEncounter = getDelayToConditionRedEncounter();
		long delayToTraderEncounter = getDelayToTraderEncounter();
		long delayToAsteroidEncounter = getDelayToAsteroidEncounter();
		long delayToShuttleOrTransporterEncounter = getDelayToShuttleEncounter();
		long delayToViperEncounter = getDelayToViperEncounter();
		this.inGame = inGame;
		if (delayToConditionRedEncounter > 0) {
			AliteLog.d("CRE", "CRE Timer, initializing");
			conditionRedTimer.initialize(delayToConditionRedEncounter, new IMethodHook() {
				private static final long serialVersionUID = 2793174977898387221L;

				@Override
				public void execute(float deltaTime) {
					spawnConditionRedObject();
				}
			});
		}
		AliteLog.d("Trader", "Trader Timer, initializing");
		traderTimer.initialize(delayToTraderEncounter, new IMethodHook() {
			private static final long serialVersionUID = 9039974139789907685L;

			@Override
			public void execute(float deltaTime) {
				spawnTrader();
			}
		});
		AliteLog.d("Asteroid", "Asteroid Timer, initializing");
		asteroidTimer.initialize(delayToAsteroidEncounter, new IMethodHook() {
			private static final long serialVersionUID = 5843714386316634097L;

			@Override
			public void execute(float deltaTime) {
				spawnAsteroid();

			}
		});
		AliteLog.d("SOT", "SOT Timer, initializing");
		shuttleOrTransportTimer.initialize(delayToShuttleOrTransporterEncounter, new IMethodHook() {
			private static final long serialVersionUID = -8088231773858070919L;

			@Override
			public void execute(float deltaTime) {
				spawnShuttle();
			}
		});
		AliteLog.d("Viper", "Viper Timer, initializing");
		viperTimer.initialize(delayToViperEncounter, new IMethodHook() {
			private static final long serialVersionUID = 6529856113983841949L;

			@Override
			public void execute(float deltaTime) {
				spawnViper();
			}
		});
		for (Mission mission : MissionManager.getInstance().getActiveMissions()) {
			TimedEvent te = inGame.isWitchSpace() ? mission.getWitchSpaceSpawnEvent(this) : mission.getSpawnEvent(this);
			if (te != null) {
				inGame.addTimedEvent(te);
			}
			te = mission.getConditionRedSpawnReplacementEvent(this);
			if (te != null) {
				conditionRedTimer.event = te;
			}
			te = mission.getTraderSpawnReplacementEvent(this);
			if (te != null) {
				traderTimer.event = te;
			}
			te = mission.getAsteroidSpawnReplacementEvent(this);
			if (te != null) {
				asteroidTimer.event = te;
			}
			te = mission.getShuttleSpawnReplacementEvent(this);
			if (te != null) {
				shuttleOrTransportTimer.event = te;
			}
			te = mission.getViperSpawnReplacementEvent(this);
			if (te != null) {
				viperTimer.event = te;
			}
		}
		if (inGame != null) {
			if (conditionRedTimer.event != null) {
				inGame.addTimedEvent(conditionRedTimer.event);
			}
			inGame.addTimedEvent(traderTimer.event);
			inGame.addTimedEvent(asteroidTimer.event);
			inGame.addTimedEvent(shuttleOrTransportTimer.event);
			inGame.addTimedEvent(viperTimer.event);
		}
		alite.setTimeFactorChangeListener((oldTimeFactor, newTimeFactor) -> updateTimers(oldTimeFactor / (float) newTimeFactor));
	}

	private void updateTimerInternal(float factor, SpawnTimer timer) {
		if (timer != null && timer.event != null) {
			timer.event.updateDelay((long) (timer.event.timeToNextTrigger() * factor));
		}
	}

	private void updateTimers(float factor) {
		updateTimerInternal(factor, conditionRedTimer);
		updateTimerInternal(factor, traderTimer);
		updateTimerInternal(factor, asteroidTimer);
		updateTimerInternal(factor, shuttleOrTransportTimer);
		updateTimerInternal(factor, viperTimer);

	}

	public void lockConditionRedEvent() {
		if (conditionRedTimer.event != null) {
			conditionRedTimer.event.lock();
		}
	}

	public void unlockConditionRedEvent() {
		if (conditionRedTimer.event != null) {
			conditionRedTimer.event.updateDelay(getDelayToConditionRedEncounter());
			conditionRedTimer.event.unlock();
		}
	}

	void startSimulation(SystemData system) {
		this.system = system;
		torus = false;
		gateWatcher = new BoxSpaceObject("GateWatcher", 510, 510, 100);
		initTimedEvents(inGame);
		launchAreaViolations = 0;
	}

	private long getDelayToConditionRedEncounter() {
		return system == null ? 0 : (long) ((system.getGovernment().ordinal() + 2 << 9) / 16.7f * 1000000000L);
	}

	private long getDelayToTraderEncounter() {
		return (long) ((200.0f + 100.0f * Math.random()) / 16.7f * 1000000000L);
	}

	private long getDelayToAsteroidEncounter() {
		return (long) ((200.0f + 100.0f * Math.random()) / 16.7f * 1000000000L);
	}

	public long getDelayToShuttleEncounter() {
		return (long) ((200.0f + 100.0f * Math.random()) / 16.7f * 1000000000L);
	}

	public long getDelayToViperEncounter() {
		return (long) (200.0f / 16.7f * 1000000000L);
	}

	public InGameManager getInGameManager() {
		return inGame;
	}

//	public void setPauseConditionRedEvent(boolean b) {
//		conditionRedTimer.pause(b);
//	}

	public void enterTorus() {
		SoundManager.repeat(Assets.torus);
		torus = true;
		if (conditionRedTimer.event == null) {
			return;
		}
		conditionRedTimer.event.updateDelay((long) (30L * 1000000000L / 16.7));
	}

	public void leaveTorus() {
		SoundManager.stop(Assets.torus);
		torus = false;
		if (conditionRedTimer.event != null) {
			conditionRedTimer.event.updateDelay((long) ((system.getGovernment().ordinal() + 2 << 9) / 16.7f * 1000000000L));
		}
		inGame.yankOutOfTorus();
	}

	public boolean isInTorus() {
		return torus;
	}

	private Vector3f getSpawnPosition() {
		MathHelper.setRandomDirection(vector);
		Vector3f spawnPosition = MathHelper.getRandomPosition(inGame.getShip().getPosition(), vector,
			(float) (16384 + 8192 * Math.random()), 1000.0f);
		spawnPosition.copy(vector);
		return spawnPosition;
	}

	private void spawnEnemyAndAttackPlayerWithoutCallback(final SpaceObject ship, int index, Vector3f spawnPosition) {
		spawnEnemyAndAttackPlayerWithoutCallback(ship, index, spawnPosition, spawnMatrix);
	}

	private void spawnEnemyAndAttackPlayerWithoutCallback(final SpaceObject ship, int index, Vector3f spawnPosition, float[] spawnMatrix) {
		ship.setPosition(vector);
		ship.orientTowards(inGame.getShip(), 0);
		inGame.addObject(ship);
		ship.assertOrthoNormal();
		ship.setAIState(SpaceObjectAI.AI_STATE_ATTACK);
		if (index < 12) {
			vector.x = spawnPosition.x + spawnMatrix[index * 2] * SPAWN_INTER_SHIP_DISTANCE;
			vector.y = spawnPosition.y + spawnMatrix[index * 2 + 1] * SPAWN_INTER_SHIP_DISTANCE;
		}
	}

	public void spawnEnemyAndAttackPlayer(final SpaceObject... ships) {
		int index = 0;
		Vector3f spawnPosition = getSpawnPosition();
		for (SpaceObject so : ships) {
			spawnEnemyAndAttackPlayer(so, index, spawnPosition);
			index++;
		}
	}

	private void spawnEnemyAndAttackPlayer(final SpaceObject ship, int index, Vector3f spawnPosition) {
		spawnEnemyAndAttackPlayerWithoutCallback(ship, index, spawnPosition);
		ship.addDestructionCallback(new IMethodHook() {
			private static final long serialVersionUID = -3628212229724389819L;

			@Override
			public void execute(float deltaTime) {
				if (inGame.getNumberOfObjects(so -> ObjectType.isEnemyShip(so.getType())) == 0 && conditionRedTimer.event != null) {
					unlockConditionRedEvent();
				}
			}

		});
	}

	void spawnThargoidInWitchSpace() {
		if (inGame.getWitchSpace() == null) {
			return;
		}
		SpaceObject thargoid = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Thargoid);
		thargoid.setSpawnDroneDistanceSq(computeSpawnThargonDistanceSq());
		spawnEnemyAndAttackPlayerWithoutCallback(thargoid, 0, getSpawnPosition());
		thargoid.addDestructionCallback(new IMethodHook(){
			private static final long serialVersionUID = -5841075572165732277L;

			public void execute(float deltaTime) {
				if (inGame.getWitchSpace() != null) {
					inGame.getWitchSpace().increaseWitchSpaceKillCounter();
				}
				long secondsToSpawn = (long) ((Math.random() * 6 + 6) * 1000000000L);
				TimedEvent event = new TimedEvent(secondsToSpawn);
				inGame.addTimedEvent(event.addAlarmEvent(new IMethodHook() {
					private static final long serialVersionUID = -8236066205369429808L;

					@Override
					public void execute(float deltaTime) {
						spawnThargoidInWitchSpace();
						event.remove();
					}
				}));
			}

		});
	}

	private void spawnConditionRedObject() {
		if (inGame.getWitchSpace() != null || conditionRedTimer.event == null) {
			return;
		}
		if (!THARGOIDS_ENABLED && !THARGONS_ENABLED && !CONDITION_RED_OBJECTS_ENABLED) {
			return;
		}
		if (Settings.disableAttackers) {
			return;
		}
		if (!inGame.isPlayerAlive()) {
			return;
		}
		if (InGameManager.playerInSafeZone) {
			return;
		}
		int thargoidNum = checkSpawnThargoid();
		int thargoids = inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Thargoid);
		if (thargoidNum + thargoids > maxNumberOfThargoids) {
			thargoidNum = maxNumberOfThargoids - thargoids;
		}
		int num = calculateNumberOfObjectsToSpawn();
		int enemies = inGame.getNumberOfObjects(so -> ObjectType.isEnemyShip(so.getType()));
		if (num + enemies > maxNumberOfEnemies) {
			num = maxNumberOfEnemies - enemies;
		}
		if (thargoidNum + num <= 0 || !CONDITION_RED_OBJECTS_ENABLED) {
			return;
		}
		if (torus) {
			int randByte = (int) (Math.random() * 256);
			if (system.getGovernment().ordinal() << 5 > randByte) {
				return;
			}
			leaveTorus();
		}
		conditionRed();
		if (thargoidNum > 0 && THARGOIDS_ENABLED) {
			conditionRedTimer.event.lock();
			spawnThargoids(thargoidNum);
			return;
		}
		conditionRedTimer.event.lock();
		Vector3f spawnPosition = getSpawnPosition();
		for (int i = 0; i < num; i++) {
			SpaceObject ship = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Pirate);
			spawnEnemyAndAttackPlayer(ship, i, spawnPosition);
		}
	}

	public void conditionRed() {
		SoundManager.play(Assets.com_conditionRed);
		inGame.repeatMessage(L.string(R.string.com_condition_red), 3);
	}

	public void spawnThargoids(int thargoidNum) {
		Vector3f spawnPosition = getSpawnPosition();
		for (int i = 0; i < thargoidNum; i++) {
			SpaceObject thargoid = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Thargoid);
			thargoid.setSpawnDroneDistanceSq(computeSpawnThargonDistanceSq());
			spawnEnemyAndAttackPlayer(thargoid, i, spawnPosition);
		}
	}

	public long computeSpawnThargonDistanceSq() {
		if ((int) (Math.random() * 5) < alite.getPlayer().getRating().ordinal()) {
			long d = (long) (8192 + 8192 * Math.random());
			return d * d;
		}
		return -1;
	}

	private int checkSpawnThargoid() {
		if (Settings.difficultyLevel < 2) {
			return 0;
		}
		int prob = alite.getPlayer().getRating().ordinal() * Settings.difficultyLevel;
		int maxProb = 20 + 5 * (Settings.difficultyLevel - 3);
		int minNum = 1;
		int maxNum = 2 + Settings.difficultyLevel - 3;

		if ((int) (Math.random() * 100) < Math.min(prob, maxProb)) {
			return alite.getPlayer().getRating().ordinal() < 5 ? 1 : Math.random() < 0.8 ? minNum : maxNum;
		}
		return 0;
	}

	private boolean isLaunchFromStationSafe(boolean trader) {
		if (!InGameManager.playerInSafeZone) {
			return false;
		}
		if (!launchTime.hasPassedSeconds(LAUNCH_BREAK_TIMER)) {
			return false;
		}
		// Vipers fly out of the docking port, so it is forbidden for all other ships ;)
		if (trader && inGame.isVipersWillEngage()) {
			return false;
		}
		if (inGame.getShipInDockingBay() != null) {
			return false;
		}

		float calcSpeed = inGame.getShip().getSpeed();
		if (calcSpeed >= 0) {
			calcSpeed = -20.0f;
		}
		inGame.getSystemStationPosition().copy(vector);
		inGame.getStation().getForwardVector().copy(vector2);
		vector2.scale(940);
		// Vector holds the center of the collision sphere in front of the station
		vector.add(vector2);
		gateWatcher.setPosition(vector);

		// Vector2 holds the forward vector of the ship (negated, so that the reference system is correct)
		// or, if the ship is (almost) at a standstill, we take the vector from ship to docking bay
		if (calcSpeed >= -20.0f) {
			vector.sub(inGame.getShip().getPosition(), vector2);
			vector2.normalize();
			AliteLog.d("Forward vector", "FV (speed near 0): " + vector2);
		} else {
			inGame.getShip().getForwardVector().copy(vector2);
			vector2.negate();
			AliteLog.d("Forward vector", "FV (speed normal): " + vector2);
		}
		float intersectionDistance = LaserManager.computeIntersectionDistance(vector2, inGame.getShip().getPosition(), vector, 580, vector3);
		float travelDistance = -calcSpeed * 8.5f;
		float distShipStationSq = inGame.getShip().getPosition().distanceSq(vector);
		AliteLog.d("Intersection check", "Intersection distance: " + intersectionDistance +
			", travelDistance: " + travelDistance + ", Speed: " + calcSpeed +
			", Distance: " + inGame.getShip().getPosition().distance(vector));
		if (intersectionDistance >= 0 && distShipStationSq < 640000) {
			if (calcSpeed > -40.0f) {
				handleLaunchAreaViolations();
			}
			return false;
		}
		return intersectionDistance <= 0 || intersectionDistance > travelDistance ||
			!gateWatcher.intersect(inGame.getShip().getPosition(), vector2);
	}

	private void handleLaunchAreaViolations() {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		if (launchAreaViolations == 0) {
			SoundManager.play(Assets.com_launch_area_violation_1st);
			inGame.setMessage(L.string(R.string.com_launch_area_violation_1st));
			AliteLog.d("Receive warning", "Hey rookie! Clear the launch area immediately.");
			launchAreaViolations++;
			if (alite.getPlayer().getLegalStatus() != LegalStatus.CLEAN) {
				launchAreaViolations++;
			}
			launchTime.reset();
		} else if (launchAreaViolations == 1) {
			SoundManager.play(Assets.com_launch_area_violation_2nd);
			inGame.setMessage(L.string(R.string.com_launch_area_violation_2nd));
			AliteLog.d("Receive warning", "Clear the launch area immediately, or we will open fire.");
			launchAreaViolations++;
			launchTime.reset();
		} else {
			SoundManager.play(Assets.com_launch_area_violation_3rd);
			inGame.setMessage(L.string(R.string.com_launch_area_violation_3rd));
			inGame.getStation().denyAccess();
			alite.getPlayer().setLegalValue(alite.getPlayer().getLegalValue() + 4);
			inGame.getShip().setUpdater(new IMethodHook() {
				private static final long serialVersionUID = 4046742301009349763L;
				private final Timer lastExecution = new Timer().setAutoResetWithImmediateAtFirstCall();

				@Override
				public void execute(float deltaTime) {
					if (lastExecution.hasPassedSeconds(1)) {
						float distance = inGame.getShip().getPosition().distance(inGame.getSystemStationPosition());
						AliteLog.d("Distance to Station", "Distance to Station: " + distance);
						if (distance > 4000) {
							inGame.getShip().setUpdater(null);
							return;
						}
						inGame.getLaserManager().damageShip(DAMAGE_SPACE_STATION_DEFENCE, true);
						alite.getCobra().increaseHitBySpaceStation();
					}
				}
			});
		}
	}

	private void spawnTrader() {
		traderTimer.event.updateDelay((long) ((600.0f + 300.0f * Math.random() / 16.7f) * 1000000000L / (float) alite.getTimeFactor()));
		if (inGame.getWitchSpace() != null) {
			return;
		}
		if (inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Trader) >= maxNumberOfTradeShips ||
				!TRADERS_ENABLED || Settings.disableTraders) {
			return;
		}
		if (InGameManager.playerInSafeZone) {
			if (!isLaunchFromStationSafe(true)) {
				return;
			}
			launchFromBay(createRandomTraderWithoutAnaconda(), new AiStateCallbackHandler() {
				private static final long serialVersionUID = -5203119902007110520L;

				@Override
				public void execute(SpaceObject so) {
					so.setInBay(false);
					so.setUpdater(new IMethodHook() {
						private static final long serialVersionUID = 118694905617185715L;

						@Override
						public void execute(float deltaTime) {
							if (Math.abs(so.getPosition().z - inGame.getShip().getPosition().z) >= 16384 + 8192) {
								so.setRemove(true);
							}
						}
					});
					so.adjustSpeed(-so.getMaxSpeed());
					so.setAIState(SpaceObjectAI.AI_STATE_FLY_STRAIGHT);
				}
			});
			return;
		}
		final SpaceObject ship = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Trader);
		ship.setPosition(getSpawnPosition());
		ship.setRandomOrientation(inGame.getShip().getUpVector());
		ship.setAIState(SpaceObjectAI.AI_STATE_GLOBAL);
		ship.adjustSpeed(-ship.getMaxSpeed());
		ship.setAIState(SpaceObjectAI.AI_STATE_FLY_STRAIGHT);
		ship.setUpdater(new IMethodHook() {
			private static final long serialVersionUID = -2146899348570326187L;

			@Override
			public void execute(float deltaTime) {
				if (Math.abs(ship.getPosition().z - inGame.getShip().getPosition().z) >= 16384 + 8192) {
					ship.setRemove(true);
				}
			}
		});
		inGame.addObject(ship);
	}

	// The Anaconda is too big for the station, so -- better not let it fly from its docking bay :)
	private SpaceObject createRandomTraderWithoutAnaconda() {
		while (true) {
			final SpaceObject ship = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Trader);
			if (!"Anaconda".equals(ship.getId())) {
				return ship;
			}
		}
	}

	public void launchFromBay(final SpaceObject so, final AiStateCallbackHandler callback) {
		so.setInBay(true);
		so.setPosition(inGame.getSystemStationPosition());
		inGame.getStation().getRightVector().copy(vector);
		vector.negate();
		so.setUpVector(vector);
		so.setForwardVector(inGame.getStation().getForwardVector());
		so.setRightVector(inGame.getStation().getUpVector());
		so.applyDeltaRotation(180, 0, 0);
		inGame.getStation().getForwardVector().copy(vector);
		vector.scale(1000);
		vector.add(inGame.getStation().getPosition());
		so.setWaypoint(WayPoint.newWayPoint(vector, so.getUpVector()));
		so.setUpdater(new IMethodHook() {
			private static final long serialVersionUID = 9195512054255867095L;

			@Override
			public void execute(float deltaTime) {
				Matrix.rotateM(matrixCopy, 0, inGame.getStation().getMatrix(), 0,
					(float) Math.toDegrees(inGame.getStation().getSpaceStationRotationSpeed()), 0, 0, 1);
				vector.x = -matrixCopy[0];
				vector.y = -matrixCopy[1];
				vector.z = -matrixCopy[2];
				so.applyDeltaRotation(180, 0, 0);
				so.orientTowards(inGame.getStation(), vector, deltaTime);
				so.applyDeltaRotation(180, 0, 0);
			}
		});
		so.registerAiStateCallbackHandler(AiStateCallback.EndOfWaypointsReached, callback);
		inGame.addObject(so);
		launchTime.reset();
	}

	private void spawnAsteroid() {
		asteroidTimer.event.updateDelay((long) ((600.0f + 300.0f * Math.random()) / 16.7f * 1000000000L / (float) alite.getTimeFactor()));
		if (inGame.getWitchSpace() != null) {
			return;
		}
		if (inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Asteroid) >= maxNumberOfAsteroids || !ASTEROIDS_ENABLED) {
			return;
		}
		if (InGameManager.playerInSafeZone) {
			// Do not spawn asteroids in safe zone; debatable...
			return;
		}
		final SpaceObject asteroid = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Asteroid);
		spawnObject(asteroid, asteroid.getMaxSpeed(), getSpawnPosition());
	}

	public void spawnTumbleObject(final SpaceObject createdObject, Vector3f position) {
		spawnObject(createdObject, 0.2f + (createdObject.getMaxSpeed() - 0.2f) * (float) Math.random(), position);
	}

	private void spawnObject(SpaceObject createdObject, float speed, Vector3f position) {
		createdObject.setPosition(position);
		createdObject.setSpeed(0);

		createRandomVector();
		// preserve values of vector2
		final float rx = vector2.x;
		final float ry = vector2.y;
		final float rz = vector2.z;

		createRandomVector();
		// preserve values of vector2
		final float ix = vector2.x;
		final float iy = vector2.y;
		final float iz = vector2.z;

		createdObject.setUpdater(new IMethodHook() {
			private static final long serialVersionUID = 5578311067399465378L;

			@Override
			public void execute(float deltaTime) {
				createdObject.setPosition(createdObject.getPosition().x + ix * speed * deltaTime,
					createdObject.getPosition().y + iy * speed * deltaTime,
					createdObject.getPosition().z + iz * speed * deltaTime);
				createdObject.applyDeltaRotation(rx, ry, rz);
				if (createdObject.getPosition().distance(inGame.getShip().getPosition()) > AliteHud.MAX_DISTANCE) {
					createdObject.setRemove(true);
				}
			}
		});
		inGame.addObject(createdObject);
	}

	private static void createRandomVector() {
		vector2.x = (float) (-2.0 + Math.random() * 4.0);
		vector2.y = (float) (-2.0 + Math.random() * 4.0);
		vector2.z = (float) (-2.0 + Math.random() * 4.0);
		vector2.normalize();
	}

	private void spawnShuttle() {
		shuttleOrTransportTimer.event.updateDelay((long) ((600.0f + 300.0f * Math.random()) / 16.7f * 1000000000L / (float) alite.getTimeFactor()));
		if (inGame.getWitchSpace() != null || !InGameManager.playerInSafeZone) {
			return;
		}
		if (!SHUTTLES_ENABLED) {
			return;
		}
		if (!isLaunchFromStationSafe(true)) {
			return;
		}
		if (inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Shuttle) >= maxNumberOfShuttles) {
			return;
		}
		final SpaceObject shuttle = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Shuttle);
		launchFromBay(shuttle, new AiStateCallbackHandler() {
			private static final long serialVersionUID = 2203774721593552494L;

			@Override
			public void execute(SpaceObject so) {
				shuttle.setUpdater(null);
				shuttle.setInBay(false);
				shuttle.setWaypoint(WayPoint.newWayPoint(FlightScreen.PLANET_POSITION, shuttle.getUpVector()));
				shuttle.setUpdater(new IMethodHook() {
					private static final long serialVersionUID = 2702506138360802890L;

					@Override
					public void execute(float deltaTime) {
						if (shuttle.getPosition().distanceSq(FlightScreen.PLANET_POSITION) < TRANSPORT_PLANET_DISTANCE_SQ) {
							shuttle.setRemove(true);
						}
					}
				});
			}
		});
	}

	private void spawnViper() {
		if (lastLogTime.hasPassedSeconds(1)) {
			AliteLog.d("Spawn Viper", "SafeZoneViolated: " + InGameManager.safeZoneViolated +
				", VipersWillEngage: " + inGame.isVipersWillEngage());
		}
		if (inGame.getWitchSpace() != null) {
			return;
		}
		if (alite.getPlayer().getLegalStatus() == LegalStatus.CLEAN) {
			return;
		}
		int numberOfPolice = inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Police);
		if (numberOfPolice >= maxNumberOfVipers || !VIPERS_ENABLED) {
			return;
		}
		if (!isLaunchFromStationSafe(false)) {
			return;
		}
		int maxNumberOfVipers = alite.getPlayer().getRating().ordinal() + 1;
		if (alite.getPlayer().getLegalStatus() == LegalStatus.FUGITIVE) {
			maxNumberOfVipers <<= 1;
		}
		numberOfPolice += inGame.getNumberOfObjects(so -> so.getType() == ObjectType.Defender);
		if (numberOfPolice >= maxNumberOfVipers) {
			return;
		}
		if (!inGame.isPlayerAlive()) {
			return;
		}

		SystemData currentSystem = alite.getPlayer().getCurrentSystem();
		ObjectType shipType = ObjectType.Police;
		if (currentSystem != null) {
			boolean playerIsCurrentlyOffensive = InGameManager.safeZoneViolated;
			if (!playerIsCurrentlyOffensive && inGame.getStation() != null) {
				if (inGame.getStation().getHitCount() > 1) {
					playerIsCurrentlyOffensive = true;
				}
			}
			Government government = currentSystem.getGovernment();
			if (!playerIsCurrentlyOffensive && (government == Government.ANARCHY ||
					government == Government.FEUDAL || !inGame.isVipersWillEngage())) {
				return;
			}
			inGame.setVipersWillEngage();
			if (government == Government.ANARCHY || government == Government.FEUDAL) {
				shipType = ObjectType.Defender;
			}
			Condition conditionOld = alite.getPlayer().getCondition();
			alite.getPlayer().setCondition(Condition.RED);
			if (conditionOld != Condition.RED) {
				conditionRed();
			}
		}

		final SpaceObject viper = SpaceObjectFactory.getInstance().getRandomObjectByType(shipType);
		viper.setIgnoreSafeZone();
		launchFromBay(viper, new AiStateCallbackHandler() {
			private static final long serialVersionUID = -2431074037545740323L;

			@Override
			public void execute(SpaceObject so) {
				viper.setUpdater(null);
				viper.setInBay(false);
				viper.setAIState(SpaceObjectAI.AI_STATE_ATTACK);
			}
		});
	}

	private int calculateNumberOfObjectsToSpawn() {
		int d = Settings.difficultyLevel;
		int result = (int) (Math.random() * (alite.getPlayer().getRating().ordinal() + 3 >> 1)) + 1;
		if (d > 3) {
			result += d - 3;
		}
		result += alite.getPlayer().getLegalStatus().ordinal() * (Math.random() < 0.05 ? 1 : 0);
		if (alite.getPlayer().getCurrentSystem() != null && alite.getPlayer().getCurrentSystem().getGovernment().ordinal() < 2) {
			result += Math.random() < 0.05 ? 1 : 0;
		}
		return Math.min(result, d == 0 ? 0 : d == 1 ? 2 : d == 2 ? 3 : d == 3 ? 4 : d == 4 ? 6 : 12);
	}

	void spawnThargons(final SpaceObject mother) {
		if (!THARGONS_ENABLED) {
			return;
		}
		int numberOfThargons = (int) (Math.random() * (mother.getMaxDrones() - mother.getMinDrones() + 1) + mother.getMinDrones());
		mother.getForwardVector().copy(vector);
		vector.scale(40);
		vector.add(mother.getPosition());
		vector.copy(vector2);
		for (int i = 0; i < numberOfThargons; i++) {
			final SpaceObject thargon = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Thargon);
			spawnEnemyAndAttackPlayerWithoutCallback(thargon, i, vector2, thargonSpawnMatrix);
			mother.addActiveDrone(thargon);
			thargon.addDestructionCallback(new IMethodHook() {
				private static final long serialVersionUID = -7756830551169201213L;

				@Override
				public void execute(float deltaTime) {
					if (mother.getHullStrength() > 0) {
						mother.removeActiveDrone(thargon);
					}
				}

			});
			mother.addDestructionCallback(new IMethodHook() {
				private static final long serialVersionUID = -2344244194022201965L;

				@Override
				public void execute(float deltaTime) {
					for (SpaceObject drone: mother.getActiveDrones()) {
						if (drone.getHullStrength() > 0) {
							drone.adjustSpeed(drone.getSpeed());
							drone.setAIState(SpaceObjectAI.AI_STATE_FLY_STRAIGHT);
						}
					}
				}

			});
		}
	}

	public void setPaused(boolean p) {
		conditionRedTimer.pause(p);
		traderTimer.pause(p);
		asteroidTimer.pause(p);
		shuttleOrTransportTimer.pause(p);
		viperTimer.pause(p);
	}
}
