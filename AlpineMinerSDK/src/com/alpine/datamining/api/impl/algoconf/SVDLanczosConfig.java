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
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;


/**
 * Eason
 */
public class SVDLanczosConfig extends AbstractModelTrainerConfig{
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.SVDVisualizationType";
	private static final List<String> parameterNames = new ArrayList<String>();
	public static final String PARAMETER_DEPENDENT_COLUMN = "dependentColumn";
	
	public static final String ConstColName = "colName";
    public static final String ConstRowName = "rowName";
    public static final String ConstNumFeatures = "numFeatures";

    public static final String ConstUmatrixTable = "UmatrixTable";
	public static final String ConstUmatrixSchema = "UmatrixSchema";
	public static final String ConstUmatrixDropIfExist = "UmatrixDropIfExist";
	public static final String ConstUmatrixTableStorageParams="UmatrixTableStorageParameters";

    public static final String ConstVmatrixTable = "VmatrixTable";
	public static final String ConstVmatrixSchema = "VmatrixSchema";
	public static final String ConstVmatrixDropIfExist = "VmatrixDropIfExist";
	public static final String ConstVmatrixTableStorageParams="VmatrixTableStorageParameters";

    public static final String ConstSingularValueTable = "singularValueTable";
	public static final String ConstSingularValueSchema = "singularValueSchema";
	public static final String ConstSingularValueDropIfExist = "singularValueDropIfExist";
	public static final String ConstSingularValueTableStorageParams="singularValueTableStorageParameters";


	static{
		parameterNames.add(PARAMETER_DEPENDENT_COLUMN);
		parameterNames.add(ConstForceRetrain);
		parameterNames.add(ConstColName);
		parameterNames.add(ConstRowName);
		parameterNames.add(ConstNumFeatures);
		parameterNames.add(ConstUmatrixTable);
		parameterNames.add(ConstUmatrixSchema);
		parameterNames.add(ConstUmatrixDropIfExist);
		parameterNames.add(ConstUmatrixTableStorageParams);
		parameterNames.add(ConstVmatrixTable);
		parameterNames.add(ConstVmatrixSchema);
		parameterNames.add(ConstVmatrixDropIfExist);
		parameterNames.add(ConstVmatrixTableStorageParams);
		parameterNames.add(ConstSingularValueTable);
		parameterNames.add(ConstSingularValueSchema);
		parameterNames.add(ConstSingularValueDropIfExist);
		parameterNames.add(ConstSingularValueTableStorageParams);

	}
	private String dependentColumn = null;
	private String colName;
	private String rowName;
	private String numFeatures;
	private String UmatrixSchema;
	private String UmatrixTable;
	private String VmatrixSchema;
	private String VmatrixTable;
	private String UmatrixDropIfExist;
	private String VmatrixDropIfExist;

	private AnalysisStorageParameterModel UmatrixTableStorageParameters;
	private AnalysisStorageParameterModel VmatrixTableStorageParameters;
	private AnalysisStorageParameterModel singularValueTableStorageParameters;
	
	private String singularValueSchema;
	private String singularValueTable;
	private String singularValueDropIfExist;
	
	
	
	public SVDLanczosConfig(){
		setParameterNames(parameterNames );
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}
	public SVDLanczosConfig(//String tableName,
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
	public String getSingularValueSchema() {
		return singularValueSchema;
	}
	public void setSingularValueSchema(String singularValueSchema) {
		this.singularValueSchema = singularValueSchema;
	}
	public String getSingularValueTable() {
		return singularValueTable;
	}
	public void setSingularValueTable(String singularValueTable) {
		this.singularValueTable = singularValueTable;
	}
	public String getSingularValueDropIfExist() {
		return singularValueDropIfExist;
	}
	public void setSingularValueDropIfExist(String singularValueDropIfExist) {
		this.singularValueDropIfExist = singularValueDropIfExist;
	}
	public AnalysisStorageParameterModel getUmatrixTableStorageParameters() {
		return UmatrixTableStorageParameters;
	}
	public void setUmatrixTableStorageParameters(
			AnalysisStorageParameterModel umatrixTableStorageParameters) {
		UmatrixTableStorageParameters = umatrixTableStorageParameters;
	}
	public AnalysisStorageParameterModel getVmatrixTableStorageParameters() {
		return VmatrixTableStorageParameters;
	}
	public void setVmatrixTableStorageParameters(
			AnalysisStorageParameterModel vmatrixTableStorageParameters) {
		VmatrixTableStorageParameters = vmatrixTableStorageParameters;
	}
	public AnalysisStorageParameterModel getSingularValueTableStorageParameters() {
		return singularValueTableStorageParameters;
	}
	public void setSingularValueTableStorageParameters(
			AnalysisStorageParameterModel singularValueTableStorageParameters) {
		this.singularValueTableStorageParameters = singularValueTableStorageParameters;
	}

}
