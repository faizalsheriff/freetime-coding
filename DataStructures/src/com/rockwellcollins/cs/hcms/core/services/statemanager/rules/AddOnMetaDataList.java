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
 * The Class AddOnMetaDataList is a list of all AddOn methods available.
 */
public class AddOnMetaDataList implements Serializable {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -7879748065421146206L;

	/** The add on method info list. */
	private ArrayList<AddOnMethodInfo> addOnMethodInfoList = null;

	/**
	 * Gets the add on method info list.
	 * 
	 * @return the add on method info list
	 */
	public final ArrayList<AddOnMethodInfo> getAddOnMethodInfoList() {
		return addOnMethodInfoList;
	}

	/**
	 * Sets the add on method info list.
	 * 
	 * @param addOnMethodInfoList
	 *            the new add on method info list
	 */
	public final void setAddOnMethodInfoList(
			final ArrayList<AddOnMethodInfo> addOnMethodInfoList) {
		this.addOnMethodInfoList = addOnMethodInfoList;
	}

	/**
	 * Instantiates a new add on meta data list with field information.
	 * 
	 * @param addOnMethodInfoList
	 *            the add on method info list
	 */
	public AddOnMetaDataList(
			final ArrayList<AddOnMethodInfo> addOnMethodInfoList) {
		this.addOnMethodInfoList = addOnMethodInfoList;
	}

	/**
	 * Instantiates a blank add on meta data list.
	 */
	public AddOnMetaDataList() {

	}
}
