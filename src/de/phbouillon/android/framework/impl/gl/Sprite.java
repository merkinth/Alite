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
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.framework.SpriteData;

public class Sprite implements Serializable {
	private static final long serialVersionUID = -8424677289585617300L;

	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer texCoordBuffer;

	private final Rect position;
	private final Rect textureCoords;
	private final String textureFilename;

	public Sprite(float left, float top, float right, float bottom, float tLeft, float tTop, float tRight,
			float tBottom, final String textureFilename) {
		vertexBuffer = GlUtils.allocateFloatBuffer(4 * 8);
		texCoordBuffer = GlUtils.allocateFloatBuffer(4 * 8);

		position = new Rect();
		textureCoords = new Rect(tLeft, tTop, tRight, tBottom);
		setPosition(left, top, right, bottom);

		texCoordBuffer.put(tLeft);
		texCoordBuffer.put(tTop);
		texCoordBuffer.put(tLeft);
		texCoordBuffer.put(tBottom);
		texCoordBuffer.put(tRight);
		texCoordBuffer.put(tTop);
		texCoordBuffer.put(tRight);
		texCoordBuffer.put(tBottom);

		texCoordBuffer.position(0);

		this.textureFilename = textureFilename;
		Alite.get().getTextureManager().addTexture(textureFilename);
	}

	public Sprite(int left, int top, SpriteData spriteData, String textureFilename) {
		this(left, top, left + spriteData.origWidth, top + spriteData.origHeight,
			spriteData.x, spriteData.y, spriteData.x2, spriteData.y2, textureFilename);
	}

	public Sprite(int left, int top, int width, int height, SpriteData spriteData, String textureFilename) {
		this(left, top, left + width - 1, top + height - 1,
			spriteData.x, spriteData.y, spriteData.x2, spriteData.y2, textureFilename);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "Sprite.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "Sprite.readObject I");
			vertexBuffer = GlUtils.allocateFloatBuffer(4 * 8);
			texCoordBuffer = GlUtils.allocateFloatBuffer(4 * 8);
			setTextureCoords(textureCoords.left, textureCoords.top, textureCoords.right, textureCoords.bottom);
			setPosition(position);
			AliteLog.d("readObject", "Sprite.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Sprite " + textureFilename, e);
			throw e;
		}
	}

	public void setTextureCoords(float tLeft, float tTop, float tRight, float tBottom) {
		texCoordBuffer.clear();
		texCoordBuffer.put(tLeft);
		texCoordBuffer.put(tTop);
		texCoordBuffer.put(tLeft);
		texCoordBuffer.put(tBottom);
		texCoordBuffer.put(tRight);
		texCoordBuffer.put(tTop);
		texCoordBuffer.put(tRight);
		texCoordBuffer.put(tBottom);
		texCoordBuffer.position(0);
	}

	public void setTextureCoords(SpriteData spriteData) {
		setTextureCoords(spriteData.x, spriteData.y, spriteData.x2, spriteData.y2);
	}

	public void setPosition(Rect rect) {
		setPosition(rect.left, rect.top, rect.right, rect.bottom);
	}

	public void scale(float scale, float left, float top, float right, float bottom) {
		float cx = (right - left) / 2.0f + left;
		float cy = (bottom - top) / 2.0f + top;
		float width = (right - left) / 2.0f * scale;
		float height = (bottom - top) / 2.0f * scale;
		float newLeft = cx - width;
		float newRight = cx + width;
		float newTop = cy - height;
		float newBottom = cy + height;
		setPosition(newLeft, newTop, newRight, newBottom);
	}

	public void setPosition(float left, float top, float right, float bottom) {
		vertexBuffer.clear();
		Graphics g = Alite.get().getGraphics();
		vertexBuffer.put(g.transX((int) left));
		vertexBuffer.put(g.transY((int) top));
		vertexBuffer.put(g.transX((int) left));
		vertexBuffer.put(g.transY((int) bottom));
		vertexBuffer.put(g.transX((int) right));
		vertexBuffer.put(g.transY((int) top));
		vertexBuffer.put(g.transX((int) right));
		vertexBuffer.put(g.transY((int) bottom));
		vertexBuffer.position(0);
		position.left = left;
		position.top = top;
		position.right = right;
		position.bottom = bottom;
	}

	public Rect getPosition() {
		return position;
	}

	protected void setUp() {
		GLES11.glDisable(GLES11.GL_LIGHTING);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);

		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);

		Alite.get().getTextureManager().setTexture(textureFilename);

		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public void render() {
		setUp();
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
		cleanUp();
	}

	public void simpleRender() {
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);

		Alite.get().getTextureManager().setTexture(textureFilename);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
	}

	public void justRender() {
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
	}

	protected void cleanUp() {
		GLES11.glDisable(GLES11.GL_BLEND);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
		GLES11.glEnable(GLES11.GL_LIGHTING);

		GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glDisableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
	}

	public void destroy() {
		Alite.get().getTextureManager().freeTexture(textureFilename);
	}
}
