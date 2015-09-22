package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

public class HadoopPivotConfig extends HadoopDataOperationConfig {

	public static final String ConstAggrColumn = "pivotColumn";
	public static final String ConstAggrType = "aggregateType";
	public static final String ConstGroupByColumn = "groupByColumn";
	public static final String ConstAggregateColumn = "aggregateColumn";
	
	private String aggregateType=null;
	private String pivotColumn=null;
	private String groupByColumn="";
	private String aggregateColumn=null;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstAggrColumn);
		parameters.add(ConstAggrType);
		parameters.add(ConstGroupByColumn);	
		parameters.add(ConstAggregateColumn);	
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}

	public HadoopPivotConfig() {
		super();
		setParameterNames(parameters);
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public String getPivotColumn() {
		return pivotColumn;
	}

	public void setPivotColumn(String pivotColumn) {
		this.pivotColumn = pivotColumn;
	}

	public String getGroupByColumn() {
		return groupByColumn;
	}

	public void setGroupByColumn(String groupByColumn) {
		this.groupByColumn = groupByColumn;
	}

	public String getAggregateColumn() {
		return aggregateColumn;
	}

	public void setAggregateColumn(String aggregateColumn) {
		this.aggregateColumn = aggregateColumn;
	}
	
}
