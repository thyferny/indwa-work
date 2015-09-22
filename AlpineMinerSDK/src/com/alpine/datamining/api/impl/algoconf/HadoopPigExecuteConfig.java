/**
 * ClassName HadoopPigExecuteConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-9
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;

/**
 * @author Jeff Dong
 *
 */
public class HadoopPigExecuteConfig extends HadoopDataOperationConfig {

	public static final String ConstPigScriptModel ="pigScript";
	public static final String NAME_HD_fileStructure ="pigExecuteFileStructure";
	
	private AnalysisFileStructureModel hadoopFileStructure;
	private AnalysisPigExecutableModel pigScriptModel; 
	
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	public HadoopPigExecuteConfig() {
		super();
		setParameterNames(parameters);
	}
	
	static{
//		parameters.add(ConstPigScriptModel);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);
//		parameters.add(NAME_HD_fileStructure);	
	}
	
	public AnalysisFileStructureModel getHadoopFileStructure() {
		return hadoopFileStructure;
	}
	
	public AnalysisFileStructureModel getPigExecuteFileStructure(){
		return hadoopFileStructure;
	}
	
	public void setHadoopFileStructure(
			AnalysisFileStructureModel hadoopFileStructure) {
		this.hadoopFileStructure = hadoopFileStructure;
	}
	
	public AnalysisPigExecutableModel getPigScriptModel() {
		return pigScriptModel;
	}
	
	public void setPigScript(AnalysisPigExecutableModel pigScriptModel) {
		this.pigScriptModel = pigScriptModel;
	}
 
	
}
