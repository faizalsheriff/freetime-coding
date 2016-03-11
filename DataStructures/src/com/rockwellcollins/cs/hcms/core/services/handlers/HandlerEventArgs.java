package com.rockwellcollins.cs.hcms.core.services.handlers;

public class HandlerEventArgs {

	private HandlerEvent event;

	private Object tag;

	public HandlerEventArgs(final HandlerEvent event) {
		this.event = event;
	}

	public HandlerEventArgs(final HandlerEvent event, final Object tag) {
		this.event = event;
		this.tag = tag;
	}

	public HandlerEvent getEvent() {
		return event;
	}

	public Object getTag() {
		return tag;
	}
}
