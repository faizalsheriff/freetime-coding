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

// TODO: Auto-generated Javadoc
/**
 * The Class RuleMacros defines the constants used by the Rules Parser/Rules
 * Engine.
 */
public final class RuleMacros {

	/**
	 * Enumeration of Java Types supported by Rules Engine.
	 */
	public static enum JavaTypes {
		
		/** The Constant for Long type. */
		LONG("java.lang.Long", "long"),
		
		/** The Constant for Boolean type. */
		BOOLEAN("java.lang.Boolean", "boolean"),
		
		/** The Constant for String type. */
		STRING("java.lang.String", "String"),
		
		/** The Constant for Double type. */
		DOUBLE("java.lang.Double", "double");

		/** The wrapper type. */
		private String wrapperType;

		/** The primitive type. */
		private String primitiveType;

		/**
		 * Instantiates a new java types.
		 * 
		 * @param wrapperType the wrapper type
		 * @param primitiveType the primitive type
		 */
		private JavaTypes(final String wrapperType, final String primitiveType) {
			this.wrapperType = wrapperType;
			this.primitiveType = primitiveType;
		}

		/**
		 * Gets the wrapper type.
		 * 
		 * @return the wrapper type
		 */
		public String getWrapperType() {
			return wrapperType;
		}

		/**
		 * Gets the primitive type.
		 * 
		 * @return the primitive type
		 */
		public String getPrimitiveType() {
			return primitiveType;
		}
	}

	/**
	 * Enumeration of RuleConstructs (if/set).
	 */
	public static enum RuleConstructType {
		
		/** if/elseif/else. */
		CONTROL_FLOW,
		
		/** Expression ('set' statements). */
		EXPRESSION
	}

	/**
	 * Enumeration of ControlFlows (if/else if/else).
	 */
	public enum ControlFlowType {
		
		/** ControlFlow type if. */
		IF("if"),
		
		/** ControlFlow type else if. */
		ELSEIF("elseif"),
		
		/** ControlFlow type else. */
		ELSE("else");

		/** The control flow type. */
		private String controlFlowType;

		/**
		 * Gets the control flow type.
		 * 
		 * @return the control flow type
		 */
		public String getControlFlowType() {
			return controlFlowType;
		}

		/**
		 * Instantiates a new control flow type.
		 * 
		 * @param controlFlowType the control flow type
		 */
		ControlFlowType(final String controlFlowType) {
			this.controlFlowType = controlFlowType;
		}
	}

	/**
	 * Enumeration of comparison operators.
	 */
	public static enum CompOperator {
		
		/** EqualsTo operator (==). */
		EQ("=="),
		
		/** Greater than operator(>). */
		GT(">"),
		
		/** Greater than or equals operator (>=). */
		GE(">="),
		
		/** Less than operator (<). */
		LT("<"),
		
		/** Less than or equals to operator (<=). */
		LE("<="),
		
		/** Not equals operator (!=). */
		NE("!=");

		/** The operator. */
		private String operator;

		/**
		 * Instantiates a new comp operator.
		 * 
		 * @param operator the operator
		 */
		CompOperator(final String operator) {
			this.operator = operator;
		}

		/**
		 * Gets the operator.
		 * 
		 * @return the operator
		 */
		public String getOperator() {
			return this.operator;
		}

	}

	/**
	 * Enumeration of methods.
	 */
	public static enum Methods {
		
		/** Add Method. */
		ADD("add"),
		
		/** Subtract Method. */
		SUB("sub"),
		
		/** Multiply Method. */
		MUL("mul"),
		
		/** Divide Method. */
		DIV("div"),
		
		/** Toggle Method. */
		TOGGLE("toggle"),
		
		/** Max Method. */
		MAX("max"),
		
		/** Min Method. */
		MIN("min"),
		
		/** Average Method. */
		AVG("avg"),
		
		/** Concatenate Method. */
		CONCAT("concat"),
		
		/** AND Method. */
		AND("and"),
		
		/** OR Method. */
		OR("or"),
		
		/** XOR Method. */
		XOR("xor"),
		
		/** Modulus Method. */
		MOD("mod"),
		
		/** Modulus Method. */
		GETCURRENTTIME("getCurrentTime"),
		
		/** EQUAL TO Method. */
		EQUALTO("equalTo"),
		
		/** Assign the current value of argument state (RC-TC00003870). */
		ASSIGN("assign"),
		
		/** Updates the value of the result state or variable, to the value of the state pointed by the argument pointer. */
		EQUALTO_ARGASPOINTER("equalTo_ArgAsPointer"),

		/** Assigns the value of the argument to the state pointed by the result. */
		EQUALTO_RESULTASPOINTER("equalTo_ResultAsPointer"),
		
		/** Queue Method. */
		QUEUE("queue"),
		
		/** FIFO Method. */
		FIFO("fifo");

		/** The method name. */
		private String methodName;

		/**
		 * Instantiates a new methods.
		 * 
		 * @param methodName the method name
		 */
		Methods(final String methodName) {
			this.methodName = methodName;
		}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return methodName;
		}
	}

	/**
	 * Enumeration of Rules XML Elements.
	 */
	public static enum RuleElements {
		
		/** The cms-rule element. */
		CMS_RULE("cms-rule"),
		
		/** The global constant element. */
		CONST("const"),
		
		/** The Rule element. */
		RULE("rule"),
		
		/** The rule level constant/variable element. */
		VAR("var"),
		
		/** The Expression (set) element. */
		SET("set"),
		
		/** The Expression arguments element. */
		ARGUMENT("argument"),
		
		/** The if control flow element. */
		IF("if"),
		
		/** The elseif control flow element. */
		ELSEIF("elseif"),
		
		/** The else control flow element. */
		ELSE("else"),
		
		/** The multiple condition element. */
		CONDITION("condition");

		/** The element name. */
		private String elementName;

		/**
		 * Instantiates a new rule elements.
		 * 
		 * @param elementName the element name
		 */
		private RuleElements(final String elementName) {
			this.elementName = elementName;
		}

		/**
		 * Gets the element name.
		 * 
		 * @return the element name
		 */
		public String getElementName() {
			return this.elementName;
		}
	}

	/** The left hand operand in a ControlFlow. */
	public static final String ATTR_LH = "lh";

	/** The right hand operand in a ControlFlow. */
	public static final String ATTR_RH = "rh";

	/** The operator in a ControlFlow. */
	public static final String ATTR_OP = "op";

	/** Used for multiple test conditions in a ControlFlow. */
	public static final String ATTR_MORE = "more";

	/** The logical operator for multiple test conditions in a ControlFlow. */
	public static final String ATTR_LOP = "lop";

	/** The name of Global/Rule Constant/Variable. */
	public static final String ATTR_NAME = "name";

	/** The type of Global/Rule Constant/Variable. */
	public static final String ATTR_TYPE = "type";

	/** The value of Global/Rule Constant/Variable. */
	public static final String ATTR_VAL = "value";

	/** The method name in an Expression. */
	public static final String ATTR_METHOD = "method";

	/** The result of an Expression. */
	public static final String ATTR_RESULT = "result";

	/** Namespace property of SAX Parser, default true. */
	public static final String NAMESPACE_PROPERTY = "http://xml.org/sax/features/namespaces";

	/** Namespace-prefix property of SAX Parser, default false. */
	public static final String NAMESPACE_PREFIX_PROPERTY = "http://xml.org/sax/features/namespace-prefixes";

	/**
	 * Instantiates a new rule parser macros.
	 */
	private RuleMacros() {

	}

	/**
	 * The Enum QueueCommands.
	 */
	public enum QueueCommands {

		/** The ADD. */
		ADD("add"),

		/** The REM. */
		REM("rem"),

		/** The CLEAR. */
		CLEAR("clr");

		/** The queue cmd. */
		private String queueCmd;

		/**
		 * Instantiates a new queue commands.
		 * 
		 * @param queueCmd the queue cmd
		 */
		QueueCommands(final String queueCmd) {
			this.queueCmd = queueCmd;
		}

		/**
		 * Gets the queue cmd.
		 * 
		 * @return the queue cmd
		 */
		public String getQueueCmd() {
			return queueCmd;
		}

	}
	
	/**
	 * The Enum FifoCommands.
	 */
	public enum FifoCommands {

		/** The ADD. */
		ADD("add"),

		/** The REM. */
		REM("rem"),

		/** The CLEAR. */
		CLEAR("clr");

		/** The fifo cmd. */
		private String fifoCmd;

		/**
		 * Instantiates a new fifo commands.
		 * 
		 * @param fifoCmd the fifo cmd
		 */
		FifoCommands(final String fifoCmd) {
			this.fifoCmd = fifoCmd;
		}

		/**
		 * Gets the fifo cmd.
		 * 
		 * @return the fifo cmd
		 */
		public String getFifoCmd() {
			return fifoCmd;
		}

	}
}
