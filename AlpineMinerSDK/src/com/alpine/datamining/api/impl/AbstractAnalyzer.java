/**
 * ClassName AbstractModelTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.utility.db.Resources;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;


/**
 * @author John Zhao
 *
 */
public abstract class AbstractAnalyzer implements DataAnalyzer {
	private static final String ANALYZER_VERSION = Resources.minerEdition;
	public static final String COLUMN_SEPRATOR = ",";

	private AnalyticSource analyticSource;
	private AnalyticOutPut outPut;
	private String name;
	private String theUUID;
	private String flowRunUUID;
	public String getFlowRunUUID(){
		return this.flowRunUUID;
	}
	
	public String getUUID() {
		return theUUID;
	}
	public void setFlowRunUUID(String uUID) {
		this.flowRunUUID = uUID;
	}

	public void setUUID(String uUID) {
        theUUID = uUID;
	}

	List<AnalyticProcessListener> listeners;
	private AnalyticContext context;
	private boolean stop =false;
	
	public List<AnalyticProcessListener> getListeners() {
		return listeners;
	}


	public void setListeners(List<AnalyticProcessListener> listeners) {
		this.listeners = listeners;
	}


	public String getName() {
		return name;
	}

	public void addListener(AnalyticProcessListener listener){
		if(listeners==null){
			listeners=new ArrayList<AnalyticProcessListener>();
		}
		listeners.add(listener) ;
	}
	
	public void setName(String name) {
		this.name = name;
	}


	public void setOutPut(AnalyticOutPut outPut) {
		this.outPut = outPut;
	}


	public AnalyticOutPut getOutPut(){
		return outPut;
	}
	
	
	public void setAnalyticSource(AnalyticSource analyticSource) {
		this.analyticSource = analyticSource;
	}
	//will do it later
	public String getVersion(){
		return ANALYZER_VERSION;
	}
	public AnalyticSource getAnalyticSource(){
		return 	  analyticSource  ;
	}
 
	public void setContext(AnalyticContext context){
		this.context=context;
		context.setCurrenctAnalyzer(this) ;
	}

	public AnalyticContext getContext() {
		return context;
	}

	/**
	 * @return
	 */
	protected String getQuotaedTableName(String schema, String table) {
		String outputTableName;
		
		if (schema!=null&&schema.trim().length()>0) {
			schema=StringHandler.doubleQ(schema);
			
			outputTableName = schema+"."+StringHandler.doubleQ(table);
		} else {
			outputTableName =StringHandler.doubleQ(table);
		}
		return outputTableName;
	}

	public void stop(){
		this.stop  = true;
	}
	
	public boolean isStop(){
		return this.stop;
	}

	
}
