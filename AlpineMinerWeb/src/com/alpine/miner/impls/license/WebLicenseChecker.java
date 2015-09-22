/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WebLicenseChecker
 * Mar 22, 2012
 */
package com.alpine.miner.impls.license;

import com.alpine.license.validator.illuminator.ILicenseValidator;

/**
 * Illuminator license validation
 * @author Gary
 *
 */
public class WebLicenseChecker {
	
	/**
	 * validate license
	 * @param totalUser current login user number
	 * @return
	 */
	public static CheckResult checkLicense(int totalUserCount, int modelerUserCount){
		String license = LicenseManager.loadLicense();
		if(license == null){
			return CheckResult.UNKNOWN_LICENSE;
		}
		ILicenseValidator.ValidationResult validationResult;
		
		try{
			validationResult = ILicenseValidator.INSTANCE.validateKey(license, totalUserCount + 1, modelerUserCount);// addition a user to check if total user number(include current request) is over restriction or not.
		}catch(Exception e){
			return CheckResult.INVALID_LICENSE;	
		}
		switch(validationResult){
		case OVER_LIMIT:
			String limitSize = validationResult.getMessage();
			CheckResult.OVER_LIMIT.setMsg(limitSize);
			return CheckResult.OVER_LIMIT;
		case EXPIRED:
			CheckResult.DATE_EXPIRE.setMsg(validationResult.getMessage());
			return CheckResult.DATE_EXPIRE;
		case MACHINE_UNMATCHED:
			return CheckResult.INVALID_MAC_ADRESS;
		case OVER_LIMIT_MODELER:
			String limitModelerSize = validationResult.getMessage();
			CheckResult.OVER_LIMIT_MODELER.setMsg(limitModelerSize);
			return CheckResult.OVER_LIMIT_MODELER;
		case PASS:
			return CheckResult.PASSED;
		default:
			return CheckResult.INVALID_LICENSE;	
		}
	}
	
	public static enum CheckResult{
		PASSED,
		DATE_EXPIRE,
		OVER_LIMIT,
		OVER_LIMIT_MODELER,
		INVALID_MAC_ADRESS,
		INVALID_LICENSE,
		UNKNOWN_LICENSE;
		
		private String msg;
		
		public void setMsg(String msg){
			this.msg = msg;
		}
		
		public String getMsg(){
			return msg;
		}
	}
}
