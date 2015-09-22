/**
* ClassName WOEModel.java
*
* Version information: 1.00
*
* Data: 30 Nov 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.operator.woe;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;

/**
 * @author Shawn
 *
 */
public class WOEModel extends Prediction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4683227271728841408L;
	private AnalysisWOETable WOEInfoTable = new AnalysisWOETable();
	public AnalysisWOETable getWOEInfoTable() {
		return WOEInfoTable;
	}

	public void setWOEInfoTable(AnalysisWOETable wOEInfoTable) {
		WOEInfoTable = wOEInfoTable;
	}

	/**
	 * @param trainingDataSet
	 */
	public WOEModel(DataSet trainingDataSet) {
		super(trainingDataSet);
		}
	

	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.training.Prediction#performPrediction(com.alpine.datamining.db.DataSet, com.alpine.datamining.db.Column)
	 */
	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		throw new UnsupportedOperationException();
	}



	
	
	
}
