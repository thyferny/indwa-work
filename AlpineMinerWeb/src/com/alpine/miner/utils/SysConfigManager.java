/**
 * ClassName :TempFileManagerImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.utils;



public interface SysConfigManager {
	public static SysConfigManager INSTANCE = new SysConfigManagerImpl();
	public static final long DEFAULT_LIVE_TIME = 1000 * 3600 * 24;
	// default 1 day check ...
	public static final long DEFAUlT_SCAN_FREQUENCY = 1000 * 3600 * 24;

	public static final String Default_Server_Encoding = "ISO8859-1";
	public static final String configFileName = "sys.properties";
	public static final String KEY_SERVER_ENCODING = "server_encoding";
	public static final String KEY_LIVETIME = "temp_file_livetime";
	public static final String KEY_SCAN_FREQUENCY = "temp_file_scan_frequency";
	public static final String KEY_SAMPLE_FOLDER = "sample_floder";
	public long getLiveTime();
	public void setLiveTime(long liveTime);
	public long getScanFrequency();
	public void setScanFrequency(long scanFrequency);
	public String getServerEncoding();
	public void setServerEncoding(String serverEncoding);
	public void init();
	public String getSampleFolder();

}
