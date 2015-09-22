/**
 * ClassName ProductRecommendationAnalyser.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

/**
 * @author Eason
 */
public class KNNTrainConfig  extends AbstractHadoopModelTrainerConfig{

	private String inputFile;
	private String outputDir;
	private String dropIfExists;
	
	public static final String ConstDropIfExist = "dropIfExist";

	private String dropIfExist;
	public String getDropIfExist() {
		return dropIfExist;
	}

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}
 	
	public KNNTrainConfig() {
	}

	public String getInputFile() {
		return inputFile;
	}


	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getDropIfExists() {
		return dropIfExists;
	}


	public void setDropIfExists(String dropIfExists) {
		this.dropIfExists = dropIfExists;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

}
