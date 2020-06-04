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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class QuantityPadScreen extends AliteScreen {
	private static final int OFFSET_X = 20;
	private static final int OFFSET_Y = 20;
	private static final int BUTTON_SIZE = 150;
	private static final int GAP = 10;

	private final int xPos;
	private final int yPos;
	private final int index;
	private final String[] buttonTexts = new String[] {
		"7", "8", "9",
		"4", "5", "6",
		"1", "2", "3",
		"0", "<-", L.string(R.string.pad_btn_hex_ok)};
	private Button[] pads;
	private final String unitString;
	private int currentAmount;
	private Screen parentScreen;
	private Performer changeScreen;
	private ParentPresenter parentPresenter;
	private boolean notModalViewWithNavigationBar;
	private final int maxAmount;
	private int maxAmountLabel;

	private interface Performer {
		void perform(int value);
	}

	private interface ParentPresenter {
		void present(float deltaTime);
	}

	public QuantityPadScreen(Screen parentScreen, int maxAmount, String unitString, int xPos, int yPos, int index) {
		setParentScreen(parentScreen);
		this.maxAmount = maxAmount;
		this.unitString = unitString;
		this.xPos = xPos;
		this.yPos = yPos;
		this.index = index;
	}

	public QuantityPadScreen(final DataInputStream dis) throws IOException {
		xPos = dis.readInt();
		yPos = dis.readInt();
		index = dis.readInt();
		maxAmount = dis.readInt();
		unitString = ScreenBuilder.readEmptyString(dis);
		currentAmount = Integer.parseInt(ScreenBuilder.readEmptyString(dis));
		parentScreen = new BuyScreen(ScreenBuilder.readString(dis));
		parentScreen.loadAssets();
		parentScreen.activate();
		setParentScreen(parentScreen);
	}

	private void setParentScreen(Screen parentScreen) {
		this.parentScreen = parentScreen;
		if (parentScreen instanceof BuyScreen) {
			changeScreen = value -> {
				((BuyScreen) parentScreen).setBoughtAmount(value);
				game.setScreen(parentScreen);
				((BuyScreen) parentScreen).performTrade(index);
			};
			parentPresenter = parentScreen::present;
			notModalViewWithNavigationBar = true;
			maxAmountLabel = R.string.trade_amount_to_buy;
			return;
		}
		changeScreen = value -> {
			newScreen = parentScreen;
			FlightScreen flightScreen = (FlightScreen) parentScreen;
			flightScreen.getInGameManager().performIntergalacticJump(value);
			flightScreen.setForwardView();
			flightScreen.setInformationScreen(null);
		};
		parentPresenter = deltaTime -> {
			((FlightScreen) game.getCurrentScreen()).renderObjects(deltaTime);
			setUpForDisplay();
		};
		maxAmountLabel = R.string.target_galaxy_number;
	}

	@Override
	public void activate() {
		pads = new Button[12];
		initializeButtons();
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(xPos);
		dos.writeInt(yPos);
		dos.writeInt(index);
		dos.writeInt(maxAmount);
		ScreenBuilder.writeString(dos, unitString);
		ScreenBuilder.writeString(dos, Integer.toString(currentAmount));
		parentScreen.saveScreenState(dos);
	}

	private void initializeButtons() {
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 3; x++) {
				pads[y * 3 + x] = Button.createRegularButton(xPos + x * (BUTTON_SIZE + GAP) + OFFSET_X,
					 yPos + y * (BUTTON_SIZE + GAP) + OFFSET_Y,
					 BUTTON_SIZE, BUTTON_SIZE,
					 y == 3 && x > 0 ? buttonTexts[y * 3 + x] : StringUtil.format("%d", Integer.parseInt(buttonTexts[y * 3 + x])));
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		parentPresenter.present(deltaTime);
		int width = (BUTTON_SIZE + GAP) * 3 + 2 * OFFSET_X;
		int height =  (BUTTON_SIZE + GAP) * 4 + 2 * OFFSET_Y;
		g.verticalGradientRect(xPos, yPos, width, height, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		g.rec3d(xPos, yPos, width, height, 10, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		for (Button b: pads) {
			b.render(g);
		}
		g.fillRect(50, 1012, 1670, 54, ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		g.drawText(L.string(maxAmountLabel, maxAmount + unitString, currentAmount == 0 ? "" : Integer.toString(currentAmount)),
			50, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		int width = (BUTTON_SIZE + GAP) * 3 + 2 * OFFSET_X;
		int height =  (BUTTON_SIZE + GAP) * 4 + 2 * OFFSET_Y;
		if (touch.x < xPos || touch.y < yPos || touch.x > xPos + width || touch.y > yPos + height) {
			if (notModalViewWithNavigationBar) {
				currentAmount = 0;
				newScreen = parentScreen;
			}
			return;
		}
		for (Button b: pads) {
			if (b.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				String t = b.getText();
				if ("<-".equals(t)) {
					currentAmount /= 10;
				} else if (L.string(R.string.pad_btn_hex_ok).equals(t)) {
					newScreen = parentScreen;
				} else {
					int testAmount = 10 * currentAmount + Integer.parseInt(t);
					if (testAmount <= maxAmount) {
						currentAmount = testAmount;
					}
				}
			}
		 }
	}

	public void clearAmount() {
		currentAmount = 0;
		newScreen = null;
	}

	public Screen getNewScreen() {
		return newScreen;
	}

	@Override
	protected void performScreenChange() {
		dispose();
		changeScreen.perform(currentAmount);
		game.getNavigationBar().performScreenChange();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.QUANTITY_PAD_SCREEN;
	}

	@Override
	public synchronized void update(float deltaTime) {
		if (notModalViewWithNavigationBar) {
			super.update(deltaTime);
		} else {
			updateWithoutNavigation(deltaTime);
		}
	}

	@Override
	public void renderNavigationBar() {
		if (notModalViewWithNavigationBar) {
			super.renderNavigationBar();
		}
	}
}
