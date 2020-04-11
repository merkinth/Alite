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

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.CougarScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

public class CougarMission extends Mission {
	private static final long serialVersionUID = 5999402243478935543L;

	public static final int ID = 4;

	private final Vector3f tempVector = new Vector3f(0, 0, 0);
	private boolean cougarCreated = false;

	public CougarMission() {
		super(ID);
	}

	class CougarCloakingUpdater implements IMethodHook {
		private static final long serialVersionUID = 6077773193969694018L;
		private long nextUpdateEvent;
		private final Timer timer = new Timer().setAutoReset();
		private final SpaceObject cougar;
		private boolean cloaked = false;

		CougarCloakingUpdater(SpaceObject cougar) {
			this.cougar = cougar;
			computeNextUpdateTime();
		}

		private void computeNextUpdateTime() {
			// 6 - 12 seconds later.
			nextUpdateEvent = (long) (6 * Math.random() + 6);
		}

		@Override
		public void execute(float deltaTime) {
			if (timer.hasPassedSeconds(nextUpdateEvent)) {
				computeNextUpdateTime();
				cloaked = !cloaked;
				cougar.setCloaked(cloaked);
			}
		}
	}

	@Override
	protected boolean checkStart(Player player) {
		return player.getCompletedMissions().contains(MissionManager.getInstance().get(SupernovaMission.ID)) &&
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
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new CougarScreen(0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		return null;
	}

	private void spawnCargoCanister(final InGameManager inGame, final SpaceObject cougar) {
		tempVector.x = (float) (-2.0 + Math.random() * 4.0);
		tempVector.y = (float) (-2.0 + Math.random() * 4.0);
		tempVector.z = (float) (-2.0 + Math.random() * 4.0);
		tempVector.normalize();
		final float ix = tempVector.x;
		final float iy = tempVector.y;
		final float iz = tempVector.z;
		tempVector.x = (float) (-2.0 + Math.random() * 4.0);
		tempVector.y = (float) (-2.0 + Math.random() * 4.0);
		tempVector.z = (float) (-2.0 + Math.random() * 4.0);
		tempVector.normalize();
		final float rx = tempVector.x;
		final float ry = tempVector.y;
		final float rz = tempVector.z;
		final SpaceObject cargo = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.CargoPod);
		cargo.setSpecialCargoContent(EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE));
		cargo.setSpeed(0.0f);
		final float speed = 0.2f + (cargo.getMaxSpeed() - 0.2f) * (float) Math.random();
		cargo.setPosition(cougar.getPosition().x, cougar.getPosition().y, cougar.getPosition().z);
		cargo.setUpdater(new IMethodHook() {
			private static final long serialVersionUID = 4203394658109589557L;

			@Override
			public void execute(float deltaTime) {
				cargo.getPosition().copy(tempVector);
				float x = tempVector.x + ix * speed * deltaTime;
				float y = tempVector.y + iy * speed * deltaTime;
				float z = tempVector.z + iz * speed * deltaTime;
				cargo.setPosition(x, y, z);
				cargo.applyDeltaRotation(rx, ry, rz);
			}
		});
		inGame.addObject(cargo);
	}

	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		boolean result = !positionMatchesTarget();
		if (state != 1 || !result || cougarCreated) {
			return null;
		}
		alite.getPlayer().addCompletedMission(this);
		alite.getPlayer().resetIntergalacticJumpCounter();
		alite.getPlayer().resetJumpCounter();
		TimedEvent event = new TimedEvent(4000000000L);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = -8640036894816728823L;

			@Override
			public void execute(float deltaTime) {
				if (cougarCreated) {
					return;
				}
				cougarCreated = true;
				manager.lockConditionRedEvent();
				event.remove();
				SoundManager.play(Assets.com_conditionRed);
				manager.getInGameManager().repeatMessage(L.string(R.string.com_condition_red), 3);
				Vector3f spawnPosition = manager.getSpawnPosition();
				final SpaceObject cougar = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Cougar);
				SpaceObject asp1 = SpaceObjectFactory.getInstance().getObjectById("asp_mk_ii");
				SpaceObject asp2 = SpaceObjectFactory.getInstance().getObjectById("asp_mk_ii");
				manager.spawnEnemyAndAttackPlayer(asp1, 0, spawnPosition );
				manager.spawnEnemyAndAttackPlayer(cougar, 1, spawnPosition);
				manager.spawnEnemyAndAttackPlayer(asp2, 2, spawnPosition);
				alite.getPlayer().removeActiveMission(CougarMission.this);
				cougar.setUpdater(new CougarCloakingUpdater(cougar));
				cougar.addDestructionCallback(1, new IMethodHook() {
					private static final long serialVersionUID = -4949764387008051526L;

					@Override
					public void execute(float deltaTime) {
						spawnCargoCanister(manager.getInGameManager(), cougar);
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
