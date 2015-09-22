/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ILicenceValidator
 * Mar 22, 2012
 */
package com.alpine.license.validator.illuminator;


/**
 * @author Gary
 *
 */
public interface ILicenseValidator {
	
	ILicenseValidator INSTANCE = new IlluminatorLicenseValidator();

	ValidationResult validateKey(String license, int userCount, int modelerCount);

	public static enum ValidationResult{
		PASS,
		UNKNOWN_LICENSE,
		OVER_LIMIT,
		OVER_LIMIT_MODELER,
		EXPIRED,
		USER_UNMATCH,
		COMPANY_UNMATCH,
		MACHINE_UNMATCHED;
		
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
	}
}
