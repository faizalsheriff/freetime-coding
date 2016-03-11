package com.rockwellcollins.cs.hcms.core;

import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;

public class UnitEvent {
	private Object tag;
	private boolean cancel;
	private CountdownTimer timer;
	private boolean repeat;
	private UnitEventCallback callback;

	public final void setTag(final Object tag) {
		this.tag = tag;
	}

	public final Object getTag() {
		return tag;
	}

	public final void setCancel(final boolean cancel) {
		this.cancel = cancel;
	}

	public final boolean isCancel() {
		return cancel;
	}

	public final void setTimer(final CountdownTimer timer) {
		this.timer = timer;
	}

	public final CountdownTimer getTimer() {
		return timer;
	}

	public final void setRepeat(final boolean repeat) {
		this.repeat = repeat;
	}

	public final boolean isRepeat() {
		return repeat;
	}

	public final void setCallback(final UnitEventCallback callback) {
		this.callback = callback;
	}

	public final UnitEventCallback getCallback() {
		return callback;
	}
}
