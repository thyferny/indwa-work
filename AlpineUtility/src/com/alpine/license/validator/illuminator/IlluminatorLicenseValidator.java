/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IlluminatorLicenseValidator
 * Mar 31, 2012
 */
package com.alpine.license.validator.illuminator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alpine.license.validator.LiecnseDecryptor;
import com.alpine.license.validator.miner.MacAddressUtil;



/**
 * @author Gary
 *
 */
public class IlluminatorLicenseValidator implements ILicenseValidator {
	
	/* (non-Javadoc)
	 * @see com.alpine.illuminator.license.validator.ILicenseValidator#validateKey(java.lang.String, com.alpine.illuminator.license.validator.ILicenseValidator.ValidateHandler)
	 */
	@Override
	public ValidationResult validateKey(String license, int userCount, int modelerUserCount) {
		ValidationResult valid = null;
		try {
			String[] comboKey = LiecnseDecryptor.decrypt(license,IlluminatorLicenseInfoBuilder.PRODUCT_NAME).split(LiecnseDecryptor.SEPARATOR_MARK);
			if(LiecnseDecryptor.ENCRYPT_KEY.equals(comboKey[0]) && IlluminatorLicenseInfoBuilder.PRODUCT_NAME.equals(comboKey[1])){//basic info passed validate
				for(int i = 2; i < comboKey.length;i++){
					switch(i){
					case 2: 
						if(!LiecnseDecryptor.IGNORE.equals(comboKey[2]) && userCount > Integer.parseInt(comboKey[2])){
							valid = ValidationResult.OVER_LIMIT;
							valid.setMessage(comboKey[2]);
						}
						break;
					case 3:
						if(!LiecnseDecryptor.IGNORE.equals(comboKey[3]) && isExpire(comboKey[3])){
							valid = ValidationResult.EXPIRED;
							valid.setMessage(comboKey[3]);
						}
						break;
					case 5:
						if(!LiecnseDecryptor.IGNORE.equals(comboKey[5]) && !MacAddressUtil.isAVlidateMacAddress(comboKey[5])){
							valid = ValidationResult.MACHINE_UNMATCHED;
						}
						break;
					case 6:
						if(!LiecnseDecryptor.IGNORE.equals(comboKey[6]) && modelerUserCount > Integer.parseInt(comboKey[6])){
							valid = ValidationResult.OVER_LIMIT_MODELER;
							valid.setMessage(comboKey[6]);
						}
					}
				}
				if(valid == null){
					valid = ValidationResult.PASS;
				}
			}else{
				valid = ValidationResult.UNKNOWN_LICENSE;
			}
		} catch (Exception e) {
			e.printStackTrace();
			valid = ValidationResult.UNKNOWN_LICENSE;
		}
		return valid;
	}
	
	private static boolean isExpire(String deadline){
		SimpleDateFormat dataformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date deadlineDate = null;
		try {
			deadlineDate = dataformat.parse(deadline + " 23:59:59");
		} catch (ParseException e) {
			return true;
		}
		return new Date().getTime() > deadlineDate.getTime();
	}
}
