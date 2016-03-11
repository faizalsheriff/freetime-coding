package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;

public class StatusMessage extends StateManagerMessage {

	private int processQueueSize;

	private byte status;

	private int bully;

	private int electionPriority;

	private int persistentDataCrc;
	
	private int signatureCrc;

	private boolean majorityPersistentData;

	public boolean isMajorityPersistentData() {
		return majorityPersistentData;
	}

	public void setMajorityPersistentData(final boolean majorityPersistentData) {
		this.majorityPersistentData = majorityPersistentData;
	}
	
	public int getSignatureCrc() {
		return signatureCrc;
	}
	
	public void setSignatureCrc(final int signatureCrc) {
		this.signatureCrc = signatureCrc;
	}

	public int getPersistentDataCrc() {
		return persistentDataCrc;
	}

	public void setPersistentDataCrc(final int persistentDataCrc) {
		this.persistentDataCrc = persistentDataCrc;
	}

	public StatusMessage() {
		setType(TYPE_STATUS);
	}

	public int getBully() {
		return bully;
	}

	public int getElectionPriority() {
		return electionPriority;
	}

	public int getProcessQueueSize() {
		return processQueueSize;
	}

	public StateManagerStatus getStatus() {
		switch (status) {
		case 0x01:
			return StateManagerStatus.ONLINE;
		case 0x02:
			return StateManagerStatus.LOADING;
		case 0x03:
			return StateManagerStatus.SYNCHRONIZED;
		case 0x04:
			return StateManagerStatus.MASTER;
		case 0x05:
			return StateManagerStatus.RECOVER;
		case 0x06:
			return StateManagerStatus.CANDIDATE;
		default:
			return StateManagerStatus.ERROR;
		}
	}

	public void setBully(final int bully) {
		this.bully = bully;
	}

	public void setElectionPriority(final int electionPriority) {
		this.electionPriority = electionPriority;
	}

	public void setProcessQueueSize(final int processQueueSize) {
		this.processQueueSize = processQueueSize;
	}

	public void setStatus(final StateManagerStatus status) {
		switch (status) {
		case ONLINE:
			this.status = 0x01;
			break;
		case LOADING:
			this.status = 0x02;
			break;
		case SYNCHRONIZED:
			this.status = 0x03;
			break;
		case MASTER:
			this.status = 0x04;
			break;
		case RECOVER:
			this.status = 0x05;
			break;
		case CANDIDATE:
			this.status = 0x06;
			break;
		default:
			this.status = 0x00;
			break;
		}
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);
		out.writeByte(status);
		out.writeShort(processQueueSize);
		out.writeInt(bully);
		out.writeShort(electionPriority);
		out.writeInt(persistentDataCrc);
		out.writeBoolean(majorityPersistentData);
		out.writeInt(signatureCrc);
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);
		status = in.readByte();
		processQueueSize = in.readShort();
		bully = in.readInt();
		electionPriority = in.readShort();
		persistentDataCrc = in.readInt();
		majorityPersistentData = in.readBoolean();
		signatureCrc = in.readInt();
	}

	@Override
	protected void onToString(final StringBuilder sb) {
		super.onToString(sb);

		sb.append("{");
		sb.append(getStatus());
		sb.append(",");
		sb.append(processQueueSize);
		sb.append(",");
		sb.append(bully);
		sb.append(",");
		sb.append(electionPriority);
		sb.append(",");
		sb.append(persistentDataCrc);
		sb.append(",");
		sb.append(signatureCrc);
		sb.append("}");
	}
}
