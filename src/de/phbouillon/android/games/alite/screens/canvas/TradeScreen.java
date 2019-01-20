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

import java.util.Locale;

import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.framework.TimeUtil;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Button;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public abstract class TradeScreen extends AliteScreen {
	int X_OFFSET = 150;
	int Y_OFFSET = 100;
	int SIZE     = 225;
	int GAP_X    = 300;
	int GAP_Y    = 300;
	int COLUMNS  = 5;
	int ROWS     = 3;

	Pixmap[] beam;
	Pixmap[] tradeGoods;
	Button[][] tradeButton = null;

	int currentFrame = 0;
	long startSelectionTime = 0;
	Button selection = null;

	private boolean continuousAnimation;
	String cashLeft = null;
	String errorText = null;

	TradeScreen(Alite game, boolean continuousAnimation) {
		super(game);
		this.continuousAnimation = continuousAnimation;
	}

	protected abstract void createButtons();

	protected abstract String getCost(int row, int column);
	protected abstract void performTrade(int row, int column);
	protected abstract void presentSelection(int row, int column);

	protected void drawAdditionalTradeGoodInformation(int row, int column, float deltaTime) {
		// The default implementation does nothing.
	}

	int getSelectionIndex() {
		if (selection == null) {
			return -1;
		}
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (tradeButton[x][y] == null) {
					continue;
				}
				if (selection == tradeButton[x][y]) {
					return y * COLUMNS + x;
				}
			}
		}
		return -1;
	}

	private void computeCurrentFrame() {
		if (!continuousAnimation) {
			if (TimeUtil.hasPassed(startSelectionTime, 5, TimeUtil.SECONDS)) {
				currentFrame = 0;
			}
			if (currentFrame > 15) {
				return;
			}
		}
		if (TimeUtil.hasPassed(startSelectionTime, 42, TimeUtil.MILLIS)) { // 1/24 second
			startSelectionTime = System.nanoTime();
			currentFrame++;
			if (continuousAnimation && currentFrame > 15) {
				currentFrame = 1;
			}
		}
	}

	void presentTradeStatus() {
		Player player = game.getPlayer();
		Graphics g = game.getGraphics();
		String cash = getOneDecimalFormatString("%d.%d", player.getCash());
		String freeCargo = player.getCobra().getFreeCargo().getStringWithoutUnit();
		String spareText = Weight.getUnitString(player.getCobra().getFreeCargo().getAppropriateUnit()) + " spare";
		int cashTextWidth = g.getTextWidth("Cash:_", Assets.regularFont);
		int cashWidth = g.getTextWidth(cash, Assets.regularFont);
		int holdTextWidth = g.getTextWidth("Cr      Hold:_", Assets.regularFont);
		int freeCargoWidth = g.getTextWidth(freeCargo, Assets.regularFont);
		int spaceWidth = g.getTextWidth("_", Assets.regularFont);

		int halfWidth = cashTextWidth + cashWidth + spaceWidth +
				         holdTextWidth + freeCargoWidth + spaceWidth +
				         g.getTextWidth(spareText, Assets.regularFont) >> 1;
		int currentX = 860 - halfWidth;

		g.drawText("Cash: ", currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		currentX += cashTextWidth;
		g.drawText(cash, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.regularFont);
		currentX += cashWidth + spaceWidth;
		g.drawText("Cr      Hold: ", currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		currentX += holdTextWidth;
		g.drawText(freeCargo, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.regularFont);
		currentX += freeCargoWidth + spaceWidth;
		g.drawText(spareText, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
	}

	protected void performTradeWhileInFlight(int row, int column) {
		SoundManager.play(Assets.error);
		errorText = "Not Docked.";
	}

	void presentTradeGoods(float deltaTime) {
		Graphics g = game.getGraphics();

		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (tradeButton[x][y] == null) {
					continue;
				}
				if (selection == tradeButton[x][y]) {
					if (Settings.animationsEnabled) {
						computeCurrentFrame();
					}
					tradeButton[x][y].render(g, currentFrame);
					GLES11.glEnable(GLES11.GL_BLEND);
					GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE);
					GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
					g.fillRect(tradeButton[x][y].getX(), tradeButton[x][y].getY(), tradeButton[x][y].getWidth(),
						tradeButton[x][y].getHeight(), ColorScheme.get(ColorScheme.COLOR_HIGHLIGHT_COLOR));
					GLES11.glDisable(GLES11.GL_BLEND);
					if (errorText == null) {
						presentSelection(y, x);
					}
				} else {
					tradeButton[x][y].render(g);
				}
				String price = getCost(y, x);
				int halfWidth =  g.getTextWidth(price, Assets.regularFont) >> 1;
				g.drawText(price, x * GAP_X + X_OFFSET + (SIZE >> 1) - halfWidth, y * GAP_Y + Y_OFFSET + SIZE + 35,
					ColorScheme.get(ColorScheme.COLOR_PRICE), Assets.regularFont);
				drawAdditionalTradeGoodInformation(y, x, deltaTime);
			}
		}
		if (errorText != null) {
			game.getGraphics().drawText(errorText, X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		}
		if (cashLeft != null) {
			g.drawText(cashLeft, X_OFFSET, 1050, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		}
	}

	@Override
	public void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (tradeButton[x][y] == null || !tradeButton[x][y].isTouched(touch.x, touch.y)) {
					continue;
				}
				if (selection == tradeButton[x][y]) {
					if (game.getCurrentScreen() instanceof FlightScreen) {
						performTradeWhileInFlight(y, x);
					} else {
						SoundManager.play(Assets.click);
						performTrade(y, x);
					}
					continue;
				}
				errorText = null;
				startSelectionTime = System.nanoTime();
				currentFrame = 0;
				selection = tradeButton[x][y];
				cashLeft = null;
				SoundManager.play(Assets.click);
			}
		}
	}

	public Screen getNewScreen() {
		return newScreen;
	}

	private void readTradegoods() {
		Graphics g = game.getGraphics();
		tradeGoods = new Pixmap[18];
		tradeGoods[ 0] = g.newPixmap("trade_icons/food.png");
		tradeGoods[ 1] = g.newPixmap("trade_icons/textiles.png");
		tradeGoods[ 2] = g.newPixmap("trade_icons/radioactives.png");
		tradeGoods[ 3] = g.newPixmap("trade_icons/slaves.png");
		tradeGoods[ 4] = g.newPixmap("trade_icons/liquor_wines.png");
		tradeGoods[ 5] = g.newPixmap("trade_icons/luxuries.png");
		tradeGoods[ 6] = g.newPixmap("trade_icons/narcotics.png");
		tradeGoods[ 7] = g.newPixmap("trade_icons/computers.png");
		tradeGoods[ 8] = g.newPixmap("trade_icons/machinery.png");
		tradeGoods[ 9] = g.newPixmap("trade_icons/alloys.png");
		tradeGoods[10] = g.newPixmap("trade_icons/firearms.png");
		tradeGoods[11] = g.newPixmap("trade_icons/furs.png");
		tradeGoods[12] = g.newPixmap("trade_icons/minerals.png");
		tradeGoods[13] = g.newPixmap("trade_icons/gold.png");
		tradeGoods[14] = g.newPixmap("trade_icons/platinum.png");
		tradeGoods[15] = g.newPixmap("trade_icons/gem_stones.png");
		tradeGoods[16] = g.newPixmap("trade_icons/alien_items.png");
		tradeGoods[17] = g.newPixmap("trade_icons/medical_supplies.png");
	}

	private void readBeamAnimation() {
		Graphics g = game.getGraphics();
		beam = new Pixmap[16];
		beam[0] = g.newPixmap("trade_icons/beam.png");
		for (int i = 1; i < 16; i++) {
			beam[i] = g.newPixmap("trade_icons/beam/" + i + ".png");
		}
	}

	void loadTradeGoodAssets() {
		if (beam == null && Settings.animationsEnabled) {
			readBeamAnimation();
		}
		if (tradeGoods == null) {
			readTradegoods();
		}
	}

	void disposeTradeGoodAssets() {
		if (beam != null) {
			for (Pixmap p: beam) {
				p.dispose();
			}
			beam = null;
		}
		if (tradeGoods != null) {
			for (Pixmap p: tradeGoods) {
				p.dispose();
			}
			tradeGoods = null;
		}
	}

	String getCashLeftString() {
		return getOneDecimalFormatString("Cash left: %d.%d Cr", game.getPlayer().getCash());
	}

	public static String getOneDecimalFormatString(String format, long value) {
		return String.format(Locale.getDefault(), format, value / 10, value % 10);
	}
}
