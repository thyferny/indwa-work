/**
 * ClassName  GoodnessOfFitOutPut.java
 *
 * Version information: 1.00
 *
 * Data: Jun 12, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.evaluator;

import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;

/**
 * @author John Zhao
 *
 */
public class GoodnessOfFitOutPut extends AbstractAnalyzerOutPut{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5688641279802460103L;
	String name;
	String description;
 
	private List<GoodnessOfFit> resultList;
	/**
	 * @param resultList
	 */
	public GoodnessOfFitOutPut(List<GoodnessOfFit> resultList) {
		this.resultList=resultList;
	 
	}
	public List<GoodnessOfFit> getResultList() {
		return resultList;
	}
	public void setResultList(List<GoodnessOfFit> resultList) {
		this.resultList = resultList;
	}
 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
 
	

}
