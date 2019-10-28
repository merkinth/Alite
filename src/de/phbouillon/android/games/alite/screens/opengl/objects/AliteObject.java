package de.phbouillon.android.games.alite.screens.opengl.objects;

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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Vector3f;

public class AliteObject extends GraphicObject implements Serializable {
	private static final long serialVersionUID = -5229181033103145634L;

	public enum ZPositioning {
		Front,
		Normal,
		Back
	}

	private boolean visible = true;
	private boolean remove = false;
	private ZPositioning positionMode = ZPositioning.Normal;
	protected float boundingSphereRadius;
	private final Map <Integer, IMethodHook> destructionCallbacks = new LinkedHashMap<>();
	protected int hudColor; // scan_class, scanner_display_color1,2, scanner_hostile_display_color1,2
	private boolean visibleOnHud = false;
	private transient boolean saving = false;
	private final float[] displayMatrix = new float[16];
	float distanceFromCenterToBorder;
	private boolean depthTest = true;

	protected final Vector3f v0    = new Vector3f(0, 0, 0);
	protected final Vector3f v1    = new Vector3f(0, 0, 0);
	private final Vector3f v2    = new Vector3f(0, 0, 0);
	private final Vector3f edge1 = new Vector3f(0, 0, 0);
	private final Vector3f edge2 = new Vector3f(0, 0, 0);
	private final Vector3f pvec  = new Vector3f(0, 0, 0);
	private final Vector3f qvec  = new Vector3f(0, 0, 0);
	private final Vector3f tvec  = new Vector3f(0, 0, 0);

	public AliteObject(String id) {
		super(id);
	}

	public void setSaving(boolean b) {
		saving = b;
		if (saving) {
			destructionCallbacks.clear();
		}
	}

	public void addDestructionCallback(int id, final IMethodHook callback) {
		if (saving) {
			return;
		}
		destructionCallbacks.put(id, callback);
	}

	public boolean hasDestructionCallback(int id) {
		return destructionCallbacks.containsKey(id);
	}

	public Collection <IMethodHook> getDestructionCallbacks() {
		if (saving) {
			return Collections.emptyList();
		}
		return destructionCallbacks.values();
	}

	public void setDepthTest(boolean depthTest) {
		this.depthTest = depthTest;
	}

	public boolean isDepthTest() {
		return depthTest;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public boolean mustBeRemoved() {
		return remove;
	}

	public ZPositioning getZPositioningMode() {
		return positionMode;
	}

	public void setZPositioningMode(ZPositioning posMode) {
		positionMode = posMode;
	}

	public float getBoundingSphereRadius() {
		return boundingSphereRadius;
	}

	public void executeDestructionCallbacks() {
		for (IMethodHook dc: destructionCallbacks.values()) {
			dc.execute(0);
		}
	}

	protected boolean intersectInternal(int numberOfVertices, Vector3f origin, Vector3f direction, float [] verts) {
		for (int i = 0; i < numberOfVertices * 3; i += 9) {
			v0.x = verts[i + 0];
			v0.y = verts[i + 1];
			v0.z = verts[i + 2];

			v1.x = verts[i + 3];
			v1.y = verts[i + 4];
			v1.z = verts[i + 5];

			v2.x = verts[i + 6];
			v2.y = verts[i + 7];
			v2.z = verts[i + 8];

			v1.sub(v0, edge1);
			v2.sub(v0, edge2);
			direction.cross(edge2, pvec);
			float det = edge1.dot(pvec);
			if (Math.abs(det) < 0.00001f) {
				continue;
			}
			float invDet = 1.0f / det;
			origin.sub(v0, tvec);
			float u = tvec.dot(pvec) * invDet;
			if (u < 0.0f || u > 1.0f) {
				continue;
			}
			tvec.cross(edge1, qvec);
			float v = direction.dot(qvec) * invDet;
			if (v < 0.0f || u + v > 1.0f) {
				continue;
			}
			return true;
		}
		return false;
	}


	public boolean isVisibleOnHud() {
		return visibleOnHud;
	}

	public void setVisibleOnHud(boolean visibleOnHud) {
		this.visibleOnHud = visibleOnHud;
	}

	public int getHudColor() {
		return hudColor;
	}

	public void render() {
	}

	public float getDistanceFromCenterToBorder() {
		return distanceFromCenterToBorder;
	}

	public void setDisplayMatrix(float[] matrix) {
		System.arraycopy(matrix, 0, displayMatrix, 0, matrix.length);
	}

	public float[] getDisplayMatrix() {
		return displayMatrix;
	}

}
