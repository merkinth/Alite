package de.phbouillon.android.games.alite.screens.opengl.objects.space;

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

// d:\TOM\java\oolite-master\Resources\Config\autoAImap.plist
// http://wiki.alioth.net/index.php/State_machine
// http://wiki.alioth.net/index.php/OXP_howto_AI
// http://wiki.alioth.net/index.php/Oolite_PriorityAI_Tutorial
// d:\TOM\java\oolite-master\Resources\Scripts\oolite-priorityai.js

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import android.opengl.Matrix;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Quaternion;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.curves.BreakDown;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.curves.BreakUp;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.curves.Curve;

public final class SpaceObjectAI implements Serializable {
	private static final long serialVersionUID = 8646121427456794783L;

	public static final String AI_STATE_GLOBAL = "GLOBAL";
	public static final String AI_STATE_ATTACK = "FIGHTING";
	public static final String AI_STATE_FLEE = "FLEEING";
	public static final String AI_STATE_LURKING = "LURKING";
	public static final String AI_STATE_FLY_STRAIGHT = "FLY_STRAIGHT";
	public static final String AI_STATE_FLY_PATH = "FLY_PATH";
	public static final String AI_STATE_EVADE = "EVADE";
	public static final String AI_STATE_TRACK = "TRACK";
	public static final String AI_STATE_MISSILE_TRACK = "MISSILE_TRACK";
	public static final String AI_STATE_FOLLOW_CURVE = "FOLLOW_CURVE";

	private static final float FIRE_MISSILE_UPON_FIRST_HIT_PROBABILITY  = 5.0f;
	private static final long  BASE_DELAY_BETWEEN_SHOOT_CHECKS          = 59880239L; // 16.7 FPS
	private static final long  SHOOT_DELAY_REDUCE_PER_RATING_LEVEL      = 3318363L; // 1.6625 Delta FPS

	private static final Set<String> priorityMessages = new HashSet<>(Arrays.asList("ENTER", "EXIT", "UPDATE",
		"LAUNCHED OKAY", "FRUSTRATED", "DESIRED_RANGE_ACHIEVED", "APPROACHING_SURFACE", "LEAVING_SURFACE",
		"TARGET_LOST", "WITCHSPACE OKAY", "STATION_LAUNCHED_SHIP", "ECM", "PLAYER WITCHSPACE", "CLOSE CONTACT",
		"POSITIVE X TRAVERSE", "NEGATIVE X TRAVERSE", "POSITIVE Y TRAVERSE", "NEGATIVE Y TRAVERSE",
		"POSITIVE Z TRAVERSE", "NEGATIVE Z TRAVERSE", "ATTACKED", "ATTACKED_BY_CLOAKED", "NAVPOINT_REACHED",
		"ENDPOINT_REACHED", "CASCADE_WEAPON_DETECTED", "ATTACKER_MISSED", "INCOMING_MISSILE", "COLLISION",
		"ENERGY_LOW", "LANDED_ON_PLANET", "OFFENCE_COMMITTED", "FOUND_PILOT", "PILOT_ARRIVED", "GROUP_ATTACK_TARGET",
		"HAZARD_CAN_BE_DESTROYED", "WITCHSPACE UNAVAILABLE", "WITCHSPACE BLOCKED", "ACCEPT_DISTRESS_CALL",
		"DOCKING_REQUESTED", "GREEN_ALERT", "YELLOW_ALERT", "RED_ALERT", "ENTER WORMHOLE", "EXITED WITCHSPACE",
		"PLAYER WITCHSPACE"));

	// => ~30 FPS at Elite.
	private final SpaceObject so;

	private final Quaternion q1 = new Quaternion();
	private final Quaternion q2 = new Quaternion();
	private final Quaternion q3 = new Quaternion();
	private final Vector3f   v0 = new Vector3f(0, 0, 0);
	private final Vector3f   v1 = new Vector3f(0, 0, 0);
	private final Vector3f   v2 = new Vector3f(0, 0, 0);
	private final Vector3f   v3 = new Vector3f(0, 0, 0);

	private final Stack <String> currentState = new Stack<>();
	private SpaceObject target = null;
	private final Vector3f evadePosition = new Vector3f(0, 0, 0);
	private float evadeRangeSq = 0;
	private final List <WayPoint> waypoints = new ArrayList<>();
	private float currentDistance = -1;
	private boolean pitchingOver = false;
	private float flightRoll = 0.0f;
	private float flightPitch = 0.0f;
	private boolean waitForSafeZoneExit = false;
	private final Timer lastShootCheck = new Timer().setAutoResetWithImmediateAtFirstCall();
	private Curve curve = null;
	private final Timer curveFollowStart = new Timer();
	private final Vector3f lastRotation = new Vector3f(0, 0, 0);

//	private Timer updateTimer = new Timer().setAutoReset();
//	private float pauseAI = 1f / 8; // sec;
	private String stateName = AI_STATE_GLOBAL;
	private String lastMessage;
	private float timeSpent;

	SpaceObjectAI(final SpaceObject so) {
		this.so = so;
		currentState.push(AI_STATE_GLOBAL);
	}

	void orientUsingRollPitchOnly(Vector3f targetPosition, float deltaTime) {
		trackInternal(targetPosition, 1000.0f, deltaTime, false);
		executeSteeringNoSpeedChange(targetPosition);
	}

	private float trackTargetPosition(float deltaTime) {
		Quaternion.fromMatrix(so.getMatrix(), q1);
		q1.normalize();

		so.getPosition().sub(target.getPosition(), v0);
		v0.normalize();

		target.getUpVector().cross(v0, v1);
		v1.normalize();
		v0.cross(v1, v2);
		v2.normalize();

		Quaternion.fromVectors(v1, v2, v0, q2);
		q2.normalize();
		q1.computeDifference(q2, q3);
		q3.normalize();

		q3.axisOfRotation(v0);
		float angle = (float) Math.toDegrees(q3.angleOfRotation());
		if (angle > 180) {
			angle = 360 - angle;
			v0.negate();
		}
		float absAngle = Math.abs(angle);
		if (deltaTime > 0) {
			angle = clamp(angle, -so.getMaxPitchSpeed(), so.getMaxPitchSpeed()) * deltaTime;
		}
		if (Math.abs(angle) > 0.0001f && !Float.isInfinite(angle) && !Float.isNaN(angle)) {
			Matrix.rotateM(so.getMatrix(), 0, angle, v0.x, v0.y, v0.z);
			so.extractVectors();
		}

		return absAngle;
	}

	private float trackInternal(Vector3f targetPosition, float desiredRangeSq, float deltaTime, boolean retreat) {
		float rate1 = 2.0f * deltaTime;
		float rate2 = 4.0f * deltaTime;
		float stickRoll = 0.0f;
		float stickPitch = 0.0f;
		float reverse = 1.0f;
		float minD = 0.004f;
		float maxCos = 0.995f;

		float maxPitch = so.getMaxPitchSpeed() * 30 * deltaTime;
		float maxRoll  = so.getMaxRollSpeed() * 30 * deltaTime;

		if (retreat) {
			reverse = -reverse;
		}

		so.getPosition().sub(targetPosition, v0);
		float rangeSq = v0.lengthSq();
		if (rangeSq > desiredRangeSq) {
			maxCos = (float) Math.sqrt(1.0 - 0.90 * desiredRangeSq / rangeSq);
		}
		if (v0.isZeroVector()) {
			v0.z = 1.0f;
		} else {
			v0.normalize();
		}

		float dRight   = v0.dot(so.getRightVector());
		float dUp      = v0.dot(so.getUpVector());
		float dForward = v0.dot(so.getForwardVector());

		if (pitchingOver) {
			maxPitch *= 4.0f;
			maxRoll *= 4.0f;
			if (reverse * dUp < 0) {
				stickPitch = maxPitch;
			} else {
				stickPitch = -maxPitch;
			}
			pitchingOver = reverse * dForward < 0.707;
		}
		if (dForward < maxCos || retreat) {
			if (dForward <= -maxCos) {
				dUp = minD * 2.0f;
			}
			if (dUp > minD) {
				int factor = (int) Math.sqrt(Math.abs(dRight) / Math.abs(minD));
				if (factor > 8) {
					factor = 8;
				}
				if (dRight > minD) {
					stickRoll = -maxRoll * 0.125f * factor;
				}
				if (dRight < -minD) {
					stickRoll = maxRoll * 0.125f * factor;
				}
				if (Math.abs(dRight) < Math.abs(stickRoll) * deltaTime) {
					stickRoll = Math.abs(dRight) / deltaTime * (stickRoll < 0 ? -1 : 1);
				}
			}
			if (dUp < -minD) {
				int factor = (int) Math.sqrt(Math.abs(dRight) / Math.abs(minD));
				if (factor > 8) {
					factor = 8;
				}
				if (dRight > minD) {
					stickRoll = maxRoll * 0.125f * factor;
				}
				if (dRight < -minD) {
					stickRoll = -maxRoll * 0.125f * factor;
				}
				if (Math.abs(dRight) < Math.abs(stickRoll) * deltaTime) {
					stickRoll = Math.abs(dRight) / deltaTime * (stickRoll < 0 ? -1 : 1);
				}
			}
			if (Math.abs(stickRoll) < 0.0001) {
				int factor = (int) Math.sqrt(Math.abs(dUp) / Math.abs(minD));
				if (factor > 8) {
					factor = 8;
				}
				if (dUp > minD) {
					stickPitch = -maxPitch * reverse * 0.125f * factor;
				}
				if (dUp < -minD) {
					stickPitch = maxPitch * reverse * 0.125f * factor;
				}
				if (Math.abs(dUp) < Math.abs(stickPitch) * deltaTime) {
					stickPitch = Math.abs(dUp) / deltaTime * (stickPitch < 0 ? -1 : 1);
				}
			}
		}

		if (stickRoll > 0.0 && flightRoll < 0.0 || stickRoll < 0.0 && flightRoll > 0.0) {
			rate1 *= 4.0f;
		}
		if (stickPitch > 0.0 && flightPitch < 0.0 || stickPitch < 0.0 && flightPitch > 0.0) {
			rate2 *= 4.0f;
		}

		if (flightRoll < stickRoll - rate1) {
			stickRoll = flightRoll + rate1;
		}
		if (flightRoll > stickRoll + rate1) {
			stickRoll = flightRoll - rate1;
		}
		if (flightPitch < stickPitch - rate2) {
			stickPitch = flightPitch + rate2;
		}
		if (flightPitch > stickPitch + rate2) {
			stickPitch = flightPitch - rate2;
		}

		flightRoll = stickRoll;
		flightPitch = stickPitch;


		if (retreat) {
			dForward *= dForward;
		}
		if (dForward < 0.0) {
			return 0.0f;
		}

		if (Math.abs(flightRoll) < 0.0001 && Math.abs(flightPitch) < 0.0001) {
			return 1.0f;
		}

		return dForward;
	}

	private float executeSteering(float desiredSpeed) {
		return executeSteering(desiredSpeed, target == null ? null : target.getPosition());
	}

	private float executeSteering(float desiredSpeed, Vector3f targetPosition) {
		float angle = executeSteeringNoSpeedChange(targetPosition);
		if (targetPosition != null) {
			if (desiredSpeed > so.getMaxSpeed()) {
				so.adjustSpeed(-so.getMaxSpeed());
			} else if (desiredSpeed < 0) {
				calculateTrackingSpeed(angle);
			} else {
				so.adjustSpeed(-desiredSpeed);
			}
		}
		return angle;
	}

	private float executeSteeringNoSpeedChange(Vector3f targetPosition) {
		so.applyDeltaRotation(flightPitch, 0, flightRoll);
		if (so.isPlayer()) {
			Alite.get().getCobra().setRotation(flightPitch, flightRoll);
		}
		if (targetPosition == null) {
			return 90; // To make sure this doesn't fire randomly...
		}
		so.getPosition().copy(v0);
		v0.sub(targetPosition);
		v0.normalize();
		return so.getForwardVector().angleInDegrees(v0);
	}

	private float clamp(float val, float min, float max) {
		return val < min ? min : val > max ? max : val;
	}

	void orient(Vector3f targetPosition, Vector3f targetUp, float deltaTime) {
		Quaternion.fromMatrix(so.getMatrix(), q1);
		q1.normalize();

		so.getPosition().sub(targetPosition, v0);
		v0.normalize();

		targetUp.cross(v0, v1);
		v1.normalize();
		v0.cross(v1, v2);
		v2.normalize();

		Quaternion.fromVectors(v1, v2, v0, q2);
		q2.normalize();
		q1.computeDifference(q2, q3);
		q3.normalize();

		q3.axisOfRotation(v0);
		float angle = (float) Math.toDegrees(q3.angleOfRotation());
		if (angle > 180) {
			angle = 360 - angle;
			v0.negate();
		}
		if (deltaTime > 0) {
			angle = clamp(angle, -so.getMaxPitchSpeed() * 40, so.getMaxPitchSpeed() * 40) * deltaTime;
		}
		if (Math.abs(angle) > 0.0001f && !Float.isInfinite(angle) && !Float.isNaN(angle)) {
			Matrix.rotateM(so.getMatrix(), 0, angle, v0.x, v0.y, v0.z);
			if (so.isPlayer()) {
				v1.x = 1;
				v1.y = 0;
				v1.z = 0;
				v2.x = 0;
				v2.y = 0;
				v2.z = 1;
				Alite.get().getCobra().setRotation(v0.dot(v1), v0.dot(v2));
			}
			so.extractVectors();
		}
	}

	private void calculateTrackingSpeed(float angle) {
		if (so.getType() == ObjectType.Missile) {
			calculateMissileSpeed(angle);
			return;
		}
		if (angle > 50) {
			so.adjustSpeed(-so.getMaxSpeed() * 0.2f);
		} else if (angle > 40) {
			so.adjustSpeed(-so.getMaxSpeed() * 0.4f);
		} else if (angle > 30) {
			so.adjustSpeed(-so.getMaxSpeed() * 0.6f);
		} else if (angle > 20) {
			so.adjustSpeed(-so.getMaxSpeed() * 0.7f);
		} else if (angle > 10) {
			so.adjustSpeed(-so.getMaxSpeed() * 0.8f);
		} else {
			so.adjustSpeed(-so.getMaxSpeed());
		}
	}

	private void calculateMissileSpeed(float angle) {
		if (angle > 50) {
			so.setSpeed(-so.getMaxSpeed() * 0.3f);
		} else if (angle > 40) {
			so.setSpeed(-so.getMaxSpeed() * 0.4f);
		} else if (angle > 30) {
			so.setSpeed(-so.getMaxSpeed() * 0.5f);
		} else if (angle > 20) {
			so.setSpeed(-so.getMaxSpeed() * 0.6f);
		} else if (angle > 10) {
			so.setSpeed(-so.getMaxSpeed() * 0.7f);
		} else {
			so.setSpeed(-so.getMaxSpeed());
		}
	}

	private void avoidCollision() {
		SpaceObject proximity = so.getProximity();
		if (proximity != null && !so.isInBay()) {
			pushState(AI_STATE_EVADE);
		}
	}

	private void attackObject(float deltaTime) {
		if (target.isPlayer()) {
			if (InGameManager.playerInSafeZone && so.getType() != ObjectType.Police && !so.isIgnoreSafeZone()) {
				waitForSafeZoneExit = true;
				pushState(AI_STATE_FLEE);
				return;
			}
		}
		trackInternal(target.getPosition(), 1000.0f, deltaTime, false);
		avoidCollision();
		float angle = executeSteering(-1);
		float distanceSq = so.getPosition().distanceSq(target.getPosition());
		if (angle >= 10 || distanceSq >= so.getShootRangeSq() || so.hasEjected()) {
			return;
		}
		if (target.isCloaked()) {
			return;
		}
		int rating = Alite.get().getPlayer().getRating().ordinal();
		if (rating < 7 && !lastShootCheck.hasPassedNanos(
			BASE_DELAY_BETWEEN_SHOOT_CHECKS - (rating + 2) * SHOOT_DELAY_REDUCE_PER_RATING_LEVEL)) {
			return;
		}
		if (Alite.get().getLaserManager() != null && so.getAggressionLevel() > Math.random() * 256) {
			Alite.get().getLaserManager().fire(so, target);
		}
	}

	private void fleeObject(float deltaTime) {
		if (target.isPlayer()) {
			if (!InGameManager.playerInSafeZone && waitForSafeZoneExit) {
				popState();
				waitForSafeZoneExit = false;
				return;
			}
		}
		trackInternal(target.getPosition(), 1000.0f, deltaTime, true);
		avoidCollision();
		executeSteering(so.getMaxSpeed());
	}

	private void flyPath(float deltaTime) {
		if (waypoints.isEmpty()) {
			popState();
			if (currentState.isEmpty()) {
				setState(AI_STATE_GLOBAL);
			}
			so.aiStateCallback(AiStateCallback.EndOfWaypointsReached);
			return;
		}
		WayPoint wp = waypoints.get(0);
		float d = trackInternal(wp.position, 1000.0f, deltaTime, false);
		float targetSpeed = wp.orientFirst ? 0.0f : so.getMaxSpeed();
		if (Math.abs(1.0 - d) < 0.01 && wp.orientFirst) {
			targetSpeed = so.getMaxSpeed();
			wp.orientFirst = false;
		}
		so.adjustSpeed(-targetSpeed);
		avoidCollision();
		executeSteering(targetSpeed, wp.position);
		float distance = so.getPosition().distanceSq(wp.position);
		if (distance < 1000 || currentDistance > 0 && currentDistance < 40000 && distance > currentDistance) {
			currentDistance = -1;
			wp.reached();
			waypoints.remove(0);
		} else {
			currentDistance = distance;
		}
	}

	private void followCurve() {
		curve.compute(curveFollowStart.getPassedSeconds());

		so.setPosition(curve.getCurvePosition());
		so.setForwardVector(curve.getcForward());
		so.setRightVector(curve.getcRight());
		so.setUpVector(curve.getcUp());

		curve.getCurveRotation().copy(v0);
		so.applyDeltaRotation(v0.x, v0.y, v0.z);
		so.assertOrthoNormal();

//		avoidCollision();

		if (curve.reachedEnd()) {
			popState();
			if (currentState.isEmpty()) {
				so.setSpeed(0);
				setState(AI_STATE_GLOBAL);
			}
		}
	}

	private void updateEvade(float deltaTime) {
		float distanceSq = so.getPosition().distanceSq(evadePosition);
		SpaceObject proximity = so.getProximity();
		boolean clearEvade = false;
		if (proximity != null && ObjectType.isSpaceStation(proximity.getType())) {
			float maxExtentSq = proximity.getMaxExtent();
			maxExtentSq *= maxExtentSq;
			clearEvade = distanceSq > maxExtentSq;
		}
		if (clearEvade || distanceSq > evadeRangeSq || proximity == null || proximity.getHullStrength() <= 0) {
			so.setProximity(null);
			popState();
			if (currentState.isEmpty()) {
				setState(AI_STATE_GLOBAL);
			}
			return;
		}
		proximity.getPosition().sub(so.getPosition(), v0);
		v0.scale(0.5f);
		v0.add(so.getPosition());
		v0.copy(evadePosition);
		evadeRangeSq = (proximity.getBoundingSphereRadiusSq() * 3.0f + so.getBoundingSphereRadiusSq() * 3.0f) * 18;
		float dForward = trackInternal(evadePosition, evadeRangeSq, deltaTime, true);
		executeSteering(so.getMaxSpeed() * (0.5f * dForward + 0.5f));
	}

	private void updateTrack(float deltaTime) {
		if (so.getType() == ObjectType.Missile) {
			calculateMissileSpeed(trackTargetPosition(deltaTime));
			return;
		}
		if (!target.isCloaked()) {
			trackInternal(target.getPosition(), 1000.0f, deltaTime, false);
		}
		executeSteering(so.getType() == ObjectType.Missile ? -1 : so.getMaxSpeed());
	}

	private boolean pushIfNewState(String newState) {
		if (currentState.isEmpty() || !currentState.peek().equals(newState)) {
			currentState.push(newState);
			return true;
		}
		return false;
	}

	private void initiateMissileTrack() {
		currentState.clear();
		if (target == null) {
			return;
		}
		float distanceSq = target.getPosition().distanceSq(so.getPosition());
		if (distanceSq < 9000000) {
			currentState.push(AI_STATE_TRACK);
			return;
		}
		pushState(AI_STATE_TRACK);
		so.getPosition().copy(v0);
		target.getPosition().sub(v0, v1);
		v1.scale(0.5f);
		target.getPosition().sub(v1, v0);
		target.getRightVector().copy(v1);
		v1.scale(1000);
		v0.add(v1);
		setWaypoints(WayPoint.newWayPoint(v0, target.getUpVector()));
		pushState(AI_STATE_FLY_PATH);
	}

	void setWaypoints(WayPoint... waypoint) {
		waypoints.clear();
		if (waypoint != null && waypoint.length > 0) {
			for (WayPoint w: waypoint) {
				if (w != null) {
					waypoints.add(w);
				}
			}
		}
		currentDistance = -1;
		if (!waypoints.isEmpty() && !waypoints.get(0).orientFirst) {
			so.adjustSpeed(-so.getMaxSpeed());
		} else {
			so.adjustSpeed(0);
		}
	}

	private void initiateFollowCurve() {
		pushIfNewState(AI_STATE_FOLLOW_CURVE);
		waypoints.clear();
		curve = Math.random() < 0.5 ? new BreakUp(so) : new BreakDown(so);
		curveFollowStart.reset();
		currentDistance = -1;
		so.adjustSpeed(-so.getMaxSpeed());
		lastRotation.x = 0;
		lastRotation.y = 0;
		lastRotation.z = 0;
	}

	private void getFartherDirection(Vector3f direction, final Vector3f targetVector, final float scale) {
		so.getPosition().sub(target.getPosition(), v0);
		v0.normalize();

		direction.copy(targetVector);
		targetVector.scale(scale);
		so.getPosition().add(targetVector, v2);
		float d1s = v2.distanceSq(target.getPosition());

		targetVector.negate();
		so.getPosition().add(targetVector, v2);
		float d2s = v2.distanceSq(target.getPosition());

		if (d1s > d2s) {
			targetVector.negate();
		}
	}

	private void initiateBank() {
		if (target == null) {
			if (currentState.isEmpty()) {
				setState(AI_STATE_GLOBAL);
			} else {
				popState();
			}
			return;
		}
		getFartherDirection(so.getRightVector(), v1, 500);
		getFartherDirection(so.getUpVector(), v3, 500);
		v2.x = so.getPosition().x + v1.x + v3.x + 800 * v0.x;
		v2.y = so.getPosition().y + v1.y + v3.y + 800 * v0.y;
		v2.z = so.getPosition().z + v1.z + v3.z + 800 * v0.z;
		WayPoint wp1 = WayPoint.newWayPoint(v2, target.getRightVector());
		v0.scale(10000.0f);
		v2.add(v0);
		WayPoint wp2 = WayPoint.newWayPoint(v2, target.getUpVector());
		setWaypoints(wp1, wp2);
		pushState(AI_STATE_FLY_PATH);
	}

	private void initiateEvade() {
		if (pushIfNewState(AI_STATE_EVADE)) {
			if (target == null) {
				target = so.getProximity();
			}
		}
		so.getProximity().getPosition().sub(so.getPosition(), v0);
		v0.scale(0.5f);
		v0.add(so.getPosition());
		v0.copy(evadePosition);
		evadeRangeSq = (so.getProximity().getBoundingSphereRadiusSq() * 3.0f + so.getBoundingSphereRadiusSq() * 3.0f) * 18;
		pitchingOver = true;
	}

	private void pushState(String newState) {
		if (!currentState.isEmpty()) {
			sendAIMessage("EXIT");
		}
		switch (newState) {
			case AI_STATE_ATTACK:
				pushIfNewState(AI_STATE_ATTACK);
				break;
			case AI_STATE_LURKING:
				initiateBank();
				break;
			case AI_STATE_EVADE:
				initiateEvade();
				break;
			case AI_STATE_FLEE:
				pushIfNewState(AI_STATE_FLEE);
				break;
			case AI_STATE_FLY_STRAIGHT:
				pushIfNewState(AI_STATE_FLY_STRAIGHT);
				break;
			case AI_STATE_FLY_PATH:
				pushIfNewState(AI_STATE_FLY_PATH);
				break;
			case AI_STATE_GLOBAL:
				pushIfNewState(AI_STATE_GLOBAL);
				so.adjustSpeed(0);
				break;
			case AI_STATE_TRACK:
				pushIfNewState(AI_STATE_TRACK);
				break;
			case AI_STATE_MISSILE_TRACK:
				target = so.getTarget();
				initiateMissileTrack();
				break;
			case AI_STATE_FOLLOW_CURVE:
				initiateFollowCurve();
				break;
		}
		stateName = newState;
		sendAIMessage("ENTER");
	}

	final void setState(String newState) {
		if (AI_STATE_ATTACK.equals(newState)) {
			target = Alite.get().getInGame().getShip();
		}
		currentState.clear();
		pushState(newState);
	}

	private void popState() {
		if (currentState.isEmpty()) {
			return;
		}
		currentState.pop();
		String state = getState();
		if (AI_STATE_FOLLOW_CURVE.equals(state)) {
			// Make sure that an interrupted "follow curve" is not resumed.
			popState();
		}
	}

	final void update(float deltaTime) {
		if (currentState.isEmpty() || so.hasEjected()) {
			return;
		}
		so.updateSpeed(deltaTime);

//		if (!updateTimer.hasPassedSeconds(pauseAI)) {
//			return;
//		}
//		deltaTime+= pauseAI;
		timeSpent = deltaTime;
		executeAIMessage("UPDATE");

		switch (currentState.peek()) {
			case AI_STATE_ATTACK:
				attackObject(deltaTime);
				break;
			case AI_STATE_LURKING:
				AliteLog.e("Updating lurking state", "This should not happen...");
				break;
			case AI_STATE_EVADE:
				updateEvade(deltaTime);
				break;
			case AI_STATE_FLEE:
				fleeObject(deltaTime);
				break;
			case AI_STATE_FLY_STRAIGHT:
				avoidCollision();
				break;
			case AI_STATE_FLY_PATH:
				flyPath(deltaTime);
				break;
			case AI_STATE_GLOBAL:
				break;
			case AI_STATE_TRACK:
				updateTrack(deltaTime);
				break;
			case AI_STATE_FOLLOW_CURVE:
				followCurve();
				break;
			default:
				break;
		}
		if (Settings.VIS_DEBUG) {
			if (so.isPlayer()) {
				String sl;
				switch (currentState.peek()) {
				case AI_STATE_ATTACK:       sl = "AT"; break;
				case AI_STATE_LURKING:            sl = "BN"; break;
				case AI_STATE_EVADE:        sl = "EV"; break;
				case AI_STATE_FLEE:         sl = "FL"; break;
				case AI_STATE_FLY_STRAIGHT: sl = "FS"; break;
				case AI_STATE_FLY_PATH:     sl = "FP"; break;
				case AI_STATE_GLOBAL:       sl = "ID"; break;
				case AI_STATE_TRACK:        sl = "TR"; break;
				case AI_STATE_FOLLOW_CURVE: sl = "FC"; break;
				default:           sl = "DE"; break;
				}
				AliteLog.d("AIS", "SOPATH: Player " + sl + " (" + so.getPosition().x + ":" + so.getPosition().y + ":" + so.getPosition().z +
					   ":" + so.getForwardVector().x + ":" + so.getForwardVector().y + ":" + so.getForwardVector().z +
					   ":" + so.getUpVector().x + ":" + so.getUpVector().y + ":" + so.getUpVector().z +
					   ":" + so.getRightVector().x + ":" + so.getRightVector().y + ":" + so.getRightVector().z +
					   ")");
			}
		}
	}

	String getState() {
		if (currentState.isEmpty()) {
			return null;
		}
		return currentState.peek();
	}

	String getStateStack() {
		String stack = "";
		for (String aCurrentState : currentState) {
			stack += aCurrentState + ", ";
		}
		return stack;
	}

	private void bankOrAttack(SpaceObject player) {
		String state = getState();
		AliteLog.d("Object has been hit", "Object has been hit. Current State == " + state);
		if (AI_STATE_FOLLOW_CURVE.equals(state)) {
			return;
		}
		if (AI_STATE_LURKING.equals(state) || AI_STATE_EVADE.equals(state) || AI_STATE_FLY_PATH.equals(state)) {
			float f = (float) Math.random();
			if (f < 0.3) {
				// Do nothing...
				AliteLog.d("NPC got Hit", "On Hit (should be 'no change'): New AI Stack: " + getStateStack());
				return;
			}
			popState();
			target = player;
			if (f < 0.7) {
				so.getForwardVector().copy(v0);
				v0.x *= -Math.random() * 2 + 1;
				v0.y *= -Math.random() * 2 + 1;
				v0.z *= -Math.random() * 2 + 1;
				if (v0.isZeroVector()) {
					v0.x = 1;
					v0.y = 0;
					v0.z = 0;
				}
				v0.normalize();
				pushState(AI_STATE_ATTACK);
				setWaypoints(WayPoint.newWayPoint(MathHelper.getRandomPosition(so.getPosition(), v0,
					5000, 1000), so.getUpVector()));
				pushState(AI_STATE_FLY_PATH);
				AliteLog.d("NPC got Hit", "On Hit (should be fly path): New AI Stack: " + getStateStack());
				return;
			}
			pushState(AI_STATE_ATTACK);
			pushState(AI_STATE_FOLLOW_CURVE);
			AliteLog.d("NPC got Hit", "On Hit (should be follow curve): New AI Stack: " + getStateStack());
		} else if (!AI_STATE_ATTACK.equals(state)) {
			target = player;
			pushState(AI_STATE_LURKING);
		}
	}

	private void flee(SpaceObject player) {
		if (AI_STATE_FLEE.equals(getState())) {
			return;
		}
		if (Math.random() * 100 < FIRE_MISSILE_UPON_FIRST_HIT_PROBABILITY) {
			so.spawnMissile(player);
		}
		target = player;
		setState(AI_STATE_FLEE);
	}

	private void fleeBankOrAttack(SpaceObject player) {
		String state = getState();
		if (AI_STATE_ATTACK.equals(state)) {
			bankOrAttack(player);
		} else if (AI_STATE_FLEE.equals(state)) {
			target = player;
			pushState(AI_STATE_LURKING);
		} else if (AI_STATE_LURKING.equals(state) || AI_STATE_EVADE.equals(state)) {
			popState();
			target = player;
			pushState(AI_STATE_LURKING);
		} else if (AI_STATE_FLY_PATH.equals(state) || AI_STATE_FLY_STRAIGHT.equals(state) || AI_STATE_GLOBAL.equals(state)) {
			if (Math.random() * 50 < so.getAggressionLevel()) {
				bankOrAttack(player);
			} else {
				flee(player);
			}
		}
		// Else do nothing...
	}

	void executeHit(SpaceObject player) {
		switch (so.getType()) {
			case Asteroid:
			case CargoPod:
			case Buoy:
			case Alloy:
			case Missile: break; // Nothing to do

			case EscapeCapsule:
			case Shuttle: flee(player); break;
			case Trader: fleeBankOrAttack(player); break;

			// EnemyShip (Pirate, Constrictor, Cougar, TieFighter, Defender, Thargoid, Thargon, Viper)
			default: bankOrAttack(player); break;
		}
	}

	void sendAIMessage(String message) {
		if (priorityMessages.contains(message)) {
			executeAIMessage(message);
		} else {
			queueAIMessage(message);
		}
	}

	private void executeAIMessage(String message) {
//		if (message.equals(lastMessage)) {
//			return;
//		}
//		lastMessage = message;

//		AliteLog.d("AI message received", "Object: " + so.getId() +
//			", AI: '" + so.getAIType() + "', state: '" + stateName + "', message: '" + message + "'");
		List<AIMethod> methods = SpaceObjectFactory.getInstance().getMethods(so.getAIType(), stateName, message);
		if (methods == null) {
			if (!SpaceObjectFactory.getInstance().isAIType(so.getAIType())) {
//				AliteLog.e("AI type error", "AI type " + so.getAIType() + " not found.");
				return;
			}
			if (!SpaceObjectFactory.getInstance().isAIState(so.getAIType(), stateName)) {
//				AliteLog.e("AI state error", "AI state " + stateName + " not found for AI type " + so.getAIType() + ".");
				return;
			}
//			AliteLog.d("AI methods", "No method for state.");
			return;
		}
		for (AIMethod method : methods) {
			try {
				SpaceObjectAI.class.getDeclaredMethod(method.getName(), String[].class).invoke(this,
					new Object[] { method.getParameter() == null ? new String[0] : method.getParameter().split(" ")});
				AliteLog.d("AI methods", "AI method " + method.getName() + "(" +
					(method.getParameter() == null ? "" : method.getParameter()) + ") called.");
			} catch (IllegalAccessException | NoSuchMethodException e) {
				AliteLog.e("AI methods", "No AI method " + method.getName() + " found.", e);
			} catch (InvocationTargetException e) {
				AliteLog.e("ai.syntax." + method.getName(), e.getCause().getMessage(), e.getCause());
			}
		}
	}

	private void queueAIMessage(String message) {
		if (message.equals(lastMessage)) {
			return;
		}
		lastMessage = message;
		// push to message queue executed by update message
	}

	private void checkArguments(int expectedArgNum, String expectedArgs, String... args) {
		if (expectedArgNum == args.length) {
			return;
		}
		throw new IllegalArgumentException("Wrong number of arguments. Expected " + expectedArgNum +
			(expectedArgs == null ? "" : " (" + expectedArgs + ")") +
			", got " + args.length + " " + Arrays.toString(args));
	}

	// Allowed AI methods for ships called by sendAIMessage.

	@SuppressWarnings("unused")
	private void setStateTo(String... state) {
		checkArguments(1, "{state_name}", state);
		setState(state[0]);
	}

	@SuppressWarnings("unused")
	private void setAITo(String... ai) {
		checkArguments(1, "{ai_name}", ai);
		// todo: push ai to aiStack
	}

	@SuppressWarnings("unused")
	private void switchAITo(String... ai) {
		checkArguments(1, "{ai_name}", ai);
		// todo: clear aiStack
		setAITo(ai);
	}

	@SuppressWarnings("unused")
	private void exitAI(String... message) {
		checkArguments(0, "", message);
		exitAIWithMessage("RESTARTED");
	}

	@SuppressWarnings("unused")
	private void exitAIWithMessage(String... message) {
		checkArguments(1, "{exit_message}", message);
		// todo: aiStack is needed instead of state stack!
		if (!currentState.empty()) {
			popState();
			sendAIMessage(message[0] == null ? "RESTARTED" : message[0]);
		}
	}

	@SuppressWarnings("unused")
	private void pauseAI(String... intervalString) {
		checkArguments(1, "{delay_time_in_sec}", intervalString);
//		pauseAI = Float.parseFloat(intervalString[0]);
	}

	@SuppressWarnings("unused")
	private void randomPauseAI(String... intervalString) {
		checkArguments(2, "{min_delay_time_in_sec} {max_delay_time_in_sec}", intervalString);
		float start = Float.parseFloat(intervalString[0]);
		float end = Float.parseFloat(intervalString[1]);
		if (start < 0 || end < 0 || end <= start) {
			throw new IllegalArgumentException("Invalid value: " + Arrays.toString(intervalString));
		}
//		pauseAI = (float) (start + (end - start) * Math.random());
	}

	@SuppressWarnings("unused")
	private void dropMessages(String... messageString) {
		if (messageString.length == 0) {
			throw new IllegalArgumentException("Missing argument, at least one message name must be passed.");
		}
		for (String message : messageString) {
			// todo: drop message
		}
	}

	@SuppressWarnings("unused")
	private void performAvoidCollision(String... args) {
		updateEvade(timeSpent);
	}

	@SuppressWarnings("unused")
	private void performAttack(String... args) {
		attackObject(timeSpent);
	}

	@SuppressWarnings("unused")
	private void debugDumpPendingMessages(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationToCurrentLocation(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDesiredRangeTo(String... rangeString) {
	}

	@SuppressWarnings("unused")
	private void setDesiredRangeForWaypoint(String... args) {
	}

	@SuppressWarnings("unused")
	private void performIntercept(String... args) {
		checkArguments(0, "", args);
		if (!isTargetExist()) {
			sendAIMessage("TARGET_LOST");
		}
		if (target.getPosition().distanceSq(so.getPosition()) <= 40000) {
			sendAIMessage("DESIRED_RANGE_ACHIEVED");
		}
		// todo send FRUSTRATED after 10 sec
	}

	@SuppressWarnings("unused")
	private void performFlyToRangeFromDestination(String... args) {
		checkArguments(0, "", args);
		if (target.getPosition().distanceSq(so.getPosition()) <= 40000) {
			sendAIMessage("DESIRED_RANGE_ACHIEVED");
		}
	}

	private boolean isTargetExist() {
		return target != null && !target.mustBeRemoved() && target.getHullStrength() > 0;
	}

	@SuppressWarnings("unused")
	private void setSpeedTo(String... speedString) {
	}

	@SuppressWarnings("unused")
	private void setSpeedFactorTo(String... speedString) {
		checkArguments(1, "{percentage_of_max_speed(0-1)}", speedString);
		float speed = Float.parseFloat(speedString[0]);
		if (speed >= 0 && speed <= 1) {
			so.setSpeed(-so.getMaxSpeed() * speed);
		}
	}

	@SuppressWarnings("unused")
	private void setSpeedToCruiseSpeed(String... args) {
	}

	@SuppressWarnings("unused")
	private void setThrustFactorTo(String... thrustFactorString) {
	}

	@SuppressWarnings("unused")
	private void setTargetToPrimaryAggressor(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestMerchantman(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForRandomMerchantman(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForLoot(String... args) {
		checkArguments(0, "", args);
		if (ObjectType.isSpaceStation(so.getType()) || so.getRepoHandler().getNumericProperty(SpaceObject.Property.has_scoop) < 1) {
			sendAIMessage("NOTHING_FOUND");
			return;
		}
		if (Alite.get().getInGame().getObjects(ObjectType.EscapeCapsule).isEmpty()) {
			List<SpaceObject> cargo = Alite.get().getInGame().getObjects(ObjectType.CargoPod);
			if (cargo.isEmpty()) {
				sendAIMessage("NOTHING_FOUND");
				return;
			}
			if (so.getType() == ObjectType.Police) {
				boolean slaveFound = false;
				for (SpaceObject c : cargo) {
					if (c.getCargoContent() == TradeGoodStore.get().getGoodById(TradeGoodStore.SLAVES)) {
						slaveFound = true;
						break;
					}
				}
				if (!slaveFound) {
					sendAIMessage("NOTHING_FOUND");
				}
				return;
			}
		}
		sendAIMessage(so.hasFreeSpace() ? "TARGET_FOUND" : "HOLD_FULL");
	}

	@SuppressWarnings("unused")
	private void scanForRandomLoot(String... args) {
	}

	@SuppressWarnings("unused")
	private void setTargetToFoundTarget(String... args) {
		//
	}

	@SuppressWarnings("unused")
	private void checkForFullHold(String... args) {
	}

	@SuppressWarnings("unused")
	private void getWitchspaceEntryCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationFromCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void setCoordinatesFromPosition(String... args) {
		checkArguments(0, "", args);
	}

	@SuppressWarnings("unused")
	private void fightOrFleeMissile(String... args) {
		checkArguments(0, "", args);
		flee(Alite.get().getInGame().getShip());
	}

	@SuppressWarnings("unused")
	private void fightOrFleeHostiles(String... args) {
	}

	@SuppressWarnings("unused")
	private void setCourseToPlanet(String... args) {
	}

	@SuppressWarnings("unused")
	private void setTakeOffFromPlanet(String... args) {
	}

	@SuppressWarnings("unused")
	private void landOnPlanet(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkTargetLegalStatus(String... args) {
		checkArguments(0, "", args);
		switch (Alite.get().getPlayer().getLegalStatus()) {
			case CLEAN:
				sendAIMessage(Alite.get().getPlayer().getLegalValue() == 0 ? "TARGET_CLEAN" : "TARGET_MINOR_OFFENDER");
			case OFFENDER:
				sendAIMessage("TARGET_OFFENDER");
			case FUGITIVE:
				sendAIMessage("TARGET_FUGITIVE");
		}
	}

	@SuppressWarnings("unused")
	private void checkOwnLegalStatus(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationToTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationWithinTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkCourseToDestination(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkAegis(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkEnergy(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkHeatInsulation(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForOffenders(String... args) {
	}

	@SuppressWarnings("unused")
	private void setCourseToWitchpoint(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationToWitchpoint(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationToStationBeacon(String... args) {
	}

	@SuppressWarnings("unused")
	private void performHyperSpaceExit(String... args) {
	}

	@SuppressWarnings("unused")
	private void performHyperSpaceExitWithoutReplacing(String... args) {
	}

	@SuppressWarnings("unused")
	private void wormholeGroup(String... args) {
	}

	@SuppressWarnings("unused")
	private void commsMessage(String... valueString) {
	}

	@SuppressWarnings("unused")
	private void commsMessageByUnpiloted(String... valueString) {
	}

	@SuppressWarnings("unused")
	private void ejectCargo(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForThargoid(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForNonThargoid(String... args) {
	}

	@SuppressWarnings("unused")
	private void thargonCheckMother(String... args) {
	}

	@SuppressWarnings("unused")
	private void becomeUncontrolledThargon(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkDistanceTravelled(String... args) {
		if (isTargetExist() && target.getPosition().distanceSq(so.getPosition()) > 90000000) {
			sendAIMessage("GONE_BEYOND_RANGE");
		}
	}

	@SuppressWarnings("unused")
	private void suggestEscort(String... args) {
	}

	@SuppressWarnings("unused")
	private void escortCheckMother(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkGroupOddsVersusTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForFormationLeader(String... args) {
	}

	@SuppressWarnings("unused")
	private void messageMother(String... msgString) {
	}

	@SuppressWarnings("unused")
	private void setPlanetPatrolCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void setSunSkimStartCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void setSunSkimEndCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void setSunSkimExitCoordinates(String... args) {
	}

	@SuppressWarnings("unused")
	private void patrolReportIn(String... args) {
	}

	@SuppressWarnings("unused")
	private void checkForMotherStation(String... args) {
	}

	@SuppressWarnings("unused")
	private void sendTargetCommsMessage(String... message) {
	}

	@SuppressWarnings("unused")
	private void markTargetForFines(String... args) {
	}

	@SuppressWarnings("unused")
	private void markTargetForOffence(String... args) {
		checkArguments(0, "", args);
		Alite.get().getPlayer().setLegalValue(Alite.get().getPlayer().getLegalValue() | 15);
	}

	@SuppressWarnings("unused")
	private void storeTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void recallStoredTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void scanForRocks(String... args) {
	}

	@SuppressWarnings("unused")
	private void setDestinationToDockingAbort(String... args) {
	}

	@SuppressWarnings("unused")
	private void requestNewTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void rollD(String... die_number) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithPrimaryRole(String... scanRole) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipHavingRole(String... scanRole) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithAnyPrimaryRole(String... scanRoles) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipHavingAnyRole(String... scanRoles) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithScanClass(String... scanScanClass) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithoutPrimaryRole(String... scanRole) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipNotHavingRole(String... scanRole) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithoutAnyPrimaryRole(String... scanRoles) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipNotHavingAnyRole(String... scanRoles) {
	}

	@SuppressWarnings("unused")
	private void scanForNearestShipWithoutScanClass(String... scanScanClass) {
	}

	@SuppressWarnings("unused")
	private void setCoordinates(String... systemXYZ) {
	}

	@SuppressWarnings("unused")
	private void checkForNormalSpace(String... args) {
	}

	@SuppressWarnings("unused")
	private void setTargetToRandomStation(String... args) {
	}

	@SuppressWarnings("unused")
	private void setTargetToLastStation(String... args) {
	}

	@SuppressWarnings("unused")
	private void addFuel(String... fuel_number) {
	}

	@SuppressWarnings("unused")
	private void scriptActionOnTarget(String... action) {
	}

	@SuppressWarnings("unused")
	private void sendScriptMessage(String... message) {
	}

	@SuppressWarnings("unused")
	private void ai_throwSparks(String... args) {
	}

	@SuppressWarnings("unused")
	private void explodeSelf(String... args) {
	}

	@SuppressWarnings("unused")
	private void ai_debugMessage(String... message) {
	}

	@SuppressWarnings("unused")
	private void targetFirstBeaconWithCode(String... code) {
	}

	@SuppressWarnings("unused")
	private void targetNextBeaconWithCode(String... code) {
	}

	@SuppressWarnings("unused")
	private void setRacepointsFromTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void performFlyRacepoints(String... args) {
	}

	@SuppressWarnings("unused")
	private void addPrimaryAggressorAsDefenseTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void addFoundTargetAsDefenseTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void findNewDefenseTarget(String... args) {
	}

	@SuppressWarnings("unused")
	private void fireECM(String... args) {
		//
	}

	// Allowed AI methods for stations called by sendAIMessage.
	@SuppressWarnings("unused")
	private void increaseAlertLevel(String... args) {
		checkArguments(0, "", args);
		Alite.get().getPlayer().increaseAlertLevel();
	}

	@SuppressWarnings("unused")
	private void decreaseAlertLevel(String... args) {
		checkArguments(0, "", args);
		Alite.get().getPlayer().decreaseAlertLevel();
	}

	@SuppressWarnings("unused")
	private void launchPolice(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchDefenseShip(String... args) {
		//
	}

	@SuppressWarnings("unused")
	private void launchScavenger(String... args) {
		//
	}

	@SuppressWarnings("unused")
	private void launchMiner(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchPirateShip(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchShuttle(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchTrader(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchEscort(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchPatrol(String... args) {
	}

	@SuppressWarnings("unused")
	private void launchShipWithRole(String... role) {
	}

	@SuppressWarnings("unused")
	private void abortAllDockings(String... args) {
	}

	@SuppressWarnings("unused")
	private void performTumble(String... args) {
		checkArguments(0, "", args);
		Alite.get().getInGame().getSpawnManager().spawnTumbleObject(so, so.getPosition());
	}
}
