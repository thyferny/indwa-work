package com.alpine.datamining.api.impl.db.attribute.histogram;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;

public interface HistogramMultiDBUtility {

	HashMap<String, HashMap<Integer, Long>> dealResult(
			ArrayList<Column> analysisList, ResultSet rs,
			Map<String, Integer> binMap) throws SQLException;
	
	void addIntArrayTail(StringBuilder sb_count_eachBin);
	
	void addIntArrayHead(StringBuilder sb_count_eachBin);
	
	void addFloatArrayTail(StringBuilder sb_count_eachBin);
	
	void addFloatArrayHead(StringBuilder sb_count_eachBin);
	
	void dealResult(HistogramAnalysisResult histogramAnalysisResult,
			ArrayList<Column> analysisList, ResultSet rs,
			HashMap<String, BigDecimal[]> binMap) throws SQLException ;
			
}
