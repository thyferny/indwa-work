package com.alpine.license.validator.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.alpine.resources.CommonLanguagePack;
import org.apache.log4j.Logger;

public class MacAddressUtil {
	private static final String OS_NAME = "os.name";
	private static final String LINUX = "Linux";
	private static final String IFCONFIG_COMMAND = "/sbin/ifconfig";
	private static final String HWADDR_Linux = "hwaddr";
	private static final String MAC_ADDRESS_FOUND = "Mac address found:";
    private static final Logger itsLogger = Logger.getLogger(MacAddressUtil.class);

	public static boolean isAVlidateMacAddress(String macAddress) throws Exception{
		ArrayList<String>  macs = getMacAddress();
		if(macAddress!=null&&macs!=null){
			for (int i = 0; i < macs.size(); i++) {
				if(macs.get(i).equalsIgnoreCase(macAddress)==true){
					return true;
				}
			}
		}
		return false;
	}


	public static ArrayList<String> getMacAddress() throws Exception{
		String operatSystem=System.getProperty(OS_NAME);
		ArrayList<String> multipleAddr = new ArrayList<String>();
		
		if(operatSystem.startsWith(LINUX) ){	
			List<String > macs= getLinuxMacStr();
			multipleAddr.addAll(macs);
		
			return multipleAddr;
		} else if( operatSystem.startsWith("Mac")){	
			List<String > macs= getMACMacStr();
			multipleAddr.addAll(macs);
		
			return multipleAddr;
		}else {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			ArrayList<NetworkInterface> netarry = Collections.list(nets);
			for (NetworkInterface netint : netarry) {
				String addr = getNonLinuxMacStr(netint);
				if (addr != null && addr.length() > 0 ) 
					multipleAddr.add(addr);
				
			}
			return multipleAddr;
		}
	}
	
	private static List<String> getMACMacStr() throws Exception {
		 return getMacAddressFromCommandLine(IFCONFIG_COMMAND,"ether");
	}


	private static List<String> getLinuxMacStr() throws Exception {
 
		 return getMacAddressFromCommandLine(IFCONFIG_COMMAND,HWADDR_Linux);
	}


	private static List<String> getMacAddressFromCommandLine(String command,String startStr) throws Exception {
		List<String> macs = new ArrayList<String>();
	        BufferedReader bufferedReader = null;  
	        Process process = null;  
	        String mac = null; 
			try {  
	      
	            process = Runtime.getRuntime().exec(command);  
	            bufferedReader = new BufferedReader(new InputStreamReader(  
	                    process.getInputStream()));  
	            String line = null;  
	            int index = -1;  
	            while ((line = bufferedReader.readLine()) != null) {  
	                index = line.toLowerCase().indexOf(startStr);  
	                if (index >= 0) { 
	                    mac = line.substring(index + startStr.length() + 1).trim();  
	                    macs.add(mac) ;
	                    itsLogger.info(MAC_ADDRESS_FOUND +mac);
//	                    System.out.println(MAC_ADDRESS_FOUND +mac);
	                }  
	            }  
	        } catch (IOException e) {  //No such file or directory
	            e.printStackTrace(); 
	            if(e.getMessage().indexOf("No such file or directory")>0) {
	            	 String errorMessag = "Alpine license will need the following command to validate the MAC address:"
	            		 +IFCONFIG_COMMAND +"\nPlease make sure you have correctly configured this."; 
					itsLogger.error(errorMessag);
					 System.out.println(errorMessag);
	            	throw new Exception(errorMessag); 
	            }
	        } finally {  
	            try {  
	                if (bufferedReader != null) {  
	                    bufferedReader.close();  
	                }  
	            } catch (IOException e1) {  
	                e1.printStackTrace();  
	            }  
	            bufferedReader = null;  
	            process = null;  
	        }  
	      
	        return macs;
	}

	private static String getNonLinuxMacStr(NetworkInterface ni) {
		StringBuffer macStr = new StringBuffer();
		try {
		
			if (ni != null) {
				byte[] mac = ni.getHardwareAddress();
				if (mac != null) {
					for (int i = 0; i < mac.length; i++) {
						macStr.append(String.format("%02X%s", mac[i],
								(i < mac.length - 1) ? "-" : ""));
					}
					itsLogger.info(macStr.toString());
//					   System.out.println(MAC_ADDRESS_FOUND +mac);
					return macStr.toString();
				}  // if non hardware networkinterface, ignore it
			} else {
				itsLogger.error(CommonLanguagePack.Network_Interface_error);
			}
		} catch (Exception e) {
			itsLogger.error(e);
		}
		return new String("");
	}
	
 
}
