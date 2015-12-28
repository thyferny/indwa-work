


package com.alpine.datamining.operator.woe;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;


public class WOEModel extends Prediction{
	
	
	private static final long serialVersionUID = -4683227271728841408L;
	private AnalysisWOETable WOEInfoTable = new AnalysisWOETable();
	public AnalysisWOETable getWOEInfoTable() {
		return WOEInfoTable;
	}

	public void setWOEInfoTable(AnalysisWOETable wOEInfoTable) {
		WOEInfoTable = wOEInfoTable;
	}

	
	public WOEModel(DataSet trainingDataSet) {
		super(trainingDataSet);
		}
	

	
	
	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		throw new UnsupportedOperationException();
	}



	
	
	
}
