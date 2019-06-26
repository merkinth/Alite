package de.phbouillon.android.games.alite.screens.opengl.objects.space.ships;

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

/**
 * Jabberwocky model and texture used from Oolite.
 * Renamed to Dugite for Alite.
 */

import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.statistics.ShipType;
import de.phbouillon.android.games.alite.screens.opengl.ingame.EngineExhaust;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;

public class Dugite extends SpaceObject {
	public static final Vector3f HUD_COLOR = new Vector3f(0.55f, 0.67f, 0.94f);

	private static final float [] VERTEX_DATA = new float [] {
		113.79f, -22.13f,  87.32f, -113.79f, -22.02f,  87.32f,
		-160.00f, -34.41f, -28.19f, -113.79f, -22.02f, -144.14f,
		113.79f, -22.13f, -144.14f, 160.00f, -34.51f, -28.19f,
		56.12f,  -0.37f,  25.98f, -56.12f,  -0.26f,  25.98f,
		-77.79f,  -6.07f, -28.19f,  -1.27f,  19.42f, -189.43f,
		77.79f,  -6.17f, -28.19f,  23.71f,   2.01f, -27.99f,
		-23.71f,   2.11f, -27.99f, -35.37f,  -1.01f, -56.92f,
		-1.27f, -17.92f, -127.88f,  35.37f,  -1.11f, -56.92f,
		77.50f, -18.42f,  43.49f, -77.50f, -18.32f,  43.49f,
		-109.31f, -26.84f, -36.00f, 109.31f, -26.95f, -36.00f,
		32.65f,  -0.39f, 205.64f, -32.65f,  -0.28f, 205.64f,
		17.82f,  19.11f,  46.51f,   8.93f,   3.71f,  46.51f,
		-8.85f,   3.71f,  46.51f, -17.74f,  19.11f,  46.51f,
		-8.85f,  34.51f,  46.51f,   8.93f,  34.51f,  46.51f,
		12.19f,  19.11f, -36.94f, -12.12f,  19.11f, -36.94f,
		0.04f,  -8.14f, -67.91f,   0.04f,  27.20f, -69.01f,
		40.80f,   7.03f, -95.33f, -40.80f,   7.14f, -95.34f,
		-54.11f, -15.66f, -101.43f,  54.11f, -15.76f, -101.43f,
		26.81f,  11.71f, -198.55f,  26.81f, -16.21f, -197.39f,
		-27.26f, -16.08f, -197.39f, -27.26f,  11.84f, -198.55f,
		0.04f, -28.13f, -189.88f,  19.47f, -13.26f, -205.64f,
		0.04f, -24.48f, -205.64f, -19.39f, -13.26f, -205.64f,
		-0.88f,  18.19f, -205.64f,  19.75f,   5.57f, -205.64f,
		-19.38f,   5.57f, -205.64f,  -0.42f,  -4.34f, -151.14f,
		8.93f,  19.11f,  72.71f,   4.48f,  11.41f,  72.71f,
		-4.41f,  11.41f,  72.71f,  -8.85f,  19.11f,  72.71f,
		-4.41f,  26.81f,  72.71f,   4.48f,  26.81f,  72.71f,
		0.04f,  19.11f,  72.71f
	};

	private static final float [] NORMAL_DATA = new float [] {
		0.32574f,   0.94501f,   0.02902f,  -0.15411f,  -0.98707f,   0.04416f,
		-0.17418f,  -0.98281f,   0.06114f,  -0.32574f,   0.94501f,   0.02902f,
		0.15411f,  -0.98707f,   0.04416f,  -0.31415f,   0.94848f,   0.04115f,
		-0.32574f,   0.94502f,  -0.02890f,   0.15411f,  -0.98707f,   0.04416f,
		0.13989f,  -0.98891f,  -0.04986f,  -0.35511f,   0.93443f,  -0.02721f,
		0.14115f,  -0.98873f,  -0.04990f,   0.04254f,  -0.99820f,  -0.04235f,
		-0.52207f,  -0.03546f,  -0.85217f,   0.32574f,   0.94502f,  -0.02890f,
		0.36173f,   0.93215f,  -0.01596f,  -0.01920f,  -0.99246f,   0.12108f,
		0.29732f,   0.94792f,   0.11422f,   0.52012f,  -0.03551f,  -0.85336f,
		0.32574f,   0.94501f,   0.02902f,  -0.13989f,  -0.98891f,  -0.04986f,
		0.31415f,   0.94848f,   0.04115f,  -0.32574f,   0.94501f,   0.02902f,
		-0.14956f,   0.98767f,  -0.04635f,  -0.14911f,   0.98774f,  -0.04623f,
		-0.05992f,   0.99817f,   0.00794f,  -0.36172f,   0.93215f,  -0.01596f,
		0.35118f,   0.93569f,  -0.03384f,  -0.41187f,   0.90962f,  -0.05429f,
		-0.30969f,   0.94596f,   0.09620f,  -0.25335f,   0.96412f,  -0.07932f,
		0.14926f,   0.98770f,  -0.04647f,   0.14927f,   0.98772f,  -0.04617f,
		-0.02185f,   0.97861f,   0.20454f,   0.05992f,   0.99817f,   0.00794f,
		0.32263f,  -0.94612f,  -0.02784f,   0.35995f,  -0.93296f,   0.00421f,
		0.06024f,  -0.73756f,   0.67258f,   0.02180f,   0.97863f,   0.20448f,
		-0.08206f,  -0.93589f,   0.34260f,   0.01859f,  -0.99237f,   0.12191f,
		-0.03885f,  -0.98608f,   0.16170f,  -0.04242f,  -0.99823f,  -0.04174f,
		0.43794f,  -0.88552f,   0.15510f,  -0.32248f,  -0.94617f,  -0.02777f,
		-0.32267f,  -0.94611f,  -0.02767f,   0.63420f,   0.77181f,  -0.04578f,
		0.55312f,   0.79563f,   0.24702f,  -0.36352f,  -0.91951f,   0.14953f,
		-0.15411f,  -0.98707f,   0.04416f,  -0.35995f,  -0.93296f,   0.00421f,
		0.32262f,  -0.94613f,  -0.02765f,   0.17418f,  -0.98281f,   0.06114f,
		0.36352f,  -0.91951f,   0.14953f,  -0.14115f,  -0.98873f,  -0.04990f,
		0.86455f,   0.49915f,  -0.05828f,   0.83087f,  -0.47970f,   0.28202f,
		0.83087f,  -0.47970f,   0.28202f,   0.86455f,  -0.49915f,  -0.05828f,
		0.92619f,  -0.37559f,  -0.03307f,   0.00000f,  -0.95941f,   0.28202f,
		0.00000f,  -0.95941f,   0.28202f,   0.00000f,  -0.99468f,   0.10300f,
		-0.83087f,  -0.47970f,   0.28202f,  -0.83087f,  -0.47970f,   0.28202f,
		-0.86455f,  -0.49915f,  -0.05828f,  -0.83087f,   0.47970f,   0.28202f,
		-0.83087f,   0.47970f,   0.28202f,  -0.86455f,   0.49915f,  -0.05828f,
		-0.71870f,   0.68825f,  -0.09888f,   0.00000f,   0.95941f,   0.28202f,
		-0.00000f,   0.95941f,   0.28202f,   0.00000f,   0.99800f,  -0.06318f,
		0.83087f,   0.47970f,   0.28202f,   0.83087f,   0.47970f,   0.28202f,
		-0.06281f,  -0.73693f,   0.67304f,   0.71870f,   0.68825f,  -0.09888f,
		-0.92619f,  -0.37559f,  -0.03307f,  -0.63173f,   0.77392f,  -0.04432f,
		0.03794f,  -0.98638f,   0.16007f,   0.07982f,  -0.93637f,   0.34181f,
		0.40944f,   0.91014f,  -0.06325f,  -0.55087f,   0.79722f,   0.24695f,
		0.24360f,   0.96752f,  -0.06761f,   0.32440f,   0.53043f,  -0.78320f,
		0.74145f,  -0.02790f,  -0.67043f,  -0.43155f,  -0.89152f,   0.13773f,
		-0.22921f,  -0.83309f,  -0.50342f,  -0.71815f,  -0.02893f,  -0.69529f,
		-0.66855f,   0.00027f,  -0.74367f,   0.23004f,  -0.83333f,  -0.50263f,
		0.49023f,  -0.84910f,  -0.19676f,  -0.49023f,  -0.84910f,  -0.19676f,
		0.71304f,  -0.01086f,  -0.70104f,  -0.93853f,   0.01429f,  -0.34489f,
		-0.47562f,   0.82379f,  -0.30848f,   0.47678f,   0.82581f,  -0.30120f,
		-0.33412f,   0.48988f,  -0.80522f,   0.53246f,  -0.78067f,  -0.32717f,
		-0.49272f,  -0.80566f,  -0.32885f,   0.94443f,  -0.00039f,  -0.32871f,
		0.00000f,   0.00000f,   1.00000f,   0.00000f,   0.00000f,   1.00000f,
		0.00000f,  -0.00000f,   1.00000f,   0.00000f,   0.00000f,   1.00000f,
		-0.00000f,   0.00000f,   1.00000f,   0.00000f,   0.00000f,   1.00000f
	};

	private static final float [] TEXTURE_COORDINATE_DATA = new float [] {
		0.27f,   0.40f,   0.35f,   0.64f,   0.39f,   0.53f,
		0.01f,   0.48f,   0.12f,   0.25f,   0.01f,   0.22f,
		0.01f,   0.48f,   0.07f,   0.77f,   0.11f,   0.43f,
		0.74f,   0.40f,   0.67f,   0.64f,   0.84f,   0.64f,
		0.99f,   0.48f,   0.89f,   0.25f,   0.89f,   0.43f,
		0.74f,   0.40f,   0.57f,   0.16f,   0.62f,   0.53f,
		0.84f,   0.64f,   0.67f,   0.64f,   0.74f,   0.88f,
		1.00f,   0.23f,   0.89f,   0.25f,   0.99f,   0.48f,
		0.82f,   0.04f,   0.89f,   0.25f,   1.00f,   0.23f,
		0.74f,   0.88f,   0.59f,   0.78f,   0.51f,   0.97f,
		0.82f,   0.04f,   0.73f,   0.17f,   0.89f,   0.25f,
		0.82f,   0.04f,   0.61f,   0.00f,   0.61f,   0.16f,
		0.57f,   1.00f,   0.76f,   0.99f,   0.74f,   0.94f,
		0.27f,   0.88f,   0.35f,   0.64f,   0.18f,   0.64f,
		0.27f,   0.88f,   0.42f,   0.78f,   0.35f,   0.64f,
		0.19f,   0.04f,   0.27f,   0.17f,   0.40f,   0.16f,
		0.27f,   0.88f,   0.45f,   1.00f,   0.51f,   0.97f,
		1.00f,   0.81f,   0.84f,   0.89f,   0.87f,   0.95f,
		0.18f,   0.64f,   0.35f,   0.64f,   0.27f,   0.40f,
		0.01f,   0.22f,   0.12f,   0.25f,   0.19f,   0.04f,
		0.39f,   0.53f,   0.44f,   0.16f,   0.27f,   0.40f,
		0.62f,   0.53f,   0.67f,   0.64f,   0.74f,   0.40f,
		0.62f,   0.53f,   0.56f,   0.64f,   0.58f,   0.70f,
		0.62f,   0.53f,   0.58f,   0.70f,   0.67f,   0.64f,
		0.62f,   0.53f,   0.57f,   0.16f,   0.56f,   0.64f,
		0.67f,   0.64f,   0.59f,   0.78f,   0.74f,   0.88f,
		0.51f,   0.97f,   0.42f,   0.78f,   0.27f,   0.88f,
		0.51f,   0.97f,   0.59f,   0.78f,   0.51f,   0.73f,
		0.51f,   0.97f,   0.56f,   1.00f,   0.74f,   0.88f,
		0.82f,   1.00f,   0.82f,   0.98f,   0.76f,   0.99f,
		0.35f,   0.64f,   0.43f,   0.70f,   0.46f,   0.64f,
		0.35f,   0.64f,   0.46f,   0.64f,   0.39f,   0.53f,
		0.35f,   0.64f,   0.42f,   0.78f,   0.43f,   0.70f,
		0.46f,   0.64f,   0.44f,   0.16f,   0.39f,   0.53f,
		0.73f,   0.33f,   0.89f,   0.25f,   0.73f,   0.27f,
		0.73f,   0.33f,   0.92f,   0.78f,   0.89f,   0.43f,
		0.73f,   0.27f,   0.65f,   0.27f,   0.69f,   0.33f,
		0.58f,   0.70f,   0.59f,   0.78f,   0.67f,   0.64f,
		0.73f,   0.27f,   0.73f,   0.17f,   0.65f,   0.27f,
		0.61f,   0.16f,   0.73f,   0.17f,   0.82f,   0.04f,
		0.40f,   0.16f,   0.27f,   0.17f,   0.35f,   0.28f,
		0.40f,   0.16f,   0.40f,   0.01f,   0.19f,   0.04f,
		0.40f,   0.16f,   0.44f,   0.04f,   0.40f,   0.01f,
		0.28f,   0.27f,   0.11f,   0.43f,   0.28f,   0.34f,
		0.28f,   0.27f,   0.12f,   0.25f,   0.11f,   0.43f,
		0.43f,   0.70f,   0.51f,   0.73f,   0.48f,   0.66f,
		0.43f,   0.70f,   0.42f,   0.78f,   0.51f,   0.73f,
		0.28f,   0.27f,   0.27f,   0.17f,   0.12f,   0.25f,
		0.11f,   0.43f,   0.12f,   0.25f,   0.01f,   0.48f,
		0.11f,   0.43f,   0.07f,   0.77f,   0.28f,   0.34f,
		0.89f,   0.43f,   0.89f,   0.25f,   0.73f,   0.33f,
		0.89f,   0.43f,   0.92f,   0.78f,   0.99f,   0.48f,
		0.89f,   0.25f,   0.73f,   0.17f,   0.73f,   0.27f,
		0.12f,   0.25f,   0.27f,   0.17f,   0.19f,   0.04f,
		0.48f,   0.09f,   0.50f,   0.26f,   0.50f,   0.09f,
		0.48f,   0.09f,   0.46f,   0.04f,   0.45f,   0.04f,
		0.48f,   0.09f,   0.45f,   0.04f,   0.44f,   0.10f,
		0.44f,   0.10f,   0.50f,   0.26f,   0.48f,   0.09f,
		0.44f,   0.10f,   0.45f,   0.33f,   0.50f,   0.26f,
		0.49f,   0.39f,   0.48f,   0.33f,   0.46f,   0.33f,
		0.49f,   0.39f,   0.46f,   0.33f,   0.45f,   0.39f,
		0.45f,   0.39f,   0.47f,   0.63f,   0.49f,   0.39f,
		0.57f,   0.10f,   0.57f,   0.04f,   0.55f,   0.04f,
		0.57f,   0.10f,   0.55f,   0.04f,   0.54f,   0.09f,
		0.54f,   0.09f,   0.51f,   0.26f,   0.57f,   0.10f,
		0.54f,   0.09f,   0.55f,   0.04f,   0.54f,   0.03f,
		0.54f,   0.09f,   0.54f,   0.03f,   0.51f,   0.08f,
		0.51f,   0.08f,   0.51f,   0.26f,   0.54f,   0.09f,
		0.52f,   0.49f,   0.51f,   0.73f,   0.53f,   0.66f,
		0.52f,   0.49f,   0.52f,   0.44f,   0.50f,   0.44f,
		0.52f,   0.49f,   0.50f,   0.44f,   0.49f,   0.49f,
		0.49f,   0.49f,   0.51f,   0.73f,   0.52f,   0.49f,
		0.50f,   0.09f,   0.48f,   0.03f,   0.46f,   0.04f,
		0.50f,   0.09f,   0.46f,   0.04f,   0.48f,   0.09f,
		0.31f,   0.33f,   0.35f,   0.28f,   0.28f,   0.27f,
		0.48f,   0.66f,   0.51f,   0.73f,   0.49f,   0.49f,
		0.51f,   0.26f,   0.55f,   0.33f,   0.57f,   0.10f,
		0.53f,   0.66f,   0.51f,   0.73f,   0.58f,   0.70f,
		0.65f,   0.27f,   0.73f,   0.17f,   0.61f,   0.16f,
		0.35f,   0.28f,   0.27f,   0.17f,   0.28f,   0.27f,
		0.51f,   0.73f,   0.42f,   0.78f,   0.51f,   0.97f,
		0.51f,   0.73f,   0.59f,   0.78f,   0.58f,   0.70f,
		0.87f,   0.95f,   0.82f,   0.98f,   0.82f,   1.00f,
		0.87f,   0.95f,   0.85f,   0.94f,   0.82f,   0.98f,
		0.84f,   0.89f,   0.83f,   0.91f,   0.87f,   0.95f,
		0.61f,   0.00f,   0.57f,   0.04f,   0.61f,   0.16f,
		0.74f,   0.94f,   0.76f,   0.94f,   0.78f,   0.89f,
		0.76f,   0.99f,   0.76f,   0.94f,   0.74f,   0.94f,
		0.76f,   0.99f,   0.77f,   0.97f,   0.76f,   0.94f,
		0.78f,   0.89f,   0.83f,   0.91f,   0.84f,   0.89f,
		0.78f,   0.89f,   0.79f,   0.90f,   0.83f,   0.91f,
		0.78f,   0.89f,   0.76f,   0.94f,   0.79f,   0.90f,
		0.83f,   0.91f,   0.85f,   0.94f,   0.87f,   0.95f,
		0.17f,   0.80f,   0.12f,   0.87f,   0.22f,   0.86f,
		0.08f,   0.80f,   0.12f,   0.87f,   0.17f,   0.80f,
		0.04f,   0.89f,   0.12f,   0.87f,   0.08f,   0.80f,
		0.82f,   0.98f,   0.77f,   0.97f,   0.76f,   0.99f,
		0.17f,   0.95f,   0.12f,   0.87f,   0.08f,   0.95f,
		0.22f,   0.86f,   0.12f,   0.87f,   0.17f,   0.95f,
		0.08f,   0.95f,   0.12f,   0.87f,   0.04f,   0.89f,
		0.51f,   0.39f,   0.52f,   0.36f,   0.49f,   0.36f,
		0.49f,   0.36f,   0.52f,   0.36f,   0.51f,   0.32f,
		0.51f,   0.32f,   0.52f,   0.36f,   0.54f,   0.32f,
		0.54f,   0.32f,   0.52f,   0.36f,   0.56f,   0.36f,
		0.56f,   0.36f,   0.52f,   0.36f,   0.54f,   0.39f,
		0.54f,   0.39f,   0.52f,   0.36f,   0.51f,   0.39f
	};

	public Dugite(Alite alite) {
		super(alite, "Dugite", ObjectType.EnemyShip);
		setName(R.string.so_dugite);
		shipType = ShipType.Dugite;
		boundingBox = new float [] {-160.00f, 160.00f, -34.51f,  34.51f, -205.64f, 205.64f};
		numberOfVertices = 318;
		textureFilename = "textures/jabberwocky.png";
		maxSpeed          = 434.2f;
		maxPitchSpeed     = 3.000f;
		maxRollSpeed      = 3.000f;
		hullStrength      = 280.0f;
		hasEcm            = true;
		cargoType         = 1;
		aggressionLevel   = 15;
		escapeCapsuleCaps = 1;
		bounty            = 250;
		score             = 280;
		legalityType      = 1;
		maxCargoCanisters = 3;
		laserHardpoints.add(VERTEX_DATA[60]);
		laserHardpoints.add(VERTEX_DATA[61]);
		laserHardpoints.add(VERTEX_DATA[62]);
		laserHardpoints.add(VERTEX_DATA[63]);
		laserHardpoints.add(VERTEX_DATA[64]);
		laserHardpoints.add(VERTEX_DATA[65]);
		laserColor = 0x7FFFFF00l;
		laserTexture = "textures/laser_yellow.png";
		init();
	}

	@Override
	protected void init() {
		vertexBuffer = createReversedRotatedFaces(VERTEX_DATA, NORMAL_DATA,
			0,  10,   6,   0,  19,   5,   0,  20,  16,   1,   8,   2,   1,  18,  17,
			1,  21,   7,   2,   8,   3,   2,  18,   1,   3,  18,   2,   3,  33,   9,
			3,  34,  18,   3,  38,  14,   3,  39,  38,   4,  10,   5,   4,  32,  10,
			4,  35,  14,   4,  36,   9,   4,  37,  36,   5,  10,   0,   5,  19,   4,
			6,  20,   0,   7,   8,   1,   7,  12,  13,   7,  13,   8,   7,  21,  12,
			8,  33,   3,   9,  32,   4,   9,  33,  31,   9,  39,   3,   9,  44,  39,
			10,  15,  11,  10,  11,   6,  10,  32,  15,  11,  20,   6,  12,  18,  13,
			12,  21,  17,  13,  30,  29,  13,  33,   8,  13,  34,  30,  14,  34,   3,
			14,  35,  30,  14,  37,   4,  14,  40,  37,  15,  16,  11,  15,  19,  16,
			15,  31,  28,  15,  32,  31,  15,  35,  19,  16,  19,   0,  16,  20,  11,
			17,  18,  12,  17,  21,   1,  18,  34,  13,  19,  35,   4,  22,  28,  27,
			22,  48,  49,  22,  49,  23,  23,  28,  22,  23,  30,  28,  23,  49,  50,
			23,  50,  24,  24,  30,  23,  24,  50,  51,  24,  51,  25,  25,  29,  24,
			25,  51,  52,  25,  52,  26,  26,  29,  25,  26,  31,  29,  26,  52,  53,
			26,  53,  27,  27,  31,  26,  27,  53,  48,  27,  48,  22,  28,  30,  15,
			28,  31,  27,  29,  30,  24,  29,  31,  13,  30,  34,  14,  30,  35,  15,
			31,  32,   9,  31,  33,  13,  36,  44,   9,  36,  45,  44,  37,  41,  36,
			38,  40,  14,  38,  43,  40,  39,  43,  38,  39,  46,  43,  40,  41,  37,
			40,  42,  41,  40,  43,  42,  41,  45,  36,  41,  47,  45,  42,  47,  41,
			43,  47,  42,  44,  46,  39,  44,  47,  46,  45,  47,  44,  46,  47,  43,
			48,  54,  49,  49,  54,  50,  50,  54,  51,  51,  54,  52,  52,  54,  53,
			53,  54,  48);
		texCoordBuffer = GlUtils.toFloatBufferPositionZero(TEXTURE_COORDINATE_DATA);
		alite.getTextureManager().addTexture(textureFilename);
		if (Settings.engineExhaust) {
			addExhaust(new EngineExhaust(this, 18, 18, 300, 0, -5, 0, 1.0f, 0.68f, 0.22f, 0.7f));
		}
		initTargetBox();
	}

	@Override
	public boolean isVisibleOnHud() {
		return true;
	}

	@Override
	public Vector3f getHudColor() {
		return HUD_COLOR;
	}

	@Override
	public float getDistanceFromCenterToBorder(Vector3f dir) {
		return 50.0f;
	}
}
