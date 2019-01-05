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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import de.phbouillon.android.framework.Audio;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.framework.Game;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.TimeFactorChangeListener;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.screens.canvas.FatalExceptionScreen;
import de.phbouillon.android.games.alite.screens.opengl.TextureManager;

public abstract class AndroidGame extends Activity implements Game, Renderer {
	enum GLGameState {
		Initialized,
		Running,
		Paused,
		Finished,
		Idle
	}

	public static boolean resetting = false;

	private GLSurfaceView glView;
	private Graphics graphics;
	private Audio audio;
	private Input input;
	private FileIO fileIO;
	private Screen screen;
	private final int targetHeight;
	private final int targetWidth;
	private int deviceHeight;
	private int deviceWidth;
	private GLGameState state = GLGameState.Initialized;
	private long startTime = System.nanoTime();
	private long lastTime = startTime;
	private int frames = 0;
	private int timeFactor = 1;
	public static float fps;
	public static float scaleFactor;
	protected final TextureManager textureManager;
	private int[] sizeArray = null;
	private FatalExceptionScreen fatalException = null;
	private TimeFactorChangeListener timeFactorChangeListener = null;

	public AndroidGame(int targetWidth, int targetHeight) {
		this.targetHeight = targetHeight;
		this.targetWidth = targetWidth;
		textureManager = new TextureManager(this);
	}

	public void setTimeFactorChangeListener(TimeFactorChangeListener tfl) {
		timeFactorChangeListener = tfl;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Point result = new Point();
		AndroidUtil.getDisplaySize(result, getWindowManager().getDefaultDisplay());

		deviceWidth = result.x;
		deviceHeight = result.y;
		glView = new GLSurfaceView(this);
		glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		glView.setRenderer(this);
		if (fileIO == null) {
			fileIO = new AndroidFileIO(this);
		}
		AliteLog.d("AndroidGame", "Width/Height of Device: " + deviceWidth + ", " + deviceHeight);
		audio = new AndroidAudio(this, fileIO);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(glView);
		if (input != null) {
			input.dispose();
		}
		createInputIfNecessary();
	}

	protected void createInputIfNecessary() {
		if (input == null || input.isDisposed()) {
			boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
			int frameBufferWidth = isLandscape ? targetWidth : targetHeight;
			int frameBufferHeight = isLandscape ? targetHeight : targetWidth;
			Rect aspect = calculateTargetRect(new Rect(0, 0, deviceWidth, deviceHeight));
			input = new AndroidInput(this, glView, frameBufferWidth / (float) aspect.width(),
				frameBufferHeight / (float) aspect.height(), aspect.left, aspect.top);
			AliteLog.d("Calculating Sizes", "Sizes in OC: Width: " + deviceWidth + ", Height: " + deviceHeight + ", Aspect: " + aspect.left + ", " + aspect.top + ", " + aspect.right + ", " + aspect.bottom);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public int[] getSize() {
		if (sizeArray == null) {
			sizeArray = new int[] {deviceWidth, deviceHeight};
		}
		return sizeArray;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (glView != null) {
			glView.onResume();
		}
		if (screen != null) {
			screen.resume();
		}
		state = GLGameState.Running;
	}

	@Override
	public void onPause() {
		super.onPause();
		glView.onPause();
		SoundManager.stopAll();
		if (screen != null) {
			if (fatalException == null) {
				if (resetting) {
					resetting = false;
				} else {
					saveState(getCurrentScreen());
				}
			}
			screen.pause();
		}

		fatalException = null;
		if (isFinishing()) {
			state = GLGameState.Finished;
		} else {
			state = GLGameState.Paused;
		}
		if (isFinishing() && screen != null) {
			screen.dispose();
			screen = null;
		}

	}

	protected abstract void saveState(Screen screen);

	@Override
	public Input getInput() {
		return input;
	}

	@Override
	public FileIO getFileIO() {
		return fileIO;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public synchronized void setScreen(Screen screen) {
		if (screen == null) {
			throw new IllegalArgumentException("Screen must not be null");
		}

		this.screen.pause();
		textureManager.clear();

		this.screen = null;
		screen.loadAssets();
		this.screen = screen;
		screen.activate();
		screen.resume();
		screen.update(0);
		graphics.setClip(-1, -1, -1, -1);
	}

	@Override
	public Screen getCurrentScreen() {
		return screen;
	}

	protected View getCurrentView() {
		return glView;
	}

	private Rect calculateTargetRect(Rect rect) {
		int width = rect.right - rect.left; // "right" and "left" aren't named correctly here; right - left yields the width!
		int height = rect.bottom - rect.top; // No adding of 1 required -- same is true for height...
		float xFactor = targetWidth / (float) width;
		float yFactor = targetHeight / (float) height;

		AliteLog.d("[ALITE-CTR]", "Rect: " + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom);
		AliteLog.d("[ALITE-CTR]", "Width: " + width + ", Height: " + height);
		AliteLog.d("[ALITE-CTR]", "TargetWidth: " + targetWidth + ", targetHeight: " + targetHeight);
		AliteLog.d("[ALITE-CTR]", "Factors: " + xFactor + ", " + yFactor);

		int x1;
		int y1;
		int x2;
		int y2;
		if (xFactor > yFactor) {
			x1 = rect.left;
			x2 = rect.right;
			height = (int) (targetHeight / xFactor);
			y1 = rect.top + (rect.height() - height) >> 1;
			y2 = y1 + height;
			scaleFactor = 1.0f / xFactor;
		} else {
			y1 = rect.top;
			y2 = rect.bottom;
			width = (int) (targetWidth / yFactor);
			x1 = rect.left + (rect.width() - width) >> 1;
			x2 = x1 + width;
			scaleFactor = 1.0f / yFactor;
		}
		return new Rect(x1, y1, x2, y2);
	}

	@SuppressLint("NewApi")
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		AliteLog.debugGlVendorData();
		AliteLog.d("AndroidGame", "onSurfaceCreated is called on " + screen);
		Point result = new Point();
		AndroidUtil.getDisplaySize(result, getWindowManager().getDefaultDisplay());
		deviceWidth = result.x;
		deviceHeight = result.y;

		Rect aspect = calculateTargetRect(new Rect(0, 0, deviceWidth, deviceHeight));
		float ratio = aspect.width() / (float) aspect.height();
		GlUtils.setViewport(aspect);
		GlUtils.gluPerspective(this, 45.0f, ratio, 1.0f, 900000.0f);

		AliteLog.d("Recalculating Sizes", "Sizes in OSC: Width: " + deviceWidth + ", Height: " + deviceHeight + ", Aspect: " + aspect.left + ", " + aspect.top + ", " + aspect.right + ", " + aspect.bottom);

		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		graphics = new AndroidGraphics(fileIO, scaleFactor, aspect, textureManager);

		screen = getStartScreen();
//		glView.onResume();
		afterSurfaceCreated();

		AliteLog.d("AndroidGame", "Calling activation on " + screen.getClass().getName());
		screen.loadAssets();
		screen.activate();
		screen.resume();
		startTime = System.nanoTime();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
	}

	private void drawFatalException() {
		try {
			if (fatalException != null) {
				fatalException.update(0);
			}
			if (fatalException != null) {
				fatalException.present(0);
			}
		} catch (Throwable t) {
			throw new RuntimeException(AliteConfig.GAME_NAME + " has ended with an uncaught exception while trying to process an uncaught exception.", t);
		}
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		if (fatalException != null) {
			drawFatalException();
			return;
		}
		try {
			GLGameState state = this.state;
			long nanoTime = System.nanoTime();
			while ((nanoTime - startTime) < 33333333L) {
				nanoTime = System.nanoTime();
			}
			if (state == GLGameState.Running && getCurrentView() == glView) {
				float deltaTime = (nanoTime - startTime) / 1000000000.0f;
				startTime = nanoTime;
				screen.update(deltaTime);
				screen.present(deltaTime);
				screen.postPresent(deltaTime);
				screen.renderNavigationBar();
				screen.postNavigationRender(deltaTime);
				frames++;
				if (lastTime == 0) {
					lastTime = nanoTime;
				} else {
					if (Settings.displayFrameRate && (nanoTime - lastTime) >= 1000000000L) {
						fps = frames;
						frames = 0;
						lastTime = nanoTime;
					}
				}
			}
			if (state == GLGameState.Paused) {
				screen.pause();
				this.state = GLGameState.Idle;
			}
			if (state == GLGameState.Finished) {
				screen.pause();
				screen.dispose();
				this.state = GLGameState.Idle;
			}
		} catch (Throwable t) {
			AliteLog.e("Uncaught Exception", "Alite has ended with an uncaught exception.", t);
			fileIO.deleteFile(AliteStartManager.ALITE_STATE_FILE);
			fatalException = new FatalExceptionScreen(t);
			fatalException.activate();
		}
	}

	public void afterSurfaceCreated() {
	}

	public int getTimeFactor() {
		return timeFactor;
	}

	public void setTimeFactor(int tf) {
		if (timeFactorChangeListener != null && timeFactor != tf) {
			timeFactorChangeListener.timeFactorChanged(timeFactor, tf);
		}
		timeFactor = tf;
	}
}
