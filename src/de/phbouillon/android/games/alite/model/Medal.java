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

import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.library.Toc;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutorialScreen;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class Medal {
	public static final int MEDAL_ID_TIME = 40;
	public static final int MEDAL_ID_PLANETS = 130;
	public static final int MEDAL_ID_GALAXY = 150;
	public static final int MEDAL_ID_HYPER_JUMP = 155;
	public static final int MEDAL_ID_FUEL = 165;
	public static final int MEDAL_ID_INVENTORY = 215;
	public static final int MEDAL_ID_FIREARMS = 220;
	public static final int MEDAL_ID_RICH = 235;
	public static final int MEDAL_ID_MINER = 245;
	public static final int MEDAL_ID_INSECTICIDE = 315;
	public static final int MEDAL_ID_KILL_THARGON = 325;
	public static final int MEDAL_ID_KILL_TRADER = 327;
	public static final int MEDAL_ID_KILL_POLICE = 328;
	public static final int MEDAL_ID_JAMMER = 335;
	public static final int MEDAL_ID_ENERGY_BOMB = 340;
	public static final int MEDAL_ID_MISSILE = 345;
	public static final int MEDAL_ID_ECM = 410;
	public static final int MEDAL_ID_LASER = 415;
	public static final int MEDAL_ID_RETRO_ROCKETS = 440;
	public static final int MEDAL_ID_CLOAKING = 445;
	public static final int MEDAL_ID_ESCAPE_CAPSULE = 450;
	public static final int MEDAL_ID_EQUIPMENT = 510;
	public static final int MEDAL_ID_MECHANIC = 515;
	public static final int MEDAL_ID_FUEL_SCOOP = 520;
	public static final int MEDAL_ID_MISSION = 570;
	public static final int MEDAL_ID_INTRO = 630;
	public static final int MEDAL_ID_ABOUT = 640;
	public static final int MEDAL_ID_TUTORIAL = 650;
	public static final int MEDAL_ID_LIBRARY = 660;
	public static final int MEDAL_ID_WEB_SITE_VISITED = 730;
	public static final int MEDAL_ID_INTERRUPTER = 735;

	public static Map<Integer, Item> gameLevelMedals = new HashMap<>();

	public static class Item {
		int id;
		int name;
		int description;
		int level;
		boolean unseen;
		Calculator calcFunc;
		int[] range;
		boolean gameLevel;

		private Item(int id, int name, int description, Calculator calcFunc, int[] range) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.calcFunc = calcFunc;
			this.range = range;
		}

		private Item(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public int getName() {
			return name;
		}

		public int getDescription() {
			return description;
		}

		public int getLevel() {
			return level;
		}

		public boolean isUnseen() {
			return unseen;
		}

		public int reachedThreshold() {
			return level > 0 ? range[level - 1] : 0;
		}

		public int nextThreshold() {
			return level < range.length ? range[level] : 0;
		}

		public int highestLevelThreshold() {
			return level + 1 == range.length ? range[range.length - 1] : 0;
		}

		public void viewed() {
			if (gameLevel) {
				getGameLevelMedal(id).unseen = false;
			}
			unseen = false;
		}

		private Item gameLevel() {
			gameLevel = true;
			return this;
		}
	}

	@FunctionalInterface
	public interface Calculator {
		long get();
	}

	private Alite game;

	private final List<Item> medals = new ArrayList<>(Arrays.asList(

		new Item(30, R.string.medal_career_man, R.string.medal_career_man_desc,
			() -> game.getPlayer().getRank(), getRangeOf(1,11)),

		new Item(35, R.string.medal_run_up, R.string.medal_run_up_desc,
			() -> game.getPlayer().getScore(), getSeriesOf125(1000,10000000)),

		new Item(MEDAL_ID_TIME, R.string.medal_addicted, R.string.medal_addicted_desc,
			() -> game.getPlayedDays(), getSeriesOf125(1,1000)),

		new Item(45, R.string.medal_systematic, R.string.medal_systematic_desc,
			() -> game.getPlayedContiguousDays(), new int[] {1, 7, 14, 30, 60, 91, 180, 365, 730, 1095, 1461, 1826}),


		// Travel-related trophies
		new Item(110, R.string.medal_traveller, R.string.medal_traveller_desc,
			() -> game.getPlayer().getTotalNumberOfPlanetVisit(), getSeriesOf125(2,1000000)),

		new Item(115, R.string.medal_explorer, R.string.medal_explorer_desc,
			() -> game.getPlayer().getNumberOfVisitedPlanets(),
			getSeriesOf125(2,GalaxyGenerator.EXTENDED_GALAXY_COUNT * 256)),

		new Item(120, R.string.medal_home_coming, R.string.medal_home_coming_desc,
			() -> game.getPlayer().getVisitNumberOfMostVisitedPlanet(), getSeriesOf125(2,1000)),

		new Item(125, R.string.medal_returner, R.string.medal_returner_desc,
			() -> Alite.elapsedDays(game.getPlayer().getMinLastVisitedTime()),
			new int[] {1, 7, 30, 91, 180, 365, 730, 1095, 1461, 1826}),

		new Item(MEDAL_ID_PLANETS, R.string.medal_roaming, R.string.medal_roaming_desc,
			() -> game.getPlayer().getNumberOfFullyVisitedGalaxies(),
			getRangeOf(1,GalaxyGenerator.EXTENDED_GALAXY_COUNT)),

		new Item(135, R.string.medal_hidden_treasure_hunter, R.string.medal_hidden_treasure_hunter_desc,
			() -> game.getPlayer().getNumberOfVisitedOrphanPlanets(),
			getSeriesOf125(1,GalaxyGenerator.ORPHAN_PLANET_COUNT)),

		new Item(140, R.string.medal_node_seeker, R.string.medal_node_seeker_desc,
			() -> game.getPlayer().getNumberOfVisitedNodePlanets(), getSeriesOf125(1,570)),

		new Item(145, R.string.medal_anarchist, R.string.medal_anarchist_desc,
			() -> game.getPlayer().getNumberOfVisitedAnarchyPlanets(), getSeriesOf125(1,7936)),

		new Item(MEDAL_ID_GALAXY, R.string.medal_galaxy_trotter, R.string.medal_galaxy_trotter_desc,
			() -> game.getPlayer().getNumberOfVisitedGalaxies(),
			new int[] {2, 4, 8, 9, 16, 32, 64, 128, 192, 225, GalaxyGenerator.EXTENDED_GALAXY_COUNT}),

		new Item(MEDAL_ID_HYPER_JUMP, R.string.medal_jetlag, R.string.medal_jetlag_desc,
			() -> game.getPlayer().getJumpCounter(), getSeriesOf125(1,100000)),

		new Item(160, R.string.medal_multi_cultural, R.string.medal_multi_cultural_desc,
			() -> game.getPlayer().getNumberOfVisitedSpecies(), getSeriesOf125(2,897)),

		new Item(MEDAL_ID_FUEL, R.string.medal_marathoner, R.string.medal_marathoner_desc,
			() -> game.getPlayer().getCobra().getTravelledDistance(), getSeriesOf125(1,10000000)),

		new Item(170, R.string.medal_approacher, R.string.medal_approacher_desc,
			() -> game.getPlayer().getManuallyDockedCount(), getSeriesOf125(1,1000)),


		// Trade-related trophies
		new Item(210, R.string.medal_trader, R.string.medal_trader_desc,
			() -> game.getPlayer().getTradedAmountInTonne(), getSeriesOf125(1,10000000)),

		new Item(MEDAL_ID_INVENTORY, R.string.medal_all_trader, R.string.medal_all_trader_desc,
			() -> TradeGoodStore.get().getTradedGoodsCount(),
			new int[] {1, 2, 4, 8, 12, TradeGoodStore.get().getGoodsCount()}),

		new Item(MEDAL_ID_FIREARMS, R.string.medal_illegal_trader, R.string.medal_illegal_trader_desc,
			() -> TradeGoodStore.get().hasTradedWithAllIllegalGoods(), getRangeOf(1,1)),

		new Item(225, R.string.medal_salesman, R.string.medal_salesman_desc,
			() -> game.getPlayer().getMaxGain(), getSeriesOf125(10,5000)),

		new Item(230, R.string.medal_loser, R.string.medal_loser_desc,
			() -> game.getPlayer().getMaxLoss(), getSeriesOf125(10,1000)),

		new Item(MEDAL_ID_RICH, R.string.medal_millionaire, R.string.medal_millionaire_desc,
			() -> game.getPlayer().getMaxCash(), getSeriesOf125(1000,10000000)),

		new Item(240, R.string.medal_collector, R.string.medal_collector_desc,
			() -> game.getPlayer().getCobra().getScoopAmount(), getSeriesOf125(1,100000)),

		new Item(MEDAL_ID_MINER, R.string.medal_miner, R.string.medal_miner_desc,
			() -> game.getPlayer().getCobra().getScoopedTonnePlatlet(), getSeriesOf125(1,1000)),

		new Item(250, R.string.medal_slave_holder, R.string.medal_slave_holder_desc,
			() -> game.getPlayer().getCobra().getScoopedTonneEscapeCapsule(), getSeriesOf125(1,100)),

		new Item(255, R.string.medal_fisherman, R.string.medal_fisherman_desc,
			() -> game.getPlayer().getCobra().getScoopedTonneAlien(), getSeriesOf125(1,1000)),

		new Item(260, R.string.medal_littering, R.string.medal_littering_desc,
			() -> game.getPlayer().getCobra().getEjectedAmount(), getSeriesOf125(1,500)),

		new Item(265, R.string.medal_boulder_pulping, R.string.medal_boulder_pulping_desc,
			() -> game.getPlayer().getKillCountAsteroid(), getSeriesOf125(1,1000)),


		// Attack-related trophies
		new Item(310, R.string.medal_destroyer, R.string.medal_destroyer_desc,
			() -> game.getPlayer().getKillCountPirate(), getSeriesOf125(1,1000000)),

		new Item(MEDAL_ID_INSECTICIDE, R.string.medal_insecticide, R.string.medal_insecticide_desc,
			() -> game.getPlayer().getKillCountThargoid(), getSeriesOf125(1,100000)),

		new Item(320, R.string.medal_buggy, R.string.medal_buggy_desc,
			() -> game.getPlayer().getKillCountInWitchSpace(), getSeriesOf125(1,100)),

		new Item(MEDAL_ID_KILL_THARGON, R.string.medal_passionate, R.string.medal_passionate_desc,
			() -> game.getPlayer().getKillCountThargon(), getSeriesOf125(1,100000)),

		new Item(MEDAL_ID_KILL_TRADER, R.string.medal_nearsighted, R.string.medal_nearsighted_desc,
			() -> game.getPlayer().getKillCountTrader(), getSeriesOf125(1,10)),

		new Item(MEDAL_ID_KILL_POLICE, R.string.medal_cop_killer, R.string.medal_cop_killer_desc,
			() -> game.getPlayer().getKillCountPolice(), getSeriesOf125(1,10)),

		new Item(330, R.string.medal_battering_ram, R.string.medal_battering_ram_desc,
			() -> game.getPlayer().getCobra().getCollisionAmount(), getSeriesOf125(1,100000)),

		new Item(MEDAL_ID_ENERGY_BOMB, R.string.medal_mass_destruction, R.string.medal_mass_destruction_desc,
			() -> game.getPlayer().getMaxScoreOfEnergyBomb(), getSeriesOf125(1,1000)),

		new Item(MEDAL_ID_MISSILE, R.string.medal_rocket_launcher, R.string.medal_rocket_launcher_desc,
			() -> game.getPlayer().getKillCountByMissile(), getSeriesOf125(1,1000)),

		new Item(350, R.string.medal_rocket_gambler, R.string.medal_rocket_gambler_desc,
			() -> game.getPlayer().getKillCountByLuckyMissile(), getSeriesOf125(1,1000)),

		new Item(MEDAL_ID_JAMMER, R.string.medal_jammer, R.string.medal_jammer_desc,
			() -> game.getPlayer().getKillCountByMissileJammer(), getSeriesOf125(1,1000)),


		// Defense related trophies
		new Item(MEDAL_ID_ECM, R.string.medal_rocket_slaughterer, R.string.medal_rocket_slaughterer_desc,
			() -> game.getPlayer().getKillCountMissileByEcm(), getSeriesOf125(1,100000)),

		new Item(MEDAL_ID_LASER, R.string.medal_golden_eye, R.string.medal_golden_eye_desc,
			() -> game.getPlayer().getKillCountMissileByLaser(), getSeriesOf125(1,1000)),

		new Item(420, R.string.medal_survivor, R.string.medal_survivor_desc,
			() -> game.getPlayer().getCobra().getLowEnergyCount(), getSeriesOf125(1,1000)),

		new Item(425, R.string.medal_rocket_crasher, R.string.medal_rocket_crasher_desc,
			() -> game.getPlayer().getCobra().getHitByMissileCount(), getSeriesOf125(1,100)),

		new Item(430, R.string.medal_cock_shot, R.string.medal_cock_shot_desc,
			() -> game.getPlayer().getCobra().getHitByLaserAmount(), getSeriesOf125(5,100000)),

		new Item(435, R.string.medal_blocker, R.string.medal_blocker_desc,
			() -> game.getPlayer().getCobra().getHitBySpaceStationCount(), getSeriesOf125(1,1)),

		new Item(MEDAL_ID_RETRO_ROCKETS, R.string.medal_flincher, R.string.medal_flincher_desc,
			() -> game.getPlayer().getCobra().getRetroRocketsTotalCount(), getSeriesOf125(1,100)),

		new Item(MEDAL_ID_CLOAKING, R.string.medal_invisible_man, R.string.medal_invisible_man_desc,
			() -> game.getPlayer().getCloakingUseCount(), getSeriesOf125(1,10000)),

		new Item(MEDAL_ID_ESCAPE_CAPSULE, R.string.medal_running_man, R.string.medal_running_man_desc,
			() -> game.getPlayer().getEscapeCapsuleUseCount(), getSeriesOf125(1,10)),


		new Item(MEDAL_ID_EQUIPMENT, R.string.medal_gadget_lover, R.string.medal_gadget_lover_desc,
			() -> game.getPlayer().getCobra().getMaxEquipments(),
			getSeriesOf125(2,EquipmentStore.get().getEquipmentCount())),

		new Item(MEDAL_ID_MECHANIC, R.string.medal_mechanic, R.string.medal_mechanic_desc,
			() -> game.getPlayer().getHyperdriveRepairCount(), getSeriesOf125(1,10)),

		new Item(MEDAL_ID_FUEL_SCOOP, R.string.medal_fuel_efficient, R.string.medal_fuel_efficient_desc,
			() -> game.getPlayer().getCobra().getScoopedFuel(), getSeriesOf125(5,1000)),


		new Item(MEDAL_ID_MISSION, R.string.medal_missionary, R.string.medal_missionary_desc,
			() -> MissionManager.getInstance().getCompletedMissionCount(),
			getRangeOf(1, MissionManager.getInstance().getMissions().size())),

		new Item(590, R.string.medal_law_breaker, R.string.medal_law_breaker_desc,
			() -> game.getPlayer().getMaxLegalValue(), new int[] {1, 16, 32, 64, 128, 192, 255}),

		new Item(620, R.string.medal_recidivist, R.string.medal_recidivist_desc,
			() -> game.getPlayer().getRecidivismCount(), getSeriesOf125(1,10)),


		new Item(MEDAL_ID_INTRO, R.string.medal_multiplexer, R.string.medal_multiplexer_desc,
			() -> countBits(getGameLevelValue(MEDAL_ID_INTRO), 2), getRangeOf(1,2)).gameLevel(),

		new Item(MEDAL_ID_ABOUT, R.string.medal_casting, R.string.medal_casting_desc,
			() -> getGameLevelValue(MEDAL_ID_ABOUT), getRangeOf(1,1)).gameLevel(),

		new Item(MEDAL_ID_TUTORIAL, R.string.medal_examinee, R.string.medal_examinee_desc,
			() -> countBits(getGameLevelValue(MEDAL_ID_TUTORIAL), TutorialScreen.MISSION_COUNT),
			getRangeOf(1,7)).gameLevel(),

		new Item(MEDAL_ID_LIBRARY, R.string.medal_bookworm, R.string.medal_bookworm_desc,
			() -> getGameLevelValue(MEDAL_ID_LIBRARY), getSeriesOf125(1,getLibraryContentSize())).gameLevel(),


		new Item(MEDAL_ID_WEB_SITE_VISITED, R.string.medal_public_spirit, R.string.medal_public_spirit_desc,
			() -> getGameLevelValue(MEDAL_ID_WEB_SITE_VISITED), getRangeOf(1,1)).gameLevel(),

		new Item(MEDAL_ID_INTERRUPTER, R.string.medal_interrupter, R.string.medal_interrupter_desc,
			() -> countBits(getGameLevelValue(MEDAL_ID_INTERRUPTER), 2), getRangeOf(1,2)).gameLevel()
	));

	private int countBits(int number, int checkCount) {
		int count = 0;
		for (int i = 0; i < checkCount; i++) {
			if ((number & (1 << i)) != 0) {
				count++;
			}
		}
		return count;
	}

	private int getLibraryContentSize() {
		try {
			return Toc.read(L.raw(Toc.TOC_FILENAME)).getEntries(null).size();
		} catch (IOException ignored) {
			return 91;
		}
	}

	private static int[] getRangeOf(int start, int end) {
		int[] range = new int[end - start + 1];
		for (int i = start; i <= end; i++) {
			range[i - start] = i;
		}
		return range;
	}

	private static final int[] pattern125 = new int[] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000,
		10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000 };

	private int[] getSeriesOf125(int start, int end) {
		if (start == end) {
			return new int[] { start };
		}
		int startIndex = 0;
		while (start >= pattern125[startIndex]) {
			startIndex++;
		}
		int endIndex = startIndex;
		while (end > pattern125[endIndex]) {
			endIndex++;
		}
		int[] range = new int[endIndex - startIndex + 2];
		if (endIndex - startIndex > 0) {
			System.arraycopy(pattern125, startIndex, range, 1, endIndex - startIndex);
		}
		range[0] = start;
		range[endIndex - startIndex + 1] = end;
		return range;
	}

	public Medal(Alite game) {
		this.game = game;
	}

	private static Item getGameLevelMedal(int id) {
		Item medal = gameLevelMedals.get(id);
		if (medal == null) {
			medal = new Item(id);
			gameLevelMedals.put(id, medal);
		}
		return medal;
	}

	private int getGameLevelValue(int id) {
		return getGameLevelMedal(id).level;
	}

	public static boolean changeGameLevelValue(int id, int value) {
		Item medal = getGameLevelMedal(id);
		if (medal.level != value) {
			medal.level = value;
			medal.unseen = true;
			return true;
		}
		return false;
	}

	public static void setGameLevelBitValue(int id, int value) {
		changeGameLevelValue(id, getGameLevelMedal(id).level | value);
	}

	public static String getGameLevelMedals() {
		String result = "";
		for(int id : gameLevelMedals.keySet()) {
			Item medal = getGameLevelMedal(id);
			if (medal.level > 0) {
				result += id + "," + medal.level + "," + medal.unseen + ";";
			}
		}
		return result;
	}

	public static void setGameLevelMedals(String values) {
		for(String item : values.split(";")) {
			String[] fields = item.split(",");
			Item medal = getGameLevelMedal(Integer.parseInt(fields[0]));
			medal.level = Integer.parseInt(fields[1]);
			medal.unseen = Boolean.parseBoolean(fields[2]);
		}
	}

	public void initMedals() {
		for (Item m : medals) {
			m.level = 0;
			long achieve = m.calcFunc.get();
			for (long l : m.range) {
				if (achieve < l) {
					AliteLog.d("initMedals", "Achievement #" + m.id +
						": current = " + achieve + ", highest level = " + m.level);
					break;
				}
				m.level++;
			}
		}
		for (Item g : gameLevelMedals.values()) {
			if (!g.unseen) {
				continue;
			}
			for (Item m : medals) {
				if (g.id == m.id) {
					m.unseen = true;
				}
			}
		}
	}

	public void initMedals(JSONArray unseenMedals) throws JSONException {
		initMedals();
		if (unseenMedals != null) {
			for (int i = 0; i < unseenMedals.length(); i++) {
				int id = unseenMedals.getInt(i);
				for (Item m : medals) {
					if (m.id == id) {
						m.unseen = true;
						break;
					}
				}
			}
		}
	}

	public int updateMedals() {
		AliteLog.d("updateMedals", "Started...");
		int newMedalCount = 0;
		for (Item m : medals) {
			if (m.level >= m.range.length) {
				if (m.unseen) {
					newMedalCount++;
				}
				continue;
			}
			long achieve = m.calcFunc.get();
			if (achieve < m.range[m.level]) {
				if (m.unseen) {
					newMedalCount++;
				}
				continue;
			}
			m.unseen = true;
			newMedalCount++;
			do {
				m.level++;
			} while (m.level < m.range.length && achieve >= m.range[m.level]);
			AliteLog.d("updateMedals", "Achievement #" + m.id +
				": current = " + achieve + ", highest level = " + m.level);
		}
		return newMedalCount;
	}

	public List<Item> getMedals() {
		return medals;
	}

	public JSONArray toJson() {
		JSONArray unseen = new JSONArray();
		for (Item m : medals) {
			if (!m.gameLevel && m.unseen) {
				unseen.put(m.id);
			}
		}
		return unseen;
	}

}
