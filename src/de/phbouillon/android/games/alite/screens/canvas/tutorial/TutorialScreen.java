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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.media.MediaPlayer;
import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.PulsingHighlighter;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.screens.NavigationBar;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.LoadingScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public abstract class TutorialScreen extends AliteScreen {
	private static final int TEXT_LINE_HEIGHT = 50;
	private static final String DIRECTORY_SOUND_TUTORIAL = LoadingScreen.DIRECTORY_SOUND + "tutorial" + File.separatorChar;
	private static final String PLAYER_STATE_BACKUP = "tut_player_state.dat";

	private final transient List <TutorialLine> lines = new ArrayList<>();
	private transient TutorialLine currentLine;
	int currentLineIndex;
	private transient TextData[] textData;
	protected final transient MediaPlayer mediaPlayer;
	private final boolean isGl;
	boolean hideCloseButton = false;

	private boolean tutorialAborted = false;
	private transient Button closeButton;

	private int currentX = -1;
	private int currentY = -1;
	private int currentWidth = -1;
	private int currentHeight = -1;

	TutorialScreen(Alite alite) {
		this(alite, false);
	}

	TutorialScreen(Alite alite, boolean gl) {
		super(alite);
		isGl = gl;
		currentLineIndex = -1;
		currentLine = null;
		mediaPlayer = new MediaPlayer();
		restorePlayerState();
		backupPlayerState();
		alite.getPlayer().clearMissions();
	}

	private void restorePlayerState() {
		if (!game.getFileIO().exists(PLAYER_STATE_BACKUP)) {
			return;
		}
		try {
			byte[] state = game.getFileIO().readFileContents(PLAYER_STATE_BACKUP);
			if (state != null && state.length > 0) {
				game.loadCommander(new DataInputStream(new ByteArrayInputStream(state)));
				game.getFileIO().deleteFile(PLAYER_STATE_BACKUP);
				AliteLog.d("Tutorial Screen restorePlayerState", "Player state restored successfully.");
			}
		} catch (IOException e) {
			AliteLog.e("Tutorial Screen restorePlayerState", "Error in restoring state.", e);
		}
	}

	private void backupPlayerState() {
		try (OutputStream stateFile = game.getFileIO().writeFile(PLAYER_STATE_BACKUP)) {
			game.saveCommander(new DataOutputStream(stateFile));
			AliteLog.d("Tutorial Screen backupPlayerState", "Player state backup successfully.");
		} catch (IOException e) {
			AliteLog.e("Tutorial Screen backupPlayerState", "Error in backup state.", e);
		}
	}

	@Override
	public void activate() {
		closeButton = Button.createPictureButton(0, 970, 110, 110, Assets.noIcon);
		restorePlayerState();
		backupPlayerState();
		game.getPlayer().clearMissions();
	}

	PulsingHighlighter makeHighlight(int x, int y, int width, int height) {
		return new PulsingHighlighter(game, x, y, width, height, 20,
			ColorScheme.get(ColorScheme.COLOR_PULSING_HIGHLIGHTER_LIGHT),
			ColorScheme.get(ColorScheme.COLOR_PULSING_HIGHLIGHTER_DARK));
	}

	void renderText() {
		if (currentLine != null) {
			currentY = currentLine.getY();
			currentHeight = currentLine.getHeight();
			if (currentHeight != 0 && textData != null && textData.length > 0) {
				currentHeight = textData[textData.length - 1].y + 30 - currentY;
				if (currentY + currentHeight > AliteConfig.SCREEN_HEIGHT) {
					AliteLog.e("Overfull VBox", "Attention: Overfull VBox for " + currentLine.getText() + " => " + (currentY + currentHeight));
				}
				currentLine.setHeight(currentHeight);
			}
			currentX = currentLine.getX();
			currentWidth = currentLine.getWidth();
		}

		if (currentLine == null || currentLine.getText().isEmpty()) {
			return;
		}

		Graphics g = game.getGraphics();
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		GLES11.glEnable(GLES11.GL_BLEND);
		if (currentX != -1 && currentY != -1) {
			g.diagonalGradientRect(currentX, currentY, currentWidth, currentHeight,
				ColorScheme.get(ColorScheme.COLOR_TUTORIAL_BUBBLE_DARK),
				ColorScheme.get(ColorScheme.COLOR_TUTORIAL_BUBBLE_LIGHT));
		}
		GLES11.glDisable(GLES11.GL_BLEND);
		if (currentX != -1 && currentY != -1) {
			g.rec3d(currentX, currentY, currentWidth, currentHeight, 4,
				ColorScheme.get(ColorScheme.COLOR_TUTORIAL_BUBBLE_LIGHT),
				ColorScheme.get(ColorScheme.COLOR_TUTORIAL_BUBBLE_DARK));
		}
		if (currentLine != null && textData != null) {
			displayText(g, textData);
		}
	}

	TutorialLine addLine(int tutorialIndex, String line) {
		return addLine(tutorialIndex, line, null);
	}

	TutorialLine addLine(int tutorialIndex, String line, String option) {
		String path = DIRECTORY_SOUND_TUTORIAL + tutorialIndex + "/";
		int index = lines.size() + 1;
		String audioName = path + (index < 10 ? "0" + index : index);
		try {
			TutorialLine result = new TutorialLine(L.rawDescriptor(audioName + ".mp3"), line);
			if (option != null) {
				result.addSpeech(L.rawDescriptor(audioName + option + ".mp3"));
			}
			lines.add(result);
			return result;
		} catch (IOException e) {
			AliteLog.e("Error Reading Tutorial", "Error reading Tutorial", e);
			return null;
		}
	}

	TutorialLine addEmptyLine() {
		TutorialLine result = new TutorialLine(null, "").setHeight(0).setWidth(0);
		lines.add(result);
		return result;
	}

	Screen updateNavBar() {
		for (TouchEvent event: game.getInput().getTouchEvents()) {
			Screen screen = checkNavigationBar(event);
			if (screen != null) {
				game.getNavigationBar().resetPending();
				return screen;
			}
		}
		return null;
	}

	protected abstract void doPresent(float deltaTime);

	protected void renderGlPart(float deltaTime, final Rect visibleArea) {
	}

	protected void doUpdate(float deltaTime) {
	}

	@Override
	public void renderNavigationBar() {
		if (isGl) {
			return;
		}
		super.renderNavigationBar();
	}

	@Override
	public void present(float deltaTime) {
		if (isGl) {
			Rect visibleArea = game.getGraphics().getVisibleArea();
			renderGlPart(deltaTime, visibleArea);
			setUpForDisplay(visibleArea);
		}
		if (currentLine != null) {
			currentLine.prePresent(deltaTime);
		}
		doPresent(deltaTime);
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		GLES11.glEnable(GLES11.GL_BLEND);
		if (!hideCloseButton) {
			closeButton.render(game.getGraphics());
		}
		GLES11.glDisable(GLES11.GL_BLEND);
		if (currentLine != null) {
			currentLine.postPresent(deltaTime);
		}
	}

	@Override
	public void postNavigationRender(float deltaTime) {
		if (currentLine != null) {
			GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
			GLES11.glEnable(GLES11.GL_BLEND);
			currentLine.renderHighlights(deltaTime);
			GLES11.glDisable(GLES11.GL_BLEND);
		}
	}

	private void checkTutorialClose(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (closeButton.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				showQuestionDialog(L.string(R.string.tutorial_quit_confirm));
			}
		}
	}

	@Override
	public void processTouch(TouchEvent event) {
		super.processTouch(event);
		if (messageResult == RESULT_YES) {
			if (currentLine != null) {
				mediaPlayer.reset();
			}
			newScreen = new TutorialSelectionScreen(game);
			performScreenChange();
			postScreenChange();
			tutorialAborted = true;
		}
		messageResult = RESULT_NONE;
	}

	@Override
	public void update(float deltaTime) {
		if (tutorialAborted) {
			game.getInput().getTouchEvents();
			return;
		}
		if (!hideCloseButton) {
			for (TouchEvent event: game.getInput().getAndRetainTouchEvents()) {
				processTouch(event);
				if (tutorialAborted) {
					return;
				}
				checkTutorialClose(event);
			}
		}

		if (currentLine == null) {
			currentLineIndex++;
			if (currentLineIndex >= lines.size()) {
				newScreen = new TutorialSelectionScreen(game);
				performScreenChange();
				postScreenChange();
				return;
			}
			currentLine = lines.get(currentLineIndex);
			currentLine.reset();
			currentLine.play(mediaPlayer);
			textData = computeTextDisplay(game.getGraphics(), currentLine.getText(),
										  currentLine.getX() + 50,
										  currentLine.getY() + 50,
										  currentLine.getWidth() - 100,
										  TEXT_LINE_HEIGHT,
										  ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		} else {
			if (!currentLine.isPlaying()) {
				currentLine.executeFinishHook();
				currentLine = null;
			}
		}

		if (currentLine != null) {
			currentLine.update(deltaTime);
		}

		if (currentLine == null || !currentLine.mustRetainEvents()) {
			for (TouchEvent event: game.getInput().getTouchEvents()) {
				if (event.type == TouchEvent.TOUCH_UP && currentLine != null) {
					if (event.x > currentLine.getX() && event.x < currentLine.getX() + currentLine.getWidth() &&
						event.y > currentLine.getY() && event.y < currentLine.getY() + currentLine.getHeight() &&
						currentLine.isSkippable()) {
						mediaPlayer.reset();
						currentLine.executeFinishHook();
						currentLine = null;
					}
				}
			}
		}
		doUpdate(deltaTime);
	}

	@Override
	public void dispose() {
		restorePlayerState();
		super.dispose();
		Settings.load(game.getFileIO());
		game.getPlayer().setCondition(Condition.DOCKED);
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_ACADEMY);
	}

	@Override
	public void pause() {
		super.pause();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		game.saveCommander(dos);
		super.saveScreenState(dos);
	}

	protected boolean loadScreenState(DataInputStream dis) throws IOException {
		game.loadCommander(dis);
		return true;
	}

}
