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

import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.SystemData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class LocalScreen extends GalaxyScreen {

	// default public constructor is required for navigation bar
	public LocalScreen() {
	}

	public LocalScreen(float zoomFactor, int centerX, int centerY) {
		super(zoomFactor, centerX, centerY);
	}

		@Override
	public void activate() {
		Player player = game.getPlayer();
		SystemData hyper = player.getHyperspaceSystem();
		initPosition(hyper == null ? player.getPosition().x : hyper.getX(),
			hyper == null ? player.getPosition().y : hyper.getY(), 4);
		activateScreen(L.string(R.string.title_local_nav_chart));
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.LOCAL_SCREEN;
	}
}
