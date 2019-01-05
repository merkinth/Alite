package de.phbouillon.android.games.alite.screens.canvas;

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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.NavigationBar;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public abstract class AliteScreen extends Screen {
	protected int startX = -1;
	protected int startY = -1;
	protected int lastX = -1;
	protected int lastY = -1;

	private String message = null;
	private MessageType msgType = MessageType.OK;
	private Button ok = null;
	private Button yes = null;
	private Button no = null;
	protected Screen newScreen;
	protected boolean disposed = false;
	protected int messageResult = 0;
	private TextData[] messageTextData;
	private boolean messageIsModal = false;
	private boolean largeMessage = false;
	protected transient Alite game;

	public enum MessageType {
		OK,
		YES_NO
	}

	public AliteScreen(Alite game) {
		this.game = game;
		Alite.setDefiningScreen(this);
		Rect visibleArea = game.getGraphics().getVisibleArea();

		setUpForDisplay(visibleArea);
	}

	public void setMessage(String msg) {
		setMessage(msg, MessageType.OK, Assets.titleFont);
	}

	protected void setQuestionMessage(String msg) {
		setMessage(msg, MessageType.YES_NO, Assets.titleFont);
	}

	private void setMessage(String msg, MessageType messageType, GLText font) {
		msgType = messageType;
		message = msg;
		Graphics g = game.getGraphics();
		messageTextData = computeTextDisplay(g, message, 530, 410, 660, 60,
			ColorScheme.get(ColorScheme.COLOR_MESSAGE), font, false);
	}

	void setModalQuestionMessage(String msg) {
		setMessage(msg, MessageType.YES_NO, Assets.regularFont);
		messageIsModal = true;
	}

		void setLargeModalMessage(String msg) {
		msgType = MessageType.YES_NO;
		message = msg;
		Graphics g = game.getGraphics();
		largeMessage = true;
		messageTextData = computeTextDisplay(g, message, 380, 310, 960, 60,
			ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont, false);
		messageIsModal = true;
	}

	public String getMessage() {
		return message;
	}

	protected void centerText(String text, int y, GLText f, int color) {
		centerText(text, 0, AliteConfig.DESKTOP_WIDTH, y, f, color);
	}

	void centerText(String text, int minX, int maxX, int y, GLText f, int color) {
		int center = maxX + minX - game.getGraphics().getTextWidth(text, f) >> 1;
		game.getGraphics().drawText(text, center, y, color, f);
	}

	void centerTextWide(String text, int y, GLText f, int color) {
		centerText(text, 0, AliteConfig.SCREEN_WIDTH, y, f, color);
	}

	protected void displayTitle(String title) {
		displayTitle(title, AliteConfig.DESKTOP_WIDTH);
	}

	private void displayTitle(String title, int width) {
		Graphics g = game.getGraphics();
		g.verticalGradientRect(0, 0, width - 1, 80,
			ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT));
		g.drawPixmap(Assets.aliteLogoSmall, 20, 5);
		g.drawPixmap(Assets.aliteLogoSmall, width - 120, 5);
		centerText(title, 60, Assets.titleFont, ColorScheme.get(ColorScheme.COLOR_MESSAGE));
	}

	void displayWideTitle(String title) {
		Graphics g = game.getGraphics();
		g.verticalGradientRect(0, 0, 1919, 80, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT));
		g.drawPixmap(Assets.aliteLogoSmall, 20, 5);
		g.drawPixmap(Assets.aliteLogoSmall, 1800, 5);
		centerTextWide(title, 60, Assets.titleFont, ColorScheme.get(ColorScheme.COLOR_MESSAGE));
	}

	private void renderMessage(AliteScreen screen) {
		if (message != null) {
			Graphics g = game.getGraphics();
			if (largeMessage) {
				g.verticalGradientRect(360, 240, 1000, 600, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
				g.rec3d(360, 240, 1000, 600, 5, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
			} else {
				g.verticalGradientRect(510, 340, 700, 400, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
				g.rec3d(510, 340, 700, 400, 5, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
			}
			displayText(g, messageTextData);
			if (msgType == MessageType.OK) {
				if (ok == null) {
					Alite.setDefiningScreen(screen);
					ok = largeMessage ? Button.createRegularButton(1210, 690, 150, 150, "OK") :
						Button.createRegularButton(1060, 590, 150, 150, "OK");
					ButtonRegistry.get().addMessageButton(ok);
				}
				ok.render(g);
			} else if (msgType == MessageType.YES_NO) {
				if (yes == null) {
					Alite.setDefiningScreen(screen);
					yes = largeMessage ? Button.createRegularButton(1010, 690, 150, 150, "Yes") :
						Button.createRegularButton(860, 590, 150, 150, "Yes");
					ButtonRegistry.get().addMessageButton(yes);
				}
				if (no == null) {
					Alite.setDefiningScreen(screen);
					no = largeMessage ? Button.createRegularButton(1210, 690, 150, 150, "No") :
						Button.createRegularButton(1060, 590, 150, 150, "No");
					ButtonRegistry.get().addMessageButton(no);
				}
				yes.render(g);
				no.render(g);
			}
		}
	}

	@Override
	public synchronized void update(float deltaTime) {
		NavigationBar navBar = game.getNavigationBar();
		newScreen = null;
		for (TouchEvent event: game.getInput().getTouchEvents()) {
			if (event.type == TouchEvent.TOUCH_DOWN && event.x >= 1920 - NavigationBar.SIZE) {
				startX = event.x;
				startY = lastY = event.y;
			}
			if (event.type == TouchEvent.TOUCH_DRAGGED && event.x >= 1920 - NavigationBar.SIZE) {
				if (event.y < lastY) {
					navBar.increasePosition(lastY - event.y);
				} else {
					navBar.decreasePosition(event.y - lastY);
				}
				lastY = event.y;
			}
			if (event.type == TouchEvent.TOUCH_UP) {
				if (Math.abs(startX - event.x) < 20 &&
					Math.abs(startY - event.y) < 20) {
					newScreen = navBar.touched(game, event.x, event.y);
				}
			}
			ButtonRegistry.get().processTouch(event);
			processTouch(event);
		}
		if (newScreen != null) {
			performScreenChange();
			postScreenChange();
		}
	}

	protected synchronized void updateWithoutNavigation(float deltaTime) {
		newScreen = null;
		for (TouchEvent event: game.getInput().getTouchEvents()) {
			ButtonRegistry.get().processTouch(event);
			processTouch(event);
		}
		if (newScreen != null) {
			performScreenChange();
			postScreenChange();
		}
	}

	boolean inFlightScreenChange() {
		Screen oldScreen = game.getCurrentScreen();
		if (oldScreen instanceof FlightScreen) {
			// We're in flight, so do not dispose the flight screen,
			// but the flight's information screen
			FlightScreen flightScreen = (FlightScreen) oldScreen;
			Screen oldInformationScreen = flightScreen.getInformationScreen();
			if (oldInformationScreen != null) {
				oldInformationScreen.dispose();
			}
			newScreen.loadAssets();
			newScreen.activate();
			newScreen.resume();
			newScreen.update(0);
			game.getGraphics().setClip(-1, -1, -1, -1);
			flightScreen.setInformationScreen((AliteScreen) newScreen);
			game.getNavigationBar().performScreenChange();
			return true;
		}
		return false;
	}

	protected void performScreenChange() {
		if (inFlightScreenChange()) {
			return;
		}
		Screen oldScreen = game.getCurrentScreen();
		if (!(newScreen instanceof TextInputScreen)) {
			oldScreen.dispose();
		}
		game.setScreen(newScreen);
		game.getNavigationBar().performScreenChange();
		postScreenChange();
		oldScreen = null;
	}

	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (message != null && !messageIsModal) {
			if (touch.x < 510 || touch.y < 340 || touch.x > 1210 || touch.y > 740) {
				SoundManager.play(Assets.click);
				message = null;
				yes = null;
				no = null;
				messageResult = -1;
				touch.x = -1;
				touch.y = -1;
				game.getInput().getTouchEvents();
				ButtonRegistry.get().clearMessageButtons();
			}
		}
		if (ok != null && ok.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			message = null;
			ok = null;
			touch.x = -1;
			touch.y = -1;
			game.getInput().getTouchEvents();
			ButtonRegistry.get().clearMessageButtons();
			messageIsModal = false;
			largeMessage = false;
			return;
		}

		if (yes != null && yes.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			message = null;
			yes = null;
			no = null;
			messageResult = 1;
			touch.x = -1;
			touch.y = -1;
			game.getInput().getTouchEvents();
			ButtonRegistry.get().clearMessageButtons();
			messageIsModal = false;
			largeMessage = false;
			return;
		}

		if (no != null && no.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			message = null;
			yes = null;
			no = null;
			messageResult = -1;
			touch.x = -1;
			touch.y = -1;
			game.getInput().getTouchEvents();
			ButtonRegistry.get().clearMessageButtons();
			messageIsModal = false;
			largeMessage = false;
		}
	}

	final TextData[] computeCenteredTextDisplay(Graphics g, String text, int x, int y, int fieldWidth, int color) {
		return computeTextDisplay(g, text, x, y, fieldWidth, 40, color, Assets.regularFont, true);
	}

	protected final TextData[] computeTextDisplay(Graphics g, String text, int x, int y, int fieldWidth, int deltaY, int color) {
		return computeTextDisplay(g, text, x, y, fieldWidth, deltaY, color, Assets.regularFont, false);
	}

	private TextData[] computeTextDisplay(Graphics g, String text, int x, int y, int fieldWidth, int deltaY, int color, GLText font, boolean centered) {
		String[] textWords = text.split(" ");
		List <TextData> resultList = new ArrayList<>();
		String result = "";
		int width = 0;
		int count = 0;
		for (String word: textWords) {
			if (result.isEmpty()) {
				result = word;
				width = g.getTextWidth(result, font);
			} else {
				String test = result + " " + word;
				int testWidth = g.getTextWidth(test, font);
				if (testWidth > fieldWidth) {
					int offset = centered ? fieldWidth - width >> 1 : 0;
					resultList.add(new TextData(result, x + offset, y + deltaY * count++, color, font));
					result = word;
					width = g.getTextWidth(result, font);
				} else {
					result = test;
					width = testWidth;
				}
			}
		}
		if (!result.isEmpty()) {
			int offset = centered ? fieldWidth - width >> 1 : 0;
			resultList.add(new TextData(result, x + offset, y + deltaY * count, color, font));
		}
		return resultList.toArray(new TextData[0]);
	}

	protected final void displayText(Graphics g, TextData[] textToDisplay) {
		if (textToDisplay == null) {
			return;
		}
		for (TextData td: textToDisplay) {
			g.drawText(td.text, td.x, td.y, td.color, td.font);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		ButtonRegistry.get().removeButtons(this);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void postNavigationRender(float deltaTime) {
	}

	protected final void setUpForDisplay(Rect visibleArea) {
		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glDisable(GLES11.GL_LIGHTING);
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0);
		GLES11.glDisable(GLES11.GL_TEXTURE_2D);

		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.ortho(game, visibleArea);

		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	public void renderNavigationBar() {
		game.getNavigationBar().render(game.getGraphics());
	}

	@Override
	public void postPresent(float deltaTime) {
		renderMessage(this);
	}

	@Override
	public void postScreenChange() {
	}
}
