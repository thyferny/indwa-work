/**
 * ClassName EngineModel
 *
 * Version information: 1.00
 *
 * Data: 2010-4-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.io.Serializable;

import com.alpine.datamining.api.impl.hadoop.models.*;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.operator.adboost.AdaboostModel;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.operator.fpgrowth.AssociationRules;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.datamining.operator.randomforest.RandomForestModel;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.svd.SVDModel;
import com.alpine.datamining.operator.svm.SVMClassificationModel;
import com.alpine.datamining.operator.svm.SVMNoveltyDetectionModel;
import com.alpine.datamining.operator.svm.SVMRegressionModel;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.woe.WOEModel;

/**
 * @author John Zhao
 *
 */
public class EngineModel implements Serializable {
	
	public static final String MPDE_TYPE_NB=NBModel.class.getName();
	public static final String MPDE_TYPE_LOR=LogisticRegressionModelDB.class.getName();
	public static final String MPDE_TYPE_LIR=LinearRegressionModelDB.class.getName();
	public static final String MPDE_TYPE_NEU=NNModel.class.getName();
	public static final String MPDE_TYPE_TREE_CLASSIFICATION=DecisionTreeModel.class.getName();
	public static final String MPDE_TYPE_TREE_RANDOM_FOREST = RandomForestModel.class.getName();
	
	public static final String MPDE_TYPE_TREE_REGRESSION=RegressionTreeModel.class.getName();
	public static final String MPDE_TYPE_ASSOCIATION=AssociationRules.class.getName();
	public static final String MPDE_TYPE_TIMESERIES=ARIMAModel.class.getName();
	public static final String MPDE_TYPE_SVM_C=SVMClassificationModel.class.getName();
	public static final String MPDE_TYPE_SVM_ND=SVMNoveltyDetectionModel.class.getName();
	public static final String MPDE_TYPE_SVM_R=SVMRegressionModel.class.getName();
	public static final String MPDE_TYPE_SVD=SVDModel.class.getName();
	public static final String MPDE_TYPE_ADABOOST=AdaboostModel.class.getName();
	public static final String MPDE_TYPE_WOE=WOEModel.class.getName();
	public static final String MPDE_TYPE_PLDA=PLDAModel.class.getName();
	public static final String MPDE_TYPE_EMCLUSTER=EMModel.class.getName();
	public static final String MPDE_TYPE_LR_SPLITMODEL="splitModel"+MPDE_TYPE_LOR;
	public static final String MPDE_TYPE_LIR_SPLITMODEL="splitModel"+MPDE_TYPE_LIR;
	public static final String MPDE_TYPE_HADOOP_LIR=LinearRegressionHadoopModel.class.getCanonicalName();
	public static final String MPDE_TYPE_HADOOP_LOR=LogisticRegressionHadoopModel.class.getCanonicalName();
	public static final String MPDE_TYPE_HADOOP_TREE_CLASSIFICATION=DecisionTreeHadoopModel.class.getName();
	public static final String MPDE_TYPE_HADOOP_ARIMA=ARIMAHadoopModel.class.getName();
    public static final String MPDE_TYPE_HADOOP_NB = NaiveBayesHadoopModel.class.getName();

	public String getVersion(){
	//	model.getVersion();
		return "1.3.0";
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8216481978184861127L;
	private Model model;
	private String modelType=null;
	private String name;
 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public EngineModel( ){
		 
	}
	public String getModelType(){
		if(model!=null){
			if(model instanceof LogisticRegressionGroupModel){
				return MPDE_TYPE_LR_SPLITMODEL;
			}else if (model instanceof LinearRegressionGroupGPModel){
				return MPDE_TYPE_LIR_SPLITMODEL;
			}else if (model instanceof NBModel){
				return MPDE_TYPE_NB;
			}else if (model instanceof LogisticRegressionModelDB){
				return MPDE_TYPE_LOR;
			}else if (model instanceof LinearRegressionModelDB){
				return MPDE_TYPE_LIR;
			}else if(model instanceof AdaboostModel){
				return MPDE_TYPE_ADABOOST;
			}else if(model instanceof NNModel){
				return MPDE_TYPE_NEU;
			}else if(model instanceof ARIMAModel){
				return MPDE_TYPE_TIMESERIES;
			}else if(model instanceof SVMNoveltyDetectionModel){
				return MPDE_TYPE_SVM_ND;
			}else if(model instanceof SVMClassificationModel){
				return MPDE_TYPE_SVM_C;
			}else if(model instanceof SVMRegressionModel){
				return MPDE_TYPE_SVM_R;
			}else if(model instanceof WOEModel)
			{	
				return MPDE_TYPE_WOE;
			}else if(model instanceof PLDAModel)
			{	
				return MPDE_TYPE_PLDA;
			}else if(model instanceof LinearRegressionHadoopModel)
			{
				return MPDE_TYPE_HADOOP_LIR;
			}else if(model instanceof EMModel)
			{
				return MPDE_TYPE_EMCLUSTER;
			} else if (model instanceof NaiveBayesHadoopModel)
            {
                return MPDE_TYPE_HADOOP_NB;
            }
			else {
				return model.getClass().getName();
			}
		}else{
			return modelType;
		}
	}
	
	public String getDependentColumn(){
		if(model!=null){
			if (model instanceof LinearRegressionModelDB){
				if(((LinearRegressionModelDB)model).getLabel()!=null){
					return ((LinearRegressionModelDB)model).getLabel().getName();
				}			
			}		
		}
		return null;
	}
	public String toString(){
		if(model!=null){
			return model.toString();
		}else{
			return null;
		}
	}
	public String toOutPutString(){
		if(model!=null){
			return model.toString();
		}else{
			return null;
		}
	}
	
	
}
