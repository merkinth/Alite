package de.phbouillon.android.games.alite;

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

import java.io.*;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.VideoView;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.impl.AndroidFileIO;
import de.phbouillon.android.games.alite.io.ObbExpansionsManager;

public class AliteIntro extends Activity implements OnClickListener {
	private static final String DIRECTORY_INTRO = "intro" + File.separator;
	private static final String INTRO_FILE_NAME = "alite_intro";
	private static final String INTRO_SUBTITLE_FILE_NAME = INTRO_FILE_NAME + ".vtt";

	private MediaPlayer mediaPlayer;
	private boolean isInPlayableState;
	private boolean needsToPlay;
	private boolean isResumed;
	private int stopPosition;
	private boolean aliteStarted;
	private int cyclingThroughVideoQualities = -1;
	private FileIO fileIO;
	private VideoView videoView;
	private int nativeSubtitleIndex;
	private int localizedSubtitleIndex;
	// Store the following constant values instead of direct index of subtitle:
	// -1: no subtitle, 0: native subtitle, 1: localized subtitle
	// Explanation: indices of subtitles are independent from order of adding
	// and I wanted fix order of subtitles (off -> native -> localized -> off)
	// Subtitles must re-add after resume thus indices have to be determined again
	private int currentSubtitle = -1;

	private String getAbsolutePath(String file) {
		try {
			for (int i = 0; i < 10; i++) {
				if (ObbExpansionsManager.getInstance() == null) {
					break;
				}
				String path = ObbExpansionsManager.getInstance().getMainRoot();
				if (path == null) {
					AliteLog.e("AliteIntro playback", "OBB not yet mounted. Trying again in 200ms");
					Thread.sleep(200);
					continue;
				}
				AliteLog.d("AliteIntro playback", "Getting path for file: " + path + file);
				return path + file;
			}
		} catch (InterruptedException ignored) { }
		throw new RuntimeException("Mount OBB Error");
	}

	private String determineIntroName(int quality) {
		String name = AliteConfig.HAS_EXTENSION_APK ? determineIntroFilename(quality) : determineIntroId(quality);
		if (name == null) {
			return null;
		}
		return AliteConfig.HAS_EXTENSION_APK ? getAbsolutePath(DIRECTORY_INTRO + INTRO_FILE_NAME + name) :
			"android.resource://de.phbouillon.android.games.alite/" + name;
	}

	private String determineIntroId(int quality) {
		switch (quality) {
			case 0: AliteLog.d("Video Playback", "Using video resolution 1920x1080");
			        return String.valueOf(AliteConfig.ALITE_INTRO_B1920);
			case 1: AliteLog.d("Video Playback", "Using video resolution 1280x720");
				    return String.valueOf(AliteConfig.ALITE_INTRO_B1280);
			case 2:	AliteLog.d("Video Playback", "Using video resolution 640x360");
					return String.valueOf(AliteConfig.ALITE_INTRO_B640);
			case 3: AliteLog.d("Video Playback", "Using video resolution 320x180");
					return String.valueOf(AliteConfig.ALITE_INTRO_B320);
			case 4: AliteLog.d("Video Playback", "Failsafe mode 1: 288");
					return String.valueOf(AliteConfig.ALITE_INTRO_288);
			case 5: AliteLog.d("Video Playback", "Failsafe mode 2: 240");
					return String.valueOf(AliteConfig.ALITE_INTRO_B240);
			default: AliteLog.d("Video Playback", "No mode found. Giving up :(.");
					 return null;
		}
	}

	private String determineIntroFilename(int quality) {
		switch (quality) {
			case 0: AliteLog.d("Video Playback", "Using video resolution 1920x1080");
			        return "_b1920.mp4";
			case 1: AliteLog.d("Video Playback", "Using video resolution 1280x720");
				    return "_b1280.mp4";
			case 2:	AliteLog.d("Video Playback", "Using video resolution 640x360");
					return "_b640.mp4";
			case 3: AliteLog.d("Video Playback", "Using video resolution 320x180");
					return "_b320.mp4";
			case 4: AliteLog.d("Video Playback", "Failsafe mode 1: 288");
					return "_288.3gp";
			case 5: AliteLog.d("Video Playback", "Failsafe mode 2: 240");
					return "_b240.mp4";
			default: AliteLog.d("Video Playback", "No mode found. Giving up :(.");
					 return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		fileIO = new AndroidFileIO(this);
		if (intent == null || !intent.getBooleanExtra(Alite.LOG_IS_INITIALIZED, false)) {
			AliteLog.initialize(fileIO);
		}
		AliteLog.d("AliteIntro.onCreate", "onCreate begin");
		final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
			AliteLog.e("Uncaught Exception (AliteIntro)", "Message: " + (paramThrowable == null ? "<null>" : paramThrowable.getMessage()), paramThrowable);
			if (oldHandler != null) {
				oldHandler.uncaughtException(paramThread, paramThrowable);
			} else {
				System.exit(2);
			}
		});
		Settings.load(fileIO);
		L.setLocale(this, fileIO.getFileName(L.DIRECTORY_LOCALES + Settings.localeFileName));
		switch (Settings.lockScreen) {
			case 0: setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); break;
			case 1: setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); break;
			case 2: setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); break;
		}

		AliteLog.d("IntroVideoQuality", "IntroVideoQuality == " + Settings.introVideoQuality);
		if (Settings.introVideoQuality < 0) {
			// Device is not capable of playing intro video :(. Skip to game.
			startAlite(null);
			return;
		}
		if (savedInstanceState != null) {
			stopPosition = savedInstanceState.getInt("position");
		}
		setContentView(R.layout.activity_play_intro);

		if (videoView == null) {
			initializeVideoView();
		}

		AliteLog.d("IntroVideoQuality", "IntroVideoQuality = " + Settings.introVideoQuality);
		if (Settings.introVideoQuality == 255) {
			cyclingThroughVideoQualities = 0;
		}
		AliteLog.d("cyclingThroughVideoQualities", "cyclingThroughVideoQualities = " + cyclingThroughVideoQualities);
		String introName = determineIntroName(Settings.introVideoQuality == 255 ? 0 : Settings.introVideoQuality);
		if (introName == null) {
			startAlite(videoView);
		}
		playVideo(introName);
		videoView.setMediaController(null);
		videoView.requestFocus();
		AliteLog.d("AliteIntro.onCreate", "onCreate end");
	}

	private void playVideo(String introName) {
		AliteLog.d("Intro path", "Intro path: " + introName);
		videoView.setVideoPath(introName);
		addNativeSubtitle();
		addLocalizedSubtitle();
	}

	private void addNativeSubtitle() {
		File file = new File(getAbsolutePath(DIRECTORY_INTRO + INTRO_SUBTITLE_FILE_NAME));
		if (!file.exists()) {
			AliteLog.d("Subtitle","No native subtitle file found");
			return;
		}
		try {
			videoView.addSubtitleSource(new FileInputStream(file),
				MediaFormat.createSubtitleFormat("text/vtt", Locale.US.getLanguage()));
		} catch (FileNotFoundException e) {
			AliteLog.e("Subtitle","Native subtitle opening error", e);
		}
	}

	private void addLocalizedSubtitle() {
		if (Locale.US.getLanguage().equals(L.currentLocale.getLanguage())) {
			return;
		}
		try {
			videoView.addSubtitleSource(L.raw(DIRECTORY_INTRO, INTRO_SUBTITLE_FILE_NAME),
				MediaFormat.createSubtitleFormat("text/vtt", L.currentLocale.getLanguage()));
		} catch (IOException ignored) {
			AliteLog.d("Subtitle","Localized subtitle file not found");
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initializeVideoView() {
		videoView = new VideoView(this);
		videoView.setClickable(true);
		videoView.setOnClickListener(this);
		FrameLayout layout = findViewById(R.id.introContainer);
		layout.addView(videoView);
		videoView.getRootView().setBackgroundColor(getResources().getColor(android.R.color.black));
		videoView.setVisibility(View.VISIBLE);
        videoView.setOnTouchListener((v, event) -> {
			if (v instanceof VideoView) {
				startAlite((VideoView) v);
				return true;
			}
			return v.performClick();
		});
		final OnCompletionListener onCompletionListener = mp -> startAlite(videoView);
		videoView.setOnCompletionListener(onCompletionListener);

		AliteLog.e("Creating Error Listener", "EL created");
		videoView.setOnErrorListener((mp, what, extra) -> {
			AliteLog.d("cyclingThroughVideoQualities [1]", "cyclingThroughVideoQualities = " + cyclingThroughVideoQualities);
			if (cyclingThroughVideoQualities != -1) {
				cyclingThroughVideoQualities++;
				String introName = determineIntroName(cyclingThroughVideoQualities);
				if (introName != null) {
					if (mp != null) {
						mp.reset();
					}
					needsToPlay = true;
					playVideo(introName);
					return true;
				}
				cyclingThroughVideoQualities = -1;
				Settings.introVideoQuality = -1;
				Settings.save(fileIO);
				startAlite(videoView);
			}

			String cause = "Undocumented cause: " + what;
			switch (what) {
				case MediaPlayer.MEDIA_ERROR_UNKNOWN: cause = "Unknown cause."; break;
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED: cause = "Server died."; break;
				case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: cause = "Not valid for progressive playback."; break;
			}

			String details = "Undocumented error details: " + extra;
			switch (extra) {
				case MediaPlayer.MEDIA_ERROR_IO: details = "Media Error IO."; break;
				case MediaPlayer.MEDIA_ERROR_MALFORMED: details = "Media Error Malformed."; break;
				case MediaPlayer.MEDIA_ERROR_UNSUPPORTED: details = "Media Error Unsupported."; break;
				case MediaPlayer.MEDIA_ERROR_TIMED_OUT: details = "Media Error Timed Out."; break;
				case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: details = "Not valid for progressive playback."; break;
			}
			AliteLog.d("Intro Playback Error", "Couldn't playback intro. " + cause + " " + details);
			onCompletionListener.onCompletion(mp);
			return true;
		});

		videoView.setOnPreparedListener(mp -> {
			AliteLog.d("VideoView", "VideoView is prepared. Playing video.");
			mediaPlayer = mp;
			nativeSubtitleIndex = -1;
			localizedSubtitleIndex = -1;
			MediaPlayer.TrackInfo[] tracks = mediaPlayer.getTrackInfo();
			for (int i=0; i < tracks.length; i++) {
				if (tracks[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
					if (Locale.US.getLanguage().equals(tracks[i].getLanguage())) {
						nativeSubtitleIndex = i;
					} else if (L.currentLocale.getLanguage().equals(tracks[i].getLanguage())) {
						localizedSubtitleIndex = i;
					}
				}
			}
			if (nativeSubtitleIndex >= 0 || localizedSubtitleIndex >= 0) {
				findViewById(R.id.subtitle).bringToFront();
				// Turn off the last added subtitle
				mediaPlayer.deselectTrack(localizedSubtitleIndex >= 0 ? localizedSubtitleIndex : nativeSubtitleIndex);
				selectOrDeselectTrack(true);
			}
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					isInPlayableState = true;
					if (needsToPlay && isResumed) {
						continuePlaying();
						needsToPlay = false;
					}
					return null;
				}
			}.execute(null, null, null);
		});
	}

	private void selectOrDeselectTrack(boolean select) {
		if (currentSubtitle >= 0) {
			if (select) {
				mediaPlayer.selectTrack(currentSubtitle == 0 ? nativeSubtitleIndex : localizedSubtitleIndex);
			} else {
				mediaPlayer.deselectTrack(currentSubtitle == 0 ? nativeSubtitleIndex : localizedSubtitleIndex);
			}
		}
	}

	@Override
	protected void onPause() {
		AliteLog.d("AliteIntro.onPause", "onPause begin");
		if (videoView == null) {
			AliteLog.e("Alite Intro", "Video view is not found. [onPause]");
			super.onPause();
			isResumed = false;
			return;
		}
		stopPosition = videoView.getCurrentPosition();
		videoView.pause();
		super.onPause();
		isResumed = false;
		AliteLog.d("AliteIntro.onPause", "onPause end");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("position", stopPosition);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		AndroidUtil.setImmersion(videoView);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof VideoView) {
			VideoView videoView = (VideoView) v;
			try {
				videoView.stopPlayback();
			} catch (IllegalStateException ignored) { }
			startAlite(videoView);
		}
		AliteLog.d("Change subtitle", "Current subtitle: " + currentSubtitle);
		if (nativeSubtitleIndex >= 0 || localizedSubtitleIndex >= 0) {
			selectOrDeselectTrack(false);
			currentSubtitle = currentSubtitle < 0 && nativeSubtitleIndex >= 0 ? 0 :
				currentSubtitle < 0 || currentSubtitle == 0 && localizedSubtitleIndex >= 0 ? 1 : -1;
			selectOrDeselectTrack(true);
		}
	}

	private synchronized void startAlite(VideoView videoView) {
		AliteLog.d("startAlite call", "startAlite begin");
		if (aliteStarted) {
			return;
		}
		aliteStarted = true;
		AliteLog.d("startAlite", "startAlite flag changed");
		boolean result = fileIO.deleteFile(AliteStartManager.ALITE_STATE_FILE);
		AliteLog.d("Deleting state file", "Delete result: " + result);
		AliteLog.d("startAlite", "updating settings");
		if (cyclingThroughVideoQualities != -1) {
			Settings.introVideoQuality = cyclingThroughVideoQualities;
			Settings.save(new AndroidFileIO(this));
		}

		AliteLog.d("startAlite", "Killing video view");
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.pause();
			videoView.clearAnimation();
			videoView.clearFocus();
		}
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		AliteLog.d("startAlite", "Calling Alite start intent");
		Intent intent = new Intent(this, Alite.class);
		intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
		AliteLog.d("startAlite", "Calling startActivity");
		startActivityForResult(intent, 0);
		AliteLog.d("startAlite", "Done");
	}

	@Override
	protected void onResume() {
		AliteLog.d("AliteIntro.onResume", "onResume begin, isInPlayableState = " + isInPlayableState);
		super.onResume();
		isResumed = true;
		if (videoView == null) {
			AliteLog.e("Alite Intro", "Video view is not found. [onResume]");
			isInPlayableState = false;
		}
		if (isInPlayableState) {
			continuePlaying();
		} else {
			needsToPlay = true;
		}
		AliteLog.d("AliteIntro.onResume", "onResume end");
	}

	private void continuePlaying() {
		addNativeSubtitle();
		addLocalizedSubtitle();
		videoView.seekTo(stopPosition);
		videoView.start();
	}

	@Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == AliteStartManager.ALITE_RESULT_CLOSE_ALL) {
	      setResult(AliteStartManager.ALITE_RESULT_CLOSE_ALL);
	      finish();
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	  }
}
