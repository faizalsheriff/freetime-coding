package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BlockCrcMessage extends StateManagerMessage {

	private int[] blockIndexes;
	private int[] blockCrcs;
	private int blockCount;
	private int blockSize;
	private int databaseSize;
	private int databaseSignature;
	private int addIndex;

	public BlockCrcMessage() {
		setType(TYPE_BLOCK_CRC);
	}

	public final int[] getBlockIndexes() {
		return blockIndexes;
	}

	public final int[] getBlockCrcs() {
		return blockCrcs;
	}

	public final void createBlockCrcs(final int count) {
		blockCount = count;
		addIndex = 0;
		blockIndexes = new int[count];
		blockCrcs = new int[count];
	}

	public final void addBlockCrc(final int index, final int blockCrc) {
		blockIndexes[addIndex] = index;
		blockCrcs[addIndex] = blockCrc;
		addIndex++;
	}

	public final int getBlockCount() {
		return blockCount;
	}

	public final int getBlockSize() {
		return blockSize;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		out.writeInt(databaseSize);
		out.writeInt(databaseSignature);
		out.writeInt(blockSize);
		out.writeInt(blockCount);

		for (int i = 0; i < blockCount; i++) {
			out.writeInt(blockIndexes[i]);
			out.writeInt(blockCrcs[i]);
		}
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		databaseSize = in.readInt();
		databaseSignature = in.readInt();
		blockSize = in.readInt();
		blockCount = in.readInt();

		createBlockCrcs(blockCount);

		for (int i = 0; i < blockCount; i++) {
			blockIndexes[i] = in.readInt();
			blockCrcs[i] = in.readInt();
		}
	}

	public int getDatabaseSize() {
		return databaseSize;
	}

	public void setDatabaseSize(int databaseSize) {
		this.databaseSize = databaseSize;
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		super.onToString(sb);

		if (blockCrcs != null) {
			sb.append("{");
			sb.append("Size=" + blockCrcs.length);
			for (int i = 0; i < blockCount; i++) {
				sb.append(",");
				sb.append(blockIndexes[i]);
				sb.append("=");
				sb.append(Integer.toHexString(blockCrcs[i]).toUpperCase());
			}
			sb.append("}");
		}
	}

	public void setBlockSize(final int blockSize) {
		this.blockSize = blockSize;
	}

	public void setDatabaseSignature(final int databaseSignature) {
		this.databaseSignature = databaseSignature;
	}

	public int getDatabaseSignature() {
		return databaseSignature;
	}
}
