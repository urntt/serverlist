package com.urntt.serverlist.gametest;

import java.util.concurrent.CountDownLatch;

/**
 * Servers whose pings hold a pinger thread, like servers behind a slow DNS lookup. Pings to an address ending in
 * {@link #HOST_SUFFIX} wait at the start of the ping until {@link #release()} (see {@code SlowServerPingMixin}).
 */
public final class SlowServers {
	public static final String HOST_SUFFIX = ".slow.serverlist.test";

	private static volatile CountDownLatch gate = new CountDownLatch(0);

	private SlowServers() {
	}

	/** Makes pings to slow servers wait until {@link #release()}. */
	public static void hold() {
		gate = new CountDownLatch(1);
	}

	public static void release() {
		gate.countDown();
	}

	public static CountDownLatch gate() {
		return gate;
	}
}
