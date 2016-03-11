package com.rockwellcollins.cs.hcms.core.services;

public interface ServiceListener {
	public void serviceStarted(Service service, ServiceStartArgs args);
	public void serviceStopped(Service service, ServiceStopArgs args);
}
