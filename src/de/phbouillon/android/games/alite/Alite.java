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

import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLES11;
import android.os.Bundle;
import android.view.Menu;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.io.FileUtils;
import de.phbouillon.android.games.alite.model.*;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.*;
import de.phbouillon.android.games.alite.screens.NavigationBar;
import de.phbouillon.android.games.alite.screens.canvas.LoadingScreen;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutorialScreen;
import de.phbouillon.android.games.alite.screens.opengl.TextureManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.LaserManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.SpaceObjectTraverser;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

import java.io.*;

public class Alite extends AndroidGame {
	public static final String LOG_IS_INITIALIZED = "logIsInitialized";

	public static int NAVIGATION_BAR_LAUNCH;
	public static int NAVIGATION_BAR_STATUS;
	public static int NAVIGATION_BAR_BUY;
	public static int NAVIGATION_BAR_INVENTORY;
	public static int NAVIGATION_BAR_EQUIP;
	public static int NAVIGATION_BAR_GALAXY;
	public static int NAVIGATION_BAR_LOCAL;
	public static int NAVIGATION_BAR_PLANET;
	public static int NAVIGATION_BAR_DISK;
	public static int NAVIGATION_BAR_OPTIONS;
	public static int NAVIGATION_BAR_LIBRARY;
	public static int NAVIGATION_BAR_ACADEMY;
	public static int NAVIGATION_BAR_HACKER;
	public static int NAVIGATION_BAR_QUIT;

	private Player player;
	private GalaxyGenerator generator;
	private Timer clock;
	private long elapsedTime;
	private NavigationBar navigationBar;
	private final FileUtils fileUtils;
	private LaserManager laserManager;
	private static Alite alite;
	private boolean saving = false;
	private boolean isHackerActive;
	private InGameManager inGame;

	public Alite() {
		super(AliteConfig.SCREEN_WIDTH, AliteConfig.SCREEN_HEIGHT);
		alite = this;
		fileUtils = new FileUtils();
	}

	@Override
	public Screen getStartScreen() {
		return new LoadingScreen();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Settings.setImmersion(getCurrentView());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent == null || !intent.getBooleanExtra(LOG_IS_INITIALIZED, false)) {
			AliteLog.initialize(getFileIO());
		}
		AliteLog.d("Alite.onCreate", "onCreate begin");
		final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
			AliteLog.e("Uncaught Exception (Alite)", "Message: " + (paramThrowable == null ? "<null>" : paramThrowable.getMessage()), paramThrowable);
			if (oldHandler != null) {
				oldHandler.uncaughtException(paramThread, paramThrowable);
			} else {
				System.exit(2);
			}
		});
		AliteLog.d("Alite.onCreate", "initializing");
		initialize();
		clock = new Timer();
		registerMissions();
		Settings.setOrientation(this);
		AliteLog.d("Alite.onCreate", "onCreate end");
	}

	public void initialize() {
		if (generator == null) {
			generator = new GalaxyGenerator();
			generator.buildGalaxy(1);
		}
		if (player == null) {
			player = new Player();
		}
	}

	private void registerMissions() {
		MissionManager.getInstance().clear();
		MissionManager.getInstance().register(new ConstrictorMission());
		MissionManager.getInstance().register(new ThargoidDocumentsMission());
		MissionManager.getInstance().register(new SupernovaMission());
		MissionManager.getInstance().register(new CougarMission());
		MissionManager.getInstance().register(new ThargoidStationMission());
		MissionManager.getInstance().register(new EndMission());
	}

	public void activateHacker() {
		if (NAVIGATION_BAR_HACKER != 0) {
			isHackerActive = true;
			navigationBar.setVisible(NAVIGATION_BAR_HACKER, true);
		}
	}

	public boolean isHackerActive() {
		return isHackerActive;
	}

	public void setGameTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
		clock.reset();
	}

	public long getGameTime() {
		return elapsedTime + clock.getPassedNanos();
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerCobra getCobra() {
		return player.getCobra();
	}

	public void setInGame(InGameManager inGame) {
		this.inGame = inGame;
	}

	public InGameManager getInGame() {
		return inGame;
	}

	public GalaxyGenerator getGenerator() {
		return generator;
	}

	public NavigationBar getNavigationBar() {
		return navigationBar;
	}

	public boolean isHyperspaceTargetValid() {
		return player.getHyperspaceSystem() != null && player.getHyperspaceSystem() != player.getCurrentSystem() &&
			(Settings.unlimitedFuel || player.getCobra().getFuel() > 0 && player.getCobra().getFuel() >= player.computeDistance());
	}

	public void performHyperspaceJump() {
		InGameManager.safeZoneViolated = false;
		if (player.getActiveMissions().isEmpty()) {
			player.increaseJumpCounter();
		}
		boolean willEnterWitchSpace = player.getRating().ordinal() > Rating.POOR.ordinal() && Math.random() <= 0.02;
		for (Mission mission: player.getActiveMissions()) {
			willEnterWitchSpace |= mission.willEnterWitchSpace();
		}
		if (player.getCobra().getPitch() <= -2.0f && player.getCobra().getRoll() <= -2.0f) {
			willEnterWitchSpace = true;
		}
		int distance = player.computeDistance();
		if (willEnterWitchSpace) {
			distance >>= 1;
			int x = player.getCurrentSystem() == null ? player.getPosition().x : player.getCurrentSystem().getX();
			int y = player.getCurrentSystem() == null ? player.getPosition().y : player.getCurrentSystem().getY();
			int dx = player.getHyperspaceSystem().getX() - x;
			int dy = player.getHyperspaceSystem().getY() - y;
			int nx = x + (dx >> 1);
			int ny = y + (dy >> 1);
			player.setPosition(nx, ny);
			player.setCurrentSystem(null);
		} else {
			player.setCurrentSystem(player.getHyperspaceSystem());
		}
		player.getCobra().setFuel(player.getCobra().getFuel() - (distance == 0 ? 1 : distance));
		FlightScreen fs = new FlightScreen(false);
		if (willEnterWitchSpace) {
			fs.enterWitchSpace();
		}
		setScreen(fs);
		GLES11.glMatrixMode(GLES11.GL_TEXTURE);
		GLES11.glLoadIdentity();
		navigationBar.setActiveIndex(NAVIGATION_BAR_STATUS);
		player.setLegalValue(player.getLegalValue() >> 1);
	}

	public boolean isRaxxlaVisible() {
		return generator.getCurrentGalaxy() == SystemData.RAXXLA_GALAXY && player.getRating() == Rating.ELITE;
	}

	public void performIntergalacticJump(int galacticNumber) {
		InGameManager.safeZoneViolated = false;
		if (player.getActiveMissions().isEmpty()) {
			player.increaseIntergalacticJumpCounter();
			if (player.getIntergalacticJumpCounter() == 1) {
				// Mimic Amiga behavior: Mission starts after 1 intergal hyperjump
				// and 63 other jumps (intergal or intragal).
				player.resetJumpCounter();
			}
		}
		if (galacticNumber > Settings.maxGalaxies || galacticNumber < 1) {
			galacticNumber = 1;
		}
		generator.buildGalaxy(galacticNumber);
		player.setCurrentSystem(generator.getSystem(player.getCurrentSystem().getIndex()));
		player.setHyperspaceSystem(player.getCurrentSystem());
		player.getCobra().removeEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE));
		setScreen(new FlightScreen(false));
		GLES11.glMatrixMode(GLES11.GL_TEXTURE);
		GLES11.glLoadIdentity();
		navigationBar.setActiveIndex(NAVIGATION_BAR_STATUS);
	}

	@Override
	protected void saveState(Screen screen) {
		AliteLog.d("Saving state", "Saving state. Screen = " + (screen == null ? "<null>" : screen.getClass().getName()));
		int screenCode = screen == null ? -1 : screen.getScreenCode();
		if (screenCode == -1) {
			getFileIO().deleteFile(AliteStartManager.ALITE_STATE_FILE);
			AliteLog.d("Saving state", "Saving state could not identify current screen, hence the state file was deleted.");
			return;
		}
		try (OutputStream stateFile = getFileIO().writeFile(AliteStartManager.ALITE_STATE_FILE)) {
			stateFile.write(screenCode);
			screen.saveScreenState(new DataOutputStream(stateFile));
			AliteLog.d("Saving state", "Saving state completed successfully.");
		} catch (IOException e) {
			AliteLog.e("Saving state", "Error during saving state.", e);
			e.printStackTrace();
		}
	}

	public final boolean readState() throws IOException {
		byte[] state = getFileIO().readFileContents(AliteStartManager.ALITE_STATE_FILE);
		AliteLog.d("Reading state", "State == " + (state == null || state.length == 0 ? "" : state[0]));
		return state != null && state.length > 0 && ScreenBuilder.createScreen(state);
	}

	@Override
	public void onPause() {
		try {
			setSaving(true);
			super.onPause();
		} finally {
			setSaving(false);
		}
		if (getInput() != null) {
			getInput().dispose();
		}
		if (textureManager != null) {
			textureManager.freeAllTextures();
		}
		AliteLog.d("Alite.onPause", "onPause end");
	}

	@Override
	public void onStop() {
		AliteLog.d("Alite.OnStop", "Stopping Alite...");
		getInput().dispose();
		if (getCurrentScreen() != null && !(getCurrentScreen() instanceof FlightScreen)) {
			try {
				AliteLog.d("[ALITE]", "Performing autosave.");
				autoSave();
			} catch (IOException e) {
				AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
			}
		}
		while (saving) {
			AliteLog.d("OnStop", "Still saving...");
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) { }
		}
		AliteLog.e("Alite.OnStop", "Stopping Alite Done.");
		super.onStop();
	}

	private synchronized void setSaving(boolean b) {
		saving = b;
	}

	@Override
	public void onResume() {
		AliteLog.d("Alite.onResume", "onResume begin");
		if (textureManager != null) {
			textureManager.clear();
		}
		createInputIfNecessary();
		super.onResume();
		AliteLog.d("Alite.onResume", "onResume end");
	}

	private void createNavigationBar() {
		// if navigation target is defined package root of screens is screens.canvas
		navigationBar = new NavigationBar();
		NAVIGATION_BAR_LAUNCH = navigationBar.add(L.string(R.string.navbar_launch), Assets.launchIcon, "opengl.ingame.FlightScreen");
		NAVIGATION_BAR_STATUS = navigationBar.add(L.string(R.string.navbar_status), Assets.statusIcon, "canvas.StatusScreen");
		NAVIGATION_BAR_BUY = navigationBar.add(L.string(R.string.navbar_buy), Assets.buyIcon, "canvas.BuyScreen");
		NAVIGATION_BAR_INVENTORY = navigationBar.add(L.string(R.string.navbar_inventory), Assets.inventoryIcon, "canvas.InventoryScreen");
		NAVIGATION_BAR_EQUIP = navigationBar.add(L.string(R.string.navbar_equip), Assets.equipIcon, "canvas.EquipmentScreen");
		NAVIGATION_BAR_GALAXY = navigationBar.add(L.string(R.string.navbar_galaxy), Assets.galaxyIcon, "canvas.GalaxyScreen");
		NAVIGATION_BAR_LOCAL = navigationBar.add(L.string(R.string.navbar_local), Assets.localIcon, "canvas.LocalScreen");
		NAVIGATION_BAR_PLANET = navigationBar.add(L.string(R.string.navbar_planet), Assets.planetIcon, "canvas.PlanetScreen");
		NAVIGATION_BAR_DISK = navigationBar.add(L.string(R.string.navbar_disk), Assets.diskIcon, "canvas.DiskScreen");
		NAVIGATION_BAR_OPTIONS = navigationBar.add(L.string(R.string.navbar_options), Assets.optionsIcon, "canvas.options.OptionsScreen");
		NAVIGATION_BAR_LIBRARY = navigationBar.add(L.string(R.string.navbar_library), Assets.libraryIcon, "canvas.LibraryScreen");
		NAVIGATION_BAR_ACADEMY = navigationBar.add(L.string(R.string.navbar_academy), Assets.academyIcon, "canvas.tutorial.TutorialSelectionScreen");
		NAVIGATION_BAR_HACKER = navigationBar.add(L.string(R.string.navbar_hacker), Assets.hackerIcon, "canvas.HackerScreen");
		navigationBar.setVisible(NAVIGATION_BAR_HACKER, false);
		NAVIGATION_BAR_QUIT = navigationBar.add(L.string(R.string.navbar_quit), Assets.quitIcon, "canvas.QuitScreen");
		navigationBar.setActiveIndex(NAVIGATION_BAR_OPTIONS);
	}

	public void afterSurfaceCreated() {
		createNavigationBar();
		navigationBar.setActiveIndex(NAVIGATION_BAR_STATUS);
		Assets.yesIcon = getGraphics().newPixmap("yes_icon_small.png");
		Assets.noIcon = getGraphics().newPixmap("no_icon_small.png");
		loadFonts();
	}

	private void loadFonts() {
		Assets.regularFont = getFont(R.font.robotor, 40);
		Assets.boldFont = getFont(R.font.robotob, 40);
		Assets.italicFont = getFont(R.font.robotoi, 40);
		Assets.boldItalicFont = getFont(R.font.robotobi, 40);
		// Although the following fonts are regular font as well,
		// because of the different size it must be created.
		// Scaling of the original font results different width of text
		// and not so smooth appearance mainly in small font
		Assets.titleFont = getFont(R.font.robotor, 60);
		Assets.smallFont = getFont(R.font.robotor, 30);
		AliteLog.d("loadFonts", "Fonts are loaded");
	}

	private GLText getFont(int fontId, int size) {
		return GLText.load(this, Settings.colorDepth, fontId,
			(int) (size * scaleFactor), size, 2, 2);
	}

	public void changeLocale() {
		new LoadingScreen().changeLocale();
		loadFonts();
		createNavigationBar();
		generator.rebuildGalaxy();
		player.setCurrentSystem(generator.getSystem(player.getCurrentSystem().getIndex()));
		player.setHyperspaceSystem(generator.getSystem(player.getHyperspaceSystem().getIndex()));
		AliteStartManager.loadLocaleDependentPlugins();
		if (inGame != null) {
			inGame.traverseObjects(new SpaceObjectTraverser() {
				private static final long serialVersionUID = -1697212661349348004L;
				@Override
				public boolean handle(SpaceObject so) {
					SpaceObjectFactory.getInstance().getTemplateObject(so.getId()).getRepoHandler().
						copyLocaleDependentTo(so.getRepoHandler());
					return false;
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		// Does nothing on purpose: Saves the player from accidentally quitting
		// the application. Later on, we might want to implement a meaningful
		// functionality here....
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (getCurrentScreen() instanceof FlightScreen) {
			FlightScreen fs = (FlightScreen) getCurrentScreen();
			if (fs.getInformationScreen() == null) {
				fs.togglePause();
			}
		}
		invalidateOptionsMenu();
		return true;
	}

	public TextureManager getTextureManager() {
		return textureManager;
	}

	public void setLaserManager(LaserManager man) {
		laserManager = man;
	}

	public LaserManager getLaserManager() {
		return laserManager;
	}

	public static Alite get() {
		return alite;
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		AliteLog.d("ON CONFIGURATION CHANGED", "ON CONFIGURATION CHANGED");
		super.onConfigurationChanged(config);
	}

	public final void autoLoad() throws IOException {
		fileUtils.autoLoad();
	}

	public final void autoSave() throws IOException {
		if (getCurrentScreen() != null && getCurrentScreen() instanceof TutorialScreen) {
			return;
		}
		fileUtils.autoSave();
	}

	public final void loadCommander(String fileName) throws IOException {
		fileUtils.loadCommander(fileName);
	}

	public final void loadCommander(DataInputStream dis) throws IOException {
		fileUtils.loadCommander(dis);
	}

	public final void saveCommander(String newName, String fileName) throws IOException {
		fileUtils.saveCommander(newName, fileName);
	}

	public final void saveCommander(String newName) throws IOException {
		fileUtils.saveCommander(newName);
	}

	public final void saveCommander(DataOutputStream dos) throws IOException {
		fileUtils.saveCommander(dos);
	}

	public CommanderData getQuickCommanderInfo(String fileName) {
		return fileUtils.getQuickCommanderInfo(CommanderData.DIRECTORY_COMMANDER + fileName);
	}

	public File[] getCommanderFiles() {
		return fileUtils.getCommanderFiles();
	}

	public boolean existsSavedCommander() {
		return fileUtils.existsSavedCommander();
	}

}
