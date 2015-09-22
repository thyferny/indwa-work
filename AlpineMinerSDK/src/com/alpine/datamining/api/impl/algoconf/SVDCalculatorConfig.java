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
public class SVDCalculatorConfig extends PredictorConfig{
	private static final List<String> parameterNames = new ArrayList<String>();
	
	public static final String ConstUdependentColumn = "UdependentColumn";
    public static final String ConstVdependentColumn = "VdependentColumn";
	public static final String ConstUfeatureColumn = "UfeatureColumn";
    public static final String ConstVfeatureColumn = "VfeatureColumn";
	public static final String ConstColName = "colName";
    public static final String ConstRowName = "rowName";
    public static final String ConstUmatrixTable = "UmatrixTable";
    public static final String ConstVmatrixTable = "VmatrixTable";
	public static final String ConstCrossProduct = "crossProduct";
    public static final String ConstKeyColumn = "keyColumn";
    public static final String ConstKeyValue = "keyValue";

    static{
		parameterNames.add(ConstUdependentColumn);
		parameterNames.add(ConstVdependentColumn);
		parameterNames.add(ConstUfeatureColumn);
		parameterNames.add(ConstVfeatureColumn);
		parameterNames.add(ConstColName);
		parameterNames.add(ConstRowName);
		parameterNames.add(ConstUmatrixTable);
		parameterNames.add(ConstVmatrixTable);
		parameterNames.add(ConstCrossProduct);
		parameterNames.add(ConstKeyColumn);
		parameterNames.add(ConstKeyValue);

	}
	private String UdependentColumn;
	private String VdependentColumn;
	private String UfeatureColumn;
	private String VfeatureColumn;
	private String colName;
	private String rowName;
	private String UmatrixTable;
	private String VmatrixTable;
	private String crossProduct;
	private String keyColumn;
	private String keyValue;

	public SVDCalculatorConfig(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}

	public SVDCalculatorConfig() {
		setParameterNames(parameters);
		setParameterNames(parameterNames);
	}

	public String getUdependentColumn() {
		return UdependentColumn;
	}

	public void setUdependentColumn(String udependentColumn) {
		UdependentColumn = udependentColumn;
	}

	public String getVdependentColumn() {
		return VdependentColumn;
	}

	public void setVdependentColumn(String vdependentColumn) {
		VdependentColumn = vdependentColumn;
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

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getRowName() {
		return rowName;
	}

	public void setRowName(String rowName) {
		this.rowName = rowName;
	}
	public String getUmatrixTable() {
		return UmatrixTable;
	}

	public void setUmatrixTable(String umatrixTable) {
		UmatrixTable = umatrixTable;
	}

	public String getVmatrixTable() {
		return VmatrixTable;
	}

	public void setVmatrixTable(String vmatrixTable) {
		VmatrixTable = vmatrixTable;
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
}
