/**
 * ClassName SVDPredictorConfig.java
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

import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author Eason
 * 
 */
public class SVDLanczosCalculatorConfig extends PredictorConfig{
	private static final List<String> parameterNames = new ArrayList<String>();
	
	public static final String ConstUdependentColumn = "UdependentColumnF";
    public static final String ConstVdependentColumn = "VdependentColumnF";
    public static final String ConstSingularValuedependentColumn = "singularValuedependentColumnF";
	public static final String ConstUfeatureColumn = "UfeatureColumn";
    public static final String ConstVfeatureColumn = "VfeatureColumn";
    public static final String ConstSingularValuefeatureColumn = "singularValuefeatureColumn";
	public static final String ConstColName = "colNameF";
    public static final String ConstRowName = "rowNameF";
    public static final String ConstUmatrixTable = "UmatrixTableF";
    public static final String ConstVmatrixTable = "VmatrixTableF";
    public static final String ConstSingularValueTable = "singularValueTableF";
	public static final String ConstCrossProduct = "crossProduct";
    public static final String ConstKeyColumn = "keyColumn";
    public static final String ConstKeyValue = "keyValue";

    static{
		parameterNames.add(ConstUdependentColumn);
		parameterNames.add(ConstVdependentColumn);
		parameterNames.add(ConstSingularValuedependentColumn);
		parameterNames.add(ConstUfeatureColumn);
		parameterNames.add(ConstVfeatureColumn);
		parameterNames.add(ConstSingularValuefeatureColumn);
		parameterNames.add(ConstColName);
		parameterNames.add(ConstRowName);
		parameterNames.add(ConstUmatrixTable);
		parameterNames.add(ConstVmatrixTable);
		parameterNames.add(ConstCrossProduct);
		parameterNames.add(ConstKeyColumn);
		parameterNames.add(ConstKeyValue);
		parameterNames.add(ConstSingularValueTable);
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);
	}
	private String UdependentColumnF;
	private String VdependentColumnF;
	private String singularValuedependentColumnF;
	private String UfeatureColumn;
	private String VfeatureColumn;
	private String singularValuefeatureColumn;
	private String colNameF;
	private String rowNameF;
	private String UmatrixTableF;
	private String VmatrixTableF;
	private String singularValueTableF;
	private String crossProduct;
	private String keyColumn;
	private String keyValue;

	public SVDLanczosCalculatorConfig(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}

	public SVDLanczosCalculatorConfig() {
		setParameterNames(parameters);
		setParameterNames(parameterNames);
	}



	public String getUfeatureColumn() {
		return UfeatureColumn;
	}

	public void setUfeatureColumn(String ufeatureColumn) {
		UfeatureColumn = ufeatureColumn;
	}

	public String getVfeatureColumn() {
		return VfeatureColumn;
	}

	public void setVfeatureColumn(String vfeatureColumn) {
		VfeatureColumn = vfeatureColumn;
	}

	public String getCrossProduct() {
		return crossProduct;
	}

	public void setCrossProduct(String crossProduct) {
		this.crossProduct = crossProduct;
	}
 
	public String getKeyColumn() {
		return keyColumn;
	}

	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getUdependentColumnF() {
		return UdependentColumnF;
	}

	public void setUdependentColumnF(String udependentColumnF) {
		UdependentColumnF = udependentColumnF;
	}

	public String getVdependentColumnF() {
		return VdependentColumnF;
	}

	public void setVdependentColumnF(String vdependentColumnF) {
		VdependentColumnF = vdependentColumnF;
	}

	public String getSingularValuedependentColumnF() {
		return singularValuedependentColumnF;
	}

	public void setSingularValuedependentColumnF(
			String singularValuedependentColumnF) {
		this.singularValuedependentColumnF = singularValuedependentColumnF;
	}

	public String getSingularValuefeatureColumn() {
		return singularValuefeatureColumn;
	}

	public void setSingularValuefeatureColumn(String singularValuefeatureColumn) {
		this.singularValuefeatureColumn = singularValuefeatureColumn;
	}

	public String getColNameF() {
		return colNameF;
	}

	public void setColNameF(String colNameF) {
		this.colNameF = colNameF;
	}

	public String getRowNameF() {
		return rowNameF;
	}

	public void setRowNameF(String rowNameF) {
		this.rowNameF = rowNameF;
	}

	public String getUmatrixTableF() {
		return UmatrixTableF;
	}

	public void setUmatrixTableF(String umatrixTableF) {
		UmatrixTableF = umatrixTableF;
	}

	public String getVmatrixTableF() {
		return VmatrixTableF;
	}

	public void setVmatrixTableF(String vmatrixTableF) {
		VmatrixTableF = vmatrixTableF;
	}

	public String getSingularValueTableF() {
		return singularValueTableF;
	}

	public void setSingularValueTableF(String singularValueTableF) {
		this.singularValueTableF = singularValueTableF;
	}

}
