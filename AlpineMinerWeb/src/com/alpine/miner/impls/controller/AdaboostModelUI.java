/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * QuantileFieldsModelUI.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 31, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceItem;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayer;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;

import java.util.*;

/**
 * @author sam_zang
 *
 */
public class AdaboostModelUI {
		
	class AdaboostItemUI {
		String adaName;
		String adaType;
		String icon;
		String classname;
		private List<PropertyDTO> propertyList;
		
		AdaboostItemUI(String type, String name) {
			this.adaType = type;
			this.adaName = name;
			setIconAndClassName(type);
		}

		/**
		 * @param type
		 * @return
		 */
		private void setIconAndClassName(String type) {
			int idx = type.lastIndexOf('.');
			
			if (idx > 0) {
				String cls = type.substring(idx + 1);
				ResourceManager rmgr = ResourceManager.getInstance();
				String[] icons = rmgr.getClassIcons(cls);
				this.icon = icons[1];
				this.classname = cls;
			}
		}
	}

	private Locale locale = Locale.getDefault(); 
	
	public AdaboostModelUI(AdaboostPersistenceModel obj,Locale locale) {
		this.locale=locale;
		this.adaboostDataList = new LinkedList<AdaboostItemUI>();
		this.adaboostTemplateList = new LinkedList<AdaboostItemUI>();
		initTamplates(locale);

		if (obj == null || obj.getAdaboostUIItems() == null) {
			return;
		}

		for (AdaboostPersistenceItem item : obj.getAdaboostUIItems()) {
			AdaboostItemUI ui = new AdaboostItemUI(item.getAdaType(),
					item.getAdaName());
			ui.propertyList = new LinkedList<PropertyDTO>();

			HiddenLayersModel model = getHiddenLayersModel(ui, item);
			Map<String, String> map = item.getParameterMap();
			Set<String> keyList = map.keySet();

			for (String key : keyList) {
				if (key.startsWith("HiddenLayer")
						|| key.startsWith("hiddenlayer")) {
					continue;
				}
				PropertyDTO prop = new PropertyDTO(key, map.get(key),locale);
				if (key.equals("hidden_layers")) {
					prop.setHiddenLayersModel(model);
				}
				ui.propertyList.add(prop);
			}
			this.adaboostDataList.add(ui);
		}

	}

	/**
	 * @param ui
	 * @param item
	 * @return
	 */
	private HiddenLayersModel getHiddenLayersModel(AdaboostItemUI ui,
			AdaboostPersistenceItem item) {
		HiddenLayersModel model = null;
		if (ui.classname.equals("NeuralNetworkOperator")) {
			model = new HiddenLayersModel();
			Map<String, String> map = item.getParameterMap();
			Set<String> keyList = map.keySet();
			
			for (String key : keyList) {
				if (key.startsWith("HiddenLayer") == false &&
						key.startsWith("hiddenlayer") == false) {
					continue;
				}
				String num = "1";
				String value = map.get(key);
				if (value != null && value.length() > 0) {
					num = value;
				}
				HiddenLayer layer = new HiddenLayer(key, num);
				model.addHiddenLayer(layer);
			}
			
		}
		return model;
	}
	
	@SuppressWarnings("unused")
	AdaboostModelUI() {}
	
	private List<AdaboostItemUI> adaboostDataList;
	private List<AdaboostItemUI> adaboostTemplateList;

	private void initTamplates(Locale locale) {
		AdaboostItemUI tmp = null;
		
		tmp = CreateNaiveBayesTemplate(locale);
		this.adaboostTemplateList.add(tmp);	
		
		tmp = CreateCartTemplate();
		this.adaboostTemplateList.add(tmp);
		
		tmp = CreateDecisionTreeTemplate();
		this.adaboostTemplateList.add(tmp);
		
		tmp = CreateLogisticRegressionTemplate();
		this.adaboostTemplateList.add(tmp);	
		
		tmp = CreateNeuralNetworkTemplate();
		this.adaboostTemplateList.add(tmp);	
		
		tmp = CreateSVMClassificationTemplate();
		this.adaboostTemplateList.add(tmp);
	}
	
	/**
	 * @return
	 */
	private AdaboostItemUI CreateSVMClassificationTemplate() {
		String adaName = VisualNLS.getMessage(VisualNLS.SVM_Classification , locale); 
		String adaType = "com.alpine.miner.workflow.operator.svm.SVMClassificationOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);	


		String[][] proplist = { 
				{ "nu", "0.001" }, 
				{ "eta", "0.05" },
				{ "gamma", "0.1" }, 
				{ "degree", "2" }, 
				{ "kernelType", "1" }
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @return
	 */
	private AdaboostItemUI CreateNeuralNetworkTemplate() {
		String adaName = VisualNLS.getMessage(VisualNLS.Neural_Network , locale);    
		String adaType = "com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);
		
		String[][] proplist = {
				{ "normalize", "true" },
				{ "fetchSize", "10000" },
				{ "training_cycles", "500" },
				{ "momentum", "0.2" },
				{ "learning_rate", "0.3" },
				{ "hidden_layers", ""},
				{ "adjust_per", "ROW" },
				{ "local_random_seed", "-1" },
				{ "error_epsilon", "0.00001" },
				{ "decay", "false" }
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @return
	 */
	private AdaboostItemUI CreateLogisticRegressionTemplate() {
		String adaName = VisualNLS.getMessage(VisualNLS.Logistic_Regression , locale);   
		String adaType = "com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);
		
		String[][] proplist = {
				{ "epsilon", "0.00000001" },
				{ "max_generations", "25" },
				{ "goodValue", "" }
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @return
	 */
	private AdaboostItemUI CreateDecisionTreeTemplate() {
		String adaName = VisualNLS.getMessage(VisualNLS.Decision_Tree , locale);  
		String adaType = "com.alpine.miner.workflow.operator.decisiontree.DecisionTreeOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);
		
		String[][] proplist = {
				{ "no_pruning", "false" },
				{ "minimal_size_for_split", "4" },
				{ "minimal_gain", "0.1" },
				{ "number_of_prepruning_alternatives", "3" },
				{ "size_threshold_load_data", "10000" },
				{ "maximal_depth", "5" },
				{ "confidence", "0.25" },
				{ "minimal_leaf_size", "2" },
				{ "no_pre_pruning", "false" }
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @return
	 */
	private AdaboostItemUI CreateCartTemplate() {
		String adaName = VisualNLS.getMessage(VisualNLS.Cart_Tree, locale); 
		String adaType = "com.alpine.miner.workflow.operator.decisiontree.CartOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);
		
		String[][] proplist = {
				{ "no_pruning", "false" },
				{ "minimal_size_for_split", "4" },
				{ "number_of_prepruning_alternatives", "3" },
				{ "size_threshold_load_data", "10000" },
				{ "maximal_depth", "5" },
				{ "confidence", "0.25" },
				{ "minimal_leaf_size", "2" },
				{ "no_pre_pruning", "false" }
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @return
	 */
	private AdaboostItemUI CreateNaiveBayesTemplate(Locale locale) {
		String adaName = VisualNLS.getMessage(VisualNLS.Naive_Bayes, locale);
		String adaType = "com.alpine.miner.workflow.operator.naivebayes.NaiveBayesOperator";
		AdaboostItemUI ui = new AdaboostItemUI(adaType, adaName);
		
		String[][] proplist = {
				{ "calculateDeviance", "false"}	
		};
		addProperties(ui, proplist);
		return ui;
	}

	/**
	 * @param ui
	 * @param proplist
	 */
	private void addProperties(AdaboostItemUI ui, String[][] proplist) {
		ui.propertyList = new LinkedList<PropertyDTO>();
		
		for (String[] data : proplist) {
			String key = data[0];
			String defValue = data[1];
			PropertyDTO prop = new PropertyDTO(key, defValue,locale);				
			ui.propertyList.add(prop);
		}			
	}

	/**
	 *
	 */
	public AdaboostPersistenceModel getValue() {

		AdaboostPersistenceModel model = new AdaboostPersistenceModel();
		List<AdaboostPersistenceItem> list = new LinkedList<AdaboostPersistenceItem>();
		for (AdaboostItemUI ui : this.adaboostDataList) {
			if (ui == null) {
				continue;
			}
			AdaboostPersistenceItem item = new AdaboostPersistenceItem(ui.adaType, ui.adaName);
			Map<String, String> map = new HashMap<String, String>();
			for (PropertyDTO obj : ui.propertyList) {
				
				if (obj.getType() == PropertyDTO.PropertyType.PT_CUSTOM_NEURAL_HIDDEN_LAYERS) {
					HiddenLayersModel hd = obj.getHiddenLayersModel();
					String value = processHiddenLayersModel(map, hd);
					map.put(obj.getName(), value);
				}
				else {
					map.put(obj.getName(), obj.getValue());
				}
			}
			
			item.setParameterMap(map);
			list.add(item);

		}
		model.setAdaboostUIItems(list);
		return model;
	}

	/**
	 * @param map
	 * @param hd
	 */
	private String processHiddenLayersModel(Map<String, String> map,
			HiddenLayersModel hd) {
		if (hd == null) {
			return "";
		}
		List<HiddenLayer> list = hd.getHiddenLayers();
		String value = "";
		for (HiddenLayer layer : list) {
			if (value.length() > 0) {
				value += ";";
			}
			String key = layer.getLayerName();
			String layerSize = "" + layer.getLayerSize();
			value += key + "," + layerSize;
			map.put(key, layerSize);
			
		}
		return value;
	}

}
