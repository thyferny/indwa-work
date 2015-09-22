/**
 * ClassName LinearRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;


/**
 * Eason
 */
public class ARIMAConfig extends AbstractModelTrainerConfig{

	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.TimeSeriesTextAndTableVisualizationType";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	public static final String ConstP = "p";
	public static final String ConstQ = "q";
	public static final String ConstD = "d";
	public static final String ConstIdColumn = "idColumn";
	public static final String ConstValueColumn = "valueColumn";
//	public static final String ConstCycle = "cycle";
	public static final String ConstThreshold = "threshold";
	public static final String ConstGroupColumn = "groupColumn";

	private String p = "";
	private String q = "";
	private String d = "";
	private String idColumn = "";
	private String valueColumn = "";
	private String groupColumn = "";
//	private String cycle = "";
	private String threshold = "";
	static{ 
//		parameters.add(ConstForceRetrain);
//		parameters.add(ConstDependentColumn);
//		parameters.add(COLUMN_NAMES);
		parameters.add(ConstP);
		parameters.add(ConstQ);
		parameters.add(ConstD);
		parameters.add(ConstIdColumn);
		parameters.add(ConstValueColumn);
		parameters.add(ConstGroupColumn);
//		parameters.add(ConstCycle);
		parameters.add(ConstThreshold);
	}

	public ARIMAConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public ARIMAConfig(//String tableName,
			String columnNames,String dependentColumn){
		this(); 
		setColumnNames(columnNames);
		setDependentColumn ( dependentColumn);
	}

	public String getP() {
		return p;
	}

	public void setP(String p) {
		this.p = p;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}

//	public String getCycle() {
//		return cycle;
//	}

//	public void setCycle(String cycle) {
//		this.cycle = cycle;
//	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getGroupColumn() {
		return groupColumn;
	}

	public void setGroupColumn(String groupColumn) {
		this.groupColumn = groupColumn;
	} 
}
