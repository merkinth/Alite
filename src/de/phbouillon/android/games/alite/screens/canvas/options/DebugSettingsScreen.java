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
import java.util.Locale;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Button;
import de.phbouillon.android.games.alite.ScreenCodes;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
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

	DebugSettingsScreen(Alite game) {
		super(game);
		game.getPlayer().setCheater(true);
	}

	private String formatCash() {
		Player player = game.getPlayer();
		return String.format(Locale.getDefault(), "%d.%d Cr", player.getCash() / 10, player.getCash() % 10);
	}

	@Override
	public void activate() {
		logToFile = Button.createGradientTitleButton(50, 130, 780, 100, "Log to file: " + (Settings.logToFile ? "Yes" : "No"));
		memDebug = Button.createGradientTitleButton(890, 130, 780, 100, "Debug Memory: " + (Settings.memDebug ? "Yes" : "No"));

		showFrameRate = Button.createGradientTitleButton(50, 250, 780, 100, "Display frame rate: " + (Settings.displayFrameRate ? "Yes" : "No"));
		invulnerable = Button.createGradientTitleButton(890, 250, 780, 100, "Invulnerable: " + (Settings.invulnerable ? "Yes" : "No"));
		showDockingDebug = Button.createGradientTitleButton(50, 370, 780, 100, "Docking Information: " + (Settings.displayDockingInformation ? "Yes" : "No"));
		laserDoesNotOverheat = Button.createGradientTitleButton(890, 370, 780, 100, "Laser Overheats: " + (Settings.laserDoesNotOverheat ? "No" : "Yes"));
		adjustCredits = Button.createGradientTitleButton(50, 490, 780, 100, "Adjust Credits (" + formatCash() + ")");
		arriveInSafeZone = Button.createGradientTitleButton(890, 490, 780, 100, "Arrive in Safe Zone: " + (Settings.enterInSafeZone ? "Yes" : "No"));
		adjustLegalStatus = Button.createGradientTitleButton(50, 610, 780, 100, "Adjust Legal (" + game.getPlayer().getLegalStatus() + ", " + game.getPlayer().getLegalValue() + ")");
		disableAttackers = Button.createGradientTitleButton(890, 610, 780, 100, "Disable Attackers: " + (Settings.disableAttackers ? "Yes" : "No"));
		adjustScore = Button.createGradientTitleButton(50, 730, 780, 100, "Adjust Score (" + game.getPlayer().getScore() + ")");
		disableTraders = Button.createGradientTitleButton(890, 730, 780, 100, "Disable Traders: " + (Settings.disableTraders ? "Yes" : "No"));
		addDockingComputer = Button.createGradientTitleButton(50, 850, 780, 100, "Add Docking Computer");
		unlimitedFuel = Button.createGradientTitleButton(890, 850, 780, 100, "Unlimited Fuel: " + (Settings.unlimitedFuel ? "Yes" : "No"));
		more = Button.createGradientTitleButton(50, 970, 1620, 100, "More");
	}

	@Override
	public void present(float deltaTime) {
		if (disposed) {
			return;
		}
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle("Debug Options");

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
		super.processTouch(touch);
		if (getMessage() != null) {
			return;
		}

		if (touch.type == TouchEvent.TOUCH_UP) {
			if (logToFile.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.logToFile = !Settings.logToFile;
				logToFile.setText("Log to file: " + (Settings.logToFile ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (memDebug.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.memDebug = !Settings.memDebug;
				memDebug.setText("Debug Memory: " + (Settings.memDebug ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (showFrameRate.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.displayFrameRate = !Settings.displayFrameRate;
				showFrameRate.setText("Display frame rate: " + (Settings.displayFrameRate ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (invulnerable.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.invulnerable = !Settings.invulnerable;
				invulnerable.setText("Invulnerable: " + (Settings.invulnerable ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (showDockingDebug.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.displayDockingInformation = !Settings.displayDockingInformation;
				showDockingDebug.setText("Docking Information: " + (Settings.displayDockingInformation ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (laserDoesNotOverheat.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.laserDoesNotOverheat = !Settings.laserDoesNotOverheat;
				laserDoesNotOverheat.setText("Laser Overheats: " + (Settings.laserDoesNotOverheat ? "No" : "Yes"));
				Settings.save(game.getFileIO());
			} else if (adjustCredits.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				game.getPlayer().setCash(game.getPlayer().getCash() + 10000);
				adjustCredits.setText("Adjust Credits (" + formatCash() + ")");
			} else if (adjustLegalStatus.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Player player = game.getPlayer();
				if (player.getLegalValue() != 0) {
					player.setLegalValue(player.getLegalValue() >> 1);
				} else {
					player.setLegalValue(255);
				}
				adjustLegalStatus.setText("Adjust Legal (" + player.getLegalStatus() + ", " + player.getLegalValue() + ")");
			} else if (adjustScore.isTouched(touch.x, touch.y)) {
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
				adjustScore.setText("Adjust Score (" + game.getPlayer().getScore() + ")");
			} else if (addDockingComputer.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				game.getCobra().addEquipment(EquipmentStore.dockingComputer);
			} else if (unlimitedFuel.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.unlimitedFuel = !Settings.unlimitedFuel;
				unlimitedFuel.setText("Unlimited Fuel: " + (Settings.unlimitedFuel ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (arriveInSafeZone.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.enterInSafeZone = !Settings.enterInSafeZone;
				arriveInSafeZone.setText("Arrive in Safe Zone: " + (Settings.enterInSafeZone ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (disableAttackers.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.disableAttackers = !Settings.disableAttackers;
				disableAttackers.setText("Disable Attackers: " + (Settings.disableAttackers ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (disableTraders.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				Settings.disableTraders = !Settings.disableTraders;
				disableTraders.setText("Disable Traders: " + (Settings.disableTraders ? "Yes" : "No"));
				Settings.save(game.getFileIO());
			} else if (more.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new MoreDebugSettingsScreen(game);
			}
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.DEBUG_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new DebugSettingsScreen(alite));
		return true;
	}
}
