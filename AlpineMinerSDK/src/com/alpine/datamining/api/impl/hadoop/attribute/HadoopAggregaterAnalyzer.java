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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopAggregaterConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;

public class HadoopAggregaterAnalyzer extends AbstractHadoopAttributeAnalyzer {
	private static Logger itsLogger = Logger.getLogger(HadoopAggregaterAnalyzer.class);
	@Override
	public String generateScript(HadoopDataOperationConfig config,String inputFileName) {
		if(null==config|| !(config instanceof HadoopAggregaterConfig )){
			String errString="Config["+config+"]is either null or not HadoopAggregaterConfig type";
			itsLogger.error(errString);
			throw new IllegalArgumentException(errString);
		}
		HadoopAggregaterConfig newConfig = (HadoopAggregaterConfig)config;
		AnalysisAggregateFieldsModel aggreagteFiels = newConfig.getAggregateFieldsModel();
		StringBuilder groupSB = new StringBuilder();
		StringBuilder aggregateSB  = new StringBuilder();
		
		List<String> groupByColumns = aggreagteFiels.getGroupByFieldList();
		String groupName = "group_" + System.currentTimeMillis();
		groupSB.append(groupName + "  =  group " + inputFileName + " by (");
		List<AnalysisAggregateField> aggregateFIelds = aggreagteFiels
				.getAggregateFieldList();
		
		aggregateSB = aggregateSB.append( getOutputTempName()).append(" = foreach ").append(groupName)
				.append(" generate ");
		if (groupByColumns.size() == 1) {//
			aggregateSB = aggregateSB.append(" group as " + groupByColumns.get(0))
					.append(",");
			groupSB = groupSB.append(groupByColumns.get(0)).append(",");
		} else {
			for (String groupByColumn : groupByColumns) {
				aggregateSB = aggregateSB.append(" group" + "." + groupByColumn)
						.append(",");
				groupSB = groupSB.append(groupByColumn).append(",");
			}
		}
		groupSB.deleteCharAt(groupSB.length() - 1).append(") ;\n");
		
	
		for (AnalysisAggregateField analysisAggregateField: aggregateFIelds) {
			String aggregateExpression =includeSourceName(analysisAggregateField.getAggregateExpression(),inputFileName)+
					" as "+analysisAggregateField.getAlias();
			aggregateSB.append(aggregateExpression).append(	",");
		}
		 
		aggregateSB.deleteCharAt(aggregateSB.length() - 1).append(";\n");;
		
		
		String scriptString  =groupSB.append(aggregateSB).toString(); 
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("The pig script is  :\n" + scriptString);
		}
		
		return scriptString ;
	}

	private String includeSourceName(String aggregateExpression,
			String groupName) {
		  
		return aggregateExpression.replace("(", "("+groupName+".");
	}

	@Override
	public AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_DESCRIPTION,locale));
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Have created the node meta info of["+nodeMetaInfo+"]");
		}
		return nodeMetaInfo;
	}
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(hadoopSource.getHadoopFileStructureModel());
		HadoopAggregaterConfig newConfig = (HadoopAggregaterConfig)config;
		AnalysisAggregateFieldsModel aggreagteFiels = newConfig.getAggregateFieldsModel();
		List<String> newColumnNameList = new ArrayList<String> ();
		List<String> newColumnTypeList =new ArrayList<String> ();
		List<String> groupbyColumnList = aggreagteFiels.getGroupByFieldList(); 
		List<AnalysisAggregateField> aggColumnList = aggreagteFiels.getAggregateFieldList();

		for (String name:groupbyColumnList) {
			newColumnNameList.add(name) ;
			//temp, don't know 
			newColumnTypeList.add(getFieldDataType(name,aggColumnList));
		}
		for (AnalysisAggregateField analysisAggregateField : aggColumnList) {
			newColumnNameList.add(analysisAggregateField.getAlias());
			newColumnTypeList.add(analysisAggregateField.getDataType());
		}
		newModel.setColumnNameList(newColumnNameList);
		newModel.setColumnTypeList(newColumnTypeList);
		return newModel; 
	}
	
	private String getFieldDataType(String groupByColumn,
			List<AnalysisAggregateField> aggregateFIelds) {
		for (Iterator iterator = aggregateFIelds.iterator(); iterator.hasNext();) {
			AnalysisAggregateField analysisAggregateField = (AnalysisAggregateField) iterator
					.next();
			String exp = analysisAggregateField.getAggregateExpression();
			if (exp.substring(exp.indexOf("(") + 1, exp.indexOf(")")).equals(
					groupByColumn)) {
				return analysisAggregateField.getDataType();
			}
		}

		return "chararray";
	}
}
