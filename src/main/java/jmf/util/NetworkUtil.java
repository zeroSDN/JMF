/*
 * Copyright 2015 ZSDN Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
