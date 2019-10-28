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

import android.graphics.Color;
import de.phbouillon.android.framework.impl.gl.Cylinder;

public class CylinderSpaceObject extends AliteObject implements Serializable {
	private static final long serialVersionUID = -6155477859420162646L;

	private final Cylinder cylinder;

	public CylinderSpaceObject(String name, float length, float radius, int segments, boolean hasTop, boolean hasBottom, String texture) {
		super(name);
		cylinder = new Cylinder(length, radius, segments, hasTop, hasBottom, texture);
		boundingSphereRadius = length / 2.0f;
		hudColor = Color.WHITE;
	}

	public void setColor(float r, float g, float b, float a) {
		cylinder.setColor(r, g, b, a);
	}

	@Override
	public void render() {
		cylinder.render();
	}
}
