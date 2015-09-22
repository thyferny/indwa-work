/**
 * ClassName HadoopFowFilterAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopVariableConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Jeff Dong
 *
 */
public class HadoopVariableAnalyzer extends AbstractHadoopAttributeAnalyzer {
	
	@Override
	public String generateScript(HadoopDataOperationConfig config,String inputTempName) {
		AnalysisDerivedFieldsModel derivedModel=((HadoopVariableConfig)config).getDerivedModel();

		StringBuffer script=new StringBuffer();
		script.append( getOutputTempName()).append(" = FOREACH ").append(inputTempName);
		script.append(" GENERATE ");
		if(derivedModel!=null&&derivedModel.getDerivedFieldsList()!=null){
			List<AnalysisDerivedFieldItem> derivedFieldsList = derivedModel.getDerivedFieldsList();
			for(AnalysisDerivedFieldItem derivedField:derivedFieldsList){
				String dataType = derivedField.getDataType();
				String columnName = derivedField.getResultColumnName();
				String expression = derivedField.getSqlExpression();
				
				script.append("(").append(HadoopDataType.getTransferDataType(dataType)).append(") (").append(expression.trim()).append(") as ");
				script.append(columnName).append(",");
			}
		}
		if(derivedModel!=null&&derivedModel.getSelectedFieldList()!=null){
			List<String> selectedFieldList = derivedModel.getSelectedFieldList();
			for(String field:selectedFieldList){
				script.append(field).append(",");
			}
		}
		script=script.deleteCharAt(script.length()-1);
		script.append(";");
		return script.toString();
	}

	
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);

		HadoopVariableConfig newConfig = (HadoopVariableConfig)config;
		AnalysisDerivedFieldsModel derivedModel = newConfig.getDerivedModel();
		List<String> newColumnNameList = new ArrayList<String>();
		List<String> newColumnTypeList =new ArrayList<String>();
		
		if(derivedModel!=null&&derivedModel.getDerivedFieldsList()!=null){
			List<AnalysisDerivedFieldItem> derivedFields = derivedModel.getDerivedFieldsList();
			for(AnalysisDerivedFieldItem derivedField:derivedFields){
				newColumnNameList.add(derivedField.getResultColumnName());
				newColumnTypeList.add(derivedField.getDataType());
			}
		}
		if(derivedModel!=null&&derivedModel.getSelectedFieldList()!=null){
			List<String> selectedFields = derivedModel.getSelectedFieldList();
			for(String selectedField:selectedFields){
				newColumnNameList.add(selectedField);
				newColumnTypeList.add(getOldColumnType(oldModel, selectedField));
			}
		}
		
		newModel.setColumnNameList(newColumnNameList);
		newModel.setColumnTypeList(newColumnTypeList);

		return newModel; 
	}

	@Override
	public AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}
