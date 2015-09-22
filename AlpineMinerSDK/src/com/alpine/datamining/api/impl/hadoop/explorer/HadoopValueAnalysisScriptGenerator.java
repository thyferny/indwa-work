package com.alpine.datamining.api.impl.hadoop.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;

public class HadoopValueAnalysisScriptGenerator {
	

	private static final String PIG_VARIABLE_NAME = "PigVariableName";
	private static final String _X = "_X";
	private static final String PROJECTOR_FOR_NUMBER 	 ="prColumnNumberPigVariableName = FOREACH PigVariableName generate ";
	private static final String GROUP_PROJECTOR_NUMBER  ="grPrNumberPigVariableName = GROUP prColumnNumberPigVariableName  ALL;";
	//private static final String GROUP_AGGREGATOR_HEADER_FOR_NUMBER = "agPrNumberPigVariableName = FOREACH grPrNumberPigVariableName GENERATE AlpinePigyDistinctLimitedValueAnalyzer(prColumnNumberPigVariableName);";
	private static final String GROUP_AGGREGATOR_HEADER_FOR_NUMBER = "agPrNumberPigVariableName = FOREACH grPrNumberPigVariableName GENERATE AlpinePigSummaryStatistics(prColumnNumberPigVariableName);";
	
	
	private static final String PROJECTOR_FOR_NUMBER_DEV 	 ="prColumnNumberPigVariableNameDevNum = FOREACH PigVariableName generate ";
	private static final String GROUP_PROJECTOR_NUMBER_DEV  ="grPrNumberPigVariableNameDevNum = GROUP prColumnNumberPigVariableNameDevNum  ALL;";
	private static final String GROUP_AGGREGATOR_HEADER_FOR_NUMBER_DEV = "agDeviPrNumberPigVariableName = FOREACH grPrNumberPigVariableNameDevNum GENERATE AlpinePigyValueAnalyzerDeviationCalculator(prColumnNumberPigVariableNameDevNum,";
	
	
	private static final String DEVIATION_SQR_DIVIDE_INITIAL = "deviationsForPigVariableName = FOREACH PigVariableName GENERATE ";
	private static final String DEVIATION_SQR_DIVIDE_INDIVIDUAL_COLUMNS = " (((Double)ColumnNameAVG)*((Double)ColumnNameAVG)/(Double)count) as sqrValueColumnName ";
	private static final String DEVIATION_GROUP_ALL = "grpdDeviationForThePigVariableName = GROUP deviationsForPigVariableName  ALL;";
	private static final String DEVIATION_INITIAL_FOR_THE_COLUMN_OF = "deviationForThePigVariableName = foreach grpdDeviationForThePigVariableName generate  ";
	private static final String DEVIAION_FINAL = " SQRT(SUM(deviationsForPigVariableName.sqrValueColumnName)) ";
	

	
	private Map<String,String> pigColumnNames;
	private String pigVariableName;
	private SortedSet<String> columnNames;
	private Map<String,Integer> columnOrder;
	

	
	public HadoopValueAnalysisScriptGenerator(){
		columnOrder=new HashMap<String,Integer>();
	}
	public HadoopValueAnalysisScriptGenerator(String pigVariableName,
			SortedSet<String> columnNames) {
		this();
		this.pigVariableName = pigVariableName;
		this.columnNames = columnNames;
		initColumnOrders();
	}
	
	
	
	private void initColumnOrders() {
		int order=0;
		for(String name:columnNames){
			columnOrder.put(name, order++);
		}
		
	}
	
	public String getPigColumnVariableName(String columnName){
		if(null==columnName){
			throw new IllegalArgumentException("Column name can not be null");
		}
		if(null==pigColumnNames){
			throw new IllegalStateException("Pig Column variable names are not assigned yet");
		}
		return pigColumnNames.get(columnName);
	}
	
	public List<String> genPigScriptForNumbers(){
		if(null==columnNames||0==columnNames.size()){
			return new ArrayList<String>();
		}
		List<String> script = new ArrayList<String>();
		String line =PROJECTOR_FOR_NUMBER.replace(PIG_VARIABLE_NAME,pigVariableName);
		//Add the columns
		int i=0;
		pigColumnNames = new HashMap<String,String>();
		for(String columnName:columnNames){
			String pigCN = columnName+_X+i;
			pigColumnNames.put(columnName, pigCN);
			line +=columnName+" as "+pigCN+",";
			i++;
		}
		line=line.substring(0,line.length()-1);
		//Add the semicolon
		line=line.concat(";");
		// group all
		script.add(line);
		script.add( GROUP_PROJECTOR_NUMBER.replace(PIG_VARIABLE_NAME, pigVariableName));
		
		
		line=GROUP_AGGREGATOR_HEADER_FOR_NUMBER.replace(PIG_VARIABLE_NAME, pigVariableName);
		line = line.substring(0,line.length()-1);
		line+=";";
		
		
		script.add(line);
		
		
		
		return  cleanUpString(script);
	}
	
	
	
	public List<String> generateDeviationScript(List<ColumnValueAnalysisResult>columnAvgCount){
		if(null==columnNames||0==columnNames.size()){
			return new ArrayList<String>();
		}
		List<String> script = new ArrayList<String>();
		
		
		
		
		String line =PROJECTOR_FOR_NUMBER_DEV.replace(PIG_VARIABLE_NAME,pigVariableName);
		//Add the columns
		int i=0;
		pigColumnNames = new HashMap<String,String>();
		for(ColumnValueAnalysisResult column:columnAvgCount){
			String pigCN = column.getColumnName()+_X+i;
			line +=column.getColumnName()+" as "+pigCN+",";
			i++;
		}
		line=line.substring(0,line.length()-1);
		//Add the semicolon
		line=line.concat(";");
		// group all
		script.add(line);
		script.add( GROUP_PROJECTOR_NUMBER_DEV.replace(PIG_VARIABLE_NAME, pigVariableName));
		
		
		line=GROUP_AGGREGATOR_HEADER_FOR_NUMBER_DEV.replace(PIG_VARIABLE_NAME, pigVariableName);
		
		
		for (ColumnValueAnalysisResult row : columnAvgCount) {
			String sqr = row.getAvg()+","+ (row.getCount()-1)+",";
			line += sqr;

		}
		line = line.substring(0, line.length()-1);
		line = line+");";
		
		script.add(line);
		
		
		
		line = line.substring(0,line.length()-1);
		line+=";";
		
		
		script.add(line);
		
		
		
		return  cleanUpString(script);
		
		
	}
	
	
	
	public Integer getColumnOrder(String columnName){
		if(null==columnName||columnName.trim().equals("")){
			throw new IllegalArgumentException("Column neither can be null nor empty");
		}
		return columnOrder.get(columnName);
		
	}
	
	private List<String> cleanUpString(List<String> script) {
		
		List<String>  cleanScript = new ArrayList<String>();
		for(int i=0;i<script.size();i++){
			cleanScript.add(script.get(i).replace("\t", " "));
			
		}
		return cleanScript;
	}

	public String getPigVariableName() {
		return pigVariableName;
	}

	public void setPigVariableName(String pigVariableName) {
		this.pigVariableName = pigVariableName;
	}

	public String getAggregatorVariableNameForText() {
		return "agPrText"+pigVariableName;
	}
	
	public String getAggregatorVariableNameForNumber() {
		return "agPrNumber"+pigVariableName;
	}
	
	public String getDeviationVariableName() {
		return "agDeviPrNumber"+pigVariableName;
	}
	
	
	
	
	
	
	
}


/*



--Make sure not to add ; to the end of next line
projectedColumnsForPigVariableName  	= FOREACH PigVariableName generate 
-- we will add "ColumnName as ColumnNameXi" for each column and will also add ";" to the end

gropuedProjectionsForPigVariableName   	= GROUP projectedColumnsForPigVariableName  ALL;

aggregatedProjectionsForPigVariableName 	 	= FOREACH gropuedProjectionsForPigVariableName{					\n
		distinctColumnNameXi 	= DISTINCT projectedColumnsForPigVariableName.ColumnNameXi; 					\n 
		zeroColumnNameXi 		= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi == 0; 		\n
		positiveColumnNameXi 	= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi <  0; 		\n
		negativeColumnNameXi 	= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi >  0; 		\n
		nullColumnNameXi 		= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi IS NULL ; 	\n 
																												\n
		GENERATE 																								\n 
																												\n
		AVG(projectedColumnsForPigVariableName.ColumnNameXi)			as avgColumnNameXi, 					\n
		COUNT(projectedColumnsForPigVariableName.ColumnNameXi) 			as countColumnNameXi,					\n
		MAX(projectedColumnsForPigVariableName.ColumnNameXi) 			as maxColumnNameXi, 					\n
		MIN(projectedColumnsForPigVariableName.ColumnNameXi) 			as minColumnNameXi,  					\n
		COUNT(positiveColumnNameXi) 									as biggerColumnNameXi,					\n
		COUNT(negativeColumnNameXi) 									as smallerColumnNameXi,					\n
		COUNT(zeroColumnNameXi) 										as zerosColumnNameXi,					\n
		COUNT(distinctColumnNameXi) 									as distinctColumnNameXi;				\n
																												\n
	};	
*/