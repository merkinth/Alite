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

import de.phbouillon.android.framework.TimeUtil;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.colors.ColorScheme;

class OnScreenMessage implements Serializable {
	private static final long serialVersionUID = 1948958480746165833L;

	class DelayedText implements Serializable {
		private static final long serialVersionUID = -936827160142919485L;

		String text;
		long time;
		long duration;
		float scale;

		DelayedText(String text, long time) {
			this.text = text;
			this.time = System.nanoTime() + time * TimeUtil.SECONDS;
			duration = 5;
			scale = 1.0f;
		}
	}

	private String text = "";
	private long activationTime = 0;
	private long duration;
	private long repetitionInterval;
	private long lastRepetitionInactive = 0;
	private int repetitionTimes = -1;
	private long repetitionDuration;
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

	void setScaledTextForDuration(String text, long duration, float scale) {
		this.text = text;
		this.scale = scale;
		activate(duration);
	}

	void repeatText(String text, long interval) {
		if (repetitionText != null && repetitionText.equals(text)) {
			return;
		}
		scale = 1.0f;
		this.text = text;
		activate(1);
		repetitionDuration = 1;
		repetitionInterval = interval;
		repetitionText = text;
		repetitionTimes = -1;
	}

	void repeatText(String text, long interval, int times, long duration) {
		if (repetitionText != null && repetitionText.equals(text)) {
			return;
		}
		scale = 1.0f;
		this.text = text;
		activate(duration);
		repetitionDuration = duration;
		repetitionInterval = interval;
		repetitionText = text;
		repetitionTimes = -1;
	}

	void repeatText(String text, long interval, int times) {
		if (repetitionText != null && repetitionText.equals(text)) {
			return;
		}
		scale = 1.0f;
		this.text = text;
		activate(1);
		repetitionDuration = 1;
		repetitionInterval = interval;
		repetitionText = text;
		repetitionTimes = times;
	}

	void clearRepetition() {
		repetitionText = null;
	}

	private void activate(long duration) {
		activate(System.nanoTime(), duration);
	}

	private void activate(long nanoTime, long duration) {
		activationTime = nanoTime;
		this.duration = duration;
	}

	boolean isActive() {
		return activationTime > 0 && !TimeUtil.hasPassed(activationTime, duration, TimeUtil.SECONDS);
	}

	void render(Alite alite) {
		long time = System.nanoTime();
		DelayedText toBeRemoved = null;
		for (DelayedText dt: delayedTexts) {
			if (time >= dt.time) {
				text = dt.text;
				scale = 1.0f;
				activate(time, dt.duration);
				toBeRemoved = dt;
			}
		}
		if (toBeRemoved != null) {
			delayedTexts.remove(toBeRemoved);
		}
		if (!isActive()) {
			if (lastRepetitionInactive <= 0 && repetitionText != null) {
				lastRepetitionInactive = System.nanoTime();
			}
			if (repetitionText != null && TimeUtil.hasPassed(lastRepetitionInactive, repetitionInterval, TimeUtil.SECONDS)) {
				if (repetitionTimes > 0) {
					repetitionTimes--;
				} else if (repetitionTimes == 0) {
					clearRepetition();
					repetitionTimes = -1;
					return;
				}
				text = repetitionText;
				activate(repetitionDuration);
			}
			return;
		}
		lastRepetitionInactive = 0;
		alite.getGraphics().drawCenteredText(text, 960, 650, ColorScheme.get(ColorScheme.COLOR_HUD_MESSAGE),
			Assets.regularFont, scale);
	}
}
