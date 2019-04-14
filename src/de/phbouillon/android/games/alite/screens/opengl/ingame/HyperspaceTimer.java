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

import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.SoundManager;

public class HyperspaceTimer extends TimedEvent {
	private static final long serialVersionUID = -855725511472476223L;

	private int countDown;
	private transient IMethodHook hyperspaceHook;

	HyperspaceTimer(InGameManager inGame, boolean isIntergalactic) {
		super(1000000000L);
		countDown = isIntergalactic ? 30 : 10;
		inGame.getMessage().setText("" + countDown);
		addAlarmEvent(deltaTime -> {
			countDown--;
			SoundManager.play(Assets.click);
			if (countDown == 0) {
				SoundManager.stopAll();
				if (hyperspaceHook != null) {
					hyperspaceHook.execute(0);
				} else {
					inGame.performHyperspaceJump(isIntergalactic);
				}
			}
			inGame.getMessage().setText("" + countDown);
		});
	}

	public void setHyperspaceHook(IMethodHook hyperspaceHook) {
		this.hyperspaceHook = hyperspaceHook;
	}

	public IMethodHook getHyperspaceHook() {
		return hyperspaceHook;
	}

}
