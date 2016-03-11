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

/**
 * The Class RuleConstruct stores the various constructs or elements that
 * constitutes a complete Rule. These elements could be: ControlFlow - 1
 * Expression - 2
 */
class RuleConstruct implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = 1198400948870376103L;

	/**
	 * The RuleConstruct type: ControlFlow - 1 Expression - 2.
	 */
	private RuleMacros.RuleConstructType ruleConstructType;

	/**
	 * Instantiates a new Rule Construct.
	 * 
	 * @param rConstruct
	 *            RuleConstruct type: ControlFlow Expression
	 */
	RuleConstruct(final RuleMacros.RuleConstructType rConstruct) {
		this.ruleConstructType = rConstruct;
	}

	/**
	 * Gets the RuleConstruct type.
	 * 
	 * @return the RuleConstruct type: ControlFlow Expression
	 */
	RuleMacros.RuleConstructType getRuleConstructType() {
		return ruleConstructType;
	}

}
