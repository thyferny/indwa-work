/**
 * ClassName NNNode
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
public abstract class NNNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4739380417814094034L;
	public static final int INPUT  = -1;
	public static final int MIDDLE = 0;
	public static final int OUTPUT = -2;
	private int layerIndex;
	private String nodeName;
	private int nodeType;
	private boolean weightsAreUpdated = false;
	protected NNNode[] inputNodes = new NNNode[0];
	protected NNNode[] outputNodes = new NNNode[0];

	protected int[] inputNodeOutputIndices = new int[0];

	protected int[] outputNodeInputIndices = new int[0];

	protected double currentValue = Double.NaN;

	protected double currentError = Double.NaN;


	public NNNode(String nodeName, int layerIndex, int nodeType) {
		this.layerIndex = layerIndex;
		this.nodeName = nodeName;
		this.nodeType = nodeType;
	}

	/** Calculates the output. */
	public abstract double computeValue(boolean calculate, double[] row);
	
	
	public abstract String computeValue(boolean calculate, DataSet dataSet);
	
	public abstract String computeValuePrediction(boolean calculate, DataSet dataSet);

	/** Calculates the error. */
	public abstract double computeError(boolean calculate, double[] row);
	
	/**  Subclasses should overwrite this method. */
	public double getWeight(int n) {
		return 1;
	}
	
	public int getLayerIndex() {
		return this.layerIndex;
	}
	
	public String getNodeName() {
		return this.nodeName;
	}

	public int getNodeType() { 
		return this.nodeType;
	}

	public void update(double[] row, double learningRate, double momentum) {
		if (!weightsAreUpdated) {
			for (int i = 0; i < inputNodes.length; i++) {
				inputNodes[i].update(row, learningRate, momentum);
			}
			weightsAreUpdated = true;
		}
	}
	
	public boolean areWeightsUpdated() {
		 return this.weightsAreUpdated;
	}

	public void reset() {
		if (!Double.isNaN(currentValue) || !Double.isNaN(currentError)) {
			weightsAreUpdated = false;
			currentValue = Double.NaN;
			currentError = Double.NaN;
			for (int i = 0; i < inputNodes.length; i++) {
				inputNodes[i].reset();
			}
		}
	}

	public NNNode[] getInputNodes() {
		return inputNodes;
	}

	public NNNode[] getOutputNodes() {
		return outputNodes;
	}

	public int[] getInputNodeOutputIndices() {
		return inputNodeOutputIndices;
	}

	public int[] getOutputNodeInputIndices() {
		return outputNodeInputIndices;
	}

	protected boolean connectInput(NNNode inputNode, int inputNodeOutputIndex) {
		NNNode[] newInputNodes = new NNNode[inputNodes.length + 1];
		System.arraycopy(inputNodes, 0, newInputNodes, 0, inputNodes.length);
		newInputNodes[newInputNodes.length - 1] = inputNode;
		inputNodes = newInputNodes;
		
		int[] newInputNodeOutputIndices = new int[inputNodeOutputIndices.length + 1];
		System.arraycopy(inputNodeOutputIndices, 0, newInputNodeOutputIndices, 0, inputNodeOutputIndices.length);
		newInputNodeOutputIndices[newInputNodeOutputIndices.length - 1] = inputNodeOutputIndex;
		inputNodeOutputIndices = newInputNodeOutputIndices;
		
		return true;
	}

	protected boolean connectOutput(NNNode outputNode, int outputNodeInputIndex) {
		NNNode[] newOutputNodes = new NNNode[outputNodes.length + 1];
		System.arraycopy(outputNodes, 0, newOutputNodes, 0, outputNodes.length);
		newOutputNodes[newOutputNodes.length - 1] = outputNode;
		outputNodes = newOutputNodes;
		
		int[] newOutputNodeInputIndices = new int[outputNodeInputIndices.length + 1];
		System.arraycopy(outputNodeInputIndices, 0, newOutputNodeInputIndices, 0, outputNodeInputIndices.length);
		newOutputNodeInputIndices[newOutputNodeInputIndices.length - 1] = outputNodeInputIndex;
		outputNodeInputIndices = newOutputNodeInputIndices;
		
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
				for (int i = deleteIndex + 1; i < numberOfInputs; i++) {
					inputNodes[i - 1] = inputNodes[i];
					inputNodeOutputIndices[i - 1] = inputNodeOutputIndices[i];
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
		
		return removed;
	}

	protected boolean disconnectOutput(NNNode outputNode, int outputNodeInputIndex) {
		int deleteIndex = -1;
		boolean removed = false;
		int numberOfOutputs = outputNodes.length;
		do {
			deleteIndex = -1;
			for (int i = 0; i < outputNodes.length; i++) {
				if (outputNode == outputNodes[i] && (outputNodeInputIndex == -1 || outputNodeInputIndex == outputNodeInputIndices[i])) {
					deleteIndex = i;
					break;
				}
			}

			if (deleteIndex >= 0) {
				for (int i = deleteIndex + 1; i < numberOfOutputs; i++) {
					outputNodes[i - 1] = outputNodes[i];
					outputNodeInputIndices[i - 1] = outputNodeInputIndices[i];
					outputNodes[i - 1].inputNodeOutputIndices[outputNodeInputIndices[i - 1]] = i - 1;
				}
				numberOfOutputs--;
				removed = true;
			}
		} while (outputNodeInputIndex == -1 && deleteIndex != -1);

		NNNode[] newOutputNodes = new NNNode[numberOfOutputs];
		System.arraycopy(outputNodes, 0, newOutputNodes, 0, numberOfOutputs);
		outputNodes = newOutputNodes;
		
		int[] newOutputNodeInputIndices = new int[numberOfOutputs];
		System.arraycopy(outputNodeInputIndices, 0, newOutputNodeInputIndices, 0, numberOfOutputs);
		outputNodeInputIndices = newOutputNodeInputIndices;
		
		return removed;
	}

	public static boolean connect(NNNode firstNode, NNNode secondNode) {
		disconnect(firstNode, secondNode);

		if (!firstNode.connectOutput(secondNode, secondNode.inputNodes.length)) {
			return false;
		}
		
		if (!secondNode.connectInput(firstNode, firstNode.outputNodes.length - 1)) {
			firstNode.disconnectOutput(secondNode, secondNode.inputNodes.length);
			return false;
		}
		
		return true;
	}

	public static boolean disconnect(NNNode firstNode, NNNode secondNode) {
		return firstNode.disconnectOutput(secondNode, -1) && secondNode.disconnectInput(firstNode, -1);
	}
}
