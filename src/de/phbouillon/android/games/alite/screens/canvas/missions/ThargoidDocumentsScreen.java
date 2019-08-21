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
import android.media.MediaPlayer;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.missions.ThargoidDocumentsMission;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.GalaxyScreen;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ThargoidDocumentsScreen extends AliteScreen {
	private final MediaPlayer mediaPlayer;

	private MissionLine attCommander;
	private MissionLine missionLine;
	private MissionLine acceptMission;
	private int lineIndex = 0;
	private TextData[] missionText;
	private Button acceptButton;
	private Button declineButton;
	private Pixmap acceptIcon;
	private Pixmap declineIcon;
	private SystemData targetSystem = null;
	private final Mission mission;
	private final int givenState;

	public ThargoidDocumentsScreen(Alite game, int state) {
		super(game);
		givenState = state;
		mission = MissionManager.getInstance().get(ThargoidDocumentsMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "2/";
		try {
			attCommander = new MissionLine(path + "01.mp3", L.string(R.string.mission_attention_commander));
			if (state == 0) {
				missionLine = new MissionLine(path + "02.mp3", L.string(R.string.mission_thargoid_documents_mission_description));
				targetSystem = mission.findMostDistantSystem();
				mission.setTarget(game.getGenerator().getCurrentGalaxy(), targetSystem.getIndex(), state);
				acceptMission = new MissionLine(path + "03.mp3", L.string(R.string.mission_accept));
			} else if (state == 1) {
				missionLine = new MissionLine(path + "04.mp3", L.string(R.string.mission_thargoid_documents_fully_serviced));
			} else if (state == 2) {
				missionLine = new MissionLine(path + "05.mp3", L.string(R.string.mission_thargoid_documents_success));
			 	mission.onMissionComplete();
				Player player = game.getPlayer();
				player.removeActiveMission(mission);
				player.addCompletedMission(mission);
				player.resetIntergalacticJumpCounter();
				player.resetJumpCounter();
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to ThargoidDocumentScreen: " + state);
			}
		} catch (IOException e) {
			AliteLog.e("Error reading mission", "Could not read mission audio.", e);
		}
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
				newScreen = new ThargoidDocumentsScreen(game, 1);
			}
			if (declineButton.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				mission.setPlayerAccepts(false);
				newScreen = new StatusScreen(game);
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_thargoid_documents));

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

		if (targetSystem != null) {
			displayStarMap();
		}
	}

	private Point toScreen(SystemData systemData, int centerX, int centerY, float zoomFactor) {
		int offsetX = (int) (centerX * zoomFactor * ConstrictorScreen.STRETCH_X) - 400;
		int offsetY = (int) (centerY * zoomFactor * ConstrictorScreen.STRETCH_Y) - 550;
		return new Point((int) (systemData.getX() * zoomFactor) * ConstrictorScreen.STRETCH_X + 900 - offsetX,
				         (int) (systemData.getY() * zoomFactor) * ConstrictorScreen.STRETCH_Y + 100 - offsetY);
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
	}

	@Override
	public void activate() {
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300, 800, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		if (acceptMission != null) {
			acceptButton = Button.createGradientPictureButton(50, 860, 200, 200, acceptIcon);
			declineButton = Button.createGradientPictureButton(650, 860, 200, 200, declineIcon);
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			int state = dis.readInt();
			alite.setScreen(new ThargoidDocumentsScreen(alite, state));
		} catch (IOException e) {
			AliteLog.e("Thargoid Documents Screen Initialize", "Error in initializer.", e);
			return false;
		}
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(givenState);
	}

	@Override
	public void loadAssets() {
		acceptIcon = game.getGraphics().newPixmap("yes_icon.png");
		declineIcon = game.getGraphics().newPixmap("no_icon.png");
	}

	@Override
	public void dispose() {
		super.dispose();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
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
	public void pause() {
		super.pause();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.THARGOID_DOCUMENTS_SCREEN;
	}
}
