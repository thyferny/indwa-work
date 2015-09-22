/**
 * ClassName VisualizationModel.java
 *
 * Version information: 1.00
 *
 * Author john zhao
 * 
 * Data: 2011-7-11
 *	@author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;

/**
 * VisualizationModel is a pure java model can be visualized by any library like Dojo,JfreeChart.
 * It should be the output of the operator and should not dependent any other libs.
 * */


public interface VisualizationModel {
	
	public static final String version ="3.0" ;
	
		
	public static final int TYPE_UNKNOW=99;

	public static final int TYPE_DATATABLE = 0;
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_BAR_CHART = 2;
	public static final int TYPE_TREE = 3;
	public static final int TYPE_NETWORK = 4;
	public static final int TYPE_CLUSTER = 5;
	//multiple tabs
	public static final int TYPE_COMPOSITE=6;
	public static final int TYPE_TABLE_GROUPED=14;
	//this used for cluster profileing table...
	//if no number means the legned
	public static final int TYPE_PIECHART=15;
	//in one tab ,switch by combo
	public static final int TYPE_LAYERED=7;
	
	public static final int TYPE_POINT_CHART = 8;
	
	public static final int TYPE_LINE_CHART = 9;
	
	public static final int TYPE_SCATTER_CHART = 10;
	
	//public static final int  TYPE_CLUSTERPROFILE_CHART=13;
	
	public static final int TYPE_BOXANDWHISKER=16;
	public static final int  TYPE_CLUSRTER_CHART=19;
	public static final int TYPE_EMPTY = 17;


	public static final int TYPE_CHART = 18; 

	public int getVisualizationType();
	public void setVisualizationType(int type);
	
	//mostly the tile is used for the tab title in the result page 
	public String getTitle();
	public void setTitle(String title);
	
	//when in the result, one error can not block the others,
	//so if has error, the adapter will only return the error message
	public boolean isError();
	public void setError(boolean error);
	public List<String> getErrorMessage();
	public void setErrorMessage(List<String> errorMessages);
	public void addErrorMessage(String errorMessage);	
//	is error and error message...
//	like too many bars, too many points...
	public void setAnalyticNodeMetaInfo(AnalyticNodeMetaInfo analyticNodeMetaInfo);
	
	public AnalyticNodeMetaInfo getAnalyticNodeMetaInfo(  ); 
	
	//for report use only...
	public void setOpeatorInputs(String[][] operatorInputs);
	public String[][] getOpeatorInputs(  ); 
	//some output may need not be counted in report. 
	public void setNeedGenerateReport(boolean need) ;
	public boolean isNeedGenerateReport() ;
	

}
