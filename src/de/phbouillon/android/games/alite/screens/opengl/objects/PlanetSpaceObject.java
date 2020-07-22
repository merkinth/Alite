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

import android.opengl.GLES11;
import de.phbouillon.android.framework.impl.gl.Disk;
import de.phbouillon.android.framework.impl.gl.Sphere;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.framework.SpriteData;

public class PlanetSpaceObject extends AliteObject implements Serializable {
	private static final long serialVersionUID = -8124332316784922499L;
	private final Disk rings;
	private final Disk ringShadow;
	private final Sphere planet;
	private final Sphere clouds;
	private final Sphere atmosphere;

	public PlanetSpaceObject(SystemData system, boolean preview) {
		super("Planet");
		Alite alite = Alite.get();
		if (system == null) {
			system = alite.getGenerator().getSystems()[0];
		}
		final float planetRadius    = preview ? 10000.0f : 30000.0f;
		final float ringStart       = preview ? 11000.0f : 31000.0f;
		final float ringSize        = preview ? 25000.0f : 55000.0f;
		final float cloudStart      = preview ? 10150.0f : 30150.0f;
		final float atmosphereStart = preview ? 10300.0f : 30800.0f; //30300.0f;

		if (alite.getGenerator().getCurrentGalaxy() == 1 && system.getIndex() == SystemData.LAVE_SYSTEM_INDEX) {
			alite.getTextureManager().addTexture("textures/planets/lave.png");
			planet = new Sphere(planetRadius, 32, 32, "textures/planets/lave.png", null, false);
			rings = null;
			ringShadow = null;
		} else if (system == SystemData.RAXXLA_SYSTEM) {
			alite.getTextureManager().addTexture("textures/planets/bdwarf.png");
			planet = new Sphere(planetRadius, 32, 32, "textures/planets/bdwarf.png", null, false);
			rings = new Disk(ringStart, ringSize, 80, 360, 60, 20, 256, "textures/planets/ring16.png");
			ringShadow = new Disk(ringStart, ringSize, 360, 80, 20, 60, 256, "textures/planets/ring16s.png");
		} else {
			int planetTexture = system.getPlanetTexture();
			int fileIndex = planetTexture / 8 + 1;
			alite.getTextureManager().addTexture("textures/planets/0" + fileIndex + ".png");
			SpriteData spriteData = alite.getTextureManager().getSprite("textures/planets/0" + fileIndex + ".png", "" + (planetTexture + 1));
			planet = new Sphere(planetRadius, 32, 32, "textures/planets/0" + fileIndex + ".png", spriteData, false);
			int ringsTexture = system.getRingsTexture();
			rings = ringsTexture != 0 ?
				new Disk(ringStart, ringSize, 80, 360, 60, 20, 256, "textures/planets/ring" + ringsTexture + ".png") : null;
			ringShadow = ringsTexture != 0 ? new Disk(ringStart, ringSize, 360, 80, 20, 60, 256, "textures/planets/ring" + ringsTexture + "s.png") : null;
		}

		AliteLog.d("Planet Debugger", "Planet " + system.getName() + " has " + (rings == null ? "no rings." : "rings with texture ring" + system.getRingsTexture()));
		int cloudsTexture = system.getCloudsTexture();
		clouds = cloudsTexture != 0 ?
					new Sphere(cloudStart, 32, 32, "textures/planets/clouds" + cloudsTexture + ".png", null, false) : null;
		atmosphere = new Sphere(atmosphereStart, 32, 32, "textures/atmosphere2.png", null, false);
		boundingSphereRadius = rings == null ? atmosphereStart : ringSize;
		distanceFromCenterToBorder = boundingSphereRadius;
	}

	@Override
	public void render() {
		GLES11.glDisable(GLES11.GL_LIGHTING);
		planet.render();
		GLES11.glDisable(GLES11.GL_DEPTH_TEST);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		if (clouds != null) {
			clouds.render();
		}
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE);
		GLES11.glEnable(GLES11.GL_DEPTH_TEST);
		atmosphere.render();

		GLES11.glEnable(GLES11.GL_LIGHTING);
		GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		if (rings != null) {
			rings.render();
			ringShadow.render();
		}
		GLES11.glDisable(GLES11.GL_DEPTH_TEST);

		GLES11.glEnable(GLES11.GL_CULL_FACE);
		Alite.get().getTextureManager().setTexture(null);
	}

	public void dispose() {
		if (atmosphere != null) {
			atmosphere.destroy();
		}
		if (clouds != null) {
			clouds.destroy();
		}
		if (planet != null) {
			planet.destroy();
		}
		if (rings != null) {
			rings.destroy();
		}
		if (ringShadow != null) {
			ringShadow.destroy();
		}
	}
}
