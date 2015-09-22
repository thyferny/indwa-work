/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FlowInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.util.Properties;

/**
 * System preference will contain a list of the preferenceInfo
 * @author zhaoyong
 * 
 */
public class PreferenceInfo {
	public static String 	GROUP_ALG = "alg",
							GROUP_SYS = "sys",
							GROUP_DB = "db",
							GROUP_UI = "ui",
                            GROUP_LOG = "log",
							
							//algorithm
							KEY_DISTINCT_VALUE_COUNT = "distinct_value_count",
							KEY_VA_DISTINCT_VALUE_COUNT = "va_distinct_value_count",
							KEY_DECIMAL_PRECISION = "decimal_precision",
							KEY_DEBUG_LEVEL = "debug_level",
							
							//database
							KEY_CONNECTION_TIMEOUT = "connection_timeout",
							KEY_ADD_OUTPUTTABLE_PREFIX = "add_outputtable_prefix",
							KEY_LOCAL_HD_RUNNER_THRESHOLD = "hd_local_data_size_threshold",

                            //logging
                            KEY_LOG_CUST_ID = "cust_id",
                            KEY_LOG_OPT_OUT = "opt_out",

							//UI...
							KEY_MAX_TABLE_LINES = "max_table_lines",
							//KEY_LINES_PER_PAGE = "lines_per_page",
							KEY_MAX_SCATTER_POINTS = "max_scatter_points",
							KEY_MAX_TIMESERIES_POINTS = "max_timeseries_points" ,
							MAX_CLUSTER_POINTS = "max_cluster_points" ;
							
	
	String id;
	public String getId() {
		return id;
	}

	public PreferenceInfo(){
		
	}
	public PreferenceInfo(String id, Properties preferenceItems){
		this.id=id;
		this.preferenceItems = preferenceItems;
	}
	public void setId(String id) {
		this.id = id;
	}

	Properties preferenceItems;

 
	public Properties getPreferenceItems() {
		return preferenceItems;
	}

	public void setPreferenceItems(Properties preferenceItems) {
		this.preferenceItems = preferenceItems;
	}

	public void setProperty(String propkey, String value) {
		if(preferenceItems==null){
			preferenceItems= new Properties();
			
		}
		preferenceItems.put(propkey, value);
		
	}

}
