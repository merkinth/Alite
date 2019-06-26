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

//This screen never needs to be serialized, as it is not part of the InGame state.
public class QuantityPadScreen extends AliteScreen {
	private static final int OFFSET_X = 20;
	private static final int OFFSET_Y = 20;
	private static final int BUTTON_SIZE = 150;
	private static final int GAP = 10;

	private final int xPos;
	private final int yPos;
	private final int row;
	private final int column;
	private final String[] buttonTexts = new String[] {
		"7", "8", "9",
		"4", "5", "6",
		"1", "2", "3",
		"0", "<-", L.string(R.string.pad_btn_hex_ok)};
	private Button[] pads;
	private final String maxAmountString;
	private int currentAmount;
	private final BuyScreen marketScreen;

	public QuantityPadScreen(BuyScreen marketScreen, Alite game, String maxAmountString, int x, int y, int row, int column) {
		super(game);
		this.xPos = x;
		this.yPos = y;
		this.row = row;
		this.column = column;
		this.maxAmountString = maxAmountString;
		this.marketScreen = marketScreen;
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
		dos.writeInt(row);
		dos.writeInt(column);
		dos.writeInt(maxAmountString == null ? 0 : maxAmountString.length());
		if (maxAmountString != null) {
			dos.writeChars(maxAmountString);
		}
		String currentAmountString = Integer.toString(currentAmount);
		dos.writeInt(currentAmountString.length());
		dos.writeChars(currentAmountString);
		marketScreen.saveScreenState(dos);
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		try {
			int xPos = dis.readInt();
			int yPos = dis.readInt();
			int row = dis.readInt();
			int col = dis.readInt();
			int len = dis.readInt();
			String maxAmount = "";
			for (int i = 0; i < len; i++) {
				maxAmount += dis.readChar();
			}
			len = dis.readInt();
			String currentAmount = "";
			for (int i = 0; i < len; i++) {
				currentAmount += dis.readChar();
			}
			BuyScreen marketScreen = BuyScreen.readScreen(alite, dis);
			marketScreen.loadAssets();
			marketScreen.activate();
			QuantityPadScreen qps = new QuantityPadScreen(marketScreen, alite, maxAmount, xPos, yPos, row, col);
			qps.currentAmount = Integer.parseInt(currentAmount);
			alite.setScreen(qps);
		} catch (IOException e) {
			AliteLog.e("Quantity Pad Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
	}

	private void initializeButtons() {
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 3; x++) {
				pads[y * 3 + x] = Button.createRegularButton(xPos + x * (BUTTON_SIZE + GAP) + OFFSET_X,
					 yPos + y * (BUTTON_SIZE + GAP) + OFFSET_Y,
					 BUTTON_SIZE, BUTTON_SIZE,
					 y == 3 && x > 0 ? buttonTexts[y * 3 + x] : String.format(L.currentLocale, "%d",
						 Integer.parseInt(buttonTexts[y * 3 + x])));
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();

		marketScreen.present(deltaTime);
		int width = (BUTTON_SIZE + GAP) * 3 + 2 * OFFSET_X;
		int height =  (BUTTON_SIZE + GAP) * 4 + 2 * OFFSET_Y;
		g.verticalGradientRect(xPos, yPos, width, height, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		g.rec3d(xPos, yPos, width, height, 10, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		for (Button b: pads) {
			b.render(g);
		}
		g.fillRect(50, 1018, 1670, 56, ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		g.drawText(L.string(R.string.trade_amount_to_buy, maxAmountString, currentAmount == 0 ? "" : Integer.toString(currentAmount)),
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
			marketScreen.setBoughtAmount(0);
			newScreen = marketScreen;
		}
		for (Button b: pads) {
			if (b.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				String t = b.getText();
				if ("<-".equals(t)) {
					currentAmount /= 10;
				} else if (L.string(R.string.pad_btn_hex_ok).equals(t)) {
					marketScreen.setBoughtAmount(currentAmount);
					newScreen = marketScreen;
				} else {
					currentAmount = 10 * currentAmount + Integer.parseInt(t);
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
		game.setScreen(marketScreen);
		marketScreen.performTrade(row, column);
		game.getNavigationBar().performScreenChange();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.QUANTITY_PAD_SCREEN;
	}
}
