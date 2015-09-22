/**
 * ClassName HadoopAnalyticFileOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output.hadoop;


import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.utility.hadoop.HadoopConnection;
/**
 * @author Jeff Dong
 */

public class HadoopMultiAnalyticFileOutPut extends AbstractAnalyzerOutPut{
	private static final long serialVersionUID = 1L;
	private HadoopConnection hadoopConnection;
	private String outputFolder;
	private String[] outputFileNames;
	private List<String[]> outputFileSampleContents;
	
	public List<String[]> getOutputFileSampleContents() {
		return outputFileSampleContents;
	}
	public void setOutputFileSampleContents(List<String[]> outputFileSampleContents) {
		this.outputFileSampleContents = outputFileSampleContents;
	}
	public String getOutputFolder() {
		return outputFolder;
	}
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
	public String[] getOutputFileNames() {
		return outputFileNames;
	}
	public void setOutputFileNames(String[] outputFileNames) {
		this.outputFileNames = outputFileNames;
	}
	private AnalysisFileStructureModel hadoopFileStructureModel;
	private int startIndex = 0;
 


	public HadoopConnection getHadoopConnection() {
		return hadoopConnection;
	}
	public void setHadoopConnection(HadoopConnection hadoopConnection) {
		this.hadoopConnection = hadoopConnection;
	}
 
	public AnalysisFileStructureModel getHadoopFileStructureModel() {
		return hadoopFileStructureModel;
	}
	public void setHadoopFileStructureModel(
			AnalysisFileStructureModel hadoopFileStructureModel) {
		this.hadoopFileStructureModel = hadoopFileStructureModel;
	}
	public void setStartIndex(int index) {
		this.startIndex = index;
		
	}
	 
	public int getStartIndex(   ) {
		return this.startIndex  ;
		
	}
	 
 
}
