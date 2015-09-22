/**
 * ClassName InformationValueAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-1-4
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.InformationValueAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;


/**
 *	Eason
 */
public class InformationValueAnalyzer extends AbstractDBAttributeAnalyzer{
	private static Logger logger= Logger.getLogger(InformationValueAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		InformationValueResult informationValueResult=null;
//		try {
		DataSet dataSet = null;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
		
		InformationValueAnalysisConfig config=(InformationValueAnalysisConfig)source.getAnalyticConfig();
		setSpecifyColumn(dataSet, config);
		
		informationValueResult = new InformationValueResult(dataSet);
		informationValueResult.setGood(config.getGood());
		try {
			informationValueResult.calculateInformationValue();
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} else
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
		AnalyzerOutPutObject outPut= new AnalyzerOutPutObject(informationValueResult);
		outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return outPut;
	}
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((InformationValueAnalysisConfig)config).getDependentColumn());
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.INFORMATIONVALUE_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.INFORMATIONVALUE_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
