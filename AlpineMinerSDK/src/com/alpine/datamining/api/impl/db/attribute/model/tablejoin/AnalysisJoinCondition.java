/**
 * ClassName  JoinCondition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tablejoin;

/**
 * @author John Zhao
 * 
 */
public class AnalysisJoinCondition {

	public static final String TAG_NAME = "JoinConditionModel";
	String tableAlias1;

	String tableAlias2;
	String joinType;
	String condition;
	String andOr;
	String column1;
	String column2;

	/**
	 * @param alias1
	 * @param alias2
	 * @param joinType
	 * @param condition
	 */
	public AnalysisJoinCondition(String alias1, String joinType, String alias2,
			String condition,String andOr) {
		this.tableAlias1 = alias1;
		this.tableAlias2 = alias2;
		this.joinType = joinType;
		this.condition = condition;
		this.andOr = andOr;
	}

	public String getTableAlias1() {
		return tableAlias1;
	}

	public void setTableAlias1(String tableAlias1) {
		this.tableAlias1 = tableAlias1;
	}

	public String getTableAlias2() {
		return tableAlias2;
	}

	public void setTableAlias2(String tableAlias2) {
		this.tableAlias2 = tableAlias2;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getColumn1() {
		return column1;
	}

	public void setColumn1(String column1) {
		this.column1 = column1;
	}

	public String getColumn2() {
		return column2;
	}

	public void setColumn2(String column2) {
		this.column2 = column2;
	}

	
	public String getAndOr() {
		return andOr;
	}

	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}

	public boolean equals(Object obj) {
		AnalysisJoinCondition target = (AnalysisJoinCondition) obj;
		if (target.getCondition().equals(condition)
				&& target.getJoinType().equals(joinType)
				&& target.getTableAlias1().equals(tableAlias1)
				&& target.getTableAlias2().equals(tableAlias2)
				&& target.getColumn1().equals(column1)
				&& target.getColumn2().equals(column2)
				&&target .getAndOr().equals(andOr)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AnalysisJoinCondition model = new AnalysisJoinCondition(tableAlias1, joinType,
				tableAlias2, condition,andOr);
		model.setColumn1(column1);
		model.setColumn2(column2);
		return model;
	}
}
