/**
 * ClassName AbstractHadoopAnalyticConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticConfiguration;
import org.apache.log4j.Logger;
/**
 * @author Eason
 *
 */

public abstract class AbstractHadoopAnalyticConfig implements AnalyticConfiguration {
	protected Locale locale=Locale.getDefault();
    private static final Logger itsLogger=Logger.getLogger(AbstractHadoopAnalyticConfig.class);

    //defualt no parameters
	protected List<String> parameterNames=null;
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
	public String getColumnNames(){
		return null;
	}

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(String columnNames){
		;
	}
	
	public Locale getLocale() {
		return locale;
	}


	public void setLocale(Locale locale) {
		this.locale = locale;
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
					e.printStackTrace();
					itsLogger.debug("getParameterMap Error:"+e);
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
					e.printStackTrace();
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
