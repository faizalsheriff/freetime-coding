/*
 *
 * Copyright 2007 Rockwell Collins, Inc. All Rights Reserved
 * NOTICE: The contents of this medium are proprietary to Rockwell
 * Collins, Inc. and shall not be disclosed, disseminated, copied,
 * or used except for purposes expressly authorized in written by
 * Rockwell Collins, Inc.
 *
 */
package com.rockwellcollins.cs.hcms.core.services.statemanager.rules;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The Class RuleCollection stores all the Rules defined in the Rules XML in a
 * HashMap. Key - Rule Name, and Value - Rule Object.
 * 
 * This also stores all the Global Constants in a HashMap. Key - Constant Name,
 * and Value - Object, wrapping its value. The Rules Parser parses the Rules XML
 * and creates the instance of this class. This class implements the
 * Serializable interface since this object will be serialized to the Flash.
 * 
 */
public class RuleCollection implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Rules HashMap. */
	private HashMap<String, Rule> rulesMap = null;

	/** The global Constants HashMap. */
	private HashMap<String, Object> globalConstMap = null;

	/**
	 * Default constructor.
	 */
	public RuleCollection() {
		this.rulesMap = new HashMap<String, Rule>();
		this.globalConstMap = new HashMap<String, Object>();
	}

	/**
	 * Gets the Rules HashMap.
	 * 
	 * @return rulesMap the rules HashMap
	 */
	public final HashMap<String, Rule> getRulesMap() {
		if (rulesMap == null) {
			rulesMap = new HashMap<String, Rule>();
		}
		return rulesMap;
	}

	/**
	 * Gets the Global Constants HashMap.
	 * 
	 * @return globalConstMap The Global Constants HashMap
	 */
	public final HashMap<String, Object> getGlobalConstMap() {
		if (globalConstMap == null) {
			globalConstMap = new HashMap<String, Object>();
		}
		return globalConstMap;
	}

	/**
	 * Gets the Rule object for the Rule name.
	 * 
	 * @param ruleName
	 *            The Rule name
	 * 
	 * @return Rule object from the HashMap
	 */
	public final Rule getRule(final String ruleName) {
		if (rulesMap != null) {
			return rulesMap.get(ruleName);
		} else {
			return null;
		}
	}
}
