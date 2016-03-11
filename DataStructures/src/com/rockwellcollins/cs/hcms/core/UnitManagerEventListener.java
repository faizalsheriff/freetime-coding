package com.rockwellcollins.cs.hcms.core;

/**
 * During the execution of the UnitManager, the UnitManager will process events.
 * 
 * Completed: The UnitManager is completely done, and the main thread will exit.
 * 
 * Components Completed: The UnitManager is done creating, configuring and
 * mapping relationships for all components
 * 
 * Components Load: The UnitManager is done instantiating components.
 * 
 * Lcp Config Parse Complete: The UnitManager is done parsing the LCP. No
 * components have been instantiated.
 * 
 * @author getownse
 * 
 */
public interface UnitManagerEventListener {

	/**
	 * The UnitManager is completely done, and the main thread will exit.
	 * 
	 * @param args
	 *            unit manager properties
	 */
	public void unitManagerCompleted(UnitManagerEventArgs args);
}
