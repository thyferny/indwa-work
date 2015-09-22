/**
 * ClassName GoodnessOfFitEvaluator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelValidator;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Container;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.evaluator.EvaluatorParameter;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.GoodnessOfFitGenerator;
import com.alpine.datamining.utility.OperatorUtil;

/**
 * eason
 */
public class GoodnessOfFitEvaluator extends AbstractDBModelValidator {
	private static Logger logger= Logger.getLogger(GoodnessOfFitEvaluator.class);
	
	@Override
	protected GoodnessOfFitOutPut doValidate(DataBaseAnalyticSource source,EvaluatorConfig config) throws AnalysisException {
		GoodnessOfFit result=null;
		List<GoodnessOfFit> resultList=new ArrayList<GoodnessOfFit>();
		try {
			DataSet dataSet = getDataSet(source,source.getAnalyticConfig());
//			dataSet.recalculateAllcolumnStatistics();
			Operator operator = OperatorUtil.createOperator(GoodnessOfFitGenerator.class);
			EvaluatorParameter parameter = new EvaluatorParameter();
			if (config.getUseModel() != null)
			{
//				operator.setParameter(GoodnessOfFitGenerator.PARAMETER_USE_MODEL, config.getUseModel());
				parameter.setUseModel(Boolean.parseBoolean(config.getUseModel()));
			}
			operator.setParameter(parameter);
			if (config.getUseModel().equals("true"))
			{
				List<EngineModel> models = config.getTrainedModel();
				
				Model model = null;
				for ( int i = 0; i < models.size(); i++)
				{
					Container container = new Container();
					model = models.get(i).getModel();
					container = container.append(dataSet);
					container = container.append(model);
					Container resultContainer = operator.apply(container);
					result = resultContainer.get(GoodnessOfFit.class);
					result.setSourceName(models.get(i).getName());
					resultList.add(result);
				}
			}
			else
			{
				Container container = new Container();
				Column label = dataSet.getColumns().getLabel();
				for ( int i = 0; i < label.getMapping().size(); i++)
				{
					String value = label.getMapping().mapIndex(i);
					Column confidence = dataSet.getColumns().get(Column.CONFIDENCE_NAME + "(" + value + ")");
					dataSet.getColumns().setSpecialColumn(confidence, Column.CONFIDENCE_NAME + "_" + value);
				}
				Column predictedLabel = dataSet.getColumns().get(Column.PREDICTION_NAME+"("+label.getName()+")");
				dataSet.getColumns().setPredictedLabel(predictedLabel);
				container = container.append(dataSet);
				Container resultContainer = operator.apply(container);
				result = resultContainer.get(GoodnessOfFit.class);
				result.setSourceName(getName());
				resultList.add(result);
			}
			 
		} catch (Exception e) {
			logger.error(e) ;
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} 
			else if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
//		AnalyzerOutPutObject out= new AnalyzerOutPutObject(resultList);
		
		GoodnessOfFitOutPut out= new GoodnessOfFitOutPut(resultList);
		
		out.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return out;
		
	}
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((EvaluatorConfig)config).getDependentColumn());
	}

	
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.GOODNESSOFFit_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.GOODNESSOFFit_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
