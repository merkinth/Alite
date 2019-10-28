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
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.impl.AndroidFileIO;
import de.phbouillon.android.games.alite.io.ObbExpansionsManager;

public class AliteIntro extends Activity implements OnClickListener {
	private static final String DIRECTORY_INTRO = "intro" + File.separatorChar;
	private static final String INTRO_FILE_NAME = "alite_intro";
	private static final String INTRO_SUBTITLE_FILE_NAME = INTRO_FILE_NAME + ".vtt";

	private MediaPlayer mediaPlayer;
	private boolean isInPlayableState;
	private boolean needsToPlay;
	private boolean isResumed;
	private int stopPosition;
	private boolean aliteStarted;
	private FileIO fileIO;
	private VideoView videoView;
	// Subtitle must re-add after resume thus indices have to be determined again
	private int subtitleIndex;
	private boolean subtitleOn;

	private String getAbsolutePath(String file) {
		try {
			for (int i = 0; i < 10; i++) {
				if (ObbExpansionsManager.getInstance() == null) {
					break;
				}
				String path = ObbExpansionsManager.getInstance().getMainRoot();
				if (path == null) {
					AliteLog.d("AliteIntro playback", "OBB not yet mounted. Trying again in 200ms");
					Thread.sleep(200);
					continue;
				}
				AliteLog.d("AliteIntro playback", "Getting path for file: " + path + file);
				return path + file;
			}
		} catch (InterruptedException ignored) { }
		throw new RuntimeException("Mount OBB Error");
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
			AliteLog.e("Uncaught Exception (AliteIntro)",
				"Message: " + (paramThrowable == null ? "<null>" : paramThrowable.getMessage()), paramThrowable);
			if (oldHandler != null) {
				oldHandler.uncaughtException(paramThread, paramThrowable);
			} else {
				System.exit(2);
			}
		});
		Settings.load(fileIO);
		L.getInstance().setLocale(this, fileIO.getFileName(L.DIRECTORY_LOCALES + Settings.localeFileName));
		Settings.setOrientation(this);

		if (savedInstanceState != null) {
			stopPosition = savedInstanceState.getInt("position");
		}
		setContentView(R.layout.activity_play_intro);

		if (videoView == null) {
			initializeVideoView();
		}

		videoView.setVideoPath(getIntroName());
		addSubtitle();

		videoView.setMediaController(null);
		videoView.requestFocus();
		AliteLog.d("AliteIntro.onCreate", "onCreate end");
	}

	private String getIntroName() {
		AliteLog.d("AliteIntro.Video Playback", "Using video resolution 1920x1080");
		try {
			return AliteConfig.HAS_EXTENSION_APK ? saveFile() :
				"android.resource://de.phbouillon.android.games.alite/" + AliteConfig.ALITE_INTRO_B1920;
		} catch (IOException ignored) {
			return null;
		}
	}

	private String saveFile() throws IOException {
		File f = L.saveFile(new FileInputStream(getAbsolutePath(DIRECTORY_INTRO + INTRO_FILE_NAME + "_b1920.mp4")));
		f.deleteOnExit();
		return f.getPath();
	}

	private void addSubtitle() {
		if (Locale.US.getLanguage().equals(L.getInstance().getCurrentLocale().getLanguage())) {
			addNativeSubtitle();
			return;
		}
		try {
			videoView.addSubtitleSource(L.raw(DIRECTORY_INTRO, INTRO_SUBTITLE_FILE_NAME),
				MediaFormat.createSubtitleFormat("text/vtt", L.getInstance().getCurrentLocale().getLanguage()));
		} catch (IOException ignored) {
			AliteLog.d("AliteIntro.Subtitle","Localized subtitle file not found");
		}
	}

	private void addNativeSubtitle() {
		File file = new File(getAbsolutePath(DIRECTORY_INTRO + INTRO_SUBTITLE_FILE_NAME));
		if (!file.exists()) {
			AliteLog.d("AliteIntro.Subtitle","No native subtitle file found");
			return;
		}
		try {
			videoView.addSubtitleSource(new FileInputStream(file),
				MediaFormat.createSubtitleFormat("text/vtt", Locale.US.getLanguage()));
		} catch (FileNotFoundException e) {
			AliteLog.e("AliteIntro.Subtitle","Native subtitle opening error", e);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initializeVideoView() {
		videoView = new VideoView(this);
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
		videoView.setOnCompletionListener(mp -> startAlite(videoView));

		AliteLog.d("AliteIntro.Creating Error Listener", "EL created");
		videoView.setOnErrorListener((mp, what, extra) -> {
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
			AliteLog.d("AliteIntro.Intro Playback Error", "Couldn't playback intro. " + cause + " " + details);
			TextView errorText = new TextView(this);
			errorText.setGravity(Gravity.CENTER);
			errorText.setText(L.string(R.string.intro_error, cause, details));
			errorText.setOnClickListener(this);
			layout.addView(errorText);
			return true;
		});

		videoView.setOnPreparedListener(mp -> {
			AliteLog.d("AliteIntro.VideoView", "VideoView is prepared. Playing video.");
			mediaPlayer = mp;
			subtitleIndex = -1;
			MediaPlayer.TrackInfo[] tracks = mediaPlayer.getTrackInfo();
			for (int i=0; i < tracks.length; i++) {
				if (tracks[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
					subtitleIndex = i;
				}
			}
			if (subtitleIndex >= 0) {
				findViewById(R.id.subtitle).bringToFront();
				mediaPlayer.deselectTrack(subtitleIndex);
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
		if (subtitleOn) {
			if (select) {
				mediaPlayer.selectTrack(subtitleIndex);
			} else {
				mediaPlayer.deselectTrack(subtitleIndex);
			}
		}
	}

	@Override
	protected void onPause() {
		AliteLog.d("AliteIntro.onPause", "onPause begin");
		if (videoView == null) {
			AliteLog.e("AliteIntro", "Video view is not found. [onPause]");
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
		Settings.setImmersion(videoView);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.subtitle) {
			if (mediaPlayer != null && subtitleIndex >= 0) {
				selectOrDeselectTrack(false);
				subtitleOn = !subtitleOn;
				selectOrDeselectTrack(true);
				findViewById(R.id.subtitle).setBackgroundResource(subtitleOn ? R.drawable.subtitle_nat : R.drawable.subtitle);
			}
			return;
		}
		// Error message
		startAlite(null);
	}

	private synchronized void startAlite(VideoView videoView) {
		AliteLog.d("AliteIntro.startAlite call", "startAlite begin");
		if (aliteStarted) {
			return;
		}
		aliteStarted = true;
		boolean result = fileIO.deleteFile(AliteStartManager.ALITE_STATE_FILE);
		AliteLog.d("AliteIntro.Deleting state file", "Delete result: " + result);
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
		AliteLog.d("AliteIntro.startAlite", "Calling Alite start intent");
		Intent intent = new Intent(this, Alite.class);
		intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
		AliteLog.d("AliteIntro.startAlite", "Calling startActivity");
		startActivityForResult(intent, 0);
		finish();
		AliteLog.d("AliteIntro.startAlite", "Done");
	}

	@Override
	protected void onResume() {
		AliteLog.d("AliteIntro.onResume", "onResume begin, isInPlayableState = " + isInPlayableState);
		super.onResume();
		isResumed = true;
		if (videoView == null) {
			AliteLog.e("AliteIntro", "Video view is not found. [onResume]");
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
		addSubtitle();
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
