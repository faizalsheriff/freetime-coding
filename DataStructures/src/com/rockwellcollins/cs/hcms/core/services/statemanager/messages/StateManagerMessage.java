package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.rockwellcollins.cs.hcms.core.Unit;
import com.rockwellcollins.cs.hcms.core.UnitManager;

public class StateManagerMessage {

	public static final short VERSION = 11;

	public static final int NUM_TYPES = 11;

	public static final byte TYPE_UNKNOWN = 0x00;

	public static final byte TYPE_STATUS = 0x01;

	public static final byte TYPE_BLOCK_CRC_REQUEST = 0x02;

	public static final byte TYPE_BLOCK_CRC = 0x03;

	public static final byte TYPE_STATE_CHANGE_REQUEST = 0x04;

	public static final byte TYPE_STATE_CHANGE_RESPONSE = 0x05;

	public static final byte TYPE_BLOCK_REQUEST = 0x06;

	public static final byte TYPE_SYNCHRONIZE = 0x07;

	public static final byte TYPE_CONTROL = 0x08;

	public static final byte TYPE_DATABASE_CRC_REQUEST = 0x09;

	public static final byte TYPE_DATABASE_CRC = 0x0A;

	private static long gid;

	public final static StateManagerMessage create(
			final ByteArrayInputStream bin) throws IOException {

		StateManagerMessage message = null;

		final DataInputStream in = new DataInputStream(bin);

		final byte type = in.readByte();
		final int version = in.readShort();
		
		if (version == VERSION) {

			switch (type) {
			case TYPE_STATUS:
				message = new StatusMessage();
				break;
			case TYPE_BLOCK_CRC_REQUEST:
				message = new BlockCrcRequestMessage();
				break;
			case TYPE_BLOCK_CRC:
				message = new BlockCrcMessage();
				break;
			case TYPE_STATE_CHANGE_REQUEST:
				message = new StateChangeRequestMessage();
				break;
			case TYPE_STATE_CHANGE_RESPONSE:
				message = new StateChangeResponseMessage();
				break;
			case TYPE_BLOCK_REQUEST:
				message = new BlockRequestMessage();
				break;
			case TYPE_SYNCHRONIZE:
				message = new SynchronizeMessage();
				break;
			case TYPE_CONTROL:
				message = new ControlMessage();
				break;
			case TYPE_DATABASE_CRC:
				message = new DatabaseCrcMessage();
				break;
			case TYPE_DATABASE_CRC_REQUEST:
				message = new DatabaseCrcRequestMessage();
				break;
			default:
				message = new StateManagerMessage();
			}
	
			message.onParse(in);
	
			final Unit unit = UnitManager.ObjectModel.getUnit();
	
			if (unit != null) {
				message.sender = UnitManager.ObjectModel.getUnit().getInetAddress()
						.equals(message.sourceInetAddress);
			}
		}
		return message;
	}

	public static long getNextId() {
		return gid++;
	}

	private boolean sender;

	private InetAddress sourceInetAddress;

	private byte type;

	private long id;

	private int version;

	public StateManagerMessage() {
		version = VERSION;
		Unit unit = UnitManager.ObjectModel.getUnit();
		if (unit != null) {
			sourceInetAddress = unit.getInetAddress();
		}
	}

	public final long getId() {
		return id;
	}

	public final byte getType() {
		return type;
	}

	public final int getVersion() {
		return version;
	}

	public final boolean isSender() {
		return this.sender;
	}

	public final void setSourceInetAddress(final InetAddress sourceInetAddress) {
		this.sourceInetAddress = sourceInetAddress;
	}

	public final InetAddress getSourceInetAddress() {
		return sourceInetAddress;
	}

	public final byte[] toBytes() {

		final ByteArrayOutputStream bout = new ByteArrayOutputStream(255);
		final DataOutputStream out = new DataOutputStream(bout);

		try {
			out.write(type);
			onGenerate(out);
			out.flush();

		} catch (final IOException e) {
			UnitManager.Logging.logSevere("State Manager Message '"
					+ toString() + "' could not generate data in toBytes.", e);
		}

		return bout.toByteArray();
	}

	protected void onGenerate(final DataOutputStream out) throws IOException {
		out.writeShort(VERSION);
		out.writeLong(id);
		writeInetAddress(out, sourceInetAddress);
	}

	protected void onParse(final DataInputStream in) throws IOException {
		id = in.readLong();
		sourceInetAddress = readInetAddress(in);
	}

	@Override
	public final String toString() {

		final StringBuilder sb = new StringBuilder();

		onToString(sb);

		return sb.toString();
	}

	protected void onToString(final StringBuilder sb) {

		switch (getType()) {
		case TYPE_STATUS:
			sb.append("STATUS");
			break;
		case TYPE_BLOCK_CRC_REQUEST:
			sb.append("CRC_REQUEST");
			break;
		case TYPE_BLOCK_CRC:
			sb.append("CRC");
			break;
		case TYPE_STATE_CHANGE_REQUEST:
			sb.append("STATE_CHANGE_REQUEST");
			break;
		case TYPE_STATE_CHANGE_RESPONSE:
			sb.append("STATE_CHANGE_RESPONSE");
			break;
		case TYPE_BLOCK_REQUEST:
			sb.append("BLOCK_REQEUST");
			break;
		case TYPE_SYNCHRONIZE:
			sb.append("STATE_SYNC");
			break;
		case TYPE_DATABASE_CRC:
			sb.append("DATABASE_CRC");
			break;
		case TYPE_DATABASE_CRC_REQUEST:
			sb.append("DATABASE_CRC_REQUEST");
			break;
		default:
			sb.append("UNKNOWN");
			break;
		}

		sb.append("[V:");
		sb.append(getVersion());
		sb.append(",ID:");
		sb.append(id);
		sb.append(",SRC:");
		sb.append(getSourceInetAddress());
		sb.append("]");
	}

	protected final void setType(final byte type) {
		this.type = type;
	}

	public final void nextId() {
		this.id = getNextId();
	}

	public final static void writeInetAddress(final DataOutputStream out,
			final InetAddress addr) throws IOException {

		final byte[] b = addr.getAddress();
		if (b == null) {
			out.writeByte(0);
		} else {
			out.writeByte(b.length);
			for (final byte element : b) {
				out.writeByte(element);
			}
		}
	}

	public final static InetAddress readInetAddress(final DataInputStream in)
			throws IOException, UnknownHostException {

		InetAddress addr = null;

		final int size = in.readByte();

		if (size > 0) {
			final byte[] address = new byte[size];

			for (int i = 0; i < size; i++) {
				address[i] = in.readByte();
			}

			addr = InetAddress.getByAddress(address);
		}

		return addr;
	}
}
