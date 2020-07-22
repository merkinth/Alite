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

import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.CougarScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

public class CougarMission extends Mission {
	private static final long serialVersionUID = 5999402243478935543L;

	public static final int ID = 4;

	public CougarMission() {
		super(ID);
	}

	@Override
	protected void acceptMission(boolean accept) {
		// The player can't decline this mission...
		state = 1;
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new CougarScreen(0);
	}

	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		if (state != 1 || positionMatchesTarget()) {
			return null;
		}
		TimedEvent event = new TimedEvent(4000000000L);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = -8640036894816728823L;

			@Override
			public void execute(float deltaTime) {
				manager.lockConditionRedEvent();
				event.remove();
				manager.conditionRed();
				final SpaceObject cougar = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Cougar);
				SpaceObject asp1 = SpaceObjectFactory.getInstance().getObjectById("asp_mk_ii");
				SpaceObject asp2 = SpaceObjectFactory.getInstance().getObjectById("asp_mk_ii");
				manager.spawnEnemyAndAttackPlayer(asp1, cougar, asp2);
				cougar.addDestructionCallback(new IMethodHook() {
					private static final long serialVersionUID = -4949764387008051526L;

					@Override
					public void execute(float deltaTime) {
						final SpaceObject cargo = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.CargoPod);
						cargo.setSpecialCargoContent(EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE));
						cargo.addDestructionCallback(new IMethodHook() {
							public static final long serialVersionUID = -7271468394126708823L;

							@Override
							public void execute(float deltaTime) {
								if (alite.getCobra().isEquipmentInstalled(
									EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE))) {
									missionCompleted();
								}
							}
						});
						manager.spawnTumbleObject(cargo, cougar.getPosition());
						manager.unlockConditionRedEvent();
					}

				});
			}
		});
	}

	@Override
	public String getObjective() {
		return L.string(R.string.mission_cougar_obj);
	}
}
