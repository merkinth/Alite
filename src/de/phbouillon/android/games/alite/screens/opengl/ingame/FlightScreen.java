package de.phbouillon.android.games.alite.screens.opengl.ingame;

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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.GlScreen;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.opengl.IAdditionalGLParameterSetter;
import de.phbouillon.android.games.alite.screens.opengl.objects.AliteObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.PlanetSpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.SphericalSpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.AIState;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;
import de.phbouillon.android.games.alite.screens.opengl.sprites.AliteHud;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

public class FlightScreen extends GlScreen implements Serializable {
	private static final long serialVersionUID = -7879686326644011429L;

	static final Vector3f PLANET_POSITION              = new Vector3f(0.0f, 0.0f, 800000.0f);
	static final Vector3f SHIP_ENTRY_POSITION          = new Vector3f(0.0f, 0.0f, 400000.0f);
	static final float    SUN_SIZE                     = 60000.0f;

	private int windowWidth;
	private int windowHeight;
	private SphericalSpaceObject star;
	private PlanetSpaceObject    planet;

	private InGameManager inGame;
	private transient AliteScreen informationScreen;

	private final float[] lightAmbient  = { 0.5f, 0.5f, 0.7f, 1.0f };
	private final float[] lightDiffuse  = { 0.4f, 0.4f, 0.8f, 1.0f };
	private final float[] lightSpecular = { 0.5f, 0.5f, 1.0f, 1.0f };
	private final float[] lightPosition = { 100.0f, 30.0f, -10.0f, 1.0f };

	private final float[] sunLightAmbient  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightDiffuse  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightPosition = {0.0f, 0.0f, 0.0f, 1.0f};
	private final float[] sunLightEmission = {4.0f, 2.8f, 0.0f, 0.0f};
	private final float[] noEmission       = {0.0f, 0.0f, 0.0f, 0.0f};

	private final ArrayList <AliteObject> allObjects = new ArrayList<>();
	private SpaceObject spaceStation;
	private boolean resetSpaceStation = true;
	private boolean fromStation;
	private boolean witchSpace = false;
	private boolean paused = false;
	private final Timer timer = new Timer().setAutoResetWithImmediateAtFirstCall();
	private boolean handleUi = true;
	private boolean needsActivation = true;
	private transient boolean isSaving = false;
	private transient Timer timeToExitTimer;
	private transient Alite game;
	private transient IMethodHook postDockingHook;

	private Vector3f v0 = new Vector3f(0, 0, 0);
	private Vector3f v1 = new Vector3f(0, 0, 0);
	private Vector3f v2 = new Vector3f(0, 0, 0);

	public FlightScreen(Alite game) {
		this(game, true);
		try {
			AliteLog.d("[ALITE]", "Performing autosave. [Launch]");
			game.autoSave();
		} catch (IOException e) {
			AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
		}
		InGameManager.safeZoneViolated = false;
	}

	public FlightScreen(Alite game, boolean fromStation) {
		this.game = game;
		AliteLog.d("Flight Screen Constructor", "FSC -- fromStation == " + fromStation);
		SHIP_ENTRY_POSITION.z = Settings.enterInSafeZone ? 685000.0f : 400000.0f;
		this.fromStation = fromStation;
		AliteButtons.OVERRIDE_HYPERSPACE = false;
		AliteButtons.OVERRIDE_INFORMATION = false;
		AliteButtons.OVERRIDE_MISSILE = false;
		AliteButtons.OVERRIDE_LASER = false;
		AliteButtons.OVERRIDE_TORUS = false;
		InGameManager.OVERRIDE_SPEED = false;
		ObjectSpawnManager.SHUTTLES_ENABLED = true;
		ObjectSpawnManager.ASTEROIDS_ENABLED = true;
		ObjectSpawnManager.CONDITION_RED_OBJECTS_ENABLED = true;
		ObjectSpawnManager.THARGOIDS_ENABLED = true;
		ObjectSpawnManager.THARGONS_ENABLED = true;
		ObjectSpawnManager.TRADERS_ENABLED = true;
		ObjectSpawnManager.VIPERS_ENABLED = true;
	}

	public static FlightScreen createScreen(Alite alite, final DataInputStream dis) throws IOException, ClassNotFoundException {
		alite.loadCommander(dis);
		ObjectInputStream ois = new ObjectInputStream(dis);
		AliteLog.d("Initializing Flight Screen", "---------------------------------------------------------------");
		FlightScreen fs = (FlightScreen) ois.readObject();
		fs.needsActivation = false;
		fs.resetSpaceStation = false;
		return fs;
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		try {
			FlightScreen fs = createScreen(alite, dis);
			alite.setScreen(fs);
			AliteLog.d("Flight Screen created from state", "---------------------------------------------------------------");
			return true;
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		} catch (IOException e) {
			AliteLog.e("Error in Initializer", e.getMessage(), e);
		}
		return false;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "FlightScreen.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "FlightScreen.readObject I");
			game = Alite.get();
			setPause(true);
			AliteLog.d("readObject", "FlightScreen.readObject II");
			isSaving = false;
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	public void enterWitchSpace() {
		witchSpace = true;
	}

	public void togglePause() {
		if (inGame != null) {
			if (timer.hasPassedSeconds(1)) {
				paused = !paused;
				inGame.setPaused(paused);
			}
		}
	}

	public void setPause(boolean b) {
		paused = b;
		if (inGame != null) {
			inGame.setPaused(b);
		}
		if (b) {
			timer.reset();
		}
	}

	@Override
	public void onActivation() {
		if (!needsActivation) {
			needsActivation = true;
			Rect visibleArea = game.getGraphics().getVisibleArea();
			windowWidth = visibleArea.width();
			windowHeight = visibleArea.height();
			initializeGl(visibleArea);
			setPause(true);
			return;
		}
		Rect visibleArea = game.getGraphics().getVisibleArea();
		windowWidth = visibleArea.width();
		windowHeight = visibleArea.height();
		initializeGl(visibleArea);

		inGame = new InGameManager(new AliteHud(speed -> inGame.getShip().getSpeed()), "textures/star_map.png", lightPosition, fromStation, true);
		PlayerCobra cobra = game.getCobra();
		cobra.setMissileTargetting(false);
		cobra.setMissileLocked(false);
		cobra.setLaserTemperature(0);
		cobra.setCabinTemperature(0);
		cobra.setAltitude(fromStation ? PlayerCobra.MAX_ALTITUDE / 2 : PlayerCobra.MAX_ALTITUDE);
		cobra.setEnergy(PlayerCobra.MAX_ENERGY);
		cobra.setFrontShield((int) PlayerCobra.MAX_SHIELD);
		cobra.setRearShield((int) PlayerCobra.MAX_SHIELD);
		if (fromStation) {
			inGame.getShip().applyDeltaRotation(180.0f, 0, 0);
		}
		inGame.getShip().setSpeed(-140.0f);
		initializeObjects();
		if (witchSpace) {
			inGame.enterWitchSpace();
		} else {
			inGame.preMissionCheck();
		}
	}

	public void setForwardView() {
		inGame.forceForwardView();
	}

	public void setInformationScreen(AliteScreen newInformationScreen) {
		if (informationScreen != null) {
			informationScreen.dispose();
		}
		informationScreen = newInformationScreen;
		if (informationScreen == null) {
			Rect visibleArea = game.getGraphics().getVisibleArea();
			windowWidth = visibleArea.width();
			windowHeight = visibleArea.height();
			initializeGl(visibleArea);
			inGame.resetHud();
		}
		game.getNavigationBar().setFlightMode(informationScreen != null);
	}

	public AliteScreen getInformationScreen() {
		return informationScreen;
	}

	public InGameManager getInGameManager() {
		return inGame;
	}

	private void initializeObjects() {
		allObjects.clear();
		SystemData currentSystem = game.getPlayer().getCurrentSystem();

		int starTexture = currentSystem == null ? 0 : currentSystem.getStarTexture();
		String starTextureName;
		float sunSize;
        if (starTexture == 22) {
            starTextureName = "textures/stars/dwarf/b.png";
            sunLightEmission[0] = 0.5f; sunLightEmission[1] = 0.0f; sunLightEmission[2] = 0.0f; sunLightEmission[3] = 1.0f;
            sunSize = 5000.0f;
        } else if (starTexture == 21) {
            starTextureName = "textures/stars/dwarf/a.png";
            sunLightEmission[0] = 0.5f; sunLightEmission[1] = 0.0f; sunLightEmission[2] = 0.5f; sunLightEmission[3] = 1.0f;
            sunSize = 10000.0f;
        } else {
            starTextureName = "textures/stars/" + ("123".charAt(starTexture / 7) + "/" + "obafgkm".charAt(starTexture % 7)) + ".png";
            sunSize = SUN_SIZE - (starTexture % 7) * 7000.0f;
            int starType = starTexture % 7;
            switch (starType) {
            	case 0: sunLightEmission[0] = 0.5f; sunLightEmission[1] = 0.5f; sunLightEmission[2] = 1.0f; sunLightEmission[3] = 1.0f; break;
            	case 1: sunLightEmission[0] = 0.3f; sunLightEmission[1] = 0.3f; sunLightEmission[2] = 1.0f; sunLightEmission[3] = 1.0f; break;
            	case 2: sunLightEmission[0] = 0.1f; sunLightEmission[1] = 0.1f; sunLightEmission[2] = 1.0f; sunLightEmission[3] = 1.0f; break;
            	case 3: sunLightEmission[0] = 0.8f; sunLightEmission[1] = 0.8f; sunLightEmission[2] = 1.0f; sunLightEmission[3] = 1.0f; break;
            	case 4: sunLightEmission[0] = 0.8f; sunLightEmission[1] = 0.8f; sunLightEmission[2] = 0.5f; sunLightEmission[3] = 1.0f; break;
            	case 5: sunLightEmission[0] = 0.8f; sunLightEmission[1] = 0.5f; sunLightEmission[2] = 0.3f; sunLightEmission[3] = 1.0f; break;
            	case 6: sunLightEmission[0] = 1.0f; sunLightEmission[1] = 0.5f; sunLightEmission[2] = 0.5f; sunLightEmission[3] = 1.0f; break;
            }
        }
		star = new SphericalSpaceObject("Sun", sunSize, starTextureName);
		star.setVisibleOnHud(false);
		star.setAdditionalGLParameters(new IAdditionalGLParameterSetter(){
			private static final long serialVersionUID = -7931217736505566905L;

			@Override
			public void setUp() {
				GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_EMISSION, sunLightEmission, 0);
			}

			@Override
			public void tearDown() {
				GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_EMISSION, noEmission, 0);
			}
		});

		SphericalSpaceObject starGlow = new SphericalSpaceObject("Glow", sunSize + 60.0f, "textures/glow_mask2.png");
		starGlow.setDepthTest(false);
		starGlow.setVisibleOnHud(false);
		starGlow.setAdditionalGLParameters(new IAdditionalGLParameterSetter() {
			private static final long serialVersionUID = -7651239619882350365L;

			@Override
			public void setUp() {
				GLES11.glDisable(GLES11.GL_CULL_FACE);
			    GLES11.glEnable(GLES11.GL_BLEND);
				GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE);
			}

			@Override
			public void tearDown() {
				GLES11.glEnable(GLES11.GL_CULL_FACE);
				GLES11.glDisable(GLES11.GL_BLEND);
			}
		});

		planet = new PlanetSpaceObject(currentSystem, false);
		planet.applyDeltaRotation(23, 0, 14);
		planet.setPosition(PLANET_POSITION);

		spaceStation = SpaceObjectFactory.getInstance().getRandomObjectByType(
			currentSystem == null || currentSystem.getTechLevel() <= 9 ? ObjectType.Coriolis :
			currentSystem.getTechLevel() > 13 ? ObjectType.Icosahedron : ObjectType.Dodecahedron);
		spaceStation.setPosition(inGame.getSystemStationPosition());
		if (fromStation) {
			spaceStation.setIdentified();
		} else {
			if (game.getPlayer().getCurrentSystem() == SystemData.RAXXLA_SYSTEM) {
				initializeGameOverParade();
			}
		}

		allObjects.add(star);
		allObjects.add(starGlow);
		allObjects.add(planet);
		allObjects.add(spaceStation);

		inGame.setSun(star);
		inGame.setSunGlow(starGlow);
		inGame.setPlanet(planet);
		inGame.setStation(spaceStation);
		inGame.initializeViperAction();
	}

	private void initializeGameOverParade() {
		for (int i = 0; i < 20; i++) {
			allObjects.add(SpaceObjectFactory.getInstance().getObjectById("tie_fighter"));
		}
	}

	public void initializeGl(final Rect visibleArea) {
		float ratio = windowWidth / (float) windowHeight;
		GlUtils.setViewport(visibleArea);
		GLES11.glDisable(GLES11.GL_FOG);
		GLES11.glPointSize(1.0f);
        GLES11.glLineWidth(1.0f);

        GLES11.glTexEnvf(GLES11.GL_TEXTURE_ENV, GLES11.GL_TEXTURE_ENV_MODE, GLES11.GL_MODULATE);

	    GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
        GLES11.glDisable(GLES11.GL_BLEND);

		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, ratio, 1.0f, 900000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES11.glShadeModel(GLES11.GL_SMOOTH);

		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_AMBIENT, lightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_DIFFUSE, lightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_SPECULAR, lightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_POSITION, lightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT1);

		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_AMBIENT, sunLightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_DIFFUSE, sunLightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_SPECULAR, sunLightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_POSITION, sunLightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT2);

		GLES11.glEnable(GLES11.GL_LIGHTING);

		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
		GLES11.glHint(GLES11.GL_PERSPECTIVE_CORRECTION_HINT, GLES11.GL_NICEST);
		GLES11.glHint(GLES11.GL_POLYGON_SMOOTH_HINT, GLES11.GL_NICEST);
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
	}

	private void performResetSpaceStation() {
		spaceStation.orientTowards(PLANET_POSITION.x, PLANET_POSITION.y, PLANET_POSITION.z, 0, 1, 0);
		spaceStation.applyDeltaRotation(0, 180.0f, 0);

	    if (fromStation) {
		    inGame.getShip().orientTowards(PLANET_POSITION.x, PLANET_POSITION.y, PLANET_POSITION.z, 0, 1, 0);
	    	float fx = spaceStation.getForwardVector().x * 1800.0f;
	    	float fy = spaceStation.getForwardVector().y * 1800.0f;
	    	float fz = spaceStation.getForwardVector().z * 1800.0f;
	    	inGame.getShip().setPosition(spaceStation.getPosition().x + fx, spaceStation.getPosition().y + fy, spaceStation.getPosition().z + fz);
	    } else {
	    	inGame.getShip().applyDeltaRotation(180.0f, 0, 0);
	    	inGame.getShip().setPosition(SHIP_ENTRY_POSITION);
	    	int count = 0;
	    	inGame.getShip().getPosition().copy(v0);
	    	inGame.getShip().getRightVector().copy(v1);
	    	v1.scale(5000);
	    	for (AliteObject ao: allObjects) {
	    		if (ao instanceof SpaceObject && ((SpaceObject)ao).getType() == ObjectType.TieFighter) {
	    			SpaceObject tie = (SpaceObject) ao;
	    			if (count % 2 == 0) {
	    				v0.add(v1, v2);
	    				tie.setPosition(v2);
						tie.orientTowards(inGame.getShip(), 0);
						tie.setAIState(AIState.IDLE, (Object[]) null);
	    				count++;
	    			} else {
	    				v0.sub(v1, v2);
	    				tie.setPosition(v2);
	    				tie.orientTowards(inGame.getShip(), 0);
	    				tie.setAIState(AIState.IDLE, (Object[]) null);
	    				inGame.getStation().getPosition().sub(inGame.getShip().getPosition(), v2);
	    				v2.scale(0.1f);
	    				v0.add(v2);
	    				count++;
	    			}
	    		}
	    	}
	    	inGame.getShip().applyDeltaRotation((float) Math.random() * 360.0f, (float) Math.random() * 360.0f, (float) Math.random() * 360.0f);
	    	inGame.getShip().assertOrthoNormal();
	    }
	    inGame.initStarDust();
		resetSpaceStation = false;
	}

	public void setHandleUI(boolean b) {
		handleUi = b;
	}

	@Override
	public void performUpdate(float deltaTime) {
		if (isSaving) {
			return;
		}
		try {
			if (paused) {
				for (TouchEvent event: game.getInput().getTouchEvents()) {
					if (event.type == TouchEvent.TOUCH_UP) {
						togglePause();
					}
				}
				return;
			}
			if (isDisposed() || inGame == null) {
				return;
			}
			if (inGame.isPlayerAlive()) {
				int tf = game.getTimeFactor();
				while (--tf >= 0 && inGame.isPlayerAlive()) {
					inGame.performUpdate(deltaTime, allObjects);
				}
			} else {
				if (timeToExitTimer == null) {
					timeToExitTimer = new Timer();
				}
				if (timeToExitTimer.hasPassedSeconds(10)) {
					// Safeguard for endless loops...
					inGame.terminateToTitleScreen();
				}
				inGame.performUpdate(deltaTime, allObjects);
			}
			if (informationScreen != null) {
				informationScreen.update(deltaTime);
			}
			Screen newScreen = inGame.getNewScreen();
			if (newScreen == null && informationScreen == null && handleUi) {
				for (TouchEvent event: game.getInput().getTouchEvents()) {
					if (inGame.handleUI(event)) {
						newScreen = inGame.getNewScreen();
					}
				}
			} else {
				game.getInput().getTouchEvents();
			}
			if (newScreen != null) {
				performScreenChange(newScreen);
			} else {
				deltaTime *= game.getTimeFactor();
				if (star != null) {
					star.applyDeltaRotation(0.0f, (float) Math.toDegrees(0.02f * deltaTime), 0.0f);
				}
				if (planet != null) {
					planet.applyDeltaRotation(0.0f, (float) Math.toDegrees(0.015f * deltaTime), 0.0f);
				}

				if (spaceStation != null) {
					if (resetSpaceStation) {
						performResetSpaceStation();
					}
					spaceStation.applyDeltaRotation(0.0f, 0.0f,
						(float) Math.toDegrees(spaceStation.getSpaceStationRotationSpeed() * deltaTime));
				}
			}
		} catch (NullPointerException e) {
			if (inGame.isDestroyed()) {
				// Ok, ignore it.
				return;
			}
			throw e;
		}
	}

	private void performScreenChange(Screen newScreen) {
		Screen oldScreen = game.getCurrentScreen();
		oldScreen.dispose();
		game.setScreen(newScreen);
		postScreenChange();
		oldScreen = null;
	}

	@Override
	public void performPresent(float deltaTime) {
		try {
			if (isDisposed()) {
				return;
			}
			if (isSaving) {
				if (inGame != null) {
					inGame.renderScroller(deltaTime);
				}
				return;
			}
			if (informationScreen != null) {
				informationScreen.present(deltaTime);
				if (inGame != null) {
					inGame.calcAllObjects(deltaTime, allObjects);
				}
			} else {
				if (inGame != null) {
					inGame.render(deltaTime, allObjects);
				}
			}
		} catch (NullPointerException e) {
			if (inGame.isDestroyed()) {
				// Ok, ignore it.
				return;
			}
			throw e;
		}
	}

	public void setPostDockingHook(IMethodHook hook) {
		postDockingHook = hook;
	}

	public IMethodHook getPostDockingHook() {
		return postDockingHook;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (postDockingHook != null) {
			postDockingHook.execute(0);
		}
		if (inGame != null) {
			inGame.destroy();
		}
		if (planet != null) {
			planet.dispose();
			planet = null;
		}
		if (spaceStation != null) {
			spaceStation.dispose();
			spaceStation = null;
		}
	}

	@Override
	public void postScreenChange() {
		inGame = null;
	}

	@Override
	public void loadAssets() {
	}

	@Override
	public void pause() {
		super.pause();
		if (inGame != null) {
			inGame.destroy();
		}
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
		if (informationScreen != null) {
			informationScreen.postPresent(deltaTime);
		}
	}

	@Override
	public void renderNavigationBar() {
		super.renderNavigationBar();
		if (informationScreen != null) {
			informationScreen.renderNavigationBar();
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.FLIGHT_SCREEN;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		isSaving = true;
		setPause(true);
		game.saveCommander(dos);
		inGame.clearObjectTransformations();
		ObjectOutputStream oos = new ObjectOutputStream(dos);
		oos.writeObject(this);
	}

	public AliteObject findObjectById(String id) {
		for (AliteObject ao: allObjects) {
			if (id.equals(ao.getId())) {
				return ao;
			}
		}
		return null;
	}
}
