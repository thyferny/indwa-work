/**
 * ClassName  DerivedFieldItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.variable;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;



/**
 * @author zhaoyong
 *
 */
public class AnalysisDerivedFieldItem {
	
	public static final String TAG_NAME="DerivedFieldItem";
	
	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_DATATYPE = "dataType";

	private static final String ATTR_EXPRESSION = "expression";

	String resultColumnName = null; 
	String dataType = null;
	String sqlExpression = null;
	
	
 
	/**
	 * @param resultColumnName
	 * @param dataType
	 * @param sqlExpression
	 */
	public AnalysisDerivedFieldItem(String resultColumnName, String dataType,
			String sqlExpression) {
	 
		this.resultColumnName = resultColumnName;
		this.dataType = dataType;
		this.sqlExpression = sqlExpression;
	}

	public String getResultColumnName() {
		return resultColumnName;
	}

	public void setResultColumnName(String resultColumnName) {
		this.resultColumnName = resultColumnName;
	}

	public String getDataType() {
		return dataType;
	}



	public void setDataType(String dataType) {
		this.dataType = dataType;
	}



	public String getSqlExpression() {
		return sqlExpression;
	}



	public void setSqlExpression(String sqlExpression) {
		this.sqlExpression = sqlExpression;
	}
	
	public boolean equals(Object obj) {
		if(obj==this){
			return true;
		}
		else if(obj instanceof AnalysisDerivedFieldItem){
			AnalysisDerivedFieldItem item = (AnalysisDerivedFieldItem)obj;
			
			return  ModelUtility.nullableEquales(item.getDataType(),this.getDataType())
				 && ModelUtility.nullableEquales(item.getResultColumnName(),this.getResultColumnName()) 
			    && ModelUtility.nullableEquales(item.getSqlExpression(),this.getSqlExpression()) ;
				
		}else{
			return false;
		}
	
	}
	
	/**
	 * @return
	 */
	public AnalysisDerivedFieldItem clone() {
		AnalysisDerivedFieldItem item = new AnalysisDerivedFieldItem(resultColumnName,dataType,sqlExpression);
		return item;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ATTR_COLUMNNAME).append(":").append(resultColumnName).append(",");
		sb.append(ATTR_DATATYPE).append(":").append(dataType);
		sb.append(ATTR_EXPRESSION).append(":").append(sqlExpression).append("\n");
		return sb.toString();
	}
	 
}
