/**
 * ClassName  OperatSystemAdapter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

/**
 * @author John Zhao
 *
 */
public class OperaterSystemAdapterFactory {
	static OperaterSystemAdapter windowsAdapter=new WindowsSystemAdapter();
	static LinuxSystemAdapter linuxAdapter=new LinuxSystemAdapter();
	static MacOSSystemAdapter macAdapter=new MacOSSystemAdapter();
	public static OperaterSystemAdapter getAdatper(){
		String operatSystem=System.getProperty("os.name");
		if(operatSystem.startsWith("Windows")){
			return windowsAdapter;
		}else if(operatSystem.startsWith("Linux")){
			 return linuxAdapter;
		}else if(operatSystem.startsWith("Mac OS")){
			 return macAdapter;
		}
		else{
			return null;
		}
	}

    public static OperaterSystemAdapter getAdatper(String type){
        //String operatSystem=System.getProperty("os.name");
        if("Windows".equalsIgnoreCase(type)){
            return windowsAdapter;
        }else if("Linux".equalsIgnoreCase(type)){
            return linuxAdapter;
        }else if("Mac".equalsIgnoreCase(type)){
            return macAdapter;
        }
        else{
            return null;
        }
    }
}
