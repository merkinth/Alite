package de.phbouillon.android.games.alite.screens.opengl;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLES11;
import android.opengl.GLUtils;
import de.phbouillon.android.framework.Game;
import de.phbouillon.android.framework.MemUtil;
import de.phbouillon.android.framework.ResourceStream;
import de.phbouillon.android.framework.Texture;
import de.phbouillon.android.framework.impl.Pool;
import de.phbouillon.android.framework.impl.Pool.PoolObjectFactory;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.framework.SpriteData;

public class TextureManager implements Texture {
	private static final int MAX_TEXTURES = 500;

	private static class Texture implements Serializable {
		private static final long serialVersionUID = 3417797379929074799L;

		final int[] index = new int[1];

		public boolean isValid() {
			return index[0] != 0;
		}
	}

	private final Map <String, Texture> textures = Collections.synchronizedMap(new HashMap<>());
	private final Set <String> bitmaps = Collections.synchronizedSet(new HashSet<>());
	private final Game game;
	private final Map <String, SpriteData> sprites = Collections.synchronizedMap(new HashMap<>());
	private static int count = 1;
	private final PoolObjectFactory <Texture> factory = new PoolObjectFactory<TextureManager.Texture>() {
		private static final long serialVersionUID = -7926981761767264375L;

		@Override
		public TextureManager.Texture createObject() {
			AliteLog.d("New Texture", "New Texture created. Count == " + count++);
			return new TextureManager.Texture();
		}
	};
	private final Pool <Texture> texturePool = new Pool<>(factory, MAX_TEXTURES);

	public TextureManager(Game game) {
		this.game = game;

	}

	@Override
	public int addTexture(String fileName) {
		return addTextureFromStream(fileName, null);
	}

	@Override
	public int addTextureFromStream(String fileName, ResourceStream textureInputStream) {
		if (bitmaps.contains(fileName)) {
			return 0;
		}
		Texture texture = textures.get(fileName);
		if (texture == null || !texture.isValid()) {
			texture = forceAddTexture(fileName, textureInputStream);
		}
		return texture.index[0];
	}

	@Override
	public boolean checkTexture(String fileName) {
		Texture texture = textures.get(fileName);
		return texture != null && texture.isValid();
	}

	@Override
	public int addTexture(String name, Bitmap bitmap) {
		Texture texture = textures.get(name);
		if (texture != null) {
			freeTexture(name);
		}
		bitmaps.add(name);
		texture = texturePool.newObject();
		texture.index[0] = 0;
		GLES11.glGenTextures(1, texture.index, 0);
		loadTexture(bitmap, texture.index[0]);
		textures.put(name, texture);
		return texture.index[0];
	}

	private Texture forceAddTexture(String fileName, ResourceStream textureInputStream) {
		Texture texture = texturePool.newObject();
		texture.index[0] = 0;
		GLES11.glGenTextures(1, texture.index, 0);
		if (!fileName.isEmpty()) {
			loadTexture(fileName, texture.index[0], textureInputStream);
		}
		textures.put(fileName, texture);
		return texture;
	}

	@Override
	public void freeTexture(String fileName) {
		Texture texture = textures.get(fileName);
		if (texture != null && texture.isValid()) {
			GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
			GLES11.glDeleteTextures(1, texture.index, 0);
			GLES11.glFinish();
			textures.put(fileName, null);
			bitmaps.remove(fileName);
			texturePool.free(texture);
		}
	}

	@Override
	public void setTexture(String fileName) {
		setTexture(fileName, null);
	}

	@Override
	public void setTexture(String fileName, ResourceStream textureInputStream) {
		if (fileName == null) {
			GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
			return;
		}
		Texture texture = textures.get(fileName);
		if (texture == null || !texture.isValid()) {
			addTextureFromStream(fileName, textureInputStream);
			texture = textures.get(fileName);
		}
		if (texture != null) {
			if (!texture.isValid()) {
				texture.index[0] = addTextureFromStream(fileName, textureInputStream);
			}

			if (texture.index[0] != 0) {
				GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, texture.index[0]);
			}
		}
	}

	@Override
	public SpriteData getSprite(String fileName, String spriteName) {
		return sprites.get(fileName + ":" + spriteName);
	}

	@Override
	public synchronized void freeAllTextures() {
		Iterator <String> iterator = Collections.synchronizedSet(textures.keySet()).iterator();
		ArrayList <String> toBeRemoved = new ArrayList<>();
		while (iterator.hasNext()) {
			String fileName = iterator.next();
			Texture texture = textures.get(fileName);
			if (texture != null) {
				GLES11.glDeleteTextures(1, texture.index, 0);
				texturePool.free(texture);
				toBeRemoved.add(fileName);
			}
		}
		for (String s: toBeRemoved) {
			textures.put(s, null);
		}
		bitmaps.clear();
		sprites.clear();
	}

	@Override
	public void reloadAllTextures() {
		for (String fileName : textures.keySet()) {
			Texture texture = textures.get(fileName);
			if (texture != null) {
				if (!bitmaps.contains(fileName)) {
					continue;
				}
			}
			if (!bitmaps.contains(fileName)) {
				forceAddTexture(fileName, null);
			}
		}
	}

	@Override
	public synchronized void clear() {
		AliteLog.d("Clearing all Textures Mem Dump (PRE)", AliteLog.getInstance().getMemoryData());
		freeAllTextures();
		textures.clear();
		AliteLog.d("Clearing all Textures Mem Dump (POST)", AliteLog.getInstance().getMemoryData());
	}

	private void parseTextures(String fileName, InputStream textureAtlas, float width, float height) throws IOException {
		if (textureAtlas == null) {
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(textureAtlas));
		String line = br.readLine();
		String[] parts;
		String[] coords;
		String[] size;
		while (line != null) {
			if (line.trim().isEmpty()) {
				line = br.readLine();
				continue;
			}
			parts = line.split(";");
			if (parts.length != 3) {
				AliteLog.e("Error in Sprite atlas", line);
			} else {
				coords = parts[1].split(",");
				size = parts[2].split(",");
				float x = Float.parseFloat(coords[0]) / Settings.textureLevel;
				float y = Float.parseFloat(coords[1]) / Settings.textureLevel;
				float sx = Float.parseFloat(size[0]) / Settings.textureLevel;
				float sy = Float.parseFloat(size[1]) / Settings.textureLevel;
				sprites.put(fileName + ":" + parts[0], new SpriteData(parts[0], x / width, y / height,
					(x + sx - 1) / width, (y + sy - 1) / height, Float.parseFloat(size[0]), Float.parseFloat(size[1])));
			}
			line = br.readLine();
		}
	}

	private Bitmap newBitmap(String fileName, ResourceStream textureInputStream) {
		try (InputStream in = textureInputStream == null ? game.getFileIO().readPrivateFile(fileName) : textureInputStream.getStream(fileName)) {
			Options options = new Options();
			options.inPreferredConfig = Settings.colorDepth == 1 ? Config.ARGB_8888 : Config.ARGB_4444;
			options.inSampleSize = Settings.textureLevel;
			Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
			if (bitmap == null) {
				throw new RuntimeException("Couldn't load bitmap from '" + fileName + "'");
			}
			return bitmap;
		} catch (IOException e) {
			AliteLog.e("Error loading bitmap", "Couldn't load bitmap from '" + fileName + "'", e);
		}
		return null;
	}

	private void loadTexture(String fileName, int index, ResourceStream textureInputStream) {
		AliteLog.d("Loading Texture: " + fileName, fileName + " Creating bitmap....");
		Bitmap bitmap = newBitmap(fileName, textureInputStream);
		if (bitmap == null) {
			return;
		}
		loadTexture(bitmap, index);
		try {
			String textureAtlas = fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
			if (game.getGraphics().existsAssetsFile(textureAtlas)) {
				try (InputStream textureAtlasInputStream = game.getFileIO().readPrivateFile(textureAtlas)) {
					parseTextures(fileName, textureAtlasInputStream, bitmap.getWidth(), bitmap.getHeight());
				} catch (IOException e) {
					AliteLog.e("Cannot read texture atlas.", e.getMessage(), e);
				}
			}
		} catch(IllegalArgumentException e) {
		    // 32-bit color depth on Android 8.0
            AliteLog.e("Loading Texture: " + fileName, e.getMessage(), e);
		} finally {
			MemUtil.freeBitmap(bitmap);
		}
	}

	private void loadTexture(Bitmap bitmap, int index) {
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, index);
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
		GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
	}
}
