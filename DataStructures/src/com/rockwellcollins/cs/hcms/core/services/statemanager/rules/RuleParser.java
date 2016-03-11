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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class RuleParser implements the parser that parses the Rules XML file.
 */
class RuleParser {

	/** The HashMap containing Rule objects. */
	private RuleCollection ruleCollection = null;

	/** The List of AddOnMetaData. */
	private final AddOnMetaDataList addOnMetaDataList = null;

	/**
	 * Instantiates a new rule parser.
	 */
	public RuleParser() {
	}

	/**
	 * Parses the rules. This is the method is exposed to be called by Unit
	 * Manager.
	 * 
	 * @param RulesXML the rules XML
	 * 
	 * @return ruleCollection The RuleCollection object that contains the
	 * HashMap of Rules
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	public RuleCollection parseRules(final String RulesXML)
			throws RulesEngineException {
		parse(RulesXML);
		return this.ruleCollection;
	}

	/**
	 * Parses the Add-Ons XML.
	 * 
	 * @param AddOnXML Name of the AddOns XML file.
	 * 
	 * @return AddOnMetaDataList object
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	public AddOnMetaDataList parseAddOnXML(final String AddOnXML)
			throws RulesEngineException {
		return this.addOnMetaDataList;
	}

	/**
	 * Parse the Rules XML using SAX parser.
	 * 
	 * @param rulesXML The rules XML
	 * 
	 * @throws RulesEngineException The core exception
	 */
	private void parse(final String rulesXML) throws RulesEngineException {
		try {
			final XMLEncode handler = new XMLEncode();
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser saxParser = factory.newSAXParser();
			/*
			 * Not using Namespaces. Does not work on Perc's implementation of
			 * XML Parser try {
			 * saxParser.setProperty(RuleMacros.NAMESPACE_PROPERTY, new
			 * Boolean(true));
			 * saxParser.setProperty(RuleMacros.NAMESPACE_PREFIX_PROPERTY, new
			 * Boolean(false)); } catch (Exception e) { //Nothing required. Just
			 * log the message later }
			 */
			final File rulesFile = new File(rulesXML);
			saxParser.parse(rulesFile, handler);
			this.ruleCollection = handler.getRuleCollection();

		} catch (final ParserConfigurationException exp) {
			throw new RulesEngineException(RuleConstants.ERROR_PARSING_RULE,
					exp);
		} catch (final SAXException exp) {
			throw new RulesEngineException(RuleConstants.ERROR_PARSING_RULE,
					exp);
		} catch (final IOException exp) {
			throw new RulesEngineException(RuleConstants.ERROR_PARSING_RULE,
					exp);
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_PARSING_RULE,
					exp);
		}
	}

	/**
	 * Gets the RuleCollection object.
	 * 
	 * @return ruleCollection The RuleCollection object
	 */
	public RuleCollection getRuleCollection() {
		return ruleCollection;
	}

}
