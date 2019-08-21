package de.phbouillon.android.games.alite.screens.canvas;

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
import java.io.DataOutputStream;
import java.io.IOException;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.InventoryItem;
import de.phbouillon.android.games.alite.model.Laser;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class HackerScreen extends AliteScreen {
	private HackerState state;
	private int yPosition = 0;
	private int startY;
	private int startX;
	private int lastY;
	private int maxY = 430;
	private int deltaY = 0;
	private Button done;
	private int offset;
	private Button[] values = new Button[256];

	class HackerState {
		byte[] values = new byte[256];

		String getCommanderName() {
			int len = 0;
			for (int i = 0; i < 16; i++) {
				if (values[i] == 0) {
					len = i;
				}
			}
			if (len == 0) {
				return "";
			}
			return new String(values, 0, len, StringUtil.CHARSET).trim();
		}

		private boolean getBitFromState(int offset, int bit) {
			return (values[offset] & (1 << (bit - 1))) != 0;
		}

		private void setBitToState(boolean value, int offset, int bit) {
			int val = 1 << (bit - 1);
			values[offset] = (byte) ((values[offset] & (255 - val)) + (value ? val : 0));
		}

		private int getLowerHalfByte(int offset) {
			return values[offset] & 15;
		}

		private void setLowerHalfByte(int value, int offset) {
			values[offset] = (byte) ((value & 15) + (values[offset] & 240));
		}

		private int getUpperHalfByte(int offset) {
			return (values[offset] & 240) >> 4;
		}

		private void setUpperHalfByte(int value, int offset) {
			values[offset] = (byte) (((value & 15) << 4) + (values[offset] & 15));
		}

		private long getLongFromState(int offset, int bytes) {
			bytes--;
			long result = 0;
			int counter = 0;
			for (; bytes >= 0; bytes--) {
				result += (long) (values[offset + counter++] & 0xFF) << (bytes << 3);
			}
			return result;
		}

		private void setLongToState(long value, int offset, int bytes) {
			for (int i = 0; i < bytes; i++) {
				values[offset + i] = 0;
			}
			for (int pos = offset; pos < offset + bytes; pos++) {
				values[pos] = (byte) ((value >> ((offset + bytes - pos - 1) << 3)) & 0xFF);
			}
		}

		private int getIntFromState(int offset, int bytes) {
			bytes--;
			int result = 0;
			int counter = 0;
			for (; bytes >= 0; bytes--) {
				result += (values[offset + counter++] & 0xFF) << (bytes << 3);
			}
			return result;
		}

		private void setIntToState(int value, int offset, int bytes) {
			for (int i = 0; i < bytes; i++) {
				values[offset + i] = 0;
			}
			for (int pos = offset; pos < offset + bytes; pos++) {
				values[pos] = (byte) ((value >> ((offset + bytes - pos - 1) << 3)) & 0xFF);
			}
		}

		void setCommanderName(String name) {
			byte[] commanderName = name.getBytes(StringUtil.CHARSET);
			System.arraycopy(commanderName, 0, values, 0, Math.min(commanderName.length, 16));
			for (int i = commanderName.length; i < 16; i++) {
				values[i] = 0;
			}
		}

		int getGalaxyNumber() {
			return values[16] & 0xFF;
		}

		void setGalaxyNumber(int galaxyNumber) {
			values[16] = (byte) (galaxyNumber & 0xFF);
		}

		int getCurrentSystem() {
			return values[23] & 0xFF;
		}

		void setCurrentSystem(int system) {
			values[23] = (byte) (system & 0xFF);
		}

		int getHyperspaceSystem() {
			return values[24] & 0xFF;
		}

		void setHyperspaceSystem(int system) {
			values[24] = (byte) (system & 0xFF);
		}

		int getFuel() {
			return values[25] & 0xFF;
		}

		void setFuel(int fuel) {
			values[25] = (byte) (fuel & 0xFF);
		}

		long getCredits() {
			return getLongFromState(26, 6);
		}

		void setCredits(long credits) {
			setLongToState(credits, 26, 6);
		}

		int getRating() {
			return values[32] & 0xFF;
		}

		void setRating(int rating) {
			values[32] = (byte) (rating & 0xFF);
		}

		int getLegalStatus() {
			return values[33] & 0xFF;
		}

		void setLegalStatus(int legalStatus) {
			values[33] = (byte) (legalStatus & 0xFF);
		}

		long getGameTime() {
			return getLongFromState(34, 5);
		}

		void setGameTime(long gameTime) {
			setLongToState(gameTime, 34, 5);
		}

		int getScore() {
			return getIntFromState(39, 4);
		}

		void setScore(int score) {
			setIntToState(score, 39, 4);
		}

		int getNumberOfMissiles() {
			return getLowerHalfByte(48);
		}

		void setNumberOfMissiles(int missiles) {
			setLowerHalfByte(missiles, 48);
		}

		int getExtraEnergyUnit() {
			return getUpperHalfByte(48);
		}

		void setExtraEnergyUnit(int eeu) {
			setUpperHalfByte(eeu, 48);
		}

		boolean isLargeCargoBay() {
			return getBitFromState(49, 1);
		}

		void setLargeCargoBay(boolean lcb) {
			setBitToState(lcb, 49, 1);
		}

		boolean isECM() {
			return getBitFromState(49, 2);
		}

		void setECM(boolean ecm) {
			setBitToState(ecm, 49, 2);
		}

		boolean isFuelScoop() {
			return getBitFromState(49, 3);
		}

		void setFuelScoop(boolean scoop) {
			setBitToState(scoop, 49, 3);
		}

		boolean isEscapeCapsule() {
			return getBitFromState(49, 4);
		}

		void setEscapeCapsule(boolean escape) {
			setBitToState(escape, 49, 4);
		}

		boolean isEnergyBomb() {
			return getBitFromState(49, 5);
		}

		void setEnergyBomb(boolean bomb) {
			setBitToState(bomb, 49, 5);
		}

		boolean isDockingComputer() {
			return getBitFromState(49, 6);
		}

		void setDockingComputer(boolean dock) {
			setBitToState(dock, 49, 6);
		}

		boolean isGalacticHyperdrive() {
			return getBitFromState(49, 7);
		}

		void setGalacticHyperdrive(boolean galHyp) {
			setBitToState(galHyp, 49, 7);
		}

		boolean isRetroRockets() {
			return getBitFromState(49, 8);
		}

		void setRetroRockets(boolean retro) {
			setBitToState(retro, 49, 8);
		}

		int getPulseLaser() {
			return getLowerHalfByte(50);
		}

		void setPulseLaser(int pulseLaser) {
			setLowerHalfByte(pulseLaser, 50);
		}

		int getBeamLaser() {
			return getUpperHalfByte(50);
		}

		void setBeamLaser(int beamLaser) {
			setUpperHalfByte(beamLaser, 50);
		}

		int getMiningLaser() {
			return getLowerHalfByte(51);
		}

		void setMiningLaser(int miningLaser) {
			setLowerHalfByte(miningLaser, 51);
		}

		int getMilitaryLaser() {
			return getUpperHalfByte(51);
		}

		void setMilitaryLaser(int militaryLaser) {
			setUpperHalfByte(militaryLaser, 51);
		}

		boolean isCloakingDevice() {
			return getBitFromState(52, 2);
		}

		void setCloakingDevice(boolean cloak) {
			setBitToState(cloak, 52, 2);
		}

		boolean isECMJammer() {
			return getBitFromState(52, 1);
		}

		void setECMJammer(boolean ecmJam) {
			setBitToState(ecmJam, 52, 1);
		}

		void setHyperspaceJumpCounter(int jumpCounter) {
			setIntToState(jumpCounter, 160, 2);
		}

		void setIntergalacticJumpCounter(int jumpCounter) {
			values[162] = (byte) jumpCounter;
		}

		void setActiveMissionIndex(int index) {
			values[163] = (byte) index;
		}

		long getFood()                                   { return getLongFromState(0x40, 4); }
		void setFood(long value)                         { setLongToState(value, 0x40, 4);   }
		long getTextiles()                               { return getLongFromState(0x44, 4); }
		void setTextiles(long value)                     { setLongToState(value, 0x44, 4);   }
		long getRadioactives()                           { return getLongFromState(0x48, 4); }
		void setRadioactives(long value)                 { setLongToState(value, 0x48, 4);   }
		long getSlaves()                                 { return getLongFromState(0x4C, 4); }
		void setSlaves(long value)                       { setLongToState(value, 0x4C, 4);   }
		long getLiquorWines()                            { return getLongFromState(0x50, 4); }
		void setLiquorWines(long value)                  { setLongToState(value, 0x50, 4);   }
		long getLuxuries()                               { return getLongFromState(0x54, 4); }
		void setLuxuries(long value)                     { setLongToState(value, 0x54, 4);   }
		long getNarcotics()                              { return getLongFromState(0x58, 4); }
		void setNarcotics(long value)                    { setLongToState(value, 0x58, 4);   }
		long getComputers()                              { return getLongFromState(0x5C, 4); }
		void setComputers(long value)                    { setLongToState(value, 0x5C, 4);   }
		long getMachinery()                              { return getLongFromState(0x60, 4); }
		void setMachinery(long value)                    { setLongToState(value, 0x60, 4);   }
		long getAlloys()                                 { return getLongFromState(0x64, 4); }
		void setAlloys(long value)                       { setLongToState(value, 0x64, 4);   }
		long getFirearms()                               { return getLongFromState(0x68, 4); }
		void setFirearms(long value)                     { setLongToState(value, 0x68, 4);   }
		long getFurs()                                   { return getLongFromState(0x6C, 4); }
		void setFurs(long value)                         { setLongToState(value, 0x6C, 4);   }
		long getMinerals()                               { return getLongFromState(0x70, 4); }
		void setMinerals(long value)                     { setLongToState(value, 0x70, 4);   }
		long getGold()                                   { return getLongFromState(0x74, 4); }
		void setGold(long value)                         { setLongToState(value, 0x74, 4);   }
		long getPlatinum()                               { return getLongFromState(0x78, 4); }
		void setPlatinum(long value)                     { setLongToState(value, 0x78, 4);   }
		long getGemStones()                              { return getLongFromState(0x7C, 4); }
		void setGemStones(long value)                    { setLongToState(value, 0x7C, 4);   }
		long getAlienItems()                             { return getLongFromState(0x80, 4); }
		void setAlienItems(long value)                   { setLongToState(value, 0x80, 4);   }
		long getMedicalSupplies()                        { return getLongFromState(0x84, 4); }
		void setMedicalSupplies(long value)              { setLongToState(value, 0x84, 4);   }

		/*
		 .. 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
		 00 Commander Name.................................
		 10 GN Galaxy Seed...... CS HS FU Credits..........
		 20 RA LS Game Time..... Score......
		 30 Equipment.....
		 40 Food....... Textiles... Radioactive Slaves.....
		 50 Liquors.... Luxuries... Narcotics.. Computers..
		 60 Machinery.. Alloys..... Firearms... Furs.......
		 70 Minerals... Gold....... Platinum... GemStones..
		 80 AlienItems. MedSupplies
		 90
		 A0 HJ HJ IJ MI Galaxy Seed...... TI ST
		 B0 IV UR UF EE CE                                    // TODO Invulnerable, Unlimited Retro Rockets, Unlimited Fuel, ECM Jammer energy usage, Cloaking Device energy usage
		 C0
		 D0
		 E0
		 F0

		 HJ Hyperspace Jump Counter
		 IJ Intergalactic Jump Counter
		 MI Active Mission Index
		 TI Active Mission Target Index
		 ST Active Mission State
		*/
	}

	// public constructor(Alite) is required for navigation bar
	public HackerScreen(Alite game) {
		super(game);
		offset = game.getGraphics().getTextWidth("MM", Assets.titleFont) - 8;
		game.getNavigationBar().setActive(false);
		initializeState(game);
	}

	@Override
	public void activate() {
		done = Button.createGradientTitleButton(1720, 880, 200, 200, L.string(R.string.hacker_btn_done));
		int counter = 0;
		for (int y = 0; y < 16; y++) {
			int yPos = (int) (140 + 80 * (y + 1) - Assets.titleFont.getSize());
			for (int x = 0; x < 16; x++) {
				values[counter] = Button.createTitleButton(5 + offset * (x + 1), yPos, offset, 80, String.format("%02X", state.values[counter]));
				counter++;
			}
		}
		game.getPlayer().setCheater(true);
	}

	static HackerScreen readScreen(Alite alite, DataInputStream dis) {
		HackerScreen hs = new HackerScreen(alite);
		try {
			dis.read(hs.state.values, 0, 256);
			for (int i = 0; i < 256; i++) {
				hs.values[i].setText(String.format("%02X", hs.state.values[i]));
			}
			hs.yPosition = dis.readInt();
		} catch (IOException e) {
			AliteLog.e("Hacker Screen Initialize", "Error in initializer.", e);
			return null;
		}
		return hs;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		HackerScreen hs = readScreen(alite, dis);
		if (hs == null) {
			return false;
		}
		alite.setScreen(hs);
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.write(state.values, 0, 256);
		dos.writeInt(yPosition);
	}

	private int getLaserValue(PlayerCobra cobra, Laser laser) {
		return (cobra.getLaser(PlayerCobra.DIR_FRONT) == laser ? 1 : 0) +
			   (cobra.getLaser(PlayerCobra.DIR_RIGHT) == laser ? 2 : 0) +
			   (cobra.getLaser(PlayerCobra.DIR_REAR)  == laser ? 4 : 0) +
			   (cobra.getLaser(PlayerCobra.DIR_LEFT)  == laser ? 8 : 0);
	}

	private void initializeState(Alite alite) {
		state = new HackerState();
		Player player = alite.getPlayer();
		GalaxyGenerator generator = alite.getGenerator();
		PlayerCobra cobra = alite.getCobra();

		state.setCommanderName(player.getName());
		state.setGalaxyNumber(generator.getCurrentGalaxy());
		state.setCurrentSystem(player.getCurrentSystem() == null ? 0 : player.getCurrentSystem().getIndex());
		state.setHyperspaceSystem(player.getHyperspaceSystem() == null ? 0 : player.getHyperspaceSystem().getIndex());
		state.setFuel(cobra.getFuel());
		state.setCredits(player.getCash());
		state.setRating(player.getRating().ordinal());
		state.setLegalStatus(player.getLegalStatus().ordinal());
		state.setGameTime(alite.getGameTime() / 1000000);
		state.setScore(player.getScore());
		state.setNumberOfMissiles(cobra.getMissiles());
		state.setExtraEnergyUnit(cobra.isEquipmentInstalled(EquipmentStore.extraEnergyUnit) ? 1 :
	         			         cobra.isEquipmentInstalled(EquipmentStore.navalEnergyUnit) ? 2 : 0);
		state.setLargeCargoBay(cobra.isEquipmentInstalled(EquipmentStore.largeCargoBay));
		state.setECM(cobra.isEquipmentInstalled(EquipmentStore.ecmSystem));
		state.setFuelScoop(cobra.isEquipmentInstalled(EquipmentStore.fuelScoop));
		state.setEscapeCapsule(cobra.isEquipmentInstalled(EquipmentStore.escapeCapsule));
		state.setEnergyBomb(cobra.isEquipmentInstalled(EquipmentStore.energyBomb));
		state.setDockingComputer(cobra.isEquipmentInstalled(EquipmentStore.dockingComputer));
		state.setGalacticHyperdrive(cobra.isEquipmentInstalled(EquipmentStore.galacticHyperdrive));
		state.setRetroRockets(cobra.isEquipmentInstalled(EquipmentStore.retroRockets));
		state.setPulseLaser(getLaserValue(cobra, EquipmentStore.pulseLaser));
		state.setBeamLaser(getLaserValue(cobra, EquipmentStore.beamLaser));
		state.setMiningLaser(getLaserValue(cobra, EquipmentStore.miningLaser));
		state.setMilitaryLaser(getLaserValue(cobra, EquipmentStore.militaryLaser));
		state.setCloakingDevice(cobra.isEquipmentInstalled(EquipmentStore.cloakingDevice));
		state.setECMJammer(cobra.isEquipmentInstalled(EquipmentStore.ecmJammer));
		InventoryItem[] inventory = cobra.getInventory();
		state.setFood(inventory[0].getWeight().getWeightInGrams());
		state.setTextiles(inventory[1].getWeight().getWeightInGrams());
		state.setRadioactives(inventory[2].getWeight().getWeightInGrams());
		state.setSlaves(inventory[3].getWeight().getWeightInGrams());
		state.setLiquorWines(inventory[4].getWeight().getWeightInGrams());
		state.setLuxuries(inventory[5].getWeight().getWeightInGrams());
		state.setNarcotics(inventory[6].getWeight().getWeightInGrams());
		state.setComputers(inventory[7].getWeight().getWeightInGrams());
		state.setMachinery(inventory[8].getWeight().getWeightInGrams());
		state.setAlloys(inventory[9].getWeight().getWeightInGrams());
		state.setFirearms(inventory[10].getWeight().getWeightInGrams());
		state.setFurs(inventory[11].getWeight().getWeightInGrams());
		state.setMinerals(inventory[12].getWeight().getWeightInGrams());
		state.setGold(inventory[13].getWeight().getWeightInGrams());
		state.setPlatinum(inventory[14].getWeight().getWeightInGrams());
		state.setGemStones(inventory[15].getWeight().getWeightInGrams());
		state.setAlienItems(inventory[16].getWeight().getWeightInGrams());
		state.setMedicalSupplies(inventory[17].getWeight().getWeightInGrams());
		state.setHyperspaceJumpCounter(player.getJumpCounter());
		state.setIntergalacticJumpCounter(player.getIntergalacticJumpCounter());
		if (!player.getActiveMissions().isEmpty()) {
			state.setActiveMissionIndex(player.getActiveMissions().get(0).getId());
			// TODO add mission state, target, galaxy seed
		}
	}

	private void setEquipped(PlayerCobra cobra, Equipment equip, boolean value) {
		if (value) {
			cobra.addEquipment(equip);
		} else {
			cobra.removeEquipment(equip);
		}
	}

	private void equipLaser(int where, Laser laser, PlayerCobra cobra) {
		if ((where & 1) > 0) cobra.setLaser(PlayerCobra.DIR_FRONT, laser);
		if ((where & 2) > 0) cobra.setLaser(PlayerCobra.DIR_RIGHT, laser);
		if ((where & 4) > 0) cobra.setLaser(PlayerCobra.DIR_REAR,  laser);
		if ((where & 8) > 0) cobra.setLaser(PlayerCobra.DIR_LEFT,  laser);
	}

	private void assignState(Alite alite) {
		Player player = alite.getPlayer();
		GalaxyGenerator generator = alite.getGenerator();
		PlayerCobra cobra = alite.getCobra();

		player.setName(state.getCommanderName());
		generator.setCurrentGalaxy(state.getGalaxyNumber());
		if (player.getCurrentSystem() == null || state.getCurrentSystem() != player.getCurrentSystem().getIndex()) {
			player.setCurrentSystem(generator.getSystem(state.getCurrentSystem()));
		}
		player.setHyperspaceSystem(generator.getSystem(state.getHyperspaceSystem()));
		cobra.setFuel(state.getFuel());
		player.setCash(state.getCredits());
		player.setRating(Rating.values()[state.getRating()]);
		player.setLegalStatus(LegalStatus.values()[state.getLegalStatus()]);
		alite.setGameTime(state.getGameTime() * 1000000);
		player.setScore(state.getScore());
		cobra.setMissiles(state.getNumberOfMissiles());
		int extraEnergyUnit = state.getExtraEnergyUnit();
		setEquipped(cobra, EquipmentStore.extraEnergyUnit, extraEnergyUnit == 1);
		setEquipped(cobra, EquipmentStore.navalEnergyUnit, extraEnergyUnit == 2);
		setEquipped(cobra, EquipmentStore.largeCargoBay, state.isLargeCargoBay());
		setEquipped(cobra, EquipmentStore.ecmSystem, state.isECM());
		setEquipped(cobra, EquipmentStore.fuelScoop, state.isFuelScoop());
		setEquipped(cobra, EquipmentStore.escapeCapsule, state.isEscapeCapsule());
		setEquipped(cobra, EquipmentStore.energyBomb, state.isEnergyBomb());
		setEquipped(cobra, EquipmentStore.dockingComputer, state.isDockingComputer());
		setEquipped(cobra, EquipmentStore.galacticHyperdrive, state.isGalacticHyperdrive());
		setEquipped(cobra, EquipmentStore.cloakingDevice, state.isCloakingDevice());
		setEquipped(cobra, EquipmentStore.ecmJammer, state.isECMJammer());
		setEquipped(cobra, EquipmentStore.retroRockets, state.isRetroRockets());
		// Punish player for cheating: If he enters values for all laser types,
		// accept the least powerful one only... (I.e. set military laser first and
		// overwrite it with lesser lasers if values are present...)
		equipLaser(15, null, cobra);
		equipLaser(state.getMilitaryLaser(), EquipmentStore.militaryLaser, cobra);
		equipLaser(state.getBeamLaser(), EquipmentStore.beamLaser, cobra);
		equipLaser(state.getMiningLaser(), EquipmentStore.miningLaser, cobra);
		equipLaser(state.getPulseLaser(), EquipmentStore.pulseLaser, cobra);
		InventoryItem[] inventory = cobra.getInventory();
		cobra.setTradeGood(TradeGoodStore.get().food(), Weight.grams(state.getFood()), inventory[0].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().textiles(), Weight.grams(state.getTextiles()), inventory[1].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().radioactives(), Weight.grams(state.getRadioactives()), inventory[2].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().slaves(), Weight.grams(state.getSlaves()), inventory[3].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().liquorWines(), Weight.grams(state.getLiquorWines()), inventory[4].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().luxuries(), Weight.grams(state.getLuxuries()), inventory[5].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().narcotics(), Weight.grams(state.getNarcotics()), inventory[6].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().computers(), Weight.grams(state.getComputers()), inventory[7].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().machinery(), Weight.grams(state.getMachinery()), inventory[8].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().alloys(), Weight.grams(state.getAlloys()), inventory[9].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().firearms(), Weight.grams(state.getFirearms()), inventory[10].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().furs(), Weight.grams(state.getFurs()), inventory[11].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().minerals(), Weight.grams(state.getMinerals()), inventory[12].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().gold(), Weight.grams(state.getGold()), inventory[13].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().platinum(), Weight.grams(state.getPlatinum()), inventory[14].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().gemStones(), Weight.grams(state.getGemStones()), inventory[15].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().alienItems(), Weight.grams(state.getAlienItems()), inventory[16].getPrice());
		cobra.setTradeGood(TradeGoodStore.get().medicalSupplies(), Weight.grams(state.getMedicalSupplies()), inventory[17].getPrice());
	}

	void changeState(int valueIndex, byte newValue) {
		state.values[valueIndex] = newValue;
		values[valueIndex].setText(String.format("%02X", newValue));
	}

	@Override
	protected void performScreenChange() {
		if (inFlightScreenChange()) {
			return;
		}
		Screen oldScreen = game.getCurrentScreen();
		if (!(newScreen instanceof HexNumberPadScreen)) {
			oldScreen.dispose();
		}
		game.setScreen(newScreen);
		game.getNavigationBar().performScreenChange();
		postScreenChange();
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_DOWN && touch.pointer == 0) {
			startX = touch.x;
			startY = lastY = touch.y;
		}
		if (touch.type == TouchEvent.TOUCH_DRAGGED && touch.pointer == 0) {
			if (touch.x > AliteConfig.DESKTOP_WIDTH) {
				return;
			}
			yPosition += lastY - touch.y;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
			lastY = touch.y;
		}
		if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0) {
			if (Math.abs(startX - touch.x) < 20 &&
				Math.abs(startY - touch.y) < 20) {
				if (done.isTouched(touch.x, touch.y)) {
					assignState(game);
					game.getNavigationBar().setActive(true);
					game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
					newScreen = new StatusScreen(game);
					SoundManager.play(Assets.click);
				} else {
					for (int i = 0; i < 256; i++) {
						Button b = values[i];
						b.setSelected(false);
						if (b.isTouched(touch.x, touch.y)) {
							b.setSelected(true);
							newScreen = new HexNumberPadScreen(this, game, i % 16 < 8 ? 975 : 60, 180, i);
							SoundManager.play(Assets.click);
						}
					}
				}
			}
		}
		if (touch.type == TouchEvent.TOUCH_SWEEP) {
			deltaY = touch.y2;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayWideTitle(L.string(R.string.title_hacker));

		for (int i = 0; i < 16; i++) {
			String hex = String.format("%02X", i);
			g.drawText(hex, 20 + offset * (i + 1), 140, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.titleFont);
		}

		if (deltaY != 0) {
			deltaY += deltaY > 0 ? -1 : 1;
			yPosition -= deltaY;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
		}

		g.setClip(0, 60, -1, 930);
		for (int i = 0; i < 16; i++) {
			String hex = String.format("%X0", i);
			int y = 140 - yPosition + 80 * (i + 1);
			g.drawText(hex, 20, y, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.titleFont);
		}
		int count = 0;
		for (Button b: values) {
			b.setYOffset(-yPosition);
			b.setTextColor(b.isSelected() ? ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION) :
				(count / 16 + count % 16) % 2 == 0 ? ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT) :
				ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
			b.render(g);
			count++;
		}
		g.setClip(-1, -1, -1, -1);

		done.render(g);
	}

	@Override
	public void renderNavigationBar() {
		// No navigation bar desired.
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.HACKER_SCREEN;
	}
}
