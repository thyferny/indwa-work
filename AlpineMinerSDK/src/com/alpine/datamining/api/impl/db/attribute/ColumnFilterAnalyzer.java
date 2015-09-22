/**
 * ClassName ColumnFilterAnalyzer
 *
 * Version information:1.00
 *
 * Date:May 13, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.ColumnFilterConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;


/**
 * @author Eason
 *
 */
public class ColumnFilterAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(ColumnFilterAnalyzer.class);
	
	private String outputSchema;
	private String outputTable;
	private String outputType;
	private String dropIfExists;
	private String columnNames;


	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		
		DatabaseConnection databaseConnection = null;
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
		
			databaseConnection=((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			ColumnFilterConfig config = (ColumnFilterConfig) source.getAnalyticConfig();
			
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			
			outputSchema=config.getOutputSchema();
			outputTable=config.getOutputTable();
			outputType=config.getOutputType();
			dropIfExists=config.getDropIfExist();
			columnNames=config.getColumnNames();
			setOutputType(outputType);
			setOutputSchema(outputSchema);
			setOutputTable(outputTable);
			setDropIfExist(dropIfExists);
			generateStoragePrameterString((DataBaseAnalyticSource) source);

			performOperation(databaseConnection,dataSet);	
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			return outPut;

		} catch ( Exception e) {
			e.printStackTrace();
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		} 	
		

	}

	public void performOperation(DatabaseConnection databaseConnection,DataSet dataSet) throws AnalysisException, OperatorException {

		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		
		String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());

		StringBuffer newColumnNames = new StringBuffer();
		String[] columnArray=columnNames.split(",");
		boolean first = true;
		for(String s:columnArray)
		{
			if(first){
				first = false;
			}else{
				newColumnNames.append(",");
			}
			newColumnNames.append(StringHandler.doubleQ(s));
		}
		StringBuilder createSql=new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		StringBuilder insertTable=new StringBuilder();
		createSql.append("CREATE ").append(getOutputType());
		createSql.append(" ").append(outputTableName).append(" ");
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		createSql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));
		createSql.append(" AS ( ");
		selectSql.append(" SELECT ").append(newColumnNames).append(" FROM ").append(inputTableName);
		createSql.append(selectSql).append(" ) ");

		if(getOutputType().equalsIgnoreCase(Resources.TableType)){
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
				
			dropIfExist(dataSet);
			
			DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
	
			logger.debug("ColumnFilterAnalyzer.performOperation():sql="+createSql);
			st.executeUpdate(createSql.toString());
			
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"ColumnFilterAnalyzer.performOperation():refreshTableSql=" + insertTable);
			}
			
			
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}		
		} finally {
			try {
				if(st != null){
					st.close();
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
	}

	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.COLUMN_FILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.COLUMN_FILTER_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}

