/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * DataSetWebModel
 * Mar 7, 2012
 */
package com.alpine.miner.impls.web.resource.operator.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.miner.impls.web.resource.operator.dataset.DatasetTransformationException.ExceptionType;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.tableset.ColumnMap;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import org.apache.log4j.Logger;

/**
 * @author Gary
 *
 */
public class DataSetWebModel {
    private static Logger itsLogger = Logger.getLogger(DataSetWebModel.class);

    private String type;
	private String firstTableName;
	private static final String KEY = "_id#";
	private static final String CUSTOMIZE_NAME = "_customize_name#";
	private static final String SPLITER = ".";
	
	private List<Map<String, String>> datasetList;
	
	private Map<String, String> aliasMap = new LinkedHashMap<String, String>();
	
	private AliasHelper helper = new AliasHelper();
	
	private List<OperatorInputTableInfo> inputTables;
	
	private String operatorUUID;
	
	public DataSetWebModel(TableSetModel model, List<Object> operatorInputList, HashMap<Object, String> inputUUIDMap) throws DatasetTransformationException{ List<OperatorInputTableInfo> inputTables = new ArrayList<OperatorInputTableInfo>();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo tableInfo;
				try {
					tableInfo = ((OperatorInputTableInfo) obj).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new DatasetTransformationException(ExceptionType.TRANSFORMATION_FAIL);
				}
				inputTables.add(tableInfo);
			}
		}
		this.inputTables = saveAlias(inputTables);
		
		if(model != null && model.getColumnMapList() != null && model.getColumnMapList().size() > 0){
			this.type = model.getSetType();
			this.firstTableName = model.getFirstTable();
			adaptDataSetList(syncSequenceOfColumnMapList(model.getColumnMapList(), inputTables,inputUUIDMap));
		}else{
			this.type = TableSetModel.TABLE_SET_TYPE[0];
			this.firstTableName = "";
			adaptDataSetList(null);
		}
	}
	
	private List<ColumnMap> syncSequenceOfColumnMapList(List<ColumnMap> columnMapList, List<OperatorInputTableInfo> reference,
			HashMap<Object, String> tableInfoUUIDMap){
		List<ColumnMap> result = new ArrayList<ColumnMap>();
		List<ColumnMap> tmpColumnMapList = new ArrayList<ColumnMap>();
		tmpColumnMapList.addAll(columnMapList);// because the columnMapList cached. then if removed any element will be crash
		result.add(tmpColumnMapList.get(0));//add output column
		referenceLoop:
		for(OperatorInputTableInfo tableInfo : reference){
			for(int i = 1;i < tmpColumnMapList.size();i++){//first columnMap contain output info
				ColumnMap columnMap;
				if(helper.hasSameTable(tableInfo.getTable())){
					columnMap = tmpColumnMapList.remove(i--);// in order to every table which has same name would be fetched.
				}else{
					columnMap = tmpColumnMapList.get(i);
				}
				if(columnMap.getTableName().equals(tableInfo.getTable())){
					result.add(columnMap);
					continue referenceLoop;
				}
			}
			result.add(new ColumnMap(tableInfo.getSchema(), tableInfo.getTable(), Collections.EMPTY_LIST, tableInfoUUIDMap.get(tableInfo)));
		}
		return result;
	}
	
	private void adaptDataSetList(List<ColumnMap> columnMapList) throws DatasetTransformationException{
		if(columnMapList == null || columnMapList.size() == 0){
//			throw new DatasetTransformationException(ExceptionType.TRANSFORMATION_FAIL);
			this.datasetList = Collections.emptyList();
			return;
		}
		int length = columnMapList.get(0).getTableColumns().size();
		List<Map<String, String>> datasetList = new ArrayList<Map<String, String>>(length);
		for(int i = 0;i < length;i++){
			datasetList.add(adapterDataset(columnMapList, i));
		}
		this.datasetList = datasetList;
	}
	
	private Map<String, String> adapterDataset(List<ColumnMap> columnMapList, int rowNum){
		Map<String, String> relativeInfo = new LinkedHashMap<String, String>();
		relativeInfo.put(KEY, String.valueOf(rowNum));
		relativeInfo.put(CUSTOMIZE_NAME, columnMapList.get(0).getTableColumns().get(rowNum));
		int i = 1;//The first element contain column output name defined by user.
		for(String alias : aliasMap.keySet()){
			ColumnMap columnMap;
			try{
				columnMap = columnMapList.get(i++);
			}catch(Exception e){
				continue;//if here means the flow file is deformed.
			}
			if(columnMap.getTableColumns().size() == 0){
				continue;
			}
			String tableName = columnMap.getSchemaName() + SPLITER + alias;
			String columnName = columnMap.getTableColumns().get(rowNum);
			relativeInfo.put(tableName, columnName);
			this.operatorUUID = columnMap.getOperatorUUID();
		}
		return relativeInfo;
	}
	
	private List<ColumnMap> buildOrigin(List<Map<String, String>> datasetList){
		List<ColumnMap> result = new ArrayList<ColumnMap>();
		Map<String, ColumnMap> columnMapContainer = new LinkedHashMap<String, ColumnMap>();
		
		for(Map<String, String> dataset : datasetList){// for each record of UI. like dataset = {_id=0, demo.golfnew=outlook, demo.qa_unit=int_id}
			for(Entry<String, String> entry : dataset.entrySet()){
				if(KEY.equals(entry.getKey())){
					continue;
				}
				if(CUSTOMIZE_NAME.equals(entry.getKey())){
					ColumnMap columnMap = columnMapContainer.get(entry.getKey());
					if(columnMap == null){
						List<String> tableColumns = new ArrayList<String> (); 
						columnMap = new ColumnMap("", "", tableColumns, "");
						columnMapContainer.put(entry.getKey(), columnMap);
					}
					columnMap.getTableColumns().add(dataset.get(entry.getKey()));
					continue;
				}
				ColumnMap columnMap = columnMapContainer.get(entry.getKey());
				if(columnMap == null){
					String[] schemaAndTableName = entry.getKey().split("\\.");
					List<String> tableColumns = new ArrayList<String> (); 
					columnMap = new ColumnMap(schemaAndTableName[0], aliasMap.get(schemaAndTableName[1]), tableColumns, operatorUUID);
					
				 
					columnMapContainer.put(entry.getKey(), columnMap);
				}
				columnMap.getTableColumns().add(dataset.get(entry.getKey()));
			}
		}
		result.addAll(columnMapContainer.values());
		return result;
	}
	
	private List<OperatorInputTableInfo> saveAlias(List<OperatorInputTableInfo> inputTableInfos){
		List<OperatorInputTableInfo> result = new ArrayList<OperatorInputTableInfo>();
		for(OperatorInputTableInfo input : inputTableInfos){
			OperatorInputTableInfo copyObj = null;
			try {
				copyObj = input.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				continue;
			}
			copyObj.setTable(buildAlias(input.getTable()));
			result.add(copyObj);
		}
		return result;
	}
	
	private String buildAlias(String tableName){
		String alias = helper.buildAlias(tableName);
		this.aliasMap.put(alias, tableName);
		return alias;
	}
	
	public TableSetModel reverse(){
		TableSetModel model = new TableSetModel();
		model.setColumnMapList(buildOrigin(this.datasetList));
		model.setFirstTable(this.firstTableName);
		model.setSetType(this.getType());
		return model;
	}
	
	public String getType() {
		return type;
	}
	public String getFirstTableName() {
		return firstTableName;
	}
	public List<Map<String, String>> getDatasetList() {
		return datasetList;
	}
	
	private static class AliasHelper{
		private Map<String, Integer> nameSeedCache = new HashMap<String, Integer>();
		String buildAlias(String tableName){
			String alias;
			Integer seed = nameSeedCache.get(tableName);
			if(seed == null){
				seed = 1;
				alias = tableName;
			}else{
				alias = tableName + "_" + seed++;
			}
			nameSeedCache.put(tableName, seed);
			return alias;
		}
		
		boolean hasSameTable(String tableName){
			return nameSeedCache.get(tableName) > 1;
		}
	}

	public List<OperatorInputTableInfo> getInputTables() {
		return inputTables;
	}
	public void setInputTables(List<OperatorInputTableInfo> inputTables) {
		this.inputTables = inputTables;
	}
	public Map<String, String> getAliasMap() {
		return aliasMap;
	}
	public void setAliasMap(Map<String, String> aliasMap) {
		this.aliasMap = aliasMap;
	}

	public String getOperatorUUID() {
		return operatorUUID;
	}

	public void setOperatorUUID(String operatorUUID) {
		this.operatorUUID = operatorUUID;
	}
}
