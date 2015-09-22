/**
 * ClassName ProfileUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-19
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.profile;

import java.util.HashMap;

public class ProfileUtility {

	public static final String UI_PAGE_MAX_SIZE = "ui_para1";
	
	public static final String UI_TABLE_LIMIT = "ui_para2";
	
	public static final String UI_ADD_PREFIX = "ui_para3";
	
	public static final String ALG_MAX_DISTINCT = "alg_para1";
	
	public static final String ALG_DIGIT_PRECISION = "alg_para2";
	
	public static final String ALG_VA_MAX_DISTINCT = "alg_para3";
	
	public static final String DB_CONN_TIMEOUT = "db_para1";
	
	public static final String SYS_LOG ="sys_log";
	
	public static final String LOCAL_HD_RUNNER_THRESHOLD = "hd_local_data_size_threshold";
	
	private HashMap<String,String> defaultMap=null;
	
	public static final ProfileUtility INSTANCE= new ProfileUtility();
	
	public static final String D_SYS_LOG ="info";
	
	public static final String D_UI_PRA1 = "200";
	
	public static final String D_UI_PRA2 = "200";
	
	public static final String D_UI_PRA3 = "false";
	
	public static final String D_ALG_PRA1 = "100000";
	
	public static final String D_ALG_PRA2 = "4";

	public static final String D_ALG_PRA3 = "1000";
	
	public static final String D_DB_PRA1 = "5";
	
	public static final String D_HD_LOCAL_RUNNER_THRESHOLD = "100";
//this is internal use
	public static final String HD_LOCAL_MODE = "hadoop_local_mode";
	
	private ProfileUtility() {
		defaultMap = new HashMap<String, String>();

		defaultMap.put(SYS_LOG, D_SYS_LOG);
		defaultMap.put(UI_PAGE_MAX_SIZE, D_UI_PRA1);
		defaultMap.put(UI_TABLE_LIMIT, D_UI_PRA2);
		defaultMap.put(UI_ADD_PREFIX, D_UI_PRA3);
		defaultMap.put(ALG_MAX_DISTINCT, D_ALG_PRA1);
		defaultMap.put(ALG_DIGIT_PRECISION, D_ALG_PRA2);
		defaultMap.put(ALG_VA_MAX_DISTINCT, D_ALG_PRA3);
		defaultMap.put(DB_CONN_TIMEOUT, D_DB_PRA1);
		defaultMap.put(LOCAL_HD_RUNNER_THRESHOLD, D_HD_LOCAL_RUNNER_THRESHOLD);

	}
	
	public String getPreferenceDefaltValue(String para){
		return defaultMap.get(para);
	}
	
}
