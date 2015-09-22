/**
 * ClassName DataAnlyticOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.operator.Model;

/**
 * @author John Zhao
 * generator a model
 */
public class AnalyzerOutPutTrainModel extends AbstractAnalyzerOutPut   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1576617531368970410L;
	private EngineModel model;
	private boolean isComeFromRetrain=false;

	public EngineModel getEngineModel() { 
		return model;
	}

	/**
	 * @param model
	 */
	public AnalyzerOutPutTrainModel(EngineModel model) {
		this.model=model;
	}
	public AnalyzerOutPutTrainModel(Model model2) {
		EngineModel em= new EngineModel();
		em.setModel(model2);
		
		this.model=em;
	}

	public String toString(){
		if(model!=null){
			return model.toString();
		}else{
			return "";
		}
	}

	public boolean isComeFromRetrain() {
		return isComeFromRetrain;
	}

	public void setComeFromRetrain(boolean isComeFromRetrain) {
		this.isComeFromRetrain = isComeFromRetrain;
	}
	

}
