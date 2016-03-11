package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

/**
 * Adapter message types.
 * @author getownse
 *
 */
public enum AdapterMessageType {
	/**
	 * Get a list of Adapters
	 */
	GET_ADAPTERS, 
	/**
	 * Get a list of Adapters Ack
	 */
	GET_ADAPTERS_ACK, 
	/**
	 * Sets Adapter mode
	 */
	SET_MODE, 
	/**
	 * Set Adapter mode Ack
	 */
	SET_MODE_ACK, 
	/**
	 * Make a method call on the Adapter
	 */
	METHOD_CALL, 
	/**
	 * Method call Ack
	 */
	METHOD_CALL_ACK
	// RETURN_VALUE, RETURN_VALUE_ACK,
}
