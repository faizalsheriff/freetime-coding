package com.rockwellcollins.cs.hcms.core;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The LcpConfig is a encapsulating class for the LCP or Loadable Configuration
 * Package.
 * 
 * @author getownse
 * 
 */
public class LcpConfig implements ErrorHandler {

	/**
	 * The Document (or root) Element
	 * 
	 * @author getownse
	 * 
	 */
	public static enum DocumentElement {
		/**
		 * The Document 1st Tier ROOT element for the LCP Document
		 */
		ROOT("lcpconfig");
		private String name;

		DocumentElement(final String name) {
			this.name = name;
		}

		/**
		 * @param doc
		 * @return returns created element
		 */
		@Deprecated
		public Element createElement(final Document doc) {
			return doc.createElement(name);
		}

		/**
		 * Returns the name of the Document Element
		 * 
		 * @return name of the document element
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * The Document 2nd Teir Elements
	 * 
	 * @author getownse
	 * 
	 */
	public static enum RootElement {
		/**
		 * The COMPONENT name for the collection of all components in the LCP
		 */
		COMPONENTS("components"),
		/**
		 * The RULES name for the collection of rules in the LCP
		 */
		RULES("rules"),
		/**
		 * The PSWGUI name for the collection of gui components in the LCP
		 */
		PSWGUI("pswgui");

		private String name;

		RootElement(final String name) {
			this.name = name;
		}

		/**
		 * @return name of the enumeration
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * The Document Teir 3 and roots Tier 1 element
	 * 
	 * @author getownse
	 * 
	 */
	public static enum RulesElement {
		/**
		 * The CONST (or constants) of all rules
		 */
		CONST("const"),
		/**
		 * A single RULE
		 */
		RULE("rule");

		private String name;

		RulesElement(final String name) {
			this.name = name;
		}

		/**
		 * @return Name of the Enumeration
		 */
		public String getName() {
			return name;
		}
	}

	// TODO collection should be final when loading from DOM instead of from SAX
	// transient private RuleCollection ruleCollection;

	private static final long serialVersionUID = 212709213718500913L;

	transient private ComponentContainer components;

	private File lcpConfigDir;

	private File lcpConfigFile;

	private File lcpSchemaFile;

	private boolean enableSchema = false;

	private Document document;

	transient private Schema schema;

	/**
	 * Creates a new LcpConfig
	 */
	public LcpConfig() {
	}

	/**
	 * Dynamically creates a Component based on it's class name.
	 * 
	 * @param className
	 *            The class name of the component to create
	 * @return The created Component
	 * @throws ClassNotFoundException
	 *             Illegal or Unknown class
	 * @throws InstantiationException
	 *             Could not instantiate class (make sure component has an empty
	 *             constructor)
	 * @throws IllegalAccessException
	 *             Framework permission issue
	 * @throws InvalidClassException
	 *             Could not properly cast the class, make sure the class
	 *             derives from Component
	 */
	public Component createComponent(final String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvalidClassException {

		Component component = null;

		final Object obj = UnitManager.Runtime.newInstance(className);

		if (!(obj instanceof Component)) {
			throw new InvalidClassException(className + " is not a Component.");
		}

		component = (Component) obj;

		return component;
	}

	public void error(final SAXParseException exception) throws SAXException {
		UnitManager.Logging.logSevere(exception);
	}

	public void fatalError(final SAXParseException exception)
			throws SAXException {
		UnitManager.Logging.logSevere(exception);
	}

	/**
	 * The LcpConfig ComponentContainer is the running creation of all
	 * Components (between <componnts></componets>) that have been dynamicly
	 * loaded.
	 * 
	 * @return container of all dynamically created components
	 */
	public ComponentContainer getComponents() {
		if (components == null) {
			components = new ComponentContainer();
		}
		return components;
	}

	/**
	 * Gets the LCP's local directory, if one does not exist a the directory
	 * will be determined by the file location. If the directory is unknown and
	 * the lcp file is unknown, null will be returned.
	 * 
	 * @return the LCP's local directory
	 */
	public File getLcpConfigDir() {

		if (lcpConfigDir == null && lcpConfigFile != null
				&& lcpConfigFile.exists()) {

			final File dir = lcpConfigFile.getParentFile();

			if (dir != null) {

				try {

					lcpConfigDir = dir.getCanonicalFile();

				} catch (final Exception e) {

					UnitManager.Logging
							.logSevere(
									"Could not get directory of the LcpConfig file.",
									e);
				}
			}
		}

		return lcpConfigDir;
	}

	/**
	 * Returns the Lcp Configuration file
	 * 
	 * @return Lcp Configuration file
	 */
	public File getLcpConfigFile() {
		return lcpConfigFile;
	}

	/**
	 * Returns the Lcp Schema file
	 * 
	 * @return Lcp Schema file
	 */
	public File getLcpSchemaFile() {
		return lcpSchemaFile;
	}

	/**
	 * Parses a LCP. Creates the Components and applies the Settings to each
	 * Components. Components created this way will be availble in the
	 * Container.
	 * 
	 * @throws ComponentXmlParserException
	 */
	public void parse() throws ComponentXmlParserException {

		if (document == null) {
			try {
				document = getDocument(getLcpConfigFile());

				if (getLcpSchemaFile() != null && isEnableSchema()) {
					if (UnitManager.Logging.isInfo()) {
						UnitManager.Logging.logInfo("Validating Schema: "
								+ getLcpSchemaFile());
					}
					schema = getSchema(getLcpSchemaFile());
					final Validator validator = schema.newValidator();
					validator.validate(new DOMSource(document));
				}
			} catch (final ParserConfigurationException e) {
				throw new ComponentXmlParserException("Internal Exception.", e);
			} catch (final SAXException e) {
				throw new ComponentXmlParserException("Internal Exception.", e);
			} catch (final IOException e) {
				throw new ComponentXmlParserException("Internal Exception.", e);
			}

			if (document == null) {
				throw new ComponentXmlParserException(
						"Error loading XML Document.  The Document is null");
			}
		}

		final Element root = document.getDocumentElement();

		if (root == null) {
			throw new ComponentXmlParserException(getLcpConfigFile()
					+ " does not have a document root node.");
		}

		if (!root.getLocalName().equalsIgnoreCase(
				DocumentElement.ROOT.getName())) {
			throw new ComponentXmlParserException(getLcpConfigFile()
					+ " document root node <" + root.getLocalName()
					+ "> is not understood.  The root node should be <"
					+ DocumentElement.ROOT.getName() + ">");
		}

		parseRoot(root);
	}

	/**
	 * Parses an LCP by a given lcp file
	 * 
	 * @param lcpConfigFile
	 *            lcp to parse
	 * @throws ComponentXmlParserException
	 */
	public void parse(final File lcpConfigFile)
			throws ComponentXmlParserException {
		setLcpConfigFile(lcpConfigFile);
		parse();
	}

	/**
	 * Parses an LCP by a given lcp file and schema validation check
	 * 
	 * @param lcpConfigFile
	 *            lcp to parse
	 * @param lcpSchemaFile
	 *            schema to validate against
	 * @throws ComponentXmlParserException
	 */
	public void parse(final File lcpConfigFile, final File lcpSchemaFile)
			throws ComponentXmlParserException {
		setLcpConfigFile(lcpConfigFile);
		setLcpSchemaFile(lcpSchemaFile);
		parse();
	}

	/**
	 * Set the Lcp Configuration file
	 * 
	 * @param lcpConfigFile
	 *            lcp configuration file
	 */
	public void setLcpConfigFile(final File lcpConfigFile) {
		this.lcpConfigFile = lcpConfigFile;
	}

	/**
	 * Set the Lcp Schema File
	 * 
	 * @param lcpSchemaFile
	 *            Lcp Schema file
	 */
	public void setLcpSchemaFile(final File lcpSchemaFile) {
		setEnableSchema(lcpSchemaFile != null);
		this.lcpSchemaFile = lcpSchemaFile;
	}

	public void warning(final SAXParseException exception) throws SAXException {
		UnitManager.Logging.logWarning("Warning in LcpParser: "
				+ exception.toString());
	}

	private Component createComponent(final Element element)
			throws ComponentXmlParserException {

		final String nodeName = element.getLocalName();
		final String className = element.getAttribute("class");
		final String componentName = element.getAttribute("name");

		if (className == null || className.length() == 0) {
			throw new ComponentXmlParserException(
					"Class attribute not defined on Element " + nodeName);
		}

		if (componentName == null || componentName.length() == 0) {
			throw new ComponentXmlParserException(
					"Name attribute not defined on Element " + nodeName);
		}

		final String nodeInfo = nodeName + "[" + className + "] details ("
				+ element.toString() + ")";

		try {

			final Component component = createComponent(className);

			try {

				component.importXmlElement(element);

				return component;

			} catch (final Exception e) {

				throw new ComponentXmlParserException(
						"Error importing Xml Element in " + nodeInfo, e);
			}

		} catch (final InvalidClassException e) {
			throw new ComponentXmlParserException("Invalid Class in "
					+ nodeInfo, e);
		} catch (final ClassNotFoundException e) {
			throw new ComponentXmlParserException("Class not found in "
					+ nodeInfo, e);
		} catch (final InstantiationException e) {
			throw new ComponentXmlParserException(
					"Could not instantiate class " + nodeInfo, e);
		} catch (final IllegalAccessException e) {
			throw new ComponentXmlParserException("Illegal access to class "
					+ nodeInfo, e);
		} catch (final Exception e) {
			throw new ComponentXmlParserException("Error creating service "
					+ nodeInfo, e);
		} catch (final Throwable t) {
			throw new ComponentXmlParserException("Error creating service "
					+ nodeInfo, t);
		}
	}

	private Document getDocument(final File xmlFile)
			throws ParserConfigurationException, SAXException, IOException,
			ComponentXmlParserException {

		if (xmlFile == null) {
			throw new ComponentXmlParserException("XML file can not be null.");
		}

		if (!xmlFile.exists()) {
			throw new ComponentXmlParserException("XML file '" + xmlFile
					+ "' does not exist.");
		}

		// create xml document factory
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();

		// Does not add xml:base when using XInclude
		// factory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris",
		// false);

		// Does not add xml:lang when doing language fixup
		// factory.setFeature("http://apache.org/xml/features/xinclude/fixup-language",
		// false);

		// must set namespace aware to true to remove namespace from elements
		factory.setNamespaceAware(true);

		// must set xinclude aware to true to allow for xincludes
		factory.setXIncludeAware(true);

		// use this line if validating against a DTD instead of a XSD
		// factory.setValidating(true);

		// create the xml document builder
		final DocumentBuilder builder = factory.newDocumentBuilder();

		// parse the xml file
		return builder.parse(xmlFile);
	}

	private Schema getSchema(final File xsdFile) throws SAXException,
			ComponentXmlParserException {

		if (xsdFile == null) {
			throw new ComponentXmlParserException("XSD file can not be null.");
		}

		if (!xsdFile.exists()) {
			throw new ComponentXmlParserException("XSD file '" + xsdFile
					+ "' does not exist.");
		}

		// create schema factory
		final SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// set LcpParser class to handler any errors
		schemaFactory.setErrorHandler(this);

		// create the schema
		final Schema schema = schemaFactory.newSchema(xsdFile);

		// return the schema
		return schema;
	}

	private void parseComponents(final Node nComponents) {

		if (nComponents != null) {

			final NodeList nodes = nComponents.getChildNodes();

			if (nodes != null) {

				for (int i = 0; i < nodes.getLength(); i++) {
					final Node node = nodes.item(i);

					if (node instanceof Element) {
						final Element element = (Element) node;

						try {
							final Component component = createComponent(element);

							if (component != null) {

								getComponents().add(component);

								if (UnitManager.Logging.isInfo()) {
									UnitManager.Logging
											.logInfo("Successfully created component \'"
													+ component.getName()
													+ "\' from class "
													+ component.getClass()
															.getName());
								}
							}

						} catch (final ComponentXmlParserException e) {
							UnitManager.Logging.logSevere(e);
						}

					} else if ("#comment".equalsIgnoreCase(node.getNodeName())) {

						UnitManager.Logging.logInfo("Xml Comment: "
								+ node.getNodeValue().trim());

					} else if (!"#text".equalsIgnoreCase(node.getNodeName())) {

						UnitManager.Logging.logWarning("Unable to process "
								+ node.getNodeName()
								+ ".  Node is not an element.");
					}
				}
			}
		}
	}

	private void parseRoot(final Node nRoot) throws ComponentXmlParserException {

		if (nRoot == null) {
			throw new ComponentXmlParserException(
					"Root element can not be null.");
		}

		final NodeList nodes = nRoot.getChildNodes();

		if (nodes == null) {
			throw new ComponentXmlParserException(
					"Root element returned null as Children.  This error should never happen.");
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);

			if (node instanceof Element) {
				final Element element = (Element) node;

				// parse all components
				if (element.getLocalName().equalsIgnoreCase(
						RootElement.COMPONENTS.getName())) {

					parseComponents(element);

				}
			}
		}
	}

	protected Document getDocument() {
		return document;
	}

	protected void setDocument(final Document document) {
		this.document = document;
	}

	protected Schema getSchema() {
		return schema;
	}

	protected void setSchema(final Schema schema) {
		this.schema = schema;
	}

	protected void setComponent(final ComponentContainer components) {
		this.components = components;
	}

	protected void setLcpConfigDir(final File lcpConfigDir) {
		this.lcpConfigDir = lcpConfigDir;
	}

	/**
	 * Is the LcpConfig currently validating against the schema?
	 * 
	 * @return true if validating against a schema, otherwise false
	 */
	public boolean isEnableSchema() {
		return enableSchema;
	}

	/**
	 * Enable or Disable Schema validation
	 * 
	 * @param enableSchema
	 *            true to enable schema validation, otherwise false
	 */
	public void setEnableSchema(final boolean enableSchema) {
		this.enableSchema = enableSchema;
	}
}
