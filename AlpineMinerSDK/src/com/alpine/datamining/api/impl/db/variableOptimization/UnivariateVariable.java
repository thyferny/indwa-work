/**
 * ClassName UnivariateVariable.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.variableOptimization;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.regressions.LogisticRegressionDBNewton;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;/**
 * Jeff
 */

public class UnivariateVariable extends AbstractDBAnalyzer {
	//private static Logger logger= Logger.getLogger(UnivariateVariable.class);
    private static final Logger itsLogger = Logger.getLogger(UnivariateVariable.class);

    @Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
	
		try {
			LogisticRegressionConfigGeneral config = (LogisticRegressionConfigGeneral) source
			.getAnalyticConfig();
			config.setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.UnivariateTextAndTableVisualizationType");
			DataSet dataSet = getDataSet(
					((DataBaseAnalyticSource) source), config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			LogisticRegressionModelDB model = null;
			TreeMap<String,Double> pValueSortedMap=new TreeMap<String,Double>(new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {
						return arg0.compareTo(arg1);			
				}
			});
			Columns atts=dataSet.getColumns();
			Iterator<Column> atts_i=atts.iterator();
			while(atts_i.hasNext())
			{
				itsLogger.debug("UnivariateVariable:Enter loop");
				Column att=atts_i.next();
				DataSet dataSet_clone=(DataSet)dataSet.clone();
				Columns atts_clone=dataSet_clone.getColumns();
				removeColumn(atts_clone,att);
				Operator learner = initOperator(config, dataSet_clone);
				model = (LogisticRegressionModelDB) ((Training) learner)
				.train(dataSet_clone);
				double minPValue=findMinPValue(model);
				pValueSortedMap.put(att.getName(), minPValue);
			}
			UnivariateVariableOutput analyzerOutPut = new UnivariateVariableOutput(pValueSortedMap);
			analyzerOutPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return analyzerOutPut;
		} catch (Exception e) {
			itsLogger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}
	}
	private double findMinPValue(LogisticRegressionModelDB model) {
		double[] pValueArray=model.getpValue();
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < pValueArray.length - 1; i++) {
			if (pValueArray[i] < min) {
				min = pValueArray[i];
			}
		}
		return min;
	}
	private void removeColumn(Columns atts, Column att) 
	{
		Columns atts_clone=(Columns)atts.clone();
		Iterator<Column> i=atts_clone.iterator();
		while(i.hasNext())
		{
			Column at=i.next();
			if(att.equals(at))
				continue;
			else
			atts.remove(at);
		}
		
	}
	private Operator initOperator(LogisticRegressionConfigGeneral config,
			DataSet dataSet) throws OperatorException {
		Operator learner = OperatorUtil
				.createOperator(LogisticRegressionDBNewton.class);
		itsLogger.debug(
				"UnivariateVariable  addAnalyzerID: ="
						+ String.valueOf(this.hashCode()));
		LogisticRegressionParameter parameter = new LogisticRegressionParameter();
		itsLogger.info("LogisticRegressionTrainer  addAnalyzerID: ="+String.valueOf(this.hashCode()));
		if(!StringUtil.isEmpty(config.getColumnNames()))
		{
			parameter.setColumnNames(config.getColumnNames());
		}

		if(!StringUtil.isEmpty(config.getAdd_intercept()))
		{
			parameter.setAddInercept(Boolean.parseBoolean(config.getAdd_intercept()));
		}
		if(!StringUtil.isEmpty(config.getMax_generations()))
		{
			parameter.setMaxGenerations(Integer.parseInt(config.getMax_generations()));
		}

		if ( !StringUtil.isEmpty(config.getGoodValue()))
		{
			parameter.setGoodValue(config.getGoodValue());
		}
		if ( !StringUtil.isEmpty(config.getEpsilon()))
		{
			parameter.setEpsilon(Double.parseDouble(config.getEpsilon()));
		}
		learner.setParameter(parameter);
		warnTooManyValue(dataSet, Integer
				.parseInt(AlpineMinerConfig.C2N_WARNING),config.getLocale());
		return learner;
	}
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) throws OperatorException {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((LogisticRegressionConfigGeneral)config).getDependentColumn());
	}
	protected void setNumericalLabelCategory(Column label){
		if(label.isNumerical())
		{
			((NumericColumn)label).setCategory(true);
		}
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.UNIVARIATE_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.UNIVARIATE_DESCRIPTION,locale)); 
		return nodeMetaInfo;
	}
}
