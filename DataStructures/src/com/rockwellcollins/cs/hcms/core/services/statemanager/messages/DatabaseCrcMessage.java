package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DatabaseCrcMessage extends StateManagerMessage {

	public static final byte CRC_TYPE_STATES = 0x01;
	public static final byte CRC_TYPE_VALUES = 0x02;
	public static final byte CRC_TYPE_BLOCKS = 0x03;

	private byte databaseCrcType;
	private int databaseCrc;
	private int databaseSignature;

	public DatabaseCrcMessage() {
		setType(TYPE_DATABASE_CRC);
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		out.writeByte(databaseCrcType);
		out.writeInt(databaseCrc);
		out.writeInt(databaseSignature);
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		databaseCrcType = in.readByte();
		databaseCrc = in.readInt();
		databaseSignature = in.readInt();
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		sb.append("{");
		sb.append(databaseCrc);
		sb.append(",");
		sb.append(databaseSignature);
		sb.append("}");
	}

	public final void setDatabaseCrcType(final byte databaseCrcType) {
		this.databaseCrcType = databaseCrcType;
	}

	public final byte getDatabaseCrcType() {
		return databaseCrcType;
	}

	public final void setDatabaseCrc(final int databaseCrc) {
		this.databaseCrc = databaseCrc;
	}

	public final int getDatabaseCrc() {
		return databaseCrc;
	}

	public void setDatabaseSignature(final int databaseSignature) {
		this.databaseSignature = databaseSignature;
	}

	public final int getDatabaseSignature() {
		return databaseSignature;
	}
}
