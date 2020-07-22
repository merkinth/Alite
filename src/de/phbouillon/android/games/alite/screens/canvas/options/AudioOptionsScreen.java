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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class AudioOptionsScreen extends OptionsScreen {
	private static final float SLIDER_STEP = 0.01f;
	private final Component<?>[] controls = new Component[7];

	@Override
	public void activate() {
		controls[0] = createFloatSlider(0, 0, 1,
			L.string(R.string.options_audio_music_volume), Settings.volumes[Sound.SoundType.MUSIC.getValue()])
			.setEvent(s -> changeVolume(Sound.SoundType.MUSIC, s));
		controls[1] = createFloatSlider(1, 0, 1,
			L.string(R.string.options_audio_sound_fx_volume), Settings.volumes[Sound.SoundType.SOUND_FX.getValue()])
			.setEvent(s -> changeVolume(Sound.SoundType.COMBAT_FX, s));
		controls[2] = createFloatSlider(2, 0, 1,
			L.string(R.string.options_audio_combat_fx_volume), Settings.volumes[Sound.SoundType.COMBAT_FX.getValue()])
			.setEvent(s -> changeVolume(Sound.SoundType.COMBAT_FX, s));
		controls[3] = createFloatSlider(3, 0, 1,
			L.string(R.string.options_audio_voice_volume), Settings.volumes[Sound.SoundType.VOICE.getValue()])
			.setEvent(s -> changeVolume(Sound.SoundType.VOICE, s));
		controls[4] = createFloatSlider(4, 0, 1,
			L.string(R.string.options_audio_vibrate_level_on_damage), Settings.vibrateLevelOnDamage)
			.setEvent(s -> {
				Settings.vibrateLevelOnDamage = s.getCurrentValue(SLIDER_STEP);
				Settings.save(game.getFileIO());
				}
			);
		controls[5] = createFloatSlider(5, 0, 1,
			L.string(R.string.options_audio_vibrate_level_on_hit), Settings.vibrateLevelOnHit)
			.setEvent(s -> {
				Settings.vibrateLevelOnHit = s.getCurrentValue(SLIDER_STEP);
				Settings.save(game.getFileIO());
				}
			);
		controls[6] = createButton(6, L.string(R.string.options_back))
			.setEvent(b -> newScreen = new OptionsScreen());
	}


	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_audio_options));
		for (Component<?> c : controls) {
			c.render(g);

		}
	}

	private void changeVolume(Sound.SoundType type, Slider slider) {
		Settings.volumes[type.getValue()] = slider.getCurrentValue(SLIDER_STEP);
		Settings.save(game.getFileIO());
	}


	@Override
	protected void processTouch(TouchEvent touch) {
		for (Component<?> c : controls) {
			if (c.checkEvent(touch)) {
				c.onEvent();
				return;
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.AUDIO_OPTIONS_SCREEN;
	}

}
