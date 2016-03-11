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

/**
 * The Interface IRulesEngineAddOn is a marker interface. All AddOn classes are
 * required to implement this interface.
 */
public interface RulesEngineAddOn {

	/**
	 * Execute.
	 * 
	 * @param info the info
	 * 
	 * @return the object
	 */
	Object execute(Object[] info);

}
