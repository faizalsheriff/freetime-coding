package com.rockwellcollins.cs.hcms.core;

import java.io.File;

public class UnitIOArgs {
	
	private File file;
	private boolean cancel;
	
	public UnitIOArgs() {
		
	}
	
	public UnitIOArgs(final File file) {
		this.file = file;
	}

	public final void setFile(File file) {
		this.file = file;
	}

	public final File getFile() {
		return file;
	}

	public final void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public final boolean isCancel() {
		return cancel;
	}
}
