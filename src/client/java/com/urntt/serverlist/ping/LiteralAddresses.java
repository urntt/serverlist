package com.urntt.serverlist.ping;

import com.google.common.net.InetAddresses;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;

/**
 * Keeps literal IP addresses from triggering reverse DNS lookups.
 *
 * <p>Vanilla resolves "1.2.3.4" with {@link InetAddress#getByName(String)}, which leaves the host name unset, and
 * then asks for the host name when it checks the address against the server block list, which performs a
 * blocking reverse DNS lookup. Giving the address the literal as its host name skips that lookup. Ping and
 * connection go to the same IP address as before.
 */
public final class LiteralAddresses {
	private LiteralAddresses() {
	}

	/**
	 * Returns {@code resolved} with {@code host} as its host name when {@code host} is a literal IP address, or
	 * {@code resolved} unchanged otherwise. Scoped IPv6 addresses are left unchanged, because rebuilding them
	 * would lose the scope.
	 */
	public static ResolvedServerAddress withLiteralHostName(final String host, final ResolvedServerAddress resolved) {
		if (!InetAddresses.isInetAddress(host)) {
			return resolved;
		}

		InetSocketAddress socketAddress = resolved.asInetSocketAddress();
		InetAddress address = socketAddress.getAddress();
		if (address == null || address instanceof Inet6Address ipv6 && ipv6.getScopeId() != 0) {
			return resolved;
		}

		try {
			InetAddress named = InetAddress.getByAddress(host, address.getAddress());
			return ResolvedServerAddress.from(new InetSocketAddress(named, socketAddress.getPort()));
		} catch (UnknownHostException e) {
			// Only thrown for an address of illegal length, which a resolved address cannot have.
			return resolved;
		}
	}
}
