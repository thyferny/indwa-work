/**
 * ClassName SampleSelectorAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.SampleSelectorConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DatabaseConnection;

/**
 * @author Richie Lo
 * 
 */
public class SampleSelectorAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(SampleSelectorAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		DatabaseConnection databaseConnection = null;

		try {
			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) source, source.getAnalyticConfig());
			databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			SampleSelectorConfig config = (SampleSelectorConfig) source
					.getAnalyticConfig();

			if (config.getSelectedTable().indexOf(".") > 0) {
				String[] namePart = config.getSelectedTable().split("\\.", 2);
				setOutputSchema(namePart[0]);
				setOutputTable(namePart[1]);
			} else {
				setOutputTable(config.getSelectedTable());
				setOutputSchema(null);
			}
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo();
			AnalyzerOutPutTableObject outPut = getResultTableSampleRow(
					databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));

			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			return outPut;
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SAMPLEING_SELECTOR_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SAMPLEING_SELECTOR_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

}
