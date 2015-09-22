/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LicenseUpdateInfo
 * Apr 1, 2012
 */
package com.alpine.miner.impls.license;

/**
 * Used in update license module.
 * @author Gary
 *
 */
public class LicenseUpdateInfo {

	private String 	adminPwd,
					license;

	public String getAdminPwd() {
		return adminPwd;
	}

	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
}
