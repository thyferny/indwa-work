/**
 * ClassName InformationValueResult.java
 *
 * Version information: 1.00
 *
 * Data: 2011-1-4
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 * 
 */
public class InformationValueResult implements Serializable {
    private static Logger itsLogger= Logger.getLogger(InformationValueResult.class);

    /**
	 */
	private static final long serialVersionUID = 1L;
	private DataSet dataSet;
	protected int numberOfClasses;
	
	private int numberOfColumns;
	
	private String[] columnNames;

	private String[][] columnValues;

	protected long[][][] counts;

	protected double[][] weightOfEvidence;
	protected double[] informationValue;

	protected IDataSourceInfo dataSourceInfo = null;
	
	protected IMultiDBUtility multiDBUtility = null;

	private long[] classCounts;
	private String good;
	private static final int GOOD_INDEX = 0;
	private static final int BAD_INDEX = 1;
	private Statement st = null;

	public InformationValueResult(DataSet dataSet) {
		this.dataSet = dataSet;
	}
	public void calculateInformationValue()throws OperatorException{

		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();
		dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
		
		Column labelColumn = dataSet.getColumns().getLabel();
		numberOfClasses = 2;
		numberOfColumns = dataSet.getColumns().size();
		columnNames = new String[numberOfColumns];
		columnValues = new String[numberOfColumns][];
		informationValue = new double[numberOfColumns];
		weightOfEvidence  = new double[numberOfColumns][];
		classCounts = new long[numberOfClasses];
		labelColumn.getName();
		int columnIndex = 0;
		counts = new long[numberOfColumns][numberOfClasses][];
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

		for (Column column : dataSet.getColumns()) {
				columnNames[columnIndex] = column.getName();
				String columnName = StringHandler.doubleQ(column.getName());
				ArrayList<String> mapping = new ArrayList<String>();
				try {
					long count = multiDBUtility.getSampleDistinctCount(st, tableName,columnName,null);
					if(count>AlpineMinerConfig.INFORMATIONVALUE_THRESHOLD)
					{
						throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED,columnName ,AlpineMinerConfig.INFORMATIONVALUE_THRESHOLD);
					}
					String sql = "select distinct " + columnName + " from " + tableName
					+ " order by " + columnName + " desc";
					itsLogger.debug("InformationValueResult.calculateInformationValue():sql="+sql);
					ResultSet rs = st.executeQuery(sql);

					while (rs.next()) {
						String value = rs.getString(1);
						if(value != null){
							mapping.add(value);
						}
					}
					rs.close();
//					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}

				int mappingSize = mapping.size() + 1;
				columnValues[columnIndex] = new String[mappingSize];
				weightOfEvidence[columnIndex] = new double[mappingSize];
				for (int i = 0; i < mappingSize - 1; i++) {
					columnValues[columnIndex][i] = (String) mapping.get(i);
				}
				columnValues[columnIndex][mappingSize - 1] = "alpine_miner_null";
				for (int i = 0; i < numberOfClasses; i++) {
					counts[columnIndex][i] = new long[mappingSize];
				}
			columnIndex++;
		}
		updateCount();
		updateResult();
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
		||sourceType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
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
			itsLogger.debug(
					"InformationValueResult.getCountArrayGreenplum():sql=" + sql);
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
				itsLogger.debug("InformationValueResult.getCountArrayOracle():sql="+sql);
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
		String goodValue=StringHandler.escQ(good);
		if(label.isNominal()){
			goodValue = "'"+goodValue+"'";
		}
		ArrayList<String> countStringArray = new ArrayList<String>();
		for (int i = 0; i < numberOfClasses; i++)
		{
			int columnIndex = 0;
			for (Column column : dataSet.getColumns())
			{
				String name = StringHandler.doubleQ(column.getName());
				for (int j = 0; j < columnValues[columnIndex].length - 1; j++)
				{
					String nominalstr = columnValues[columnIndex][j];
					StringBuffer caseString = new StringBuffer();
					nominalstr=StringHandler.escQ(nominalstr);
					if(column.isNominal()){
						nominalstr = CommonUtility.quoteValue(dataSourceInfo.getDBType(),column,nominalstr);
					}
					caseString.append("sum( ").append("(case when ").append(name).append("=").append(nominalstr).append(" and ").append(labelName).append(i==0?"":"!").append("= ").append(goodValue).append(" then 1 else 0 end))");
					countStringArray.add(caseString.toString());
				}
				StringBuffer caseString = new StringBuffer();
				caseString.append("sum( ").append("(case when ").append(name).append(" is null and ").append(labelName).append(i==0?"":"!").append("= ").append(goodValue).append(" then 1 else 0 end))");
				countStringArray.add(caseString.toString());
				columnIndex++;
			}
		}
		for (int i = 0; i < numberOfClasses; i++)
		{
			countStringArray.add("sum(case when "+labelName+(i==0?"":"!")+"= "+goodValue+" then 1 else 0 end)");
		}
		return countStringArray;
	}
	public void updateResult(){
		for (int columnIndex = 0; columnIndex < dataSet.getColumns().size(); columnIndex++)
		{
			informationValue[columnIndex] = Double.NaN;
			for (int j = 0; j < columnValues[columnIndex].length; j++)
			{
				double goodCount = counts[columnIndex][GOOD_INDEX][j];
				double badCount = counts[columnIndex][BAD_INDEX][j];
				double goodDist = Double.NaN;
				double badDist = Double.NaN;
				if (classCounts[GOOD_INDEX] != 0 ){
					goodDist = 1.0*goodCount/classCounts[GOOD_INDEX];
				}
				if (classCounts[BAD_INDEX] != 0){
					badDist = 1.0*badCount/classCounts[BAD_INDEX];
				}
				if (goodDist != 0 && !Double.isNaN(goodDist) && badDist != 0 && !Double.isNaN(badDist)){
					weightOfEvidence[columnIndex][j] = Math.log(goodDist/badDist)*100;
					double iv = (goodDist - badDist) * weightOfEvidence[columnIndex][j]/100;
					if (Double.isNaN(informationValue[columnIndex])){
						informationValue[columnIndex] = iv;
					}else{
						informationValue[columnIndex] += iv;
					}
				}else{
					weightOfEvidence[columnIndex][j] = Double.NaN;
				}
			}
		}
	}
	public String toString(){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < dataSet.getColumns().size(); i++)
		{
			{
				buf.append("IV(").append(columnNames[i]).append("):").append(informationValue[i]).append(Tools.getLineSeparator());
				for (int j = 0; j < columnValues[i].length; j++)
				{
					buf.append("  WOE(").append(columnValues[i][j]).append("):").append(weightOfEvidence[i][j]).append(Tools.getLineSeparator());
				}
			}
		}
		return buf.toString();
	}

	public String[] getColumnNames() {
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

	public double[][] getWeightOfEvidence() {
		return weightOfEvidence;
	}

	public void setWeightOfEvidence(double[][] weightOfEvidence) {
		this.weightOfEvidence = weightOfEvidence;
	}

	public void setInformationValue(double[] informationValue) {
		this.informationValue = informationValue;
	}
	public String getGood() {
		return good;
	}
	public void setGood(String good) {
		this.good = good;
	}
	public double[] getInformationValue() {
		return informationValue;
	}
}
