package de.phbouillon.android.framework.impl.gl;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

import android.opengl.Matrix;
import de.phbouillon.android.framework.IMethodHook;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;

public class GraphicObject implements Serializable {
	private static final long serialVersionUID = -8039542554642450651L;
	private static final float SPEED_CHANGE_PER_SECOND = 225.0f;

	protected Vector3f worldPosition;
	protected Vector3f rightVector;
	protected Vector3f upVector;
	protected Vector3f forwardVector;
	private Vector3f initialDirection = new Vector3f(1.0f, 1.0f, 1.0f);
	private Vector3f temp = new Vector3f(1.0f, 1.0f, 1.0f);
	private float speed;
	private float targetSpeed;

	private String id;
	private int name;

	protected float [] currentMatrix = new float[16];
	private float [] tempMatrix = new float[16];
	private float [] tempMatrix2 = new float[16];
	protected boolean cached = false;
	private IMethodHook updater = null;

	public GraphicObject() {
		this("Unknown");
	}

	private String toDebugString() {
		return "GO:            " + (id == null ? "<null>" : id) +
			   "\nworldPosition: " + worldPosition +
			   "\nforward:       " + forwardVector +
			   "\nup:            " + upVector +
			   "\nright:         " + rightVector +
			   "\ninitial:       " + initialDirection +
			   "\nspeed:         " + speed +
			   "\ntargetSpeed:   " + targetSpeed +
			   "\ncurrentMatrix: " + getMatrixString();
	}

	public void onUpdate(float deltaTime) {
		if (updater == null) {
			return;
		}
		updater.execute(deltaTime);
	}

	public void setUpdater(IMethodHook updater) {
		this.updater = updater;
	}

	public IMethodHook getUpdater() {
		return updater;
	}

	public GraphicObject(float [] matrix) {
		this(matrix, "Unknown");
	}

	public void assertOrthoNormal() {
		computeMatrix();
		float det = currentMatrix[0] * (currentMatrix[5] * currentMatrix[10] - currentMatrix[6] * currentMatrix[9]) -
				    currentMatrix[1] * (currentMatrix[4] * currentMatrix[10] - currentMatrix[6] * currentMatrix[8]) +
				    currentMatrix[2] * (currentMatrix[4] * currentMatrix[ 9] - currentMatrix[5] * currentMatrix[8]);

		if (Math.abs(det - 1.0) > 0.0001) {
			AliteLog.e("ALERT!", "Determinant of matrix != 1: " + det);
		}
	}

	public GraphicObject(String id) {
		worldPosition = new Vector3f(0.0f, 0.0f, 0.0f);
		rightVector   = new Vector3f(1.0f, 0.0f, 0.0f);
		upVector      = new Vector3f(0.0f, 1.0f, 0.0f);
		forwardVector = new Vector3f(0.0f, 0.0f, 1.0f);
		speed         = 0.0f;
		targetSpeed   = 0.0f;
		this.id = id;
	}

	public GraphicObject(float [] matrix, String id) {
		worldPosition = new Vector3f(matrix[12], matrix[13], matrix[14]);
		rightVector   = new Vector3f(matrix[ 0], matrix[ 1], matrix[ 2]);
		rightVector.normalize();
		upVector      = new Vector3f(matrix[ 4], matrix[ 5], matrix[ 6]);
		upVector.normalize();
		forwardVector = new Vector3f(matrix[ 8], matrix[ 9], matrix[10]);
		forwardVector.normalize();
		speed         = 0.0f;
		targetSpeed   = 0.0f;
		this.id = id;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.e("readObject", "GraphicObject.readObject");
			in.defaultReadObject();
			AliteLog.e("readObject", "GraphicObject.readObject I");
			AliteLog.d("Read object", toDebugString());
			AliteLog.e("readObject", "GraphicObject.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	private void writeObject(ObjectOutputStream out)
            throws IOException {
		try {
			out.defaultWriteObject();
		} catch(IOException e) {
			AliteLog.e("PersistenceException", "Graphic Object " + getId(), e);
			throw e;
		}
    }

	public void setPosition(Vector3f position) {
		worldPosition.x = position.x;
		worldPosition.y = position.y;
		worldPosition.z = position.z;
		cached = false;
	}

	public void setPosition(float x, float y, float z) {
		worldPosition.x = x;
		worldPosition.y = y;
		worldPosition.z = z;
		cached = false;
	}

	public void setRightVector(Vector3f rightVector) {
		this.rightVector.x = rightVector.x;
		this.rightVector.y = rightVector.y;
		this.rightVector.z = rightVector.z;
		this.rightVector.normalize();
		cached = false;
	}

	public void setUpVector(Vector3f upVector) {
		this.upVector.x = upVector.x;
		this.upVector.y = upVector.y;
		this.upVector.z = upVector.z;
		this.upVector.normalize();
		cached = false;
	}

	public void setForwardVector(Vector3f forwardVector) {
		this.forwardVector.x = forwardVector.x;
		this.forwardVector.y = forwardVector.y;
		this.forwardVector.z = forwardVector.z;
		this.forwardVector.normalize();
		cached = false;
	}

	public void setForwardVector(float x, float y, float z) {
		forwardVector.x = x;
		forwardVector.y = y;
		forwardVector.z = z;
		forwardVector.normalize();
		cached = false;
	}

	public Vector3f getPosition() {
		return worldPosition;
	}

	public Vector3f getRightVector() {
		return rightVector;
	}

	public Vector3f getUpVector() {
		return upVector;
	}

	public Vector3f getForwardVector() {
		return forwardVector;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name == 0 ? id : L.string(name);
	}

	public void setName(int name) {
		this.name = name;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed       = speed;
		this.targetSpeed = speed;
	}

	public void adjustSpeed(float speed) {
		targetSpeed = speed;
	}

	public void updateSpeed(float deltaTime) {
		if (Math.abs(targetSpeed - speed) < 0.0001) {
			return;
		}
		if (speed < targetSpeed) {
			speed += deltaTime * SPEED_CHANGE_PER_SECOND;
			if (speed > targetSpeed) {
				speed = targetSpeed;
			}
		} else {
			speed -= deltaTime * SPEED_CHANGE_PER_SECOND;
			if (speed < targetSpeed) {
				speed = targetSpeed;
			}
		}
	}

	public float getTargetSpeed() {
		return targetSpeed;
	}

	public final void computeMatrix() {
		if (!cached) {
			Vector3f rn = rightVector;
			rn.normalize();
			currentMatrix[ 0] = rn.x;
			currentMatrix[ 1] = rn.y;
			currentMatrix[ 2] = rn.z;
			currentMatrix[ 3] = 0.0f;

			Vector3f un = upVector;
			un.normalize();
			currentMatrix[ 4] = un.x;
			currentMatrix[ 5] = un.y;
			currentMatrix[ 6] = un.z;
			currentMatrix[ 7] = 0.0f;

			Vector3f fn = forwardVector;
			fn.normalize();
			currentMatrix[ 8] = fn.x;
			currentMatrix[ 9] = fn.y;
			currentMatrix[10] = fn.z;
			currentMatrix[11] = 0.0f;

			currentMatrix[12] = worldPosition.x;
			currentMatrix[13] = worldPosition.y;
			currentMatrix[14] = worldPosition.z;
			currentMatrix[15] = 1.0f;

			cached = true;
		}
	}

	public float [] getMatrix() {
		computeMatrix();
		return currentMatrix;
	}

	private String getMatrixString() {
		computeMatrix();
		return String.format(Locale.getDefault(), "[%4.2f, %4.2f, %4.2f, %4.2f\n" +
		                     " %4.2f, %4.2f, %4.2f, %4.2f\n" +
		                     " %4.2f, %4.2f, %4.2f, %4.2f\n" +
		                     " %4.2f, %4.2f, %4.2f, %4.2f]\n",
		                       currentMatrix[ 0], currentMatrix[ 4], currentMatrix[ 8], currentMatrix[12],
		                       currentMatrix[ 1], currentMatrix[ 5], currentMatrix[ 9], currentMatrix[13],
		                       currentMatrix[ 2], currentMatrix[ 6], currentMatrix[10], currentMatrix[14],
		                       currentMatrix[ 3], currentMatrix[ 7], currentMatrix[11], currentMatrix[15]);
	}

	public void translateForward(float deltaTime) {
		if (Math.abs(speed) < 0.00001) {
			return;
		}
		computeMatrix();
		temp.x = forwardVector.x * speed * deltaTime;
		temp.y = forwardVector.y * speed * deltaTime;
		temp.z = forwardVector.z * speed * deltaTime;

		Matrix.setIdentityM(tempMatrix, 0);
		Matrix.translateM(tempMatrix, 0, temp.x, temp.y, temp.z);
		Matrix.multiplyMM(tempMatrix2, 0, currentMatrix, 0, tempMatrix, 0);
		System.arraycopy(tempMatrix2, 0, currentMatrix, 0, 16);
		extractVectors();
	}

	public void moveForward(float deltaTime) {
		if (Math.abs(speed) < 0.00001) {
			return;
		}
		temp.x = forwardVector.x * speed * deltaTime;
		temp.y = forwardVector.y * speed * deltaTime;
		temp.z = forwardVector.z * speed * deltaTime;
		worldPosition.add(temp);
		cached = false;
	}

	public void setInitialDirection(float x, float y, float z) {
		initialDirection.x = x;
		initialDirection.y = y;
		initialDirection.z = z;
		initialDirection.normalize();
	}

	public Vector3f getInitialDirection() {
		return initialDirection;
	}

	public void moveForward(float deltaTime, Vector3f dir) {
		if (Math.abs(speed) < 0.00001) {
			return;
		}
		temp.x = dir.x * speed * deltaTime;
		temp.y = dir.y * speed * deltaTime;
		temp.z = dir.z * speed * deltaTime;
		worldPosition.add(temp);

		cached = false;
	}

	public float [] getScaledMatrix(float scale) {
		computeMatrix();
		Matrix.scaleM(tempMatrix, 0, currentMatrix, 0, scale, scale, scale);
		return tempMatrix;
	}

	public void scale(float scale) {
		computeMatrix();
		Matrix.scaleM(currentMatrix, 0, scale, scale, scale);
		extractVectors();
	}

	public void scale(float scaleX, float scaleY, float scaleZ) {
		computeMatrix();
		Matrix.scaleM(currentMatrix, 0, scaleX, scaleY, scaleZ);
		extractVectors();
	}

	protected final void extractVectors() {
		rightVector.x   = currentMatrix[ 0];
		rightVector.y   = currentMatrix[ 1];
		rightVector.z   = currentMatrix[ 2];

		upVector.x      = currentMatrix[ 4];
		upVector.y      = currentMatrix[ 5];
		upVector.z      = currentMatrix[ 6];

		forwardVector.x = currentMatrix[ 8];
		forwardVector.y = currentMatrix[ 9];
		forwardVector.z = currentMatrix[10];

		worldPosition.x = currentMatrix[12];
		worldPosition.y = currentMatrix[13];
		worldPosition.z = currentMatrix[14];
	}

	public float [] applyDeltaRotation(float x, float y, float z) {
		computeMatrix();
		Matrix.rotateM(currentMatrix, 0, z, 0, 0, 1);
		Matrix.rotateM(currentMatrix, 0, x, 1, 0, 0);
		Matrix.rotateM(currentMatrix, 0, y, 0, 1, 0);
		extractVectors();
		return currentMatrix;
	}

	public void orthoNormalize() {
		computeMatrix();
		forwardVector.normalize();
		upVector.cross(forwardVector, temp);
		temp.normalize();
		forwardVector.cross(temp, upVector);
		cached = false;
	}

	private void lookAt(float x, float y, float z, float ux, float uy, float uz) {
		forwardVector.x = x - worldPosition.x;
		forwardVector.y = y - worldPosition.y;
		forwardVector.z = z - worldPosition.z;
		forwardVector.normalize();
		upVector.x = ux;
		upVector.y = uy;
		upVector.z = uz;
		upVector.normalize();
		forwardVector.cross(upVector, rightVector);
		cached = false;
		assertOrthoNormal();
		computeMatrix();
	}

	public void lookAt(Vector3f v, Vector3f up) {
		lookAt(v.x, v.y, v.z, up.x, up.y, up.z);
	}

	public void setMatrix(float [] matrix) {
		int counter = 0;
		for (float f: matrix) {
			currentMatrix[counter++] = f;
		}
		cached = true;
		extractVectors();
	}
}
