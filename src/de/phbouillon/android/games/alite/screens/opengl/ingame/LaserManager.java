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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.opengl.GLES11;
import android.opengl.Matrix;
import android.os.Vibrator;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.Pool;
import de.phbouillon.android.framework.impl.Pool.PoolObjectFactory;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Assets;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.SoundManager;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.Laser;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.statistics.WeaponType;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.opengl.objects.AliteObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.Explosion;
import de.phbouillon.android.games.alite.screens.opengl.objects.LaserCylinder;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectAI;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.CargoCanister;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.ships.Platlet;
import de.phbouillon.android.games.alite.screens.opengl.sprites.AliteHud;

public class LaserManager implements Serializable {
	private static final long serialVersionUID = -3608347957484414304L;

	private static final long  NAVAL_REFRESH_RATE    = 898203592L;
	private static final long  NORMAL_REFRESH_RATE   = 1437125748L;
	public  static final float MAX_ENEMY_DISTANCE_SQ = 603979776; // (16384 + 8192) squared

	public  static final int   MAX_LASERS                       = 500;
	public  static final float LASER_SPEED                      = 12400.0f;
	public  static final float LASER_BEAM_SPEED                 = 124000.0f;
	private static final float DIST_FRONT                       = -10.0f;
	private static final float DIST_RIGHT                       = 30.0f;
	private static final float DIST_CONVERGE                    = 24000.0f;

	private transient Alite alite;
	private final Vector3f shotOrigin;
	private final Vector3f shotDirection;
	private final Vector3f laserRight;
	private final Vector3f laserForward;
	private final Timer lastLaserFireUp = new Timer().setAutoResetWithSkipFirstCall();
	private final Timer lastFrontWarning = new Timer().setAutoResetWithImmediateAtFirstCall();
	private final Timer lastRearWarning = new Timer().setAutoResetWithImmediateAtFirstCall();
	private boolean autoFire = false;
	private final Vector3f tempVector = new Vector3f(0, 0, 0);
	private final float [] tempVecArray = new float [] {0.0f, 0.0f, 0.0f, 0.0f};
	private final float [] tempVecArray2 = new float [] {0.0f, 0.0f, 0.0f, 0.0f};
	private final List <LaserCylinder> createdLasers = new ArrayList<>();
	private final List <Explosion> activeExplosions = new ArrayList<>();
	private InGameManager inGame;

	private final Timer lockTime = new Timer().setAutoResetWithImmediateAtFirstCall();

	class LaserCylinderFactory implements PoolObjectFactory <LaserCylinder> {
		private static final long serialVersionUID = -4204164060599078689L;

		@Override
		public LaserCylinder createObject() {
			LaserCylinder result = new LaserCylinder();
			result.setVisible(false);
			return result;
		}
	}

	private transient PoolObjectFactory <LaserCylinder> laserFactory = new LaserCylinderFactory();
	private transient Pool <LaserCylinder> laserPool = new Pool<>(laserFactory, MAX_LASERS);
	final List <LaserCylinder> activeLasers = new ArrayList<>();

	LaserManager(final Alite alite, final InGameManager inGame) {
		this.alite = alite;
		this.inGame = inGame;
		shotOrigin = new Vector3f(0, 0, 0);
		shotDirection = new Vector3f(0, 0, 0);
		laserRight = new Vector3f(0.0f, 0.0f, 0.0f);
		laserForward = new Vector3f(0.0f, 0.0f, 0.0f);
		alite.getTextureManager().addTexture("textures/lasers.png");
		alite.setLaserManager(this);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.e("readObject", "LaserManager.readObject");
			in.defaultReadObject();
			AliteLog.e("readObject", "LaserManager.readObject I");
			alite = Alite.get();
			laserFactory = new LaserCylinderFactory();
			laserPool = new Pool<>(laserFactory, MAX_LASERS);
			laserPool.reset();
			alite.setLaserManager(this);
			AliteLog.e("readObject", "LaserManager.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	static float computeIntersectionDistance(final Vector3f dir, final Vector3f origin, final Vector3f center, final float radius, final Vector3f tVec) {
		origin.sub(center, tVec);
		float ocsq  = tVec.lengthSq();
		float loc   = dir.dot(tVec);
		float docsq = loc * loc;
		float expUnderRoot = docsq - ocsq + radius * radius;
		if (expUnderRoot < 0) {
			return -1.0f;
		}
		float sqr = (float) Math.sqrt(expUnderRoot);
		float d = -loc;
		if (d - sqr > 0) {
			return d - sqr;
		}
		if (d + sqr > 0) {
			return d + sqr;
		}
		return -1.0f;
	}

	private void spawnPlatlets(final SpaceObject so, final WeaponType weaponType) {
		int platletCount = Math.random() < 0.9 ? 0 : 1;
		if (weaponType == WeaponType.MiningLaser) {
			platletCount = (int) (Math.random() * (so.getMaxCargoCanisters() + 1));
		}
		for (int i = 0; i < platletCount; i++) {
			final Platlet platlet = new Platlet(alite);
			inGame.getSpawnManager().spawnTumbleObject(platlet, so.getPosition());
		}
	}

	private void spawnCargoCanisters(final SpaceObject so, int forceCount, WeaponType weaponType) {
		AliteLog.d("Spawn Cargo Canisters", so.getName() + " has Cargo type: " + so.getCargoType() + " and spawns cargo canisters: " + so.spawnsCargoCanisters());
		if (so.getType() == ObjectType.Asteroid) {
			spawnPlatlets(so, weaponType);
			return;
		}
		if (so.getCargoType() == null || !so.spawnsCargoCanisters()) {
			// Legacy: The cargo type is still declared at a ship, but not longer used...
			// However, if it was defined that a ship has no cargo type, we still
			// exit here...
			return;
		}

		int numberOfCanistersToSpawn = forceCount > 0 ? forceCount : (int) (Math.random() * (so.getMaxCargoCanisters() + 1));
		for (int i = 0; i < numberOfCanistersToSpawn; i++) {
			final CargoCanister cargo = new CargoCanister(alite);
			TradeGood tradeGood = TradeGoodStore.get().getRandomTradeGoodForContainer();
			cargo.setContent(tradeGood, Weight.unit(tradeGood.getUnit(), (int) (Math.random() * 3 + 1)));
			inGame.getSpawnManager().spawnTumbleObject(cargo, so.getPosition());
		}
	}

	public void explode(SpaceObject so) {
		SoundManager.play(Assets.shipDestroyed);
		activeExplosions.add(new Explosion(alite, so, inGame));
	}

	void explode(SpaceObject so, WeaponType weaponType) {
		explode(so);
		spawnCargoCanisters(so, so.getCargoCanisterOverrideCount(), weaponType);
	}

	public final boolean isUnderCross(final SpaceObject object, final GraphicObject ship, final int viewDirection) {
		if (object.isCloaked()) {
			return false;
		}
		ship.getPosition().copy(shotOrigin);
		switch (viewDirection) {
			case PlayerCobra.DIR_FRONT: ship.getForwardVector().copy(shotDirection); shotDirection.negate(); break;
			case PlayerCobra.DIR_RIGHT: ship.getRightVector().copy(shotDirection); break;
			case PlayerCobra.DIR_REAR: ship.getForwardVector().copy(shotDirection); break;
			case PlayerCobra.DIR_LEFT: ship.getRightVector().copy(shotDirection); shotDirection.negate(); break;
		}
		shotDirection.normalize();
		float d = computeIntersectionDistance(shotDirection, shotOrigin, object.getPosition(), object.getBoundingSphereRadius(), tempVector);
		if (d > 0.0f && d < AliteHud.MAX_DISTANCE) {
			float distSq = object.getPosition().distanceSq(ship.getPosition());
			float scaleFactor = distSq <= SpaceObjectAI.SHOOT_DISTANCE_SQ ? 1.0f : 1.0f + 5.0f * ((distSq - SpaceObjectAI.SHOOT_DISTANCE_SQ) / MAX_ENEMY_DISTANCE_SQ);
			if (scaleFactor > 3.0f) {
				scaleFactor = 3.0f;
			} else if (scaleFactor < 1.0f) {
				scaleFactor = 1.0f;
			}
			return object.intersect(shotOrigin, shotDirection, scaleFactor);
		}
		return false;
	}

	private void loseCargo() {
		if (alite.getCobra().hasCargo()) {
			inGame.setMessage("Cargo lost.");
			alite.getCobra().clearInventory();
			// TODO add computer voice message
		}
	}

	private void loseEquipment() {
		List<Equipment> installedLosableEquipment = alite.getCobra().getInstalledLosableEquipment();
		if (installedLosableEquipment.isEmpty()) {
			return;
		}
		int i = (int) (Math.random() * installedLosableEquipment.size());
		Equipment lostEquip = installedLosableEquipment.get(i);
		inGame.setMessage(lostEquip.getShortName() + " lost.");
		alite.getCobra().removeEquipment(lostEquip);
		if (lostEquip == EquipmentStore.dockingComputer) {
			SoundManager.play(Assets.com_lostDockingComputer);
			inGame.getDockingComputerAI().disengage();
		} else if (lostEquip == EquipmentStore.ecmSystem) {
			SoundManager.play(Assets.com_lostEcm);
		} else if (lostEquip == EquipmentStore.fuelScoop) {
			SoundManager.play(Assets.com_lostFuelScoop);
		} else if (lostEquip == EquipmentStore.escapeCapsule) {
			SoundManager.play(Assets.com_lostEscapeCapsule);
		} else if (lostEquip == EquipmentStore.energyBomb) {
			SoundManager.play(Assets.com_lostEnergyBomb);
		} else if (lostEquip == EquipmentStore.extraEnergyUnit) {
			SoundManager.play(Assets.com_lostExtraEnergyUnit);
		} else if (lostEquip == EquipmentStore.galacticHyperdrive) {
			SoundManager.play(Assets.com_lostGalacticHyperdrive);
		} else if (lostEquip == EquipmentStore.retroRockets) {
			SoundManager.play(Assets.com_lostRetroRockets);
		}
	}

	void damageShip(int amount, boolean front) {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		SoundManager.play(Assets.hullDamage);
		int shield = front ? alite.getCobra().getFrontShield() : alite.getCobra().getRearShield();
		if (shield > 0) {
			if (shield >= amount) {
				shield -= amount;
				amount = 0;
			}
			else {
				amount -= shield;
				shield = 0;
			}
			if (front) {
				if (shield <= 0 && !lastFrontWarning.hasPassedSeconds(4)) {
					SoundManager.play(Assets.com_frontShieldHasFailed);
					inGame.setMessage("Front shield has failed");
				}
				alite.getCobra().setFrontShield(shield);
			} else {
				if (shield <= 0 && !lastRearWarning.hasPassedSeconds(4)) {
					SoundManager.play(Assets.com_aftShieldHasFailed);
					inGame.setMessage("Aft shield has failed");
				}
				alite.getCobra().setRearShield(shield);
			}
		}
		long vbLen = (long) (Settings.vibrateLevel * 30.0f);
		if (amount > 0) {
			int newVal = alite.getCobra().getEnergy() - amount;
			if (newVal <= 0) {
				alite.getCobra().setEnergy(0);
				inGame.gameOver();
				vbLen <<= 1;
			} else {
				alite.getCobra().setEnergy(newVal);
				checkEnergyLow();
				if (newVal <= 2 * PlayerCobra.MAX_ENERGY_BANK) {
					if (Math.random() * 256 < 20) {
						if (!inGame.getMessage().isActive()) {
							if (Math.random() * 20 < 1) {
								loseCargo();
							} else {
								loseEquipment();
							}
						}
					}
				}
			}
			vbLen <<= 1;
		}
		if (vbLen > 0) {
			Vibrator vb = (Vibrator) alite.getSystemService(Context.VIBRATOR_SERVICE);
			if (vb != null) {
				vb.vibrate(vbLen);
			}
		}
	}

	private void checkPlayerHit(final LaserCylinder laser, final float distanceToNextShot, final GraphicObject ship) {
		if (!inGame.isPlayerAlive() || Settings.invulnerable) {
			return;
		}
		// Fire at player, check for hit.
		float intersectionDistance = computeIntersectionDistance(shotDirection, shotOrigin, ship.getPosition(), 156, tempVector);
		if (intersectionDistance > 0 && intersectionDistance <= distanceToNextShot) {
			for (LaserCylinder lc: laser.getTwins()) {
				lc.setVisible(false);
				lc.clearTwins();
			}
			laser.setVisible(false);
			laser.clearTwins();
			damageShip(8, shotDirection.dot(ship.getForwardVector()) >= 0);
		}
	}

	private void checkObjectHit(final LaserCylinder laser, final float distanceToNextShot, final GraphicObject ship, List<AliteObject> allObjects) {
		for (AliteObject eo: allObjects) {
			if (!(eo instanceof SpaceObject)) {
				continue;
			}
			if (laser.getOrigin() == eo) {
				continue;
			}
			if (((SpaceObject) eo).isCloaked()) {
				continue;
			}
			float distSq = eo.getPosition().distanceSq(ship.getPosition());
			float scaleFactor = distSq <= SpaceObjectAI.SHOOT_DISTANCE_SQ ? 1.0f : 1.0f + 5.0f * ((distSq - SpaceObjectAI.SHOOT_DISTANCE_SQ) / MAX_ENEMY_DISTANCE_SQ);
			if (scaleFactor > 3.0f) {
				scaleFactor = 3.0f;
			} else if (scaleFactor < 1.0f) {
				scaleFactor = 1.0f;
			}
			float intersectionDistance = computeIntersectionDistance(shotDirection, shotOrigin, eo.getPosition(),
				eo.getBoundingSphereRadius() * scaleFactor, tempVector);
			if (intersectionDistance > 0 && intersectionDistance <= distanceToNextShot) {
				if (((SpaceObject) eo).intersect(shotOrigin, shotDirection, scaleFactor)) {
					// Make sure the laser is at least rendered once...
					laser.removeInNFrames(4);
					for (LaserCylinder lc: laser.getTwins()) {
						lc.removeInNFrames(4);
					}
					SoundManager.play(Assets.laserHit);
					if (((SpaceObject) eo).getType() != ObjectType.SpaceStation || "Alien Space Station".equals(eo.getName())) {
						// Space Stations are invulnerable --- in general ;)
						if (Settings.laserPowerOverride != 0) {
							alite.getPlayer().setCheater(true);
						}
						float hullStrength = ((SpaceObject) eo).applyDamage(laser.getLaser().getPower() + Settings.laserPowerOverride);
						if (hullStrength <= 0) {
							if (laser.getOrigin() == null) {
								// Player has destroyed something
								inGame.computeBounty((SpaceObject) eo);
							}
							explode((SpaceObject) eo, WeaponType.values()[laser.getLaser().getIndex()]);
							eo.setRemove(true);
						}
					}
					((SpaceObject) eo).executeHit((SpaceObject) ship);
				}
			}
		}
	}

	final void update(float deltaTime, final GraphicObject ship, List<AliteObject> allObjects) {
		Iterator<LaserCylinder> laserIterator = activeLasers.iterator();
		while (laserIterator.hasNext()) {
			LaserCylinder laser = laserIterator.next();
			if (laser.isVisible()) {
				laser.getPosition().copy(shotOrigin); // Origin of shot
				laser.moveForward(deltaTime, laser.getInitialDirection());
				Vector3f laserPos = laser.getPosition();
				laser.getPosition().copy(shotDirection);
				shotDirection.sub(shotOrigin);
				float distanceToNextShot = shotDirection.length();
				shotDirection.normalize(); // Direction of shot
				if (laser.getOrigin() != null && laser.getRemoveInNFrames() == -1) {
					checkPlayerHit(laser, distanceToNextShot, ship);
				}
				if (laser.getOrigin() == null && laser.getRemoveInNFrames() == -1) {
					checkObjectHit(laser, distanceToNextShot, ship, allObjects);
				}
				Vector3f shipPos  = ship.getPosition();
				float distanceSq = (laserPos.x - shipPos.x) * (laserPos.x - shipPos.x) +
					           (laserPos.y - shipPos.y) * (laserPos.y - shipPos.y) +
					           (laserPos.z - shipPos.z) * (laserPos.z - shipPos.z);
				if (distanceSq > 1936000000.0f) {
					laser.setVisible(false);
					laserPool.free(laser);
					laserIterator.remove();
				}
			} else {
				laserPool.free(laser);
				laserIterator.remove();
			}
		}
		Iterator<Explosion> explosionIterator = activeExplosions.iterator();
		while (explosionIterator.hasNext()) {
			Explosion ex = explosionIterator.next();
			ex.update();
			if (ex.isFinished())  {
				explosionIterator.remove();
			}
		}
	}

	public void fire(SpaceObject so, GraphicObject ship) {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		SoundManager.play(Assets.enemyFireLaser);
		createdLasers.clear();
		int max = so.getNumberOfLasers();
		for (int i = 0; i < max; i++) {
			int off = i == 0 ? -1 : i == 1 ? 1 : 0;
			if (max == 1) {
				off = 0;
			}
			tempVecArray[0] = so.getLaserX(i);
			tempVecArray[1] = so.getLaserY(i);
			tempVecArray[2] = so.getLaserZ(i);
			tempVecArray[3] = 1.0f;
			Matrix.multiplyMV(tempVecArray2, 0, so.getMatrix(), 0, tempVecArray, 0);
			ship.getForwardVector().scale(-Math.abs(ship.getSpeed()) - 80, tempVector);
			tempVector.add(ship.getPosition());
			float xd = tempVecArray2[0] - tempVector.x + off * DIST_RIGHT * so.getRightVector().x;
			float yd = tempVecArray2[1] - tempVector.y + off * DIST_RIGHT * so.getRightVector().y;
			float zd = tempVecArray2[2] - tempVector.z + off * DIST_RIGHT * so.getRightVector().z;
			LaserCylinder laser = laserPool.newObject();
			laser.reset();
			laser.setBeam(false);
			laser.setColor(so.getLaserColor());
			laser.setTexture(so.getLaserTexture(), alite);
			laser.clearTwins();
			laser.setPosition(tempVecArray2[0], tempVecArray2[1], tempVecArray2[2]);
			laser.setInitialDirection(xd, yd, zd);
			laser.setForwardVector(xd, yd, zd);
			so.getUpVector().cross(laser.getForwardVector(), tempVector);
			laser.setRightVector(tempVector);
			laser.setUpVector(so.getUpVector());
			laser.setVisible(true);
			laser.setLaser(EquipmentStore.militaryLaser);
			laser.setOrigin(so);
			laser.setAiming(false);
			activeLasers.add(laser);
			createdLasers.add(laser);
		}
		for (int i = 0; i < createdLasers.size(); i++) {
			for (int j = i + 1; j < createdLasers.size(); j++) {
				createdLasers.get(i).addTwin(createdLasers.get(j));
				createdLasers.get(j).addTwin(createdLasers.get(i));
			}
		}
	}

	private void computeLaserVectors(int viewDirection, GraphicObject ship) {
		if (viewDirection == PlayerCobra.DIR_FRONT) {
			ship.getRightVector().copy(laserRight);
			ship.getForwardVector().copy(laserForward);
		} else if (viewDirection == PlayerCobra.DIR_RIGHT) {
			ship.getRightVector().copy(laserForward);
			laserForward.negate();
			ship.getForwardVector().copy(laserRight);
		} else if (viewDirection == PlayerCobra.DIR_REAR) {
			ship.getForwardVector().copy(laserForward);
			laserForward.negate();
			ship.getRightVector().copy(laserRight);
			laserRight.negate();
		} else if (viewDirection == PlayerCobra.DIR_LEFT) {
			ship.getRightVector().copy(laserForward);
			ship.getForwardVector().copy(laserRight);
			laserRight.negate();
		}

		if (!Settings.laserDoesNotOverheat) {
			alite.getCobra().setLaserTemperature(alite.getCobra().getLaserTemperature() + 1);
		}
		if (alite.getCobra().getLaserTemperature() == 40 && lockTime.hasPassedSeconds(5)) {
			SoundManager.play(Assets.com_laserTemperatureCritical);
			inGame.setMessage("Laser temperature critical!");
		}
	}

	private LaserCylinder spawnPlayerLaserCylinder(Laser laser, GraphicObject ship, float x, float y, float z, float xd, float yd, float zd, boolean aiming) {
		LaserCylinder laserCylinder = laserPool.newObject();
		laserCylinder.reset();
		laserCylinder.setBeam(laser.isBeam());
		laserCylinder.setColor(laser.getColor());
		laserCylinder.setTexture(laser.getTexture(), alite);
		laserCylinder.clearTwins();
		laserCylinder.setOrigin(null);
		laserCylinder.setPosition(x, y, z);
		laserCylinder.setInitialDirection(xd, yd, zd);
		laserCylinder.setVisible(true);
		laserCylinder.setLaser(laser);
		laserCylinder.setAiming(aiming);
		laserCylinder.setForwardVector(laserCylinder.getInitialDirection());
		ship.getUpVector().cross(laserCylinder.getForwardVector(), tempVector);
		laserCylinder.setRightVector(tempVector);
		laserCylinder.setUpVector(ship.getUpVector());
		activeLasers.add(laserCylinder);

		return laserCylinder;
	}

	private void playerFireLaserCylinder(Laser laser, GraphicObject ship) {
		Vector3f shipPos = ship.getPosition();
		float x = shipPos.x + laserForward.x * DIST_FRONT + laserRight.x * DIST_RIGHT;
		float y = shipPos.y + laserForward.y * DIST_FRONT+ laserRight.y * DIST_RIGHT;
		float z = shipPos.z + laserForward.z * DIST_FRONT + laserRight.z * DIST_RIGHT;
		float xd = laserForward.x + laserRight.x * DIST_RIGHT / DIST_CONVERGE;
		float yd = laserForward.y + laserRight.y * DIST_RIGHT / DIST_CONVERGE;
		float zd = laserForward.z + laserRight.z * DIST_RIGHT / DIST_CONVERGE;
		LaserCylinder laser1 = spawnPlayerLaserCylinder(laser, ship, x, y, z, xd, yd, zd, false);

		x = shipPos.x + laserForward.x * DIST_FRONT - laserRight.x * DIST_RIGHT;
		y = shipPos.y + laserForward.y * DIST_FRONT - laserRight.y * DIST_RIGHT;
		z = shipPos.z + laserForward.z * DIST_FRONT - laserRight.z * DIST_RIGHT;
		xd = laserForward.x - laserRight.x * DIST_RIGHT / DIST_CONVERGE;
		yd = laserForward.y - laserRight.y * DIST_RIGHT / DIST_CONVERGE;
		zd = laserForward.z - laserRight.z * DIST_RIGHT / DIST_CONVERGE;
		LaserCylinder laser2 = spawnPlayerLaserCylinder(laser, ship, x, y, z, xd, yd, zd, false);

		x = shipPos.x + laserForward.x * DIST_FRONT;
		y = shipPos.y + laserForward.y * DIST_FRONT;
		z = shipPos.z + laserForward.z * DIST_FRONT;
		xd = laserForward.x;
		yd = laserForward.y;
		zd = laserForward.z;
		LaserCylinder laser3 = spawnPlayerLaserCylinder(laser, ship, x, y, z, xd, yd, zd, true);

		laser1.setTwins(laser2, laser3);
		laser2.setTwins(laser1, laser3);
		laser3.setTwins(laser1, laser2);
		SoundManager.play(Assets.fireLaser);
	}

	private void fire(int viewDirection, GraphicObject ship) {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		if (alite.getCobra().getLaserTemperature() >= PlayerCobra.MAX_LASER_TEMPERATURE) {
			return;
		}
		Laser laser = alite.getCobra().getLaser(viewDirection);
		if (laser == null || !laser.fire()) {
			return;
		}

		computeLaserVectors(viewDirection, ship);
		playerFireLaserCylinder(laser, ship);
	}

	final List <TimedEvent> registerTimedEvents() {
		List <TimedEvent> timedEvents = new ArrayList<>();
		timedEvents.add(new TimedEvent(NORMAL_REFRESH_RATE) { // Cool down laser and replenish energy banks...
			private static final long serialVersionUID = -7421881699858933872L;

			@Override
			public void doPerform() {
				long nd = (alite.getCobra().isEquipmentInstalled(EquipmentStore.navalEnergyUnit) ? NAVAL_REFRESH_RATE:NORMAL_REFRESH_RATE) / alite.getTimeFactor();
				if (delay != nd) {
					updateDelay(nd);
				}
				alite.getCobra().setLaserTemperature(alite.getCobra().getLaserTemperature() - 1);

				int energy = alite.getCobra().getEnergy();
				boolean updateFrontRearShields = energy == PlayerCobra.MAX_ENERGY ||
					    alite.getCobra().isEquipmentInstalled(EquipmentStore.extraEnergyUnit) ||
					    alite.getCobra().isEquipmentInstalled(EquipmentStore.navalEnergyUnit);
				if (energy < PlayerCobra.MAX_ENERGY) {
					alite.getCobra().setEnergy(energy + 1);
				}
				if (updateFrontRearShields) {
					alite.getCobra().setFrontShield(alite.getCobra().getFrontShield() + 1);
					alite.getCobra().setRearShield(alite.getCobra().getRearShield() + 1);
				}
				checkEnergyLow();
			}
		});
		return timedEvents;
	}

	final void checkEnergyLow() {
		if (!inGame.isPlayerAlive()) {
			return;
		}
		int energy = alite.getCobra().getEnergy();
		if (energy < PlayerCobra.MAX_ENERGY_BANK) {
			AliteLog.d("EnergyLow", "EnergyLow! -- Playing? " + SoundManager.isPlaying(Assets.energyLow));
		}
		if (energy < PlayerCobra.MAX_ENERGY_BANK && !SoundManager.isPlaying(Assets.energyLow)) {
			SoundManager.repeat(Assets.energyLow);
		} else if (energy >= PlayerCobra.MAX_ENERGY_BANK && SoundManager.isPlaying(Assets.energyLow)) {
			SoundManager.stop(Assets.energyLow);
		}
	}

	void performUpdate(final int viewDirection, final GraphicObject ship) {
		if (autoFire) {
			fire(viewDirection, ship);
		}
	}

	public void setAutoFire(boolean b) {
		Laser laser = alite.getCobra().getLaser(inGame.getViewDirection());
		if (laser == null) {
			autoFire = false;
			return;
		}
		autoFire = b;
	}

	public boolean isAutoFire() {
		return autoFire && inGame.isPlayerAlive();
	}

	void handleTouchUp(final int viewDirection, final GraphicObject ship) {
		if (!lastLaserFireUp.hasPassedSeconds(0.5f)) {
			autoFire = !autoFire;
		}
		if (!autoFire) {
			fire(viewDirection, ship);
		}
	}

	void renderLaser(LaserCylinder laser) {
		if (laser.isAiming()) {
			return;
		}
		alite.getTextureManager().setTexture(null);
		GLES11.glPushMatrix();
		laser.render(alite);
	    GLES11.glPopMatrix();
	    laser.postRender();
	}

	public void destroy() {
		alite.setLaserManager(null);
		inGame = null;
	}
}
