/**
 * ClassName LogisticRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.association.AnalysisExpressionModel;




/**
 * @author John Zhao
 * 
 */
public class FPGrowthConfig extends AbstractAssociationConfig {
 
	public static final String ConstMinSupport = "minSupport";
	public static final String ConstTableSizeThreshold = "tableSizeThreshold";
	public static final String ConstMinConfidence = "minConfidence";
	public static final String ConstOutputTable = "outputTable";
	public static final String ConstOutputSchema = "outputSchema";
	public static final String ConstUseArray = "useArray";
	public static final String ConstDropIfExist = "dropIfExist";

	private String outputSchema;
	private String outputTable;
	private String dropIfExist;
	//ruleCriterion
	
	private static final List<String> parameterNames = new ArrayList<String>();
	static{
		parameterNames.add(ConstMinSupport);
		parameterNames.add(ConstTableSizeThreshold);
		parameterNames.add(ConstMinConfidence);
		parameterNames.add(PARAMETER_COLUMN_NAMES);
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstUseArray);
		parameterNames.add(ConstOutputTableStorageParameters);
	}


	/**
	 * @param tableName
	 */
	public FPGrowthConfig( String columnnames, String minSupport,
			String tableSizeThreshold,String ruleMinConfidence,String ruleCriterion
			) {
	 		super( ruleMinConfidence,ruleCriterion);
		
		this.minSupport = minSupport;
			
		this.tableSizeThreshold = tableSizeThreshold;
		
		setColumnNames(columnnames);
		setParameterNames(parameterNames );
	
		setVisualizationTypeClass(
		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");
	}
	public FPGrowthConfig(){
		super();
		setParameterNames(parameterNames );
		setVisualizationTypeClass(
		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");
	}
 
	private String minSupport = "";
	
	private AnalysisExpressionModel expressionModel;
	
	private String tableSizeThreshold = "";
	
	private String useArray="";

	public String getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(String minSupport) {
		this.minSupport = minSupport;
	}

	public String getTableSizeThreshold() {
		return tableSizeThreshold;
	}

	public void setTableSizeThreshold(String tableSizeThreshold) {
		this.tableSizeThreshold = tableSizeThreshold;
	}
	public String getOutputSchema() {
		return outputSchema;
	}
	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}
	public String getOutputTable() {
		return outputTable;
	}
	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}
	public String getDropIfExist() {
		return dropIfExist;
	}
	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}
	public String getUseArray() {
		return useArray;
	}
	public void setUseArray(String useArray) {
		this.useArray = useArray;
	}
	public AnalysisExpressionModel getExpressionModel() {
		return expressionModel;
	}
	public void setExpressionModel(AnalysisExpressionModel expressionModel) {
		this.expressionModel = expressionModel;
	}
	
}
