/**
 * ClassName VisuliazationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.Locale;


/**
 * @author John Zhao
 *
 */
public interface VisualizationType {
	String channel ="RCP";//Web
	String typeName ="Image";//Text, table(simple table, cluster table ,bar table), //
//	String algorithomType=""; //ROC, linera regression...
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut);

	public String getTypeName();
	public Locale getLocale() ;

	public void setLocale(Locale locale) ;
}
