package de.phbouillon.android.games.alite.model.missions;

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

import java.io.*;

import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ThargoidStationScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.*;

public class ThargoidStationMission extends Mission implements Serializable {
	private static final long serialVersionUID = 4664546653830973675L;

	public static final int ID = 5;
	public static final String ALIEN_SPACE_STATION = "Alien Space Station";

	private TimedEvent conditionRedEvent;

	class LaunchThargoidFromStationEvent extends TimedEvent {
		private static final long serialVersionUID = 8124490360245596874L;

		LaunchThargoidFromStationEvent(final ObjectSpawnManager spawnManager, long delayInNanoSeconds, int numberOfThargoidsToSpawn) {
			super(delayInNanoSeconds);
			addAlarmEvent(new IMethodHook() {
				private static final long serialVersionUID = -519339357944058227L;
				@Override
				public void execute(float deltaTime) {
					if (numberOfThargoidsToSpawn == 0 || state != 2) {
						return;
					}
					final SpaceObject thargoid = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Thargoid);
					spawnManager.launchFromBay(thargoid, new AiStateCallbackHandler() {
						private static final long serialVersionUID = 3546094888645182119L;

						@Override
						public void execute(SpaceObject so) {
							thargoid.setUpdater(null);
							thargoid.setInBay(false);
							thargoid.setIgnoreSafeZone();
							thargoid.setAIState(AIState.ATTACK, spawnManager.getInGameManager().getShip());
						}
					});
				}
			});
		}
	}

	public ThargoidStationMission(Alite alite) {
		super(alite, ID);
	}

	@Override
	protected boolean checkStart(Player player) {
		return player.getCompletedMissions().contains(MissionManager.getInstance().get(CougarMission.ID)) &&
			player.getIntergalacticJumpCounter() + player.getJumpCounter() >= 64;
	}

	@Override
	protected void acceptMission(boolean accept) {
		// The player can't decline this mission...
		alite.getPlayer().addActiveMission(this);
		state = 1;
	}

	@Override
	public void onMissionComplete() {
		active = false;
		alite.getCobra().addEquipment(EquipmentStore.ecmJammer);
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new ThargoidStationScreen(alite, 0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		if (alite.getPlayer().getCondition() != Condition.DOCKED || state < 1 || !started || !active) {
			return null;
		}
		if (state == 3) {
			return new ThargoidStationScreen(alite, 1);
		}
		return null;
	}


	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		boolean result = positionMatchesTarget();
		if ((state != 1 || result) && (state != 2 || !result)) {
			return null;
		}
		// !result => Any system but the one where the player received the mission
		// result => The precise system where the Alien Space Station
		// was when the player first saw it. He escaped and now tries again...
		state = 2;
		TimedEvent event = new TimedEvent(10000000000L);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 5516217861394636289L;

			@Override
			public void execute(float deltaTime) {
				SpaceObject station = manager.getInGameManager().getStation();
				station.setId(ALIEN_SPACE_STATION);
				station.setProperty(SpaceObject.Property.name, L.string(R.string.thargoid_station_name));
				station.setHullStrength(1024);
				station.denyAccess();
				station.addDestructionCallback(2, new IMethodHook() {
					private static final long serialVersionUID = 6715650816893032921L;

					@Override
					public void execute(float deltaTime) {
						state = 3;
					}

				});
				event.remove();
			}
		});
	}

	private void spawnThargoids(final ObjectSpawnManager manager) {
		if (manager.isInTorus()) {
			manager.leaveTorus();
		}
		SoundManager.play(Assets.com_conditionRed);
		manager.getInGameManager().repeatMessage(L.string(R.string.com_condition_red), 3);
		conditionRedEvent.pause();
		Vector3f spawnPosition = manager.getSpawnPosition();
		int thargoidNum = alite.getPlayer().getRating().ordinal() < 3 ? 1 : Math.random() < 0.5 ? 2 : 3;
		for (int i = 0; i < thargoidNum; i++) {
			SpaceObject thargoid = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Thargoid);
			thargoid.setSpawnDroneDistanceSq(manager.computeSpawnThargonDistanceSq());
			manager.spawnEnemyAndAttackPlayer(thargoid, i, spawnPosition);
		}
	}

	@Override
	public TimedEvent getConditionRedSpawnReplacementEvent(final ObjectSpawnManager manager) {
		if (state != 2) {
			return null;
		}
		conditionRedEvent = new TimedEvent((long) ((2 << 9) / 16.7f * 1000000000L));
		return conditionRedEvent.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 7815560584428889246L;

			@Override
			public void execute(float deltaTime) {
				spawnThargoids(manager);
			}
		});
	}

	@Override
	public TimedEvent getViperSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		if (state == 2 && InGameManager.playerInSafeZone) {
			return new LaunchThargoidFromStationEvent(objectSpawnManager, objectSpawnManager.getDelayToViperEncounter(), 0);
		}
		return null;
	}

	@Override
	public TimedEvent getShuttleSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		if (state == 2) {
			return new LaunchThargoidFromStationEvent(objectSpawnManager, objectSpawnManager.getDelayToShuttleEncounter(), 0);
		}
		return null;
	}

	@Override
	public TimedEvent getTraderSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		if (state == 2) {
			return new LaunchThargoidFromStationEvent(objectSpawnManager, objectSpawnManager.getDelayToViperEncounter(), 1);
		}
		return null;
	}

	@Override
	public String getObjective() {
		return L.string(R.string.mission_thargoid_station_obj);
	}
}
