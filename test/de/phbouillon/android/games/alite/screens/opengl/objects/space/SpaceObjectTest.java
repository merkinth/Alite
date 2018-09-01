package de.phbouillon.android.games.alite.screens.opengl.objects.space;

import org.junit.Assert;
import org.junit.Test;

public class SpaceObjectTest {

	private static final int NUMBER_OF_RUNS = 1000;

	@Test
	public void createRandomEnemy() {
		int[] galaxySeed = new int[] { 0x5, 0xB, 0x6, 0xD, 0xA, 0x4, 0x9, 0x2 };
		int[] statistics = new int[13];
		for (int galaxy=0; galaxy<8; galaxy++) {
			for (int i=0; i<13; i++) {
				statistics[i] = 0;
			}
			for (int i = 0; i< NUMBER_OF_RUNS; i++) {
				statistics[createRandomEnemyAlgorithm(galaxySeed[galaxy])]++;
			}
			for (int i=0; i<13; i++) {
				String msg = i == 11 ?  checkBetween(statistics[i], 0, 3) :
						i == 12 ?  checkBetween(statistics[i], 0, 4) :
						galaxy == 0 ? i >= 9 ? checkBetween(statistics[i], 0, 0) :
						checkBetween(statistics[i], 6, 15) : checkBetween(statistics[i], 4, 13);
				if (!msg.isEmpty()) {
					Assert.fail("Galaxy #" + (galaxy + 1) + " type " + i + " should be " + msg +
						" but was " + 100.0f * statistics[i] / NUMBER_OF_RUNS);
				}
			}
		}
	}

	private int createRandomEnemyAlgorithm(int extraShip) {
		int type = (int) (Math.random() * 100);
		if (type == 0) { // 1%
			return 11; // CobraMkIII
		}
		if (type < 3) { // 2%
			return 12; // CobraMkI
		}
		return (int) (Math.random() * (9 + (extraShip == 5 || extraShip == 8 ? 0 : 2)));
	}

	private String checkBetween(int value, int min, int max) {
		value = (int) (100.0f * value / NUMBER_OF_RUNS);
		return value >= min && value <= max ? "" :
			min == max ? Integer.toString(min) : "between " + min + "% and " + max + "%";
	}
}