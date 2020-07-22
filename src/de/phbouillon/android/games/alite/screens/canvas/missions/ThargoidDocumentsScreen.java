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

import java.io.DataOutputStream;
import java.io.IOException;

import android.media.MediaPlayer;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
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
	private SystemData targetSystem = null;
	private final Mission mission;
	private final int givenState;

	public ThargoidDocumentsScreen(int state) {
		givenState = state;
		mission = MissionManager.getInstance().get(ThargoidDocumentsMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "2/";
		try {
			attCommander = new MissionLine(path + "01.mp3", L.string(R.string.mission_attention_commander));
			if (state == 0) {
				missionLine = new MissionLine(path + "02.mp3", L.string(R.string.mission_thargoid_documents_mission_description));
				targetSystem = mission.findMostDistantSystem();
				mission.setTargetPlanet(targetSystem, state);
				acceptMission = new MissionLine(path + "03.mp3", L.string(R.string.mission_accept));
			} else if (state == 1) {
				missionLine = new MissionLine(path + "04.mp3", L.string(R.string.mission_thargoid_documents_fully_serviced));
			} else if (state == 2) {
				missionLine = new MissionLine(path + "05.mp3", L.string(R.string.mission_thargoid_documents_success));
			 	mission.missionCompleted();
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
		if (acceptButton.isPressed(touch)) {
			mission.setPlayerAccepts(true);
			newScreen = new ThargoidDocumentsScreen(1);
		}
		if (declineButton.isPressed(touch)) {
			mission.setPlayerAccepts(false);
			newScreen = new StatusScreen();
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_thargoid_documents));

		g.drawText(L.string(R.string.mission_attention_commander), 50, 200,
			ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		if (missionText != null) {
			displayText(g, missionText);
		}
		if (acceptMission != null) {
			g.drawText(L.string(R.string.mission_accept), 50, 800,
				ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
			if (acceptButton != null) {
				acceptButton.render(g);
			}
			if (declineButton != null) {
				declineButton.render(g);
			}
		}

		if (targetSystem != null) {
			GalaxyScreen.displayStarMap(targetSystem);
		}
	}

	@Override
	public void activate() {
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300,
			800, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		if (acceptMission != null) {
			acceptButton = Button.createGradientPictureButton(50, 860, 200, 200, pics.get("yes_icon"));
			declineButton = Button.createGradientPictureButton(650, 860, 200, 200, pics.get("no_icon"));
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(givenState);
	}

	@Override
	public void loadAssets() {
		addPictures("yes_icon", "no_icon");
	}

	@Override
	public void dispose() {
		super.dispose();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
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
