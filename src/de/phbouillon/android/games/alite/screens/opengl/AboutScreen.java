package de.phbouillon.android.games.alite.screens.opengl;

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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.impl.gl.Sprite;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.LoadingScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;

// ??              - Adder Mk II          - Gopher
//                 - Mosquito Trader      - Indigo
//                 - Jabberwocky          - Dugite
//                 - TIE-Fighter          - TIE-Fighter
//                 - Tiger                - Lyre
// ADCK            - Eagle Mk IV          - Rattlesnake   - Michael Francis
// Captain Beatnik - Coluber Pitviper     - Harlequin     - captain.beatnik@gmx.de (Robert Triflinger)
// Wyvern
// Clym Angus      - Kirin                - Yellowbelly   -
// Galileo         - Huntsman             - Mussurana     - Eric Walsh
// Murgh           - Bandy-Bandy Courier  - Coral         - ??
//                 - Cat                  - Cougar        - ??
// Wolfwood        - Drake Mk II          - Bushmaster    - ??


// This screen never needs to be serialized, as it is not part of the InGame state.
public class AboutScreen extends GlScreen {
	private static final int WAIT_CYCLE_IN_50_MICROS = 60; // 3s

	private final Rect visibleArea;
	private Sprite background;
	private Sprite aliteLogo;
	private final Timer timer = new Timer().setAutoReset();
	private Music endCreditsMusic;
	private float alpha = 0.0001f;
	private float globalAlpha = 1.0f;
	private int mode;
	private int y = 1200;
	private boolean returnToOptions = false;
	private float musicVolume = Settings.volumes[Sound.SoundType.MUSIC.getValue()];
	private Alite game;

	private int pendingMode = -1;

	private static final String SPACE_300 = "300";
	private static final String SPACE_250 = "250";
	private static final String SPACE_200 = "200";
	private static final String SPACE_100 = "100";
	private static final String SPACE_90  = " 90";
	private static final String SPACE_60  = " 60";
	private static final String SPACE_0   = "  0";

	private static final String SCALE_3_0 = "3.0";
	private static final String SCALE_2_0 = "2.0";
	private static final String SCALE_1_5 = "1.5";

	private static final char DESCRIPTION_COLOR = 'w';
	private static final char PERSON_COLOR      = 'y';
	private static final char ADDITION_COLOR    = 'o';

	private static final String[] formatter = new String[] {
		SPACE_0 + SCALE_3_0 + PERSON_COLOR, // 0 - MAIN_TITLE
		SPACE_300 + SCALE_1_5 + DESCRIPTION_COLOR, // 1 - TITLE_1
		SPACE_250 + SCALE_1_5 + DESCRIPTION_COLOR, // 2 - TITLE_2
		SPACE_200 + SCALE_1_5 + DESCRIPTION_COLOR, // 3 - TITLE_3
		SPACE_100 + SCALE_1_5 + DESCRIPTION_COLOR, // 4 - TITLE_4
		SPACE_60 + SCALE_2_0 + PERSON_COLOR, // 5 - FIRST_ELEMENT
		SPACE_90 + SCALE_2_0 + PERSON_COLOR, // 6 - FURTHER_ELEMENT
		SPACE_90 + SCALE_2_0 + ADDITION_COLOR, // 7 - ADDITIONAL_TEXT
		SPACE_200 + SCALE_2_0 + PERSON_COLOR, // 8 - VIP_TEXT
		SPACE_250 + SCALE_1_5 + PERSON_COLOR, // 9 - ELITE_TEXT
		SPACE_60 + SCALE_1_5 + PERSON_COLOR, // 10 - ELITE_TEXT
		SPACE_60 + SCALE_1_5 + DESCRIPTION_COLOR }; // 11 - BLUE_DANUBE_TEXT

	private final List <TextData> texts;

	// default public constructor is required for navigation bar
	public AboutScreen() {
		game = Alite.get();
		visibleArea = game.getGraphics().getVisibleArea();
		background = new Sprite(0, 0, AliteConfig.SCREEN_WIDTH, AliteConfig.SCREEN_HEIGHT,
			0.0f, 0.0f, 1.0f, 1.0f, "textures/star_map_title.png");
		aliteLogo  = new Sprite(0, 0, AliteConfig.SCREEN_WIDTH, AliteConfig.SCREEN_HEIGHT,
			0.0f, 0.0f, 1615.0f / 2048.0f, AliteConfig.SCREEN_HEIGHT / 2048.0f, "title_logo.png");
		aliteLogo.scale(0.96f, 0, 0, AliteConfig.SCREEN_WIDTH, AliteConfig.SCREEN_HEIGHT);
		endCreditsMusic = game.getAudio().newMusic(LoadingScreen.DIRECTORY_MUSIC + "end_credits.mp3");
		texts = new ArrayList<>();
		for (String s: L.string(R.string.about, AliteConfig.GAME_NAME).split("\n")) {
			int b = s.indexOf('[');
			int e = s.indexOf(']');
			addLine(Integer.parseInt(s.substring(b + 1, e)), s.substring(e+1));
		}
	}

	public AboutScreen(int pendingMode, int y, float alpha) {
		this();
		this.pendingMode = pendingMode;
		this.y = y;
		this.alpha = alpha;
	}

	private void addLine(int formatIndex, String text) {
		String sequence = formatter[formatIndex];
		int height = Integer.parseInt(sequence.substring(0, 3).trim());
		float scale = Float.parseFloat(sequence.substring(3, 6));
		int color = ColorScheme.get(ColorScheme.COLOR_MESSAGE);
		switch (sequence.charAt(6)) {
			case DESCRIPTION_COLOR:
				color = ColorScheme.get(ColorScheme.COLOR_CREDITS_DESCRIPTION);
				break;
			case PERSON_COLOR:
				color = ColorScheme.get(ColorScheme.COLOR_CREDITS_PERSON);
				break;
			case ADDITION_COLOR:
				color = ColorScheme.get(ColorScheme.COLOR_CREDITS_ADDITION);
				break;
		}
		TextData td = new TextData(text, 960, (texts.isEmpty() ? 0 : texts.get(texts.size()-1).y) +
			height, color, null);
		td.scale = scale;
		texts.add(td);
	}

	@Override
	public void onActivation() {
		endCreditsMusic.setLooping(true);
		endCreditsMusic.play();
		initializeGl();
		mode = pendingMode == -1 ? 0 : pendingMode;
		pendingMode = -1;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(mode);
		dos.writeInt(y);
		dos.writeFloat(alpha);
	}

	private void initializeGl() {
		float ratio = visibleArea.width() / (float) visibleArea.height();
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GlUtils.setViewport(visibleArea);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, ratio, 10.0f, 1000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		GLES11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT | GLES11.GL_DEPTH_BUFFER_BIT);
		GLES11.glHint(GLES11.GL_PERSPECTIVE_CORRECTION_HINT, GLES11.GL_NICEST);
		GLES11.glEnable(GLES11.GL_DEPTH_TEST);
		GLES11.glDepthFunc(GLES11.GL_LEQUAL);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE);
	}

	private void performFadeIn() {
		alpha *= 1.1f;
		if (alpha >= 1.0f) {
			alpha = 1.0f;
			mode++;
		}
	}

	private void performFadeOut() {
		alpha *= 0.99f;
		if (alpha <= 0.3f) {
			alpha = 0.3f;
			mode++;
		}
	}

	private void doNotScrollLastParagraph() {
		int n = texts.size();
		// Leave the Thanks to Klaudia part on the screen, but scroll everything else...
		for (int i = 1; i < 5; i++) {
			texts.get(n - i).y += 2;
		}
	}

	@Override
	public void performUpdate(float deltaTime) {
		if (timer.hasPassedMicros(50)) {
			y -= 2;
			if (mode == 0) {
				performFadeIn();
			} else if (mode <= WAIT_CYCLE_IN_50_MICROS) {
				mode++;
			} else if (mode == WAIT_CYCLE_IN_50_MICROS + 1) {
				performFadeOut();
			} else if (mode == WAIT_CYCLE_IN_50_MICROS + 3) {
				doNotScrollLastParagraph();
			}
		}
		if (returnToOptions) {
			globalAlpha *= 0.95f;
			musicVolume *= 0.95f;
			endCreditsMusic.setVolume(musicVolume);
		}
		for (TouchEvent event: game.getInput().getTouchEvents()) {
			if (event.type == TouchEvent.TOUCH_DOWN && !returnToOptions) {
				SoundManager.play(Assets.click);
				returnToOptions = true;
			}
		}
		if (returnToOptions && globalAlpha < 0.01) {
			GLES11.glClear(GLES11.GL_DEPTH_BUFFER_BIT | GLES11.GL_COLOR_BUFFER_BIT);
			GLES11.glDisable(GLES11.GL_DEPTH_TEST);
			game.setScreen(new OptionsScreen());
		}
	}

	@Override
	public void performPresent(float deltaTime) {
		if (isDisposed()) {
			return;
		}
		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT | GLES11.GL_DEPTH_BUFFER_BIT);
		GLES11.glClearDepthf(1.0f);

		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPushMatrix();
		GLES11.glLoadIdentity();
		Rect visibleArea = game.getGraphics().getVisibleArea();
		GlUtils.ortho(game, visibleArea);

		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		GLES11.glDisable(GLES11.GL_DEPTH_TEST);
		GLES11.glColor4f(globalAlpha, globalAlpha, globalAlpha, globalAlpha);
		background.render();
		GLES11.glColor4f(globalAlpha * alpha, globalAlpha * alpha, globalAlpha * alpha, globalAlpha * alpha);
		aliteLogo.render();
		game.getGraphics().drawText(L.string(R.string.about_version, AliteConfig.GAME_NAME, AliteConfig.VERSION_STRING), 0, 1030,
			AliteColor.argb(globalAlpha, globalAlpha, globalAlpha, globalAlpha), Assets.regularFont, 1.0f);
		if (y < 1200) {
			int i = 0;
			for (TextData text: texts) {
				i++;
				if (y + text.y > -120) {
					if (y + text.y > AliteConfig.SCREEN_HEIGHT) {
						break;
					}
					game.getGraphics().drawCenteredText(text.text, text.x, y + text.y,
						AliteColor.colorAlpha(text.color, globalAlpha), Assets.boldFont, text.scale);
					if (y + text.y < 525 && i == texts.size() - 1) {
						mode = WAIT_CYCLE_IN_50_MICROS + 3;
					}
				}
			}
		}
		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glPopMatrix();
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glEnable(GLES11.GL_DEPTH_TEST);

		GLES11.glDisable(GLES11.GL_TEXTURE_2D);
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void postPresent(float deltaTime) {
	}

	@Override
	public void pause() {
		super.pause();
		disposeMusic();
	}

	private void disposeMusic() {
		if (endCreditsMusic != null) {
			endCreditsMusic.stop();
			endCreditsMusic.dispose();
			endCreditsMusic = null;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (aliteLogo != null) {
			aliteLogo.destroy();
			aliteLogo = null;
		}
		if (background != null) {
			background.destroy();
			background = null;
		}
		disposeMusic();
	}

	@Override
	public void loadAssets() {
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.ABOUT_SCREEN;
	}
}
