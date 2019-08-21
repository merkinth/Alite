package de.phbouillon.android.framework.impl;

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

import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics.PixmapFormat;
import de.phbouillon.android.framework.MemUtil;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.screens.opengl.TextureManager;

public class AndroidPixmap implements Pixmap {
	private Bitmap bitmap;
	private final PixmapFormat format;
	private final String fileName;
	private final TextureManager textureManager;
	private final int width;
	private final int height;
	private final FloatBuffer vertexBuffer;
	private final FloatBuffer texCoordBuffer;
	private int lastX = -1;
	private int lastY = -1;
	private final float tx2;
	private final float ty2;

	AndroidPixmap(Bitmap bitmap, PixmapFormat format, String fileName, TextureManager textureManager, int width, int height, float tx2, float ty2) {
		this.format = format;
		this.fileName = fileName;
		this.textureManager = textureManager;
		this.bitmap = bitmap;
		textureManager.addTexture(fileName, bitmap);
		this.width = width;
		this.height = height;
		this.tx2 = tx2;
		this.ty2 = ty2;

		vertexBuffer = GlUtils.allocateFloatBuffer(4 * 8);
		texCoordBuffer = GlUtils.allocateFloatBuffer(4 * 8);
		setTextureCoordinates(0, 0, tx2, ty2);
		textureManager.setTexture(null);
	}

	@Override
	public int getWidth() {
		return (int) (width / AndroidGame.scaleFactor);
	}

	@Override
	public int getHeight() {
		return (int) (height / AndroidGame.scaleFactor);
	}

	@Override
	public PixmapFormat getFormat() {
		return format;
	}

	@Override
	public void dispose() {
		if (textureManager != null) {
			textureManager.freeTexture(fileName);
		} else {
			AliteLog.e("TEXTURE MANAGER LOST!", "Texture Manager is NULL!");
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			MemUtil.freeBitmap(bitmap);
		}
	}

	@Override
	public Bitmap getBitmap() {
		return bitmap;
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		if (this.bitmap != bitmap) {
			MemUtil.freeBitmap(this.bitmap);
		}
		this.bitmap = bitmap;
		textureManager.addTexture(fileName, bitmap);
	}

	@Override
	public void setTextureCoordinates(int left, int top, int right, int bottom) {
		float width = bitmap.getWidth();
		float height = bitmap.getHeight();
		setTextureCoordinates(left / width, top / height, right / width, bottom / height);
	}

	private void setTextureCoordinates(float left, float top, float right, float bottom) {
		texCoordBuffer.clear();
		texCoordBuffer.put(left);
		texCoordBuffer.put(top);
		texCoordBuffer.put(left);
		texCoordBuffer.put(bottom);
		texCoordBuffer.put(right);
		texCoordBuffer.put(top);
		texCoordBuffer.put(right);
		texCoordBuffer.put(bottom);
		texCoordBuffer.position(0);
	}

	@Override
	public void setCoordinates(int left, int top, int right, int bottom) {
		vertexBuffer.clear();
		vertexBuffer.put(left);
		vertexBuffer.put(top);
		vertexBuffer.put(left);
		vertexBuffer.put(bottom);
		vertexBuffer.put(right);
		vertexBuffer.put(top);
		vertexBuffer.put(right);
		vertexBuffer.put(bottom);
		vertexBuffer.position(0);
		lastX = -1;
		lastY = -1;
	}

	@Override
	public void resetTextureCoordinates() {
		setTextureCoordinates(0, 0, tx2, ty2);
	}

	@Override
	public void render(float alpha) {
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		if (!textureManager.checkTexture(fileName)) {
			if (bitmap != null && !bitmap.isRecycled()) {
				textureManager.addTexture(fileName, bitmap);
			}
		}
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		textureManager.setTexture(fileName);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
		GLES11.glDisable(GLES11.GL_BLEND);
		GLES11.glDisable(GLES11.GL_TEXTURE_2D);
		textureManager.setTexture(null);
		GLES11.glDisableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
	}

	@Override
	public void render(int x, int y) {
		render(x, y, 1);
	}

	@Override
	public void render(int x, int y, float pixmapAlpha) {
		if (x != lastX || y != lastY) {
			setCoordinates(x, y, x + width - 1, y + height - 1);
			lastX = x;
			lastY = y;
		}
		render(pixmapAlpha);
	}
}
