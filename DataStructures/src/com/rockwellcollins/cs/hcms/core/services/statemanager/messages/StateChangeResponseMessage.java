package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;

public class StateChangeResponseMessage extends StateManagerMessage {

	private String requestor;

	private StateMap stateMap;

	private InetAddress requestIp;

	private long requestId;

	private boolean duplicate;

	public StateChangeResponseMessage() {
		setType(TYPE_STATE_CHANGE_RESPONSE);
		duplicate = false;
	}

	public long getRequestId() {
		return requestId;
	}

	public InetAddress getRequestIp() {
		return requestIp;
	}

	public StateMap getStateMap() {
		return stateMap;
	}

	public void setRequestId(final long requestId) {
		this.requestId = requestId;
	}

	public void setRequestIp(final InetAddress requestIp) {
		this.requestIp = requestIp;
	}

	public void setStateMap(final StateMap stateMap) {
		this.stateMap = stateMap;
	}

	public void setDuplicate(final boolean duplicate) {
		this.duplicate = duplicate;
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	@Override
	protected void onGenerate(final DataOutputStream out) throws IOException {
		super.onGenerate(out);

		final int ipSize = requestIp != null ? getRequestIp().getAddress().length
				: 0;

		out.writeByte(ipSize);

		if (ipSize > 0) {
			out.write(requestIp.getAddress());
		}

		out.writeBoolean(duplicate);
		out.writeLong(requestId);
		out.writeShort(stateMap.size());

		for (final Entry<String, String> entry : stateMap.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}

		out.writeUTF(requestor);
	}

	@Override
	protected void onParse(final DataInputStream in) throws IOException {
		super.onParse(in);

		final int ipSize = in.readByte();

		if (ipSize > 0) {
			final byte[] ip = new byte[ipSize];

			for (int i = 0; i < ip.length; i++) {
				ip[i] = in.readByte();
			}
			requestIp = InetAddress.getByAddress(ip);
		}

		duplicate = in.readBoolean();
		requestId = in.readLong();

		final int size = in.readShort();

		stateMap = new StateMap();

		for (int i = 0; i < size; i++) {
			stateMap.put(in.readUTF(), in.readUTF());
		}

		requestor = in.readUTF();
	}

	@Override
	protected void onToString(final StringBuilder sb) {

		super.onToString(sb);

		sb.append("{");
		sb.append(requestIp);
		sb.append(",");
		sb.append(requestor);
		sb.append(",");
		sb.append(duplicate);
		sb.append(",");
		sb.append(requestId);
		if (stateMap != null) {
			sb.append(",");
			sb.append(stateMap);
		}
		sb.append("}");
	}

	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}

	public String getRequestor() {
		return requestor;
	}
}
