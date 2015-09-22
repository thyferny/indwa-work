package com.alpine.datamining.api.impl.db.attribute;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableSetConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisColumnMap;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisTableSetModel;
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
import com.alpine.utility.tools.StringHandler;

public class TableSetAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(TableSetAnalyzer.class);
		
	private AnalysisTableSetModel tableSetModel;
	private ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
	
			TableSetConfig config = (TableSetConfig) source.getAnalyticConfig();
			
			sqlGenerator=SqlGeneratorMultiDBFactory.createConnectionInfo(source.getDataSourceType());
			
			setTableSetModel(config.getTableSetModel());
			
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());
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

	private void performOperation(DatabaseConnection databaseConnection,
			DataSet dataSet) throws AnalysisError, OperatorException {
		
		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		
		String selectSql = buildSelectSql();
		
		dropIfExist(dataSet);
		
		DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
		
		StringBuilder createSql = new StringBuilder("create ");
		StringBuilder insertTable = new StringBuilder();
		
		createSql.append(getOutputType()).append(" ").append(outputTableName);
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		createSql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));//for oracle
		createSql.append(" as ( ");
		createSql.append(selectSql).append(" )");
		
		if(getOutputType().equalsIgnoreCase(Resources.TableType)){
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(),outputTableName));
		}
		
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			logger.debug("TableSetAnalyzer.performOperation():sql="+createSql);
			st.executeUpdate(createSql.toString());
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"TableSetAnalyzer.performOperation():insertTableSql=" + insertTable);
			}
			
		} catch (SQLException e) {
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				 
				logger.error(e);
			}
		}
	 
	}

	private String buildSelectSql() {
		StringBuffer selectSql = new StringBuffer(" select ");
		if(getTableSetModel()!=null){
			List<AnalysisColumnMap> columnMapList = getTableSetModel().getColumnMapList();
			List<String> columnNamesList = null;
			if(columnMapList!=null&&columnMapList.size()>0){
				AnalysisColumnMap columnNamesMap = columnMapList.get(0);
				columnNamesList = columnNamesMap.getTableColumns();
			}
			
			//Special for Oracle
			String setType = sqlGenerator.getTableSetType(getTableSetModel().getSetType());
			String firstTable = getTableSetModel().getFirstTable();
			AnalysisColumnMap firstTableColumnMap = null;
			boolean isExceptType=false;
			if(getTableSetModel().getSetType().equals(AnalysisTableSetModel.TABLE_SET_TYPE[3])){
				isExceptType=true;
			}
			if(false==isExceptType){//"EXCEPT"
				//first item is columnname list
				//the second is real first table.
				firstTableColumnMap = columnMapList.get(1);
			}else{//Type:"EXCEPT"
				String[] tableArray = StringHandler.splitQuatedTableName(firstTable);
				firstTableColumnMap = getTableSetModel().getColumnMap(tableArray[0],tableArray[1]);
			}
									
			if(firstTableColumnMap!=null&&columnNamesList!=null){
				String schemaName = firstTableColumnMap.getSchemaName();
				String tableName = firstTableColumnMap.getTableName();
				String fullTableName=StringHandler.combinTableName(schemaName, tableName);
				List<String> firstTableColumns = firstTableColumnMap.getTableColumns();
				for(int i = 0;i<columnNamesList.size();i++){
					selectSql.append(StringHandler.doubleQ(firstTableColumns.get(i)));
					selectSql.append(" as ").append(StringHandler.doubleQ(columnNamesList.get(i)));
					selectSql.append(",");
				}
				selectSql.deleteCharAt(selectSql.length()-1);
				selectSql.append(" from ").append(fullTableName);
				selectSql.append(" ").append(setType);
			}	
			
			if(columnMapList!=null){
				boolean firstFlag=true;
				for(int i=0;i<columnMapList.size();i++){
					if(i==0){//index 0 is columnname list.
						continue;
					}else if(false==isExceptType//Not Type:"Except" and the index 1 have been used.
							&&i==1){
						continue;
					}
					AnalysisColumnMap columnMap = columnMapList.get(i);
			
					String schemaName = columnMap.getSchemaName();
					String tableName = columnMap.getTableName();
					
					String fullTableName=StringHandler.combinTableName(schemaName, tableName);
					
					//Type:"Except",find used table
					if(isExceptType&&
							firstTable.equals(fullTableName)){
						if(firstFlag){
							firstFlag=false;
							continue;
						}				
					}
					selectSql.append(" select ");
					List<String> tableColumns = columnMap.getTableColumns();
					for(String s:tableColumns){
						selectSql.append(StringHandler.doubleQ(s)).append(",");
					}
					selectSql.deleteCharAt(selectSql.length()-1);
					selectSql.append(" from ").append(fullTableName).append(" ");
					selectSql.append(setType);				
				}	
				selectSql.delete(selectSql.lastIndexOf(setType), selectSql.length());
			}
		}
		
		return selectSql.toString();
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.TABLESET_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.TABLESET_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

	public AnalysisTableSetModel getTableSetModel() {
		return tableSetModel;
	}

	public void setTableSetModel(AnalysisTableSetModel tableSetModel) {
		this.tableSetModel = tableSetModel;
	}

}
