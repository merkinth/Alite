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
import java.io.Serializable;
import java.nio.FloatBuffer;

import android.opengl.GLES11;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;

public class TargetBox implements Serializable {
	private static final long serialVersionUID = 9054522060986894235L;

	private transient FloatBuffer lineBuffer;
	private final float wh, hh, dh;

	public TargetBox(float width, float height, float depth) {
		wh = width / 2;
		hh = height / 2;
		dh = depth / 2;

		initialize();
	}

	private void initialize() {
	    float[] vertexData = new float[] {
			-wh, -hh, -dh,    wh, -hh, -dh,   wh, hh, -dh,  -wh, hh, -dh,
			-wh, -hh,  dh,    wh, -hh,  dh,   wh, hh,  dh,  -wh, hh,  dh,
		};

		createLines(vertexData,
			0, 1, 1, 2, 2, 3, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7, 4, 1, 5, 2, 6, 3, 7);
	}

	private void createLines(float[] vertexData, int ...indices) {
		float[] lines = new float[indices.length * 3];

		int offset = 0;
		for (int i: indices) {
			lines[offset]     = vertexData[i * 3];
			lines[offset + 1] = vertexData[i * 3 + 1];
			lines[offset + 2] = -vertexData[i * 3 + 2];
			offset += 3;
		}
		lineBuffer = GlUtils.toFloatBufferPositionZero(lines);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "TargetBox.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "TargetBox.readObject I");
			initialize();
			AliteLog.d("readObject", "TargetBox.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	public void render(int color, float a) {
		Alite.get().getTextureManager().setTexture(null);
		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glVertexPointer(3, GLES11.GL_FLOAT, 0, lineBuffer);
		Alite.get().getGraphics().setColor(color, a);
		GLES11.glDisable(GLES11.GL_LIGHTING);
		GLES11.glLineWidth(5);
		GLES11.glDrawArrays(GLES11.GL_LINES, 0, 24);
		GLES11.glLineWidth(1);
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glEnable(GLES11.GL_LIGHTING);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
	}
}
