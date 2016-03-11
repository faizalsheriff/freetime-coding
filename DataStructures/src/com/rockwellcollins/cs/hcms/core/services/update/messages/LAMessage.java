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
import com.rockwellcollins.cs.hcms.core.services.update.ModSpecificFile;
import com.rockwellcollins.cs.hcms.core.services.update.ParentCI;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateService;

/**
 * The Class LAMessage describes the Load Announcement Message.  The 
 * message is represented in the form of a JSON string.
 * 
 * A LA message includes the following information:
 * 
 * - Load Announcement message version
 * - Details about all the CII files present in the LRU
 * - Details about all the Parent 811 files present in the LRU
 * - Details about all the Child 811 present within every Parent 811
 * - Path to Parent 811 and CII files
 * - FTP details to get those files
 * - Force Load flag
 * - Self Load disable flag
 * - NFS flag
 * - Disable Compatibility flag
 * - Reboot Wait flag
 * - Original and Current time of the LA Message
 * - LRU IP address, if left empty the LA message is meant for all LRUs
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateServiceMessage
 * @see com.rockwellcollins.cs.hcms.core.services.update.UpdateService
 * 
 */
 public class LAMessage extends UpdateServiceMessage {
	 
	private UpdateService updateService;
	 
	private HashMap<String, ParentCI> parentCIs;
	
	private boolean forceLoadFlag;
	
	private boolean selfLoadDisableFlag;
	
	private String lruIpAddress;
	
	private String originalTime;
	
	private String currentTime;
	
	private String pathToCII;
	
	private String pathToLoadable;
	
	private String ftpIpAddress;
	
	private int ftpPortNumber;
	
	private String ftpUserName;
	
	private String ftpPassword;
	
	private boolean isNfs;
	
	private boolean isDisableCompatibility;
	
	private boolean isRebootWait;

	private static final String JSON_PARENT_CI = "parent ci";

	private static final String JSON_CII_FILE_CRC = "cii file crc";

	private static final String JSON_CII_FILE_NAME = "cii file name";

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

	private static final String JSON_CHILD_SUPPORTED_MODS = "supported Mods";
	
	private static final String JSON_FORCE_LOAD = "force load";

	private static final String JSON_SELF_LOAD_DISABLE = "self load disable";

	private static final String JSON_LRU_IP = "lru ip";

	private static final String JSON_ORIGINAL_TIME = "original time";

	private static final String JSON_CURRENT_TIME = "current time";

	private static final String JSON_PATH_TO_CII = "path to cii";

	private static final String JSON_PATH_TO_LOADABLE = "path to loadable";

	private static final String JSON_FTP_IP = "ftp ip";

	private static final String JSON_FTP_PORT = "ftp port";

	private static final String JSON_FTP_USERNAME = "ftp username";

	private static final String JSON_FTP_PASSWORD = "ftp password";
	
	private static final String JSON_IS_NFS = "is nfs";
	
	/** The Constant JSON_MOD_SPECIFIC_ARRAY. */
	private static final String JSON_MOD_SPECIFIC_ARRAY = "mod specific info array";

	/** The Constant JSON_MODSPECIFIC_FILE_NAME. */
	private static final String JSON_MODSPECIFIC_FILE_NAME = "mod specific file name";

	/** The Constant JSON_MODSPECIFIC_FILE_MD5. */
	private static final String JSON_MODSPECIFIC_FILE_MD5 = "mod specific file md5";

	/** The Constant JSON_MODSPECIFIC_FILE_MODS. */
	private static final String JSON_MODSPECIFIC_FILE_MODS = "mod specific file mod";
	
	private static final String JSON_IS_DISABLE_COMPATIBILITY = "disable compatibility";
	
	private static final String JSON_IS_REBOOT_WAIT = "is reboot wait";

	/** The Constant VERSION. */
	public static final String VERSION = "4.0";

	/**
	 * Instantiates a new LA message.
	 */
	public LAMessage() {
		super();
		setType(UpdateServiceMessageTypes.LOAD_ANNOUNCEMENT);
		setMessageVersion(VERSION);
	}

	/**
	 * Instantiates a new LA message.
	 * 
	 * @param jsonObject the json object
	 */
	public LAMessage(final JSONObject jsonObject) {
		super(jsonObject);
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
	 * Initializes the newly created LA Message.
	 */
	private void initialize() {
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
						
						JSONArray jmodSpecificArray = null;
						try {
							jmodSpecificArray = jChildCI.getJSONArray(JSON_MOD_SPECIFIC_ARRAY);
						} catch (JSONException jex) {
							UnitManager.Logging.logDebug("Update Service - LAMessage: initialize: Exception while parsing Modspecific info array");
						}
						
						if (jmodSpecificArray != null && jmodSpecificArray.length() > 0) {
							for (int index = 0; index < jmodSpecificArray.length(); index++) {
								JSONObject jmodSpecFile = null;
								try {
									jmodSpecFile  = jmodSpecificArray.getJSONObject(index);
									
								} catch (JSONException jex) {
									UnitManager.Logging.logDebug("Update Service - LAMessage: initialize: Exception while parsing Modspecific info file");
								}
								if (jmodSpecFile != null) {
									ModSpecificFile tempFileInfo = new ModSpecificFile();
									tempFileInfo.setFileName(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_NAME));
									tempFileInfo.setMd5Sum(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_MD5));
									
									JSONArray jmods = null;
									try {
										jmods = jmodSpecFile.getJSONArray(JSON_MODSPECIFIC_FILE_MODS);
									} catch (JSONException e) {
										UnitManager.Logging.logDebug("Update Service - LAMessage: Initialize: Exception while parsing " +
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
				
				parentCIs.put(lruType, parentCI);
			}
		}
		
		forceLoadFlag = getJsonObject().optBoolean(JSON_FORCE_LOAD);
		
		selfLoadDisableFlag = getJsonObject().optBoolean(JSON_SELF_LOAD_DISABLE);
		
		lruIpAddress = getJsonObject().optString(JSON_LRU_IP);
		
		originalTime = getJsonObject().optString(JSON_ORIGINAL_TIME);
		
		currentTime = getJsonObject().optString(JSON_CURRENT_TIME);
		
		pathToCII = getJsonObject().optString(JSON_PATH_TO_CII);
		
		pathToLoadable = getJsonObject().optString(JSON_PATH_TO_LOADABLE);
		
		ftpIpAddress = getJsonObject().optString(JSON_FTP_IP);
		
		ftpPortNumber = getJsonObject().optInt(JSON_FTP_PORT);
		
		ftpUserName = getJsonObject().optString(JSON_FTP_USERNAME);
		
		ftpPassword = getJsonObject().optString(JSON_FTP_PASSWORD);
		
		isNfs = getJsonObject().optBoolean(JSON_IS_NFS);
		
		isDisableCompatibility = getJsonObject().optBoolean(JSON_IS_DISABLE_COMPATIBILITY);
		
		isRebootWait = getJsonObject().optBoolean(JSON_IS_REBOOT_WAIT);
	}
	
	/**
	 * Gets the parent CIs.
	 * 
	 * @return the parent CIs
	 */
	public HashMap<String, ParentCI> getParentCIs() {
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
				
				jParentCIs.put(lruType, jParentCI);
			}
			getJsonObject().put(JSON_PARENT_CI, jParentCIs);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Checks if is force load flag.
	 * 
	 * @return true, if is force load flag
	 */
	public boolean isForceLoadFlag() {
		return forceLoadFlag;
	}

	/**
	 * Sets the force load flag.
	 * 
	 * @param forceLoadFlag the new force load flag
	 */
	public void setForceLoadFlag(final boolean forceLoadFlag) {
		this.forceLoadFlag = forceLoadFlag;
		try {
			getJsonObject().put(JSON_FORCE_LOAD, forceLoadFlag);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Checks if is self load disable flag.
	 * 
	 * @return true, if is self load disable flag
	 */
	public boolean isSelfLoadDisableFlag() {
		return selfLoadDisableFlag;
	}

	/**
	 * Sets the self load disable flag.
	 * 
	 * @param selfLoadDisableFlag the new self load disable flag
	 */
	public void setSelfLoadDisableFlag(final boolean selfLoadDisableFlag) {
		this.selfLoadDisableFlag = selfLoadDisableFlag;
		try {
			getJsonObject().put(JSON_SELF_LOAD_DISABLE, selfLoadDisableFlag);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the lru ip address.
	 * 
	 * @return the lru ip address
	 */
	public String getLruIpAddress() {
		return lruIpAddress;
	}

	/**
	 * Sets the lru ip address.
	 * 
	 * @param lruIpAddress the new lru ip address
	 */
	public void setLruIpAddress(final String lruIpAddress) {
		this.lruIpAddress = lruIpAddress;
		try {
			getJsonObject().put(JSON_LRU_IP, lruIpAddress);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the original time.
	 * 
	 * @return the original time
	 */
	public String getOriginalTime() {
		return originalTime;
	}

	/**
	 * Sets the original time.
	 * 
	 * @param originalTime the new original time
	 */
	public void setOriginalTime(final String originalTime) {
		this.originalTime = originalTime;
		try {
			getJsonObject().put(JSON_ORIGINAL_TIME, originalTime);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the current time.
	 * 
	 * @return the current time
	 */
	public String getCurrentTime() {
		return currentTime;
	}

	/**
	 * Sets the current time.
	 * 
	 * @param currentTime the new current time
	 */
	public void setCurrentTime(final String currentTime) {
		this.currentTime = currentTime;
		try {
			getJsonObject().put(JSON_CURRENT_TIME, currentTime);
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
		return ftpIpAddress;
	}

	/**
	 * Sets the ftp ip address.
	 * 
	 * @param ftpIpAddress the new ftp ip address
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
		return ftpPortNumber;
	}

	/**
	 * Sets the ftp port number.
	 * 
	 * @param ftpPortNumber the new ftp port number
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
	 * Checks if is nfs.
	 * 
	 * @return true, if is nfs
	 */
	public boolean isNfs() {
		return isNfs;
	}

	/**
	 * Sets the nfs.
	 * 
	 * @param isNfs the new nfs
	 */
	public void setNfs(final boolean isNfs) {
		this.isNfs = isNfs;
		try {
			getJsonObject().put(JSON_IS_NFS, isNfs);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Sets the disable compatibility.
	 * 
	 * @param isDisableCompatibility the new disable compatibility
	 */
	public void setDisableCompatibility(final boolean isDisableCompatibility) {
		this.isDisableCompatibility = isDisableCompatibility;
		try {
			getJsonObject().put(JSON_IS_DISABLE_COMPATIBILITY, isDisableCompatibility);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Checks if is disable compatibility.
	 * 
	 * @return true, if is disable compatibility
	 */
	public boolean isDisableCompatibility() {
		return isDisableCompatibility;
	}
	
	/**
	 * Checks if is reboot wait.
	 * 
	 * @return true, if is reboot wait
	 */
	public boolean isRebootWait() {
		return isRebootWait;
	}

	/**
	 * Sets the reboot wait.
	 * 
	 * @param isRebootWait the new reboot wait
	 */
	public void setRebootWait(final boolean isRebootWait) {
		this.isRebootWait = isRebootWait;
		try {
			getJsonObject().put(JSON_IS_REBOOT_WAIT, isRebootWait);
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
			updateService.logInfo("Printing current values of LAMessage");

			updateService.logInfo("LA Message Version:                      " + getMessageVersion());

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
						updateService.logInfo("		childCI.getSupportedMods():               " + childCI.getSupportedModsString());
						
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
			}

			updateService.logInfo("forceLoadFlag:                           " + isForceLoadFlag());
			updateService.logInfo("selfLoadDisableFlag:                     " + isSelfLoadDisableFlag());
			updateService.logInfo("lruIPAdress:                             " + getLruIpAddress());
			updateService.logInfo("originalTime:                            " + getOriginalTime());
			updateService.logInfo("currentTime:                             " + getCurrentTime());
			updateService.logInfo("pathToCII:                               " + getPathToCII());
			updateService.logInfo("pathToLoadable:                          " + getPathToLoadable());
			updateService.logInfo("ftpIPAddress:                            " + getFtpIpAddress());
			updateService.logInfo("ftpPortNumber:                           " + getFtpPortNumber());
			updateService.logInfo("ftpUserName:                             " + getFtpUsername());
			updateService.logInfo("ftpPassword:                             " + getFtpPassword());
			updateService.logInfo("isNfs:                                   " + isNfs());
			updateService.logInfo("isDisableCompatibility:                  " + isDisableCompatibility());
			updateService.logInfo("isRebootWait:                            " + isRebootWait());
			updateService.logInfo("*******************************************");
		}
	}
}
