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
import java.util.ArrayList;
import java.util.List;

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.colors.ColorScheme;

class OnScreenMessage implements Serializable {
	private static final long serialVersionUID = 1948958480746165833L;

	class DelayedText implements Serializable {
		private static final long serialVersionUID = -936827160142919485L;

		String text;
		private Timer timer;
		private int delayInSec;
		long durationInSec;

		DelayedText(String text, int delayInSec) {
			this.text = text;
			this.delayInSec = delayInSec;
			timer = new Timer();
			durationInSec = 5;
		}

		boolean passed() {
			return timer.hasPassedSeconds(delayInSec);
		}
	}

	private String text = "";
	private final Timer activationTime = new Timer();
	private long durationInSec;
	private long repetitionIntervalInSec;
	private final Timer lastRepetitionInactive = new Timer();
	private int repetitionTimes = -1;
	private long repetitionDurationInSec;
	private String repetitionText;
	private float scale = 1.0f;
	private final List <DelayedText> delayedTexts = new ArrayList<>();

	void setText(String text) {
		this.text = text;
		scale = 1.0f;
		activate(5);
	}

	void setDelayedText(String text) {
		delayedTexts.add(new DelayedText(text, 10));
	}

	void setScaledTextForDuration(String text, long durationInSec, float scale) {
		this.text = text;
		this.scale = scale;
		activate(durationInSec);
	}

	void repeatText(String text, long intervalInSec) {
		repeatText(text, intervalInSec, -1, 1);
	}

	void repeatText(String text, long intervalInSec, int times, long durationInSec) {
		if (repetitionText != null && repetitionText.equals(text)) {
			return;
		}
		scale = 1.0f;
		this.text = text;
		activate(durationInSec);
		repetitionDurationInSec = durationInSec;
		repetitionIntervalInSec = intervalInSec;
		repetitionText = text;
		repetitionTimes = times;
	}

	void clearRepetition() {
		repetitionText = null;
	}

	private void activate(long durationInSec) {
		activationTime.reset();
		this.durationInSec = durationInSec;
	}

	boolean isActive() {
		return !activationTime.hasPassedSeconds(durationInSec);
	}

	void render() {
		DelayedText toBeRemoved = null;
		for (DelayedText dt: delayedTexts) {
			if (dt.passed()) {
				text = dt.text;
				scale = 1.0f;
				activate(dt.durationInSec);
				toBeRemoved = dt;
			}
		}
		if (toBeRemoved != null) {
			delayedTexts.remove(toBeRemoved);
		}
		if (isActive()) {
			lastRepetitionInactive.reset();
			Alite.get().getGraphics().drawCenteredText(text, 960, 650, ColorScheme.get(ColorScheme.COLOR_HUD_MESSAGE),
				Assets.regularFont, scale);
			return;
		}
		if (repetitionText == null || !lastRepetitionInactive.hasPassedSeconds(repetitionIntervalInSec)) {
			return;
		}
		if (repetitionTimes > 0) {
			repetitionTimes--;
		} else if (repetitionTimes == 0) {
			clearRepetition();
			repetitionTimes = -1;
			return;
		}
		text = repetitionText;
		activate(repetitionDurationInSec);
	}
}
