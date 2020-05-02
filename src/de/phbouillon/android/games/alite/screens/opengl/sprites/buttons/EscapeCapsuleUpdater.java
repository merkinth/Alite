package de.phbouillon.android.games.alite.screens.opengl.sprites.buttons;

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
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

class EscapeCapsuleUpdater implements IMethodHook {
	private static final long serialVersionUID = -296076467539527770L;

	private enum EscapeCapsuleState {
		SPAWN,
		MOVE,
		QUIT
	}

	private final InGameManager inGame;
	private final Timer timer = new Timer();
	private EscapeCapsuleState state = EscapeCapsuleState.SPAWN;
	private SpaceObject cobra = null;
	private SpaceObject esc = null;
	private final Vector3f vec1 = new Vector3f(0, 0, 0);
	private final Vector3f vec2 = new Vector3f(0, 0, 0);

	EscapeCapsuleUpdater(InGameManager inGame) {
		this.inGame = inGame;
	}

	@Override
	public void execute(float deltaTime) {
		switch (state) {
			case SPAWN: spawnShip();
						spawnEscapeCapsule();
						break;
			case MOVE: moveShip();
					   moveEscapeCapsule(deltaTime);
					   break;
			case QUIT: endSequence(); break;
		}
	}

	private void spawnShip() {
		GraphicObject ship = inGame.getShip();
		ship.computeMatrix();
		cobra = SpaceObjectFactory.getInstance().getObjectById("cobra_mk_iii");
		cobra.setUpVector(ship.getUpVector());
		cobra.setRightVector(ship.getRightVector());
		cobra.setForwardVector(ship.getForwardVector());
		cobra.applyDeltaRotation(15, 15, -25);
		ship.getPosition().copy(vec1);
		ship.getForwardVector().copy(vec2);
		vec2.scale(-800);
		vec1.add(vec2);
		ship.getUpVector().copy(vec2);
		vec2.scale(-50);
		vec1.add(vec2);
		cobra.setPosition(vec1);
		cobra.setSpeed(-cobra.getMaxSpeed());
		ship.setSpeed(0);
		state = EscapeCapsuleState.MOVE;
		inGame.addObject(cobra);
	}

	private void spawnEscapeCapsule() {
		esc = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.EscapeCapsule);
		cobra.getForwardVector().copy(vec1);
		vec1.negate();
		esc.setForwardVector(vec1);

		esc.setRightVector(cobra.getRightVector());
		cobra.getUpVector().copy(vec1);
		vec1.negate();
		esc.setUpVector(vec1);
		cobra.getPosition().copy(vec1);
		esc.setPosition(vec1);
		esc.setSpeed(-esc.getMaxSpeed());
		inGame.addObject(esc);
	}

	private void moveShip() {
		if (timer.hasPassedSeconds(4)) {
			state = EscapeCapsuleState.QUIT;
		}
		// Nothing else to be done here; InGameRender advances the cobra...
	}

	private void moveEscapeCapsule(float deltaTime) {
		esc.moveForward(deltaTime);
	}

	private void endSequence() {
		inGame.terminateToStatusScreen();
	}
}
