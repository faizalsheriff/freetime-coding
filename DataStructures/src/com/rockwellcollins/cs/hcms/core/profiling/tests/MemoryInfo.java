package com.rockwellcollins.cs.hcms.core.profiling.tests;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import java.io.*;


public class MemoryInfo extends ProfileTest{
	private static final long serialVersionUID = 1L;
	private double percentageMemory;
	private double totalMemory;
	private double freeMemory;
	private double usedMemory;
	
	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {
		super.onInitialize(source, args);
	}
	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);
		
		UnitManager.Timing.getTimeAlive();	
			
		/** Parse Memory from /proc/meminfo **/
		parseMemoryInfo();
		
		/** Calculate Percentage Memory  **/
		percentageMemory = calcMemoryPercentage();
				
		/** Assign results **/
		if (percentageMemory != 0 ) {
			result.setValue(percentageMemory);
			result.setMax(totalMemory);
		}
	}
	private void parseMemoryInfo() {
		String delimeter = " ";
		String memoryStatus= "/proc/meminfo";
		String line = "";
		String memFree = "";
		String memTotal = "";
		String memTotalParse[];
		String memFreeParse[];
		
		File memInfo = new File(memoryStatus);
		
		try {
			if (memInfo != null && memInfo.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(
						memInfo));
				while ((line = in.readLine()) != null) {
					if (line.startsWith("MemTotal:")) {
						memTotal = line.substring(9).trim();
	//					System.out.println("MemoryInfo: Parsed Memory Total: " + memTotal);
					}
					
					if (line.startsWith("MemFree:")) {
						memFree = line.substring(9).trim();
	//					System.out.println("MemoryInfo: Parsed Memory Free: " + memFree);
					}
				}
				memTotalParse = memTotal.split(delimeter, 2);
				memFreeParse = memFree.split(delimeter, 2);
				totalMemory = Integer.parseInt(memTotalParse[0]);
	//			System.out.println("MemoryInfo: Total Memory : " + totalMemory);
				freeMemory = Integer.parseInt(memFreeParse[0]);
	//			System.out.println("MemoryInfo: Free Memory : " + freeMemory);
				usedMemory = totalMemory - freeMemory;
	//			System.out.println("MemoryInfo: Used Memory : " + usedMemory);
				in.close();
			}
		}catch (final Exception e) {
			UnitManager.Logging.logSevere("MemoryInfoTest exception parsing proc file" + e);
		}
}
	private double calcMemoryPercentage() {
		double percMemory;
		if (totalMemory != 0){
			percMemory = (usedMemory * 100) / totalMemory;
		}
		else {
			percMemory = 0;
		}
//		System.out.println("MemoryInfo: Used Percentage Memory : " + percMemory);
		return percMemory;
	}
}
