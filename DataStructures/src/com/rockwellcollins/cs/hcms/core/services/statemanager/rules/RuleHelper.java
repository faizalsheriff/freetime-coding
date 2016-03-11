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
import java.util.StringTokenizer;

import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * The Class RuleHelper defines the methods supported by the RuleEngine.
 */
public final class RuleHelper {

	/**
	 * Instantiates a new rule helper.
	 */
	private RuleHelper() {

	}

	/**
	 * Adds variable number of arguments of type Number.
	 * 
	 * @param args The list of operands that needs to be added
	 * 
	 * @return result of type Number after adding the arguments
	 * 
	 * @throws RulesEngineException the rules engine exception
	 */
	public static Number add(final Number... args) throws RulesEngineException {
		Number result;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; i++) {
				result = addNumbers(result, args[i]);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Adds 2 operands of type Number.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return result of type Number after adding the input Numbers
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	private static Number addNumbers(final Number num1, final Number num2)
			throws RulesEngineException {
		Number result;
		try {
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = num1.longValue() + num2.longValue();
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = num1.longValue() + num2.doubleValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = num1.doubleValue() + num2.longValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = num1.doubleValue() + num2.doubleValue();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
			return result;
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
	}

	/**
	 * Multiplies variable number of arguments of type Number.
	 * 
	 * @param args
	 *            The list of operands that needs to be multiplied
	 * 
	 * @return result of type Number after multiplying the arguments
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number multiply(final Number... args)
			throws RulesEngineException {
		Number result;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; i++) {
				result = multiplyNumbers(result, args[i]);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Multiplies 2 operands of type Number.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return result of type Number after multiplying the input Numbers
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	private static Number multiplyNumbers(final Number num1, final Number num2)
			throws RulesEngineException {
		Number result;
		try {
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = num1.longValue() * num2.longValue();
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = num1.longValue() * num2.doubleValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = num1.doubleValue() * num2.longValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = num1.doubleValue() * num2.doubleValue();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
			return result;
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
	}

	/**
	 * Returns the ratio of 2 Numbers.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return result of type Number after dividing the first Number by the
	 *         second
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number divide(final Number num1, final Number num2)
			throws RulesEngineException {

		Number result = null;
		try {
			if (num2.doubleValue() == 0.0) {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.OPERATION_NOT_ALLOWED
								+ RuleConstants.SEPARATOR
								+ RuleConstants.DIVISION_BY_ZERO);
			}
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = num1.longValue() / num2.longValue();
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = num1.longValue() / num2.doubleValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = num1.doubleValue() / num2.longValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = num1.doubleValue() / num2.doubleValue();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final ArithmeticException exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.OPERATION_NOT_ALLOWED, exp);
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
		return result;
	}

	/**
	 * Returns the difference of 2 Numbers.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return result of type Number after subtracting the second Number from
	 *         the first
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number subtract(final Number num1, final Number num2)
			throws RulesEngineException {

		Number result;
		try {
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = num1.longValue() - num2.longValue();
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = num1.longValue() - num2.doubleValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = num1.doubleValue() - num2.longValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = num1.doubleValue() - num2.doubleValue();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
		return result;
	}

	/**
	 * Returns the Average of the list of arguments of type Number.
	 * 
	 * @param args
	 *            The list of Number arguments.
	 * 
	 * @return the average as Number
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number average(final Number... args)
			throws RulesEngineException {
		Number sum, avg;
		if (args != null && args.length > 0) {
			final long size = args.length;
			sum = add(args);
			avg = divide(sum, size);
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return avg;
	}

	/**
	 * Concatenates the multiple String arguments passed as input.
	 * 
	 * @param args
	 *            The list of strings to be concatenated.
	 * 
	 * @return the string after concatenating the input arguments
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static String concat(final String... args)
			throws RulesEngineException {
		final StringBuffer result = new StringBuffer();
		if (args != null && args.length > 0) {
			for (final String str : args) {
				result.append(str);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result.toString();
	}

	/**
	 * Performs an AND operation between the multiple Boolean arguments passed
	 * as input.
	 * 
	 * @param args
	 *            Boolean arguments which are to be AND'ed
	 * 
	 * @return the boolean value after the AND operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static boolean doAND(final Boolean... args) throws RulesEngineException {
		boolean result = false;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result &= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Performs an AND operation between the multiple Long (0,1) arguments
	 * passed as input.
	 * 
	 * @param args
	 *            Long arguments which are to be AND'ed
	 * 
	 * @return the long value after the AND operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static long doAND(final Long... args) throws RulesEngineException {
		long result = 0;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result &= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Performs an OR operation on the multiple Boolean arguments passed as
	 * input.
	 * 
	 * @param args
	 *            Boolean arguments which are to be OR'ed
	 * 
	 * @return the boolean value after the OR operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static boolean doOR(final Boolean... args) throws RulesEngineException {
		boolean result = false;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result |= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Performs an OR operation on the multiple Long arguments passed as input.
	 * 
	 * @param args
	 *            Long arguments which are to be OR'ed
	 * 
	 * @return the long value after the OR operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static long doOR(final Long... args) throws RulesEngineException {
		long result = 0;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result |= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Performs an XOR operation on the multiple Boolean arguments passed as
	 * input.
	 * 
	 * @param args
	 *            Boolean arguments which are to be XOR'ed
	 * 
	 * @return the boolean value after the XOR operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static boolean doXOR(final Boolean... args) throws RulesEngineException {
		boolean result = false;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result ^= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Performs an XOR operation on the multiple bit (Long) arguments passed as
	 * input.
	 * 
	 * @param args
	 *            Long arguments which are to be XOR'ed
	 * 
	 * @return the Long value after the XOR operation
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	static long doXOR(final Long... args) throws RulesEngineException {
		long result = 0;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; ++i) {
				result ^= args[i];
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Toggles the boolean value passed as input.
	 * 
	 * @param flag
	 *            The boolean value to be inverted
	 * 
	 * @return the inverted value
	 */
	static final boolean toggle(boolean flag) {
		return !flag;
	}

	/**
	 * Toggles the Long value (0,1) passed as input.
	 * 
	 * @param flag
	 *            The Long value to be inverted
	 * 
	 * @return the inverted value (1 if and only if argument is 0, otherwise
	 *         always 0)
	 */
	static long toggle(final long flag) {
		if (flag == 0) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the smallest number from the list of arguments of type Number.
	 * 
	 * @param args
	 *            The Number arguments
	 * 
	 * @return the smallest Number
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */

	public static Number minimum(final Number... args)
			throws RulesEngineException {

		Number result;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; i++) {
				result = getMin(result, args[i]);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Returns the smaller of the two numbers.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return the smallest Number
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	private static Number getMin(final Number num1, final Number num2)
			throws RulesEngineException {

		Number result;
		try {
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = Math.min(num1.longValue(), num2.longValue());
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = Math.min(num1.longValue(), num2.doubleValue());
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = Math.min(num1.doubleValue(), num2.longValue());
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = Math.min(num1.doubleValue(), num2.doubleValue());
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
			return result;
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
	}

	/**
	 * Returns the largest number from the list of arguments of type Number.
	 * 
	 * @param args
	 *            The Number arguments
	 * 
	 * @return the largest Number
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number maximum(final Number... args)
			throws RulesEngineException {
		Number result;
		if (args != null && args.length > 0) {
			result = args[0];
			for (int i = 1; i < args.length; i++) {
				result = getMax(result, args[i]);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT);
		}
		return result;
	}

	/**
	 * Returns the bigger of the two numbers.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return the bigger Number
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	private static Number getMax(final Number num1, final Number num2)
			throws RulesEngineException {

		Number result;
		try {
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = Math.max(num1.longValue(), num2.longValue());
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = Math.max(num1.longValue(), num2.doubleValue());
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = Math.max(num1.doubleValue(), num2.longValue());
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = Math.max(num1.doubleValue(), num2.doubleValue());
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
			return result;
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
	}

	/**
	 * Gets the data type of the input Number argument.
	 * 
	 * @param num
	 *            The Number Object
	 * 
	 * @return the type of the Number argument in Long
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static RuleMacros.JavaTypes getNumberType(final Number num)
			throws RulesEngineException {
		RuleMacros.JavaTypes type;
		if (num != null) {
			if (num instanceof java.lang.Long) {
				type = RuleMacros.JavaTypes.LONG;
			} else if (num instanceof java.lang.Double) {
				type = RuleMacros.JavaTypes.DOUBLE;
			} else {
				throw new RulesEngineException(RuleConstants.INVALID_TYPE);
			}
			return type;
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE);
		}
	}

	/**
	 * Gets the data type of the input argument Value Object.
	 * 
	 * @param obj
	 *            The Value Object
	 * 
	 * @return the object type
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static RuleMacros.JavaTypes getObjectType(final Object obj)
			throws RulesEngineException {
		RuleMacros.JavaTypes type;
		if (obj != null) {
			if (obj instanceof java.lang.Long) {
				type = RuleMacros.JavaTypes.LONG;
			} else if (obj instanceof java.lang.Double) {
				type = RuleMacros.JavaTypes.DOUBLE;
			} else if (obj instanceof java.lang.Boolean) {
				type = RuleMacros.JavaTypes.BOOLEAN;
			} else if (obj instanceof java.lang.String) {
				type = RuleMacros.JavaTypes.STRING;
			} else {
				throw new RulesEngineException(RuleConstants.INVALID_TYPE);
			}
			return type;
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE);
		}

	}

	/**
	 * Checks whether the method is an AddOn or not.
	 * 
	 * @param methodName
	 *            The method name
	 * 
	 * @return true/false
	 */
	public static boolean isAddOnMethod(final String methodName) {
		if (methodName != null && methodName.indexOf('.') != -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Executes the AddOn method defined by the input AddOnMethodInfo object.
	 * 
	 * @param addOnMethodInfo The add on method information.
	 * 
	 * @return the object
	 */
	public static Object executeAddOnMethod(
			final AddOnMethodInfo addOnMethodInfo) {
		Object result = null;
		if (addOnMethodInfo != null) {
			final String className = addOnMethodInfo.getClassName();
			// String returnType = addOnMethodInfo.getReturnType();
			// ArrayList<Object> argList = addOnMethodInfo.getArgObjList();

			try {
				final Object[] objArr = { addOnMethodInfo };
				final Class<?> cls = Class.forName(className);
				final RulesEngineAddOn iFace = (RulesEngineAddOn) cls
						.newInstance();
				result = iFace.execute(objArr);
				// Method[] mthds = cls.getMethods();
				// Method mthd = cls.getMethod("execute");

				// result = mthd.invoke(cls.newInstance(), objArr);
			} catch (final Exception e) {

				UnitManager.Logging.logSevere(e);
			}
		}
		return result;
	}

	/**
	 * Evaluate a simple condition.
	 * 
	 * @param lh
	 *            The left hand operand
	 * @param rh
	 *            The right hand operand
	 * @param op
	 *            The operator
	 * 
	 * @return true, if successful
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static boolean evaluateCondition(final Object lh, final Object rh,
			final RuleMacros.CompOperator op) throws RulesEngineException {
		boolean result = false;
		if (lh instanceof java.lang.Long && rh instanceof java.lang.Long) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((Long) lh).longValue() == ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GE) {
				if (((Long) lh).longValue() >= ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GT) {
				if (((Long) lh).longValue() > ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LE) {
				if (((Long) lh).longValue() <= ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LT) {
				if (((Long) lh).longValue() < ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.NE) {
				if (((Long) lh).longValue() != ((Long) rh).longValue()) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else if (lh instanceof java.lang.Double
				&& rh instanceof java.lang.Long) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((Double) lh).doubleValue() == ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GE) {
				if (((Double) lh).doubleValue() >= ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GT) {
				if (((Double) lh).doubleValue() > ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LE) {
				if (((Double) lh).doubleValue() <= ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LT) {
				if (((Double) lh).doubleValue() < ((Long) rh).longValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.NE) {
				if (((Double) lh).doubleValue() != ((Long) rh).longValue()) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else if (lh instanceof java.lang.Long
				&& rh instanceof java.lang.Double) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((Long) lh).longValue() == ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GE) {
				if (((Long) lh).longValue() >= ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GT) {
				if (((Long) lh).longValue() > ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LE) {
				if (((Long) lh).longValue() <= ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LT) {
				if (((Long) lh).longValue() < ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.NE) {
				if (((Long) lh).longValue() != ((Double) rh).doubleValue()) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else if (lh instanceof java.lang.Double
				&& rh instanceof java.lang.Double) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((Double) lh).doubleValue() == ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GE) {
				if (((Double) lh).doubleValue() >= ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.GT) {
				if (((Double) lh).doubleValue() > ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LE) {
				if (((Double) lh).doubleValue() <= ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.LT) {
				if (((Double) lh).doubleValue() < ((Double) rh).doubleValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.NE) {
				if (((Double) lh).doubleValue() != ((Double) rh).doubleValue()) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else if (lh instanceof java.lang.Boolean
				&& rh instanceof java.lang.Boolean) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((Boolean) lh).booleanValue() == ((Boolean) rh)
						.booleanValue()) {
					result = true;
				}
			} else if (op == RuleMacros.CompOperator.NE) {
				if (((Boolean) lh).booleanValue() != ((Boolean) rh)
						.booleanValue()) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else if (lh instanceof java.lang.String
				&& rh instanceof java.lang.String) {
			if (op == RuleMacros.CompOperator.EQ) {
				if (((String) lh).equalsIgnoreCase((String) rh)) {
					result = true;
				}
			} // RC-TC00004487
			else if (op == RuleMacros.CompOperator.NE) {
				if (!((String) lh).equalsIgnoreCase((String) rh)) {
					result = true;
				}
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR + RuleConstants.INVALID_TYPE);
		}
		return result;
	}

	/**
	 * Returns the Wrapper object, for the input primitive data type or String.
	 * 
	 * @param stateType
	 *            The state type
	 * @param stateValue
	 *            The state value
	 * 
	 * @return wrapper Object for the State primitive data type
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Object getObjectByType(final String stateType,
			final String stateValue) throws RulesEngineException {
		Object retObject;
		if (RuleMacros.JavaTypes.LONG.getPrimitiveType().equalsIgnoreCase(
				stateType)
				|| RuleMacros.JavaTypes.LONG.getWrapperType().equalsIgnoreCase(
						stateType)) {
			retObject = new Long(stateValue);
		} else if (RuleMacros.JavaTypes.DOUBLE.getPrimitiveType()
				.equalsIgnoreCase(stateType)
				|| RuleMacros.JavaTypes.DOUBLE.getWrapperType()
						.equalsIgnoreCase(stateType)) {
			retObject = new Double(stateValue);
		} else if (RuleMacros.JavaTypes.BOOLEAN.getPrimitiveType()
				.equalsIgnoreCase(stateType)
				|| RuleMacros.JavaTypes.BOOLEAN.getWrapperType()
						.equalsIgnoreCase(stateType)) {
			retObject = Boolean.valueOf(stateValue);
		} else if (RuleMacros.JavaTypes.STRING.getPrimitiveType()
				.equalsIgnoreCase(stateType)
				|| RuleMacros.JavaTypes.STRING.getWrapperType()
						.equalsIgnoreCase(stateType)) {
			/* For String types */
			retObject = String.valueOf(stateValue);
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR + RuleConstants.INVALID_TYPE);
		}
		return retObject;
	}

	/**
	 * Gets the Enumeration Value for a Java data type.
	 * 
	 * @param type
	 *            The String containing java type
	 * 
	 * @return the Enum Value for a particular Java data type
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static RuleMacros.JavaTypes getEnumForType(final String type)
			throws RulesEngineException {
		RuleMacros.JavaTypes javaType;
		if (type != null) {
			if (RuleMacros.JavaTypes.LONG.getPrimitiveType().equalsIgnoreCase(
					type)
					|| RuleMacros.JavaTypes.LONG.getWrapperType()
							.equalsIgnoreCase(type)) {
				javaType = RuleMacros.JavaTypes.LONG;
			} else if (RuleMacros.JavaTypes.DOUBLE.getPrimitiveType()
					.equalsIgnoreCase(type)
					|| RuleMacros.JavaTypes.DOUBLE.getWrapperType()
							.equalsIgnoreCase(type)) {
				javaType = RuleMacros.JavaTypes.DOUBLE;
			} else if (RuleMacros.JavaTypes.BOOLEAN.getPrimitiveType()
					.equalsIgnoreCase(type)
					|| RuleMacros.JavaTypes.BOOLEAN.getWrapperType()
							.equalsIgnoreCase(type)) {
				javaType = RuleMacros.JavaTypes.BOOLEAN;
			} else if (RuleMacros.JavaTypes.STRING.getPrimitiveType()
					.equalsIgnoreCase(type)
					|| RuleMacros.JavaTypes.STRING.getWrapperType()
							.equalsIgnoreCase(type)) {
				javaType = RuleMacros.JavaTypes.STRING;
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_TYPE);
			}
			return javaType;
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE);
		}
	}

	/**
	 * Gets the Enum Value for a operator.
	 * 
	 * @param op
	 *            The String containing a comparison operator
	 * 
	 * @return the Enum Value for a particular operator type
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static RuleMacros.CompOperator getOperatorEnum(final String op)
			throws RulesEngineException {
		RuleMacros.CompOperator operator;
		if (op != null) {
			if (RuleMacros.CompOperator.EQ.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.EQ;
			} else if (RuleMacros.CompOperator.GE.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.GE;
			} else if (RuleMacros.CompOperator.GT.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.GT;
			} else if (RuleMacros.CompOperator.NE.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.NE;
			} else if (RuleMacros.CompOperator.LE.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.LE;
			} else if (RuleMacros.CompOperator.LT.name().equalsIgnoreCase(op)) {
				operator = RuleMacros.CompOperator.LT;
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_OPERATOR);
			}
			return operator;
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE);
		}
	}

	/**
	 * Gets the enum Value for a method name.
	 * 
	 * @param methodName
	 *            The method name
	 * 
	 * @return the enum Value for a method name
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static RuleMacros.Methods getMethodEnum(final String methodName)
			throws RulesEngineException {
		RuleMacros.Methods method;
		if (methodName != null) {
			if (RuleMacros.Methods.ADD.name().equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.ADD;
			} else if (RuleMacros.Methods.AND.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.AND;
			} else if (RuleMacros.Methods.AVG.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.AVG;
			} else if (RuleMacros.Methods.CONCAT.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.CONCAT;
			} else if (RuleMacros.Methods.DIV.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.DIV;
			} else if (RuleMacros.Methods.MAX.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.MAX;
			} else if (RuleMacros.Methods.MIN.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.MIN;
			} else if (RuleMacros.Methods.MUL.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.MUL;
			} else if (RuleMacros.Methods.OR.name()
					.equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.OR;
			} else if (RuleMacros.Methods.SUB.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.SUB;
			} else if (RuleMacros.Methods.TOGGLE.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.TOGGLE;
			} else if (RuleMacros.Methods.XOR.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.XOR;
			} else if (RuleMacros.Methods.EQUALTO.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.EQUALTO;
			} else if (RuleMacros.Methods.MOD.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.MOD;
			} else if (RuleMacros.Methods.GETCURRENTTIME.name()
					.equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.GETCURRENTTIME;
			} else if (RuleMacros.Methods.QUEUE.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.QUEUE;
			} else if (RuleMacros.Methods.FIFO.name().equalsIgnoreCase(
					methodName)) {
				method = RuleMacros.Methods.FIFO;
			} else if (RuleMacros.Methods.EQUALTO_RESULTASPOINTER.name()
					.equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.EQUALTO_RESULTASPOINTER;
			} else if (RuleMacros.Methods.EQUALTO_ARGASPOINTER.name()
					.equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.EQUALTO_ARGASPOINTER;
			} else if (RuleMacros.Methods.ASSIGN.name()
					.equalsIgnoreCase(methodName)) {
				method = RuleMacros.Methods.ASSIGN;
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD);
			}
			return method;
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE);
		}
	}

	/**
	 * Returns the modulus of 2 Numbers.
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * 
	 * @return the modulus (remainder) of two numbers
	 * 
	 * @throws RulesEngineException
	 *             the core exception
	 */
	public static Number mod(final Number num1, final Number num2)
			throws RulesEngineException {

		Number result = null;
		try {
			if (num2.doubleValue() == 0.0) {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.OPERATION_NOT_ALLOWED);
			}
			if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Long) {
				result = num1.longValue() % num2.longValue();
			} else if (num1 instanceof java.lang.Long
					&& num2 instanceof java.lang.Double) {
				result = num1.longValue() % num2.doubleValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Long) {
				result = num1.doubleValue() % num2.longValue();
			} else if (num1 instanceof java.lang.Double
					&& num2 instanceof java.lang.Double) {
				result = num1.doubleValue() % num2.doubleValue();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_METHOD_ARGUMENT);
			}
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final ArithmeticException exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.OPERATION_NOT_ALLOWED, exp);
		} catch (final Exception exp) {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR
					+ RuleConstants.INVALID_METHOD_ARGUMENT, exp);
		}
		return result;
	}
}

/**
 * The Class BoolExpEvaluator, provides the functionality to convert a boolean
 * expression from InFix notation to PostFix notation. And also evaluates a
 * PostFix boolean expression. t
 */
class BoolExpEvaluator {

	/** The operator map. */
	private static HashMap<String, Integer> operatorMap;

	/**
	 * Evaluate a boolean expression.
	 * 
	 * @param expr
	 *            The string containing boolean expression.
	 * 
	 * @return true, if successful
	 * 
	 * @throws RulesEngineException
	 *             the exception
	 */
	static boolean evaluateBooleanExpression(final String expr)
			throws RulesEngineException {

		final Stack<Boolean> operandStack = new Stack<Boolean>();
		String exprToExecute = "";
		String token = "";
		boolean result = false;
		operatorMap = new HashMap<String, Integer>();
		try {
			populateMap(operatorMap);
			if (expr != null && !expr.equals("")) {
				exprToExecute = expr.trim();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_CONDITION);
			}
			if (validate(exprToExecute) == false) {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_CONDITION);
			}
			final String postfixExpr = convertToPostfix(exprToExecute);
			final StringTokenizer tokenizedExpr = new StringTokenizer(
					postfixExpr, " ");
			if (tokenizedExpr != null) {
				if (tokenizedExpr.countTokens() == 1) {
					return Boolean.parseBoolean(postfixExpr);
				}
				while (tokenizedExpr.hasMoreTokens()) {
					token = tokenizedExpr.nextToken();
					if (isOperand(token)) {
						operandStack.push(Boolean.valueOf(token));
					} else if (isOperator(token)) {
						result = evaluate(operandStack.pop().booleanValue(),
								operandStack.pop().booleanValue(), token);
						operandStack.push(result);
					}
				}
				return operandStack.pop();
			} else {
				throw new RulesEngineException(
						RuleConstants.ERROR_EXECUTING_RULE
								+ RuleConstants.SEPARATOR
								+ RuleConstants.INVALID_CONDITION);
			}
		} catch (final RulesEngineException exp) {
			throw exp;
		} catch (final Exception exp) {
			throw new RulesEngineException(exp);
		}
	}

	/**
	 * Checks whether a string is operator or not.
	 * 
	 * @param str
	 *            The string representing a boolean operator.
	 * 
	 * @return true, if it is an operator
	 */
	private static boolean isOperator(final String str) {
		if (str != null && operatorMap.containsKey(str)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether a string is a boolean operand (TRUE/FALSE) or not.
	 * 
	 * @param str
	 *            The string representing a boolean operand.
	 * 
	 * @return true, if it is an operator
	 */
	private static boolean isOperand(final String str) {
		if (str != null && "true".equalsIgnoreCase(str)
				|| "false".equalsIgnoreCase(str)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Execute a simple boolean expression.
	 * 
	 * @param lh
	 *            The left hand operand
	 * @param rh
	 *            The right hand operand
	 * @param op
	 *            The boolean operator
	 * 
	 * @return true, if successful
	 */
	private static boolean evaluate(final boolean lh, final boolean rh,
			final String op) {
		if (op != null) {
			if (op.equalsIgnoreCase("&&")) {
				return lh && rh;
			} else if (op.equalsIgnoreCase("||")) {
				return lh || rh;
			} else if (op.equalsIgnoreCase("&")) {
				return lh & rh;
			} else {
				return lh | rh;
			}
		} else {
			return false;
		}
	}

	/**
	 * Populate the HashMap, with boolean operators with their corresponding
	 * precedence. Key - Boolean Operator (String) Value - Integer Object
	 * (Representing the precedence)
	 * 
	 * @param operatorMap
	 *            The operator map
	 */
	private static void populateMap(final HashMap<String, Integer> operatorMap) {
		if (operatorMap != null) {
			operatorMap.put("&", 4);
			operatorMap.put("|", 3);
			operatorMap.put("&&", 2);
			operatorMap.put("||", 1);
		}
	}

	/**
	 * Validate whether a passed boolean expression is valid or not.
	 * 
	 * @param expr
	 *            The string containing boolean expression.
	 * 
	 * @return true, if successful
	 */
	private static boolean validate(final String expr) {

		if (expr != null) {
			final StringTokenizer tokenizedExpr = new StringTokenizer(expr, " ");
			final String[] tokens = new String[tokenizedExpr.countTokens()];
			boolean isValid = true;
			int count = 0;
			while (tokenizedExpr.hasMoreTokens()) {
				tokens[count++] = tokenizedExpr.nextToken();
			}
			if (tokens.length % 2 == 0) {
				isValid = false;
			} else if (tokens.length == 1) {
				if (isOperator(tokens[0]) || !isOperand(tokens[0])) {
					isValid = false;
				}
			} else {
				for (int i = 0; i < tokens.length; i += 2) {
					if (isOperand(tokens[i])) {
						continue;
					} else {
						isValid = false;
					}
				}
				for (int i = 1; i < tokens.length; i += 2) {
					if (isOperator(tokens[i])) {
						continue;
					} else {
						isValid = false;
					}
				}
			}
			return isValid;
		} else {
			return false;
		}
	}

	/**
	 * Convert an InFix boolean expression to PostFix.
	 * 
	 * @param expr
	 *            The string containing Infix boolean expression.
	 * 
	 * @return postfixExpression, PostFix string
	 * 
	 * @throws RulesEngineException
	 *             , the exception
	 */
	private static String convertToPostfix(final String expr)
			throws RulesEngineException {

		final Stack<String> operatorStack = new Stack<String>();
		final StringBuffer postfixExpression = new StringBuffer();

		String exprToExecute = "", token = "";

		exprToExecute = expr.trim();
		final StringTokenizer tokenizedExpr = new StringTokenizer(
				exprToExecute, " ");
		if (tokenizedExpr != null) {
			if (tokenizedExpr.countTokens() == 1) {
				return exprToExecute;
			}
			while (tokenizedExpr.hasMoreTokens()) {
				token = tokenizedExpr.nextToken();
				if (isOperator(token)) {
					if (!operatorStack.isEmpty()) {
						while (true) {
							if (!operatorStack.isEmpty()
									&& operatorMap.get(operatorStack.peek()) >= operatorMap
											.get(token)) {
								appendToPostfix(postfixExpression,
										operatorStack.pop());
							} else {
								operatorStack.push(token);
								break;
							}
						}
					} else {
						operatorStack.push(token);
					}
				} else {
					appendToPostfix(postfixExpression, token);
				}
			}

			while (!operatorStack.isEmpty()) {
				appendToPostfix(postfixExpression, operatorStack.pop());
			}
			return postfixExpression.toString();
		} else {
			throw new RulesEngineException(RuleConstants.ERROR_EXECUTING_RULE
					+ RuleConstants.SEPARATOR + RuleConstants.INVALID_CONDITION);
		}
	}

	/**
	 * Append a String token to PostFix expression.
	 * 
	 * @param postFixExpr
	 *            The PostFix expression
	 * @param strToAppend
	 *            The string to append
	 */
	private static void appendToPostfix(final StringBuffer postFixExpr,
			final String strToAppend) {
		if (postFixExpr != null) {
			postFixExpr.append(" ");
			postFixExpr.append(strToAppend);
		}
	}
}
