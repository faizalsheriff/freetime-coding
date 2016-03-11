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

/**
 * The Class AddOnMethodInfo contains information about an AddOn method.
 */
public class AddOnMethodInfo implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -1754586562846511667L;

	/** The class name. */
	private String className = null;

	/** The method name. */
	private String methodName = null;

	/** The return type. */
	private String returnType = null;

	/** The arguments type list. */
	private ArrayList<Object> argObjList = null;

	/**
	 * Gets the AddOn class name.
	 * 
	 * @return the AddOn class name
	 */
	public final String getClassName() {
		return className;
	}

	/**
	 * Sets the AddOn class name.
	 * 
	 * @param className
	 *            The new AddOn class name
	 */
	public final void setClassName(final String className) {
		this.className = className;
	}

	/**
	 * Gets the AddOn method name.
	 * 
	 * @return the AddOn method name
	 */
	public final String getMethodName() {
		return methodName;
	}

	/**
	 * Sets the AddOn method name.
	 * 
	 * @param methodName
	 *            The new AddOn method name
	 */
	public final void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets the return type of the AddOn method.
	 * 
	 * @return the return type of the AddOn method.
	 */
	public final String getReturnType() {
		return returnType;
	}

	/**
	 * Sets the return type of the AddOn method.
	 * 
	 * @param returnType
	 *            The new return type of the AddOn method.
	 */
	public final void setReturnType(final String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Instantiates a new add on method info with field information.
	 * 
	 * @param className
	 *            The AddOn class name
	 * @param methodName
	 *            The AddOn method name
	 * @param returnType
	 *            The return type of the AddOn method
	 * @param argObjList
	 *            The arguments type list of the AddOn method
	 */
	public AddOnMethodInfo(final String className, final String methodName,
			final String returnType, final ArrayList<Object> argObjList) {
		this.className = className;
		this.methodName = methodName;
		this.returnType = returnType;
		this.argObjList = argObjList;
	}

	/**
	 * Instantiates a blank add-on method info.
	 */
	public AddOnMethodInfo() {

	}

	/**
	 * Gets the arg obj list.
	 * 
	 * @return the arg obj list
	 */
	public final ArrayList<Object> getArgObjList() {
		return argObjList;
	}

	/**
	 * Sets the arg obj list.
	 * 
	 * @param argObjList
	 *            the new arg obj list
	 */
	public final void setArgObjList(final ArrayList<Object> argObjList) {
		this.argObjList = argObjList;
	}

}
