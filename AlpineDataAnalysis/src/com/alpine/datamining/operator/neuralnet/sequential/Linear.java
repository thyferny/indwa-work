/**
 * ClassName Linear
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
 */
public class Linear  implements Base, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4253274454853943456L;

	public String getTypeName() {
		return "Linear";
	}
	
	public double calculateValue(NodeInner node, double[] row) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		double weightedSum = weights[0]; // threshold
		for (int i = 0; i < inputs.length; i++) {
			weightedSum += inputs[i].computeValue(true, row) * weights[i + 1];
		}
		return weightedSum;
	}

	public double calculateError(NodeInner node, double[] row) {
		NNNode[] outputs = node.getOutputNodes();
		int[] numberOfOutputs = node.getOutputNodeInputIndices();
		double errorSum = 0;
		for (int i = 0; i < outputs.length; i++) {
			errorSum += outputs[i].computeError(true, row) * outputs[i].getWeight(numberOfOutputs[i]);
		}
		return errorSum;
	}
	
	public String calculateValue(NodeInner node, DataSet dataSet) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		String weightedSumSQL = String.valueOf(weights[0]); 
		for (int i = 0; i < inputs.length; i++) {
			weightedSumSQL += "+"+inputs[i].computeValue(true, dataSet)+" *"+ weights[i + 1];
		}
		return "("+weightedSumSQL+")";
	}
	public String calculateValuePrediction(NodeInner node, DataSet dataSet) {
		NNNode[] inputs = node.getInputNodes();
		double[] weights = node.getWeights();
		String weightedSumSQL = String.valueOf(weights[0]); 
		for (int i = 0; i < inputs.length; i++) {
			weightedSumSQL += "+"+inputs[i].computeValuePrediction(true, dataSet)+" *"+ weights[i + 1];
		}
		return "("+weightedSumSQL+")";
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
