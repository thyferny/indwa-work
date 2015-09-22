package com.alpine.datamining.api.impl.db.attribute.histogram;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;

public class HistogramMultiDBUtilityDB2 implements HistogramMultiDBUtility {

	@Override
	public void addIntArrayHead(StringBuilder sbCountEachBin) {

	}

	@Override
	public void addIntArrayTail(StringBuilder sbCountEachBin) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, HashMap<Integer, Long>> dealResult(
			ArrayList<Column> analysisList, ResultSet rs,
			Map<String, Integer> binMap) throws SQLException {
		int temp = 0;
		int temp1 = 0;
		HashMap<String, HashMap<Integer, Long>> countForEachColumn = new HashMap<String, HashMap<Integer, Long>>();
		HashMap<Integer, Long> countMap = new HashMap<Integer, Long>();
		for (int s = 0; s < rs.getMetaData().getColumnCount(); s++) {
			countMap.put(temp + 1, rs.getLong(s+1));
			if (temp+1 == binMap.get(analysisList.get(temp1).getName())) {
				HashMap<Integer, Long> newCountMap = (HashMap<Integer, Long>) countMap
						.clone();
				countForEachColumn.put(analysisList.get(temp1).getName(),
						newCountMap);
				countMap.clear();
				temp = 0;
				temp1++;
			} else {
				temp++;
			}
		}
		return countForEachColumn;
	}

	@Override
	public void addFloatArrayHead(StringBuilder sbCountEachBin) {
		
	}

	@Override
	public void addFloatArrayTail(StringBuilder sbCountEachBin) {
		
	}

	@Override
	public void dealResult(HistogramAnalysisResult histogramAnalysisResult,
			ArrayList<Column> analysisList, ResultSet rs,
			HashMap<String, BigDecimal[]> binMap) throws SQLException {
		int m = 0;
		int resulttemp = 0;
		for (int s = 0; s < rs.getMetaData().getColumnCount(); s = s + 4) {
			BigDecimal[] column_bins = binMap
					.get(analysisList.get(m).getName());
			BinHistogramAnalysisResult binHistogramAnalysisResult = new BinHistogramAnalysisResult();
			binHistogramAnalysisResult.setColumnName(analysisList.get(m).getName());
			binHistogramAnalysisResult.setBin((resulttemp + 1));
			binHistogramAnalysisResult
					.setBegin(column_bins[resulttemp].floatValue());
			binHistogramAnalysisResult
					.setEnd(column_bins[resulttemp + 1].floatValue());
			binHistogramAnalysisResult.setCount(rs.getInt(s+1));
			binHistogramAnalysisResult.setPercentage(rs.getFloat(s+2));
			binHistogramAnalysisResult.setAccumCount(rs.getFloat(s+3));
			binHistogramAnalysisResult
					.setAccumPercentage(rs.getFloat(s+4));
			histogramAnalysisResult
					.addBinResult(binHistogramAnalysisResult);
			if (resulttemp+1 == column_bins.length-1) {
				m++;
				resulttemp = 0;
			} else {
				resulttemp++;
			}
		
	}
	}
}
