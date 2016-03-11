package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * The Class ConformityChecker determines what needs to be installed. It 
 * does a conformity check by comparing the new CII file with the existing old 
 * CII file for every Child CI available. It also has an option of force load.
 * If the force load is true, it will not do any conformity check and returns
 * everything needs to be installed.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see InstallListInfo
 * @see ParentCI
 * @see ChildCI
 * 
 */
public class ConformityChecker {
	
	private UpdateService updateService;

	private HardwareInfo hardwareDetails;
	
	private HashMap<String, ParentCI> parentCIs;
	
	private List<String> primaryPackagesList;
	
	private List<String> secondaryLruList;
	
	private List<String> backupLruList;
	
	private InstallListInfo installList;

	private HashMap<String, ParentCI> parentCIsToInstall;

	private HashMap<String, List<ChildCI>> childCIsToInstall;

	private HashMap<String, List<ChildCI>> childCIsNotToInstall;
	
	/** Stores all the ParentCIs from HDSInfo.xml file */
	private HashMap<String, ParentCI> hdsInfo;
	
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}
	
	public void setBackupLruList(final List<String> backupLruList) {
		this.backupLruList = backupLruList;
	}

	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
	}

	public void setParentCIs(final HashMap<String, ParentCI> parentCIs) {
		this.parentCIs = parentCIs;
	}
	
	public void setPrimaryPackagesList(final List<String> primaryPackagesList) {
		this.primaryPackagesList = primaryPackagesList;
	}

	public void setSecondaryLruList(final List<String> secondaryLruList) {
		this.secondaryLruList = secondaryLruList;
	}
	
	/**
	 * Gets the install list.
	 * 
	 * @param newParentCIs the new parent CIs
	 * @param forceLoadFlag the force load flag
	 * 
	 * @return the install list
	 */
	public InstallListInfo getInstallList(final HashMap<String, ParentCI> newParentCIs, boolean forceLoadFlag) {
		installList = new InstallListInfo();
		
		installList.setUpdateService(updateService);

		parentCIsToInstall = new HashMap<String, ParentCI>();

		childCIsToInstall = new HashMap<String, List<ChildCI>>();

		childCIsNotToInstall = new HashMap<String, List<ChildCI>>();

		ParentCI newParentCI = null;

		// Check whether LCP needs to be installed
		newParentCI = newParentCIs.get(updateService.getLCPType());

		if (newParentCI != null) {
			if (!forceLoadFlag) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Checking LCP conformity...");
				}

				boolean isConform = checkConformityPrimitive(newParentCI);

				if (!isConform) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Existing LCP DO NOT CONFORM with the new LCP");
					}
					parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Existing LCP conforms with the new LCP");
					}
				}
			} else {
				parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
			}
		}

		// Check whether current LRU software needs to be installed
		newParentCI = newParentCIs.get(hardwareDetails.getDeviceType());

		if (newParentCI != null) {
			if (!newParentCI.isPrimitive()) {
				if (!forceLoadFlag) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Checking " + hardwareDetails.getDeviceType() + " conformity...");
					}

					final List<ChildCI> toInstall = new ArrayList<ChildCI>();
					final List<ChildCI> notToInstall = new ArrayList<ChildCI>();

					boolean isConform = checkConformityNonPrimitive(newParentCI, toInstall, notToInstall);

					if (!isConform) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + hardwareDetails.getDeviceType() + " software DO NOT CONFORM with the new " + hardwareDetails.getDeviceType() + " software");
						}
						parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
						childCIsToInstall.put(newParentCI.getLruType(), toInstall);
						childCIsNotToInstall.put(newParentCI.getLruType(), notToInstall);
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + hardwareDetails.getDeviceType() + " software CONFORMS with the new " + hardwareDetails.getDeviceType() + " software");
						}
					}
				} else {
					final List<ChildCI> toInstall = new ArrayList<ChildCI>();

					boolean isConform = getAllChildCIs(newParentCI, toInstall);
					if (!isConform) {
						parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
						childCIsToInstall.put(newParentCI.getLruType(), toInstall);
					} else {
						UnitManager.Logging.logSevere("Update Service: There are no new ChildCIs available in the new ParentCI");
						UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: Force Load failed for " + hardwareDetails.getDeviceType()));
						return null;
					}
				}
			} else {
				UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: The new software for " + hardwareDetails.getDeviceType() + " should be non-primitive"));
				return null;
			}
		}

		// Check whether any of the Primary Packages needs to be installed
		for (final String primaryPackageType : primaryPackagesList) {
			newParentCI = newParentCIs.get(primaryPackageType);
			if(newParentCI != null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI for " + primaryPackageType + " is available in new Parent CIs.");
				}
				boolean isCheckSuccess = checkConformity(primaryPackageType, newParentCI, forceLoadFlag);
				if(!isCheckSuccess) {
					return null;
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI for " + primaryPackageType + " is NOT available in new Parent CIs.");
				}
			}
		}

		// Check whether any of the Secondary LRU needs to be installed
		for (final String secondaryLruType : secondaryLruList) {
			newParentCI = newParentCIs.get(secondaryLruType);
			if(newParentCI != null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI for " + secondaryLruType + " is available in new Parent CIs.");
				}
				boolean isCheckSuccess = checkConformity(secondaryLruType, newParentCI, forceLoadFlag);
				if(!isCheckSuccess) {
					return null;
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI for " + secondaryLruType + " is NOT available in new Parent CIs.");
				}
			}
		}

		// Check whether any of the LRUs needs to be stored as backup
		for (final String backupLruType : backupLruList) {
			String backupLCPType = updateService.getLruToLCPTypes().get(backupLruType);
			if(backupLCPType == null) {
				backupLCPType = UpdateService.LCP_TYPE;
			}
			newParentCI = newParentCIs.get(backupLruType);
			final ParentCI newLCPParentCI = newParentCIs.get(backupLCPType);
			if(newParentCI != null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI for " + backupLruType + " is available in new Parent CIs.");
					updateService.logInfo("Update Service: Current LRU uses LCP type: " + updateService.getLCPType());
					updateService.logInfo("Update Service: Backup LRU uses LCP type: " + backupLCPType);
				}
				if(backupLCPType.equals(updateService.getLCPType()) || backupLCPType.equals(UpdateService.NO_LCP)) {
					boolean isCheckSuccess = checkConformity(backupLruType, newParentCI, forceLoadFlag);
					if(!isCheckSuccess) {
						return null;
					}
				} else {
					final ParentCI oldLCPParentCI = parentCIs.get(backupLCPType);
					if(newLCPParentCI != null || oldLCPParentCI != null) {
						boolean isCheckSuccess = checkConformity(backupLruType, newParentCI, forceLoadFlag);
						if(!isCheckSuccess) {
							return null;
						}
						
						if(newLCPParentCI != null) {
							isCheckSuccess = checkConformity(backupLCPType, newLCPParentCI, forceLoadFlag);
							if(!isCheckSuccess) {
								return null;
							}
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: LCP type: " + backupLCPType + " is not available in new Parent CIs.");
								updateService.logInfo("Update Service: But still backing up: " + backupLruType + " as old LCP is available");
							}
						}
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Cannot backup: " + backupLruType);
							updateService.logInfo("Update Service: LCP type: " + backupLCPType + " is not available in new Parent CIs.");
						}
					}
				}
			} else {
				if(newLCPParentCI != null) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: ParentCI for " + backupLruType + " is NOT available in new Parent CIs.");
						updateService.logInfo("Update Service: Checking whether the corresponding LCP type needs to be loaded or not: " + backupLCPType);
					}
					if(parentCIs.containsKey(backupLruType)) {
						updateService.logInfo("Update Service: Already have a backup of: " + backupLruType);
						if(!backupLCPType.equals(updateService.getLCPType()) && !backupLCPType.equals(UpdateService.NO_LCP)) {
							final boolean isCheckSuccess = checkConformity(backupLCPType, newLCPParentCI, forceLoadFlag);
							if(!isCheckSuccess) {
								return null;
							}
						}
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Nothing to load for: " + backupLruType);
					}
				}
			}
		}

		// CR-123 START
		// Get the HDS info and determine whether the child CI should be loaded or not
		hdsInfo = updateService.getUpdateHandler().getHDSInfo();
		if (hdsInfo != null) {
			ParentCI newLRUParentCI = parentCIsToInstall.get(hardwareDetails.getDeviceType());
			ParentCI existingSWParentCI = hdsInfo.get(hardwareDetails.getDeviceType());
			if (newLRUParentCI != null && existingSWParentCI != null) {
				if (updateService.isInfo()) {
					updateService
							.logInfo("Update Service: HDSInfo available for "
									+ hardwareDetails.getDeviceType());
				}
				for (final Entry<String, ChildCI> existingCIEntry : existingSWParentCI.getChildCIs().entrySet()) {
					final ChildCI existingChildCI = existingCIEntry.getValue();
					String []lruModList = hardwareDetails.getHardwareModLevel().split(",");
					
					filterSupportedMods(existingChildCI, lruModList);
					
					ChildCI newChildCI = newLRUParentCI.getChildCI(existingChildCI.getChildCIType());
					boolean newCIIsSubset = false;
					
					if (newChildCI != null && existingChildCI != null) {
						if (updateService.isInfo()) {
							updateService
									.logInfo("Update Service: Mods available for "
											+ existingChildCI.getChildCIType()
											+ " in HDSInfo file.");
						}
						if (existingChildCI.getSupportedMods() != null && existingChildCI.getSupportedMods().size() > 0) {
							if (newChildCI.getSupportedMods() != null) {
								if (newChildCI.getSupportedMods().containsAll(existingChildCI.getSupportedMods())) {
									newCIIsSubset = true;
									if (updateService.isInfo()) {
										updateService
												.logInfo("Update Service: New CII is subset " + true);
									}
								} else {
									UnitManager.Logging.logSevere("Update Service: New CII is subset " + false);
								}
							} else {
								if (updateService.isInfo()) {
									updateService
											.logInfo("Update Service: Empty supported modlist for new child CII- "
													+ newChildCI.getChildCIType());
								}
							}
						} else {
							newCIIsSubset = true; // Empty set is subset of all sets
							if (updateService.isInfo()) {
								updateService
										.logInfo("Update Service: Empty supported modlist for existing child CII- "
												+ newChildCI.getChildCIType());
							}
						}
					} else {
						if (updateService.isInfo()) {
							updateService
									.logInfo("Update Service: Child CI is null - "
											+ existingChildCI.getChildCIType());
						}
					}
					
					// Get the Mod specific file details for the hardware dependent childCI
					ArrayList<ModSpecificFile> modSpecificInfoList = newChildCI.getModSpecificFileInfo();
					if (modSpecificInfoList != null) {
						for (ModSpecificFile modSpecificItem : modSpecificInfoList) {
							if (modSpecificItem.getMods().containsAll(existingChildCI.getSupportedMods())) {
								newCIIsSubset = true;
								if (updateService.isInfo()) {
									updateService
											.logInfo("Update Service: <modspecific> New CII is subset " + true);
								}
								
								List<ChildCI> childCIList = childCIsToInstall.get(hardwareDetails.getDeviceType());

								for (ChildCI childCI : childCIList) {
									if (childCI.getChildCIType().equalsIgnoreCase(newChildCI.getChildCIType())) {
										ChildCI tempChildCI = childCI;
										tempChildCI.setChild811FileName(modSpecificItem.getFileName());
										tempChildCI.setMd5Value(modSpecificItem.getMd5Sum());
										if (updateService.isInfo()) {
											updateService
													.logInfo("Update Service: <modspecific> New filename for child "
															+ tempChildCI.getChildCIType()
															+ " is "
															+ tempChildCI.getChild811FileName());
										}
									}
								}
							} else {
								UnitManager.Logging.logSevere("Update Service: <modspecific> New CII is subset " + false);
							}
						}
					} else {
						if (updateService.isInfo()) {
							updateService
									.logInfo("Update Service: Modspecific info doesn't exist for childCI- "
											+ newChildCI.getChildCIType());
						}
					}
					
					// Update the install list with the valid childCIs to install
					if (!newCIIsSubset) {
						List<ChildCI> tempList = childCIsToInstall.get(hardwareDetails.getDeviceType());
						if (tempList.size() > 0) {
							final Iterator<ChildCI> tempChildItr = tempList.iterator();
							while (tempChildItr.hasNext()) {
								final ChildCI tempChildCI = tempChildItr.next();
								if (tempChildCI != null && tempChildCI.getChildCIType().equalsIgnoreCase(newChildCI.getChildCIType())) {
									if (updateService.isInfo()) {
										updateService
												.logInfo("Update Service: Removing childCI- "
														+ newChildCI.getChildCIType()
														+ " from InstallList");
									}
									tempChildItr.remove();
								}
							}
						}
						
						childCIsToInstall.put(hardwareDetails.getDeviceType(), tempList);
						if (childCIsNotToInstall.get(hardwareDetails.getDeviceType()) == null) {
							childCIsNotToInstall.put(hardwareDetails.getDeviceType(), new ArrayList<ChildCI>());
						}
						childCIsNotToInstall.get(hardwareDetails.getDeviceType()).add(newChildCI);
					}
				}
			} else {
				if (existingSWParentCI == null) {
					if (updateService.isInfo()) {
						updateService
								.logInfo("Update Service: Existing Software's parentCI is null");
					}
				}
				if (newLRUParentCI == null) {
					if (updateService.isInfo()) {
						updateService
								.logInfo("Update Service: New Software's parentCI is null");
					}
				}
			}
		} else {
			if (updateService.isInfo()) {
				updateService
						.logInfo("Update Service: HDSInfo file doesn't exist, new files will be installed");
			}
		} // CR-123 END
		
		installList.setParentCIsToInstall(parentCIsToInstall);
		installList.setChildCIsToInstall(childCIsToInstall);
		installList.setChildCIsNotToInstall(childCIsNotToInstall);

		return installList;
	}
	
	/**
	 * Filter supported mods.
	 *
	 * @param existingChildCI the existing childCI
	 * @param lruModList the lru mod list
	 */
	private void filterSupportedMods(final ChildCI existingChildCI, final String[] lruModList) {
		ArrayList<Integer> ciSupportedMods = existingChildCI.getSupportedMods();
		ArrayList<String> lruMods = null;
		
		if (lruModList.length > 0) {
			lruMods = new ArrayList<String>(Arrays.asList(lruModList));
		}
		
		if (ciSupportedMods != null && ciSupportedMods.size() > 0 && lruMods != null && lruMods.size() > 0) {
			final Iterator<Integer> ciSupportedModsItr = ciSupportedMods.iterator();
			while (ciSupportedModsItr.hasNext()) {
				final Integer ciSupportMod = ciSupportedModsItr.next();
				if (ciSupportMod != null && !lruMods.contains(ciSupportMod.toString())) {
					ciSupportedModsItr.remove();
				}
			}
		}
	}

	/**
	 * Check conformity.
	 * 
	 * @param lruType the lru type
	 * @param newParentCI the new parent CI
	 * @param forceLoadFlag the force load flag
	 * 
	 * @return true, if successful
	 */
	private boolean checkConformity(final String lruType, final ParentCI newParentCI, final boolean forceLoadFlag) {
		if (newParentCI != null) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: ParentCI for " + lruType + " is available in new Parent CIs.");
			}
			if (newParentCI.isPrimitive()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI " + lruType + " is primitive");
				}
				if (!forceLoadFlag) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Checking " + lruType + " conformity...");
					}

					boolean isConform = checkConformityPrimitive(newParentCI);

					if (!isConform) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + lruType + " software DO NOT CONFORM with the new " + lruType + " software");
						}
						parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + lruType + " software conforms with the new " + lruType + " software");
						}
					}
				} else {
					parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: ParentCI " + lruType + " is non-primitive");
				}
				if (!forceLoadFlag) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Checking " + lruType + " conformity...");
					}

					final List<ChildCI> toInstall = new ArrayList<ChildCI>();
					final List<ChildCI> notToInstall = new ArrayList<ChildCI>();

					boolean isConform = checkConformityNonPrimitive(newParentCI, toInstall, notToInstall);

					if (!isConform) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + lruType + " software DO NOT CONFORM with the new " + lruType + " software");
						}
						parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
						childCIsToInstall.put(newParentCI.getLruType(), toInstall);
						childCIsNotToInstall.put(newParentCI.getLruType(), notToInstall);
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Existing " + lruType + " software CONFORMS with the new " + lruType + " software");
						}
					}
				} else {
					final List<ChildCI> toInstall = new ArrayList<ChildCI>();

					boolean isConform = getAllChildCIs(newParentCI, toInstall);
					if (!isConform) {
						parentCIsToInstall.put(newParentCI.getLruType(), newParentCI);
						childCIsToInstall.put(newParentCI.getLruType(), toInstall);
					} else {
						UnitManager.Logging.logSevere("Update Service: There are no new ChildCIs available in the new ParentCI");
						UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: Force Load failed for " + lruType));
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check conformity non primitive.
	 * 
	 * @param newParentCI the new parent CI
	 * @param toInstall the to install
	 * @param notToInstall the not to install
	 * 
	 * @return true, if successful
	 */
	private boolean checkConformityNonPrimitive(final ParentCI newParentCI, final List<ChildCI> toInstall, final List<ChildCI> notToInstall) {
		boolean isConform = true;

		final ParentCI parentCI = parentCIs.get(newParentCI.getLruType());

		if (parentCI == null) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Conformity Failure: No ParentCI installed already to perform conformity check");
				updateService.logInfo("Update Service: Conformity Failure: The new ParentCI needs to be installed: " + newParentCI.getLruType());
			}
			return getAllChildCIs(newParentCI, toInstall);
		}

		final HashMap<String, ChildCI> newChildCIs = newParentCI.getChildCIs();

		final HashMap<String, ChildCI> childCIs = parentCI.getChildCIs();

		if (childCIs == null || childCIs.size() == 0) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Conformity Failure: No ChildCI installed already to perform conformity check of ParentCI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
			}
			return getAllChildCIs(newParentCI, toInstall);
		}

		if (newChildCIs == null || newChildCIs.size() == 0) {
			UnitManager.Logging.logSevere("Update Service: Not able to perform conformity check");
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: No new ChildCI available in the new ParentCI: " + newParentCI.getLruType() + " : " + newParentCI.getCpn()));
			return true;
		}

		// Loop through every new Child CI
		// To perform conformity check and to find any new Child CI that was not
		// installed earlier
		// If exists in old Child CI, perform conformity check
		// If not, add the new Child CI to the install list
		for (final Entry<String, ChildCI> newChildCIEntry : newChildCIs.entrySet()) {
			final String newChildCIType = newChildCIEntry.getKey();
			final ChildCI newChildCI = newChildCIEntry.getValue();

			if (childCIs.containsKey(newChildCIType)) {
				final ChildCI childCI = childCIs.get(newChildCIType);

				if (!childCI.getCpn().equals(newChildCI.getCpn()) || !childCI.getBuildNumber().equals(newChildCI.getBuildNumber()) || !childCI.getMd5Value().equals(newChildCI.getMd5Value())) {
					isConform = false;
					toInstall.add(newChildCI);
				} else {
					notToInstall.add(newChildCI);
				}
			} else {
				UnitManager.Logging.logWarning("Update Service: Unknown ChildCI type: " + newChildCIType);
				UnitManager.Logging.logWarning("Update Service: Adding to install list: " + newChildCI.getCpn());
				toInstall.add(newChildCI);
			}
		}

		// Loop through every old Child CI
		// To find any old Child CI is missing from the new Child CIs
		// If exists in new Child CI, nothing to do as conformity check is done in the earlier loop
		// If not, add the old Child CI to not to install list
		for (final Entry<String, ChildCI> childCIEntry : childCIs.entrySet()) {
			final String childCIType = childCIEntry.getKey();
			final ChildCI childCI = childCIEntry.getValue();

			if (!newChildCIs.containsKey(childCIType)) {
				UnitManager.Logging.logWarning("Update Service: ChildCI type: " + childCIType + " is missing in the LA Message");
				UnitManager.Logging.logWarning("Update Service: The Child CI: " + childCI.getCpn() + " will not be installed");
				notToInstall.add(childCI);
			}
		}

		return isConform;
	}

	/**
	 * Check conformity primitive.
	 * 
	 * @param newParentCI the new parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean checkConformityPrimitive(final ParentCI newParentCI) {
		boolean isConform = true;

		final ParentCI parentCI = parentCIs.get(newParentCI.getLruType());

		if (parentCI == null) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Conformity Failure: No ParentCI installed already to perform conformity check");
				updateService.logInfo("Update Service: Conformity Failure: The new ParentCI needs to be installed: " + newParentCI.getLruType());
			}
			return false;
		}

		if (!parentCI.getCpn().equals(newParentCI.getCpn()) || !parentCI.getBuildNumber().equals(newParentCI.getBuildNumber()) || !parentCI.getMd5Value().equals(newParentCI.getMd5Value())) {
			isConform = false;
		} else {
			isConform = true;
		}
		return isConform;
	}

	/**
	 * Gets the all child CIs.
	 * 
	 * @param newParentCI the new parent CI
	 * @param toInstall the to install
	 * 
	 * @return the all child CIs
	 */
	private boolean getAllChildCIs(final ParentCI newParentCI, final List<ChildCI> toInstall) {
		final HashMap<String, ChildCI> newChildCIs = newParentCI.getChildCIs();

		if (newChildCIs == null || newChildCIs.size() == 0) {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: No new ChildCI available in the new ParentCI: " + newParentCI.getLruType() + " : " + newParentCI.getCpn()));
			return true;
		}
		for (final Entry<String, ChildCI> newChildCIEntry : newChildCIs.entrySet()) {
			final ChildCI newChildCI = newChildCIEntry.getValue();
			toInstall.add(newChildCI);
		}
		return false;
	}
}
