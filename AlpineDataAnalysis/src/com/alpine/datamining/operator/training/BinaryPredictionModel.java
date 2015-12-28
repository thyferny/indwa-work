
package com.alpine.datamining.operator.training;

import com.alpine.datamining.db.DataSet;


public abstract class BinaryPredictionModel extends Prediction {
	
	private static final long serialVersionUID = 4218999671408512315L;
	protected double threshold = 0.0d;
	
	protected BinaryPredictionModel(DataSet dataSet, double threshold) {
		super(dataSet);
        this.threshold = threshold;
	}
}
