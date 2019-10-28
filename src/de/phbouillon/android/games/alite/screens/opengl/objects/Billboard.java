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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;

import android.opengl.GLES11;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.screens.opengl.sprites.SpriteData;

public class Billboard extends AliteObject {
	private static final long serialVersionUID = 6210999939098179291L;

	protected transient FloatBuffer vertexBuffer;
	protected transient FloatBuffer texCoordBuffer;
	protected String textureFilename;
	private float rotation;
	private float width, height;
	private final float tLeft, tTop, tRight, tBottom;

	public Billboard(String name, float centerX, float centerY, float centerZ, float width, float height,
		float tLeft, float tTop, float tRight, float tBottom, String textureFilename) {
		super(name);
		this.width = width;
		this.height = height;
		this.tLeft = tLeft;
		this.tTop = tTop;
		this.tRight = tRight;
		this.tBottom = tBottom;
		this.textureFilename = textureFilename;
		Alite.get().getTextureManager().addTexture(textureFilename);
		worldPosition.x = centerX;
		worldPosition.y = centerY;
		worldPosition.z = centerZ;

		init();

		boundingSphereRadius = 100.0f;
	}

	Billboard(String name, float centerX, float centerY, float centerZ, float width, float height,
		String textureFilename, SpriteData sprite) {
		super(name);
		this.textureFilename = textureFilename;
		Alite.get().getTextureManager().addTexture(textureFilename);
		this.width = width;
		this.height = height;

		worldPosition.x = centerX;
		worldPosition.y = centerY;
		worldPosition.z = centerZ;

		if (sprite != null) {
			tLeft = sprite.x;
			tTop = sprite.y;
			tRight = sprite.x2;
			tBottom = sprite.y2;
		} else {
			tLeft = -1;
			tTop = -1;
			tRight = -1;
			tBottom = -1;
		}

		init();

		boundingSphereRadius = 100.0f;
	}

	private void init() {
		vertexBuffer   = GlUtils.allocateFloatBuffer(32);
		texCoordBuffer = GlUtils.allocateFloatBuffer(32);

		float w = width / 2.0f;
		float h = height / 2.0f;
		vertexBuffer.put(-w);
		vertexBuffer.put(-h);
		vertexBuffer.put(-w);
		vertexBuffer.put(h);
		vertexBuffer.put(w);
		vertexBuffer.put(-h);
		vertexBuffer.put(w);
		vertexBuffer.put(h);

		if (tLeft >= 0) {
			texCoordBuffer.put(tLeft);
			texCoordBuffer.put(tTop);
			texCoordBuffer.put(tLeft);
			texCoordBuffer.put(tBottom);
			texCoordBuffer.put(tRight);
			texCoordBuffer.put(tTop);
			texCoordBuffer.put(tRight);
			texCoordBuffer.put(tBottom);
		}

		vertexBuffer.position(0);
		texCoordBuffer.position(0);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "Billboard.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "Billboard.readObject I");
			init();
			AliteLog.d("readObject", "Billboard.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	void updateTextureCoordinates(SpriteData sprite) {
		if (sprite == null) {
			// This can only happen if the player pauses the game when an
			// explosion billboard is on the screen... We ignore it, it will
			// be cleaned up in later frames anyway.
			return;
		}
		texCoordBuffer.clear();
		texCoordBuffer.put(sprite.x);
		texCoordBuffer.put(sprite.y);
		texCoordBuffer.put(sprite.x);
		texCoordBuffer.put(sprite.y2);
		texCoordBuffer.put(sprite.x2);
		texCoordBuffer.put(sprite.y);
		texCoordBuffer.put(sprite.x2);
		texCoordBuffer.put(sprite.y2);
		texCoordBuffer.position(0);
	}

	public void update(GraphicObject camera) {
		setForwardVector(camera.getPosition().x - worldPosition.x,
			camera.getPosition().y - worldPosition.y,
			camera.getPosition().z - worldPosition.z);
		camera.getUpVector().cross(forwardVector, rightVector);
		rightVector.normalize();
		forwardVector.cross(rightVector, upVector);
		upVector.normalize();
		computeMatrix();
	}

	public void resize(float newWidth, float newHeight) {
		width = newWidth;
		height = newHeight;
		float w = width / 2.0f;
		float h = height / 2.0f;
		vertexBuffer.clear();
		vertexBuffer.put(-w);
		vertexBuffer.put(-h);
		vertexBuffer.put(-w);
		vertexBuffer.put(h);
		vertexBuffer.put(w);
		vertexBuffer.put(-h);
		vertexBuffer.put(w);
		vertexBuffer.put(h);
		vertexBuffer.position(0);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setRotation(float r) {
		rotation = r;
	}

	public float getRotation() {
		return rotation;
	}

	@Override
	public void render() {
		GLES11.glDisableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);
		GLES11.glDisable(GLES11.GL_LIGHTING);
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Alite.get().getTextureManager().setTexture(textureFilename);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
		GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glEnable(GLES11.GL_LIGHTING);
	}

	void batchRender() {
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, vertexBuffer);
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, texCoordBuffer);
		Alite.get().getTextureManager().setTexture(textureFilename);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
	}
}
