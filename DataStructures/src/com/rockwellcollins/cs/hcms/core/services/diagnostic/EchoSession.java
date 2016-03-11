package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import java.net.InetAddress;

public class EchoSession {

	final public InetAddress ip;
	final public int port;
	public long id;
	public long expire;

	public EchoSession(final InetAddress ip, final int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public EchoSession(final InetAddress ip, final int port, final long expire) {
		this.ip = ip;
		this.port = port;
		this.expire = expire;
	}
}
