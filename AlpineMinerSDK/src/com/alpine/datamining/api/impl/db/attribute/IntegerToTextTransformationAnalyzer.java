package com.alpine.datamining.api.impl.db.attribute;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.IntegerToTextTransformConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class IntegerToTextTransformationAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(IntegerToTextTransformationAnalyzer.class);
	
	ISqlGeneratorMultiDB sqlGenerator;
	
	String columnNames;
	
	String modifyOriginTable;
	
	String dataSourceType;
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		try {
 

			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
			dataSourceType=source.getDataSourceType();
			IntegerToTextTransformConfig config = (IntegerToTextTransformConfig)source.getAnalyticConfig();
			setDropIfExist(config.getDropIfExist());
			setOutputType(config.getOutputType());
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			columnNames=config.getColumnNames();
			modifyOriginTable=config.getModifyOriginTable();
			
			if(modifyOriginTable.equalsIgnoreCase(Resources.TrueOpt)){
				setOutputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
				setOutputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			}else{
				setOutputSchema(config.getOutputSchema());
				setOutputTable(config.getOutputTable());
			}

			sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(source.getDataSourceType());
			
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			generateStoragePrameterString((DataBaseAnalyticSource) source);
			performOperation(databaseConnection,dataSet);
			
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(((DataBaseAnalyticSource) source).getDataBaseInfo());
			if(modifyOriginTable.equalsIgnoreCase(Resources.TrueOpt)){
				outPut.setSchemaName(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
				outPut.setTableName(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			}else{
				outPut.setSchemaName(config.getOutputSchema());
				outPut.setTableName(config.getOutputTable());
			}		 
			return outPut;
			 
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		} 
	}
	
	private void performOperation(DatabaseConnection databaseConnection,DataSet dataSet) throws OperatorException, AnalysisError 
	{	
		String tableName= getQuotaedTableName(getInputSchema(),getInputTable());	
		
		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
			
		Statement st = null;
		
		String result_type=getOutputType();
			
		String[] columnArray=columnNames.split(",");
		
		ArrayList<String> columnNameList=new ArrayList<String>();
		for(String s:columnArray)
		{
			columnNameList.add(s);
		}
			
		Columns atts=dataSet.getColumns();
		
		Iterator<Column> i_atts=atts.allColumns();
		
		ArrayList<Column> needToTransformList=new ArrayList<Column>();
		
		while(i_atts.hasNext())
		{
			Column att=i_atts.next();		

			if(!columnNameList.contains((String)att.getName()))
					continue;
			if(!att.isNumerical())
				continue;
			needToTransformList.add(att);
		}
		
		if(needToTransformList.size()==0) 
		{
			logger.error("There is no column need to convert!");
			return;	
		}
		
		String resultTableName;
		
		if(modifyOriginTable.equalsIgnoreCase("true"))
		{
			resultTableName=tableName;		
			Iterator<Column> need_i=needToTransformList.iterator();
			try {
				st = databaseConnection.createStatement(false);
				while(need_i.hasNext())
				{
					Column att=need_i.next();
					String attname=StringHandler.doubleQ(att.getName());
					DBDataUtil.alterColumnType(st,attname, resultTableName, dataSourceType);
				}	
			} catch (SQLException e) {
					logger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
			} 
		
		}else
		{
			resultTableName=outputTableName;
			StringBuilder createSql;
			StringBuilder selectSql=new StringBuilder();
			StringBuilder insertTable=new StringBuilder();

				dropIfExist(dataSet);
			
				createSql=new StringBuilder("create ");
				
				if(result_type.equalsIgnoreCase("table"))
				{
					createSql.append("table ");
				}else
				{
					createSql.append("view ");
				}
				createSql.append(resultTableName);
				createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");

				createSql.append(DatabaseUtil.addParallel(databaseConnection,result_type));
				createSql.append(" as ( ");
				selectSql.append(" select ");
				Iterator<Column> all_i=atts.allColumns();
				while(all_i.hasNext())
				{
					Column att=all_i.next();
					String attname=StringHandler.doubleQ(att.getName());
					if(!needToTransformList.contains(att))
					{
						selectSql.append(attname).append(",");
					}else
					{
						selectSql.append(sqlGenerator.castToText(attname)).append(" as ");
						selectSql.append(attname).append(",");
					}
				}
				selectSql=selectSql.deleteCharAt(selectSql.length()-1);
				selectSql.append(" from ").append(tableName);
				
				createSql.append(selectSql).append(" )");
				
				if(result_type.equalsIgnoreCase("table"))
				{			
					createSql.append(getEndingString());
					insertTable.append(sqlGenerator.insertTable(selectSql.toString(), resultTableName));
				}
				
				DatabaseUtil.alterParallel(databaseConnection,result_type);//for oracle
				
				try {
				st = databaseConnection.createStatement(false);
				logger.debug("IntegerToText.apply():sql="+createSql);
				st.execute(createSql.toString());
				
				if(insertTable.length()>0){
					st.execute(insertTable.toString());
					logger.debug(
							"IntegerToText.applyNormalization():refreshTableSql=" + insertTable);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		return;
	}		
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.N2T_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.N2T_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}


}
