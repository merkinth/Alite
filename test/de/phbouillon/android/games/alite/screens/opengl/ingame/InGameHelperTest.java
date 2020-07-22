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

import com.dd.plist.NSDictionary;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.model.*;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;
import de.phbouillon.android.games.alite.screens.canvas.InventoryScreen;
import de.phbouillon.android.games.alite.screens.opengl.TestTexture;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObject;
import de.phbouillon.android.games.alite.screens.opengl.objects.space.SpaceObjectFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InGameHelperTest {

	private static final TradeGood SLAVES = TradeGoodStore.get().getGoodById(TradeGoodStore.SLAVES);
	private final SpaceObject SLAVES_CARGO = createCargoObject(SLAVES, Weight.tonnes(1));
	private final SpaceObject ESCAPE_CAPSULE = createEscapeCapsuleObject();

	private static final Input input = new TestInput();
	private static final FileIO fileIO = new TestFileIO();
	private static final Texture texture = new TestTexture();
	private static final Alite alite = new Alite() {
		@Override
		public Texture getTextureManager() {
			return texture;
		}

		@Override
		public FileIO getFileIO() {
			return fileIO;
		}

		@Override
		public Input getInput() {
			return input;
		}
	};
	private final PlayerCobra cobra = alite.getCobra();
	private static InventoryScreen inventoryScreen;
	private static InGameHelper inGameHelper;
	private JSONObject state;
	private static final float[] matrix = new float[16];

	private static boolean scooped;
	private static boolean rammed;

	@BeforeClass
	public static void beforeClass() throws IOException {
		AliteLog.setInstance(new TestLogger());
		L.getInstance().addDefaultResource(new File("res\\values").getAbsolutePath(), FileInputStream::new, "");
		L.getInstance().setLocale(Locale.US);

		addObject("cobra_mk_iii", "");
		addObject("cargo_canister", "CargoPod");
		addObject("escape_capsule", "EscapeCapsule");
		addObject("coriolis_station", "Coriolis")
			.getRepoHandler().setProperty(SpaceObject.Property.port_radius, 1000L);

		EquipmentStore.get().addEquipment(new Equipment("EQ_FUEL_SCOOPS", "equip_fuel_scoop",
			4, 5250, "equip_short_fuel_scoop", new NSDictionary()));

		alite.initialize();
		inGameHelper = new InGameHelper(new InGameManager(null, null, null,
			true, false));
		inGameHelper.setScoopCallback(new ScoopCallback() {
			@Override
			public void scooped(SpaceObject scoopedObject) {
				scooped = true;
			}

			@Override
			public void rammed(SpaceObject rammedObject) {
				rammed = true;
			}
		});
		inventoryScreen = new InventoryScreen();
		alite.setScreen(new FlightScreen());
		matrix[13] = -1; // world.y
	}

	private static SpaceObject addObject(String name, String role) {
		SpaceObject object = new SpaceObject(name);
		object.createFaces(new float[] {0,0,0}, new float[] {0,0,0}, 0);
		object.setTexture("", null, null);
		object.getRepoHandler().setProperty(SpaceObject.Property.roles, role);
		SpaceObjectFactory.getInstance().registerSpaceObject(object);
		return object;
	}

	@Before
	public void setUp() throws JSONException {
		scooped = false;
		rammed = false;
		cobra.clearInventory();
		cobra.clearEquipment();
		cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP));
		state = alite.getPlayer().toJson();
	}

	@After
	public void tearDown() throws JSONException {
		alite.getPlayer().fromJson(state);
	}

	@Test
	public void scoopCargoWithoutFuelScoopTest() {
		cobra.clearEquipment();
		inGameHelper.scoop(SLAVES_CARGO);
		Assert.assertFalse(scooped);
		Assert.assertTrue(rammed);
	}

	@Test
	public void scoopCargoWithFullCargoHoldTest() {
		cobra.setTradeGood(TradeGoodStore.get().getGoodById(TradeGoodStore.FOOD),
			cobra.getFreeCargo(), 1000);
		inGameHelper.scoop(SLAVES_CARGO);
		Assert.assertFalse(scooped);
		Assert.assertTrue(rammed);
	}

	@Test
	public void scoopFirstCargoTest() {
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(1,0, 0,0);
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(2,0, 0,0);
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(3,0, 0,0);
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(4,0, 0,0);

		inventoryScreen.performTradeWhileInFlight(0);
		testScoopedEjectedAmounts(0,0, 0,0);

		SpaceObject ejectedCargo = inventoryScreen.getLastEjectedCargo();
		Assert.assertEquals(4, ejectedCargo.getCargoQuantity().getQuantityInAppropriateUnit());
		Assert.assertEquals(0, ejectedCargo.getCargoCanisterOverrideCount());
		Assert.assertEquals(0, ejectedCargo.getCargoPrice());

		scoopASlaveCargo(ejectedCargo); // scoop back
		testScoopedEjectedAmounts(4,0, 0,0);
	}

	@Test
	public void scoopFirstEscapeCapsuleAndCargoTest() {
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(1,0, 0,0);
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(2,0, 0,0);
		int q = scoopASlaveCargo(ESCAPE_CAPSULE);
		testScoopedEjectedAmounts(2 + q,0, q,0);
		q += scoopASlaveCargo(ESCAPE_CAPSULE);
		testScoopedEjectedAmounts(2 + q,0, q,0);

		inventoryScreen.performTradeWhileInFlight(0);
		testScoopedEjectedAmounts(2 + q - 4,0, Math.max(q - 4, 0),0);

		SpaceObject ejectedCargo = inventoryScreen.getLastEjectedCargo();
		Assert.assertEquals(4, ejectedCargo.getCargoQuantity().getQuantityInAppropriateUnit());
		Assert.assertEquals(Math.min(q, 4), ejectedCargo.getCargoCanisterOverrideCount());
		Assert.assertEquals(0, ejectedCargo.getCargoPrice());

		scoopASlaveCargo(ejectedCargo); // scoop back
		testScoopedEjectedAmounts(2 + q,0, q,0);
	}

	@Test
	public void buyThenScoopCompoundTest() {
		cobra.addTradeGood(SLAVES, Weight.tonnes(1), 100);
		scoopASlaveCargo(SLAVES_CARGO);
		testScoopedEjectedAmounts(1,100, 0,0);
		int q = scoopASlaveCargo(ESCAPE_CAPSULE);
		testScoopedEjectedAmounts(1 + q,100, q,0);
		inventoryScreen.performTradeWhileInFlight(0);
		testScoopedEjectedAmounts(Math.max(1 + q - 4, 0), 1 + q - 4 >= 0 ? 100 : 0, Math.max(q - 4, 0),
			2 + q - 4 > 0 ? 0 : 1);

		SpaceObject ejectedCargo = inventoryScreen.getLastEjectedCargo();
		Assert.assertEquals(Math.min(2 + q, 4), ejectedCargo.getCargoQuantity().getQuantityInAppropriateUnit());
		Assert.assertEquals(q, ejectedCargo.getCargoCanisterOverrideCount());
		Assert.assertEquals(100, ejectedCargo.getCargoPrice());

		scoopASlaveCargo(ejectedCargo); // scoop back
		testScoopedEjectedAmounts(1 + q,100, q,0);
	}

	private void testScoopedEjectedAmounts(long scoopedAmount, long price, int escapeCapsuleAmount, long ejectedAmount) {
		Assert.assertEquals("scoopedAmount", scoopedAmount * Unit.TONNE.getValue(), cobra.getScoopAmount());
		InventoryItem slaves = cobra.getInventoryItemByGood(SLAVES);
		Assert.assertEquals("price", price, slaves == null ? 0 : slaves.getPrice());
		Assert.assertEquals("escapeCapsuleAmount", escapeCapsuleAmount, cobra.getScoopedTonneEscapeCapsule());
		Assert.assertEquals(0, cobra.getScoopedTonneAlien());
		Assert.assertEquals(0, cobra.getScoopedTonnePlatlet());
		Assert.assertEquals("ejectedAmount", ejectedAmount * Unit.TONNE.getValue(), cobra.getEjectedAmount());
	}

	private int scoopASlaveCargo(SpaceObject cargo) {
		List<InventoryItem> slaves = cobra.getInventory();
		int w = slaves.isEmpty() ? 0 : slaves.get(0).getWeight().getQuantityInAppropriateUnit();
		cargo.setDisplayMatrix(matrix);
		inGameHelper.scoop(cargo);
		Assert.assertTrue(scooped);
		Assert.assertFalse(rammed);
		return slaves.get(0).getWeight().getQuantityInAppropriateUnit() - w;
	}

	private SpaceObject createCargoObject(TradeGood good, Weight weight) {
		final SpaceObject cargo = SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.CargoPod);
		cargo.setCargoContent(good, weight, weight);
		return cargo;
	}

	private SpaceObject createEscapeCapsuleObject() {
		return SpaceObjectFactory.getInstance().getRandomObjectByType(ObjectType.EscapeCapsule);
	}

}