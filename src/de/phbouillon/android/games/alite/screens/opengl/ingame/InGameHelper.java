package de.phbouillon.android.games.alite.screens.opengl.ingame;

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
import java.io.Serializable;
import java.util.List;

import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.Condition;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.statistics.WeaponType;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.StatusScreen;
import de.phbouillon.android.games.alite.screens.opengl.objects.AliteObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectAI;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;

class InGameHelper implements Serializable {
	private static final long serialVersionUID = 9018204797190210420L;
	// 1000 * 1000 * 3 is approximately the radius (squared) of a space station; add 1.000.000 (= 1000m) as a safety margin;
	private static final float STATION_PROXIMITY_DISTANCE_SQ = 4000000.0f;
	static final float STATION_VESSEL_PROXIMITY_DISTANCE_SQ = 8000000.0f;
	private static final float PROXIMITY_WARNING_RADIUS_FACTOR = 18.0f;

	private static final int DAMAGE_CARGO_COLLISION = 10;
	private static final int DAMAGE_STATION_COLLISION = 5;
	private static final int DAMAGE_OBJECT_COLLISION = 20;
	private static final int DAMAGE_MISSILE = 40;

	private final InGameManager inGame;
	private final Vector3f tempVector;
	private float fuelScoopFuel = 0.0f;
	private final Timer lastMissileWarning = new Timer().setAutoResetWithImmediateAtFirstCall();
	private final AttackTraverser attackTraverser;
	private transient ScoopCallback scoopCallback = null;
	private boolean cabinTemperatureAlarm;

	InGameHelper(InGameManager inGame) {
		this.inGame = inGame;
		tempVector = new Vector3f(0, 0, 0);
		attackTraverser = new AttackTraverser();
	}

	void setScoopCallback(ScoopCallback callback) {
		scoopCallback = callback;
	}

	ScoopCallback getScoopCallback() {
		return scoopCallback;
	}

	private void ramCargo(SpaceObject rammedObject) {
		rammedObject.applyDamage(4000);
		inGame.getLaserManager().collisionWithPlayerShip(rammedObject, DAMAGE_CARGO_COLLISION, true);
	}

	void checkShipStationProximity() {
		SpaceObject ship = inGame.getShip();
		SpaceObject station = inGame.getStation();
		if (ship == null || station == null) {
			return;
		}
		float distanceSq = ship.getPosition().distanceSq(station.getPosition());
		if (distanceSq <= STATION_PROXIMITY_DISTANCE_SQ) {
			if (ship.getProximity() != station) {
				ship.setProximity(station);
				AliteLog.d("Proximity", "Setting ship/station proximity");
			}
		} else {
			if (ship.getProximity() == station) {
				ship.setProximity(null);
			}
		}
	}

	void checkProximity(List <AliteObject> allObjects) {
		int n = allObjects.size();
		if (n < 2) {
			return;
		}
		for (int i = 0; i < n - 1; i++) {
			if (!(allObjects.get(i) instanceof SpaceObject)) {
				continue;
			}
			SpaceObject objectA = (SpaceObject) allObjects.get(i);
			float objectAProximityDistance = objectA.getBoundingSphereRadiusSq() * PROXIMITY_WARNING_RADIUS_FACTOR;
			float distanceCamSq = objectA.getPosition().distanceSq(inGame.getShip().getPosition());
			if (distanceCamSq <= objectAProximityDistance) {
				objectA.setProximity(inGame.getShip());
			}
			for (int j = i + 1; j < n; j++) {
				if (!(allObjects.get(j) instanceof SpaceObject)) {
					continue;
				}
				SpaceObject objectB = (SpaceObject) allObjects.get(j);
				float distanceSq = objectA.getPosition().distanceSq(objectB.getPosition());
				float objectBProximityDistance = objectB.getBoundingSphereRadiusSq() * PROXIMITY_WARNING_RADIUS_FACTOR;
				if (distanceSq <= objectAProximityDistance + objectBProximityDistance) {
					objectA.setProximity(objectB);
					objectB.setProximity(objectA);
				}
				distanceCamSq = objectB.getPosition().distanceSq(inGame.getShip().getPosition());
				if (distanceCamSq <= objectBProximityDistance) {
					objectB.setProximity(inGame.getShip());
				}
				if (ObjectType.isSpaceStation(objectA.getType())) {
					float intersectionDistance = LaserManager.computeIntersectionDistance(objectB.getForwardVector(), objectB.getPosition(), objectA.getPosition(), 1000.0f, tempVector);
					float travelDistance = -objectB.getSpeed() * 3.0f;
					if (intersectionDistance > 0 && intersectionDistance < travelDistance) {
						objectB.setProximity(objectA);
					}
				}
				if (ObjectType.isSpaceStation(objectB.getType())) {
					float intersectionDistance = LaserManager.computeIntersectionDistance(objectA.getForwardVector(), objectA.getPosition(), objectB.getPosition(), 1000.0f, tempVector);
					float travelDistance = -objectA.getSpeed() * 3.0f;
					if (intersectionDistance > 0 && intersectionDistance < travelDistance) {
						objectA.setProximity(objectB);
					}
				}
			}
		}
	}

	private void scoop(SpaceObject cargo) {
		// Fuel scoop must be installed and cargo must be in the lower half of the screen...
		Alite alite  = Alite.get();
		if (!alite.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP)) || cargo.getDisplayMatrix()[13] >= 0) {
			ramCargo(cargo);
			if (scoopCallback != null) {
				scoopCallback.rammed(cargo);
			}
			return;
		}

		if (cargo.getType() == ObjectType.CargoPod && cargo.getSpecialCargoContent() != null) {
			cargo.applyDamage(4000);
			SoundManager.play(Assets.scooped);
			alite.getCobra().addEquipment(cargo.getSpecialCargoContent());
			inGame.getMessage().setText(cargo.getSpecialCargoContent().getName());
			if (scoopCallback != null) {
				scoopCallback.scooped(cargo);
			}
			return;
		}
		Weight quantity = cargo.getType() == ObjectType.CargoPod ? cargo.getCargoQuantity() : Weight.tonnes((int) (Math.random() * 3 + 1));
		if (alite.getCobra().getFreeCargo().compareTo(quantity) < 0) {
			ramCargo(cargo);
			if (scoopCallback != null) {
				scoopCallback.rammed(cargo);
			}
			inGame.getMessage().setText(L.string(R.string.msg_full_cargo));
			return;
		}
		cargo.applyDamage(4000);
		SoundManager.play(Assets.scooped);
		if (scoopCallback != null) {
			scoopCallback.scooped(cargo);
		}
		TradeGood scoopedTradeGood = cargo.getType() == ObjectType.CargoPod ? cargo.getCargoContent() :
			cargo.getType() == ObjectType.EscapeCapsule ? TradeGoodStore.get().getGoodById(TradeGoodStore.SLAVES) :
			cargo.getType() == ObjectType.Thargon ? TradeGoodStore.get().getGoodById(TradeGoodStore.ALIEN_ITEMS) :
				TradeGoodStore.get().getGoodById(TradeGoodStore.ALLOYS);
		long price = cargo.getType() == ObjectType.CargoPod ? cargo.getCargoPrice() : 0;
		if (scoopedTradeGood == null) {
			AliteLog.e("Scooped null cargo!", "Scooped null cargo!");
			scoopedTradeGood = TradeGoodStore.get().getRandomTradeGoodForContainer();
		}
		AliteLog.d("Scooped Cargo", "Scooped Cargo " + scoopedTradeGood.getName());
		if (alite.getCobra().getInventoryItemByGood(scoopedTradeGood) == null && cargo.getType() == ObjectType.CargoPod) {
			// This makes sure that if a player scoops his dropped cargo back up, the
			// gain/loss calculation stays accurate
			alite.getCobra().addTradeGood(scoopedTradeGood, quantity, price);
			if (price == 0) {
				alite.getCobra().addUnpunishedTradeGood(scoopedTradeGood, quantity);
			}
		} else {
			alite.getCobra().addTradeGood(scoopedTradeGood, quantity, 0);
			alite.getCobra().addUnpunishedTradeGood(scoopedTradeGood, quantity);
		}
		inGame.getMessage().setText(scoopedTradeGood.getName());
	}

	void automaticDockingSequence() {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		Alite alite  = Alite.get();
		alite.getPlayer().setCondition(Condition.DOCKED);
		SoundManager.stopAll();
		inGame.getMessage().clearRepetition();
		alite.getNavigationBar().setFlightMode(false);
		if (inGame.getPostDockingScreen() instanceof StatusScreen) {
			try {
				AliteLog.d("[ALITE]", "Performing autosave. [Docked]");
				alite.autoSave();
			} catch (IOException e) {
				AliteLog.e("[ALITE]", "Autosaving commander failed.", e);
			}
		}
		inGame.setNewScreen(inGame.getPostDockingScreen());
	}

	final void checkShipObjectCollision(List <AliteObject> allObjects) {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		for (AliteObject object: allObjects) {
			float distanceSq = inGame.computeDistanceSq(object, inGame.getShip());
			SpaceObject so = null;
			ObjectType objectType = null;
			if (object instanceof SpaceObject) {
				so = (SpaceObject) object;
				objectType = so.getType();
			}
			if (objectType == ObjectType.Thargoid) {
				if (distanceSq <= so.getSpawnDroneDistanceSq()) {
					so.setSpawnDroneDistanceSq(-1);
					inGame.getSpawnManager().spawnThargons(so);
				}
			}
			if (distanceSq >= 2500) {
				continue;
			}
			if (ObjectType.isSpaceStation(objectType) && inGame.getWitchSpace() == null) {
				if (inGame.getDockingComputerAI().checkForCorrectDockingAlignment(so, inGame.getShip())) {
					inGame.clearMissileLock();
					automaticDockingSequence();
				} else {
					inGame.getLaserManager().collisionWithPlayerShip(so, DAMAGE_STATION_COLLISION, so.getDisplayMatrix()[14] < 0);
					inGame.getShip().setSpeed(0.0f);
				}
			} else if (objectType == ObjectType.CargoPod ||
					so != null && so.isDrone() && !so.hasLivingMother() ||
					objectType == ObjectType.EscapeCapsule || objectType == ObjectType.Alloy) {
				scoop(so);
			} else if (objectType != ObjectType.Missile) {
				if (inGame.getWitchSpace() != null && (object.getId().equals("Planet") ||
						object.getId().equals("Sun") ||
					ObjectType.isSpaceStation(objectType) ||
						object.getId().equals("Glow"))) {
					continue;
				}
				if (so != null && so.getHullStrength() > 0) {
					AliteLog.d("Crash Occurred", object.getId() + " crashed into player. " + so.getCurrentAIStack());
					inGame.getLaserManager().collisionWithPlayerShip(so, DAMAGE_OBJECT_COLLISION, so.getDisplayMatrix()[14] < 0);
					so.setHullStrength(0);
					object.setRemove(true);
					inGame.computeBounty(so);
					inGame.getLaserManager().explode(so, WeaponType.Collision);
				}
				 else {
					SoundManager.play(Assets.hullDamage);
				}
			}
		}
	}

	void launchEscapeCapsule(SpaceObject source) {
		SoundManager.play(Assets.retroRocketsOrEscapeCapsuleFired);
		SpaceObject esc = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.EscapeCapsule);

		source.getUpVector().copy(tempVector);
		esc.setForwardVector(tempVector);
		esc.setRightVector(source.getRightVector());
		source.getForwardVector().copy(tempVector);
		tempVector.negate();
		esc.setUpVector(tempVector);

		source.getPosition().copy(tempVector);
		esc.setPosition(tempVector);
		esc.setSpeed(-esc.getMaxSpeed());
		inGame.addObject(esc);
	}

	SpaceObject spawnMissile(SpaceObject source, SpaceObject target) {
		Vector3f shipPos = source.getPosition();
		if (inGame.getViewDirection() == PlayerCobra.DIR_FRONT || source != inGame.getShip()) {
			source.getForwardVector().copy(tempVector);
		} else if (inGame.getViewDirection() == PlayerCobra.DIR_RIGHT) {
			source.getRightVector().copy(tempVector);
			tempVector.negate();
		} else if (inGame.getViewDirection() == PlayerCobra.DIR_REAR) {
			source.getForwardVector().copy(tempVector);
			tempVector.negate();
		} else if (inGame.getViewDirection() == PlayerCobra.DIR_LEFT) {
			source.getRightVector().copy(tempVector);
		}

		float x = shipPos.x + tempVector.x * -1000f;
		float y = shipPos.y + tempVector.y * -1000f;
		float z = shipPos.z + tempVector.z * -10f;
		SpaceObject missile = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.Missile);
		missile.setPosition(x, y, z);
		missile.orientTowards(x + tempVector.x * -1000.0f,
							  y + tempVector.y * -1000.0f,
							  z + tempVector.z * -1000.0f, source.getUpVector().x, source.getUpVector().y, source.getUpVector().z);
		missile.setSpeed(-missile.getMaxSpeed());
		missile.setVisible(true);
		missile.setTarget(target);
		missile.setSource(source);
		if (source == inGame.getShip()) {
			// Check if target has ECM; then the missile's fate will be
			// decided here: If the player is close enough (< 1000m (1000 * 1000 = 1000000)),
			// he'll have a small chance to get through (10%).
			if (target.hasEcm() && (target.getPosition().distanceSq(source.getPosition()) >= 1000000 || Math.random() > 0.1)) {
				missile.setWillBeDestroyedByECM();
			}
			if (ObjectType.isSpaceStation(target.getType()) ||
					target.getType() == ObjectType.Shuttle || target.getType() == ObjectType.Trader) {
				Alite.get().getPlayer().computeLegalStatusAtLaunchMissile();
			}
		}
		missile.setAIState(SpaceObjectAI.AI_STATE_MISSILE_TRACK, target);
		inGame.addObject(missile);
		return missile;
	}

	void handleMissileUpdate(SpaceObject missile, float deltaTime) {
		SpaceObject target = missile.getTarget();
		// And track target object...
		if (target == inGame.getShip() && lastMissileWarning.hasPassedSeconds(2)) {
			SoundManager.play(Assets.com_incomingMissile);
		}
		if (missile.getHullStrength() <= 0) {
			return;
		}

		if (target == null || target.mustBeRemoved() || target.getHullStrength() <= 0) {
			inGame.setMessage(L.string(R.string.msg_target_lost));
			missile.setHullStrength(0);
			inGame.getLaserManager().explode(missile, WeaponType.PulseLaser);
			return;
		}

		boolean willBeDestroyedByECM = missile.getWillBeDestroyedByECM();
		missile.update(deltaTime);
		if (willBeDestroyedByECM && missile.getPosition().distanceSq(target.getPosition()) < 40000 && !inGame.getShip().isEcmJammer()) {
			missile.setHullStrength(0);
			inGame.getLaserManager().explode(missile, WeaponType.ECM);
			SoundManager.play(Assets.ecm);
			if (inGame.getHud() != null) {
				inGame.getHud().showECM();
			}
			return;
		}

		float distance = LaserManager.computeIntersectionDistance(missile.getForwardVector(),
			missile.getPosition(), target.getPosition(), target.getBoundingSphereRadius(), tempVector);
		if (distance <= 0 || distance >= -missile.getSpeed() * deltaTime && distance >= 4000) {
			return;
		}

		missile.setHullStrength(0);
		inGame.getLaserManager().explode(missile, WeaponType.SelfDestruct);
		if (willBeDestroyedByECM && !inGame.getShip().isEcmJammer()) {
			SoundManager.play(Assets.ecm);
			if (inGame.getHud() != null) {
				inGame.getHud().showECM();
			}
			return;
		}

		if (target == inGame.getShip()) {
			inGame.getLaserManager().damageShip(DAMAGE_MISSILE, missile.getDisplayMatrix()[14] < 0);
			return;
		}

		target.setHullStrength(0);
		target.setRemove(true);
		inGame.computeBounty(target);
		inGame.getLaserManager().explode(target, WeaponType.Missile);
	}

	void checkAltitudeLowAlert() {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		Alite alite  = Alite.get();
		float altitude = alite.getCobra().getAltitude();
		if (altitude < 6 && !SoundManager.isPlaying(Assets.altitudeLow)) {
			SoundManager.repeat(Assets.altitudeLow);
			inGame.getMessage().repeatText(L.string(R.string.msg_altitude_low), 1);
		} else if (altitude >= 6 && SoundManager.isPlaying(Assets.altitudeLow)) {
			if (alite.getCobra().getEnergy() > PlayerCobra.MAX_ENERGY_BANK) {
				SoundManager.stop(Assets.altitudeLow);
				inGame.getMessage().clearRepetition();
			}
		}
		if (altitude < 2) {
			inGame.gameOver();
		}
	}

	void checkCabinTemperatureAlert(float deltaTime) {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		Alite alite  = Alite.get();
		int cabinTemperature = alite.getCobra().getCabinTemperature();
		if (cabinTemperature > 24) {
			if (!hasCabinTemperatureSound()) {
				if (cabinTemperatureAlarm) {
					SoundManager.repeat(Assets.temperatureHigh);
				} else {
					SoundManager.play(Assets.com_cabinTemperatureCritical);
					cabinTemperatureAlarm = true;
				}
				fuelScoopFuel = alite.getCobra().getFuel();
				inGame.getMessage().repeatText(L.string(R.string.com_cabin_temperature_critical), 1);
			}
		} else if (hasCabinTemperatureSound()) {
			if (alite.getCobra().getEnergy() > PlayerCobra.MAX_ENERGY_BANK) {
				SoundManager.stop(Assets.com_cabinTemperatureCritical);
				SoundManager.stop(Assets.temperatureHigh);
				inGame.getMessage().clearRepetition();
				cabinTemperatureAlarm = false;
			}
		}
		if (cabinTemperature > 26 && alite.getCobra().isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP)) &&
				alite.getCobra().getFuel() < PlayerCobra.MAX_FUEL) {
			inGame.getMessage().repeatText(L.string(R.string.msg_fuel_scoop_activated), 1);
			fuelScoopFuel += 20 * -inGame.getShip().getSpeed() / inGame.getShip().getMaxSpeed() * deltaTime;
			int newFuel = (int) fuelScoopFuel;
			if (newFuel > PlayerCobra.MAX_FUEL) {
				newFuel = (int) PlayerCobra.MAX_FUEL;
			}
			alite.getCobra().setFuel(newFuel);
			if (newFuel >= PlayerCobra.MAX_FUEL) {
				inGame.getMessage().repeatText(L.string(R.string.com_cabin_temperature_critical), 1);
			}
		}
		if (cabinTemperature > 28) {
			inGame.gameOver();
		}
	}

	private boolean hasCabinTemperatureSound() {
		return SoundManager.isPlaying(Assets.com_cabinTemperatureCritical) || SoundManager.isPlaying(Assets.temperatureHigh);
	}

	void updatePlayerCondition() {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		Alite alite  = Alite.get();
		if (alite.getCobra().getEnergy() < PlayerCobra.MAX_ENERGY_BANK ||
				alite.getCobra().getCabinTemperature() > 24 ||
				alite.getCobra().getAltitude() < 6 ||
				inGame.getWitchSpace() != null) {
			alite.getPlayer().setCondition(Condition.RED);
			return;
		}
		if (InGameManager.playerInSafeZone) {
			if (alite.getPlayer().getLegalStatus() == LegalStatus.CLEAN) {
				alite.getPlayer().setCondition(Condition.GREEN);
			} else {
				Condition conditionOld = alite.getPlayer().getCondition();
				alite.getPlayer().setCondition(inGame.getNumberOfObjects(ObjectType.Police) > 0 ? Condition.RED : Condition.YELLOW);
				Condition conditionNew = alite.getPlayer().getCondition();
				if (conditionOld != conditionNew && conditionNew == Condition.RED) {
					SoundManager.play(Assets.com_conditionRed);
					inGame.repeatMessage(L.string(R.string.com_condition_red), 3);
				}
			}
		} else {
			alite.getPlayer().setCondition(Condition.YELLOW);
			// Can refer to sortedObjectsToDraw here, because we're in update: PRE rendering, so the
			// list has not yet been emptied...
			inGame.traverseObjects(attackTraverser);
		}
	}
}
