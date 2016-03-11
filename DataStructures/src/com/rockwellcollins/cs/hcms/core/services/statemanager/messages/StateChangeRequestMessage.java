package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;

public class StateChangeRequestMessage extends StateManagerMessage {

	private String requestor;

	private StateMap stateMap;

	private int retry;

	private long retryTime;
	
	private boolean conflict;
	
	private boolean duplicate;

	private long queueTime;
	
	private long sendTime;

	public StateChangeRequestMessage() {
		setType(TYPE_STATE_CHANGE_REQUEST);
		retry = 0;
		conflict = false;
		duplicate = false;
	}
	
	public void setSendTime(long currTime) {
		this.sendTime = currTime;
	}
	
	public long getSendTime() {
		return this.sendTime;
	}
	
	public void setQueueTime(long currTime) {
		this.queueTime = currTime;
	}
	
	public long getQueueTime() {
		return this.queueTime;
	}
	
	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}
	
	public boolean isConflict() {
		return conflict;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
	
	public boolean isDuplicate() {
		return duplicate;
	}

	public String getRequestor() {
		return requestor;
	}

	public int getRetry() {
		return retry;
	}

	public long getRetryTime() {
		return retryTime;
	}

	public StateMap getStateMap() {
		return stateMap;
	}

	public void setRequestor(final String requestor) {
		this.requestor = requestor;
	}

	public void setRetry(final int retry) {
		this.retry = retry;
	}

	public void setRetryTime(final long retryTime) {
		this.retryTime = retryTime;
	}

	public void setStateMap(final StateMap stateMap) {
		this.stateMap = stateMap;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		out.writeShort(retry);
		out.writeLong(retryTime);
		out.writeUTF(requestor);

		out.writeShort(stateMap.size());

		for (final Entry<String, String> entry : stateMap.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		retry = in.readShort();
		retryTime = in.readLong();
		requestor = in.readUTF();

		final int size = in.readShort();
		stateMap = new StateMap();

		for (int i = 0; i < size; i++) {
			stateMap.put(in.readUTF(), in.readUTF());
		}
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		sb.append("{");
		sb.append(retry);
		sb.append(",");
		sb.append(retryTime);
		sb.append(",");
		sb.append(requestor);
		if (stateMap != null) {
			sb.append(",");
			sb.append(stateMap);
		}
		sb.append("}");
	}
}
