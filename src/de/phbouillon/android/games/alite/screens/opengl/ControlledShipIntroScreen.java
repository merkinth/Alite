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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.GlScreen;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.objects.AliteObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Adder;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Anaconda;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.AspMkII;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Asteroid1;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Asteroid2;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.BoaClassCruiser;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CargoCanister;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CobraMkI;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CobraMkIII;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Constrictor;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Coriolis;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Dodec;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.EscapeCapsule;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.FerDeLance;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Gecko;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Icosaeder;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Krait;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Mamba;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Missile;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.MorayStarBoat;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.OrbitShuttle;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Python;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Sidewinder;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Thargoid;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Thargon;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Transporter;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Viper;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.WolfMkII;
import de.phbouillon.android.games.alite.screens.opengl.sprites.AliteHud;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class ControlledShipIntroScreen extends GlScreen {
	enum DisplayMode {
		ZOOM_IN,
		CONTROL,
		ZOOM_OUT;
	}

	private static final float START_Z = -10000.0f;

	private int windowWidth;
	private int windowHeight;
	private final List <AliteObject> allObjects = new ArrayList<>();
	private final InGameManager inGame;
	private final Timer timer = new Timer().setAutoReset();
	private int currentShipIndex = 0;

	private final float[] lightAmbient  = { 0.5f, 0.5f, 0.7f, 1.0f };
	private final float[] lightDiffuse  = { 0.4f, 0.4f, 0.8f, 1.0f };
	private final float[] lightSpecular = { 0.5f, 0.5f, 1.0f, 1.0f };
	private final float[] lightPosition = { 100.0f, 30.0f, -10.0f, 1.0f };

	private final float[] sunLightAmbient  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightDiffuse  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightPosition = {0.0f, 0.0f, 0.0f, 1.0f};

	private DisplayMode displayMode = DisplayMode.ZOOM_IN;
	private float[] matrix = null;
	private Alite game;

	public ControlledShipIntroScreen(Alite game) {
		this.game = game;
		AliteLog.d("Ship Intro Screen", "Constructor. Now loading background image... glError: " + GLES11.glGetError());
		inGame = new InGameManager(game, null, "textures/purple_screen.png", lightPosition, false, false);
		AliteLog.d("Ship Intro Screen", "Constructor done. Background image should have been loaded. glError: " + GLES11.glGetError());
	}

	@Override
	public void onActivation() {
		AliteLog.d("Ship Intro Screen", "On Activation. glError: " + GLES11.glGetError());
		Rect visibleArea = game.getGraphics().getVisibleArea();
		windowWidth = visibleArea.width();
		windowHeight = visibleArea.height();
		initializeGl(visibleArea);

		AliteLog.d("Ship Intro Screen", "On Activation. After init. glError: " + GLES11.glGetError());
		AliteHud.ct = new DefaultCoordinateTransformer(game);
		allObjects.clear();
		SpaceObject cobra = new CargoCanister(game);
		cobra.setPosition(0.0f, 0.0f, START_Z);
		inGame.getShip().setPosition(0.0f, 0.0f, 0.0f);
		allObjects.add(cobra);
		displayMode = DisplayMode.ZOOM_IN;
		AliteLog.d("Ship Intro Screen", "On Activation done. glError: " + GLES11.glGetError());
	}

	private void initializeGl(final Rect visibleArea) {
		AliteLog.d("Ship Intro Screen", "Initialize GL. glError: " + GLES11.glGetError());

		float ratio = windowWidth / (float) windowHeight;
		GlUtils.setViewport(visibleArea);
		GLES11.glDisable(GLES11.GL_FOG);
		GLES11.glPointSize(1.0f);
        GLES11.glLineWidth(1.0f);

        AliteLog.d("Ship Intro Screen", "Initialize GL lineWidth. glError: " + GLES11.glGetError());
        GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
        GLES11.glDisable(GLES11.GL_BLEND);
        AliteLog.d("SIS", "Blending: " + GLES11.glGetError());

		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, ratio, 1.0f, 900000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		AliteLog.d("SIS", "Matrix Setup: " + GLES11.glGetError());

		GLES11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES11.glShadeModel(GLES11.GL_SMOOTH);
		AliteLog.d("SIS", "Clear Color & Shading Model: " + GLES11.glGetError());

		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_AMBIENT, lightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_DIFFUSE, lightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_SPECULAR, lightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_POSITION, lightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT1);
		AliteLog.d("SIS", "Defined Light 1: " + GLES11.glGetError());

		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_AMBIENT, sunLightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_DIFFUSE, sunLightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_SPECULAR, sunLightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_POSITION, sunLightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT2);
		AliteLog.d("SIS", "Defined Light 2: " + GLES11.glGetError());

		GLES11.glEnable(GLES11.GL_LIGHTING);
		AliteLog.d("SIS", "After Enable Lighting: " + GLES11.glGetError());

	    AliteLog.d("SIS", "After Lighting: " + GLES11.glGetError());

		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glEnable(GLES11.GL_CULL_FACE);


		AliteLog.d("SIS", "End of Init: " + GLES11.glGetError());
	}

	private void zoomIn(float deltaTime) {
		if (allObjects.isEmpty()) {
			return;
		}
		Vector3f pos = allObjects.get(0).getPosition();
		float newZ = pos.z + deltaTime * 5000.0f;
		if (newZ >= -((SpaceObject) allObjects.get(0)).getMaxExtent() * 2.2f) {
			newZ = -((SpaceObject) allObjects.get(0)).getMaxExtent() * 2.2f;
			displayMode = DisplayMode.CONTROL;
		}
		allObjects.get(0).setPosition(0, 0, newZ);
	}

	private void zoomOut(float deltaTime) {
		if (allObjects.isEmpty()) {
			return;
		}
		Vector3f pos = allObjects.get(0).getPosition();
		float newZ = pos.z - deltaTime * 5000.0f;
		if (newZ <= START_Z) {
			newZ = START_Z;
			((SpaceObject) allObjects.get(0)).dispose();
			allObjects.clear();
			allObjects.add(getNextShip());
			if (matrix != null) {
				allObjects.get(0).setMatrix(matrix);
			}
			displayMode = DisplayMode.ZOOM_IN;
		}
		allObjects.get(0).setPosition(0, 0, newZ);
	}

	@Override
	public void performUpdate(float deltaTime) {
		switch (displayMode) {
			case ZOOM_IN:  zoomIn(deltaTime);  break;
			case ZOOM_OUT: zoomOut(deltaTime); break;
			case CONTROL: break;
		}
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		for (TouchEvent event: touchEvents) {
			if (event.type == TouchEvent.TOUCH_SWEEP) {
				float[] temp = allObjects.get(0).getMatrix();
				if (matrix == null) {
					matrix = new float[16];
				}
				System.arraycopy(temp, 0, matrix, 0, 16);
				if (displayMode == DisplayMode.CONTROL) {
					displayMode = DisplayMode.ZOOM_OUT;
				}
				// Make sure that a sweep event is correctly processed and not
				// mistaken for a touch_up
				return;
			}
		}
		if (!timer.hasPassedSeconds(1)) {
			return;
		}
		for (TouchEvent event: touchEvents) {
			if (event.type == TouchEvent.TOUCH_UP) {
				if (event.x < 640) {
					if (event.y < 540) {
						allObjects.get(0).applyDeltaRotation(-2.0f, 0, 0);
					} else {
						allObjects.get(0).applyDeltaRotation(2.0f, 0, 0);
					}
				} else if (event.x < 1280) {
					if (event.y < 540) {
						allObjects.get(0).applyDeltaRotation(0, -2.0f, 0);
					} else {
						allObjects.get(0).applyDeltaRotation(0, 2.0f, 0);
					}
				} else {
					if (event.y < 540) {
						allObjects.get(0).applyDeltaRotation(0, 0, -2.0f);
					} else {
						allObjects.get(0).applyDeltaRotation(0, 0, 2.0f);
					}
				}
			}
		}
	}

	private SpaceObject getNextShip() {
		currentShipIndex++;
		if (currentShipIndex == 28) {
			currentShipIndex = 0;
		}
		switch (currentShipIndex) {
			case  0: return new CobraMkIII(game);
			case  1: return new Krait(game);
			case  2: return new Thargoid(game);
			case  3: return new BoaClassCruiser(game);
			case  4: return new Gecko(game);
			case  5: return new MorayStarBoat(game);
			case  6: return new Adder(game);
			case  7: return new Mamba(game);
			case  8: return new Viper(game);
			case  9: return new FerDeLance(game);
			case 10: return new CobraMkI(game);
			case 11: return new Python(game);
			case 12: return new Anaconda(game);
			case 13: return new AspMkII(game);
			case 14: return new Sidewinder(game);
			case 15: return new WolfMkII(game);
			case 16: return new OrbitShuttle(game);
			case 17: return new Transporter(game);
			case 18: return new Thargon(game);
			case 19: return new Constrictor(game);
			case 20: return new Asteroid1(game);
			case 21: return new Asteroid2(game);
			case 22: return new Coriolis(game);
			case 23: return new Dodec(game);
			case 24: return new Icosaeder(game);
			case 25: return new CargoCanister(game);
			case 26: return new EscapeCapsule(game);
			case 27: return new Missile(game);
		}
		return new CobraMkIII(game);
	}

	@Override
	public void performPresent(float deltaTime) {
		inGame.render(deltaTime, allObjects);
		if (!allObjects.isEmpty()) {
			GLES11.glMatrixMode(GLES11.GL_PROJECTION);
			GLES11.glPushMatrix();
			GLES11.glLoadIdentity();
			Rect visibleArea = game.getGraphics().getVisibleArea();
			GlUtils.ortho(game, visibleArea);
			GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
			GLES11.glLoadIdentity();
			GLES11.glColor4f(0.937f, 0.396f, 0.0f, 1.0f);
			GLES11.glMatrixMode(GLES11.GL_PROJECTION);
			GLES11.glPopMatrix();
			GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void loadAssets() {
	}

	@Override
	public void pause() {
		super.pause();
		inGame.destroy();
	}

	@Override
	public void resume() {
		super.resume();
		Rect visibleArea = game.getGraphics().getVisibleArea();
		initializeGl(visibleArea);
		game.getTextureManager().reloadAllTextures();
	}

	@Override
	public void postPresent(float deltaTime) {
	}

	@Override
	public int getScreenCode() {
		return -1;
	}
}
