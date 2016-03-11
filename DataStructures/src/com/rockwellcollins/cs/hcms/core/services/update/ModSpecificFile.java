package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * The Class ModSpecificFile holds the details of the modspecific details for
 * the childCI. This is read from the <modspecificfile> tag in HDSInfo.xml file
 */
public class ModSpecificFile {

	/**
	 * Gets the md5 sum.
	 * 
	 * @return the md5 sum
	 */
	public final String getMd5Sum() {
		return md5Sum;
	}

	/**
	 * Sets the md5 sum.
	 *
	 * @param md5SumValue the new md5 sum
	 */
	public final void setMd5Sum(final String md5SumValue) {
		this.md5Sum = md5SumValue;
	}

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param newFileName the new file name
	 */
	public final void setFileName(final String newFileName) {
		this.fileName = newFileName;
	}

	/**
	 * Adds the mods.
	 *
	 * @param mod the mod
	 */
	public final void addMods(final int mod) {
		if (mods == null) {
			mods = new ArrayList<Integer>();
		}
		mods.add(mod);
	}

	/**
	 * Adds the supported mods.
	 *
	 * @param modNos the mod nos
	 */
	public final void addMods(final String modNos) {
		String []modList = modNos.split(",");
		for (int loopCount = 0; loopCount < modList.length; loopCount++) {
			try {
				if (modList[loopCount].trim().length() > 0) {
					addMods(Integer.parseInt(modList[loopCount]));
				}
			} catch (NumberFormatException e) {
				UnitManager.Logging
						.logSevere("Update Service: ModSpecificFile: Error while adding mods");
			}
		}
	}

	/**
	 * @return the mods
	 */
	public final ArrayList<Integer> getMods() {
		return mods;
	}

	/** The mods. */
	private ArrayList<Integer> mods;

	/** The md5 sum. */
	private String md5Sum;

	/** The file name. */
	private String fileName;
}