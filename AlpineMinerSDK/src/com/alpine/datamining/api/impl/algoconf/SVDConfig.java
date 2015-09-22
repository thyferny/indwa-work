/**
 * ClassName SVDConfig
 *
 * Version information: 1.00
 *
 * Data: 2011-1-4
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;


/**
 * Eason
 */
public class SVDConfig extends AbstractModelTrainerConfig{
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.SVDVisualizationType";
	private static final List<String> parameterNames = new ArrayList<String>();
	public static final String PARAMETER_DEPENDENT_COLUMN = "dependentColumn";
	
	public static final String ConstColName = "colName";
    public static final String ConstRowName = "rowName";
    public static final String ConstNumFeatures = "numFeatures";
    public static final String ConstOriginalStep = "originalStep";
    public static final String ConstSpeedupConst = "speedupConst";
    public static final String ConstFastSpeedupConst = "fastSpeedupConst";
    public static final String ConstSlowdownConst = "slowdownConst";
    public static final String ConstNumIterations = "numIterations";
    public static final String ConstMinNumIterations = "minNumIterations";
    public static final String ConstMinImprovement = "minImprovement";
    public static final String ConstImprovementReached = "improvementReached";
    public static final String ConstInitValue = "initValue";
    public static final String ConstEarlyTeminate = "earlyTeminate";

    public static final String ConstUmatrixTable = "UmatrixTable";
	public static final String ConstUmatrixSchema = "UmatrixSchema";
	public static final String ConstUmatrixDropIfExist = "UmatrixDropIfExist";

    public static final String ConstVmatrixTable = "VmatrixTable";
	public static final String ConstVmatrixSchema = "VmatrixSchema";
	public static final String ConstVmatrixDropIfExist = "VmatrixDropIfExist";


	static{
		parameterNames.add(PARAMETER_DEPENDENT_COLUMN);
		parameterNames.add(ConstColName);
		parameterNames.add(ConstRowName);
		parameterNames.add(ConstNumFeatures);
		parameterNames.add(ConstOriginalStep);
		parameterNames.add(ConstSpeedupConst);
		parameterNames.add(ConstFastSpeedupConst);
		parameterNames.add(ConstSlowdownConst);
		parameterNames.add(ConstNumIterations);
		parameterNames.add(ConstMinNumIterations);
		parameterNames.add(ConstMinImprovement);
		parameterNames.add(ConstImprovementReached);
		parameterNames.add(ConstInitValue);
		parameterNames.add(ConstEarlyTeminate);
		parameterNames.add(ConstUmatrixTable);
		parameterNames.add(ConstUmatrixSchema);
		parameterNames.add(ConstUmatrixDropIfExist);
		parameterNames.add(ConstVmatrixTable);
		parameterNames.add(ConstVmatrixSchema);
		parameterNames.add(ConstVmatrixDropIfExist);
		parameterNames.add(ConstForceRetrain);

	}
	private String dependentColumn = null;
	private String colName;
	private String rowName;
	private String numFeatures;
	private String originalStep;
	private String speedupConst;
	private String fastSpeedupConst;
	private String slowdownConst;
	private String numIterations;
	private String minNumIterations;
	private String minImprovement;
	private String improvementReached;
	private String initValue;
	private String earlyTeminate;
	private String UmatrixSchema;
	private String UmatrixTable;
	private String VmatrixSchema;
	private String VmatrixTable;
	private String UmatrixDropIfExist;
	private String VmatrixDropIfExist;

	
	public SVDConfig(){
		setParameterNames(parameterNames );
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}
	public SVDConfig(//String tableName,
			String columnNames,String dependentColumn){
		this(); 
		setColumnNames(columnNames);
		setDependentColumn ( dependentColumn);
	
	}

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	public String getRowName() {
		return rowName;
	}

	public void setRowName(String rowName) {
		this.rowName = rowName;
	}

	public String getNumFeatures() {
		return numFeatures;
	}

	public void setNumFeatures(String numFeatures) {
		this.numFeatures = numFeatures;
	}

	public String getOriginalStep() {
		return originalStep;
	}

	public void setOriginalStep(String originalStep) {
		this.originalStep = originalStep;
	}

	public String getSpeedupConst() {
		return speedupConst;
	}

	public void setSpeedupConst(String speedupConst) {
		this.speedupConst = speedupConst;
	}

	public String getFastSpeedupConst() {
		return fastSpeedupConst;
	}

	public void setFastSpeedupConst(String fastSpeedupConst) {
		this.fastSpeedupConst = fastSpeedupConst;
	}

	public String getSlowdownConst() {
		return slowdownConst;
	}

	public void setSlowdownConst(String slowdownConst) {
		this.slowdownConst = slowdownConst;
	}

	public String getNumIterations() {
		return numIterations;
	}

	public void setNumIterations(String numIterations) {
		this.numIterations = numIterations;
	}

	public String getMinNumIterations() {
		return minNumIterations;
	}

	public void setMinNumIterations(String minNumIterations) {
		this.minNumIterations = minNumIterations;
	}

	public String getMinImprovement() {
		return minImprovement;
	}

	public void setMinImprovement(String minImprovement) {
		this.minImprovement = minImprovement;
	}

	public String getImprovementReached() {
		return improvementReached;
	}

	public void setImprovementReached(String improvementReached) {
		this.improvementReached = improvementReached;
	}

	public String getInitValue() {
		return initValue;
	}

	public void setInitValue(String initValue) {
		this.initValue = initValue;
	}

	public String getEarlyTeminate() {
		return earlyTeminate;
	}

	public void setEarlyTeminate(String earlyTeminate) {
		this.earlyTeminate = earlyTeminate;
	}

	public String getUmatrixSchema() {
		return UmatrixSchema;
	}
	public void setUmatrixSchema(String umatrixSchema) {
		UmatrixSchema = umatrixSchema;
	}
	public String getUmatrixTable() {
		return UmatrixTable;
	}
	public void setUmatrixTable(String umatrixTable) {
		UmatrixTable = umatrixTable;
	}
	public String getVmatrixSchema() {
		return VmatrixSchema;
	}
	public void setVmatrixSchema(String vmatrixSchema) {
		VmatrixSchema = vmatrixSchema;
	}
	public String getVmatrixTable() {
		return VmatrixTable;
	}
	public void setVmatrixTable(String vmatrixTable) {
		VmatrixTable = vmatrixTable;
	}
	public String getUmatrixDropIfExist() {
		return UmatrixDropIfExist;
	}
	public void setUmatrixDropIfExist(String umatrixDropIfExist) {
		UmatrixDropIfExist = umatrixDropIfExist;
	}
	public String getVmatrixDropIfExist() {
		return VmatrixDropIfExist;
	}
	public void setVmatrixDropIfExist(String vmatrixDropIfExist) {
		VmatrixDropIfExist = vmatrixDropIfExist;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}

}
