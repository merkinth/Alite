package de.phbouillon.android.games.alite.model.generator.enums;

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

import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;

public enum Government {
	ANARCHY,
	FEUDAL,
	MULTI_GOVERNMENT,
	DICTATORSHIP,
	COMMUNIST,
	CONFEDERACY,
	DEMOCRACY,
	CORPORATE_STATE;

	public String getDescription() {
		switch (this) {
			case ANARCHY: return L.string(R.string.government_anarchy);
			case FEUDAL: return L.string(R.string.government_feudal);
			case MULTI_GOVERNMENT: return L.string(R.string.government_multi_government);
			case DICTATORSHIP: return L.string(R.string.government_dictatorship);
			case COMMUNIST: return L.string(R.string.government_communist);
			case CONFEDERACY: return L.string(R.string.government_confederacy);
			case DEMOCRACY: return L.string(R.string.government_democracy);
			case CORPORATE_STATE: return L.string(R.string.government_corporate_state);
		}
		return "";
	}

}
