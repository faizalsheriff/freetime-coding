package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * The Class ChildCI represents one Child 811. The following attributes of 
 * Child CI is used for conformity check:
 * Part Number
 * Build Number
 * MD5 Checksum
 * If any one of the attributes is found to be out of conformity, 
 * the entire Child CI needs to be loaded.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see ParentCI
 * 
 */
public class ChildCI {

	/** The childCI type. */
	private String childCIType;

	/** The cpn. */
	private String cpn;

	/** The child811 file name. */
	private String child811FileName;

	/** The build number. */
	private String buildNumber;

	/** The md5 value. */
	private String md5Value;

	/** supportsmods data for childCI from the cii file. */
	private ArrayList<Integer> supportedMods;
	
	/** modspecificfile data for a childCI from the cii file. */
	private ArrayList<ModSpecificFile> modSpecificFileInfo;
	
	/** supportedmods buffer. */
	private StringBuffer modsBuffer = new StringBuffer();
	
	/**
	 * Gets the mod specific file info.
	 *
	 * @return the mod specific file info
	 */
	public ArrayList<ModSpecificFile> getModSpecificFileInfo() {
		return modSpecificFileInfo;
	}

	/**
	 * Adds the mod specific file info.
	 *
	 * @param modSpecificFileInfo the mod specific file info
	 */
	public void addModSpecificFileInfo(final ModSpecificFile modSpecificFileInfo) {
		if (this.modSpecificFileInfo == null) {
			this.modSpecificFileInfo = new ArrayList<ModSpecificFile>();
		}
		
		this.modSpecificFileInfo.add(modSpecificFileInfo);
	}
	
	/**
	 * Gets the supported mods.
	 *
	 * @return the supported mods
	 */
	public ArrayList<Integer> getSupportedMods() {
		return supportedMods;
	}

	/**
	 * Adds the supported mods.
	 *
	 * @param mod the mod
	 */
	public void addSupportedMods(final int mod) {
		if (supportedMods == null) {
			supportedMods = new ArrayList<Integer>();
		}
		supportedMods.add(mod);
	}
	
	/**
	 * Adds the supported mods.
	 *
	 * @param mods the mods
	 */
	public void addSupportedMods(final String mods) {
		String []modList = mods.split(",");
		for (int loopCount = 0; loopCount < modList.length; loopCount++) {
			try {
				if (modList[loopCount].trim().length() > 0) {
					addSupportedMods(Integer.parseInt(modList[loopCount]));
				}
			} catch (NumberFormatException e) {
				UnitManager.Logging
						.logSevere("Update Service - ChildCI::addSupportedMods: Error while adding supported mods");
			}
		}
	}

	/**
	 * Gets the child811 file name.
	 * 
	 * @return the child811 file name
	 */
	public String getChild811FileName() {
		return child811FileName;
	}

	/**
	 * Sets the child811 file name.
	 * 
	 * @param child811FileName
	 *            the new child811 file name
	 */
	public void setChild811FileName(final String child811FileName) {
		this.child811FileName = child811FileName;
	}

	/**
	 * Gets the build number.
	 * 
	 * @return the build number
	 */
	public String getBuildNumber() {
		return buildNumber;
	}

	/**
	 * Sets the build number.
	 * 
	 * @param buildNumber
	 *            the new build number
	 */
	public void setBuildNumber(final String buildNumber) {
		this.buildNumber = buildNumber;
	}

	/**
	 * Gets the cpn.
	 * 
	 * @return the cpn
	 */
	public String getCpn() {
		return cpn;
	}

	/**
	 * Sets the cpn.
	 * 
	 * @param cpn
	 *            the new cpn
	 */
	public void setCpn(final String cpn) {
		this.cpn = cpn;
	}

	/**
	 * Gets the MD5 value.
	 * 
	 * @return the MD5 value
	 */
	public String getMd5Value() {
		return md5Value;
	}

	/**
	 * Sets the MD5 value.
	 * 
	 * @param md5Value
	 *            the new MD5 value
	 */
	public void setMd5Value(final String md5Value) {
		this.md5Value = md5Value;
	}

	/**
	 * Gets the child CI type.
	 * 
	 * @return the child CI type
	 */
	public String getChildCIType() {
		return childCIType;
	}

	/**
	 * Sets the child CI type.
	 * 
	 * @param childCIType
	 *            the new child CI type
	 */
	public void setChildCIType(final String childCIType) {
		this.childCIType = childCIType;
	}

	/**
	 * Gets the supported mods string.
	 *
	 * @return the supported mods string
	 */
	public String getSupportedModsString() {
		modsBuffer.delete(0, modsBuffer.length());
		if (supportedMods != null) {
			for (int index = 0; index < supportedMods.size(); index++) {
				modsBuffer.append(supportedMods.get(index).toString() + ",");
			}
			
			modsBuffer.deleteCharAt(modsBuffer.lastIndexOf(","));
		}
		return modsBuffer.toString();
	}
}
