/**
 * 

* ClassName AnalyzerOutPutPLDATrainModel.java
*
* Version information: 1.00
*
* Data: Mar 14, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.operator.Model;

/**
 * @author Shawn
 *
 */
public class AnalyzerOutPutPLDATrainModel extends AnalyzerOutPutTrainModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2871639054076822299L;
	private AnalyzerOutPutTableObject PLDAWordTopicOutTable;
	private AnalyzerOutPutTableObject PLDADocTopicOutTable;

	/**
	 * @param model
	 */
	public AnalyzerOutPutPLDATrainModel(EngineModel model) {
		super(model);
		}
	
	
	public AnalyzerOutPutPLDATrainModel(Model model) {
		super(model);
		}

	
	  
	  public String toString()
	  {
	    return "";
	  }


	public AnalyzerOutPutTableObject getPLDAWordTopicOutTable() {
		return PLDAWordTopicOutTable;
	}


	public void setPLDAWordTopicOutTable(
			AnalyzerOutPutTableObject pLDAWordTopicOutTable) {
		PLDAWordTopicOutTable = pLDAWordTopicOutTable;
	}


	public AnalyzerOutPutTableObject getPLDADocTopicOutTable() {
		return PLDADocTopicOutTable;
	}


	public void setPLDADocTopicOutTable(
			AnalyzerOutPutTableObject pLDADocTopicOutTable) {
		PLDADocTopicOutTable = pLDADocTopicOutTable;
	}
	  
	  
}
