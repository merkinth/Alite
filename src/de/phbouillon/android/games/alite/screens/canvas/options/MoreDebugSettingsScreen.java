package de.phbouillon.android.games.alite.screens.canvas.options;

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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.missions.ConstrictorMission;
import de.phbouillon.android.games.alite.model.missions.CougarMission;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.missions.SupernovaMission;
import de.phbouillon.android.games.alite.model.missions.ThargoidDocumentsMission;
import de.phbouillon.android.games.alite.model.missions.ThargoidStationMission;
import de.phbouillon.android.games.alite.screens.canvas.AliteScreen;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class MoreDebugSettingsScreen extends AliteScreen {
	private Button startConstrictorMission;
	private Button startThargoidDocumentsMission;
	private Button startSupernovaMission;
	private Button startCougarMission;
	private Button startThargoidBaseMission;
	private Button clearMission;
    private Button back;

	MoreDebugSettingsScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
		startConstrictorMission = Button.createGradientTitleButton(50, 130, 1620, 100, L.string(R.string.options_more_debug_start_constrictor_mission));
		startThargoidDocumentsMission = Button.createGradientTitleButton(50, 250, 1620, 100, L.string(R.string.options_more_debug_start_thargoid_documents_mission));
		startSupernovaMission = Button.createGradientTitleButton(50, 370, 1620, 100, L.string(R.string.options_more_debug_start_supernova_mission));
		startCougarMission = Button.createGradientTitleButton(50, 490, 1620, 100, L.string(R.string.options_more_debug_start_cougar_mission));
		startThargoidBaseMission = Button.createGradientTitleButton(50, 610, 1620, 100, L.string(R.string.options_more_debug_start_thargoid_base_mission));
		clearMission = Button.createGradientTitleButton(50, 730, 1620, 100, L.string(R.string.options_more_debug_clear_mission));
		back = Button.createGradientTitleButton(50, 970, 1620, 100, L.string(R.string.options_back));
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_more_debug_options));

		startConstrictorMission.render(g);
		startThargoidDocumentsMission.render(g);
		startSupernovaMission.render(g);
		startCougarMission.render(g);
		startThargoidBaseMission.render(g);
		clearMission.render(g);
		back.render(g);
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (startConstrictorMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().clearSpecialCargo();
			game.getPlayer().clearMissions();
			game.getPlayer().setIntergalacticJumpCounter(1);
			game.getPlayer().setJumpCounter(62);
			MissionManager.getInstance().get(ConstrictorMission.ID).resetStarted();
			return;
		}
		if (startThargoidDocumentsMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().clearSpecialCargo();
			game.getPlayer().clearMissions();
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ConstrictorMission.ID));
			MissionManager.getInstance().get(ThargoidDocumentsMission.ID).resetStarted();
			game.getPlayer().resetIntergalacticJumpCounter();
			game.getPlayer().setJumpCounter(63);
			return;
		}
		if (startSupernovaMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().clearSpecialCargo();
			game.getPlayer().clearMissions();
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ConstrictorMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ThargoidDocumentsMission.ID));
			MissionManager.getInstance().get(SupernovaMission.ID).resetStarted();
			game.getPlayer().resetIntergalacticJumpCounter();
			game.getPlayer().setJumpCounter(63);
			return;
		}
		if (startCougarMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().clearSpecialCargo();
			game.getPlayer().clearMissions();
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ConstrictorMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ThargoidDocumentsMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(SupernovaMission.ID));
			MissionManager.getInstance().get(CougarMission.ID).resetStarted();
			game.getPlayer().resetIntergalacticJumpCounter();
			game.getPlayer().setJumpCounter(63);
			return;
		}
		if (startThargoidBaseMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getCobra().clearSpecialCargo();
			game.getPlayer().clearMissions();
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ConstrictorMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(ThargoidDocumentsMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(SupernovaMission.ID));
			game.getPlayer().addCompletedMission(MissionManager.getInstance().get(CougarMission.ID));
			MissionManager.getInstance().get(ThargoidStationMission.ID).resetStarted();
			game.getPlayer().resetIntergalacticJumpCounter();
			game.getPlayer().setJumpCounter(63);
			return;
		}
		if (clearMission.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			game.getPlayer().getActiveMissions().clear();
			String completedMissions = "";
			for (Mission m: game.getPlayer().getCompletedMissions()) {
				completedMissions += m.getClass().getSimpleName() + "; ";
			}
			showMessageDialog(L.string(R.string.options_more_debug_completed_missions, completedMissions));
			return;
		}
		if (back.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new DebugSettingsScreen(game);
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.MORE_DEBUG_OPTIONS_SCREEN;
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		alite.setScreen(new MoreDebugSettingsScreen(alite));
		return true;
	}
}
