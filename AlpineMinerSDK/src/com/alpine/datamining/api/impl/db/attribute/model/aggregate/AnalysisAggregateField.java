/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.aggregate;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;


/**
 * @author zhaoyong
 * 
 */
public class AnalysisAggregateField {

	public static final String TAG_NAME="AggregateField";
	
	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_EXPRESSION = "expression";
	
	private static final String ATTR_TYPE = "dataType";

	String alias = null;
	
	String aggregateExpression = null;
	
	String dataType = null;
	
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAggregateExpression() {
		return aggregateExpression;
	}

	public void setAggregateExpression(String aggregateExpression) {
		this.aggregateExpression = aggregateExpression;
	}

	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public AnalysisAggregateField(String alias, String aggregateExpression,String dataType) {
		this.aggregateExpression = aggregateExpression;
		this.alias = alias;
		this.dataType=dataType;
	}



	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisAggregateField(alias, aggregateExpression,dataType);
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof AnalysisAggregateField){
			 AnalysisAggregateField aggField = (AnalysisAggregateField) obj;
			 return ModelUtility.nullableEquales(alias ,aggField.getAlias())
			 && ModelUtility.nullableEquales(aggregateExpression ,aggField.getAggregateExpression())
			 &&ModelUtility.nullableEquales(dataType ,aggField.getDataType());
 
		 }else{
			 return false;
		 }
	 }
 
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(ATTR_COLUMNNAME).append(":").append(alias).append(",");
			sb.append(ATTR_TYPE).append(":").append(dataType);
			sb.append(ATTR_EXPRESSION).append(":").append(aggregateExpression).append("\n");
			return sb.toString();
		}
		
}