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

import android.graphics.Bitmap;
import android.graphics.Point;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.ColorFilterGenerator;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Medal;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.library.Toc;
import de.phbouillon.android.framework.SpriteData;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.opengl.sprites.buttons.AliteButtons;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class AchievementsScreen extends AliteScreen {
	private static final String TEXTURE_FILE = "medals.png";
	private static final String ICON_GRAY_TROPHY = "gray_trophy";

	private final Set<Medal.Item> viewedMedals = new HashSet<>();
	private int maxHeight;

	private final ScrollPane scrollPane = new ScrollPane(0, 80, AliteConfig.DESKTOP_WIDTH,
		AliteConfig.SCREEN_HEIGHT, () -> new Point(AliteConfig.SCREEN_WIDTH, maxHeight));

	// default public constructor is required for navigation bar
	@SuppressWarnings("unused")
	public AchievementsScreen() {
	}

	public AchievementsScreen(int yPosition) {
		scrollPane.position.y = yPosition;
	}

	@Override
	public void activate() {
		setUpForDisplay();
	}

	private void drawInformation() {
		int y = 120 - scrollPane.position.y;
		Graphics g = game.getGraphics();
		g.setClip(-1, 0, -1, AliteConfig.SCREEN_HEIGHT - 80);
		for (Medal.Item medal : game.getMedals()) {
			String name = L.string(medal.getName());

			int nameColor = ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION);
			int levelColor = ColorScheme.get(ColorScheme.COLOR_RATING);
			int descriptionColor = ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT);
			int nextLevelColor = ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT);

			int reachedThreshold = medal.reachedThreshold();
			if (reachedThreshold == 0) {
				nameColor = AliteColor.grayScale(nameColor);
				levelColor = AliteColor.grayScale(levelColor);
				descriptionColor = AliteColor.grayScale(descriptionColor);
				nextLevelColor = AliteColor.grayScale(nextLevelColor);
			}

			Pixmap icon = getMedalPixmap(medal, reachedThreshold > 0);
			game.getGraphics().drawPixmap(icon, 100 - (icon.getWidth() >> 1), y);
			if (medal.isUnseen()) {
				Pixmap iconNew = pics.get("ext_new");
				game.getGraphics().drawPixmap(iconNew, 100 + (icon.getWidth() >> 1) -
					(iconNew.getWidth() >> 1), y - (iconNew.getHeight() >> 2));
			}

			g.drawText(name, 200, y + 30, nameColor, Assets.titleFont);
			g.drawText(" (" + L.string(R.string.medal_level, medal.getLevel()) + ")",
				200 + g.getTextWidth(name, Assets.titleFont), y + 30, levelColor, Assets.regularFont);

			int nextLevel = medal.nextThreshold();
			g.drawText(nextLevel == 0 ? L.string(R.string.medal_max_level) :
				L.string(R.string.medal_next_threshold, medal.highestLevelThreshold() % 10 != 0 ?
					L.string(R.string.num_all) : getShortCompactNumber(nextLevel)),
				1100, y + 30, nextLevelColor, Assets.regularFont);

			int dy = y;
			if (reachedThreshold > 0 && nextLevel != 0) {
				g.drawText(L.string(R.string.medal_reached_threshold, getShortCompactNumber(reachedThreshold)),
					200, y + 80, levelColor, Assets.regularFont);
				dy += 40;
			}
			TextData[] text = computeTextDisplay(g, L.string(medal.getDescription()), 200, dy + 90,
				900, descriptionColor);
			displayText(g, text);
			y += Math.max(100 + 40 * text.length, icon.getHeight()) + 60;
			if (medal.isUnseen() && y - 60 <= scrollPane.area.bottom) {
				viewedMedals.add(medal);
			}
		}
		maxHeight = y - 120 + scrollPane.position.y;
		g.setClip(-1, -1, -1, -1);
	}

	private Pixmap getMedalPixmap(Medal.Item medal, boolean achieved) {
		switch (medal.getId()) {
			case Medal.MEDAL_ID_TIME:
				return achieved ? getFromButtons("time_drive") : pics.get(ICON_GRAY_TROPHY);

			case 110:
				return pics.get(achieved ? "travel_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_PLANETS:
				return pics.get(achieved ? "local_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_GALAXY:
				return pics.get(achieved ? "galaxy_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_HYPER_JUMP:
				return achieved ? getFromButtons("hyperspace") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_FUEL:
				return pics.get(achieved ? EquipmentStore.FUEL : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_INVENTORY:
				return pics.get(achieved ? "inventory_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_FIREARMS:
				return pics.get(achieved ? "firearms_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_RICH:
				return pics.get(achieved ? "gold_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_MINER:
				return pics.get(achieved ? EquipmentStore.MINING_LASER : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_INSECTICIDE:
				if (!achieved) {
					return pics.get(ICON_GRAY_TROPHY);
				}
				if (!pics.containsKey("insect")) {
					pics.put("insect", game.getGraphics().newPixmap(PlanetScreen.computeImage(
						SystemData.INHABITANT_RACE_ALIEN + "3" + (5 - (medal.getLevel() - 1) / 3) + "77"),
						"medal_insect", 125, 207)); // weird insect
				}
				return pics.get(pics.containsKey("insect") ? "insect" : "trophy");

			case Medal.MEDAL_ID_KILL_THARGON:
				return pics.get(achieved ? "thargon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_KILL_TRADER:
				return pics.get(achieved ? "trader" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_KILL_POLICE:
				return pics.get(achieved ? "police" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_JAMMER:
				return achieved ? getFromButtons("ecm_jammer") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_ENERGY_BOMB:
				return achieved ? getFromButtons("energy_bomb") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_MISSILE:
				return achieved ? getFromButtons("missile") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_ECM:
				return achieved ? getFromButtons("ecm") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_LASER:
				int threshold = medal.reachedThreshold();
				return pics.get(!achieved ? ICON_GRAY_TROPHY : threshold <= 10 ? EquipmentStore.PULSE_LASER :
					threshold <= 100 ? EquipmentStore.BEAM_LASER : EquipmentStore.MILITARY_LASER);

			case Medal.MEDAL_ID_RETRO_ROCKETS:
				return achieved ? getFromButtons("retro_rockets") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_CLOAKING:
				return achieved ? getFromButtons("cloaking_device") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_ESCAPE_CAPSULE:
				return achieved ? getFromButtons("escape_capsule") : pics.get(ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_EQUIPMENT:
				return pics.get(achieved ? "equipment_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_MECHANIC:
				return pics.get(achieved ? "options_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_FUEL_SCOOP:
				return pics.get(achieved ? EquipmentStore.FUEL_SCOOP : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_MISSION:
				return pics.get(achieved ? "mission_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_ABOUT:
				return pics.get(achieved ? "about_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_TUTORIAL:
				return pics.get(achieved ? "academy_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_LIBRARY:
				return pics.get(achieved ? "library_icon" : ICON_GRAY_TROPHY);

			case Medal.MEDAL_ID_INTERRUPTER:
				return pics.get(achieved ? "quit_icon" : ICON_GRAY_TROPHY);
		}
		int i = medal.getLevel();
		while (i >= 0) {
			String spriteName = medal.getId() + "_" + i;
			SpriteData data = Alite.get().getTextureManager().getSprite(TEXTURE_FILE, spriteName);
			if (data != null) {
				if (!pics.containsKey(spriteName)) {
					pics.put(spriteName, getImagePart(data, "medals",  spriteName, -1, -1));
				}
				return pics.get(spriteName);
			}
			i--;
		}
		return pics.get(achieved ? "trophy" : ICON_GRAY_TROPHY);
	}

	private Pixmap getImagePart(SpriteData data, String textureName, String spriteName, int width, int height) {
		Pixmap pixmap = pics.get(textureName);
		AliteLog.d("getImagePart", textureName + ":" + spriteName + " (" + data.x * pixmap.getWidth() +
			"," +  data.y * pixmap.getHeight() + ", "  + data.origWidth + "," + data.origHeight + ")");
		Pixmap medal = game.getGraphics().newPixmap(Bitmap.createBitmap(pixmap.getBitmap(),
			(int) (data.x * pixmap.getWidth()), (int) (data.y * pixmap.getHeight()),
			(int) data.origWidth, (int) data.origHeight), spriteName);
		if (width != -1 && height != -1) {
			medal = getResizedPixmapPart(medal, 0, 0, spriteName, width, height);
		}
		return medal;
	}

	private Pixmap getFromButtons(String spriteName) {
		if (!pics.containsKey(spriteName)) {
			pics.put(spriteName, getImagePart(Alite.get().getTextureManager().getSprite(AliteButtons.TEXTURE_FILE, spriteName),
				"buttons",  spriteName, 150, 150));
		}
		return pics.get(spriteName);
	}

	private String getShortCompactNumber(int number) {
		return number >= 1000000 ? number / 1000000 + L.string(R.string.num_million) :
			number < 10000 ? String.valueOf(number) : number / 1000 + L.string(R.string.num_thousand);
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(scrollPane.position.y);
	}

	@Override
	public void present(float deltaTime) {
		game.getGraphics().clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_achievements));
		drawInformation();
		scrollPane.scrollingFree();
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		scrollPane.handleEvent(touch);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (!viewedMedals.isEmpty()) {
			for (Medal.Item medal : viewedMedals) {
				medal.viewed();
			}
			Settings.save(game.getFileIO());
			game.getNavigationBar().setNotificationNumber(Alite.NAVIGATION_BAR_ACHIEVEMENTS,
				game.getNavigationBar().getNotificationNumber(Alite.NAVIGATION_BAR_ACHIEVEMENTS) - viewedMedals.size());
		}
	}

	@Override
	public void loadAssets() {
		Alite.get().getTextureManager().addTexture(TEXTURE_FILE);
		pics.put("medals", game.getGraphics().newPixmap(TEXTURE_FILE));

		Alite.get().getTextureManager().addTexture(AliteButtons.TEXTURE_FILE);
		pics.put("buttons", game.getGraphics().newPixmap(AliteButtons.TEXTURE_FILE));

		Pixmap p = game.getGraphics().newPixmap(Bitmap.createBitmap(Assets.achievementsIcon.getBitmap(),
			36, 14, 126, 128), "medal_trophy");
		pics.put("trophy", p);
		p = game.getGraphics().newPixmap(p.getBitmap(), ICON_GRAY_TROPHY);
		pics.put(ICON_GRAY_TROPHY, p);
		game.getGraphics().applyFilterToPixmap(p, ColorFilterGenerator.adjustColor(-100, 0)); // grayscale
		addPictures("ext_new");
		pics.put("about_icon", getResizedPixmapPart(Assets.aliteLogoSmall, 0, 0, "about_icon", 150, 100));
		addFromIcon(Assets.inventoryIcon, 10, 26, 180, 114, "inventory_icon");
		addFromIcon(Assets.planetIcon, 0, 39, 177, 72, "travel_icon");
		addFromIcon(Assets.localIcon, 7, 7, 175, 154, "local_icon");
		addFromIcon(Assets.galaxyIcon, 10, 11, 180, 129, "galaxy_icon");
		addFromIcon(Assets.equipIcon, 29, 13, 138, 140, "equipment_icon");
		addFromIcon(Assets.optionsIcon, 28, 9, 143, 158, "options_icon");
		addFromIcon(Assets.academyIcon, 19, 0, 163, 163, "academy_icon");
		addFromIcon(Assets.libraryIcon, 16, 5, 167, 157, "library_icon");
		addFromIcon(Assets.quitIcon, 24, 2, 153, 150, "quit_icon");

		pics.put("firearms_icon", game.getGraphics().newPixmap(TradeGoodStore.get().getGoodById(
			TradeGoodStore.FIREARMS).getIconName(), 150, 150));
		addFromIcon(game.getGraphics().newPixmap(TradeGoodStore.get().getGoodById(
			TradeGoodStore.GOLD).getIconName()), 6, 36, 214, 177, "gold_icon");
		addFromIcon(game.getGraphics().newPixmap(TradeGoodStore.get().getGoodById(
			TradeGoodStore.THARGOID_DOCUMENTS).getIconName()), 0, 25, 223, 170, "mission_icon");
		try {
			addFromLibrary("thargon", 275, 47, "thargon");
			addFromLibrary("anaconda", 250, 195, "trader");
			addFromLibrary("viper_gui", 360, 65, "police");

			setFromEquipment(EquipmentStore.FUEL, 0);
			setFromEquipment(EquipmentStore.FUEL_SCOOP, 0);
			setFromEquipment(EquipmentStore.MINING_LASER, 31);
			setFromEquipment(EquipmentStore.PULSE_LASER, 23);
			setFromEquipment(EquipmentStore.BEAM_LASER, 0);
			setFromEquipment(EquipmentStore.MILITARY_LASER, 0);
		} catch (IOException e) {
			AliteLog.e("Achievement icon load error", "Loading icon failed.", e);
		}
		super.loadAssets();
	}

	private void addFromIcon(Pixmap pixmap, int x, int y, int width, int height, String name) {
		pics.put(name, getResizedPixmapRect(pixmap, x, y, width, height, name, 150, 150 * height / width ));
	}

	private void addFromLibrary(String fileName, int x, int y, String name) throws IOException {
		fileName = Toc.DIRECTORY_LIBRARY + fileName + ".png";
		Pixmap pixmap = game.getGraphics().newPixmap(fileName, game.getFileIO().readPrivateFile(fileName), -1, -1);
		pics.put(name, getResizedPixmapPart(pixmap, x, y, "medal_" + name, 150,
			150 * (pixmap.getHeight() - y) / (pixmap.getWidth() - x)));
	}

	private void setFromEquipment(String equipmentName, int top) throws IOException {
		String fileName = EquipmentStore.get().getEquipmentById(equipmentName).getIcon() + ".png";
		pics.put(equipmentName, getResizedPixmapPart(game.getGraphics().newPixmap(fileName,
			game.getFileIO().readAssetFile(fileName), -1, -1), 0, top, equipmentName, 150, 150));
	}

	private Pixmap getResizedPixmapPart(Pixmap pixmap, int x, int y, String name, int destWidth, int destHeight) {
		return getResizedPixmapRect(pixmap, x, y, pixmap.getWidth() - x, pixmap.getHeight() - y,
			name, destWidth, destHeight);
	}

	private Pixmap getResizedPixmapRect(Pixmap pixmap, int x, int y, int width, int height,
			String name, int destWidth, int destHeight) {
		return game.getGraphics().newPixmap(Bitmap.createBitmap(pixmap.getBitmap(), x, y, width, height),
			name, destWidth, destHeight);
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.ACHIEVEMENTS_SCREEN;
	}
}
