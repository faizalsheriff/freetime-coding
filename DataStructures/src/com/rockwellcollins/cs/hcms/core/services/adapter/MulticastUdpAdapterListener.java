package com.rockwellcollins.cs.hcms.core.services.adapter;

import java.net.DatagramPacket;

public interface MulticastUdpAdapterListener {
	void udpAdapterReceivedPacket(MulticastUdpAdapter source, DatagramPacket packet);
}
