/**
 * ClassName  SampleDataDefinition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-19
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tablejoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * @author zhaoyong
 *
 */
public class AnalysisTableJoinModel{
	
 
	//we keep  this tag name to compaitable with old version's flow file
	public static final String TAG_NAME = "TableJoinDefinition"; 

	private List<AnalysisJoinTable> joinTabls=new ArrayList<AnalysisJoinTable>();
	
	private List<AnalysisJoinColumn> joinColumns= new ArrayList<AnalysisJoinColumn>();
	
	private List<AnalysisJoinCondition> joinConditions= new ArrayList<AnalysisJoinCondition>();
	 	
 
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisTableJoinModel(joinTabls,joinColumns,joinConditions);
	}

	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		 for (Iterator<AnalysisJoinTable> iterator = getJoinTables().iterator(); iterator.hasNext();) {
			 AnalysisJoinTable item = iterator.next();
			out.append(item.toString());		
		}
		 for (Iterator<AnalysisJoinColumn> iterator = getJoinColumns().iterator(); iterator.hasNext();) {
			 AnalysisJoinColumn item = iterator.next();
			out.append(item.toString());		
		}
		 for (Iterator<AnalysisJoinCondition> iterator = getJoinConditions().iterator(); iterator.hasNext();) {
			 AnalysisJoinCondition item = iterator.next();
			out.append(item.toString());		
		}
		return out.toString();
	}

	/**
	 * @param joinTabls
	 * @param joinColumns
	 * @param joinConditions
	 */
	public AnalysisTableJoinModel(List<AnalysisJoinTable> joinTabls,
			List<AnalysisJoinColumn> joinColumns, List<AnalysisJoinCondition> joinConditions) {
		this.joinTabls = joinTabls;
		this.joinColumns = joinColumns;
		this.joinConditions = joinConditions;
	}

	/**
	 * 
	 */
	public AnalysisTableJoinModel() {
		 
	}
	public void addJoinTable(AnalysisJoinTable joinTable){
		getJoinTables().add(joinTable);
	}
	public void deleteJoinTable(AnalysisJoinTable joinTable){
		getJoinTables().remove(joinTable);
	}
	
	public void addJoinColumn	(AnalysisJoinColumn joinColumn){
		getJoinColumns().add(joinColumn);
	}
	public void deleteJoinColumn(AnalysisJoinColumn joinColumn){
		getJoinColumns().remove(joinColumn);
	}
	
	public void addJoinCondition(AnalysisJoinCondition joinCondition){
		getJoinConditions().add(joinCondition);
	}
	public void deleteJoinCondition(AnalysisJoinCondition joinCondition){
		getJoinConditions().remove(joinCondition);
	}
	
	public List<AnalysisJoinTable> getJoinTables() {
		return joinTabls;
	}

	public void setJoinTables(List<AnalysisJoinTable> joinTabls) {
		this.joinTabls = joinTabls;
	}

	public List<AnalysisJoinColumn> getJoinColumns() {
		return joinColumns;
	}

	public void setJoinColumns(List<AnalysisJoinColumn> joinColumns) {
		this.joinColumns = joinColumns;
	}

	public List<AnalysisJoinCondition> getJoinConditions() {
		return joinConditions;
	}

	public void setJoinConditions(List<AnalysisJoinCondition> joinConditions) {
		this.joinConditions = joinConditions;
	}

	
	 public boolean equals(Object obj) {
		 AnalysisTableJoinModel target=(AnalysisTableJoinModel )obj;
		 boolean res=true;
		 if((joinTabls==null&&target.getJoinTables()!=null)
				 ||(target.getJoinTables()==null&&joinTabls!=null)
				 ||(joinColumns==null&&target.getJoinColumns()!=null)
				 ||(target.getJoinColumns()==null&&joinColumns!=null)
					||(joinConditions==null&&target.getJoinConditions()!=null)
						 ||(target.getJoinConditions()==null&&joinConditions!=null)
			){
				return false;	 
			 
		 	}
		 else if (joinTabls.size()!=target.getJoinTables().size()
				 || joinColumns.size()!=target.getJoinColumns().size()
				 ||joinConditions.size()!=target.getJoinConditions().size()
				 ){
			 return false;
		 }
		 else if(tableJoinEquals(target)==false||
				 tableColumnEquals(target)==false||
				 joinConditionEquals(target)==false){
			 return false;
		 }
 
		 
		 return res;
		 
	 }

	/**
	 * @param target
	 * @return
	 */
	private boolean joinConditionEquals(AnalysisTableJoinModel target) {
		for (Iterator<AnalysisJoinCondition> iterator = joinConditions.iterator(); iterator.hasNext();) {
			AnalysisJoinCondition joinModel = iterator.next();
			if(hasSameJoinCondition(target.getJoinConditions(),joinModel)==false){
				return false;
			}
			
		}
		for (Iterator<AnalysisJoinCondition> iterator = target.getJoinConditions().iterator(); iterator.hasNext();) {
			AnalysisJoinCondition joinModel = iterator.next();
			if(hasSameJoinCondition(joinConditions,joinModel)==false){
				return false;
			}
			
		}
		 
		return true;
	}
 

	/**
	 * @param joinConditions2
	 * @param joinModel
	 * @return
	 */
	private boolean hasSameJoinCondition(
			List<AnalysisJoinCondition> target,
			AnalysisJoinCondition joinModel) {
		for (Iterator<AnalysisJoinCondition> iterator = target.iterator(); iterator.hasNext();) {
			AnalysisJoinCondition joinConditionModel =iterator
					.next();
			if(joinConditionModel!=null&&joinConditionModel.equals(joinModel)==true){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param target
	 * @return
	 */
	private boolean tableColumnEquals(AnalysisTableJoinModel target) {
		for (Iterator<AnalysisJoinColumn> iterator = joinColumns.iterator(); iterator.hasNext();) {
			AnalysisJoinColumn joinModel = iterator.next();
			if(hasSameJoinColumn(target.getJoinColumns(),joinModel)==false){
				return false;
			}
			
		}
		for (Iterator<AnalysisJoinColumn> iterator = target.getJoinColumns().iterator(); iterator.hasNext();) {
			AnalysisJoinColumn joinModel = iterator.next();
			if(hasSameJoinColumn(joinColumns,joinModel)==false){
				return false;
			}
			
		}
		 
		return true;
	}

	/**
	 * @param joinColumns2
	 * @param joinModel
	 * @return
	 */
	private boolean hasSameJoinColumn(List<AnalysisJoinColumn> target,
			AnalysisJoinColumn joinModel) {
		for (Iterator<AnalysisJoinColumn> iterator = target.iterator(); iterator.hasNext();) {
			AnalysisJoinColumn jmodel = iterator
					.next();
			if(jmodel!=null&&jmodel.equals(joinModel)==true){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param target
	 * @return
	 */
	private boolean tableJoinEquals(AnalysisTableJoinModel target) {
		for (Iterator<AnalysisJoinTable> iterator = joinTabls.iterator(); iterator.hasNext();) {
			AnalysisJoinTable joinModel = iterator.next();
			if(hasSameJoinTable(target.getJoinTables(),joinModel)==false){
				return false;
			}
			
		}
		for (Iterator<AnalysisJoinTable> iterator = target.getJoinTables().iterator(); iterator.hasNext();) {
			AnalysisJoinTable joinModel = iterator.next();
			if(hasSameJoinTable(joinTabls,joinModel)==false){
				return false;
			}
			
		}
		 
		return true;
	}

	/**
	 * @param joinTabls2
	 * @param joinModel
	 * @return
	 */
	private boolean hasSameJoinTable(List<AnalysisJoinTable> target,
			AnalysisJoinTable joinModel) {
		for (Iterator<AnalysisJoinTable> iterator = target.iterator(); iterator.hasNext();) {
			AnalysisJoinTable jmodel = iterator
					.next();
			if(jmodel!=null&&jmodel.equals(joinModel)==true){
				return true;
			}
		}
		return false;
	}
}