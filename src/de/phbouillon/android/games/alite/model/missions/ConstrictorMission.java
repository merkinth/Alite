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
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ConstrictorScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

public class ConstrictorMission extends Mission {
	private static final long serialVersionUID = -5769172780079332330L;

	public static final int ID = 1;

	private boolean constrictorCreated = false;

	public ConstrictorMission(Alite alite) {
		super(alite, ID);
	}

	@Override
	protected boolean checkStart(Player player) {
		return player.getIntergalacticJumpCounter() > 0 &&
			player.getIntergalacticJumpCounter() + player.getJumpCounter() >= 64;
	}

	@Override
	protected void acceptMission(boolean accept) {
		if (accept) {
			alite.getPlayer().addActiveMission(this);
			state = 1;
			resetTargetName();
		} else {
			alite.getPlayer().resetIntergalacticJumpCounter();
			alite.getPlayer().resetJumpCounter();
			alite.getPlayer().addCompletedMission(this);
		}
	}

	@Override
	public void onMissionComplete() {
		active = false;
		alite.getPlayer().setCash(alite.getPlayer().getCash() + 100000);
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new ConstrictorScreen(alite, 0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		if (alite.getPlayer().getCondition() != Condition.DOCKED || state < 1 || !started || !active) {
			return null;
		}
		if (state == 1 && positionMatchesTarget()) {
			return new ConstrictorScreen(alite, 2);
		}
		if (state >= 2 && state <= 5 && positionMatchesTarget()) {
			return new ConstrictorScreen(alite, 3);
		}
		if (state == 6 && positionMatchesTarget()) {
			// Player arrived at target, but _did not destroy_ the Constrictor...
			// Try again...
			state--;
			return new ConstrictorScreen(alite, 3);
		}
		if (state == 7 && positionMatchesTarget()) {
			return new ConstrictorScreen(alite, 4);
		}
		return null;
	}

	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		boolean result = positionMatchesTarget();
		if (state != 6 || !result || constrictorCreated) {
			return null;
		}
		TimedEvent event = new TimedEvent(0);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 1657605081590177007L;

			@Override
			public void execute(float deltaTime) {
				constrictorCreated = true;
				event.remove();
				SoundManager.play(Assets.com_conditionRed);
				manager.getInGameManager().repeatMessage(L.string(R.string.com_condition_red), 3);
				Vector3f spawnPosition = manager.getSpawnPosition();
				SpaceObject constrictor = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Constrictor);
				manager.spawnEnemyAndAttackPlayer(constrictor, 0, spawnPosition);
				manager.lockConditionRedEvent();
				constrictor.addDestructionCallback(13, new IMethodHook() {
					private static final long serialVersionUID = -7774734879444916116L;

					@Override
					public void execute(float deltaTime) {
						state = 7;
						manager.unlockConditionRedEvent();
					}

				});
			}
		});
	}

	@Override
	public String getObjective() {
		if (state == 2) {
			return L.string(R.string.mission_constrictor_obj_jump);
		}
		if (state >= 1 && state <= 6) {
			return L.string(R.string.mission_constrictor_obj_fly, getTargetName());
		}
		return "";
	}

}
