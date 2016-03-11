package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Class CIIParser is used to parse CII files. CII files are nothing but 
 * XML files with ".cii" as extension.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see ParentCI
 * @see ChildCI
 * 
 */
public class CIIParser {

	private Document document;

	private ParentCI parentCI;

	/** ParenCIs from HDSInfo.xml */
	private HashMap<String, ParentCI> parentCIs = new HashMap<String, ParentCI>();
	
	/** The Constant HDSINFO_FILE. */
	public static final String HDSINFO_FILE = "HDSInfo.xml";
	
	/**
	 * Parses the CII.
	 * 
	 * @param xmlFile the xml file
	 * 
	 * @return the parent CI
	 * 
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UpdateServiceException the update service exception
	 */
	public ParentCI parseCII(final File xmlFile) throws ParserConfigurationException, SAXException, IOException, UpdateServiceException {
		parentCI = null;

		if (xmlFile == null) {
			throw new UpdateServiceException("Update Service - CIIParser: XML file is null");
		}

		if (!xmlFile.exists()) {
			throw new UpdateServiceException("Update Service - CIIParser: XML file '" + xmlFile + "' does not exist.");
		}

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		final DocumentBuilder builder = factory.newDocumentBuilder();

		document = builder.parse(xmlFile);

		if (document == null) {
			throw new UpdateServiceException("Update Service - CIIParser: Error loading XML Document.  The Document is null");
		}

		final Element root = document.getDocumentElement();

		if (root == null) {
			throw new UpdateServiceException("Update Service - CIIParser: " + xmlFile + " does not have a document root node.");
		}

		if (!root.getNodeName().equalsIgnoreCase("cii")) {
			throw new UpdateServiceException("Update Service - CIIParser: " + xmlFile + " document root node <" + root.getNodeName() + "> is not understood.  The root node should be <cii>");
		}

		final NodeList nodes = root.getChildNodes();

		if (nodes == null) {
			throw new UpdateServiceException("Update Service - CIIParser: Root element returned null as Children. This error should never happen.");
		}

		parentCI = new ParentCI();
		ParentCI tempParent = new ParentCI();

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);

			if (node instanceof Element) {
				final Element element = (Element) node;

				if (element.getNodeName().equalsIgnoreCase("ciifilecrc")) {
					tempParent.setCiiFileCrc(element.getTextContent());
				} else if (element.getNodeName().equalsIgnoreCase("ciifilename")) {
					tempParent.setCiiFileName(element.getTextContent());
				} else if (element.getNodeName().equalsIgnoreCase("parent_811")) {
					parentCI = parseParent811(element);
					
					// Set parentCI with the tempParent data
					parentCI.setCiiFileCrc(tempParent.getCiiFileCrc());
					parentCI.setCiiFileName(tempParent.getCiiFileName());
					
					// Add the parentCI to parentCIs if the file is HDSInfo.xml
					if (xmlFile.getName().equalsIgnoreCase(HDSINFO_FILE)) {
						parentCIs.put(parentCI.getLruType(), parentCI);
					}
				} else if (element.getNodeName().equalsIgnoreCase("companion_811s")) {
					parseCompanion811s(element);
				}
			}
		}
		return parentCI;
	}

	/**
	 * Parses the parent811.
	 * 
	 * @param parent811Node the parent811 node
	 * @return the parentCI
	 * @throws UpdateServiceException the update service exception
	 */
	public ParentCI parseParent811(final Node parent811Node) throws UpdateServiceException {
		if (parent811Node instanceof Element) {
			final Element parent811Element = (Element) parent811Node;
			final NodeList nodes = parent811Element.getChildNodes();
			ParentCI localParentCI = new ParentCI();

			if (nodes == null) {
				localParentCI = null;
				throw new UpdateServiceException("Update Service - CIIParser: No child tags present under the Parent 811.");
			}

			final String isPrimitive = parent811Element.getAttribute("primitive");
			localParentCI.setPrimitive(Boolean.parseBoolean(isPrimitive));

			final String lruType = parent811Element.getAttribute("type");
			localParentCI.setLruType(lruType);

			final String cpn = parent811Element.getAttribute("cpn");
			
			if (cpn.length() == 0) {
				throw new UpdateServiceException("Update Service - CPN is empty");
			}
			localParentCI.setCpn(cpn);
			
			String buildNumber = "";

			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);

				if (node instanceof Element) {
					final Element element = (Element) node;

					if (element.getNodeName().equalsIgnoreCase("filename")) {
						localParentCI.setParent811FileName(element.getTextContent().trim());
					} else if (element.getNodeName().equalsIgnoreCase("md5")) {
						localParentCI.setMd5Value(element.getTextContent().trim());
					} else if (element.getNodeName().equalsIgnoreCase("description")) {
						localParentCI.setDescription(element.getTextContent().trim());
					} else if (element.getNodeName().equalsIgnoreCase("build")) {
						buildNumber = element.getTextContent().trim();
						localParentCI.setBuildNumber(buildNumber);
					} else if (element.getNodeName().equalsIgnoreCase("software_release")) {
						localParentCI.setReleaseNumber(element.getTextContent().trim());
					} else if (element.getNodeName().equalsIgnoreCase("hwpartnumbers")) {
						final List<String> hardwarePartNumbers = parseHardwarePartNumbers(element);
						localParentCI.setHardwarePartNumbers(hardwarePartNumbers);
					}

					if (!localParentCI.isPrimitive()) {
						HashMap<String, ChildCI> childCIs = localParentCI.getChildCIs();
						if (childCIs == null) {
							childCIs = new HashMap<String, ChildCI>();
							localParentCI.setChildCIs(childCIs);
						}
						if (element.getNodeName().equalsIgnoreCase("child_811")) {
							final ChildCI childCI = parseChild811(element);
							if (childCI != null) {
								childCIs.put(childCI.getChildCIType(), childCI);
							}
						}
					}
				}
			}
			if(lruType != null && lruType.equals(UpdateService.LCP_TYPE)) {
				localParentCI.setRegLCPPartNumber(cpn);
				localParentCI.setRegLCPBuildNumber(buildNumber);
			}
			return localParentCI;
		} else {
			return null;
		}
	}

	/**
	 * Parses the child811.
	 * 
	 * @param child811Node the child811 node
	 * 
	 * @return the child CI
	 * @throws UpdateServiceException the update service exception
	 */
	public ChildCI parseChild811(final Node child811Node) throws UpdateServiceException {
		if (child811Node instanceof Element) {
			final Element child811Element = (Element) child811Node;
			final NodeList nodes = child811Element.getChildNodes();

			if (nodes == null) {
				return null;
			}

			final ChildCI childCI = new ChildCI();

			final String childCIType = child811Element.getAttribute("type");
			childCI.setChildCIType(childCIType);

			final String cpn = child811Element.getAttribute("cpn");
			childCI.setCpn(cpn);

			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);

				if (node instanceof Element) {
					final Element element = (Element) node;

					if (element.getNodeName().equalsIgnoreCase("filename")) {
						final String fileName = element.getTextContent();
						childCI.setChild811FileName(fileName.trim());
					} else if (element.getNodeName().equalsIgnoreCase("md5")) {
						final String md5 = element.getTextContent();
						childCI.setMd5Value(md5.trim());
					} else if (element.getNodeName().equalsIgnoreCase("build")) {
						final String build = element.getTextContent();
						childCI.setBuildNumber(build.trim());
					} else if (element.getNodeName().equalsIgnoreCase("supportsmods")) {
						final String mods = element.getTextContent();
						String []modList = mods.split(",");
						for (int loopCount = 0; loopCount < modList.length; loopCount++) {
							try {
								if (modList[loopCount].trim().length() > 0) {
									childCI.addSupportedMods(Integer.parseInt(modList[loopCount]));
								}
							} catch (NumberFormatException e) {
								throw new UpdateServiceException("Exception while parsing the supported Mods");
							}
						}
					} else if (element.getNodeName().equalsIgnoreCase("modspecificfile")) {
						childCI.addModSpecificFileInfo(parseModSpecificInfo(element));
					}
				}
			}
			return childCI;
		} else {
			return null;
		}
	}
	
	/**
	 * Parses the mod specific info.
	 *
	 * @param modSpecInfo the mod specific info
	 * @return the mod specific file
	 */
	private ModSpecificFile parseModSpecificInfo(final Element modSpecInfo) {
		if (modSpecInfo != null) {
			final Element modSpecificInfo = (Element) modSpecInfo;
			final NodeList nodes = modSpecificInfo.getChildNodes();

			if (nodes == null) {
				return null;
			}

			ModSpecificFile modSpecificFileInfo = new ModSpecificFile();

			final String mod = modSpecificInfo.getAttribute("mod");
			modSpecificFileInfo.addMods(mod);

			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);

				if (node instanceof Element) {
					final Element element = (Element) node;

					if (element.getNodeName().equalsIgnoreCase("filename")) {
						 final String fileName = element.getTextContent();
						modSpecificFileInfo.setFileName(fileName.trim());
					} else if (element.getNodeName().equalsIgnoreCase("md5")) {
						final String md5 = element.getTextContent();
						modSpecificFileInfo.setMd5Sum(md5.trim());
					}
				}
			}
			return modSpecificFileInfo;
		} else {
			return null;
		}
	}
	
	/**
	 * Parses the hardware part numbers.
	 * 
	 * @param hwPartNumbersNode the hw part numbers node
	 * 
	 * @return the list<string> of hardware part numbers
	 */
	public List<String> parseHardwarePartNumbers(final Node hwPartNumbersNode) {
		if (hwPartNumbersNode instanceof Element) {
			final Element hwPartNumbersElement = (Element) hwPartNumbersNode;
			final NodeList nodes = hwPartNumbersElement.getChildNodes();

			if (nodes == null) {
				return null;
			}

			final List<String> hardwarePartNumbers = new ArrayList<String>();

			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);

				if (node instanceof Element) {
					final Element element = (Element) node;

					if (element.getNodeName().equalsIgnoreCase("partnumber")) {
						hardwarePartNumbers.add(element.getTextContent().trim());
					}
				}
			}
			return hardwarePartNumbers;
		} else {
			return null;
		}
	}
	
	/**
	 * Parses the companion811s.
	 *
	 * @param companion811sNode the companion811s node
	 */
	private void parseCompanion811s(final Node companion811sNode) {
		if (companion811sNode instanceof Element) {
			final Element companion811sElement = (Element) companion811sNode;
			final NodeList nodes = companion811sElement.getChildNodes();

			if (nodes == null) {
				return;
			} else {
				for (int i = 0; i < nodes.getLength(); i++) {
					final Node node = nodes.item(i);

					if (node instanceof Element) {
						final Element element = (Element) node;
						if (element.getNodeName().equalsIgnoreCase("companion_811")) {
							parseCompanion811(node);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Parses the companion811.
	 *
	 * @param companion811Node the companion811 node
	 */
	private void parseCompanion811(final Node companion811Node) {
		if (companion811Node instanceof Element) {
			final Element companion811Element = (Element) companion811Node;
			final NodeList nodes = companion811Element.getChildNodes();

			if (nodes == null) {
				return;
			}

			final String lruType = companion811Element.getAttribute("type");
			if(lruType != null && lruType.equals(UpdateService.LCP_TYPE)) {
				final String cpn = companion811Element.getAttribute("cpn");
				parentCI.setRegLCPPartNumber(cpn);
				for (int i = 0; i < nodes.getLength(); i++) {
					final Node node = nodes.item(i);

					if (node instanceof Element) {
						final Element element = (Element) node;
						if (element.getNodeName().equalsIgnoreCase("build")) {
							parentCI.setRegLCPBuildNumber(element.getTextContent().trim());
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the hDS parentCIs.
	 *
	 * @return the hDS parentCIs
	 */
	public final HashMap<String, ParentCI> getHDSParentCIs() {
		return parentCIs;
	}
}
