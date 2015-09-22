/**
 * ClassName TableJoinOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.db.tablejoin.TableJoinAnalyzer;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinColumn;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinCondition;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class TableJoinOperator extends DataOperationOperator {
    private static final Logger itsLogger=Logger.getLogger(TableJoinOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_createSequenceID,
			OperatorParameter.NAME_Set_Table_Join_Parameters,
			
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
	});
		
	public TableJoinOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TABLEJOIN_OPERATOR,locale);
	}

 

	@Override
	public boolean isVaild(VariableModel variableModel) {	
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		TableJoinModel tjModel=null;
		for(OperatorParameter para:paraList){
			if(para.getValue() instanceof TableJoinModel){
				tjModel=(TableJoinModel)para.getValue();
				break;
			}
		}
		Map<String, List<String>> availableColumnsListMap = OperatorUtility.getAllAvailableColumnsList(this,false);
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_createSequenceID)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Set_Table_Join_Parameters)){
				if(tjModel!=null){
					List<Object> inputList = getOperatorInputList();
					List<JoinCondition>  joinConditions=tjModel.getJoinConditions();
					int i=0;
					for(JoinCondition joinCondition:joinConditions){
						if(i==0&&StringUtil.isEmpty(joinCondition.getTableAlias1())
								&&!invalidParameterList.contains(paraName)){
							invalidParameterList.add(paraName);
						}
						if(!StringUtil.isEmpty(joinCondition.getJoinType())){
							i++;
						}
					}
					if((i<inputList.size()-1||i==0)&&
							!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}else{
					invalidParameterList.add(paraName);
				}
				if(!invalidParameterList.contains(paraName)&&!isFromSameDatabase()){
					invalidParameterList.add(paraName);
				}
				validateJoinTables(tjModel,invalidParameterList, paraName);
				validateJoinColumns(tjModel,invalidParameterList, paraName,availableColumnsListMap);
				validateJoinCondition(tjModel,invalidParameterList, paraName);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}		
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}	
	}
	
	private boolean isFromSameDatabase(){
		List<String> tableURLList=new ArrayList<String>();
		List<OperatorInputTableInfo> parentDBTableSet = OperatorUtility.getParentDBTableSet(this);
		if(parentDBTableSet==null)return false;
		for(OperatorInputTableInfo tableInfo:parentDBTableSet){
			if(tableInfo!=null){
				String URL=tableInfo.getUrl();
				if(!tableURLList.contains(URL)){
					tableURLList.add(URL);
				}	
			}
		}
		if(tableURLList.size()>1){
			return false;
		}else{
			return true;
		}
	}
	
	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList= new ArrayList<Object>();
		for(Object obj:getOperatorInputList()){
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
				TableJoinModel joinModel=(TableJoinModel)getOperatorParameter(OperatorParameter.NAME_Set_Table_Join_Parameters).getValue();
				if(joinModel!=null){//could be null MINER-1954
					List<JoinColumn> joinColumnList=joinModel.getJoinColumns();
					for(JoinColumn column:joinColumnList){
						if(!StringUtil.isEmpty(column.getColumnType())){
							String[] fieldColumn=new String[]{column.getNewColumnName(),column.getColumnType()};
							newFieldColumns.add(fieldColumn);
						}			
					}
				}
				String  createSequanceID=(String)getOperatorParameter(OperatorParameter.NAME_createSequenceID).getValue();
				if(createSequanceID.equalsIgnoreCase("Yes")){
					String[] fieldColumn=new String[]{TableJoinAnalyzer.Alpine_ID,ParameterUtility.getIdType(operatorInputTableInfo.getSystem())};
					newFieldColumns.add(fieldColumn);
				}
				newOperatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(newOperatorInputTableInfo);
				break;
			}
		}
		return operatorInputList;
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> tableJoinNodeList=opTypeXmlManager.getNodeList(opNode, TableJoinModel.TAG_NAME);
		if(tableJoinNodeList!=null&&tableJoinNodeList.size()>0){
			Element tableJoinElement=(Element)tableJoinNodeList.get(0);
			TableJoinModel tableJoinModel=TableJoinModel.fromXMLElement(tableJoinElement);
			getOperatorParameter(OperatorParameter.NAME_Set_Table_Join_Parameters).setValue(tableJoinModel);
		}		
	
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		OperatorParameter tableJoinParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_Set_Table_Join_Parameters);

		setJoinModel(xmlDoc, element, tableJoinParameter,addSuffixToOutput);
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
//				if(isDBSame(list)==true){
					return true;
//				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
		
	}
	
	private void validateJoinCondition(TableJoinModel joinModel,List<String>  invalidParameterList, String paraName) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		
		List<JoinCondition> joinConditions = joinModel.getJoinConditions();
		if(joinConditions.size()==0){
			return;
		}
		//The main thinking is split all conditions into many groups.
		//The conditions in one group has same right join table.
		//If the right join table disappear,delete all conditions in this group.
		List<JoinTable> joinTables = joinModel.getJoinTables();
		List<String> aliasList=new ArrayList<String>();
		for(JoinTable joinTable:joinTables){
			aliasList.add(joinTable.getAlias());
		}
		List<JoinCondition> removeList=new ArrayList<JoinCondition>();

		Map<Integer,String> indexMap=new LinkedHashMap<Integer,String>();
		
		//Find each group's start index.
		for(int i=0;i<joinConditions.size();i++){
			if(!StringUtil.isEmpty(joinConditions.get(i).getTableAlias2())){
				indexMap.put(i, joinConditions.get(i).getTableAlias2());
			}
		}
		
		//Traverse all condition,put each condition in it's group.
		Map<List<Integer>,String> rangeMap=new LinkedHashMap<List<Integer>,String>();
		int lastIndex=0;
		String lastAlias="";
		Iterator<Entry<Integer, String>> iter = indexMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Integer, String> entry = iter.next();
			String alias = entry.getValue();
			Integer index = entry.getKey();
			if(index==0){
				lastIndex=index;
				lastAlias=alias;
				continue;
			}	
			List<Integer> rangeList=new ArrayList<Integer>();
			for(int i=lastIndex;i<index;i++){
				rangeList.add(i);
			}
			rangeMap.put(rangeList,lastAlias);
			lastIndex=index;
			lastAlias=alias;
		}

		List<Integer> rangeList=new ArrayList<Integer>();
		for(int i=lastIndex;i<joinConditions.size();i++){
			rangeList.add(i);
		}
		rangeMap.put(rangeList,lastAlias);
		
		
		//Find which condition group need to be deleted
		boolean isFirst=true;
		Iterator<Entry<List<Integer>, String>> iterNew = rangeMap.entrySet().iterator();
		while(iterNew.hasNext()){
			Entry<List<Integer>, String> entry = iterNew.next();
			if(isFirst){//Judge the left table.
				if(!aliasList.contains(joinConditions.get(0).getTableAlias1())){
					rangeList=entry.getKey();
					for(Integer i:rangeList){
						removeList.add(joinConditions.get(i.intValue()));
					}				
				}
				isFirst=false;
			}
			if(!aliasList.contains(entry.getValue())){
				rangeList=entry.getKey();
				for(Integer i:rangeList){
					if(!removeList.contains(joinConditions.get(i.intValue()))){
						removeList.add(joinConditions.get(i.intValue()));
					}				
				}
				
			}
		}
		
		if(removeList.size()>0){
			invalidParameterList.add(paraName);
		}		
	}

	private void validateJoinColumns(TableJoinModel joinModel,List<String> invalidParameterList,String paraName, 
			Map<String, List<String>> availableColumnsListMap) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		
		List<JoinColumn> joinColumns = joinModel.getJoinColumns();
		List<JoinTable> joinTables = joinModel.getJoinTables();
		List<String> aliasList=new ArrayList<String>();
		for(JoinTable joinTable:joinTables){
			aliasList.add(joinTable.getAlias());
		}
		List<JoinColumn> removeList=new ArrayList<JoinColumn>();
		for(JoinColumn joinColumn:joinColumns){
			if(!aliasList.contains(joinColumn.getTableAlias())){
				removeList.add(joinColumn);
				break;
			}else{
				JoinTable joinTable=findJoinTable(joinTables,joinColumn.getTableAlias());
				if(joinTable!=null){
					String tableName = StringHandler.combinTableName(joinTable.getSchema(), joinTable.getTable());
					List<String> availableColumnsList=availableColumnsListMap.get(tableName);
					if(availableColumnsList!=null&&!availableColumnsList.contains(joinColumn.getColumnName())){
						removeList.add(joinColumn);
						break;
					}
				}
			}
		}
		if(removeList.size()>0){
			invalidParameterList.add(paraName);
		}
	}

	private JoinTable findJoinTable(List<JoinTable> joinTables,String alias){
		for(JoinTable joinTable:joinTables){
			if(joinTable.getAlias().equals(alias)){
				return joinTable;
			}
		}
		return null;
	}
	private void validateJoinTables(TableJoinModel joinModel,List<String> invalidParameterList,String paraName) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		List<JoinTable> joinTables = joinModel.getJoinTables();
		List<UIOperatorModel> parents = OperatorUtility.getParentList(getOperModel());
		Map<String,UIOperatorModel> parentsMap=new HashMap<String,UIOperatorModel>();
		for(UIOperatorModel model:parents){
			parentsMap.put(model.getUUID(), model);
		}
		List<JoinTable> removeJoinTables=new ArrayList<JoinTable>();
		for(JoinTable joinTable:joinTables){
			String schema=joinTable.getSchema();
			String table=joinTable.getTable();
			boolean isContain=false;
			if(parentsMap.containsKey(joinTable.getOperatorModelID())){
				UIOperatorModel parent = parentsMap.get(joinTable.getOperatorModelID());
				String parentTableName = OperatorUtility.getTableName(parent, false);
				String parentSchemaName = OperatorUtility.getSchemaName(parent);
				if(schema.equals(parentSchemaName)
						&&table.equals(parentTableName)){
					isContain=true;
				}
			}
			if(!isContain){
				removeJoinTables.add(joinTable);
			}
		}
		if(removeJoinTables.size()>0){
			invalidParameterList.add(paraName);
		}

	}

	@Override
	public String validateInputLink(Operator precedingOperator) {
		
		if(precedingOperator instanceof SQLExecuteOperator){
			List<UIOperatorModel>  operatorList=OperatorUtility.getParentList(precedingOperator.getOperModel());
			if(operatorList==null||operatorList.size()==0){
				return LanguagePack.getMessage(LanguagePack.SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR,locale);
			}else{
				return this.validateInputLink(operatorList.get(0).getOperator());
			}	
		}else{
			List<String> sOutputList = precedingOperator.getOutputClassList();
			List<String> tInputList = this.getInputClassList();
			boolean isReady = false;
			if(sOutputList == null || tInputList == null){
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
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
						precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
			}
			
			//pivotal 38901313
//			/**
//			 * check inputClass and outputClass is equals
//			 */
//			
//			List<Object> outPuts = precedingOperator.getOperatorOutputList();
//			if(outPuts==null||outPuts.size()==0){
//				return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
//			}
//		 
//			for (Iterator iterator = outPuts.iterator(); iterator.hasNext();) {
//				Object obj = (Object) iterator.next();
//				if (obj instanceof OperatorInputTableInfo) {
//					
//					OperatorInputTableInfo dbTableSet= (OperatorInputTableInfo) obj;
//					if(dbTableSet.getSchema()==null
//							||dbTableSet.getSchema().trim().length()==0
//							||dbTableSet.getTable()==null
//							||dbTableSet.getTable().trim().length()==0
//							){
//						return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
//	 
//					} 
//				}
//			}
		 	
	 	
			/**
			 * check repick linked
			 */
			if(precedingOperator.getOperModel().containTarget(getOperModel())){
				return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
			}
			
			
			return "";
		}
		
	
	}
	
	private void setJoinModel(Document xmlDoc, Element element, OperatorParameter tableJoinParameter,boolean addSuffixToOutput) {
		if(tableJoinParameter!=null&&tableJoinParameter.getValue()!=null){ 
			if(addSuffixToOutput){
				TableJoinModel tjd=(TableJoinModel)tableJoinParameter.getValue();
				TableJoinModel tjdNew =null;
				try {
					  tjdNew = (TableJoinModel)tjd.clone();
				} catch (CloneNotSupportedException e) {
					itsLogger.error(e.getMessage(),e);
				}
				if(tjd!=null&&tjdNew!=null){
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
						}else if(opModel.getOperator() instanceof IntegerToTextOperator){
							String modifyOriginTable = (String)opModel.getOperator().getOperatorParameter
								(OperatorParameter.NAME_modifyOriginTable).getValue();
							if(!StringUtil.isEmpty(modifyOriginTable)&&
									modifyOriginTable.equals(Resources.TrueOpt)){
								List<UIOperatorModel> n2tParents = OperatorUtility.getParentList(opModel);
								if(n2tParents!=null&&n2tParents.size()>0){
									UIOperatorModel n2tParent = n2tParents.get(0);
									operatorParameterList=n2tParent.getOperator().getOperatorParameterList();
								}
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
					
					List<JoinTable> jtModelList=tjdNew.getJoinTables();
					Iterator<JoinTable> iter=jtModelList.iterator();
					while(iter.hasNext()){
						JoinTable jtModel=iter.next();
						if(isParentDbTableMap.get(jtModel.getTable())!=null&&!isParentDbTableMap.get(jtModel.getTable())){
							jtModel.setTable(StringHandler.addPrefix(jtModel.getTable(), this.userName));
						}	
					}
				}
				element.appendChild(tjdNew.toXMLElement(xmlDoc));
			}else{
				element.appendChild(((TableJoinModel)tableJoinParameter.getValue()).toXMLElement(xmlDoc));
			}	
		}else {
			TableJoinModel model=new TableJoinModel();
			List<Operator> parents = getParentOperatorList();
			int count=1;
			List<String> newAliasNameList=new ArrayList<String>();
			for(Operator parent:parents){
				String schemaName = OperatorUtility.getSchemaName(parent.getOperModel());
				String tableName= OperatorUtility.getTableName(parent.getOperModel(), addSuffixToOutput);
				String alias = generateAlias(newAliasNameList,count,schemaName,tableName);
				JoinTable joinTable = new JoinTable(schemaName,tableName,alias,parent.getOperModel().getUUID());
				model.getJoinTables().add(joinTable);
				count++;
			}
			tableJoinParameter.setValue(model);
			element.appendChild(model.toXMLElement(xmlDoc));
		}
	}
	
	private String generateAlias(List<String> newAliasNameList, int i,String schemaName,String tableName) {
		String alias=schemaName+"_"+tableName+"_a";
		if(i>1){
			alias=alias+i;
		}
		if(newAliasNameList.contains(alias)){
			return generateAlias(newAliasNameList,i+1,schemaName,tableName);
		}else{
			newAliasNameList.add(alias);
			return alias;
		}	
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_createSequenceID)){
			return Resources.NoOpt;
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}
		
	}
	
	
}
