package com.alpine.datamining.api.impl.hadoop.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;

public class HadoopHistogramScriptGenerator {
	
	private static final Logger itsLogger =Logger.getLogger(HadoopHistogramScriptGenerator.class);
	private static final String PIG_VARIABLE_NAME = "PigVariableName";
	private static final String _X = "_X";
	private static final String ALPINE_MAX_MIN="AlpinePigyMaxMin";
	private static final String ALPINE_BIN_UDF="AlpinePiggyHistogram";
	
	
	
	
	private static final String PROJECTOR_FOR_NUMBER 	 ="prColumnNumberPigVariableName = FOREACH PigVariableName generate ";
	
	
	private static final String GROUP_PROJECTOR_NUMBER  ="grPrNumberPigVariableName = GROUP prColumnNumberPigVariableName  ALL;";
	
	private static final String GROUP_AGGREGATOR_HEADER_FOR_NUMBER = "agPrNumberPigVariableName = FOREACH grPrNumberPigVariableName GENERATE "+ALPINE_MAX_MIN+"(prColumnNumberPigVariableName);";

	private static final String BIN_SCRIPT = "histogramBinDistributionPigVariableName = FOREACH grPrNumberPigVariableName GENERATE "+ALPINE_BIN_UDF+"(prColumnNumberPigVariableName,";
	
	private static final String DEVIATION_SQR_DIVIDE_INITIAL = "deviationsForPigVariableName = FOREACH PigVariableName GENERATE ";
	private static final String DEVIATION_SQR_DIVIDE_INDIVIDUAL_COLUMNS = " (((Double)ColumnNameAVG)*((Double)ColumnNameAVG)/(Double)count) as sqrValueColumnName ";
	private static final String DEVIATION_GROUP_ALL = "grpdDeviationForThePigVariableName = GROUP deviationsForPigVariableName  ALL;";
	private static final String DEVIATION_INITIAL_FOR_THE_COLUMN_OF = "deviationForThePigVariableName = foreach grpdDeviationForThePigVariableName generate  ";
	private static final String DEVIAION_FINAL = " SQRT(SUM(deviationsForPigVariableName.sqrValueColumnName)) ";
	

	
	private Map<String,String> pigColumnNames;
	private String pigVariableName;
	private SortedSet<String> sortedColumnNames;
	private Map<String,Integer> columnOrder;

	private Map<String, AnalysisColumnBin> binsMap;
	

	
	public HadoopHistogramScriptGenerator(){
		columnOrder=new HashMap<String,Integer>();
	}
	
	public HadoopHistogramScriptGenerator(String pigVariableName,
			Map<String, AnalysisColumnBin> binsMap) {
		this();
		this.pigVariableName = pigVariableName;
		this.binsMap=binsMap;
		initColumnOrders();
	}
	
	
	
	private void initColumnOrders() {
		Set<String> colNames=binsMap.keySet();
		sortedColumnNames = new TreeSet<String>();
		for(String name:colNames){
			sortedColumnNames.add(name);
		}
		int order=0;
		for(String name:sortedColumnNames){
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
	
	
	public String generateFilter() {
		StringBuilder sb = new StringBuilder("");
		for (String colName:sortedColumnNames) {
			sb.append(colName).append(" IS NOT NULL AND ");
		}
		String str = sb.toString();
		str = str.substring(0, str.length() - 4) + ";";
		return str;
	}
	public List<String> genPigScriptForNumbers(){
		if(null==sortedColumnNames||0==sortedColumnNames.size()){
			return new ArrayList<String>();
		}
		List<String> script = new ArrayList<String>();
		
		
		//Do the filtering here
		List<String> filteredName = doFiltering();
		if(itsLogger.isDebugEnabled()&&null!=filteredName&&0!=filteredName.size()){
			itsLogger.debug(filteredName.toString());
		}else if(itsLogger.isDebugEnabled()){
			itsLogger.debug("There is no need for filtering");
		}
		
		//pigVariableName = 0==filteredName.size()?pigVariableName:"filtered"+pigVariableName;
		String line =PROJECTOR_FOR_NUMBER.replace(PIG_VARIABLE_NAME,pigVariableName);
		//Add the columns
		int i=0;
		pigColumnNames = new HashMap<String,String>();
		for(String columnName:sortedColumnNames){
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
		
		
		script.add(line);
		
		
		
		return  cleanUpString(script);
	}
	public String genPigScriptForBins(String minMaxStepSize){
		String line =BIN_SCRIPT.replace(PIG_VARIABLE_NAME, pigVariableName);
		line +=minMaxStepSize+");";
		return  line;
	}
	
	
	public String getOverloadedPigVariableName(){
		return pigVariableName;
	}
	private List<String> doFiltering() {
		List<String> filters = new ArrayList<String>();
		for(String columnName:sortedColumnNames){
			AnalysisColumnBin bin = binsMap.get(columnName);
			boolean isFiltered = bin.isMin() || bin.isMax();
			if(isFiltered){
				filters.add(genFilter(bin));
			}
		}
		
		return filters;
	}
	
	private String genFilter(AnalysisColumnBin bin){
		
		StringBuilder filter = new StringBuilder();
		boolean isFiltered = bin.isMin() || bin.isMax();
		if (isFiltered) {
			/*
		  		file165080751 = load 'hdfs://supportserver.local:8200/s/cleanSalaries.txt' USING PigStorage(',')  as (salary:int,age:int,edu:int);
		  		records_group_filtered = FILTER file165080751 BY edu >  10.0;
				records_group = GROUP records_group_filtered ALL;
				record_max_min = foreach records_group generate MAX(records_group_filtered.edu) as maxEdu,MIN(records_group_filtered.edu) as minEdu;
			 * 
			 */ 
			if (bin.isMin() && !bin.isMax()) {// only min
				filter.append("columnName >  ");
				filter.append(bin.getMin());
			} else if (!bin.isMin() && bin.isMax()) {// only max
				filter.append("columnName <= ");
				filter.append(bin.getMax());
			} else {// max and min
				filter.append("columnName >");
				filter.append(bin.getMin());
				filter.append(" and ");
				filter.append("columnName <= ");
				filter.append(bin.getMax());
			}
		}
		return filter.toString().replace("columnName", bin.getColumnName());
	}

	public List<String> generateDeviationScript(List<ColumnValueAnalysisResult>columnAvgCount){
		
		
		List<String> script = new ArrayList<String>();
		String line = DEVIATION_SQR_DIVIDE_INITIAL.replace(PIG_VARIABLE_NAME,pigVariableName);
		
		
		for (ColumnValueAnalysisResult row : columnAvgCount) {
			String sqr = DEVIATION_SQR_DIVIDE_INDIVIDUAL_COLUMNS.replace("AVG",
					row.getAvg()>0
						?("-"+Math.abs(row.getAvg()))
						:("+"+Math.abs(row.getAvg())));
			sqr = sqr.replace("count", row.getCount()-1 + "");
			sqr = sqr.replace("ColumnName", row.getColumnName());
			sqr += ",";
			line += sqr;

		}
		line = line.substring(0, line.length()-1);
		line = line+";";
		
		script.add(line);
		
		
		line = DEVIATION_GROUP_ALL.replace(PIG_VARIABLE_NAME,pigVariableName);
		script.add(line);
		
		line = DEVIATION_INITIAL_FOR_THE_COLUMN_OF.replace(PIG_VARIABLE_NAME,pigVariableName);
		
		
		for(ColumnValueAnalysisResult row:columnAvgCount){
			String sqr = DEVIAION_FINAL.replace("sqrValueColumnName", "sqrValue"+row.getColumnName());
			sqr = sqr.replace(PIG_VARIABLE_NAME,pigVariableName);
			sqr+=",";
			line +=sqr;
		}
		line = line.substring(0, line.length()-1);
		line = line+";";
		
		script.add(line);
		
		
		return cleanUpString(script);
		
		
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
	public String getHistogramBinDistributionName(){
		return "histogramBinDistributionPigVariableName".replace("PigVariableName", getPigVariableName());
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
		return "deviationForThe"+pigVariableName;
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