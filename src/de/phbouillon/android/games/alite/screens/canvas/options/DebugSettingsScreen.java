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


import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class DebugSettingsScreen extends AliteScreen {
	private Button logToFile;
	private Button memDebug;
	private Button showFrameRate;
	private Button showDockingDebug;
	private Button adjustCredits;
	private Button adjustLegalStatus;
	private Button adjustScore;
	private Button addDockingComputer;
	private Button unlimitedFuel;
    private Button arriveInSafeZone;
    private Button disableAttackers;
    private Button disableTraders;
    private Button invulnerable;
    private Button laserDoesNotOverheat;
    private Button more;

	public DebugSettingsScreen() {
		game.getPlayer().setCheater(true);
	}

	private String formatCash() {
		Player player = game.getPlayer();
		return L.getOneDecimalFormatString(R.string.cash_amount_value_ccy, player.getCash());
	}

	@Override
	public void activate() {
		logToFile = Button.createGradientTitleButton(50, 130, 780, 100, L.string(R.string.debug_settings_log_to_file,
			L.string(Settings.logToFile ? R.string.options_yes : R.string.options_no)));
		memDebug = Button.createGradientTitleButton(890, 130, 780, 100, L.string(R.string.debug_settings_mem_debug,
			L.string(Settings.memDebug ? R.string.options_yes : R.string.options_no)));

		showFrameRate = Button.createGradientTitleButton(50, 250, 780, 100,
			L.string(R.string.debug_settings_show_frame_rate, L.string(Settings.displayFrameRate ? R.string.options_yes : R.string.options_no)));
		invulnerable = Button.createGradientTitleButton(890, 250, 780, 100,
			L.string(R.string.debug_settings_invulnerable, L.string(Settings.invulnerable ? R.string.options_yes : R.string.options_no)));
		showDockingDebug = Button.createGradientTitleButton(50, 370, 780, 100,
			L.string(R.string.debug_settings_show_docking_debug, L.string(Settings.displayDockingInformation ? R.string.options_yes : R.string.options_no)));
		laserDoesNotOverheat = Button.createGradientTitleButton(890, 370, 780, 100,
			L.string(R.string.debug_settings_laser_does_not_overheat, L.string(Settings.laserDoesNotOverheat ? R.string.options_no : R.string.options_yes)));
		adjustCredits = Button.createGradientTitleButton(50, 490, 780, 100,
			L.string(R.string.debug_settings_adjust_credits, formatCash()));
		arriveInSafeZone = Button.createGradientTitleButton(890, 490, 780, 100,
			L.string(R.string.debug_settings_arrive_in_safe_zone, L.string(Settings.enterInSafeZone ? R.string.options_yes : R.string.options_no)));
		adjustLegalStatus = Button.createGradientTitleButton(50, 610, 780, 100,
			L.string(R.string.debug_settings_adjust_legal_status, game.getPlayer().getLegalStatus().getName(), game.getPlayer().getLegalValue()));
		disableAttackers = Button.createGradientTitleButton(890, 610, 780, 100,
			L.string(R.string.debug_settings_disable_attackers, L.string(Settings.disableAttackers ? R.string.options_yes : R.string.options_no)));
		adjustScore = Button.createGradientTitleButton(50, 730, 780, 100,
			L.string(R.string.debug_settings_adjust_score, game.getPlayer().getScore()));
		disableTraders = Button.createGradientTitleButton(890, 730, 780, 100,
			L.string(R.string.debug_settings_disable_traders, L.string(Settings.disableTraders ? R.string.options_yes : R.string.options_no)));
		addDockingComputer = Button.createGradientTitleButton(50, 850, 780, 100,
			L.string(R.string.debug_settings_add_docking_computer));
		unlimitedFuel = Button.createGradientTitleButton(890, 850, 780, 100,
			L.string(R.string.debug_settings_unlimited_fuel, L.string(Settings.unlimitedFuel ? R.string.options_yes : R.string.options_no)));
		more = Button.createGradientTitleButton(50, 970, 1620, 100, L.string(R.string.debug_settings_button_more));
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_debug_options));

		logToFile.render(g);
		memDebug.render(g);
		showFrameRate.render(g);
		invulnerable.render(g);
		showDockingDebug.render(g);
		laserDoesNotOverheat.render(g);
		adjustCredits.render(g);
		arriveInSafeZone.render(g);
		adjustLegalStatus.render(g);
		disableAttackers.render(g);
		adjustScore.render(g);
		disableTraders.render(g);
		addDockingComputer.render(g);
		unlimitedFuel.render(g);
		more.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (logToFile.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.logToFile = !Settings.logToFile;
			logToFile.setText(L.string(R.string.debug_settings_log_to_file,
				L.string(Settings.logToFile ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (memDebug.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.memDebug = !Settings.memDebug;
			memDebug.setText(L.string(R.string.debug_settings_mem_debug, L.string(Settings.memDebug ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (showFrameRate.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.displayFrameRate = !Settings.displayFrameRate;
			showFrameRate.setText(L.string(R.string.debug_settings_show_frame_rate, L.string(Settings.displayFrameRate ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (invulnerable.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.invulnerable = !Settings.invulnerable;
			invulnerable.setText(L.string(R.string.debug_settings_invulnerable, L.string(Settings.invulnerable ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (showDockingDebug.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.displayDockingInformation = !Settings.displayDockingInformation;
			showDockingDebug.setText(L.string(R.string.debug_settings_show_docking_debug, L.string(Settings.displayDockingInformation ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (laserDoesNotOverheat.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.laserDoesNotOverheat = !Settings.laserDoesNotOverheat;
			laserDoesNotOverheat.setText(L.string(R.string.debug_settings_laser_does_not_overheat, L.string(Settings.laserDoesNotOverheat ? R.string.options_no : R.string.options_yes)));
			Settings.save(game.getFileIO());
			return;
		}
		if (adjustCredits.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getPlayer().setCash(game.getPlayer().getCash() + 10000);
			adjustCredits.setText(L.string(R.string.debug_settings_adjust_credits, formatCash()));
			return;
		}
		if (adjustLegalStatus.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Player player = game.getPlayer();
			if (player.getLegalValue() != 0) {
				player.setLegalValue(player.getLegalValue() >> 1);
			} else {
				player.setLegalValue(255);
			}
			adjustLegalStatus.setText(L.string(R.string.debug_settings_adjust_legal_status, game.getPlayer().getLegalStatus().getName(), game.getPlayer().getLegalValue()));
			return;
		}
		if (adjustScore.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Player player = game.getPlayer();
			int score = player.getScore();
			if (score < Rating.HARMLESS.getScoreThreshold() - 1) {
				score = Rating.HARMLESS.getScoreThreshold() - 1;
			} else if (score < Rating.MOSTLY_HARMLESS.getScoreThreshold() - 1) {
				score = Rating.MOSTLY_HARMLESS.getScoreThreshold() - 1;
			} else if (score < Rating.POOR.getScoreThreshold() - 1) {
				score = Rating.POOR.getScoreThreshold() - 1;
			} else if (score < Rating.AVERAGE.getScoreThreshold() - 1) {
				score = Rating.AVERAGE.getScoreThreshold() - 1;
			} else if (score < Rating.ABOVE_AVERAGE.getScoreThreshold() - 1) {
				score = Rating.ABOVE_AVERAGE.getScoreThreshold() - 1;
			} else if (score < Rating.COMPETENT.getScoreThreshold() - 1) {
				score = Rating.COMPETENT.getScoreThreshold() - 1;
			} else if (score < Rating.DANGEROUS.getScoreThreshold() - 1) {
				score = Rating.DANGEROUS.getScoreThreshold() - 1;
			} else if (score < Rating.DEADLY.getScoreThreshold() - 1) {
				score = Rating.DEADLY.getScoreThreshold() - 1;
			}
			player.setScore(score);
			while (score >= game.getPlayer().getRating().getScoreThreshold() && game.getPlayer().getRating().getScoreThreshold() > 0) {
				game.getPlayer().setRating(Rating.values()[game.getPlayer().getRating().ordinal() + 1]);
			}
			adjustScore.setText(L.string(R.string.debug_settings_adjust_score, game.getPlayer().getScore()));
			return;
		}
		if (addDockingComputer.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER));
			return;
		}
		if (unlimitedFuel.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.unlimitedFuel = !Settings.unlimitedFuel;
			unlimitedFuel.setText(L.string(R.string.debug_settings_unlimited_fuel, L.string(Settings.unlimitedFuel ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (arriveInSafeZone.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.enterInSafeZone = !Settings.enterInSafeZone;
			arriveInSafeZone.setText(L.string(R.string.debug_settings_arrive_in_safe_zone, L.string(Settings.enterInSafeZone ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (disableAttackers.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.disableAttackers = !Settings.disableAttackers;
			disableAttackers.setText(L.string(R.string.debug_settings_disable_attackers, L.string(Settings.disableAttackers ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (disableTraders.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			Settings.disableTraders = !Settings.disableTraders;
			disableTraders.setText(L.string(R.string.debug_settings_disable_traders, L.string(Settings.disableTraders ? R.string.options_yes : R.string.options_no)));
			Settings.save(game.getFileIO());
			return;
		}
		if (more.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new MoreDebugSettingsScreen();
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.DEBUG_SCREEN;
	}

}
