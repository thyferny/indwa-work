package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.operator.Parameter;

public class DecisionTreeParameter implements Parameter {
	private int splitMinSize= 4;
	private int MinLeafSize= 2;
	private double MinGain = 0.1;
	private int maxDepth = 5;
	private int prepruningAlternativesNumber = 3;
	private boolean noPrePruning = false;
	private boolean noPruning = false;
	private int thresholdLoadData = 10000;
	private double confidence = 0.25;
	public int getSplitMinSize() {
		return splitMinSize;
	}
	public void setSplitMinSize(int splitMinSize) {
		this.splitMinSize = splitMinSize;
	}
	public int getMinLeafSize() {
		return MinLeafSize;
	}
	public void setMinLeafSize(int minLeafSize) {
		MinLeafSize = minLeafSize;
	}
	public double getMinGain() {
		return MinGain;
	}
	public void setMinGain(double minGain) {
		MinGain = minGain;
	}
	public int getMaxDepth() {
		return maxDepth;
	}
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	public int getPrepruningAlternativesNumber() {
		return prepruningAlternativesNumber;
	}
	public void setPrepruningAlternativesNumber(int prepruningAlternativesNumber) {
		this.prepruningAlternativesNumber = prepruningAlternativesNumber;
	}
	public boolean isNoPrePruning() {
		return noPrePruning;
	}
	public void setNoPrePruning(boolean noPrePruning) {
		this.noPrePruning = noPrePruning;
	}
	public boolean isNoPruning() {
		return noPruning;
	}
	public void setNoPruning(boolean noPruning) {
		this.noPruning = noPruning;
	}
	public int getThresholdLoadData() {
		return thresholdLoadData;
	}
	public void setThresholdLoadData(int thresholdLoadData) {
		this.thresholdLoadData = thresholdLoadData;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	}
