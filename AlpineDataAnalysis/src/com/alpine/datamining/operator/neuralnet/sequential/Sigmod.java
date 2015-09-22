/**
 * ClassName Sigmod
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.neuralnet.sequential;

import java.io.Serializable;

import com.alpine.datamining.db.DataSet;

/**
 *
 */
public class Sigmod implements Base, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4598833104501642810L;

	public String getTypeName() {
		return "Sigmoid";
	}
	
	public double calculateValue(NodeInner node, double[] row) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		double weightedSum = weights[0]; // threshold
		for (int i = 0; i < inputs.length; i++) {
			weightedSum += inputs[i].computeValue(true, row) * weights[i + 1];
		}

		double result = 0.0d;
		if (weightedSum < -45.0d) {
			result = 0;
		} else if (weightedSum > 45.0d) {
			result = 1;
		} else {
			result = 1 / (1 + Math.exp((-1) * weightedSum));
		}
		return result;
	}

	public double calculateError(NodeInner node, double[] row) {
		NNNode[] outputs = node.getOutputNodes();
		int[] numberOfOutputs = node.getOutputNodeInputIndices();
		double errorSum = 0;
		for (int i = 0; i < outputs.length; i++) {
			errorSum += outputs[i].computeError(true, row) * outputs[i].getWeight(numberOfOutputs[i]);
		}
		double value = node.computeValue(false, row);
		return errorSum * value * (1 - value);
	}
	public String calculateValue(NodeInner node, DataSet dataSet) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		String weightedSumSQL = String.valueOf(weights[0]);
		for (int i = 0; i < inputs.length; i++) {
			weightedSumSQL += "+"+inputs[i].computeValue(true, dataSet) +"*"+ weights[i + 1];
		}
		String resultSQL = "case when ("+weightedSumSQL+ ")< -45.0 then 0.0 when ("+weightedSumSQL+") > 45.0 then 1.0 else 1 / (1 + exp((-1) * ("+weightedSumSQL+"))) end";

		return "("+resultSQL+")";
	}
	public String calculateValuePrediction(NodeInner node, DataSet dataSet) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		String weightedSumSQL = String.valueOf(weights[0]);
		for (int i = 0; i < inputs.length; i++) {
			weightedSumSQL += "+"+inputs[i].computeValuePrediction(true, dataSet) +"*"+ weights[i + 1];
		}
		String resultSQL = "case when ("+weightedSumSQL+ ")< -45.0 then 0.0 when ("+weightedSumSQL+") > 45.0 then 1.0 else 1 / (1 + exp((-1) * ("+weightedSumSQL+"))) end";

		return "("+resultSQL+")";
	}
	
	public void update(NodeInner node, double[] row, double learningRate, double momentum) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		double[] weightChanges = node.getWeightChanges();
		double delta = learningRate * node.computeError(false,  row);

		// threshold update
		double thresholdChange = delta + momentum * weightChanges[0];
		weights[0] += thresholdChange;
		weightChanges[0] = thresholdChange;

		// update node weights
		for (int i = 1; i < inputs.length + 1; i++) {
			double currentChange = delta * inputs[i - 1].computeValue(false, row);
			currentChange += momentum * weightChanges[i];
			weights[i] += currentChange;
			weightChanges[i] = currentChange;
		}
		
		node.setWeights(weights);
		node.setWeightChanges(weightChanges);
	}
}
