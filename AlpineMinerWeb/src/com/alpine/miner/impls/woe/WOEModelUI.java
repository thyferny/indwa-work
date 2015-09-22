/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WOEModelUI
 * Jan 5, 2012
 */
package com.alpine.miner.impls.woe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.woe.WoeCalculateElement.WoeCalculateInfoNode;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.woe.WOEInforList;
import com.alpine.miner.workflow.operator.parameter.woe.WOENode;
import com.alpine.miner.workflow.operator.parameter.woe.WOENominalNode;
import com.alpine.miner.workflow.operator.parameter.woe.WOENumericNode;
import com.alpine.miner.workflow.operator.parameter.woe.WOETable;
import com.alpine.utility.db.DbConnection;

/**
 * @author Gary
 *	used in initialize Property on open Property editor.
 */
public class WOEModelUI {
    private static final Logger itsLogger=Logger.getLogger(WOEModelUI.class);

    public static enum DataType{
		TEXT,NUMERIC,UNKNOW;
	}
	
	public static final String GROUP_INFO_PREFIX = "Group";

	private WoeCalculateElement[] calculateElements;
	
	private Map<String,String> columnTypeInfo = new HashMap<String,String>();
	
	private Map<String,List<String>> nominalColumnValues = new HashMap<String,List<String>>();
	
	public WOEModelUI(WOETable woeTable, UIOperatorModel operatorModel, String user, ResourceType type){
		convertWoeInfo(woeTable);
		initColumnType(operatorModel.getOperator(), user, type);
	}
	
	private void convertWoeInfo(WOETable woeTable){
		if(woeTable == null){
			calculateElements = new WoeCalculateElement[0];
			return;
		}
		List<WOEInforList> infoList = woeTable.getDataTableWOE();
		calculateElements = new WoeCalculateElement[infoList.size()];
		for(int i = 0;i < infoList.size();i++){
			WOEInforList info = infoList.get(i);
			WoeCalculateElement calculateElement = new WoeCalculateElement();
			calculateElement.setColumnName(info.getColumnName());
			calculateElement.setGini(info.getGini());
			calculateElement.setInforValue(info.getInforValue());
			calculateElement.setInforList(convertWoeNodeArray(info.getInforList()));
			calculateElements[i] = calculateElement;
		}
	}
	private WoeCalculateInfoNode[] convertWoeNodeArray(List<WOENode> woeNodeList){
		WoeCalculateInfoNode[] calculateNodeArray = new WoeCalculateInfoNode[woeNodeList.size()];
		for(int i = 0;i < woeNodeList.size();i++){
			WOENode node = woeNodeList.get(i);
			WoeCalculateInfoNode calculateNode = new WoeCalculateInfoNode();
			calculateNode.setGroupInfo(node.getGroupInfo());
			calculateNode.setWOEValue(node.getWOEValue());
			if(node instanceof WOENumericNode){
				WOENumericNode numericNode = (WOENumericNode) node;
				calculateNode.setBottom(numericNode.getBottom());
				calculateNode.setUpper(numericNode.getUpper());
			}else if(node instanceof WOENominalNode){
				WOENominalNode nominalNode = (WOENominalNode) node;
				calculateNode.setChoosedList(nominalNode.getChoosedList().toArray(new String[nominalNode.getChoosedList().size()]));
			}
			calculateNodeArray[i] = calculateNode;
		}
		return calculateNodeArray;
	}
	

	private List<WOEInforList> reverseWoeInfo(WoeCalculateElement[] calculateElements){
		List<WOEInforList> infoList = new ArrayList<WOEInforList>(calculateElements.length);
		for(int i = 0;i < calculateElements.length;i++){
			WoeCalculateElement calculateElement = calculateElements[i];
			WOEInforList info = new WOEInforList();
			if(calculateElement.getDataType() == null){//for not edit woe config and update other attributes and submit, then the data type not be initialized.
				String dataType = this.getColumnTypeInfo().get(calculateElement.getColumnName());
                if (dataType != null)
                {
                    calculateElement.setDataType(DataType.valueOf(dataType));
                } else
                {
                    calculateElement.setDataType(DataType.UNKNOW);
                }
			}
			info.setColumnName(calculateElement.getColumnName());
			info.setGini(calculateElement.getGini());
			info.setInforValue(calculateElement.getInforValue());
			info.setInforList(reverseNodeList(calculateElement.getInforList(), calculateElement.getDataType()));
			infoList.add(info);
		}
		return infoList;
	}

	private List<WOENode> reverseNodeList(WoeCalculateInfoNode[] nodeArray, DataType dataType){
		List<WOENode> woeNodeList = new ArrayList<WOENode>(nodeArray.length);
		for(int i = 0;i < nodeArray.length;i++){
			WoeCalculateInfoNode calculateNode = nodeArray[i];
			
			switch(dataType){
			case NUMERIC: 
				WOENumericNode numericNode = new WOENumericNode();
				numericNode.setGroupInfo(GROUP_INFO_PREFIX + calculateNode.getGroupInfo());
				numericNode.setWOEValue(calculateNode.getWOEValue());
				numericNode.setBottom(i == 0 ? String.valueOf(Double.NEGATIVE_INFINITY) : calculateNode.getBottom());
				numericNode.setUpper(i == nodeArray.length - 1 ? String.valueOf(Double.POSITIVE_INFINITY) : calculateNode.getUpper());
				woeNodeList.add(numericNode);
				break;
			case TEXT: 
				WOENominalNode nominalNode = new WOENominalNode();
				nominalNode.setGroupInfo(GROUP_INFO_PREFIX + calculateNode.getGroupInfo());
				nominalNode.setWOEValue(calculateNode.getWOEValue());
				nominalNode.setChoosedList(Arrays.asList(calculateNode.getChoosedList()));
				woeNodeList.add(nominalNode);
				break;
			}
		}
		return woeNodeList;
	}
	
	private void initColumnType(Operator operator, String user, ResourceType resourceType){
		String connName = OperatorUtility.getDBConnectionName(operator.getOperModel());
		if(connName == null ){
			throw new NullPointerException("Please make sure a connection for the current flow is available, and try again.");
		}
		OperatorInputTableInfo inputInfo = (OperatorInputTableInfo) operator.getOperatorInputList().get(0);
		List<String[]> columns = inputInfo.getFieldColumns();
		if(columns == null){
			//there is no parent operator, WOE can not be work on null.
			return;
		}
		String dbType = OperatorUtility.getDbType(operator);

		DbConnectionInfo dbConn;
		boolean isAddSuffixToOutput;
		try {
			dbConn = ResourceManager.getInstance().getDBConnection(user, connName, resourceType);
			String addSuffixToOutput = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX );
			isAddSuffixToOutput = Boolean.parseBoolean(addSuffixToOutput);
		} catch (Exception e) {
            itsLogger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
		
		for(String[] column : columns){
			DataType type;
			if(ParameterUtility.isNumberColumnType(column[1], dbType)){
				type = DataType.NUMERIC;
			}else{// if(ParameterUtility.isCategoryColumnType(column[1], dbType)){
				type = DataType.TEXT;
				nominalColumnValues.put(column[0], getValuesByColumn(operator.getParentOperators().get(0), column[0], dbConn.getConnection(), user, isAddSuffixToOutput));
			}
			columnTypeInfo.put(column[0], type.name());
		}
	}
	
	private List<String> getValuesByColumn(Operator previousOperator, String columnName, DbConnection dbConn,String userName, boolean isAddSuffixToOutput){
		return OperatorUtility.getAvailableValuesForEachColumn(previousOperator.getOperModel(), dbConn, columnName, isAddSuffixToOutput, userName);
	}

	public WOETable getWoeTable() {
		WOETable table = new WOETable();
		table.setDataTableWOE(reverseWoeInfo(this.getCalculateElements()));
		return table;
	}

	public Map<String, String> getColumnTypeInfo() {
		return columnTypeInfo;
	}

	public void setColumnTypeInfo(Map<String, String> columnTypeInfo) {
		this.columnTypeInfo = columnTypeInfo;
	}

	public WoeCalculateElement[] getCalculateElements() {
		return calculateElements;
	}

	public void setCalculateElements(WoeCalculateElement[] calculateElements) {
		this.calculateElements = calculateElements;
	}
}
