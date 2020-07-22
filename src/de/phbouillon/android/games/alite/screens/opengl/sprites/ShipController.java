package de.phbouillon.android.games.alite.screens.opengl.sprites;

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

import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.framework.impl.gl.Sprite;
import de.phbouillon.android.games.alite.Alite;

import java.io.Serializable;

public abstract class ShipController implements Serializable {
	private static final long serialVersionUID = 1603088074583149L;
	private float accelY;
	private float accelZ;

	protected final Sprite genSprite(String name, Rect r) {
		// Careful: Rect is used as (x, y) - (width, height) here... So right and bottom are really width and height!
		return new Sprite((int) r.left, (int) r.top, (int) r.right, (int) r.bottom,
			Alite.get().getTextureManager().getSprite(AliteHud.TEXTURE_FILE, name), AliteHud.TEXTURE_FILE);
	}


	final float getZ() {
		return accelZ;
	}

	final float getY() {
		return accelY;
	}

	final void update(float deltaTime) {
		if (isLeft()) { // left
			accelY += deltaTime * (accelY < 0 ? 5 : 1.66f);
			if (accelY > 2) {
				accelY = 2;
			}
		} else if (isRight()) { // right
			accelY -= deltaTime * (accelY > 0 ? 5 : 1.66f);
			if (accelY < -2) {
				accelY = -2;
			}
		} else {
			if (accelY > 0) {
				accelY -= deltaTime * 3.33f;
				if (accelY < 0) {
					accelY = 0.0f;
				}
			} else if (accelY < 0) {
				accelY += deltaTime * 3.33f;
				if (accelY > 0) {
					accelY = 0.0f;
				}
			}
		}
		if (isUp()) { // up
			accelZ -= deltaTime * (accelZ > 0 ? 5 : 1.66f);
			if (accelZ < -2) {
				accelZ = -2;
			}
		} else if (isDown()) { // down
			accelZ += deltaTime * (accelZ < 0 ? 5 : 1.66f);
			if (accelZ > 2) {
				accelZ = 2;
			}
		} else {
			if (accelZ > 0) {
				accelZ -= deltaTime * 3.33f;
				if (accelZ < 0) {
					accelZ = 0.0f;
				}
			} else if (accelZ < 0) {
				accelZ += deltaTime * 3.33f;
				if (accelZ > 0) {
					accelZ = 0.0f;
				}
			}
		}
	}

	protected abstract boolean isDown();
	protected abstract boolean isUp();
	protected abstract boolean isRight();
	protected abstract boolean isLeft();

	abstract boolean handleUI(TouchEvent event);
	abstract void setDirections(boolean left, boolean right, boolean up, boolean down);
	abstract void render();
}
