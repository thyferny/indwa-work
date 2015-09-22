/**
 * ClassName ARIMARPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.algoconf.ARIMARPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * 
 */
public class ARIMARPredictor extends AbstractAnalyzer {

    private static final Logger itsLogger = Logger.getLogger(ARIMARPredictor.class);
    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	protected String getQuotaedTableName(String schema, String table) {
		String outputTableName;
		
		if (schema!=null&&schema.trim().length()>0) {
			schema=StringHandler.doubleQ(schema);
			
			outputTableName = schema+"."+StringHandler.doubleQ(table);
		} else {
			outputTableName =StringHandler.doubleQ(table);
		}
		return outputTableName;
	}
	protected void dropIfExist(
			DataBaseAnalyticSource source, AnalyticConfiguration config)
			throws OperatorException
			{
	
		String OutputSchema= ((PredictorConfig)config).getOutputSchema();
		String OutputTable=((PredictorConfig)config).getOutputTable();
		String DropIfExist=((PredictorConfig)config).getDropIfExist();
		String newTableName = getQuotaedTableName(OutputSchema, OutputTable);
		Connection conn =null;
		Statement st = null;
		try {
		    conn =source.getConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		} 
	
		StringBuilder dropSql = new StringBuilder();
		if (DropIfExist.equalsIgnoreCase("yes")) {
			if (source.getDataBaseInfo().getSystem().equals(
					DataSourceInfoOracle.dBType)) {
				int tableExistInt = 0;
				StringBuffer tableExist = new StringBuffer();
				tableExist.append("select count(*) from dba_tables where owner = '").append(OutputSchema).append("' and table_name = '").append(OutputTable).append("'");
				ResultSet rs;
				try {
                    itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
							+ tableExist.toString());
					rs = st.executeQuery(tableExist.toString());
					while(rs.next())
					{
						tableExistInt = rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new OperatorException(e1.getLocalizedMessage());
				}
				if (tableExistInt > 0)
				{
					dropSql.append("drop table ");
					dropSql.append(newTableName);
					try {
                        itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
										+ dropSql.toString());
						st.execute(dropSql.toString());
					} catch (SQLException e) {
						e.printStackTrace();
						itsLogger.error(e.getMessage(),e);
						throw new OperatorException(e.getLocalizedMessage());
					}
				}
			}else if (source.getDataBaseInfo().getSystem().equals(
					DataSourceInfoDB2.dBType)){
				dropSql.append("call PROC_DROPSCHTABLEIFEXISTS( '").append(StringHandler.doubleQ(OutputSchema)).append("','").append(StringHandler.doubleQ(OutputTable)).append("')");
				
				try {
                    itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
//					throw new OperatorException(e.getLocalizedMessage());
				}
			}else if (source.getDataBaseInfo().getSystem().equals(
					DataSourceInfoNZ.dBType)){
//				dropSql.append("drop table ");
				dropSql.append("call droptable_if_existsdoubleq('").append(StringHandler.doubleQ(OutputTable)).append("')");
//				dropSql.append(newTableName);
				try {
					itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
//					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else{
				dropSql.append("drop table if exists ");
				dropSql.append(newTableName);
				try {
                    itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
		}
}
	

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		ARIMARPredictorConfig config = (ARIMARPredictorConfig)source.getAnalyticConfig();
		Connection connection = ((DataBaseAnalyticSource)source).getConnection();
		try {	
			dropIfExist((DataBaseAnalyticSource)source, config);
		} catch (Exception e) 
		{
			itsLogger.error(e.getMessage(),e) ;
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

		int aheadNumber = Integer.parseInt(config.getAheadNumber());
		ARIMAModel aRIMAmodel=(ARIMAModel)config.getTrainedModel().getModel();
		ARIMARPredictResult ret = null;
		String OutputSchema= ((PredictorConfig)config).getOutputSchema();
		String OutputTable=((PredictorConfig)config).getOutputTable();
		String newTableName = getQuotaedTableName(OutputSchema, OutputTable);
		AnalysisStorageParameterModel analysisStorageParameterModel = ((PredictorConfig)config).getStorageParameters();
		try{
			ret = aRIMAmodel.prediction(aheadNumber, connection, newTableName, analysisStorageParameterModel);
		}catch (Exception e) {
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
		AnalyzerOutPutARIMARPredict output=new AnalyzerOutPutARIMARPredict(ret);
		output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return output;
	}
}
