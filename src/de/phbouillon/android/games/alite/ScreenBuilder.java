package de.phbouillon.android.games.alite;

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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.phbouillon.android.framework.Screen;
import de.phbouillon.android.games.alite.screens.NavigationBar;
import de.phbouillon.android.games.alite.screens.canvas.*;
import de.phbouillon.android.games.alite.screens.canvas.missions.ConstrictorScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.CougarScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.SupernovaScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ThargoidDocumentsScreen;
import de.phbouillon.android.games.alite.screens.canvas.missions.ThargoidStationScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.AudioOptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.ControlOptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.DebugSettingsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.DisplayOptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.GameplayOptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.InFlightButtonsOptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.MoreDebugSettingsScreen;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutAdvancedFlying;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutBasicFlying;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutEquipment;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutHud;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutIntroduction;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutNavigation;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutTrading;
import de.phbouillon.android.games.alite.screens.canvas.tutorial.TutorialSelectionScreen;
import de.phbouillon.android.games.alite.screens.opengl.AboutScreen;
import de.phbouillon.android.games.alite.screens.opengl.HyperspaceScreen;
import de.phbouillon.android.games.alite.screens.opengl.ingame.FlightScreen;

public class ScreenBuilder {
	static boolean createScreen(byte[] state) {
		int screen = state[0];
		if (screen != ScreenCodes.FLIGHT_SCREEN) {
			try {
				AliteLog.d("[ALITE]", "Loading autosave.");
				Alite.get().autoLoad();
			} catch (IOException e) {
				AliteLog.e("[ALITE]", "Loading autosave commander failed.", e);
			}
		}
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(state, 1, state.length - 1))) {
			AliteLog.d("[ALITE]", "Set screen, code: " + screen + ".");
			Alite.get().setScreen(getInstanceByScreenCode(dis, screen));
			return true;
		} catch (IOException | ClassNotFoundException ignored) {
		} finally {
			moveToScreen();
		}
		return false;
	}

	private static Screen getInstanceByScreenCode(DataInputStream dis, int screenCode) throws IOException, ClassNotFoundException {
		try {
			switch (screenCode) {
				case ScreenCodes.BUY_SCREEN: return new BuyScreen(readString(dis));
				case ScreenCodes.EQUIP_SCREEN: return new EquipmentScreen(readString(dis));
				case ScreenCodes.INVENTORY_SCREEN: return new InventoryScreen(readString(dis));
				case ScreenCodes.GALAXY_SCREEN: return new GalaxyScreen(dis.readFloat(), dis.readInt(), dis.readInt());
				case ScreenCodes.LOCAL_SCREEN: return new LocalScreen(dis.readFloat(), dis.readInt(), dis.readInt());
				case ScreenCodes.SHIP_INTRO_SCREEN: return new ShipIntroScreen(readString(dis));
				case ScreenCodes.STATUS_SCREEN: return new StatusScreen();
				case ScreenCodes.PLANET_SCREEN: PlanetScreen.disposeInhabitantLayers(); return new PlanetScreen();
				case ScreenCodes.QUANTITY_PAD_SCREEN: return new QuantityPadScreen(dis);
				case ScreenCodes.DISK_SCREEN: return new DiskScreen();
				case ScreenCodes.LOAD_SCREEN: return new LoadScreen(dis);
				case ScreenCodes.SAVE_SCREEN: return new SaveScreen(dis);
				case ScreenCodes.CATALOG_SCREEN: return new CatalogScreen(dis);
				case ScreenCodes.OPTIONS_SCREEN: return new OptionsScreen();
				case ScreenCodes.DISPLAY_OPTIONS_SCREEN: return new DisplayOptionsScreen();
				case ScreenCodes.PLUGINS_SCREEN: return new PluginsScreen(dis.readInt());
				case ScreenCodes.AUDIO_OPTIONS_SCREEN: return new AudioOptionsScreen();
				case ScreenCodes.CONTROL_OPTIONS_SCREEN: return new ControlOptionsScreen(dis.readBoolean());
				case ScreenCodes.GAMEPLAY_OPTIONS_SCREEN: return new GameplayOptionsScreen();
				case ScreenCodes.INFLIGHT_BUTTONS_OPTIONS_SCREEN: return new InFlightButtonsOptionsScreen(dis.readBoolean());
				case ScreenCodes.DEBUG_SCREEN: return new DebugSettingsScreen();
				case ScreenCodes.MORE_DEBUG_OPTIONS_SCREEN: return new MoreDebugSettingsScreen();
				case ScreenCodes.ABOUT_SCREEN: return new AboutScreen(dis.readInt(), dis.readInt(), dis.readFloat());
				case ScreenCodes.LIBRARY_SCREEN: return new LibraryScreen(readString(dis), dis.readInt());
				case ScreenCodes.LIBRARY_PAGE_SCREEN: return new LibraryPageScreen(dis);
				case ScreenCodes.TUTORIAL_SELECTION_SCREEN: return new TutorialSelectionScreen();
				case ScreenCodes.HACKER_SCREEN: return new HackerScreen(dis);
				case ScreenCodes.HEX_NUMBER_PAD_SCREEN: return new HexNumberPadScreen(dis);
				case ScreenCodes.CONSTRICTOR_SCREEN: return new ConstrictorScreen(dis);
				case ScreenCodes.COUGAR_SCREEN: return new CougarScreen(dis.readInt());
				case ScreenCodes.SUPERNOVA_SCREEN: return new SupernovaScreen(dis.readInt());
				case ScreenCodes.THARGOID_DOCUMENTS_SCREEN: return new ThargoidDocumentsScreen(dis.readInt());
				case ScreenCodes.THARGOID_STATION_SCREEN: return new ThargoidStationScreen(dis.readInt());
				case ScreenCodes.TUT_INTRODUCTION_SCREEN: return new TutIntroduction(dis);
				case ScreenCodes.TUT_TRADING_SCREEN: return new TutTrading(dis);
				case ScreenCodes.TUT_EQUIPMENT_SCREEN: return new TutEquipment(dis);
				case ScreenCodes.TUT_NAVIGATION_SCREEN: return new TutNavigation(dis);
				case ScreenCodes.TUT_HUD_SCREEN: return new TutHud(dis);
				case ScreenCodes.TUT_BASIC_FLYING_SCREEN: return new TutBasicFlying(dis);
				case ScreenCodes.TUT_ADVANCED_FLYING_SCREEN: return new TutAdvancedFlying(dis);
				case ScreenCodes.HYPERSPACE_SCREEN: return new HyperspaceScreen(dis);
				case ScreenCodes.FLIGHT_SCREEN: PlanetScreen.disposeInhabitantLayers(); return FlightScreen.createScreen(dis);
				case ScreenCodes.ACHIEVEMENTS_SCREEN: return new AchievementsScreen(dis.readInt());
				default: throw new IOException();
			}
		} catch (IOException | ClassNotFoundException e) {
			AliteLog.e("ScreenBuilderError", "Error in initializer of screen " + screenCode + ".", e);
			throw e;
		}
	}

	private static void moveToScreen() {
		Screen screen = Alite.get().getCurrentScreen();
		if (screen == null) {
			return;
		}
		NavigationBar navigationBar = Alite.get().getNavigationBar();
		switch (screen.getScreenCode()) {
			case ScreenCodes.STATUS_SCREEN: Alite.get().setStatusOrAchievements(); break;

			case ScreenCodes.BUY_SCREEN:
			case ScreenCodes.QUANTITY_PAD_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_BUY); break;

			case ScreenCodes.INVENTORY_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_INVENTORY); break;
			case ScreenCodes.EQUIP_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_EQUIP); break;
			case ScreenCodes.GALAXY_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_GALAXY); break;
			case ScreenCodes.LOCAL_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_LOCAL); break;
			case ScreenCodes.PLANET_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_PLANET); break;

			case ScreenCodes.DISK_SCREEN:
			case ScreenCodes.CATALOG_SCREEN:
			case ScreenCodes.LOAD_SCREEN:
			case ScreenCodes.SAVE_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_DISK); break;

			case ScreenCodes.ACHIEVEMENTS_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_ACHIEVEMENTS); break;

			case ScreenCodes.ABOUT_SCREEN:
			case ScreenCodes.DISPLAY_OPTIONS_SCREEN:
			case ScreenCodes.GAMEPLAY_OPTIONS_SCREEN:
			case ScreenCodes.AUDIO_OPTIONS_SCREEN:
			case ScreenCodes.CONTROL_OPTIONS_SCREEN:
			case ScreenCodes.INFLIGHT_BUTTONS_OPTIONS_SCREEN:
			case ScreenCodes.DEBUG_SCREEN:
			case ScreenCodes.MORE_DEBUG_OPTIONS_SCREEN:
			case ScreenCodes.PLUGINS_SCREEN:
			case ScreenCodes.OPTIONS_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_OPTIONS); break;

			case ScreenCodes.LIBRARY_SCREEN:
			case ScreenCodes.LIBRARY_PAGE_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_LIBRARY); break;

			case ScreenCodes.TUTORIAL_SELECTION_SCREEN: navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_ACADEMY); break;
			case ScreenCodes.HACKER_SCREEN:
				if (Alite.get().isHackerActive()) {
					navigationBar.setActiveIndex(Alite.NAVIGATION_BAR_HACKER);
				}
		}
	}

	public static void writeString(DataOutputStream dos, String str) throws IOException {
		dos.writeByte(str == null ? 0 : str.length());
		if (str != null) {
			dos.writeChars(str);
		}
	}

	public static String readString(DataInputStream dis) throws IOException {
		int length = dis.readByte();
		if (length == 0) {
			return null;
		}
		String result = "";
		for (int i=0; i < length; i++) {
			result += dis.readChar();
		}
		return result;
	}

	public static String readEmptyString(DataInputStream dis) throws IOException {
		String result = readString(dis);
		return result == null ? "" : result;
	}

}
