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
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.missions.SupernovaMission;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class SupernovaScreen extends AliteScreen {
	private final MediaPlayer mediaPlayer;

	private MissionLine announcement;
	private MissionLine missionLine;
	private MissionLine acceptMission;
	private int lineIndex = 0;
	private TextData[] missionText;
	private TextData[] announcementText;
	private Button acceptButton;
	private Button declineButton;
	private final Mission mission;
	private final int givenState;

	public SupernovaScreen(int state) {
		givenState = state;
		mission = MissionManager.getInstance().get(SupernovaMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "3/";
		try {
			if (state == 0) {
				announcement = new MissionLine(path + "01.mp3", L.string(R.string.mission_supernova_announcement));
				missionLine = new MissionLine(path + "02.mp3", L.string(R.string.mission_supernova_mission_description));
				acceptMission = new MissionLine(path + "06.mp3", L.string(R.string.mission_supernova_accept));
			} else if (state == 1) {
				missionLine = new MissionLine(path + "05.mp3", L.string(R.string.mission_supernova_mission_decline));
				mission.setTargetPlanet(game.getPlayer().getCurrentSystem(), 2);
			} else if (state == 2) {
				missionLine = new MissionLine(path + "03.mp3", L.string(R.string.mission_supernova_drop_us_off));
				mission.setTargetPlanet(game.getPlayer().getCurrentSystem(), 1);
			} else if (state == 3) {
				missionLine = new MissionLine(path + "04.mp3", L.string(R.string.mission_supernova_success));
				mission.missionCompleted();
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to SupernovaScreen: " + state);
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
		if (lineIndex == 0) {
			if (announcement != null && !announcement.isPlaying()) {
				announcement.play(mediaPlayer);
				lineIndex++;
			} else if (announcement == null && !missionLine.isPlaying()) {
				missionLine.play(mediaPlayer);
				lineIndex += 2;
			}
		} else if (lineIndex == 1 && announcement != null && !announcement.isPlaying() && !missionLine.isPlaying()) {
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
			mission.setState(1);
			mission.setPlayerAccepts(true);
			newScreen = new SupernovaScreen(2);
		}
		if (declineButton.isPressed(touch)) {
			// The player cannot opt to evade the super nova by not accepting
			// the mission, hence we "accept" it here, so that the mission
			// is in the list of active missions. We use a different state,
			// though.
			mission.setState(2);
			mission.setPlayerAccepts(true);
			newScreen = new SupernovaScreen(1);
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_supernova));

		if (announcementText != null) {
			displayText(g, announcementText);
		}
		if (missionText != null) {
			displayText(g, missionText);
		}
		if (acceptMission != null) {
			g.drawText(L.string(R.string.mission_supernova_accept), 50, 800,
				ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
			if (acceptButton != null) {
				acceptButton.render(g);
			}
			if (declineButton != null) {
				declineButton.render(g);
			}
		}
	}

	@Override
	public void activate() {
		if (announcement != null) {
			announcementText = computeTextDisplay(game.getGraphics(), announcement.getText(), 50, 200,
				800, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT));
		}
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50,
			announcement == null ? 200 : 400, 800, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
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
		return ScreenCodes.SUPERNOVA_SCREEN;
	}
}
