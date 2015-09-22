/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WOEDataServiceImpl
 * Jan 9, 2012
 */
package com.alpine.miner.impls.woe.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.TableInfo;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEAutoGroup;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutWOE;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.woe.IWOEDataService;
import com.alpine.miner.impls.woe.WoeCalculateElement;
import com.alpine.miner.impls.woe.WoeCalculateElement.WoeCalculateInfoNode;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.woe.WOEInforList;
import com.alpine.miner.workflow.operator.parameter.woe.WOENode;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author Gary
 *
 */
public class WOEDataServiceImpl implements IWOEDataService {

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.woe.IWOEDataService#autoGroup()
	 */
	@Override
	public List<WoeCalculateElement> autoCalculate(Operator operator, WoeCalculateElement[] elements, String user, ResourceType resourceType) throws AnalysisException {
		DataBaseAnalyticSource analyticSource;
		try {
			analyticSource = buildCalculateParameter(operator, elements, user, resourceType);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		
		AnalyzerOutPutWOE output = (AnalyzerOutPutWOE) WOEAutoGroup.autoGroup(analyticSource);
		List<AnalysisWOEColumnInfo> columnList = output.getResultList().getDataTableWOE();
		List<WoeCalculateElement> result = new ArrayList<WoeCalculateElement>(columnList.size());
		for(AnalysisWOEColumnInfo item : columnList){
			result.add(convert(item));
		}
		return result;
	}
	
	public WoeCalculateElement calculate(Operator operator, WoeCalculateElement[] elements, String user, ResourceType resourceType) throws AnalysisException{
		DataBaseAnalyticSource analyticSource;
		try {
			analyticSource = buildCalculateParameter(operator, elements, user, resourceType);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		WeightOfEvidenceConfig woeConf = (WeightOfEvidenceConfig) analyticSource.getAnalyticConfig();
		AnalysisWOETable woeTableInfor = new AnalysisWOETable();
		List<AnalysisWOEColumnInfo> dataTableWOE = new ArrayList<AnalysisWOEColumnInfo>();
		woeTableInfor.setDataTableWOE(dataTableWOE);
		
		for(WoeCalculateElement element : elements){// may just only one row.
			AnalysisWOEColumnInfo woeInfo = new AnalysisWOEColumnInfo();
			List<AnalysisWOENode> infoList = new ArrayList<AnalysisWOENode>();
			woeInfo.setInforList(infoList);
			final WoeCalculateInfoNode[] inforNodeArray = element.getInforList();
			
			AnalysisWOENodeBuilder builder = null;
			switch(element.getDataType()){
			case NUMERIC: 
				final int lastIdx = inforNodeArray.length - 1;
				builder = new AnalysisWOENodeBuilder() {
					@Override
					public AnalysisWOENode build(WoeCalculateInfoNode node, VariableModel variable, int index) {
						AnalysisWOENumericNode numericNode = (AnalysisWOENumericNode) AnalysisWOENodeBuilder.NUMERIC_BUILDER.build(node, variable, index);
						if(index == 0){
							//if first node then set bottom to -Infinity
							numericNode.setBottom(Double.NEGATIVE_INFINITY);
						}else if(index == lastIdx){
							//if last node then set upper to Infinity
							numericNode.setUpper(Double.POSITIVE_INFINITY);
						}
						return numericNode;
					}
				};
				break;
			case TEXT: 
				builder = AnalysisWOENodeBuilder.NOMINAL_BUILDER;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported type: " + element.getDataType() + " in " + element.getColumnName() + " column.");
			}
			for(int i = 0;i< inforNodeArray.length;i++){// for each info node.
				WoeCalculateInfoNode node = inforNodeArray[i];
				AnalysisWOENode woeNode = builder.build(node, operator.getWorkflow().getVariableModelList().get(0), i);
				infoList.add(woeNode);
			}
			woeInfo.setColumnName(element.getColumnName());
			dataTableWOE.add(woeInfo);
		}
		
		woeConf.setWOETableInfor(woeTableInfor);
		
		AnalyzerOutPutWOE output = (AnalyzerOutPutWOE) WOEAutoGroup.computeWOE(analyticSource);
		List<AnalysisWOEColumnInfo> columnList = output.getResultList().getDataTableWOE();
		WoeCalculateElement calculatedElement = convert(columnList.get(0));
		if(elements.length == 1){
			WoeCalculateElement element = elements[0];
			int i = 0;
			for(WoeCalculateInfoNode node : element.getInforList()){
				if(node.getBottom() != null)
					calculatedElement.getInforList()[i].setBottom(node.getBottom());
				if(node.getUpper() != null)
					calculatedElement.getInforList()[i].setUpper(node.getUpper());
				i++;
			}
		}
		return calculatedElement;
	}
	
	private WoeCalculateElement convert(AnalysisWOEColumnInfo origin){
		WoeCalculateElement result = new WoeCalculateElement();
		result.setColumnName(origin.getColumnName());
		result.setGini(origin.getGini());
		result.setInforValue(origin.getInforValue());
		List<AnalysisWOENode> originWOENodeList = origin.getInforList() == null ? Collections.EMPTY_LIST : origin.getInforList();
		WoeCalculateInfoNode[] nodeArray = new WoeCalculateInfoNode[originWOENodeList.size()];
		for(int i = 0;i < originWOENodeList.size();i++){
			AnalysisWOENode nodeItem = originWOENodeList.get(i);
			WoeCalculateInfoNode node = new WoeCalculateInfoNode();
			node.setGroupInfo(nodeItem.getGroupInfror());
			node.setWOEValue(nodeItem.getWOEValue());
			if(nodeItem instanceof AnalysisWOENumericNode){
				node.setBottom(String.valueOf(((AnalysisWOENumericNode) nodeItem).getBottom()));
				node.setUpper(String.valueOf(((AnalysisWOENumericNode) nodeItem).getUpper()));
			}else if(nodeItem instanceof AnalysisWOENominalNode){
				List<String> optionalValues = ((AnalysisWOENominalNode) nodeItem).getChoosedList();
				node.setChoosedList(optionalValues.toArray(new String[optionalValues.size()]));
			}
			nodeArray[i] = node;
		}
		result.setInforList(nodeArray);
		return result;
	}
	
	private DataBaseAnalyticSource buildCalculateParameter(Operator operator, WoeCalculateElement[] elements, String user, ResourceType resourceType) throws Exception{
		String connName = OperatorUtility.getDBConnectionName(operator.getOperModel());
		DbConnection dbConn;

		dbConn = ResourceManager.getInstance().getDBConnection(user, connName, resourceType).getConnection();

		Connection conn = AlpineUtil.createConnection(dbConn);
		
		DataBaseAnalyticSource analyticSource = new DataBaseAnalyticSource();
		analyticSource.setDataBaseInfo(new DataBaseInfo(dbConn.getDbType(), dbConn.getUrl(), dbConn.getDbuser(), dbConn.getPassword(), dbConn.getUseSSL()));
		analyticSource.setConenction(conn);
		analyticSource.setTableInfo(buildTableInfo(operator, elements,user));
		WeightOfEvidenceConfig config = new WeightOfEvidenceConfig();
		config.setDependentColumn((String) operator.getOperatorParameter(OperatorParameter.NAME_dependentColumn).getValue());
		config.setGoodValue(VariableModelUtility.getReplaceValue(operator.getWorkflow().getVariableModelList().get(0), (String) operator.getOperatorParameter(OperatorParameter.NAME_goodValue).getValue()));
		StringBuilder sb = new StringBuilder();
		for(WoeCalculateElement column : elements){
			sb.append(column.getColumnName()).append(",");
		}
		String columnNames = sb.length() > 1 ? sb.substring(0, sb.length() - 1) : sb.toString();
		config.setColumnNames(columnNames);
		analyticSource.setAnalyticConfiguration(config);
		return analyticSource;
	}
	
	private TableInfo buildTableInfo(Operator operator, WoeCalculateElement[] elements, String userName) throws Exception{

		ResourceManager rmgr = ResourceManager.getInstance();
		String addSuffixToOutput;
		try {
			addSuffixToOutput = rmgr.getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX );
		} catch (Exception e) {
			throw e;
		}
		
		UIOperatorModel firstOperator = OperatorUtility.getParentList(operator.getOperModel()).get(0);
		String schemaName = VariableModelUtility.getReplaceValue(operator.getWorkflow().getVariableModelList().get(0), OperatorUtility.getSchemaName(firstOperator));
		String tableName = VariableModelUtility.getReplaceValue(operator.getWorkflow().getVariableModelList().get(0), OperatorUtility.getTableName(firstOperator, Boolean.parseBoolean(addSuffixToOutput),userName));
		List<TableColumnMetaInfo> activeColumnList = new ArrayList<TableColumnMetaInfo>(elements.length);
		for(WoeCalculateElement column : elements){
			activeColumnList.add(new TableColumnMetaInfo(column.getColumnName(),OperatorUtility.getDbType(operator)));
		}
		return new TableInfo(VariableModelUtility.getReplaceValue(operator.getWorkflow().getVariableModelList().get(0), schemaName), tableName, activeColumnList);
	}
	
	/**
	 * just for convert node
	 * @author Gary
	 *
	 */
	private static interface AnalysisWOENodeBuilder{

		AnalysisWOENodeBuilder NUMERIC_BUILDER = new AnalysisWOENodeBuilder(){
			@Override
			public AnalysisWOENode build(WoeCalculateInfoNode node, VariableModel variable, int index) {
				AnalysisWOENumericNode result = new AnalysisWOENumericNode();
				if(node.getBottom() != null)//avoid -infinity
					result.setBottom(Double.parseDouble(VariableModelUtility.getReplaceValue(variable, node.getBottom())));
				result.setGroupInfror(node.getGroupInfo());
				if(node.getUpper() != null)//avoid infinity
					result.setUpper(Double.parseDouble(VariableModelUtility.getReplaceValue(variable, node.getUpper())));
				result.setWOEValue(node.getWOEValue());
				return result;
			}
		};
		
		AnalysisWOENodeBuilder NOMINAL_BUILDER = new AnalysisWOENodeBuilder() {
			@Override
			public AnalysisWOENode build(WoeCalculateInfoNode node, VariableModel variable, int index) {
				AnalysisWOENominalNode result = new AnalysisWOENominalNode();
				result.setChoosedList(Arrays.asList(node.getChoosedList()));
				result.setGroupInfror(node.getGroupInfo());
				result.setWOEValue(node.getWOEValue());
				return result;
			}
		};
		
		AnalysisWOENode build(WoeCalculateInfoNode node, VariableModel variable, int index);
	}

	public static class WOEInfoListAdapter extends WOEInforList{
		private AnalysisWOEColumnInfo proxy;
		private WOEInfoListAdapter(AnalysisWOEColumnInfo proxy){
			this.proxy = proxy;
			
			List<AnalysisWOENode> analysisWOENode = proxy.getInforList();
			List<WOENode> woeNodeList = new ArrayList<WOENode>(analysisWOENode.size());
			for(AnalysisWOENode orign : analysisWOENode){
				woeNodeList.add(new WOENodeAdapter(orign));
			}
			super.setInforList(woeNodeList);
		}
		@Override
		public String getColumnName() {
			return proxy.getColumnName();
		}
		@Override
		public double getGini() {
			return proxy.getGini();
		}
		@Override
		public List<WOENode> getInforList() {
			return super.getInforList();
		}
		@Override
		public double getInforValue() {
			return proxy.getInforValue();
		}
		@Override
		public double getWOEValue(String groupInfo) {
			return proxy.getWOEValue(groupInfo);
		}
	}
	public static class WOENodeAdapter extends WOENode{
		private AnalysisWOENode proxy;
		
		private WOENodeAdapter(AnalysisWOENode proxy){
			this.proxy = proxy;
		}

		@Override
		public String getGroupInfo() {
			return proxy.getGroupInfror();
		}

		@Override
		public double getWOEValue() {
			return proxy.getWOEValue();
		}
	}
}
