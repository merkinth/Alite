package de.phbouillon.android.games.alite.screens.canvas.tutorial;

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

import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.screens.canvas.EquipmentScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
public class TutEquipment extends TutorialScreen {
	private StatusScreen status;
	private EquipmentScreen equip;
	private int screenToInitialize = 0;

	TutEquipment() {
		super(3, false);
		initLine_00();
		initLine_01();
		initLine_02();
		initLine_03();
		initLine_04();
		initLine_05();
	}

	public TutEquipment(DataInputStream dis) throws IOException {
		this();
		loadScreenState(dis);
	}

	private void initLine_00() {
		addLine(L.string(R.string.tutorial_equipment_00)).setY(700);

		status = new StatusScreen();
	}

	private void initLine_01() {
		final TutorialLine line =
				addLine(L.string(R.string.tutorial_equipment_01)).setY(700);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			Screen newScreen = updateNavBar();
			if (newScreen instanceof EquipmentScreen) {
				changeToEquipmentScreen();
				setFinished(line);
				currentLineIndex++;
			} else if (newScreen != null) {
				setFinished(line);
			}
		});
	}

	private void changeToEquipmentScreen() {
		status.dispose();
		status = null;
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_EQUIP);
		equip = new EquipmentScreen();
		equip.loadAssets();
		equip.activate();
	}

	private void initLine_02() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_equipment_02)).setY(700);

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			Screen newScreen = updateNavBar();
			if (newScreen instanceof EquipmentScreen) {
				changeToEquipmentScreen();
				setFinished(line);
			} else if (newScreen != null) {
				setFinished(line);
				currentLineIndex--;
			}
		});
	}

	private void initLine_03() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_equipment_03)).setY(700).addHighlight(makeHighlight(150, 100, 225, 225));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			equip.processAllTouches();
			if (equip.getSelectedEquipment() != null && equip.getSelectedEquipment() != EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL)) {
				setFinished(line);
			} else if (equip.getEquippedEquipment() == EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL)) {
				setFinished(line);
				currentLineIndex++;
			}
		}).setFinishHook(deltaTime -> equip.clearSelection());
	}

	private void initLine_04() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_equipment_04)).setY(700).
			addHighlight(makeHighlight(150, 100, 225, 225));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			equip.processAllTouches();
			if (equip.getSelectedEquipment() != null && equip.getSelectedEquipment() != EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL)) {
				setFinished(line);
				currentLineIndex--;
				equip.clearSelection();
			} else if (equip.getEquippedEquipment() == EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL)) {
				setFinished(line);
			}
		});
	}

	private void initLine_05() {
		addLine(L.string(R.string.tutorial_equipment_05)).setY(700).setPause(5000);
	}

	@Override
	public void activate() {
		super.activate();
		switch (screenToInitialize) {
			case 0:
				status.activate();
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
				break;
			case 1:
				changeToEquipmentScreen();
				break;
		}
		if (currentLineIndex <= 0) {
			game.getCobra().setFuel(0);
			game.getPlayer().setCash(1000);
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(currentLineIndex - 1);
		if (status != null) {
			dos.writeByte(0);
		} else if (equip != null) {
			dos.writeByte(1);
		}
		super.saveScreenState(dos);
	}

	@Override
	protected void loadScreenState(DataInputStream dis) throws IOException {
		currentLineIndex = dis.readInt();
		screenToInitialize = dis.readByte();
		super.loadScreenState(dis);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		status.loadAssets();
	}

	@Override
	public void doPresent(float deltaTime) {
		if (status != null) {
			status.present(deltaTime);
		} else if (equip != null) {
			equip.present(deltaTime);
		}

		renderText();
	}

	@Override
	public void dispose() {
		if (status != null) {
			status.dispose();
			status = null;
		}
		if (equip != null) {
			equip.dispose();
			equip = null;
		}
		super.dispose();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_EQUIPMENT_SCREEN;
	}
}
