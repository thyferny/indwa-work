package com.alpine.miner.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.ContentsOperatorModel;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.hadoop.CopytoHadoopOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopDataOperationOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopFileOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopKmeansOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPigExecuteOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPredictOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopSampleSelectorOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopTimeSeriesPredictOperator;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.pca.PCAOperator;
import com.alpine.miner.workflow.operator.plda.PLDAPredictOperator;
import com.alpine.miner.workflow.operator.plda.PLDATrainerOperator;
import com.alpine.miner.workflow.operator.sampling.SampleSelectorOperator;
import com.alpine.miner.workflow.operator.solutions.ProductRecommendationOperator;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesPredictOperator;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class OperatorUtility {
    private static final Logger itsLogger=Logger.getLogger(OperatorUtility.class);

    public static String getOutputType(UIOperatorModel model) {
		String outputType = null;
		Operator operator = model.getOperator();
		if (operator instanceof DataOperationOperator) {
			outputType = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputType).getValue();
		} else {
			outputType = com.alpine.utility.db.Resources.OutputTypes[0];
		}
		return outputType;
	}

	public static String getSchemaName(UIOperatorModel model) {
		String schemaName = null;
		Operator operator = model.getOperator();
		if (operator instanceof SubFlowOperator &&((SubFlowOperator)operator).getExitTableInfo()!=null&&((SubFlowOperator)operator).getExitTableInfo() instanceof OperatorInputTableInfo)   {
			schemaName =((OperatorInputTableInfo) ((SubFlowOperator)operator).getExitTableInfo()).getSchema();
		}else if (operator instanceof DbTableOperator) {
			schemaName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_schemaName).getValue();
		}else if (operator instanceof CopyToDBOperator) {
			schemaName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_schemaName).getValue();
		} else if (operator instanceof SampleSelectorOperator) {
			List<Object> operatorInputList = operator.getOperatorInputList();
			for (Object obj : operatorInputList) {
				if (obj instanceof OperatorInputTableInfo) {
					schemaName = ((OperatorInputTableInfo) obj).getSchema();
					break;
				}
			}
		} else if(operator instanceof IntegerToTextOperator){
			String modifyOriginTable = (String)operator.getOperatorParameter(OperatorParameter.NAME_modifyOriginTable).getValue();
			if(modifyOriginTable.equalsIgnoreCase(Resources.TrueOpt)){
				List<UIOperatorModel> parents = getParentList(model);
				if(parents!=null&&parents.size()>0){
					schemaName=getSchemaName(parents.get(0));
					if(schemaName==null){
						return null;
					}
				}
			}else{
				if (operator
						.getOperatorParameter(OperatorParameter.NAME_outputSchema) == null) {
					return null;
				}
				schemaName = (String) operator.getOperatorParameter(
						OperatorParameter.NAME_outputSchema).getValue();
			}
		} else {
			if (operator
					.getOperatorParameter(OperatorParameter.NAME_outputSchema) == null) {
				return null;
			}
			schemaName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputSchema).getValue();
		}
		return schemaName;
	}

	public static String getHadoopFileName(UIOperatorModel model){
		String hadoopFileName = null;
		Operator operator = model.getOperator();
		if (operator instanceof HadoopFileOperator) {
			hadoopFileName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_fileName).getValue();
		} else {
			if (operator
					.getOperatorParameter(OperatorParameter.NAME_HD_ResultsName) == null) {
				return null;
			}
			if(operator
					.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults) == null){
				return null;
			}
			if(operator
					.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults).getValue().equals(Resources.TrueOpt)){
				hadoopFileName = (String)operator
						.getOperatorParameter(OperatorParameter.NAME_HD_ResultsName).getValue();
			}
		}
		return hadoopFileName;
	}
	
	public static String getTableName(UIOperatorModel model,
			boolean addSuffixToOutput,String suffixName){
		String tableName = null;
		Operator operator = model.getOperator();
		if (operator instanceof SubFlowOperator &&((SubFlowOperator)operator).getExitTableInfo()!=null&&((SubFlowOperator)operator).getExitTableInfo() instanceof OperatorInputTableInfo) {
			tableName =((OperatorInputTableInfo) ((SubFlowOperator)operator).getExitTableInfo()).getTable();
		} 
		else if (operator instanceof DbTableOperator) {
			tableName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_tableName).getValue();
		}else if (operator instanceof CopyToDBOperator) {
			tableName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_copyToTableName).getValue();
		} else if (operator instanceof SampleSelectorOperator) {
			tableName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_selectedTable).getValue();
			if (tableName.indexOf(".") > 0) {
				tableName = tableName.substring(tableName.indexOf(".") + 1,
						tableName.length());
			}

		} else if(operator instanceof IntegerToTextOperator){
			String modifyOriginTable = (String)operator.getOperatorParameter(OperatorParameter.NAME_modifyOriginTable).getValue();
			if(modifyOriginTable.equalsIgnoreCase(Resources.TrueOpt)){
				List<UIOperatorModel> parents = getParentList(model);
				if(parents!=null&&parents.size()>0){
					tableName=getTableName(parents.get(0), false, suffixName);
					if(tableName==null){
						return null;
					}
				}
			}else{
				if (operator
						.getOperatorParameter(OperatorParameter.NAME_outputTable) == null) {
					return null;
				}
				tableName = (String) operator.getOperatorParameter(
						OperatorParameter.NAME_outputTable).getValue();
			}
		}else {
			if (operator
					.getOperatorParameter(OperatorParameter.NAME_outputTable) == null) {
				return null;
			}
			tableName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputTable).getValue();
		}

		if (!(operator instanceof DbTableOperator) && addSuffixToOutput) {
			tableName = StringHandler.addPrefix(tableName, suffixName);
		}
		return tableName;
	}
	public static String getTableName(UIOperatorModel model,
			boolean addSuffixToOutput) {
		return getTableName(model, addSuffixToOutput, System
					.getProperty("user.name"));
	}

	public static List<String> getAvailableValuesForEachColumn(UIOperatorModel opModel,
			DbConnection dbConn, String columnName,boolean addSuffixToOutput,String suffixName) {
		VariableModel variableModel = opModel.getOperator().getWorkflow().getParentVariableModel();
		columnName=VariableModelUtility.getReplaceValue(variableModel, columnName);
		String schemaName = VariableModelUtility.getReplaceValue(variableModel,getSchemaName(opModel));
		schemaName=VariableModelUtility.getReplaceValue(variableModel, schemaName);
		String tableName = VariableModelUtility.getReplaceValue(variableModel,getTableName(opModel,addSuffixToOutput,suffixName));
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = AlpineUtil.createConnection(dbConn );
			st = conn.createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct ").append(
					StringHandler.doubleQ(columnName));
			sb.append(" from ").append(
					StringHandler.combinTableName(schemaName, tableName));
			rs = st.executeQuery(sb.toString());
			List<String> values = new ArrayList<String>();
			while (rs.next()) {
				String str = rs.getString(1);
				if (!StringUtil.isEmpty(str)
						||(str!=null&&str.length()>0)) {//incase space
					values.add(rs.getString(1));
				}
			}
			return values;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			return new ArrayList<String>();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
		}
	}
	public static List<String> getAvailableValuesForEachColumn(UIOperatorModel opModel,
			DbConnection dbConn, String columnName,boolean addSuffixToOutput) {
		return getAvailableValuesForEachColumn(opModel,dbConn,columnName,addSuffixToOutput,System
				.getProperty("user.name"));
	}

	public static List<UIOperatorModel> getParentList(UIOperatorModel model) {
		List<UIOperatorModel> parentList = new ArrayList<UIOperatorModel>();
		List<UIConnectionModel> connList = model.getSourceConnection();
		for (UIConnectionModel connModel : connList) {
			parentList.add(connModel.getSource());
		}
		return parentList;
	}

	public static List<UIOperatorModel> getChildList(UIOperatorModel model) {
		List<UIOperatorModel> childList = new ArrayList<UIOperatorModel>();
		List<UIConnectionModel> connList = model.getTargetConnection();
		for (UIConnectionModel connModel : connList) {
			childList.add(connModel.getTarget());
		}
		return childList;
	}

	public static List<UIOperatorConnectionModel> getConnModelList(
			ContentsOperatorModel container) {
		List<UIOperatorConnectionModel> connModelList = new ArrayList<UIOperatorConnectionModel>();
		for (String[] connModels : container.getListConnection()) {
			UIOperatorConnectionModel connModel = new UIOperatorConnectionModel();
			for (UIOperatorModel operatorModel : container.getChildren()) {
				if (operatorModel.getId().equals(connModels[0])) {
					connModel.setSource(operatorModel);
				} else if (operatorModel.getId().equals(connModels[1])) {
					connModel.setTarget(operatorModel);
				}
			}
			connModelList.add(connModel);
		}
		return connModelList;
	}

	public static Map<String, String> getColumnTypeMap(Operator operator) {
		Map<String, String> columnTypeMap = new HashMap<String, String>();
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				List<String[]> fieldColumns = ((OperatorInputTableInfo) obj)
						.getFieldColumns();
				for (String[] fieldColumn : fieldColumns) {
					columnTypeMap.put(fieldColumn[0], fieldColumn[1]);
				}
			}	else	if (obj instanceof OperatorInputFileInfo) {
					FileStructureModel columnInfos = ((OperatorInputFileInfo) obj).getColumnInfo();  
					List<String> nameList = columnInfos.getColumnNameList();
					List<String> typeList = columnInfos.getColumnTypeList();
					for (int i =0; i < nameList.size();i++) {
						columnTypeMap.put(nameList.get(i), typeList.get(i));
					}
			}
		}
		return columnTypeMap;
	}

	public static Map<String, List<String>> getFieldMap(Operator operator) {
		Map<String, List<String>> fieldMap = new HashMap<String, List<String>>();
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				String schema = operatorInputTableInfo.getSchema();
				String table = operatorInputTableInfo.getTable();
				String tableName = StringHandler.combinTableName(schema, table);
				List<String> fieldList = new ArrayList<String>();
				for (String[] fieldColumns : operatorInputTableInfo
						.getFieldColumns()) {
					fieldList.add(fieldColumns[0]);
				}
				fieldMap.put(tableName, fieldList);
			}
		}
		return fieldMap;
	}

	public static List<String[]> getAvailableFieldColumnsList(Operator operator) {
		List<String[]> availableColumns = new ArrayList<String[]>();
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				availableColumns = operatorInputTableInfo.getFieldColumns();
				return availableColumns;
			}else if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				if(columnInfo!=null &&columnInfo.getColumnNameList()!=null){
					List<String> typeList = columnInfo.getColumnTypeList();
					List<String> nameList = columnInfo.getColumnNameList();
                    if(typeList.size()==nameList.size()){
                        availableColumns=new ArrayList<String[]>();
                        for (int i =0 ;i <nameList.size();i++) {
                            availableColumns.add(new String[]{nameList.get(i),typeList.get(i)}) ;

                        }
                    }
				}
				return availableColumns;
			}
		}
		return availableColumns;
	}

	public static List<String> getAvailableColumnsList(Operator operator,
			boolean filterArray) {
		List<String[]> list = new ArrayList<String[]>();
		List<String> availableColumns = new ArrayList<String>();
		String dbType = null;
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				list = operatorInputTableInfo.getFieldColumns();
				dbType = operatorInputTableInfo.getSystem();
				break;
			}else if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				if(columnInfo!=null){
					availableColumns=columnInfo.getColumnNameList();
				}
				return availableColumns;
			}
		}
		if (list == null) {
			return availableColumns;
		}
		for (String[] fieldColumn : list) {
			if (!filterArray) {
				availableColumns.add(fieldColumn[0]);
			} else {
				if (!StringUtil.isEmpty(fieldColumn[1])&&!DataTypeConverter
						.isArrayColumnType(fieldColumn[1], dbType)
						&& !DataTypeConverter.isArrayArrayColumnType(
								fieldColumn[1], dbType)) {
					availableColumns.add(fieldColumn[0]);
				}
			}
		}
		return availableColumns;
	}
	
	public static Map<String,List<String>> getAllAvailableColumnsList(Operator operator,
			boolean filterArray) {		
		String dbType = null;
		Map<String,List<String>> availableColumnsListMap=new HashMap<String,List<String>>();
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				List<String[]> list = operatorInputTableInfo.getFieldColumns();
				dbType = operatorInputTableInfo.getSystem();
				String tableName = StringHandler.combinTableName(operatorInputTableInfo.getSchema(), operatorInputTableInfo.getTable());
				List<String> availableColumns = new ArrayList<String>();
				availableColumnsListMap.put(tableName, availableColumns);
				for (String[] fieldColumn : list) {
					if (!filterArray) {
						availableColumns.add(fieldColumn[0]);
					} else {
						if (!StringUtil.isEmpty(fieldColumn[1])&&!DataTypeConverter
								.isArrayColumnType(fieldColumn[1], dbType)
								&& !DataTypeConverter.isArrayArrayColumnType(
										fieldColumn[1], dbType)) {
							availableColumns.add(fieldColumn[0]);
						}
					}
				}
			}else if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
                List<String> availableColumns = new ArrayList<String>();
                FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
                // MINERWEB-1151 / PIVOTAL-34430729 if operatorInputFileInfo.getColumnInfo() is null catch npe & return empty availableColumns
                if(columnInfo!=null){
                    availableColumns=columnInfo.getColumnNameList();
                }
                String operatorId = operatorInputFileInfo.getOperatorUUID();
                availableColumnsListMap.put(operatorId, availableColumns);
			}
		}

		return availableColumnsListMap;
	}

	public static Map<String,List<String[]>> getAllAvailableColumnsAndTypeList(Operator operator,
			boolean filterArray) {		
		String dbType = null;
		Map<String,List<String[]>> availableColumnsListMap=new HashMap<String,List<String[]>>();
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				List<String[]> list = operatorInputTableInfo.getFieldColumns();
				dbType = operatorInputTableInfo.getSystem();
				String tableName = StringHandler.combinTableName(operatorInputTableInfo.getSchema(), operatorInputTableInfo.getTable());
				List<String[]> availableColumns = new ArrayList<String[]>();
				availableColumnsListMap.put(tableName, availableColumns);
				for (String[] fieldColumn : list) {
					if (!filterArray) {
						availableColumns.add(fieldColumn);
					} else {
						if (!StringUtil.isEmpty(fieldColumn[1])&&!DataTypeConverter
								.isArrayColumnType(fieldColumn[1], dbType)
								&& !DataTypeConverter.isArrayArrayColumnType(
										fieldColumn[1], dbType)) {
							availableColumns.add(fieldColumn);
						}
					}
				}
			}else if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
                List<String[]> availableColumns = new ArrayList<String[]>();
                FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
                // MINERWEB-1151 / PIVOTAL-34430729 if operatorInputFileInfo.getColumnInfo() is null catch npe & return empty availableColumns
                if(columnInfo!=null){
                	List<String> columnNameList = columnInfo.getColumnNameList();
                	List<String> columnTypeList = columnInfo.getColumnTypeList();
                	for(int i =0;i<columnNameList.size();i++){
                		availableColumns.add(new String[]{
                				columnNameList.get(i),
                				columnTypeList.get(i)});
                	}
                }
                String operatorId = operatorInputFileInfo.getOperatorUUID();
                availableColumnsListMap.put(operatorId, availableColumns);
			}
		}

		return availableColumnsListMap;
	}
	
	public static List<String> getAvailableNumColumnsList(Operator operator,
			boolean filterArray) {
		List<String[]> list = new ArrayList<String[]>();
		List<String> availableColumns = new ArrayList<String>();
		String dbType = null;
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				list = operatorInputTableInfo.getFieldColumns();
				dbType = operatorInputTableInfo.getSystem();
				break;
			}else if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				if(columnInfo!=null){
					availableColumns=columnInfo.getColumnNameList();
				}
				List<String> filteredColumns=new ArrayList<String>();
				if(availableColumns!=null){
					for(int i=0;i<availableColumns.size();i++){
						if(HadoopDataType.isNumberType(columnInfo.getColumnTypeList().get(i))){
							filteredColumns.add(availableColumns.get(i));
						}
					}
				}
				return filteredColumns;
			}
		}
		for (String[] fieldColumn : list) {
			if (!filterArray) {
				if (DataTypeConverter
						.isNumberColumnType(fieldColumn[1], dbType)) {
					availableColumns.add(fieldColumn[0]);
				}
			} else {
				if (!DataTypeConverter
						.isArrayColumnType(fieldColumn[1], dbType)
						&& !DataTypeConverter.isArrayArrayColumnType(
								fieldColumn[1], dbType)) {
					if (DataTypeConverter.isNumberColumnType(fieldColumn[1],
							dbType)) {
						availableColumns.add(fieldColumn[0]);
					}
				}
			}
		}
		return availableColumns;
	}

	public static List<String> filterArrayColumn(OperatorInputTableInfo fdb) {
		List<String> filterArrayColumns = new ArrayList<String>();
		List<String[]> fieldColumns = fdb.getFieldColumns();
		for (String[] fieldColumn : fieldColumns) {
			if (!DataTypeConverter.isArrayColumnType(fieldColumn[1], fdb
					.getSystem())
					&& !DataTypeConverter.isArrayArrayColumnType(
							fieldColumn[1], fdb.getSystem())) {
				filterArrayColumns.add(fieldColumn[0]);
			}
		}
		return filterArrayColumns;
	}

	public static String getDbType(Operator operator) {
		String dbType = null;
		List<Object> operatorInputList = operator.getOperatorInputList();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				dbType = operatorInputTableInfo.getSystem();
				return dbType;
			}else if(obj instanceof OperatorInputFileInfo){
				return HadoopDataType.HADOOP;
			}
		}
		return dbType;
	}

	public static List<OperatorInputTableInfo> getParentDBTableSet(
			Operator operator) {
		List<Object> operatorInputList = operator.getOperatorInputList();
		List<OperatorInputTableInfo> dbTableSets = new ArrayList<OperatorInputTableInfo>();

		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				dbTableSets.add((OperatorInputTableInfo) obj);
			}
		}
		return dbTableSets;
	}

	public static List<OperatorInputFileInfo> getParentHadoopFileInfos(
			Operator operator) {
		List<Object> operatorInputList = operator.getOperatorInputList();
		List<OperatorInputFileInfo> hadoopfileSets = new ArrayList<OperatorInputFileInfo>();

		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputFileInfo) {
				hadoopfileSets.add((OperatorInputFileInfo) obj);
			}
		}
		return hadoopfileSets;
	}
	
	public static boolean isComingFromSameHadoopConnetion(Operator operator){
		boolean isSame=true;
		List<OperatorInputFileInfo> hadoopFileInfos = getParentHadoopFileInfos(operator);
		if(hadoopFileInfos!=null&&hadoopFileInfos.size()>1){
			String oldFileInfoStr="";
			for(OperatorInputFileInfo fileInfo:hadoopFileInfos){
				String newFileInfoStr=fileInfo.getConnectionName()+fileInfo.getHdfsHostname()+fileInfo.getHdfsPort()+
						fileInfo.getJobHostname()+fileInfo.getJobPort();
				if(StringUtil.isEmpty(oldFileInfoStr)==false){
					if(oldFileInfoStr.equals(newFileInfoStr)==false){
						isSame=false;
						return isSame;
					}
				}else{
					oldFileInfoStr=newFileInfoStr;
				}
			}
		}	
		return isSame;
	}
	public static Hashtable<String, String> getParentColumnType(
			Operator operator) {
		List<OperatorInputTableInfo> dbSet = getParentDBTableSet(operator);
		for (Iterator<OperatorInputTableInfo> iterator = dbSet.iterator(); iterator
				.hasNext();) {
			OperatorInputTableInfo operatorInputTableInfo = iterator.next();
			Hashtable<String, String> fieldColumnType = new Hashtable<String, String>();
			for (String[] fieldColumn : operatorInputTableInfo
					.getFieldColumns()) {
				fieldColumnType.put(fieldColumn[0], fieldColumn[1]);
			}
			return fieldColumnType;
		}

		return null;
	}
	public static String getHadoopFilePath(UIOperatorModel model) {
		String hadoopFilePath= null;
		Operator operator = model.getOperator();
		if (operator instanceof HadoopFileOperator){
			hadoopFilePath = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_fileName).getValue();
		}
		else if(operator instanceof CopytoHadoopOperator){
			String resultsName=(String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_copyToFileName).getValue();
			String resultsLocation=(String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_ResultsLocation).getValue();
			if(StringUtil.isEmpty(resultsName)
					||StringUtil.isEmpty(resultsLocation)){
				hadoopFilePath="";
			}
			hadoopFilePath = resultsLocation+HadoopFile.SEPARATOR+resultsName;
		}
		else if(operator instanceof HadoopSampleSelectorOperator){
			hadoopFilePath = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_selectedFile).getValue();
		}else if(operator instanceof HadoopDataOperationOperator
				||operator instanceof HadoopKmeansOperator
				||operator instanceof HadoopPigExecuteOperator
				||operator instanceof HadoopPredictOperator){
			String resultsName=(String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_ResultsName).getValue();
			String resultsLocation=(String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_ResultsLocation).getValue();
			if(StringUtil.isEmpty(resultsName)
					||StringUtil.isEmpty(resultsLocation)){
				hadoopFilePath="";
			}
			hadoopFilePath = resultsLocation+HadoopFile.SEPARATOR+resultsName;
		}
		return hadoopFilePath;
	}
	public static String getHadoopConnectionName(UIOperatorModel model) {
		String hadoopConnName = null;
		Operator operator = model.getOperator();
		if (operator instanceof SubFlowOperator &&((SubFlowOperator)operator).getExitTableInfo()!=null) {
//			dbConnName  = ((SubFlowOperator)operator).getExitTableInfo().getConnectionName();
			//TODO:
		}if (operator instanceof HadoopFileOperator 
				||operator instanceof CopytoHadoopOperator
				|| operator instanceof HadoopTimeSeriesPredictOperator) {
			hadoopConnName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_connetionName).getValue();
		}
		else {
			List<Object> operatorInputList = operator.getOperatorInputList();
			for (Object obj : operatorInputList) {
				if (obj instanceof OperatorInputFileInfo) {
					hadoopConnName = ((OperatorInputFileInfo) obj)
							.getConnectionName();
					break;
				}
			}
		}
		return hadoopConnName;
	}

	
	public static FileStructureModel getHadoopFileStructureModel(UIOperatorModel model) {
		FileStructureModel fileStructureModel = null;
		Operator operator = model.getOperator();
		if (operator instanceof SubFlowOperator &&((SubFlowOperator)operator).getExitTableInfo()!=null) {
//			dbConnName  = ((SubFlowOperator)operator).getExitTableInfo().getConnectionName();
			//TODO:
		}if (operator instanceof HadoopFileOperator) {
			fileStructureModel = (FileStructureModel) operator.getOperatorParameter(
					OperatorParameter.NAME_HD_fileStructure).getValue();
		} else {
			List<Object> operatorOutputList = operator.getOperatorOutputList();
			for (Object obj : operatorOutputList) {
				if (obj instanceof OperatorInputFileInfo) {
					fileStructureModel = ((OperatorInputFileInfo) obj)
							.getColumnInfo();
					break;
				}
			}
		}
		return fileStructureModel;
	}
	public static String getDBConnectionName(UIOperatorModel model) {
		String dbConnName = null;

		Operator operator = model.getOperator();
		// OperatorParameters parameters = (OperatorParameters)
		// operator.getOperatorParameters();
		if (operator instanceof SubFlowOperator &&((SubFlowOperator)operator).getExitTableInfo()!=null&&((SubFlowOperator)operator).getExitTableInfo() instanceof OperatorInputTableInfo) {
			dbConnName  =((OperatorInputTableInfo) ((SubFlowOperator)operator).getExitTableInfo()).getConnectionName();
		}if (operator instanceof DbTableOperator) {
			dbConnName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_dBConnectionName).getValue();
		} else if (operator instanceof TimeSeriesPredictOperator) {
			dbConnName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_dBConnectionName).getValue();
		} else if (operator instanceof CopyToDBOperator) {
			dbConnName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_dBConnectionName).getValue();
		}else {
			List<Object> operatorInputList = operator.getOperatorInputList();
			for (Object obj : operatorInputList) {
				if (obj instanceof OperatorInputTableInfo) {
					dbConnName = ((OperatorInputTableInfo) obj)
							.getConnectionName();
					break;
				}
			}
		}
		return dbConnName;
	}

	public static List<String[]> getOperatorOutputTables(Operator operator,VariableModel variableModel) {
		// String[]:length=3. index 0 is schema;index 1 is tableName;index 2 is
		// tableType
		
		List<String[]> outputTables = new ArrayList<String[]>();

			if(operator instanceof SubFlowOperator){
				List<UIOperatorModel> subModels = ((SubFlowOperator)operator).getSubFlowModels();
				if(subModels!=null){
					for(int i = 0 ;i<subModels.size();i++){
						if(subModels.get(i).getOperator() instanceof DbTableOperator ==false){
							outputTables.addAll(getOperatorOutputTables(subModels.get(i).getOperator(),
									((SubFlowOperator)operator).getVariableModel()));
						}
					}
				}
	 
			}else{
	
				String[] schemaNames = getOperatorOutputSchemas(operator);
		
				String[] tableNames = getOperatorOutputTableNames(operator);
		
				String[] outputTypes = getOperatorOutputTypes(operator);
		
				for (int i = 0; i < schemaNames.length; i++) {
					String[] outputTable = new String[3];
					
					outputTable[0] = VariableModelUtility.getReplaceValue(variableModel, schemaNames[i]);	
					outputTable[1] = VariableModelUtility.getReplaceValue(variableModel, tableNames[i]);
					outputTable[2] = outputTypes[i];
					outputTables.add(outputTable);
				}
			}
  
		return outputTables;
	}

	private static String[] getOperatorOutputTypes(Operator operator) {
		if (operator instanceof DataOperationOperator) {
			String[] outputType = new String[1];
			OperatorParameter operatorParameter = operator
					.getOperatorParameter(OperatorParameter.NAME_outputType);
			if (operatorParameter == null) {
				outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
				return outputType;
			}
			outputType[0] = (String) operatorParameter.getValue();
			return outputType;
		} else if (operator instanceof SVDLanczosOperator) {
			String[] outputType = new String[3];

			outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[1] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[2] = com.alpine.utility.db.Resources.OutputTypes[0];

			return outputType;

		} else if (operator instanceof PCAOperator) {
			String[] outputType = new String[2];

			outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[1] = com.alpine.utility.db.Resources.OutputTypes[0];

			return outputType;

		} else if (operator instanceof PLDATrainerOperator) {
			String[] outputType = new String[3];

			outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[1] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[2] = com.alpine.utility.db.Resources.OutputTypes[0];
			
			return outputType;

		} else if (operator instanceof PLDAPredictOperator) {
			String[] outputType = new String[2];

			outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
			outputType[1] = com.alpine.utility.db.Resources.OutputTypes[0];

			return outputType;

		} else if (operator instanceof ProductRecommendationOperator) {
			String[] outputType = new String[1];
			outputType[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputType).getValue();
			return outputType;

		} else {
			String[] outputType = new String[1];
			outputType[0] = com.alpine.utility.db.Resources.OutputTypes[0];
			return outputType;
		}
	}

	private static String[] getOperatorOutputSchemas(Operator operator) {
		if (operator instanceof DbTableOperator) {
			String[] schemaName = new String[1];
			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_schemaName).getValue();
			return schemaName;
		} else if (operator instanceof SampleSelectorOperator) {
			String[] schemaName = new String[1];
			List<Object> operatorInputList = operator.getOperatorInputList();
			for (Object obj : operatorInputList) {
				if (obj instanceof OperatorInputTableInfo) {
					schemaName[0] = ((OperatorInputTableInfo) obj).getSchema();
					return schemaName;
				}
			}
		} else if (operator instanceof SVDLanczosOperator) {
			String[] schemaName = new String[3];

			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_UmatrixSchema).getValue();
			schemaName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_VmatrixSchema).getValue();
			schemaName[2] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_singularValueSchema).getValue();

			return schemaName;

		} else if (operator instanceof PCAOperator) {
			String[] schemaName = new String[2];

			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PCAQoutputSchema).getValue();
			schemaName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PCAQvalueOutputSchema).getValue();

			return schemaName;

		} else if (operator instanceof PLDATrainerOperator) {
			String[] schemaName = new String[3];

			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PLDAModelOutputSchema).getValue();
			schemaName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_topicOutSchema).getValue();
			schemaName[2] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_docTopicOutSchema).getValue();
			return schemaName;

		} else if (operator instanceof PLDAPredictOperator) {
			String[] schemaName = new String[2];

			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputSchema).getValue();
			schemaName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PLDADocTopicOutputSchema).getValue();

			return schemaName;

		} else if(operator instanceof CopyToDBOperator){
			return new String[]{
				(String) operator.getOperatorParameter(OperatorParameter.NAME_schemaName).getValue()
			};
		} else {
			if (operator
					.getOperatorParameter(OperatorParameter.NAME_outputSchema) == null) {
				return new String[0];
			}
			String[] schemaName = new String[1];
			schemaName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputSchema).getValue();
			return schemaName;
		}
		return new String[0];
	}

	private static String[] getOperatorOutputTableNames(Operator operator) {
		if (operator instanceof DbTableOperator) {
			String[] tableName = new String[1];
			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_tableName).getValue();
			return tableName;
		} else if (operator instanceof SampleSelectorOperator) {
			String[] tableName = new String[1];
			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_selectedTable).getValue();
			if (!StringUtil.isEmpty(tableName[0])) {
				if (tableName[0].indexOf(".") > 0) {
					tableName[0] = tableName[0].substring(tableName[0]
							.indexOf(".") + 1, tableName[0].length());
				}
			}
			return tableName;
		} else if (operator instanceof SVDLanczosOperator) {
			String[] tableName = new String[3];

			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_UmatrixTable).getValue();
			tableName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_VmatrixTable).getValue();
			tableName[2] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_singularValueTable).getValue();

			return tableName;

		} else if (operator instanceof PCAOperator) {
			String[] tableName = new String[2];

			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PCAQoutputTable).getValue();
			tableName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PCAQvalueOutputTable).getValue();

			return tableName;

		} else if (operator instanceof PLDATrainerOperator) {
			String[] tableName = new String[3];

			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PLDAModelOutputTable).getValue();
			tableName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_topicOutTable).getValue();
			tableName[2] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_docTopicOutTable).getValue();
			return tableName;

		} else if (operator instanceof PLDAPredictOperator) {
			String[] tableName = new String[2];

			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputTable).getValue();
			tableName[1] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_PLDADocTopicOutputTable).getValue();
			return tableName;

		} else if(operator instanceof CopyToDBOperator){
			return new String[]{
					(String) operator.getOperatorParameter(OperatorParameter.NAME_HD_copyToTableName).getValue()
			};
		} else {
			if (operator
					.getOperatorParameter(OperatorParameter.NAME_outputTable) == null) {
				return new String[0];
			}
			String[] tableName = new String[1];
			tableName[0] = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_outputTable).getValue();
			return tableName;
		}
	}

	public static Map<String, String> refreshTableInfo(Operator operator,
			String userName, ResourceType resourceType) {
		DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
				.getManager();
		if (StringUtil.isEmpty(userName) || resourceType == null) {
			return null;
		}
		if (operator
				.getOperatorParameter(OperatorParameter.NAME_dBConnectionName) == null) {
			return null;
		}
		String connName = (String) operator.getOperatorParameter(
				OperatorParameter.NAME_dBConnectionName).getValue();
		if (StringUtil.isEmpty(connName)) {
			return null;
		}
		Map<String, String> paraMap = new HashMap<String, String>();
		try {
			DbConnection dbconn = dbManager.getDBConnection(userName, connName,
					resourceType).getConnection();
			paraMap.put(OperatorParameter.NAME_UserName, dbconn.getDbuser());
			paraMap.put(OperatorParameter.NAME_URL, dbconn.getUrl());
		 
			paraMap.put(OperatorParameter.NAME_USESSL, dbconn.getUseSSL());
			paraMap.put(OperatorParameter.NAME_Password, XmlDocManager
					.encryptedPassword(dbconn.getPassword()));
			paraMap.put(OperatorParameter.NAME_System, dbconn.getDbType());
		} catch (Exception e) {
			//if no connection, will get empty map and do no refresh.
			itsLogger.error(e.getMessage(),e);
			e.printStackTrace();
		}
		return paraMap;
	}
	
	public static Map<String, String> refreshHadoopFileInfo(HadoopOperator operator,
			String userName, ResourceType resourceType) {
		HadoopConnectionManagerIfc dbManager = HadoopConnectionManagerFactory.INSTANCE
				.getManager();
		if (StringUtil.isEmpty(userName) || resourceType == null) {
			return null;
		}
		if (operator
				.getOperatorParameter(OperatorParameter.NAME_HD_connetionName ) == null) {
			return null;
		}
		String connName = (String) operator.getOperatorParameter(
				OperatorParameter.NAME_HD_connetionName).getValue();
		if (StringUtil.isEmpty(connName)) {
			return null;
		}
		Map<String, String> paraMap = new HashMap<String, String>();
		try {
			HadoopConnection hadoopConn = dbManager.readHadoopConnection(  connName, userName,
					resourceType) ;
			 
			
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_hdfsHostname, hadoopConn.getHdfsHostName()  );
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_hdfsPort, String.valueOf( hadoopConn.getHdfsPort() ) );
		 
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_jobHostname , hadoopConn.getJobHostName()   );
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_jobPort,String.valueOf(  hadoopConn.getJobPort() ) );
			
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_version , hadoopConn.getVersion()  );
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_user, hadoopConn.getUserName() );
			paraMap.put(HadoopFileSelectorConfig.NAME_HD_group, hadoopConn.getGroupName() );
			
			
			paraMap.put (HadoopFileSelectorConfig.NAME_HD_securityMode, hadoopConn.getSecurityMode() );
			paraMap.put (HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal, hadoopConn.getHdfsPrincipal() );
			paraMap.put (HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab, hadoopConn.getHdfsKeyTab() );
			paraMap.put (HadoopFileSelectorConfig.NAME_HD_mapredPrincipal, hadoopConn.getMapredPrincipal() );
			paraMap.put (HadoopFileSelectorConfig.NAME_HD_mapredKeyTab, hadoopConn.getMapredKeyTab() );
			
		} catch (Exception e) {
			//if no connection, will get empty map and do no refresh.
			itsLogger.error(e.getMessage(),e);
			e.printStackTrace();
		}
		return paraMap;
	}
	
	

	public static boolean hasInputTable( Operator operator,String schemaName, String tableName) {
		List<Object> inputList = operator.getOperatorInputList();
		if(inputList!=null){
			for(int i = 0 ;i<inputList.size();i++){
				if(inputList.get(i) instanceof OperatorInputTableInfo){
					OperatorInputTableInfo tableInfo = (OperatorInputTableInfo) inputList.get(i);
					if(tableInfo.getSchema()!=null&&tableInfo.getSchema().equals(schemaName)
							&&tableInfo.getTable()!=null&&tableInfo.getTable().equals(tableName)){
						return true;
					}
				}
			}
		}
		 
		return false;
	}

	/**
	 * get operator parent contents
	 * @return
	 */
	public static boolean checkLoopOperator(UIOperatorModel source,UIOperatorModel target){
		return findLoopOperator(source,target);
	}
	
	private static boolean findLoopOperator(UIOperatorModel parent,UIOperatorModel target){
		for(UIOperatorModel om:OperatorUtility.getParentList(parent)){
			if(om.getId().equals(target.getId())){
				return true;
			}else{
				boolean loop = findLoopOperator(om,target);
				if(loop){
					return true;
				}
			}
		}
		return false;
	}

    /**
     * Functions for generating output table names
     */

    public static String getOperatorOutputTableName(String operatorClassName, String paraName, List<UIOperatorModel> opModelList) {
        if(opModelList!=null){
            int index=0;
            String operatorNaming = com.alpine.miner.inter.resources.Resources.getOperatorNaming(operatorClassName);
            String outputTableName = VariableModel.DEFAULT_PREFIX  +"_"+operatorNaming+"_"+index;
            outputTableName = handleMultiOutputTable(paraName,
                    outputTableName);
            while(isOutputTableNameExists(outputTableName,paraName,opModelList)==true){
                index = index+1;
                outputTableName =  VariableModel.DEFAULT_PREFIX  +"_"+operatorNaming+"_"+index;
                outputTableName = handleMultiOutputTable(paraName,
                        outputTableName);
            }
            return outputTableName;
        }
        return null;
    }

    /**
     * handles operators that have more than one output table - we need to distinguish between them
     * @param paraName
     * @param outputTableName
     * @return
     */
    private static String handleMultiOutputTable(String paraName,
                                          String outputTableName) {
        if(paraName.equals(OperatorParameter.NAME_UmatrixTable)){
            outputTableName=outputTableName+"_1";
        }else if(paraName.equals(OperatorParameter.NAME_VmatrixTable)){
            outputTableName=outputTableName+"_2";
        }else if(paraName.equals(OperatorParameter.NAME_singularValueTable)){
            outputTableName=outputTableName+"_3";
        }else if(paraName.equals(OperatorParameter.NAME_PCAQoutputTable)){
            outputTableName=outputTableName+"_1";
        }else if(paraName.equals(OperatorParameter.NAME_PCAQvalueOutputTable)){
            outputTableName=outputTableName+"_2";
        }else if(paraName.equals(OperatorParameter.NAME_PLDAModelOutputTable)){
            outputTableName=outputTableName+"_1";
        }else if(paraName.equals(OperatorParameter.NAME_topicOutTable)){
            outputTableName=outputTableName+"_2";
        }else if(paraName.equals(OperatorParameter.NAME_docTopicOutTable)){
            outputTableName=outputTableName+"_3";
        }else if(paraName.equals(OperatorParameter.NAME_PLDADocTopicOutputTable)){
            outputTableName=outputTableName+"_1";
        }
        return outputTableName;
    }

    private static boolean isOutputTableNameExists(String outputTableName, String paraName, List<UIOperatorModel> models) {
        for (Iterator<UIOperatorModel> iterator = models.iterator(); iterator.hasNext();) {
            UIOperatorModel uiOperatorModel =iterator.next();
            Operator op = uiOperatorModel.getOperator();
            OperatorParameter operatorParameter = ParameterUtility.getParameterByName(op, paraName);
            if(operatorParameter!=null){
                Object obj = operatorParameter.getValue();
                if(obj!=null){
                    if(((String)obj).equals(outputTableName) ==true){
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
