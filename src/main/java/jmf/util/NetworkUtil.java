package jmf.util;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Network utilities for finding out information about the network environment (local IP)
 * Created on 7/25/15.
 * @author Jan Strauß
 */
public class NetworkUtil {

	public static String getLocalIp(final String interfaceName, final boolean useIpv6) {
		try {
			final NetworkInterface netInterface = NetworkInterface.getByName(interfaceName);
			final ArrayList<InetAddress> addresses = Collections.list(netInterface.getInetAddresses());

			for (final InetAddress address : addresses) {
				if (address instanceof Inet4Address && !useIpv6) {
					return address.getHostAddress();
				} else if (address instanceof Inet6Address && useIpv6) {
					return address.getHostAddress();
				}
			}
		} catch (final SocketException e) {
			throw new IllegalStateException(e);
		}
		throw new IllegalStateException("no suitable local address found");
	}

	public static String getLocalIp(final boolean useIpv6) {
		try {
			final ArrayList<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (final NetworkInterface netInterface : networkInterfaces) {
				if (!netInterface.isLoopback() && !netInterface.isPointToPoint()) {
					final ArrayList<InetAddress> addresses = Collections.list(netInterface.getInetAddresses());
					for (final InetAddress address : addresses) {
						if (address instanceof Inet4Address && !useIpv6) {
							return address.getHostAddress();
						} else if (address instanceof Inet6Address && useIpv6) {
							return address.getHostAddress();
						}
					}
				}
			}
		} catch (final SocketException e) {
			throw new IllegalStateException(e);
		}
		throw new IllegalStateException("no suitable local address found");
	}

	public static void main(final String[] args) throws UnknownHostException, SocketException {
		System.out.println(getLocalIp(false));
	}
}
