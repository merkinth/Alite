package de.phbouillon.android.games.alite.model;

import de.phbouillon.android.games.alite.model.trading.TradeGood;

public class InventoryItem {
	private TradeGood good;
	private Weight weight = Weight.grams(0);
	private Weight unpunished = Weight.grams(0);
	private long totalBuyPrice;

	public InventoryItem(TradeGood good) {
		this.good = good;
	}

	public void add(Weight w, long price) {
		totalBuyPrice += price;
		weight = weight.add(w);
	}

	public void set(Weight w, long price) {
		totalBuyPrice = price;
		weight = weight.set(w);
	}

	public void addUnpunished(Weight w) {
		unpunished = unpunished.add(w);
	}

	public void subUnpunished(Weight weight) {
		unpunished = unpunished.sub(weight);
		if (unpunished.getWeightInGrams() < 0) {
			unpunished = Weight.grams(0);
		}
	}

	public void resetUnpunished() {
		unpunished = Weight.grams(0);
	}

	public Weight getUnpunished() {
		return unpunished;
	}

	public long getPrice() {
		return totalBuyPrice;
	}

	public Weight getWeight() {
		return weight;
	}

	public TradeGood getGood() {
		return good;
	}
}
