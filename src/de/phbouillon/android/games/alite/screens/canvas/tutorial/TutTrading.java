package de.phbouillon.android.games.alite.screens.canvas.tutorial;

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

import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.BuyScreen;
import de.phbouillon.android.games.alite.screens.canvas.InventoryScreen;
import de.phbouillon.android.games.alite.screens.canvas.QuantityPadScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;

//This screen never needs to be serialized, as it is not part of the InGame state,
//also, all used inner classes (IMethodHook, etc.) will be reset upon state loading,
//hence they never need to be serialized, either.
public class TutTrading extends TutorialScreen {
	private StatusScreen status;
	private BuyScreen buy;
	private InventoryScreen inventory;
	private QuantityPadScreen quantity;
	private boolean success = false;
	private int screenToInitialize = 0;

	TutTrading() {
		super(2, false);
		initLine_00();
		initLine_01();
		initLine_02();
		initLine_03();
		initLine_04();
		initLine_05();
		initLine_06();
		initLine_07();
		initLine_08();
		initLine_09();
		initLine_10();
		initLine_11();
	}

	public TutTrading(DataInputStream dis) throws IOException {
		this();
		currentLineIndex = dis.readInt();
		screenToInitialize = dis.readByte();
		loadScreenState(dis);
	}

	private void initLine_00() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_00));

		status = new StatusScreen();
		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (updateNavBar() instanceof BuyScreen) {
				changeToBuyScreen();
				setFinished(line);
			}
		});
	}

	private void initLine_01() {
		addLine(L.string(R.string.tutorial_trading_01));
	}

	private void initLine_02() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_02));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (buy.getSelectedGood() == null) {
				buy.processAllTouches();
			}
			if (buy.getSelectedGood() != null) {
				setFinished(line);
			}
		});
	}

	private void initLine_03() {
		addLine(L.string(R.string.tutorial_trading_03))
		.setFinishHook(deltaTime -> buy.resetSelection()).setHeight(150);
	}

	private void initLine_04() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_04));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (buy.getSelectedGood() == null) {
				buy.processAllTouches();
			}
			TradeGood selectedGood = buy.getSelectedGood();
			if (selectedGood != null) {
				setFinished(line);
				if (selectedGood == TradeGoodStore.get().getGoodById(TradeGoodStore.FOOD)) {
					currentLineIndex++;
				} else {
					buy.resetSelection();
				}
			}
		}).addHighlight(makeHighlight(50, 100, 225, 225)).setHeight(150);
	}

	private void initLine_05() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_05));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (buy.getSelectedGood() == null) {
				buy.processAllTouches();
			}
			TradeGood selectedGood = buy.getSelectedGood();
			if (selectedGood != null) {
				setFinished(line);
				if (selectedGood != TradeGoodStore.get().getGoodById(TradeGoodStore.FOOD)) {
					currentLineIndex--;
					buy.resetSelection();
				}
			}
		}).addHighlight(makeHighlight(50, 100, 225, 225)).setHeight(150);
	}

	private void initLine_06() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_06));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			buy.processAllTouches();
			if (buy.getGoodToBuy() == TradeGoodStore.get().getGoodById(TradeGoodStore.FOOD)) {
				quantity = (QuantityPadScreen) buy.getNewScreen();
				quantity.loadAssets();
				quantity.activate();
				setFinished(line);
			}
		}).setHeight(150);
	}

	private void initLine_07() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_07));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			quantity.processAllTouches();
			if (quantity.getNewScreen() == buy) {
				success = buy.getBoughtAmount() == 1;
				if (success) {
					currentLineIndex++;
				}
				setFinished(line);
			}
		}).setWidth(800).setHeight(350).setY(400).setFinishHook(deltaTime -> {
			if (quantity != null) {
				quantity.clearAmount();
				if (success) {
					quantity.dispose();
					quantity = null;
					buy.performTrade(0);
				}
			}
		});
	}

	private void initLine_08() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_08));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			quantity.processAllTouches();
			if (quantity.getNewScreen() == buy) {
				success = buy.getBoughtAmount() == 1;
				setFinished(line);
				if (!success) {
					currentLineIndex--;
				}
			}
		}).setWidth(800).setHeight(350).setY(400).setFinishHook(deltaTime -> {
			if (success) {
				quantity.dispose();
				quantity = null;
				buy.performTrade(0);
			} else {
				quantity.clearAmount();
			}
		});
	}

	private void initLine_09() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_09));

		line.setUnskippable().setUpdateMethod(deltaTime -> {
			if (updateNavBar() instanceof InventoryScreen) {
				buy.dispose();
				buy = null;
				changeToInventoryScreen();
				setFinished(line);
			}
		}).setHeight(150);
	}

	private void initLine_10() {
		final TutorialLine line = addLine(L.string(R.string.tutorial_trading_10));

		line.setUnskippable().setY(500).
			addHighlight(makeHighlight(450, 970, 850, 40)).
			setUpdateMethod(deltaTime -> {
				inventory.processAllTouches();
				if (inventory.getCashLeft() != null) {
					setFinished(line);
				}
			});
	}

	private void initLine_11() {
		addLine(L.string(R.string.tutorial_trading_11)).setY(500).setHeight(400).
				setPause(5000);
	}

	@Override
	public void activate() {
		super.activate();
		TradeGood food = TradeGoodStore.get().getGoodById(TradeGoodStore.FOOD);
		switch (screenToInitialize) {
			case 0:
				status.activate();
				game.getNavigationBar().moveToTop();
				game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_STATUS);
				break;
			case 1:
				changeToBuyScreen();
				break;
			case 2:
				changeToBuyScreen();
				quantity = new QuantityPadScreen(buy, game.getPlayer().getMarket().getQuantity(food),
					food.getUnit().toUnitString(), 1075, 200, 0);
				quantity.loadAssets();
				quantity.activate();
				break;
			case 3:
				status.dispose();
				status = null;
				changeToInventoryScreen();
				break;
		}
		if (currentLineIndex <= 0) {
			game.getCobra().clearInventory();
			game.getPlayer().getMarket().setQuantity(food, 17);
			game.getPlayer().setCash(1000);
		}
	}

	private void changeToInventoryScreen() {
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_INVENTORY);
		inventory = new InventoryScreen();
		inventory.loadAssets();
		inventory.activate();
	}

	private void changeToBuyScreen() {
		status.dispose();
		status = null;
		game.getNavigationBar().setActiveIndex(Alite.NAVIGATION_BAR_BUY);
		buy = new BuyScreen();
		buy.loadAssets();
		buy.activate();
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(currentLineIndex - 1);
		if (status != null) {
			dos.writeByte(0);
		} else if (buy != null) {
			dos.writeByte(quantity != null ? 2 : 1);
		} else if (inventory != null) {
			dos.writeByte(3);
		}
		super.saveScreenState(dos);
	}

	@Override
	public void loadAssets() {
		super.loadAssets();
		status.loadAssets();
	}

	@Override
	public void doPresent(float deltaTime) {
		if (status != null) {
			status.present(deltaTime);
		} else if (buy != null) {
			if (quantity != null) {
				quantity.present(deltaTime);
			} else {
				buy.present(deltaTime);
			}
		} else if (inventory != null) {
			inventory.present(deltaTime);
		}

		renderText();
	}

	@Override
	public void dispose() {
		if (status != null) {
			status.dispose();
			status = null;
		}
		if (buy != null) {
			buy.dispose();
			buy = null;
		}
		if (inventory != null) {
			inventory.dispose();
			inventory = null;
		}
		if (quantity != null) {
			quantity.dispose();
			quantity = null;
		}
		super.dispose();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.TUT_TRADING_SCREEN;
	}
}
