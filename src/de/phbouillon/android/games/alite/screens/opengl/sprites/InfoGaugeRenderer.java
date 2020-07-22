package de.phbouillon.android.games.alite.screens.opengl.sprites;

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

import java.io.Serializable;

import android.graphics.Color;
import de.phbouillon.android.framework.IntFunction;
import de.phbouillon.android.framework.impl.gl.Sprite;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.PlayerCobra;

public final class InfoGaugeRenderer implements Serializable {
	private static final long serialVersionUID = -6680085992043102347L;

	private static final float GAUGE_LENGTH = 350.0f;

	private final Sprite gaugeOverlay;
	private final Sprite gaugeContent;
	private final Sprite missile;
	private final Sprite emptySlot;
	private final Sprite filledSlot;
	private final Sprite targettingSlot;
	private final Sprite lockedSlot;
	private final Sprite[] uiTexts = new Sprite[13];
	private float pitchPos = 175.0f;
	private float rollPos = 175.0f;
	private final IntFunction<Float> getSpeed;

	InfoGaugeRenderer(final AliteHud hud) {
		gaugeOverlay   = hud.genSprite("gauge_overlay", 150, 700);
		gaugeContent   = hud.genSprite("gauge_content", 150, 700);
		missile        = hud.genSprite("missile", 60, 994);
		emptySlot      = hud.genSprite("missile_empty", 0, 0);
		filledSlot     = hud.genSprite("missile_loaded", 0, 0);
		targettingSlot = hud.genSprite("missile_targetting", 0, 0);
		lockedSlot     = hud.genSprite("missile_targeted", 0, 0);
		int sy = 700;
		String[] uiStrings = new String[]{"fs", "as", "fu", "ct", "lt", "al", "sp", "rl", "dc", "1", "2", "3", "4"};
		for (int i = 0; i < 13; i++) {
			int sx = i < 6 ? 80 : 1792;
			uiTexts[i] = hud.genSprite(uiStrings[i], sx, sy);
			sy += 40;
			if (i == 1 || i == 8) {
				sy += 25;
			}
			if (i == 5) {
				sy = 700;
			}
		}
		getSpeed = hud.getSpeedFunction();
	}

	private float extractFrontShield() {
		float shieldValue = Alite.get().getCobra().getFrontShield();
		if (shieldValue > PlayerCobra.MAX_SHIELD) {
			shieldValue = PlayerCobra.MAX_SHIELD;
		}
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_FRONT_SHIELD, shieldValue / PlayerCobra.MAX_SHIELD), Settings.alpha);
	    return shieldValue / PlayerCobra.MAX_SHIELD * GAUGE_LENGTH;
	}

	private float extractRearShield() {
		float shieldValue = Alite.get().getCobra().getRearShield();
		if (shieldValue > PlayerCobra.MAX_SHIELD) {
			shieldValue = PlayerCobra.MAX_SHIELD;
		}
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_AFT_SHIELD, shieldValue / PlayerCobra.MAX_SHIELD), Settings.alpha);
	    return shieldValue / PlayerCobra.MAX_SHIELD * GAUGE_LENGTH;
	}

	private float extractFuel() {
		float fuel = (float) Alite.get().getCobra().getFuel() / Alite.get().getCobra().getMaxFuel();
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_FUEL, fuel), Settings.alpha);
	    return fuel * GAUGE_LENGTH;
	}

	private float extractCabinTemperature() {
		float cabinTemperature = Alite.get().getCobra().getCabinTemperature();
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_CABIN_TEMPERATURE, cabinTemperature / PlayerCobra.MAX_CABIN_TEMPERATURE), Settings.alpha);
		return cabinTemperature / PlayerCobra.MAX_CABIN_TEMPERATURE * GAUGE_LENGTH;
	}

	private float extractLaserTemperature() {
		float laserTemperature = Alite.get().getCobra().getLaserTemperature();
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_LASER_TEMPERATURE, laserTemperature / PlayerCobra.MAX_LASER_TEMPERATURE), Settings.alpha);
	    return laserTemperature / PlayerCobra.MAX_LASER_TEMPERATURE * GAUGE_LENGTH;
	}

	private float extractAltitude() {
		float altitude = Alite.get().getCobra().getAltitude();
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_ALTITUDE, altitude / PlayerCobra.MAX_ALTITUDE), Settings.alpha);
	    return altitude / PlayerCobra.MAX_ALTITUDE * GAUGE_LENGTH;
	}

	private float extractSpeed() {
		float speed = getSpeed.apply(0);
		if (-speed > PlayerCobra.MAX_SPEED) {
			speed = -PlayerCobra.MAX_SPEED;
		}
		if (speed > 0) { // Retro rockets fired
			speed = -PlayerCobra.MAX_SPEED;
		}
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_SPEED, -speed / PlayerCobra.MAX_SPEED), Settings.alpha);
	    return -speed / PlayerCobra.MAX_SPEED * GAUGE_LENGTH;
	}

	private float extractEnergyBank(int bank) {
		float energyValue = Alite.get().getCobra().getEnergy(bank);
		Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_WHOLE) == Color.TRANSPARENT ?
			ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_X + bank, energyValue / PlayerCobra.MAX_ENERGY_BANK) :
			ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_WHOLE, Alite.get().getCobra().getEnergy() / 4.0f /
				PlayerCobra.MAX_ENERGY_BANK), Settings.alpha);
		return energyValue / PlayerCobra.MAX_ENERGY_BANK * GAUGE_LENGTH;
	}

	private float computeDataValueAndSetColor(int index, float alpha) {
		switch (index) {
			case  0: return extractFrontShield();
			case  1: return extractRearShield();
			case  2: return extractFuel();
			case  3: return extractCabinTemperature();
			case  4: return extractLaserTemperature();
			case  5: return extractAltitude();
			case  6: return extractSpeed();
			case  7: // Roll dummy
			case  8: Alite.get().getGraphics().setColor(ColorScheme.get(ColorScheme.COLOR_INDICATOR_BAR, alpha), Settings.alpha);
					 return 0; // Pitch dummy
			case  9: return extractEnergyBank(0);
			case 10: return extractEnergyBank(1);
			case 11: return extractEnergyBank(2);
			case 12: return extractEnergyBank(3);
		}
		return GAUGE_LENGTH;
	}

	private void renderPitch(float sy) {
		float pitchValue = (Alite.get().getCobra().getPitch() + 2.0f) / 4.0f * 350.0f;
		if (pitchValue > 346.0f) {
			pitchValue = 346.0f;
		}
		if (pitchPos < pitchValue) {
			pitchPos += (pitchValue - pitchPos) / 3.0f;
		} else if (pitchPos > pitchValue) {
			pitchPos -= (pitchPos - pitchValue) / 3.0f;
		}
		gaugeContent.setPosition(1420 + pitchPos, sy, 1420 + pitchPos + 4.0f, sy + 36);
	}

	private void renderRoll(float sy) {
		float rollValue = (-Alite.get().getCobra().getRoll() + 2.0f) / 4.0f * 350.0f;
		if (rollValue > 346.0f) {
			rollValue = 346.0f;
		}
		if (rollPos < rollValue) {
			rollPos += (rollValue - rollPos) / 3.0f;
		} else if (rollPos > rollValue) {
			rollPos -= (rollPos - rollValue) / 3.0f;
		}
		gaugeContent.setPosition(1420 + rollPos, sy, 1420 + rollPos + 4.0f, sy + 36);
	}

	private void renderMissiles() {
		int installedMissiles = Alite.get().getCobra().getMissiles();
		Alite.get().getGraphics().setColor(AliteColor.argb(0.2f * Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
		missile.justRender();
		for (int i = 0; i < 4; i++) {
			if (i < installedMissiles) {
				if (i == installedMissiles - 1 && Alite.get().getCobra().isMissileLocked()) {
					lockedSlot.setPosition(165 + i * 80, 990, 165 + i * 80 + 80, 1027);
					lockedSlot.justRender();
				} else if (i == installedMissiles - 1 && Alite.get().getCobra().isMissileTargetting()) {
					targettingSlot.setPosition(165 + i * 80, 990, 165 + i * 80 + 80, 1027);
					targettingSlot.justRender();
				} else {
					filledSlot.setPosition(165 + i * 80, 990, 165 + i * 80 + 80, 1027);
					filledSlot.justRender();
				}
			} else {
				emptySlot.setPosition(165 + i * 80, 990, 165 + i * 80 + 80, 1027);
				emptySlot.justRender();
			}
		}
	}

	public void render() {
		int sy = 700;
		float dataValue;

		for (int i = 0, n = uiTexts.length; i < n; i++) {
			Sprite s = uiTexts[i];
			s.justRender();

			gaugeOverlay.setPosition(i < 6 ? 150 : 1420, sy, (i < 6 ? 150 : 1420) + 350, sy + 36);
			Alite.get().getGraphics().setColor(AliteColor.argb(0.2f * Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
			gaugeOverlay.justRender();

			dataValue = computeDataValueAndSetColor(i, Settings.alpha);
			if (i == 7) {
				renderRoll(sy);
			} else if (i == 8) {
				renderPitch(sy);
			} else {
				if (dataValue > 0) {
					dataValue++;
				}
				if (i >= 6) {
					dataValue++;
				}
				gaugeContent.setPosition(i < 6 ? 149 : 1419, sy, (i < 6 ? 149 : 1419) + dataValue, sy + 36);
			}
			gaugeContent.justRender();

			Alite.get().getGraphics().setColor(AliteColor.argb(Settings.alpha, Settings.alpha, Settings.alpha, Settings.alpha));
			sy += 40;
			if (i == 1 || i == 8) {
				sy += 25;
			}
			if (i == 5) {
				sy = 700;
			}
		}
		renderMissiles();
	}
}
