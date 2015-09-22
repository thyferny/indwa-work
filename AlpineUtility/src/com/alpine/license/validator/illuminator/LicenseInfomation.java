/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LicenseInfomation
 * Apr 17, 2012
 */
package com.alpine.license.validator.illuminator;

/**
 * @author Gary
 *
 */
public interface LicenseInfomation {

	String getProductID();
	
	String getExpireDate();
	
	String getLimitUserCount();
	
	String getLimitModelerCount();
}
