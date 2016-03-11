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
 * The Class ControlFlowCondition is used to store the conditions that needs to
 * be evaluated in the ControlFlows like if and else if.
 */
class ControlFlowCondition implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = 2654244943522743137L;

	/** The Left hand operand in the condition. */
	private String lh = null;

	/** The Right hand operand in the condition. */
	private String rh = null;

	/** Comparison Operator used in the condition. */
	private String op = null;

	/**
	 * Logical operator to evaluate more than one conditions. Null for 1'st
	 * condition
	 */
	private String lop = null;

	/**
	 * True if multiple conditions exist. Used only with the 1'st Condition in
	 * If/ElseIf
	 */
	private Boolean more = false;

	/**
	 * Initializes the ControlFlowCondition.
	 * 
	 * @param lh
	 *            the left hand operand
	 * @param rh
	 *            the right hand operand
	 * @param op
	 *            the operator
	 * @param lop
	 *            the operator to evaluate more than one conditions
	 * @param more
	 *            the boolean flag to determine whether the ControlFlow has
	 *            multiple conditions or not
	 */
	public ControlFlowCondition(final String lh, final String rh,
			final String op, final String lop, final Boolean more) {
		this.lh = lh;
		this.rh = rh;
		this.op = op;
		this.lop = lop;
		this.more = more;
	}

	/**
	 * Gets the left hand operand.
	 * 
	 * @return lh
	 */
	public String getLh() {
		return lh;
	}

	/**
	 * Gets the right hand operand.
	 * 
	 * @return rh
	 */
	public String getRh() {
		return rh;
	}

	/**
	 * Gets the operator.
	 * 
	 * @return op
	 */
	public String getOp() {
		return op;
	}

	/**
	 * Gets the logical operator to evaluate multiple conditions.
	 * 
	 * @return lop
	 */
	public String getLop() {
		return lop;
	}

	/**
	 * Checks if there are multiple conditions to be evaluated.
	 * 
	 * @return more
	 */
	public Boolean isMore() {
		return more;
	}
}
