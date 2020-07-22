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

import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.screens.opengl.ingame.ObjectType;

import java.util.*;

public class SpaceObjectFactory {

	private static SpaceObjectFactory instance;
	private final List<SpaceObject> objects = new ArrayList<>();
	private final Set<String> demoObjectId = new HashSet<>();
	//         AI file     States      Messages     Methods
	private final Map<String, Map<String, Map<String, List<AIMethod>>>> AIs = new HashMap<>();

	public static SpaceObjectFactory getInstance() {
		if (instance == null) {
			instance = new SpaceObjectFactory();
		}
		return instance;
	}
	private SpaceObjectFactory() {
	}

	public void registerSpaceObject(SpaceObject object) {
		if (!objects.contains(object)) {
			objects.add(object);
		}
	}

	public SpaceObject getRandomObjectByType(ObjectType type) {
		double totalWeight = getTotalWeight(type);
		if (totalWeight == 0) {
			return null;
		}
		double rnd = Math.random();
		double sumWeight = 0;
		for (SpaceObject object : objects) {
			int allowedGalaxy = object.getRepoHandler().getNumericProperty(SpaceObject.Property.galaxy).intValue();
			if (allowedGalaxy == 0 || allowedGalaxy == Alite.get().getGenerator().getCurrentGalaxy()) {
				sumWeight += getWeight(object, type);
				if (rnd < sumWeight / totalWeight) {
					return object.cloneObject(type);
				}
			}
		}
		return null;
	}

	private double getTotalWeight(ObjectType type) {
		double totalWeight = 0;
		for (SpaceObject object : objects) {
			int allowedGalaxy = object.getRepoHandler().getNumericProperty(SpaceObject.Property.galaxy).intValue();
			if (allowedGalaxy == 0 || allowedGalaxy == Alite.get().getGenerator().getCurrentGalaxy()) {
				totalWeight += getWeight(object, type);
			}
		}
		return totalWeight;
	}

	private double getWeight(SpaceObject so, ObjectType type) {
		for (String role: so.getRepoHandler().getStringProperty(SpaceObject.Property.roles).split(" ")) {
			int roleLength = type.toString().length();
			role = role.toLowerCase().replace("-", "");
			if (role.startsWith(type.toString().toLowerCase())) {
				if (role.length() == roleLength) {
					return 1;
				}
				if (role.charAt(roleLength) == '(') {
					return Double.parseDouble(role.substring(roleLength + 1, role.indexOf(')', roleLength + 1)));
				}
			}
		}
		return 0;
	}

	public SpaceObject getTemplateObject(String id) {
		int index = getIndexById(id);
		return index < 0 ? null : objects.get(index);
	}

	public SpaceObject getObjectById(String id) {
		int index = getIndexById(id);
		if (index < 0) {
			return null;
		}
		SpaceObject so = objects.get(index);
		return so.cloneObject(getDefaultRole(so));
	}

	private ObjectType getDefaultRole(SpaceObject so) {
		for (String role: so.getRepoHandler().getStringProperty(SpaceObject.Property.roles).split(" ")) {
			try {
				int e = role.indexOf('(');
				return ObjectType.valueOf(e < 0 ? role : role.substring(0, e));
			} catch (IllegalArgumentException ignored) {
				// currently not handled role
			}
		}
		return ObjectType.Pirate;
	}

	private int getIndexById(String id) {
		if (id == null) {
			return -1;
		}
		for (int i = 0; i < objects.size(); i++) {
			if (id.equals(objects.get(i).getId())) {
				return i;
			}
		}
		return -1;
	}

	public void addToDemoList(String objectId) {
		demoObjectId.add(objectId);
	}

	public SpaceObject getNextObject(SpaceObject currentShip, int direction, boolean allObjects) {
		int index = currentShip == null ? -1 : getIndexById(currentShip.getId());
		SpaceObject so;
		do {
			if (index < 0) {
				index = 0;
			} else {
				index += direction;
				if (index < 0) index = objects.size() - 1;
				else if (index >= objects.size()) index = 0;
			}
			so = objects.get(index);
			if (direction == 0) {
				direction = 1;
			}
		} while (!allObjects && !demoObjectId.contains(so.getId()));
		return so.cloneObject(getDefaultRole(so));
	}

	public void clearLocaleDependentProperties() {
		for (SpaceObject so : objects) {
			so.getRepoHandler().clearLocaleDependent();
		}
	}

	public boolean existsAI(String aiName) {
		return AIs.get(aiName) != null;
	}

	public void addMethodsOfStateOfAI(String aiName, String stateName, String messageName, List<AIMethod> methods) {
		Map<String, Map<String, List<AIMethod>>> ai = AIs.get(aiName);
		if (ai == null) {
			ai = new HashMap<>();
			AIs.put(aiName, ai);
		}
		Map<String, List<AIMethod>> state = ai.get(stateName);
		if (state == null) {
			state = new HashMap<>();
			ai.put(stateName, state);
		}
		state.put(messageName, methods);
	}

	List<AIMethod> getMethods(String aiName, String stateName, String messageName) {
		Map<String, Map<String, List<AIMethod>>> ai = AIs.get(aiName);
		if (ai == null) {
			return null;
		}
		Map<String, List<AIMethod>> state = ai.get(stateName);
		if (state == null) {
			return null;
		}
		return state.get(messageName);
	}

	public boolean isAIType(String aiName) {
		return AIs.get(aiName) != null;
	}

	public boolean isAIState(String aiName, String stateName) {
		return AIs.get(aiName).get(stateName) != null;
	}
}
