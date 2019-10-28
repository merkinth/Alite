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
import de.phbouillon.android.framework.impl.gl.Disk;
import de.phbouillon.android.games.alite.screens.opengl.IAdditionalGLParameterSetter;

public class DiskSpaceObject extends AliteObject implements Serializable {
	private static final long serialVersionUID = 5388571762772747540L;

	private final Disk disk;
	private IAdditionalGLParameterSetter additionalParameters = null;

	public DiskSpaceObject(String name, float innerRadius, float outerRadius, float beginAngle, float endAngle, int sections, String texture) {
		super(name);
		disk = new Disk(innerRadius, outerRadius, beginAngle, endAngle, beginAngle, endAngle, sections, texture);
		boundingSphereRadius = outerRadius;
		hudColor = Color.WHITE;
	}

	@Override
	public void render() {
		if (additionalParameters != null) {
			additionalParameters.setUp();
		}
		disk.render();
		if (additionalParameters != null) {
			additionalParameters.tearDown();
		}
	}

	public void setAdditionalGLParameters(IAdditionalGLParameterSetter additionalParameters) {
		this.additionalParameters = additionalParameters;
	}
}
