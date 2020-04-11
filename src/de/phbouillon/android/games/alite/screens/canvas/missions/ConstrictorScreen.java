package de.phbouillon.android.games.alite.screens.canvas.missions;

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

import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.ConstrictorMission;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.GalaxyScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.MathHelper;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ConstrictorScreen extends AliteScreen {
	static final int STRETCH_X = 7;
	static final int STRETCH_Y = 7;

	private transient MediaPlayer mediaPlayer;

	private MissionLine attCommander;
	private MissionLine missionLine;
	private MissionLine acceptMission;
	private int lineIndex = 0;
	private SpaceObject constrictor;
	private TextData[] missionText;
	private final Timer timer = new Timer().setAutoReset();
	private Vector3f currentDelta = new Vector3f(0,0,0);
	private Vector3f targetDelta = new Vector3f(0,0,0);

	private Button acceptButton;
	private Button declineButton;
	private Pixmap acceptIcon;
	private Pixmap declineIcon;
	private SystemData targetSystem = null;
	private final Mission mission;
	private final int givenState;

	public ConstrictorScreen(int state) {
		givenState = state;
		mission = MissionManager.getInstance().get(ConstrictorMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "1/";
		try {
			attCommander = new MissionLine(path + "01.mp3", L.string(R.string.mission_attention_commander));
			if (state == 0) {
				missionLine = new MissionLine(path + "02.mp3", L.string(R.string.mission_constrictor_mission_description));
				acceptMission = new MissionLine(path + "03.mp3", L.string(R.string.mission_accept));
				constrictor = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Constrictor);
				constrictor.setPosition(200, 0, -700.0f);
			} else if (state == 1) {
				missionLine = new MissionLine(path + "04.mp3", L.string(R.string.mission_constrictor_report_to_base));
				targetSystem = mission.findMostDistantSystem();
				mission.setTarget(game.getGenerator().getCurrentGalaxy(), targetSystem.getIndex(), state);
			} else if (state == 2) {
				missionLine = new MissionLine(path + "06.mp3", L.string(R.string.mission_constrictor_intergalactic_jump));
				mission.setTarget(game.getGenerator().getNextGalaxy(), -1, state);
			} else if (state == 3) {
				missionLine = new MissionLine(path + "05.mp3", L.string(R.string.mission_constrictor_hyperspace_jump));
				targetSystem = mission.findRandomSystemInRange(75, 120);
				mission.setTarget(game.getGenerator().getCurrentGalaxy(), targetSystem.getIndex(), mission.getState() + 1);
			} else if (state == 4) {
				missionLine = new MissionLine(path + "07.mp3", L.string(R.string.mission_constrictor_success));
				mission.onMissionComplete();
				Player player = game.getPlayer();
				player.removeActiveMission(mission);
				player.addCompletedMission(mission);
				player.resetIntergalacticJumpCounter();
				player.resetJumpCounter();
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to ConstrictorScreen: " + state);
			}
		} catch (IOException e) {
			AliteLog.e("Error reading mission", "Could not read mission audio.", e);
		}
	}

	public ConstrictorScreen(DataInputStream dis) throws IOException {
		this(dis.readInt());
		if (givenState == 3) {
			targetSystem = Alite.get().getGenerator().getSystems()[dis.readInt()];
			// Mission (model) state has been increased in constructor; now reduce it again...
			mission.setTarget(Alite.get().getGenerator().getCurrentGalaxy(), targetSystem.getIndex(), mission.getState() - 1);
		}
	}

	private void dance() {
		if (timer.hasPassedSeconds(4)) {
			MathHelper.getRandomRotationAngles(targetDelta);
		}
		MathHelper.updateAxes(currentDelta, targetDelta);
		constrictor.applyDeltaRotation(currentDelta.x, currentDelta.y, currentDelta.z);
	}

	@Override
	public void update(float deltaTime) {
		if (acceptButton == null && declineButton == null) {
			super.update(deltaTime);
		} else {
			updateWithoutNavigation(deltaTime);
		}
		if (lineIndex == 0 && !attCommander.isPlaying()) {
			attCommander.play(mediaPlayer);
			lineIndex++;
		} else if (lineIndex == 1 && !attCommander.isPlaying() && !missionLine.isPlaying()) {
			missionLine.play(mediaPlayer);
			lineIndex++;
		} else if (lineIndex == 2 && acceptMission != null && !acceptMission.isPlaying() && !missionLine.isPlaying()) {
			acceptMission.play(mediaPlayer);
			lineIndex++;
		}
		if (constrictor != null) {
			dance();
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (acceptButton == null && declineButton == null) {
			return;
		}
		if (touch.type == TouchEvent.TOUCH_UP) {
			if (acceptButton.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				mission.setPlayerAccepts(true);
				newScreen = new ConstrictorScreen(1);
			}
			if (declineButton.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				mission.setPlayerAccepts(false);
				newScreen = new StatusScreen();
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_constrictor));

		g.drawText(L.string(R.string.mission_attention_commander), 50, 200, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		if (missionText != null) {
			displayText(g, missionText);
		}
		if (acceptMission != null) {
			g.drawText(L.string(R.string.mission_accept), 50, 800, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
			if (acceptButton != null) {
				acceptButton.render(g);
			}
			if (declineButton != null) {
				declineButton.render(g);
			}
		}

		if (constrictor != null) {
			displayObject(constrictor, 1.0f, 100000.0f);
		} else if (targetSystem != null) {
			displayStarMap();
		} else {
			Rect visibleArea = game.getGraphics().getVisibleArea();
			setUpForDisplay(visibleArea);
		}
	}

	private Point toScreen(SystemData systemData, int centerX, int centerY, float zoomFactor) {
		int offsetX = (int) (centerX * zoomFactor * STRETCH_X) - 400;
		int offsetY = (int) (centerY * zoomFactor * STRETCH_Y) - 550;
		return new Point((int) (systemData.getX() * zoomFactor) * STRETCH_X + 900 - offsetX,
				         (int) (systemData.getY() * zoomFactor) * STRETCH_Y + 100 - offsetY);
	}

	private void drawSystem(SystemData system, int centerX, int centerY, float zoomFactor, boolean clearBackground) {
		Graphics g = game.getGraphics();
		Point p = toScreen(system, centerX, centerY, zoomFactor);
		if (p.x < 900 || p.x > 1700 || p.y < 100 || p.y > 1000) {
			return;
		}
		g.fillCircle(p.x, p.y, (int) (3 * zoomFactor), system.getEconomy().getColor(), 32);
		int nameWidth = g.getTextWidth(system.getName(), system == targetSystem ? Assets.regularFont : Assets.smallFont);
		int nameHeight = g.getTextHeight(system.getName(), system == targetSystem ? Assets.regularFont : Assets.smallFont);
		int positionX = (int) (3 * zoomFactor) + 2;
		int positionY = 40;
		if (p.x + nameWidth > GalaxyScreen.HALF_WIDTH << 1) {
			positionX = -positionX - nameWidth;
		}
		if (p.y + 40 > GalaxyScreen.HALF_HEIGHT << 1) {
			positionY = -40;
		}
		if (clearBackground) {
			g.fillRect(p.x + positionX, p.y + positionY - nameHeight, nameWidth, nameHeight, ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		}
		g.drawText(system.getName(), p.x + positionX, p.y + positionY, system.getEconomy().getColor(), system == targetSystem ? Assets.regularFont : Assets.smallFont);
		if (system == targetSystem) {
			g.drawLine(p.x, p.y - GalaxyScreen.CROSS_SIZE - GalaxyScreen.CROSS_DISTANCE, p.x, p.y - GalaxyScreen.CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x, p.y + GalaxyScreen.CROSS_SIZE + GalaxyScreen.CROSS_DISTANCE, p.x, p.y + GalaxyScreen.CROSS_DISTANCE, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x - GalaxyScreen.CROSS_SIZE - GalaxyScreen.CROSS_DISTANCE, p.y, p.x - GalaxyScreen.CROSS_DISTANCE, p.y, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
			g.drawLine(p.x + GalaxyScreen.CROSS_SIZE + GalaxyScreen.CROSS_DISTANCE, p.y, p.x + GalaxyScreen.CROSS_DISTANCE, p.y, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION));
		}
	}

	private void displayStarMap() {
		int centerX = targetSystem.getX();
		int centerY = targetSystem.getY();

		for (SystemData system: game.getGenerator().getSystems()) {
			drawSystem(system, centerX, centerY, 3.0f, false);
		}
		// Make sure the target system is rendered on top...
		drawSystem(targetSystem, centerX, centerY, 3.0f, true);

		Rect visibleArea = game.getGraphics().getVisibleArea();
		setUpForDisplay(visibleArea);
	}

	@Override
	public void activate() {
		initGl();
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300, 800, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		MathHelper.getRandomRotationAngles(targetDelta);
		if (acceptMission != null) {
			acceptButton = Button.createGradientPictureButton(50, 860, 200, 200, acceptIcon);
			declineButton = Button.createGradientPictureButton(650, 860, 200, 200, declineIcon);
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(givenState);
		if (givenState == 3) {
			dos.writeInt(targetSystem.getIndex());
		}
	}

	@Override
	public void loadAssets() {
		acceptIcon = game.getGraphics().newPixmap("yes_icon.png");
		declineIcon = game.getGraphics().newPixmap("no_icon.png");
	}

	@Override
	public void pause() {
		super.pause();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
		if (constrictor != null) {
			constrictor.dispose();
			constrictor = null;
		}
		if (acceptIcon != null) {
			acceptIcon.dispose();
			acceptIcon = null;
		}
		if (declineIcon != null) {
			declineIcon.dispose();
			declineIcon = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.CONSTRICTOR_SCREEN;
	}
}
