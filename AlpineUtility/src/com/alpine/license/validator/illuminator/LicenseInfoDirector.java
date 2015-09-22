/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LicenseInfoDirector
 * Apr 17, 2012
 */
package com.alpine.license.validator.illuminator;

/**
 * @author Gary
 *
 */
public class LicenseInfoDirector {
	
	private IlluminatorLicenseInfoBuilder builder;

	private LicenseInfoDirector(IlluminatorLicenseInfoBuilder builder){
		this.builder = builder;
	}
	
	public static LicenseInfoDirector getInstance(IlluminatorLicenseInfoBuilder builder){
		return new LicenseInfoDirector(builder);
	}
	
	public LicenseInfomation getLicenseInfo(){
		final String 	productID = builder.buildProductID(),
						expireDate = builder.buildExpireDate(),
						limitUserCount = builder.buildLimitUserCount(),
						limitModelerCount = builder.buildLimitModelerCount();
		return new LicenseInfomation() {
			
			@Override
			public String getProductID() {
				return productID;
			}
			
			@Override
			public String getLimitUserCount() {
				return limitUserCount;
			}
			
			@Override
			public String getExpireDate() {
				return expireDate;
			}

			@Override
			public String getLimitModelerCount() {
				return limitModelerCount;
			}
		};
	}
}
