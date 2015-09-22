/**
 * ClassName HadoopUnionOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-11
 *
 * COPYRIGHT (C) 2010,2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.*;

import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionFile;
import com.alpine.utility.file.StringUtil;
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
import com.alpine.miner.workflow.operator.parameter.ParameterValidateUtility;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModelItem;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionSourceColumn;
import com.alpine.utility.db.Resources;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;
/***
 * 
 * @author john zhao
 *
 */
public class HadoopUnionOperator extends HadoopDataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_Union_Model,

		//	OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
 
	});
	
	public HadoopUnionOperator() {
		super(parameterNames);
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TABLESET_OPERATOR,locale);
	}
	

	@Override
	public boolean isVaild(VariableModel variableModel) {
			List<String> invalidParameterList=new ArrayList<String>();
			List<OperatorParameter> paraList=getOperatorParameterList();
			HadoopUnionModel unionModel=null;
			for(OperatorParameter para:paraList){
				if(para.getValue() instanceof HadoopUnionModel){
					unionModel=(HadoopUnionModel)para.getValue();
					break;
				}
			}
			//For synchronize preceding operator's data type changing
			sysnHadoopUnionModel(unionModel);
			
			int mappedFileNumber=0;
			for(OperatorParameter para:paraList){
				String paraName=para.getName();
				String paraValue=null;
				if(para.getValue() instanceof String){
					paraValue=(String)para.getValue();
				}
				if(paraName.equals(OperatorParameter.NAME_HD_Union_Model)){
					if(unionModel!=null){
				 
						  List<HadoopUnionModelItem> unionItem = unionModel.getOutputColumns();
						if(unionItem==null||unionItem.isEmpty()){
							invalidParameterList.add(paraName);
						} else{
							mappedFileNumber = unionItem.get(0).getMappingColumns().size();
							//add ore remove the file in the flow
							if(mappedFileNumber!=getParentHadoopFileInputs().size()){
								invalidParameterList.add(paraName);
							}else{
                                validateUnionTables(unionModel, invalidParameterList, paraName);
								validateColumns(invalidParameterList,unionItem,paraName);
							}
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
	
	private void sysnHadoopUnionModel(HadoopUnionModel unionModel) {
		if (unionModel == null) {
			return;
		}
		List<HadoopUnionModelItem> outputColumns = unionModel.getOutputColumns();
		if(outputColumns!=null){
			Map<String, List<String[]>> allAvailableColumnsList = OperatorUtility
					.getAllAvailableColumnsAndTypeList(this, false);
			for(HadoopUnionModelItem item:outputColumns){
				List<HadoopUnionSourceColumn> mappingColumns = item.getMappingColumns();
				if(mappingColumns==null)continue;
				List<String> columnTypeList=new ArrayList<String>();
				for(HadoopUnionSourceColumn unionSourceColumn:mappingColumns){
					String opid = unionSourceColumn.getOperatorModelID();
					String columnName = unionSourceColumn.getColumnName();
					if(allAvailableColumnsList.containsKey(opid)){
						List<String[]> availableColumnsList = allAvailableColumnsList.get(opid);
						if(availableColumnsList!=null){
							for(String[] ss:availableColumnsList){
								if(ss.length==2&&ss[0].equals(columnName)){
									columnTypeList.add(ss[1]);
									break;
								}
							}
						}
					}
				}
				String newColumnType = HadoopDataType.findMaxHadoopDataType(columnTypeList);
				if(newColumnType!=null){
					item.setColumnType(newColumnType);
				}
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
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());
				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				HadoopUnionModel unionModel = (HadoopUnionModel)getOperatorParameter(OperatorParameter.NAME_HD_Union_Model).getValue();
				
				List<String> newColumnNameList = new ArrayList<String>();
				List<String> newColumnTypeList = new ArrayList<String>();
				
				if(unionModel!=null&&unionModel.getOutputColumns()!=null){ 
					  List<HadoopUnionModelItem> unionItems = unionModel.getOutputColumns();
					for(HadoopUnionModelItem joinColumn:unionItems){
						newColumnNameList.add(joinColumn.getColumnName());
						newColumnTypeList.add(joinColumn.getColumnType());
					}
				}
                if (columnInfo != null)
                {
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
//			if(outPuts==null||outPuts.size()==0){
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
		
		ArrayList<Node> hadoopJoinNodeList=opTypeXmlManager.getNodeList(opNode, HadoopUnionModel.TAG_NAME);
		if(hadoopJoinNodeList!=null&&hadoopJoinNodeList.size()>0){
			Element tableJoinElement=(Element)hadoopJoinNodeList.get(0);
			HadoopUnionModel hadoopUnionModel=HadoopUnionModel.fromXMLElement(tableJoinElement);
			getOperatorParameter(OperatorParameter.NAME_HD_Union_Model).setValue(hadoopUnionModel);
		}
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		OperatorParameter tableJoinParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_HD_Union_Model);

		 
		if(tableJoinParameter!=null&&tableJoinParameter.getValue()!=null){ 
			HadoopUnionModel model=(HadoopUnionModel)tableJoinParameter.getValue();
			element.appendChild(model.toXMLElement(xmlDoc));
	}
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

    private void validateUnionTables(HadoopUnionModel unionModel, List<String> invalidParameterList, String paraName ) {
        if(invalidParameterList.contains(paraName)){
            return;
        }

        List<HadoopUnionFile> unionFiles = unionModel.getUnionFiles();
        List<UIOperatorModel> parents = OperatorUtility.getParentList(getOperModel());
        Map<String,UIOperatorModel> parentsMap = new HashMap<String,UIOperatorModel>();
        for(UIOperatorModel model:parents){
            parentsMap.put(model.getUUID(), model);
        }
        List<HadoopUnionFile> removedUnionFiles = new ArrayList<HadoopUnionFile>();
        for(HadoopUnionFile unionFile:unionFiles) {
            String parentFileName = unionFile.getFile();
            if(!StringUtil.isEmpty(parentFileName)) {
                if (!parentsMap.containsKey(unionFile.getOperatorModelID())) {
                    removedUnionFiles.add(unionFile);
                }
            }
        }
        if (removedUnionFiles.size()>0) {
            invalidParameterList.add(paraName);
        }
    }

	private void validateColumns(List<String> invalidParameterList,List<HadoopUnionModelItem> outputColumns,String paramName) {
        if(invalidParameterList.contains(paramName)){
            return;
        }

        if(outputColumns!=null){
			for(HadoopUnionModelItem item: outputColumns){
				List<HadoopUnionSourceColumn> columns = item.getMappingColumns();
				List<String>  inputColumnTypeList = new ArrayList<String>();
				for (HadoopUnionSourceColumn column : columns) {
					String uuid = column.getOperatorModelID();
					OperatorInputFileInfo fileInfo = getInputFileInfo(uuid);
					if(fileInfo!=null&&false == hasColumnNameinFile(column.getColumnName(),fileInfo)){
						invalidParameterList.add(paramName)	 ;//not existed any more
						return ;
					}else{
						String inputColumnType = ParameterValidateUtility.getInputColumnType(column.getColumnName(), fileInfo);
						inputColumnTypeList.add(inputColumnType ) ;
					}


				}
				if(false == HadoopDataType.isSimilarType4List(inputColumnTypeList )){
					invalidParameterList.add(paramName)	 ;//type has been changed
					return ;

				}

			}

		}
		
	}
	  
	public boolean hasColumnNameinFile(String columnName,
			OperatorInputFileInfo fileInfo) {
		FileStructureModel columnInfo = fileInfo.getColumnInfo();
		if(columnInfo!=null&&columnInfo.getColumnNameList()!=null){
			return columnInfo.getColumnNameList().contains(columnName);
		}
		return false;
	}
	
	

}