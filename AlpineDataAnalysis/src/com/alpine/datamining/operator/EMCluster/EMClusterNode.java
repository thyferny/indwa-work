 


package com.alpine.datamining.operator.EMCluster;



import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class EMClusterNode implements Serializable{
	
	
	private static final long serialVersionUID = -2236308273246117569L;
	private double alpha;
	private Map <String,Double> muValue =new LinkedHashMap<String,Double>();
	private Map <String,Double> sigmaValue =new LinkedHashMap<String,Double>();
	public double getAlpha() {
		return alpha;
	}
	public void setAlpha(Double alpha) {
		this.alpha = alpha;
	}
	public Map<String, Double> getMuValue() {
		return muValue;
	}
	public void setMuValue(Map<String, Double> muValue) {
		this.muValue = muValue;
	}
	public Map<String, Double> getSigmaValue() {
		return sigmaValue;
	}
	public void setSigmaValue(Map<String, Double> sigmaValue) {
		this.sigmaValue = sigmaValue;
	}
	
}
