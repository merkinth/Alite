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
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ThargoidDocumentsScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;

public class ThargoidDocumentsMission extends Mission implements Serializable {
	private static final long serialVersionUID = 7271967050611429726L;

	public static final int ID = 2;

	private TimedEvent conditionRedEvent;

	public ThargoidDocumentsMission() {
		super(ID);
	}

	@Override
	protected void acceptMission(boolean accept) {
		if (accept) {
			alite.getCobra().setMissiles(4);
			alite.getCobra().setFuel(alite.getCobra().getMaxFuel());
			alite.getCobra().setTradeGood(TradeGoodStore.get().getGoodById(TradeGoodStore.THARGOID_DOCUMENTS), Weight.grams(482), 0);
			state = 1;
			resetTargetName();
		} else {
			finalizeMission();
		}
	}

	@Override
	public void onMissionComplete() {
		alite.getCobra().removeItem(alite.getCobra().getInventoryItemByGood(
			TradeGoodStore.get().getGoodById(TradeGoodStore.THARGOID_DOCUMENTS)));
		alite.getCobra().removeEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT));
		alite.getCobra().addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT));
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new ThargoidDocumentsScreen(0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		return missionDidNotStart() || state != 1 || !positionMatchesTarget() ? null :
			new ThargoidDocumentsScreen(2);
	}

	private void spawnThargoids(final ObjectSpawnManager manager) {
		if (manager.isInTorus()) {
			if ((int) (Math.random() * 256) < 32) {
				return;
			}
			manager.leaveTorus();
		}
		manager.conditionRed();
		conditionRedEvent.pause();
		manager.spawnThargoids(alite.getPlayer().getRating().ordinal() < 3 ? 1 : Math.random() < 0.5 ? 1 : 2);
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
