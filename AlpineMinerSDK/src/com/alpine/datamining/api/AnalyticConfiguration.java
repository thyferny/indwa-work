/**
 * ClassName AnnalysiticConfiguration.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * @author John Zhao
 *
 */
public interface AnalyticConfiguration {
	
	public String getVisualizationTypeClass();
	public void setVisualizationTypeClass(String visualizationType) ;
//	public VisualizationType getDefaultVisualizationType();
	/**
	 * @return
	 */
	
	public List<String> getParameterNames() ;

	public void setParameterNames(List<String> parameterNames) ;
	
	
	public String getColumnNames() ;

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(String columnNames);
	
	public HashMap<String, String> getValueAsMap();
	
	public void setValuesMap(HashMap<String, String> valueMap);
	
	public Locale getLocale();
	public void setLocale(Locale locale);
	
}
