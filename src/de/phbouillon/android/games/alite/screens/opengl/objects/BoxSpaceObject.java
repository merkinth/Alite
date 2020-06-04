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

import android.graphics.Color;
import de.phbouillon.android.framework.impl.gl.Box;
import de.phbouillon.android.framework.math.Vector3f;

public class BoxSpaceObject extends AliteObject {
	private static final long serialVersionUID = -290076973449626682L;

	private final Box box;

	public BoxSpaceObject(String name, float width, float height, float depth) {
		super(name);
		box = new Box(width, height, depth);
		boundingSphereRadius = width;
		hudColor = Color.WHITE;
		setDepthTest(false);
	}

	@Override
	public void render() {
		box.render();
	}

	public void setColor(float r, float g, float b) {
		box.setColor(r, g, b, 1.0f);
	}

	public void setColor(float r, float g, float b, float a) {
		box.setColor(r, g, b, a);
	}

	public void setAlpha(float a) {
		box.setAlpha(a);
	}

	public void setFarPlane(Vector3f far) {
		box.setFarPlane(far);
	}

	public boolean intersect(Vector3f origin, Vector3f direction) {
		float [] vertices = box.getVertices();
		float [] verts = new float[vertices.length + 1];
		float [] matrix = getMatrix();
		for (int i = 0; i < vertices.length; i += 3) {
			calculateVertex(verts, i, matrix, vertices, i, 1);
		}
		return intersectInternal(vertices.length / 3, origin, direction, verts);
	}
}
