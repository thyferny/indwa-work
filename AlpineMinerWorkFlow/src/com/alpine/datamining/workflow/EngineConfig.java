/**
 * ClassName  EngineConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-17
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author John Zhao
 *
 */
public class EngineConfig {
    private static final Logger itsLogger =Logger.getLogger(EngineConfig.class);
    public static EngineConfig instance = new EngineConfig();
	
	private int process_waiting_timeout=20000;//default value
	
	private int check_thread_period=100;//default value
	
	private int report_csv_table_max_rows=1000;
	
	private int max_process_instance=30;//default value
	
	public int getReport_csv_table_max_rows() {
		return report_csv_table_max_rows;
	}

	public void setReport_csv_table_max_rows(int reportCsvTableMaxRows) {
		report_csv_table_max_rows = reportCsvTableMaxRows;
	}

	public int getCheck_thread_period() {
		return check_thread_period;
	}

	public void setCheck_thread_period(int checkThreadPeriod) {
		check_thread_period = checkThreadPeriod;
	}

	public int getProcess_waiting_timeout() {
		return process_waiting_timeout;
	}

	public void setProcess_waiting_timeout(int processWaitingTimeout) {
		process_waiting_timeout = processWaitingTimeout;
	}

	public int getMax_process_instance() {
		return max_process_instance;
	}

	public void setMax_process_instance(int maxProcessInstance) {
		max_process_instance = maxProcessInstance;
	}


	
	private EngineConfig(){
	
		InputStream inStream=this.getClass().getResourceAsStream("engine.properties");
		Properties props=new Properties();
		try {
			props.load(inStream);
			process_waiting_timeout=Integer.parseInt(props.get("process_waiting_timeout").toString());
			max_process_instance=Integer.parseInt(props.get("max_process_instance").toString());
			report_csv_table_max_rows=Integer.parseInt(props.get("report_csv_table_max_rows").toString());
			check_thread_period=Integer.parseInt(props.get("check_thread_period").toString());
		} catch (IOException e) {
			itsLogger.error(e.getMessage(),e);
			itsLogger.error("Read properties file error:engine.properties");
		}
		
	}

}
