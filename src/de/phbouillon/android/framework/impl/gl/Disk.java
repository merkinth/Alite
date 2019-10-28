package de.phbouillon.android.framework.impl.gl;

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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.FloatBuffer;

import android.opengl.GLES11;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;

public class Disk implements Serializable {
	private static final long serialVersionUID = -6937157348886910417L;

	private transient FloatBuffer normalBuffer;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer texCoordBuffer;

	private final int numberOfVertices;
	private int glDrawMode;
	private final String textureFilename;

	private float innerRadius;
	private float outerRadius;
	private float beginAngle;
	private float endAngle;
	private float beginAngleOuter;
	private float endAngleOuter;
	private int sections;

	public Disk(final float innerRadius, final float outerRadius, final float beginAngle, final float endAngle, final float beginAngleOuter, final float endAngleOuter, final int sections, final String textureFilename) {
		numberOfVertices = 2 * (sections + 1);
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.beginAngle  = beginAngle;
		this.endAngle    = endAngle;
		this.beginAngleOuter = beginAngleOuter;
		this.endAngleOuter = endAngleOuter;
		this.sections    = sections;
		vertexBuffer     = GlUtils.allocateFloatBuffer(4 * 3 * numberOfVertices);
		texCoordBuffer   = GlUtils.allocateFloatBuffer(4 * 2 * numberOfVertices);
		normalBuffer     = GlUtils.allocateFloatBuffer(4 * 3 * numberOfVertices);

		plotDiskPoints(innerRadius, outerRadius, (float) Math.toRadians(beginAngle),
				                                 (float) Math.toRadians(endAngle),
				                                 (float) Math.toRadians(beginAngleOuter),
				                                 (float) Math.toRadians(endAngleOuter),
				                                 sections);
		this.textureFilename = textureFilename;
		if (textureFilename != null) {
			Alite.get().getTextureManager().addTexture(textureFilename);
		}
		glDrawMode = GLES11.GL_TRIANGLE_STRIP;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "Disk.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "Disk.readObject II");
			vertexBuffer   = GlUtils.allocateFloatBuffer(4 * 3 * numberOfVertices);
			texCoordBuffer = GlUtils.allocateFloatBuffer(4 * 2 * numberOfVertices);
			normalBuffer   = GlUtils.allocateFloatBuffer(4 * 3 * numberOfVertices);
			plotDiskPoints(innerRadius, outerRadius, (float) Math.toRadians(beginAngle),
					                                 (float) Math.toRadians(endAngle),
					                                 (float) Math.toRadians(beginAngleOuter),
					                                 (float) Math.toRadians(endAngleOuter),
					                                 sections);
			if (textureFilename != null) {
				Alite.get().getTextureManager().addTexture(textureFilename);
			}
			glDrawMode = GLES11.GL_TRIANGLE_STRIP;
			AliteLog.d("readObject", "Disk.readObject III");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	private void writeObject(ObjectOutputStream out)
            throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Disk", e);
			throw e;
		}
    }

	 private void plotDiskPoints(float innerRadius, float outerRadius, float beginAngle, float endAngle, float beginAngleOuter, float endAngleOuter, int sections) {
		 float angle = beginAngle < endAngle ? endAngle - beginAngle : 2.0f * 3.1415926535f - beginAngle + endAngle;
		 float angleOuter = beginAngleOuter < endAngleOuter ? endAngleOuter - beginAngleOuter : 2.0f * 3.1415926535f - beginAngleOuter + endAngleOuter;

		 for (int i = 0; i <= sections; i++) {
			 float t = i / (float) sections;
			 float theta = beginAngle + t * angle;
			 if (theta > 2.0 * Math.PI) {
				 theta -= 2.0 * Math.PI;
			 }
			 float thetaOuter = beginAngleOuter + t * angleOuter;
			 if (thetaOuter > 2.0 * Math.PI) {
				 thetaOuter -= 2.0 * Math.PI;
			 }
			 float s = (float) Math.sin(theta);
			 float c = (float) Math.cos(theta);
			 float so = (float) Math.sin(thetaOuter);
			 float co = (float) Math.cos(thetaOuter);

			 texCoordBuffer.put(0f);
			 texCoordBuffer.put(0.5f);
			 vertexBuffer.put(s * innerRadius);
			 vertexBuffer.put(0f);
			 vertexBuffer.put(c * innerRadius);
			 normalBuffer.put(0);
			 normalBuffer.put(1);
			 normalBuffer.put(0);

			 texCoordBuffer.put(1f);
			 texCoordBuffer.put(0.5f);
			 vertexBuffer.put(so * outerRadius);
			 vertexBuffer.put(0f);
			 vertexBuffer.put(co * outerRadius);
			 normalBuffer.put(0);
			 normalBuffer.put(1);
			 normalBuffer.put(0);
		 }
		 texCoordBuffer.position(0);
		 normalBuffer.position(0);
		 vertexBuffer.position(0);
	}

	public void render() {
		GLES11.glNormalPointer(GLES11.GL_FLOAT, 0, normalBuffer);
		GLES11.glVertexPointer(3, GLES11.GL_FLOAT, 0, vertexBuffer);
		if (textureFilename != null) {
			GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);
			Alite.get().getTextureManager().setTexture(textureFilename);
		} else {
			GLES11.glDisable(GLES11.GL_LIGHTING);
			GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		}
		GLES11.glDrawArrays(glDrawMode, 0, numberOfVertices);
		if (textureFilename == null) {
			GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
			GLES11.glEnable(GLES11.GL_LIGHTING);
		}
	}

	public void destroy() {
		Alite.get().getTextureManager().freeTexture(textureFilename);
	}
}

