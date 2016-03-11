package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.rockwellcollins.cs.hcms.core.collections.IntArray;

/**
 * A block request message requests 1 to N number of blocks from the Master
 * StateManager. The Master StateManager will send SynchronizeMessage message
 * for each block requested.
 * 
 * @author getownse
 * @see SynchronizeMessage
 * @see StateManagerImpl
 * @see StateManager
 * 
 */
public class BlockRequestMessage extends StateManagerMessage {

	private IntArray blocks;

	/**
	 * Create a Block Request Message
	 */
	public BlockRequestMessage() {
		setType(TYPE_BLOCK_REQUEST);
	}

	/**
	 * @return a list of all the blocks to return a SynchronizeMessage for
	 */
	public IntArray getBlocks() {
		return blocks;
	}

	/**
	 * @param blocks
	 *            set the list of all block ids to request
	 */
	public void setBlocks(final IntArray blocks) {
		this.blocks = blocks;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		out.writeShort(blocks.length);

		for (int i = 0; i < blocks.length; i++) {
			out.writeShort(blocks.values[i]);
		}
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		final int size = in.readShort();

		blocks = new IntArray(size);

		for (int i = 0; i < size; i++) {
			blocks.add((int) in.readShort());
		}
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		if (blocks != null) {
			sb.append(blocks);
		}
	}

}