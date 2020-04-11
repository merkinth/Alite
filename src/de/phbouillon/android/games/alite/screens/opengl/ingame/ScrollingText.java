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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteConfig;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.colors.ColorScheme;

class ScrollingText implements Serializable {
	private static final long serialVersionUID = -9124382771601908756L;

	private final String textToDisplay;
	private float x = AliteConfig.SCREEN_WIDTH;
	private int width;

	ScrollingText() {
		long diffInSeconds = TimeUnit.SECONDS.convert(Alite.get().getGameTime(), TimeUnit.NANOSECONDS);
		int diffInDays = (int) (diffInSeconds / 86400);
		diffInSeconds -= diffInDays * 86400;
		int diffInHours = (int) (diffInSeconds / 3600);
		diffInSeconds -= diffInHours * 3600;
		int diffInMinutes = (int) (diffInSeconds / 60);

		textToDisplay = L.string(R.string.msg_pause_game_scrolling_info, AliteConfig.GAME_NAME,
			AliteConfig.VERSION_STRING, L.plurals(R.plurals.game_time_days, diffInDays, diffInDays),
			L.plurals(R.plurals.game_time_hours, diffInHours, diffInHours),
			L.plurals(R.plurals.game_time_minutes, diffInMinutes, diffInMinutes),
			Alite.get().getPlayer().getScore());
		width = (int) Assets.regularFont.getWidth(textToDisplay,1.5f);
	}

	void render(float deltaTime) {
		x -= deltaTime * 128.0f;
		Alite.get().getGraphics().drawText(textToDisplay, (int) x, 400,
			AliteColor.colorAlpha(ColorScheme.get(ColorScheme.COLOR_SCROLLING_TEXT), 1.0f),
			Assets.regularFont, 1.5f);
		if (x + width < -20) {
			x = AliteConfig.SCREEN_WIDTH;
		}
	}
}
