package de.phbouillon.android.games.alite.model;

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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

import android.graphics.Point;
import com.google.api.client.util.DateTime;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.trading.AliteMarket;
import de.phbouillon.android.games.alite.model.trading.Market;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Player {
	private static final int LAVE_INDEX = 7;

	private String name = L.string(R.string.cmdr_default_commander_name);
	private SystemData currentSystem;
	private SystemData hyperspaceSystem;
	private Condition condition = Condition.DOCKED;
	private LegalStatus legalStatus = LegalStatus.CLEAN;
	private Rating rating = Rating.HARMLESS;
	private long cash = 1000;
	private int score;
	private int rank;
	private int killCount;
	private final PlayerCobra cobra = new PlayerCobra();
	private final Market market;
	private int legalValue;
	private int maxLegalValue;
	private int recidivismCount; // even: should reach fugitive, odd: should reach clean
	private final Point position = new Point(-1, -1);
	private int jumpCounter;
	private int jumpCounterSinceLastMission;
	private int intergalacticJumpCounter;
	private int intergalacticJumpCounterSinceLastMission;
	private boolean cheater;
	private final Map<Integer,PlanetInfo> visitedPlanets = new HashMap<>();
	private int lastVisitedPlanet;
	private int highestVisitCount;
	private long minLastVisitedTime = System.currentTimeMillis();
	private int manuallyDockedCount;
	private long tradedAmountInGram;
	private long maxCash;
	private int maxGain;
	private int maxLoss;
	private int killCountInWitchSpace;
	private int killCountPirate;
	private int killCountThargoid;
	private int killCountThargon;
	private int killCountAsteroid;
	private int killCountMissileByLaser;
	private int killCountMissileByEcm;
	private int killCountByMissile;
	private int killCountByMissileJammer;
	private int killCountByLuckyMissile;
	private int killCountTrader;
	private int killCountPolice;
	private int maxScoreOfEnergyBomb;
	private int hyperdriveRepairCount;
	private int cloakingUseCount;
	private int escapeCapsuleUseCount;
	private int pauseModes;

	private class PlanetInfo {
		int id;
		Government government;
		int inhabitantCode;
		boolean isNode;
		int visitCount;
		long lastVisitedTime;

		PlanetInfo() {
			id = currentSystem.getId();
			government = currentSystem.getGovernment();
			inhabitantCode = currentSystem.getInhabitantCode().charAt(SystemData.INHABITANT_INDEX_RACE) ==
				SystemData.INHABITANT_RACE_HUMAN ? 0 : Integer.parseInt(currentSystem.getInhabitantCode());
			currentSystem.computeReachableSystems(Alite.get().getGenerator().getSystems());
			isNode = currentSystem.getReachableSystems().length > 15; // contains itself
			revisited();
		}

		PlanetInfo(int id, int visitCount, long lastVisitedTime, int government, int inhabitantCode, boolean isNode) {
			this.id = id;
			this.visitCount = visitCount;
			this.lastVisitedTime = lastVisitedTime;
			this.government = Government.values()[government];
			this.inhabitantCode = inhabitantCode;
			this.isNode = isNode;
			if (visitCount > highestVisitCount) {
				highestVisitCount = visitCount;
			}
		}

		void revisited() {
			if (lastVisitedTime != 0 && lastVisitedTime < minLastVisitedTime) {
				minLastVisitedTime = lastVisitedTime;
			}
			visitCount++;
			lastVisitedTime = System.currentTimeMillis();
			if (visitCount > highestVisitCount) {
				highestVisitCount = visitCount;
			}
		}
	}

	public Player() {
		market = new AliteMarket();
		Alite alite = Alite.get();
		alite.getGenerator().buildGalaxy(1);
		currentSystem = alite.getGenerator().getSystem(LAVE_INDEX);
		hyperspaceSystem = currentSystem;
		market.setFluct(0);
		market.setSystem(currentSystem);
		market.generate();
		TradeGoodStore.get().clearTraded();
		Settings.maxGalaxies = GalaxyGenerator.GALAXY_COUNT;
	}

	public PlayerCobra getCobra() {
		return cobra;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SystemData getCurrentSystem() {
		return currentSystem;
	}

	public SystemData getHyperspaceSystem() {
		return hyperspaceSystem;
	}

	public void setHyperspaceSystem(SystemData hyperspaceSystem) {
		this.hyperspaceSystem = hyperspaceSystem;
	}

	public void setCurrentSystem(SystemData currentSystem) {
		this.currentSystem = currentSystem;
		if (currentSystem != null) {
			market.setFluct((int) (Math.random() * 256));
			market.setSystem(currentSystem);
			market.generate();
		}
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition newCondition) {
		if (condition == Condition.DOCKED && newCondition == Condition.RED) {
			return;
		}
		condition = newCondition;
		if(condition != Condition.GREEN && condition != Condition.YELLOW)
			Alite.get().setTimeFactor(1);
	}

	public void increaseAlertLevel() {
		if (condition == Condition.GREEN) {
			condition = Condition.YELLOW;
		} else if (condition == Condition.YELLOW) {
			condition = Condition.RED;
		}
	}

	public void decreaseAlertLevel() {
		if (condition == Condition.RED) {
			condition = Condition.YELLOW;
		} else if (condition == Condition.YELLOW) {
			condition = Condition.GREEN;
		}
	}

	public LegalStatus getLegalStatus() {
		return legalStatus;
	}

	public void setLegalStatus(LegalStatus legalStatus) {
		this.legalStatus = legalStatus;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public long getCash() {
		return cash;
	}

	public void setCash(long newCash) {
		cash = newCash;
		if (cash > maxCash) {
			maxCash = cash;
		}
	}

	public int getRank() {
		int newRank = score == 0 ? 0 : Settings.maxGalaxies == GalaxyGenerator.EXTENDED_GALAXY_COUNT ? 11 :
			Alite.get().getPlayer().isPlanetVisited(SystemData.RAXXLA_SYSTEM.getId()) ? 10 : rating.ordinal() + 1;
		if (newRank > rank) {
			rank = newRank;
		}
		return rank;
	}

	public int getTradedAmountInTonne() {
		return (int) (tradedAmountInGram / Unit.TONNE.getValue());
	}

	public long getMaxCash() {
		return maxCash / 10;
	}

	public long getMaxGain() {
		return maxGain / 10;
	}

	public long getMaxLoss() {
		return maxLoss / 10;
	}

	public Market getMarket() {
		return market;
	}

	public void trade(InventoryItem item, long price) {
		item.getGood().traded();
		cobra.removeItem(item);
		setCash(cash + price);
		int gain = (int) (price - item.getPrice());
		if (gain >= 0) {
			maxGain = Math.max(maxGain, gain);
		} else {
			maxLoss = Math.max(maxLoss, -gain);
		}
		tradedAmountInGram += item.getWeight().getWeightInGrams();
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void increaseKillCount(SpaceObject destroyedObject, String destroyedByEquipment, boolean cloaked) {
		if (destroyedObject.getScore() > 0) {
			killCount++;
		}

		if (destroyedObject.getType() == ObjectType.Missile) {
			if (EquipmentStore.PULSE_LASER.equals(destroyedByEquipment)) {
				killCountMissileByLaser++;
				return;
			}
			if (EquipmentStore.ECM_SYSTEM.equals(destroyedByEquipment)) {
				killCountMissileByEcm++;
			}
			return;
		}

		if (EquipmentStore.MISSILES.equals(destroyedByEquipment) || EquipmentStore.ECM_JAMMER.equals(destroyedByEquipment)) {
			killCountByMissile++;
			if (destroyedObject.hasEcm()) {
				if (EquipmentStore.ECM_JAMMER.equals(destroyedByEquipment)) {
					killCountByMissileJammer++;
				} else {
					killCountByLuckyMissile++;
				}
			}
		}

		switch (destroyedObject.getType()) {
			case Pirate:
				killCountPirate++;
				if (cloaked && EquipmentStore.PULSE_LASER.equals(destroyedByEquipment)) {
					cloakingUseCount++;
				}
				break;
			case Thargoid:
				killCountThargoid++;
				if (cloaked && EquipmentStore.PULSE_LASER.equals(destroyedByEquipment)) {
					cloakingUseCount++;
				}
				break;
			case Thargon:
				killCountThargon++;
				if (cloaked && EquipmentStore.PULSE_LASER.equals(destroyedByEquipment)) {
					cloakingUseCount++;
				}
				break;
			case Asteroid:
				killCountAsteroid++;
				break;
			case Trader:
				killCountTrader++;
				break;
			case Police:
				killCountPolice++;
				break;
		}
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public int getKillCount() {
		return killCount;
	}

	public void increaseKillCountInWitchSpace(int amount) {
		killCountInWitchSpace += amount;
		hyperdriveRepairCount++;
	}

	public int getKillCountInWitchSpace() {
		return killCountInWitchSpace;
	}

	public int getKillCountPirate() {
		return killCountPirate;
	}

	public int getKillCountTrader() {
		return killCountTrader;
	}

	public int getKillCountPolice() {
		return killCountPolice;
	}

	public int getKillCountThargoid() {
		return killCountThargoid;
	}

	public int getKillCountThargon() {
		return killCountThargon;
	}

	public int getKillCountAsteroid() {
		return killCountAsteroid;
	}

	public int getKillCountMissileByLaser() {
		return killCountMissileByLaser;
	}

	public int getKillCountMissileByEcm() {
		return killCountMissileByEcm;
	}

	public int getKillCountByMissile() {
		return killCountByMissile;
	}

	public int getKillCountByMissileJammer() {
		return killCountByMissileJammer;
	}

	public int getKillCountByLuckyMissile() {
		return killCountByLuckyMissile;
	}

	public void setScoreOfEnergyBomb(int scoreOfEnergyBomb) {
		if (scoreOfEnergyBomb > maxScoreOfEnergyBomb) {
			maxScoreOfEnergyBomb = scoreOfEnergyBomb;
		}
	}

	public int getMaxScoreOfEnergyBomb() {
		return maxScoreOfEnergyBomb;
	}

	public int getHyperdriveRepairCount() {
		return hyperdriveRepairCount;
	}

	public int getManuallyDockedCount() {
		return manuallyDockedCount;
	}

	public int getCloakingUseCount() {
		return cloakingUseCount;
	}

	public void increaseEscapeCapsuleUse() {
		escapeCapsuleUseCount++;
	}

	public int getEscapeCapsuleUseCount() {
		return escapeCapsuleUseCount;
	}

	public void setResumedFromPause() {
		pauseModes |= 1;
	}

	public void setResumedFromAppSwitch() {
		pauseModes |= 2;
	}

	public int getPauseModes() {
		return pauseModes;
	}

	public void setLegalValueByContraband(float legalityType, int buyAmount) {
		if (Math.random() * 100 < getLegalProblemLikelihoodInPercent()) {
			setLegalValue(legalValue + (int) (legalityType * buyAmount));
		}
	}

	public void setLegalValue(int legalValue) {
		legalValue = legalValue < 0 ? 0 : Math.min(legalValue, 255);
		if (legalValue == 0) {
			if (this.legalValue > 0 && recidivismCount % 2 == 1) {
				recidivismCount++;
			}
			setLegalStatus(LegalStatus.CLEAN);
		} else if (legalValue < 32) {
			setLegalStatus(LegalStatus.OFFENDER);
		} else {
			if (this.legalValue < 32 && recidivismCount % 2 == 0) {
				recidivismCount++;
			}
			setLegalStatus(LegalStatus.FUGITIVE);
		}
		if (legalValue > maxLegalValue) {
			maxLegalValue = legalValue;
		}
		this.legalValue = legalValue;
	}

	public int getLegalValue() {
		return legalValue;
	}

	public int getMaxLegalValue() {
		return maxLegalValue;
	}

	public int getRecidivismCount() {
		return recidivismCount / 2;
	}

	public void setPosition(int px, int py) {
		position.x = px;
		position.y = py;
	}

	public Point getPosition() {
		return position;
	}

	public int computeDistance() {
		return currentSystem != null ? hyperspaceSystem.computeDistance(currentSystem) :
			SystemData.computeDistance(position.x, position.y, hyperspaceSystem.getX(), hyperspaceSystem.getY());
	}

	public void increaseJumpCounter() {
		jumpCounter++;
		jumpCounterSinceLastMission++;
	}

	public void resetJumpCounter() {
		jumpCounterSinceLastMission = 0;
	}

	public int getJumpCounterSinceLastMission() {
		return jumpCounterSinceLastMission;
	}

	public int getJumpCounter() {
		return jumpCounter;
	}

	public void setJumpCounter(int counter) {
		jumpCounterSinceLastMission = counter;
	}

	public void increaseIntergalacticJumpCounter() {
		intergalacticJumpCounter++;
		intergalacticJumpCounterSinceLastMission++;
	}

	public void resetIntergalacticJumpCounter() {
		intergalacticJumpCounterSinceLastMission = 0;
	}

	public void setIntergalacticJumpCounter(int counter) {
		intergalacticJumpCounter = counter;
		intergalacticJumpCounterSinceLastMission = counter;
	}

	public int getIntergalacticJumpCounter() {
		return intergalacticJumpCounter;
	}

	public void setIntergalacticJumpCounterSinceLastMission(int counter) {
		intergalacticJumpCounterSinceLastMission = counter;
	}

	public int getIntergalacticJumpCounterSinceLastMission() {
		return intergalacticJumpCounterSinceLastMission;
	}

	public void setCheater(boolean b) {
		cheater = b;
	}

	public int getLegalProblemLikelihoodInPercent() {
		if (currentSystem == null) {
			return 0;
		}
		switch (currentSystem.getGovernment()) {
			case ANARCHY: return 0;
			case FEUDAL: return 10;
			case MULTI_GOVERNMENT: return 20;
			case DICTATORSHIP: return 40;
			case COMMUNIST: return 60;
			case CONFEDERACY: return 80;
			case DEMOCRACY: return 100;
			case CORPORATE_STATE: return 100;
		}
		return 0;
	}

	public void computeLegalStatusAfterFriendlyHit(int hullStrength) {
		computeLegalStatusAfterHit(hullStrength, currentSystem == null ||
			currentSystem.getGovernment() != Government.ANARCHY, 64);
	}
	public void computeLegalStatusAfterStationHit(int hullStrength) {
		computeLegalStatusAfterHit(hullStrength, true, 64);
	}

	public void computeLegalStatusAtLaunchMissile() {
		computeLegalStatusAfterHit(0, true, 32);
	}

	private void computeLegalStatusAfterHit(int hullStrength, boolean safeZoneViolated, int maxIncrement) {
		if (InGameManager.playerInSafeZone && safeZoneViolated) {
			InGameManager.safeZoneViolated = true;
		}
		int increment = 0;
		if (currentSystem != null) {
			switch (currentSystem.getGovernment()) {
				case ANARCHY: break; // In anarchies, you can do whatever you want.
				case FEUDAL: if (hullStrength < 10 && Math.random() > 0.9) { increment = 16; } break;
				case MULTI_GOVERNMENT: if (hullStrength < 10 && Math.random() > 0.8) { increment = 24; } break;
				case DICTATORSHIP: if (hullStrength < 10 && Math.random() > 0.6) { increment = 32; } break;
				case COMMUNIST: if (hullStrength < 20 && Math.random() > 0.4) { increment = 40; } break;
				case CONFEDERACY: if (hullStrength < 30 && Math.random() > 0.2) { increment = 48; } break;
				case DEMOCRACY: increment = 56; break;
				case CORPORATE_STATE: increment = 64; break;
			}
		} else {
			increment = 64;
		}
		setLegalValue(legalValue + Math.min(increment, maxIncrement));
	}

	public boolean isPlanetVisited(int id) {
		return getPlanetInfo(id) != null;
	}

	private PlanetInfo getPlanetInfo(int id) {
		return visitedPlanets.get(id);
	}

	public int getNumberOfVisits(int id) {
		PlanetInfo planetInfo = getPlanetInfo(id);
		return planetInfo == null ? 0 : planetInfo.visitCount;
	}

	public long getLastVisitTime(int id) {
		PlanetInfo planetInfo = getPlanetInfo(id);
		return planetInfo == null ? 0 : planetInfo.lastVisitedTime;
	}

	public void addVisitedPlanetWithManualDocking() {
		addVisitedPlanet();
		manuallyDockedCount++;
	}

	public void addVisitedPlanet() {
		// Visit counts only after jump
		if (!isPlanetChanged()) {
			return;
		}
		PlanetInfo planet = visitedPlanets.get(lastVisitedPlanet);
		if (planet == null) {
			visitedPlanets.put(lastVisitedPlanet, new PlanetInfo());
		} else {
			planet.revisited();
		}
		cobra.checkLowEnergy();
	}

	private boolean isPlanetChanged() {
		int key = currentSystem == null ? 0 : currentSystem.getId();
		if (lastVisitedPlanet == key && visitedPlanets.containsKey(key)) {
			return false;
		}
		lastVisitedPlanet = key;
		return true;
	}

	public void loadVisitedPlanets(DataInputStream dis) throws IOException {
		isPlanetChanged(); // sets lastVisitedPlanet
		visitedPlanets.clear();
		try {
			int count = dis.readInt();
			for (int i = 0; i < count; i++) {
				addVisitedPlanet((dis.readChar() << 10) + dis.readChar(), dis.readInt(), dis.readLong(),
					dis.readByte(), dis.readChar(), false);
			}
		} catch (EOFException ignored) { }
	}

	private void addVisitedPlanet(int id, int visitCount, long lastVisitedDate, int government, int inhabitantCode, boolean isNode) {
		AliteLog.d("Planet visitor info", id + ": " + visitCount + " (" + new DateTime(lastVisitedDate) + ")");
		visitedPlanets.put(id, new PlanetInfo(id, visitCount, lastVisitedDate, government, inhabitantCode, isNode));
	}

	public int getTotalNumberOfPlanetVisit() {
		int sum = 0;
		for (PlanetInfo p : visitedPlanets.values()) {
			sum += p.visitCount;
		}
		return sum;
		//from API 24
		//return visitedPlanets.values().stream().mapToInt(p -> p.visitCount).sum();
	}

	public int getNumberOfVisitedPlanets() {
		return visitedPlanets.size();
	}

	public int getVisitNumberOfMostVisitedPlanet() {
		int max = 0;
		for (PlanetInfo p : visitedPlanets.values()) {
			if (p.visitCount > max) {
				max = p.visitCount;
			}
		}
		return max;
		//from API 24
		//return visitedPlanets.values().stream().mapToInt(p -> p.visitCount).max().orElse(0);
	}

	public long getMinLastVisitedTime() {
		return minLastVisitedTime;
	}

	public int getNumberOfFullyVisitedGalaxies() {
		int count = 0;
		Map<Integer, Integer> galaxies = new HashMap<>();
		for (Integer id : visitedPlanets.keySet()) {
			int galaxy = SystemData.getGalaxyOf(id);
			Integer visitedCount = galaxies.get(galaxy);
			visitedCount = visitedCount == null ? 1 : visitedCount + 1;
			galaxies.put(galaxy, visitedCount);
			if (visitedCount == GalaxyGenerator.PLANET_COUNT) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) visitedPlanets.keySet().stream().collect(Collectors.groupingBy(SystemData::getGalaxyOf,
		//	Collectors.counting())).values().stream().filter(p -> p == 256).count();
	}

	public int getNumberOfVisitedOrphanPlanets() {
		int count = 0;
		for (int id : visitedPlanets.keySet()) {
			if (Alite.get().getGenerator().isOrphan(id)) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) visitedPlanets.keySet().stream().filter(p -> Alite.get().getGenerator().isOrphan(p)).count();
	}

	public int getNumberOfVisitedNodePlanets() {
		int count = 0;
		for (PlanetInfo planet : visitedPlanets.values()) {
			if (planet.isNode) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) visitedPlanets.values().stream().filter(p -> p.isNode).count();
	}

	public int getNumberOfVisitedAnarchyPlanets() {
		int count = 0;
		for (PlanetInfo planet : visitedPlanets.values()) {
			if (planet.government == Government.ANARCHY) {
				count++;
			}
		}
		return count;
		//from API 24
		//return (int) visitedPlanets.values().stream().filter(p -> p.government == Government.ANARCHY).count();
	}

	public int getNumberOfVisitedGalaxies() {
		Set<Integer> galaxies = new HashSet<>();
		for (Integer id : visitedPlanets.keySet()) {
			galaxies.add(SystemData.getGalaxyOf(id));
		}
		return galaxies.size();
		//from API 24
		//return visitedPlanets.keySet().stream().collect(Collectors.groupingBy(SystemData::getGalaxyOf)).values().size();
	}

	public int getNumberOfVisitedSpecies() {
		Set<Integer> species = new HashSet<>();
		for (PlanetInfo planet : visitedPlanets.values()) {
			species.add(planet.inhabitantCode);
		}
		return species.size();
		//from API 24
		//return visitedPlanets.values().stream().collect(Collectors.groupingBy(p -> p.inhabitantCode)).size();
	}

	public JSONObject toJson() throws JSONException {
		JSONArray planets = new JSONArray();
		for (PlanetInfo p : visitedPlanets.values()) {
			planets.put(new JSONObject()
				.put("id", p.id)
				.put("visitCount", p.visitCount)
				.put("lastVisitedTime", p.lastVisitedTime)
				.put("government", p.government.ordinal())
				.put("inhabitantCode", p.inhabitantCode)
				.put("isNode", p.isNode));
		}

		return new JSONObject()
			.put("name", name)
			.put("currentSystem", currentSystem == null ? 0 : currentSystem.getIndex())
			.put("hyperspaceSystem", hyperspaceSystem == null ? 0 : hyperspaceSystem.getIndex())
			.putOpt("positionX", currentSystem == null ? position.x : null)
			.putOpt("positionY", currentSystem == null ? position.y : null)
			.put("cash", cash)
			.put("rating", rating.ordinal())
			.put("legalStatus", legalStatus.ordinal())
			.put("score", score)
			.put("rank", rank)
			.put("legalValue", legalValue)
			.put("maxLegalValue", maxLegalValue)
			.put("recidivismCount", recidivismCount)
			.put("intergalacticJumpCounter", intergalacticJumpCounter)
			.put("intergalacticJumpCounterSinceLastMission", intergalacticJumpCounterSinceLastMission)
			.put("jumpCounter", jumpCounter)
			.put("jumpCounterSinceLastMission", jumpCounterSinceLastMission)
			.put("cheater", cheater)
			.put("killCount", killCount)
			.put("killCountInWitchSpace", killCountInWitchSpace)
			.put("killCountPirate", killCountPirate)
			.put("killCountThargoid", killCountThargoid)
			.put("killCountThargon", killCountThargon)
			.put("killCountAsteroid", killCountAsteroid)
			.put("killCountMissileByLaser", killCountMissileByLaser)
			.put("killCountMissileByEcm", killCountMissileByEcm)
			.put("killCountByMissile", killCountByMissile)
			.put("killCountByMissileJammer", killCountByMissileJammer)
			.put("killCountByLuckyMissile", killCountByLuckyMissile)
			.put("killCountTrader", killCountTrader)
			.put("killCountPolice", killCountPolice)
			.put("maxScoreOfEnergyBomb", maxScoreOfEnergyBomb)
			.put("hyperdriveRepairCount", hyperdriveRepairCount)
			.put("manuallyDockedCount", manuallyDockedCount)
			.put("cloakingUseCount", cloakingUseCount)
			.put("escapeCapsuleUseCount", escapeCapsuleUseCount)
			.put("tradedAmountInGram", tradedAmountInGram)
			.put("maxCash", maxCash)
			.put("maxGain", maxGain)
			.put("maxLoss", maxLoss)
			.put("market", market.toJson())
			.put("cobra", cobra.toJson())
			.put("missions", MissionManager.getInstance().toJson())
			.put("visitedPlanets", planets)
			.put("minLastVisitedTime", minLastVisitedTime)
			.put("maxGalaxies", Settings.maxGalaxies)
			.put("pauseModes", pauseModes);
	}

	public void fromJson(JSONObject player) throws JSONException {
		name = player.getString("name");
		setCurrentSystem(Alite.get().getGenerator().getSystem(player.getInt("currentSystem")));
		hyperspaceSystem = Alite.get().getGenerator().getSystem(player.getInt("hyperspaceSystem"));
		int x = player.optInt("positionX");
		int y = player.optInt("positionY");
		if (x != 0 || y != 0) {
			setPosition(x, y);
			setCurrentSystem(null);
		}
		cash = player.getLong("cash");
		rating = Rating.values()[player.getInt("rating")];
		legalStatus = LegalStatus.values()[player.getInt("legalStatus")];
		score = player.getInt("score");
		rank = player.optInt("rank");
		legalValue = player.getInt("legalValue");
		maxLegalValue = player.optInt("maxLegalValue");
		recidivismCount = player.optInt("recidivismCount");
		intergalacticJumpCounter = player.getInt("intergalacticJumpCounter");
		intergalacticJumpCounterSinceLastMission = player.getInt("intergalacticJumpCounterSinceLastMission");
		jumpCounter = player.getInt("jumpCounter");
		jumpCounterSinceLastMission = player.optInt("jumpCounterSinceLastMission", jumpCounter);
		cheater = player.getBoolean("cheater");
		killCount = player.getInt("killCount");
		killCountInWitchSpace = player.optInt("killCountInWitchSpace");
		killCountPirate = player.optInt("killCountPirate");
		killCountThargoid = player.optInt("killCountThargoid");
		killCountThargon = player.optInt("killCountThargon");
		killCountAsteroid = player.optInt("killCountAsteroid");
		killCountMissileByLaser = player.optInt("killCountMissileByLaser");
		killCountMissileByEcm = player.optInt("killCountMissileByEcm");
		killCountByMissile = player.optInt("killCountByMissile");
		killCountByMissileJammer = player.optInt("killCountByMissileJammer");
		killCountByLuckyMissile = player.optInt("killCountByLuckyMissile");
		killCountTrader = player.optInt("killCountTrader");
		killCountPolice = player.optInt("killCountPolice");
		maxScoreOfEnergyBomb = player.optInt("maxScoreOfEnergyBomb");
		hyperdriveRepairCount = player.optInt("hyperdriveRepairCount");
		manuallyDockedCount = player.getInt("manuallyDockedCount");
		cloakingUseCount = player.optInt("cloakingUseCount");
		escapeCapsuleUseCount = player.optInt("escapeCapsuleUseCount");
		tradedAmountInGram = player.getLong("tradedAmountInGram");
		maxCash = player.getLong("maxCash");
		maxGain = player.getInt("maxGain");
		maxLoss = player.getInt("maxLoss");
		minLastVisitedTime = player.optLong("minLastVisitedTime", System.currentTimeMillis());
		Settings.maxGalaxies = player.optInt("maxGalaxies", GalaxyGenerator.GALAXY_COUNT);
		pauseModes = player.optInt("pauseModes");
		market.fromJson(player.getJSONObject("market"));
		cobra.fromJson(player.getJSONObject("cobra"));

		MissionManager.getInstance().fromJson(player.getJSONArray("missions"));

		isPlanetChanged(); // sets lastVisitedPlanet
		visitedPlanets.clear();
		JSONArray planets = player.getJSONArray("visitedPlanets");
		for (int i = 0; i < planets.length(); i++) {
			JSONObject p = planets.getJSONObject(i);
			addVisitedPlanet(p.getInt("id"), p.getInt("visitCount"), p.getLong("lastVisitedTime"),
				p.getInt("government"), p.getInt("inhabitantCode"), p.optBoolean("isNode"));
		}
	}

}
