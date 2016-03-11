package com.rockwellcollins.cs.hcms.core;

/**
 * Exception for component XmlParsing.
 * @author getownse
 *
 */
public class ComponentXmlParserException extends ComponentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new exception
	 */
	public ComponentXmlParserException() {
		super();
	}

	/**
	 * Create a new exception with descriptive error
	 * @param error descriptive error
	 */
	public ComponentXmlParserException(final String error) {
		super(error);
	}

	/**
	 * Create a new exception with descriptive error and inner exception
	 * @param error descriptive error
	 * @param throwable inner exception
	 */
	public ComponentXmlParserException(final String error, final Throwable throwable) {
		super(error, throwable);
	}

	/**
	 * Create a new exception with inner exception
	 * @param throwable inner exception
	 */
	public ComponentXmlParserException(final Throwable throwable) {
		super(throwable);
	}
}
