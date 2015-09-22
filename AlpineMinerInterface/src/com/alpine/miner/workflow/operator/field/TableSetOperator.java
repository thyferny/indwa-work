/**
 * ClassName TableSetOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-11
 *
 * COPYRIGHT (C) 2010,2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.tableset.ColumnMap;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class TableSetOperator extends DataOperationOperator {
    private static final Logger itsLogger=Logger.getLogger(TableSetOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_tableSetConfig,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist
 
	});
	
	public TableSetOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TABLESET_OPERATOR,locale);
	}
	
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput); 
		
		OperatorParameter tableSetConfig=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_tableSetConfig);
		TableSetModel model = (TableSetModel)tableSetConfig.getValue();
		if(model!=null){
			if(addSuffixToOutput){
				TableSetModel newModel = null;
				try {
					newModel = model.clone();
				} catch (CloneNotSupportedException e) {
					itsLogger.error(e.getMessage(),e);
				}
				if(model!=null&&newModel!=null){
					HashMap<String,Boolean> isParentDbTableMap=new HashMap<String,Boolean>();
					List<UIOperatorModel> opParentsModelList=new ArrayList<UIOperatorModel>();
					List<UIConnectionModel> connList=this.getOperModel().getSourceConnection();
					for(UIConnectionModel connModel:connList){
						opParentsModelList.add(connModel.getSource());
					}
					
					for(UIOperatorModel opModel:opParentsModelList){
						List<OperatorParameter> operatorParameterList=opModel.getOperator().getOperatorParameterList();
						
						//miner 1987
						if(opModel.getOperator() instanceof SubFlowOperator){
							Operator	exitOperator = ((SubFlowOperator)opModel.getOperator()).getExitOperator();
							if(exitOperator!=null){
								operatorParameterList=exitOperator.getOperatorParameterList();
							}
						}
						for(OperatorParameter operatorParameter:operatorParameterList){
							String key=operatorParameter.getName();
							Object obj=operatorParameter.getValue();
							if(!(obj instanceof String))continue;
							String value=(String)operatorParameter.getValue();
							if(key.equals(XmlDocManager.TABLE_NAME)){
								isParentDbTableMap.put(value, true);
								break;
							}
							if(key.equals(XmlDocManager.SELECTED_OUTPUT_TABLE)){
								String[] temp=value.split("\\.");
								value=temp[1];
							}
							isParentDbTableMap.put(value, false);
						}
					}
					
					String[] tableArray = StringHandler.splitQuatedTableName(newModel.getFirstTable());
					
					if(isParentDbTableMap.get(tableArray[1])!=null&&!isParentDbTableMap.get(tableArray[1])){
						newModel.setFirstTable(StringHandler.combinTableName(tableArray[0], StringHandler.addPrefix(tableArray[1],userName)));
					}
					
					List<ColumnMap> columnMapList = newModel.getColumnMapList();
					Iterator<ColumnMap> iter=columnMapList.iterator();
					while(iter.hasNext()){
						ColumnMap columnMap = iter.next();
						if(isParentDbTableMap.get(columnMap.getTableName())!=null&&!isParentDbTableMap.get(columnMap.getTableName())){
							columnMap.setTableName(StringHandler.addPrefix(columnMap.getTableName(), userName));
						}	
					}
				}
				element.appendChild(newModel.toXMLElement(xmlDoc));
			}else{
				element.appendChild(model.toXMLElement(xmlDoc));
			}		
		}
	 
	}


	@Override
	public boolean isVaild(VariableModel variableModel) {	
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			 if(paraName.equals(OperatorParameter.NAME_tableSetConfig)){
				 TableSetModel tableSetModel = (TableSetModel)para.getValue(); 
				 if(para.getValue()==null||tableSetModel.isValid()==false){
						invalidParameterList.add(paraName);
				 }
				 else if(TableSetModel.TABLE_SET_TYPE[3].equals(tableSetModel.getSetType())){
					 if(fisrtTableExists(tableSetModel)==false){
						 invalidParameterList.add(paraName);
					 }
					 validateFirstTable(invalidParameterList,paraName,(TableSetModel)para.getValue());
				 }
				
				 validateColumnMap(invalidParameterList,paraName,(TableSetModel)para.getValue());
				 continue;
			 }
			 if(para.getValue() instanceof String){
			String paraValue = (String)para.getValue();
				if(paraName.equals(OperatorParameter.NAME_outputType)){
					validateNull(invalidParameterList, paraName,  (String)paraValue);
				}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
					validateNull(invalidParameterList, paraName, (String)paraValue);
					validateSchemaName(invalidParameterList, paraName,  (String)paraValue,variableModel);
				}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
					validateNull(invalidParameterList, paraName,  (String)paraValue );
					validateTableName(invalidParameterList, paraName,  (String)paraValue,variableModel);
				}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
					validateNull(invalidParameterList, paraName,  (String)paraValue );
				}
			 }
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}
	}
	

	
	private boolean fisrtTableExists(TableSetModel tableSetModel) {
		String firstTable = tableSetModel.getFirstTable();
		List<OperatorInputTableInfo> inputTables = getParentDBTableSet();
		if(inputTables!=null&&inputTables.size()>0){
			for(int i = 0 ; i<inputTables.size();i++){
				String schema= inputTables.get(i).getSchema();
				String table= inputTables.get(i).getTable();
				if((StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(table)).equals(firstTable)){
					return true;
				}
			}
		}
		return false;
	}



	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
	
	@Override
	public boolean isInputObjectsReady() {
		List<Object> inputObjectList = getParentOutputClassList();
		List<OperatorInputTableInfo> list = new ArrayList<OperatorInputTableInfo>();
		if (inputObjectList != null) {
			for (Object obj : inputObjectList) {
				if(obj instanceof OperatorInputTableInfo){
					list.add((OperatorInputTableInfo)obj);
				}
			}
			if(list.size()>1){
					return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public String validateInputLink(Operator parent) {
		
		if(parent instanceof SQLExecuteOperator){
			List<UIOperatorModel>  operatorList=OperatorUtility.getParentList(parent.getOperModel());
			if(operatorList==null||operatorList.size()==0){
				return LanguagePack.getMessage(LanguagePack.SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR,locale);
			}else{
				return this.validateInputLink(operatorList.get(0).getOperator());
			}	
		}else{
			List<String> sOutputList = parent.getOutputClassList();
			List<String> tInputList = this.getInputClassList();
			boolean isReady = false;
			if(sOutputList == null || tInputList == null){
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						parent.getToolTipTypeName(),this.getToolTipTypeName());
			}
			for(int i=0;i<sOutputList.size();i++){
				for(int j=0;j<tInputList.size();j++){
					if(sOutputList.get(i).equals(tInputList.get(j))){
						isReady = true;
					}
				}
			}
			if(!isReady){
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						parent.getToolTipTypeName(),this.getToolTipTypeName());
			}
			
			//pivotal 38901313
//			/**
//			 * check inputClass and outputClass is equals
//			 */
//			
//			List<Object> outPuts = parent.getOperatorOutputList();
//			if(outPuts==null||outPuts.size()==0){
//				return(parent.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
//			}
//		 
//			for (Iterator iterator = outPuts.iterator(); iterator.hasNext();) {
//				Object obj = (Object) iterator.next();
//				if (obj instanceof OperatorInputTableInfo) {
//					
//					OperatorInputTableInfo dbTableSet= (OperatorInputTableInfo) obj;
//					if(StringUtil.isEmpty(dbTableSet.getSchema())
//							||StringUtil.isEmpty(dbTableSet.getTable()) 
//							){
//						return(parent.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
//	 
//					} 
//				}
//			}
		 
			/**
			 * check repick linked
			 */
			if(parent.getOperModel().containTarget(getOperModel())){
				return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
			}
			
			return "";
		}
	
	}
	

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> tableSetNodes=opTypeXmlManager.getNodeList(opNode, TableSetModel.TAG_NAME);
		if(tableSetNodes!=null&&tableSetNodes.size()>0){
			Element tableSetElement=(Element)tableSetNodes.get(0);
			TableSetModel tableSetModel=TableSetModel.fromXMLElement(tableSetElement); 
			getOperatorParameter(OperatorParameter.NAME_tableSetConfig).setValue(tableSetModel);
		}		
	
		return operatorParameters;
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorOutputList= new ArrayList<Object>(); 
		List<Object> inputList = getOperatorInputList(); 
	 
		for(Object obj:inputList){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				OperatorInputTableInfo newOperatorInputTableInfo=new OperatorInputTableInfo();
				newOperatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				newOperatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				newOperatorInputTableInfo.setTableType((String)getOperatorParameter(OperatorParameter.NAME_outputType).getValue());
				newOperatorInputTableInfo.setConnectionName(operatorInputTableInfo.getConnectionName());
				newOperatorInputTableInfo.setPassword(operatorInputTableInfo.getPassword());
				newOperatorInputTableInfo.setSystem(operatorInputTableInfo.getSystem());
				newOperatorInputTableInfo.setUrl(operatorInputTableInfo.getUrl());
				newOperatorInputTableInfo.setUseSSL(operatorInputTableInfo.getUseSSL());
				newOperatorInputTableInfo.setUsername(operatorInputTableInfo.getUsername());
						
				List<String[]> newFieldColumns=new ArrayList<String[]>();
				TableSetModel tablesetModel=(TableSetModel)getOperatorParameter(OperatorParameter.NAME_tableSetConfig).getValue(); 
				if(tablesetModel!=null&&tablesetModel.getFirstTable()!=null){
					String tableName = tablesetModel.getFirstTable();
					//"demo"."acount"
					 String[] schemaTable = StringHandler.splitQuatedTableName(tableName);
					 tableName = schemaTable[1];
					 String schemaName = schemaTable[0];
				  	ColumnMap columnMap = tablesetModel.getColumnMap(null,schemaName,tableName);
				  	if(columnMap!=null&&columnMap.getTableColumns()!=null){
						List<String> columnNameList=columnMap.getTableColumns();
						OperatorInputTableInfo table = getInputTable( inputList,tableName);
						if(table!=null){//if is null means been deleteed.
							for(int i = 0 ;i<columnNameList.size();i++){
								String column = columnNameList.get(i);
								String outputColumn = tablesetModel.getColumnMapList().get(0).getTableColumns().get(i);
								String[] fieldColumn=new String[]{outputColumn,getColumnType(column,table)};
								newFieldColumns.add(fieldColumn);
							}
						}
					}
				}

				newOperatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorOutputList.add(newOperatorInputTableInfo);
				break;
			}
		}
		return operatorOutputList;
	}

	private String getColumnType(String column, OperatorInputTableInfo table) {
		List<String[]> columns = table.getFieldColumns();
		for(int i =0;i<columns.size();i++){
			if(columns.get(i)[0].equals(column)){
				return columns.get(i)[1] ;
			}
		}
		return null;
	}



	private OperatorInputTableInfo getInputTable(List<Object> inputList,String tableName) {
		for(Object obj:inputList){
			if(obj instanceof OperatorInputTableInfo){
				if(tableName.equals(((OperatorInputTableInfo)obj).getTable())){
					return (OperatorInputTableInfo)obj;
				}
			}
		}
		return null;
	}

	private void validateColumnMap(List<String> invalidParameterList, String paraName,TableSetModel tableSetModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		
		List<OperatorInputTableInfo> inputTableInfoList = OperatorUtility.getParentDBTableSet(this);
		
		List<String> newTableNameList=new ArrayList<String>();
		for(OperatorInputTableInfo tableInfo:inputTableInfoList){
			newTableNameList.add(StringHandler.combinTableName(tableInfo.getSchema(), tableInfo.getTable()));
		}

		List<ColumnMap> mapList = tableSetModel.getColumnMapList();
		List<ColumnMap> newcolumnMapList = new ArrayList<ColumnMap>(); 
		
		if(mapList!=null&&mapList.size()>0){
			if(inputTableInfoList!=null&&(inputTableInfoList.size()+1)!=mapList.size()){
				invalidParameterList.add(paraName);
				return;
			}else{
				List<String> oldTableNameList=new ArrayList<String>();
				for(int i=1;i<mapList.size();i++){
					String tableName = mapList.get(i).getTableName();
					String schemName = mapList.get(i).getSchemaName();
					oldTableNameList.add(StringHandler.combinTableName(schemName, tableName));
					OperatorInputTableInfo table = getInputTable(inputTableInfoList,schemName,tableName);
					if(table!=null){
						List<String> columns = mapList.get(i).getTableColumns();
						if(columns ==null||hasDirtyColumn(table,columns)==true){
							invalidParameterList.add(paraName);
							return;
						}
						else{
							newcolumnMapList.add(mapList.get(i));
						}
					} else if(!invalidParameterList.contains(paraName)){
						//the table do not exists.
						invalidParameterList.add(paraName);
					}
				}
				if(!invalidParameterList.contains(paraName)){
					for(String s:newTableNameList){
						if(!oldTableNameList.contains(s)){
							invalidParameterList.add(paraName);
							return;
						}
					}
				}
			}
            if(!invalidParameterList.contains(paraName)&&
                    ModelUtility.equalsWithOrder(mapList.subList(1,mapList.size()),  newcolumnMapList)==false){
                invalidParameterList.add(paraName);
            }
		} else {
            invalidParameterList.add(paraName);
        }

	} 

	private boolean hasDirtyColumn(OperatorInputTableInfo table, 
			List<String> columns) {
		if(columns!=null){
			for(int i = 0 ; i <columns.size();i++ ){
				if(hasColumnInTable(columns.get(i),table)==false){
					return true;
				}
			}
		}
		return false;
	}



	private boolean hasColumnInTable(String columnName, OperatorInputTableInfo table) { 
		List<String[]> columns = table.getFieldColumns();
		if(columns!=null){
			for(int i = 0 ; i <columns.size();i++ ){
				if( columns.get(i)[0].equals(columnName)==true){
					return true;
				}
			}

		}
		return false;
	}



	private OperatorInputTableInfo getInputTable( List<OperatorInputTableInfo> inputList,String  schema,String tableName) {
		if(inputList!=null){
			for (OperatorInputTableInfo obj : inputList) {
				if(tableName!=null&&schema!=null
						&&schema.equals(((OperatorInputTableInfo) obj).getSchema())
						&&tableName.equals(((OperatorInputTableInfo) obj).getTable())){
					return obj;
				}
			}
		}
		return null;
	}


	private void validateFirstTable(List<String> invalidParameterList, String paraName,TableSetModel tableSetModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		String firstTable = tableSetModel.getFirstTable();
		List<Object> inputList = this.getOperatorInputList();
		boolean found = false;
		for (Object inputInfo : inputList) {
			if (inputInfo instanceof OperatorInputTableInfo) {				
				String realTableName=StringHandler.doubleQ(((OperatorInputTableInfo) inputInfo).getSchema())
					+ "."+	StringHandler.doubleQ(((OperatorInputTableInfo) inputInfo).getTable());  
				if(firstTable!=null&&firstTable.equals(	realTableName)){
					found = true;
					break;
				}
			}
		}
		if(found==false){//should remove
			invalidParameterList.add(paraName);
		}
	}
}
