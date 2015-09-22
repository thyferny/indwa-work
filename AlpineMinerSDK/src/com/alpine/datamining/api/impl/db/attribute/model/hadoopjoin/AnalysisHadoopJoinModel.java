/**
 * ClassName HadoopJoinModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class AnalysisHadoopJoinModel{
	
	public static final String JOIN_TYPE_TAG_NAME = "joinType";

	private List<AnalysisHadoopJoinFile> joinTables = new ArrayList<AnalysisHadoopJoinFile>();
	
	private List<AnalysisHadoopJoinColumn> joinColumns = new ArrayList<AnalysisHadoopJoinColumn>();
	
	private List<AnalysisHadoopJoinCondition> joinConditions = new ArrayList<AnalysisHadoopJoinCondition>();
	
	private String joinType;
	
	/**
	 * @param joinTabls
	 * @param joinColumns
	 * @param joinConditions
	 */
	public AnalysisHadoopJoinModel(List<AnalysisHadoopJoinFile> joinTables,
			List<AnalysisHadoopJoinColumn> joinColumns, 
			List<AnalysisHadoopJoinCondition> joinConditions,
			String joinType) {
		this.joinTables = joinTables;
		this.joinColumns = joinColumns;
		this.joinConditions = joinConditions;
		this.joinType=joinType;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		List<AnalysisHadoopJoinFile> newJoinTables = new ArrayList<AnalysisHadoopJoinFile>();
		for (AnalysisHadoopJoinFile joinTable : joinTables) {
			newJoinTables.add((AnalysisHadoopJoinFile) joinTable.clone());
		}
		List<AnalysisHadoopJoinColumn> newJoinColumns = new ArrayList<AnalysisHadoopJoinColumn>();
		for (AnalysisHadoopJoinColumn joinColumn : joinColumns) {
			newJoinColumns.add((AnalysisHadoopJoinColumn) joinColumn.clone());
		}
		List<AnalysisHadoopJoinCondition> newJoinConditions = new ArrayList<AnalysisHadoopJoinCondition>();
		for (AnalysisHadoopJoinCondition joinCondition : joinConditions) {
			newJoinConditions.add((AnalysisHadoopJoinCondition) joinCondition.clone());
		}
		return new AnalysisHadoopJoinModel(newJoinTables, newJoinColumns,
				newJoinConditions,joinType);
	}

	public boolean equals(Object obj) {
		AnalysisHadoopJoinModel target = (AnalysisHadoopJoinModel) obj;
		if(StringUtil.isEmpty(joinType)==true||joinType.equals(target.getJoinType())==false){
			return false;
		}else
		if ((joinTables == null && target.getJoinTables() != null)
				|| (target.getJoinTables() == null && joinTables != null)
				|| (joinColumns == null && target.getJoinColumns() != null)
				|| (target.getJoinColumns() == null && joinColumns != null)
				|| (joinConditions == null && target.getJoinConditions() != null)
				|| (target.getJoinConditions() == null && joinConditions != null)) {
			return false;

		} else if (joinTables.size() != target.getJoinTables().size()
				|| joinColumns.size() != target.getJoinColumns().size()
				|| joinConditions.size() != target.getJoinConditions().size()) {
			return false;
		} else if (ListUtility.equalsIgnoreOrder(joinTables, target
				.getJoinTables()) == false
				|| ListUtility.equalsIgnoreOrder(joinColumns, target
						.getJoinColumns()) == false
				|| ListUtility.equalsIgnoreOrder(joinConditions, target
						.getJoinConditions()) == false) {
			return false;
		}
		return true;

	}
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(JOIN_TYPE_TAG_NAME).append(":").append(getJoinType()).append(";");
		for (Iterator<AnalysisHadoopJoinFile> iterator = getJoinTables().iterator(); iterator
				.hasNext();) {
			AnalysisHadoopJoinFile item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<AnalysisHadoopJoinColumn> iterator = getJoinColumns().iterator(); iterator
				.hasNext();) {
			AnalysisHadoopJoinColumn item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<AnalysisHadoopJoinCondition> iterator = getJoinConditions().iterator(); iterator
				.hasNext();) {
			AnalysisHadoopJoinCondition item = iterator.next();
			out.append(item.toString());
		}
		return out.toString();
	}
	public List<AnalysisHadoopJoinFile> getJoinTables() {
		return joinTables;
	}
	public void setJoinTables(List<AnalysisHadoopJoinFile> joinTables) {
		this.joinTables = joinTables;
	}
	public List<AnalysisHadoopJoinColumn> getJoinColumns() {
		return joinColumns;
	}
	public void setJoinColumns(List<AnalysisHadoopJoinColumn> joinColumns) {
		this.joinColumns = joinColumns;
	}
	public List<AnalysisHadoopJoinCondition> getJoinConditions() {
		return joinConditions;
	}
	public void setJoinConditions(List<AnalysisHadoopJoinCondition> joinConditions) {
		this.joinConditions = joinConditions;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	
}
