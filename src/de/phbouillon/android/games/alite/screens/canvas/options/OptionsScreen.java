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

import java.io.IOException;

import android.content.Intent;
import android.graphics.*;
import androidx.core.content.res.ResourcesCompat;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.PluginModel;
import de.phbouillon.android.framework.impl.AndroidGame;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.*;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.PluginsScreen;
import de.phbouillon.android.games.alite.screens.opengl.AboutScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class OptionsScreen extends AliteScreen {
	public static boolean SHOW_DEBUG_MENU = false;
	private static final boolean RESTORE_SAVEGAME = false;

	private Button resetGame;
	private Button about;
	private Button debug;
	private Button gameplayOptions;
	private Button displayOptions;
	private Button audioOptions;
	private Button controlOptions;
	private Button languages;
	private Button extensionPacks;
	private boolean confirmReset = false;
	private int rowSize = 130;
	private int buttonSize = 100;

	Button createButton(int row, String text) {
		return Button.createGradientTitleButton(50, rowSize * (row + 1), 1620, buttonSize, text);
	}

	Button createSmallButton(int row, boolean left, String text) {
		return Button.createGradientTitleButton(left ? 50 : 890, rowSize * (row + 1), 780, buttonSize, text);
	}

	Slider createFloatSlider(int row, float minValue, float maxValue, String text, float currentValue) {
		Slider s = new Slider(50, rowSize * (row + 1), 1620, buttonSize, minValue, maxValue, currentValue, text, Assets.titleFont);
		s.setScaleTexts(StringUtil.format("%2.1f", minValue),
			StringUtil.format("%2.1f", maxValue),
			StringUtil.format("%2.1f", (maxValue + minValue) / 2.0f));
		return s;
	}

	@Override
	public void activate() {
		L.getInstance().loadLocaleList(Settings.getLocaleFiles(game.getFileIO()));
		gameplayOptions = createSmallButton(0, true, L.string(R.string.title_gameplay_options));
		displayOptions  = createSmallButton(0, false, L.string(R.string.title_display_options));
		audioOptions    = createSmallButton(1, true, L.string(R.string.title_audio_options));
		controlOptions  = createSmallButton(1, false, L.string(R.string.title_control_options));

		languages = createSmallButton(2, true, L.string(R.string.options_main_language,
			L.getInstance().getCurrentLocale().getDisplayLanguage(L.getInstance().getCurrentLocale())));
		addPixmapAfterText(languages, getCountryFlag(L.getInstance().getCurrentLocale().getCountry()));

		int count = new PluginModel(game.getFileIO(),
			PluginModel.DIRECTORY_PLUGINS + PluginsScreen.PLUGINS_META_FILE).countNewAndUpgraded();
		extensionPacks = createSmallButton(2, false, L.string(R.string.title_plugins));
		if (count > 0) {
			addPixmapAfterText(extensionPacks, getNewAndUpgradedMarker(count));
		}

		resetGame = createButton(4, L.string(R.string.options_main_reset_game));
		about = createButton(5, L.string(R.string.options_main_about))
			.setVisible(!(game.getCurrentScreen() instanceof FlightScreen));
		debug = createButton(6, SHOW_DEBUG_MENU ? L.string(R.string.title_debug_menu) :
			L.string(R.string.debug_settings_log_to_file, L.string(Settings.logToFile ? R.string.options_yes : R.string.options_no)));
	}

	private Pixmap getCountryFlag(String countryCode) {
		int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
		int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;

		Bitmap bitmap = Bitmap.createBitmap(100, 50,
			Settings.colorDepth  == 1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444);
		bitmap.eraseColor(Color.TRANSPARENT);
		Paint paint = new Paint();
		paint.setTextSize(60);
		new Canvas(bitmap).drawText(new String(Character.toChars(firstLetter)) +
			new String(Character.toChars(secondLetter)), 10, 50, paint);
		return game.getGraphics().newPixmap(bitmap, "countryFlag");
	}

	private void addPixmapAfterText(Button b, Pixmap pixmap) {
		b.setPixmap(pixmap).setPixmapOffset(b.getWidth() +
			game.getGraphics().getTextWidth(b.getText(), Assets.titleFont) + 10 >> 1, b.getHeight() - pixmap.getHeight() >> 1);
	}

	private Pixmap getNewAndUpgradedMarker(int count) {
		Paint paint = new Paint();
		paint.setTypeface(ResourcesCompat.getFont(game, R.font.robotor));
		paint.setTextSize(Assets.regularFont.getSize());

		String text = StringUtil.format("%d", count);

		Graphics g = game.getGraphics();
		int width = g.getTextWidth(text, Assets.regularFont);
		int r = (Math.max(width, g.getTextHeight(text, Assets.regularFont)) >> 1) + 5;

		Bitmap bitmap = Bitmap.createBitmap(r << 1, r << 1,
			Settings.colorDepth  == 1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444);
		bitmap.eraseColor(Color.TRANSPARENT);

		Canvas canvas = new Canvas(bitmap);
		paint.setColor(ColorScheme.get(ColorScheme.COLOR_WARNING_MESSAGE));
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(r, r, r, paint);

		paint.setColor(ColorScheme.get(ColorScheme.COLOR_MESSAGE));
		canvas.drawText(text, r - (width >> 1), r  + ((int)Assets.regularFont.getSize() >> 1) - 5, paint);
		return game.getGraphics().newPixmap(bitmap, "newPlugins");
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));

		displayTitle(L.string(R.string.title_options));
		gameplayOptions.render(g);
		displayOptions.render(g);
		audioOptions.render(g);
		controlOptions.render(g);
		languages.render(g);
		extensionPacks.render(g);
		resetGame.render(g);
		about.render(g);
		debug.render(g);
	}

	public static int cycleFromZeroTo(int current, int max) {
		return current < max ? current + 1 : 0;
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
			newScreen = new GameplayOptionsScreen();
			return;
		}
		if (displayOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new DisplayOptionsScreen();
			return;
		}
		if (audioOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new AudioOptionsScreen();
			return;
		}
		if (controlOptions.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new ControlOptionsScreen(false);
			return;
		}
		if (languages.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			L.getInstance().setLocale(L.getInstance().getNextLocale());
			game.changeLocale();
			Settings.save(game.getFileIO());
			newScreen = new OptionsScreen();
			return;
		}
		if (extensionPacks.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new PluginsScreen(0);
			return;
		}
		if (resetGame.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			showQuestionDialog(L.string(R.string.options_main_reset_game_confirm));
			confirmReset = true;
			return;
		}
		if (about.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new AboutScreen();
			return;
		}
		if (debug.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			if (SHOW_DEBUG_MENU) {
				newScreen = new DebugSettingsScreen();
			} else {
				Settings.logToFile = !Settings.logToFile;
				debug.setText(L.string(R.string.debug_settings_log_to_file,
					L.string(Settings.logToFile ? R.string.options_yes : R.string.options_no)));
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
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_SYSTEM));
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY));
			cobra.setMissiles(4);
			cobra.setLaser(PlayerCobra.DIR_FRONT, EquipmentStore.get().getEquipmentById(EquipmentStore.MILITARY_LASER));
			cobra.setLaser(PlayerCobra.DIR_REAR, EquipmentStore.get().getEquipmentById(EquipmentStore.MILITARY_LASER));
			cobra.setLaser(PlayerCobra.DIR_LEFT, EquipmentStore.get().getEquipmentById(EquipmentStore.MILITARY_LASER));
			cobra.setLaser(PlayerCobra.DIR_RIGHT, EquipmentStore.get().getEquipmentById(EquipmentStore.MILITARY_LASER));

			game.setGameTime(283216L * 1000L * 1000L * 1000L);
			try {
				game.saveCommander("richard");
			} catch (IOException e) {
				AliteLog.e("[ALITE] SaveCommander", "Error while saving commander.", e);
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

}
