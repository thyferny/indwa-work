
package com.alpine.datamining.operator.neuralnet.sequential;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.AlpineRandom;


public class NodeInner extends NNNode{
	
	private static final long serialVersionUID = 6260741440827480948L;
	private double[] weights;
	private double[] bestErrorWeights;
	private double[] weightChanges;
	private AlpineRandom randomGenerator;
	private Base function;
	private String currentValueSQL;
	private String currentValueSQLPrediction;
	

	public NodeInner(String nodeName, int layerIndex, AlpineRandom randomGenerator, Base function) {
		super(nodeName, layerIndex, MIDDLE);
		this.randomGenerator = randomGenerator;
		this.function = function;
		weights = new double[] { this.randomGenerator.nextDouble() * 0.1d - 0.05d }; 
		bestErrorWeights = new double[1];
		System.arraycopy(weights, 0, bestErrorWeights, 0, bestErrorWeights.length); 
		weightChanges = new double[] { 0 };
	}

	public void setFunction(Base function) {
		this.function = function;
	}

	public Base getFunction() {
		return function;
	}

	public double computeValue(boolean shouldCalculate, double [] row) {
		if (Double.isNaN(currentValue) && shouldCalculate) {
			currentValue = function.calculateValue(this, row);
		}
		return currentValue;
	}

	public double computeError(boolean shouldCalculate, double [] row) {
		if (!Double.isNaN(currentValue) && Double.isNaN(currentError) && shouldCalculate) {
			currentError = function.calculateError(this, row);
		}
		return currentError;
	}
	public String computeValue(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQL && shouldCalculate) {
			currentValueSQL = function.calculateValue(this, dataSet);
		}
		return "("+currentValueSQL+")";
	}
	public String computeValuePrediction(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQLPrediction && shouldCalculate) {
			currentValueSQLPrediction = function.calculateValuePrediction(this, dataSet);
		}
		return "("+currentValueSQLPrediction+")";
	}
	public double[] getBestErrorWeights() {
		return bestErrorWeights;
	}

	public void setBestErrorWeights(double[] bestErrorWeights) {
		this.bestErrorWeights = bestErrorWeights;
	}
	public void copyWeightsToBestErrorWeights() {
		System.arraycopy(weights, 0, bestErrorWeights, 0, bestErrorWeights.length); 
	}
	
	public void copyBestErrorWeightsToWeights() {
		System.arraycopy(bestErrorWeights, 0, weights, 0, weights.length); 
	}

	public double getWeight(int n) {
		return weights[n + 1];
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	
	public double[] getWeightChanges() {
		return weightChanges;
	}
	
	public void setWeightChanges(double[] weightChanges) {
		this.weightChanges = weightChanges;
	}

	public void update(double [] row, double learningRate, double momentum) {
		if (!areWeightsUpdated() && !Double.isNaN(currentError)) {
			function.update(this, row, learningRate, momentum);
			super.update(row, learningRate, momentum);
		}
	}

	
	protected boolean connectInput(NNNode i, int n) {
		if (!super.connectInput(i, n)) {
			return false;
		}
		
		double[] newWeights = new double[weights.length + 1];
		System.arraycopy(weights, 0, newWeights, 0, weights.length);
		newWeights[newWeights.length - 1] = this.randomGenerator.nextDouble() * 0.1d - 0.05d;
		weights = newWeights;
		
		double[] newWeightChanges = new double[weightChanges.length + 1];
		System.arraycopy(weightChanges, 0, newWeightChanges, 0, weightChanges.length);
		newWeightChanges[newWeightChanges.length - 1] = 0;
		weightChanges = newWeightChanges;

		double[] newBestErrorWeights = new double[bestErrorWeights.length + 1];
		System.arraycopy(bestErrorWeights, 0, newBestErrorWeights, 0, bestErrorWeights.length);
		newBestErrorWeights[newBestErrorWeights.length - 1] = 0;
		bestErrorWeights = newBestErrorWeights;

		
		return true;
	}

	
	protected boolean disconnectInput(NNNode inputNode, int inputNodeOutputIndex) {
		int deleteIndex = -1;
		boolean removed = false;
		int numberOfInputs = inputNodes.length;
		do {
			deleteIndex = -1;
			for (int i = 0; i < inputNodes.length; i++) {
				if (inputNode == inputNodes[i] && (inputNodeOutputIndex == -1 || inputNodeOutputIndex == inputNodeOutputIndices[i])) {
					deleteIndex = i;
					break;
				}
			}

			if (deleteIndex >= 0) {
				for (int i = deleteIndex + 1; i < inputNodes.length; i++) {
					inputNodes[i - 1] = inputNodes[i];
					inputNodeOutputIndices[i - 1] = inputNodeOutputIndices[i];
					weights[i] = weights[i + 1];
					weightChanges[i] = weightChanges[i + 1];
					bestErrorWeights[i] = bestErrorWeights[i + 1];
					inputNodes[i - 1].outputNodeInputIndices[inputNodeOutputIndices[i - 1]] = i - 1;
				}
				numberOfInputs--;
				removed = true;
			}
		} while (inputNodeOutputIndex == -1 && deleteIndex != -1);
		
		NNNode[] newInputNodes = new NNNode[numberOfInputs];
		System.arraycopy(inputNodes, 0, newInputNodes, 0, numberOfInputs);
		inputNodes = newInputNodes;
		
		int[] newInputNodeOutputIndices = new int[numberOfInputs];
		System.arraycopy(inputNodeOutputIndices, 0, newInputNodeOutputIndices, 0, numberOfInputs);
		inputNodeOutputIndices = newInputNodeOutputIndices;
		
		double[] newWeights = new double[numberOfInputs + 1];
		System.arraycopy(weights, 0, newWeights, 0, numberOfInputs + 1);
		weights = newWeights;
		
		double[] newWeightChanges = new double[numberOfInputs + 1];
		System.arraycopy(weightChanges, 0, newWeightChanges, 0, numberOfInputs + 1);
		weightChanges = newWeightChanges;
		
		double[] newBestErrorWeights = new double[numberOfInputs + 1];
		System.arraycopy(bestErrorWeights, 0, newBestErrorWeights, 0, numberOfInputs + 1);
		bestErrorWeights = newBestErrorWeights;
		
		return removed;
	}
}
