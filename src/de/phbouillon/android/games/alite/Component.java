package de.phbouillon.android.games.alite;

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;

public abstract class Component<E> {
	@FunctionalInterface
	public interface OnEvent<E> {
		void perform(E self);
	}

	protected OnEvent<E> onEvent;
	private int fingerDown = 0;

	@SuppressWarnings("unchecked")
	public E setEvent(OnEvent<E> onEvent) {
		this.onEvent = onEvent;
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public void onEvent() {
		if (onEvent != null) {
			onEvent.perform((E) this);
		}
	}

	abstract public boolean checkEvent(TouchEvent e);
	abstract public void render(Graphics g);

	void clearFingerDown() {
		fingerDown = 0;
	}

	void fingerDown(int pointer) {
		fingerDown = TouchEvent.fingerDown(fingerDown, pointer);
	}

	void fingerUp(int pointer) {
		fingerDown = TouchEvent.fingerUp(fingerDown, pointer);
	}

	boolean isDown(int pointer) {
		return TouchEvent.isDown(fingerDown, pointer);
	}

	boolean isDown() {
		return TouchEvent.isDown(fingerDown);
	}

}
