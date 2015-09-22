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

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;

/**
 * @author Jeff Dong
 *
 */
public class HadoopJoinAnalyzer extends AbstractHadoopAttributeAnalyzer {
	private static final Logger itsLogger = Logger.getLogger(HadoopJoinAnalyzer.class);
	@Override
	public String generateScript(HadoopDataOperationConfig config,String pureFileName) {
		AnalysisHadoopJoinModel joinModel = ((HadoopJoinConfig)config).getJoinModel();
		List<AnalysisHadoopJoinFile> joinTables = joinModel.getJoinTables();
		List<AnalysisHadoopJoinCondition> joinConditions = joinModel.getJoinConditions();
		List<AnalysisHadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
		String joinType = joinModel.getJoinType();
		
		String tempName="A"+System.currentTimeMillis();
		StringBuffer script=new StringBuffer();
 
		
		generateJoinScript(joinTables, joinConditions,joinType ,tempName,
				script);
		
		generateSelectedColumnScript(joinColumns,
				tempName, script);
		
		itsLogger.info(script.toString());
		return script.toString();
	}


	private void generateSelectedColumnScript(
			List<AnalysisHadoopJoinColumn> joinColumns, String tempName,
			StringBuffer script) {
		script.append(getOutputTempName()).append(" = FOREACH ").append(tempName);
		script.append(" GENERATE ");
		if(joinColumns!=null){
			for(int i = 0;i<joinColumns.size();i++){
				AnalysisHadoopJoinColumn joinColumn=joinColumns.get(i);
				String columnName = joinColumn.getColumnName();
				String tempInput = joinColumn.getFileId().replace(".","");
				if(tempInput.startsWith(OUT_PREFIX)==false){
					tempInput=OUT_PREFIX+tempInput;
				}
				columnName=  tempInput+"::"+columnName;
				String newColumnName = joinColumn.getNewColumnName();
				script.append(columnName).append(" as ").append(newColumnName);
				script.append(",");
			}
			script=script.deleteCharAt(script.length()-1);
			script.append(";");
		}
	}


	private void generateJoinScript(
			List<AnalysisHadoopJoinFile> joinTables,
			List<AnalysisHadoopJoinCondition> joinConditions, 
			String joinType,
			String tempName,
			StringBuffer script) {
		script.append(tempName).append(" = join ");		

		for(int i=0;i<joinConditions.size();i++){
			AnalysisHadoopJoinFile joinFile=joinTables.get(i);
			AnalysisHadoopJoinCondition joinCondition = joinConditions.get(i);
			 
			String keyColumn = joinCondition.getKeyColumn();
 			String tempInput = joinFile.getOperatorModelID().replace(".","");
			if(tempInput.startsWith(OUT_PREFIX)==false){
				tempInput=OUT_PREFIX+tempInput;
			}
			
			if(getContext()!=null&&getContext().isEmptyPigVariable(tempInput)){
				throw new RuntimeException(EMPTY_INPUT_MSG);
			}
			script.append(tempInput).append(" by ").append(keyColumn);
			if(i!=(joinConditions.size()-1)){
				if(joinType.equals(HadoopJoinConfig.JOIN_TYPE[0])){
					joinType="";
				}
				script.append(" ").append(joinType);
			}		
			script.append(" ,");
		}
//		C = join records by outlook, record1s by outlook;
//		D = join records by outlook left outer, record1s by outlook left outer,record2s by outlook;
//		D = join records by outlook, record1s by outlook,record2s by outlook;

		script=script.deleteCharAt(script.length()-1);
		script.append(";\n");
	}

	
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);

		HadoopJoinConfig newConfig = (HadoopJoinConfig)config;
		AnalysisHadoopJoinModel joinModel = newConfig.getJoinModel();
		List<String> newColumnNameList = new ArrayList<String>();
		List<String> newColumnTypeList = new ArrayList<String>();
		
		if(joinModel!=null&&joinModel.getJoinColumns()!=null){
			List<AnalysisHadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
			for(AnalysisHadoopJoinColumn joinColumn:joinColumns){
				newColumnNameList.add(joinColumn.getNewColumnName());
				newColumnTypeList.add(joinColumn.getColumnType());
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
