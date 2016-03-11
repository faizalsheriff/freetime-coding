package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.HashMap;
import java.util.List;

/**
 * The Class ParentCI describes a Parent 811 loadable file. A Parent CI has 
 * its own part number, build number and MD5 checksum of the file. A given 
 * Parent CI can be primitive or non-primitive. Those that are non-primitive
 * can have n number of Child CIs under them.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see ChildCI
 * @see UpdateService
 * 
 */
/**
 * ******************************************************************
 * File format for reference:
 * 
 * <?xml version="1.0" encoding="UTF-8"?><cii>
 * <ciifilename>811-3302-002_1.cii</ciifilename>
 * <parent_811 cpn="811-3138-002" primitive="true" type="ppc">
 * <filename>811-3138-002_1.zip</filename>
 * <md5>7c9bb3769d3808e0d253366d2e2e764e</md5>
 * <build>1</build>
 * <description>This is CII for PPC FW</description>
 * </parent_811>
 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#dsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>R5sVa+HRdfz8vhra4KI3o5NgkQY=</DigestValue></Reference></SignedInfo><SignatureValue>XqiJ5/mjv20smbVSTuQOS23gcgMqB/kEsrJX+zLGp/78h+JQQyHHJw==</SignatureValue><KeyInfo><KeyValue><DSAKeyValue><P>/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9
 * xD7nN1kuFw==</P><Q>li7dzDacuo67Jg7mtqEm2TRuOMU=</Q><G>Z4Rxsnqc9E7pGknFFH2xqaryRPBaQ01khpMdLRQnG541Awtx/XPaF5Bpsy4pNWMOHCBiNU0Nogps
 * QW5QvnlMpA==</G><Y>f8NHJSZbiRouuZ5+an0A6/CXH/Nps09elAWJ9Zhky/SPIHn8RTUWvXJFHSQPkdZBdc1jmXiBw7Z7
 * bTDA7HV34A==</Y></DSAKeyValue></KeyValue></KeyInfo></Signature></cii>
 * ******************************************************************
 */

public class ParentCI {

	/** The cii file crc. */
	private String ciiFileCrc;

	/** The cii file name. */
	private String ciiFileName;

	/** The lru type. */
	private String lruType;

	/** The cpn. */
	private String cpn;

	/** The is primitive. */
	private boolean isPrimitive;

	/** The parent811 file name. */
	private String parent811FileName;

	/** The md5 value. */
	private String md5Value;

	/** The description. */
	private String description;

	/** The child C is. */
	private HashMap<String, ChildCI> childCIs;

	/** The build number. */
	private String buildNumber;

	/** The release number. */
	private String releaseNumber;
	
	/** The hardware part numbers. */
	private List<String> hardwarePartNumbers;
	
	/** The regular LCP part number. */
	private String regLCPPartNumber;

	/** The regular LCP build number. */
	private String regLCPBuildNumber;
	
	/**
	 * Compares.
	 * 
	 * @param compare the compare
	 * 
	 * @return true, if successful
	 */
	public boolean compares(final ParentCI compare) {
		if (compare.getBuildNumber().equals(this.buildNumber) &&
			compare.getCiiFileName().equals(this.ciiFileName) &&
			compare.getCiiFileCrc().equals(this.ciiFileCrc) &&
			compare.getCpn().equals(this.cpn) &&
			compare.getParent811FileName().equals(this.parent811FileName) &&
			compare.getLruType().equals(this.lruType) &&
			compare.getMd5Value().equals(this.md5Value) &&
			compare.getReleaseNumber().equals(this.releaseNumber) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the Parent 811 part number.
	 * 
	 * @return the part number
	 */
	public String getCpn() {
		return cpn;
	}

	/**
	 * Sets the Parent 811 part number.
	 * 
	 * @param cpn the new part number
	 */
	public void setCpn(final String cpn) {
		this.cpn = cpn;
	}

	/**
	 * Gets the cii file name.
	 * 
	 * @return the cii file name
	 */
	public String getCiiFileName() {
		return ciiFileName;
	}

	/**
	 * Sets the cii file name.
	 * 
	 * @param ciiFileName the new cii file name
	 */
	public void setCiiFileName(final String ciiFileName) {
		this.ciiFileName = ciiFileName;
	}

	/**
	 * Gets the parent811 file name.
	 * 
	 * @return the parent811 file name
	 */
	public String getParent811FileName() {
		return parent811FileName;
	}

	/**
	 * Sets the parent811 file name.
	 * 
	 * @param parent811FileName the new parent811 file name
	 */
	public void setParent811FileName(final String parent811FileName) {
		this.parent811FileName = parent811FileName;
	}

	/**
	 * Gets the release number.
	 * 
	 * @return the release number
	 */
	public String getReleaseNumber() {
		return releaseNumber;
	}

	/**
	 * Sets the release number.
	 * 
	 * @param releaseNumber the new release number
	 */
	public void setReleaseNumber(final String releaseNumber) {
		this.releaseNumber = releaseNumber;
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
	 * @param md5Value the new MD5 value
	 */
	public void setMd5Value(final String md5Value) {
		this.md5Value = md5Value;
	}

	/**
	 * Gets the child CIs.
	 * 
	 * @return the child CIs
	 */
	public HashMap<String, ChildCI> getChildCIs() {
		return childCIs;
	}

	/**
	 * Gets the childCI .
	 *
	 * @param childCIType the childCI  type
	 * @return the childCI
	 */
	public final ChildCI getChildCI(final String childCITyp){
		return childCIs.get(childCITyp);
	}
	/**
	 * Sets the child CIs.
	 * 
	 * @param childCIs the child CIs
	 */
	public void setChildCIs(final HashMap<String, ChildCI> childCIs) {
		this.childCIs = childCIs;
	}

	/**
	 * Gets the Parent 811 build number.
	 * 
	 * @return the build number
	 */
	public String getBuildNumber() {
		return buildNumber;
	}

	/**
	 * Sets the Parent 811 build number.
	 * 
	 * @param buildNumber the new build number
	 */
	public void setBuildNumber(final String buildNumber) {
		this.buildNumber = buildNumber;
	}

	/**
	 * Checks if Parent 811 is primitive.
	 * 
	 * @return true, if primitive
	 */
	public boolean isPrimitive() {
		return isPrimitive;
	}

	/**
	 * Sets the Parent 811 as primitive.
	 * 
	 * @param isPrimitive the new primitive
	 */
	public void setPrimitive(final boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
	}

	/**
	 * Gets the cii file crc.
	 * 
	 * @return the cii file crc
	 */
	public String getCiiFileCrc() {
		return ciiFileCrc;
	}

	/**
	 * Sets the cii file crc.
	 * 
	 * @param ciiFileCrc the new cii file crc
	 */
	public void setCiiFileCrc(final String ciiFileCrc) {
		this.ciiFileCrc = ciiFileCrc;
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
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Gets the hardware part numbers.
	 * 
	 * @return the hardware part numbers
	 */
	public List<String> getHardwarePartNumbers() {
		return hardwarePartNumbers;
	}

	/**
	 * Sets the hardware part numbers.
	 * 
	 * @param hardwarePartNumbers the new hardware part numbers
	 */
	public void setHardwarePartNumbers(final List<String> hardwarePartNumbers) {
		this.hardwarePartNumbers = hardwarePartNumbers;
	}

	/**
	 * Gets the reg LCP part number.
	 * 
	 * @return the reg LCP part number
	 */
	public String getRegLCPPartNumber() {
		return regLCPPartNumber;
	}

	/**
	 * Sets the reg LCP part number.
	 * 
	 * @param regLCPPartNumber the new reg LCP part number
	 */
	public void setRegLCPPartNumber(String regLCPPartNumber) {
		this.regLCPPartNumber = regLCPPartNumber;
	}

	/**
	 * Gets the reg LCP build number.
	 * 
	 * @return the reg LCP build number
	 */
	public String getRegLCPBuildNumber() {
		return regLCPBuildNumber;
	}

	/**
	 * Sets the reg LCP build number.
	 * 
	 * @param regLCPBuildNumber the new reg LCP build number
	 */
	public void setRegLCPBuildNumber(String regLCPBuildNumber) {
		this.regLCPBuildNumber = regLCPBuildNumber;
	}
}
