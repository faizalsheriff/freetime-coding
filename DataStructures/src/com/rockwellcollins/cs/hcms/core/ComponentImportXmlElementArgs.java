package com.rockwellcollins.cs.hcms.core;

import org.w3c.dom.Element;

/**
 * Argument that is passed in the component onImportXmlElement and
 * onImportElementComplete events
 * 
 * @author getownse
 * 
 */
public class ComponentImportXmlElementArgs {
	private final Element element;

	/**
	 * Creates a new argument with XML element that describes the imported
	 * component
	 * 
	 * @param element
	 */
	public ComponentImportXmlElementArgs(final Element element) {
		this.element = element;
	}

	/**
	 * Get the XML element that describes the import of a component
	 * 
	 * @return XML element
	 */
	public final Element getElement() {
		return element;
	}
}
