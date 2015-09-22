package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ARIMARPredictResult  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<SingleARIMARPredictResult> results = new ArrayList<SingleARIMARPredictResult>();
	private String groupColumnName = null;
	public List<SingleARIMARPredictResult> getResults() {
		return results;
	}

	public void setResults(List<SingleARIMARPredictResult> results) {
		this.results = results;
	}

	public String toString(){
		String ret = "";
		for(int i = 0; i < results.size(); i++){
			ret += results.toString(); 
		}
		return ret;
	}

	public String getGroupColumnName() {
		return groupColumnName;
	}

	public void setGroupColumnName(String groupColumnName) {
		this.groupColumnName = groupColumnName;
	}
}
