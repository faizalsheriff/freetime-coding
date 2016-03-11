package com.rockwellcollins.cs.hcms.core;

import java.util.Map;

public abstract class UnitManagerLoader {
	protected String LCPPathName;

	protected enum UnitManagerStep {
		Initialize, Load, Setup, Verify, Complete
	}

	public final void initialize(final Map<String, String> properties)
			throws CoreException {
		onProgressChange(UnitManagerStep.Initialize);
		onInitialize(properties);
	}

	public final void load() throws CoreException {
		onProgressChange(UnitManagerStep.Load);
		onLoad();
	}

	public final void setup() throws CoreException {
		onProgressChange(UnitManagerStep.Setup);
		onSetup();
	}

	public final void complete() throws CoreException {
		onProgressChange(UnitManagerStep.Complete);
		onComplete();
	}

	public final void verify() throws CoreException {
		onProgressChange(UnitManagerStep.Verify);
		onVerify();
	}
	public final String getLoadedLCPName(){
		return LCPPathName;
	}
	abstract protected void onLoad() throws CoreException;

	abstract protected void onComplete() throws CoreException;

	abstract protected void onInitialize(Map<String, String> properties)
			throws CoreException;

	abstract protected void onSetup() throws CoreException;

	abstract protected void onSetup(final Component component)
			throws CoreException;

	abstract protected void onVerify() throws CoreException;

	abstract protected void onProgressChange(final UnitManagerStep step)
			throws CoreException;

}
