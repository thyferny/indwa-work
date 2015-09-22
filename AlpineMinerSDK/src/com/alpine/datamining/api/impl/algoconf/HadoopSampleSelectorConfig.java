/**
 * ClassName HadoopSampleSelectorConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Jeff Dong
 *
 */
public class HadoopSampleSelectorConfig extends HadoopDataOperationConfig {
	public static final String HD_HADOOP_SAMPLING_VISUALIZATIONCLASS="com.alpine.datamining.api.impl.visual.DataOperationSampleVisualizationType";

	private String selectedFile;
	
	private static final String ConstSelectedTable = "selectedFile";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstSelectedTable);		
	}
	
	public HadoopSampleSelectorConfig() {
		setParameterNames(parameters);
	}

	public String getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
	}
	@Override
	public String getVisualizationTypeClass() {
		return HD_HADOOP_SAMPLING_VISUALIZATIONCLASS;
	}
}
