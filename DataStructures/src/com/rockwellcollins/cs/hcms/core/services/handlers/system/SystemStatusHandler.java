/*
 * 
 */
package com.rockwellcollins.cs.hcms.core.services.handlers.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerPropertyNotFoundException;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangeTimeoutArgs;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;

/**
 * The Class SystemStatusHandler.
 * 
 * @author Thomas Bundick
 */
public class SystemStatusHandler extends Handler {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The m node collection. */
	private transient HashMap<String, SSNode> mNodeCollection;

	/** The m leaf queue. */
	private transient Vector<SSNode> mLeafQueue;

	/** The m state manager. */
	private transient StateManager mStateManager = null;

	/** The Handler queue timeout setting name. */
	public static final String SETTING_HANDLER_QUEUE_TIMEOUT = "handler queue timeout";

	/** The Handler queue timeout default value. */
	public static final long HANDLER_QUEUE_TIMEOUT = 30000;

	/** The Timeout monitor timer timeout period */
	private transient long timeout;

	/** The Timeout monitor CountDown timer */
	private transient CountdownTimer monitorTimer;

	/**
	 * Checks if is master.
	 * 
	 * @return true, if is master
	 */
	private boolean isMaster() {
		if (getMyStateManager() != null) {
			return (getMyStateManager().getStatus() == StateManagerStatus.MASTER);
		} else {
			return false;
		}
	}

	/**
	 * Gets the my state manager.
	 * 
	 * @return the my state manager
	 */
	private StateManager getMyStateManager() {
		if (mStateManager == null) {
			mStateManager = (StateManager) getStateManager();
		}

		return mStateManager;
	}  
    
	/**
	 * gets the state name for the specified property name
	 * @param propName
	 * 
	 * @return state Name
	 */
	public final String getStateNameForProperty(final String propName) {
		return getStateName(propName);
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.handlers.Handler#onPropertyChanged(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs)
	 */
	@Override
	protected void onPropertyChanged(Object source, PropertyChangedArgs args) {
		if (isMaster()) {
			SSNode node = getNodeCollection().get(args.getPropertyName());
			if (node != null) {
				node.setStatus(args.getPropertyValue());
			}
		}
		if (monitorTimer.hasExpired()) {
			refreshLeafs();
			if (isDebug()) {
				logDebug("SystemStatusHandler: onPropChanged()->Timer expired. Prop: '" + args.getPropertyName() + "' Value: '" + args.getPropertyValue() + "'");
			}
			monitorTimer.startTimer(timeout);
		}
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.handlers.Handler#onPropertyChangedTimeout(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangeTimeoutArgs)
	 */
	@Override
	protected void onPropertyChangedTimeout(Object source, PropertyChangeTimeoutArgs args) {
		/***
		 * Refresh the leaves here irrespect of if the unit is the Master so that the local status of all the children are updated with in this unit and when the master changes, it doesn't affect the whole tree. The Handler sets the state only if it is the Master and when ever there is a change in the value but not always
		 */
		if (monitorTimer.hasExpired()) {
			if (isDebug()) {
				logDebug("SystemStatusHandler: propChangedTimeout()->Timer expired. Calling refreshLeafs()...");
			}
			refreshLeafs();
			monitorTimer.startTimer(timeout);
		}
	}

	/**
	 * Refresh leafs.
	 */
	private void refreshLeafs() {
		if (!getLeafQueue().isEmpty()) {
			int size = getLeafQueue().size();
			SSNode node = null;

			/* loop thru all of the leafs refreshing each */
			for (int i = 0; i < size; i++) {
				node = getLeafQueue().get(i);

				if (node != null && node.getPropertyName() != null) {
					try {
						node.setStatus(getProperty(node.getPropertyName()));
					} catch (HandlerPropertyNotFoundException e) {
					}
				}
			}
		}
	}

	/**
	 * Gets the node collection.
	 * 
	 * @return the node collection
	 */
	private HashMap<String, SSNode> getNodeCollection() {
		if (mNodeCollection == null) {
			mNodeCollection = new HashMap<String, SSNode>();
		}

		return mNodeCollection;
	}

	/**
	 * Import settings.
	 */
	private void importSettings() {
		String[] propertyNames = getSettingList("NodePropertyNames");

		for (String propertyName : propertyNames) {
			SSNode node = new SSNode(this);
			node.setPropertyName(propertyName);
			try {
				node.setMStatus(getProperty(propertyName));
			} catch (Exception e) {
			}

			getNodeCollection().put(propertyName, node);
		}

		for (String propertyName : propertyNames) {
			Map<String, String> settings = getSettingMap(propertyName);

			SSNode node = getNodeCollection().get(propertyName);

			if (settings != null && node != null) {
				SSNode parent = getNodeCollection().get(settings.get("parent"));
				if (parent != null) {
					parent.addChild(node);
				} else {
					if (isDebug()) {
						logDebug("SystemStatusHandler: "
								+ node.getPropertyName() + " has no parent.");
					}
				}
				
				for (int i = 1; i <= settings.size(); i++) {
					if (settings.get("linkedparent" + i) != null) {

						SSNode linkedParentLRU = getNodeCollection().get(
								settings.get("linkedparent" + i));
						if (linkedParentLRU != null) {
							linkedParentLRU.addInterLinkedChild(node);
							node.addLinkedParentLRU("linkedparent" + i,
									linkedParentLRU);
						} else {
							if (isInfo()) {
								logInfo("SystemStatusHandler: "
										+ node.getPropertyName()
										+ " has no child LRU.");
							}
						}
					}
				}
			}
		}

		for (String propName : propertyNames) {
			SSNode node = getNodeCollection().get(propName);

			if (node.isLeaf()) {
				getLeafQueue().add(node);
			}
		}
		timeout = getSetting(SETTING_HANDLER_QUEUE_TIMEOUT, HANDLER_QUEUE_TIMEOUT);
		if (isDebug()) {
			logDebug("SYSTEM STATUS HANDLER -> HANDLER_QUEUE_TIMEOUT: " + timeout);
		}

		if (isDebug()) {
			logDebug("SystemStatusHandler: Successfully Loaded the settings.");
		}
	}

	/**
	 * Gets the leaf queue.
	 * 
	 * @return the leaf queue
	 */
	private Vector<SSNode> getLeafQueue() {
		if (mLeafQueue == null) {
			mLeafQueue = new Vector<SSNode>();
		}

		return mLeafQueue;
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.handlers.Handler#onSetup(java.lang.Object, com.rockwellcollins.cs.hcms.core.ComponentSetupArgs)
	 */
	@Override
	protected void onSetup(Object source, ComponentSetupArgs args) throws ComponentSetupException {
		super.onSetup(source, args);

		importSettings();
	}

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args) throws ServiceStartException {
		super.onStarted(source, args);
		monitorTimer = new CountdownTimer();
		monitorTimer.startTimer(timeout);
	}

	/**
	 * Calls the base handler set property method. Since this handler may process every state change it has a property for, even the ones it updates; it is important to not stop updating the property if it has not changed.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * 
	 * @return true, if sets the property
	 */
	@Override
	public boolean setProperty(String name, String value) {
		try {
			if (isMaster()) {
				if (value != null && !value.equals(getProperty(name))) {
					if (isDebug()) {
						logDebug("Setting '" + name + "' to '" + value + "'");
					}
					return super.setProperty(name, value);
				}
			}
		} catch (HandlerPropertyNotFoundException e) {
			UnitManager.Logging.logSevere("SystemStatusHandler: " + e);
		}

		return false;
	}

	@Override
	protected boolean isInfo() {
		return super.isInfo();
	}

	@Override
	protected void logInfo(String message) {
		super.logInfo(message);
	}

	@Override
	protected boolean isDebug() {
		return super.isDebug();
	}

	@Override
	protected void logDebug(String message) {
		super.logDebug(message);
	}
}
