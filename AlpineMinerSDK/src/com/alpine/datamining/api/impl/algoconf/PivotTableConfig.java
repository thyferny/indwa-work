package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

public class PivotTableConfig extends DataOperationConfig {

	public static final String ConstAggrColumn = "pivotColumn";
	public static final String ConstAggrType = "aggregateType";
	public static final String ConstGroupByColumn = "groupByColumn";
	public static final String ConstAggregateColumn = "aggregateColumn";
	public static final String ConstUseArray = "useArray";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstOutputType);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputTableStorageParameters);
		parameters.add(ConstAggrColumn);
		parameters.add(ConstAggrType);
		parameters.add(ConstGroupByColumn);	
		parameters.add(ConstAggregateColumn);	
		parameters.add(ConstUseArray);	
	}
	
	public PivotTableConfig() {
		super();
		setParameterNames(parameters);
	}
	private String aggregateType=null;
	private String pivotColumn=null;
	private String groupByColumn="";
	private String aggregateColumn=null;
	private String useArray=null;
	
	public String getAggregateColumn() {
		return aggregateColumn;
	}
	public void setAggregateColumn(String aggregateColumn) {
		this.aggregateColumn = aggregateColumn;
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
	public String getAggregateType() {
		return aggregateType;
	}
	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}	
	public String getUseArray() {
		return useArray;
	}
	public void setUseArray(String useArray) {
		this.useArray = useArray;
	}	
}
