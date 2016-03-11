package com.rockwellcollins.cs.hcms.core.services.update.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.update.ChildCI;
import com.rockwellcollins.cs.hcms.core.services.update.HardwareInfo;
import com.rockwellcollins.cs.hcms.core.services.update.ModSpecificFile;
import com.rockwellcollins.cs.hcms.core.services.update.ParentCI;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateService;

/**
 * The Class CAMessage describes the Configuration Announcment Message. The 
 * message is represented in the form of a JSON string.
 * 
 * A CA message includes the following information:
 * 
 * - Configuration Announcement message version
 * - Load Announcement message version
 * - Status Announcement message version
 * - Device Type
 * - Device ID
 * - Hardware Part Number
 * - Hardware Serial Number
 * - Hardware Mod Level
 * - IP Address
 * - MAC Adress
 * - Details about all the CII files present in the LRU
 * - Details about all the Parent 811 files present in the LRU
 * - Details about all the Child 811 present within every Parent 811
 * - Path to Parent 811 and CII files
 * - FTP details to get those files
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateServiceMessage
 * @see com.rockwellcollins.cs.hcms.core.services.update.UpdateService
 * 
 */
public class CAMessage extends UpdateServiceMessage {
	
	private UpdateService updateService;
	
	private String laMessageVersion;
	
	private String saMessageVersion;
	
	private HardwareInfo hardwareDetails;
	
	private HashMap<String, ParentCI> parentCIs;
	
	private boolean isRegularLCPType;

	private String lcpType;
	
	private String pathToCII;
	
	private String pathToLoadable;
	
	private String ftpIpAddress;
	
	private int ftpPortNumber = -1;
	
	private String ftpUserName;
	
	private String ftpPassword;

	private static final String JSON_LA_MESSAGE_VERSION = "la message version";

	private static final String JSON_SA_MESSAGE_VERSION = "sa message version";

	private static final String JSON_DEVICE_TYPE = "device type";

	private static final String JSON_HW_PART_NUMBER = "hw part number";

	private static final String JSON_HW_SERIAL_NUMBER = "hw serial number";

	private static final String JSON_HW_MOD_LEVEL = "hw mod level";

	private static final String JSON_IP_ADDRESS = "ip address";

	private static final String JSON_MAC_ADDRESS = "mac address";

	private static final String JSON_PARENT_CI = "parent ci";

	private static final String JSON_CII_FILE_CRC = "cii file crc";

	private static final String JSON_CII_FILE_NAME = "cii file name";

	private static final String JSON_CHILD_SUPPORTED_MODS = "supported Mods";

	private static final String JSON_LRU_TYPE = "lru type";

	private static final String JSON_PARENT_CPN = "parent cpn";

	private static final String JSON_PRIMITIVE = "primitive";

	private static final String JSON_PARENT_FILE_NAME = "parent file name";

	private static final String JSON_PARENT_MD5 = "parent md5";

	private static final String JSON_DESCRIPTION = "description";

	private static final String JSON_PARENT_BUILD = "parent build";

	private static final String JSON_SOFTWARE_RELEASE = "software release";

	private static final String JSON_HW_PART_NUMBERS = "hw part numbers";

	private static final String JSON_CHILD_CI = "child ci";

	private static final String JSON_CHILD_CI_TYPE = "child ci type";

	private static final String JSON_CHILD_CPN = "child cpn";

	private static final String JSON_CHILD_FILE_NAME = "child file name";

	private static final String JSON_CHILD_BUILD = "child build";

	private static final String JSON_CHILD_MD5 = "child md5";
	
	private static final String JSON_REGULAR_LCP_PN = "reg lcp pn";
	
	private static final String JSON_REGULAR_LCP_BN = "reg lcp bn";
	
	private static final String JSON_LCP_REGULAR = "lcp reg";
	
	private static final String JSON_LCP_TYPE = "lcp type";
	
	private static final String JSON_PATH_TO_CII = "path to cii";

	private static final String JSON_PATH_TO_LOADABLE = "path to loadable";

	private static final String JSON_FTP_IP = "ftp ip";

	private static final String JSON_FTP_PORT = "ftp port";

	private static final String JSON_FTP_USERNAME = "ftp username";

	private static final String JSON_FTP_PASSWORD = "ftp password";
	
	/** The Constant JSON_MOD_SPECIFIC_ARRAY. */
	private static final String JSON_MOD_SPECIFIC_ARRAY = "mod specific info array";

	/** The Constant JSON_MODSPECIFIC_FILE_NAME. */
	private static final String JSON_MODSPECIFIC_FILE_NAME = "mod specific file name";

	/** The Constant JSON_MODSPECIFIC_FILE_MD5. */
	private static final String JSON_MODSPECIFIC_FILE_MD5 = "mod specific file md5";

	/** The Constant JSON_MODSPECIFIC_FILE_MODS. */
	private static final String JSON_MODSPECIFIC_FILE_MODS = "mod specific file mod";
	
	/** The Constant VERSION. */
	public static final String VERSION = "4.0";

	/**
	 * Instantiates a new CA message.
	 */
	public CAMessage() {
		super();
		setType(UpdateServiceMessageTypes.CONFIGURATION_ANNOUNCEMENT);
		setMessageVersion(VERSION);
	}

	/**
	 * Instantiates a new CA message.
	 * 
	 * @param message the message
	 */
	public CAMessage(final JSONObject message) {
		super(message);
		ftpPortNumber = -1;
		initialize();
	}
	
	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Initializes the newly created CA Message.
	 */
	private void initialize() {
		laMessageVersion = getJsonObject().optString(JSON_LA_MESSAGE_VERSION);
		
		saMessageVersion = getJsonObject().optString(JSON_SA_MESSAGE_VERSION);
		
		hardwareDetails = new HardwareInfo();
		hardwareDetails.setDeviceType(getJsonObject().optString(JSON_DEVICE_TYPE));
		hardwareDetails.setHardwarePartNumber(getJsonObject().optString(JSON_HW_PART_NUMBER));
		hardwareDetails.setHardwareSerialNumber(getJsonObject().optString(JSON_HW_SERIAL_NUMBER));
		hardwareDetails.setHardwareModLevel(getJsonObject().optString(JSON_HW_MOD_LEVEL));
		hardwareDetails.setIpAddress(getJsonObject().optString(JSON_IP_ADDRESS));
		hardwareDetails.setMacAddress(getJsonObject().optString(JSON_MAC_ADDRESS));
	}

	/**
	 * Gets the LA message version.
	 * 
	 * @return the LA message version
	 */
	public String getLAMessageVersion() {
		return laMessageVersion;
	}

	/**
	 * Sets the LA message version.
	 * 
	 * @param version the new LA message version
	 */
	public void setLAMessageVersion(final String laMessageVersion) {
		this.laMessageVersion = laMessageVersion;
		try {
			getJsonObject().put(JSON_LA_MESSAGE_VERSION, laMessageVersion);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the SA message version.
	 * 
	 * @return the SA message version
	 */
	public String getSAMessageVersion() {
		return saMessageVersion;
	}

	/**
	 * Sets the SA message version.
	 * 
	 * @param version the new SA message version
	 */
	public void setSAMessageVersion(final String saMessageVersion) {
		this.saMessageVersion = saMessageVersion;
		try {
			getJsonObject().put(JSON_SA_MESSAGE_VERSION, saMessageVersion);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the hardware details.
	 * 
	 * @return the hardware details
	 */
	public HardwareInfo getHardwareDetails() {
		return hardwareDetails;
	}

	/**
	 * Sets the hardware details.
	 * 
	 * @param hardwareDetails the new hardware details
	 */
	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
		try {
			getJsonObject().put(JSON_DEVICE_TYPE, hardwareDetails.getDeviceType());
			getJsonObject().put(JSON_HW_PART_NUMBER, hardwareDetails.getHardwarePartNumber());
			getJsonObject().put(JSON_HW_SERIAL_NUMBER, hardwareDetails.getHardwareSerialNumber());
			getJsonObject().put(JSON_HW_MOD_LEVEL, hardwareDetails.getHardwareModLevel());
			getJsonObject().put(JSON_IP_ADDRESS, hardwareDetails.getIpAddress());
			getJsonObject().put(JSON_MAC_ADDRESS, hardwareDetails.getMacAddress());
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the parent CIs.
	 * 
	 * @return the parent CIs
	 */
	public HashMap<String, ParentCI> getParentCIs() {
		if (parentCIs == null) {
			parentCIs = new HashMap<String, ParentCI>();
			final JSONObject jParentCIs = getJsonObject().optJSONObject(JSON_PARENT_CI);
			if (jParentCIs != null) {
				final Iterator<?> jParentKeys = jParentCIs.keys();
				while (jParentKeys.hasNext()) {
					final ParentCI parentCI = new ParentCI();
					final String lruType = (String) jParentKeys.next();
					final JSONObject jParentCI = jParentCIs.optJSONObject(lruType);
					parentCI.setCiiFileCrc(jParentCI.optString(JSON_CII_FILE_CRC));
					parentCI.setCiiFileName(jParentCI.optString(JSON_CII_FILE_NAME));
					parentCI.setLruType(jParentCI.optString(JSON_LRU_TYPE));
					parentCI.setCpn(jParentCI.optString(JSON_PARENT_CPN));
					parentCI.setPrimitive(jParentCI.optBoolean(JSON_PRIMITIVE));
					parentCI.setParent811FileName(jParentCI.optString(JSON_PARENT_FILE_NAME));
					parentCI.setMd5Value(jParentCI.optString(JSON_PARENT_MD5));
					parentCI.setDescription(jParentCI.optString(JSON_DESCRIPTION));
					parentCI.setBuildNumber(jParentCI.optString(JSON_PARENT_BUILD));
					parentCI.setReleaseNumber(jParentCI.optString(JSON_SOFTWARE_RELEASE));
					final JSONArray jHardwarePartNumbers = jParentCI.optJSONArray(JSON_HW_PART_NUMBERS);
					if(jHardwarePartNumbers != null && jHardwarePartNumbers.length() > 0) {
						final List<String> hardwarePartNumbers = new ArrayList<String>();
						for(int i = 0;i < jHardwarePartNumbers.length();i++) {
							hardwarePartNumbers.add(jHardwarePartNumbers.optString(i));
						}
						parentCI.setHardwarePartNumbers(hardwarePartNumbers);
					}

					if (!parentCI.isPrimitive()) {
						final HashMap<String, ChildCI> childCIs = new HashMap<String, ChildCI>();
						final JSONObject jChildCIs = jParentCI.optJSONObject(JSON_CHILD_CI);
						final Iterator<?> jChildKeys = jChildCIs.keys();
						while (jChildKeys.hasNext()) {
							final ChildCI childCI = new ChildCI();
							final String childCIType = (String) jChildKeys.next();
							final JSONObject jChildCI = jChildCIs.optJSONObject(childCIType);
							childCI.setChildCIType(jChildCI.optString(JSON_CHILD_CI_TYPE));
							childCI.setCpn(jChildCI.optString(JSON_CHILD_CPN));
							childCI.setChild811FileName(jChildCI.optString(JSON_CHILD_FILE_NAME));
							childCI.setBuildNumber(jChildCI.optString(JSON_CHILD_BUILD));
							childCI.setMd5Value(jChildCI.optString(JSON_CHILD_MD5));
							childCI.addSupportedMods(jChildCI.optString(JSON_CHILD_SUPPORTED_MODS));
							
							//Get the mod specific info
							JSONArray jmodSpecificArray = null;
							try {
								jmodSpecificArray = jChildCI.getJSONArray(JSON_MOD_SPECIFIC_ARRAY);
							} catch (JSONException jex) {
								UnitManager.Logging.logDebug("Update Service - CAMessage: getParentCIs: Exception while parsing Modspecific info array");
							}
							
							if (jmodSpecificArray != null && jmodSpecificArray.length() > 0) {
								for (int index = 0; index < jmodSpecificArray.length(); index++) {
									JSONObject jmodSpecFile = null;
									try {
										jmodSpecFile  = jmodSpecificArray.getJSONObject(index);
										
									} catch (JSONException jex) {
										UnitManager.Logging.logDebug("Update Service - CAMessage: getParentCIs: Exception while parsing Modspecific info file");
									}
									if (jmodSpecFile != null) {
										ModSpecificFile tempFileInfo = new ModSpecificFile();
										tempFileInfo.setFileName(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_NAME));
										tempFileInfo.setMd5Sum(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_MD5));
										JSONArray jmods = null;
										try {
											jmods = jmodSpecFile.getJSONArray(JSON_MODSPECIFIC_FILE_MODS);
										} catch (JSONException e) {
											UnitManager.Logging.logDebug("Update Service - CAMessage: Initialize: Exception while parsing " +
													"mods in modspecific file");
										}
										ArrayList<Integer> mods = new ArrayList<Integer>();
										if(jmods != null && jmods.length() > 0){
											for(int modsIndex = 0; modsIndex < jmods.length(); modsIndex++)
											mods.add(jmods.optInt(modsIndex));
										}
										tempFileInfo.addMods(jmodSpecFile.optInt(JSON_MODSPECIFIC_FILE_MODS));
										childCI.addModSpecificFileInfo(tempFileInfo);
									}
								}
							}
							childCIs.put(childCIType, childCI);
						}
						parentCI.setChildCIs(childCIs);
					}
					
					parentCI.setRegLCPPartNumber(jParentCI.optString(JSON_REGULAR_LCP_PN));
					parentCI.setRegLCPBuildNumber(jParentCI.optString(JSON_REGULAR_LCP_BN));
					
					parentCIs.put(lruType, parentCI);
				}
			}			
		}
		return parentCIs;
	}

	/**
	 * Sets the parent CIs.
	 * 
	 * @param parentCIs the parent CIs
	 */
	public void setParentCIs(final HashMap<String, ParentCI> parentCIs) {
		this.parentCIs = parentCIs;
		try {
			final JSONObject jParentCIs = new JSONObject();

			for (final Entry<String, ParentCI> parentCIEntry : parentCIs.entrySet()) {
				final String lruType = parentCIEntry.getKey();
				final ParentCI parentCI = parentCIEntry.getValue();
				final JSONObject jParentCI = new JSONObject();
				jParentCI.put(JSON_CII_FILE_CRC, parentCI.getCiiFileCrc());
				jParentCI.put(JSON_CII_FILE_NAME, parentCI.getCiiFileName());
				jParentCI.put(JSON_LRU_TYPE, parentCI.getLruType());
				jParentCI.put(JSON_PARENT_CPN, parentCI.getCpn());
				jParentCI.put(JSON_PRIMITIVE, parentCI.isPrimitive());
				jParentCI.put(JSON_PARENT_FILE_NAME, parentCI.getParent811FileName());
				jParentCI.put(JSON_PARENT_MD5, parentCI.getMd5Value());
				jParentCI.put(JSON_DESCRIPTION, parentCI.getDescription());
				jParentCI.put(JSON_PARENT_BUILD, parentCI.getBuildNumber());
				jParentCI.put(JSON_SOFTWARE_RELEASE, parentCI.getReleaseNumber());
				final List<String> hardwarePartNumbers = parentCI.getHardwarePartNumbers();
				if(hardwarePartNumbers != null && hardwarePartNumbers.size() > 0) {
					final JSONArray jHardwarePartNumbers = new JSONArray(hardwarePartNumbers.toArray());
					jParentCI.put(JSON_HW_PART_NUMBERS, jHardwarePartNumbers);
				}

				if (!parentCI.isPrimitive()) {
					final HashMap<String, ChildCI> childCIs = parentCI.getChildCIs();
					final JSONObject jChildCIs = new JSONObject();
					for (final Entry<String, ChildCI> childCIEntry : childCIs.entrySet()) {
						final String childCIType = childCIEntry.getKey();
						final ChildCI childCI = childCIEntry.getValue();
						final JSONObject jChildCI = new JSONObject();
						jChildCI.put(JSON_CHILD_CI_TYPE, childCI.getChildCIType());
						jChildCI.put(JSON_CHILD_CPN, childCI.getCpn());
						jChildCI.put(JSON_CHILD_FILE_NAME, childCI.getChild811FileName());
						jChildCI.put(JSON_CHILD_BUILD, childCI.getBuildNumber());
						jChildCI.put(JSON_CHILD_MD5, childCI.getMd5Value());
						jChildCI.put(JSON_CHILD_SUPPORTED_MODS, childCI.getSupportedModsString());
						
						JSONArray jmodSpecificInfoArray = new JSONArray();
						ArrayList<ModSpecificFile> list = childCI.getModSpecificFileInfo();
						if (list != null && list.size() > 0) {
							for (int index = 0; index < list.size(); index++) {
								JSONObject jtempModSpecificInfo = new JSONObject();
								jtempModSpecificInfo.put(JSON_MODSPECIFIC_FILE_NAME, list.get(index).getFileName());
								jtempModSpecificInfo.put(JSON_MODSPECIFIC_FILE_MD5, list.get(index).getMd5Sum());
								
								JSONArray jmods = new JSONArray();
								ArrayList<Integer> templist = list.get(index).getMods();
								for(int modIndex = 0; modIndex < templist.size(); modIndex++){
									jmods.put(templist.get(modIndex));
								}
								jtempModSpecificInfo.put(JSON_MODSPECIFIC_FILE_MODS, jmods);
								jmodSpecificInfoArray.put(jtempModSpecificInfo);
							}
						}
						jChildCI.put(JSON_MOD_SPECIFIC_ARRAY, jmodSpecificInfoArray);
						jChildCIs.put(childCIType, jChildCI);
					}
					jParentCI.put(JSON_CHILD_CI, jChildCIs);
				}
				
				jParentCI.put(JSON_REGULAR_LCP_PN, parentCI.getRegLCPPartNumber());
				jParentCI.put(JSON_REGULAR_LCP_BN, parentCI.getRegLCPBuildNumber());
				
				jParentCIs.put(lruType, jParentCI);
			}
			getJsonObject().put(JSON_PARENT_CI, jParentCIs);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Checks if is regular LCP type.
	 * 
	 * @return true, if is regular LCP type
	 */
	public boolean isRegularLCPType() {
		isRegularLCPType = getJsonObject().optBoolean(JSON_LCP_REGULAR);
		return isRegularLCPType;
	}

	/**
	 * Sets the regular LCP type.
	 * 
	 * @param isRegularLCPType the new regular LCP type
	 */
	public void setRegularLCPType(final boolean isRegularLCPType) {
		this.isRegularLCPType = isRegularLCPType;
		try {
			getJsonObject().put(JSON_LCP_REGULAR, isRegularLCPType);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the lcp type.
	 * 
	 * @return the lcp type
	 */
	public String getLcpType() {
		if (lcpType == null) {
			lcpType = getJsonObject().optString(JSON_LCP_TYPE);			
		}
		return lcpType;
	}

	/**
	 * Sets the lcp type.
	 * 
	 * @param lcpType the new lcp type
	 */
	public void setLcpType(final String lcpType) {
		this.lcpType = lcpType;
		try {
			getJsonObject().put(JSON_LCP_TYPE, lcpType);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the path to CII.
	 * 
	 * @return the path to CII
	 */
	public String getPathToCII() {
		if (pathToCII == null) {
			pathToCII = getJsonObject().optString(JSON_PATH_TO_CII);			
		}
		return pathToCII;
	}

	/**
	 * Sets the path to CII.
	 * 
	 * @param pathToCII the new path to CII
	 */
	public void setPathToCII(final String pathToCII) {
		this.pathToCII = pathToCII;
		try {
			getJsonObject().put(JSON_PATH_TO_CII, pathToCII);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the path to loadable.
	 * 
	 * @return the path to loadable
	 */
	public String getPathToLoadable() {
		if (pathToLoadable == null) {
			pathToLoadable = getJsonObject().optString(JSON_PATH_TO_LOADABLE);
		}
		return pathToLoadable;
	}

	/**
	 * Sets the path to loadable.
	 * 
	 * @param pathToLoadable the new path to loadable
	 */
	public void setPathToLoadable(final String pathToLoadable) {
		this.pathToLoadable = pathToLoadable;
		try {
			getJsonObject().put(JSON_PATH_TO_LOADABLE, pathToLoadable);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the ftp ip address.
	 * 
	 * @return the ftp ip address
	 */
	public String getFtpIpAddress() {
		if (ftpIpAddress == null) {
			ftpIpAddress = getJsonObject().optString(JSON_FTP_IP);
		}
		return ftpIpAddress;
	}

	/**
	 * Sets the ftp ip address.
	 * 
	 * @param ftpIp the new ftp ip address
	 */
	public void setFtpIpAddress(final String ftpIpAddress) {
		this.ftpIpAddress = ftpIpAddress;
		try {
			getJsonObject().put(JSON_FTP_IP, ftpIpAddress);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the ftp port number.
	 * 
	 * @return the ftp port number
	 */
	public int getFtpPortNumber() {
		if (ftpPortNumber == -1) {
			ftpPortNumber = getJsonObject().optInt(JSON_FTP_PORT);
		}
		return ftpPortNumber;
	}

	/**
	 * Sets the ftp port number.
	 * 
	 * @param ftpPort the new ftp port number
	 */
	public void setFtpPortNumber(final int ftpPortNumber) {
		this.ftpPortNumber = ftpPortNumber;
		try {
			getJsonObject().put(JSON_FTP_PORT, ftpPortNumber);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the ftp username.
	 * 
	 * @return the ftp username
	 */
	public String getFtpUsername() {
		if (ftpUserName == null) {
			ftpUserName = getJsonObject().optString(JSON_FTP_USERNAME);
		}
		return ftpUserName;
	}

	/**
	 * Sets the ftp username.
	 * 
	 * @param ftpUsername the new ftp username
	 */
	public void setFtpUsername(final String ftpUsername) {
		this.ftpUserName = ftpUsername;
		try {
			getJsonObject().put(JSON_FTP_USERNAME, ftpUsername);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the ftp password.
	 * 
	 * @return the ftp password
	 */
	public String getFtpPassword() {
		if (ftpPassword == null) {
			ftpPassword = getJsonObject().optString(JSON_FTP_PASSWORD);			
		}
		return ftpPassword;
	}

	/**
	 * Sets the ftp password.
	 * 
	 * @param ftpPassword the new ftp password
	 */
	public void setFtpPassword(final String ftpPassword) {
		this.ftpPassword = ftpPassword;
		try {
			getJsonObject().put(JSON_FTP_PASSWORD, ftpPassword);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Prints the current values.
	 */
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of CAMessage");

			updateService.logInfo("CA Message Version:                      " + getMessageVersion());
			updateService.logInfo("LA Message Version:                      " + getLAMessageVersion());
			updateService.logInfo("SA Message Version:                      " + getSAMessageVersion());

			final HardwareInfo hardwareDetails = getHardwareDetails();
			updateService.logInfo("DeviceType:                              " + hardwareDetails.getDeviceType());
			updateService.logInfo("HardwarePartNumber:                      " + hardwareDetails.getHardwarePartNumber());
			updateService.logInfo("HardwareSerialNumber:                    " + hardwareDetails.getHardwareSerialNumber());
			updateService.logInfo("HardwareModLevel:                        " + hardwareDetails.getHardwareModLevel());
			updateService.logInfo("IpAddress:                               " + hardwareDetails.getIpAddress());
			updateService.logInfo("MacAddress:                              " + hardwareDetails.getMacAddress());

			final HashMap<String, ParentCI> parentCIs = getParentCIs();
			updateService.logInfo("parentCIs.size():                        " + parentCIs.size());

			for (final Entry<String, ParentCI> parentEntry : parentCIs.entrySet()) {
				final String parentKey = parentEntry.getKey();

				final ParentCI parentCI = parentEntry.getValue();

				updateService.logInfo("	parentKey:                                  " + parentKey);
				updateService.logInfo("	parentCI.getCiiFileCrc():                   " + parentCI.getCiiFileCrc());
				updateService.logInfo("	parentCI.getCiiFileName():                  " + parentCI.getCiiFileName());
				updateService.logInfo("	parentCI.getLruType():                      " + parentCI.getLruType());
				updateService.logInfo("	parentCI.getCpn():                          " + parentCI.getCpn());
				updateService.logInfo("	parentCI.isPrimitive():                     " + parentCI.isPrimitive());
				updateService.logInfo("	parentCI.getParent811FileName():            " + parentCI.getParent811FileName());
				updateService.logInfo("	parentCI.getMd5Value():                     " + parentCI.getMd5Value());
				updateService.logInfo("	parentCI.getBuildNumber():                  " + parentCI.getBuildNumber());
				updateService.logInfo("	parentCI.getReleaseNumber():                " + parentCI.getReleaseNumber());
				final List<String> hardwarePartNumbers = parentCI.getHardwarePartNumbers();
				if(hardwarePartNumbers != null && hardwarePartNumbers.size() > 0) {
					final StringBuilder sb = new StringBuilder();
					for(final String hardwarePartNumber : hardwarePartNumbers) {
						sb.append(hardwarePartNumber);
						sb.append(" ");
					}
					updateService.logInfo("	parentCI.getHardwarePartNumbers():          " + sb.toString());
				} else {
					updateService.logInfo("	parentCI.getHardwarePartNumbers():          null");
				}
				
				if (!parentCI.isPrimitive()) {
					updateService.logInfo("	parentCI.getChildCIs().size():              " + parentCI.getChildCIs().size());

					for (final Entry<String, ChildCI> childEntry : parentCI
							.getChildCIs().entrySet()) {
						final String childKey = childEntry.getKey();

						final ChildCI childCI = childEntry.getValue();

						updateService.logInfo("		childKey:                               " + childKey);
						updateService.logInfo("		childCI.getChildCIType():               " + childCI.getChildCIType());
						updateService.logInfo("		childCI.getCpn():                       " + childCI.getCpn());
						updateService.logInfo("		childCI.getFileName():                  " + childCI.getChild811FileName());
						updateService.logInfo("		childCI.getMd5Value():                  " + childCI.getMd5Value());
						updateService.logInfo("		childCI.getBuildNumber():               " + childCI.getBuildNumber());
						updateService.logInfo("		childCI.getSupportedMods():             " + childCI.getSupportedMods());
						
						ArrayList<ModSpecificFile> modSpecificInfoList = childCI.getModSpecificFileInfo();
						if (modSpecificInfoList != null) {
							updateService.logInfo("		childCI.getModSpecificFileInfo()");
							for (ModSpecificFile modSpecificItem : modSpecificInfoList) {
								updateService.logInfo("		childCI.ModSpecificFile	Name:       	" + modSpecificItem.getFileName());
								updateService.logInfo("		childCI.ModSpecificFile	ModNo:          " + modSpecificItem.getMods());
								updateService.logInfo("		childCI.ModSpecificFile	Md5Value:       " + modSpecificItem.getMd5Sum());
							}
						} else {
							updateService.logInfo("	childCI.getModSpecificFileInfo():          null");
						}
					}
				}
				
				updateService.logInfo("	parentCI.getRegLCPPartNumber():             " + parentCI.getRegLCPPartNumber());
				updateService.logInfo("	parentCI.getRegLCPBuildNumber():            " + parentCI.getRegLCPBuildNumber());
			}

			updateService.logInfo("IsRegularLCPType:                        " + isRegularLCPType());
			updateService.logInfo("LCPType:                                 " + getLcpType());
			updateService.logInfo("PathToCII:                               " + getPathToCII());
			updateService.logInfo("PathToLoadable:                          " + getPathToLoadable());
			updateService.logInfo("FtpIpAddress:                            " + getFtpIpAddress());
			updateService.logInfo("FtpPortNumber:                           " + getFtpPortNumber());
			updateService.logInfo("FtpUsername:                             " + getFtpUsername());
			updateService.logInfo("FtpPassword:                             " + getFtpPassword());
			updateService.logInfo("*******************************************");
		}
	}
}
