/**
* ClassName WeightOfEvidence.java
*
* Version information: 1.00
*
* Data: 25 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.operator.woe.AnalysisWOETable;



/**
 * @author Shawn
 *
 */
public class WeightOfEvidenceConfig  extends AbstractModelTrainerConfig{

	public static final String PARAMETER_DEPENDENTCOLUMN = "dependentColumn";
	public static final String ConstGoodValue = "goodValue";
	
	private static final List<String> parameterNames = new ArrayList<String>();

	private String dependentColumn;
	private String goodValue;
	private AnalysisWOETable WOETableInfor=new AnalysisWOETable();
	private Map<String,Boolean> isChanged=new HashMap<String,Boolean>();

	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.WOERulesTextAndTableVisualizationType";
	
	public WeightOfEvidenceConfig() {
		super();
		setParameterNames(parameterNames);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public Map<String, Boolean> getIsChanged() {
		return isChanged;
	}
	public void setIsChanged(Map<String, Boolean> isChanged) {
		this.isChanged = isChanged;
	}
	public AnalysisWOETable getWOETableInfor() {
		return WOETableInfor;
	}
	public void setWOETableInfor(AnalysisWOETable wOETableInfor) {
		WOETableInfor = wOETableInfor;
	}
	static{
		parameterNames.add(ConstGoodValue);
		parameterNames.add(PARAMETER_DEPENDENTCOLUMN);
		parameterNames.add(PARAMETER_COLUMN_NAMES);
	}
	
	public String getGoodValue() {
		return goodValue;
	}
	public void setGoodValue(String goodValue) {
		this.goodValue = goodValue;
	}
	public String getDependentColumn() {
		return dependentColumn;
	}
	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}


	

}
