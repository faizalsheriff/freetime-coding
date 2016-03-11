package com.rockwellcollins.cs.hcms.core;

import java.util.Map;

import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartupType;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingService;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateHandler;

public class DefaultUnitManagerLoader extends UnitManagerLoader {

	private final StringBuilder report = new StringBuilder();

	@Override
	protected void onComplete() throws CoreException {

		/*
		 * Auto start Service with Auto start set to True or if State Manager is
		 * already synchronized and Auto start is synchronized
		 */
		for (Component component : UnitManager.ObjectModel.components) {

			if (component instanceof Service) {

				Service service = (Service) component;

				if (service.getStartupType() == ServiceStartupType.TRUE
						&& service.getServiceState() == ServiceState.STOPPED) {
					try {
						service.start();

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging
									.logCore("UnitManager started AUTOSTART service '"
											+ service.getName() + "'");
						}

					} catch (final Exception e) {
						report.append("### Service '" + service.getName()
								+ "' could not Autostart ### >> ");
						report.append(UnitManager.Logging.getStackTrace(e));
					}
				}
			}
		}

		if (report.length() > 0) {
			UnitManager.Logging
					.logSevere("### Unit Manager Loader Exceptions ### >> "
							+ report.toString());
		}

		/**
		 * Forcing Garbage Collection to Cleanup all Unused Objects before
		 * Continuing
		 */
		System.gc();
	}

	@Override
	protected void onInitialize(final Map<String, String> properties)
			throws CoreException {

	}

	@Override
	protected void onLoad() throws CoreException {

	}

	@Override
	protected void onSetup() throws CoreException {

		UnitManager.ObjectModel.components.sort();

		ComponentContainer components = (ComponentContainer) UnitManager.ObjectModel.components
				.clone();

		for (final Component component : components) {
			onSetup(component);
		}
	}

	@Override
	protected void onVerify() throws CoreException {

		/*
		 * Verify Unit and StateManager exist
		 */
		if (UnitManager.ObjectModel.getUnit() == null) {
			UnitManager.Logging
					.logSevere("Unit is not found in Unit Manager.  Make sure Unit is getting loaded in the Lcp.  Creating a default for workaround.");
			UnitManager.ObjectModel.unit = new Unit();
		}

		if (UnitManager.ObjectModel.getStateManager() == null) {
			UnitManager.Logging
					.logSevere("State Manager is not found in Unit Manager.  Make sure State Manager is getting loaded in the Lcp.  Creating a default for workaround.");
			UnitManager.ObjectModel.stateManager = new StateManager();
			UnitManager.ObjectModel.stateManager
					.setStartupType(ServiceStartupType.TRUE);
		}

		ComponentContainer components = (ComponentContainer) UnitManager.ObjectModel.components
				.clone();

		for (final Component component : components) {
			try {
				component.verify();
			} catch (final Exception e) {
				report
						.append("### Component '"
								+ component.getName()
								+ "' FAILED for Verify. Removing from Object Model and Destroying ### >> ");
				report.append(UnitManager.Logging.getStackTrace(e));
				UnitManager.ObjectModel.getComponents().remove(component);
				component.destroy();
			}
		}
	}

	@Override
	protected void onProgressChange(UnitManagerStep step) throws CoreException {

	}

	protected void onSetup(final Component component) {

		try {
			component.setup();
		} catch (final Exception e) {
			report
					.append("### Component '"
							+ component.getName()
							+ "' FAILED to Setup. Removing from Object Model and Destroying ### >> ");
			report.append(UnitManager.Logging.getStackTrace(e));
			UnitManager.ObjectModel.components.remove(component);
			try {
				component.destroy();
			} catch (final ComponentDestroyException de) {
				report.append("### Component '" + component.getName()
						+ "' + could not be destroyed ### >> ");
				report.append(UnitManager.Logging.getStackTrace(de));

			}
		}
		/*
		 * Set known components in ObjectModel
		 */
		if (component instanceof Unit) {

			UnitManager.ObjectModel.unit = (Unit) component;

		} else if (component instanceof LoggingService) {

			UnitManager.ObjectModel.loggingService = (LoggingService) component;

		} else if (component instanceof StateManager) {

			UnitManager.ObjectModel.stateManager = (StateManager) component;

		} else if (component instanceof UpdateHandler) {

			UnitManager.ObjectModel.updateHandler = (UpdateHandler) component;

		}

	}
}
