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

import java.util.ArrayList;

/**
 * ControlFlow class is used by the Rule's parser to store if, else if and else
 * constructs defined in a Rule.
 * 
 */
class ControlFlow extends RuleConstruct {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -7710594084061658081L;

	/** ControlFlowCondition objects List. */
	private ArrayList<ControlFlowCondition> ctrlFLowConditionList = null;

	/** RuleConstruct objects List. */
	private ArrayList<RuleConstruct> ruleConstruct = null;

	/**
	 * ControlFlow Type IF ELSEIF ELSE.
	 */
	private RuleMacros.ControlFlowType controlFlowType;

	/** ControlFlow objects List to store ElseIf/Else corresponding to an If. */
	private ArrayList<ControlFlow> elseControlFlowList = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[ControlFlow] Type: ");
		sb.append(controlFlowType);
		
		return sb.toString();
	}

	/**
	 * Instantiates a new ControlFlow.
	 * 
	 * @param controlFlowType
	 *            Type of Control Flow IF ELSEIF ELSE
	 */
	public ControlFlow(final RuleMacros.ControlFlowType controlFlowType) {
		super(RuleMacros.RuleConstructType.CONTROL_FLOW);
		this.controlFlowType = controlFlowType;
	}

	/**
	 * Gets the ControlFlowCondition list.
	 * 
	 * @return ctrlFlowConditionList the ControlFlowCondition List
	 */
	ArrayList<ControlFlowCondition> getCtrlFLowConditionList() {
		if (ctrlFLowConditionList == null) {
			ctrlFLowConditionList = new ArrayList<ControlFlowCondition>();
		}
		return ctrlFLowConditionList;
	}

	/**
	 * Sets the ControlFlowCondition List.
	 * 
	 * @param ctrlFLowConditionList
	 *            the ControlFlowCondition List to set
	 */
	void setCtrlFLowConditionList(
			final ArrayList<ControlFlowCondition> ctrlFLowConditionList) {
		this.ctrlFLowConditionList = ctrlFLowConditionList;
	}

	/**
	 * Gets the RuleConstruct list.
	 * 
	 * @return the ruleConstructList
	 */
	ArrayList<RuleConstruct> getRuleConstruct() {
		if (ruleConstruct == null) {
			ruleConstruct = new ArrayList<RuleConstruct>();
		}
		return ruleConstruct;
	}

	/**
	 * Sets the rule construct list.
	 * 
	 * @param ruleConstruct
	 *            The ruleConstructs to set
	 */
	void setRuleConstruct(final ArrayList<RuleConstruct> ruleConstruct) {
		this.ruleConstruct = ruleConstruct;
	}

	/**
	 * Gets the ControlFlow type.
	 * 
	 * @return the ControlFlow Type IF ELSEIF ELSE
	 */
	RuleMacros.ControlFlowType getControlFlowType() {
		return controlFlowType;
	}

	/**
	 * Gets the ElseIf/Else ControlFlow objects corresponding to the If
	 * ControlFlow.
	 * 
	 * @return the List of ElseIf/Else ControlFlows corresponding to this If
	 *         ControlFlow
	 */
	ArrayList<ControlFlow> getElseControlFlowList() {
		if (this.elseControlFlowList == null) {
			this.elseControlFlowList = new ArrayList<ControlFlow>();
		}
		return elseControlFlowList;
	}

}
