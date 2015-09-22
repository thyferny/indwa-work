/**
 * ClassName VisualizationManagerImpl.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.AnalyticNodeImpl;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.DecisionTreeConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.SVMClassificationConfig;
import com.alpine.datamining.api.impl.db.trainer.CartTrainer;
import com.alpine.datamining.api.impl.db.trainer.DecisionTreeTrainer;
import com.alpine.datamining.api.impl.output.AdaBoostAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.impl.visual.BoxAndWhiskerImageVisualizationType;
import com.alpine.datamining.api.impl.visual.CartTreeVisualizationType;
import com.alpine.datamining.api.impl.visual.DbTableAnalysisImageVisualizationType;
import com.alpine.datamining.api.impl.visual.DecisionTreeVisualizationType;
import com.alpine.datamining.api.impl.visual.FrequencyShapeVisualizationType;
import com.alpine.datamining.api.impl.visual.GINIImageVisualizationType;
import com.alpine.datamining.api.impl.visual.HadoopDecesionTreeVisualizationType;
import com.alpine.datamining.api.impl.visual.HadoopKmeansClusterVisualizationType;
import com.alpine.datamining.api.impl.visual.HadoopKmeansProfilesVisualizationType;
import com.alpine.datamining.api.impl.visual.HadoopLinearRegressionNormalProbabilityPlotVisualization;
import com.alpine.datamining.api.impl.visual.HadoopLinearRegressionResidualPlotVisualization;
import com.alpine.datamining.api.impl.visual.HadoopMultiOutputTableVisualizationType;
import com.alpine.datamining.api.impl.visual.HistogramImageVisualizationType;
import com.alpine.datamining.api.impl.visual.HistogramShapeVisualizationType;
import com.alpine.datamining.api.impl.visual.KMeansClusterAllVisualizationType;
import com.alpine.datamining.api.impl.visual.KMeansClusterScatterVisualizationType;
import com.alpine.datamining.api.impl.visual.KSImageVisualizationType;
import com.alpine.datamining.api.impl.visual.LIFTImageVisualizationType;
import com.alpine.datamining.api.impl.visual.LinearRegressionNormalProbabilityPlotVisualization;
import com.alpine.datamining.api.impl.visual.LinearRegressionResidualPlotVisualization;
import com.alpine.datamining.api.impl.visual.NeuralNetworkTreeVisualizationType;
import com.alpine.datamining.api.impl.visual.ROCImageVisualizationType;
import com.alpine.datamining.api.impl.visual.ScatterImageVisualizationType;
import com.alpine.datamining.api.impl.visual.ScatterMatrixImageVisualizationType;
import com.alpine.datamining.api.impl.visual.UnivariateImageVisualizationType;
import com.alpine.datamining.api.impl.visual.ValueNumericVisualizationType;
import com.alpine.datamining.api.impl.visual.ValueShapeVisualizationType;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.adboost.AdaboostModel;
import com.alpine.datamining.operator.adboost.AdaboostSingleModel;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.operator.randomforest.RandomForestModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.svm.SVMClassificationModel;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import org.apache.log4j.Logger;

/**
 * @author John Zhao
 *
 */
public class VisualizationManagerImpl implements VisualizationManager {
    private static final Logger itsLogger =Logger.getLogger(VisualizationManagerImpl.class);
    private DateFormat dataFormat=new SimpleDateFormat("hh:mm:ss");
	private  boolean inCommandMode=false;
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationManager#visul(com.alpine.datamining.api.AnalyzerOutPut)
	 */
	@Override
	public  void visual(AnalyticOutPut outPut,boolean drawChart) throws Exception{
		if(drawChart==false){
			return;
		}
		String vTypeClass=outPut.getVisualizationTypeClass();
		
		if(outPut instanceof AnalyzerOutPutTrainModel)
		{
			AnalyzerOutPutTrainModel trainModel=(AnalyzerOutPutTrainModel)outPut;
			Model model=trainModel.getEngineModel().getModel();
			if(model instanceof RandomForestModel){
				CompositeVisualizationOutPut cvOut=new CompositeVisualizationOutPut();
			 	generateRandomForestOutput(vTypeClass,outPut, (RandomForestModel) model, cvOut,  drawChart);
			 	
				return;
			}
		}
		
		if(vTypeClass==null||vTypeClass.trim().length()==0){
			return;
		}
		
		else if(outPut instanceof AnalyzerOutPutTrainModel){//For adaBoost
			AnalyzerOutPutTrainModel trainModel=(AnalyzerOutPutTrainModel)outPut;
			Model model=trainModel.getEngineModel().getModel();
			CompositeVisualizationOutPut cvOut=new CompositeVisualizationOutPut();
			if(model instanceof AdaboostModel){
			 	generateAdaboostOutput(vTypeClass,outPut, model, cvOut,  drawChart);
				return;
			}
			if(model instanceof RandomForestModel){
			 	generateRandomForestOutput(vTypeClass,outPut, (RandomForestModel) model, cvOut,  drawChart);
			 	
				return;
			}
		}else if(outPut instanceof HadoopMultiAnalyticFileOutPut){
			CompositeVisualizationOutPut cvOut=new CompositeVisualizationOutPut();
			cvOut.setName(outPut.getAnalyticNode().getName());
			StringTokenizer sTokenizaer=new StringTokenizer(vTypeClass,",");
			int i = 0;
			while(sTokenizaer.hasMoreTokens()){
				String className=sTokenizaer.nextToken();
				VisualizationType vType=(VisualizationType)
						Class.forName(className).newInstance();
				
			 	((HadoopMultiOutputTableVisualizationType)vType).setIndex(i);
			 	if(isChartVisualizationType(className)==true&&drawChart==false){
			 		continue;
			 	}
				VisualizationOutPut vOut=vType.generateOutPut(outPut);			
				if(vOut!=null){
					vOut.setVisualizationType(vType);
					if(vOut.getAnalyzer()==null&&outPut.getDataAnalyzer()!=null){
						vOut.setAnalyzer(outPut.getDataAnalyzer());
					}
				}
				cvOut.addChildOutPut(vOut);
				i++;
			}	
			outPut.setVisualizationOutPut(cvOut);
			return;
		}
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Before Visualization:"+outPut.getAnalyticNode().getName()
				+" total memory="+Runtime.getRuntime().totalMemory()
				+" free memory="+Runtime.getRuntime().freeMemory());
		}
		//class1,class2,class3,
		StringTokenizer sTokenizaer=new StringTokenizer(vTypeClass,",");
		VisualizationOutPut vOut=null;
		if(sTokenizaer.countTokens()==1){
			vOut=generateSimpleOutPut(vTypeClass,   outPut,drawChart);
		}else{
			vOut=generateCompositeOutPut(sTokenizaer,outPut,drawChart);
		}
		
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("After Visualization:"
					+ outPut.getAnalyticNode().getName() + " total memory="
					+ Runtime.getRuntime().totalMemory() + " free memory="
					+ Runtime.getRuntime().freeMemory());
		}
		outPut.setVisualizationOutPut(vOut);
	}

 
	
	private void generateRandomForestOutput(String vTypeClass,
			AnalyticOutPut outPut, RandomForestModel model,
			CompositeVisualizationOutPut cvOut, boolean drawChart) {
		
 
	 
		
		for(int i=0;i<model.getModelNum()&&i<20;i++){
			 SingleModel singleModel = model.getModelList().get(i);
			 if (singleModel instanceof DecisionTreeModel){
				 DecisionTreeVisualizationType type = new DecisionTreeVisualizationType();
				 cvOut.addChildOutPut(type.generateVOut("Tree_"+(i+1), (DecisionTreeModel)singleModel) ) ;
			 }else{
				 CartTreeVisualizationType type = new CartTreeVisualizationType();
				 cvOut.addChildOutPut(type.generateVOut("Tree_"+(i+1),  singleModel) ) ;
			 }
			 
			 
		}	
		outPut.setVisualizationOutPut(cvOut);
		
	}

	private boolean isChartVisualizationType(String className) {
		
		if(BoxAndWhiskerImageVisualizationType.class.getName().equals(className)||
				DbTableAnalysisImageVisualizationType.class.getName().equals(className)||
				GINIImageVisualizationType.class.getName().equals(className)||
				HadoopLinearRegressionNormalProbabilityPlotVisualization.class.getName().equals(className)||
				HadoopLinearRegressionResidualPlotVisualization.class.getName().equals(className)||
				HistogramImageVisualizationType.class.getName().equals(className)||
				KSImageVisualizationType.class.getName().equals(className)||
				LIFTImageVisualizationType.class.getName().equals(className)||
				LinearRegressionNormalProbabilityPlotVisualization.class.getName().equals(className)||
				LinearRegressionResidualPlotVisualization.class.getName().equals(className)||
				ROCImageVisualizationType.class.getName().equals(className)||
				ScatterImageVisualizationType.class.getName().equals(className)||
				ScatterMatrixImageVisualizationType.class.getName().equals(className)||
				UnivariateImageVisualizationType.class.getName().equals(className)||
				
				
				CartTreeVisualizationType.class.getName().equals(className)||
				DecisionTreeVisualizationType.class.getName().equals(className)||
				HadoopDecesionTreeVisualizationType.class.getName().equals(className)||
				NeuralNetworkTreeVisualizationType.class.getName().equals(className)||
				
				
				FrequencyShapeVisualizationType.class.getName().equals(className)||
				HadoopKmeansClusterVisualizationType.class.getName().equals(className)||
				HadoopKmeansProfilesVisualizationType.class.getName().equals(className)||
				HistogramShapeVisualizationType.class.getName().equals(className)||
				KMeansClusterAllVisualizationType.class.getName().equals(className)||
				KMeansClusterScatterVisualizationType.class.getName().equals(className)||
				
				ValueNumericVisualizationType.class.getName().equals(className)||		
				ValueShapeVisualizationType.class.getName().equals(className)){
			return true;
		}
		
		else{
			return false;
		}
	}

	private void generateAdaboostOutput(String vTypeClass,AnalyticOutPut outPut, Model model,
			CompositeVisualizationOutPut cvOut, boolean drawChart) throws Exception { 
		AdaboostModel adaboostModel=(AdaboostModel)model;
		
		VisualizationOutPut summaryOut=generateSimpleOutPut(vTypeClass,   outPut,drawChart);
		if(summaryOut!=null){
			cvOut.addChildOutPut(summaryOut);
		}
		
		for(int i=0;i<adaboostModel.getModelNum();i++){
			AdaboostSingleModel singleModel=adaboostModel.getModel(i);
			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(singleModel.getModel());
			analyzerOutPutModel.getEngineModel().setName(singleModel.getName());
			AnalyticNode analyticNode=new AnalyticNodeImpl();
			analyticNode.setName(singleModel.getName());
			analyticNode.setAdaBoost(true);
			analyzerOutPutModel.setAnalyticNode(analyticNode);
			AdaBoostAnalyzerOutPutTrainModel adaBoostAnalyzerOutPutTrainModel=(AdaBoostAnalyzerOutPutTrainModel)outPut;
			DataAnalyzer  analyzer=adaBoostAnalyzerOutPutTrainModel.getDataAnalyzer(singleModel.getName());
			if(analyzer!=null){
				analyzer.setOutPut(analyzerOutPutModel);
				analyzerOutPutModel.setDataAnalyzer(analyzer);
			}
			AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
			analyzerOutPutModel.setAnalyticNodeMetaInfo(nodeMetaInfo);	
			VisualizationOutPut vOut=null;
			if(singleModel.getModel() instanceof LogisticRegressionModelDB){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_DESCRIPTION);				
				String visualizationTypeClass=LogisticRegressionConfigGeneral.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}else if(singleModel.getModel() instanceof NBModel){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.NAIVE_BAYES_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.NAIVE_BAYES_TRAIN_DESCRIPTION);				
				String visualizationTypeClass=NaiveBayesConfig.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}else if(singleModel.getModel() instanceof DecisionTreeModel
					&&singleModel.getType().equals(CartTrainer.class.getCanonicalName())){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.CART_TREE_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.CART_TREE_TRAIN_DESCRIPTION);			
				String visualizationTypeClass=CartConfig.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}else if(singleModel.getModel() instanceof DecisionTreeModel
					&&singleModel.getType().equals(DecisionTreeTrainer.class.getCanonicalName())){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.DECISION_TREE_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.DECISION_TREE_TRAIN_DESCRIPTION);			
				String visualizationTypeClass=DecisionTreeConfig.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}else if(singleModel.getModel() instanceof NNModel){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.NEURALNETWORK_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.NEURALNETWORK_TRAIN_DESCRIPTION);				
				String visualizationTypeClass=NeuralNetworkConfig.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}else if(singleModel.getModel() instanceof SVMClassificationModel){
				nodeMetaInfo.setAlgorithmName(SDKLanguagePack.SVM_CL_TRAIN_NAME);
				nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.SVM_CL_TRAIN_DESCRIPTION);			
				String visualizationTypeClass=SVMClassificationConfig.VISUALIZATION_TYPE;
				vOut = generateVOutput(analyzerOutPutModel,
						visualizationTypeClass,drawChart);
			}
			cvOut.addChildOutPut(vOut);
		}	
		outPut.setVisualizationOutPut(cvOut);
	}

	private VisualizationOutPut generateVOutput(
			AnalyzerOutPutTrainModel analyzerOutPutModel,
			String visualizationTypeClass, boolean drawChart) throws Exception { 
		VisualizationOutPut vOut;
		StringTokenizer sTokenizaer=new StringTokenizer(visualizationTypeClass,",");
		if(sTokenizaer.countTokens()==1){
			vOut=generateSimpleOutPut(visualizationTypeClass,analyzerOutPutModel,drawChart);
		}else{
			vOut=generateCompositeOutPut(sTokenizaer,analyzerOutPutModel,drawChart);
		}
		return vOut;
	}

	/**
	 * @param sTokenizaer
	 * @param outPut
	 * @param drawChart 
	 * @return
	 * @throws Exception 
	 */
	private CompositeVisualizationOutPut generateCompositeOutPut(
			StringTokenizer sTokenizaer, AnalyticOutPut outPut, boolean drawChart) throws Exception {
		CompositeVisualizationOutPut cvOut=new CompositeVisualizationOutPut();
		cvOut.setName(outPut.getAnalyticNode().getName());
		while(sTokenizaer.hasMoreTokens()){
			String className=sTokenizaer.nextToken();
			VisualizationOutPut vOut = generateSimpleOutPut(className,outPut,drawChart);
			if(vOut!=null){
				cvOut.addChildOutPut(vOut);
			}
		}
		return cvOut;
	}

	/**
	 * @param vTypeClass 
	 * @param outPut 
	 * @return
	 * @throws Exception 
	 */
	private VisualizationOutPut generateSimpleOutPut(String vTypeClass, AnalyticOutPut outPut,boolean drawChart)
	throws  Exception {
		if(isChartVisualizationType(vTypeClass)==true&&drawChart==false){
			return null;
		}
		VisualizationType vType=(VisualizationType)
		Class.forName(vTypeClass).newInstance();
		
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("["+dataFormat.format(Calendar.getInstance().getTime())+"]"
				+"VisualizationManager:visual for "	+outPut.getAnalyticNode().getName()+" started");
		}
		VisualizationOutPut vOut=vType.generateOutPut(outPut);
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("["+dataFormat.format(Calendar.getInstance().getTime())+"]"
				+"VisualizationManager:visual for "	+outPut.getAnalyticNode().getName()+" ended");
		}
		if(vOut!=null){
			vOut.setVisualizationType(vType);
			if(vOut.getAnalyzer()==null&&outPut.getDataAnalyzer()!=null){
				vOut.setAnalyzer(outPut.getDataAnalyzer());
			}
		}


		return vOut;
	}

	public boolean isInCommandMode() {
		return inCommandMode;
	}

	public void setInCommandMode(boolean inCommandMode) {
		this.inCommandMode = inCommandMode;
	}
}
