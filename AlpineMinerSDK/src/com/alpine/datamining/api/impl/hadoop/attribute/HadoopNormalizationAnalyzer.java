/**
 * ClassName HadoopNormalizationAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: Aug 1, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopNormalizationConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Jeff Dong
 *
 */
public class HadoopNormalizationAnalyzer extends
		AbstractHadoopAttributeAnalyzer {
	
	private static Logger itsLogger = Logger.getLogger(HadoopNormalizationAnalyzer.class);
	
	private static final String PRE_CALCULATE_SCRIPT_COLUMNSQUARE_RESULT = "preCalculateScriptColumnSquareResult";
	private static final String PRE_CALCULATE_SCRIPT_COLUMNSQUARE_GROUP = "preCalculateScriptColumnSquareGroup";
	private static final String PRE_CALCULATE_SCRIPT_COLUMNSQUARE = "preCalculateScriptColumnSquare";
	private static final String PRE_CALCULATE_SCRIPT_GROUP = "preCalculateScriptGroup";
	private static final String PRE_CALCULATE_SCRIPT_VARIABLE = "preCalculateScriptVariable";

	private String[] methods = { "Proportion-Transformation",
			"Range-Transformation", "Z-Transformation","DivideByAverage-Transformation"};
	
	private String rangeMax = null;
	private String rangeMin = null;
	private List<String> originColumnName;
	@Override
	public String generateScript(HadoopDataOperationConfig config,
			String inputTempName) {
		HadoopNormalizationConfig normalizationConfig=(HadoopNormalizationConfig)config;
		
		String methodType = normalizationConfig.getMethod();
		String columnNames = normalizationConfig.getColumnNames();
		rangeMax = normalizationConfig.getRangeMax();
		rangeMin = normalizationConfig.getRangeMin();
		
		String[] columnNameArray = columnNames.split(",");
		
		if(hadoopSource!=null&&hadoopSource.getHadoopFileStructureModel()!=null){
			AnalysisFileStructureModel fileStructureModel = hadoopSource.getHadoopFileStructureModel();
			setOriginColumnName(fileStructureModel.getColumnNameList());
		}
		//First:Calculate some aggregate value for next step;
		//1.Proportion-Transformation:sum;2.Range-Transformation:min and max;3.Z-Transformation:avg and variance;4.DivideByAverage-Transformation:avg
		String preCalculateScript = generatePreCalculateScript(methodType, columnNameArray,inputTempName);
		itsLogger.info(preCalculateScript);
		
		//Second:normalize each column user selected by aggregate value;
		String calculateScript = generateCalculateScript(methodType, columnNameArray, inputTempName);
		itsLogger.info(calculateScript);
		
		String script = preCalculateScript+calculateScript;
		return script;
	}

	
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);
		HadoopNormalizationConfig newConfig = (HadoopNormalizationConfig)config;
		
		List<String> columnNameList = oldModel.getColumnNameList();
		List<String> columnTypeList = oldModel.getColumnTypeList();
		
		String columnName = newConfig.getColumnNames();
		
		if(StringUtil.isEmpty(columnName)==false){
			String[] columnNames = columnName.split(",");
			for(int i=0;i<columnNames.length;i++){
				columnTypeList.set(columnNameList.indexOf(columnNames[i]), HadoopDataType.DOUBLE);
			}
		}
		
		newModel.setColumnNameList(columnNameList);
		newModel.setColumnTypeList(columnTypeList);
		return newModel; 
	}


	private String generateCalculateScript(String methodType,
			String[] columnNameArray,String inputTempName) {
		StringBuilder calculateScript=new StringBuilder();
		List<String> originColumnName =getOriginColumnName();
		List<String> columnNameList = Arrays.asList(columnNameArray);
		calculateScript.append( getOutputTempName()).append(" = ").append(" FOREACH ").append(inputTempName);
		calculateScript.append(" GENERATE ");
		for(String s:originColumnName){
			if(columnNameList.contains(s)){
				if(methodType.equals(methods[0])){
					calculateScript.append(s).append("/(1.0*").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".sum").append(s).append(") as ").append(s).append(",");
				}else if(methodType.equals(methods[1])){
					calculateScript.append("(").append(s).append("-").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".min").append(s).append(")*1.0");
					calculateScript.append("/(").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".max").append(s).append("-").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".min").append(s).append(")");
					calculateScript.append("*(").append(rangeMax).append("-").append(rangeMin).append(")+").append(rangeMin).append(" as ").append(s).append(",");
				}else if(methodType.equals(methods[2])){
					calculateScript.append("(").append(s).append("-").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".avg").append(s).append(")*1.0");
					calculateScript.append("/SQRT(").append("(1.0*").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".").append("count").append(s).append("/(").append(PRE_CALCULATE_SCRIPT_VARIABLE);
					calculateScript.append(".").append("count").append(s).append("-1)").append(")");
					calculateScript.append("*((double)").append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE_RESULT).append(".").append("columnSquare").append(s);
					calculateScript.append("/").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".").append("count").append(s).append("-").append(PRE_CALCULATE_SCRIPT_VARIABLE);
					calculateScript.append(".").append("avg").append(s).append("*").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".").append("avg").append(s).append(")) as ").append(s).append(",");
				}else if(methodType.equals(methods[3])){
					calculateScript.append(s).append("/(1.0*").append(PRE_CALCULATE_SCRIPT_VARIABLE).append(".avg").append(s).append(") as ").append(s).append(",");
				}
			}else{
				calculateScript.append(s).append(" as ").append(s).append(",");
			}	
		}
		calculateScript=calculateScript.deleteCharAt(calculateScript.length()-1);
		calculateScript.append(";\n");
		return calculateScript.toString();
	}

	private String generatePreCalculateScript(String methodType,String[] columnNameArray,String inputTempName) {
		StringBuilder preCalculateScript=new StringBuilder();
		if(methodType.equals(methods[0])){
			preCalculateScript = preCalProportionTransformationScript(
					columnNameArray, inputTempName, preCalculateScript);
		}else if(methodType.equals(methods[1])){
			preCalculateScript = preCalRangeTransformationScript(
					columnNameArray, inputTempName, preCalculateScript);
		}else if(methodType.equals(methods[2])){
			preCalculateScript = preCalZTransformationScript(columnNameArray,
					inputTempName, preCalculateScript);
		}else if(methodType.equals(methods[3])){
			preCalculateScript = preCalAverageTransformationScript(
					columnNameArray, inputTempName, preCalculateScript);
		}
		return preCalculateScript.toString();
	}

	private StringBuilder preCalAverageTransformationScript(
			String[] columnNameArray, String inputTempName,
			StringBuilder preCalculateScript) {
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_GROUP).append(" = ").append(" GROUP ").append(inputTempName).append(" ALL;\n");
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_VARIABLE).append(" = ").append(" FOREACH ").append(PRE_CALCULATE_SCRIPT_GROUP);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append(" AVG (").append(inputTempName).append(".").append(s).append(") as avg").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		return preCalculateScript;
	}

	private StringBuilder preCalZTransformationScript(String[] columnNameArray,
			String inputTempName, StringBuilder preCalculateScript) {
		//Calculate variance
		//Step 1:Calculate sum(A*A)
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE).append(" = ").append(" FOREACH ").append(inputTempName);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append(" (").append(s).append("*").append(s).append(") as columnSquare").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE_GROUP).append(" = ").append(" GROUP ").append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE).append(" ALL;\n");
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE_RESULT).append(" = ").append(" FOREACH ").append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE_GROUP);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append("  SUM (").append(PRE_CALCULATE_SCRIPT_COLUMNSQUARE).append(".").append("columnSquare").append(s).append(") as columnSquare").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		//Step 2:Calculate sum(A)*sum(A)
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_GROUP).append(" = ").append(" GROUP ").append(inputTempName).append(" ALL;\n");

		//Step 2:Calculate average(A) and count(A)
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_VARIABLE).append(" = ").append(" FOREACH ").append(PRE_CALCULATE_SCRIPT_GROUP);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append(" AVG (").append(inputTempName).append(".").append(s).append(") as avg").append(s).append(",");
			preCalculateScript.append(" COUNT (").append(inputTempName).append(".").append(s).append(") as count").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		return preCalculateScript;
	}

	private StringBuilder preCalRangeTransformationScript(
			String[] columnNameArray, String inputTempName,
			StringBuilder preCalculateScript) {
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_GROUP).append(" = ").append(" GROUP ").append(inputTempName).append(" ALL;\n");
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_VARIABLE).append(" = ").append(" FOREACH ").append(PRE_CALCULATE_SCRIPT_GROUP);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append(" MIN (").append(inputTempName).append(".").append(s).append(") as min").append(s).append(",");
			preCalculateScript.append(" MAX (").append(inputTempName).append(".").append(s).append(") as max").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		return preCalculateScript;
	}

	private StringBuilder preCalProportionTransformationScript(
			String[] columnNameArray, String inputTempName,
			StringBuilder preCalculateScript) {
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_GROUP).append(" = ").append(" GROUP ").append(inputTempName).append(" ALL;\n");
		preCalculateScript.append(PRE_CALCULATE_SCRIPT_VARIABLE).append(" = ").append(" FOREACH ").append(PRE_CALCULATE_SCRIPT_GROUP);
		preCalculateScript.append(" GENERATE ");
		for(String s:columnNameArray){
			preCalculateScript.append(" SUM (").append(inputTempName).append(".").append(s).append(") as sum").append(s).append(",");
		}
		preCalculateScript=preCalculateScript.deleteCharAt(preCalculateScript.length()-1);
		preCalculateScript.append(";\n");
		return preCalculateScript;
	}
	
	public List<String> getOriginColumnName() {
		return originColumnName;
	}

	public void setOriginColumnName(List<String> originColumnName) {
		this.originColumnName = originColumnName;
	}

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_DESCRIPTION,locale));

		return nodeMetaInfo;
	}
}
