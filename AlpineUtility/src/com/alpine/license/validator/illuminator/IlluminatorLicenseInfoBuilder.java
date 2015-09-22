/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ILicenseInfoBuilder
 * Apr 17, 2012
 */
package com.alpine.license.validator.illuminator;

import java.io.IOException;
import java.util.Properties;

import com.alpine.license.validator.LiecnseDecryptor;


/**
 * License info builder
 * @author Gary
 *
 */
public class IlluminatorLicenseInfoBuilder { 
	public static final String PRODUCT_NAME = "AlpineIlluminator";
	private String[] decryptedLicenseSeries;
	private static final String NEVER_EXPIRE = "Permanent",
								LIMITLESS = "No limitation",
								VERSION = getVersion();
	
	private static String getVersion(){
		Properties props = new Properties();
		try {
			props.load(IlluminatorLicenseInfoBuilder.class.getResourceAsStream("productionInfo.properties"));
			return props.getProperty("version");
		} catch (Exception e) {
			return "Unknow version";
		}
	}
	
	private IlluminatorLicenseInfoBuilder(String licenseSeries) throws Exception{
		this.decryptedLicenseSeries = LiecnseDecryptor.decrypt(licenseSeries,PRODUCT_NAME).split(LiecnseDecryptor.SEPARATOR_MARK);
	}
	
	public static IlluminatorLicenseInfoBuilder createIntance(String licenseSeries) throws Exception{
		return new IlluminatorLicenseInfoBuilder(licenseSeries);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.illuminator.license.validator.ILicenseInfoBuilder#buildExpireDate()
	 */
	public String buildExpireDate() {
		String expireDate = decryptedLicenseSeries[3];
		return "-1".equals(expireDate) ? NEVER_EXPIRE : expireDate;
	}

	/* (non-Javadoc)
	 * @see com.alpine.illuminator.license.validator.ILicenseInfoBuilder#buildLimitUserCount()
	 */
	public String buildLimitUserCount() {
		String limit = decryptedLicenseSeries[2];
		return "-1".equals(limit) ? LIMITLESS : limit;
	}

	/* (non-Javadoc)
	 * @see com.alpine.illuminator.license.validator.ILicenseInfoBuilder#buildProductID()
	 */
	public String buildProductID() {
		return VERSION;
	}
	
	public String buildLimitModelerCount(){
		if(decryptedLicenseSeries.length < 6){
			return LIMITLESS;//compatible old license.
		}
		String limitModelerCount = decryptedLicenseSeries[6];
		return "-1".equals(limitModelerCount) ? LIMITLESS : limitModelerCount;
	}
	
}
