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

import android.media.MediaPlayer;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
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
	private final transient MediaPlayer mediaPlayer;

	private MissionLine attCommander;
	private MissionLine missionLine;
	private MissionLine acceptMission;
	private int lineIndex = 0;
	private SpaceObject constrictor;
	private TextData[] missionText;
	private final Timer timer = new Timer().setAutoReset();
	private final Vector3f currentDelta = new Vector3f(0,0,0);
	private final Vector3f targetDelta = new Vector3f(0,0,0);

	private Button acceptButton;
	private Button declineButton;
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
				mission.setTargetPlanet(targetSystem, state);
			} else if (state == 2) {
				missionLine = new MissionLine(path + "06.mp3", L.string(R.string.mission_constrictor_intergalactic_jump));
				mission.setTargetGalaxy(game.getGenerator().getNextGalaxy(), state);
			} else if (state == 3) {
				missionLine = new MissionLine(path + "05.mp3", L.string(R.string.mission_constrictor_hyperspace_jump));
				targetSystem = mission.findRandomSystemInRange(75, 120);
				mission.setTargetPlanet(targetSystem, mission.getState() + 1);
			} else if (state == 4) {
				missionLine = new MissionLine(path + "07.mp3", L.string(R.string.mission_constrictor_success));
				mission.missionCompleted();
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
			mission.setTargetPlanet(targetSystem, mission.getState() - 1);
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
		if (acceptButton.isPressed(touch)) {
			mission.setPlayerAccepts(true);
			newScreen = new ConstrictorScreen(1);
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
		displayTitle(L.string(R.string.title_mission_constrictor));

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

		if (constrictor != null) {
			displayObject(constrictor, 1.0f, 100000.0f);
		} else if (targetSystem != null) {
			GalaxyScreen.displayStarMap(targetSystem);
		}
		setUpForDisplay();
	}

	@Override
	public void activate() {
		initGl();
		missionText = computeTextDisplay(game.getGraphics(), missionLine.getText(), 50, 300,
			800, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		MathHelper.getRandomRotationAngles(targetDelta);
		if (acceptMission != null) {
			acceptButton = Button.createGradientPictureButton(50, 860, 200, 200, pics.get("yes_icon"));
			declineButton = Button.createGradientPictureButton(650, 860, 200, 200, pics.get("no_icon"));
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
		addPictures("yes_icon", "no_icon");
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
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.CONSTRICTOR_SCREEN;
	}
}
