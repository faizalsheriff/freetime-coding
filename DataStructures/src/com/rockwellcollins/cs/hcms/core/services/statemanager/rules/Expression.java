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
 * The Class Expression represents a Set statement.
 */
class Expression extends RuleConstruct {

	/** The serialVersionUID. */
	private static final long serialVersionUID = 1328499865523838226L;

	/** The method name. */
	private String methodName = null;

	/** The resulting State/Variable. */
	private String result = null;

	/** The list of arguments passed to the method. 
	 * (States/Constants/Variables) */
	private ArrayList<String> argList = null;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Expression] Method Name: ");
		sb.append(methodName);
		sb.append(", Result: ");
		sb.append(result);
		
		if (argList != null) {
			sb.append(", Arg List: ");
			sb.append(argList.toString());
		}
		
		return sb.toString();
	}

	/**
	 * Instantiates a new expression. Value for construct type could be:
	 * ControlFlow Expression
	 * 
	 * @param rConstruct
	 *            The construct type
	 */
	Expression(final RuleMacros.RuleConstructType rConstruct) {
		super(rConstruct);
	}

	/**
	 * Gets the method name.
	 * 
	 * @return methodName The method name
	 */
	String getMethodName() {
		return methodName;
	}

	/**
	 * Sets the method name.
	 * 
	 * @param methodName
	 *            The new method name
	 */
	void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets the result.
	 * 
	 * @return result The result
	 */
	String getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 * 
	 * @param result
	 *            The new result
	 */
	void setResult(final String result) {
		this.result = result;
	}

	/**
	 * Gets the argument list for the method.
	 * 
	 * @return argList The argList
	 */
	ArrayList<String> getArgList() {
		if (argList == null) {
			argList = new ArrayList<String>();
		}
		return argList;
	}

	/**
	 * Sets the argument list.
	 * 
	 * @param argList
	 *            The new argument list passed to the method
	 */
	void setArgList(final ArrayList<String> argList) {
		this.argList = argList;
	}

}
