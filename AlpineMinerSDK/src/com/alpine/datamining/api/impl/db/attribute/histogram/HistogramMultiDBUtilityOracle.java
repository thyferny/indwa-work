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
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;

public class HistogramMultiDBUtilityOracle implements HistogramMultiDBUtility {
	
	IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(DataSourceInfoOracle.dBType);

	@Override
	public void addIntArrayHead(StringBuilder sbCountEachBin) {
		sbCountEachBin.append(multiDBUtility.intArrayHead());

	}

	@Override
	public void addIntArrayTail(StringBuilder sbCountEachBin) {
		sbCountEachBin.append(multiDBUtility.floatArrayTail());
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, HashMap<Integer, Long>> dealResult(
			ArrayList<Column> analysisList, ResultSet rs,
			Map<String, Integer> binMap) throws SQLException {
		ArrayList<Double> countEachBin = new ArrayList<Double>();
		ResultSet resultSet =rs.getArray(1).getResultSet();
		while(resultSet.next()){
			countEachBin.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
		}
		int temp = 0;
		int temp1 = 0;
		HashMap<String, HashMap<Integer, Long>> countForEachColumn = new HashMap<String, HashMap<Integer, Long>>();
		HashMap<Integer, Long> countMap = new HashMap<Integer, Long>();
		for (int s = 0; s < countEachBin.size(); s++) {	
			countMap.put(temp + 1, (countEachBin.get(s).longValue()));
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
		sbCountEachBin.append(multiDBUtility.floatArrayHead());
	}

	@Override
	public void addFloatArrayTail(StringBuilder sbCountEachBin) {
		sbCountEachBin.append(multiDBUtility.floatArrayTail());
	}

	@Override
	public void dealResult(HistogramAnalysisResult histogramAnalysisResult,
			ArrayList<Column> analysisList, ResultSet rs,
			HashMap<String, BigDecimal[]> binMap) throws SQLException {
		ArrayList<Double> resultArray = new ArrayList<Double>();
		ResultSet resultSet = rs.getArray(1).getResultSet();
		while(resultSet.next()){
			resultArray.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
		}
		int m = 0;
		int resulttemp = 0;
		for (int s = 0; s < resultArray.size(); s = s + 4) {
			BigDecimal[] column_bins = binMap
					.get(analysisList.get(m).getName());
			BinHistogramAnalysisResult binHistogramAnalysisResult = new BinHistogramAnalysisResult();
			binHistogramAnalysisResult.setColumnName(analysisList.get(m).getName());
			binHistogramAnalysisResult.setBin((resulttemp + 1));
			binHistogramAnalysisResult
					.setBegin(column_bins[resulttemp].floatValue());
			binHistogramAnalysisResult
					.setEnd(column_bins[resulttemp + 1].floatValue());
			binHistogramAnalysisResult.setCount((resultArray.get(s).intValue()));
			binHistogramAnalysisResult.setPercentage((resultArray.get(s + 1).floatValue()));
			binHistogramAnalysisResult.setAccumCount((resultArray.get(s + 2).intValue()));
			binHistogramAnalysisResult
					.setAccumPercentage((resultArray.get(s + 3).floatValue()));
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
