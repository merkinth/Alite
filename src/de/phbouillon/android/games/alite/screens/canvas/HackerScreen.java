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
import de.phbouillon.android.games.alite.model.*;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.trading.TradeGood;

//This screen never needs to be serialized, as it is not part of the InGame state.
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

	private static class HackerState {
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

		long getGood(TradeGood tradeGood) {
			return getLongFromState(0x40 + 4 * tradeGood.getId(), 4);
		}

		void setGood(TradeGood tradeGood, long value) {
			int id = tradeGood.getId();
			if (id >= 0 && id <= 17) {
				setLongToState(value, 0x40 + 4 * id, 4);
			}
		}

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
		 // TODO Invulnerable, Unlimited Retro Rockets, Unlimited Fuel, ECM Jammer energy usage, Cloaking Device energy usage
		 B0 IV UR UF EE CE
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

	// default public constructor is required for navigation bar
	public HackerScreen() {
		offset = game.getGraphics().getTextWidth("MM", Assets.titleFont) - 8;
		game.getNavigationBar().setActive(false);
		initializeState();
	}

	public HackerScreen(DataInputStream dis) throws IOException {
		this();
		dis.read(state.values, 0, 256);
		for (int i = 0; i < 256; i++) {
			values[i].setText(String.format("%02X", state.values[i]));
		}
		yPosition = dis.readInt();
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

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.write(state.values, 0, 256);
		dos.writeInt(yPosition);
	}

	private void initializeState() {
		state = new HackerState();
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = game.getCobra();

		state.setCommanderName(player.getName());
		state.setGalaxyNumber(generator.getCurrentGalaxy());
		state.setCurrentSystem(player.getCurrentSystem() == null ? 0 : player.getCurrentSystem().getIndex());
		state.setHyperspaceSystem(player.getHyperspaceSystem() == null ? 0 : player.getHyperspaceSystem().getIndex());
		state.setFuel(cobra.getFuel());
		state.setCredits(player.getCash());
		state.setRating(player.getRating().ordinal());
		state.setLegalStatus(player.getLegalStatus().ordinal());
		state.setGameTime(game.getGameTime() / 1000000);
		state.setScore(player.getScore());
		state.setNumberOfMissiles(cobra.getMissiles());
		state.setExtraEnergyUnit(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT)) ? 1 :
	         			         cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT)) ? 2 : 0);
		state.setLargeCargoBay(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY)));
		state.setECM(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_SYSTEM)));
		state.setFuelScoop(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP)));
		state.setEscapeCapsule(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE)));
		state.setEnergyBomb(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB)));
		state.setDockingComputer(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER)));
		state.setGalacticHyperdrive(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE)));
		state.setRetroRockets(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS)));
		state.setPulseLaser(cobra.getLaserValue(EquipmentStore.PULSE_LASER));
		state.setBeamLaser(cobra.getLaserValue(EquipmentStore.BEAM_LASER));
		state.setMiningLaser(cobra.getLaserValue(EquipmentStore.MINING_LASER));
		state.setMilitaryLaser(cobra.getLaserValue(EquipmentStore.MILITARY_LASER));
		state.setCloakingDevice(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE)));
		state.setECMJammer(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_JAMMER)));
		for (InventoryItem item : cobra.getInventory()) {
			state.setGood(item.getGood(), item.getWeight().getWeightInGrams());
		}
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

	private void assignState() {
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = game.getCobra();

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
		game.setGameTime(state.getGameTime() * 1000000);
		player.setScore(state.getScore());
		cobra.setMissiles(state.getNumberOfMissiles());
		int extraEnergyUnit = state.getExtraEnergyUnit();
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT), extraEnergyUnit == 1);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT), extraEnergyUnit == 2);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY), state.isLargeCargoBay());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_SYSTEM), state.isECM());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP), state.isFuelScoop());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE), state.isEscapeCapsule());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB), state.isEnergyBomb());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER), state.isDockingComputer());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE), state.isGalacticHyperdrive());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE), state.isCloakingDevice());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_JAMMER), state.isECMJammer());
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS), state.isRetroRockets());
		// Punish player for cheating: If he enters values for all laser types,
		// accept the least powerful one only... (I.e. set military laser first and
		// overwrite it with lesser lasers if values are present...)
		cobra.equipLaser(15, "");
		cobra.equipLaser(state.getMilitaryLaser(), EquipmentStore.MILITARY_LASER);
		cobra.equipLaser(state.getBeamLaser(), EquipmentStore.BEAM_LASER);
		cobra.equipLaser(state.getMiningLaser(), EquipmentStore.MINING_LASER);
		cobra.equipLaser(state.getPulseLaser(), EquipmentStore.PULSE_LASER);
		for (InventoryItem item : cobra.getInventory()) {
			cobra.setTradeGood(item.getGood(), Weight.grams(state.getGood(item.getGood())), item.getPrice());
		}
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
					assignState();
					game.getNavigationBar().setActive(true);
					game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
					newScreen = new StatusScreen();
					SoundManager.play(Assets.click);
				} else {
					for (int i = 0; i < 256; i++) {
						Button b = values[i];
						b.setSelected(false);
						if (b.isTouched(touch.x, touch.y)) {
							b.setSelected(true);
							newScreen = new HexNumberPadScreen(this, i % 16 < 8 ? 975 : 60, 180, i);
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
