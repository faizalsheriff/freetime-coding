package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.List;

/**
 * The Class HardwareInfo stores the LRU's hardware details. The hardware 
 * details include the device type, device id, hardware part number, hardware
 * serial number, hardware mod level, IP address and MAC address.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see Update Service
 * 
 */
public class HardwareInfo {

	private String deviceType;

	private String hardwarePartNumber;

	private String hardwareSerialNumber;

	private String hardwareModLevel;

	private String ipAddress;

	private String macAddress;

	private List<String> nonFieldCINumbers;

	/**
	 * Gets the non field CI numbers.
	 * 
	 * @return the non field CI numbers
	 */
	public List<String> getNonFieldCINumbers() {
		return nonFieldCINumbers;
	}

	/**
	 * Sets the non field CI numbers.
	 * 
	 * @param nonFieldCINumbers the new non field CI numbers
	 */
	public void setNonFieldCINumbers(final List<String> nonFieldCINumbers) {
		this.nonFieldCINumbers = nonFieldCINumbers;
	}

	/**
	 * Gets the device type.
	 * 
	 * @return the device type
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * Sets the device type.
	 * 
	 * @param deviceType the new device type
	 */
	public void setDeviceType(final String deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * Gets the hardware mod level.
	 * 
	 * @return the hardware mod level
	 */
	public String getHardwareModLevel() {
		return hardwareModLevel;
	}

	/**
	 * Sets the hardware mod level.
	 * 
	 * @param hardwareModLevel the new hardware mod level
	 */
	public void setHardwareModLevel(final String hardwareModLevel) {
		this.hardwareModLevel = hardwareModLevel;
	}

	/**
	 * Gets the hardware part number.
	 * 
	 * @return the hardware part number
	 */
	public String getHardwarePartNumber() {
		return hardwarePartNumber;
	}

	/**
	 * Sets the hardware part number.
	 * 
	 * @param hardwarePartNumber the new hardware part number
	 */
	public void setHardwarePartNumber(final String hardwarePartNumber) {
		this.hardwarePartNumber = hardwarePartNumber;
	}

	/**
	 * Gets the hardware serial number.
	 * 
	 * @return the hardware serial number
	 */
	public String getHardwareSerialNumber() {
		return hardwareSerialNumber;
	}

	/**
	 * Sets the hardware serial number.
	 * 
	 * @param hardwareSerialNumber the new hardware serial number
	 */
	public void setHardwareSerialNumber(final String hardwareSerialNumber) {
		this.hardwareSerialNumber = hardwareSerialNumber;
	}

	/**
	 * Gets the ip address.
	 * 
	 * @return the ip address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the ip address.
	 * 
	 * @param ipAddress the new ip address
	 */
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Gets the mac address.
	 * 
	 * @return the mac address
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Sets the mac address.
	 * 
	 * @param macAddress the new mac address
	 */
	public void setMacAddress(final String macAddress) {
		this.macAddress = macAddress;
	}
}
