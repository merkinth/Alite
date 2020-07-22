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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.CommanderData;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class CatalogScreen extends AliteScreen {
	protected String title;
	private final List<Button> button = new ArrayList<>();
	List<CommanderData> commanderData = new ArrayList<>();
	private Button btnListForward;
	private Button btnListBackward;
	private Button btnBack;
	Button deleteButton;
	int currentPage = 0;
	boolean confirmDelete = false;
	List<CommanderData> selectedCommanderData = new ArrayList<>();
	List<Integer> pendingSelectionIndices = null;
	private boolean pendingShowMessage = false;

	CatalogScreen(String title) {
		this.title = title;
	}

	public CatalogScreen(final DataInputStream dis) throws IOException {
		title = L.string(R.string.title_catalog);
		currentPage = dis.readInt();
		confirmDelete = dis.readBoolean();
		int selectionCount = dis.readInt();
		if (selectionCount != 0) {
			pendingSelectionIndices = new ArrayList<>();
			for (int i = 0; i < selectionCount; i++) {
				pendingSelectionIndices.add(dis.readInt());
			}
		}
		pendingShowMessage = dis.readBoolean();
	}

	@Override
	public void activate() {
		File[] commanders = game.getCommanderFiles();
		btnListBackward = Button.createGradientRegularButton(1400, 950, 100, 100, "<");
		btnListForward = Button.createGradientRegularButton(1550, 950, 100, 100, ">");
		btnBack = Button.createGradientRegularButton(1100, 950, 250, 100, L.string(R.string.options_back));

		button.clear();
		commanderData.clear();
		if (commanders != null) {
			for (int i = 0; i < commanders.length; i++) {
				CommanderData data = game.getQuickCommanderInfo(commanders[i].getName());
				if (data != null) {
					Button b = Button.createPictureButton(20, 210 + (i % 5) * 140, 1680, 120,
						pics.get("catalog_button"))
						.setPushedBackground(pics.get("catalog_button_pushed"))
						.setText("")
						.setFont(Assets.regularFont);
					button.add(b);
					commanderData.add(data);
				}
			}
		}
		Collections.sort(commanderData, (c1, c2) -> {
			if (c1 == null) {
				return c2 == null ? 0 : -1;
			}
			if (c2 == null) {
				return 1;
			}
			// Autosave always comes first
			if (c1.isAutoSaved()) {
				return c2.isAutoSaved() ? Long.compare(game.getFileIO().fileLastModifiedDate(c2.getFileName()),
					game.getFileIO().fileLastModifiedDate(c1.getFileName())) : -1;
			}
			if (c2.isAutoSaved()) {
				return 1;
			}
			String n1 = c1.getName() == null ? "" : c1.getName();
			String n2 = c2.getName() == null ? "" : c2.getName();
			int result = n1.compareTo(n2);
			// We want to display longer game times first.
			return result == 0 ? c1.getGameTime() < c2.getGameTime() ? 1 : -1 : result;
		});

		deleteButton = Button.createGradientRegularButton(50, 950, 600, 100, L.string(R.string.cmdr_btn_delete_one));
		if (pendingSelectionIndices != null) {
			int n = commanderData.size();
			for (int i: pendingSelectionIndices) {
				if (i >= 0 && i < n) {
					selectedCommanderData.add(commanderData.get(i));
					button.get(i).setPixmap(pics.get("catalog_button_selected")).setSelected(true);
				}
			}
			pendingSelectionIndices.clear();
		}
		if (pendingShowMessage) {
			if (selectedCommanderData.size() == 1) {
				showQuestionDialog(L.string(R.string.cmdr_delete_one_confirm, selectedCommanderData.get(0).getName()));
			} else {
				showQuestionDialog(L.string(R.string.cmdr_delete_more_confirm));
			}
			pendingShowMessage = false;
		}
		confirmDelete = true;
	}

	protected static CatalogScreen initializeScreen(final DataInputStream dis, CatalogScreen screen) throws IOException {
		screen.currentPage = dis.readInt();
		screen.confirmDelete = dis.readBoolean();
		int selectionCount = dis.readInt();
		if (selectionCount != 0) {
			screen.pendingSelectionIndices = new ArrayList<>();
			for (int i = 0; i < selectionCount; i++) {
				screen.pendingSelectionIndices.add(dis.readInt());
			}
		}
		screen.pendingShowMessage = dis.readBoolean();
		return screen;
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
		if (btnBack.isPressed(touch)) {
			newScreen = new DiskScreen();
			return;
		}
		if (btnListForward.isPressed(touch)) {
			currentPage++;
		}
		if (btnListBackward.isPressed(touch)) {
			currentPage--;
		}
		for (int i = currentPage * 5; i < Math.min(currentPage * 5 + 5, button.size()); i++) {
			if (button.get(i).isPressed(touch)) {
				boolean select = !selectedCommanderData.contains(commanderData.get(i));
				if (select) {
					selectedCommanderData.add(commanderData.get(i));
					button.get(i).setPixmap(pics.get("catalog_button_selected"));
				} else {
					selectedCommanderData.remove(commanderData.get(i));
					button.get(i).setPixmap(pics.get("catalog_button"));
				}
				button.get(i).setSelected(select);
				if (deleteButton != null) {
					deleteButton.setText(selectedCommanderData.size() > 1 ?
						L.string(R.string.cmdr_btn_delete_more) : L.string(R.string.cmdr_btn_delete_one));
				}
			}
		}
		if (deleteButton != null && deleteButton.isPressed(touch) && messageResult == RESULT_NONE) {
			if (selectedCommanderData.size() == 1) {
				showQuestionDialog(L.string(R.string.cmdr_delete_one_confirm, selectedCommanderData.get(0).getName()));
			} else {
				showQuestionDialog(L.string(R.string.cmdr_delete_more_confirm));
			}
			confirmDelete = true;
		}

		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (confirmDelete && messageResult != RESULT_NONE) {
			confirmDelete = false;
			if (messageResult == RESULT_YES) {
				for (CommanderData cd: selectedCommanderData) {
					game.getFileIO().deleteFile(cd.getFileName());
				}
				newScreen = new CatalogScreen(L.string(R.string.title_catalog));
			}
			clearSelection();
			messageResult = RESULT_NONE;
		}
	}

	void clearSelection() {
		for (Button aButton : button) {
			if (aButton.isSelected()) {
				aButton.setPixmap(pics.get("catalog_button")).setSelected(false);
			}
		}
		selectedCommanderData.clear();
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(title);

		g.diagonalGradientRect(20, 100, 1680, 80, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT));
		g.rec3d(20, 100, 1680, 800, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));
		g.drawText(L.string(R.string.cmdr_table_name), 50, 160, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		g.drawText(L.string(R.string.cmdr_table_system), 600, 160, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		g.drawText(L.string(R.string.cmdr_table_time), 850, 160, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		g.drawText(L.string(R.string.cmdr_table_score), 1100, 160, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		g.drawText(L.string(R.string.cmdr_table_rating), 1300, 160, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.titleFont);
		g.rec3d(20, 100, 1680, 80, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));
		g.rec3d(590, 100, 3, 800, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));
		g.rec3d(840, 100, 3, 800, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));
		g.rec3d(1090, 100, 3, 800, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));
		g.rec3d(1290, 100, 3, 800, 3, ColorScheme.get(ColorScheme.COLOR_FRAME_LIGHT), ColorScheme.get(ColorScheme.COLOR_FRAME_DARK));

		for (int i = currentPage * 5; i < Math.min(currentPage * 5 + 5, button.size()); i++) {
			button.get(i).render(g);
			CommanderData data = commanderData.get(i);
			int color = ColorScheme.get(i % 2 == 0 ? ColorScheme.COLOR_MESSAGE : ColorScheme.COLOR_MAIN_TEXT);
			String textToDisplay = data.getName();
			String suffix = "";
			if (data.isAutoSaved()) {
				int no = 0;
				if (data.getFileName().contains("1")) {
					no = 1;
				} else if (data.getFileName().contains("2")) {
					no = 2;
				}
				textToDisplay = "[" +  L.string(R.string.cmdr_file_autosave) + " " + (no + 1) + "] " + data.getName();
			}
			while (g.getTextWidth(textToDisplay + suffix, Assets.regularFont) > 500) {
				textToDisplay = textToDisplay.substring(0, textToDisplay.length() - 1);
				suffix = "...";
			}
			g.drawText(textToDisplay + suffix, 50, 285 + (i % 5) * 140, color, Assets.regularFont);
			g.drawText(data.getDockedSystem(), 600, 285 + (i % 5) * 140, color, Assets.regularFont);
			String timeString = StatusScreen.getGameTime(data.getGameTime());
			g.drawText(timeString, AliteConfig.SCREEN_HEIGHT - g.getTextWidth(timeString, Assets.regularFont),
				285 + (i % 5) * 140, color, Assets.regularFont);
			g.drawText("" + data.getPoints(), 1280 - g.getTextWidth("" + data.getPoints(), Assets.regularFont),
				285 + (i % 5) * 140, color, Assets.regularFont);
			g.drawText(data.getRating().getName(), 1300, 285 + (i % 5) * 140, color, Assets.regularFont);
		}
		btnListBackward.render(g);
		btnListForward.render(g);
		btnBack.render(g);
		if (deleteButton != null) {
			deleteButton.render(g);
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		btnListBackward.setVisible(currentPage > 0);
		btnListForward.setVisible(currentPage < (button.size() - 1) / 5);
		if (deleteButton != null) {
			deleteButton.setVisible(!selectedCommanderData.isEmpty());
		}
	}

	@Override
	public void loadAssets() {
		addPictures("catalog_button", "catalog_button_selected", "catalog_button_pushed");
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.CATALOG_SCREEN;
	}
}
