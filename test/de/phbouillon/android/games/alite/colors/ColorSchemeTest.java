package de.phbouillon.android.games.alite.colors;

import android.graphics.Color;
import de.phbouillon.android.games.alite.Settings;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class ColorSchemeTest {

	private static final String SAMPLE_SCHEME_FILE_NAME = "sample" + ColorScheme.ALITE_COLOR_SCHEME_EXTENSION;

	@BeforeClass
	public static void setUp() {
		Settings.suppressOnlineLog = true;
	}

	@Test
	public void setColorScheme() {
		// File structure error (e.g. empty file)
		assertNotEquals("", ColorScheme.setColorScheme(null, "", ""));

		// Different kind of erroneous lines
		assertEquals("Unknown color (xx) for color item 'ConditionGreen'.",
			ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": \"xx\"\n}\n}\n", ""));

		// Different kind of erroneous lines
		assertEquals("JSONObject[\"endColor\"] not found.",
			ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": {\n" +
			"\"startColor\": \"#00000000\"\n}\n}\n}\n", ""));

		// Different kind of erroneous lines
		assertEquals("JSONObject[\"startColor\"] not found.",
			ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": {\n" +
			"\"wrongName\": \"#00000000\"\n}\n}\n}\n", ""));

		// Different kind of correct lines
		assertNull(ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": \"#00000000\"\n}\n}\n", ""));
		assertEquals(0,ColorScheme.get(0));

		// Different kind of correct lines
		assertNull(ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": {\n" +
			"\"startColor\": \"#00000000\",\n\"endColor\": \"#00000000\"\n}\n}\n}\n", ""));
		assertEquals(0,ColorScheme.get(0));

		// Different kind of correct lines
		assertNull(ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": {\n" +
			"\"startColor\": \"#ffff0000\",\n\"endColor\": \"RED\"\n}\n}\n}\n", ""));
		assertEquals(0xffff0000,ColorScheme.get(0,1));
		assertEquals(Color.RED,ColorScheme.get(0,0));

		// Different kind of correct lines
		assertNull(ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": {\n" +
			"\"startColor\": \"Red\",\n\"endColor\": \"#ffff0000\"\n}\n}\n}\n", ""));
		assertEquals(Color.RED,ColorScheme.get(0,1));
		assertEquals(0xffff0000,ColorScheme.get(0,0));

		// Different kind of correct lines
		assertNull(ColorScheme.setColorScheme(null, "{\n\"colors\": {\n\"ConditionGreen\": \"red\"\n}\n}\n", ""));
		assertEquals(Color.RED,ColorScheme.get(0));

		// Correct test file
		File file = new File(getClass().getClassLoader().getResource(SAMPLE_SCHEME_FILE_NAME).getFile());
		System.out.println(file.getPath());
		try(FileInputStream fin = new FileInputStream(file)) {
			byte [] fileContent = new byte[(int) file.length()];
			fin.read(fileContent);
			assertNull(ColorScheme.setColorScheme(null, new String(fileContent), ""));
			assertEquals(0xff2f4858,ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_WHOLE,1));
			assertEquals(Color.RED,ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_WHOLE,0));
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	@Test
	public void get() {
		ColorScheme.setColorScheme(null, null, ColorScheme.COLOR_SCHEME_CLASSIC);
		assertEquals("Should be invalid", Color.TRANSPARENT, ColorScheme.get(-1));
		assertEquals("Should be invalid", Color.TRANSPARENT, ColorScheme.get(1000));
		assertEquals(AliteColor.LIGHT_GREEN, ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN));
		assertEquals(AliteColor.LIGHT_GREEN, ColorScheme.get(ColorScheme.COLOR_CONDITION_GREEN, 0.5f));
		assertEquals(Color.MAGENTA, ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_X+3, 1));
		assertEquals(Color.RED, ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_X+3, 0));
		assertEquals(0xFFFF00BB, ColorScheme.get(ColorScheme.COLOR_ENERGY_BANK_X+3, 0.5f));
	}

}