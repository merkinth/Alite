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
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.CommanderData;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class CatalogScreen extends AliteScreen {
	protected String title;
	private List<Button> button = new ArrayList<>();
	List<CommanderData> commanderData = new ArrayList<>();
	private Pixmap buttonBackground;
	private Pixmap buttonBackgroundPushed;
	private Pixmap buttonBackgroundSelected;
	private Button btnListForward;
	private Button btnListBackward;
	private Button btnBack;
	Button deleteButton;
	int currentPage = 0;
	boolean confirmDelete = false;
	List<CommanderData> selectedCommanderData = new ArrayList<>();
	List<Integer> pendingSelectionIndices = null;
	private boolean pendingShowMessage = false;

	CatalogScreen(Alite game, String title) {
		super(game);
		this.title = title;
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
					Button b = Button.createPictureButton(20, 210 + (i % 5) * 140, 1680, 120, buttonBackground)
						.setPushedBackground(buttonBackgroundPushed)
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
					button.get(i).setPixmap(buttonBackgroundSelected).setSelected(true);
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

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		CatalogScreen cs = new CatalogScreen(alite, L.string(R.string.title_catalog));
		try {
			cs.currentPage = dis.readInt();
			cs.confirmDelete = dis.readBoolean();
			int selectionCount = dis.readInt();
			if (selectionCount != 0) {
				cs.pendingSelectionIndices = new ArrayList<>();
				for (int i = 0; i < selectionCount; i++) {
					cs.pendingSelectionIndices.add(dis.readInt());
				}
			}
			cs.pendingShowMessage = dis.readBoolean();
		} catch (IOException e) {
			AliteLog.e("Load Screen Initialize", "Error in initializer.", e);
			return false;
		}
		alite.setScreen(cs);
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
		if (touch.type != TouchEvent.TOUCH_UP) {
			return;
		}
		if (btnBack.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			newScreen = new DiskScreen(game);
			return;
		}
		if (btnListForward.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			currentPage++;
		}
		if (btnListBackward.isTouched(touch.x, touch.y)) {
			SoundManager.play(Assets.click);
			currentPage--;
		}
		for (int i = currentPage * 5; i < Math.min(currentPage * 5 + 5, button.size()); i++) {
			if (button.get(i).isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				boolean select = !selectedCommanderData.contains(commanderData.get(i));
				if (select) {
					selectedCommanderData.add(commanderData.get(i));
					button.get(i).setPixmap(buttonBackgroundSelected);
				} else {
					selectedCommanderData.remove(commanderData.get(i));
					button.get(i).setPixmap(buttonBackground);
				}
				button.get(i).setSelected(select);
				if (deleteButton != null) {
					deleteButton.setText(selectedCommanderData.size() > 1 ?
						L.string(R.string.cmdr_btn_delete_more) : L.string(R.string.cmdr_btn_delete_one));
				}
			}
		}
		if (confirmDelete && messageResult != RESULT_NONE) {
			confirmDelete = false;
			if (messageResult == RESULT_YES) {
				for (CommanderData cd: selectedCommanderData) {
					game.getFileIO().deleteFile(cd.getFileName());
				}
				newScreen = new CatalogScreen(game, L.string(R.string.title_catalog));
			}
			clearSelection();
			messageResult = RESULT_NONE;
		}
		if (deleteButton != null && deleteButton.isTouched(touch.x, touch.y)) {
			if (messageResult == RESULT_NONE) {
				SoundManager.play(Assets.alert);
				if (selectedCommanderData.size() == 1) {
					showQuestionDialog(L.string(R.string.cmdr_delete_one_confirm, selectedCommanderData.get(0).getName()));
				} else {
					showQuestionDialog(L.string(R.string.cmdr_delete_more_confirm));
				}
				confirmDelete = true;
			}
		}
	}

	void clearSelection() {
		for (Button aButton : button) {
			if (aButton.isSelected()) {
				aButton.setPixmap(buttonBackground).setSelected(false);
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
	public void dispose() {
		super.dispose();
		if (buttonBackground != null) {
			buttonBackground.dispose();
			buttonBackground = null;
		}
		if (buttonBackgroundSelected != null) {
			buttonBackgroundSelected.dispose();
			buttonBackgroundSelected = null;
		}
		if (buttonBackgroundPushed != null) {
			buttonBackgroundPushed.dispose();
			buttonBackgroundPushed = null;
		}
	}

	@Override
	public void loadAssets() {
		buttonBackground = game.getGraphics().newPixmap("catalog_button.png");
		buttonBackgroundSelected = game.getGraphics().newPixmap("catalog_button_selected.png");
		buttonBackgroundPushed = game.getGraphics().newPixmap("catalog_button_pushed.png");
		super.loadAssets();
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.CATALOG_SCREEN;
	}
}
