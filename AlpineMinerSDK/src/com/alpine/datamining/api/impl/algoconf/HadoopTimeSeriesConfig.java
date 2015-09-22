package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;

public class HadoopTimeSeriesConfig extends AbstractModelTrainerConfig{

	public static String ConstID = "idColumn";
	public static String ConstVALUE = "valueColumn";
	public static String ConstGroupBy = "groupColumn";
	public static String ConstAutoregressionStep = "p";
	public static String ConstMovingAvgStep = "q";
	public static String ConstIntegratedStep = "d";
	public static String ConstLengthOfWindow="lengthOfWindow";
	public static String ConstTimeFormat="timeFormat";
	
	private String idColumn;
	private String valueColumn;
	private String groupColumn;
	private String p;
	private String q;
	private String d;
	private String lengthOfWindow;
	private String timeFormat;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{ 
		parameters.add(ConstID);
		parameters.add(ConstVALUE);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstGroupBy);
		parameters.add(ConstAutoregressionStep);
		parameters.add(ConstMovingAvgStep);
		parameters.add(ConstIntegratedStep);
		parameters.add(ConstLengthOfWindow);	
		parameters.add(ConstTimeFormat);	
		
		}

	public HadoopTimeSeriesConfig(){
		setParameterNames(parameters);
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

	public String getGroupColumn() {
		return groupColumn;
	}

	public void setGroupColumn(String groupColumn) {
		this.groupColumn = groupColumn;
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

	public String getLengthOfWindow() {
		return lengthOfWindow;
	}

	public void setLengthOfWindow(String lengthOfWindow) {
		this.lengthOfWindow = lengthOfWindow;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
	
}
