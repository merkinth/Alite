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

import android.graphics.Bitmap;
import de.phbouillon.android.framework.ResourceStream;
import de.phbouillon.android.framework.Texture;
import de.phbouillon.android.framework.SpriteData;

public class TestTexture implements Texture {
	@Override
	public int addTexture(String fileName) {
		return 0;
	}

	@Override
	public int addTextureFromStream(String fileName, ResourceStream textureInputStream) {
		return 0;
	}

	@Override
	public boolean checkTexture(String fileName) {
		return false;
	}

	@Override
	public int addTexture(String name, Bitmap bitmap) {
		return 0;
	}

	@Override
	public void freeTexture(String fileName) {

	}

	@Override
	public void setTexture(String fileName) {

	}

	@Override
	public void setTexture(String fileName, ResourceStream textureInputStream) {

	}

	@Override
	public SpriteData getSprite(String fileName, String spriteName) {
		return new SpriteData("", 0, 0, 100, 100, 100, 100);
	}

	@Override
	public void freeAllTextures() {

	}

	@Override
	public void reloadAllTextures() {

	}

	@Override
	public void clear() {

	}
}
