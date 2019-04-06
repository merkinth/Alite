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

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.games.alite.screens.opengl.objects.IMethodHook;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TimedEvent implements Serializable {
	private static final long serialVersionUID = -7887711369377615831L;

	private final Timer timer = new Timer().setAutoReset();
	long delay;
	private IMethodHook method;
	private long pauseTime;
	private boolean remove;
	protected boolean locked;

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (!remove) {
			out.defaultWriteObject();
		}
	}

	public TimedEvent(long delayInNanos) {
		this(delayInNanos, -1, -1, null);
	}

	public TimedEvent(long delayInNanos, long lastExecutionTime, long pauseTime, IMethodHook method) {
		delay = delayInNanos;
		if (lastExecutionTime != -1) {
			timer.setTimer(lastExecutionTime);
		}
		this.pauseTime = pauseTime;
		this.method = method;
	}

	protected void remove() {
		remove = true;
	}

	boolean mustBeRemoved() {
		return remove;
	}

	void updateDelay(long newDelay) {
		delay = newDelay;
		timer.reset();
		pauseTime = -1;
	}

	long timeToNextTrigger() {
		return delay - timer.getPassedNanos();
	}
	long getLastExecutionTime() {
		return timer.getTimer();
	}

	void perform() {
		if (pauseTime == -1 && !locked) {
			if (timer.hasPassedNanos(delay)) {
				if (method != null) method.execute(0);
				else doPerform();
			}
		}
	}

	public void lock() {
		locked = true;
	}

	void unlock() {
		if (locked) {
			timer.reset();
			locked = false;
		}
	}

	public long pause() {
		if (pauseTime == -1) {
			pauseTime = timer.getPassedNanos();
		}
		return pauseTime;
	}

	public boolean isPaused() {
		return pauseTime != -1;
	}

	public void resume() {
		if (pauseTime != -1) {
			timer.setTimer(pauseTime);
			pauseTime = -1;
		}
	}

	public void doPerform() {
	}
}
