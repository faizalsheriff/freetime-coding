package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class DatabaseCrcRequestMessage extends StateManagerMessage {

	private InetAddress destInetAddress;

	public DatabaseCrcRequestMessage() {
		setType(TYPE_DATABASE_CRC_REQUEST);
	}

	public DatabaseCrcRequestMessage(final InetAddress destInetAddress) {
		setType(TYPE_DATABASE_CRC_REQUEST);
		this.destInetAddress = destInetAddress;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);
		
		writeInetAddress(out, destInetAddress);
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);
		
		destInetAddress = readInetAddress(in);
	}

	public InetAddress getDestInetAddress() {
		return destInetAddress;
	}

	public void setDestInetAddress(final InetAddress target) {
		this.destInetAddress = target;
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		if (destInetAddress != null) {
			sb.append("{");
			sb.append(destInetAddress);
			sb.append("}");
		}
	}
}
