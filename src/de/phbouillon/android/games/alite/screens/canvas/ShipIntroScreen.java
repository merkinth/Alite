package de.phbouillon.android.games.alite.screens.canvas;

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

import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.SkySphereSpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.MathHelper;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectAI;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ShipIntroScreen extends AliteScreen {
	private static final boolean DEBUG_EXHAUST = false;
	private static final boolean ONLY_CHANGE_SHIPS_AFTER_SWEEP = false;
	private static final boolean SHOW_DOCKING = false;
	private static final boolean DANCE = true;

	private SkySphereSpaceObject skysphere;
	private SpaceObject currentShip;

	private final Timer timer = new Timer().setAutoReset();
	private final Timer danceTimer = new Timer().setAutoReset();
	private Vector3f currentDelta = new Vector3f(0,0,0);
	private Vector3f targetDelta = new Vector3f(0,0,0);

	private Button yesButton;
	private Button noButton;
	private Button tapToStartButton;
	private int selectionDirection = 1;
	private SpaceObject coriolis;

	enum DisplayMode {
		ZOOM_IN,
		DANCE,
		ZOOM_OUT
	}

	private DisplayMode displayMode = DisplayMode.ZOOM_IN;
	private Music theChase;
	private boolean showLoadNewCommander;

	public ShipIntroScreen() {
		showLoadNewCommander = game.existsSavedCommander();
		theChase = game.getAudio().newMusic(LoadingScreen.DIRECTORY_MUSIC + "the_chase.mp3");
	}

	public ShipIntroScreen(String objectId) {
		this();
		currentShip = SpaceObjectFactory.getInstance().getObjectById(objectId);
		selectionDirection = 0;
	}

	@Override
	public void update(float deltaTime) {
		updateWithoutNavigation(deltaTime);
		if (DANCE) {
			MathHelper.updateAxes(currentDelta, targetDelta);
		}
		if (currentShip != null) {
			currentShip.applyDeltaRotation(currentDelta.x, currentDelta.y, currentDelta.z);
			currentShip.update(deltaTime);
		}
		switch (displayMode) {
			case ZOOM_IN:  zoomIn();  break;
			case DANCE:    dance();   break;
			case ZOOM_OUT: zoomOut(); break;
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_SWEEP) {
			if (displayMode == DisplayMode.DANCE) {
				displayMode = DisplayMode.ZOOM_OUT;
			}
			if (touch.x2 > 0) {
				AliteLog.d("TouchSweep", touch.x + ", " + touch.x2 + ", " + touch.y + ", " + touch.y2);
				selectionDirection = -1;
			} else {
				selectionDirection = 1;
			}
		}
		if (yesButton.isPressed(touch)) {
			SoundManager.play(Assets.click);
			newScreen = new LoadScreen(L.string(R.string.title_cmdr_load));
			game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_DISK);
		}
		if (noButton.isPressed(touch)) {
			SoundManager.play(Assets.click);
			newScreen = new StatusScreen();
		}
		if (tapToStartButton.isPressed(touch)) {
			SoundManager.play(Assets.click);
			newScreen = new StatusScreen();
		}
		if (newScreen != null) {
			if (theChase != null) {
				theChase.stop();
				theChase.dispose();
				theChase = null;
			}
		}
	}

	@Override
	public void renderNavigationBar() {
	}

	private void debugExhausts() {
		if (DEBUG_EXHAUST && currentShip != null) {
			boolean ok = currentShip.getNumberOfLasers() < 2 || currentShip.getLaserX(0) > currentShip.getLaserX(1);
			centerTextWide(ok ? "Ok!" : "NOT OK!", 150, Assets.titleFont,
				ColorScheme.get(ok ? ColorScheme.COLOR_CONDITION_GREEN : ColorScheme.COLOR_CONDITION_RED));
		}
	}

	@Override
	public void present(float deltaTime) {
		final Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayShip();
		if (showLoadNewCommander) {
			g.drawText(L.string(R.string.cmdr_load_new_commander), 300, 1015, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		}
		centerTextWide(currentShip.getName(), 80, Assets.titleFont, ColorScheme.get(ColorScheme.COLOR_SHIP_TITLE));
		g.drawText(L.string(R.string.about_game_inspired_by, AliteConfig.GAME_NAME),
			1350, 1020, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.smallFont);
		g.drawText(L.string(R.string.about_elite_copyright), 1350, 1050, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.smallFont);
		debugExhausts();
		game.getTextureManager().setTexture(null);
		yesButton.render(g);
		noButton.render(g);
		tapToStartButton.render(g);
	}

	private void initDisplay(final Rect visibleArea) {
		float aspectRatio = visibleArea.width() / (float) visibleArea.height();
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, aspectRatio, 1.0f, 900000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glPushMatrix();
		GLES11.glMultMatrixf(skysphere.getMatrix(), 0);
		skysphere.render();
		GLES11.glPopMatrix();

		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glEnable(GLES11.GL_DEPTH_TEST);
		GLES11.glDepthFunc(GLES11.GL_LESS);
		GLES11.glClear(GLES11.GL_DEPTH_BUFFER_BIT);

		GLES11.glDisable(GLES11.GL_BLEND);
	}

	private void endDisplay(final Rect visibleArea) {
		GLES11.glDisable(GLES11.GL_DEPTH_TEST);
		GLES11.glDisable(GLES11.GL_TEXTURE_2D);

		setUpForDisplay(visibleArea);
	}

	private void displayShip() {
		Rect visibleArea = game.getGraphics().getVisibleArea();

		initDisplay(visibleArea);

		if (SHOW_DOCKING && coriolis != null) {
			GLES11.glPushMatrix();
			GLES11.glMultMatrixf(coriolis.getMatrix(), 0);
			coriolis.render();
			GLES11.glPopMatrix();
		}
		GLES11.glPushMatrix();
		GLES11.glMultMatrixf(currentShip.getMatrix(), 0);
		currentShip.render();
		GLES11.glPopMatrix();

		endDisplay(visibleArea);
	}

	@Override
	public void activate() {
		theChase.setLooping(true);
		theChase.play();
		initGl();
		skysphere = new SkySphereSpaceObject("skysphere", 8000.0f, 16, 16, "textures/star_map.png");
		currentShip = SpaceObjectFactory.getInstance().getNextObject(currentShip, selectionDirection, DEBUG_EXHAUST);
		selectionDirection = 1;
		currentShip.setPosition(0.0f, 0.0f, -currentShip.getMaxExtentWithoutExhaust() * 2.0f);
		if (SHOW_DOCKING) {
			coriolis = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Coriolis);
			coriolis.applyDeltaRotation(0, 0, 90);
			coriolis.setPosition(0, 0, -1300);
		}
		MathHelper.getRandomRotationAngles(targetDelta);
		timer.reset();
		displayMode = DisplayMode.DANCE;
		currentShip.setAIState(SpaceObjectAI.AI_STATE_GLOBAL, (Object[]) null);
		currentShip.setSpeed(-currentShip.getMaxSpeed());
		yesButton = Button.createGradientPictureButton(1000, 950, 110, 110, Assets.yesIcon)
			.setVisible(showLoadNewCommander);
		noButton = Button.createGradientPictureButton(1150, 950, 110, 110, Assets.noIcon)
			.setVisible(showLoadNewCommander);
		tapToStartButton = Button.createGradientRegularButton(530, 980, 800, 80, L.string(R.string.intro_btn_tap_to_start))
			.setTextColor(ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT))
			.setVisible(!showLoadNewCommander);
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		ScreenBuilder.writeString(dos, currentShip.getId());
	}

	@Override
	public void loadAssets() {
	}

	private void disposeMusic() {
		if (theChase != null) {
			theChase.stop();
			theChase.dispose();
			theChase = null;
		}
	}

	@Override
	public void pause() {
		super.pause();
		disposeMusic();
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeMusic();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.SHIP_INTRO_SCREEN;
	}

	private void zoomIn() {
		if (currentShip == null) {
			return;
		}
		float step = 2.0f * currentShip.getMaxExtentWithoutExhaust();
		float newZ = currentShip.getPosition().z + step;
		if (newZ >= -step) {
			newZ = -step;
			displayMode = DisplayMode.DANCE;
			timer.reset();
		}
		currentShip.setPosition(0, 0, newZ);
	}

	private void dance() {
		if (DANCE && danceTimer.hasPassedSeconds(4)) {
			MathHelper.getRandomRotationAngles(targetDelta);
		}
		if (timer.hasPassedSeconds(15) && !ONLY_CHANGE_SHIPS_AFTER_SWEEP) {
			displayMode = DisplayMode.ZOOM_OUT;
		}
	}

	private void zoomOut() {
		if (currentShip == null) {
			return;
		}
		float step = 2.0f *currentShip.getMaxExtentWithoutExhaust();
		float newZ = currentShip.getPosition().z - step;
		if (newZ <= -50 * step) {
			if (currentShip != null) {
				currentShip.dispose();
			}
			currentShip = getNextShip();
			newZ = -50 * currentShip.getMaxExtentWithoutExhaust();
			displayMode = DisplayMode.ZOOM_IN;
			timer.reset();
		}
		currentShip.setPosition(0, 0, newZ);
	}

	private SpaceObject getNextShip() {
		SpaceObject ao = SpaceObjectFactory.getInstance().getNextObject(currentShip, selectionDirection, DEBUG_EXHAUST);
		selectionDirection = 1;
		ao.setAIState(SpaceObjectAI.AI_STATE_GLOBAL, (Object[]) null);
		ao.setSpeed(-ao.getMaxSpeed());
		return ao;
	}
}
