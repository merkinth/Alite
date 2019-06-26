package de.phbouillon.android.games.alite.model;

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

public enum Rating {
	HARMLESS       (2048),
	MOSTLY_HARMLESS(4096),
	POOR           (8192),
	AVERAGE        (16384),
	ABOVE_AVERAGE  (32768),
	COMPETENT      (65536),
	DANGEROUS      (262144),
	DEADLY         (655360),
	ELITE          (-1);

	private int upToScore;

	Rating(int upToScore) {
		this.upToScore = upToScore;
	}

	public String getName() {
		switch (this) {
			case HARMLESS: return L.string(R.string.rating_harmless);
			case MOSTLY_HARMLESS: return L.string(R.string.rating_mostly_harmless);
			case POOR: return L.string(R.string.rating_poor);
			case AVERAGE: return L.string(R.string.rating_average);
			case ABOVE_AVERAGE: return L.string(R.string.rating_above_average);
			case COMPETENT: return L.string(R.string.rating_competent);
			case DANGEROUS: return L.string(R.string.rating_dangerous);
			case DEADLY: return L.string(R.string.rating_deadly);
			case ELITE: return L.string(R.string.rating_elite);
		}
		return "";
	}

	public int getScoreThreshold() {
		return upToScore;
	}

}
