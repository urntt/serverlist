package com.urntt.serverlist.ping;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Adjusts the thread pool that vanilla's server list uses to ping servers.
 *
 * <p>Vanilla pings at most five servers at a time; the others wait in the queue, so a few slow or unreachable
 * servers keep the rest at "Pinging...". With the setting enabled, more pings run at once. The number of
 * threads stays bounded, no ping is ever dropped, and idle threads time out.
 */
public final class PingerThreads {
	/** Pings that may run at once with the setting enabled. */
	public static final int FASTER_THREADS = 32;
	/** How long an idle pinger thread lives with the setting enabled. */
	private static final long IDLE_TIMEOUT_SECONDS = 30;

	private static int vanillaThreads = -1;
	private static long vanillaKeepAliveNanos;

	private PingerThreads() {
	}

	/**
	 * Configures {@code pool} for the given setting. A pool that is not vanilla's type was replaced by another
	 * mod and is left alone. Turning the setting off restores vanilla's configuration; extra idle threads then
	 * stop right away, and busy ones when their ping is done.
	 */
	public static synchronized void configure(final ThreadPoolExecutor pool, final boolean faster) {
		if (!(pool instanceof ScheduledThreadPoolExecutor)) {
			return;
		}

		if (vanillaThreads < 0) {
			vanillaThreads = pool.getCorePoolSize();
			vanillaKeepAliveNanos = pool.getKeepAliveTime(TimeUnit.NANOSECONDS);
		}

		if (faster) {
			pool.setKeepAliveTime(IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			pool.allowCoreThreadTimeOut(true);
			pool.setCorePoolSize(FASTER_THREADS);
		} else if (pool.getCorePoolSize() != vanillaThreads) {
			pool.allowCoreThreadTimeOut(false);
			pool.setKeepAliveTime(vanillaKeepAliveNanos, TimeUnit.NANOSECONDS);
			pool.setCorePoolSize(vanillaThreads);
		}
	}
}
