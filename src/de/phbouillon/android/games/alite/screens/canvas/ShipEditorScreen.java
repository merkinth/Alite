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
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.opengl.ingame.EngineExhaust;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectAI;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ShipEditorScreen extends AliteScreen {

	private SpaceObject currentShip;

	private Button increaseX;
	private Button decreaseX;
	private Button increaseY;
	private Button decreaseY;
	private Button increaseZ;
	private Button decreaseZ;
	private Button increaseRadiusX;
	private Button decreaseRadiusX;
	private Button increaseRadiusY;
	private Button decreaseRadiusY;
	private Button increaseLength;
	private Button decreaseLength;
	private Button decreaseSpeed;
	private Button increaseSpeed;
	private Button toggleExhaustCount;
	private Button nextShip;

	private Slider r1;
	private Slider g1;
	private Slider b1;
	private Slider a1;

	private float lastZoom = -1.0f;
	private int numberOfExhausts = 2;
	private final Vector3f temp = new Vector3f(0, 0, 0);
	private int lastX = -1;
	private int lastY = -1;


	class ExhaustParameters {
		int xOffset;
		int yOffset;
		int zOffset;
		int radiusX;
		int radiusY;
		int maxLength;
		float r1, g1, b1, a1;

		public String toString() {
			return "xOffset: " + xOffset + ", yOffset: " + yOffset + ", zOffset: " + zOffset + ", radiusX: " + radiusX + ", radiusY: " + radiusY + ", len: " + maxLength + ", (" + r1 + ", " + g1 + ", " + b1 + ", " + a1 + ")";
		}
	}

	private ExhaustParameters exp = new ExhaustParameters();

	public ShipEditorScreen() {
		currentShip = SpaceObjectFactory.getInstance().getObjectById("cobra_mk_iii");
		List<EngineExhaust> exhausts = currentShip.getExhausts();
		exhausts.clear();
		exp.xOffset = 50;
		exp.yOffset = 0;
		exp.zOffset = 0;
		exp.radiusX = 13;
		exp.radiusY = 13;
		exp.maxLength = 300;
		exp.r1 = 0.7f;
		exp.g1 = 0.8f;
		exp.b1 = 0.8f;
		exp.a1 = 0.7f;

		exhausts.add(new EngineExhaust(13.0f, 13.0f, 300.0f, -50.0f, 0, 0));
		exhausts.add(new EngineExhaust(13.0f, 13.0f, 300.0f,  50.0f, 0, 0));
		currentShip.setPosition(0, 0, -700.0f);
		currentShip.setAIState(SpaceObjectAI.AI_STATE_GLOBAL, (Object[]) null);
		currentShip.setSpeed(-currentShip.getMaxSpeed());
	}

	@Override
	public void update(float deltaTime) {
		updateWithoutNavigation(deltaTime);
		if (currentShip != null) {
			currentShip.update(deltaTime);
		}
	}

	private void modifyExhaust() {
		for (EngineExhaust ex: currentShip.getExhausts()) {
			ex.getPosition().copy(temp);
			if (temp.x < 0) {
				ex.setPosition(-exp.xOffset, exp.yOffset, currentShip.getBoundingBox()[5] + exp.zOffset);
			} else {
				ex.setPosition(exp.xOffset, exp.yOffset, currentShip.getBoundingBox()[5] + exp.zOffset);
			}
			ex.setRadiusX(exp.radiusX);
			ex.setRadiusY(exp.radiusY);
			ex.setMaxLength(exp.maxLength);
			ex.setColor(exp.r1, exp.g1, exp.b1, exp.a1);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (r1.checkEvent(touch)) {
			exp.r1 = r1.getCurrentValue();
			modifyExhaust();
			return;
		}
		if (g1.checkEvent(touch)) {
			exp.g1 = g1.getCurrentValue();
			modifyExhaust();
			return;
		}
		if (b1.checkEvent(touch)) {
			exp.b1 = b1.getCurrentValue();
			modifyExhaust();
			return;
		}
		if (a1.checkEvent(touch)) {
			exp.a1 = a1.getCurrentValue();
			modifyExhaust();
			return;
		}

		if (touch.type == TouchEvent.TOUCH_SCALE && game.getInput().getTouchCount() > 1) {
			if (lastZoom  < 0) {
				lastZoom = touch.zoomFactor;
			} else {
				if (touch.zoomFactor > lastZoom) {
					currentShip.setPosition(0, 0, currentShip.getPosition().z + 5);
				} else {
					currentShip.setPosition(0, 0, currentShip.getPosition().z - 5);
				}
			}
			return;
		}
		if (game.getInput().getTouchCount() > 1) {
			return;
		}
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (increaseX.isTouched(touch.x, touch.y)) {
				exp.xOffset += 5;
				modifyExhaust();
			}
			if (decreaseX.isTouched(touch.x, touch.y)) {
				exp.xOffset -= 5;
				modifyExhaust();
			}
			if (increaseY.isTouched(touch.x, touch.y)) {
				exp.yOffset += 5;
				modifyExhaust();
			}
			if (decreaseY.isTouched(touch.x, touch.y)) {
				exp.yOffset -= 5;
				modifyExhaust();
			}
			if (increaseZ.isTouched(touch.x, touch.y)) {
				exp.zOffset += 5;
				modifyExhaust();
			}
			if (decreaseZ.isTouched(touch.x, touch.y)) {
				exp.zOffset -= 5;
				modifyExhaust();
			}
			if (increaseRadiusX.isTouched(touch.x, touch.y)) {
				exp.radiusX += 1;
				modifyExhaust();
			}
			if (decreaseRadiusX.isTouched(touch.x, touch.y)) {
				exp.radiusX -= 1;
				modifyExhaust();
			}
			if (increaseRadiusY.isTouched(touch.x, touch.y)) {
				exp.radiusY += 1;
				modifyExhaust();
			}
			if (decreaseRadiusY.isTouched(touch.x, touch.y)) {
				exp.radiusY -= 1;
				modifyExhaust();
			}
			if (increaseLength.isTouched(touch.x, touch.y)) {
				exp.maxLength += 20;
				modifyExhaust();
			}
			if (decreaseLength.isTouched(touch.x, touch.y)) {
				exp.maxLength -= 20;
				modifyExhaust();
			}

			if (increaseSpeed.isTouched(touch.x, touch.y)) {
				float newSpeed = currentShip.getSpeed() - 10.0f;
				if (-newSpeed > currentShip.getMaxSpeed()) {
					newSpeed = -currentShip.getMaxSpeed();
				}
				currentShip.setSpeed(newSpeed);
			}
			if (decreaseSpeed.isTouched(touch.x, touch.y)) {
				float newSpeed = currentShip.getSpeed() + 10.0f;
				if (newSpeed > 0) {
					newSpeed = 0;
				}
				currentShip.setSpeed(newSpeed);
			}
			if (toggleExhaustCount.isTouched(touch.x, touch.y)) {
				numberOfExhausts = -numberOfExhausts + 3;
				currentShip.getExhausts().clear();
				for (int i = 0; i < numberOfExhausts; i++) {
					currentShip.getExhausts().add(new EngineExhaust(exp.radiusX, exp.radiusY, exp.maxLength, i == 0 ? -exp.xOffset : exp.xOffset, exp.yOffset, exp.zOffset));
					currentShip.getExhausts().get(i).setColor(exp.r1, exp.g1, exp.b1, exp.a1);
				}
			}
			if (nextShip.isTouched(touch.x, touch.y)) {
				AliteLog.d("Exhaust Configuration", currentShip.getId() + ": Number of exhausts: " + numberOfExhausts + " Params: " + exp);
				exp.xOffset = 50;
				exp.yOffset = 0;
				exp.zOffset = 0;
				exp.radiusX = 13;
				exp.radiusY = 13;
				exp.maxLength = 300;
				exp.r1 = 0.7f;
				exp.g1 = 0.8f;
				exp.b1 = 0.8f;
				exp.a1 = 0.7f;
				numberOfExhausts = 2;
				currentShip = SpaceObjectFactory.getInstance().getNextObject(currentShip, 1, true);
				currentShip.getExhausts().clear();
				currentShip.setSpeed(-currentShip.getMaxSpeed());
				currentShip.setPosition(0, 0, -700.0f);
				for (int i = 0; i < numberOfExhausts; i++) {
					currentShip.getExhausts().add(new EngineExhaust(exp.radiusX, exp.radiusY, exp.maxLength, i == 0 ? -exp.xOffset : exp.xOffset, exp.yOffset, exp.zOffset));
					currentShip.getExhausts().get(i).setColor(exp.r1, exp.g1, exp.b1, exp.a1);
				}
			}
		}

		if (touch.y > 800 || touch.x > 800) {
			return;
		}

		if (touch.type == TouchEvent.TOUCH_DRAGGED) {
			if (lastX != -1 && lastY != -1) {
				int diffX = touch.x - lastX;
				int diffY = touch.y - lastY;
				int ady = Math.abs(diffY);
				int adx = Math.abs(diffX);
				if (adx > ady) {
					currentShip.applyDeltaRotation(0, diffX, 0);
				} else {
					currentShip.applyDeltaRotation(diffY, 0, 0);
				}
			}
			lastX = touch.x;
			lastY = touch.y;
		}
		if (touch.type == TouchEvent.TOUCH_DOWN) {
			lastX = touch.x;
			lastY = touch.y;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayWideTitle("Ship Configuration Screen");

		increaseX.render(g);
		decreaseX.render(g);
		increaseY.render(g);
		decreaseY.render(g);
		increaseZ.render(g);
		decreaseZ.render(g);
		increaseRadiusX.render(g);
		decreaseRadiusX.render(g);
		increaseRadiusY.render(g);
		decreaseRadiusY.render(g);
		increaseLength.render(g);
		decreaseLength.render(g);
		increaseSpeed.render(g);
		decreaseSpeed.render(g);
		toggleExhaustCount.render(g);
		nextShip.render(g);

		if (currentShip != null) {
			g.drawText(currentShip.getName(), 20, 150, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		}

		r1.render(g);
		g1.render(g);
		b1.render(g);
		a1.render(g);

		if (currentShip != null) {
			displayObject(currentShip, 1.0f, 900000.0f);
		}
	}

	@Override
	public void activate() {
		initGl();
		decreaseX = Button.createGradientSmallButton(0, 900, 150, 80, "X-");
		increaseX = Button.createGradientSmallButton(0, 1000, 150, 80, "X+");
		decreaseY = Button.createGradientSmallButton(200, 900, 150, 80, "Y-");
		increaseY = Button.createGradientSmallButton(200, 1000, 150, 80, "Y+");
		decreaseZ = Button.createGradientSmallButton(400, 900, 150, 80, "Z-");
		increaseZ = Button.createGradientSmallButton(400, 1000, 150, 80, "Z+");
		decreaseRadiusX = Button.createGradientSmallButton(600, 900, 150, 80, "Rx-");
		increaseRadiusX = Button.createGradientSmallButton(600, 1000, 150, 80, "Rx+");
		decreaseRadiusY = Button.createGradientSmallButton(800, 900, 150, 80, "Ry-");
		increaseRadiusY = Button.createGradientSmallButton(800, 1000, 150, 80, "Ry+");
		decreaseLength = Button.createGradientSmallButton(1000, 900, 150, 80, "L-");
		increaseLength = Button.createGradientSmallButton(1000, 1000, 150, 80, "L+");
		decreaseSpeed = Button.createGradientSmallButton(1200, 900, 150, 80, "S-");
		increaseSpeed = Button.createGradientSmallButton(1200, 1000, 150, 80, "S+");
		toggleExhaustCount = Button.createGradientSmallButton(1400, 900, 150, 80, "TC");
		nextShip = Button.createGradientSmallButton(1400, 1000, 150, 80, "Next");

		r1 = new Slider(1100, 150, 720, 100, 0.2f, 1.0f, 0.7f, "r1", Assets.smallFont);
		g1 = new Slider(1100, 350, 720, 100, 0, 1, 0.8f, "g1", Assets.smallFont);
		b1 = new Slider(1100, 550, 720, 100, 0, 0.8f, 0.8f, "b1", Assets.smallFont);
		a1 = new Slider(1100, 750, 720, 100, 0, 0.7f, 0.7f, "a1", Assets.smallFont);
	}


	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
	}

	@Override
	public void loadAssets() {
	}

	@Override
	public int getScreenCode() {
		return 0;
	}

	@Override
	public void renderNavigationBar() {
	}
}
