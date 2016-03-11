package com.rockwellcollins.cs.hcms.core.utils;


import java.io.File;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;

public class Directory {

	static String SHARED_LIBRARY_NAME = "jnidirectorycheck";	
	static boolean libraryLoaded = false;
	 public static boolean isDirectoryGood(String path){
		 
		 //If we have the library, then perform the checks otherwise
		 //act as if this logic of checking the directory doesn't exist
		 
		 if(libraryLoaded){
			 if(path != null && path.trim().length() > 0)
				 return isDirectoryGoodNative(path);
			 
			 if(UnitManager.Logging.isDebug())
				 UnitManager.Logging.logInfo("Directory::isDirectoryGood - The Directory " + path + "is faulty");
			 return false;
		 }else{
			 return true;
		 }
	 }
	 static{
		 String name = System.getProperty("java.library.path");
			String []paths = name.split(Consts.IOs.PATH_SEPARATOR);
			boolean found = false;
			for(String path : paths){
				if(!path.endsWith(Consts.IOs.FILE_SEPARATOR))
					path += Consts.IOs.FILE_SEPARATOR;
				File f = new File(path +  System.mapLibraryName(SHARED_LIBRARY_NAME));
				if(f.exists()){
					found = true;
					break;
				}
			}
			if(found)
			{
				System.loadLibrary(SHARED_LIBRARY_NAME);
				libraryLoaded = true;
			}
			else{
				if(UnitManager.Logging.isDebug())
					UnitManager.Logging.logDebug("Not loading the library" );
			}
	 }

	 
	 public static void sync(){
		 //If we have the library, then perform the sync 
		 if(libraryLoaded)
			 syncNative();
		 
	 }
	 private static native boolean isDirectoryGoodNative(String path);
	 private static native void syncNative();
}