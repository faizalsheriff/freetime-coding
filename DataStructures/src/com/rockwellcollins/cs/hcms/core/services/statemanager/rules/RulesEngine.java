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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.CoreException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;
import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RuleMacros.QueueCommands;
import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RuleMacros.FifoCommands;

/**
 * The Class RulesEngine is implementation of the IRulesEngine interface.
 */
public class RulesEngine implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The RuleCollection object. */
	private RuleCollection ruleCollection = null;

	/** The add on meta data list. */
	private AddOnMetaDataList addOnMetaDataList = null;

	/** The map of States modified by this Rule. */
	private StateMap localStateDB;

	/** The original state db. */
	private StateMap originalStateDB;

	/** The state database. */
	private transient StateDatabase stateDatabase;

	/** The last rule executed. */
	private String lastRuleExecuted = "";

	/**
	 * The temporary map to hold intermediate values of Constants and Rule
	 * Variables during Rule execution.
	 */
	private HashMap<String, Object> localVarsConst;

	/** The Constant QUEUE_DELIM. */
	private static final String QUEUE_DELIM = ";;;";
	
	/** The Constant FIFO_DELIM. */
	private static final String FIFO_DELIM = ";;;";
	
	/**
	 * Instantiates a RulesEngine Object.
	 * 
	 * @param stateDatabase the state database
	 */
	public RulesEngine(final StateDatabase stateDatabase) {
		this.stateDatabase = stateDatabase;
	}

	/**
	 * This method will execute the Rules associated with the triggering States
	 * provided as input in the HashMap.
	 * 
	 * @param inputMap
	 *            The input to this method will be a HashMap with the following
	 *            structure: Key - Name of the triggering State (String) Value -
	 *            Value of the triggering State (String)
	 * 
	 * @return StateMap - This method returns a StateMap containing all the
	 *         updated States and their values, that will be updated in the
	 *         State Database.
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public final StateMap executeRule(final StateMap inputMap)
			throws RulesEngineException {
		try {
			if (inputMap == null) {
				throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
						+ RuleConstants.SEPARATOR
						+ RuleConstants.INVALID_INPUT);
			}
			localStateDB = new StateMap();
			originalStateDB = new StateMap();

			/* store values before the rules have run */
			for (final String statename1 : inputMap.keySet()) {
				originalStateDB.put(statename1, stateDatabase
						.getValue(statename1));
			}

			for (final Entry<String, String> stateEntry : inputMap.entrySet()) {
				String stateName = stateEntry.getKey();
				int stateIndex = stateDatabase.getIndex(stateName);
				
				if (stateIndex == -1) {
					continue;
				}

				final String ruleName = stateDatabase.getRule(stateIndex);
				localStateDB.put(stateName, stateEntry.getValue());
				
				// see if there is a rule for that state
				if (ruleName == null || ruleName.length() == 0) {
					continue;
				}

				lastRuleExecuted = ruleName;
				final Rule rule = this.ruleCollection.getRule(ruleName);

				// make sure the rule exist 
				if (rule == null) {
					throw new CoreException(RuleConstants.ERROR_RETRIEVING_RULE
							+ RuleConstants.SEPARATOR + ruleName);
				}

				// create a duplicate of the rule's constants 
				localVarsConst = new HashMap<String, Object>(
						this.ruleCollection.getGlobalConstMap());
				localVarsConst.putAll(rule.getLocalDB());

				try {
					processRule(rule);
				} catch (Exception ex) {
			
					 // add the rule name and the triggering state to the
					 // exception
					throw new CoreException("Rule: " + ruleName
							+ ", Triggering State: " + stateName, ex);
				}
			}
			/*****************************************
			for (final String stateName : inputMap.keySet()) {
				int stateIndex = stateDatabase.getIndex(stateName);

				if (stateIndex == -1) {
					continue;
				}

				final String ruleName = stateDatabase.getRule(stateIndex);
				localStateDB.put(stateName, inputMap.get(stateName));

				// see if there is a rule for that state
				if (ruleName == null || ruleName.length() == 0) {
					continue;
				}

				lastRuleExecuted = ruleName;
				final Rule rule = this.ruleCollection.getRule(ruleName);

				// make sure the rule exist
				if (rule == null) {
					throw new CoreException(RuleConstants.ERROR_RETRIEVING_RULE
							+ RuleConstants.SEPARATOR + ruleName);
				}

				// create a duplicate of the rule's constants
				localVarsConst = new HashMap<String, Object>(
						this.ruleCollection.getGlobalConstMap());
				localVarsConst.putAll(rule.getLocalDB());

				try {
					processRule(rule);
				} catch (Exception ex) {
					 // add the rule name and the triggering state to the
					 // exception
					throw new CoreException("Rule: " + ruleName
							+ ", Triggering State: " + stateName, ex);
				}
			}
			*****************************************/

		} catch (final RulesEngineException exp) {
			UnitManager.Logging.logSevere(exp + RuleConstants.SEPARATOR
					+ "Last Rule excecuted: " + lastRuleExecuted);
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR + "Last Rule excecuted: "
					+ lastRuleExecuted, exp);
		}

		return localStateDB;
	}

	/**
	 * Gets the Rules Collection Object.
	 * 
	 * @return the rule collection
	 */
	public final RuleCollection getRuleCollection() {
		return ruleCollection;
	}

	/**
	 * Sets the rule collection.
	 * 
	 * @param ruleCollection
	 *            The new rule collection
	 */
	public final void setRuleCollection(final RuleCollection ruleCollection) {
		this.ruleCollection = ruleCollection;
	}

	/**
	 * Gets the add on meta data list.
	 * 
	 * @return the add on meta data list
	 */
	public final AddOnMetaDataList getAddOnMetaDataList() {
		return addOnMetaDataList;
	}

	/**
	 * Sets the add on meta data list.
	 * 
	 * @param addOnMetaDataList
	 *            The new add on meta data list
	 */
	public final void setAddOnMetaDataList(
			final AddOnMetaDataList addOnMetaDataList) {
		this.addOnMetaDataList = addOnMetaDataList;
	}

	/**
	 * Gets the local state DB.
	 * 
	 * @return the local state DB
	 */
	public final StateMap getLocalStateDB() {
		if (localStateDB == null) {
			localStateDB = new StateMap();
		}
		return localStateDB;
	}

	/**
	 * Sets the local state DB.
	 * 
	 * @param localStateDB
	 *            The local state DB
	 */
	public final void setLocalStateDB(final StateMap localStateDB) {
		this.localStateDB = localStateDB;
	}

	/**
	 * Processes the Rule.
	 * 
	 * @param rule The Rule Object to be processed.
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private void processRule(final Rule rule) throws RulesEngineException {
		final ArrayList<RuleConstruct> constructList = rule.getRuleConstruct();
		if (constructList != null && constructList.size() > 0) {
			for (final Object obj : constructList) {
				processRuleConstruct((RuleConstruct) obj);
			}
		} else {
			// TBR decide whether to throw exp here or not. may not be required.
			// throw new CoreException(ERROR_EXECUTING_RULE);
		}

	}

	/**
	 * Execute an Expression in a Rule and saves the result.
	 * 
	 * @param expr
	 *            The Expression instance to be executed.
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	private void executeExpression(final Expression expr) throws CoreException {
		final String resultName = expr.getResult();
		final String methodName = expr.getMethodName();
		final ArrayList<String> argsList = expr.getArgList();
		String resultDataType = getJavaType(resultName);
		if (RuleHelper.isAddOnMethod(methodName)) {
			/* REV */
			final AddOnMethodInfo info = new AddOnMethodInfo();
			final int indexOfDot = methodName.lastIndexOf('.');
			info.setClassName(methodName.substring(0, indexOfDot));
			info.setMethodName(methodName.substring(indexOfDot + 1));
			info.setReturnType(resultDataType);
			final ArrayList<Object> argObjList = new ArrayList<Object>();
			for (final String arg : argsList) {
				argObjList.add(this.getValueObject(arg, resultDataType));
			}
			info.setArgObjList(argObjList);
			final Object result = RuleHelper.executeAddOnMethod(info);
			saveExpressionResult(resultName, getJavaType(resultName), result);
		} else {
			switch (RuleHelper.getMethodEnum(methodName)) {
			case ADD: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					final Number[] numbers = new Number[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						numbers[ctr++] = (Number) this.getValueObject(arg,
								resultDataType);
					}
					final Number result = RuleHelper.add(numbers);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case AVG: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					final Number[] numbers = new Number[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						numbers[ctr++] = (Number) this.getValueObject(arg,
								resultDataType);
					}
					final Number result = RuleHelper.average(numbers);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case AND: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}

					if (RuleHelper.getEnumForType(getJavaType(argsList.get(0))) == RuleMacros.JavaTypes.BOOLEAN) {
						final Boolean[] flags = new Boolean[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Boolean) this.getValueObject(arg,
									resultDataType);
						}
						final Boolean result = RuleHelper.doAND(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else if (RuleHelper.getEnumForType(getJavaType(argsList
							.get(0))) == RuleMacros.JavaTypes.LONG) {
						final Long[] flags = new Long[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Long) this.getValueObject(arg,
									resultDataType);
						}
						final Long result = RuleHelper.doAND(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_TYPE);
					}

				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case CONCAT: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					final String[] strs = new String[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						strs[ctr++] = (String) this.getValueObject(arg,
								resultDataType);
					}
					final String result = RuleHelper.concat(strs);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case DIV: {
				try {
					if (argsList == null || argsList.size() != 2) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final Number num1 = (Number) this.getValueObject(argsList
							.get(0), resultDataType);
					final Number num2 = (Number) this.getValueObject(argsList
							.get(1), resultDataType);
					final Number result = RuleHelper.divide(num1, num2);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case MAX: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					final Number[] numbers = new Number[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						numbers[ctr++] = (Number) this.getValueObject(arg,
								resultDataType);
					}
					final Number result = RuleHelper.maximum(numbers);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case MIN: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						throw new CoreException();
					}
					final Number[] numbers = new Number[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						numbers[ctr++] = (Number) this.getValueObject(arg,
								resultDataType);
					}
					final Number result = RuleHelper.minimum(numbers);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case MUL: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					final Number[] numbers = new Number[ctr];
					ctr = 0;
					for (final String arg : argsList) {
						numbers[ctr++] = (Number) this.getValueObject(arg,
								resultDataType);
					}
					final Number result = RuleHelper.multiply(numbers);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case OR: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}

					if (RuleHelper.getEnumForType(getJavaType(argsList.get(0))) == RuleMacros.JavaTypes.BOOLEAN) {
						final Boolean[] flags = new Boolean[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Boolean) this.getValueObject(arg,
									resultDataType);
						}
						final Boolean result = RuleHelper.doOR(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else if (RuleHelper.getEnumForType(getJavaType(argsList
							.get(0))) == RuleMacros.JavaTypes.LONG) {
						final Long[] flags = new Long[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Long) this.getValueObject(arg,
									resultDataType);
						}
						final Long result = RuleHelper.doOR(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_TYPE);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case SUB: {
				try {
					if (argsList == null || argsList.size() != 2) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final Number num1 = (Number) this.getValueObject(argsList
							.get(0), resultDataType);
					final Number num2 = (Number) this.getValueObject(argsList
							.get(1), resultDataType);
					final Number result = RuleHelper.subtract(num1, num2);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case TOGGLE: {
				try {
					if (argsList == null || argsList.size() != 1) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final String flagArg = argsList.get(0);
					if (RuleHelper.getEnumForType(getJavaType(flagArg)) == RuleMacros.JavaTypes.BOOLEAN) {
						final Boolean flag = (Boolean) this.getValueObject(
								flagArg, resultDataType);
						saveExpressionResult(resultName,
								getJavaType(resultName), Boolean.valueOf(RuleHelper
										.toggle(flag)));
					} else if (RuleHelper.getEnumForType(getJavaType(flagArg)) == RuleMacros.JavaTypes.LONG) {
						final Long flag = (Long) this.getValueObject(flagArg,
								resultDataType);
						saveExpressionResult(resultName,
								getJavaType(resultName), Long.valueOf(RuleHelper
										.toggle(flag)));
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_TYPE);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case XOR: {
				try {
					if (argsList == null) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					int ctr = argsList.size();
					if (ctr == 0) {
						break;
					}
					if (RuleHelper.getEnumForType(getJavaType(argsList.get(0))) == RuleMacros.JavaTypes.BOOLEAN) {
						final Boolean[] flags = new Boolean[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Boolean) this.getValueObject(arg,
									resultDataType);
						}
						final Boolean result = RuleHelper.doXOR(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else if (RuleHelper.getEnumForType(getJavaType(argsList
							.get(0))) == RuleMacros.JavaTypes.LONG) {
						final Long[] flags = new Long[ctr];
						ctr = 0;
						for (final String arg : argsList) {
							flags[ctr++] = (Long) this.getValueObject(arg,
									resultDataType);
						}
						final Long result = RuleHelper.doXOR(flags);
						saveExpressionResult(resultName,
								getJavaType(resultName), result);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_TYPE);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case EQUALTO: {
				try {
					if (argsList == null || argsList.size() != 1) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final String argument = argsList.get(0);
					final RuleMacros.JavaTypes resultType = RuleHelper
							.getEnumForType(getJavaType(resultName));
					final RuleMacros.JavaTypes argType = RuleHelper
							.getEnumForType(getJavaType(argument));
					final Object argValue = this.getValueObject(argument,
							resultDataType);
					if (resultType == RuleMacros.JavaTypes.STRING
							&& argType == RuleMacros.JavaTypes.STRING
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.DOUBLE
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.BOOLEAN
							&& argType == RuleMacros.JavaTypes.BOOLEAN
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.DOUBLE) {
						saveExpressionResult(resultName,
								getJavaType(resultName), argValue);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.OPERATION_NOT_ALLOWED);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}

			case ASSIGN: {
				try {
					if (argsList == null || argsList.size() != 1) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final String argument = argsList.get(0);
					UnitManager.Logging
							.logDebug("RulesEngineImpl->executeExpression()->argument name: "
									+ argument);
					final RuleMacros.JavaTypes resultType = RuleHelper
							.getEnumForType(getJavaType(resultName));
					final RuleMacros.JavaTypes argType = RuleHelper
							.getEnumForType(getJavaType(argument));
					final Object argValue = this
							.getOriginalValueObject(argument);
					UnitManager.Logging
							.logDebug("RulesEngineImpl->executeExpression()->argument value: "
									+ argValue);
					UnitManager.Logging
							.logDebug("RulesEngineImpl->executeExpression()->result name: "
									+ resultName);

					if (resultType == RuleMacros.JavaTypes.STRING
							&& argType == RuleMacros.JavaTypes.STRING
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.DOUBLE
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.BOOLEAN
							&& argType == RuleMacros.JavaTypes.BOOLEAN
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.DOUBLE) {
						UnitManager.Logging
								.logDebug("Before saveExpressionResult....");
						saveExpressionResult(resultName,
								getJavaType(resultName), argValue);
						UnitManager.Logging
								.logDebug("After SaveExpressionResult....");
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.OPERATION_NOT_ALLOWED);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}

			case EQUALTO_RESULTASPOINTER: {
				/*
				 * This method sets the argument value to the State/Variable
				 * which is defined as the value of the variable used for
				 * result.
				 */
				try {
					if (argsList == null || argsList.size() != 1) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final String argument = argsList.get(0);
					final String actualResultState = (String) this
							.getValueObject(resultName, resultDataType);

					final RuleMacros.JavaTypes resultType = RuleHelper
							.getEnumForType(getJavaType(actualResultState));
					final RuleMacros.JavaTypes argType = RuleHelper
							.getEnumForType(getJavaType(argument));
					final Object argValue = this.getValueObject(argument,
							resultDataType);
					if (resultType == RuleMacros.JavaTypes.STRING
							&& argType == RuleMacros.JavaTypes.STRING
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.DOUBLE
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.BOOLEAN
							&& argType == RuleMacros.JavaTypes.BOOLEAN
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.DOUBLE) {
						saveExpressionResult(actualResultState,
								getJavaType(actualResultState), argValue);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.OPERATION_NOT_ALLOWED);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}

			case EQUALTO_ARGASPOINTER: {
				/*
				 * Updates the value of the result state or variable, to the
				 * value of the state pointed by the argument pointer
				 */
				try {
					if (argsList == null || argsList.size() != 1) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final String argument = argsList.get(0);
					final String actualArgumentState = (String) this
							.getValueObject(argument, resultDataType);
					final Object actualArgumentStateValue = this
							.getValueObject(actualArgumentState, resultDataType);

					final RuleMacros.JavaTypes resultType = RuleHelper
							.getEnumForType(getJavaType(resultName));
					final RuleMacros.JavaTypes argType = RuleHelper
							.getEnumForType(getJavaType(actualArgumentState));

					if (resultType == RuleMacros.JavaTypes.STRING
							&& argType == RuleMacros.JavaTypes.STRING
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.DOUBLE
							|| resultType == RuleMacros.JavaTypes.DOUBLE
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.LONG
							|| resultType == RuleMacros.JavaTypes.BOOLEAN
							&& argType == RuleMacros.JavaTypes.BOOLEAN
							|| resultType == RuleMacros.JavaTypes.LONG
							&& argType == RuleMacros.JavaTypes.DOUBLE) {
						saveExpressionResult(resultName,
								getJavaType(resultName),
								actualArgumentStateValue);
					} else {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.OPERATION_NOT_ALLOWED);
					}
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}

			case MOD: {
				try {
					if (argsList == null || argsList.size() != 2) {
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_METHOD_ARGUMENT);
					}
					final Number num1 = (Number) this.getValueObject(argsList
							.get(0), resultDataType);
					final Number num2 = (Number) this.getValueObject(argsList
							.get(1), resultDataType);
					final Number result = RuleHelper.mod(num1, num2);
					saveExpressionResult(resultName, getJavaType(resultName),
							result);
				} catch (final CoreException exp) {
					throw exp;
				} catch (final Exception exp) {
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE,
							exp);
				}
				break;
			}
			case GETCURRENTTIME: {
				final Long result = UnitManager.Timing.getTimeAlive();
				saveExpressionResult(resultName, getJavaType(resultName),
						result);
				break;
			}
			case QUEUE: {
				if (argsList == null || argsList.size() != 3) {
					UnitManager.Logging
							.logSevere(RuleConstants.INVALID_QUEUE_ARG);
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_METHOD_ARGUMENT);
				}
				final String charLimit = argsList.get(0);
				final Long charLimitValue = (Long) this.getValueObject(
						charLimit, resultDataType);

				final String cmdState = argsList.get(1);
				final String cmdStateVal = (String) this.getValueObject(
						cmdState, resultDataType);
				if (cmdStateVal == null || cmdStateVal.length() == 0) {
					UnitManager.Logging
							.logSevere(RuleConstants.INVALID_QUEUE_COMMAND);
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_METHOD_ARGUMENT);
				}
				final int cmdDelimIndex = cmdStateVal.indexOf(" ");

				final String command = cmdStateVal.substring(0, 3);
				String value = "";

				final String queueState = argsList.get(2);
				final String queueStateVal = (String) this.getValueObject(
						queueState, resultDataType);
				final String topStateValue = (String) this.getValueObject(
						resultName, resultDataType);

				if (command.equalsIgnoreCase(QueueCommands.ADD.getQueueCmd())) {
					if (cmdDelimIndex == -1) {
						UnitManager.Logging
								.logSevere(RuleConstants.INVALID_QUEUE_COMMAND
										+ ": " + cmdStateVal);
						throw new CoreException(
								RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_QUEUE_COMMAND
										+ ": " + cmdStateVal);
					}

					value = cmdStateVal.substring(cmdDelimIndex + 1).trim();

					if (queueStateVal.length() + value.length() <= charLimitValue) {
						if (value == null || value.length() == 0
								|| value.contains(QUEUE_DELIM)) {
							UnitManager.Logging
									.logSevere(RuleConstants.INVALID_QUEUE_COMMAND_VALUE);
							throw new CoreException(
									RuleConstants.ERROR_EXECUTING_RULE
											+ RuleConstants.SEPARATOR
											+ RuleConstants.INVALID_QUEUE_COMMAND_VALUE);
						} else {
							if (topStateValue == null
									|| topStateValue.length() == 0) {
								saveExpressionResult(resultName,
										getJavaType(resultName), value);
							} else {
								if (queueStateVal == null
										|| queueStateVal.length() == 0) {
									saveExpressionResult(queueState,
											getJavaType(queueState), value);
								} else {
									saveExpressionResult(queueState,
											getJavaType(queueState),
											queueStateVal + QUEUE_DELIM + value);
								}
							}
						}
					} else {
						if (UnitManager.Logging.isDebug()) {
							UnitManager.Logging.logDebug(RuleConstants.QUEUE_FULL);
						}
					}
				} else if (command.equalsIgnoreCase(QueueCommands.REM
						.getQueueCmd())) {
					final String resultValue = (String) this.getValueObject(
							resultName, resultDataType);
					if (resultValue == null || resultValue.length() == 0) {
						if (UnitManager.Logging.isDebug()) {
							UnitManager.Logging
								.logDebug(RuleConstants.QUEUE_EMPTY);
						}
						// Commented to fix the MCD notification issue
						// throw new CoreException(ERROR_EXECUTING_RULE +
						// SEPARATOR + QUEUE_EMPTY);
					}
					if (queueStateVal == null || queueStateVal.length() == 0) {
						saveExpressionResult(resultName,
								getJavaType(resultName), "");
					} else {
						final int queueDelimIndex = queueStateVal
								.indexOf(QUEUE_DELIM);
						if (queueDelimIndex == -1) {
							saveExpressionResult(resultName,
									getJavaType(resultName), queueStateVal);
							saveExpressionResult(queueState,
									getJavaType(queueState), "");
						} else {
							saveExpressionResult(resultName,
									getJavaType(resultName), queueStateVal
											.substring(0, queueDelimIndex));
							saveExpressionResult(queueState,
									getJavaType(queueState), queueStateVal
											.substring(queueDelimIndex
													+ QUEUE_DELIM.length()));
						}
					}
				} else if (command.equalsIgnoreCase(QueueCommands.CLEAR
						.getQueueCmd())) {
					if (UnitManager.Logging.isDebug()) {
						UnitManager.Logging
								.logDebug(RuleConstants.CLEARING_QUEUE);
					}
					saveExpressionResult(resultName, getJavaType(resultName),
							"");
					saveExpressionResult(queueState, getJavaType(queueState),
							"");
				} else {
					UnitManager.Logging
							.logSevere(RuleConstants.INVALID_QUEUE_COMMAND
									+ ": " + cmdStateVal);
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_QUEUE_COMMAND + ": "
							+ cmdStateVal);
				}
				break;
			}
			case FIFO: {
				if (argsList == null || argsList.size() != 3) {
					UnitManager.Logging.logSevere(RuleConstants.INVALID_FIFO_ARG);
					
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_METHOD_ARGUMENT);
				}
				// fifo size limit argument (i.e., number of entries)
				final String fifoLimit = argsList.get(0);
				final Long fifoLimitValue = (Long) this.getValueObject(
						fifoLimit, resultDataType);

				// state containing FIFO CMD (and optionally appended 'value')
				// format: "CMD" or "CMD value"
				final String cmdState = argsList.get(1);
				final String cmdStateVal = (String) this.getValueObject(
						cmdState, resultDataType);
				if (cmdStateVal == null || cmdStateVal.length() == 0) {
					UnitManager.Logging
							.logSevere(RuleConstants.INVALID_FIFO_COMMAND);
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_METHOD_ARGUMENT);
				}
				final int cmdDelimIndex = cmdStateVal.indexOf(" ");

				final String command = cmdStateVal.substring(0, 3);
				String value = "";

				// fifo state name (storage)
				final String fifoState = argsList.get(2);
				// concatenated/delimited string of current fifo values
				final String fifoStateVal = (String) this.getValueObject(
						fifoState, resultDataType);
				final String topStateValue = (String) this.getValueObject(
						resultName, resultDataType);

				if (command.equalsIgnoreCase(FifoCommands.ADD.getFifoCmd())) {
					// if no appended value
					if (cmdDelimIndex == -1) {
						UnitManager.Logging.logSevere(RuleConstants.INVALID_FIFO_COMMAND
										+ ": " + cmdStateVal);
						throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
										+ RuleConstants.SEPARATOR
										+ RuleConstants.INVALID_FIFO_COMMAND
										+ ": " + cmdStateVal);
					}

					value = cmdStateVal.substring(cmdDelimIndex + 1).trim();
					
					String[] fifoEntries;
					if (fifoStateVal == null || fifoStateVal.length() == 0) {
						fifoEntries = new String[0];
					} else {
						fifoEntries = fifoStateVal.split(FIFO_DELIM);
					}

					// if fifo not at size limit 
					// and value to be added is not already present
					// in top or fifo then add where appropriate
					if (fifoEntries.length < fifoLimitValue) {
						if (value == null || value.length() == 0
								|| value.contains(FIFO_DELIM)) {
							UnitManager.Logging
									.logSevere(RuleConstants.INVALID_FIFO_COMMAND_VALUE);
							throw new CoreException(
									RuleConstants.ERROR_EXECUTING_RULE
											+ RuleConstants.SEPARATOR
											+ RuleConstants.INVALID_FIFO_COMMAND_VALUE);
						} else {
							// if top state is empty add value there
							// no need to check fifo since it must be empty
							if (topStateValue == null
									|| topStateValue.length() == 0) {
								saveExpressionResult(resultName,
										getJavaType(resultName), value);
							} else { // top not empty so
								// if nothing in fifo only check top state
								if (fifoStateVal == null
										|| fifoStateVal.length() == 0) {
									// make sure value not same as that in top state
									if (!topStateValue.equalsIgnoreCase(value)) {
										// save in fifo
										saveExpressionResult(fifoState,
											getJavaType(fifoState), 
											value + FIFO_DELIM);
									} // else discard
								} else { // fifo not empty 
									// so check both top and fifo for already present
									boolean alreadyInFifo = false;
									// if not in top state
									if (!topStateValue.equalsIgnoreCase(value)) {
										// ensure value not already present in fifo
										for (String entry:fifoEntries) {
											if (entry.equalsIgnoreCase(value)) {
												alreadyInFifo = true;
												break;
											}
										}
										if (!alreadyInFifo) {
											// add to fifo
											saveExpressionResult(fifoState,
												getJavaType(fifoState),
												fifoStateVal + value + FIFO_DELIM); 
										}
									}
								}
							}
						}
					} else {
						if (UnitManager.Logging.isDebug()) {
							UnitManager.Logging.logDebug(RuleConstants.FIFO_FULL);
						}
					}
				} else if (command.equalsIgnoreCase(FifoCommands.REM.getFifoCmd())) {
					// if REM command w/o value argument
					if (cmdDelimIndex == -1) {
						// if fifo empty simply remove top value 
						if (fifoStateVal == null || fifoStateVal.length() == 0) {
								saveExpressionResult(resultName,
									getJavaType(resultName), "");
						} else {						
							// else advance 1st fifo entry to top
							final int queueDelimIndex = fifoStateVal.indexOf(FIFO_DELIM);
							String[] entries = fifoStateVal.split(FIFO_DELIM);
							// if only one entry in fifo
							if (entries.length == 1) {
								// advance 1st fifo entry to top
								saveExpressionResult(resultName,
										getJavaType(resultName), 
										entries[0]);
								
								// then clear fifo
								saveExpressionResult(fifoState,
										getJavaType(fifoState), "");	
							} else {
								// more than one entry in fifo so
								// move 1st fifo entry to top
								saveExpressionResult(resultName,
										getJavaType(resultName), 
										entries[0]);
								
								// remove 1st fifo entry from fifo
								saveExpressionResult(fifoState,
										getJavaType(fifoState), 
										fifoStateVal.substring(queueDelimIndex
										+ FIFO_DELIM.length()));
							}							
						}
					} else { // else if REM command w/ value argument
						// parse the value to be removed
						value = cmdStateVal.substring(cmdDelimIndex + 1).trim();
						// logic SYNOPSIS:
						// search for match in top & fifo and remove if found
						// then advance/rebuild fifo as appropriate

						// why not use 'topStateValue' here ???
						final String resultValue = (String) this.getValueObject(
								resultName, resultDataType);
					
						// if top state is empty then FIFO is empty 
						// and there is nothing to do
						if (resultValue == null || resultValue.length() == 0) {
							if (UnitManager.Logging.isDebug()) {
								UnitManager.Logging.logDebug(RuleConstants.FIFO_EMPTY);
							}
						} else { // top not empty so
							// first check to see if top is a match
							if (resultValue.equalsIgnoreCase(value)) {
								// if fifo empty simply remove top
								if (fifoStateVal == null || fifoStateVal.length() == 0) {
									saveExpressionResult(resultName,
											getJavaType(resultName), "");
									
								} else { // fifo not empty 
									// so an advance to top is required
									// after top removal
									final int queueDelimIndex = fifoStateVal.indexOf(FIFO_DELIM);
									String[] entries = fifoStateVal.split(FIFO_DELIM);
									
									// if only one entry in fifo
									if (entries.length == 1) {									
										// advance 1st fifo entry to top
										saveExpressionResult(resultName,
												getJavaType(resultName), 
												entries[0]);
										
										// clear fifo
										saveExpressionResult(fifoState,
												getJavaType(fifoState), "");	
									} else {
										// more than one entry in fifo so
										// move 1st fifo entry to top
										saveExpressionResult(resultName,
												getJavaType(resultName), 
												entries[0]);
										
										// remove 1st fifo entry from fifo
										saveExpressionResult(fifoState,
												getJavaType(fifoState), 
												fifoStateVal.substring(queueDelimIndex
												+ FIFO_DELIM.length()));
									}
								}
							} else { // top not a match so
								// check to see if fifo contains value
								// NOTE: no advance to top is required but 
								// remove from and rebuild fifo as necessary
								if (fifoStateVal != null && fifoStateVal.length() > 0) {
									String[] entries = fifoStateVal.split(FIFO_DELIM);
									// if only one entry in fifo
									if (entries.length == 1) {
										if (entries[0].equalsIgnoreCase(value)) {
											// delete only entry in fifo if match
											saveExpressionResult(fifoState,
													getJavaType(fifoState), "");
										}
									} else { // more than one entry in fifo
										// so search fifo for a match
										if (entries.length > 1) {
											for (String entry:entries) {
												if (entry.equalsIgnoreCase(value)) {
													String newFifoVal = fifoStateVal.replaceFirst(
															(value + FIFO_DELIM), "");
													saveExpressionResult(fifoState,
															getJavaType(fifoState), newFifoVal);
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				} else if (command.equalsIgnoreCase(FifoCommands.CLEAR
						.getFifoCmd())) {
					if (UnitManager.Logging.isDebug()) {
						UnitManager.Logging.logDebug(RuleConstants.CLEARING_FIFO);
					}
					// clear top state
					saveExpressionResult(resultName, getJavaType(resultName),
							"");
					// clear fifo state
					saveExpressionResult(fifoState, getJavaType(fifoState),
							"");
				} else {
					UnitManager.Logging.logSevere(RuleConstants.INVALID_FIFO_COMMAND
									+ ": " + cmdStateVal);
					throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
							+ RuleConstants.SEPARATOR
							+ RuleConstants.INVALID_FIFO_COMMAND + ": "
							+ cmdStateVal);
				}
				break;
			}
			default: {
				throw new CoreException(RuleConstants.INVALID_METHOD);
			}
			}
		}

	}

	/**
	 * Process a RuleConstruct of the Rule and execute Expressions.
	 * 
	 * @param ruleConstruct The rule construct
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private void processRuleConstruct(final RuleConstruct ruleConstruct)
			throws RulesEngineException {
		try {
			boolean bConditionResult = false;
			if (ruleConstruct != null) {
				if (ruleConstruct.getRuleConstructType() == RuleMacros.RuleConstructType.EXPRESSION) {
					executeExpression((Expression) ruleConstruct);
				} else if (ruleConstruct.getRuleConstructType() == RuleMacros.RuleConstructType.CONTROL_FLOW) {
					final ControlFlow ctrlFlow = (ControlFlow) ruleConstruct;
					if (ctrlFlow.getControlFlowType() == RuleMacros.ControlFlowType.IF) {
						if (true == evaluateConditionList(ctrlFlow
								.getCtrlFLowConditionList())) {
							if (ctrlFlow.getRuleConstruct() != null) {
								for (final Object obj : ctrlFlow
										.getRuleConstruct()) {
									processRuleConstruct((RuleConstruct) obj);
								}
							} else {
								// TBR No need to throw exception here
							}
						} else {
							final ArrayList<ControlFlow> elseCtrlFlows = ctrlFlow
									.getElseControlFlowList();
							if (elseCtrlFlows != null
									&& elseCtrlFlows.size() >= 1) {
								for (final ControlFlow obj : elseCtrlFlows) {
									if (obj.getControlFlowType() == RuleMacros.ControlFlowType.ELSEIF) {
										bConditionResult = evaluateConditionList(obj
												.getCtrlFLowConditionList());
										if (true == bConditionResult) {
											if (obj.getRuleConstruct() != null) {
												for (final Object innerObj : obj
														.getRuleConstruct()) {
													processRuleConstruct((RuleConstruct) innerObj);
												}
											} else {
												// TBR No need to throw
												// exception here
											}
											break;
										} else {
											continue;
										}
									} else if (obj.getControlFlowType() == RuleMacros.ControlFlowType.ELSE) {
										if (obj.getRuleConstruct() != null) {
											for (final Object innerObj : obj
													.getRuleConstruct()) {
												processRuleConstruct((RuleConstruct) innerObj);
											}
										} else {
											// TBR No need to throw exception
											// here
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE,
					exp);
		}
	}

	/**
	 * Evaluates the conditional expression.
	 * 
	 * @param conditions List of the conditions
	 * 
	 * @return true, if successful
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private boolean evaluateConditionList(
			final ArrayList<ControlFlowCondition> conditions)
			throws RulesEngineException {

		final StringBuffer boolExpression = new StringBuffer();
		ControlFlowCondition cnd;
		Object lhObj;
		Object rhObj;
		String op;
		String logicOp;
		final String space = " ";
		boolean temp;
		boolean result;

		if (conditions != null && conditions.size() > 0) {
			for (final Object obj : conditions) {
				cnd = (ControlFlowCondition) obj;
				lhObj = getValueObject(cnd.getLh());
				rhObj = getValueObject(cnd.getRh());
				op = cnd.getOp();
				logicOp = cnd.getLop();
				temp = RuleHelper.evaluateCondition(lhObj, rhObj, RuleHelper
						.getOperatorEnum(op));
				if (logicOp != null) {
					boolExpression.append(logicOp);
					boolExpression.append(space);
				}
				boolExpression.append(temp);
				boolExpression.append(space);

			}
			result = BoolExpEvaluator.evaluateBooleanExpression(boolExpression
					.toString());
		} else {
			throw new RulesEngineException(RuleConstants.INVALID_RULE
					+ RuleConstants.SEPARATOR 
					+ RuleConstants.INVALID_CONDITION);
		}

		return result;
	}

	/**
	 * Gets the object value of a State/Variable/Constant.
	 * 
	 * @param key The name of the State/Variable/Constant
	 * 
	 * @return the object value
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private Object getValueObject(final String key) 
	throws RulesEngineException {
		Object value;
		if (localVarsConst.containsKey(key)) {
			value = localVarsConst.get(key);
		} else if (localStateDB.containsKey(key)) {
			final String stateValue = localStateDB.get(key);

			int stateIndex = stateDatabase.getIndex(key);

			if (stateIndex != -1) {
				final String stateType = stateDatabase.getType(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE);
			}

		} else {

			int stateIndex = stateDatabase.getIndex(key);

			if (stateIndex != -1) {
				final String stateType = stateDatabase.getType(stateIndex);
				final String stateValue = stateDatabase.getValue(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE);
			}
		}
		return value;
	}

	// RC-TC00003870 - Start

	/**
	 * Gets the original value object.
	 * 
	 * @param key the key
	 * 
	 * @return the original value object
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private Object getOriginalValueObject(final String key)
			throws RulesEngineException {
		Object value;

		if (originalStateDB.containsKey(key)) {
			value = originalStateDB.get(key);
			int stateIndex = stateDatabase.getIndex(key);
			try {
				value = RuleHelper.getObjectByType(stateDatabase
						.getType(stateIndex), stateDatabase
						.getValue(stateIndex));
			} catch (Exception e) {
				UnitManager.Logging
						.logSevere("Exception in getOriginalValueObject....."
								+ e);
				UnitManager.Logging.logSevere("getOriginalValueObject()->value"
						+ value);
			}
		} else if (localVarsConst.containsKey(key)) {
			value = localVarsConst.get(key);
		} else if (localStateDB.containsKey(key)) {
			final String stateValue = localStateDB.get(key);
			int stateIndex = stateDatabase.getIndex(key);
			if (stateIndex != -1) {
				final String stateType = stateDatabase.getType(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE);
			}
		} else {

			int stateIndex = stateDatabase.getIndex(key);
			if (stateIndex != -1) {
				final String stateType = stateDatabase.getType(stateIndex);
				final String stateValue = stateDatabase.getValue(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE);
			}
		}
		return value;
	}

	// RC-TC00003870 - End

	// RC-TC00003873 - Start
	/**
	 * Gets the value object.
	 * 
	 * @param key the key
	 * @param resultDataType the result data type
	 * 
	 * @return the value object
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	private Object getValueObject(final String key, final String resultDataType)
			throws RulesEngineException {
		Object value;
		if (localVarsConst.containsKey(key)) {
			value = localVarsConst.get(key);
		} else if (localStateDB.containsKey(key)) {
			final String stateValue = localStateDB.get(key);

			int stateIndex = stateDatabase.getIndex(key);
			if (stateIndex != -1) {
				final String stateType = stateDatabase.getType(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE);
			}

		} else {
			UnitManager.Logging
					.logDebug("getValueObject()->checking in "
							+ "complete State Database");
			int stateIndex = stateDatabase.getIndex(key);
			if (stateIndex != -1) {
				UnitManager.Logging
						.logDebug("getValueObject()->contained " 
								+ "in complete State Database");
				final String stateType = stateDatabase.getType(stateIndex);
				final String stateValue = stateDatabase.getValue(stateIndex);
				value = RuleHelper.getObjectByType(stateType, stateValue);
			} else {
				UnitManager.Logging
						.logDebug("getValueObject()->computing the " 
								+ "numeric constant...");
				value = RuleHelper.getObjectByType(resultDataType, key);
				if (value == null) {
					UnitManager.Logging
							.logWarning("RulesEngineImpl->Value is found " 
									+ "null in getValueObject()");
					throw new RulesEngineException(
							RuleConstants.ERROR_EXECUTING_RULE);
				} else {
					UnitManager.Logging
							.logDebug("Hurray constructed the new Object");
				}
			}
		}
		return value;
	}

	// RC-TC00003873 - End

	/**
	 * Gets the java type for a State/Variable/Constant.
	 * 
	 * @param key
	 *            The name of the State/Variable/Constant
	 * 
	 * @return the java type
	 * 
	 * @throws CoreException
	 *             , the core exception
	 */
	private String getJavaType(final String key) throws CoreException {
		String classType;
		if (key != null) {
			if (this.localVarsConst.containsKey(key)) {
				classType = localVarsConst.get(key).getClass().getName();
			} else {
				int stateIndex = stateDatabase.getIndex(key);
				final String stateType = stateDatabase.getType(stateIndex);
				classType = stateType;
			}
			return classType;
		} else {
			throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR + RuleConstants.INVALID_INPUT);
		}
	}

	/**
	 * Save expression result into the temporary HashMap.
	 * 
	 * @param resultName
	 *            The result name
	 * @param resultType
	 *            The result type
	 * @param resultValue
	 *            The result value
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	private void saveExpressionResult(final String resultName,
			final String resultType, final Object resultValue)
			throws CoreException {
		Object resultObj;
		String strResult = null;
		try {
			switch (RuleHelper.getEnumForType(resultType)) {
			case BOOLEAN:
				strResult = String.valueOf(((Boolean) resultValue)
						.booleanValue());
				break;
			case STRING:
				strResult = resultValue.toString();
				break;
			case DOUBLE:
				strResult = String
						.valueOf(((Number) resultValue).doubleValue());
				break;
			case LONG:
				strResult = String.valueOf(((Number) resultValue).longValue());
				break;
			default:
				throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE
						+ RuleConstants.SEPARATOR + RuleConstants.INVALID_TYPE);
			}

			if (localVarsConst.containsKey(resultName)) {
				resultObj = RuleHelper.getObjectByType(resultType, strResult);
				localVarsConst.put(resultName, resultObj);
			} else if (stateDatabase.getIndex(resultName) != -1) {
				localStateDB.put(resultName, strResult);
			}
		} catch (final CoreException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new CoreException(RuleConstants.ERROR_EXECUTING_RULE, exp);
		}
	}

	/**
	 * Gets the last rule executed.
	 * 
	 * @return the last rule executed
	 */
	public final String getLastRuleExecuted() {
		return lastRuleExecuted;
	}

	/**
	 * Load rules.
	 * 
	 * @param rulesFile the rules file
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	public final void loadRules(final File rulesFile)
	throws RulesEngineException {
		// if (!loadSerRules()) {
			final RuleParser parser = new RuleParser();
	
			parser.parseRules(rulesFile.getAbsolutePath());
	
			this.ruleCollection = parser.getRuleCollection();
	
			if (this.ruleCollection != null) {
				if (UnitManager.Logging.isInfo()) {
					UnitManager.Logging
							.logInfo("Successfully loaded Rule's Collection from "
									+ rulesFile.getAbsolutePath());
				}
				//saveRules();
			}
		//}
	}

	/*******************************
	private final boolean loadSerRules() {
		boolean serSuccess = false;

		if (rulesSerFile != null && rulesSerFile.exists()) {
			final Object serObj = UnitManager.IO.deserialize(rulesSerFile);

			 
			if (serObj instanceof RuleCollection) {
				ruleCollection = (RuleCollection) serObj;
				
				UnitManager.Logging.logWarning(ruleCollection.toString());
				
				serSuccess = true;

			} else {
				UnitManager.Logging
						.logSevere("Could not load Serialized Rules.");
			}
		}
		else {
			UnitManager.Logging
				.logSevere("Serialized Rules file doesn't exist.");
		}

		return serSuccess;
	}
	
	public final boolean saveRules() {
		boolean result = UnitManager.IO.serialize(this.ruleCollection, rulesSerFile);
		if (result) {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Rules serialized to: "
						+ rulesSerFile);			
			}
		}
		return result;
	}
	
	public synchronized final void setSerRulesFile(final File serRulesFile) {
		this.rulesSerFile = serRulesFile;
	}

	public synchronized final File getSerRulesFile() {
		return this.rulesSerFile;
	}
	*********************************/

}
