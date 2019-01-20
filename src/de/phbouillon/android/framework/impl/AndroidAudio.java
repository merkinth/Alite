package de.phbouillon.android.framework.impl;

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

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import de.phbouillon.android.framework.Audio;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.Music;
import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;

public class AndroidAudio implements Audio {
	private static final int MAXIMUM_NUMBER_OF_CONCURRENT_SAMPLES = 20;

	private final SoundPool soundPool;
	private final FileIO fileIO;

	AndroidAudio(Activity activity, FileIO fileIO) {
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		this.fileIO = fileIO;
		soundPool = new SoundPool(MAXIMUM_NUMBER_OF_CONCURRENT_SAMPLES, AudioManager.STREAM_MUSIC, 0);
	}

	@Override
	public Music newMusic(String fileName) {
		try {
			return new AndroidMusic(fileIO.getPrivatePath(fileName), Sound.SoundType.MUSIC, fileName);
		} catch (IOException ignored) {
			AliteLog.e("Cannot load music", "Music " + fileName + " not found.");
			return null;
		}
	}

	@Override
	public Sound newSound(String fileName) {
		return newSound(fileName, Sound.SoundType.SOUND_FX);
	}

	@Override
	public Sound newCombatSound(String fileName) {
		return newSound(fileName, Sound.SoundType.COMBAT_FX);
	}

	private Sound newSound(String fileName, Sound.SoundType soundType) {
		try {
			AliteLog.d("Loading Sound", "Loading sound " + fileName);
			int soundId = soundPool.load((String) fileIO.getPrivatePath(fileName), 0);
			return new AndroidSound(soundPool, soundId, soundType);
		} catch (IOException ignored) {
			AliteLog.e("Cannot load sound", "Sound " + fileName + " not found.");
			return null;
		}
	}

	@Override
	public Sound newSoundAsset(String fileName) {
		try {
			AliteLog.d("Loading Sound Asset", "Loading sound asset " + fileName);
			int soundId = soundPool.load(L.rawDescriptor(fileName), 0);
			return new AndroidSound(soundPool, soundId, Sound.SoundType.VOICE);
		} catch (IOException ignored) {
			AliteLog.e("Cannot load sound asset", "Sound asset " + fileName + " not found.");
			return null;
		}
	}
}
