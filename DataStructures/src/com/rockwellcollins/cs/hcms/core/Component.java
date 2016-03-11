package com.rockwellcollins.cs.hcms.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rockwellcollins.cs.hcms.core.utils.StringHelper;

/**
 * Components are the fundamental building blocks of the Core Framework. All
 * components are serializeable, have individual settings and can be runtime
 * created.
 * 
 * There are 2 ways a component can be created. 1) Direction instantiation and
 * 2) Deserialization. With 1) Direct instantiation the initialize method is
 * called after the constructor. With 2) deserialized objects are first created
 * in the framework readObject method, and then call initialize. The only method
 * that is guaranteed to be called during instantiation is the initialize method
 * (or onInitialized event)
 * 
 * All Components contain settings. A setting is a name to value association.
 * The value can be a string, a string list or a string to string map. The
 * majority of settings are imported by the importXmlElement method.
 * 
 * All Components can be configured with an XML element. The importXmlElement
 * takes an element arguemnt, and then calls onImportXmlElement and
 * onImportXmlElement complete. These methods are used to extract information
 * from the XML element and save the information as settings or serializeable
 * fields.
 * 
 * Components should have unique names with the setName method.
 * 
 * All Components have a container in which they can hold references to other
 * Components. In most cases, these components are added to the container with
 * the XML element 'contains'.
 * 
 * @author getownse
 * 
 */
public abstract class Component implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_COMPONENT_SETUP_PRIORITY = "component setup priority";

	private static final String ATTRIBUTE_COMPONENT_REF = "component-ref";
	private static final String ELEMENT_CONTAINS = "contains";
	private static final String ELEMENT_SETTING_MAP = "settingMap";
	private static final String ELEMENT_SETTING_LIST = "settingList";
	private static final String ELEMENT_SETTING = "setting";

	private String name = getClass().getName();
	private final Map<String, Object> settings = new HashMap<String, Object>();
	private final List<String> contains = new ArrayList<String>();
	private int setupPriority = 50;

	private transient ComponentContainer components;
	private transient boolean setup;
	private transient boolean verify;
	private transient boolean destroy;
	private transient boolean didInitialize;
	private transient boolean didSetup;
	private transient boolean didVerify;
	private transient ArrayList<ComponentListener> listeners;

	/**
	 * Create a Component. Component constructor does not have parameters and
	 * all classes deriving from Component should not have parameters to the
	 * constructor. Parameterless constructors are less likely to cause problems
	 * when serializing and deserializing.
	 * 
	 * This constructor will call initialize.
	 */
	public Component() {

		try {
			initialize();
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' failed to create.", e);
		}
	}

	public synchronized final void destroy() throws ComponentDestroyException {

		if (!destroy) {
			try {

				final ComponentDestroyArgs args = new ComponentDestroyArgs();
				onDestroy(this, args);

			} catch (final Exception e) {

				throw new ComponentDestroyException("Component '" + getName()
						+ "' exception while destroying.", e);
			}

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Component '" + getName()
						+ "' has been destroyed.");
			}

			destroy = true;
		}
	}

	/**
	 * Get this components component container
	 * 
	 * @return get component container
	 */
	public final synchronized ComponentContainer getComponents() {
		return components;
	}

	public final String getName() {
		return name;
	}

	public synchronized final void importXmlElement(final Element element)
			throws ComponentXmlParserException {

		if (element == null) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' element can not be null.  Exiting.");
			return;
		}

		processName(element);

		final NodeList nodes = element.getChildNodes();

		if (nodes != null) {

			for (int i = 0; i < nodes.getLength(); i++) {

				final Node node = nodes.item(i);

				if (node instanceof Element) {
					final Element eNode = (Element) node;

					final String localName = eNode.getLocalName();

					if (ELEMENT_SETTING.equals(localName)) {
						processStringSetting(eNode);
					} else if (ELEMENT_SETTING_LIST.equals(localName)) {
						processListSetting(eNode);
					} else if (ELEMENT_SETTING_MAP.equals(localName)) {
						processMapSetting(eNode);
					} else if (ELEMENT_CONTAINS.equals(localName)) {
						processContains(eNode);
					}
				}
			}

		}

		final ComponentImportXmlElementArgs args = new ComponentImportXmlElementArgs(
				element);
		onImportXmlElement(this, args);
	}

	public synchronized final void verify() throws ComponentVerifyException {

		if (!verify) {

			ComponentVerifyArgs args = new ComponentVerifyArgs();
			onVerify(this, args);

			if (!didSetup) {
				throw new ComponentVerifyException(
						"Component '"
								+ getName()
								+ "' class '"
								+ getClass()
								+ "' did not setup properly.  Ensure all derrived classes call super.onSetup in their onSetup overload.");
			}

			if (!didVerify) {
				throw new ComponentVerifyException(
						"Component '"
								+ getName()
								+ "' class '"
								+ getClass()
								+ "' did not verify properly.  Ensure all derrived classes call super.onVerify in their onVerify overload.");
			}

			verify = true;

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Component '" + getName()
						+ "' verified.");
			}
		}
	}

	public synchronized final void setup() throws ComponentSetupException {

		if (!setup) {

			for (final String contain : contains) {

				final Component component = UnitManager.ObjectModel
						.getComponents().get(contain);

				if (component == null) {

					UnitManager.Logging.logWarning("Component '" + getName()
							+ "' could not find contains '" + contain + "'");

				} else {

					components.add(component);
				}
			}

			final ComponentSetupArgs args = new ComponentSetupArgs();
			onSetup(this, args);

			setup = true;

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Setup Component '" + toString()
						+ "'");
			}
		}
	}

	private synchronized final void initialize()
			throws ComponentInitializeException {

		didInitialize = false;
		didVerify = false;
		didSetup = false;

		listeners = new ArrayList<ComponentListener>();
		components = new ComponentContainer();

		setup = false;
		destroy = false;

		try {

			final ComponentInitializeArgs args = new ComponentInitializeArgs();
			onInitialize(this, args);

		} catch (final Exception e) {
			throw new ComponentInitializeException("Component '" + getName()
					+ "' failed to initialize.", e);
		}

		if (!didInitialize) {
			throw new ComponentInitializeException(
					"Component '"
							+ getName()
							+ "' class '"
							+ getClass()
							+ "' did not initialize properly.  Ensure all derrived classes call super.onInitialize in their onInitialize overload.");
		}

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Component '" + getName()
					+ "' initialized");
		}
	}

	/**
	 * Name of the component. Names should be unique. The name is used for many
	 * Maps in the core framework.
	 * 
	 * @param name
	 *            Name of the component
	 */
	public final void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public final void setSetting(final String name, final String value) {
		settings.put(name, value);
	}

	private final void processContains(final Element element) {

		if (element == null) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' element can not be null.  Exiting.");
			return;
		}

		final String name = element.getAttribute(ATTRIBUTE_COMPONENT_REF);

		if (name != null && name.length() > 0) {
			synchronized (contains) {
				contains.add(name);
			}
		}
	}

	private final void processListSetting(final Element eSetting)
			throws ComponentXmlParserException {

		if (eSetting == null) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' element can not be null.  Exiting.");
			return;
		}

		final String name = eSetting.getAttribute("name");

		if (name == null || name.length() == 0) {
			throw new ComponentXmlParserException(
					"settingList element must have a name attribute.");
		}

		try {

			final NodeList nItems = eSetting.getElementsByTagNameNS("*",
					"listItem");
			final ArrayList<String> items = new ArrayList<String>();

			for (int i = 0; i < nItems.getLength(); i++) {
				final Node nItem = nItems.item(i);

				if (nItem instanceof Element) {
					final Element eItem = (Element) nItem;

					final String itemValue = eItem.getAttribute("value");

					if (itemValue != null && itemValue.length() > 0) {
						items.add(itemValue);
					}
				}
			}

			settings.put(name, items.toArray(new String[] {}));

		} catch (final Exception e) {
			throw new ComponentXmlParserException(
					"Exception Processing List Settings in Component '"
							+ toString() + "'", e);
		}
	}

	private final void processMapSetting(final Element eSetting)
			throws ComponentXmlParserException {

		if (eSetting == null) {
			throw new ComponentXmlParserException(
					"Element can not be null in Component '" + toString()
							+ "'.");
		}

		final String name = eSetting.getAttribute("name");

		if (name == null || name.length() == 0) {
			throw new ComponentXmlParserException(
					"settingMap element must have a name attribute.");
		}

		try {

			final NodeList nItems = eSetting.getElementsByTagNameNS("*",
					"mapItem");
			final HashMap<String, String> map = new HashMap<String, String>();

			for (int i = 0; i < nItems.getLength(); i++) {
				final Node nItem = nItems.item(i);

				if (nItem instanceof Element) {
					final Element eItem = (Element) nItem;

					final String itemKey = eItem.getAttribute("key");
					final String itemValue = eItem.getAttribute("value");

					if (itemKey != null && itemKey.length() > 0) {

						if (!map.containsKey(itemKey) && itemValue != null
								&& itemValue.length() > 0) {

							map.put(itemKey, itemValue);
						} else {
							map.put(itemKey, "");
						}
					}
				}
			}

			settings.put(name, map);

		} catch (final Exception e) {
			throw new ComponentXmlParserException(
					"Excetion Processing Setting Map Elements", e);
		}
	}

	private final void processName(final Element element) {

		if (element == null) {
			UnitManager.Logging
					.logSevere("Element can not be null in Component '"
							+ toString() + "'");
			return;
		}

		final String name = element.getAttribute("name");

		if (name != null) {
			setName(name);
		}
	}

	private final void processStringSetting(final Element eSetting)
			throws ComponentXmlParserException {

		if (eSetting == null) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' setting can not be null.  Exiting.");
			return;
		}

		final String name = eSetting.getAttribute("name");

		if (name == null || name.length() == 0) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' setting must have a name.  Exiting.");
			return;
		}

		final String value = eSetting.getAttribute("value");

		if (value == null) {
			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' setting value can not be null.  Exiting.");
			return;
		}

		settings.put(name, value);
	}

	private final void readObject(final ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {

		final ComponentDeserializeArgs args = new ComponentDeserializeArgs(
				inputStream);

		/** read default java deserialization information */
		inputStream.defaultReadObject();

		try {

			onDeserialize(this, args);

		} catch (final ComponentDeserializeException e) {

			UnitManager.Logging.logSevere("Component '" + getName()
					+ " could not deserialize.", e);

		}

		try {

			initialize();

		} catch (final ComponentInitializeException e) {

			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' initialize exception while deserializing", e);
		}
	}

	private final void writeObject(final ObjectOutputStream out)
			throws IOException {

		final ComponentSerializeArgs args = new ComponentSerializeArgs(out);

		/** write default java serialization information */
		out.defaultWriteObject();

		try {

			onSerialize(this, args);

		} catch (final ComponentSerializeException e) {

			UnitManager.Logging.logSevere("Component '" + getName()
					+ "' could not serialize", e);
		}
	}

	@Override
	protected final void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	public final boolean isSetting(final String name) {
		return settings.containsKey(name);
	}

	public final boolean getSetting(final String name,
			final boolean defaultReturn) {

		boolean result = defaultReturn;
		Object value = settings.get(name);

		try {

			if (value != null && value instanceof String) {
				result = Boolean.parseBoolean((String) value);
			} else {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}

		} catch (final Exception e) {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', could not parse '" + value
						+ "' to a long for setting '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}
		}

		return result;
	}

	public final float getSetting(final String name, final float defaultReturn) {

		float result = defaultReturn;
		Object value = settings.get(name);

		try {

			if (value != null && value instanceof String) {
				result = Float.parseFloat((String) value);
			} else {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}

		} catch (final Exception e) {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', could not parse '" + value
						+ "' to a float for setting '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}
		}

		return result;
	}

	public final int getSetting(final String name, final int defaultReturn) {

		int result = defaultReturn;
		Object value = settings.get(name);

		try {

			if (value != null && value instanceof String) {
				result = Integer.parseInt((String) value);
			} else {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}

		} catch (final Exception e) {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', could not parse '" + value
						+ "' to an int for setting '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}
		}

		return result;
	}

	public final long getSetting(final String name, final long defaultReturn) {

		long result = defaultReturn;
		Object value = settings.get(name);

		try {

			if (value != null && value instanceof String) {
				result = Long.parseLong((String) value);
			} else {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}

		} catch (final Exception e) {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', could not parse '" + value
						+ "' to a long for setting '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}
		}

		return result;
	}

	public final String getSetting(final String name, final String defaultReturn) {

		String result = defaultReturn;
		Object value = settings.get(name);

		if (value instanceof String) {

			result = (String) value;

		} else {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using default value of '" + defaultReturn + "'");
			}
		}

		return result;
	}

	public final String[] getSettingList(final String name) {
		return getSettingList(name, new String[] {});
	}

	public final String[] getSettingList(final String name,
			final String[] defaultReturn) {

		String[] result = defaultReturn;

		final Object value = settings.get(name);

		if (value instanceof String[]) {

			result = (String[]) value;

		} else {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name + "' using list '"
						+ StringHelper.join(defaultReturn, ",") + "'");
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public final Map<String, String> getSettingMap(final String name) {

		Map<String, String> result = new HashMap<String, String>();

		final Object value = settings.get(name);

		if (value instanceof HashMap) {

			result = (HashMap<String, String>) value;

		} else {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In Component '" + getName()
						+ "', no setting found for '" + name
						+ "' using empty map");
			}
		}

		return result;
	}
	
	public final Map<String,String> getSystemSettingMap(final String name) {
		return SystemSettingParser.getSystemSettingMap(name);
	}

	protected void onDeserialize(final Object source,
			final ComponentDeserializeArgs args)
			throws ComponentDeserializeException, IOException,
			ClassNotFoundException {

	}

	protected void onDestroy(final Object source,
			final ComponentDestroyArgs args) throws ComponentDestroyException {

	}

	protected void onImportXmlElement(final Object source,
			final ComponentImportXmlElementArgs args)
			throws ComponentXmlParserException {

	}

	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		didInitialize = true;
	}

	protected void onVerify(final Object source, final ComponentVerifyArgs args)
			throws ComponentVerifyException {

		didVerify = true;

		try {
			synchronized (listeners) {
				int len = listeners.size();
				for (int i = 0; i < len; i++) {
					listeners.get(i).componentVerify(this, args);
				}
			}
		} catch (final Exception e) {
			throw new ComponentVerifyException("Component '" + getName()
					+ "' exception in onVerify listeners", e);
		}
	}

	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		setupPriority = getSetting(SETTING_COMPONENT_SETUP_PRIORITY,
				setupPriority);

		didSetup = true;

		try {
			synchronized (listeners) {
				int len = listeners.size();
				for (int i = 0; i < len; i++) {
					listeners.get(i).componentSetup(this, args);
				}
			}
		} catch (final Exception e) {
			throw new ComponentSetupException("Component '" + getName()
					+ "' exception in onSetup listeners", e);
		}
	}

	protected void onSerialize(final Object source,
			final ComponentSerializeArgs args)
			throws ComponentSerializeException, IOException {
	}

	public final void setSetupPriority(final int setupPriority) {
		this.setupPriority = setupPriority;
	}

	public final int getSetupPriority() {
		return setupPriority;
	}

	public final boolean register(final ComponentListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public final boolean unregister(final ComponentListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}
}
