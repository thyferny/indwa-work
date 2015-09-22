/**
 * ClassName AdaboostConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;
/**
 * @author Shawn
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;

public class AdaboostConfig extends AbstractModelTrainerConfig {
	private static final ArrayList<String> parameters = new ArrayList<String>();

	private List<AbstractModelTrainerConfig> configlist = new LinkedList<AbstractModelTrainerConfig>();

	private Map<String,AbstractModelTrainerConfig> nameConfigMap = new HashMap<String,AbstractModelTrainerConfig>();
	
	private AnalysisAdaboostPersistenceModel adaboostUIModel;
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.AdaboostWeightTextVisualizationType";//only for not null
	
	static{
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstDependentColumn);
		parameters.add(ConstForceRetrain);
	}
	
	public AdaboostConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public boolean isListEmpty() {
		return configlist.isEmpty();
	}

	public AbstractModelTrainerConfig removeconfig() {
		return configlist.remove(0);
	}

	public void addconfig(AbstractModelTrainerConfig tempconfig) {
		configlist.add(tempconfig);
	}
	
	public void addConfigName(String configName,AbstractModelTrainerConfig config){
		nameConfigMap.put(configName,config);
	}

	public List<AbstractModelTrainerConfig> getConfiglist() {
		return configlist;
	}

	public AnalysisAdaboostPersistenceModel getAdaboostUIModel() {
		return adaboostUIModel;
	}

	public void setAdaboostUIModel(AnalysisAdaboostPersistenceModel adaboostUIModel) {
		this.adaboostUIModel = adaboostUIModel;
	}

	public Map<String, AbstractModelTrainerConfig> getNameConfigMap() {
		return nameConfigMap;
	}

	public void setNameConfigMap(
			Map<String, AbstractModelTrainerConfig> nameConfigMap) {
		this.nameConfigMap = nameConfigMap;
	}

}
