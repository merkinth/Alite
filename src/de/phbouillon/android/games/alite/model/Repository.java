package de.phbouillon.android.games.alite.model;

import com.dd.plist.NSNumber;
import com.dd.plist.NSString;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.colors.AliteColor;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository<T> implements Serializable {
	private static final long serialVersionUID = -2688987946111213892L;

	private final Map<T,Object> properties = new HashMap<>();
	private final Map<T,Object> localeDependentProperties = new HashMap<>();

	public void setProperty(T name, Object value) {
		setProperty(name, value, false);
	}

	public void setLocalizedProperty(T name, Object value) {
		setProperty(name, value, true);
	}

	private void setProperty(T name, Object value, boolean localized) {
		try {
			if (value instanceof NSString) {
				value = getResString(((NSString) value).getContent());
			} else if (value instanceof String) {
				value = getResString((String) value);
			} else if (value instanceof NSNumber) {
				value = getNumber((NSNumber) value);
			}
//			AliteLog.d("set" + (localized ? "Localized" : "") + "Property", name + " [" +
//				(value != null ? value.getClass().getName() : "null") + "] = " + value);
			if (localized) {
				localeDependentProperties.put(name, value);
			} else {
				properties.put(name, value);
			}
		} catch (IllegalArgumentException ignored) {
			AliteLog.e((localized ? "Localized " : "") + "Property setting error", "Unknown property '" + name + "'");
		}
	}

	private Object getResString(String value) {
		return value.indexOf('@') > 0 ? -value.hashCode() : value;
	}

	public static Object getNumber(NSNumber value) {
		switch (value.type()) {
			case NSNumber.INTEGER: {
				return value.longValue();
			}
			case NSNumber.REAL: {
				return value.doubleValue();
			}
			case NSNumber.BOOLEAN: {
				return value.boolValue();
			}
		}
		return null;
	}

	public void setUnsetProperty(T name, Object value) {
		if (getProperty(name) == null) {
			setProperty(name, value);
		}
	}

	public void checkMissingRequiredProperty(String fileName, List<T> propertyList) throws IOException {
		for(T p : propertyList) {
			throwIfMissing(fileName, p.toString(), getProperty(p));
		}
	}

	public static <V> V throwIfMissing(String fileName, String name, V value) throws IOException {
		if (value == null) {
			String message = "Missing required property '" + name + "' in file '" + fileName + "'.";
			AliteLog.e("Property error", message);
			throw new IOException(message);
		}
		return value;
	}

	public Object getProperty(T name) {
		Object property = localeDependentProperties.get(name);
		return property != null ? property : properties.get(name);
	}

	public String getStringProperty(T name) {
		return (String) getProperty(name);
	}

	public String getResStringProperty(T name) {
		Object s = getProperty(name);
		return s instanceof Integer ? L.string((Integer) s) : (String) s;
	}

	public Float getNumericProperty(T name) {
		Object value = getProperty(name);
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
		AliteLog.e("getNumericProperty error", "Cannot get value of '" + name +
			"' with type " + value.getClass().getName());
		return 0f;
	}

	public List<String> getArrayProperty(T name) {
		return (List<String>) getProperty(name);
	}

	public int getColorProperty(T name) {
		return getColor((String) getProperty(name));
	}

	public void clearLocaleDependent() {
		localeDependentProperties.clear();
	}

	public void copyToIfUndefined(Repository<T> dest) {
		dest.localeDependentProperties.clear();
		for (Map.Entry<T, Object> p : properties.entrySet()) {
			if (!dest.properties.containsKey(p.getKey())) {
				dest.properties.put(p.getKey(), p.getValue());
			}
		}
	}

	public void copyLocaleDependentTo(Repository<T> dest) {
		dest.localeDependentProperties.clear();
		dest.localeDependentProperties.putAll(localeDependentProperties);
	}

	public void copyTo(Repository<T> dest) {
		dest.properties.clear();
		dest.properties.putAll(properties);
	}

	public static int getColor(String color) {
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
		float divider = red <= 1 && green <= 1 && blue <= 1 &&
			(factors.length < 4 || Float.parseFloat(factors[3]) <= 1) ? 1 : 255;
		return AliteColor.argb(factors.length == 4 ? Float.parseFloat(factors[3]) / divider : 1,
			red / divider, green / divider, blue / divider);
	}

}
