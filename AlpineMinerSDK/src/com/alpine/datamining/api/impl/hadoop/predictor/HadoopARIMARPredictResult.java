package com.alpine.datamining.api.impl.hadoop.predictor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HadoopARIMARPredictResult  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<HadoopSingleARIMARPredictResult> results = new ArrayList<HadoopSingleARIMARPredictResult>();
	private String groupColumnName = null;
	public List<HadoopSingleARIMARPredictResult> getResults() {
		return results;
	}

	public void setResults(List<HadoopSingleARIMARPredictResult> results) {
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
