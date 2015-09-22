/**
 * ClassName AbstractAnalyticConfiguration.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import org.apache.log4j.Logger;
/**
 * @author John Zhao
 *
 */
public abstract class AbstractAnalyticConfig implements AnalyticConfiguration {
    private static final Logger itsLogger = Logger.getLogger(AbstractAnalyticConfig.class);
    protected Locale locale=Locale.getDefault();
	
	public static final String PARAMETER_COLUMN_NAMES = "columnNames";
	public static final String ConstOutputTableStorageParameters = "StorageParameters";

	//defualt no parameters
	protected List<String> parameterNames=null;
	//columnNames is like a1,a2,a3...
	//this is for runable, is not for configurable
 
	private String columnNames = null;
	private AnalysisStorageParameterModel StorageParameters;

	/**
	 * @return the columnNames
	 */
	public String getColumnNames() {
		return columnNames;
	}


	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

 
	public List<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	private String visualizationType;

	public void setVisualizationTypeClass(String visualizationType) {
		this.visualizationType = visualizationType;
	}

	public String getVisualizationTypeClass() {
		return visualizationType;
	}
	
	public Locale getLocale() {
		return locale;
	}


	public void setLocale(Locale locale) {
		this.locale = locale;
	}



	public AnalysisStorageParameterModel getStorageParameters() {
		return StorageParameters;
	}


	public void setStorageParameters(AnalysisStorageParameterModel storageParameters) {
		StorageParameters = storageParameters;
	}


	public String toString(){
		String blank="         ";	
		StringBuffer sb= new StringBuffer();
  		try {
			if(parameterNames!=null){
				for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
						
						String paramName = (String) iterator.next();
//						if(paramName!="columnNames"){
						String firstChar=  String.valueOf(paramName.charAt(0));
						String methodName="get"+firstChar.toUpperCase()+paramName.substring(1);
						Method method = this.getClass().getMethod(methodName, null);
						String paramValue="";
						if(method!=null){
							Object obj=method.invoke(this);
							if(obj!=null){
								paramValue = obj.toString();
							}
							sb.append(blank+paramName+" = "+paramValue+"\n");
						}
//					}
					}
				}
			} catch ( Exception e) {
				//dont log it ,no problem if the method is not found
//					e.printStackTrace();
			 	itsLogger.debug("getParameterMap  :"+e.getMessage());
			} 
				
		return sb.toString();
	}
	//param name-> param value
	public HashMap<String, String> getValueAsMap(){
		 HashMap<String, String> resultMap=new  HashMap<String, String>();
  		try {
			if(parameterNames!=null){
				for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
						String paramName = (String) iterator.next();
 
						String firstChar=  String.valueOf(paramName.charAt(0));
						String methodName="get"+firstChar.toUpperCase()+paramName.substring(1);
						Method method = this.getClass().getMethod(methodName, null);
						String paramValue="";
						if(method!=null){
							Object obj=method.invoke(this);
							if(obj!=null){
								paramValue = obj.toString();
							}
							resultMap.put(paramName, paramValue) ;
						}
					}
				}
			} catch ( Exception e) {
//					e.printStackTrace();
					itsLogger.debug("getParameterMap Error:"+e);
			} 
		return resultMap;
	}
	
	public void setValuesMap(HashMap<String, String> valueMap){
  		try {
			if(parameterNames!=null){
				for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
						String paramName = (String) iterator.next();
 
						String firstChar=  String.valueOf(paramName.charAt(0));
						String methodName="set"+firstChar.toUpperCase()+paramName.substring(1);
						Method method = this.getClass().getMethod(methodName, String.class);
						String paramValue=valueMap.get(paramName);
						if(method!=null){
							Object obj=method.invoke(this,paramValue);
							 
						}
					}
				}
			} catch ( Exception e) {
					e.printStackTrace();
					itsLogger.debug("getParameterMap Error:"+e);
			} 
		
	}
 
 
}
