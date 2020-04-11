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
import java.util.ArrayList;
import java.util.List;

import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TradeScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectSpawnManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.TimedEvent;

public abstract class Mission implements Serializable {
	private static final long serialVersionUID = 9188011962471959641L;

	protected boolean active = false;
	protected boolean started = false;
	protected transient Alite alite;
	private int galaxy;
	private int targetIndex;
	int state;
	private final int id;
	private String targetName = null;

	protected abstract boolean checkStart(Player player);
	protected abstract void acceptMission(boolean accept);

	public void onMissionAccept() {
	}

	public void onMissionDecline() {
	}

	public abstract void onMissionComplete();

	public void onMissionUpdate() {
	}

	public abstract AliteScreen getMissionScreen();

	public boolean willStartOnDock() {
		return false;
	}

	protected Mission(int id) {
		alite = Alite.get();
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean missionStarts() {
		Player player = alite.getPlayer();
		if (!started &&
				!player.getActiveMissions().contains(this) &&
				!player.getCompletedMissions().contains(this) &&
				player.getCondition() == Condition.DOCKED &&
				checkStart(player)) {
			active = true;
			started = true;
			return true;
		}
		return false;
	}

	public abstract AliteScreen checkForUpdate();

	public void setPlayerAccepts(boolean playerAccepts) {
		acceptMission(playerAccepts);
		if (!playerAccepts) {
			active = false;
		}
	}

	public boolean isActive() {
		return active;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			in.defaultReadObject();
			alite = Alite.get();
		} catch (ClassNotFoundException e) {
			AliteLog.e("Error in Initializer", e.getMessage(), e);
		}
	}

	public void load(DataInputStream dis) throws IOException {
		galaxy = dis.readByte();
		targetIndex = dis.readInt();
		state = dis.readInt();
		targetName = null;
		active = true;
		started = true;
	}

	public byte [] save() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(16);
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(galaxy);
		dos.writeInt(targetIndex);
		dos.writeInt(state);
		dos.close();
		bos.close();
		return bos.toByteArray();
	}

	public final SystemData findMostDistantSystem() {
		int maxDist = -1;
		SystemData current = alite.getPlayer().getCurrentSystem();
		SystemData target = null;
		for (SystemData system: alite.getGenerator().getSystems()) {
			int dist = current.computeDistance(system);
			if (dist > maxDist) {
				maxDist = dist;
				target = system;
			}
		}
		return target;
	}

	public final SystemData findRandomSystemInRange(int min, int max) {
		List <SystemData> candidates = new ArrayList<>();
		SystemData current = alite.getPlayer().getCurrentSystem();
		for (SystemData system: alite.getGenerator().getSystems()) {
			int dist = current.computeDistance(system);
			if (dist >= min && dist <= max) {
				candidates.add(system);
			}
		}
		if (candidates.isEmpty()) {
			return alite.getGenerator().getSystem(current.getIndex() == 0 ? 1 : 0);
		}
		return candidates.get((int) (Math.random() * candidates.size()));
	}

	boolean positionMatchesTarget() {
		if (alite.getGenerator().getCurrentGalaxy() != galaxy) {
			return false;
		}
		return alite.getPlayer().getCurrentSystem() != null &&
			(targetIndex == -1 || targetIndex == alite.getPlayer().getCurrentSystem().getIndex());
	}

	public TimedEvent getWitchSpaceSpawnEvent(final ObjectSpawnManager manager) {
		return null;
	}

	public TimedEvent getSpawnEvent(final ObjectSpawnManager manager) {
		return null;
	}

	public TimedEvent getConditionRedSpawnReplacementEvent(final ObjectSpawnManager manager) {
		return null;
	}

	public boolean willEnterWitchSpace() {
		return false;
	}

	public TimedEvent getPreStartEvent(InGameManager manager) {
		return null;
	}

	public boolean performTrade(TradeScreen tradeScreen, Equipment equipment) {
		return false;
	}

	public boolean performTrade(TradeScreen tradeScreen, TradeGood tradeGood) {
		return false;
	}

	public TimedEvent getViperSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return null;
	}

	public TimedEvent getShuttleSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return null;
	}

	public TimedEvent getAsteroidSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return null;
	}

	public TimedEvent getTraderSpawnReplacementEvent(final ObjectSpawnManager objectSpawnManager) {
		return null;
	}

	public abstract String getObjective();

	public void resetStarted() {
		started = false;
	}

	public void setTarget(int galaxy, int targetIndex, int state) {
		this.galaxy = galaxy;
		this.targetIndex = targetIndex;
		this.state = state;
		targetName = null;
	}

	public void resetTargetName() {
		targetName = null;
	}

	String getTargetName() {
		if (targetName != null) {
			return targetName;
		}
		if (targetIndex != -1) {
			if (alite.getGenerator().getCurrentGalaxy() != galaxy) {
				GalaxyGenerator gen = new GalaxyGenerator();
				gen.buildGalaxy(galaxy);
				targetName = gen.getSystem(targetIndex).getName();
				return targetName;
			}
			targetName = alite.getGenerator().getSystem(targetIndex).getName();
		} else {
			targetName = L.string(R.string.mission_unknown_target);
		}
		return targetName;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
