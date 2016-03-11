package com.rockwellcollins.cs.hcms.core.services.statemanager.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Control Messages are Multicast messages that can control the system. The
 * messages is created in the following format:
 * 
 * byte: Command (0x01: ELECT, 0x02: REJECT, 0x03: ELECT PRIORITY, 0x04: BULLY)
 * byte: Number of IPs to Execute Command (repeat next) bytex4: IP of the Unit
 * to Execute Command
 * 
 * 
 * @author getownse
 * 
 */
public class ControlMessage extends StateManagerMessage {

	final private List<InetAddress> targets = new ArrayList<InetAddress>();
	private ControlCommand command;
	private int electionPriority;
	private int bully;
	private boolean global;

	public ControlMessage() {
		setType(TYPE_CONTROL);
	}

	@Override
	protected void onGenerate(DataOutputStream out) throws IOException {
		super.onGenerate(out);
		
		switch (getCommand()) {
		case Elect:
			out.writeByte(0x01);
			break;
		case Reject:
			out.writeByte(0x02);
			break;
		case ElectPriority:
			out.writeByte(0x03);
			break;
		case Bully:
			out.writeByte(0x04);
			break;
		default:
			out.writeByte(0x00);
		}
		
		out.writeBoolean(isGlobal());
		
		synchronized (targets) {
			out.writeInt(targets.size());
			for (InetAddress addr : targets) {
				writeInetAddress(out, addr);
			}
		}
	}

	@Override
	protected void onParse(DataInputStream in) throws IOException {
		super.onParse(in);

		switch (in.readByte()) {
		case 0x01:
			setCommand(ControlCommand.Elect);
			break;
		case 0x02:
			setCommand(ControlCommand.Reject);
			break;
		case 0x03:
			setCommand(ControlCommand.ElectPriority);
			setElectionPriority(in.readInt());
			break;
		case 0x04:
			setCommand(ControlCommand.Bully);
			setBully(in.readInt());
			break;
		default:
			setCommand(ControlCommand.Unknown);
		}
		
		setGlobal(in.readBoolean());
		
		synchronized (targets) {
			targets.clear();

			final int nAddr = in.readInt();
			for (int i = 0; i < nAddr; i++) {
				targets.add(readInetAddress(in));
			}
		}

	}

	public void setCommand(ControlCommand command) {
		this.command = command;
	}

	public ControlCommand getCommand() {
		return command;
	}

	public void setElectionPriority(int electionPriority) {
		this.electionPriority = electionPriority;
	}

	public int getElectionPriority() {
		return electionPriority;
	}

	public void setBully(int bully) {
		this.bully = bully;
	}

	public int getBully() {
		return bully;
	}

	public boolean isTarget(final InetAddress addr) {
		boolean result = isGlobal();

		if (!result) {
			synchronized (targets) {
				for (InetAddress target : targets) {
					if (target.equals(addr)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public void addTarget(final InetAddress addr) {
		synchronized (targets) {
			targets.add(addr);
		}
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public boolean isGlobal() {
		return global;
	}
}
