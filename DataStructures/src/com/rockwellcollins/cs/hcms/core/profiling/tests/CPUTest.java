package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import java.io.*;
public class CPUTest extends ProfileTest {

	private static final long serialVersionUID = 1L;
	private double user ;
	private double sys ;
	private double nice;
	private double idle;
	private double percCpuUsage ;
	
	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {
		super.onInitialize(source, args);
	}
	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		
		System.out.println("CPUTest: Poll Time: " + this.getPollTime() );
		
		/*** call Calculate Percentage CPU ***/
		System.out.println("CPUTest: First Data "+ "User: " + user + " sys: " 
				+ sys + " nice: " + nice + " idle: " + idle);
		/*** Parse CPU usage information from /proc/stat and calculate percentage ***/
		percCpuUsage = CalculatePercentage();
		System.out.println("CPUTest: Second Data "+ "User: " + user + " sys: " 
				+ sys + " nice: " + nice + " idle: " + idle);

		/*** assign results ***/
		result.setValue(percCpuUsage);
		System.out.println("CPUTest: CPU Percentage: " + percCpuUsage);
			
	}
	
	private boolean parseCPUInfo() {
		String cpuStatFilename= "/proc/stat";
		String line;
		String delimeter = " ";
		String cpuInfo[];
		String cpu = "";
		
		/*** File class to access /proc/stat file ***/
		File cpuStat = new File(cpuStatFilename);
				
		try {
			if (cpuStat != null && cpuStat.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(
						cpuStat));
				while ((line = in.readLine()) != null) {
					if (line.startsWith("cpu ")) {
						 cpu = line.substring(5).trim();
						 
					}
				}
				System.out.println("Before Parsing cpu data:" + cpu);
				cpuInfo = cpu.split(delimeter, 5);
				user = Integer.parseInt(cpuInfo[0]);
				sys = Integer.parseInt(cpuInfo[1]);
				nice = Integer.parseInt(cpuInfo[2]);
				idle = Integer.parseInt(cpuInfo[3]);
				in.close();
				return true;
			}
		}catch (final Exception e) {
			UnitManager.Logging.logSevere("CPUTest exception parsing proc file" + e);
		}
		return false;
	}

	private double CalculatePercentage()   {
		/*** Variable created to store the previous value of User, Sys, Nice, and Idle***/
		double firstUser;
		double firstSys;
		double firstNice;
		double firstIdle;

		/*** Variable created to assign the delta values ***/
		double deltaUser;
		double deltaSys;
		double deltaNice;
		double deltaIdle;
		double deltaTotal;
		double cpuPerc;
		
		/*** Assigning Previous Value ***/
		firstUser = user;
		firstSys = sys;
		firstNice = nice;
		firstIdle = idle;
		

		/*** Parsing the second set of values ***/
		
		if (parseCPUInfo() == true) {

			deltaUser = user - firstUser;
			deltaSys = sys  - firstSys;
			deltaNice = nice - firstNice;
			deltaIdle = idle  - firstIdle;
			deltaTotal = deltaUser + deltaNice + deltaIdle + deltaSys;
			
			System.out.println("CPUTest: Delta Data "+ "User: " + deltaUser + " sys: " 
					+ deltaSys + " nice: " + deltaNice + " idle: " + deltaIdle + " total:" + deltaTotal);
			/*** Handling Divide by zero case ***/
			if (deltaTotal != 0 ) {
				//cpuPerc = ((deltaUser + deltaSys + deltaNice) * 100 / deltaTotal);
				cpuPerc = (1000 * (deltaTotal - deltaIdle)/deltaTotal )/10;
			}
			else {
				cpuPerc = 0;
			}
			System.out.println("CPUTest: Calculated CPU percentage: " + cpuPerc);
			return cpuPerc;
		}
		return 0;
	}
}
