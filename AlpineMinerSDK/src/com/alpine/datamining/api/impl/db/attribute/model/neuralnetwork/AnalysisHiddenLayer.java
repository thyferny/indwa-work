/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;

/**
 * @author zhaoyong
 * 
 */
public class AnalysisHiddenLayer{

	public static final String TAG_NAME="HiddenLayer";

	String layerName = null;	
	Integer layerSize = null;
	
	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public Integer getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(Integer layerSize) {
		this.layerSize = layerSize;
	}


 

	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public AnalysisHiddenLayer(String layerName, Integer layerSize) {
		this.layerName = layerName;
		this.layerSize = layerSize;
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisHiddenLayer(layerName,layerSize);
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof AnalysisHiddenLayer){
			 AnalysisHiddenLayer aggField = (AnalysisHiddenLayer) obj;
			 return ModelUtility.nullableEquales(layerName ,aggField.getLayerName())
			 && ModelUtility.nullableEquales(layerSize ,aggField.getLayerSize());
 
		 }else{
			 return false;
		 }
	 }
 

}