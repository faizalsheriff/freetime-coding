package com.rockwellcollins.cs.hcms.core.services;

public interface MulticastServiceListener {
	void multicastServiceReceivedDatagramPacket(
			final MulticastService multicastService,
			final MulticastServiceReceivedDatagramPacketArgs args);

	void multicastServiceReceivedTimeout(
			final MulticastService multicastService,
			final MulticastServiceTimeoutArgs args);
}
