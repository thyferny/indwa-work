/**
 * ClassName AnalysisHadoopFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-26
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop.filestructure;

import java.util.List;

/**
 * @author Jeff Dong
 *
 */
public interface AnalysisFileStructureModel {

	public static final String ATTR_INCLUDEHEADER = "includeHeader";
 	
	public static final String ATTR_COLUMNNAME = "columnName";

	public static final String COLUMNNAMES_TAG_NAME = "columnNames";
	
	public static final String ATTR_COLUMNTYPE = "columnType";

	public static final String COLUMNTYPES_TAG_NAME = "columnTypes";
	
	//true false
		public String getIsFirstLineHeader();
		public void setIsFirstLineHeader(String isFirstLineHeader) ;
		public List<String> getColumnNameList();
		public void setColumnNameList(List<String> columnNameList);
		public List<String> getColumnTypeList();
		public void setColumnTypeList(List<String> columnTypeList);
		
	public 	  AnalysisFileStructureModel clone() throws CloneNotSupportedException;
}
