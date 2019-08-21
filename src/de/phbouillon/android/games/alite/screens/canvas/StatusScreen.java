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

import java.io.DataInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.net.Uri;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.Laser;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.canvas.options.ControlOptionsScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class StatusScreen extends AliteScreen {
	private static final int SHIP_X                 = 360;
	private static final int SHIP_Y                 = 400;

	private static volatile Pixmap cobra;
	private AliteScreen forwardingScreen = null;
	private boolean requireAnswer = false;
	private boolean pendingShowControlOptions = false;

	public StatusScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
		setUpForDisplay(game.getGraphics().getVisibleArea());
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
		game.getNavigationBar().resetPending();
		for (Mission m: MissionManager.getInstance().getMissions()) {
			if (m.missionStarts()) {
				forwardingScreen = m.getMissionScreen();
				break;
			}
			if (m.isActive()) {
				AliteScreen t = m.checkForUpdate();
				if (t != null) {
					forwardingScreen = t;
					break;
				}
			}
		}
		if (!Settings.hasBeenPlayedBefore && game.getGenerator().getCurrentGalaxy() == 1) {
			Player player = game.getPlayer();
			if (player.getCurrentSystem() != null && player.getCurrentSystem().getIndex() == SystemData.LAVE_SYSTEM_INDEX) {
				showModalQuestionDialog(L.string(R.string.intro_new_player, AliteConfig.GAME_NAME));
				requireAnswer = true;
				Settings.hasBeenPlayedBefore = true;
				Settings.save(game.getFileIO());
			}
		}
	}

	private void drawInformation(final Graphics g) {
		Player player = game.getPlayer();

		g.drawText(L.string(R.string.status_present_system), 40, 150, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_hyperspace),     40, 190, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_condition),      40, 230, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_legal_status),   40, 270, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_rating),         40, 310, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_fuel),           40, 350, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.status_cash),           40, 390, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		if (!player.getActiveMissions().isEmpty()) {
			g.drawText(L.string(R.string.status_mission_objective), 750, 150, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		}

		if (player.getCurrentSystem() != null) {
			g.drawText(player.getCurrentSystem().getName(), 400, 150, ColorScheme.get(ColorScheme.COLOR_CURRENT_SYSTEM_NAME), Assets.regularFont);
		}
		if (player.getHyperspaceSystem() != null) {
			g.drawText(player.getHyperspaceSystem().getName(), 400, 190, ColorScheme.get(ColorScheme.COLOR_HYPERSPACE_SYSTEM_NAME), Assets.regularFont);
		}
		g.drawText(player.getCondition().getName(), 400, 230, player.getCondition().getColor(),  Assets.regularFont);
		g.drawText(player.getLegalStatus().getName(), 400, 270, ColorScheme.get(ColorScheme.COLOR_LEGAL_STATUS),  Assets.regularFont);
		g.drawText(player.getRating().getName() + L.string(R.string.status_score, player.getScore()),
			400, 310, ColorScheme.get(ColorScheme.COLOR_RATING), Assets.regularFont);
		g.drawText(L.getOneDecimalFormatString(R.string.status_fuel_value, player.getCobra().getFuel()), 400, 350,
			ColorScheme.get(ColorScheme.COLOR_REMAINING_FUEL), Assets.regularFont);
		g.drawText(L.getOneDecimalFormatString(R.string.cash_amount_value_currency, player.getCash()), 400, 390,
				ColorScheme.get(ColorScheme.COLOR_BALANCE), Assets.regularFont);
		if (!player.getActiveMissions().isEmpty()) {
			g.drawText(player.getActiveMissions().get(0).getObjective(), 1110, 150,
					ColorScheme.get(ColorScheme.COLOR_MISSION_OBJECTIVE), Assets.regularFont);
		}
		if (!(game.getCurrentScreen() instanceof FlightScreen)) {
			g.drawText(L.string(R.string.status_visit_web_page, AliteConfig.ALITE_WEBSITE), 50, 1050,
				ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		}
	}

	private void drawLasers(final Graphics g) {
		PlayerCobra cobra = game.getCobra();

		int lineColor = ColorScheme.get(ColorScheme.COLOR_ARROW);
		int textColor = ColorScheme.get(ColorScheme.COLOR_EQUIPMENT_DESCRIPTION);

		Laser front = cobra.getLaser(PlayerCobra.DIR_FRONT);
		if (front != null) {
			g.drawText(front.getShortName(), SHIP_X + 630, SHIP_Y - 20, textColor, Assets.smallFont);
			int halfWidth = g.getTextWidth(front.getShortName(), Assets.smallFont) >> 1;
			g.drawLine(SHIP_X + 630 + halfWidth, SHIP_Y - 10, SHIP_X + 630 + halfWidth, SHIP_Y + 20, lineColor);
			g.drawArrow(SHIP_X + 630 + halfWidth, SHIP_Y + 20, SHIP_X + 490, SHIP_Y + 20, lineColor, Graphics.ArrowDirection.LEFT);
		}
		Laser back = cobra.getLaser(PlayerCobra.DIR_REAR);
		if (back != null) {
			g.drawText(back.getShortName(), SHIP_X + 130, SHIP_Y + 620, textColor, Assets.smallFont);
			int halfWidth = g.getTextWidth(back.getShortName(), Assets.smallFont) >> 1;
			g.drawLine(SHIP_X + 130 + halfWidth, SHIP_Y + 580, SHIP_X + 130 + halfWidth, SHIP_Y + 590, lineColor);
			g.drawLine(SHIP_X + 130 + halfWidth, SHIP_Y + 580, SHIP_X + 480, SHIP_Y + 580, lineColor);
			g.drawArrow(SHIP_X + 480, SHIP_Y + 580, SHIP_X + 480, SHIP_Y + 550, lineColor, Graphics.ArrowDirection.UP);
		}
		Laser right = cobra.getLaser(PlayerCobra.DIR_RIGHT);
		if (right != null) {
			g.drawText(right.getShortName(), SHIP_X + 1000, SHIP_Y + 420, textColor, Assets.smallFont);
			int halfWidth = g.getTextWidth(right.getShortName(), Assets.smallFont) >> 1;
			g.drawLine(SHIP_X + 1000 + halfWidth, SHIP_Y + 390, SHIP_X + 1000 + halfWidth, SHIP_Y + 370, lineColor);
			g.drawArrow(SHIP_X + 940, SHIP_Y + 370, SHIP_X + 1000 + halfWidth, SHIP_Y + 370, lineColor, Graphics.ArrowDirection.LEFT);
		}
		Laser left = cobra.getLaser(PlayerCobra.DIR_LEFT);
		if (left != null) {
			g.drawText(left.getShortName(), SHIP_X - 250, SHIP_Y + 420, textColor, Assets.smallFont);
			int halfWidth = g.getTextWidth(left.getShortName(), Assets.smallFont) >> 1;
			g.drawLine(SHIP_X - 250 + halfWidth, SHIP_Y + 390, SHIP_X - 250 + halfWidth, SHIP_Y + 370, lineColor);
			g.drawArrow(SHIP_X + 20, SHIP_Y + 370, SHIP_X - 250 + halfWidth, SHIP_Y + 370, lineColor, Graphics.ArrowDirection.RIGHT);
		}
	}

	private void drawEquipment(final Graphics g) {
		List <Equipment> installedEquipment = game.getCobra().getInstalledEquipment();
		PlayerCobra cobra = game.getCobra();
		int missileCount = cobra.getMissiles();

		int counter = 0;
		int beginY = missileCount > 0 ? SHIP_Y + 140 : SHIP_Y + 170;
		for (Equipment equip: installedEquipment) {
			g.drawText(equip.getShortName(), SHIP_X + 980, beginY - counter * 30,
				ColorScheme.get(ColorScheme.COLOR_EQUIPMENT_DESCRIPTION), Assets.smallFont);
			g.drawLine(SHIP_X + 960, beginY - 12 - counter * 30, SHIP_X + 930, beginY - 12 - counter * 30,
				ColorScheme.get(ColorScheme.COLOR_ARROW));
			g.drawLine(SHIP_X + 930, beginY - 12 - counter * 30, SHIP_X + 930, beginY + 24 - counter * 30,
					ColorScheme.get(ColorScheme.COLOR_ARROW));
			counter++;
		}
		if (missileCount > 0) {
			g.drawText(L.string(R.string.status_missiles_count, missileCount), SHIP_X + 980, SHIP_Y + 170,
				ColorScheme.get(ColorScheme.COLOR_EQUIPMENT_DESCRIPTION), Assets.smallFont);
			g.drawLine(SHIP_X + 960, beginY + 18, SHIP_X + 930, beginY + 18, ColorScheme.get(ColorScheme.COLOR_ARROW));
		}
		if (missileCount > 0 || !installedEquipment.isEmpty()) {
			g.drawLine(SHIP_X + 930, beginY + 18, SHIP_X + 930, SHIP_Y + 200, ColorScheme.get(ColorScheme.COLOR_ARROW));
			g.drawArrow(SHIP_X + 780, SHIP_Y + 200, SHIP_X + 930, SHIP_Y + 200,
				ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.LEFT);
		}
	}

	static String getGameTime(long gameTime) {
		long diffInSeconds = TimeUnit.SECONDS.convert(gameTime, TimeUnit.NANOSECONDS);
		int diffInDays = (int) (diffInSeconds / 86400);
		diffInSeconds -= diffInDays * 86400;
		int diffInHours = (int) (diffInSeconds / 3600);
		diffInSeconds -= diffInHours * 3600;
		int diffInMinutes = (int) (diffInSeconds / 60);
		diffInSeconds -= diffInMinutes * 60;
		return StringUtil.format("%02d:%02d:%02d:%02d", diffInDays, diffInHours, diffInMinutes, diffInSeconds);
	}

	private void drawGameTime(final Graphics g) {
		g.drawRect(SHIP_X + 800, SHIP_Y + 570, 300, 100, ColorScheme.get(ColorScheme.COLOR_MESSAGE));
		int halfWidth = g.getTextWidth(L.string(R.string.status_game_time), Assets.regularFont) >> 1;
		g.drawText(L.string(R.string.status_game_time), SHIP_X + 800 + 150 - halfWidth, SHIP_Y + 610, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		String text = getGameTime(game.getGameTime());
		halfWidth = g.getTextWidth(text, Assets.regularFont) >> 1;
		g.drawText(text, SHIP_X + 800 + 150 - halfWidth, SHIP_Y + 650, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (forwardingScreen != null) {
			newScreen = forwardingScreen;
			forwardingScreen = null;
			performScreenChange();
			postScreenChange();
		}
	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (!(game.getCurrentScreen() instanceof FlightScreen)) {
				if (touch.x > 30 && touch.x < 960 && touch.y > 1020) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AliteConfig.ALITE_WEBSITE));
					game.startActivityForResult(browserIntent, 0);
				}
			}
		}
		if (requireAnswer && messageResult != RESULT_NONE) {
			requireAnswer = false;
			if (messageResult == RESULT_YES) {
				newScreen = new ControlOptionsScreen(game, !pendingShowControlOptions);
			} else if (!pendingShowControlOptions) {
				showLargeModalQuestionDialog(L.string(R.string.intro_new_player_info));
				requireAnswer = true;
				pendingShowControlOptions = true;
			}
			messageResult = RESULT_NONE;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		Player player = game.getPlayer();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_status, StringUtil.toUpperFirstCase(player.getName())));
		if (cobra == null) {
		  loadAssets();
		} else {
		  g.drawPixmap(cobra, SHIP_X, SHIP_Y);
		}

		drawInformation(g);
		drawEquipment(g);
		drawLasers(g);
		drawGameTime(g);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (cobra != null) {
			cobra.dispose();
			cobra = null;
		}
	}

	@Override
	public void loadAssets() {
		if (cobra != null) {
			cobra.dispose();
		}
		cobra = game.getGraphics().newPixmap("cobra_small.png");
		super.loadAssets();
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		alite.setScreen(new StatusScreen(alite));
		return true;
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.STATUS_SCREEN;
	}
}
