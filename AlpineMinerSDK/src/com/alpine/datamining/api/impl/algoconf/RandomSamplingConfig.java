/**
 * ClassName RandomSamplingConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 8, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.db.attribute.model.sampling.AnalysisSampleSizeModel;

/**
 * @author Richie Lo
 *
 */
public class RandomSamplingConfig extends AbstractAnalyticConfig {

	private String sampleCount;
	private String sampleSizeType;
	private AnalysisSampleSizeModel sampleSize;
	private String randomSeed;
	private String outputType;
	private String outputSchema;
	private String outputTablePrefix;
	private String dropIfExist;
	private String keyColumnList;
	private String consistent;
	private String replacement;
	private String disjoint;
//	private HashMap<String,Double> sampleSizeMap=new HashMap<String,Double>();


	protected static final String ConstSampleCount = "sampleCount";
	protected static final String ConstSampleSizeType = "sampleSizeType";
	protected static final String ConstSampleSize = "sampleSize";
	protected static final String ConstRandomSeed = "randomSeed";
	protected static final String ConstOutputType = "outputType";
	protected static final String ConstOutputSchema = "outputSchema";
	protected static final String ConstOutputTablePrefix = "outputTable";
	protected static final String ConstDropIfExist = "dropIfExist";
	protected static final String ConstKeyColumnList = "keyColumnList";
	protected static final String ConstConsistent = "consistent";
	protected static final String ConstReplacement = "replacement";
	protected static final String ConstDisjoint = "disjoint";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstSampleCount);
		parameters.add(ConstSampleSizeType);
		parameters.add(ConstSampleSize);
		parameters.add(ConstRandomSeed);
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTablePrefix);
		parameters.add(ConstOutputTableStorageParameters);
		parameters.add(ConstKeyColumnList);
		parameters.add(ConstConsistent);
		parameters.add(ConstReplacement);
		parameters.add(ConstDisjoint);
	}

	public RandomSamplingConfig(String outputType, 
			String outputSchema, String outputTablePrefix, String dropIfExist) {
		this();
		this.setOutputType(outputType);
		this.setOutputSchema(outputSchema);
		this.setOutputTable(outputTablePrefix);
		this.setDropIfExist(dropIfExist);
 
	}
	
	public RandomSamplingConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DataOperationSampleVisualizationType");
	}
	
	public void setSampleCount(String sampleCount) {
		this.sampleCount = sampleCount;
	}
	
	public String getSampleCount() {
		return sampleCount;
	}

	public void setSampleSizeType(String sampleSizeType) {
		this.sampleSizeType = sampleSizeType;
	}

	public String getSampleSizeType() {
		return sampleSizeType;
	}

	public void setSampleSize(AnalysisSampleSizeModel sampleSize) {
		this.sampleSize = sampleSize;
	}

	public AnalysisSampleSizeModel getSampleSize() {
		return sampleSize;
	}

	public void setRandomSeed(String randomSeed) {
		this.randomSeed = randomSeed;
	}

	public String getRandomSeed() {
		return randomSeed;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getOutputSchema() {
		return outputSchema;
	}

	public void setOutputTable(String outputTablePrefix) {
		this.outputTablePrefix = outputTablePrefix;
	}

	public String getOutputTable() {
		return outputTablePrefix;
	}

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}

	public String getDropIfExist() {
		return dropIfExist;
	}
	public String getKeyColumnList() {
		return keyColumnList;
	}

	public void setKeyColumnList(String keyColumnList) {
		this.keyColumnList = keyColumnList;
	}
	public String getConsistent() {
		return consistent;
	}

	public void setConsistent(String consistent) {
		this.consistent = consistent;
	}
	
	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
	public String getDisjoint() {
		return disjoint;
	}

	public void setDisjoint(String disjoint) {
		this.disjoint = disjoint;
	}
}
