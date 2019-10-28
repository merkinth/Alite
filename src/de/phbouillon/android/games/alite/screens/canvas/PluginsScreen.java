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

import android.graphics.Color;
import android.opengl.GLES11;
import android.os.Messenger;
import android.text.format.DateUtils;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.impl.PluginManagerImpl;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.screens.canvas.options.OptionsScreen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class PluginsScreen extends AliteScreen implements IDownloaderClient {
	public static final String DIRECTORY_PLUGINS = "plugins" + File.separator;
	public static final String PLUGINS_META_FILE = "alite_plugins.json";

	private final List<Button> buttons = new ArrayList<>();
	private Button btnBack;
	private PluginModel pluginModel;
	private List<Plugin> plugins;
	private Plugin currentPlugin;
	private int yPosition = 0;
	private int startY;
	private int startX;
	private int lastY;
	private int maxY;
	private float deltaY = 0.0f;

	private Pixmap iconDownload;
	private Pixmap iconNew;
	private Pixmap iconDownloadable;
	private Pixmap iconInstalled;
	private Pixmap iconUpgraded;
	private Pixmap iconOutdated;
	private Pixmap iconRemoved;
	private Pixmap iconNewOfRemoved;

	private int downloadState;
	private DownloadProgressInfo progress;

	public PluginsScreen(Alite game) {
		super(game);
	}


	@Override
	public void activate() {
		pluginModel = new PluginModel(game.getFileIO(), DIRECTORY_PLUGINS + PLUGINS_META_FILE);
		plugins = pluginModel.getOrderedListOfPlugins();
		buildPluginButtons();
		btnBack = Button.createGradientRegularButton(1400, 950, 250, 100, L.string(R.string.options_back));
		setMaxY();
	}

	private void buildPluginButtons() {
		buttons.clear();
		Graphics g = game.getGraphics();
		int y = 100;
		int width = (AliteConfig.SCREEN_WIDTH >> 1) - 150;
		for (Plugin plugin : plugins) {
			TextData[] description = computeTextDisplayWidths(g, plugin.description != null && !plugin.description.trim().isEmpty() ?
				plugin.description : L.string(R.string.plugins_no_description), 110, 2 * (int) Assets.titleFont.getSize(),
				new int[] {width, width, 1300 }, 50, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT));
			TextData[] removalReason = null;
			if (Plugin.META_STATUS_REMOVED.equals(plugin.status) && plugin.removalReason != null && !plugin.removalReason.trim().isEmpty()) {
				removalReason = computeTextDisplayWidths(g, L.string(R.string.plugins_removal_reason, plugin.removalReason),
				110, description[description.length-1].y + 50,  new int[] { description.length == 1 ? width : 1300, 1300 },
					50, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT));
			}
			TextData[] text = new TextData[description.length + (removalReason != null ? removalReason.length : 0) + 1];
			System.arraycopy(description, 0, text, 1, description.length);
			if (removalReason != null) {
				System.arraycopy(removalReason, 0, text, description.length + 1, removalReason.length);
			}
			text[0] = new TextData(getPluginName(plugin), 110, (int) Assets.titleFont.getSize(),
				ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.titleFont);

			Button item = Button.createRegularButton(20, y, AliteConfig.DESKTOP_WIDTH - 40, 50 * text.length + 60, null)
				.setTextData(text);
			buttons.add(item);
			ButtonRegistry.get().removeButton(this, item);
			int yc = y + (item.getHeight() - 110 >> 1);
			buttons.add(Button.createPictureButton(1430, yc, 110, 110, iconDownload, 0.85f)
				.setPixmapOffset(5, 5)
				.setCommand(RESULT_YES));
			buttons.add(Button.createPictureButton(1560, yc, 110, 110, Assets.noIcon, 0.85f)
				.setPixmapOffset(5, 5)
				.setCommand(RESULT_NO));

			y += item.getHeight() + 20;
		}
	}

	private String getPluginName(Plugin plugin) {
		return plugin.filename.substring(0, plugin.filename.lastIndexOf('.'));
	}

	private void setMaxY() {
		if (buttons.isEmpty()) {
			maxY = 0;
			return;
		}
		Button last = buttons.get(buttons.size() - 1);
		maxY = last.getY() + last.getHeight() - 870;
		if (maxY < 0) {
			maxY = 0;
		}
	}

	public static boolean initialize(Alite alite, DataInputStream dis) {
		try {
			PluginsScreen screen = new PluginsScreen(alite);
			screen.yPosition = dis.readInt();
			alite.setScreen(screen);
			return true;
		} catch (IOException e) {
			AliteLog.e("Plugins Screen Initialize", "Error in initializer.", e);
			return false;
		}
	}

	@Override
	public void saveScreenState(DataOutputStream dos) throws IOException {
		dos.writeInt(yPosition);
	}

	@Override
	public synchronized void update(float deltaTime) {
		if (downloadState == 0) {
			super.update(deltaTime);
		} else {
			updateWithoutNavigation(deltaTime);
		}
		if (messageResult == RESULT_YES) {
			pluginModel.removePlugin(currentPlugin, inputText);
			buildPluginButtons(); // required only for change removal reason / description text
		}
		messageResult = RESULT_NONE;
		if (downloadState >= IDownloaderClient.STATE_COMPLETED && !isMessageDialogActive()) {
			String message = L.string(downloadState == IDownloaderClient.STATE_COMPLETED ?
				R.string.plugins_download_succeeded : R.string.plugins_download_failed, getPluginName(currentPlugin));
			AliteLog.d("Extension download", message + " " +
				L.string(Helpers.getDownloaderStringResourceIDFromState(downloadState)));
			if (downloadState == IDownloaderClient.STATE_COMPLETED) {
				pluginModel.downloadedPlugin(currentPlugin);
				buildPluginButtons(); // required only for change removal reason / description text
			}
			showMessageDialog(message);
		}
	}

	@Override
	protected void processTouch(TouchEvent touch) {
		if (downloadState >= IDownloaderClient.STATE_COMPLETED && !isMessageDialogActive()) {
			downloadState = 0;
			progress = null;
		}
		if (downloadState != 0) {
			return;
		}

		if (touch.type == TouchEvent.TOUCH_DOWN && touch.pointer == 0) {
			startX = touch.x;
			startY = lastY = touch.y;
			deltaY = 0;
			return;
		}

		if (touch.type == TouchEvent.TOUCH_DRAGGED && touch.pointer == 0) {
			if (touch.x > AliteConfig.DESKTOP_WIDTH) {
				return;
			}
			yPosition += lastY - touch.y;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
			lastY = touch.y;
			return;
		}

		if (touch.type == TouchEvent.TOUCH_UP && touch.pointer == 0) {
			if (touch.x > AliteConfig.DESKTOP_WIDTH || Math.abs(startX - touch.x) >= 20 ||
					Math.abs(startY - touch.y) >= 20 || touch.y <= 79) {
				return;
			}

			if (btnBack.isTouched(touch.x, touch.y)) {
				SoundManager.play(Assets.click);
				newScreen = new OptionsScreen(game);
				return;
			}

			for (int i = 0; i < buttons.size(); i++) {
				Button button = buttons.get(i);
				if (button.isTouched(touch.x, touch.y) && button.getCommand() != 0) {
					SoundManager.play(Assets.click);
					currentPlugin = plugins.get(i/3);
					if (button.getCommand() == RESULT_NO) {
						popupTextInput(L.string(R.string.plugins_get_removal_reason), "", -1);
						return;
					}
					downloadState = IDownloaderClient.STATE_DOWNLOADING;
					progress = new DownloadProgressInfo(1, 0, -1, 0);
					new PluginManagerImpl(game.getApplicationContext(), game.getFileIO(),
						AliteConfig.ROOT_DRIVE_FOLDER, AliteConfig.GAME_NAME, this).
						downloadFile(currentPlugin.fileId, currentPlugin.size, currentPlugin.folder, currentPlugin.filename);
					return;
				}
			}
			return;
		}

		if (touch.type == TouchEvent.TOUCH_SWEEP && touch.x < AliteConfig.DESKTOP_WIDTH) {
			deltaY = touch.y2;
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(Color.BLACK);
		displayTitle(L.string(R.string.title_plugins));
		btnBack.render(g);

		if (deltaY != 0) {
			boolean neg = deltaY < 0;
			deltaY += deltaY > 0 ? -8.0f * deltaTime : deltaY < 0 ? 8.0f * deltaTime : 0;
			if (neg && deltaY > 0 || !neg && deltaY < 0) {
				deltaY = 0;
			}
			yPosition -= deltaY;
			if (yPosition < 0) {
				yPosition = 0;
			}
			if (yPosition > maxY) {
				yPosition = maxY;
			}
		}

		g.setClip(-1, 130, -1, 1000);
		for (int i = 0; i < buttons.size(); i++) {
			Button button = buttons.get(i).setYOffset(-yPosition);
			String status = plugins.get(i/3).status;

			switch (i % 3) {
				case 0:
					button.render(g);
					g.drawPixmap(getIcon(status), 45, button.getY() - yPosition + 20);
					if (downloadState != 0 && plugins.get(i/3) == currentPlugin) {
						renderProgressCircle();
						break;
					}
					if (isRemovable(status)) {
						Locale.setDefault(L.getInstance().getCurrentLocale());
						String infoText = DateUtils.getRelativeTimeSpanString(plugins.get(i/3).downloadTime).toString();
						g.drawText(infoText, 1415 - g.getTextWidth(infoText, Assets.regularFont),
							button.getY() - yPosition + (int) Assets.titleFont.getSize(),
							ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);
					}

					if (isDownloadable(status)) {
						long size = plugins.get(i/3).size;
						String infoText = size > AliteLog.MB ? L.string(R.string.plugins_size_mb, size / AliteLog.MB) :
							L.string(R.string.plugins_size_kb, size / AliteLog.KB);
						g.drawText(infoText, 1415 - g.getTextWidth(infoText, Assets.regularFont),
							button.getY() - yPosition + 2 * (int) Assets.titleFont.getSize(),
							ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);
					}
					break;

				case 1:
					button.setVisible(isDownloadable(status)).render(g);
					break;

				case 2:
					button.setVisible(isRemovable(status)).render(g);
					break;
			}
		}
		g.setClip(-1, -1, -1, -1);
	}

	private Pixmap getIcon(String status) {
		switch (status) {
			case Plugin.META_STATUS_NEW: return iconNew;
			case Plugin.META_STATUS_DOWNLOADABLE: return iconDownloadable;
			case Plugin.META_STATUS_INSTALLED: return iconInstalled;
			case Plugin.META_STATUS_UPGRADED: return iconUpgraded;
			case Plugin.META_STATUS_OUTDATED: return iconOutdated;
			case Plugin.META_STATUS_NEW_OF_REMOVED: return iconNewOfRemoved;
		}
		return iconRemoved;
	}

	private boolean isDownloadable(String status) {
		switch (status) {
			case Plugin.META_STATUS_NEW:
			case Plugin.META_STATUS_DOWNLOADABLE:
			case Plugin.META_STATUS_OUTDATED:
			case Plugin.META_STATUS_REMOVED:
			case Plugin.META_STATUS_NEW_OF_REMOVED:
				return true;
		}
		return false;
	}

	private boolean isRemovable(String status) {
		switch (status) {
			case Plugin.META_STATUS_INSTALLED:
			case Plugin.META_STATUS_UPGRADED:
			case Plugin.META_STATUS_OUTDATED:
				return true;
		}
		return false;
	}

	@Override
	public void loadAssets() {
		iconDownload = game.getGraphics().newPixmap("download_icon_small.png");
		iconNew = game.getGraphics().newPixmap("ext_new.png");
		iconDownloadable = game.getGraphics().newPixmap("ext_downloadable.png");
		iconInstalled = game.getGraphics().newPixmap("ext_installed.png");
		iconUpgraded = game.getGraphics().newPixmap("ext_upgraded.png");
		iconOutdated = game.getGraphics().newPixmap("ext_outdated.png");
		iconRemoved = game.getGraphics().newPixmap("ext_removed.png");
		iconNewOfRemoved = game.getGraphics().newPixmap("ext_new_of_removed.png");
		super.loadAssets();
	}

	@Override
	public void dispose() {
		pluginModel.pluginsVisited();
		super.dispose();
		if (iconDownload != null) {
			iconDownload.dispose();
			iconDownload = null;
		}
		if (iconNew != null) {
			iconNew.dispose();
			iconNew = null;
		}
		if (iconDownloadable != null) {
			iconDownloadable.dispose();
			iconDownloadable = null;
		}
		if (iconInstalled != null) {
			iconInstalled.dispose();
			iconInstalled = null;
		}
		if (iconUpgraded != null) {
			iconUpgraded.dispose();
			iconUpgraded = null;
		}
		if (iconOutdated != null) {
			iconOutdated.dispose();
			iconOutdated = null;
		}
		if (iconRemoved != null) {
			iconRemoved.dispose();
			iconRemoved = null;
		}
		if (iconNewOfRemoved != null) {
			iconNewOfRemoved.dispose();
			iconNewOfRemoved = null;
		}
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.PLUGINS_SCREEN;
	}

	@Override
	public void onServiceConnected(Messenger m) {

	}

	@Override
	public void onDownloadStateChanged(int newState) {
		downloadState = newState;
	}

	@Override
	public void onDownloadProgress(DownloadProgressInfo progress) {
		this.progress = progress;
	}

	private void renderProgressCircle() {
		int progressInPercent = (int) (progress.mOverallProgress / (float) progress.mOverallTotal * 100.0f);
		Button button = buttons.get(3 * plugins.indexOf(currentPlugin));
		Graphics g = game.getGraphics();
		int r = 60;
		int x = r + (AliteConfig.SCREEN_WIDTH >> 1);
		int y = r + button.getY() - yPosition + 20;
		g.drawCircle(x, y, r, ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), 64);
		GLES11.glLineWidth(3);
		g.drawArc(x, y, r, ColorScheme.get(ColorScheme.COLOR_ADDITIONAL_TEXT), (int)(3.6 * progressInPercent));
		GLES11.glLineWidth(1);
		String text = StringUtil.format("%d%%", progressInPercent);
		g.drawText(text, x - ((int) Assets.regularFont.getWidth(text, 1) >> 1),
			y + ((int) Assets.regularFont.getSize() >> 1) - 10,
			ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);

		g.drawText(L.string(R.string.plugins_download_progress,
			progress.mOverallProgress / AliteLog.MB, progress.mOverallTotal / AliteLog.MB),
			x + r + 40, button.getY() - yPosition + (int) Assets.titleFont.getSize(),
			ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);
		g.drawText(L.string(R.string.plugins_download_time,
			progress.mCurrentSpeed / AliteLog.KB, (int) (progress.mTimeRemaining / 1000.0f)),
			x + r + 40, button.getY() - yPosition + 2 * (int) Assets.titleFont.getSize(),
			ColorScheme.get(ColorScheme.COLOR_BASE_INFORMATION), Assets.regularFont);
	}

}
