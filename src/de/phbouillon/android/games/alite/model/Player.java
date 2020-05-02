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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.trading.AliteMarket;
import de.phbouillon.android.games.alite.model.trading.Market;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;

public class Player {
	private static final int LAVE_INDEX = 7;

	private String name;
	private SystemData currentSystem;
	private SystemData hyperspaceSystem;
	private Condition condition;
	private LegalStatus legalStatus;
	private Rating rating;
	private long cash;
	private int score;
	private int killCount;
	private PlayerCobra cobra;
	private final Market market;
	private int legalValue;
	private final Point position = new Point(-1, -1);
	private int jumpCounter = 0;
	private int intergalacticJumpCounter = 0;
	private final List <Mission> activeMissions = new ArrayList<>();
	private final List <Mission> completedMissions = new ArrayList<>();
	private boolean cheater;

	public Player() {
		market = new AliteMarket();
		reset();
	}

	public void reset() {
		cobra = new PlayerCobra();
		name = L.string(R.string.cmdr_default_commander_name);
		condition = Condition.DOCKED;
		legalStatus = LegalStatus.CLEAN;
		legalValue = 0;
		rating = Rating.HARMLESS;
		cash = 1000;
		score = 0;
		killCount = 0;
		Alite alite = Alite.get();
		alite.getGenerator().buildGalaxy(1);
		currentSystem = alite.getGenerator().getSystems()[LAVE_INDEX];
		hyperspaceSystem = alite.getGenerator().getSystems()[LAVE_INDEX];
		market.setFluct(0);
		market.setSystem(currentSystem);
		market.generate();
		position.x = -1;
		position.y = -1;
		clearMissions();
		jumpCounter = 0;
		intergalacticJumpCounter = 0;
		cheater = false;
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
	}

	public Market getMarket() {
		return market;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void increaseKillCount(int amount) {
		killCount += amount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setLegalValueByContraband(float legalityType, int buyAmount) {
		if (Math.random() * 100 < getLegalProblemLikelihoodInPercent()) {
			setLegalValue(legalValue + (int) (legalityType * buyAmount));
		}
	}

	public void setLegalValue(int legalValue) {
		this.legalValue = legalValue < 0 ? 0 : Math.min(legalValue, 255);
		if (this.legalValue == 0) {
			setLegalStatus(LegalStatus.CLEAN);
		} else if (this.legalValue < 32) {
			setLegalStatus(LegalStatus.OFFENDER);
		} else {
			setLegalStatus(LegalStatus.FUGITIVE);
		}
	}

	public int getLegalValue() {
		return legalValue;
	}

	public void setPosition(int px, int py) {
		position.x = px;
		position.y = py;
	}

	public Point getPosition() {
		return position;
	}

	public int computeDistance() {
		if (currentSystem != null) {
			return hyperspaceSystem.computeDistance(currentSystem);
		}
		int dx = position.x - hyperspaceSystem.getX();
		int dy = position.y - hyperspaceSystem.getY();
		return (int) Math.sqrt(dx * dx + dy * dy) << 2;
	}

	public void increaseJumpCounter() {
		jumpCounter++;
	}

	public void resetJumpCounter() {
		jumpCounter = 0;
	}

	public int getJumpCounter() {
		return jumpCounter;
	}

	public void setJumpCounter(int counter) {
		jumpCounter = counter;
	}

	public void increaseIntergalacticJumpCounter() {
		intergalacticJumpCounter++;
	}

	public void resetIntergalacticJumpCounter() {
		intergalacticJumpCounter = 0;
	}

	public void setIntergalacticJumpCounter(int counter) {
		intergalacticJumpCounter = counter;
	}

	public int getIntergalacticJumpCounter() {
		return intergalacticJumpCounter;
	}

	public void clearMissions() {
		activeMissions.clear();
		completedMissions.clear();
	}

	public void addActiveMission(Mission mission) {
		activeMissions.add(mission);
	}

	public void removeActiveMission(Mission mission) {
		activeMissions.remove(mission);
	}

	public void addCompletedMission(Mission mission) {
		completedMissions.add(mission);
	}

	public List <Mission> getActiveMissions() {
		return activeMissions;
	}

	public List <Mission> getCompletedMissions() {
		return completedMissions;
	}

	public boolean isCheater() {
		return cheater;
	}

	public void setCheater(boolean b) {
		cheater = b;
	}

	public int getLegalProblemLikelihoodInPercent() {
		if (getCurrentSystem() == null) {
			return 0;
		}
		Government g = getCurrentSystem().getGovernment();
		switch (g) {
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

}
