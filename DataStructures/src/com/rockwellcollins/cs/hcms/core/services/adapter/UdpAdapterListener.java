package com.rockwellcollins.cs.hcms.core.services.adapter;

import java.net.DatagramPacket;

public interface UdpAdapterListener {
	void udpAdapterReceivedPacket(UdpAdapter source, DatagramPacket packet);
}
