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

import java.io.FileInputStream;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import de.phbouillon.android.framework.Music;
import de.phbouillon.android.framework.Sound;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Settings;

public class AndroidMusic implements Music, OnCompletionListener {
	private final MediaPlayer mediaPlayer;
	private boolean isPrepared = false;
	private final Sound.SoundType soundType;
	
	public AndroidMusic(AndroidFileIO afi, String path, Sound.SoundType soundType) throws IOException {
		mediaPlayer = new MediaPlayer();
		FileInputStream fis = null;
		Object musicObject;
		musicObject = afi.getPrivatePath(path);
		try {
			AliteLog.d("Loading Music", "Loading Music " + path + ", Type: " + soundType);
			fis = new FileInputStream((String) musicObject);
			mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
			this.soundType = soundType;
		} catch (Exception e) {
			AliteLog.e("Loading Music caused an Error", e.getMessage(), e);
			throw new RuntimeException("Couldn't load music.");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					AliteLog.e("Error when closing Music File Input Stream", e.getMessage(), e);
				}
			}
		}
	}
	
	public AndroidMusic(AssetFileDescriptor afd, Sound.SoundType soundType) throws IOException {
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
			this.soundType = soundType;
		} catch (Exception e) {
			AliteLog.e("Loading Music caused an Error", e.getMessage(), e);
			throw new RuntimeException("Couldn't load music.");
		} 
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		synchronized (this) {
			isPrepared = false;
		}
	}

	@Override
	public void play() {
		if (mediaPlayer.isPlaying()) {
			return;
		}
		setVolume(Settings.volumes[soundType.getValue()]);
		try {
			synchronized(this) {
				if (!isPrepared) {
					try {
						mediaPlayer.prepare();
					} catch (IllegalStateException e) {
						AliteLog.w("Music Playback", "Suppressed ISE in play.");
					}
					isPrepared = true;
				}
				mediaPlayer.start();
			}
		} catch (IllegalStateException e) {
			AliteLog.e("Music Playback", "IllegalStateException occurred when trying to play back music.", e);
		} catch (IOException e) {
			AliteLog.e("Music Playback", "IOException occurred when trying to play back music.", e);
		}
	}

	@Override
	public void stop() {
		mediaPlayer.stop();
		synchronized (this) {
			isPrepared = false;
		}
	}

	@Override
	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	@Override
	public void setLooping(boolean looping) {
		mediaPlayer.setLooping(looping);
	}

	@Override
	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	@Override
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	@Override
	public boolean isStopped() {
		return !isPrepared;
	}

	@Override
	public boolean isLooping() {
		return mediaPlayer.isLooping();
	}

	@Override
	public void dispose() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		mediaPlayer.release();
	}
}
