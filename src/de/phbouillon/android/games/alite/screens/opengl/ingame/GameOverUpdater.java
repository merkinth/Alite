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

import java.io.IOException;
import java.io.ObjectInputStream;

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.statistics.WeaponType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CobraMkIII;

class GameOverUpdater implements IMethodHook {
	private static final long serialVersionUID = 5497403578670138689L;

	private enum GameOverState {
		SPAWN,
		MOVE,
		EXPLODE,
		QUIT
	}

	private transient Alite alite;
	private final InGameManager inGame;
	private final GraphicObject ship;
	private final Timer startTime = new Timer();
	private GameOverState state = GameOverState.SPAWN;
	private CobraMkIII cobra = null;
	private boolean needsDestruction = true;
	private final Vector3f vec1 = new Vector3f(0, 0, 0);
	private final Vector3f vec2 = new Vector3f(0, 0, 0);

	GameOverUpdater(Alite alite, InGameManager inGame, GraphicObject ship) {
		this.alite = alite;
		this.inGame = inGame;
		this.ship = ship;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.e("readObject", "GameOverUpdater.readObject");
			in.defaultReadObject();
			AliteLog.e("readObject", "GameOverUpdater.readObject I");
			alite = Alite.get();
			AliteLog.e("readObject", "GameOverUpdater.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	@Override
	public void execute(float deltaTime) {
		switch (state) {
			case SPAWN: spawnShip(); break;
			case MOVE: moveShip(); break;
			case EXPLODE: destroyShip(); break;
			case QUIT: endSequence(); break;
		}
	}

	private void spawnShip() {
		ship.computeMatrix();
		cobra = new CobraMkIII(alite);
		cobra.setIdentified();
		cobra.setUpVector(ship.getUpVector());
		cobra.setRightVector(ship.getRightVector());
		cobra.setForwardVector(ship.getForwardVector());
		cobra.applyDeltaRotation(15, 15, -15);
		ship.getPosition().copy(vec1);
		ship.getForwardVector().copy(vec2);
		vec2.scale(-200);
		vec1.add(vec2);
		ship.getUpVector().copy(vec2);
		vec2.scale(-50);
		vec1.add(vec2);
		cobra.setPosition(vec1);
		cobra.setSpeed(-cobra.getMaxSpeed());
		ship.setSpeed(0);
		state = GameOverState.MOVE;
		inGame.addObject(cobra);
		inGame.getMessage().setScaledTextForDuration(L.string(R.string.msg_game_over), 14, 4.0f);
	}

	private void moveShip() {
		if (startTime.hasPassedSeconds(2)) {
			state = GameOverState.EXPLODE;
		}
		// Nothing else to be done here; InGameManager advances the cobra...
	}

	private void destroyShip() {
		if (needsDestruction) {
			cobra.setHullStrength(0);
			inGame.getLaserManager().explode(cobra, WeaponType.BeamLaser);
			needsDestruction = false;
		}
		if (startTime.hasPassedSeconds(10)) {
			state = GameOverState.QUIT;
		}
	}

	private void endSequence() {
		inGame.terminateToTitleScreen();
	}
}
