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

import android.opengl.GLES11;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.InventoryItem;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

//This screen never needs to be serialized, as it is not part of the InGame state.
public abstract class TradeScreen extends AliteScreen {
	private String pendingSelection;

	int X_OFFSET = 150;
	int Y_OFFSET = 100;
	int SIZE     = 225;
	int GAP_X    = 300;
	int GAP_Y    = 300;
	int COLUMNS  = 5;

	Pixmap[] beam;
	Map<TradeGood,Pixmap> tradeGoods = new HashMap<>();
	List<Button> tradeButton = new ArrayList<>();

	int currentFrame = 0;
	final Timer selectionTimer = new Timer().setAutoReset();
	int selectionIndex = -1;

	private boolean continuousAnimation;
	String cashLeft = null;
	String errorText = null;

	TradeScreen(boolean continuousAnimation, String pendingSelection) {
		this.continuousAnimation = continuousAnimation;
		this.pendingSelection = pendingSelection;
	}

	protected abstract void createButtons();

	protected abstract String getCost(int index);
	protected abstract void performTrade(int index);
	protected abstract void presentSelection(int index);

	protected void drawAdditionalTradeGoodInformation(int index, float deltaTime) {
		// The default implementation does nothing.
	}

	@Override
	public void activate() {
		createButtons();
		if (pendingSelection == null) {
			return;
		}
		for (int i = 0; i < tradeButton.size(); i++) {
			Button b = tradeButton.get(i);
			if (b == null || b.getName() == null) {
				continue;
			}
			if (pendingSelection.equals(b.getName())) {
				selectionIndex = i;
				b.setSelected(true);
				loadSelectedAnimationCheck();
			}
		}
		pendingSelection = null;
	}

	private void loadSelectedAnimationCheck() {
		if (Settings.animationsEnabled) {
			loadSelectedAnimation();
		}
	}

	protected void loadSelectedAnimation() {
	}

	private void computeCurrentFrame() {
		if (selectionIndex < 0 || selectionIndex >= tradeButton.size()) {
			return;
		}
		int maxFrames = tradeButton.get(selectionIndex).getFrameCount();
		if (!continuousAnimation) {
			if (selectionTimer.hasPassedSeconds(5)) {
				currentFrame = 0;
			}
			if (currentFrame > maxFrames) {
				return;
			}
		}
		if (selectionTimer.hasPassedMillis(42)) { // 1/24 second
			currentFrame++;
			if (continuousAnimation && currentFrame > maxFrames) {
				currentFrame = 1;
			}
		}
	}

	void presentTradeStatus() {
		Player player = game.getPlayer();
		Graphics g = game.getGraphics();
		String cash = L.getOneDecimalFormatString(R.string.cash_amount_only, player.getCash());
		String freeCargo = player.getCobra().getFreeCargo().getStringWithoutUnit();
		String spareText = player.getCobra().getFreeCargo().getAppropriateUnit().toUnitString() + L.string(R.string.trade_spare_cargo);
		int cashTextWidth = g.getTextWidth(L.string(R.string.trade_cash), Assets.regularFont);
		int cashWidth = g.getTextWidth(cash, Assets.regularFont);
		int holdTextWidth = g.getTextWidth(L.string(R.string.trade_ccy_hold), Assets.regularFont);
		int freeCargoWidth = g.getTextWidth(freeCargo, Assets.regularFont);
		int spaceWidth = g.getTextWidth(" ", Assets.regularFont);

		int halfWidth = cashTextWidth + cashWidth + spaceWidth + holdTextWidth + freeCargoWidth + spaceWidth +
			g.getTextWidth(spareText, Assets.regularFont) >> 1;
		int currentX = 860 - halfWidth;

		g.drawText(L.string(R.string.trade_cash), currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		currentX += cashTextWidth;
		g.drawText(cash, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.regularFont);
		currentX += cashWidth + spaceWidth;
		g.drawText(L.string(R.string.trade_ccy_hold), currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		currentX += holdTextWidth;
		g.drawText(freeCargo, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), Assets.regularFont);
		currentX += freeCargoWidth + spaceWidth;
		g.drawText(spareText, currentX, 1000, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
	}

	protected void performTradeWhileInFlight(int index) {
		SoundManager.play(Assets.error);
		errorText = L.string(R.string.state_not_docked);
	}

	void presentTradeGoods(float deltaTime) {
		Graphics g = game.getGraphics();
		int index = 0;
		for (int i = 0; i < tradeButton.size(); i++) {
			Button b = tradeButton.get(i);
			if (b == null) {
				index++;
				continue;
			}
			if (selectionIndex == i) {
				if (Settings.animationsEnabled) {
					computeCurrentFrame();
				}
				b.render(g, currentFrame);
				GLES11.glEnable(GLES11.GL_BLEND);
				GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE);
				GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
				g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight(), ColorScheme.get(ColorScheme.COLOR_HIGHLIGHT_COLOR));
				GLES11.glDisable(GLES11.GL_BLEND);
				if (errorText == null) {
					presentSelection(index);
				}
			} else {
				b.render(g);
			}
			String price = getCost(index);
			int halfWidth = g.getTextWidth(price, Assets.regularFont) >> 1;
			g.drawText(price, index % COLUMNS * GAP_X + X_OFFSET + (SIZE >> 1) - halfWidth,
				index / COLUMNS * GAP_Y + Y_OFFSET + SIZE + 35, ColorScheme.get(ColorScheme.COLOR_PRICE), Assets.regularFont);
			drawAdditionalTradeGoodInformation(index, deltaTime);
			index++;
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
		int index = 0;
		for (int i = 0; i < tradeButton.size(); i++) {
			Button b = tradeButton.get(i);
			if (b == null || !b.isTouched(touch.x, touch.y)) {
				index++;
				continue;
			}
			if (selectionIndex == i) {
				if (game.getCurrentScreen() instanceof FlightScreen) {
					performTradeWhileInFlight(index);
				} else {
					SoundManager.play(Assets.click);
					performTrade(index);
				}
				index++;
				continue;
			}
			errorText = null;
			disposeSelectedAnimationCheck();
			selectionIndex = i;
			loadSelectedAnimationCheck();
			selectionTimer.reset();
			currentFrame = 0;
			cashLeft = null;
			SoundManager.play(Assets.click);
			index++;
		}
	}

	private void disposeSelectedAnimationCheck() {
		if (selectionIndex != -1) {
			disposeSelectedAnimation(selectionIndex);
		}
	}

	protected void disposeSelectedAnimation(int index) {
	}

	public Screen getNewScreen() {
		return newScreen;
	}

	private void readBeamAnimation() {
		Graphics g = game.getGraphics();
		beam = new Pixmap[16];
		beam[0] = g.newPixmap("trade_icons/beam.png");
		for (int i = 1; i < 16; i++) {
			beam[i] = g.newPixmap("trade_icons/beam/" + i + ".png");
		}
	}

	void loadTradeGoodAssets(List<TradeGood> goods) {
		if (beam == null && Settings.animationsEnabled) {
			readBeamAnimation();
		}
		for (TradeGood good : goods) {
			addGoodIcon(good);
		}
	}

	private void addGoodIcon(TradeGood good) {
		if (tradeGoods.get(good) == null) {
			tradeGoods.put(good, game.getGraphics().newPixmap(good.getIconName()));
		}
	}

	void loadTradeGoodAssetsByInventory(List<InventoryItem> items) {
		if (beam == null && Settings.animationsEnabled) {
			readBeamAnimation();
		}
		for (InventoryItem item : items) {
			addGoodIcon(item.getGood());
		}
	}

	void disposeTradeGoodAssets() {
		if (beam != null) {
			for (Pixmap p: beam) {
				p.dispose();
			}
			beam = null;
		}
		for (Pixmap p: tradeGoods.values()) {
			p.dispose();
		}
		tradeGoods.clear();
	}

	String getCashLeftString() {
		return L.getOneDecimalFormatString(R.string.cash_left, game.getPlayer().getCash());
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		ScreenBuilder.writeString(dos, selectionIndex == -1 ? null : tradeButton.get(selectionIndex).getName());
	}
}
