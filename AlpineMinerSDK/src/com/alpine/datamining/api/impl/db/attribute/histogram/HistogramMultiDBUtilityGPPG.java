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
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;

public class HistogramMultiDBUtilityGPPG implements HistogramMultiDBUtility {

	IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(DataSourceInfoGreenplum.dBType);
	
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, HashMap<Integer, Long>> dealResult(
			ArrayList<Column> analysisList, ResultSet rs,
			Map<String, Integer> binMap) throws SQLException {
		Object[] count_eachBin =(Object[])rs.getArray(1).getArray();
		
		int temp = 0;
		int temp1 = 0;
		HashMap<String, HashMap<Integer, Long>> countForEachColumn = new HashMap<String, HashMap<Integer, Long>>();
		HashMap<Integer, Long> countMap = new HashMap<Integer, Long>();
		for (int s = 0; s < count_eachBin.length; s++) {	
			countMap.put(temp + 1, multiDBUtility.castArrayIntegerToLong(count_eachBin[s]));
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
	public void addIntArrayTail(StringBuilder sbCountEachBin) {
		sbCountEachBin.append(multiDBUtility.intArrayTail());
	}

	@Override
	public void addIntArrayHead(StringBuilder sbCountEachBin) {
		sbCountEachBin.append(multiDBUtility.floatArrayHead());
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
		Object[] resultArray = (Object[]) rs.getArray(1).getArray();
		int m = 0;
		int resulttemp = 0;
		for (int s = 0; s < resultArray.length; s = s + 4) {
			BigDecimal[] column_bins = binMap
					.get(analysisList.get(m).getName());
			BinHistogramAnalysisResult binHistogramAnalysisResult = new BinHistogramAnalysisResult();
			binHistogramAnalysisResult.setColumnName(analysisList.get(m).getName());
			binHistogramAnalysisResult.setBin((resulttemp + 1));
			binHistogramAnalysisResult
					.setBegin(column_bins[resulttemp].floatValue());
			binHistogramAnalysisResult
					.setEnd(column_bins[resulttemp + 1].floatValue());
			binHistogramAnalysisResult.setCount(multiDBUtility.castArrayToInt(resultArray[s]));
			binHistogramAnalysisResult.setPercentage(multiDBUtility.castArrayToFloat(resultArray[s + 1]));
			binHistogramAnalysisResult.setAccumCount(multiDBUtility.castArrayToFloat(resultArray[s + 2]));
			binHistogramAnalysisResult
					.setAccumPercentage(multiDBUtility.castArrayToFloat(resultArray[s + 3]));
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
