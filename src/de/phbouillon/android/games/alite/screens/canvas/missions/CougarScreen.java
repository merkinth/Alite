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
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.missions.CougarMission;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;
import de.phbouillon.android.games.alite.screens.canvas.TextData;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.MathHelper;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class CougarScreen extends AliteScreen {
	private final MediaPlayer mediaPlayer;

	private MissionLine missionLine;
	private int lineIndex = 0;
	private SpaceObject cougar;
	private TextData[] missionText;
	private final Timer timer = new Timer().setAutoReset();
	private Vector3f currentDelta = new Vector3f(0,0,0);
	private Vector3f targetDelta = new Vector3f(0,0,0);

	private final int givenState;

	public CougarScreen(int state) {
		givenState = state;
		Mission mission = MissionManager.getInstance().get(CougarMission.ID);
		mediaPlayer = new MediaPlayer();
		String path = MissionManager.DIRECTORY_SOUND_MISSION + "4/";
		try {
			if (state == 0) {
				missionLine = new MissionLine(path + "01.mp3", L.string(R.string.mission_cougar_mission_description));
				cougar = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Cougar);
				cougar.setPosition(200, 0, -700.0f);
				mission.setPlayerAccepts(true);
				mission.setTarget(game.getGenerator().getCurrentGalaxy(), game.getPlayer().getCurrentSystem().getIndex(), 1);
			} else {
				AliteLog.e("Unknown State", "Invalid state variable has been passed to CougarScreen: " + state);
			}
		} catch (IOException e) {
			AliteLog.e("Error reading mission", "Could not read mission audio.", e);
		}
	}

	private void dance() {
		if (timer.hasPassedSeconds(4)) {
			MathHelper.getRandomRotationAngles(targetDelta);
		}
		MathHelper.updateAxes(currentDelta, targetDelta);
		cougar.applyDeltaRotation(currentDelta.x, currentDelta.y, currentDelta.z);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (lineIndex == 0 && !missionLine.isPlaying()) {
			missionLine.play(mediaPlayer);
			lineIndex++;
		}
		if (cougar != null) {
			dance();
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_mission_cougar));

		if (missionText != null) {
			displayText(g, missionText);
		}

		if (cougar != null) {
			displayObject(cougar, 1.0f, 100000.0f);
		} else {
			setUpForDisplay();
		}
	}

	@Override
	public void activate() {
		initGl();
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 200, 800, 40, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		MathHelper.getRandomRotationAngles(targetDelta);
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(givenState);
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
		if (cougar != null) {
			cougar.dispose();
			cougar = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.COUGAR_SCREEN;
	}
}
