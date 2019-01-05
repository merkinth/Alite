package de.phbouillon.android.games.alite.screens.canvas.options;

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
import java.io.IOException;
import java.util.Locale;

import android.content.Intent;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.opengl.AboutScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class OptionsScreen extends AliteScreen {
	public static boolean SHOW_DEBUG_MENU = false;
	private static final boolean RESTORE_SAVEGAME = false;

	private Button resetGame;
	private Button about;
	private Button debug;
	private Button gameplayOptions;
	private Button displayOptions;
	private Button controlOptions;
	private Button audioOptions;
	private boolean confirmReset = false;
	private int rowSize = 130;
	private int buttonSize = 100;

	public OptionsScreen(Alite game) {
		super(game);
	}

	Button createButton(int row, String text) {
		return Button.createGradientTitleButton(50, rowSize * (row + 1), 1620, buttonSize, text);
	}

	Button createSmallButton(int row, boolean left, String text) {
		return Button.createGradientTitleButton(left ? 50 : 890, rowSize * (row + 1), 780, buttonSize, text);
	}

	Slider createFloatSlider(int row, float minValue, float maxValue, String text, float currentValue) {
		Slider s = new Slider(50, rowSize * (row + 1), 1620, buttonSize, minValue, maxValue, currentValue, text, Assets.titleFont);
		s.setScaleTexts(String.format(Locale.getDefault(),  "%2.1f", minValue),
				        String.format(Locale.getDefault(),  "%2.1f", maxValue),
				        String.format(Locale.getDefault(),  "%2.1f", (maxValue - minValue) / 2.0f + minValue));
		return s;
	}

	@Override
	public void activate() {
		gameplayOptions = createButton(0, "Gameplay Options");
		displayOptions  = createButton(1, "Display Options");
		audioOptions    = createButton(2, "Audio Options");
		controlOptions  = createButton(3, "Control Options");
		resetGame       = createButton(4, "Reset Game");
		about           = createButton(5, "About")
			.setVisible(!(game.getCurrentScreen() instanceof FlightScreen));
		debug           = createButton(6, SHOW_DEBUG_MENU ? "Debug Menu" : "Log to file: " + (Settings.logToFile ? "Yes" : "No"));
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle("Options");
		gameplayOptions.render(g);
		displayOptions.render(g);
		audioOptions.render(g);
		controlOptions.render(g);
		resetGame.render(g);
		about.render(g);
		debug.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (confirmReset && messageResult != RESULT_NONE) {
			confirmReset = false;
			if (messageResult == RESULT_YES) {
				resetGame();
			}
			messageResult = RESULT_NONE;
		}

		if (gameplayOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new GameplayOptionsScreen(game);
			return;
		}
		if (displayOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new DisplayOptionsScreen(game);
			return;
		}
		if (audioOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new AudioOptionsScreen(game);
			return;
		}
		if (controlOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new ControlOptionsScreen(game, false);
			return;
		}
		if (languages.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			L.setLocale(game, Settings.DEFAULT_LOCALE_FILE.equals(Settings.localeFileName.substring(
					Settings.localeFileName.lastIndexOf(File.separator) + 1)) ?
				game.getFileIO().getFileName(L.DIRECTORY_LOCALES + File.separator + "hu_hu_official.zip") : "");
			game.changeLocale();

			String languageText = "Language: " + L.currentLocale.getDisplayLanguage(L.currentLocale);
			languages.setText(languageText)
				.setPixmap(getCountryFlag(L.currentLocale.getCountry()))
				.setPixmapOffset(languages.getWidth() + game.getGraphics().
					getTextWidth(languageText, Assets.titleFont) + 10 >> 1, 25);
			Settings.save(game.getFileIO());
			return;
		}
		if (resetGame.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			showQuestionDialog("Are you sure?");
			confirmReset = true;
			return;
		}
		if (about.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new AboutScreen(game);
			return;
		}
		if (debug.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			if (SHOW_DEBUG_MENU) {
				newScreen = new DebugSettingsScreen(game);
			} else {
				Settings.logToFile = !Settings.logToFile;
				debug.setText("Log to file: " + (Settings.logToFile ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			}
		}
	}

	private void resetGame() {
		AndroidGame.resetting = true;
		Settings.introVideoQuality = 255;
		Settings.save(game.getFileIO());
		game.getPlayer().reset();
		game.setGameTime(0);

		if (RESTORE_SAVEGAME) {
			Player player = game.getPlayer();
			PlayerCobra cobra = player.getCobra();
			player.setCash(831095);
			player.setLegalStatus(LegalStatus.CLEAN);
			player.setLegalValue(0);
			player.setRating(Rating.DEADLY);
			player.setScore(386655);
			player.setName("richard");
			player.setCurrentSystem(game.getGenerator()
					.getSystem(84));
			player.setHyperspaceSystem(game.getGenerator().getSystem(84));
			cobra.addEquipment(EquipmentStore.fuelScoop);
			cobra.addEquipment(EquipmentStore.retroRockets);
			cobra.addEquipment(EquipmentStore.galacticHyperdrive);
			cobra.addEquipment(EquipmentStore.dockingComputer);
			cobra.addEquipment(EquipmentStore.extraEnergyUnit);
			cobra.addEquipment(EquipmentStore.energyBomb);
			cobra.addEquipment(EquipmentStore.escapeCapsule);
			cobra.addEquipment(EquipmentStore.ecmSystem);
			cobra.addEquipment(EquipmentStore.largeCargoBay);
			cobra.setMissiles(4);
			cobra.setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.militaryLaser);
			cobra.setLaser(PlayerCobra.DIR_REAR, EquipmentStore.militaryLaser);
			cobra.setLaser(PlayerCobra.DIR_LEFT, EquipmentStore.militaryLaser);
			cobra.setLaser(PlayerCobra.DIR_RIGHT, EquipmentStore.militaryLaser);

			game.setGameTime(283216L * 1000L * 1000L * 1000L);
			try {
				game.saveCommander("richard");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Intent intent = new Intent(game, AliteIntro.class);
		intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
		game.startActivityForResult(intent, 0);
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.OPTIONS_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new OptionsScreen(alite));
		return true;
	}
}
