package com.rockwellcollins.cs.hcms.core.services;

import java.net.DatagramPacket;

import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

public class MessagingServiceReceiveMessageArgs {

	private DatagramPacket packet;

	private final Message message;

	public MessagingServiceReceiveMessageArgs(final Message message) {
		this.message = message;
	}

	public MessagingServiceReceiveMessageArgs(final Message message,
			final DatagramPacket packet) {
		this.message = message;
		this.packet = packet;
	}

	public DatagramPacket getDatagramPacket() {
		return this.packet;
	}

	public Message getMessage() {
		return message;
	}
}
