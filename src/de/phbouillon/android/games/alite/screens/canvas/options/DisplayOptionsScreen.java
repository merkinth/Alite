package de.phbouillon.android.games.alite.screens.canvas.options;

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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class DisplayOptionsScreen extends OptionsScreen {
	private Button animations;
	private Button colorScheme;
	private Button textureLevel;
	private Button colorDepth;
	private Button engineExhaust;
	private Button lockOrientation;
	private Button targetBox;
	private Button stardustDensity;
	private Slider alpha;
	private Slider controlsAlpha;
	private Button immersion;
	private Button back;

	DisplayOptionsScreen(Alite game) {
		super(game);
		ColorScheme.loadColorSchemeList(game.getFileIO(), Settings.colorScheme);
	}

	@Override
	public void activate() {
		textureLevel    = createSmallButton(0, true, L.string(R.string.options_display_texture_level, L.string(getTextureLevelString(Settings.textureLevel))));
		stardustDensity = createSmallButton(0, false, L.string(R.string.options_display_stardust_density, L.string(getStardustDensityString(Settings.particleDensity))));
		colorDepth      = createSmallButton(1, true, L.string(R.string.options_display_color_depth, L.string(Settings.colorDepth == 1 ? R.string.options_display_color_depth_32_bit : R.string.options_display_color_depth_16_bit)));
		engineExhaust   = createSmallButton(1, false, L.string(R.string.options_display_engine_exhaust, L.string(Settings.engineExhaust ? R.string.options_on : R.string.options_off)));
		lockOrientation = createSmallButton(2, true, L.string(R.string.options_display_lock_orientation, L.string(getOrientationLockString(Settings.lockScreen))));
		targetBox       = createSmallButton(2, false, L.string(R.string.options_display_target_box, L.string(Settings.targetBox ? R.string.options_yes : R.string.options_no)));
		alpha           = createFloatSlider(3, 0, 1, L.string(R.string.options_display_alpha), Settings.alpha);
		controlsAlpha   = createFloatSlider(4, 0, 1, L.string(R.string.options_display_controls_alpha), Settings.controlAlpha);

		animations      = createSmallButton(5, true, L.string(R.string.options_display_animations, L.string(Settings.animationsEnabled ? R.string.options_on : R.string.options_off)));
		colorScheme     = createSmallButton(5, false, L.string(R.string.options_display_color_scheme, ColorScheme.getSchemeDisplayName(Settings.colorScheme)));
		immersion       = createSmallButton(6, true, L.string(R.string.options_display_immersion,
			L.string(Settings.navButtonsVisible ? R.string.options_display_immersion_off : R.string.options_display_immersion_full)));
		back            = createSmallButton(6, false, L.string(R.string.options_back));
	}

	private int getTextureLevelString(int i) {
		switch (i) {
			case 1: return R.string.options_display_texture_level_high;
			case 2: return R.string.options_display_texture_level_medium;
			case 4: return R.string.options_display_texture_level_low;
		}
		return R.string.options_display_invalid_value;
	}

	private int getStardustDensityString(int i) {
		switch (i) {
			case 0: return R.string.options_display_stardust_density_off;
			case 1: return R.string.options_display_stardust_density_low;
			case 2: return R.string.options_display_stardust_density_medium;
			case 3: return R.string.options_display_stardust_density_high;
		}
		return R.string.options_display_invalid_value;
	}

	private int getOrientationLockString(int i) {
		switch (i) {
			case 0: return R.string.options_display_lock_orientation_no_lock;
			case 1: return R.string.options_display_lock_orientation_right;
			case 2: return R.string.options_display_lock_orientation_left;
		}
		return R.string.options_display_invalid_value;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_display_options));
		textureLevel.render(g);
		stardustDensity.render(g);
		engineExhaust.render(g);
		colorDepth.render(g);
		lockOrientation.render(g);
		targetBox.render(g);
		alpha.render(g);
		controlsAlpha.render(g);
		animations.render(g);
		colorScheme.render(g);
		immersion.render(g);
		back.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (alpha.checkEvent(touch)) {
			Settings.alpha = alpha.getCurrentValue();
			Settings.save(game.getFileIO());
		}
		if (controlsAlpha.checkEvent(touch)) {
			Settings.controlAlpha = controlsAlpha.getCurrentValue();
			Settings.save(game.getFileIO());
		}
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (textureLevel.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.textureLevel <<= 1;
				if (Settings.textureLevel > 4) {
					Settings.textureLevel = 1;
				}
				textureLevel.setText(L.string(R.string.options_display_texture_level, L.string(getTextureLevelString(Settings.textureLevel))));
				Settings.save(game.getFileIO());
			} else if (stardustDensity.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.particleDensity++;
				if (Settings.particleDensity >= 4) {
					Settings.particleDensity = 0;
				}
				stardustDensity.setText(L.string(R.string.options_display_stardust_density, L.string(getStardustDensityString(Settings.particleDensity))));
				Settings.save(game.getFileIO());
			} else if (colorDepth.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.colorDepth = -Settings.colorDepth + 1;
				colorDepth.setText(L.string(R.string.options_display_color_depth, L.string(Settings.colorDepth == 1 ? R.string.options_display_color_depth_32_bit : R.string.options_display_color_depth_16_bit)));
				Settings.save(game.getFileIO());
			} else if (animations.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.animationsEnabled = !Settings.animationsEnabled;
				animations.setText(L.string(R.string.options_display_animations, L.string(Settings.animationsEnabled ? R.string.options_on : R.string.options_off)));
				Settings.save(game.getFileIO());
			} else if (colorScheme.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.colorScheme = ColorScheme.getNextColorScheme(Settings.colorScheme);
				colorScheme.setText(L.string(R.string.options_display_color_scheme, ColorScheme.getSchemeDisplayName(Settings.colorScheme)));
				String error = ColorScheme.setColorScheme(game.getFileIO(), null, Settings.colorScheme);
				if (error != null) {
					showMessageDialog(L.string(R.string.options_display_color_scheme,error));
				}
				Settings.save(game.getFileIO());
				newScreen = new DisplayOptionsScreen(game);
			} else if (lockOrientation.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.lockScreen++;
				if (Settings.lockScreen >= 3) {
					Settings.lockScreen = 0;
				}
				Settings.setOrientation(game);
				lockOrientation.setText(L.string(R.string.options_display_lock_orientation, L.string(getOrientationLockString(Settings.lockScreen))));
				Settings.save(game.getFileIO());
			} else if (targetBox.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.targetBox = !Settings.targetBox;
				targetBox.setText(L.string(R.string.options_display_target_box, L.string(Settings.targetBox ? R.string.options_yes : R.string.options_no)));
				Settings.save(game.getFileIO());
			} else if (engineExhaust.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.engineExhaust = !Settings.engineExhaust;
				engineExhaust.setText(L.string(R.string.options_display_engine_exhaust, L.string(Settings.engineExhaust ? R.string.options_on : R.string.options_off)));
				Settings.save(game.getFileIO());
			} else if (immersion.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.navButtonsVisible = !Settings.navButtonsVisible;
				immersion.setText(L.string(R.string.options_display_immersion,
					L.string(Settings.navButtonsVisible ? R.string.options_display_immersion_off : R.string.options_display_immersion_full)));
				Settings.save(game.getFileIO());
			} else if (back.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new OptionsScreen(game);
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.DISPLAY_OPTIONS_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new DisplayOptionsScreen(alite));
		return true;
	}
}
