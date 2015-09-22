/**
 * ClassName  HadoopJoinCondition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin;


/**
 * @author Jeff Dong
 *
 */
public class AnalysisHadoopJoinCondition {
	
	String keyColumn;
	
	public AnalysisHadoopJoinCondition(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	public String getKeyColumn() {
		return keyColumn;
	}
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	public boolean equals(Object obj) {
		 if(obj==null||obj instanceof AnalysisHadoopJoinCondition ==false){
			 return false;
		 }
		 AnalysisHadoopJoinCondition target=(AnalysisHadoopJoinCondition) obj ;
		 //avoid the the null point
		 
		 return target.getKeyColumn().equals(getKeyColumn());
	 }
	 
		@Override
		public Object clone() throws CloneNotSupportedException {
			AnalysisHadoopJoinCondition model=new AnalysisHadoopJoinCondition(keyColumn);
			model.setKeyColumn(keyColumn);
			return model;
		}
		
} 
