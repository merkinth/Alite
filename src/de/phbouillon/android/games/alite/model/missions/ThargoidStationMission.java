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
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.EquipmentStore;
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
							thargoid.setAIState(SpaceObjectAI.AI_STATE_ATTACK);
						}
					});
				}
			});
		}
	}

	public ThargoidStationMission() {
		super(ID);
	}

	@Override
	protected void acceptMission(boolean accept) {
		// The player can't decline this mission...
		state = 1;
	}

	@Override
	public void onMissionComplete() {
		alite.getCobra().addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_JAMMER));
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new ThargoidStationScreen(0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		return missionDidNotStart() || state != 3 ? null : new ThargoidStationScreen(1);
	}


	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		boolean result = positionMatchesTarget();
		// !result => Any system but the one where the player received the mission
		// result => The precise system where the Alien Space Station
		// was when the player first saw it. He escaped and now tries again...
		if (state == 1 && !result) {
			state = 2;
			return null;
		}
		if (state != 2 || !result) {
			return null;
		}
		TimedEvent event = new TimedEvent(10000000000L);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 5516217861394636289L;

			@Override
			public void execute(float deltaTime) {
				SpaceObject station = manager.getInGameManager().getStation();
				station.setId(ALIEN_SPACE_STATION);
				station.setName(R.string.thargoid_station_name);
				station.setHullStrength(1024);
				station.denyAccess();
				station.addDestructionCallback(new IMethodHook() {
					private static final long serialVersionUID = 6715650816893032921L;

					@Override
					public void execute(float deltaTime) {
						manager.getInGameManager().setStation(null);
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
		manager.conditionRed();
		conditionRedEvent.pause();
		manager.spawnThargoids(alite.getPlayer().getRating().ordinal() < 3 ? 1 : Math.random() < 0.5 ? 2 : 3);
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
		return state == 2 && InGameManager.playerInSafeZone && positionMatchesTarget() ?
			new LaunchThargoidFromStationEvent(objectSpawnManager,
				objectSpawnManager.getDelayToViperEncounter(), 0) : null;
	}

	@Override
	public TimedEvent getShuttleSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return state == 2 && InGameManager.playerInSafeZone && positionMatchesTarget() ?
			new LaunchThargoidFromStationEvent(objectSpawnManager,
				objectSpawnManager.getDelayToShuttleEncounter(), 0) : null;
	}

	@Override
	public TimedEvent getTraderSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return state == 2 && positionMatchesTarget() ? new LaunchThargoidFromStationEvent(objectSpawnManager,
			objectSpawnManager.getDelayToViperEncounter(), 1) : null;
	}

	@Override
	public String getObjective() {
		return L.string(R.string.mission_thargoid_station_obj, getTargetName());
	}
}
