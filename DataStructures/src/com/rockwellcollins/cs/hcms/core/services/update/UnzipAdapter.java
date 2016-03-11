package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.services.adapter.Adapter;

/**
 * The Class UnzipAdapter is an abstract class that provides methods for
 * extracting zip files like .zip, .tgz and others.
 * 
 * @author Raja Sonnia Pattabiraman
 * 
 */
public abstract class UnzipAdapter extends Adapter {

	private static final long serialVersionUID = 1L;

	private String fileNameToExtract;

	private String filesListToExtract;

	private String filesListToExclude;

	private String extractDirectory;

	/**
	 * Unzip.
	 * 
	 * @return the int
	 */
	public abstract int unzip();

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public abstract int getStatus();

	/**
	 * Gets the file name to extract.
	 * 
	 * @return the file name to extract
	 */
	public String getFileNameToExtract() {
		return fileNameToExtract;
	}

	/**
	 * Sets the file name to extract.
	 * 
	 * @param fileNameToExtract the new file name to extract
	 */
	public void setFileNameToExtract(final String fileNameToExtract) {
		this.fileNameToExtract = fileNameToExtract;
	}

	/**
	 * Gets the files list to extract.
	 * 
	 * @return the files list to extract
	 */
	public String getFilesListToExtract() {
		return filesListToExtract;
	}

	/**
	 * Sets the files list to extract.
	 * 
	 * @param filesListToExtract the new files list to extract
	 */
	public void setFilesListToExtract(final String filesListToExtract) {
		this.filesListToExtract = filesListToExtract;
	}

	/**
	 * Gets the files list to exclude.
	 * 
	 * @return the files list to exclude
	 */
	public String getFilesListToExclude() {
		return filesListToExclude;
	}

	/**
	 * Sets the files list to exclude.
	 * 
	 * @param filesListToExclude the new files list to exclude
	 */
	public void setFilesListToExclude(final String filesListToExclude) {
		this.filesListToExclude = filesListToExclude;
	}

	/**
	 * Gets the extract directory.
	 * 
	 * @return the extract directory
	 */
	public String getExtractDirectory() {
		return extractDirectory;
	}

	/**
	 * Sets the extract directory.
	 * 
	 * @param extractDirectory the new extract directory
	 */
	public void setExtractDirectory(final String extractDirectory) {
		this.extractDirectory = extractDirectory;
	}
}
