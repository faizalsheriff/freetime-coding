package com.rockwellcollins.cs.hcms.core.services;

import java.net.DatagramPacket;

public class MulticastServiceReceivedDatagramPacketArgs {
	private DatagramPacket datagramPacket;

	public MulticastServiceReceivedDatagramPacketArgs(
			final DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
	}

	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
}
