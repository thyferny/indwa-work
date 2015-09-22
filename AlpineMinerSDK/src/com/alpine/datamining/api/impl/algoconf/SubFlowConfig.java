package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.HashMap;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;


//for step run equals use only...
public class SubFlowConfig extends AbstractAnalyticConfig{
	

	public static final String NAME_subflowPath = "subflowPath"; 
	public static final String NAME_tableMapping = "tableMapping";
	public static final String NAME_exitOperator = "exitOperator"; //uuid		
	public static final String NAME_subflowVariable = "subflowVariable"; 

	private static final ArrayList<String> parameterNames = new ArrayList<String>();
  	private String subflowPath;
  	private HashMap<String, String> tableMapping;
  	private String exitOperator;
  	private HashMap<String, String> subflowVariable;

  	
	static{
		parameterNames.add(NAME_subflowPath);
		parameterNames.add(NAME_tableMapping);
		parameterNames.add(NAME_exitOperator);
		parameterNames.add(NAME_subflowVariable);
 
		
	}

	public SubFlowConfig() {
		setParameterNames(parameterNames);
	}

	public String getSubflowPath() {
		return subflowPath;
	}

	public void setSubflowPath(String subflowPath) {
		this.subflowPath = subflowPath;
	}

	public HashMap<String, String> getTableMapping() {
		return tableMapping;
	}

	public void setTableMapping(HashMap<String, String> tableMapping) {
		this.tableMapping = tableMapping;
	}

	public String getExitOperator() {
		return exitOperator;
	}

	public void setExitOperator(String exitOperator) {
		this.exitOperator = exitOperator;
	}

	public HashMap<String, String> getSubflowVariable() {
		return subflowVariable;
	}

	public void setSubflowVariable(HashMap<String, String> subflowVariable) {
		this.subflowVariable = subflowVariable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((exitOperator == null) ? 0 : exitOperator.hashCode());
		result = prime * result
				+ ((subflowPath == null) ? 0 : subflowPath.hashCode());
		result = prime * result
				+ ((subflowVariable == null) ? 0 : subflowVariable.hashCode());
		result = prime * result
				+ ((tableMapping == null) ? 0 : tableMapping.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubFlowConfig other = (SubFlowConfig) obj;
		if (exitOperator == null) {
			if (other.exitOperator != null)
				return false;
		} else if (!exitOperator.equals(other.exitOperator))
			return false;
		if (subflowPath == null) {
			if (other.subflowPath != null)
				return false;
		} else if (!subflowPath.equals(other.subflowPath))
			return false;
		if (subflowVariable == null) {
			if (other.subflowVariable != null)
				return false;
		} else if (!subflowVariable.equals(other.subflowVariable))
			return false;
		if (tableMapping == null) {
			if (other.tableMapping != null)
				return false;
		} else if (!tableMapping.equals(other.tableMapping))
			return false;
		return true;
	}

	public SubFlowConfig(  String subflowPath, 
			HashMap<String, String> tableMapping, String exitOperator, HashMap<String, String> subflowVariable) {
	 
		this.subflowPath = subflowPath;
		this.tableMapping = tableMapping;
		this.exitOperator = exitOperator;
		this.subflowVariable = subflowVariable;
	}

 
	
}
