/**
 * ClassName  LicenseHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.license.validator.miner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.alpine.license.validator.illuminator.ILicenseValidator.ValidationResult;
import com.alpine.resources.LanguagePackUtility;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

/**
 * @author John Zhao
 * 
 */
public class MinerLicenseValidator {
	public static final String Prop_Name_User="user";
	public static final String Prop_Name_Company="company";
	public static final String Prop_Name_Key="key";
	private static final String Is_Expireed = "expired";
    private static final Logger itsLogger = Logger.getLogger(MinerLicenseValidator.class);


    /**
	 * @param key
	 * @param company
	 * @param user
	 * @return
	 */
	public static String validateLinceseKey(String user, String company,
			String key)  {
		try {
			ValidationResult result = getValidateResult(user, company, key);
 			if(result!= ValidationResult.PASS){
 				return LanguagePackUtility.INVALID_LICENSE_KEY+":"+ result.getMessage();
 			}
 			else {
 				return null;
 			}
		} catch (Exception e) {
			return LanguagePackUtility.INVALID_LICENSE_KEY;
		}
	}




	public static ValidationResult getValidateResult(String user,
			String company, String key) throws Exception {
		AlpineMinerLicense license= new AlpineMinerLicense(user,company);
		 ValidationResult result = license.validLicense(key);
		return result;
	}
	
	
 

	/**
	 * @param externalForm
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static boolean validateLicenseFile(String filePath) throws Exception {

		if(filePath.startsWith("file:")){//form eclipse...
			filePath=filePath.substring(5,filePath.length());
		}
		File file= new File(filePath);
		 
		Properties props = getLicenseProperty(file);
		
 
		String user = props.getProperty(Prop_Name_User);
		String company = props.getProperty(Prop_Name_Company);
		String key = props.getProperty(Prop_Name_Key);
		String isExpired = props.getProperty(Is_Expireed);
		if (isExpired != null && isExpired.equalsIgnoreCase("true")) {
			return false;
		} else {
			try {
				ValidationResult result = getValidateResult(user, company, key);

				if (result != ValidationResult.PASS) {
					if (result == ValidationResult.EXPIRED) {
						//expired
						makeLicenseFileExpired(file);
						 
					}  
						return false;
					 
				} else {
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}

	}




	private static Properties getLicenseProperty(File file)
			throws FileNotFoundException, IOException {
		StringBuffer propsStr = FileUtility.readFiletoString(file);
		String str=		propsStr.toString();
		Properties props=(Properties)StringUtil.stringToObject(str);
		return props;
	}

	/**
	 * @param file 
	 * 
	 */
	private static void makeLicenseFileExpired(File file) {
		String isExpired="true";
		StringBuffer propsStr;
		try {
			propsStr = FileUtility.readFiletoString(file);
		
			String str=		propsStr.toString();
			Properties props=(Properties)StringUtil.stringToObject(str);
			props.setProperty(Is_Expireed, isExpired);
			String fileString=StringUtil.objectToString(props) ;
			FileUtility.writeFile(file.getAbsolutePath(), fileString) ;
			
		} catch  (Exception e) {
			 
			itsLogger.error(e);
		}
		 
		
	}

	public static boolean isDateExceeedTrialLicenseExpireDate()  {
		try {
			AlpineMinerLicense al = new AlpineMinerLicense("","");
			if (System.currentTimeMillis() > al.getTrialLicenseExpiredTime()) {
				
				return true;
			} else {
				
				
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
 

}
