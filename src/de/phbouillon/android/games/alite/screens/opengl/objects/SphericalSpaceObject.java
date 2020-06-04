package de.phbouillon.android.games.alite.screens.opengl.objects;

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

import de.phbouillon.android.framework.impl.gl.Sphere;

public class SphericalSpaceObject extends AliteObject implements Serializable {
	private static final long serialVersionUID = 5293882896307129631L;

	private final Sphere sphere;

	public SphericalSpaceObject(String name, float radius, String texture) {
		super(name);
		sphere = new Sphere(radius, 32, 32, texture, null, false);
		boundingSphereRadius = radius;
		distanceFromCenterToBorder = radius;
		setVisibleOnHud(true);
	}

	public float getRadius() {
		return sphere.getRadius();
	}

	public void setNewSize(float radius) {
		sphere.setNewSize(radius);
		boundingSphereRadius = radius;
		distanceFromCenterToBorder = radius;
	}

	protected void glSetUp() {
	}

	protected void glTearDown() {
	}

	@Override
	public void render() {
		glSetUp();
		sphere.render();
		glTearDown();
	}

	public void setColor(float r, float g, float b, float a) {
		sphere.setColor(r, g, b, a);
	}

}
