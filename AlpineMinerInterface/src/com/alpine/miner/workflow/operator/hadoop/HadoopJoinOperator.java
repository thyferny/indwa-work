/**
 * ClassName  HadoopJoinOperator.java
 *
 * Version information: 1.00
 *
 * Data: Jun 28, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinColumn;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinCondition;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinFile;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;
/**
 * 
 * @author Jeff Dong
 *
 */
public class HadoopJoinOperator extends HadoopDataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{

			OperatorParameter.NAME_HD_JOIN_MODEL,

			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});
	
	public HadoopJoinOperator() {
		super(parameterNames);
	}

	

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TABLEJOIN_OPERATOR,locale);
	}

	

	@Override
	public boolean isVaild(VariableModel variableModel) {
			List<String> invalidParameterList=new ArrayList<String>();
			List<OperatorParameter> paraList=getOperatorParameterList();
			HadoopJoinModel joinModel=null;
			for(OperatorParameter para:paraList){
				if(para.getValue() instanceof HadoopJoinModel){
					joinModel=(HadoopJoinModel)para.getValue();
					break;
				}
			}
			
			//For synchronize preceding operator's data type changing
			sysnHadoopJoinModel(joinModel);
			
			for(OperatorParameter para:paraList){
				String paraName=para.getName();
				String paraValue=null;
				if(para.getValue() instanceof String){
					paraValue=(String)para.getValue();
				}
				if(paraName.equals(OperatorParameter.NAME_HD_JOIN_MODEL)){
					if(joinModel!=null){
						validateJoinTables(joinModel,invalidParameterList,paraName);
						validateJoinColumns(joinModel,invalidParameterList, paraName);
						validateJoinCondition(joinModel,invalidParameterList, paraName);
						List<HadoopJoinCondition> joinConditions = joinModel.getJoinConditions();
						List<HadoopJoinFile> joinTables = joinModel.getJoinTables();
						List<UIOperatorModel> parents = OperatorUtility.getParentList(getOperModel());
						if(joinTables==null||joinConditions==null){
							invalidParameterList.add(paraName);
						}else if(joinTables.size()!=joinConditions.size()){
							invalidParameterList.add(paraName);
						}else if(parents!=null&&parents.size()!=joinTables.size()){
							invalidParameterList.add(paraName);
						}
						
						//check preceding operators are coming from same connection
						if(invalidParameterList.contains(paraName)==false){
							if(OperatorUtility.isComingFromSameHadoopConnetion(this)==false){
								invalidParameterList.add(paraName);
							}
						}
					}else{
						invalidParameterList.add(paraName);
					}
				}else{
					validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
				}		
			}
			invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
			if(invalidParameterList.size()==0){
				return true;
			}else{
				return false;
			}	
		}

	private void validateJoinColumns(HadoopJoinModel joinModel,
			List<String> invalidParameterList, String paraName) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		Map<String, List<String>> availableColumnsListMap = OperatorUtility.getAllAvailableColumnsList(this,false);
		List<HadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
		if(joinColumns!=null){
			for(HadoopJoinColumn joinColumn:joinColumns){
				boolean isContain=false;
				List<String> availableColumns=availableColumnsListMap.get(joinColumn.getFileId());
				if(availableColumns!=null&&availableColumns.contains(joinColumn.getColumnName())){
					isContain=true;
				}
				if(isContain==false){
					invalidParameterList.add(paraName);
					break;
				}
			}
			
			if(invalidParameterList.contains(paraName)){
				return;
			}
		}
	}



	private void sysnHadoopJoinModel(HadoopJoinModel joinModel) {
		if (joinModel == null) {
			return;
		}
		List<HadoopJoinFile> JoinTables = joinModel.getJoinTables();
		List<HadoopJoinColumn> JoinColumns = joinModel.getJoinColumns();
		if (JoinTables != null && JoinColumns != null) {
			Map<String, List<String[]>> allAvailableColumnsList = OperatorUtility
					.getAllAvailableColumnsAndTypeList(this, false);
			for(HadoopJoinFile joinFile:JoinTables){
				String operId = joinFile.getOperatorModelID();
				if(allAvailableColumnsList.containsKey(operId)){
					List<String[]> availableColumnsList = allAvailableColumnsList.get(operId);
					for(HadoopJoinColumn joinColumn:JoinColumns){
						if(joinColumn.getFileId().equals(operId)){
							String columnName = joinColumn.getColumnName();
							if(availableColumnsList!=null){
								for(String[] ss:availableColumnsList){
									if(ss.length==2&&ss[0].equals(columnName)){
										joinColumn.setColumnType(ss[1]);
										break;
									}
								}
							}
						}//end if
					}
				}//end if
			}
		}
	}



	private void validateJoinCondition(HadoopJoinModel joinModel,
			List<String> invalidParameterList, String paraName) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		Map<String, List<String>> availableColumnsListMap = OperatorUtility.getAllAvailableColumnsList(this,false);
		List<HadoopJoinCondition> joinConditions = joinModel.getJoinConditions();
		if(joinConditions!=null){
			for(HadoopJoinCondition joinCondition:joinConditions){
				boolean isContain=false;
				List<String> joinColumns=availableColumnsListMap.get(joinCondition.getFileId());
				if(joinColumns!=null&&joinColumns.contains(joinCondition.getKeyColumn())){
					isContain=true;
				}
				if(isContain==false){
					invalidParameterList.add(paraName);
					break;
				}
			}
			
			if(invalidParameterList.contains(paraName)){
				return;
			}
			List<String> conditionDataTypeList = new ArrayList<String>();
			for(HadoopJoinCondition joinCondition:joinConditions){
				OperatorInputFileInfo inputFileInfo = getInputFileInfo(joinCondition.getFileId());
				if(inputFileInfo!=null){
					List<String> columnNameList = inputFileInfo.getColumnInfo().getColumnNameList();
					List<String> columnTypeList = inputFileInfo.getColumnInfo().getColumnTypeList();
					for(int i=0;i<columnNameList.size();i++){
						if(columnNameList.get(i).equals(joinCondition.getKeyColumn())){
							conditionDataTypeList.add(columnTypeList.get(i));
							break;
						}
					}
				}
			}
			if(HadoopDataType.isSimilarType4List(conditionDataTypeList)==false){
				invalidParameterList.add(paraName);
			}
		}
	}



	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputFileInfo) {
				OperatorInputFileInfo operatorInputFileInfo=(OperatorInputFileInfo)obj;
				operatorInputFileInfo=operatorInputFileInfo.clone();
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());
				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				HadoopJoinModel joinModel = (HadoopJoinModel)getOperatorParameter(OperatorParameter.NAME_HD_JOIN_MODEL).getValue();
				
				List<String> newColumnNameList = new ArrayList<String>();
				List<String> newColumnTypeList = new ArrayList<String>();
				
				if(joinModel!=null&&joinModel.getJoinColumns()!=null){
					List<HadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
					for(HadoopJoinColumn joinColumn:joinColumns){
						newColumnNameList.add(joinColumn.getNewColumnName());
						newColumnTypeList.add(joinColumn.getColumnType());
					}
				}
                if (columnInfo != null){
                    columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
                    columnInfo.setColumnNameList(newColumnNameList);
                    columnInfo.setColumnTypeList(newColumnTypeList);
                }
				operatorInputFileInfo.setColumnInfo(columnInfo);
				operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
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
//			if(precedingOperator instanceof SubFlowOperator==false&& outPuts==null||outPuts.size()==0){
//				return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
//			}	 
//			for (Iterator<Object> iterator = outPuts.iterator(); iterator.hasNext();) {
//				Object obj = iterator.next();
//				if (obj instanceof OperatorInputFileInfo) {			
//					OperatorInputFileInfo dbTableSet= (OperatorInputFileInfo) obj;
//					if(StringUtil.isEmpty(dbTableSet.getHadoopFileName())){
//						return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
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
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> hadoopJoinNodeList=opTypeXmlManager.getNodeList(opNode, HadoopJoinModel.TAG_NAME);
		if(hadoopJoinNodeList!=null&&hadoopJoinNodeList.size()>0){
			Element tableJoinElement=(Element)hadoopJoinNodeList.get(0);
			HadoopJoinModel hadoopJoinModel=HadoopJoinModel.fromXMLElement(tableJoinElement);
			getOperatorParameter(OperatorParameter.NAME_HD_JOIN_MODEL).setValue(hadoopJoinModel);
		}
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		OperatorParameter tableJoinParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_HD_JOIN_MODEL);

		setJoinModel(xmlDoc, element, tableJoinParameter,addSuffixToOutput);

	}
	
	@Override
	public boolean isInputObjectsReady() {
		List<Object> inputObjectList = getParentOutputClassList();
		List<OperatorInputFileInfo> list = new ArrayList<OperatorInputFileInfo>();
		if (inputObjectList != null) {
			for (Object obj : inputObjectList) {
				if(obj instanceof OperatorInputFileInfo){
					list.add((OperatorInputFileInfo)obj);
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
	
	private void validateJoinTables(HadoopJoinModel joinModel,List<String> invalidParameterList,String paraName) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		List<HadoopJoinFile> joinTables = joinModel.getJoinTables();
		List<UIOperatorModel> parents = OperatorUtility.getParentList(getOperModel());
		Map<String,UIOperatorModel> parentsMap=new HashMap<String,UIOperatorModel>();
		for(UIOperatorModel model:parents){
			parentsMap.put(model.getUUID(), model);
		}
		List<HadoopJoinFile> removeJoinTables=new ArrayList<HadoopJoinFile>();
		for(HadoopJoinFile joinTable:joinTables){
			String parentFileName=joinTable.getFile();
			if(StringUtil.isEmpty(parentFileName)==false){
			boolean isContain=false;
			if(parentsMap.containsKey(joinTable.getOperatorModelID())){
					isContain=true;
			}
			if(false==isContain){
				removeJoinTables.add(joinTable);
			}
		}
		}
		if(removeJoinTables.size()>0){
			invalidParameterList.add(paraName);
		}

	}

 
	private void setJoinModel(Document xmlDoc, Element element, OperatorParameter tableJoinParameter,boolean addSuffixToOutput) {
			if(tableJoinParameter!=null&&tableJoinParameter.getValue()!=null){ 
				HadoopJoinModel model=(HadoopJoinModel)tableJoinParameter.getValue();
				element.appendChild(model.toXMLElement(xmlDoc));
		}
	}
}