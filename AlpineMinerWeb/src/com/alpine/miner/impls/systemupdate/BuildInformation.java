/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SystemInformation.java
 */
package com.alpine.miner.impls.systemupdate;

import java.util.Date;

/**
 * @author Gary
 * Dec 5, 2012
 */
public interface BuildInformation {

	/**
	 * return the version number
	 * @return
	 */
	String getVersion();
	
	/**
	 * return the application context name
	 * @return
	 */
	String getApplicationName();
	
	/**
	 * return the URI where can get the build.
	 * @return
	 */
	String getDownloadURI();
	
	/**
	 * return the build's description
	 * @return
	 */
	String getDescription();
	
	/**
	 * return the release date of current build.
	 * @return
	 */
	Date getReleaseDate();
}
