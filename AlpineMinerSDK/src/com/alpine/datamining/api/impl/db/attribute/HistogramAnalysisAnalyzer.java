/**
 * ClassName HistogramAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.histogram.HistogramMultiDBUtility;
import com.alpine.datamining.api.impl.db.attribute.histogram.HistogramMultiDBUtilityFactory;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 *Eason
 */
public class HistogramAnalysisAnalyzer extends AbstractDBAttributeAnalyzer{
	private static Logger logger= Logger.getLogger(HistogramAnalysisAnalyzer.class);
	
	private HistogramMultiDBUtility histogramMultiDBUtility;
	
	private static BigDecimal THRESHOLD=new BigDecimal(AlpineMinerConfig.HISTOGRAM_ANALYSIS_THRESHOLD);
	private static BigDecimal ZERO=new BigDecimal(0);
	private static BigDecimal ONE=new BigDecimal(1);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		DataSet dataSet;

		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source, source
					.getAnalyticConfig());

			String tableName = ((DBTable) dataSet
					.getDBTable()).getTableName();

			String dbtype = source.getDataSourceType();
			
			histogramMultiDBUtility=HistogramMultiDBUtilityFactory.createHistogramMultiDBUtility(dbtype);
			
			HistogramAnalysisConfig config = (HistogramAnalysisConfig) source
					.getAnalyticConfig();

			Map<String, Integer> columnBinMap = getSpecifiedColumn(config);
			
			AnalysisColumnBinsModel columnBinsModel = config.getColumnBinModel();
			
			Map<String,AnalysisColumnBin> columnBinsMap=new HashMap<String,AnalysisColumnBin>();
			List<AnalysisColumnBin> columnBins = columnBinsModel.getColumnBins();
			for(AnalysisColumnBin columnBin:columnBins){
				columnBinsMap.put(columnBin.getColumnName(), columnBin);
			}

			HistogramAnalysisResult histogramAnalysisResult = new HistogramAnalysisResult(
					tableName);
		
			HashMap<String, HashMap<String, BigDecimal>> countminmaxForEachColumn = new HashMap<String, HashMap<String, BigDecimal>>();
			ArrayList<Column> analysisList = new ArrayList<Column>();
			
			caculateMinAndMax(dataSet,columnBinMap, countminmaxForEachColumn, analysisList,config.getLocale(),columnBinsMap);
			
			calculateIsContainData(config, countminmaxForEachColumn);
			
			HashMap<String, HashMap<Integer, Long>> countForEachColumn = caculateCountForEachBin(
						dataSet, columnBinMap, countminmaxForEachColumn, analysisList,columnBinsMap,config.getLocale());
			caculateHistogram(dataSet,columnBinMap, histogramAnalysisResult,
						countminmaxForEachColumn, analysisList, countForEachColumn,columnBinsMap);
								
			AnalyzerOutPutObject outPut = new AnalyzerOutPutObject(
					histogramAnalysisResult);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return outPut;
		} catch (Exception e) {
			logger.error(e);
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}

	}

	private void calculateIsContainData(HistogramAnalysisConfig config,
			HashMap<String, HashMap<String, BigDecimal>> countminmaxForEachColumn)
			throws AnalysisError {
		List<String> countNullList=new ArrayList<String>();
		Iterator<Entry<String, HashMap<String, BigDecimal>>> iter = countminmaxForEachColumn.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, HashMap<String, BigDecimal>> entry = iter.next();
			BigDecimal count = entry.getValue().get("count");
			if(count.compareTo(new BigDecimal(0))==0){
				countNullList.add(entry.getKey());
			}
		}
		if(countNullList.size()!=0){
			StringBuilder sb=new StringBuilder();
			for(String s:countNullList){
				sb.append(StringHandler.doubleQ(s)).append(",");
			}
			if(sb.length()>0){
				sb=sb.deleteCharAt(sb.length()-1);
			}
			throw new AnalysisError(this,AnalysisErrorName.HISTOGRAM_COUNT_NULL,config.getLocale(),sb.toString());
		}
	}


	private void caculateHistogram(DataSet dataSet,
			Map<String, Integer> columnBinMap,
			HistogramAnalysisResult histogramAnalysisResult,
			HashMap<String, HashMap<String, BigDecimal>> countminmaxForEachColumn,
			ArrayList<Column> analysisList,
			HashMap<String, HashMap<Integer, Long>> countForEachColumn,
			Map<String,AnalysisColumnBin> columnBinsMap)
			throws SQLException {
		Statement st;
		ResultSet rs;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		Iterator<Column> it = dataSet.getColumns().allColumns();
		StringBuilder sb_analysis = new StringBuilder("select ");
		histogramMultiDBUtility.addFloatArrayHead(sb_analysis);
		HashMap<String, BigDecimal[]> binMap = new HashMap<String, BigDecimal[]>();
		while (it.hasNext()) {
			Column att = it.next();
			String valueName = StringHandler.doubleQ(att.getName());
			if (!columnBinMap.containsKey(att.getName()))
				continue;
			if(!columnBinsMap.containsKey(att.getName())){
				continue;
			}
			AnalysisColumnBin columnBin = columnBinsMap.get(att.getName());
			int bin = columnBinMap.get(att.getName());
			
			BigDecimal min = countminmaxForEachColumn.get(att.getName()).get(
					"min");
			BigDecimal max = countminmaxForEachColumn.get(att.getName()).get(
					"max");

			if(columnBin.isMin()&&min.compareTo(new BigDecimal(columnBin.getMin()))==-1){
				min=new BigDecimal(columnBin.getMin());
			}
			if(columnBin.isMax()&&max.compareTo(new BigDecimal(columnBin.getMax()))==1){
				max=new BigDecimal(columnBin.getMax());
			}
			
			BigDecimal diff =null;
			if(columnBin.getType()==AnalysisColumnBin.TYPE_BY_NUMBER){
				diff = ((max.subtract(min)).divide(new BigDecimal(bin),5, BigDecimal.ROUND_HALF_EVEN));
			}else{
				diff = new BigDecimal(columnBin.getWidth());
				BigDecimal tempBin=((max.subtract(min)).divide(diff,5, BigDecimal.ROUND_HALF_EVEN));
				if(tempBin.remainder(ONE).compareTo(ZERO)==0){
					bin=tempBin.intValue();
				}else{
					bin=((int)Math.floor(tempBin.intValue()))+1;
				}
			}
			
			BigDecimal[] column_bins = new BigDecimal[bin + 1];
			
			long accumulateCount = 0;
			for (int j = 0; j < bin; j++) {
				column_bins[j] = min.add(diff.multiply(new BigDecimal(j)));
			}
			column_bins[bin] = max;
			binMap.put(att.getName(), column_bins);
			HashMap<Integer, Long> countMap = countForEachColumn.get(att
					.getName());		
			for (int j = 0; j < bin; j++) {
				float count=countminmaxForEachColumn.get(att.getName()).get("count").floatValue();
				generateSql(sb_analysis, valueName, bin, column_bins,
						j,columnBin.isMin(),columnBin.getMin(),columnBin.isMax(),columnBin.getMax());
				sb_analysis.append(",");
				generateSql(sb_analysis, valueName, bin, column_bins,
						j,columnBin.isMin(),columnBin.getMin(),columnBin.isMax(),columnBin.getMax());
				sb_analysis.append("/").append(count).append(",");
				generateSql(sb_analysis, valueName, bin, column_bins,
						j,columnBin.isMin(),columnBin.getMin(),columnBin.isMax(),columnBin.getMax());
				sb_analysis.append("+").append(accumulateCount)
						.append(",(");
				generateSql(sb_analysis, valueName, bin, column_bins,
						j,columnBin.isMin(),columnBin.getMin(),columnBin.isMax(),columnBin.getMax());
				sb_analysis.append("+").append(accumulateCount)
						.append(")/").append(count).append(",");
				accumulateCount = accumulateCount + countMap.get((j + 1));
			}
		}
		sb_analysis = sb_analysis.deleteCharAt(sb_analysis
				.length() - 1);
		histogramMultiDBUtility.addFloatArrayTail(sb_analysis);
		
		sb_analysis.append(" from ").append(
				tableName);

		st = databaseConnection.createStatement(false);
		logger.debug("HistogramAnalysis.doAnalysis():sql="
				+ sb_analysis.toString());
		rs = st.executeQuery(sb_analysis.toString());
		rs.next();
		
		histogramMultiDBUtility.dealResult(histogramAnalysisResult, analysisList, rs, binMap);
	}

	private HashMap<String, HashMap<Integer, Long>> caculateCountForEachBin(
			DataSet dataSet, Map<String, Integer> columnBinMap, 
			HashMap<String, HashMap<String, BigDecimal>> countminmaxForEachColumn,
			ArrayList<Column> analysisList, Map<String, AnalysisColumnBin> columnBinsMap, Locale locale) 
			throws SQLException, AnalysisError {
		Statement st;
		ResultSet rs;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		Iterator<Column> itt = dataSet.getColumns()
				.allColumns();
		StringBuilder sb_count_eachBin = new StringBuilder("select ");

		histogramMultiDBUtility.addIntArrayHead(sb_count_eachBin);

		Map<String,Integer> binMap=new HashMap<String,Integer>();
		while (itt.hasNext()) {
			Column att = itt.next();
			String valueName = StringHandler.doubleQ(att.getName());
			if (!columnBinMap.containsKey(att.getName()))
				continue;
			if (!columnBinsMap.containsKey(att.getName()))
				continue;
			AnalysisColumnBin columnBin=columnBinsMap.get(att.getName());
			int bin = columnBinMap.get(att.getName());

			BigDecimal min = countminmaxForEachColumn.get(att.getName()).get(
					"min");
			BigDecimal max = countminmaxForEachColumn.get(att.getName()).get(
					"max");
			
			if(columnBin.isMin()&&min.compareTo(new BigDecimal(columnBin.getMin()))==-1){
				min=new BigDecimal(columnBin.getMin());
			}
			if(columnBin.isMax()&&max.compareTo(new BigDecimal(columnBin.getMax()))==1){
				max=new BigDecimal(columnBin.getMax());
			}
			
			BigDecimal diff = null;
			if(columnBin.getType()==AnalysisColumnBin.TYPE_BY_NUMBER){
				diff = ((max.subtract(min)).divide(new BigDecimal(bin),5, BigDecimal.ROUND_HALF_EVEN));
			}else{
				diff = new BigDecimal(columnBin.getWidth());
				BigDecimal tempBin=((max.subtract(min)).divide(diff,5, BigDecimal.ROUND_HALF_EVEN));
				if(tempBin.compareTo(THRESHOLD)==0||
						tempBin.compareTo(THRESHOLD)==1){
					throw new AnalysisError(this, AnalysisErrorName.Exceed_MAX_Bin_NUMBER,locale,
							AlpineMinerConfig.HISTOGRAM_ANALYSIS_THRESHOLD,att.getName());//
				}
				if(tempBin.remainder(ONE).compareTo(ZERO)==0){
					bin=tempBin.intValue();
				}else{
					bin=((int)Math.floor(tempBin.intValue()))+1;
				}
			}

			binMap.put(att.getName(), bin);
			BigDecimal[] column_bins = new BigDecimal[bin + 1];
			
			for (int j = 0; j < bin; j++) {
				column_bins[j] = min.add(diff.multiply(new BigDecimal(j)));
			}
			column_bins[bin] = max;
			for (int j = 0; j < bin; j++) {
				generateSql(sb_count_eachBin, valueName, bin, column_bins,
						j,columnBin.isMin(),columnBin.getMin(),columnBin.isMax(),columnBin.getMax());
				sb_count_eachBin.append(",");
			}
		}
		sb_count_eachBin = sb_count_eachBin.deleteCharAt(sb_count_eachBin
				.length() - 1);

		histogramMultiDBUtility.addIntArrayTail(sb_count_eachBin);

		sb_count_eachBin.append(" from ").append(tableName);

		logger.debug("HistogramAnalysis.doAnalysis():sql="
				+ sb_count_eachBin.toString());
		st=databaseConnection.createStatement(false);
		rs = st.executeQuery(sb_count_eachBin.toString());
		rs.next();
		
		
		HashMap<String, HashMap<Integer, Long>> countForEachColumn = histogramMultiDBUtility.dealResult(
				analysisList, rs, binMap);
		
		return countForEachColumn;
	}

	private void caculateMinAndMax(DataSet dataSet,
			Map<String, Integer> columnBinMap,
			HashMap<String, HashMap<String, BigDecimal>> countminmaxForEachColumn,
			ArrayList<Column> analysisList, Locale locale, Map<String, AnalysisColumnBin> columnBinsMap) throws OperatorException,
			AnalysisException, SQLException {
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		
		Statement st=null;
		ResultSet rs=null;
		try {	
			st=databaseConnection.createStatement(false);
			
			Iterator<Column> i = dataSet.getColumns().allColumns();
			while (i.hasNext()) {
				Column att = i.next();
				String valueTypeName = StringHandler.doubleQ(att.getName());
				if (!columnBinMap.containsKey(att.getName()))
					continue;
				if (!columnBinsMap.containsKey(att.getName()))
					continue;
				AnalysisColumnBin columnBin = columnBinsMap.get(att.getName());
				if (!att.isNumerical()) {
					logger
							.error("Histogram Analyzer cannot accept non-numeric type column!");
						throw new AnalysisError(this,AnalysisErrorName.Non_numeric,locale,SDKLanguagePack.getMessage(SDKLanguagePack.HISTOGRAM_NAME,locale));
					 
				}
				analysisList.add(att);
				
				StringBuilder sql=new StringBuilder();
				sql.append("select count(").append(valueTypeName).append("),min(");
				sql.append(valueTypeName).append("),max(").append(valueTypeName);
				sql.append(") from ").append(tableName);
				if(columnBin.isMin()||columnBin.isMax()){
					sql.append(" where ");
					
					if(columnBin.isMin()&&!columnBin.isMax()){//only min
						sql.append(valueTypeName).append(" >  ");
						sql.append(columnBin.getMin());
					}else if(!columnBin.isMin()&&columnBin.isMax()){//only max
						sql.append(valueTypeName).append(" <=  ");
						sql.append(columnBin.getMax());
					}else{//max and min
						sql.append(valueTypeName).append(" >  ");
						sql.append(columnBin.getMin());
						sql.append(" and ");
						sql.append(valueTypeName).append(" <=  ");
						sql.append(columnBin.getMax());
					}
				}
			
				logger.debug("HistogramAnalysis.doAnalysis():sql="
						+ sql.toString());
				rs = st.executeQuery(sql.toString());
				
				rs.next();
				
				HashMap<String, BigDecimal> countminmax = new HashMap<String, BigDecimal>();
						
				countminmax.put("count", rs.getBigDecimal(1));
				countminmax.put("min", rs.getBigDecimal(2));
				countminmax.put("max", rs.getBigDecimal(3));
				
				countminmaxForEachColumn.put(att.getName(),
						countminmax);		
			}
			
		} catch (SQLException e) {
			logger.error(e);
			throw e;
		}finally{
			if(st!=null){
				st.close();
			}
			if(rs!=null){
				rs.close();
			}
		}
	}

	private void generateSql(StringBuilder sb_numeric_array, String valueName,
			int bin, BigDecimal[] column_bins, int j,boolean isMin,double min,boolean isMax,double max) {
		if(column_bins[j].scale()>10){
			column_bins[j]=new BigDecimal(column_bins[j].toString()).setScale(10,BigDecimal.ROUND_HALF_UP);
		}
		if(column_bins[j+1].scale()>10){
			column_bins[j+1]=new BigDecimal(column_bins[j+1].toString()).setScale(10,BigDecimal.ROUND_HALF_UP);
		}
		StringBuilder sb=new StringBuilder();
		if(isMin){
			sb.append(" and ").append(valueName).append(" > ").append(min);
		}
		if(isMax){
			sb.append(" and ").append(valueName).append(" <= ").append(max);
		}
		if(bin==1){
			sb_numeric_array.append("sum(case when").append(valueName).append(
			" >= ").append(column_bins[j]);
			sb_numeric_array.append(" and ").append(valueName).append(" <= ").append(column_bins[j + 1]).append(sb).append(
			" then 1 else 0 end)");
		}
		else if(j==0)
		{
			sb_numeric_array.append("sum(case when").append(valueName).append(" <= ").append(column_bins[j + 1]);
			sb_numeric_array.append(sb).append(" then 1 else 0 end)");
		}
		else if(j == (bin - 1))
		{
			sb_numeric_array.append("sum(case when").append(valueName).append(" > ").append(column_bins[j]);
			sb_numeric_array.append(sb).append(" then 1 else 0 end)");
		}
		else
		{
			sb_numeric_array.append("sum(case when").append(valueName).append(
			" > ").append(column_bins[j]);
			sb_numeric_array.append(" and ").append(valueName).append(" <= ").append(column_bins[j + 1]).append(sb).append(
			" then 1 else 0 end)");
		}
	}

	private Map<String, Integer> getSpecifiedColumn(
			HistogramAnalysisConfig config) throws OperatorException, AnalysisError {
		Map<String, Integer> columnBinMap = new HashMap<String, Integer>();
		AnalysisColumnBinsModel model = config.getColumnBinModel();
		
		if (model == null || model.getColumnBins() == null) {
			logger
					.error("Histogram Analyzer's columnNames and bin cannot null");
			throw new AnalysisError(this,AnalysisErrorName.Not_null,config.getLocale(),SDKLanguagePack.getMessage(SDKLanguagePack.HISTOGRAM_COLUMN,config.getLocale()));
		}

		for(AnalysisColumnBin columnBin:model.getColumnBins()){
			columnBinMap
			.put(columnBin.getColumnName(), columnBin.getBin());
		}
		return columnBinMap;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HISTOGRAM_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HISTOGRAM_DESCRIPTION,locale));

		return nodeMetaInfo;
	}

}
