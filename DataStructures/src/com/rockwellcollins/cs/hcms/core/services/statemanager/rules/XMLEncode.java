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

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rockwellcollins.cs.hcms.core.CoreException;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLEncode parses the Rules XML file using SAX parser API. This
 * Class extends from DefaultHandler Class and overrides all the methods
 * required to do the parsing.
 */
public class XMLEncode extends DefaultHandler {

	/** The objects stack. */
	private Stack<Object> objectsStack;

	/** The rule collection. */
	private RuleCollection ruleCollection;

	/**
	 * Instantiates an XMLEncode Object.
	 */
	public XMLEncode() {
		objectsStack = new Stack<Object>();
		ruleCollection = new RuleCollection();
	}

	/**
	 * Receive notification of the beginning of the document.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() {

	}

	/**
	 * Receive notification of the end of the document.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() {

	}

	/**
	 * Receive notification of the beginning of the document.
	 * 
	 * @param uri The NameSpace URI of the XML document node
	 * @param localName The local name of the element node
	 * @param qName The qualified name of the element node
	 * @param atts Attributes of the element node.
	 * 
	 * @throws SAXException the SAX exception
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public final void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
		try {
			if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CMS_RULE.getElementName())) {
				// Do nothing
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CONST.name())) {
				processAttributes(RuleMacros.RuleElements.CONST, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.RULE.name())) {
				final Rule rule = new Rule();
				push(this.objectsStack, rule);
				processAttributes(RuleMacros.RuleElements.RULE, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.VAR.name())) {
				processAttributes(RuleMacros.RuleElements.VAR, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.SET.name())) {
				final Expression exp = new Expression(RuleMacros.RuleConstructType.EXPRESSION);
				push(this.objectsStack, exp);
				processAttributes(RuleMacros.RuleElements.SET, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.ARGUMENT.name())) {
				processAttributes(RuleMacros.RuleElements.ARGUMENT, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.IF.name())) {
				final ControlFlow ctrlFlow = new ControlFlow(RuleMacros.ControlFlowType.IF);
				push(this.objectsStack, ctrlFlow);
				processAttributes(RuleMacros.RuleElements.IF, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CONDITION.name())) {
				processAttributes(RuleMacros.RuleElements.CONDITION, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.ELSEIF.name())) {
				final ControlFlow ctrlFlow = new ControlFlow(RuleMacros.ControlFlowType.ELSEIF);
				push(this.objectsStack, ctrlFlow);
				processAttributes(RuleMacros.RuleElements.ELSEIF, atts);
			} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.ELSE.name())) {
				final ControlFlow ctrlFlow = new ControlFlow(RuleMacros.ControlFlowType.ELSE);
				push(this.objectsStack, ctrlFlow);
			} else {
				throw new SAXException(RuleConstants.ERROR_PARSING_RULE + RuleConstants.SEPARATOR + RuleConstants.INVALID_RULES_XML);
			}
		} catch (Exception e) {
			throw new SAXException(new CoreException(e.getMessage() + "\nElement: " + qName + "\nObject Stack: " + getObjectStackAsString(), e));
		}
	}
	
	/**
	 * Gets the object stack as string.
	 * 
	 * @return the object stack as string
	 */
	public final String getObjectStackAsString() {
		StringBuilder sb = new StringBuilder();
		
		if (objectsStack != null) {
			for (Object obj : objectsStack.toArray()) {
				sb.append(obj);
				sb.append(" -> ");
			}
		}
		
		return sb.toString();
	}

	/**
	 * Process attributes of a XML Element.
	 * 
	 * @param element The XML element
	 * @param atts The attributes of the this element
	 * 
	 * @throws SAXException the SAX exception
	 */
	private void processAttributes(final RuleMacros.RuleElements element,
			final Attributes atts) throws SAXException {
		int noOfAttributes = 0;
		String attrName;
		String attrType;
		String attrVal;
		String attrResult;
		String attrMethod;
		String attrLH;
		String attrRH;
		String attrOP;
		String attrMore;
		String attrLOP;
		try {
			if (atts == null) {
				throw new SAXException(new CoreException(
						RuleConstants.INVALID_RULE));
			} else {
				noOfAttributes = atts.getLength();
				switch (element) {
				case RULE: {
					if (noOfAttributes == 1) {
						attrName = atts.getValue(RuleMacros.ATTR_NAME);
						if (isNullOrBlank(attrName)) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object currentObject = objectsStack.peek();
								if (currentObject != null
										&& currentObject instanceof Rule) {
									currentObject = objectsStack.pop();
									((Rule) currentObject)
											.setRuleName(attrName);
									push(this.objectsStack, currentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE
													+ ": " + attrName));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE + ": "
												+ attrName));
							}
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case CONST: {
					if (noOfAttributes == 3) {
						attrType = atts.getValue(RuleMacros.ATTR_TYPE);
						attrName = atts.getValue(RuleMacros.ATTR_NAME);
						attrVal = atts.getValue(RuleMacros.ATTR_VAL);
						if (isNullOrBlank(attrType)
								|| isNullOrBlank(attrName)
								|| isNullOrBlank(attrVal)
								&& !attrType
										.equalsIgnoreCase(RuleMacros.JavaTypes.STRING
												.getPrimitiveType())
								|| attrType
										.equalsIgnoreCase(RuleMacros.JavaTypes.STRING
												.getPrimitiveType())
								&& attrVal == null) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							addConstToMap(ruleCollection.getGlobalConstMap(),
									attrName, attrType, attrVal);
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case VAR: {
					if (noOfAttributes == 3) {
						attrType = atts.getValue(RuleMacros.ATTR_TYPE);
						attrName = atts.getValue(RuleMacros.ATTR_NAME);
						attrVal = atts.getValue(RuleMacros.ATTR_VAL);
						if (isNullOrBlank(attrType)
								|| isNullOrBlank(attrName)
								|| isNullOrBlank(attrVal)
								&& !attrType
										.equalsIgnoreCase(RuleMacros.JavaTypes.STRING
												.getPrimitiveType())
								|| attrType
										.equalsIgnoreCase(RuleMacros.JavaTypes.STRING
												.getPrimitiveType())
								&& attrVal == null) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object currentObject = objectsStack.peek();
								if (currentObject != null
										&& currentObject instanceof Rule) {
									currentObject = objectsStack.pop();
									addConstToMap(((Rule) currentObject)
											.getLocalDB(), attrName, attrType,
											attrVal);
									push(this.objectsStack, currentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE
													+ ": " + attrName));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE + ": "
												+ attrName));
							}
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case SET: {
					if (noOfAttributes == 2) {
						attrMethod = atts.getValue(RuleMacros.ATTR_METHOD);
						attrResult = atts.getValue(RuleMacros.ATTR_RESULT);
						if (isNullOrBlank(attrMethod)
								|| isNullOrBlank(attrResult)) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object currentObject = objectsStack.peek();
								if (currentObject != null
										&& currentObject instanceof Expression) {
									currentObject = objectsStack.pop();
									((Expression) currentObject)
											.setMethodName(attrMethod);
									((Expression) currentObject)
											.setResult(attrResult);
									push(this.objectsStack, currentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case ARGUMENT: {
					if (noOfAttributes == 1) {
						attrName = atts.getValue(RuleMacros.ATTR_NAME);
						if (!isNullOrBlank(attrName)) {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object currentObject = objectsStack.peek();
								if (currentObject != null
										&& currentObject instanceof Expression) {
									currentObject = objectsStack.pop();
									((Expression) currentObject).getArgList()
											.add(attrName);
									push(this.objectsStack, currentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						} else {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case IF:
				case ELSEIF: {
					if (noOfAttributes == 4) {
						attrLH = atts.getValue(RuleMacros.ATTR_LH);
						attrRH = atts.getValue(RuleMacros.ATTR_RH);
						attrOP = atts.getValue(RuleMacros.ATTR_OP);
						attrMore = atts.getValue(RuleMacros.ATTR_MORE);
						if (isNullOrBlank(attrLH) || isNullOrBlank(attrRH)
								|| isNullOrBlank(attrOP)
								|| isNullOrBlank(attrMore)) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object parentObject = objectsStack.peek();
								if (parentObject != null
										&& parentObject instanceof ControlFlow
										&& !(((ControlFlow) parentObject)
												.getControlFlowType() == RuleMacros.ControlFlowType.ELSE)) {
									parentObject = objectsStack.pop();
									final ControlFlowCondition cfCondition = new ControlFlowCondition(
											attrLH, attrRH, attrOP, null,
											Boolean.valueOf(attrMore));
									((ControlFlow) parentObject)
											.getCtrlFLowConditionList().add(
													cfCondition);
									push(this.objectsStack, parentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				case CONDITION: {
					if (noOfAttributes == 4) {
						attrLH = atts.getValue(RuleMacros.ATTR_LH);
						attrRH = atts.getValue(RuleMacros.ATTR_RH);
						attrOP = atts.getValue(RuleMacros.ATTR_OP);
						attrLOP = atts.getValue(RuleMacros.ATTR_LOP);
						if (isNullOrBlank(attrLH) || isNullOrBlank(attrRH)
								|| isNullOrBlank(attrOP)
								|| isNullOrBlank(attrLOP)) {
							throw new SAXException(new CoreException(
									RuleConstants.INVALID_RULE));
						} else {
							if (objectsStack != null && !objectsStack.isEmpty()) {
								Object parentObject = objectsStack.peek();
								if (parentObject != null
										&& parentObject instanceof ControlFlow
										&& !(((ControlFlow) parentObject)
												.getControlFlowType() == RuleMacros.ControlFlowType.ELSE)) {
									parentObject = objectsStack.pop();
									final ControlFlowCondition cfCondition = new ControlFlowCondition(
											attrLH, attrRH, attrOP, attrLOP,
											null);
									((ControlFlow) parentObject)
											.getCtrlFLowConditionList().add(
													cfCondition);
									push(this.objectsStack, parentObject);
								} else {
									throw new SAXException(new CoreException(
											RuleConstants.ERROR_PARSING_RULE));
								}
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.INVALID_RULE));
					}
					break;
				}
				default:
					break;
				}
			}
		} catch (final SAXException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new SAXException(exp);
		}
	}

	/**
	 * Receive notification of the end of the document.
	 * 
	 * @param uri The NameSpace URI of the XML document node
	 * @param localName The local name of the element node
	 * @param qName The qualified name of the element node
	 * 
	 * @throws SAXException the SAX exception
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public final void endElement(final String uri, final String localName,
			final String qName) throws SAXException {
		if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CMS_RULE
				.getElementName())) {
			// No handling required here.
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CONST.name())) {
			// No handling required here.
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.RULE.name())) {
			try {
				if (objectsStack != null && !objectsStack.isEmpty()) {
					Object currentObject = objectsStack.peek();
					if (currentObject != null && currentObject instanceof Rule) {
						currentObject = objectsStack.pop();
						ruleCollection.getRulesMap().put(
								((Rule) currentObject).getRuleName(),
								(Rule) currentObject);
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.ERROR_PARSING_RULE));
					}
				} else {
					throw new SAXException(new CoreException(
							RuleConstants.ERROR_PARSING_RULE));
				}
			} catch (final SAXException exp) {
				throw exp;
			} catch (final Exception exp) {
				throw new SAXException(exp);
			}
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.VAR.name())) {
			// No handling required here.
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.SET.name())) {

			try {
				if (objectsStack != null && !objectsStack.isEmpty()) {
					Object currentObject = objectsStack.peek();
					if (currentObject != null
							&& currentObject instanceof Expression) {
						currentObject = objectsStack.pop();
						Object parentObject = objectsStack.peek();
						if (parentObject != null) {
							if (parentObject instanceof Rule) {
								parentObject = objectsStack.pop();
								((Rule) parentObject).getRuleConstruct().add(
										(Expression) currentObject);
								push(this.objectsStack, parentObject);
							} else if (parentObject instanceof ControlFlow) {
								parentObject = objectsStack.pop();
								((ControlFlow) parentObject).getRuleConstruct()
										.add((Expression) currentObject);
								push(this.objectsStack, parentObject);
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						} else {
							throw new SAXException(new CoreException(
									RuleConstants.ERROR_PARSING_RULE));
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.ERROR_PARSING_RULE));
					}
				} else {
					throw new SAXException(new CoreException(
							RuleConstants.ERROR_PARSING_RULE));
				}
			} catch (final SAXException exp) {
				throw exp;
			} catch (final Exception exp) {
				throw new SAXException(exp);
			}
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.ARGUMENT
				.name())) {
			// No handling required here.
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.IF.name())) {

			try {
				if (objectsStack != null && !objectsStack.isEmpty()) {
					Object currentObject = objectsStack.peek();
					if (currentObject != null
							&& currentObject instanceof ControlFlow) {
						currentObject = objectsStack.pop();
						Object parentObject = objectsStack.peek();
						if (parentObject != null) {
							if (parentObject instanceof Rule) {
								parentObject = objectsStack.pop();
								((Rule) parentObject).getRuleConstruct().add(
										(ControlFlow) currentObject);
								push(this.objectsStack, parentObject);
							} else if (parentObject instanceof ControlFlow) {
								parentObject = objectsStack.pop();
								((ControlFlow) parentObject).getRuleConstruct()
										.add((ControlFlow) currentObject);
								push(this.objectsStack, parentObject);
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						} else {
							throw new SAXException(new CoreException(
									RuleConstants.ERROR_PARSING_RULE));
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.ERROR_PARSING_RULE));
					}
				} else {
					throw new SAXException(new CoreException(
							RuleConstants.ERROR_PARSING_RULE));
				}
			} catch (final SAXException exp) {
				throw exp;
			} catch (final Exception exp) {
				throw new SAXException(exp);
			}
		} else if (qName.equalsIgnoreCase(RuleMacros.RuleElements.CONDITION
				.name())) {
			// No handling required here
		} else if (qName
				.equalsIgnoreCase(RuleMacros.RuleElements.ELSEIF.name())
				|| qName.equalsIgnoreCase(RuleMacros.RuleElements.ELSE.name())) {

			try {
				if (objectsStack != null && !objectsStack.isEmpty()) {
					Object currentObject = objectsStack.peek();
					if (currentObject != null
							&& currentObject instanceof ControlFlow
							&& !(((ControlFlow) currentObject)
									.getControlFlowType() == RuleMacros.ControlFlowType.IF)) {
						currentObject = objectsStack.pop();
						Object parentObject = objectsStack.peek();
						if (parentObject != null) {
							if (parentObject instanceof ControlFlow
									&& ((ControlFlow) parentObject)
											.getControlFlowType() == RuleMacros.ControlFlowType.IF) {

								for (final ControlFlow cFlow : ((ControlFlow) parentObject)
										.getElseControlFlowList()) {
									if (cFlow != null
											&& cFlow.getControlFlowType() == RuleMacros.ControlFlowType.ELSE) {
										throw new SAXException(
												new CoreException(
														RuleConstants.ERROR_PARSING_RULE));
									}
								}
								parentObject = objectsStack.pop();
								((ControlFlow) parentObject)
										.getElseControlFlowList().add(
												(ControlFlow) currentObject);
								push(this.objectsStack, parentObject);
							} else {
								throw new SAXException(new CoreException(
										RuleConstants.ERROR_PARSING_RULE));
							}
						} else {
							throw new SAXException(new CoreException(
									RuleConstants.ERROR_PARSING_RULE));
						}
					} else {
						throw new SAXException(new CoreException(
								RuleConstants.ERROR_PARSING_RULE));
					}
				} else {
					throw new SAXException(new CoreException(
							RuleConstants.ERROR_PARSING_RULE));
				}
			} catch (final SAXException exp) {
				throw exp;
			} catch (final Exception exp) {
				throw new SAXException(exp);
			}
		}
	}

	/**
	 * Receive notification of ignorable whitespace in element content.
	 * 
	 * @param ch The whitespace characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int,
	 * int)
	 */

	@Override
	public void ignorableWhitespace(final char[] ch, final int start,
			final int length) {

	}

	/**
	 * Receive notification of a processing instruction.
	 * 
	 * @param target The processing instruction target.
	 * @param data The processing instruction data, or null if none is supplied.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void processingInstruction(final String target, final String data) {

	}

	/**
	 * Receive notification of character data inside an element.
	 * 
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(final char[] ch, final int start, final int length) {

	}

	/**
	 * Adds the Constant to Global Constants Map.
	 * 
	 * @param map The Global Constant HashMap
	 * @param name The name of the Constant
	 * @param type The type of the Constant
	 * @param value The value of the Constant
	 * 
	 * @throws CoreException the core exception
	 */
	private void addConstToMap(final HashMap<String, Object> map,
			final String name, final String type, final String value)
			throws CoreException {
		if (type.equalsIgnoreCase(RuleMacros.JavaTypes.BOOLEAN
				.getPrimitiveType())) {
			map.put(name, Boolean.valueOf(value));
		} else if (type.equalsIgnoreCase(RuleMacros.JavaTypes.STRING
				.getPrimitiveType())) {
			map.put(name, String.valueOf(value));
		} else if (type.equalsIgnoreCase(RuleMacros.JavaTypes.LONG
				.getPrimitiveType())) {
			map.put(name, new Long(value));
		} else if (type.equalsIgnoreCase(RuleMacros.JavaTypes.DOUBLE
				.getPrimitiveType())) {
			map.put(name, new Double(value));
		} else {
			throw new CoreException(RuleConstants.ERROR_PARSING_RULE
					+ RuleConstants.SEPARATOR + RuleConstants.INVALID_TYPE);
		}
	}

	/**
	 * Pushes the Object onto the Stack.
	 * 
	 * @param stack The stack
	 * @param object The object to be pushed
	 */
	private void push(Stack<Object> stack, final Object object) {
		if (stack == null) {
			stack = new Stack<Object>();
		}
		stack.push(object);
	}

	/**
	 * Checks if is null or blank.
	 * 
	 * @param test the test
	 * 
	 * @return true If is null or blank
	 */
	private boolean isNullOrBlank(final String test) {
		if (test != null && test.length() > 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Gets the parsed RuleCollection object.
	 * 
	 * @return RuleCollection object
	 */
	public final RuleCollection getRuleCollection() {
		return ruleCollection;
	}

}
