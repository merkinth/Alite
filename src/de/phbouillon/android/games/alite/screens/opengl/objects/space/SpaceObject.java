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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

import android.opengl.GLES11;
import android.opengl.Matrix;
import de.phbouillon.android.framework.ResourceStream;
import de.phbouillon.android.framework.Timer;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.impl.gl.GraphicObject;
import de.phbouillon.android.framework.math.Quaternion;
import de.phbouillon.android.framework.math.Vector3f;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.generator.enums.Government;
import de.phbouillon.android.games.alite.model.missions.ThargoidStationMission;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.opengl.ingame.EngineExhaust;
import de.phbouillon.android.games.alite.screens.opengl.ingame.InGameManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.LaserManager;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;
import de.phbouillon.android.games.alite.screens.opengl.objects.AliteObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.TargetBoxSpaceObject;

public class SpaceObject extends AliteObject implements Serializable {
	private static final long serialVersionUID = 5206073222641804313L;

	public enum Property {
		// Ship keys
		accuracy,
		affected_by_energy_bomb, // alite specific, probability between 0 and 1 that energy bomb destroys the ship
		aft_eject_position,
		aft_weapon_type,
		aggression_level, // alite specific may be replaced by AI / subentities type=ball_turret fire_rate
		ai_type,
		auto_ai,
		auto_weapons,
		beacon,
		beacon_label,
		bounty,
		cargo_carried, // ok - only the number (but not the type) of launched cargos after explosion
		cargo_type,
		cloak_automatic,
		cloak_passive,
		conditions,
		condition_script,
		counts_as_kill,
		custom_views,
		death_actions,
		debris_role,
		density,
		display_name, // ok
		energy_recharge_rate,
		escape_pod_model,
		escorts, // ok, skipped, if escort_roles is defined
		escort_role, // ok, skipped, if escort_roles is defined
		escort_roles, // todo currently only the first item is taken into account and only for spawning thargoids
		escort_ship, // ok - redirected to escort_role
		exhaust, // ok
		exhaust_emissive_color, // ok - currently supported formats: named colors / #aarrggbb / #rrggbb / RGBA tuples
		explosion_type,
		extra_cargo,
		forward_weapon_type,
		frangible,
		fragment_chance,
		fuel,
		galaxy, // alite specific may be replaced by condition_script.allowSpawnShip
		has_cloaking_device,
		has_ecm, // ok
		has_energy_bomb,
		has_escape_pod, // ok
		has_fuel_injection,
		has_military_jammer, // has cloaking device
		has_military_scanner_filter, // can see the currently cloaked object on the radar
		has_scoop,
		has_scoop_message,
		has_shield_booster,
		has_shield_enhancer,
		heat_insulation,
		hud,
		hyperspace_motor,
		hyperspace_motor_spin_time,
		injector_burn_rate,
		injector_speed_factor,
		is_external_dependency,
		is_submunition,
		is_template,
		laser_color, // ok - currently supported formats: named colors / #aarrggbb / #rrggbb / RGBA tuples
		launch_actions,
		like_ship, // ok
		likely_cargo, // ok
		materials,
		max_cargo,
		max_energy, // ok
		max_flight_pitch, // ok
		max_flight_roll, // ok
		max_flight_speed, // ok
		max_flight_yaw,
		max_missiles, // ok
		missile_launch_position,
		missile_load_time, // ok
		missiles, // ok
		missile_role,
		model, // ok
		model_scale_factor, // ok
		name, // ok
		no_boulders,
		pilot,
		port_weapon_type,
		proximity_warning, // alite specific
		roles, // ok - currently supported values: see ObjectType
		rotational_velocity,
		scan_class,
		scan_description,
		scanner_display_color1,
		scanner_display_color2,
		scanner_hostile_display_color1,
		scanner_hostile_display_color2,
		scanner_range,
		scoop_position,
		script,
		script_info,
		script_actions,
		setup_actions,
		shaders,
		ship_name,
		ship_class_name,
		show_damage,
		smooth,
		spawn,
		starboard_weapon_type,
		subentities, // partly
		sun_glare_filter,
		track_contacts,
		throw_sparks,
		thrust,
		unpiloted,
		view_position_aft,
		view_position_forward,
		view_position_port,
		view_position_starboard,
		weapon_energy,
		weapon_facings,
		weapon_mount_mode,
		weapon_offset,
		weapon_position_aft,
		weapon_position_forward, // ok
		weapon_position_port,
		weapon_position_starboard,

		// Station keys
		allegiance,
		allows_auto_docking,
		allows_fast_docking,
		defense_ship,
		defense_ship_role,
		equipment_price_factor,
		equivalent_tech_level,
		has_npc_traffic,
		has_patrol_ships,
		has_shipyard,
		interstellar_undocking,
		is_carrier,
		market,
		market_broadcast,
		market_capacity,
		market_definition,
		market_monitored,
		market_script,
		max_defense_ships,
		max_police,
		max_scavengers,
		port_dimensions,
		port_radius, // ok
		requires_docking_clearance,
		rotating,
		station_roll, // ok
		tunnel_corners,
		tunnel_start_angle,
		tunnel_aspect_ratio
	}

	private Map<Property,Object> properties = new HashMap<>();

	private static final int DEFAULT_HUD_COLOR_ENEMY = 0x8CAAF0;
	private static final int DEFAULT_HUD_COLOR_TRADER = 0xF0F000;
	private static final int DEFAULT_HUD_COLOR_ASTEROID = 0xF000F0;
	private static final int DEFAULT_HUD_COLOR_SPACE_STATION = 0xF000F0;
	private static final int DEFAULT_HUD_COLOR_MISSILE = 0xF00000;
	private static final int DEFAULT_HUD_COLOR_SHUTTLE = 0xF0F000;
	private static final int DEFAULT_HUD_COLOR_VIPER = 0x545487;
	private static final int DEFAULT_HUD_COLOR_BUOY = 0xEFEF00;
	private static final int DEFAULT_HUD_COLOR_CARGO = 0x8A00F0;
	private static final int DEFAULT_HUD_COLOR_PUBLIC_ENEMY = 0x8C8C8C;
	private static final int DEFAULT_HUD_COLOR_ESCAPE_CAPSULE = 0x8CF000;

	private String textureFilename;
	private float[] vertexBuffer;
	private float[] facesBuffer;
	private float[] texCoordBuffer;
	private transient ResourceStream textureInputStream;

	private final float[] boundingBox = new float[6];
	private int hullStrength; // current energy, normally started from max_energy
	private final List<Vector3f> laserHardpoint = new ArrayList<>();

	private final List<EngineExhaust> exhaust = new ArrayList<>();
	private final List<SpaceObject> parts = new ArrayList<>();

	private boolean hasEcm;
	private boolean cloaked;
	private int escapePod;
	private boolean affectedByEnergyBomb;

	private float[] originalBoundingBox = null;
	private boolean inBay;
	private final HashMap <AiStateCallback, AiStateCallbackHandler> aiStateCallbackHandlers = new HashMap<>();
	private int ejectedPods;
	private int cargoCanisterCount;
	private boolean ignoreSafeZone;
	private final Timer lastMissileTime = new Timer().setAutoResetWithImmediateAtFirstCall();
	private long spawnDroneDistanceSq = -1;
	private final List <SpaceObject> activeDrones = new ArrayList<>();
	private boolean drone;
	private SpaceObject mother;
	private boolean player = false;

	private boolean identified;
 	private float scale = 1;
	private final List <ObjectType> objectsToSpawn = new ArrayList<>();


	private final SpaceObjectAI ai = new SpaceObjectAI(this);
	private ObjectType type;
	private TargetBoxSpaceObject targetBox;
	private SpaceObject proximity;
	private int playerHitCount;
	private boolean accessDenied;

	// missile
	private SpaceObject target;
	private SpaceObject source;
	private boolean ecmDestroy;

	// cargo canister
	private TradeGood cargoContent;
	private Weight cargoWeight;
	private Equipment specialCargoContent;
	private long cargoPrice;


	public SpaceObject(String id) {
		super(id);
		setVisibleOnHud(true);
	}

	public void setProperty(Property name, Object value) {
		try {
//			AliteLog.d("setProperty", name + " [" + (value != null ? value.getClass().getName() : "null") + "] = " + value);
			properties.put(name, value);
		} catch (IllegalArgumentException ignored) {
			AliteLog.e("Space object property error", "Unknown property '" + name + "'");
		}
	}

	public Object getProperty(Property name) {
		return properties.get(name);
	}

	public String getStringProperty(Property name) {
		return (String) properties.get(name);
	}

	public Float getNumericProperty(Property name) {
		Object value = properties.get(name);
		if (value == null) {
			return 0f;
		}
		if (value instanceof Long) {
			return ((Long) value).floatValue();
		}
		if (value instanceof Double) {
			return ((Double) value).floatValue();
		}
		if (value instanceof Boolean) {
			return (boolean) value ? 1f : 0f;
		}
		AliteLog.e("getNumericProperty error", "Cannot get number with type " + value.getClass().getName());
		return 0f;
	}

	public List<String> getArrayProperty(Property name) {
		return (List<String>) properties.get(name);
	}

	public List<Vector3f> getLaserHardpoint() {
		return laserHardpoint;
	}

	public boolean isAffectedByEnergyBomb() {
		return affectedByEnergyBomb;
	}

	public List <EngineExhaust> getExhausts() {
		return exhaust;
	}

	private void readObject(ObjectInputStream in) throws IOException {
		try {
			AliteLog.d("readObject", "SpaceObject.readObject");
			in.defaultReadObject();
			AliteLog.d("readObject", "SpaceObject.readObject I");
			SpaceObject so = SpaceObjectFactory.getInstance().getObjectById(getId());
			if (so != null) {
				textureInputStream = so.textureInputStream;
			}
			scaleBoundingBox(scale);
			AliteLog.d("readObject", "SpaceObject.readObject II");
		} catch (ClassNotFoundException e) {
			AliteLog.e("Class not found", e.getMessage(), e);
		}
	}

	void addObjectToSpawn(ObjectType type) {
		objectsToSpawn.add(type);
	}

	public List <ObjectType> getObjectsToSpawn() {
		return objectsToSpawn;
	}

	public void clearObjectsToSpawn() {
		objectsToSpawn.clear();
	}

	public ObjectType getType() {
		return type;
	}

	public void setType(ObjectType type) {
		this.type = type;
		setDefaultHudColor();
	}

	private void setDefaultHudColor() {
		switch (type) {
			case Trader:
				hudColor = DEFAULT_HUD_COLOR_TRADER;
				break;
			case Asteroid:
			case Alloy:
				hudColor = DEFAULT_HUD_COLOR_ASTEROID;
				break;
			case Coriolis:
			case Dodecahedron:
			case Icosahedron:
				hudColor = DEFAULT_HUD_COLOR_SPACE_STATION;
				break;
			case Missile:
				hudColor = DEFAULT_HUD_COLOR_MISSILE;
				break;
			case Shuttle:
				hudColor = DEFAULT_HUD_COLOR_SHUTTLE;
				break;
			case Police:
			case Defender:
				hudColor = DEFAULT_HUD_COLOR_VIPER;
				break;
			case Buoy:
				hudColor = DEFAULT_HUD_COLOR_BUOY;
				break;
			case CargoPod:
				hudColor = DEFAULT_HUD_COLOR_CARGO;
				break;
			case Constrictor:
			case Cougar:
			case Thargoid:
			case Thargon:
				hudColor = DEFAULT_HUD_COLOR_PUBLIC_ENEMY;
				break;
			case EscapeCapsule:
				hudColor = DEFAULT_HUD_COLOR_ESCAPE_CAPSULE;
				break;
			default:
				hudColor = DEFAULT_HUD_COLOR_ENEMY;
				break;
		}
	}

	final int getMissileCount() {
		return getNumericProperty(Property.missiles).intValue();
	}

	boolean canFireMissile() {
		return getMissileCount() > 0 && lastMissileTime.hasPassedSeconds(getNumericProperty(Property.missile_load_time));
	}

	int getEscapePod() {
		return escapePod;
	}

	void setEjected() {
		ejectedPods++;
	}

	boolean hasEjected() {
		return escapePod > 0 && ejectedPods >= escapePod;
	}

	@Override
	public float getBoundingSphereRadius() {
		return (float) Math.sqrt(getBoundingSphereRadiusSq());
	}

	public float getBoundingSphereRadiusSq() {
		float me = getMaxExtent() / 2.0f;
		return me * me * 3.0f;
	}

	public void setInBay(boolean b) {
		inBay = b;
	}

	public boolean isInBay() {
		return inBay;
	}

	public void setCloaked(boolean cloaked) {
		this.cloaked = cloaked;
	}

	public boolean isCloaked() {
		return cloaked;
	}

	private boolean receivesProximityWarning() {
		return getNumericProperty(Property.proximity_warning).intValue() == 1 && (!drone || hasLivingMother());
	}

	void hasBeenHitByPlayer() {
		if (type == ObjectType.Trader || type == ObjectType.Shuttle || type == ObjectType.EscapeCapsule) {
			computeLegalStatusAfterFriendlyHit();
		}
	}

	private void computeLegalStatusAfterFriendlyHit() {
		SystemData currentSystem = Alite.get().getPlayer().getCurrentSystem();
		int legalValue = Alite.get().getPlayer().getLegalValue();
		if (InGameManager.playerInSafeZone) {
			if (ObjectType.isSpaceStation(getType()) || currentSystem == null || currentSystem.getGovernment() != Government.ANARCHY) {
				InGameManager.safeZoneViolated = true;
			}
		}
		if (currentSystem != null) {
			Government government = currentSystem.getGovernment();
			switch (government) {
				case ANARCHY: break; // In anarchies, you can do whatever you want.
				case FEUDAL: if (hullStrength < 10 && Math.random() > 0.9) { legalValue += 16; } break;
				case MULTI_GOVERNMENT: if (hullStrength < 10 && Math.random() > 0.8) { legalValue += 24; } break;
				case DICTATORSHIP: if (hullStrength < 10 && Math.random() > 0.6) { legalValue += 32; } break;
				case COMMUNIST: if (hullStrength < 20 && Math.random() > 0.4) { legalValue += 40; } break;
				case CONFEDERACY: if (hullStrength < 30 && Math.random() > 0.2) { legalValue += 48; } break;
				case DEMOCRACY: legalValue += 56; break;
				case CORPORATE_STATE: legalValue += 64; break;
			}
		} else {
			// Default behavior as a "safeguard". Shouldn't really happen...
			legalValue += 64;
		}
		Alite.get().getPlayer().setLegalValue(legalValue);
	}

	public void setProximity(SpaceObject other) {
		if (other == null) {
			proximity = null;
			return;
		}
		if (!receivesProximityWarning()) {
			return;
		}
		if (proximity == other) {
			return;
		}
		if (ObjectType.isSpaceStation(other.getType())) {
			if (inBay) {
				return;
			}
			float maxExtentSq = other.getMaxExtent();
			maxExtentSq *= maxExtentSq;
			if (other.getPosition().distanceSq(getPosition()) > maxExtentSq) {
				return;
			}
		}
		if (other.getType() == ObjectType.Missile) {
			if (other.getSource() == this) {
				return;
			}
		}

		other.getPosition().sub(getPosition(), v0);
		float dotForward = v0.dot(getForwardVector());
		float dotUp = v0.dot(getUpVector());
		float dotRight = v0.dot(getRightVector());
		if (dotForward < 0 && getSpeed() < 0) {
			dotForward *= 0.25f * getMaxSpeed() / -getSpeed();
		}
		float dotSq = dotForward * dotForward + dotUp * dotUp + dotRight * dotRight;
		float collisionSq = getBoundingSphereRadius() * 3.0f + other.getBoundingSphereRadius() * 3.0f;
		collisionSq *= collisionSq;

		if (dotSq > collisionSq) {
			return;
		}
		if (ai.getState() == AIState.EVADE && proximity != null) {
			getPosition().sub(proximity.getPosition(), v0);
			v0.normalize();
			float angleProx = getForwardVector().angleInDegrees(v0);
			if (angleProx >= 180) {
				angleProx = 360 - angleProx;
			}
			getPosition().sub(other.getPosition(), v0);
			v0.normalize();
			float angleOther = getForwardVector().angleInDegrees(v0);
			if (angleOther >= 180) {
				angleOther = 360 - angleOther;
			}
			if (angleProx < angleOther) {
				return;
			}
		}
		if (Settings.VIS_DEBUG) {
			Vector3f pP = new Vector3f(0, 0, 0);
			Vector3f pvX = new Vector3f(0, 0, 0);
			Vector3f pvY = new Vector3f(0, 0, 0);
			Vector3f pvZ = new Vector3f(0, 0, 0);
			Vector3f oP = new Vector3f(0, 0, 0);
			Vector3f ovX = new Vector3f(0, 0, 0);
			Vector3f ovY = new Vector3f(0, 0, 0);
			Vector3f ovZ = new Vector3f(0, 0, 0);
			Vector3f tt = new Vector3f(0, 0, 0);
			getRightVector().copy(pvX);
			pvX.scale(0.0f-boundingBox[0]);
			getUpVector().copy(pvY);
			pvY.scale(0.0f-boundingBox[2]);
			getForwardVector().copy(pvZ);
			pvZ.scale(boundingBox[4]);
			getPosition().sub(pvX,pP);
			pP.sub(pvY);
			pP.sub(pvZ);
			getRightVector().copy(tt);
			tt.scale(boundingBox[1]);
			pvX.add(tt);
			getUpVector().copy(tt);
			tt.scale(boundingBox[3]);
			pvY.add(tt);
			getForwardVector().copy(tt);
			tt.scale(0.0f-boundingBox[5]);
			pvZ.add(tt);
			other.getRightVector().copy(ovX);
			ovX.scale(0.0f-other.boundingBox[0]);
			other.getUpVector().copy(ovY);
			ovY.scale(0.0f-other.boundingBox[2]);
			other.getForwardVector().copy(ovZ);
			ovZ.scale(other.boundingBox[4]);
			other.getPosition().sub(ovX,oP);
			oP.sub(ovY);
			oP.sub(ovZ);
			other.getRightVector().copy(tt);
			tt.scale(other.boundingBox[1]);
			ovX.add(tt);
			other.getUpVector().copy(tt);
			tt.scale(other.boundingBox[3]);
			ovY.add(tt);
			other.getForwardVector().copy(tt);
			tt.scale(0.0f-other.boundingBox[5]);
			ovZ.add(tt);
			if (player) {
				AliteLog.d("AIS",
					   "PRXMTY: Player (" + getPosition().x + ":" + getPosition().y + ":" + getPosition().z + ":" + getBoundingSphereRadius() +
					   ":" + pP.x + ":" + pP.y + ":" + pP.z + ":" + pvX.x + ":" + pvX.y + ":" + pvX.z + ":" + pvY.x + ":" + pvY.y + ":" + pvY.z + ":" + pvZ.x + ":" + pvZ.y + ":" + pvZ.z +
					   ") " + other +
					   " (" + other.getPosition().x + ":" + other.getPosition().y + ":" + other.getPosition().z + ":" + other.getBoundingSphereRadius() +
					   ":" + oP.x + ":" + oP.y + ":" + oP.z + ":" + ovX.x + ":" + ovX.y + ":" + ovX.z + ":" + ovY.x + ":" + ovY.y + ":" + ovY.z + ":" + ovZ.x + ":" + ovZ.y + ":" + ovZ.z +
					   ")");
			}
			else {
				AliteLog.d("AIS",
					   "PRXMTY: " + this + " (" + getPosition().x + ":" + getPosition().y + ":" + getPosition().z + ":" + getBoundingSphereRadius() +
					   ":" + pP.x + ":" + pP.y + ":" + pP.z + ":" + pvX.x + ":" + pvX.y + ":" + pvX.z + ":" + pvY.x + ":" + pvY.y + ":" + pvY.z + ":" + pvZ.x + ":" + pvZ.y + ":" + pvZ.z +
					   ") " + other +
					   " (" + other.getPosition().x + ":" + other.getPosition().y + ":" + other.getPosition().z + ":" + other.getBoundingSphereRadius() +
					   ":" + oP.x + ":" + oP.y + ":" + oP.z + ":" + ovX.x + ":" + ovX.y + ":" + ovX.z + ":" + ovY.x + ":" + ovY.y + ":" + ovY.z + ":" + ovZ.x + ":" + ovZ.y + ":" + ovZ.z +
					   ")");
			}
		}
		proximity = other;
	}

	public SpaceObject getProximity() {
		return proximity;
	}

	@Override
	public void render() {
		Alite.get().getTextureManager().setTexture(textureFilename, textureInputStream);

		boolean enabled = GLES11.glIsEnabled(GLES11.GL_CULL_FACE);
		if (enabled) {
			GLES11.glDisable(GLES11.GL_CULL_FACE);
		}
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES11.glVertexPointer(3, GLES11.GL_FLOAT, 0, GlUtils.toFloatBufferPositionZero(vertexBuffer));
		GLES11.glNormalPointer(GLES11.GL_FLOAT, 0, GlUtils.toFloatBufferPositionZero(facesBuffer));
		GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, GlUtils.toFloatBufferPositionZero(texCoordBuffer));
		GLES11.glDrawArrays(GLES11.GL_TRIANGLES, 0, facesBuffer.length / 3);

		for (SpaceObject part : parts) {
			GLES11.glPushMatrix();
			GLES11.glMultMatrixf(part.getMatrix(), 0);
			part.render();
			GLES11.glPopMatrix();
		}
		Alite.get().getTextureManager().setTexture(null);
		if (Settings.engineExhaust && !exhaust.isEmpty() && getSpeed() < 0f) {
			for (EngineExhaust ex: exhaust) {
				ex.render(this);
			}
		}

		if (enabled) {
			GLES11.glEnable(GLES11.GL_CULL_FACE);
		}
	}

	public void renderTargetBox(float distSq) {
		if (!Settings.targetBox || targetBox == null || distSq <= SpaceObjectAI.SHOOT_DISTANCE_SQ) {
			return;
		}
		float alpha = (distSq - SpaceObjectAI.SHOOT_DISTANCE_SQ) / (LaserManager.MAX_ENEMY_DISTANCE_SQ - SpaceObjectAI.SHOOT_DISTANCE_SQ);
		targetBox.setAlpha(alpha);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE);
		targetBox.render();
		GLES11.glDisable(GLES11.GL_BLEND);
	}

	public final void createFaces(float[] vertexData, float[] faces, int ...indices) {
		float scale = getNumericProperty(Property.model_scale_factor);
		vertexBuffer = new float[indices.length * 3];
		facesBuffer = new float[indices.length * 3];

		int offset = 0;
		for (int i: indices) {
			vertexBuffer[offset] = vertexData[i * 3] * scale;
			vertexBuffer[offset + 1] = vertexData[i * 3 + 1] * scale;
			vertexBuffer[offset + 2] = -vertexData[i * 3 + 2] * scale;

			facesBuffer[offset] = -faces[i * 3];
			facesBuffer[offset + 1] = -faces[i * 3 + 1];
			facesBuffer[offset + 2] = faces[i * 3 + 2];

			offset += 3;
		}
//		calculateBoundingBox(); // todo
	}

	private void calculateBoundingBox() {
		boundingBox[0] = boundingBox[1] = vertexBuffer[0];
		boundingBox[2] = boundingBox[3] = vertexBuffer[1];
		boundingBox[4] = boundingBox[5] = vertexBuffer[2];
		calculateBoundingBoxOfPart(this);

		for (SpaceObject part : parts) {
			calculateBoundingBoxOfPart(part);
		}
	}

	private void calculateBoundingBoxOfPart(SpaceObject so) {
		float[] v = new float[4];
		float[] matrix = so.getMatrix();
		for (int i = 0; i < so.vertexBuffer.length; i += 3) {
			calculateVertex(v, 0, matrix, so.vertexBuffer, i, 1);
			boundingBox[0] = Math.min(boundingBox[0], v[0]);
			boundingBox[1] = Math.max(boundingBox[1], v[0]);
			boundingBox[2] = Math.min(boundingBox[2], v[1]);
			boundingBox[3] = Math.max(boundingBox[3], v[1]);
			boundingBox[4] = Math.min(boundingBox[4], v[2]);
			boundingBox[5] = Math.max(boundingBox[5], v[2]);
		}
	}

	public float[] getBoundingBox() {
		return boundingBox;
	}

	public void scaleBoundingBox(float scale) {
		if (boundingBox == null) {
			return;
		}
		if (originalBoundingBox == null) {
			originalBoundingBox = new float[boundingBox.length];
			System.arraycopy(boundingBox, 0, originalBoundingBox, 0, boundingBox.length);
		}
		for (int i = 0; i < originalBoundingBox.length; i++) {
			boundingBox[i] = originalBoundingBox[i] * scale;
		}
	}

	@Override
	public void scale(float scale) {
		this.scale = scale;
		super.scale(scale);
		scaleBoundingBox(scale);
	}

	public float getMaxExtent() {
		float add = 0.0f;
		if (!exhaust.isEmpty()) {
			add = exhaust.get(0).getMaxLen();
		}
		return getMaxExtentBase(add);
	}

	private float getMaxExtentBase(float add) {
		return Math.max(Math.abs(boundingBox[0]) + Math.abs(boundingBox[1]),
			Math.max(Math.abs(boundingBox[2]) + Math.abs(boundingBox[3]),
			Math.abs(boundingBox[4]) + Math.abs(boundingBox[5]) + add));
	}

	public float getMaxExtentWithoutExhaust() {
		return getMaxExtentBase(0);
	}

	public void dispose() {
		if (textureFilename != null) {
			Alite.get().getTextureManager().freeTexture(textureFilename);
		}
		for (SpaceObject part : parts) {
			part.dispose();
		}
	}

	@Override
	public String getName() {
		return getStringProperty(Property.name);
	}

	public float getMaxSpeed() {
		return getNumericProperty(Property.max_flight_speed);
	}

	float getMaxRollSpeed() {
		return getNumericProperty(Property.max_flight_roll);
	}

	float getMaxPitchSpeed() {
		return getNumericProperty(Property.max_flight_pitch);
	}

	public int getHullStrength() {
		return hullStrength;
	}

	public boolean hasEcm() {
		return hasEcm;
	}

	int getAggressionLevel() {
		return getNumericProperty(Property.aggression_level).intValue();
	}

	public int getMaxCargoCanisters() {
		return getNumericProperty(Property.cargo_carried).intValue();
	}

	public TradeGood getCargoType() {
		int cargoType = getNumericProperty(Property.likely_cargo).intValue();
		if (cargoType <= 0 || cargoType > TradeGoodStore.NUMBER_OF_GOODS) {
			return TradeGoodStore.get().getRandomTradeGoodForContainer();
		}
		return TradeGoodStore.get().fromNumber(cargoType - 1);
	}

	public int applyDamage(int amount) {
		hullStrength -= amount;
		if (hullStrength < 0) {
			hullStrength = 0;
		}
		return hullStrength;
	}

	public void setHullStrength(int hullStrength) {
		if (hullStrength < 0) {
			hullStrength = 0;
		}
		this.hullStrength = hullStrength;
	}

	public boolean intersect(Vector3f origin, Vector3f direction, float scaleFactor) {
		float[] verts = new float[vertexBuffer.length + 1];
		float[] matrix = getMatrix();
		for (int i = 0; i < vertexBuffer.length; i += 3) {
			calculateVertex(verts, i, matrix, vertexBuffer, i, scaleFactor);
		}
		if (intersectInternal(vertexBuffer.length / 3, origin, direction, verts)) {
			return true;
		}
		float[] tempMatrix = new float[16];
		for (SpaceObject part : parts) {
			MathHelper.copyMatrix(part.getMatrix(), tempMatrix);
			boolean hit = part.intersect(origin, direction, scaleFactor);
			part.setMatrix(tempMatrix);
			if (hit) {
				return true;
			}
		}
		return false;
	}

	public void orientTowards(float x, float y, float z, float ux, float uy, float uz) {
		v0.x = x;
		v0.y = y;
		v0.z = z;
		v1.x = ux;
		v1.y = uy;
		v1.z = uz;
		ai.orient(v0, v1, 0);
	}

	public void orientTowards(GraphicObject object, float deltaTime) {
		object.getUpVector().copy(v0);
//		v0.negate();
		ai.orient(object.getPosition(), v0, deltaTime);
	}

	public void orientTowards(GraphicObject object, Vector3f up, float deltaTime) {
		ai.orient(object.getPosition(), up, deltaTime);
	}

	public void orientTowardsUsingRollPitch(GraphicObject object, float deltaTime) {
		ai.orientUsingRollPitchOnly(object.getPosition(), deltaTime);
	}

	public void setRandomOrientation(Vector3f origin, Vector3f up) {
		up.copy(v0);
		v1.x = (float) (0.7 - Math.random() * 1.4);
		v1.y = (float) (0.7 - Math.random() * 1.4);
		v1.z = (float) (0.7 - Math.random() * 1.4);
		MathHelper.getRandomPosition(origin, v1, 16384.0f, 8192.0f);
		ai.orient(v1, v0, 0);
	}

	public void update(float deltaTime) {
		ai.update(deltaTime);
		if (Settings.engineExhaust && !exhaust.isEmpty()) {
			for (EngineExhaust ex: exhaust) {
				ex.update(this);
			}
		}
	}

	public void setAIState(AIState newState, Object ...data) {
		ai.setState(newState, data);
	}

	public AIState getAIState() {
		return ai.getState();
	}

	public String getCurrentAIStack() {
		return ai.getStateStack();
	}

	public int getNumberOfLasers() {
		return laserHardpoint.size();
	}

	public float getLaserX(int i) {
		return laserHardpoint.get(i).x;
	}

	public float getLaserY(int i) {
		return laserHardpoint.get(i).y;
	}

	public float getLaserZ(int i) {
		return laserHardpoint.get(i).z;
	}

	void aiStateCallback(AiStateCallback type) {
		AiStateCallbackHandler handler = aiStateCallbackHandlers.get(type);
		if (handler != null) {
			handler.execute(this);
		}
	}

	public void executeHit(SpaceObject player) {
		if (ObjectType.isSpaceStation(type)) {
			if (ThargoidStationMission.ALIEN_SPACE_STATION.equals(getId())) {
				return;
			}
			Alite.get().getPlayer().setLegalValue(Alite.get().getPlayer().getLegalValue() + 10);
			playerHitCount++;
			if (playerHitCount > 1) {
				computeLegalStatusAfterFriendlyHit();
			}
			return;
		}
		ai.executeHit(player);
	}

	public void registerAiStateCallbackHandler(AiStateCallback type, AiStateCallbackHandler handler) {
		aiStateCallbackHandlers.put(type, handler);
	}

	public void clearAiStateCallbackHandler(AiStateCallback type) {
		aiStateCallbackHandlers.remove(type);
	}

	public int getBounty() {
		return getNumericProperty(Property.bounty).intValue();
	}

	public int getScore() {
		switch (type) {
			case Asteroid:
			case Buoy:
			case CargoPod:
			case Shuttle:
			case Alloy:
			case Missile:
			case TieFighter: return 0;
			case Constrictor: return 5000;
			case Trader: return (int) (getNumericProperty(Property.max_energy).intValue() * 0.6f);
		}
		return getNumericProperty(SpaceObject.Property.max_energy).intValue();
	}

	@Override
	public String toString() {
		return getId();
	}

	public int getCargoCanisterOverrideCount() {
		return cargoCanisterCount;
	}

	public void setCargoCanisterCount(int count) {
		cargoCanisterCount = count;
	}

	public void setIgnoreSafeZone() {
		ignoreSafeZone = true;
	}

	boolean isIgnoreSafeZone() {
		return ignoreSafeZone;
	}

	public int getLaserColor() {
		return getColor(getStringProperty(Property.laser_color));
	}

	public int getColor(String color) {
		if (color.toLowerCase().endsWith("color")) {
			return AliteColor.parseColor(color.substring(0, color.length() - 5));
		}
		if (color.charAt(0) == '#') {
			return AliteColor.parseColor(color);
		}
		String[] factors = color.split(" ");
		float red = Float.parseFloat(factors[0]);
		float green = Float.parseFloat(factors[1]);
		float blue = Float.parseFloat(factors[2]);
		float divider = red <= 1 && green <= 1 && blue <= 1 && (factors.length < 4 || Float.parseFloat(factors[3]) <= 1) ? 1 : 255;
		return AliteColor.argb(factors.length == 4 ? Float.parseFloat(factors[3]) / divider : 1,
			red / divider, green / divider, blue / divider);
	}

	public boolean isIdentified() {
		return identified;
	}

	public void setIdentified() {
		identified = true;
	}

	public void setHudColor(int hudColor) {
		this.hudColor = hudColor;
		targetBox.setColor(hudColor);
	}

	@Override
	public boolean isVisibleOnHud() {
		return super.isVisibleOnHud() && !cloaked;
	}

	@Override
	public float getDistanceFromCenterToBorder() {
		if (!ObjectType.isSpaceStation(type)) {
			return 50;
		}
		Float radius = getNumericProperty(Property.port_radius);
		if (radius != 0) {
			return radius;
		}
		float[] boundingBox = parts.get(0).getBoundingBox(); // docking bay
		return Math.abs(boundingBox[4]) + Math.abs(boundingBox[5]);
	}

	public float getSpaceStationRotationSpeed() {
		float stationRoll = getNumericProperty(Property.station_roll);
		return stationRoll == 0 ? 0.2f : stationRoll;
	}

	public void setSpawnDroneDistanceSq(long distance) {
		spawnDroneDistanceSq = distance;
	}

	public long getSpawnDroneDistanceSq() {
		return spawnDroneDistanceSq;
	}

	public void addActiveDrone(SpaceObject drone) {
		activeDrones.add(drone);
		drone.mother = this;
		drone.drone = true;
	}

	public void removeActiveDrone(SpaceObject drone) {
		activeDrones.remove(drone);
	}

	public List <SpaceObject> getActiveDrones() {
		return activeDrones;
	}

	public int getMinDrones() {
		if (getArrayProperty(Property.escort_roles) == null) {
			return getNumericProperty(Property.escorts).intValue();
		}
		String[] p = getArrayProperty(Property.escort_roles).get(0).split(" ");
		return Integer.parseInt(p[1].substring(1, p[1].length() - 1));
	}

	public int getMaxDrones() {
		if (getArrayProperty(Property.escort_roles) == null) {
			return getNumericProperty(Property.escorts).intValue();
		}
		String[] p = getArrayProperty(Property.escort_roles).get(0).split(" ");
		return Integer.parseInt(p[2].substring(1, p[2].length() - 1));
	}

	public boolean isDrone() {
		return drone;
	}

	public boolean hasLivingMother() {
		return mother != null && mother.getHullStrength() > 0;
	}

	public void setTexture(String textureFilename, float[] texCoordBuffer, ResourceStream textureInputStream) {
		this.textureFilename = textureFilename;
		this.texCoordBuffer = texCoordBuffer;
		this.textureInputStream = textureInputStream;
		if (Alite.get() != null) {
			Alite.get().getTextureManager().addTextureFromStream(textureFilename, textureInputStream);
		}
	}

	public void addSubEntity(String subEntity) {
		String[] items = subEntity.split("\\s");
		// sub-entity key
		SpaceObject entity = SpaceObjectFactory.getInstance().getObjectById(items[0]);
		// position vector (x,y,z) orientation quaternion (w,x,y,z)
		Quaternion q = new Quaternion(Float.parseFloat(items[5]), Float.parseFloat(items[6]), Float.parseFloat(items[7]), Float.parseFloat(items[4]));
		Vector3f axis = new Vector3f(q.x, q.y, q.z);
		q.axisOfRotation(axis);
		float angle = (float) Math.toDegrees(q.angleOfRotation());
		if (Math.abs(angle) > 0.0001f && !Float.isInfinite(angle) && !Float.isNaN(angle)) {
			Matrix.rotateM(entity.getMatrix(), 0, angle, axis.x, axis.y, axis.z);
			entity.extractVectors();
		}

		float px = Float.parseFloat(items[1]);
		float py = Float.parseFloat(items[2]);
		float pz = Float.parseFloat(items[3]);
		if (px != 0 || py != 0 || pz != 0) {
			entity.setPosition(px, py, pz);
		}
		parts.add(entity);
	}

	SpaceObject cloneObject(ObjectType type) {
		SpaceObject object = new SpaceObject(getId());
		object.setType(type);
		copyPropertiesAndModelData(object, false);

		if (hudColor != 0) {
			object.hudColor = hudColor;
		}
		object.hasEcm = hasByProbability(object.getNumericProperty(Property.has_ecm));
		object.affectedByEnergyBomb = hasByProbability(object.getNumericProperty(Property.affected_by_energy_bomb));
		float escapePodProbability = object.getNumericProperty(Property.has_escape_pod);
		if (escapePodProbability > 1) object.escapePod = (int) escapePodProbability;
		else object.escapePod = hasByProbability(escapePodProbability) ? 1 : 0;
		object.setMatrix(getMatrix());
		object.setVisibleOnHud(isVisibleOnHud());
		object.target = target;
		object.source = source;
		object.ecmDestroy = ecmDestroy;
		object.cargoContent = cargoContent;
		object.cargoWeight = cargoWeight;
		object.specialCargoContent = specialCargoContent;
		object.cargoPrice = cargoPrice;
		object.hullStrength = getNumericProperty(SpaceObject.Property.max_energy).intValue();

		for (SpaceObject part: parts) {
			object.parts.add(part.cloneObject(part.getType()));
		}
		for (EngineExhaust e: exhaust) {
			object.exhaust.add(new EngineExhaust(e.getRadiusX(), e.getRadiusY(), e.getMaxLen(),
				e.getX(), e.getY(), e.getZ(), e.getR(), e.getG(), e.getB(), e.getA()));
		}
		for (Vector3f l: laserHardpoint) {
			object.laserHardpoint.add(new Vector3f(l.x, l.y, l.z));
		}
		object.initTargetBox();
		return object;
	}

	private void initTargetBox() {
		calculateBoundingBox();
		float size = getMaxExtentWithoutExhaust() * 1.25f;
		targetBox = new TargetBoxSpaceObject("targetBox", size, size, size);
		targetBox.setColor(hudColor);
	}

	public void copyPropertiesAndModelData(SpaceObject dest) {
		copyPropertiesAndModelData(dest, true);
	}

	private void copyPropertiesAndModelData(SpaceObject dest, boolean undefinedOnly) {
		if (undefinedOnly) {
			for (Property p : properties.keySet()) {
				if (!dest.properties.containsKey(p)) {
					dest.properties.put(p, properties.get(p));
				}
			}
		} else {
			dest.properties.putAll(properties);
		}
		dest.vertexBuffer = new float[vertexBuffer.length];
		System.arraycopy(vertexBuffer, 0, dest.vertexBuffer, 0, vertexBuffer.length);
		dest.facesBuffer = new float[facesBuffer.length];
		System.arraycopy(facesBuffer, 0, dest.facesBuffer, 0, facesBuffer.length);
		System.arraycopy(boundingBox, 0, dest.boundingBox, 0, boundingBox.length);
		dest.setTexture(textureFilename, texCoordBuffer, textureInputStream);
	}

	private boolean hasByProbability(float probability) {
		return probability > Math.random();
	}

	public void setPlayer(boolean b) {
		player = b;
		if (b) {
			exhaust.clear();
		}
	}

	public boolean isPlayer() {
		return player;
	}

	public boolean isAccessDenied() {
		return accessDenied || playerHitCount >= 4;
	}

	public void denyAccess() {
		accessDenied = true;
	}

	public int getHitCount() {
		return playerHitCount;
	}

	public void setTarget(SpaceObject target) {
		this.target = target;
	}

	public SpaceObject getTarget() {
		return target;
	}

	public void setWillBeDestroyedByECM(boolean ecmDestroy) {
		this.ecmDestroy = ecmDestroy;
	}

	public boolean getWillBeDestroyedByECM() {
		return ecmDestroy;
	}

	public void setSource(SpaceObject source) {
		this.source = source;
	}

	public SpaceObject getSource() {
		return source;
	}

	public void setCargoContent(TradeGood tradeGood, Weight quantity) {
		cargoContent = tradeGood;
		cargoWeight = quantity;
	}

	public void setSpecialCargoContent(Equipment equipment) {
		specialCargoContent = equipment;
		cargoContent = null;
		cargoWeight = null;
	}

	public Equipment getSpecialCargoContent() {
		return specialCargoContent;
	}

	public TradeGood getCargoContent() {
		return cargoContent;
	}

	public Weight getCargoQuantity() {
		return cargoWeight;
	}

	public long getCargoPrice() {
		return cargoPrice;
	}

	public void setCargoPrice(long cargoPrice) {
		this.cargoPrice = cargoPrice;
	}
}
