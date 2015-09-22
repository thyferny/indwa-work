package com.alpine.datamining.operator.neuralnet.sequential;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.Parameter;

public class NNParameter implements Parameter {
	private List<String[]> hiddenLayers = new ArrayList<String[]>();
	private int trainingIteration = 500; 
	private double errorEpsilon = 0.00001;
	private double learningRate = 0.3;
	private double momentum = 0.2;
	private boolean decay = false;
	private boolean adjustPerRow = true;
	private boolean normalize = true;
	private int randomSeed = -1;
	private int fetchSize = 10000;
//	private String columnNames;
	public List<String[]> getHiddenLayers() {
		return hiddenLayers;
	}
	public void setHiddenLayers(List<String[]> hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}
	public int getTrainingIteration() {
		return trainingIteration;
	}
	public void setTrainingIteration(int trainingIteration) {
		this.trainingIteration = trainingIteration;
	}
	public double getErrorEpsilon() {
		return errorEpsilon;
	}
	public void setErrorEpsilon(double errorEpsilon) {
		this.errorEpsilon = errorEpsilon;
	}
	public double getLearningRate() {
		return learningRate;
	}
	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}
	public double getMomentum() {
		return momentum;
	}
	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}
	public boolean isDecay() {
		return decay;
	}
	public void setDecay(boolean decay) {
		this.decay = decay;
	}
	public boolean isNormalize() {
		return normalize;
	}
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
	public int getRandomSeed() {
		return randomSeed;
	}
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
	}
	public int getFetchSize() {
		return fetchSize;
	}
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
	public boolean isAdjustPerRow() {
		return adjustPerRow;
	}
	public void setAdjustPerRow(boolean adjustPerRow) {
		this.adjustPerRow = adjustPerRow;
	}
}
