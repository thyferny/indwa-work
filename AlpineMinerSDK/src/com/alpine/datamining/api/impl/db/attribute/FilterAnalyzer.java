/**
 * ClassName FilterAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 1, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.FilterConfig;
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
import org.apache.log4j.Logger;

/**
 * @author Richie Lo
 *
 */
public class FilterAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(FilterAnalyzer.class);
	
	private String whereClause;
	
	private String outputSchema;
	private String outputTable;
	private String outputType;
	private String dropIfExists;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
 
		DatabaseConnection databaseConnection = null;
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
		
			databaseConnection=((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			FilterConfig config = (FilterConfig) source.getAnalyticConfig();
			
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			setWhereClause(config.getWhereClause());
			
			outputSchema=config.getOutputSchema();
			outputTable=config.getOutputTable();
			outputType=config.getOutputType();
			dropIfExists=config.getDropIfExist();
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

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void performOperation(DatabaseConnection databaseConnection,DataSet dataSet) throws AnalysisException, OperatorException {
		// prepare the WHERE clause;
		if (whereClause!=null) {
			whereClause = "WHERE " + whereClause;
		} else {
			whereClause = "";
		}

		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		
		String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());
		
		StringBuilder createSql=new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		StringBuilder insertTable=new StringBuilder();
		createSql.append("CREATE ").append(getOutputType());
		createSql.append(" ").append(outputTableName).append(" ");
		createSql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		createSql.append(" AS ( ");
		selectSql.append("  SELECT * FROM  ").append(inputTableName).append(whereClause);
		createSql.append(selectSql).append(" )");
		if(getOutputType().equalsIgnoreCase("table")){
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
				
			dropIfExist(dataSet);
			
			DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
	
			logger.debug("FilterAnalyzer.performOperation():sql="+createSql);
			st.executeUpdate(createSql.toString());
			
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"FilterAnalyzer.performOperation():refreshTableSql=" + insertTable);
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
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.DATA_FILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.DATA_FILTER_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}

