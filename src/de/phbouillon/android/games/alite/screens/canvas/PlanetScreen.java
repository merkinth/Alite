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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.ColorFilterGenerator;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.screens.opengl.objects.PlanetSpaceObject;

//This screen never needs to be serialized, as it is not part of the InGame state.
public class PlanetScreen extends AliteScreen {
	private static final Vector3f PLANET_POSITION = new Vector3f(15000, -1000, -50000);

	private static Pixmap cobraRight;
	private static Pixmap background;
	private PlanetSpaceObject planet;


	private static Pixmap inhabitantBottomLayer;
	private static Pixmap inhabitantTopLayer;
	private Pixmap aig_temp1;
	private Pixmap aig_temp2;

	private TextData[] descriptionTextData;
	private TextData[] inhabitantTextData;
	private SystemData system;
	private int inhabitantGenerationStep;

	private String hig_subDir;
	private int hig_bodyType;
	private int hig_faceType;
	private int hig_hairType;
	private int hig_eyeType;
	private int hig_eyebrowType;
	private int hig_noseType;
	private int hig_earType;
	private int hig_mouthType;
	private Bitmap hig_humanImage;
	private Canvas hig_composer;
	private Paint hig_paint;
	private ColorFilter hig_skinModifier;
	private ColorFilter hig_hairModifier;
	private ColorFilter hig_lipModifier;
	private ColorFilter hig_eyeModifier;

	@Override
	public void activate() {
		Player player = game.getPlayer();
		system = player.getHyperspaceSystem() == null ? player.getCurrentSystem() : player.getHyperspaceSystem();
		if (system != null) {
			inhabitantTextData = computeCenteredTextDisplay(game.getGraphics(), system.getInhabitants(), 20, 800, 400,
				ColorScheme.get(ColorScheme.COLOR_INHABITANT_INFORMATION));
			descriptionTextData = computeTextDisplay(game.getGraphics(), system.getDescription(), 450, 900, 1100, 40,
				ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		} else {
			SoundManager.play(Assets.error);
		}
		initGl();
		createPlanet();
		inhabitantGenerationStep = 0;
	}

	private boolean containsInhabitant(int resId, String inhabitant) {
		return inhabitant.contains(L.string(resId).toLowerCase() + " ");
	}

	private ColorFilter adjustColor(String inhabitant) {
		if (containsInhabitant(R.string.inhabitant_color_green, inhabitant)) {
			return ColorFilterGenerator.adjustColor(100, 82);
		}
		if (containsInhabitant(R.string.inhabitant_color_blue, inhabitant)) {
			return ColorFilterGenerator.adjustColor(100, -148);
		}
		if (containsInhabitant(R.string.inhabitant_color_red, inhabitant)) {
			return ColorFilterGenerator.adjustColor(100, -18);
		}
		if (containsInhabitant(R.string.inhabitant_color_yellow, inhabitant)) {
			return ColorFilterGenerator.adjustColor(100, 45);
		}
		if (containsInhabitant(R.string.inhabitant_color_white, inhabitant)) {
			return ColorFilterGenerator.adjustColor(100, 0, -100,    0);
		}
		return null;
	}

	private String getType(String inhabitant) {
		if (containsInhabitant(R.string.inhabitant_appearance_bony, inhabitant)) { return "boned_";    }
		if (containsInhabitant(R.string.inhabitant_appearance_bug_eyed, inhabitant)) { return "bug_eyed_"; }
		if (containsInhabitant(R.string.inhabitant_appearance_fat, inhabitant))      { return "fat_";      }
		if (containsInhabitant(R.string.inhabitant_appearance_furry, inhabitant))    { return "furry_";    }
		if (containsInhabitant(R.string.inhabitant_appearance_horned, inhabitant))   { return "horned_";   }
		if (containsInhabitant(R.string.inhabitant_appearance_mutant, inhabitant))   { return "mutant_";   }
		if (containsInhabitant(R.string.inhabitant_appearance_slimy, inhabitant))    { return "slimy_";    }
		if (containsInhabitant(R.string.inhabitant_appearance_weird, inhabitant))    { return "weird_";    }
		return "";
	}

	private String getRace(String inhabitant) {
		if (containsInhabitant(R.string.inhabitant_type_bird, inhabitant))     { return "bird/"; }
		if (containsInhabitant(R.string.inhabitant_type_feline, inhabitant))   { return "feline/"; }
		if (containsInhabitant(R.string.inhabitant_type_frog, inhabitant))     { return "frog/"; }
		if (containsInhabitant(R.string.inhabitant_type_humanoid, inhabitant)) { return "humanoid/"; }
		if (containsInhabitant(R.string.inhabitant_type_insect, inhabitant))   { return "insect/"; }
		if (containsInhabitant(R.string.inhabitant_type_lizard, inhabitant))   { return "lizard/"; }
		if (containsInhabitant(R.string.inhabitant_type_lobster, inhabitant))  { return "lobster/"; }
		if (containsInhabitant(R.string.inhabitant_type_rodent, inhabitant))   { return "rodent/"; }
		return "";
	}

	private void computeAlienImage() {
		Graphics g = game.getGraphics();
		String inhabitant = system.getInhabitants().toLowerCase() + " ";
		String basePath = "alien_icons/" + getRace(inhabitant) + getType(inhabitant);
		if (system == SystemData.RAXXLA_SYSTEM || g.existsAssetsFile(basePath + "base.png")) {
			if (inhabitantGenerationStep == 0) {
				if (inhabitantBottomLayer == null) {
					if (system == SystemData.RAXXLA_SYSTEM) {
						aig_temp1 = g.newPixmap("alien_icons/treeard.png");
					} else {
						aig_temp1 = g.newPixmap(basePath + "base.png");
					}
					g.applyFilterToPixmap(aig_temp1, adjustColor(inhabitant));
				}
			} else if (inhabitantGenerationStep == 1) {
				if (inhabitantTopLayer == null) {
					if (system == SystemData.RAXXLA_SYSTEM) {
						aig_temp2 = g.newPixmap("alien_icons/treeard.png");
					} else {
						aig_temp2 = g.newPixmap(basePath + "details.png");
					}
				}
			}
		}
		if (inhabitantGenerationStep == 2) {
			inhabitantBottomLayer = aig_temp1;
			inhabitantTopLayer = aig_temp2;
			inhabitantGenerationStep = -1;
		}
		if (inhabitantGenerationStep >= 0) {
			inhabitantGenerationStep++;
		}
	}

	private Pixmap load(String section, String gender, int type, int layer) {
		Graphics g = game.getGraphics();
		String fileName = "alien_icons/human/" + section + (gender == null ? "" : "/" + gender) + "/" + type + "_" + layer + ".png";
		if (g.existsAssetsFile(fileName)) {
			return g.newPixmap(fileName);
		}
		return null;
	}

	private ColorFilter adjustSkinColor(int skinColorType) {
		switch (skinColorType) {
			case  0: return null;                  //           B    C     S     H
			case  1: return ColorFilterGenerator.adjustColor(  48,   0,    0,    0);
			case  2: return ColorFilterGenerator.adjustColor(  24,   0,    0,    0);
			case  3: return ColorFilterGenerator.adjustColor(             28,   18);
			case  4: return ColorFilterGenerator.adjustColor( -32,   0,   28,   18);
			case  5: return ColorFilterGenerator.adjustColor( -64,   0,   28,   18);
			case  6: return ColorFilterGenerator.adjustColor(                  -18);
			case  7: return ColorFilterGenerator.adjustColor(  48,   0,    0,  -18);
			case  8: return ColorFilterGenerator.adjustColor(  24,   0,    0,  -18);
			case  9: return ColorFilterGenerator.adjustColor( -24,   0,    0,  -18);
			case 10: return ColorFilterGenerator.adjustColor( -48,   0,    0,  -18);
			case 11: return ColorFilterGenerator.adjustColor( -72,   0,    0,  -18);
			case 12: return ColorFilterGenerator.adjustColor( -96,   0,    0,  -18);
			case 13: return ColorFilterGenerator.adjustColor(       40,  -38,  -27);
			case 14: return ColorFilterGenerator.adjustColor(  48,  40,  -38,  -27);
			case 15: return ColorFilterGenerator.adjustColor(  24,  40,  -38,  -27);
			case 16: return ColorFilterGenerator.adjustColor( -24,  40,  -38,  -27);
			case 17: return ColorFilterGenerator.adjustColor( -48,  40,  -38,  -27);
			case 18: return ColorFilterGenerator.adjustColor( -72,  40,  -38,  -27);
			case 19: return ColorFilterGenerator.adjustColor( -96,  40,  -38,  -27);
			case 20: return ColorFilterGenerator.adjustColor( -24,   0,    0,    0);
			case 21: return ColorFilterGenerator.adjustColor( -48,   0,    0,    0);
			case 22: return ColorFilterGenerator.adjustColor( -72,   0,    0,    0);
			case 23: return ColorFilterGenerator.adjustColor( -96,   0,    0,    0);
			case 24: return ColorFilterGenerator.adjustColor(  48,   0,   40,    0);
			case 25: return ColorFilterGenerator.adjustColor(  24,   0,   40,    0);
			case 26: return ColorFilterGenerator.adjustColor( -24,   0,   40,    0);
			case 27: return ColorFilterGenerator.adjustColor( -48,   0,   40,    0);
			case 28: return ColorFilterGenerator.adjustColor( -72,   0,   40,    0);
			case 29: return ColorFilterGenerator.adjustColor( -96,   0,   40,    0);
			case 30: return ColorFilterGenerator.adjustColor( -30,  20,   80,  130);
			case 31: return ColorFilterGenerator.adjustColor( -72,  20,   80, -130);
			default: return null;
		}
	}

	private ColorFilter adjustEyeColor(int eyeColorType) {
		switch (eyeColorType) {
			case  0: return null;
			case  1: return ColorFilterGenerator.adjustColor(                    8);
			case  2: return ColorFilterGenerator.adjustColor(                   16);
			case  3: return ColorFilterGenerator.adjustColor(                   24);
			case  4: return ColorFilterGenerator.adjustColor(                   32);
			case  5: return ColorFilterGenerator.adjustColor(                   40);
			case  6: return ColorFilterGenerator.adjustColor(                   48);
			case  7: return ColorFilterGenerator.adjustColor(                   56);
			case  8: return ColorFilterGenerator.adjustColor(                   64);
			case  9: return ColorFilterGenerator.adjustColor(                   72);
			case 10: return ColorFilterGenerator.adjustColor(                   80);
			case 11: return ColorFilterGenerator.adjustColor(                   88);
			case 12: return ColorFilterGenerator.adjustColor(                   96);
			case 13: return ColorFilterGenerator.adjustColor(                   -8);
			case 14: return ColorFilterGenerator.adjustColor(                  -16);
			case 15: return ColorFilterGenerator.adjustColor(                  -24);
			case 16: return ColorFilterGenerator.adjustColor(                  -32);
			case 17: return ColorFilterGenerator.adjustColor(                  -40);
			case 18: return ColorFilterGenerator.adjustColor(                  -48);
			case 19: return ColorFilterGenerator.adjustColor(                  -56);
			case 20: return ColorFilterGenerator.adjustColor(                  -64);
			case 21: return ColorFilterGenerator.adjustColor(                  -72);
			case 22: return ColorFilterGenerator.adjustColor(                  -80);
			case 23: return ColorFilterGenerator.adjustColor(                  -88);
			case 24: return ColorFilterGenerator.adjustColor(                  -96);
			case 25: return ColorFilterGenerator.adjustColor(            100,    0);
			case 26: return ColorFilterGenerator.adjustColor(            100,  -16);
			case 27: return ColorFilterGenerator.adjustColor(            100,  -32);
			case 28: return ColorFilterGenerator.adjustColor(            100,  -48);
			case 29: return ColorFilterGenerator.adjustColor(            100,   16);
			case 30: return ColorFilterGenerator.adjustColor(            100,   32);
			case 31: return ColorFilterGenerator.adjustColor(            100,   48);
			default: return null;
		}
	}

	private ColorFilter adjustLipColor(int lipColorType) {
		if (lipColorType == 0) {
			return ColorFilterGenerator.adjustColor(100, 82);
		}
		return ColorFilterGenerator.adjustColor(0);
	}

	private ColorFilter adjustHairColor(int hairColorType) {
		switch (hairColorType) {
			case  0: return null;
			case  1: return ColorFilterGenerator.adjustColor(             67,   26);
			case  2: return ColorFilterGenerator.adjustColor(                  108);
			case  3: return ColorFilterGenerator.adjustColor(                  180);
			case  4: return ColorFilterGenerator.adjustColor(                  -84);
			case  5: return ColorFilterGenerator.adjustColor(                 -140);
			case  6: return ColorFilterGenerator.adjustColor(  70,   0,  100,    0);
			case  7: return ColorFilterGenerator.adjustColor(  70,   0,  100,  -20);
			case  8: return ColorFilterGenerator.adjustColor(  70,   0,  100,  -58);
			case  9: return ColorFilterGenerator.adjustColor(  70,   0,  100, -133);
			case 10: return ColorFilterGenerator.adjustColor(  70,   0,  100,   26);
			case 11: return ColorFilterGenerator.adjustColor(  70,   0,  100,   30);
			case 12: return ColorFilterGenerator.adjustColor(  70,   0,  100,   42);
			case 13: return ColorFilterGenerator.adjustColor(  70,   0,  100,   96);
			case 14: return ColorFilterGenerator.adjustColor( -50,   0, -100,    0);
			case 15: return ColorFilterGenerator.adjustColor( 100,   0, -100,    0);
			case 16: return ColorFilterGenerator.adjustColor(  80,   0, -100,    0);
			case 17: return ColorFilterGenerator.adjustColor(  40,   0, -100,    0);
			case 18: return ColorFilterGenerator.adjustColor(  64,   0,    0,  -96);
			case 19: return ColorFilterGenerator.adjustColor(  64,   0,    0,  -72);
			case 20: return ColorFilterGenerator.adjustColor(  64,   0,    0,  -48);
			case 21: return ColorFilterGenerator.adjustColor(  64,   0,    0,  -24);
			case 22: return ColorFilterGenerator.adjustColor(  64,   0,    0,   24);
			case 23: return ColorFilterGenerator.adjustColor(  64,   0,    0,   48);
			case 24: return ColorFilterGenerator.adjustColor(  64,   0,    0,   72);
			case 25: return ColorFilterGenerator.adjustColor(  64,   0,    0,   96);
			case 26: return ColorFilterGenerator.adjustColor(  64,   0,    0, -120);
			case 27: return ColorFilterGenerator.adjustColor(  64,   0,    0, -144);
			case 28: return ColorFilterGenerator.adjustColor(  64,   0,    0, -168);
			case 29: return ColorFilterGenerator.adjustColor(  64,   0,    0,  120);
			case 30: return ColorFilterGenerator.adjustColor(  64,   0,    0,  144);
			case 31: return ColorFilterGenerator.adjustColor(  64,   0,    0,  168);
			default: return null;
		}
	}

	private void composePart(final Pixmap pixmap, final ColorFilter filter) {
		if (pixmap == null) {
			return;
		}
		if (filter != null) {
			game.getGraphics().applyFilterToPixmap(pixmap, filter);
		}
		hig_composer.drawBitmap(pixmap.getBitmap(), 0, 0, hig_paint);
		pixmap.dispose();
	}

	private void initComputeHumanImage() {
		String inhabitantCode = system.getInhabitantCode();

		hig_subDir        = Integer.parseInt(inhabitantCode.substring(31, 32), 2) == 1 ? "m" : "f";
		hig_bodyType      = Integer.parseInt(inhabitantCode.substring(28, 31), 2);
		hig_faceType      = Integer.parseInt(inhabitantCode.substring(26, 28), 2);
		hig_hairType      = Integer.parseInt(inhabitantCode.substring(25, 29), 2);
		hig_eyeType       = Integer.parseInt(inhabitantCode.substring(19, 22), 2);
		hig_eyebrowType   = Integer.parseInt(inhabitantCode.substring(15, 19), 2);
		hig_noseType      = Integer.parseInt(inhabitantCode.substring(11, 15), 2);
		hig_earType       = Integer.parseInt(inhabitantCode.substring( 7, 11), 2);
		hig_mouthType     = Integer.parseInt(inhabitantCode.substring( 5,  8), 2);
		int hig_skinColorType = Integer.parseInt(inhabitantCode.substring(16, 21), 2);
		int hig_eyeColorType = Integer.parseInt(inhabitantCode.substring(11, 16), 2);
		int hig_lipColorType = Integer.parseInt(inhabitantCode.substring(6, 11), 2);
		int hig_hairColorType = Integer.parseInt(inhabitantCode.substring(8, 13), 2);

		if (background == null) {
			return;
		}
		Bitmap hig_bg = background.getBitmap();
		hig_humanImage = hig_bg.copy(hig_bg.getConfig(), true);
		hig_composer = new Canvas(hig_humanImage);
		hig_paint = new Paint();

		hig_skinModifier = adjustSkinColor(hig_skinColorType);
		hig_hairModifier = adjustHairColor(hig_hairColorType);
		hig_lipModifier  = adjustLipColor(hig_lipColorType);
		hig_eyeModifier  = adjustEyeColor(hig_eyeColorType);
	}

	private void computeHumanImage() {
		switch (inhabitantGenerationStep) {
		    case 1: initComputeHumanImage(); break;
			case 2: composePart(load("hair",     hig_subDir, hig_hairType    + 1, 1), hig_hairModifier); break;
			case 3: composePart(load("bodies",   hig_subDir, hig_bodyType    + 1, 1), hig_skinModifier); break;
			case 4: composePart(load("bodies",   hig_subDir, hig_bodyType    + 1, 2), null); break;
			case 5: composePart(load("heads",    hig_subDir, hig_faceType    + 1, 1), hig_skinModifier); break;
			case 6: composePart(load("eyes",     hig_subDir, hig_eyeType     + 1, 1), null); break;
			case 7: composePart(load("eyes",     hig_subDir, hig_eyeType     + 1, 2), hig_eyeModifier); break;
			case 8: composePart(load("ears",     null,   hig_earType     + 1, 1), hig_skinModifier); break;
			case 9: composePart(load("noses",    hig_subDir, hig_noseType    + 1, 1), hig_skinModifier); break;
			case 10: composePart(load("mouths",   hig_subDir, hig_mouthType   + 1, 1), null); break;
			case 11: composePart(load("mouths",   hig_subDir, hig_mouthType   + 1, 2), hig_lipModifier); break;
			case 12: composePart(load("eyebrows", hig_subDir, hig_eyebrowType + 1, 1), hig_hairModifier); break;
			case 13: composePart(load("hair",     hig_subDir, hig_hairType    + 1, 2), hig_hairModifier); break;
		}
		inhabitantGenerationStep++;
		if (inhabitantGenerationStep == 14) {
			inhabitantBottomLayer = game.getGraphics().newPixmap(hig_humanImage, "humanImage");
			inhabitantGenerationStep = -1;
		}
	}

	private void displayInhabitants() {
		Graphics g = game.getGraphics();
		g.drawPixmap(background, 20, 100);
		if (inhabitantBottomLayer == null && inhabitantTopLayer == null) {
			centerText(L.string(R.string.planet_inhabitant_db_load1), 20, 400, 350, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
			centerText(L.string(R.string.planet_inhabitant_db_load2), 20, 400, 390, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		} else {
			if (inhabitantBottomLayer != null) {
				g.drawPixmap(inhabitantBottomLayer, 20, 100);
			}
			if (inhabitantTopLayer != null) {
				g.drawPixmap(inhabitantTopLayer, 20, 100);
			}
		}
		g.rec3d(20, 100, 400, 650, 5, ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT),
			ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK));
		displayText(g, inhabitantTextData);
	}

	private int computeDistance() {
		SystemData currentSystem = game.getPlayer().getCurrentSystem();
		int dx = (currentSystem == null ? game.getPlayer().getPosition().x : currentSystem.getX()) - system.getX();
		int dy = (currentSystem == null ? game.getPlayer().getPosition().y : currentSystem.getY()) - system.getY();
		return (int) Math.sqrt(dx * dx + dy * dy) << 2;
	}

	private void displayInformation() {
		Graphics g = game.getGraphics();
		g.drawText(L.string(R.string.planet_economy),          450, 150, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.planet_government),       450, 190, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.planet_technical_level),  450, 230, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(L.string(R.string.planet_population),       450, 270, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);

		g.drawText(system == null ? "?" : system.getEconomy().getDescription(),    800, 150, ColorScheme.get(ColorScheme.COLOR_ECONOMY), Assets.regularFont);
		g.drawText(system == null ? "?" : system.getGovernment().getDescription(), 800, 190, ColorScheme.get(ColorScheme.COLOR_GOVERNMENT), Assets.regularFont);
		g.drawText(system == null ? "?" : "" + system.getTechLevel(),         800, 230, ColorScheme.get(ColorScheme.COLOR_TECH_LEVEL),  Assets.regularFont);
		g.drawText(system == null ? L.string(R.string.planet_population_unknown) : system.getPopulation(), 800, 270, ColorScheme.get(ColorScheme.COLOR_POPULATION), Assets.regularFont);

		g.drawPixmap(cobraRight, 450, 320);
		g.drawArrow(720, 560, 1000, 560, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.RIGHT);

		int halfWidth = g.getTextWidth(L.string(R.string.planet_distance), Assets.regularFont) >> 1;
		g.drawText(L.string(R.string.planet_distance), 860 - halfWidth, 605, ColorScheme.get(ColorScheme.COLOR_SHIP_DISTANCE), Assets.regularFont);
		String distString = system == null ? "?" : L.getOneDecimalFormatString(R.string.cash_amount_only, computeDistance());
		halfWidth = g.getTextWidth(distString, Assets.regularFont) >> 1;
		g.drawText(distString, 860 - halfWidth, 545, ColorScheme.get(ColorScheme.COLOR_SHIP_DISTANCE), Assets.regularFont);

		g.drawArrow(1090, 270, 1210, 270, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.LEFT);
		g.drawArrow(1630, 270, 1510, 270, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.RIGHT);
		String diameter = system == null ? L.string(R.string.planet_diameter_unknown) : system.getDiameter();
		halfWidth = g.getTextWidth(diameter, Assets.regularFont) >> 1;
		g.drawText(diameter, 1370 - halfWidth, 280, ColorScheme.get(ColorScheme.COLOR_DIAMETER), Assets.regularFont);

		g.drawText(L.string(R.string.planet_gnp), 450, 840, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(system == null ? L.string(R.string.planet_gnp_unknown) : system.getGnp(), 800, 840, ColorScheme.get(ColorScheme.COLOR_GNP), Assets.regularFont);

		displayText(g, descriptionTextData);
	}

	@Override
	public void update(float deltaTime) {
		if (isDisposed()) {
			return;
		}
		super.update(deltaTime);
		if (planet != null) {
			planet.applyDeltaRotation(0, deltaTime * 5.0f, 0);
		}
		if (inhabitantGenerationStep >= 0 && system != null) {
			if (system.getInhabitantCode() == null) {
				if (inhabitantGenerationStep == 0) {
					aig_temp1 = null;
					aig_temp2 = null;
				}
				computeAlienImage();
			} else {
				computeHumanImage();
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle(L.string(R.string.title_planet, system != null ? system.getName() : L.string(R.string.galaxy_unknown)));

		displayObject(planet, 20000.0f, 1000000.0f);

		displayInhabitants();
		displayInformation();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (cobraRight != null) {
			cobraRight.dispose();
			cobraRight = null;
		}
		if (background != null) {
			background.dispose();
			background = null;
		}
		disposeInhabitantLayers();
		if (planet != null) {
			planet.dispose();
			planet = null;
		}
	}

	@Override
	public void loadAssets() {
		Graphics g = game.getGraphics();

		if (cobraRight != null) {
			cobraRight.dispose();
		}
		cobraRight = g.newPixmap("cobra_right.png");
		if (background != null) {
			background.dispose();
		}
		background = g.newPixmap("metal2_2i.png");
		super.loadAssets();
	}

	private void createPlanet() {
		planet = new PlanetSpaceObject(system, true);
		planet.setPosition(PLANET_POSITION);
		planet.applyDeltaRotation(16, 35, 8);
	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.PLANET_SCREEN;
	}

	public static void disposeInhabitantLayers() {
		if (inhabitantBottomLayer != null) {
			inhabitantBottomLayer.dispose();
			inhabitantBottomLayer = null;
		}
		if (inhabitantTopLayer != null) {
			inhabitantTopLayer.dispose();
			inhabitantTopLayer = null;
		}
	}
}
