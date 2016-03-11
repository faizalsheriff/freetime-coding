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
 * The Class MAMessage describes the Media Announcement Message.  The 
 * message is represented in the form of a JSON string.
 * 
 * A MA message includes the following information:
 * 
 * - MA Action type
 * - LRU type
 * - LRU instance
 * - Source IP
 * - Destination IP
 * - Details about all the CII files present in the media
 * - Details about all the Parent 811 files present in the LRU
 * - Details about all the Child 811 present within every Parent 811
 * - Error string
 * - Original and Current time of the MA Message
 * - Disable compatibility flag
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateServiceMessage
 * @see com.rockwellcollins.cs.hcms.core.services.update.UpdateService
 * 
 */
public class MAMessage extends UpdateServiceMessage {
	
	private UpdateService updateService;
	
	private MAActionTypes action;
	
	private String lruType;
	
	private String lruInstance;
	
	private String sourceIp;
	
	private String destinationIp;
	
	private HashMap<String, HashMap<String, ParentCI>> ciiFiles;
	
	private String error;
	
	private String originalTime;
	
	private String currentTime;
	
	private boolean isForceLoad;
	
	private boolean isDisableCompatibility;
	
	private static final String JSON_ACTION = "action";
	
	private static final String JSON_LRU_TYPE = "lru type";
	
	private static final String JSON_LRU_INSTANCE = "lru instance";
	
	private static final String JSON_SOURCE_IP = "source ip";
	
	private static final String JSON_DESTINATION_IP = "destination ip";
	
	private static final String JSON_CII_FILES = "cii files";
	
	private static final String JSON_CII_FILE_CRC = "cii file crc";

	private static final String JSON_CII_FILE_NAME = "cii file name";

	private static final String JSON_CII_LRU_TYPE = "lru type";

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
	
	private static final String JSON_ERROR = "error";
	
	private static final String JSON_IS_FORCE_LOAD = "force load";
	
	private static final String JSON_IS_DISABLE_COMPATIBILITY = "disable compatibility";
	
	private static final String JSON_ORIGINAL_TIME = "original time";
	
	private static final String JSON_CURRENT_TIME = "current time";
	
	/** The Constant VERSION. */
	public static final String VERSION = "2.0";

	/** The Constant JSON_MOD_SPECIFIC_ARRAY. */
	private static final String JSON_MOD_SPECIFIC_ARRAY = "mod specific info array";

	/** The Constant JSON_MODSPECIFIC_FILE_NAME. */
	private static final String JSON_MODSPECIFIC_FILE_NAME = "mod specific file name";

	/** The Constant JSON_MODSPECIFIC_FILE_MD5. */
	private static final String JSON_MODSPECIFIC_FILE_MD5 = "mod specific file md5";

	/** The Constant JSON_MODSPECIFIC_FILE_MODS. */
	private static final String JSON_MODSPECIFIC_FILE_MODS = "mod specific file mod";
	
	/**
	 * Instantiates a new MA message.
	 */
	public MAMessage() {
		super();
		setType(UpdateServiceMessageTypes.MEDIA_ANNOUNCEMENT);
		setMessageVersion(VERSION);
	}
	
	/**
	 * Instantiates a new MA message.
	 * 
	 * @param jsonObject the json object
	 */
	public MAMessage(final JSONObject jsonObject) {
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
	 * Initializes the newly created MA Message.
	 */
	private void initialize() {
		action = MAActionTypes.valueOf(getJsonObject().optString(JSON_ACTION));
		
		lruType = getJsonObject().optString(JSON_LRU_TYPE);
		
		lruInstance = getJsonObject().optString(JSON_LRU_INSTANCE);
		
		sourceIp = getJsonObject().optString(JSON_SOURCE_IP);
		
		destinationIp = getJsonObject().optString(JSON_DESTINATION_IP);
		
		ciiFiles = new HashMap<String, HashMap<String,ParentCI>>();
		final JSONObject jCiiFiles = getJsonObject().optJSONObject(JSON_CII_FILES);
		if(jCiiFiles != null) {
			final Iterator<?> jItr = jCiiFiles.keys();
			while(jItr.hasNext()) {
				final String mediaPath = (String) jItr.next();
				final HashMap<String, ParentCI> parentCIs = new HashMap<String, ParentCI>();

				final JSONObject jParentCIs = jCiiFiles.optJSONObject(mediaPath);
				if (jParentCIs != null) {
					final Iterator<?> jParentKeys = jParentCIs.keys();
					while (jParentKeys.hasNext()) {
						final ParentCI parentCI = new ParentCI();
						final String lruType = (String) jParentKeys.next();
						final JSONObject jParentCI = jParentCIs.optJSONObject(lruType);
						parentCI.setCiiFileCrc(jParentCI.optString(JSON_CII_FILE_CRC));
						parentCI.setCiiFileName(jParentCI.optString(JSON_CII_FILE_NAME));
						parentCI.setLruType(jParentCI.optString(JSON_CII_LRU_TYPE));
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
									UnitManager.Logging.logDebug("Update Service - MAMessage: initialize: Exception while parsing Modspecific info array");
								}
								
								if (jmodSpecificArray != null && jmodSpecificArray.length() > 0) {
									for (int index = 0; index < jmodSpecificArray.length(); index++) {
										JSONObject jmodSpecFile = null;
										try {
											jmodSpecFile  = jmodSpecificArray.getJSONObject(index);
											
										} catch (JSONException jex) {
											UnitManager.Logging.logDebug("Update Service - MAMessage: initialize: Exception while parsing Modspecific info file");
										}
										if (jmodSpecFile != null) {
											ModSpecificFile tempFileInfo = new ModSpecificFile();
											tempFileInfo.setFileName(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_NAME));
											tempFileInfo.setMd5Sum(jmodSpecFile.optString(JSON_MODSPECIFIC_FILE_MD5));
											JSONArray jmods = null;
											try {
												jmods = jmodSpecFile.getJSONArray(JSON_MODSPECIFIC_FILE_MODS);
											} catch (JSONException e) {
												UnitManager.Logging.logDebug("Update Service - MAMessage: Initialize: Exception while parsing " +
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
				ciiFiles.put(mediaPath, parentCIs);
			}
		}

		error = getJsonObject().optString(JSON_ERROR);
		
		originalTime = getJsonObject().optString(JSON_ORIGINAL_TIME);
		
		currentTime = getJsonObject().optString(JSON_CURRENT_TIME);
		
		isForceLoad = getJsonObject().optBoolean(JSON_IS_FORCE_LOAD);
		
		isDisableCompatibility = getJsonObject().optBoolean(JSON_IS_DISABLE_COMPATIBILITY);
	}
	
	/**
	 * Gets the action.
	 * 
	 * @return the action
	 */
	public MAActionTypes getAction() {
		return action;
	}
	
	/**
	 * Sets the action.
	 * 
	 * @param action the new action
	 */
	public void setAction(final MAActionTypes action) {
		this.action = action;
		try {
			getJsonObject().put(JSON_ACTION, action.toString());
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the lru type.
	 * 
	 * @return the lru type
	 */
	public String getLruType() {
		return lruType;
	}
	
	/**
	 * Sets the lru type.
	 * 
	 * @param lruType the new lru type
	 */
	public void setLruType(final String lruType) {
		this.lruType = lruType;
		try {
			getJsonObject().put(JSON_LRU_TYPE, lruType);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the lru instance.
	 * 
	 * @return the lru instance
	 */
	public String getLruInstance() {
		return lruInstance;
	}
	
	/**
	 * Sets the lru instance.
	 * 
	 * @param lruInstance the new lru instance
	 */
	public void setLruInstance(final String lruInstance) {
		this.lruInstance = lruInstance;
		try {
			getJsonObject().put(JSON_LRU_INSTANCE, lruInstance);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the source ip.
	 * 
	 * @return the source ip
	 */
	public String getSourceIp() {
		return sourceIp;
	}
	
	/**
	 * Sets the source ip.
	 * 
	 * @param sourceIp the new source ip
	 */
	public void setSourceIp(final String sourceIp) {
		this.sourceIp = sourceIp;
		try {
			getJsonObject().put(JSON_SOURCE_IP, sourceIp);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the destination ip.
	 * 
	 * @return the destination ip
	 */
	public String getDestinationIp() {
		return destinationIp;
	}
	
	/**
	 * Sets the destination ip.
	 * 
	 * @param destinationIp the new destination ip
	 */
	public void setDestinationIp(final String destinationIp) {
		this.destinationIp = destinationIp;
		try {
			getJsonObject().put(JSON_DESTINATION_IP, destinationIp);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the cii files.
	 * 
	 * @return the cii files
	 */
	public HashMap<String, HashMap<String, ParentCI>> getCiiFiles() {
		return ciiFiles;
	}
	
	/**
	 * Sets the cii files.
	 * 
	 * @param ciiFiles the cii files
	 */
	public void setCiiFiles(final HashMap<String, HashMap<String, ParentCI>> ciiFiles) {
		this.ciiFiles = ciiFiles;
		try {
			final JSONObject jCiiFiles = new JSONObject();
			if(ciiFiles != null) {
				for(final Entry<String, HashMap<String, ParentCI>> ciiFilesEntry : ciiFiles.entrySet()) {
					final String mediaPath = ciiFilesEntry.getKey();
					final HashMap<String, ParentCI> parentCIs = ciiFilesEntry.getValue();
					final JSONObject jParentCIs = new JSONObject();

					if(parentCIs != null) {
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
																	
									//Add modspecific info to json object;
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
					}
					jCiiFiles.put(mediaPath, jParentCIs);
				}
			}
			getJsonObject().put(JSON_CII_FILES, jCiiFiles);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * Sets the error.
	 * 
	 * @param error the new error
	 */
	public void setError(final String error) {
		this.error = error;
		try {
			getJsonObject().put(JSON_ERROR, error);
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
	 * Checks if is force load.
	 * 
	 * @return true, if is force load
	 */
	public boolean isForceLoad() {
		return isForceLoad;
	}
	
	/**
	 * Sets the force load.
	 * 
	 * @param isForceLoad the new force load
	 */
	public void setForceLoad(final boolean isForceLoad) {
		this.isForceLoad = isForceLoad;
		try {
			getJsonObject().put(JSON_IS_FORCE_LOAD, isForceLoad);
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
	 * Prints the current values.
	 */
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of MAMessage");
			updateService.logInfo("MA Message Version:                      " + getMessageVersion());
			updateService.logInfo("action:                                  " + getAction().toString());
			updateService.logInfo("lruType:                                 " + getLruType());
			updateService.logInfo("lruInstance:                             " + getLruInstance());
			updateService.logInfo("sourceIp:                                " + getSourceIp());
			updateService.logInfo("destinationIp:                           " + getDestinationIp());
			
			final HashMap<String, HashMap<String, ParentCI>> ciiFiles = getCiiFiles();
			if(ciiFiles != null) {
				for(final Entry<String, HashMap<String, ParentCI>> ciiFilesEntry : ciiFiles.entrySet()) {
					final String mediaPath = ciiFilesEntry.getKey();
					final HashMap<String, ParentCI> parentCIs = ciiFilesEntry.getValue();
					updateService.logInfo("mediaPath:                               " + mediaPath);
					if(parentCIs != null) {
						for (final Entry<String, ParentCI> parentEntry : parentCIs.entrySet()) {
							final ParentCI parentCI = parentEntry.getValue();
							
							updateService.logInfo("	ciiFile:                                 " + parentEntry.getValue().getCiiFileName());
							updateService.logInfo("	parent811File:                           " + parentEntry.getValue().getParent811FileName());

							if (!parentCI.isPrimitive()) {
								updateService.logInfo("	parentCI.getChildCIs().size():              " + parentCI.getChildCIs().size());
	
								for (final Entry<String, ChildCI> childEntry : parentCI
										.getChildCIs().entrySet()) {
									
									final ChildCI childCI = childEntry.getValue();
										
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
					} else {
						updateService.logInfo("	ciiFile:                                 null");
					}
				}
			} else {
				updateService.logInfo("ciiFiles:                                null");
			}
			
			updateService.logInfo("error:                                   " + getError());
			updateService.logInfo("isForceLoad:                             " + isForceLoad());
			updateService.logInfo("isDisableCompatibility:                  " + isDisableCompatibility());
			updateService.logInfo("originalTime:                            " + getOriginalTime());
			updateService.logInfo("currentTime:                             " + getCurrentTime());
			updateService.logInfo("*******************************************");
		}
	}
}
