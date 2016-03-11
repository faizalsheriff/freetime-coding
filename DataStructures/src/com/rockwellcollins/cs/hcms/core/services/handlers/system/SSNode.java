////////////////////////////////////////////////////////////////////////////////
//Copyright 2009 Rockwell Collins, Inc. All Rights Reserved
//NOTICE: The contents of this medium are proprietary to Rockwell
//Collins, Inc. and shall not be disclosed, disseminated, copied,
//or used except for purposes expressly authorized in written by
//Rockwell Collins, Inc.
////////////////////////////////////////////////////////////////////////////////
package com.rockwellcollins.cs.hcms.core.services.handlers.system;

import java.util.HashMap;
import java.util.Vector;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.utils.TriStateConstants;


import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerPropertyNotFoundException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;

/**
 * The Class SSNode.
 */
public class SSNode {

	/** The m parent. */
	private SSNode mParent = null;

	/** The m property name. */
	private String mPropertyName = null;

	/** The m InterLinkdedParent. */
	private SSNode mInterLinkedParent = null;

	/** The m status. */
	private String mStatus = Boolean.toString(true);

	/** The m children. */
	private Vector<SSNode> mChildren;

	/** The m handler. */
	private SystemStatusHandler mHandler;

	/** The m InterLinkded children. */
	private Vector<SSNode> mInterLinkedChildren;

	/** The m InterLinkded parent LRU. */
	private HashMap<String, SSNode> mLinkedParentLRU = new HashMap<String, SSNode>();
	
	/** The m State Database. */
	private StateDatabase statedB;

	/** The m State Manager. */
	private StateManager stateMgr;

	/**
	 * get the State database 
	 * @return the state database
	 */
	private StateDatabase getStatedB() {
		if (statedB == null) {
			//Get the state manager and database
			stateMgr = UnitManager.ObjectModel.getStateManager();
			statedB = stateMgr.getStateDatabase();
		}
		return statedB;
	}

	/**
	 * Get the hash map of linked parent
	 * @return the linkedParent LRUs 
	 */
	public HashMap<String, SSNode> getmLinkedParentLRU() {
		return mLinkedParentLRU;
	}

	/**
	 * Add the interlinked parent LRU
	 * @param linkedLRU - LRU Name
	 * @param obj - SSNode
	 */
	public void addLinkedParentLRU(String linkedLRU, SSNode obj) {
		mLinkedParentLRU.put(linkedLRU, obj);
	}

	/**
	 * Instantiates a new sS node.
	 * 
	 * @param handler
	 *            the handler
	 */
	public SSNode(SystemStatusHandler handler) {
		setHandler(handler);
	}

	/**
	 * Checks if is leaf.
	 * 
	 * @return true, if is leaf
	 */
	public boolean isLeaf() {
		if (getChildren().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets the handler.
	 * 
	 * @param handler
	 *            the new handler
	 */
	public void setHandler(SystemStatusHandler handler) {
		mHandler = handler;
	}

	/**
	 * Gets the handler.
	 * 
	 * @return the handler
	 */
	public SystemStatusHandler getHandler() {
		return mHandler;
	}

	/**
	 * Adds the child.
	 * 
	 * @param child
	 *            the child
	 */
	public void addChild(SSNode child) {
		if (child != null) {
			child.setParent(this);
			getChildren().add(child);
		}
	}

	/**
	 * Add Interlinked child
	 * @param 
	 * 		  SSnode child obj
	 */
	public void addInterLinkedChild(SSNode child) {
		if (child != null) {
			//Add the interlinked children to the parent
			child.setInterLinkedParent(this);
			getInterLinkedChildren().add(child);
		}
	}

	/**
	 * sets the interlinkedParent  
	 * @param parent
	 *         SSnode parent LRU
	 */
	public void setInterLinkedParent(SSNode parent) {
		mInterLinkedParent = parent;
	}

	/**
	 * @return
	 *     Return the inter linked children
	 */
	private Vector<SSNode> getInterLinkedChildren() {
		if (mInterLinkedChildren == null) {
			mInterLinkedChildren = new Vector<SSNode>();
		}

		return mInterLinkedChildren;
	}

	/**
	 * Gets the children.
	 * 
	 * @return the children
	 */
	private Vector<SSNode> getChildren() {
		if (mChildren == null) {
			mChildren = new Vector<SSNode>();
		}

		return mChildren;
	}

	/**
	 * Gets the property name.
	 * 
	 * @return the property name
	 */
	public String getPropertyName() {
		return mPropertyName;
	}

	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public SSNode getParent() {
		return mParent;
	}

	/**
	 * Gets the Interlinked parent
	 * 
	 * @return the interlinked parent
	 */
	public SSNode getInterLinkedParent() {
		return mInterLinkedParent;
	}

	/**
	 * Sets the property name.
	 * 
	 * @param label
	 *            the new property name
	 */
	public void setPropertyName(String label) {
		mPropertyName = label;
	}

	/**
	 * Sets the parent.
	 * 
	 * @param parent
	 *            the new parent
	 */
	public void setParent(SSNode parent) {
		mParent = parent;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public String getStatus() {
		return mStatus.toLowerCase();
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(String status) {
		mStatus = status.toLowerCase();
		if (UnitManager.ObjectModel.getUnit().isTriStateEnabled()) {
			if (mStatus.equalsIgnoreCase("true") || mStatus.equalsIgnoreCase(TriStateConstants.UNKNOWN.getValue())) {
				boolean isUnknown = false;
				for (SSNode node : getChildren()) {
					if (node.getStatus().equalsIgnoreCase("false")) {
						mStatus = node.getStatus();
						if (getHandler().isDebug()) {
							getHandler().logDebug("SystemStatusHandler: setStatus(), status is false");
						}
						break;
					} else if (node.getStatus().equalsIgnoreCase(TriStateConstants.UNKNOWN.getValue())) {
						isUnknown = true;
						mStatus = node.getStatus();
						if (getHandler().isDebug()) {
							getHandler().logDebug("SystemStatusHandler: setStatus(), status should be unknown or false");
						}
					} else if (node.getStatus().equalsIgnoreCase("true") && !isUnknown) {
						mStatus = node.getStatus();
					}
				}
			}
		} else {
			if (mStatus.equalsIgnoreCase("true")) {
				for (SSNode node : getChildren()) {
					if (node.getStatus().equalsIgnoreCase("false")) {
						mStatus = node.getStatus();
						if (getHandler().isDebug()) {
							getHandler().logDebug("SystemStatusHandler: setStatus(), status is false");
						}
						break;
					} else if (node.getStatus().equalsIgnoreCase("true")) {
						mStatus = node.getStatus();
					}
				}
			}

		}

		if (getParent() != null) {
			getParent().setStatus(mStatus);
		}
		// If the Ethernet Ping Status is false, set all of its Siblings to false.
		String ethernetPingRegex = ".*\\.\\d+Status";
		if (getPropertyName().matches(ethernetPingRegex) && mStatus.equalsIgnoreCase("false")) {
			if (getParent() != null) {
				for (SSNode node : getParent().getChildren()) {
					if (!node.getPropertyName().matches(ethernetPingRegex)) {
						try {
							if (getHandler().getProperty(node.getPropertyName()).equalsIgnoreCase("true")) {
								if (getHandler().isDebug()) {
									getHandler().logDebug("SystemStatusHandler: " + node.getPropertyName() + "' is set to FALSE");
								}
								if (UnitManager.ObjectModel.getUnit().isTriStateEnabled()) {
									getHandler().setProperty(node.getPropertyName(), TriStateConstants.UNKNOWN.getValue());
								} else {
									getHandler().setProperty(node.getPropertyName(), "false");
								}
							}
							if (node.getChildren() != null) {
								for (SSNode children : node.getChildren()) {
									if (getHandler().getProperty(children.getPropertyName()).equalsIgnoreCase("true")) {
										if (UnitManager.ObjectModel.getUnit().isTriStateEnabled()) {
											getHandler().setProperty(children.getPropertyName(), TriStateConstants.UNKNOWN.getValue());
										} else {
											getHandler().setProperty(children.getPropertyName(), "false");
										}
									}
								}
							}
						} catch (Exception e) {
							UnitManager.Logging
									.logWarning("SystemStatusHandler: Exception in SSNode while setting Siblings.."
											+ e);
						}
					}
				}
			}

			// Added for CR:11826
			//Get Interlinked children connected to the parent LRU
			for (SSNode childLRU : getInterLinkedChildren()) {
				boolean bChkActive = false;
				
				//Get the Interlinked parent LRU connected to the child LRU 
				for (int i = 1; i <= childLRU.getmLinkedParentLRU().size(); i++) {
					SSNode linkedParentLRU = childLRU.getmLinkedParentLRU().get(
							"linkedparent" + i);

					try {
						// Check the parent LRU's ping status. If any one of the
						// parent LRU ping status is true the
						// sets the isTrue to true.
						if (getHandler().getProperty(
								linkedParentLRU.getPropertyName())
								.equalsIgnoreCase("true")) {
							bChkActive = true;
						}
					} catch (HandlerPropertyNotFoundException e) {
						UnitManager.Logging
								.logSevere(" Expection occured - " + e);
					}
				}
				
                //If all parent LRU connected to the child LRU goes down then sets the child LRU's sibbling
				// to false/unknown. else no need to set. 
				if (!bChkActive) {
					for (SSNode childLRUSibling : childLRU.getParent()
							.getChildren()) {
						try {

							if (!(getHandler().getProperty(
									childLRUSibling.getPropertyName())
									.equalsIgnoreCase(TriStateConstants.UNKNOWN
											.getValue()))
									|| (getHandler().getProperty(
											childLRUSibling.getPropertyName())
											.equalsIgnoreCase(TriStateConstants.FALSE
													.getValue()))) {
								// If any all parent LRU's ping status is false
								// then set
								// the linked child state value to unknown
								/*
								 * if (UnitManager.ObjectModel.getUnit()
								 * .isTriStateEnabled()) {
								 */
								int index = 0;
								//Gets the index for the specific state 
								index = getStatedB().getIndex(
										getHandler().getStateNameForProperty(
												childLRUSibling
														.getPropertyName()));
                                //Gets the type of the state name
								String stateType = statedB.getType(index);
								if (stateType.equalsIgnoreCase("string")) {
									childLRUSibling
											.setMStatus(TriStateConstants.UNKNOWN
													.getValue());
									getHandler().setProperty(
											childLRUSibling.getPropertyName(),
											TriStateConstants.UNKNOWN
													.getValue());
									childLRUSibling
											.setStatus(TriStateConstants.UNKNOWN
													.getValue());
									if (getHandler().isInfo()) {
										getHandler().logInfo("PropertyName: "
													+ childLRUSibling
															.getPropertyName()
													+ " Status: unknown");
									}
								} else {
									childLRUSibling
											.setMStatus(TriStateConstants.FALSE
													.getValue());

									getHandler().setProperty(
											childLRUSibling.getPropertyName(),
											"false");
									childLRUSibling
											.setStatus(TriStateConstants.FALSE
													.getValue());
									if (getHandler().isInfo()) {
										getHandler().logInfo("PropertyName: "
													+ childLRUSibling
															.getPropertyName()
													+ " Status: false");
									}
								}
							}
						} catch (Exception e) {

							UnitManager.Logging.logWarning("Exception " + e);
						}
					}
				}
			}
		}

		if (getHandler().isDebug()) {
			getHandler().logDebug("SystemStatusHandler: setProperty() as" + mStatus);
		}

		if (!isLeaf()) {
			getHandler().setProperty(getPropertyName(), getStatus());
		} else {
			if (getHandler().isDebug()) {
				getHandler().logDebug("SystemStatusHandler: No need to set a leaf node status: " + mStatus);
			}
		}
	}

	/**
	 * Sets the m status.
	 * 
	 * @param status
	 *            the new m status
	 */
	public void setMStatus(String status) {
		mStatus = status;
	}
}
