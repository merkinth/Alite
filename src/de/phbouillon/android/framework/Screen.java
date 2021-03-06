package de.phbouillon.android.framework;

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

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Screen {
	private boolean disposed;

	public final boolean isDisposed() {
		return disposed;
	}

	public void loadAssets() {
		disposed = false;
	}

	public abstract void activate();
	public abstract void resume();
	public abstract void update(float deltaTime);
	public abstract void present(float deltaTime);
	public abstract void postPresent(float deltaTime);
	public abstract void renderNavigationBar();
	public abstract void postNavigationRender(float deltaTime);
	public abstract void pause();

	public void dispose() {
		disposed = true;
	}

	public abstract void postScreenChange();

	public abstract int getScreenCode();
	public void saveScreenState(DataOutputStream dos) throws IOException {
	}
}
