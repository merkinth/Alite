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
public class MoreDebugSettingsScreen extends AliteScreen {
	private final Button[] buttons = new Button[7];

	@Override
	public void activate() {
		buttons[0] = Button.createGradientTitleButton(50, 130, 1620, 100,
				L.string(R.string.options_more_debug_start_constrictor_mission))
			.setEvent(b -> startMission(ConstrictorMission.ID));
		buttons[1] = Button.createGradientTitleButton(50, 250, 1620, 100,
				L.string(R.string.options_more_debug_start_thargoid_documents_mission))
			.setEvent(b -> startMission(ThargoidDocumentsMission.ID));
		buttons[2] = Button.createGradientTitleButton(50, 370, 1620, 100,
				L.string(R.string.options_more_debug_start_supernova_mission))
			.setEvent(b -> startMission(SupernovaMission.ID));
		buttons[3] = Button.createGradientTitleButton(50, 490, 1620, 100,
				L.string(R.string.options_more_debug_start_cougar_mission))
			.setEvent(b -> startMission(CougarMission.ID));
		buttons[4] = Button.createGradientTitleButton(50, 610, 1620, 100,
				L.string(R.string.options_more_debug_start_thargoid_base_mission))
			.setEvent(b -> startMission(ThargoidStationMission.ID));
		buttons[5] = Button.createGradientTitleButton(50, 730, 1620, 100,
				L.string(R.string.options_more_debug_clear_mission))
			.setEvent(b -> {
				MissionManager.getInstance().clearActiveMissions();
				String completedMissions = "";
				for (Mission m: MissionManager.getInstance().getMissions()) {
					if (m.isCompleted()) {
						completedMissions += m.getClass().getSimpleName() + "; ";
					}
				}
				showLargeMessageDialog(L.string(R.string.options_more_debug_completed_missions, completedMissions));

			});
		buttons[6] = Button.createGradientTitleButton(50, 970, 1620, 100, L.string(R.string.options_back))
			.setEvent(b -> newScreen = new DebugSettingsScreen());
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_more_debug_options));
		for (Button b : buttons) {
			b.render(g);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		for (Button b : buttons) {
			if (b.isPressed(touch)) {
				b.onEvent();
				return;
			}
		}
	}

	private void startMission(int id) {
		game.getCobra().clearSpecialCargo();
		for (int i = 1; i < id; i++) {
			MissionManager.getInstance().get(i).done();
		}
		MissionManager.getInstance().get(id).resetStarted();
		if (id == ConstrictorMission.ID) {
			game.getPlayer().setIntergalacticJumpCounter(1);
			game.getPlayer().setJumpCounter(62);
		} else {
			game.getPlayer().resetIntergalacticJumpCounter();
			game.getPlayer().setJumpCounter(63);
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.MORE_DEBUG_OPTIONS_SCREEN;
	}

}
