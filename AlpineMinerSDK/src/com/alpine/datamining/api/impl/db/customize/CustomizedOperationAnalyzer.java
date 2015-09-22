/**
 * ClassName CustomizedOperationAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.customize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.DataOperationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.datamining.api.impl.db.attribute.model.customized.ParameterModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class CustomizedOperationAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(CustomizedOperationAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		DataSet dataSet;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			CustomziedConfig config = (CustomziedConfig) source.getAnalyticConfig();
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());
			AnalysisStorageParameterModel analysisStorageParameterModel = config.getStorageParameters();

			dropIfExist(dataSet);
			
			String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
			
			String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());
			
			String dataSource=((DBTable) dataSet.
					getDBTable()).getDatabaseConnection().getProperties().getName();
			
			String udfSchema=config.getUdfSchema();
			String udfName=config.getUdfName();
			HashMap<String, String>  paraMap=config.getParametersMap();
			
			CustomizedOperatorModel coModel=getCustomizedOperatorModel(config.getOperatorName());
			HashMap<String, String> ctrlTypeMap=new HashMap<String, String>();
			HashMap<String, String> columnTypeMap=new HashMap<String, String>();
			HashMap<String, List<String>> opValueMap=new HashMap<String, List<String>>();
			
			getModelInfo(coModel, ctrlTypeMap, columnTypeMap,opValueMap);
			
			
			StringBuilder sb_column=new StringBuilder();
			sb_column.append(udfSchema).append(".").append(udfName).append("(");
			for(int i=1;i<paraMap.size()+1;i++){
				if(StringUtil.isEmpty(paraMap.get(String.valueOf(i))))continue;
				String column=null;
				String ctrlType=ctrlTypeMap.get(String.valueOf(i));
				String columnType=columnTypeMap.get(String.valueOf(i));
				if(ctrlType.equalsIgnoreCase("columndialog")){
					String columns=paraMap.get(String.valueOf(i));
					String[] temp=columns.split(",");
					StringBuilder sb_new=new StringBuilder();
					for(int j=0;j<temp.length;j++){
						sb_new.append(StringHandler.doubleQ(temp[j])).append(",");
					}
					sb_new=sb_new.deleteCharAt(sb_new.length()-1);
					if(!StringUtil.isEmpty(columnType)){
						column=AlpineUtil.getArrayName(columnType,dataSource,sb_new.toString());
					}else{
						column=sb_new.toString();
					}
				}else if(ctrlType.equalsIgnoreCase("combo")){
					List<String> opValue=opValueMap.get(String.valueOf(i));
					if(opValue!=null&&opValue.size()>0&&!StringUtil.isEmpty(columnType)&&columnType.equalsIgnoreCase("number")){
						column=paraMap.get(String.valueOf(i));
					}else if(opValue!=null&&opValue.size()>0&&!StringUtil.isEmpty(columnType)&&columnType.equalsIgnoreCase("text")){
						column="'"+paraMap.get(String.valueOf(i))+"'";
					}else{
						column=StringHandler.doubleQ(paraMap.get(String.valueOf(i)));
					}		
				}else if(ctrlType.equalsIgnoreCase("text")){
					if(StringUtil.isEmpty(columnType)||(!StringUtil.isEmpty(columnType)&&columnType.equalsIgnoreCase("number"))){
						column=paraMap.get(String.valueOf(i));
					}else if(!StringUtil.isEmpty(columnType)&&columnType.equalsIgnoreCase("text")){
						column="'"+paraMap.get(String.valueOf(i))+"'";
					}else{
						column=StringHandler.doubleQ(paraMap.get(String.valueOf(i)));
					}	
				}	
				sb_column.append(column).append(",");
			}
			sb_column=sb_column.deleteCharAt(sb_column.length()-1);
			sb_column.append(")");	
			
			boolean appendOnly = false;
			if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
				appendOnly = false;
			}else{
				appendOnly = true;
			}
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());

			StringBuilder createSql =new StringBuilder("create ");
			StringBuilder selectSql = new StringBuilder();
			StringBuilder insertTable = new StringBuilder();
			createSql.append(getOutputType()).append(" ").append(outputTableName);
			if(getOutputType().equalsIgnoreCase("table")){
				createSql.append(
						appendOnly ? sqlGenerator.getStorageString(
						analysisStorageParameterModel.isAppendOnly(),
						analysisStorageParameterModel.isColumnarStorage(),
						analysisStorageParameterModel.isCompression(),
						analysisStorageParameterModel.getCompressionLevel())
						: " ");
			}
			createSql.append(" as (").append(" select ");
			HashMap<String, String>  outputMap=config.getOutputMap();
			if(outputMap.size()>1){
				Set<String> keyset=outputMap.keySet();
				Iterator<String> iter=keyset.iterator();
				while(iter.hasNext()){
					String name=iter.next();
//					String type = AlpineUtil.converterDateType(outputMap.get(name), dataSource);
					selectSql.append("(").append(sb_column.toString()).append(").");
					selectSql.append(StringHandler.doubleQ(name)).append(",");//.append("::").append(type)
				}
				selectSql=selectSql.deleteCharAt(selectSql.length()-1);
			}else{
				Set<String> keyset=outputMap.keySet();
				Iterator<String> iter=keyset.iterator();
				while(iter.hasNext()){
					String name=iter.next();
//					String type = AlpineUtil.converterDateType(outputMap.get(name), dataSource);
					selectSql.append(sb_column.toString());
					selectSql.append(" as ").append(StringHandler.doubleQ(name));
				}
			}
			String sourceColumns=config.getRemainColumns();
			if(!StringUtil.isEmpty(sourceColumns)){
				String[] temp=sourceColumns.split(",");
				for(int i=0;i<temp.length;i++){
					selectSql.append(",").append(StringHandler.doubleQ(temp[i]));
				}
			}
			selectSql.append(" from ").append(inputTableName);
			createSql.append(selectSql).append(" )");
			if(getOutputType().equalsIgnoreCase("table")){
				createSql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()));
				insertTable.append(sqlGenerator.insertTable(selectSql.toString(),outputTableName));
			}

			Statement st = null;
			try {
				st = databaseConnection.createStatement(false);
				logger.debug("CustomizedOperationAnalyzer.performOperation():sql="+createSql.toString());
				st.executeUpdate(createSql.toString());
				if(insertTable.length()>0){
					st.execute(insertTable.toString());
					logger.debug(
							"CustomizedOperationAnalyzer.doAnalysis():insertTable=" + insertTable);
				}
				
				DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
				AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
				outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
				outPut.setDbInfo(dbInfo);
				outPut.setSchemaName(getOutputSchema());
				outPut.setTableName(getOutputTable());
				return outPut;
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
	private void getModelInfo(CustomizedOperatorModel coModel,
			HashMap<String, String> ctrlTypeMap,
			HashMap<String, String> columnTypeMap,
			HashMap<String, List<String>> opValueMap) {
		HashMap<String, ParameterModel>  paraModelMap=coModel.getParaMap();
		Iterator<Entry<String, ParameterModel>>  iter_entry=paraModelMap.entrySet().iterator();
		while(iter_entry.hasNext()){
			Entry<String, ParameterModel> entry=iter_entry.next();
			String position=entry.getValue().getPosition();
			String paraType=entry.getValue().getParaType();
			String columnType=entry.getValue().getDataType();
			  List<String> opValue = entry.getValue().getOptionalValue();
			ctrlTypeMap.put(position, paraType);
			columnTypeMap.put(position, columnType);
			opValueMap.put(position, opValue);
		}
	}
	private CustomizedOperatorModel getCustomizedOperatorModel(String operatorName){
		String filePath=CustomziedConfig.getObjectPath()+File.separator+operatorName+CustomziedConfig.MODEL_SUFFIX;
		ObjectInputStream ois=null;
		try {
			FileInputStream fis = new FileInputStream(filePath);
			ois = new ObjectInputStream(fis);
			CustomizedOperatorModel coModel=(CustomizedOperatorModel)ois.readObject();
			return coModel;
		} catch (Exception e) {
			logger.error(getClass().getName()+"\n"+e.toString());
		} finally{
			if(ois!=null){
				try {
					ois.close();
				} catch (IOException e) {
					logger.error(CustomizedOperationAnalyzer.class.getName()+"\n"+e.toString());
				}
			}
		}
	
		return null;
}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.AGGREGATE_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.AGGREGATE_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
