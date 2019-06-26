package de.phbouillon.android.games.alite.screens.canvas;

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
import java.util.ArrayList;

import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.CommanderData;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class LoadScreen extends CatalogScreen {
	private boolean confirmedLoad = false;
	private boolean pendingShowMessage = false;

	LoadScreen(Alite game, String title) {
		super(game, title);
	}

	@Override
	public void activate() {
		super.activate();
		deleteButton = null;
		if (pendingShowMessage) {
			showQuestionDialog(L.string(R.string.cmdr_load_confirm, selectedCommanderData.get(0).getName()));
			confirmDelete = false;
			pendingShowMessage = false;
		}
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		LoadScreen ls = new LoadScreen(alite, L.string(R.string.title_cmdr_load));
		try {
			ls.currentPage = dis.readInt();
			ls.confirmDelete = dis.readBoolean();
			int selectionCount = dis.readInt();
			if (selectionCount != 0) {
				ls.pendingSelectionIndices = new ArrayList<>();
				for (int i = 0; i < selectionCount; i++) {
					ls.pendingSelectionIndices.add(dis.readInt());
				}
			}
			ls.pendingShowMessage = dis.readBoolean();
		} catch (IOException e) {
			AliteLog.e("Load Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(ls);
		return true;
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(currentPage);
		dos.writeBoolean(confirmDelete);
		dos.writeInt(selectedCommanderData.size());
		for (CommanderData c: selectedCommanderData) {
			// This is o(n^2), but does it really matter?
			// The commander data could be transformed to a map, and doing it
			// here would simplify to o(n) (2 * n, to be precise), but even if
			// someone has stored 10000 commanders and wants to delete all of them,
			// this lookup isn't the problem.
			dos.writeInt(commanderData.indexOf(c));
		}
		dos.writeBoolean(isMessageDialogActive());
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		super.processTouch(touch);
		if (confirmedLoad) {
			newScreen = new StatusScreen(game);
			confirmedLoad = false;
		}
		if (selectedCommanderData.size() == 1) {
			if (messageResult == RESULT_NONE) {
				showQuestionDialog(L.string(R.string.cmdr_load_confirm, selectedCommanderData.get(0).getName()));
				confirmDelete = false;
				SoundManager.play(Assets.alert);
				return;
			}
			if (messageResult == RESULT_YES) {
				try {
					game.loadCommander(selectedCommanderData.get(0).getFileName());
					showMessageDialog(L.string(R.string.cmdr_load_succeeded, selectedCommanderData.get(0).getDockedSystem()));
					SoundManager.play(Assets.alert);
					confirmedLoad = true;
				} catch (IOException e) {
					showMessageDialog(L.string(R.string.cmdr_load_failed, selectedCommanderData.get(0).getName(), e.getMessage()));
				}
			}
			clearSelection();
			messageResult = RESULT_NONE;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.LOAD_SCREEN;
	}
}
