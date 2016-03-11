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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class Rule defines each Rule in the Rule's XML file.
 * 
 */
public class Rule implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -8082637341316615193L;

	/** The rule name. */
	private String ruleName;

	/** The map of Rule level Constants/Variable/States. */
	private HashMap<String, Object> localDB;

	/** List of all RuleConstruct objects. */
	private ArrayList<RuleConstruct> ruleConstruct;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[Rule] Rule Name: ");
		sb.append(ruleName);
		
		return sb.toString();
	}

	/**
	 * Instantiates a new rule.
	 */
	public Rule() {
	}

	/**
	 * Gets the rule name.
	 * 
	 * @return ruleName The rule name
	 */
	public final String getRuleName() {
		return ruleName;
	}

	/**
	 * Sets the rule name.
	 * 
	 * @param ruleName
	 *            the new rule name
	 */
	public final void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * Gets the localDB.
	 * 
	 * @return localDB The local DB
	 */
	public final HashMap<String, Object> getLocalDB() {
		if (localDB == null) {
			localDB = new HashMap<String, Object>();
		}
		return localDB;
	}

	/**
	 * Sets the local DB.
	 * 
	 * @param localDB
	 *            The local DB
	 */
	public final void setLocalDB(final HashMap<String, Object> localDB) {
		this.localDB = localDB;
	}

	/**
	 * Gets the list of RuleConstruct.
	 * 
	 * @return ruleConstruct The rule constructs list
	 */
	public final ArrayList<RuleConstruct> getRuleConstruct() {
		if (ruleConstruct == null) {
			ruleConstruct = new ArrayList<RuleConstruct>();
		}
		return ruleConstruct;
	}

	/**
	 * Sets the RuleConstructs list.
	 * 
	 * @param ruleConstruct
	 *            The Rule Construct list
	 */
	public final void setRuleConstruct(final ArrayList<RuleConstruct> ruleConstruct) {
		this.ruleConstruct = ruleConstruct;
	}

}
