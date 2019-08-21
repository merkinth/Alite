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
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TradeScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.SupernovaScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;
import de.phbouillon.android.games.alite.screens.opengl.objects.SphericalSpaceObject;

public class SupernovaMission extends Mission implements Serializable {
	private static final long serialVersionUID = -526592825746968885L;

	public static final int ID = 3;

	private final TimedEvent preStartEvent;
	private Timer timer;

	public SupernovaMission(final Alite alite) {
		super(alite, ID);
		preStartEvent = new TimedEvent(100000000);
		preStartEvent.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = -6824297537488646688L;

			@Override
			public void execute(float deltaTime) {
				alite.getCobra().setFuel(alite.getCobra().getFuel() - 1);
				if (alite.getCobra().getFuel() <= 0) {
					preStartEvent.remove();
				}
			}
		});
	}

	@Override
	protected boolean checkStart(Player player) {
		return player.getCompletedMissions().contains(MissionManager.getInstance().get(ThargoidDocumentsMission.ID)) &&
			player.getIntergalacticJumpCounter() + player.getJumpCounter() >= 64;
	}

	@Override
	public boolean willStartOnDock() {
		Player player = alite.getPlayer();
		return !started &&
			   !player.getCompletedMissions().contains(this) &&
				player.getCompletedMissions().contains(MissionManager.getInstance().get(ThargoidDocumentsMission.ID)) &&
				player.getIntergalacticJumpCounter() + player.getJumpCounter() >= 64;
	}

	@Override
	public TimedEvent getPreStartEvent(InGameManager manager) {
		manager.setMessage(L.string(R.string.com_fuel_system_malfunction));
		SoundManager.play(Assets.com_fuelSystemMalfunction);
		return preStartEvent;
	}

	@Override
	protected void acceptMission(boolean accept) {
		alite.getPlayer().addActiveMission(this);
		if (state == 1) {
			alite.getCobra().clearInventory();
			alite.getCobra().addSpecialCargo(TradeGoodStore.GOOD_UNHAPPY_REFUGEES, alite.getCobra().isEquipmentInstalled(EquipmentStore.largeCargoBay) ? Weight.tonnes(35) : Weight.tonnes(20));
		}
	}

	@Override
	public void onMissionComplete() {
		active = false;
		alite.getCobra().removeSpecialCargo(TradeGoodStore.GOOD_UNHAPPY_REFUGEES);
		alite.getCobra().addTradeGood(TradeGoodStore.get().gemStones(), Weight.kilograms(1), 0);
	}

	@Override
	public AliteScreen getMissionScreen() {
		return new SupernovaScreen(alite, 0);
	}

	@Override
	public AliteScreen checkForUpdate() {
		if (alite.getPlayer().getCondition() != Condition.DOCKED || state < 1 || !started || !active) {
			return null;
		}
		if (state == 1 && !positionMatchesTarget()) {
			return new SupernovaScreen(alite, 3);
		}
		if (state == 2 && !positionMatchesTarget()) {
			active = false;
			alite.getPlayer().removeActiveMission(this);
			alite.getPlayer().addCompletedMission(this);
		}
		return null;
	}

	@Override
	public boolean performTrade(TradeScreen tradeScreen, Equipment equipment) {
		tradeScreen.showMessageDialog(L.string(R.string.mission_supernova_trade));
		SoundManager.play(Assets.error);
		return true;
	}

	@Override
	public boolean performTrade(TradeScreen tradeScreen, TradeGood tradeGood) {
		tradeScreen.showMessageDialog(L.string(R.string.mission_supernova_trade));
		SoundManager.play(Assets.error);
		return true;
	}

	@Override
	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		boolean result = positionMatchesTarget();
		if (state != 1 && state != 2 || !result) {
			return null;
		}
		TimedEvent event = new TimedEvent(100000000);
		return event.addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 7855977766031440861L;

			@Override
			public void execute(float deltaTime) {
				InGameManager inGame = manager.getInGameManager();
				SphericalSpaceObject sun = (SphericalSpaceObject) inGame.getSun();
				sun.setNewSize(sun.getRadius() * 1.01f);
				SphericalSpaceObject sunGlow = (SphericalSpaceObject) inGame.getSunGlow();
				sunGlow.setNewSize(sun.getRadius() + 400.0f);
				if (timer == null) {
					inGame.setMessage(L.string(R.string.mission_supernova_danger));
					SoundManager.repeat(Assets.criticalCondition);
					timer = new Timer();
				} else {
					if (timer.hasPassedSeconds(20)) {
						event.remove();
						timer = null;
						inGame.gameOver();
					}
				}
			}
		});
	}

	@Override
	public String getObjective() {
		return L.string(R.string.mission_supernova_obj);
	}
}
