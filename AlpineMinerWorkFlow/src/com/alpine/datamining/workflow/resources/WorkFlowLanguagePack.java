/**
 * ClassName  WorkFlowLanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-8
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author John Zhao
 *
 */
public class WorkFlowLanguagePack {
	
	 
	
	private static final String Bundle_Name = "com.alpine.datamining.workflow.resources.language";
	
	
	
	public static final List<Locale> Supported_Locales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	public static final Locale Default_Locale = Locale.ENGLISH; 

	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();

	static{
		
		for(int i = 0;i<Supported_Locales.size();i++){
			Locale locale = Supported_Locales.get(i);
			 ResourceBundle rb =null;
				if(locale==Locale.US){
					   rb = ResourceBundle.getBundle(Bundle_Name,Locale.ENGLISH  );
				} else if(locale==Locale.CHINA){
					   rb = ResourceBundle.getBundle(Bundle_Name,Locale.CHINESE);
				}else if(locale==Locale.JAPAN){
					   rb = ResourceBundle.getBundle(Bundle_Name,Locale.JAPANESE);
				}
			else{
			   rb = ResourceBundle.getBundle(Bundle_Name,locale  );
			 }
			 resourceMap.put(locale, rb); 
			 
		}
		
	}
	
	public static String getMessage(String key, Locale locale){
 		if(Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale) ;
 		if(rb!=null){
 			return rb.getString(key);
 		}else{
 			return "";
 		}
 	}
 
	
 
	public static final String RPT_OverView = "OVERVIEW";
	public static final String RPT_Overview_chapter ="RPT_OVERVIEW_CHAPTER";
	
	
	
	public static final String Invalid_License_File = "INVALID_LICENSE_FILE";
	public static final String Invalid_Flow_File = "INVALID_FLOW_FILE";
	public static final String Flow_File_NotExist = "FLOW_FILE_NOTEXIST";
	public static final String SubFlow_File_NotExist = "SUB_FLOW_FILE_NOTEXIST";
	public static final String Please_input_Flow_File = "PLEASE_INPUT_FLOW_FILE";

	
	public static final String AnalyticResultExporter_Node="ANALYTICRESULTEXPORTER_NODE"; 
	public static final String AnalyticResultExporter_Algorithm_Description="ANALYTICRESULTEXPORTER_ALGORITHM_DESCRIPTION";
	public static final String AnalyticResultExporter_Algorithm_name="ANALYTICRESULTEXPORTER_ALGORITHM_NAME";
	public static final String AnalyticResultExporter_CopyRight="ANALYTICRESULTEXPORTER_COPYRIGHT";
	public static final String AnalyticResultExporter_End_to="ANALYTICRESULTEXPORTER_END_TO";
		
	public static final String AnalyticResultExporter_Engine_Name="ANALYTICRESULTEXPORTER_ENGINE_NAME";
	public static final String AnalyticResultExporter_Execute_User="ANALYTICRESULTEXPORTER_EXECUTE_USER";
	public static final String AnalyticResultExporter_Flow_Owner="ANALYTICRESULTEXPORTER_FLOW";
	public static final String AnalyticResultExporter_Flow_Description="ANALYTICRESULTEXPORTER_FLOW_DESCRIPTION";
	public static final String AnalyticResultExporter_Flow_Name="ANALYTICRESULTEXPORTER_FLOW_NAME";
	public static final String AnalyticResultExporter_Input="ANALYTICRESULTEXPORTER_INPUT";
		
	public static final String AnalyticResultExporter_Node_name="ANALYTICRESULTEXPORTER_NODE_NAME";
	public static final String AnalyticResultExporter_OutPut="ANALYTICRESULTEXPORTER_OUTPUT";
	public static final String AnalyticResultExporter_OverView="ANALYTICRESULTEXPORTER_OVERVIEW";
	
	public static final String AnalyticResultExporter_Param_information="ANALYTICRESULTEXPORTER_PARAM_INFORMATION";
	public static final String AnalyticResultExporter_Server_Configuration="ANALYTICRESULTEXPORTER_SERVER_CONFIGURATION";
	public static final String AnalyticResultExporter_Slected_Column="ANALYTICRESULTEXPORTER_SELECTED_COLUMN";
	
	public static final String AnalyticResultExporter_Source_Information="ANALYTICRESULTEXPORTER_SOURCE_INFORMATION";
	public static final String AnalyticResultExporter_Start_From="ANALYTICRESULTEXPORTER_START_FROM";
	public static final String Output_Nodes_Description = "OUTPUT_NODES_DESCRIPTION";
	public static final String Output_Image = "OUTPUT_IMAGE";
	public static final String Layer_Description =  "LAYER_DESCRIPTION";
	public static final String Parent_Node =  "PARENT_NODE";
	public static final String Link_Condition = "LINK_CONDITION";
	public static final String Child_Node = "CHILD_NODE";
	public static final String Node_Name = "NODE_NAME";
	public static final String Node_Description = "NODE_DESCRIPTION";
	
	public static final String Analytic_Flow_Finished = "ANALYTIC_FLOW_FINISHED";	
	public static final String Analytic_Node_Finished = "ANALYTIC_NODE_FINISHED";
	public static final String Analytic_Error_Hanppens = "ANALYTIC_ERROR_HANPPENS";
	public static final String Analytic_Message = "ANALYTIC_MESSAGE";
	public static final String Analytic_Node_Started = "ANALYTIC_NODE_STARTED";
	public static final String AnalyticFLow_Stoped = "ANALYTICFLOW_STOPED";
	public static final String Too_Many_Columns = "TOO_MANY_COLUMNS";
	public static final String Too_Many_Rows ="TOO_MANY_ROWS";
	public static final String See_Table_in_Database ="SEE_TABLE_IN_DATABASE";
	public static final String See_Table_in_WorkBench ="SEE_TABLE_IN_WORKBENCH";
	public static final String Output_Image_3Layer = "OUTPUT_IMAGE_3LAYER";
	public static final String Columns_numbers =  "COLUMNS_NUMBERS";
	public static final String Row_numbers =  "ROW_NUMBERS";
	public static final String Table_Name = "TABLE_NAME";
	public static final String Result_already_exists = "RESULT_ALREADY_EXISTS";
	public static final String CSVTable_OVerView = "CSVTABLE_OVERVIEW";
	public static final String CSVFile_OVerView = "CSVFILE_OVERVIEW";
	public static final String Table_Columns_From = "TABLE_COLUMNS_FROM";
	public static final String Table_Columns_To = "TABLE_COLUMNS_TO";
	public static final String QUANTILE_COLUMN_NAME = "QUANTILE_COLUMN_NAME";
	public static final String NO_OF_BIN = "NO_OF_BIN";
	public static final String QUANTILE_TYPE = "QUANTILE_TYPE";
	public static final String CREATE_NEW_COLUMN = "CREATE_NEW_COLUMN";
	public static final String BIN = "BIN";
	public static final String TYPE_CUSTIMZE_LABEL = "TYPE_CUSTIMZE_LABEL";
	public static final String TYPE_AVG_ASC_LABEL = "TYPE_AVG_ASC_LABEL";
	public static final String JOIN_TABLE_NAME = "JOIN_TABLE_NAME";
	public static final String JOIN_SCHEMA_NAME = "JOIN_SCHEMA_NAME";
	public static final String JOIN_ALIAS = "JOIN_ALIAS";
	public static final String JOIN_COLUMN_NAME = "JOIN_COLUMN_NAME";
	public static final String JOIN_NEW_COLUMN_NAME = "JOIN_NEW_COLUMN_NAME";
	public static final String JOIN_NEW_COLUMN_TYPE = "JOIN_NEW_COLUMN_TYPE";
	public static final String JOIN_TABLE_RIGHT = "JOIN_TABLE_RIGHT";
	public static final String JOIN_TABLE_LEFT = "JOIN_TABLE_LEFT";
	public static final String JOIN_TYPE = "JOIN_TYPE";
	public static final String JOIN_CONDITION = "JOIN_CONDITION";
	public static final String JOIN_TABLE_INFO = "JOIN_TABLE_INFO";
	public static final String JOIN_COLUMN_INFO = "JOIN_COLUMN_INFO";
	public static final String JOIN_CONDITION_INFO = "JOIN_CONDITION_INFO";
	
	public static final String SPLITMODEL_TOOMANYGROUP_WARNING = "SPLITMODEL_TOOMANYGROUP_WARNING";
 


	public static final String TableSet_Source_Info = "TableSet_Source_Info";
	public static final String  TableSet_Column_Info = "TableSet_Column_Info";
	
	public static final String Model_Saved = "Model_Saved";
}
