
package com.alpine.datamining.operator.neuralnet.sequential;

import com.alpine.datamining.db.DataSet;


public interface Base {

	public abstract String getTypeName();
	public abstract double calculateValue(NodeInner node, double[] row);
	public abstract String calculateValue(NodeInner node, DataSet dataSet);
	public abstract String calculateValuePrediction(NodeInner node, DataSet dataSet);
	public abstract double calculateError(NodeInner node, double[] row);
	public abstract void update(NodeInner node, double[] row, double learningRate, double momentum);

}
