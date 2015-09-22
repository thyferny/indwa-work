/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterDecisionTree.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.operator.randomforest.RandomForestModel;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelTree;

 public class VisualAdapterRandomForest extends VisualAdapterCartTree {
	 
	public static final VisualAdapterRandomForest INSTANCE = new VisualAdapterRandomForest();
	//only show 20 trees in UI
	private static final int TREE_NUMBER_LIMIT = 20;
  
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
	 
		Object model = null;
		 
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			model = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			
			Tree tree =null;
			if(model instanceof RandomForestModel){
				
				List<SingleModel> trees = ((  RandomForestModel) model).getModelList();
				
				List<VisualizationModel> models = new ArrayList<VisualizationModel>(); 
				int index = 0 ;
				for (Iterator iterator = trees.iterator(); iterator.hasNext();) {
					SingleModel singleModel = (SingleModel) iterator.next();
					String oobLable = " " ;
					double oobError = 0 ;
					if(singleModel instanceof RegressionTreeModel){
						tree=(((RegressionTreeModel) singleModel).getRoot());
						oobLable = VisualNLS.getMessage(VisualNLS.OOB_VALUE_LOSS, locale); 
						  oobError = ((RandomForestModel) model).getOobLoss().get(index);
						  oobLable = oobLable + " = "+ oobError+" \n";
						  oobLable = oobLable+ "OOB MAPE" + " = "+  ((RandomForestModel) model).getOobMape().get(index);
					}else if(singleModel instanceof DecisionTreeModel){
						tree=(((DecisionTreeModel) singleModel).getRoot());
						oobLable = VisualNLS.getMessage(VisualNLS.OOB_Estimate_Error, locale);  
						  oobError = ((RandomForestModel) model).getOobEstimateError().get(index);
						  oobLable = oobLable + " = "+ oobError;
						  
					}
					if(index >= TREE_NUMBER_LIMIT){
						break;
					}
					if(tree != null)  {
						index=index+1;
						VisualAdapterCartTree visualAdapterCartTree = new VisualAdapterCartTree		();
						VisualizationModelTree treeVisualModel = visualAdapterCartTree.createTreeModel(tree, "Tree_"+index);
					
						
						treeVisualModel.addErrorMessage(oobLable) ;
						models.add(treeVisualModel) ;
					
					}
					
				}
				VisualizationModelComposite vModel = new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName(), models);
				return vModel;
			}
			
		} 
		return null;
	}
  
}
