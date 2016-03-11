package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author kknight
 *
 */
public class SynchronizeMessage extends StateManagerMessage {

	private int[] indexes;
	private String[] values;
	private String[] names;
	private int[] stateCrcs;
	private long[] ages;
	private int count;
	/**
	 * Retained for compatibility with Protocol Version 8
	 */
	private String[] types; 
	private String[] rules;
	private boolean[] persistents;
	private boolean[] crcEnables;

	public SynchronizeMessage() {
		setType(TYPE_SYNCHRONIZE);
	}

	public final void setIndex(final int i, final int index) {
		indexes[i] = index;
	}

	public final int getIndex(final int i) {
		return indexes[i];
	}
	
	public final void setName(final int i, final String name) {
		names[i] = name;
	}

	public final String getName(final int i) {
		return names[i];
	}	

	public final void setValue(final int i, final String value) {
		values[i] = value;
	}

	public final String getValue(final int i) {
		return values[i];
	}

	public final void setAge(final int i, final long age) {
		ages[i] = age;
	}

	public final long getAge(final int i) {
		return ages[i];
	}

	public final void setCrc(final int i, final int crc) {
		stateCrcs[i] = crc;
	}

	public final int getCrc(final int i) {
		return stateCrcs[i];
	}

	public void createStates(int count) {
		indexes = new int[count];
		names = new String[count];
		values = new String[count];
		ages = new long[count];
		stateCrcs = new int[count];
		this.count = count;
		
		types = new String[count];
		rules = new String[count];
		persistents = new boolean[count];
		crcEnables = new boolean[count];
	}

	public int getStateCount() {
		return count;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		out.writeShort(count);

		for (int i = 0; i < count; i++) {
			out.writeLong(ages[i]);
			out.writeUTF(names[i]);
			out.writeInt(indexes[i]);
			out.writeUTF(""); 			// Empty type for Version 8
			out.writeUTF(values[i]);
			out.writeBoolean(false); 	// Empty persistent for Version 8
			out.writeUTF(""); 			// Empty rules for Version 8
			out.writeBoolean(false); 	// Empty crcEnable for Version 8
			out.writeInt(stateCrcs[i]);
		}
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		count = in.readShort();
		createStates(count);

		for (int i = 0; i < count; i++) {
			ages[i] = in.readLong();
			String name = in.readUTF();
			names[i] = name;
			indexes[i] = in.readInt();
			String type = in.readUTF();
			types[i] = type;
			String value = in.readUTF();
			values[i] = value;
			boolean persistent = in.readBoolean();
			persistents[i] = persistent;
			String rule = in.readUTF();
			rules[i] = rule;
			crcEnables[i] = in.readBoolean();
			stateCrcs[i] = in.readInt();
		}
	}

	@Override
	protected void onToString(final StringBuilder sb) {
		// TODO Auto-generated method stub
		super.onToString(sb);

		sb.append("[");

		for (int i = 0; i < count; i++) {
			sb.append("{");
			if (ages == null) {
				sb.append("Unknown");
			} else {
				sb.append(ages[i]);
			}
			sb.append(indexes[i]);
			sb.append(",");
			sb.append(names[i]);
			sb.append(",");
			sb.append(types[i]);
			sb.append(",");
			sb.append(persistents[i]);
			sb.append(",");
			sb.append(rules[i]);
			sb.append(",");
			sb.append(stateCrcs[i]);
			sb.append("}");
		}
		sb.append("]");
	}
}
