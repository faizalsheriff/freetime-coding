package com.rockwellcollins.cs.hcms.core.services.adapter;

/**
 * Adapter Modes
 * 
 * @author tmcrane
 * 
 */
public enum AdapterMode {
	/**
	 * TST is not connected. The communication happens only with device drivers
	 * and handlers.
	 */
	NORMAL,
	/**
	 * TST is connected and monitoring the messages received and sent by Adapter
	 * to the Handler. The communication continues to happen with device driver.
	 */
	TEST,
	/**
	 * TST is connected and simulating the output of the Adapter to both device
	 * drivers and handler. The communication to the device driver is stopped
	 * and all the output are simulated in the TST.
	 */
	DEVICE_ONLY_SIMULATION,
	/**
	 * TST is connected and simulating the output of adapter to handler. Any
	 * device input/interrupt will be sent to the TST and not to the handler.
	 * The TST will simulate the device input/interrupt to the handler. Any
	 * device call fromt he handler will be sent to both TST and the device
	 * driver.
	 */
	SIMULATION
}
