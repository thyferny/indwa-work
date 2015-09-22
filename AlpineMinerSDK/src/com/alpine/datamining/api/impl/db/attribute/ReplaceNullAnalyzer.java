/**
 * ClassName ReplaceNullAnalyzer
 *
 * Version information:1.00
 *
 * Date:Jun 1, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.ReplaceNullConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementItem;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;


/**
 *
 */
public class ReplaceNullAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(ReplaceNullAnalyzer.class);
	
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
			ReplaceNullConfig config = (ReplaceNullConfig) source.getAnalyticConfig();
			Map<String, String> map = getSpecifiedColumn(config);
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			
			outputSchema=config.getOutputSchema();
			outputTable=config.getOutputTable();
			outputType=config.getOutputType();
			dropIfExists=config.getDropIfExist();
			setOutputType(outputType);
			setOutputSchema(outputSchema);
			setOutputTable(outputTable);
			setDropIfExist(dropIfExists);
			
			generateStoragePrameterString((DataBaseAnalyticSource) source);
			performOperation(databaseConnection,dataSet, map);	
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
	private Map<String, String> getSpecifiedColumn(
			ReplaceNullConfig config) throws OperatorException, AnalysisError {
		Map<String, String> columnReplacementMap = new HashMap<String, String>();
		AnalysisNullReplacementModel model = config.getNullReplacementModel();
		if (model == null || model.getNullReplacements() == null || model.getNullReplacements().isEmpty()) {
			logger
					.error("ReplaceNull Analyzer's columnNames and replacement cannot null");
			throw new AnalysisError(this,AnalysisErrorName.Not_null,config.getLocale(),SDKLanguagePack.getMessage(SDKLanguagePack.REPLACE_NULL_COLUMN,config.getLocale()));
		}
		for(AnalysisNullReplacementItem item:model.getNullReplacements()){
			columnReplacementMap
			.put(item.getColumnName(), item.getValue());
		}
		return columnReplacementMap;
	}

	public void performOperation(DatabaseConnection databaseConnection,DataSet dataSet,Map<String, String> map) throws AnalysisException, OperatorException {
		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(databaseConnection.getProperties().getName());
		StringBuffer newColumnNames = new StringBuffer();
		boolean first = true;
		int i = 0;
		for(Column column:dataSet.getColumns()){
			if (first){
				first = false;
			}else{
				newColumnNames.append(",");
			}
			String attName = column.getName();
			String attNameQ = StringHandler.doubleQ(attName);
			if(map.containsKey(attName)){
				String replacement = map.get(attName);
				if (StringUtil.isEmpty(replacement)){
					if (column.isNominal()){
						replacement = "''";
					}else{
						replacement = "0";
					}
				}
				if (column.isNominal()){
					if (replacement.startsWith("'") && replacement.endsWith("'")){
						replacement = replacement.substring(replacement.indexOf("'")+1, replacement.lastIndexOf("'"));
					}
					replacement = CommonUtility.quoteValue(dataSourceInfo.getDBType(), column, replacement);
				}
				newColumnNames.append(" (case when ").append(attNameQ).append(" is null then ").append(replacement)
				.append(" else ").append(attNameQ).append(" end) ").append(attNameQ);
			}else{
				newColumnNames.append(StringHandler.doubleQ(attName));
			}
			i++;
		}
		StringBuffer createSql = new StringBuffer();
		StringBuffer selectSql = new StringBuffer();
		StringBuffer insertTable=new StringBuffer();
		createSql.append("CREATE ").append(getOutputType()).append(" ").append(outputTableName);
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		createSql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));
		createSql.append(" AS  (");
		selectSql.append(" SELECT ").append(newColumnNames).append(" FROM ").append(" ").append(inputTableName);
		createSql.append(selectSql).append(" ) ");
		if(getOutputType().equalsIgnoreCase("table")){
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}
		
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			dropIfExist(dataSet);
			
			DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
			
			logger.debug("ReplaceNullAnalyzer.performOperation():sql="+createSql);
			st.executeUpdate(createSql.toString());
			
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"ReplaceNullAnalyzer.aperformOperation():refreshTableSql=" + insertTable);
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
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.REPLACE_NULL_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.REPLACE_NULL_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}

