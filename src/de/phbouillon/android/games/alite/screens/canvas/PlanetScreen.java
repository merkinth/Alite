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
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.ColorFilterGenerator;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.ScreenCodes;
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

	private final float[] lightAmbient  = { 0.5f, 0.5f, 0.7f, 1.0f };
	private final float[] lightDiffuse  = { 0.4f, 0.4f, 0.8f, 1.0f };
	private final float[] lightSpecular = { 0.5f, 0.5f, 1.0f, 1.0f };
	private final float[] lightPosition = { 100.0f, 30.0f, -10.0f, 1.0f };

	private final float[] sunLightAmbient  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightDiffuse  = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
	private final float[] sunLightPosition = {0.0f, 0.0f, 0.0f, 1.0f};


	private static Pixmap inhabitantBottomLayer;
	private static Pixmap inhabitantTopLayer;
	private Pixmap aig_temp1;
	private Pixmap aig_temp2;

	private TextData[] descriptionTextData;
	private TextData[] inhabitantTextData;
	private SystemData system;
	private int inhabitantGenerationStep = 0;

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

	public PlanetScreen(Alite game) {
		super(game);
	}

	@Override
	public void activate() {
		Player player = game.getPlayer();
		final SystemData system = player.getHyperspaceSystem() == null ? player.getCurrentSystem() : player.getHyperspaceSystem();
		this.system = system;
		inhabitantTextData = computeCenteredTextDisplay(game.getGraphics(), system.getInhabitants(), 20, 800, 400,
			ColorScheme.get(ColorScheme.COLOR_INHABITANT_INFORMATION));
		descriptionTextData = computeTextDisplay(game.getGraphics(), system.getDescription(), 450, 900, 1100, 40,
			ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
		initGl();
		createPlanet(system);
		inhabitantGenerationStep = 0;
	}

	private void initGl() {
		Rect visibleArea = game.getGraphics().getVisibleArea();
		int windowWidth = visibleArea.width();
		int windowHeight = visibleArea.height();

		float ratio = windowWidth / (float) windowHeight;
		GlUtils.setViewport(visibleArea);
		GLES11.glDisable(GLES11.GL_FOG);
		GLES11.glPointSize(1.0f);
        GLES11.glLineWidth(1.0f);

        GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
        GLES11.glDisable(GLES11.GL_BLEND);

		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, ratio, 1.0f, 900000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES11.glShadeModel(GLES11.GL_SMOOTH);

		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_AMBIENT, lightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_DIFFUSE, lightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_SPECULAR, lightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT1, GLES11.GL_POSITION, lightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT1);

		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_AMBIENT, sunLightAmbient, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_DIFFUSE, sunLightDiffuse, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_SPECULAR, sunLightSpecular, 0);
		GLES11.glLightfv(GLES11.GL_LIGHT2, GLES11.GL_POSITION, sunLightPosition, 0);
		GLES11.glEnable(GLES11.GL_LIGHT2);

		GLES11.glEnable(GLES11.GL_LIGHTING);


		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
		GLES11.glHint(GLES11.GL_PERSPECTIVE_CORRECTION_HINT, GLES11.GL_NICEST);
		GLES11.glHint(GLES11.GL_POLYGON_SMOOTH_HINT, GLES11.GL_NICEST);
//		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glEnable(GLES11.GL_CULL_FACE);

	}

	private ColorFilter adjustColor(String inhabitant) {
		if (inhabitant.contains("green")) { return ColorFilterGenerator.adjustColor(         100,   82); }
		if (inhabitant.contains("blue")) { return ColorFilterGenerator.adjustColor(         100, -148); }
		if (inhabitant.contains("red")) { return ColorFilterGenerator.adjustColor(         100,  -18); }
		if (inhabitant.contains("yellow")) { return ColorFilterGenerator.adjustColor(         100,   45); }
		if (inhabitant.contains("white")) { return ColorFilterGenerator.adjustColor(100, 0, -100,    0); }
		return null;
	}

	private String getType(String inhabitant) {
		if (inhabitant.contains("bony"))     { return "boned_";    }
		if (inhabitant.contains("bug-eyed")) { return "bug_eyed_"; }
		if (inhabitant.contains("fat"))      { return "fat_";      }
		if (inhabitant.contains("furry"))    { return "furry_";    }
		if (inhabitant.contains("horned"))   { return "horned_";   }
		if (inhabitant.contains("mutant"))   { return "mutant_";   }
		if (inhabitant.contains("slimy"))    { return "slimy_";    }
		if (inhabitant.contains("weird"))    { return "weird_";    }
		return "";
	}

	private String getRace(String inhabitant) {
		if (inhabitant.contains("bird"))     { return "bird/"; }
		if (inhabitant.contains("feline"))   { return "feline/"; }
		if (inhabitant.contains("frog"))     { return "frog/"; }
		if (inhabitant.contains("humanoid")) { return "humanoid/"; }
		if (inhabitant.contains("insect"))   { return "insect/"; }
		if (inhabitant.contains("lizard"))   { return "lizard/"; }
		if (inhabitant.contains("lobster"))  { return "lobster/"; }
		if (inhabitant.contains("rodent"))   { return "rodent/"; }
		return "";
	}

	private void computeAlienImage(final Graphics g, SystemData system) {
		String inhabitant = system.getInhabitants().toLowerCase(Locale.getDefault());
		String basePath = "alien_icons/" + getRace(inhabitant) + getType(inhabitant);
		if (system.getIndex() == 256 || g.existsAssetsFile(basePath + "base.png")) {
			if (inhabitantGenerationStep == 0) {
				if (inhabitantBottomLayer == null) {
					if (system.getIndex() == 256) {
						aig_temp1 = g.newPixmap("alien_icons/treeard.png");
					} else {
						aig_temp1 = g.newPixmap(basePath + "base.png");
					}
					g.applyFilterToPixmap(aig_temp1, adjustColor(inhabitant));
				}
			} else if (inhabitantGenerationStep == 1) {
				if (inhabitantTopLayer == null) {
					if (system.getIndex() == 256) {
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

	private Pixmap load(final Graphics g, String section, String gender, int type, int layer) {
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

	private void composePart(final Graphics g, final Pixmap pixmap, final ColorFilter filter, final Canvas composer, final Paint paint) {
		if (pixmap == null) {
			return;
		}
		if (filter != null) {
			g.applyFilterToPixmap(pixmap, filter);
		}
		composer.drawBitmap(pixmap.getBitmap(), 0, 0, paint);
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

	private void computeHumanImage(final Graphics g) {
		switch (inhabitantGenerationStep) {
		    case 1: initComputeHumanImage(); break;
			case 2: composePart(g, load(g, "hair",     hig_subDir, hig_hairType    + 1, 1), hig_hairModifier, hig_composer, hig_paint); break;
			case 3: composePart(g, load(g, "bodies",   hig_subDir, hig_bodyType    + 1, 1), hig_skinModifier, hig_composer, hig_paint); break;
			case 4: composePart(g, load(g, "bodies",   hig_subDir, hig_bodyType    + 1, 2), null, hig_composer, hig_paint); break;
			case 5: composePart(g, load(g, "heads",    hig_subDir, hig_faceType    + 1, 1), hig_skinModifier, hig_composer, hig_paint); break;
			case 6: composePart(g, load(g, "eyes",     hig_subDir, hig_eyeType     + 1, 1), null, hig_composer, hig_paint); break;
			case 7: composePart(g, load(g, "eyes",     hig_subDir, hig_eyeType     + 1, 2), hig_eyeModifier, hig_composer, hig_paint); break;
			case 8: composePart(g, load(g, "ears",     null,   hig_earType     + 1, 1), hig_skinModifier, hig_composer, hig_paint); break;
			case 9: composePart(g, load(g, "noses",    hig_subDir, hig_noseType    + 1, 1), hig_skinModifier, hig_composer, hig_paint); break;
			case 10: composePart(g, load(g, "mouths",   hig_subDir, hig_mouthType   + 1, 1), null, hig_composer, hig_paint); break;
			case 11: composePart(g, load(g, "mouths",   hig_subDir, hig_mouthType   + 1, 2), hig_lipModifier, hig_composer, hig_paint); break;
			case 12: composePart(g, load(g, "eyebrows", hig_subDir, hig_eyebrowType + 1, 1), hig_hairModifier, hig_composer, hig_paint); break;
			case 13: composePart(g, load(g, "hair",     hig_subDir, hig_hairType    + 1, 2), hig_hairModifier, hig_composer, hig_paint); break;
		}
		inhabitantGenerationStep++;
		if (inhabitantGenerationStep == 14) {
			inhabitantBottomLayer = g.newPixmap(hig_humanImage, "humanImage");
			inhabitantGenerationStep = -1;
		}
	}

	private void displayInhabitants(final Graphics g) {
		g.drawPixmap(background, 20, 100);
		if (inhabitantBottomLayer == null && inhabitantTopLayer == null) {
			centerText("Accessing", 20, 400, 350, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
			centerText("Database...", 20, 400, 390, Assets.regularFont, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT));
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

	private void displayInformation(final Graphics g, SystemData system) {
		g.drawText("Economy:",          450, 150, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText("Government:",       450, 190, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText("Technical Level:",  450, 230, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText("Population:",       450, 270, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);

		g.drawText(system.getEconomy().getDescription(),    800, 150, ColorScheme.get(ColorScheme.COLOR_ECONOMY), Assets.regularFont);
		g.drawText(system.getGovernment().getDescription(), 800, 190, ColorScheme.get(ColorScheme.COLOR_GOVERNMENT), Assets.regularFont);
		g.drawText("" + system.getTechLevel(),         800, 230, ColorScheme.get(ColorScheme.COLOR_TECH_LEVEL),  Assets.regularFont);
		g.drawText(system.getPopulation(),                  800, 270, ColorScheme.get(ColorScheme.COLOR_POPULATION), Assets.regularFont);

		g.drawPixmap(cobraRight, 450, 320);
		g.drawArrow(720, 560, 1000, 560, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.RIGHT);

		int halfWidth = g.getTextWidth("Light Years", Assets.regularFont) >> 1;
		g.drawText("Light Years", 860 - halfWidth, 605, ColorScheme.get(ColorScheme.COLOR_SHIP_DISTANCE), Assets.regularFont);
		int dist = computeDistance();
		String distString = String.format(Locale.getDefault(), "%d.%d", dist / 10, dist % 10);
		halfWidth = g.getTextWidth(distString, Assets.regularFont) >> 1;
		g.drawText(distString, 860 - halfWidth, 545, ColorScheme.get(ColorScheme.COLOR_SHIP_DISTANCE), Assets.regularFont);

		g.drawArrow(1090, 270, 1210, 270, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.LEFT);
		g.drawArrow(1630, 270, 1510, 270, ColorScheme.get(ColorScheme.COLOR_ARROW), Graphics.ArrowDirection.RIGHT);
		String diameter = system.getDiameter();
		halfWidth = g.getTextWidth(diameter, Assets.regularFont) >> 1;
		g.drawText(diameter, 1370 - halfWidth, 280, ColorScheme.get(ColorScheme.COLOR_DIAMETER), Assets.regularFont);

		g.drawText("GNP:", 450, 840, ColorScheme.get(ColorScheme.COLOR_INFORMATION_TEXT), Assets.regularFont);
		g.drawText(system.getGnp(), 800, 840, ColorScheme.get(ColorScheme.COLOR_GNP), Assets.regularFont);

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
		if (inhabitantGenerationStep >= 0) {
			if (system.getInhabitantCode() == null) {
				if (inhabitantGenerationStep == 0) {
					aig_temp1 = null;
					aig_temp2 = null;
				}
				computeAlienImage(game.getGraphics(), system);
			} else {
				computeHumanImage(game.getGraphics());
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		Player player = game.getPlayer();
		SystemData system = player.getHyperspaceSystem() == null ? player.getCurrentSystem() : player.getHyperspaceSystem();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		displayTitle("Data on " + system.getName());

		afterDisplay();

		displayInhabitants(g);
		displayInformation(g, system);
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
		if (inhabitantTopLayer != null) {
			inhabitantTopLayer.dispose();
			inhabitantTopLayer = null;
		}
		if (inhabitantBottomLayer != null) {
			inhabitantBottomLayer.dispose();
			inhabitantBottomLayer = null;
		}
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

	private void createPlanet(final SystemData system) {
		planet = new PlanetSpaceObject(game, system, true);
		planet.setPosition(PLANET_POSITION);
		planet.applyDeltaRotation(16, 35, 8);
	}

	private void afterDisplay() {
		Rect visibleArea = game.getGraphics().getVisibleArea();
		float aspectRatio = visibleArea.width() / (float) visibleArea.height();
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
		GLES11.glMatrixMode(GLES11.GL_PROJECTION);
		GLES11.glLoadIdentity();
		GlUtils.gluPerspective(game, 45.0f, aspectRatio, 20000.0f, 1000000.0f);
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();

		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);

		GLES11.glEnable(GLES11.GL_DEPTH_TEST);
		GLES11.glDepthFunc(GLES11.GL_LESS);
		GLES11.glClear(GLES11.GL_DEPTH_BUFFER_BIT);
		GLES11.glPushMatrix();
		GLES11.glMultMatrixf(planet.getMatrix(), 0);
		planet.render();
		GLES11.glPopMatrix();

		GLES11.glDisable(GLES11.GL_DEPTH_TEST);
		GLES11.glDisable(GLES11.GL_TEXTURE_2D);
		setUpForDisplay(visibleArea);

	}

	@Override
	public int getScreenCode() {
		return ScreenCodes.PLANET_SCREEN;
	}

	public static boolean initialize(Alite alite, final DataInputStream dis) {
		if (inhabitantBottomLayer != null) {
			inhabitantBottomLayer.dispose();
		}
		if (inhabitantTopLayer != null) {
			inhabitantTopLayer.dispose();
		}
		inhabitantBottomLayer = null;
		inhabitantTopLayer = null;
		alite.setScreen(new PlanetScreen(alite));
		return true;
	}
}
