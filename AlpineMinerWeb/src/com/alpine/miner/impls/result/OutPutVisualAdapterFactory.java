/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OutPutVisualAdapterFactory.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.HashMap;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.association.FPGrowthAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ColumnFilterAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.CorrelationAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.FrequencyAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.HistogramAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.InformationValueAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.IntegerToTextTransformationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.NormalizationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.PCAAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ValueAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionAnalyzer;
import com.alpine.datamining.api.impl.db.cluster.KMeansAnalyzer;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.LiftGeneralEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.ROCGeneralEvaluator;
import com.alpine.datamining.api.impl.db.predictor.ARIMARPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVDCalculator;
import com.alpine.datamining.api.impl.db.predictor.SVDLanczosCalculator;
import com.alpine.datamining.api.impl.db.recommendation.RecommendationEvaluationAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.db.variableOptimization.UnivariateVariable;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopConfusionEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopGoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopLiftEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopRocEvaluator;
import com.alpine.datamining.api.impl.hadoop.explorer.*;
import com.alpine.datamining.api.impl.hadoop.kmeans.HadoopKmeansAnalyzer;
import com.alpine.datamining.api.impl.hadoop.models.*;
import com.alpine.datamining.api.impl.hadoop.predictor.HadoopTimeSeriesPredictor;
import com.alpine.datamining.api.impl.output.AdaBoostAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutBoxWhisker;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdatePLDA;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPLDATrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.SVDLanczosAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.operator.randomforest.RandomForestModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.svm.SVMModel;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.woe.WOEModel;
import org.apache.log4j.Logger;
/** 
 * This is the adapter factory for the visual model.
 * It will return the adapter instance for each analytic output.
 * */
public class OutPutVisualAdapterFactory {
    private static Logger itsLogger = Logger.getLogger(OutPutVisualAdapterFactory.class);

    //the barchart and histogram will only support max 30 bars (total)
	public static final int MAX_ELEMENTS_DEFAULT = 100;
	
	public static final int MAX_POINTS_DEFAULT = 200;
 
	//scope number for bar, line number for line charts
	private int maxChartElements = MAX_ELEMENTS_DEFAULT; 
	private int maxPoints = MAX_POINTS_DEFAULT;

	public int getMaxChartElements() {
		return maxChartElements;
	}

	public void setMaxChartElements(int maxChartElements) {
		this.maxChartElements = maxChartElements;
	}

	public int getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

  
	
 	private static OutPutVisualAdapterFactory instance= new OutPutVisualAdapterFactory();
 	//this is for the step run use, make sure clear it after run it
 	//String 1 is user name ,String 2 is json string
 	
 	private HashMap<String,HashMap<AnalyticOutPut,String>> tempVisualModelMap= new HashMap<String,HashMap<AnalyticOutPut,String>>(); 
 
	public HashMap<String,HashMap<AnalyticOutPut,String>> getTempVisualModelMap() {
		return tempVisualModelMap;
	}

	public void setTempVisualModelMap(
			HashMap<String,HashMap<AnalyticOutPut,String>> tempVisualModelMap) {
		this.tempVisualModelMap = tempVisualModelMap;
	}

	public void addTempVModelString(String user,AnalyticOutPut output,String jsonString){
		if(tempVisualModelMap.get(user) == null){
			tempVisualModelMap.put(user, new HashMap<AnalyticOutPut,String>());
		} 
		HashMap<AnalyticOutPut,String> userMap = tempVisualModelMap.get(user) ;
		if(userMap.get(output) == null||userMap.get(output).equals(jsonString) == false){
			userMap.put(output, jsonString) ;
		}
	}
	
	public void removeTempVModelString(String user,AnalyticOutPut output ){
		if(tempVisualModelMap.get(user) == null){
			//nothing to do
			return;
		} 
		HashMap<AnalyticOutPut,String> userMap = tempVisualModelMap.get(user) ;
		if(userMap.get(output) != null){
			userMap.remove(output) ;
		}
	}
	
	public String getTempModelString(String user,AnalyticOutPut output ){
		if(tempVisualModelMap.get(user)!= null){
			return tempVisualModelMap.get(user).get(output);
		} else{
			return null;
		}
	}
	
 
	public void removeAllTempModelString(String user){
		if(tempVisualModelMap.get(user)!= null){
			  tempVisualModelMap.remove(user);
		}  
	}
	
	private OutPutVisualAdapterFactory(){
 
	}
	
	public static OutPutVisualAdapterFactory getInstance() {
 
		return instance;
	}

	public OutPutVisualAdapter getAdapter(AnalyticOutPut outPut) {
		OutPutVisualAdapter adapter=null;
		
		itsLogger.info("getAdapter:outPut type:"+outPut.getClass().getName());
		
		//Logger.getLogger(this.getClass().getName()).log(Level.INFO, "getAdapter:outPut="+outPut);
		
//		AnalyticConfiguration config = outPut.getDataAnalyzer().getAnalyticSource().getAnalyticConfig();
		//only from the confi can see the barchart,otherwise the output is a table...
		//MINERWEB-551
		if(outPut  instanceof SVDLanczosAnalyzerOutPutTrainModel){
			adapter = VisualAdapterSVDTrainer.INSTANCE;
		}
		else if(outPut instanceof AnalyzerOutPutPLDATrainModel){
			adapter = VisualAdapterPLDATrainer.getInstance();
		}
		else if(outPut instanceof AnalyzerOutPutTrainModel){
			return getModelAdapter((AnalyzerOutPutTrainModel)outPut);
		}
		else if(outPut instanceof AnalyzerOutPutDataBaseUpdatePLDA){
			adapter = VisualAdapterPLDAPredictor.getInstance();
		}		
		else if(outPut.getDataAnalyzer() instanceof FPGrowthAnalyzer){
			adapter = VisualAdapterAssociation.INSTANCE;
		}
		 else if(outPut.getDataAnalyzer() instanceof NormalizationAnalyzer){
			adapter = VisualAdapterEmptyTableOutput.INSTANCE;
		}
		 else if(outPut.getDataAnalyzer() instanceof PCAAnalyzer){
				adapter = VisualAdapterPCA.INSTANCE;
			}
//		 else if(outPut.getDataAnalyzer() instanceof AdaboostTrainer){
//				adapter = VisualAdapterAdaboostTrainer.INSTANCE;
//			}
		 else if(outPut.getDataAnalyzer() instanceof RecommendationEvaluationAnalyzer){
				adapter = VisualAdapterProductRecommendationEvaluation.INSTANCE;
			}

		 else if(outPut.getDataAnalyzer() instanceof IntegerToTextTransformationAnalyzer){
				adapter = VisualAdapterEmptyTableOutput.INSTANCE;
			}
		 else if(outPut.getDataAnalyzer() instanceof ColumnFilterAnalyzer){
				adapter = VisualAdapterTableData.INSTANCE;
			}
		 else if(outPut.getDataAnalyzer() instanceof InformationValueAnalyzer){
				adapter = VisualAdapterInformationValue.INSTANCE;
			}
		 else if(outPut.getDataAnalyzer() instanceof VariableSelectionAnalyzer){
				adapter = VisualAdapterVariableSelection.INSTANCE;
			}
        else if(outPut.getDataAnalyzer() instanceof SVDCalculator){
				adapter = VisualAdapterPredictor.INSTANCE;
			}
		 else if(outPut.getDataAnalyzer() instanceof HistogramAnalysisAnalyzer){
				adapter = VisualAdapterHistogram.INSTANCE;
	     }else if(outPut.getDataAnalyzer() instanceof HadoopHistogramAnalyzer){
            adapter = VisualAdapterHistogram.INSTANCE;
        }
		 
//		 else if(outPut.getDataAnalyzer() instanceof NeuralNetworkTrainer){
//				adapter = new VisualAdapterNeuralNetwork();
//			}
//		 else if(outPut.getDataAnalyzer() instanceof DecisionTreeTrainer){v
//				adapter = VisualAdapterDecisionTree.INSTANCE;
//			}
//		 else if(outPut.getDataAnalyzer() instanceof CartTrainer){v
//				adapter = new VisualAdapterCartTree();
//			}
        else if (outPut.getDataAnalyzer() instanceof KMeansAnalyzer) {
            adapter = VisualAdapterKmeans.INSTANCE;
        } else if (outPut.getDataAnalyzer() instanceof HadoopKmeansAnalyzer) {
            adapter = VisualAdapterHadoopKmeans.INSTANCE;
        }       else if (outPut.getDataAnalyzer()  instanceof HadoopVariableSelectionAnalyzer)
        {
            return VisualAdapterHadoopVariableSelection.INSTANCE;
        }
 		 
//		 else if(outPut.getDataAnalyzer() instanceof SVDTrainer){
//				adapter = VisualAdapterSVD.INSTANCE;
//			}
		 //time series
//		 else if(outPut.getDataAnalyzer() instanceof ARIMARTrainer){v
//				adapter = VisualAdapterARIMARTrainer.INSTANCE;
//			}
		 else if(outPut.getDataAnalyzer() instanceof ARIMARPredictor){
				adapter = new VisualAdapterARIMARPredictor();
			}
        else if(outPut.getDataAnalyzer() instanceof HadoopTimeSeriesPredictor){
            adapter = new VisualAdapterHadoopARIMARPredictor();
        }
		else if(outPut.getDataAnalyzer() instanceof UnivariateVariable){
			adapter = VisualAdapterUnivariateVariable.INSTANCE;
		}
//		else if(outPut.getDataAnalyzer() instanceof NaiveBayesTrainer){v
//			adapter = VisualAdapterNaiveBayes.INSTANCE;
//		}
//		else if(outPut.getDataAnalyzer() instanceof LogisticRegressionTrainerGeneral){v
//			adapter = VisualAdapterLogisticRegression.INSTANCE;
//		}
		else if(outPut.getDataAnalyzer() instanceof ROCGeneralEvaluator){
			adapter = VisualAdapterROC.INSTANCE;
		}else if(outPut.getDataAnalyzer() instanceof HadoopRocEvaluator){
            adapter = VisualAdapterROC.INSTANCE;
        }else if(outPut.getDataAnalyzer() instanceof LiftGeneralEvaluator || outPut.getDataAnalyzer() instanceof HadoopLiftEvaluator){
			adapter = VisualAdapterLift.INSTANCE;
		}
		else if(outPut.getDataAnalyzer() instanceof GoodnessOfFitEvaluator || outPut.getDataAnalyzer() instanceof HadoopGoodnessOfFitEvaluator){
			adapter = VisualAdapterGodnessOfFit.INSTANCE;
		} else if (outPut.getDataAnalyzer() instanceof HadoopConfusionEvaluator) {
            adapter = VisualAdapterConfusionMatrix.INSTANCE;
        }
		else if(outPut.getDataAnalyzer() instanceof  CorrelationAnalysisAnalyzer){
			adapter = VisualAdapterCorrelation.INSTANCE;
		}
		else if(outPut.getDataAnalyzer()  instanceof ValueAnalysisAnalyzer){
			adapter = VisualAdapterValueAnalysis.INSTANCE;
		}else if(outPut.getDataAnalyzer() instanceof HadoopValueAnalysisAnalyzer){
            adapter = VisualAdapterValueAnalysis.INSTANCE;
        }
		//3 svm share the same model
//		else if(outPut.getDataAnalyzer() instanceof SVMClassificationTrainer   v
//				|| outPut.getDataAnalyzer() instanceof SVMNoveltyDetectionTrainer
//				|| outPut.getDataAnalyzer() instanceof SVMRegressionTrainer){  // // //
//			adapter = VisualAdapterSVM.INSTANCE;
//		}
		else if(outPut instanceof AnalyzerOutPutScatterMatrix){
			adapter = VisualAdapterScarrtMatrix.getInstance();
		}
		else if(outPut.getDataAnalyzer() instanceof AbstractDBModelPredictor
				||outPut.getDataAnalyzer() instanceof ARIMARPredictor
				||outPut.getDataAnalyzer() instanceof SVDLanczosCalculator
				){  //
			adapter = VisualAdapterPredictor.INSTANCE;
		}
		else if(outPut.getDataAnalyzer()  instanceof FrequencyAnalysisAnalyzer){
			adapter = VisualAdapterFrequencyAnalysis.INSTANCE;
		}else if(outPut.getDataAnalyzer() instanceof HadoopFrequencyAnalysisAnalyzer){
            adapter = VisualAdapterFrequencyAnalysis.INSTANCE;
        }
		else if(outPut.getDataAnalyzer() instanceof TableAnalysisAnalyzer){
			adapter = VisualAdapterBarChart.INSTANCE;
		}else if(outPut.getDataAnalyzer() instanceof HadoopBarChartAnalyzer){
            adapter = VisualAdapterBarChart4HadoopFile.INSTANCE;
        } else if (outPut.getDataAnalyzer() instanceof HadoopBoxPlotAnalyzer) {
            adapter = VisualAdapterBoxWhisker.INSTANCE;
        }
//		else if(outPut.getDataAnalyzer() instanceof LinearRegressionTrainer){v
//			adapter = VisualAdapterLinearRegression.INSTANCE;
//		} 
		
		else if(outPut instanceof AnalyzerOutPutSampling){
			//this has no output now!
			adapter =  VisualAdapterSampling.INSTANCE;
		}
		else if(outPut instanceof AnalyzerOutPutTableObject){
			//this is for aggregate and column filter now
			adapter = VisualAdapterTableData.INSTANCE;
		}else if (outPut instanceof AnalyzerOutPutBoxWhisker){
			adapter = VisualAdapterBoxWhisker.INSTANCE;
			
		}else if(outPut instanceof HadoopMultiAnalyticFileOutPut){
			adapter = VisualAdapterHadoopRowFilter.INSTANCE;
		}else{
			//this is for temp use, only an id and title
			//client will not show this kind or output finally
			 
		}
			if(adapter!=null){
				itsLogger.info("getAdapter:outPut type:"+outPut.getClass().getName()
					+"\n      adapter= "+adapter.getClass().getName());
			}else{
				itsLogger.info("getAdapter:outPut type:"+outPut.getClass().getName()
						+"\n      adapter= null");
					
			}
			return adapter;
		 
	}



    private OutPutVisualAdapter getModelAdapter(AnalyzerOutPutTrainModel outPut) {
		if(outPut.getDataAnalyzer()!=null&&
				outPut.getDataAnalyzer() instanceof  EngineModelWrapperAnalyzer ==true){
			
			return null;
		}
		else if(outPut instanceof AdaBoostAnalyzerOutPutTrainModel){
			return VisualAdapterAdaboostTrainer.INSTANCE ;
		}else{
			Model model = outPut.getEngineModel().getModel();
			if(model instanceof NNModel){
				return  new VisualAdapterNeuralNetwork();
			}
			else if(model instanceof SVMModel){
				return VisualAdapterSVM.INSTANCE;
			}
			else if(model instanceof RegressionTreeModel){
				return new VisualAdapterCartTree();
			}
			else if(model instanceof RandomForestModel){
				return new VisualAdapterRandomForest();
			}
			else if(model instanceof DecisionTreeModel
					||model instanceof DecisionTreeHadoopModel){
				return new VisualAdapterDecisionTree();
			}
			 
			else if(model instanceof ARIMAModel){
				return VisualAdapterARIMARTrainer.INSTANCE;
			}else if(model instanceof ARIMAHadoopModel){
				return VisualAdapterHadoopARIMARTrainer.INSTANCE;
			}
			else if(model instanceof NBModel){
				return VisualAdapterNaiveBayes.INSTANCE;
			}
			else if(model instanceof LogisticRegressionModelDB
                    ||model instanceof LogisticRegressionHadoopModel){
				return VisualAdapterLogisticRegression.INSTANCE;
			}
			else if(model instanceof LinearRegressionModelDB){
				return VisualAdapterLinearRegression.INSTANCE;
			}
            else if(model instanceof LinearRegressionHadoopModel){
                return VisualAdapterLinearRegressionHadoop.INSTANCE;
            }
            else if(model instanceof WOEModel){
				return VisualAdapterWOEModel.INSTANCE;
			}else if(model instanceof EMModel){
				return VisualAdapterEMClustering.INSTANCE;
			}  else if (model instanceof NaiveBayesHadoopModel)
            {
                return VisualAdapterHadoopNaiveBayes.INSTANCE;
            }

			else{
				return null ;
			}
  		
		}
	 
	}

}
