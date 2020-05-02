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
import de.phbouillon.android.games.alite.model.generator.StringUtil;

public class HyperspaceTimer extends TimedEvent {
	private static final long serialVersionUID = -855725511472476223L;

	private int countDown;
	private boolean isIntergalactic;

	HyperspaceTimer(InGameManager inGame, boolean isIntergalactic) {
		super(1000000000L);
		this.isIntergalactic = isIntergalactic;
		countDown = isIntergalactic ? 30 : 10;
		inGame.getMessage().setText(StringUtil.format("%d", countDown));
		addAlarmEvent(new IMethodHook() {
			private static final long serialVersionUID = 8631878861995197334L;
			@Override
			public void execute(float deltaTime) {
				countDown--;
				SoundManager.play(Assets.click);
				if (countDown == 0) {
					SoundManager.stopAll();
					if (inGame.getHyperspaceHook() != null) {
						inGame.getHyperspaceHook().execute(0);
					} else {
						inGame.performHyperspaceJump(isIntergalactic);
					}
				}
				inGame.getMessage().setText(StringUtil.format("%d", countDown));
			}
		});
	}

	public boolean isIntergalactic() {
		return isIntergalactic;
	}
}
