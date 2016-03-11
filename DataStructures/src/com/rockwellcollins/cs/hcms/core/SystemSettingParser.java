package com.rockwellcollins.cs.hcms.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class parses SystemSettings configuration file. This configuration file is 
 * intended to keep settings,settingMap that are referred by multiple Venue LRUs.
 * 
 * This class is included since Venue Release 5
 * @author Jayadevi Murugesan
 */

public final class SystemSettingParser {
	
	/** The system setting map. */
	private static Map<String, Object> systemSettingMap = new HashMap<String, Object>();
	
	/** The Constant SYSTEM_CONF_LOCATION. */
	private static final String SYSTEM_CONF_LOCATION = "/usr/local/HCMS/conf/custom/system/data/globalsettings/SystemSettings.xml";
	private static final String DEF_SYSTEM_CONF_LOCATION = "/usr/local/HCMS/conf/default/system/data/globalsettings/SystemSettings.xml";
		
	/** The file parsed. */
	private static boolean fileParsed = false;
	
	/**
	 * Instantiates a new system setting parser.
	 */
	private SystemSettingParser() {
		
	}
	
	/**
	 * Parses.
	 */
	private static void parse() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {			
			String sysSettingFileLoc = SYSTEM_CONF_LOCATION;
			File sysSettingsFile = new File(SYSTEM_CONF_LOCATION);
			if (!sysSettingsFile.exists())
			{
				sysSettingFileLoc = DEF_SYSTEM_CONF_LOCATION;
			}
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(sysSettingFileLoc);
			Element element = document.getDocumentElement();
			parseElements(element);
			fileParsed = true;			
		} catch (ParserConfigurationException e) {
			UnitManager.Logging.logSevere("Exception while parsing SystemSetting file" + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			UnitManager.Logging.logSevere("Exception while parsing SystemSetting file" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			UnitManager.Logging.logSevere("Exception while refering SystemSetting file" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the elements.
	 * 
	 * @param element the element
	 */
	private static void parseElements(final Element element) {
		if (element == null) {
			UnitManager.Logging.logSevere("Root element has no child element");
			return;
		}
		
		NodeList nodes = element.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			
			if (node instanceof Element) {
				final Element eNode = (Element) node;
				final String nodeName = eNode.getNodeName();
				if (nodeName.endsWith("settingMap")) {
					parseSettingMap(eNode);
				} 
			}
		}
	}
	
	
	/**
	 * Parses the setting map.
	 * 
	 * @param element the element
	 */
	private static void parseSettingMap(final Element element) {
		final String name = element.getAttribute("name");
		if (name == null || name.length() == 0) {
			UnitManager.Logging.logSevere("settingMap element must have a name attribute.");
			return;
		}
		
		try {
			final NodeList nItems = element.getElementsByTagName("hcms:mapItem");
	
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
			systemSettingMap.put(name, map);

		} catch (final Exception e) {
			UnitManager.Logging.logSevere(
					"Excetion Processing Setting Map Elements", e);
		}
	}
	
	
	
	/**
	 * Gets the system setting map.
	 * 
	 * @param name the name
	 * 
	 * @return the system setting map
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, String> getSystemSettingMap(final String name) {
		if (!fileParsed) {
			parse();
		}
		
		HashMap<String, String> result = new HashMap<String, String>();
		final Object value = systemSettingMap.get(name);

		if (value instanceof HashMap) {
			result = (HashMap<String, String>) value;
		} else {
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("In SystemSettings Configuration file " 
						+ ", no setting found for '" + name
						+ "' using empty map");
			}
		}
		return result;
	}
		

}
