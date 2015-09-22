/**
 * ClassName VariableSelectionAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-25
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.variableselection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.VariableSelectionConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class VariableSelectionAnalyzer extends AbstractDBAttributeAnalyzer {
	private static Logger logger= Logger.getLogger(VariableSelectionAnalyzer.class);
	private String scoreType = VARIABLE_SELECTION_SCORETYPE_INFO_GAIN;
//	private boolean transformed ;
//	private int DiscretizationType = 0;
	private DataSet dataSet;
	private int numberOfClasses;
	
	private int numberOfColumns;
	
	private String[] columnNames;

	private String[][] columnValues;
	private double[] columnMax;
	private double[] columnMin;
	private int[] columnBin;
	private double[] columnBinWidth;

	private long[][][] counts;
	private double[] scores;
	private double thresholdCategory;
	private double thresholdNumber;

	private IDataSourceInfo dataSourceInfo = null;
	private IMultiDBUtility multiDBUtility = null;
	
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN="Info gain";
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO="Info gain ratio";
	public static final String VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN="Transformed info gain";

	private double[] classCounts;
	private Statement st = null;
	private String tableName = null;
	public double[] calculateScore(Locale locale)throws OperatorException{
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();
		dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
		
		numberOfClasses = dataSet.getColumns().getLabel().getMapping().size();
		if( scoreType.equals(VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN) && numberOfClasses != 2){
			throw new OperatorException(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_TRANSFORM_LABEL,locale));
		}
		numberOfColumns = dataSet.getColumns().size();
		columnNames = new String[numberOfColumns];
		columnValues = new String[numberOfColumns][];
		columnMax = new double[numberOfColumns];
		columnMin = new double[numberOfColumns];
		columnBin =  new int[numberOfColumns];
		columnBinWidth = new double[numberOfColumns];

//		scores = new double[numberOfColumns];
		classCounts = new double[numberOfClasses];
		int columnIndex = 0;
		counts = new long[numberOfColumns][numberOfClasses][];
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		for (Column column : dataSet.getColumns()) {
				columnNames[columnIndex] = column.getName();
				String columnName = StringHandler.doubleQ(column.getName());
				ArrayList<String> mapping = new ArrayList<String>();
				int mappingSize = 0;// + 1;
				try {
					if (column.isNominal()){
						long count = multiDBUtility.getSampleDistinctCount(st, tableName,columnName,null);
						if(count>AlpineMinerConfig.VARIABLE_SELECTION_THRESHOLD)
						{
							throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED,columnName ,AlpineMinerConfig.VARIABLE_SELECTION_THRESHOLD);
						}
						String sql = "select distinct " + columnName + " from " + tableName
						+ " order by " + columnName + " desc";
						logger.debug("VariableSelectionResult.calculateScore():sql="+sql);
						ResultSet rs = st.executeQuery(sql);
	
						while (rs.next()) {
							String value = rs.getString(1);
							if(value != null){
								mapping.add(value);
							}
						}
						rs.close();
						mappingSize = mapping.size();
					}else{
						mappingSize = getDiscretizationNumber(column, columnIndex);
					}
//					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
				columnValues[columnIndex] = new String[mappingSize];
				for (int i = 0; i < mappingSize; i++){// - 1; i++) {
					if (column.isNominal()){
						columnValues[columnIndex][i] = (String) mapping.get(i);
					}else{
						columnValues[columnIndex][i] = String.valueOf(i);
					}
				}
//				columnValues[attributeIndex][mappingSize - 1] = "alpine_miner_null";
				for (int i = 0; i < numberOfClasses; i++) {
					counts[columnIndex][i] = new long[mappingSize];
				}
			columnIndex++;
		}
		updateCount();
		if(scoreType.equals(VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO)){
			getInfoGainRatioResult();
		}else if (scoreType.equals(VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN)){
			transform(locale);
			getInfoGainResult();
		}else{
			getInfoGainResult();
		}
		getChanceThreshold();
		return scores;
	}
	private void getChanceThreshold(){
		if(scoreType.equals(VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO)){
			thresholdCategory = getChanceThresholdCategoryInfoGainRatio();
			thresholdNumber = getChanceThresholdNumberInfoGainRatio();
		}else{
			thresholdCategory = getChanceThresholdCategoryInfoGain();
			thresholdNumber = getChanceThresholdNumberInfoGain();
		}

	}
	private double getChanceThresholdCategoryInfoGain(){
		double entropy = 0;
		double totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}

		double prior[] = new double[numberOfClasses];
		double[][] countAtt = new double[numberOfClasses][numberOfClasses];
		for(int i = 0 ; i < numberOfClasses; i++){
			prior[i] = classCounts[i]/totalCount;
		}
		for(int i = 0; i < numberOfClasses; i++){
			for(int j=0; j < numberOfClasses; j++){
				countAtt[i][j] = totalCount *prior[i]*prior[j] ;
			}
		}
		double conditionEntropyAttr = 0;
//		double countAttr = 0;
		double[] countAttrValue = new double[numberOfClasses];
		double[] entropyAttrValue = new double[numberOfClasses];
		for (int j = 0; j < numberOfClasses; j++)
		{
			for(int k = 0; k < numberOfClasses; k++){
				countAttrValue[j] += countAtt[k][j];
//				countAttr += countAtt[k][j];
			}
			for(int k = 0; k < numberOfClasses; k++){
				if(countAtt[k][j] != 0 && countAttrValue[j] != 0){
					entropyAttrValue[j] += - countAtt[k][j] * 1.0/countAttrValue[j] * Math.log(countAtt[k][j] * 1.0/countAttrValue[j] )/Math.log(2);
				}
			}
			conditionEntropyAttr += countAttrValue[j]/ totalCount* entropyAttrValue[j];
		}
		double infoGain = entropy - conditionEntropyAttr;

		return infoGain;
	}
	private double getChanceThresholdCategoryInfoGainRatio(){
		double entropy = 0;
		double totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}

		double prior[] = new double[numberOfClasses];
		double[][] countAtt = new double[numberOfClasses][numberOfClasses];

		for(int i = 0 ; i < numberOfClasses; i++){
			prior[i] = classCounts[i]/totalCount;
		}
		for(int i = 0; i < numberOfClasses; i++){
			for(int j=0; j < numberOfClasses; j++){
				countAtt[i][j] = totalCount *prior[i]*prior[j] ;
			}
		}
		double conditionEntropyAttr = 0;
		double countAttr = 0;
		double[] countAttrValue = new double[numberOfClasses];
		double[] entropyAttrValue = new double[numberOfClasses];
		for (int j = 0; j < numberOfClasses; j++)
		{
			for(int k = 0; k < numberOfClasses; k++){
				countAttrValue[j] += countAtt[k][j];
				countAttr += countAtt[k][j];
			}
			for(int k = 0; k < numberOfClasses; k++){
				if(countAtt[k][j] != 0 && countAttrValue[j] != 0){
					entropyAttrValue[j] += - countAtt[k][j] * 1.0/countAttrValue[j] * Math.log(countAtt[k][j] * 1.0/countAttrValue[j] )/Math.log(2);
				}
			}
			conditionEntropyAttr += countAttrValue[j]/ totalCount* entropyAttrValue[j];
		}
		double entropyAttr = 0;
		for (int j = 0; j < numberOfClasses; j++)
		{
			if(countAttrValue[j] != 0 && countAttr != 0){
				entropyAttr += countAttrValue[j]/countAttr*Math.log(countAttrValue[j]/countAttr)/Math.log(2);
			}
		}

		double infoGain = entropy - conditionEntropyAttr;

		double infoGainRatio = infoGain/entropyAttr;
		return infoGainRatio;
	}

	private double getChanceThresholdNumberInfoGainRatio(){
		double entropy = 0;
		double totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}

//		double threshold = 0;
		double stddev = Math.sqrt((totalCount + 1) * 1.0 / 12 /totalCount);
		double binwidth = 3.5*stddev/Math.pow(totalCount, 1.0/3);
		double max = (2 * totalCount - 1)/2.0/totalCount;
		double min = 1.0/2/totalCount;
		int bin = (int)Math.ceil((max - min)/binwidth);
		if (bin<=0){
			bin = 1;
		}

//		double binNumber = totalCount/bin;
		double counts[][] = new double[numberOfClasses][bin];
		double countAttr = 0;
		double[] countAttrValue = new double[bin];
		double[] entropyAttrValue = new double[bin];
		double conditionEntropyAttr = 0;
		double []prior = new double[numberOfClasses];
		for(int i = 0 ; i < numberOfClasses; i++){
			prior[i] = classCounts[i]/totalCount;
		}
		for(int i = 0; i < numberOfClasses; i++){
			for(int j=0; j < bin; j++){
				counts[i][j] = totalCount *1.0/bin*prior[i] ;
			}
		}

		for (int j = 0; j < bin; j++)
		{
			for(int k = 0; k < numberOfClasses; k++){
				countAttrValue[j] += counts[k][j];
				countAttr += counts[k][j];
			}
			for(int k = 0; k < numberOfClasses; k++){
				if(counts[k][j] != 0 && countAttrValue[j] != 0){
					entropyAttrValue[j] += - counts[k][j] * 1.0/countAttrValue[j] * Math.log(counts[k][j] * 1.0/countAttrValue[j] )/Math.log(2);
				}
			}
			conditionEntropyAttr += countAttrValue[j]/ totalCount* entropyAttrValue[j];
		}
		double infoGain = entropy - conditionEntropyAttr;
		double entropyAttr = 0;
		for (int j = 0; j < bin; j++)
		{
			if(countAttrValue[j] != 0 && countAttr != 0){
				entropyAttr += - countAttrValue[j]/countAttr*Math.log(countAttrValue[j]/countAttr)/Math.log(2);
			}
		}
		double infoGainRatio = infoGain/entropyAttr;
		return infoGainRatio;
	}
	private double getChanceThresholdNumberInfoGain(){
		double entropy = 0;
		double totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}
//		double threshold = 0;
//		int totalCount = 0;
		double stddev = Math.sqrt((totalCount + 1) * 1.0 / 12 /totalCount);
		double binwidth = 3.5*stddev/Math.pow(totalCount, 1.0/3);
		double max = (2 * totalCount - 1)/2.0/totalCount;
		double min = 1.0/2/totalCount;
		int bin = (int)Math.ceil((max - min)/binwidth);
		if (bin<=0){
			bin = 1;
		}

//		double binNumber = n/bin;
		double counts[][] = new double[numberOfClasses][bin];
//		double countAttr = 0;
		double[] countAttrValue = new double[bin];
		double[] entropyAttrValue = new double[bin];
		double conditionEntropyAttr = 0;
		double []prior = new double[numberOfClasses];
		for(int i = 0 ; i < numberOfClasses; i++){
			prior[i] = classCounts[i]/totalCount;
		}
		for(int i = 0; i < numberOfClasses; i++){
			for(int j=0; j < bin; j++){
				counts[i][j] = totalCount *1.0/bin*prior[i] ;
			}
		}

		for (int j = 0; j < bin; j++)
		{
			for(int k = 0; k < numberOfClasses; k++){
				countAttrValue[j] += counts[k][j];
//				countAttr += counts[k][j];
			}
			for(int k = 0; k < numberOfClasses; k++){
				if(counts[k][j] != 0 && countAttrValue[j] != 0){
					entropyAttrValue[j] += - counts[k][j] * 1.0/countAttrValue[j] * Math.log(counts[k][j] * 1.0/countAttrValue[j] )/Math.log(2);
				}
			}
			conditionEntropyAttr += countAttrValue[j]/ totalCount* entropyAttrValue[j];
		}
		double infoGain = entropy - conditionEntropyAttr;
		return infoGain;
	}
	private int getDiscretizationNumber(Column column, int index) throws SQLException {
		if (column.getValueType() == DataType.INTEGER){
//		if (DiscretizationType == 1){
			return getDiscretizationNumberSturgesRule(column, index);
		}else{
			return getDiscretizationNumberScottRule(column, index);
		}
	}
	private int getDiscretizationNumberSturgesRule(Column column, int index) throws SQLException{
		// CHECK AT FIRST
//		if (attribute.getValueType() == )
		int bin = 1;
		String columnName = StringHandler.doubleQ(column.getName());
		String sql = "select count("+columnName+"), max("+columnName+"),min("+columnName+") from "+tableName;
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()){
			int count = rs.getInt(1);
			double max = rs.getDouble(2);
			double min = rs.getDouble(3);
			bin = (int) Math.ceil(Math.log(count)/Math.log(10) + 1);//???
			double binWidth = (max - min)/bin;
			columnMax[index] = max;
			columnMin[index] = min;
			columnBin[index] =  bin;
			columnBinWidth[index] = binWidth;

		}
		return bin;
	}
	private int getDiscretizationNumberScottRule(Column column, int index) throws SQLException{
		int bin = 1;
		String columnName = StringHandler.doubleQ(column.getName());
		StringBuilder sb=new StringBuilder();
		if(dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)){
			sb.append("select count(").append(columnName).append("), max(").append(columnName).append("),min(").append(columnName);
			sb.append("),stddev(").append(columnName).append(")*sqrt(count(").append(columnName);
			sb.append("))/sqrt(count(").append(columnName).append(")-1) from ").append(tableName);
		}else{
			sb.append("select count(").append(columnName).append("), max(").append(columnName).append("),min(").append(columnName);
			sb.append("),stddev(").append(columnName).append(") from ").append(tableName);
		}
		ResultSet rs = st.executeQuery(sb.toString());
		while (rs.next()){
			int count = rs.getInt(1);
			double stddev = rs.getDouble(4);
			double max = rs.getDouble(2);
			double min = rs.getDouble(3);
			double binWidth = 3.5*stddev/Math.pow(count, 1.0/3);
			bin = (int)Math.ceil((max - min)/binWidth);
			columnMax[index] = max;
			columnMin[index] = min;
			columnBin[index] =  bin;
			columnBinWidth[index] = binWidth;
		}		
		return bin;
	}
	public void updateCount() throws OperatorException {
		String tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();
		Column label =dataSet.getColumns().getLabel();
		ArrayList<String> countStringArray = getCountStringArray(label);
		String sourceType = ((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName();;
		Long [] result = getCountArray(sourceType,countStringArray,tableName);
		int allColumnIndex = 0;
		for (int classIndex = 0; classIndex < numberOfClasses; classIndex++) {
			for (int columnIndex = 0; columnIndex < dataSet
					.getColumns().size(); columnIndex++) {
				for (int valueIndex = 0; valueIndex < columnValues[columnIndex].length; valueIndex++) {
					counts[columnIndex][classIndex][valueIndex] = result[allColumnIndex];
					allColumnIndex++;
				}
			}
		}
		for (int i = 0; i < numberOfClasses; i++) {
			classCounts[i] = result[allColumnIndex];
			allColumnIndex++;
		}
	}
	private Long[] getCountArray(String sourceType, ArrayList<String> countStringArray, String tableName)throws OperatorException{
		if (sourceType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)
		 || sourceType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return getCountArrayGreenplum(countStringArray, tableName);
		}else{
			return getCountArrayOracle(countStringArray, tableName);
		}
	}

	private Long[] getCountArrayGreenplum(ArrayList<String> countStringArray, String tableName)throws OperatorException{
		 Long[] result = null;
		 StringBuffer sql = new StringBuffer("select array[");
		 for(int i = 0; i < countStringArray.size(); i++){
			 if(i != 0){
				 sql.append(",");
			 }
			 sql.append(countStringArray.get(i));
		 }
		sql.append("] from ").append(tableName);
		try {
			logger.debug(
					"VariableSelectionResult.getCountArrayGreenplum():sql=" + sql);
			ResultSet rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				result = (Long[]) rs.getArray(1).getArray();
			}
			rs.close();
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
		return result;
	}
	private Long[] getCountArrayOracle(ArrayList<String> countStringArray, String tableName)throws OperatorException{
		Long[] result = new Long[countStringArray.size()];
		int eachCount = 1000;
		int cycle = countStringArray.size()/eachCount;
		if (countStringArray.size()%eachCount != 0){
			cycle += 1;
		}
		for(int i = 0; i < cycle; i++){
			StringBuffer sql = new StringBuffer("select ");
			int start = i*eachCount;
			int end = 0;
			if(i == cycle - 1){
				end = countStringArray.size();
			}else{
				end = (i + 1) * eachCount;
			}
			for(int j = start; j < end; j++){
				if(j != start){
					sql.append(",");
				}
				sql.append(countStringArray.get(j));
			}
			sql.append(" from ").append(tableName);

			try{
				logger.debug("VariableSelectionResult.getCountArrayOracle():sql="+sql);
				ResultSet rs = st.executeQuery(sql.toString());
				if(rs.next()){
					for(int k = 0; k < end - start; k++){
						result[start + k] = rs.getLong(k + 1);
					}
				}
				rs.close();
			}catch(SQLException e){
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		return result;
	}

	private ArrayList<String> getCountStringArray(
			Column label) {
		String labelName = StringHandler.doubleQ(label.getName());
		ArrayList<String> countStringArray = new ArrayList<String>();
		for (int i = 0; i < numberOfClasses; i++)
		{
			String value=StringHandler.escQ(label.getMapping().mapIndex(i));
			if(label.isNominal()){
				value = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,
						value);
			}
			int columnIndex = 0;
			for (Column column : dataSet.getColumns())
			{
				String name = StringHandler.doubleQ(column.getName());
				if (column.isNominal()){
					for (int j = 0; j < columnValues[columnIndex].length; j++)
					{
						String nominalstr = columnValues[columnIndex][j];
						StringBuffer caseStringTemp = new StringBuffer();
						nominalstr=StringHandler.escQ(nominalstr);
						if(column.isNominal()){
							nominalstr = CommonUtility.quoteValue(dataSourceInfo.getDBType(),column,nominalstr);
						}
						caseStringTemp.append("sum( ").append("(case when ").append(name).append("=").append(nominalstr).append(" and ").append(labelName).append("= ").append(value).append(" then 1 else 0 end))");
						countStringArray.add(caseStringTemp.toString());
					}
				}else{
					for (int j = 0; j < columnValues[columnIndex].length ; j++)
					{
						StringBuffer caseStringTemp = new StringBuffer();
						if (j == 0){
							if (j == columnValues[columnIndex].length - 1){
								caseStringTemp.append("sum(1)");
							}else{
								double max = columnMin[columnIndex] + (j + 1) * columnBinWidth[columnIndex];
								caseStringTemp.append("sum( ").append("(case when ").append(name).append(" < ").append(max).append(" and ").append(labelName).append(" = ").append(value).append(" then 1 else 0 end))");
							}
						}else if ( j == columnValues[columnIndex].length - 1){
							double min = columnMin[columnIndex] + j * columnBinWidth[columnIndex];
							caseStringTemp.append("sum( ").append("(case when ").append(name).append(" >= ").append(min).append(" and ").append(labelName).append(" = ").append(value).append(" then 1 else 0 end))");

						}else{
							double min = columnMin[columnIndex] + j * columnBinWidth[columnIndex];
							double max = columnMin[columnIndex] + (j + 1) * columnBinWidth[columnIndex];
							caseStringTemp.append("sum( ").append("(case when ").append(name).append(" >= ").append(min).append(" and ").append(name).append(" < ").append(max).append(" and ").append(labelName).append(" = ").append(value).append(" then 1 else 0 end))");
						}
//						caseStringTemp.append("sum( ").append("(case when ").append(name).append("=").append(nominalstr).append(" and ").append(labelName).append("= ").append(value).append(" then 1 else 0 end))");
						countStringArray.add(caseStringTemp.toString());
					}
//					caseString.append("sum( ").append("(case when ").append(name).append(" is null and ").append(labelName).append("= ").append(value).append(" then 1 else 0 end))");
				}
//				countStringArray.add(caseString.toString());
				columnIndex++;
			}
		}
		for (int i = 0; i < numberOfClasses; i++)
		{
			String value=StringHandler.escQ(label.getMapping().mapIndex(i));
			if(label.isNominal()){
				value = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,
						value);
			}
			countStringArray.add("sum(case when "+labelName+"= "+value+" then 1 else 0 end)");
		}
		return countStringArray;
	}
	public void getInfoGainResult(){
		double entropy = 0;
		int totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}
		double conditionEntropyAttr[] = new double [dataSet.getColumns().size()];
		double infoGain[] = new double [dataSet.getColumns().size()];
		for (int columnIndex = 0; columnIndex < dataSet.getColumns().size(); columnIndex++)
		{
			double countAttr = 0;
			double[] countAttrValue = new double[columnValues[columnIndex].length];
			double[] entropyAttrValue = new double[columnValues[columnIndex].length];
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{
				for(int k = 0; k < numberOfClasses; k++){
					countAttrValue[j] += counts[columnIndex][k][j];
					countAttr += counts[columnIndex][k][j];
				}
				for(int k = 0; k < numberOfClasses; k++){
					if(counts[columnIndex][k][j] != 0 && countAttrValue[j] != 0){
						entropyAttrValue[j] += - counts[columnIndex][k][j] * 1.0/countAttrValue[j] * Math.log(counts[columnIndex][k][j] * 1.0/countAttrValue[j] )/Math.log(2);
					}
				}
				conditionEntropyAttr[columnIndex] += countAttrValue[j]/ totalCount* entropyAttrValue[j];
			}
			infoGain[columnIndex] = entropy - conditionEntropyAttr[columnIndex];
			scores = infoGain;
		}
	}
	public void getInfoGainRatioResult(){
		double entropy = 0;
		int totalCount = 0;
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			if(classCounts[i] != 0 && totalCount != 0){
				entropy += - classCounts[i] * 1.0/totalCount * Math.log(classCounts[i] * 1.0/totalCount)/Math.log(2);
			}
		}
		double conditionEntropyAttr[] = new double [dataSet.getColumns().size()];
		double entropyAttr[] = new double [dataSet.getColumns().size()];
		double infoGain[] = new double [dataSet.getColumns().size()];
		double infoGainRatio[] = new double [dataSet.getColumns().size()];
		for (int columnIndex = 0; columnIndex < dataSet.getColumns().size(); columnIndex++)
		{
			double countAttr = 0;
			double[] countAttrValue = new double[columnValues[columnIndex].length];
			double[] entropyAttrValue = new double[columnValues[columnIndex].length];
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{
				for(int k = 0; k < numberOfClasses; k++){
					countAttrValue[j] += counts[columnIndex][k][j];
					countAttr += counts[columnIndex][k][j];
				}
				for(int k = 0; k < numberOfClasses; k++){
					if(counts[columnIndex][k][j] != 0 && countAttrValue[j] != 0){
						entropyAttrValue[j] += - counts[columnIndex][k][j] * 1.0/countAttrValue[j] * Math.log(counts[columnIndex][k][j] * 1.0/countAttrValue[j] )/Math.log(2);
					}
				}
				conditionEntropyAttr[columnIndex] += countAttrValue[j]/ totalCount* entropyAttrValue[j];
			}
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{
				if(countAttrValue[j] != 0 && countAttr != 0){
					entropyAttr[columnIndex] += - countAttrValue[j]/countAttr*Math.log(countAttrValue[j]/countAttr)/Math.log(2);
				}
			}
			infoGain[columnIndex] = entropy - conditionEntropyAttr[columnIndex];
			infoGainRatio[columnIndex] = infoGain[columnIndex]/entropyAttr[columnIndex];
			scores = infoGainRatio;
		}
	}
	public void transform(Locale locale) throws OperatorException{
		if(numberOfClasses != 2){
			throw new OperatorException(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_TRANSFORM_LABEL,locale));
		}
		int totalCount = 0;
		double[] classProb = new double[numberOfClasses];
		for(int i = 0; i < numberOfClasses; i++){
			totalCount += classCounts[i];
		}
		for(int i = 0; i < numberOfClasses; i++){
			classProb[i] = classCounts[i]/totalCount;
		}
//		double probAttr[] = new double [dataSet.getcolumns().size()];
//		double entropyAttr[] = new double [dataSet.getcolumns().size()];
//		double infoGain[] = new double [dataSet.getcolumns().size()];

		for (int columnIndex = 0; columnIndex < dataSet.getColumns().size(); columnIndex++)
		{
			boolean[] columnProb = new boolean[columnValues[columnIndex].length];
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{

				double columnCount = 0;
				double columnProbTrue = 0;
				for(int k = 0; k < numberOfClasses; k++){
					columnCount += counts[columnIndex][k][j];
				}
				columnProbTrue = counts[columnIndex][0][j]/columnCount;
				if(columnProbTrue > classProb[0]){
					columnProb[j] = true;
				}else{
					columnProb[j] = false;
				}
			}
			long [] countsNewTrue = new long[2]; 
			long [] countsNewFalse = new long[2];
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{
				if(columnProb[j]){
					countsNewTrue[0] += counts[columnIndex][0][j];
					countsNewFalse[0] += counts[columnIndex][1][j];
				}else{
					countsNewTrue[1] += counts[columnIndex][0][j];
					countsNewFalse[1] += counts[columnIndex][1][j];
				}

			}
			columnValues[columnIndex] = new String[2];
			columnValues[columnIndex][0] = "true";
			columnValues[columnIndex][1] = "false";
			counts[columnIndex][0] = countsNewTrue;
			counts[columnIndex][1] = countsNewFalse;
		}
	}


	public String[] getAttributeNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public String[][] getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(String[][] columnValues) {
		this.columnValues = columnValues;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}
	public double[] getScores() {
		return scores;
	}
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
//		dataSet = null;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());
			dataSet.computeColumnStatistics(dataSet.getColumns().getLabel());
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
		VariableSelectionConfig config=(VariableSelectionConfig)source.getAnalyticConfig();
		setSpecifyColumn(dataSet, config);

		scoreType = config.getScoreType();
		double[] scores = null;
		try {
			scores = calculateScore(config.getLocale());
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} else
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
		VariableSelectionResult variableSelectionResult = new VariableSelectionResult(columnNames, scores,thresholdCategory,thresholdNumber);
		AnalyzerOutPutObject outPut= new AnalyzerOutPutObject(variableSelectionResult);
		outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return outPut;
	}
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((VariableSelectionConfig)config).getDependentColumn());
	}
	protected void setNumericalLabelCategory(Column label){
		if(label.isNumerical())
		{
			((NumericColumn)label).setCategory(true);
		}
	}

    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_DESCRIPTION,locale));

        return nodeMetaInfo;
	}

}
