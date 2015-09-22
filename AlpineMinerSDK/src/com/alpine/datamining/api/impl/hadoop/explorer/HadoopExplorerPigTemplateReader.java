package com.alpine.datamining.api.impl.hadoop.explorer;

import static com.alpine.utility.file.AlpineFileUtility.readAlpinePigScript;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.utility.file.AlpineFileUtility;

public class HadoopExplorerPigTemplateReader {
	private static Logger itsLogger = Logger
			.getLogger(HadoopExplorerPigTemplateReader.class);
	
	private static final Map<String,String> pigTemplateSrcMap;
	
	//
	private static final String  PIG_VALUE_ANALYSIS_TEXT ="PIG_VALUE_ANALYSIS_TEXT";
	private static final String  PIG_VALUE_ANALYSIS_NUMERIC ="PIG_VALUE_ANALYSIS_NUMERIC";
	private static final String  PIG_SCATTER_PLOT_MATRIX ="PIG_SCATTER_PLOT_MATRIX";
	private static final String  PIG_RANDOM_SAMPLING ="PIG_RANDOM_SAMPLING";
	
	//HardCoded
	private static final String[]  PIG_VALUE_ANALYSIS_TEXT_HARD_CODED;
	private static final String[]  PIG_VALUE_ANALYSIS_NUMERIC_HARD_CODED;
	private static final String[]  PIG_SCATTER_PLOT_MATRIX_HARD_CODED;
	private static final String[] PIG_RANDOM_SAMPLING_HARD_CODED;
	
	
	private static final String PIG_RESOURCE_DIR = "/pigscript/";
	private static final String PIG_VALUE_ANALYSIS_TEXT_SRC 	= "HadoopValueAnalysisAnalyzerForNonNumericValues.pig";
	private static final String PIG_VALUE_ANALYSIS_NUMERIC_SRC 	= "HadoopValueAnalysisAnalyzer.pig";
	private static final String PIG_SCATTER_PLOT_MATRIX_SRC 	= "HadoopScatterPlotMatrixAnalyzerTemplate.pig";
	private static final String PIG_RANDOM_SAMPLING_SRC 		= "randomSampling.pig";
	
	
	
	static{
		pigTemplateSrcMap=new HashMap<String,String>();
		pigTemplateSrcMap.put(PIG_VALUE_ANALYSIS_TEXT, PIG_RESOURCE_DIR+PIG_VALUE_ANALYSIS_TEXT_SRC);
		pigTemplateSrcMap.put(PIG_VALUE_ANALYSIS_NUMERIC, PIG_RESOURCE_DIR+PIG_VALUE_ANALYSIS_NUMERIC_SRC);
		pigTemplateSrcMap.put(PIG_SCATTER_PLOT_MATRIX, PIG_RESOURCE_DIR+PIG_SCATTER_PLOT_MATRIX_SRC);
		pigTemplateSrcMap.put(PIG_RANDOM_SAMPLING, PIG_RESOURCE_DIR+PIG_RANDOM_SAMPLING_SRC);
		
		
		PIG_VALUE_ANALYSIS_TEXT_HARD_CODED=getValueAnalysisTextScript();
		PIG_VALUE_ANALYSIS_NUMERIC_HARD_CODED=getValueAnalysisNumericScript();
		PIG_SCATTER_PLOT_MATRIX_HARD_CODED=getScatterPlotMatrixScript();
		PIG_RANDOM_SAMPLING_HARD_CODED = getRandomSamplingScript();
		
	}
	
	private static String[] getHardcodeScriptForTheFileOf(
			String nameOfTheTemplateFile) {
		if(nameOfTheTemplateFile.equalsIgnoreCase(PIG_VALUE_ANALYSIS_TEXT)){
			return PIG_VALUE_ANALYSIS_TEXT_HARD_CODED;
		}else if(nameOfTheTemplateFile.equalsIgnoreCase(PIG_VALUE_ANALYSIS_NUMERIC)){
			return PIG_VALUE_ANALYSIS_NUMERIC_HARD_CODED;
		}else if(nameOfTheTemplateFile.equalsIgnoreCase(PIG_SCATTER_PLOT_MATRIX)){
			return PIG_SCATTER_PLOT_MATRIX_HARD_CODED;
		}else if(nameOfTheTemplateFile.equalsIgnoreCase(PIG_RANDOM_SAMPLING)){
			return PIG_RANDOM_SAMPLING_HARD_CODED;
		}
		
		itsLogger.fatal("We do not have the hard coded script for ["+nameOfTheTemplateFile+"]");
		return null;
	}
	public static String[] fetchAlpinePigTemplate(String nameOfTheTemplateFile){
		if(null==nameOfTheTemplateFile){
			String strError="Alpine Pig template file can not be null";
			throw new IllegalArgumentException(strError);
		}
		
		String fileLocation = pigTemplateSrcMap.get(nameOfTheTemplateFile);
		
		if(null==fileLocation){
			String strError="Alpine Pig template file location is not register yet for the file name of["+nameOfTheTemplateFile+"]";
			throw new IllegalStateException(strError);
		}
		InputStream is =null;
		try{
			is = HadoopValueAnalysisAnalyzer.class.getResourceAsStream(fileLocation);
			if(null==is){
				itsLogger.error("Could not open the resorce of[]will try to get hard coded pig script for it");
				return  getHardcodeScriptForTheFileOf(nameOfTheTemplateFile);
			}
			return readAlpinePigScript(is);
			
		}catch(Throwable e){
			itsLogger.error("Failed to load the pig script due to the exception of:",e);
			return getHardcodeScriptForTheFileOf(nameOfTheTemplateFile);
			
		}finally{
			if(null!=is){
				try {
					is.close();
				} catch (IOException e) {
					itsLogger.error("Could not close the input stream due to exception of",e);
				}
			}
			
		}
		
		
	}
	
	



	private static String[] getValueAnalysisNumericScript(){
		
		List<String> worAroundScript = new ArrayList<String>();
		worAroundScript.add("aColumnNameColumn  = FOREACH pigVariable generate ColumnName as colColumnName;");
		worAroundScript.add("gGroupedColumnName = GROUP aColumnNameColumn  ALL;");
		worAroundScript.add("aggregatedColumnName = FOREACH gGroupedColumnName{ \\");	
		worAroundScript.add("distinctColumnName = DISTINCT aColumnNameColumn.colColumnName; \\");
		worAroundScript.add("generate MAX(aColumnNameColumn.colColumnName) as maxColumnName, \\");
		worAroundScript.add("MIN(aColumnNameColumn.colColumnName) as minColumnName, \\");
		worAroundScript.add("AVG(aColumnNameColumn.colColumnName) as avgColumnName, \\");
		worAroundScript.add("COUNT(aColumnNameColumn.colColumnName) as countColumnName,\\");
		worAroundScript.add("COUNT(distinctColumnName) as distColumnName,\\");
		worAroundScript.add("AlpineNegativePositiveZeroNull(aColumnNameColumn.colColumnName) as alpineAvgs;\\");
		worAroundScript.add("};");
		worAroundScript.add("deviationIndividualsColumnNames  = FOREACH aColumnNameColumn generate ((colColumnName-aggregatedColumnName.avgColumnName)*(colColumnName-aggregatedColumnName.avgColumnName)/aggregatedColumnName.countColumnName) as sqrValue;");
		worAroundScript.add("grpdDeviationsColumnName =  GROUP deviationIndividualsColumnNames  ALL;");
		worAroundScript.add("deviationColumnName = foreach grpdDeviationsColumnName generate SQRT(SUM(deviationIndividualsColumnNames.sqrValue));");
		String[] cScript=worAroundScript.toArray(new String[]{});
		cScript = AlpineFileUtility.convertIntoPigScript(cScript);
		return cScript;
	}
	
	private static String[] getValueAnalysisTextScript(){
		List<String> worAroundScript = new ArrayList<String>();
		worAroundScript = new ArrayList<String>();
		worAroundScript.add("aColumnNameColumn  = FOREACH pigVariable generate ColumnName as colColumnName;");
		worAroundScript.add("gGroupedColumnName = GROUP aColumnNameColumn  ALL;");
		worAroundScript.add("charAggregatedColumnName = FOREACH gGroupedColumnName{ \\");	
		worAroundScript.add("distinctColumnName = DISTINCT aColumnNameColumn.colColumnName; \\");
		worAroundScript.add("generate COUNT(aColumnNameColumn.colColumnName) as countColumnName,\\");
		worAroundScript.add("COUNT(distinctColumnName) as distColumnName,\\");
		worAroundScript.add("COUNT_STAR(aColumnNameColumn.colColumnName) as countAllColumnName;\\");
		worAroundScript.add("};");
		String[] cScript=worAroundScript.toArray(new String[]{});
		cScript = AlpineFileUtility.convertIntoPigScript(cScript);
		
		return cScript;
	}
	
	private static String[] getScatterPlotMatrixScript(){
		List<String> worAroundScript = new ArrayList<String>();
		
		worAroundScript.add("aScatterPlotColumnspigVariable = FOREACH pigVariable generateXXX");
		worAroundScript.add("preCalculationpigVariable = GROUP aScatterPlotColumnspigVariable  ALL;");
		worAroundScript.add("agPrNumberpigVariable = FOREACH preCalculationpigVariable GENERATE AlpinePiggyScatterPlotterMatrix(aScatterPlotColumnspigVariable);");
		
		String[] cScript=worAroundScript.toArray(new String[]{});
		cScript = AlpineFileUtility.convertIntoPigScript(cScript);
		
		return cScript;
	}
	private static String[] getRandomSamplingScript(){
		List<String> worAroundScript = new ArrayList<String>();
		worAroundScript.add("raws = FOREACH (GROUP pigVariable ALL) GENERATE COUNT($1) as totalCount;");
		worAroundScript.add("samplePlotColumnsByCount = SAMPLE pigVariable 1NumberOfSamplesXXX/raws.totalCount;");
		worAroundScript.add("samplePlotColumnsByPercentage = SAMPLE pigVariable 2percantageYYY;");
		String[] cScript=worAroundScript.toArray(new String[]{});
		cScript = AlpineFileUtility.convertIntoPigScript(cScript);
		
		return cScript;
	}
	

}
