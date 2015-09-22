package com.alpine.datamining.api.impl.db.attribute;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PivotTableConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class PivotTableAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(PivotTableAnalyzer.class);
	
	private String columnNames;
	
	private String groupColumn;
	
	private String aggColumn;
	
	private String aggrType;

	private boolean useArray;
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
//			dataSet.recalculateAllcolumnStatistics();
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			PivotTableConfig config =(PivotTableConfig)source.getAnalyticConfig();
			
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());
			columnNames=config.getPivotColumn();
			groupColumn=config.getGroupByColumn();
			aggColumn=config.getAggregateColumn();
			aggrType=config.getAggregateType();
			String dbType=databaseConnection.getProperties().getName();
			if (config.getUseArray() != null && config.getUseArray().equalsIgnoreCase("true")){
				if(dbType.equals(DataSourceInfoDB2.dBType)
						||dbType.equals(DataSourceInfoNZ.dBType)){
					useArray = false;
				}else{
					useArray = true;
				}
			}else{
				useArray = false;
			}
			generateStoragePrameterString((DataBaseAnalyticSource) source);
			performOperation(databaseConnection,dataSet,config.getLocale());
			
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			return outPut;
			
		} catch ( Exception e) {
			logger.error(e);
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
	}

	private void performOperation(DatabaseConnection databaseConnection,DataSet dataSet, Locale locale) throws AnalysisError, OperatorException {
		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		
		String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());	
		
		Columns atts = dataSet.getColumns();
		String dbType = databaseConnection.getProperties().getName();
		IDataSourceInfo  dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dbType);
		
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
		
		dropIfExist(dataSet);
		
		DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
	
		StringBuilder sb_create=new StringBuilder("create ");
		StringBuilder insertTable=new StringBuilder();

		if(getOutputType().equalsIgnoreCase("table"))
		{
			sb_create.append(" table ");
		}
		else
		{
			sb_create.append(" view ");
		}
		sb_create.append(outputTableName);
		sb_create.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		sb_create.append(DatabaseUtil.addParallel(databaseConnection,getOutputType())).append(" as (");
		StringBuilder selectSql=new StringBuilder(" select ");

		selectSql.append(StringHandler.doubleQ(groupColumn)).append(",");

		Column att =atts.get(columnNames);
		dataSet.computeColumnStatistics(att);
		if(att.isNumerical())
		{
			logger.error(
			"PivotTableAnalyzer cannot accept numeric type column");			
			throw new AnalysisError(this,AnalysisErrorName.Not_numeric,locale,SDKLanguagePack.getMessage(SDKLanguagePack.PIVOT_NAME,locale));
		}
		String attName=StringHandler.doubleQ(att.getName());
		List<String> valueList = att.getMapping().getValues();
		if(!useArray && valueList.size()>Integer.parseInt(AlpineMinerConfig.PIVOT_DISTINCTVALUE_THRESHOLD))
		{
			logger.error(
			"Too many distinct value for column "+StringHandler.doubleQ(columnNames));			
			throw new AnalysisError(this,AnalysisErrorName.Too_Many_Distinct_value,locale,StringHandler.doubleQ(columnNames),AlpineMinerConfig.PIVOT_DISTINCTVALUE_THRESHOLD);
		}

		if(valueList.size()<=0){
			logger.error(
					"Empty table");	
			throw new AnalysisError(this, AnalysisErrorName.Empty_table,locale);
		}
		
		String aggColumnName;
		if(!StringUtil.isEmpty(aggColumn)){
			aggColumnName=StringHandler.doubleQ(aggColumn);
		}else{
			aggColumnName="1";
		}

		Iterator<String> valueList_i = valueList.iterator();

		if (useArray){
			if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
				ArrayList<String> array = new ArrayList<String>();
				while (valueList_i.hasNext()) {
					String value = StringHandler.escQ(valueList_i.next());
					String newValue = "alpine_miner_null_to_0("+aggrType+" (case when "+attName+"="+CommonUtility.quoteValue(dbType, att, value)+" then "+aggColumnName+" end )) ";
					array.add(newValue);
				}
				selectSql.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float));
			}else{
				selectSql.append(multiDBUtility.floatArrayHead());
				while (valueList_i.hasNext()) 
					{
						String value = valueList_i.next();
						selectSql.append("alpine_miner_null_to_0(").append(aggrType);
						selectSql.append(" (case when ").append(attName).append("=");
						value = StringHandler.escQ(value);
						selectSql.append(CommonUtility.quoteValue(dbType, att, value)).append(" then ").append(aggColumnName).append(" end )) ");// else 0
						selectSql.append(",");
					}
				selectSql=selectSql.deleteCharAt(selectSql.length()-1);
				selectSql.append(multiDBUtility.floatArrayTail());
			}
			selectSql.append(" "+StringHandler.doubleQ(att.getName()));
		}else{
			if(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equals(DataSourceInfoNZ.dBType)){
				while (valueList_i.hasNext()) 
				{
					String value = valueList_i.next();
					selectSql.append("(").append(aggrType);
					selectSql.append(" (case when ").append(attName).append("=");
					value = StringHandler.escQ(value);
					selectSql.append(CommonUtility.quoteValue(dbType, att, value)).append(" then ").append(aggColumnName).append(" end )) ");// else 0
					String colName =StringHandler.doubleQ(att.getName()+"_"+value);
					selectSql.append(colName);
					selectSql.append(",");
				}
			selectSql=selectSql.deleteCharAt(selectSql.length()-1);
			}else if(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equals(DataSourceInfoDB2.dBType)){
				while (valueList_i.hasNext()) 
				{
					String value = valueList_i.next();
					selectSql.append("alpine_miner_null_to_0(").append(aggrType);
					selectSql.append(" (double(case when ").append(attName).append("=");
					value = StringHandler.escQ(value);
					selectSql.append(CommonUtility.quoteValue(dbType, att, value)).append(" then ").append(aggColumnName).append(" end ))) ");// else 0
					String colName =StringHandler.doubleQ(att.getName()+"_"+value);
					selectSql.append(colName);
					selectSql.append(",");
				}
			selectSql=selectSql.deleteCharAt(selectSql.length()-1);
			}else{
				while (valueList_i.hasNext()) 
				{
					String value = valueList_i.next();
					selectSql.append("alpine_miner_null_to_0(").append(aggrType);
					selectSql.append(" (case when ").append(attName).append("=");
					value = StringHandler.escQ(value);
					selectSql.append(CommonUtility.quoteValue(dbType, att, value)).append(" then ").append(aggColumnName).append(" end )) ");// else 0
					String colName =StringHandler.doubleQ(att.getName()+"_"+value);
					selectSql.append(colName);
					selectSql.append(",");
				}
			selectSql=selectSql.deleteCharAt(selectSql.length()-1);
			}
		}
		selectSql.append(" from ").append(inputTableName).append(" foo group by ");
		selectSql.append(StringHandler.doubleQ(groupColumn));
		
		if(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equals(DataSourceInfoNZ.dBType)){
			StringBuilder sb=new StringBuilder();
			sb.append("select ").append(StringHandler.doubleQ(groupColumn)).append(",");
			Iterator<String> valueList_new = valueList.iterator();
			while (valueList_new.hasNext()) {
				String value = valueList_new.next();
				String colName =StringHandler.doubleQ(att.getName()+"_"+value);
				sb.append("case when ").append(colName).append(" is null then 0 else ");
				sb.append(colName).append(" end ").append(colName).append(",");
			}
			sb=sb.deleteCharAt(sb.length()-1);
			sb.append(" from (").append(selectSql).append(") foo ");
			selectSql=sb;
		}		
		sb_create.append(selectSql).append(" )");
		

		
		
		if(getOutputType().equalsIgnoreCase("table")){
			sb_create.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}
		try {
			Statement st = databaseConnection.createStatement(false);
			logger.debug("PivotTableAnalyzer.performOperation():sql="+sb_create);
			st.execute(sb_create.toString());
			
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"PivotTableAnalyzer.performOperation():insertTableSql=" + insertTable);
			}
		} catch (SQLException e) {
			logger.error(e);
			if (e.getMessage().startsWith("ORA-03001")||e.getMessage().startsWith("ERROR:  invalid identifier")){
				throw new AnalysisError(this,AnalysisErrorName.Invalid_Identifier,locale);
			}else{
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.PIVOT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.PIVOT_DESRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
