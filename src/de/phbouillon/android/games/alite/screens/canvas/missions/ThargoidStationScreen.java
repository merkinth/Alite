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
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.missions.ThargoidStationMission;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class ThargoidStationScreen extends AliteScreen {
	private final MediaPlayer mediaPlayer;

	private MissionLine attCommander;
	private MissionLine missionLine;
	private int lineIndex = 0;
	private TextData[] missionText;
	private final int givenState;

	public ThargoidStationScreen(int state) {
		givenState = state;
		Mission mission = MissionManager.getInstance().get(ThargoidStationMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "5/";
		try {
			attCommander = new MissionLine(path + "01.mp3", L.string(R.string.mission_attention_commander));
			if (state == 0) {
				missionLine = new MissionLine(path + "02.mp3", L.string(R.string.mission_thargoid_station_mission_description));
				mission.setPlayerAccepts(true);
				mission.setTarget(game.getGenerator().getCurrentGalaxy(), game.getPlayer().getCurrentSystem().getIndex(), 1);
			} else if (state == 1) {
				missionLine = new MissionLine(path + "04.mp3", L.string(R.string.mission_thargoid_station_success));
			 	mission.onMissionComplete();
				Player player = game.getPlayer();
				player.removeActiveMission(mission);
				player.addCompletedMission(mission);
				player.resetIntergalacticJumpCounter();
				player.resetJumpCounter();
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to ThargoidStationScreen: " + state);
			}
		} catch (IOException e) {
			AliteLog.e("Error reading mission", "Could not read mission audio.", e);
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (lineIndex == 0 && !attCommander.isPlaying()) {
			attCommander.play(mediaPlayer);
			lineIndex++;
		} else if (lineIndex == 1 && !attCommander.isPlaying() && !missionLine.isPlaying()) {
			missionLine.play(mediaPlayer);
			lineIndex++;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_thargoid_station));

		g.drawText(L.string(R.string.mission_attention_commander), 50, 200, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		if (missionText != null) {
			displayText(g, missionText);
		}
	}

	@Override
	public void activate() {
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300, 800, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(givenState);
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
		return ScreenCodes.THARGOID_STATION_SCREEN;
	}
}
