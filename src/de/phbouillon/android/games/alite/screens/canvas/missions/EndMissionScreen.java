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
import java.io.IOException;

import android.media.MediaPlayer;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.missions.EndMission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class EndMissionScreen extends AliteScreen {
	private final MediaPlayer mediaPlayer;

	private MissionLine welcomeLine;
	private MissionLine missionLine;
	private MissionLine congratulationsLine;
	private TextData[] welcomeText;
	private TextData[] missionText;
	private TextData[] congratulationsText;
    private int state = 0;

	public EndMissionScreen(Alite game, int state) {
		super(game);
		EndMission mission = (EndMission) MissionManager.getInstance().get(EndMission.ID);
		mediaPlayer = new MediaPlayer();
		try {
			if (state == 0) {
				welcomeLine = new MissionLine(MissionManager.DIRECTORY_SOUND_MISSION + "01.mp3", L.string(R.string.mission_elite_welcome_commander));
				missionLine = new MissionLine(null, L.string(R.string.mission_elite_mission_description));
				congratulationsLine = new MissionLine(null, L.string(R.string.mission_elite_congratulations));
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to EndMissionScreen: " + state);
			}
		} catch (IOException e) {
			AliteLog.e("Error reading mission", "Could not read mission audio.", e);
		}
		mission.setPlayerAccepts(true);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (state == 0 && welcomeLine != null && !welcomeLine.isPlaying()) {
			welcomeLine.play(mediaPlayer);
			state = 1;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_elite));

		if (welcomeText != null) {
			displayText(g, welcomeText);
		}
		if (missionText != null) {
			displayText(g, missionText);
		}
		if (congratulationsText != null) {
			displayText(g, congratulationsText);
		}
	}

	@Override
	public void activate() {
		if (welcomeLine != null) {
			welcomeText = computeTextDisplay(game.getGraphics(), welcomeLine.getText(), 50, 200, 800, 40, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT));
			missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300, 800, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
			congratulationsText = computeTextDisplay(game.getGraphics(), congratulationsLine.getText(), 50, 800, 800, 40, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT));
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new EndMissionScreen(alite, 0));
		return true;
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
		return ScreenCodes.END_MISSION_SCREEN;
	}
}
