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
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ThargoidDocumentsScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Thargoid;

public class ThargoidDocumentsMission extends Mission implements Serializable {
	private static final long serialVersionUID = 7271967050611429726L;

	public static final int ID = 2;

	private TimedEvent conditionRedEvent;

	public ThargoidDocumentsMission(Alite alite) {
		super(alite, ID);
	}

	@Override
	protected boolean checkStart(Player player) {
		return player.getCompletedMissions().contains(MissionManager.getInstance().get(ConstrictorMission.ID)) &&
			player.getIntergalacticJumpCounter() + player.getJumpCounter() >= 64;
	}

	@Override
	protected void acceptMission(boolean accept) {
		if (accept) {
			alite.getPlayer().addActiveMission(this);
			alite.getCobra().setMissiles(4);
			alite.getCobra().setFuel(70);
			alite.getCobra().addSpecialCargo(TradeGoodStore.GOOD_THARGOID_DOCUMENTS, Weight.grams(482));
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
		alite.getCobra().removeSpecialCargo(TradeGoodStore.GOOD_THARGOID_DOCUMENTS);
		alite.getCobra().removeEquipment(EquipmentStore.extraEnergyUnit);
		alite.getCobra().addEquipment(EquipmentStore.navalEnergyUnit);
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new ThargoidDocumentsScreen(alite, 0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		if (alite.getPlayer().getCondition() != Condition.DOCKED || state < 1 || !started || !active) {
			return null;
		}
		if (state == 1 && positionMatchesTarget()) {
			return new ThargoidDocumentsScreen(alite, 2);
		}
		return null;
	}

	private void spawnThargoids(final ObjectSpawnManager manager) {
		if (manager.isInTorus()) {
			int randByte = (int) (Math.random() * 256);
			if (randByte < 32) {
				return;
			}
			manager.leaveTorus();
		}
		SoundManager.play(Assets.com_conditionRed);
		manager.getInGameManager().repeatMessage(L.string(R.string.com_condition_red), 3);
		conditionRedEvent.pause();
		Vector3f spawnPosition = manager.getSpawnPosition();
		int thargoidNum = alite.getPlayer().getRating().ordinal() < 3 ? 1 : Math.random() < 0.5 ? 1 : 2;
		for (int i = 0; i < thargoidNum; i++) {
			Thargoid thargoid = new Thargoid(alite);
			thargoid.setSpawnThargonDistanceSq(manager.computeSpawnThargonDistanceSq());
			manager.spawnEnemyAndAttackPlayer(thargoid, i, spawnPosition);
		}
	}

	@Override
	public TimedEvent getConditionRedSpawnReplacementEvent(final ObjectSpawnManager manager) {
		conditionRedEvent = new TimedEvent((long) ((2 << 9) / 16.7f * 1000000000L));
		return conditionRedEvent.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 4448575917547734318L;

			@Override
			public void execute(float deltaTime) {
				spawnThargoids(manager);
			}
		});
	}

	@Override
	public boolean willEnterWitchSpace() {
		return Math.random() <= 0.15;
	}

	@Override
	public String getObjective() {
		return L.string(R.string.mission_thargoid_documents_obj, getTargetName());
	}
}
