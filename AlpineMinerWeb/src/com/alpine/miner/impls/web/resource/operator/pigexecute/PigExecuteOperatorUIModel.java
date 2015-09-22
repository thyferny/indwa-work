/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * PigExecuteOperatorUIModel.java
 */
package com.alpine.miner.impls.web.resource.operator.pigexecute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.pigexe.PigInputMapItem;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.parameter.pigexe.PigExecutableModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;

/**
 * @author Gary
 * Oct 30, 2012
 */
public class PigExecuteOperatorUIModel {

	private String pigScript = "";
	private List<InputFile> inputFileList = new ArrayList<InputFile>();

	
	public PigExecuteOperatorUIModel(PigExecutableModel scriptModel, Operator op){
		List<Object> inputList = op.getOperatorInputList();
		if(scriptModel != null){
			for(PigInputMapItem mapItem : scriptModel.getPigInputMapItems()){
				for(Object input : inputList){
		    		OperatorInputFileInfo inputFile = (OperatorInputFileInfo) input;
					if(rebuildParentUUID(op, inputFile.getOperatorUUID()).equals(mapItem.getInputUUID())){
						inputFileList.add(convertMapItem(mapItem));
					}
				}
			}
			pigScript = scriptModel.getPigScript();
		}
    	inputMark:
    	for(Object input : inputList){
    		OperatorInputFileInfo inputFile = (OperatorInputFileInfo) input;
    		for(InputFile item : inputFileList){
    			if(item.getOperatorUUID().equals(rebuildParentUUID(op, inputFile.getOperatorUUID()))){
    				item.fileName = inputFile.getHadoopFileName();
    				item.columnNames = inputFile.getColumnInfo().getColumnNameList();
    				continue inputMark;
    			}
    		}
    		//create new alias
    		String newAlias = null;
    		outerMark:
    		for(int i = 1;i <= inputList.size();i++){
    			newAlias = "alpine_pig_input_" + i;
    			//validate newAlias whether already occupied
    			for(InputFile item : inputFileList){
        			if(item.getAlias().equals(newAlias)){
        				continue outerMark;
        			}
        		}
    			break;
    		}
    		InputFile file = new InputFile();
    		file.operatorUUID = rebuildParentUUID(op, inputFile.getOperatorUUID());
    		file.alias = newAlias;
    		if(inputFile.getColumnInfo() != null){
        		file.columnNames = inputFile.getColumnInfo().getColumnNameList();
    		}else{
    			file.columnNames = Collections.EMPTY_LIST;
    		}
    		file.fileName = inputFile.getHadoopFileName();
    		inputFileList.add(file);
    	}
	}
	
	/**
	 * use subflow operator's uuid instead its exit operator uuid, otherwise return inputOperatorUUID
	 * @param operator
	 * @param inputOperatorUUID
	 * @return
	 */
	private String rebuildParentUUID(Operator operator, String inputOperatorUUID){
		for(Operator parent : operator.getParentOperators()){
			if(parent instanceof SubFlowOperator){
				SubFlowOperator subFlowParent = (SubFlowOperator) parent;
				if(inputOperatorUUID.equals(subFlowParent.getExitOperator().getOperModel().getUUID())){
					return subFlowParent.getOperModel().getUUID();
				}
			}
		}
		return inputOperatorUUID;
	}
	
	public PigExecutableModel revertModel(){
		PigExecutableModel model = new PigExecutableModel();
		model.setPigScript(this.getPigScript());
		model.setPigInputMapItems(new ArrayList<PigInputMapItem>());
		for(InputFile inputFile : inputFileList){
			model.getPigInputMapItems().add(revertMapItem(inputFile));
		}
		return model;
	}
	
	private InputFile convertMapItem(PigInputMapItem model){
		InputFile file = new InputFile();
		file.operatorUUID = model.getInputUUID();
		file.alias = model.getPigAliasName();
		return file;
	}
	
	private PigInputMapItem revertMapItem(InputFile file){
		PigInputMapItem mapItem = new PigInputMapItem();
		mapItem.setInputUUID(file.getOperatorUUID());
		mapItem.setPigAliasName(file.getAlias());
		return mapItem;
	}

	public String getPigScript() {
		return pigScript;
	}


	public void setPigScript(String pigScript) {
		this.pigScript = pigScript;
	}


	public List<InputFile> getInputFileList() {
		return inputFileList;
	}


	public void setInputFileList(List<InputFile> inputFileList) {
		this.inputFileList = inputFileList;
	}
	
	
	public static class InputFile{
		private String 	operatorUUID,
						alias,
						fileName;
		private List<String> columnNames;
		public String getOperatorUUID() {
			return operatorUUID;
		}
		public void setOperatorUUID(String operatorUUID) {
			this.operatorUUID = operatorUUID;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public List<String> getColumnNames() {
			return columnNames;
		}
		public void setColumnNames(List<String> columnNames) {
			this.columnNames = columnNames;
		}
	}
}