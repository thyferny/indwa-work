/**
 * ClassName NeuralNetwork.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;

/**
 * Eason
 */
public class NeuralNetworkConfig extends AbstractModelTrainerConfig {

	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.NeuralNetworkTreeVisualizationType";

	public static final String ConstTraining_cycles = "training_cycles"; // int
	public static final String ConstLearning_rate = "learning_rate"; // double
	public static final String ConstMomentum = "momentum";// double
	public static final String ConstDecay = "decay";// boolean;
	public static final String ConstNormalize = "normalize";// boolean;
	public static final String ConstError_epsilon = "error_epsilon";// float;
	public static final String ConstLocal_random_seed = "local_random_seed";// fs
	public static final String ConstFetchSize = "fetchSize";
	public static final String ConstAdjustPer = "adjust_per";
	private final static ArrayList<String> parameters = new ArrayList<String>();

	public static final String ConstHidden_layers = "hidden_layers";
	static {
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		
		parameters.add(ConstTraining_cycles);

		parameters.add(ConstLearning_rate);
		parameters.add(ConstMomentum);

		parameters.add(ConstDecay);
		parameters.add(ConstNormalize);

		parameters.add(ConstError_epsilon);
		parameters.add(ConstLocal_random_seed);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstFetchSize);
		parameters.add(ConstAdjustPer);
	}

	public NeuralNetworkConfig(String columnnames, String dependentColumn) {
		this();
		setColumnNames(columnnames);
		setDependentColumn(dependentColumn);
 	}

	public NeuralNetworkConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	private String training_cycles = null;
	private String error_epsilon = null;

	private String learning_rate = null;
	private String local_random_seed = null;

	private String momentum = null;
	private String decay = null;
	private String normalize = null;

	private String fetchSize = null;
	private String adjust_per = null;
	
	private AnalysisHiddenLayersModel hiddenLayersModel;
 

	public AnalysisHiddenLayersModel getHiddenLayersModel() {
		return hiddenLayersModel;
	}

	public void setHiddenLayersModel(AnalysisHiddenLayersModel hiddenLayersModel) {
		this.hiddenLayersModel = hiddenLayersModel;
	}

	public String getAdjust_per() {
		return adjust_per;
	}

	public void setAdjust_per(String adjustPer) {
		adjust_per = adjustPer;
	}

	public String getTraining_cycles() {
		return training_cycles;
	}

	public void setTraining_cycles(String trainingCycles) {
		training_cycles = trainingCycles;
	}

	public String getLearning_rate() {
		return learning_rate;
	}

	public void setLearning_rate(String learningRate) {
		learning_rate = learningRate;
	}

	public String getLocal_random_seed() {
		return local_random_seed;
	}


	public void setLocal_random_seed(String localRandomSeed) {
		local_random_seed = localRandomSeed;
	}


	public String getError_epsilon() {
		return error_epsilon;
	}

	public void setError_epsilon(String errorEpsilon) {
		error_epsilon = errorEpsilon;
	}

	/**
	 * @return the momentum
	 */
	public String getMomentum() {
		return momentum;
	}

	/**
	 * @param momentum
	 *            the momentum to set
	 */
	public void setMomentum(String momentum) {
		this.momentum = momentum;
	}

	/**
	 * @return the decay
	 */
	public String getDecay() {
		return decay;
	}

	/**
	 * @param decay
	 *            the decay to set
	 */
	public void setDecay(String decay) {
		this.decay = decay;
	}

	/**
	 * @return the normalize
	 */
	public String getNormalize() {
		return normalize;
	}

	/**
	 * @param normalize
	 *            the normalize to set
	 */
	public void setNormalize(String normalize) {
		this.normalize = normalize;
	}

	/**
	 * @return the fetchSize
	 */
	public String getFetchSize() {
		return fetchSize;
	}

	/**
	 * @param fetchSize
	 *            the fetchSize to set
	 */
	public void setFetchSize(String fetchSize) {
		this.fetchSize = fetchSize;
	}

}
