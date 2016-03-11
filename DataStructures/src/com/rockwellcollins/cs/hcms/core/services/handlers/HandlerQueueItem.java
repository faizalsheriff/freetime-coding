package com.rockwellcollins.cs.hcms.core.services.handlers;

import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;

public class HandlerQueueItem {

	private HandlerEvent event;

	private HandlerQueueItemType type;

	private Object tag;

	private StateMap stateMap;

	public HandlerQueueItem(final StateMap stateMap) {
		this.stateMap = stateMap;
		this.type = HandlerQueueItemType.STATE_CHANGE;
	}

	public HandlerQueueItem(final HandlerEvent event) {
		this.event = event;
		this.type = HandlerQueueItemType.EVENT;
	}

	public HandlerQueueItem(final HandlerEvent event, final Object tag) {
		this.event = event;
		this.tag = tag;
		this.type = HandlerQueueItemType.EVENT;
	}

	public final StateMap getStateMap() {
		return stateMap;
	}

	public final HandlerQueueItemType getType() {
		return type;
	}

	public final HandlerEvent getEvent() {
		return event;
	}

	public final Object getTag() {
		return tag;
	}
}
