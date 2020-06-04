package de.phbouillon.android.games.alite.screens.opengl.sprites.buttons;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import android.graphics.Color;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.gl.Sprite;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.canvas.QuantityPadScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;

public class AliteButtons implements Serializable {
	private static final long serialVersionUID = 3492100128009209777L;

	private static final float RETRO_ROCKET_SPEED    = 8350.0f; // m/s

	public static boolean OVERRIDE_HYPERSPACE  = false;
	public static boolean OVERRIDE_INFORMATION = false;
	public static boolean OVERRIDE_LASER       = false;
	public static boolean OVERRIDE_MISSILE     = false;
	public static boolean OVERRIDE_TORUS       = false;

	private static final int TORUS_DRIVE      =  0;
	private static final int HYPERSPACE       =  1;
	private static final int GAL_HYPERSPACE   =  2;
	private static final int STATUS           =  3;
	private static final int DOCKING_COMPUTER =  4;
	private static final int ECM              =  5;
	private static final int ESCAPE_CAPSULE   =  6;
	private static final int ENERGY_BOMB      =  7;
	private static final int RETRO_ROCKETS    =  8;
	private static final int ECM_JAMMER       =  9;
	private static final int CLOAKING_DEVICE  = 10;
	private static final int MISSILE          = 11;
	private static final int FIRE             = 12;
	private static final int TIME_DRIVE       = 13;

	private ButtonData[] buttons = new ButtonData[15];
	private Sprite overlay;
	private Sprite yellowOverlay;
	private Sprite redOverlay;
	private Sprite cycleLeft;
	private Sprite cycleRight;
	private Sprite yesButton;
	private Sprite noButton;

	private final String textureFilename;
	protected transient Alite alite;
	private final InGameManager inGame;
	private ButtonData source = null;
	private ButtonGroup[] buttonGroup;
	private boolean sweepLeftPossible = false;
	private boolean sweepRightPossible = false;
	private boolean actionPerformed = false;
	private boolean sweepLeftDown = false;
	private boolean sweepRightDown = false;
	private boolean yesTouched = false;
	private boolean noTouched = false;
	private Timer downTime;
	private transient int[] sweepLeftPos;
	private transient int[] sweepRightPos;

	private final TorusBlockingTraverser torusTraverser;
	private final EnergyBombTraverser energyBombTraverser;
	private final ECMTraverser ecmTraverser;
	private int fireButtonPressed = -1;

	public AliteButtons(InGameManager inGame) {
		alite = Alite.get();
		this.inGame = inGame;

		textureFilename = "textures/ui4.png";
		alite.getTextureManager().addTexture(textureFilename);
		reset();

		overlay       = new Sprite(0, 0, 20, 20,
			200.0f / 1024.0f, 600.0f / 1024.0f, 400.0f / 1024.0f,  800.0f / 1024.0f, textureFilename);
		yellowOverlay = new Sprite(0, 0, 20, 20,
			600.0f / 1024.0f, 0.0f, 800.0f / 1024.0f,  200.0f / 1024.0f, textureFilename);
		redOverlay    = new Sprite(0, 0, 20, 20,
			400.0f / 1024.0f, 800.0f / 1024.0f, 600.0f / 1024.0f, 1000.0f / 1024.0f, textureFilename);
		yesButton     = new Sprite(500, 250, 700, 450,
			600.0f / 1024.0f, 600.0f / 1024.0f, 800.0f / 1024.0f,  800.0f / 1024.0f, textureFilename);
		noButton      = new Sprite(1220, 250, 1420, 450,
			600.0f / 1024.0f, 800.0f / 1024.0f, 800.0f / 1024.0f, 1000.0f / 1024.0f, textureFilename);

		torusTraverser = new TorusBlockingTraverser(inGame);
		energyBombTraverser = new EnergyBombTraverser(inGame);
		ecmTraverser = new ECMTraverser(inGame);
	}

	public void reset() {
		createButtonGroups();
		createButtons();
		setSweepPos();
		cycleLeft  = new Sprite(sweepLeftPos[0], sweepLeftPos[1], sweepLeftPos[0] + 100, sweepLeftPos[1] + 100,
			800.0f / 1024.0f, 800.0f / 1024.0f, 900.0f / 1024.0f,  900.0f / 1024.0f, textureFilename);
		cycleRight = new Sprite(sweepRightPos[0], sweepRightPos[1], sweepRightPos[0] + 100, sweepRightPos[1] + 100,
			800.0f / 1024.0f, 800.0f / 1024.0f, 900.0f / 1024.0f,  900.0f / 1024.0f, textureFilename);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "AliteButtons.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "AliteButtons.readObject I");
			alite = Alite.get();
			setSweepPos();
			AliteLog.d("readObject", "AliteButtons.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	private void setSweepPos() {
		sweepLeftPos = new int[2];
		sweepRightPos = new int[2];
		sweepLeftPos[0]  = Settings.flatButtonDisplay ?  800 :  250;
		sweepLeftPos[1]  = Settings.flatButtonDisplay ?   50 :  400;
		sweepRightPos[0] = Settings.flatButtonDisplay ? 1020 : 1560;
		sweepRightPos[1] = Settings.flatButtonDisplay ?   50 :  400;
	}

	private void createButtons() {
		buttons[FIRE]             = genButtonData(3, 1, Settings.FIRE, "Fire", true);
		buttons[MISSILE]          = genButtonData(2, 3, Settings.MISSILE, "Missile", false);
		buttons[ECM]              = genButtonData(0, 3, Settings.ECM, "ECM", EquipmentStore.ECM_SYSTEM);
		buttons[RETRO_ROCKETS]    = genButtonData(1, 2, Settings.RETRO_ROCKETS, "Retro Rockets", EquipmentStore.RETRO_ROCKETS);
		buttons[ESCAPE_CAPSULE]   = genButtonData(0, 4, Settings.ESCAPE_CAPSULE, "Escape Capsule", EquipmentStore.ESCAPE_CAPSULE);
		buttons[ENERGY_BOMB]      = genButtonData(1, 1, Settings.ENERGY_BOMB, "Energy Bomb", EquipmentStore.ENERGY_BOMB);

		buttons[STATUS]           = genButtonData(2, 2, Settings.STATUS, "Status", true);
		// Torus drive, time drive and docking computer buttons are always in the same position, as they're mutually exclusive.
		buttons[TORUS_DRIVE]      = genButtonData(2, 1, Settings.TORUS, "Torus Drive", false);
		buttons[TIME_DRIVE]       = genButtonData(4, 1, Settings.TORUS, "Time Drive", false);
		buttons[DOCKING_COMPUTER] = genButtonData(0, 0, Settings.TORUS, "Docking Computer", false);
		buttons[HYPERSPACE]       = genButtonData(0, 1, Settings.HYPERSPACE, "Hyperspace", alite.isHyperspaceTargetValid());

		buttons[GAL_HYPERSPACE]   = genButtonData(0, 2, Settings.GALACTIC_HYPERSPACE, "Galactic Hyperspace", EquipmentStore.GALACTIC_HYPERDRIVE);
		buttons[CLOAKING_DEVICE]  = genButtonData(3, 2, Settings.CLOAKING_DEVICE, "Cloaking Device", EquipmentStore.CLOAKING_DEVICE);
		buttons[ECM_JAMMER]       = genButtonData(1, 0, Settings.ECM_JAMMER, "ECM Jammer", EquipmentStore.ECM_JAMMER);
	}

	private void createButtonGroups() {
		buttonGroup = new ButtonGroup[4];

		buttonGroup[0] = new ButtonGroup(0, true, true);
		buttonGroup[1] = new ButtonGroup(1, true, false);
		buttonGroup[2] = new ButtonGroup(2, false, true);
		buttonGroup[3] = new ButtonGroup(3, false, false);
	}

	private ButtonData genButtonData(float x, float y, int positionIndex, String name, String equipId) {
		return genButtonData(x, y, positionIndex, name, alite.getPlayer().getCobra().isEquipmentInstalled(
			EquipmentStore.get().getEquipmentById(equipId)));
	}

	private ButtonData genButtonData(float x, float y, int positionIndex, String name, boolean active) {
		positionIndex = Settings.buttonPosition[positionIndex];
		// Left Side: (0, 0), (1, 1), (0, 2)
		// Right Side: (11.4, 0), (10.4, 1), (11.4, 2)
		float xt;
		int groupIndex;
		if (positionIndex < 6) {
			// Left Side
			xt = Settings.flatButtonDisplay ? (positionIndex % 3) * 1.5f : // 0 1.5 3
				positionIndex % 3 % 2 == 0 ? 0.0f : 1.0f;
			groupIndex = positionIndex < 3 ? 0 : 1;
		} else {
			// Right Side
			xt = Settings.flatButtonDisplay ? 8.4f + (positionIndex % 3) * 1.5f : // 8.4 9.9 11.4
				positionIndex % 3 % 2 == 0 ? 11.4f : 10.4f;
			groupIndex = positionIndex < 9 ? 2 : 3;
		}
		float yt = Settings.flatButtonDisplay ? 0 : positionIndex % 3;

		ButtonData result = new ButtonData(new Sprite(xt * 150.0f, yt * 150.0f, xt * 150.0f + 200.0f,
			yt * 150.0f + 200.0f, x * 200.0f / 1024.0f, y * 200.0f / 1024.0f,
			(x + 1.0f) * 200.0f / 1024.0f, (y + 1.0f) * 200.0f / 1024.0f, textureFilename),
			xt * 150.0f + 100.0f, yt * 150.0f + 100.0f, name);
		result.active = active;
		buttonGroup[groupIndex].addButton(result);
		return result;
	}

	private void check(int button, String equipId, boolean yellow) {
		if (buttons[button] != null) {
			buttons[button].active = alite.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(equipId));
			buttons[button].yellow = yellow;
		}
	}

	private void updateFireButton() {
		if (buttons[FIRE] == null) {
			return;
		}
		buttons[FIRE].active = alite.getCobra().getLaser(inGame.getViewDirection()) != null;
		buttons[FIRE].yellow = alite.getLaserManager() != null && alite.getLaserManager().isAutoFire();
		if (!Settings.laserButtonAutoFire && buttons[FIRE].yellow) {
			// Prevent a green highlight if single shot finger-down...
			buttons[FIRE].selected = false;
		}
	}

	private void updateMissileButton() {
		if (buttons[MISSILE] == null) {
			return;
		}
		if (downTime != null && downTime.hasPassedSeconds(1.5f) && buttons[MISSILE].red) {
			inGame.handleMissileIcons();
			downTime = null;
			buttons[MISSILE].red = false;
			SoundManager.play(Assets.click);
			source = null;
			buttons[MISSILE].selected = false;
		}
		SpaceObject missileLock = inGame.getMissileLock();
		if (missileLock != null && (missileLock.getHullStrength() < 0 || missileLock.mustBeRemoved())) {
			inGame.handleMissileIcons();
			downTime = null;
			buttons[MISSILE].red = false;
			buttons[MISSILE].selected = false;
			inGame.setMessage(L.string(R.string.msg_target_lost));
		}
		buttons[MISSILE].active = alite.getCobra().getMissiles() > 0;
		buttons[MISSILE].yellow = alite.getCobra().isMissileTargetting();
		buttons[MISSILE].red = alite.getCobra().isMissileLocked();
	}

	private void updateDockingComputerButton() {
		if (buttons[DOCKING_COMPUTER] == null) {
			return;
		}
		if (alite.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER)) && InGameManager.playerInSafeZone) {
			buttons[DOCKING_COMPUTER].active = true;
			buttons[DOCKING_COMPUTER].yellow = inGame.isDockingComputerActive();
		} else {
			buttons[DOCKING_COMPUTER].active = false;
			if (inGame.isDockingComputerActive()) {
				toggleDockingComputer();
				buttons[DOCKING_COMPUTER].yellow = false;
			}
		}
	}

	private void updateTorusDriveButton() {
		if (buttons[TORUS_DRIVE] == null) {
			return;
		}
		if ((buttons[DOCKING_COMPUTER] == null || !buttons[DOCKING_COMPUTER].active) &&
				inGame.getShip().getSpeed() <= -PlayerCobra.MAX_SPEED &&
				!inGame.isInExtendedSafeZone() && !inGame.isWitchSpace() &&
				alite.getCobra().getCabinTemperature() == 0 &&
				!inGame.traverseObjects(torusTraverser)) {
			buttons[TORUS_DRIVE].active = true;
			if (inGame.isTorusDriveEngaged()) {
				buttons[TORUS_DRIVE].yellow = true;
			} else if (isTimeDriveEngaged()) {
				// time drive currently engaged, auto change to torus drive
				alite.setTimeFactor(1);
				toggleTorusDrive();
				buttons[TORUS_DRIVE].yellow = true;
			} else {
				buttons[TORUS_DRIVE].yellow = false;
			}
		} else {
			buttons[TORUS_DRIVE].active = false;
			if (inGame.isTorusDriveEngaged()) {
				toggleTorusDrive();
			}
		}
	}

	private boolean isTimeDriveEngaged() {
		return alite.getTimeFactor() > 1;
	}

	private void updateTimeDriveButton() {
		if (buttons[TIME_DRIVE] == null) {
			return;
		}
		if ((buttons[DOCKING_COMPUTER] == null || !buttons[DOCKING_COMPUTER].active) &&
				(buttons[TORUS_DRIVE] == null || !buttons[TORUS_DRIVE].active) &&
				alite.getPlayer().getCondition() != Condition.RED && !InGameManager.playerInSafeZone) {
			buttons[TIME_DRIVE].active = true;
			buttons[TIME_DRIVE].yellow = isTimeDriveEngaged();
		} else {
			if (inGame.isDockingComputerActive()) {
				return;
			}
			buttons[TIME_DRIVE].active = false;
			if (isTimeDriveEngaged()) {
				alite.setTimeFactor(1);
			}
		}
	}

	private void updateHyperspaceButton() {
		if (buttons[HYPERSPACE] == null) {
			return;
		}
		buttons[HYPERSPACE].active = alite.isHyperspaceTargetValid() && !inGame.isHyperdriveMalfunction();
		buttons[HYPERSPACE].yellow = inGame.isHyperspaceEngaged(false);
	}

	private void updateGalHyperspaceButton() {
		if (buttons[GAL_HYPERSPACE] == null) {
			return;
		}
		buttons[GAL_HYPERSPACE].active = alite.getCobra().isEquipmentInstalled(
			EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE)) && !inGame.isHyperdriveMalfunction();
		buttons[GAL_HYPERSPACE].yellow = inGame.isHyperspaceEngaged(true);
	}

	private void updateRetroRocketsButton() {
		if (buttons[RETRO_ROCKETS] == null) {
			return;
		}
		buttons[RETRO_ROCKETS].active = alite.getCobra().isEquipmentInstalled(
			EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS)) && inGame.getShip().getSpeed() <= 0.0f;
	}

	private void updateSweep() {
		int countLeft = 0;
		int countRight = 0;
		boolean needsSweepLeft = false;
		boolean needsSweepRight = false;
		for (ButtonGroup bg: buttonGroup) {
			if (bg.hasActiveButtons()) {
				if (bg.left) {
					countLeft++;
				} else {
					countRight++;
				}
			} else {
				if (bg.active) {
					if (bg.left) {
						needsSweepLeft = true;
					} else {
						needsSweepRight = true;
					}
				}
			}
		}
		sweepLeftPossible = countLeft > 1;
		sweepRightPossible = countRight > 1;
		if (countLeft >= 1 && needsSweepLeft) {
			sweep(true);
		}
		if (countRight >= 1 && needsSweepRight) {
			sweep(false);
		}
	}

	private void update() {
		updateFireButton();
		updateMissileButton();
		updateDockingComputerButton();
		updateTorusDriveButton();
		updateTimeDriveButton();
		updateHyperspaceButton();
		updateGalHyperspaceButton();

		check(CLOAKING_DEVICE, EquipmentStore.CLOAKING_DEVICE, inGame.getShip().isCloaked());
		check(ECM, EquipmentStore.ECM_SYSTEM, false);
		check(ECM_JAMMER, EquipmentStore.ECM_JAMMER, inGame.getShip().isEcmJammer());
		check(ESCAPE_CAPSULE, EquipmentStore.ESCAPE_CAPSULE, false);
		check(ENERGY_BOMB, EquipmentStore.ENERGY_BOMB, false);

		updateRetroRocketsButton();

		updateSweep();
	}

	public void render() {
		update();
		alite.getGraphics().setColor(AliteColor.argb(Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
		for (ButtonGroup bg: buttonGroup) {
			if (!bg.active) {
				continue;
			}
			for (ButtonData bd: bg.buttons) {
				if (bd == null || !bd.active) {
					continue;
				}
				bd.sprite.render();
				if (bd.selected) {
					overlay.setPosition(bd.sprite.getPosition());
					overlay.render();
				}
				if (bd.yellow) {
					yellowOverlay.setPosition(bd.sprite.getPosition());
					yellowOverlay.render();
				}
				if (bd.red) {
					redOverlay.setPosition(bd.sprite.getPosition());
					redOverlay.render();
				}
			}
		}
		if (sweepLeftPossible) {
			cycleLeft.render();
		}
		if (sweepRightPossible) {
			cycleRight.render();
		}
		alite.getGraphics().setColor(Color.WHITE);
	}

	public void renderYesNoButtons() {
		alite.getGraphics().setColor(AliteColor.argb(Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
		yesButton.render();
		noButton.render();
		if (yesTouched) {
			overlay.setPosition(yesButton.getPosition());
			overlay.render();
		}
		if (noTouched) {
			overlay.setPosition(noButton.getPosition());
			overlay.render();
		}
		alite.getGraphics().setColor(Color.WHITE);
	}

	private void sweep(boolean left) {
		int activeIndex = 0;
		int first = left ? 0 : -1;
		for (int i = 0; i < buttonGroup.length; i++) {
			if (buttonGroup[i].left == left) {
				if (first == -1) {
					first = i;
				}
				if (buttonGroup[i].active) {
					activeIndex = i;
					buttonGroup[i].active = false;
				}
			}
		}
		int iterations = 0;
		do {
			activeIndex++;
			if (activeIndex >= buttonGroup.length) {
				activeIndex = 0;
			}
			iterations++;
		} while (iterations <= buttonGroup.length && (buttonGroup[activeIndex].left != left || !buttonGroup[activeIndex].hasActiveButtons()));
		if (iterations <= buttonGroup.length) {
			buttonGroup[activeIndex].active = true;
		} else {
			buttonGroup[first].active = true;
		}
	}

	public void checkFireRelease(TouchEvent e) {
		if (e.pointer == fireButtonPressed && e.type == TouchEvent.TOUCH_UP && !Settings.laserButtonAutoFire) {
			fireButtonPressed = -1;
			deactivateFire();
		}
	}

	public boolean handleTouch(TouchEvent e) {
		boolean result = false;
		if (e.type == TouchEvent.TOUCH_DOWN) {
			yesTouched = (e.x - 600) * (e.x - 600) + (e.y - 350) * (e.y - 350) <= 10000.0f;
			noTouched  = (e.x - 1320) * (e.x - 1320) + (e.y - 350) * (e.y - 350) <= 10000.0f;
		} else if (e.type == TouchEvent.TOUCH_UP) {
			yesTouched = false;
			noTouched = false;
			if ((e.x - 600) * (e.x - 600) + (e.y - 350) * (e.y - 350) <= 10000.0f) {
				inGame.yesSelected();
			}
			if ((e.x - 1320) * (e.x - 1320) + (e.y - 350) * (e.y - 350) <= 10000.0f) {
				inGame.noSelected();
			}
		}
		for (int i = 0; i < buttons.length; i++) {
			ButtonData bd = buttons[i];
			if (bd == null || !bd.active || !bd.parent.active || !bd.isTouched(e.x, e.y, e.type)) {
				continue;
			}
			if (e.type == TouchEvent.TOUCH_DOWN) {
				actionPerformed = false;
				source = bd;
				if (source == buttons[MISSILE] && buttons[MISSILE].red) {
					downTime = new Timer();
				}
				if (source == buttons[FIRE] && !Settings.laserButtonAutoFire && !OVERRIDE_LASER) {
					fireButtonPressed = e.pointer;
					alite.getLaserManager().setAutoFire(true);
				}
			} else if (e.type == TouchEvent.TOUCH_UP) {
				bd.selected = false;
				if (source != null) {
					source.selected = false;
				}
				if (source != bd) {
					source = null;
					return true;
				}
				source = null;
				SoundManager.play(Assets.click);
				switch (i) {
					case TORUS_DRIVE:      toggleTorusDrive();         break;
					case TIME_DRIVE:       toggleTimeDrive();          break;
					case HYPERSPACE:       engageHyperspace();         break;
					case GAL_HYPERSPACE:   engageGalacticHyperspace(); break;
					case ECM:              engageECM();                break;
					case ESCAPE_CAPSULE:   engageEscapeCapsule();      break;
					case ECM_JAMMER:       toggleECMJammer();          break;
					case DOCKING_COMPUTER: toggleDockingComputer();    break;
					case ENERGY_BOMB:      engageEnergyBomb();         break;
					case RETRO_ROCKETS:    engageRetroRockets();       break;
					case STATUS:           goToStatusView();           break;
					case FIRE:             toggleAutoFire();           break;
					case MISSILE:          updateMissileState();       break;
					case CLOAKING_DEVICE:  toggleCloakingDevice();     break;
				}
			}
			result = true;
		}
		if (e.type == TouchEvent.TOUCH_DOWN) {
			if (sweepLeftPossible && e.x >= sweepLeftPos[0] && e.x <= sweepLeftPos[0] + 100 && e.y >= sweepLeftPos[1] && e.y <= sweepLeftPos[1] + 100) {
				sweepLeftDown = true;
			}
			if (sweepRightPossible && e.x >= sweepRightPos[0] && e.x <= sweepRightPos[0] + 100 && e.y >= sweepRightPos[1] && e.y <= sweepRightPos[1] + 100) {
				sweepRightDown = true;
			}
		}
		if (e.type == TouchEvent.TOUCH_UP) {
			downTime = null;
			if (source != null) {
				source.selected = false;
				source = null;
			}
			if (sweepLeftDown && e.x >= sweepLeftPos[0] && e.x <= sweepLeftPos[0] + 100 && e.y >= sweepLeftPos[1] && e.y <= sweepLeftPos[1] + 100) {
				sweep(true);
				actionPerformed = true;
			}
			if (sweepRightDown && e.x >= sweepRightPos[0] && e.x <= sweepRightPos[0] + 100 && e.y >= sweepRightPos[1] && e.y <= sweepRightPos[1] + 100) {
				sweep(false);
				actionPerformed = true;
			}
			sweepLeftDown = false;
			sweepRightDown = false;
			source = null;
			result |= actionPerformed;
			actionPerformed = false;
		}
		return result;
	}

	private void deactivateFire() {
		if (alite.getLaserManager() != null) {
			alite.getLaserManager().setAutoFire(false);
		}
	}

	private void toggleAutoFire() {
		if (OVERRIDE_LASER || isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		if (alite.getLaserManager() != null) {
			alite.getLaserManager().setAutoFire(Settings.laserButtonAutoFire && !alite.getLaserManager().isAutoFire());
		}
	}

	private void toggleCloakingDevice() {
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		inGame.toggleCloaked();
		buttons[CLOAKING_DEVICE].yellow = inGame.getShip().isCloaked();
	}

	private void updateMissileState() {
		if (OVERRIDE_MISSILE) {
			return;
		}
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		ButtonData missile = buttons[MISSILE];
		if (missile == null) {
			return;
		}

		if (missile.red) {
			missile.red = false;
			inGame.fireMissile();
			return;
		}
		missile.yellow = !missile.yellow;
		inGame.handleMissileIcons();
	}

	private void toggleECMJammer() {
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		inGame.toggleECMJammer();
		buttons[ECM_JAMMER].yellow = inGame.getShip().isEcmJammer();
	}

	private void engageEscapeCapsule() {
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		if (inGame.isWitchSpace()) {
			SoundManager.play(Assets.com_escapeMalfunction);
			inGame.setMessage(L.string(R.string.com_escape_malfunction));
			return;
		}
		alite.getCobra().removeEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE));
		alite.getCobra().clearInventory();
		SoundManager.stop(Assets.energyLow);
		SoundManager.stop(Assets.criticalCondition);
		SoundManager.play(Assets.retroRocketsOrEscapeCapsuleFired);
		inGame.clearMessageRepetition();
		inGame.setPlayerControl(false);
		if (alite.getCurrentScreen() instanceof FlightScreen) {
			((FlightScreen) alite.getCurrentScreen()).setInformationScreen(null);
		}
		inGame.forceForwardView();
		inGame.killHud();
		inGame.getShip().setUpdater(new EscapeCapsuleUpdater(inGame));
		alite.getPlayer().setCondition(Condition.DOCKED);
		// The police track record is identified by the ship's id,
		// so leaving it with an escape capsule can be abused to get a
		// fresh start.
		alite.getPlayer().setLegalValue(0);
	}

	private void engageECM() {
		ecmTraverser.reset();
		inGame.traverseObjects(ecmTraverser);
		inGame.reduceShipEnergy(1);
	}

	private void engageGalacticHyperspace() {
		if (alite.getCurrentScreen() instanceof FlightScreen &&
				!inGame.isHyperspaceEngaged(true) &&
				Settings.maxGalaxies == GalaxyGenerator.EXTENDED_GALAXY_COUNT &&
				alite.getPlayer().getCurrentSystem() == SystemData.RAXXLA_SYSTEM) {
			deactivateFire();
			new QuantityPadScreen(alite.getCurrentScreen(), GalaxyGenerator.EXTENDED_GALAXY_COUNT, "",
				215, 200, -1).changeFromInFlightScreen();
			return;
		}
		engageGalacticHyperspace(0);
	}

	public void engageGalacticHyperspace(int galacticNumber) {
		buttons[GAL_HYPERSPACE].yellow = inGame.toggleHyperspaceCountdown(galacticNumber == 0 ?
			alite.getGenerator().getCurrentGalaxy() + 1 : galacticNumber);
		buttons[HYPERSPACE].yellow = false;
	}

	private void goToStatusView() {
		if (OVERRIDE_INFORMATION) {
			return;
		}
		if (alite.getCurrentScreen() instanceof FlightScreen) {
			deactivateFire();
			alite.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
			new StatusScreen().changeFromInFlightScreen();
		}
	}

	private void engageHyperspace() {
		if (OVERRIDE_HYPERSPACE) {
			return;
		}
		buttons[HYPERSPACE].yellow = inGame.toggleHyperspaceCountdown(0);
		buttons[GAL_HYPERSPACE].yellow = false;
	}

	private void toggleTorusDrive() {
		if (OVERRIDE_TORUS) {
			return;
		}

		if (inGame.isTorusDriveEngaged()) {
			inGame.getSpawnManager().leaveTorus();
		} else {
			unarm();
			inGame.getSpawnManager().enterTorus();
			inGame.getShip().setSpeed(-PlayerCobra.TORUS_SPEED);
			inGame.setPlayerControl(false);
		}
	}

	private void unarm() {
		deactivateFire();
		unlockMissile();
		if (inGame.getShip().isCloaked()) toggleCloakingDevice();
		if (inGame.getShip().isEcmJammer()) toggleECMJammer();
	}

	private void unlockMissile() {
		if (buttons[MISSILE] == null || !buttons[MISSILE].red && !buttons[MISSILE].yellow) {
			return;
		}
		inGame.handleMissileIcons();
	}

	private void toggleTimeDrive() {
		if (OVERRIDE_TORUS) {
			return;
		}
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			alite.setTimeFactor(1);
		} else {
			unarm();
			alite.setTimeFactor(PlayerCobra.SPEED_UP_FACTOR);
		}
	}

	private void toggleDockingComputer() {
		if (!inGame.isDockingComputerActive()) {
			unarm();
		}
		if (!alite.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER))) {
			inGame.toggleStationHandsDocking();
		} else {
			inGame.toggleDockingComputer(true);
		}
	}

	private void engageEnergyBomb() {
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		alite.getCobra().removeEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB));
		inGame.traverseObjects(energyBombTraverser);
	}

	private void engageRetroRockets() {
		if (isTimeDriveEngaged() || inGame.isTorusDriveEngaged()) {
			return;
		}
		alite.getCobra().setRetroRocketsUseCount(alite.getCobra().getRetroRocketsUseCount() - 1);
		SoundManager.play(Assets.retroRocketsOrEscapeCapsuleFired);
		inGame.getShip().setSpeed(RETRO_ROCKET_SPEED);
	}

}