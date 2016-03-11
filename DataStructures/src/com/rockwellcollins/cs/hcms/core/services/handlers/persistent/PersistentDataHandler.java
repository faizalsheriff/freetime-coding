package com.rockwellcollins.cs.hcms.core.services.handlers.persistent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.PersistentData;

public class PersistentDataHandler extends Handler {

	private static final long serialVersionUID = 1L;

	transient private File persistentDataFile;
	transient private PersistentData persistentData;

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		persistentData = new PersistentData();
	}

	@Override
	protected void onPropertyChanged(final Object source,
			final PropertyChangedArgs args) {

		final String key = args.getPropertyName();
		final String value = args.getPropertyValue();

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Persistent Data Handler '" + getName()
					+ "' storing '" + key + "' = '" + value + "'");
		}

		getPersistentData().put(key, value);
		getPersistentData().save(true);
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		persistentDataFile = new File(UnitManager.ObjectModel.getUnit()
				.getStoreDirectory(), getSetting("persistent name", getName()
				+ ".persistent.ser"));

		String persistentClass = getSetting("persistent class", "");

		if (persistentClass.length() > 0) {
			try {
				Object obj = UnitManager.Runtime.newInstance(persistentClass);
				persistentData = (PersistentData) obj;
			} catch (Exception e) {
				UnitManager.Logging.logSevere(e);
			}
		}

		if (persistentData == null) {
			persistentData = new PersistentData();
		}

		persistentData.setFile(persistentDataFile);
		persistentData.load();
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		final List<String> removal = new ArrayList<String>();

		for (final String key : getPersistentData()) {

			final String value = getPersistentData().get(key, null);

			if (value != null && hasProperty(key)) {

				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging.logCore("Persistent Data Handler '"
							+ getName() + "' requesting '" + key + "' = '"
							+ value + "'");
				}

				setProperty(key, value);

			} else {

				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging.logCore("Persistent Data Handler '"
							+ getName() + "' removing '" + key + "' = '"
							+ value + "'.  No longer exist in LCP.");
				}

				removal.add(key);
			}
		}

		for (final String key : removal) {
			getPersistentData().remove(key);
		}

		getPersistentData().save(true);
	}

	protected final PersistentData getPersistentData() {
		return persistentData;
	}

	protected final void setPersistentData(final PersistentData persistentData) {
		this.persistentData = persistentData;
	}

	protected final File getPersistentDataFile() {
		return persistentDataFile;
	}
}
