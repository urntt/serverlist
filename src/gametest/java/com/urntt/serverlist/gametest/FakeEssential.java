package com.urntt.serverlist.gametest;

/**
 * Switch of {@code FakeEssentialServerListMixin}, which imitates the method Essential adds to the server list
 * screen. A non-null handler means Essential's own drag-and-drop is active.
 */
public final class FakeEssential {
	public static volatile Object dragHandler;

	private FakeEssential() {
	}
}
