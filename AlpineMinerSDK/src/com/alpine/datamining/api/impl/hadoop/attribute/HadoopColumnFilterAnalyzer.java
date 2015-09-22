package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopColumnFilterConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.file.StringUtil;

public class HadoopColumnFilterAnalyzer extends AbstractHadoopAttributeAnalyzer {

	@Override
	public String generateScript(HadoopDataOperationConfig config,String inputTempName) {
		HadoopColumnFilterConfig cConfig = (HadoopColumnFilterConfig)config;
		String columnNames = cConfig.getColumnNames();
		String[] columnNamesArray = columnNames.split(",");
		StringBuffer script=new StringBuffer();
		script.append( getOutputTempName()).append(" = FOREACH ").append(inputTempName);
		script.append(" GENERATE ");
		for(String columnName:columnNamesArray){
			script.append(columnName).append(",");
		}
		script=script.deleteCharAt(script.length()-1).append(";");
		return script.toString();
	}
	
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);
		HadoopColumnFilterConfig newConfig = (HadoopColumnFilterConfig)config;
		
		List<String> newColumnNameList = new ArrayList<String>();
		List<String> newColumnTypeList =new ArrayList<String>();
		
		List<String> columnNameList = oldModel.getColumnNameList();
		List<String> columnTypeList = oldModel.getColumnTypeList();
		String columnName = newConfig.getColumnNames();
		if(StringUtil.isEmpty(columnName )==false){
			String[] columnNames = columnName.split(",");
			for(String column:columnNames){
				newColumnNameList.add(column);
				for(int i=0;i<columnNameList.size();i++){
					if(column.equals(columnNameList.get(i))){
						newColumnTypeList.add(columnTypeList.get(i));
						break;
					}
				}
			}
		}
		
		newModel.setColumnNameList(newColumnNameList);
		newModel.setColumnTypeList(newColumnTypeList);

		return newModel; 
	}
	
	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.COLUMN_FILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.COLUMN_FILTER_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

}
