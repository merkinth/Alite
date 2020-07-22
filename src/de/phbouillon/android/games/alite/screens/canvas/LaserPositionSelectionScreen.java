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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class LaserPositionSelectionScreen extends AliteScreen {
	private final int index;
	private final Button[] pads = new Button[4];
	private final int front;
	private final int right;
	private final int rear;
	private final int left;
	private final EquipmentScreen equipmentScreen;

	LaserPositionSelectionScreen(EquipmentScreen equipmentScreen, int front, int right, int rear, int left, int index) {
		this.index = index;
		this.front = front;
		this.right = right;
		this.rear = rear;
		this.left = left;
		this.equipmentScreen = equipmentScreen;
	}

	@Override
	public void activate() {
		initializeButtons();
	}

	private Pixmap getIcon(int currentEquip) {
		return currentEquip == 0 ? Assets.yesIcon : currentEquip < 0 ? Assets.noIcon : pics.get("change_icon");
	}

	private void initializeButtons() {
		pads[0] = Button.createGradientRegularButton(710, 210, 300, 100, L.string(R.string.laser_pos_front))
			.setPixmap(getIcon(front))
			.setTextPosition(Button.TextPosition.RIGHT);
		pads[1] = Button.createGradientRegularButton(1340, 480, 200, 200, L.string(R.string.laser_pos_right))
			.setPixmap(getIcon(right))
			.setPixmapOffset(50, 0)
			.setTextPosition(Button.TextPosition.BELOW);
		pads[2] = Button.createGradientRegularButton(710, 855, 300, 100, L.string(R.string.laser_pos_rear))
			.setPixmap(getIcon(rear))
			.setTextPosition(Button.TextPosition.RIGHT);
		pads[3] = Button.createGradientRegularButton(180, 480, 200, 200, L.string(R.string.laser_pos_left))
			.setPixmap(getIcon(left))
			.setPixmapOffset(50, 0)
			.setTextPosition(Button.TextPosition.BELOW);
		for (Button b: pads) {
			b.setTextColor(ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();

		equipmentScreen.present(deltaTime);
		g.verticalGradientRect(160, 160, 1400, 800,
			ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		g.rec3d(160, 160, 1400, 800, 10,
			ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		int halfWidth = g.getTextWidth(L.string(R.string.laser_pos_select), Assets.regularFont) >> 1;
		g.drawText(L.string(R.string.laser_pos_select), 860 - halfWidth, 195, ColorScheme.get(ColorScheme.COLOR_MESSAGE), Assets.regularFont);
		g.drawPixmap(pics.get("cobra_small"), 380, 310);

		for (Button b: pads) {
			b.render(g);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		for (int i = 0; i < pads.length; i++) {
			if (pads[i].isPressed(touch)) {
				equipmentScreen.setLaserPosition(i);
				newScreen = equipmentScreen;
				return;
			}
		}
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (!Rect.inside(touch.x, touch.y, 160, 160, 1560, 960)) {
			equipmentScreen.setLaserPosition(-2);
			newScreen = equipmentScreen;
		}
	}

	@Override
	protected void performScreenChange() {
		dispose();
		game.setScreen(equipmentScreen);
		equipmentScreen.performTrade(index);
		game.getNavigationBar().performScreenChange();
	}

	@Override
	public void loadAssets() {
		addPictures("cobra_small", "change_icon");
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return -1;
	}
}
