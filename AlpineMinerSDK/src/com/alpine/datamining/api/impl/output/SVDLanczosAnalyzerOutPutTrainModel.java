/**
 * ClassName SVDAnalyzerOutPutTrainModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-6-20
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.operator.Model;

/**
 * @author Eason
 * generator a model
 */
public class SVDLanczosAnalyzerOutPutTrainModel extends AnalyzerOutPutTrainModel    {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1576617531368970410L;
	private AnalyzerOutPutTableObject UmatrixTable;
	private AnalyzerOutPutTableObject VmatrixTable;
	private AnalyzerOutPutTableObject singularValueTable;
	/**
	 * @param model
	 */
	public SVDLanczosAnalyzerOutPutTrainModel(EngineModel model) {
		super(model);
	}
	public SVDLanczosAnalyzerOutPutTrainModel(Model model) {
		super(model);
	}
	public AnalyzerOutPutTableObject getUmatrixTable() {
		return UmatrixTable;
	}
	public void setUmatrixTable(AnalyzerOutPutTableObject umatrixTable) {
		UmatrixTable = umatrixTable;
	}
	public AnalyzerOutPutTableObject getVmatrixTable() {
		return VmatrixTable;
	}
	public void setVmatrixTable(AnalyzerOutPutTableObject vmatrixTable) {
		VmatrixTable = vmatrixTable;
	}
	public AnalyzerOutPutTableObject getSingularValueTable() {
		return singularValueTable;
	}
	public void setSingularValueTable(AnalyzerOutPutTableObject singularValueTable) {
		this.singularValueTable = singularValueTable;
	}


}
