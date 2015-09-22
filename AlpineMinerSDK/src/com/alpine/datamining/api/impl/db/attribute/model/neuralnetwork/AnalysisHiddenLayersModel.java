/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author jeff Dong
 *
 */

public class AnalysisHiddenLayersModel{
	public static final String TAG_NAME="HiddenLayersModel";
	List<AnalysisHiddenLayer> hiddenLayers=null;
	
  	
	public List<AnalysisHiddenLayer> getHiddenLayers() {
		return hiddenLayers;
	}

	public void setHiddenLayers(List<AnalysisHiddenLayer> hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}

	public AnalysisHiddenLayersModel(List<AnalysisHiddenLayer> hiddenLayers ){ 
		this.hiddenLayers=hiddenLayers;
 
	}
	
	public AnalysisHiddenLayersModel( ){
	 
		this.hiddenLayers=new ArrayList<AnalysisHiddenLayer>();
	}
	  
	
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisHiddenLayersModel){
				return  ListUtility.equalsIgnoreOrder(hiddenLayers,
						((AnalysisHiddenLayersModel)obj).getHiddenLayers());			
		}else{
			return false;
		}
	
	}
  
	public void addHiddenLayer (AnalysisHiddenLayer layer) {
		if(hiddenLayers==null){
			hiddenLayers= new ArrayList<AnalysisHiddenLayer>();
		}
		hiddenLayers.add(layer) ;
		 
		
	}
	public String toString(){
		String temp="";
		if (hiddenLayers != null){
			int count = hiddenLayers.size();
			int i =0;
			for(AnalysisHiddenLayer hiddenLayer: hiddenLayers){
				temp+=hiddenLayer.getLayerName()+","+hiddenLayer.getLayerSize();
				i++;
				if(i<count){
					temp+=";";
				}
			}
		}
		return temp;

	}

}
