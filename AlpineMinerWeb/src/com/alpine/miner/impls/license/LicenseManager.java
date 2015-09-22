/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LicenseManager
 * Apr 1, 2012
 */
package com.alpine.miner.impls.license;

import java.io.File;
import java.io.IOException;

import com.alpine.license.validator.illuminator.IlluminatorLicenseInfoBuilder;
import com.alpine.license.validator.illuminator.LicenseInfoDirector;
import com.alpine.license.validator.illuminator.LicenseInfomation;
import com.alpine.miner.impls.license.WebLicenseChecker.CheckResult;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.utility.file.FileUtility;

/**
 * License management
 * @author Gary
 * 
 */
public class LicenseManager {

	public static final String LICENSE_FILE = FilePersistence.ROOT
			+ "Alpine_Illuminator_License";

	/**
	 * initialize data for first time run Illuminator Software
	 */
	public static void initialize() {
//		SeriesIDHelper.generateSeriesID();
	}

	/**
	 * save license series to license file
	 * @param license
	 * @throws IOException
	 */
	public static void storeLicense(String license) throws IOException {
		FileUtility.writeFile(LICENSE_FILE, license);
	}

	public static void main(String[] args) {
		System.out.println(WebLicenseChecker.checkLicense(0, 0));
	}
	static String loadLicense() {
		String license;
		try {
			license = FileUtility.readFiletoString(new File(LICENSE_FILE))
					.toString();
		} catch (Exception e) {
			return null;
		}
		return license;
	}

	/**
	 * get license information from license file
	 * @return
	 * @throws Exception
	 */
	public static LicenseInfomation getLicenseInfo() throws Exception {
		IlluminatorLicenseInfoBuilder builder;
		try {
			builder = IlluminatorLicenseInfoBuilder.createIntance(loadLicense());
		} catch (Exception e) {
			throw new Exception(CheckResult.INVALID_LICENSE.name());
		}
		return LicenseInfoDirector.getInstance(builder).getLicenseInfo();
	}
}
