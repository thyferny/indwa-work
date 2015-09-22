/**
* ClassName DBChiSquareIndexStandard.java
*
* Version information: 1.00
*
* Data: 3 Nov 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.operator.tree.cartclassification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.tree.cart.AbstractStandard;
import com.alpine.datamining.operator.tree.cart.Combination;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DBPortingFactory;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public class DBChiSquareIndexStandard extends AbstractStandard{
    private static final Logger itsLogger = Logger.getLogger(DBChiSquareIndexStandard.class);

    List<String> bestValues = null;
	double bestSplit = Double.NaN;
	
	private ICartClassfication cartdb;
 
	@Override
	public double getNominalStandard(DataSet dataSet, Column column)
			throws OperatorException {
		
			Column label = dataSet.getColumns().getLabel();
			if (label.getMapping().size() == 2)
			{
				double benefit= getNominalBenefit2Classes(dataSet, column);
				return benefit;
			}
			String labelName = StringHandler.doubleQ(label.getName());
			int numberOfLabels = label.getMapping().size();
			int numberOfValues = column.getMapping().size();

			double[][] weightCounts = null;
			double bestImpurityReduction = Double.NaN;

			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			String whereCondition = ((DBTable) dataSet
					.getDBTable()).getWhereCondition();
			String tableName = ((DBTable) dataSet
					.getDBTable()).getTableName();
	    	if (whereCondition == null || whereCondition.length() == 0)
	    	{
	    		whereCondition = "";
	    	}
	    	else
	    	{
	    		whereCondition = " where "+whereCondition;
	    	}
			int[] bestCombin = new int[numberOfValues];
			int[] combin = new int[numberOfValues];
			weightCounts = new double[2][numberOfLabels];
			for (int m = 1; m <= numberOfValues / 2; m++) {
				Combination.initCombin(combin, m, numberOfValues);

				calculte(dataSet, column, labelName, weightCounts,
						databaseConnection, tableName, whereCondition, combin, m);
				int position = 0;
				bestImpurityReduction = calculateBestBenefit(weightCounts,
						bestImpurityReduction, combin, bestCombin);
				while (true) {
					if (Combination.firstOne(combin) == (numberOfValues - m))
						break;
					position = Combination.firstOneZero(combin);
					Combination.swap(combin, position);
					Combination.ifNeedMove(combin, position);
					calculte(dataSet, column, labelName, weightCounts,
							databaseConnection, tableName, whereCondition, combin, m);
					bestImpurityReduction = calculateBestBenefit(weightCounts,
							bestImpurityReduction, combin, bestCombin);
				}
			}
			bestValues = new ArrayList<String>();
			for (int i = 0; i < bestCombin.length; i++) {
				if (bestCombin[i] != 1)
					continue;
				{
					bestValues.add(column.getMapping().mapIndex(i));
				}
			}
//			for (int i = 0; i < weightCounts.length; i++) {
//				double weight[] = weightCounts[i];
//				for (int j = 0; j < weight.length; j++) {
//				}
//			}
			return bestImpurityReduction;
			
		
	}

	public double getNominalBenefit2Classes(DataSet dataSet,
			Column column) throws OperatorException {
		Column label = dataSet.getColumns().getLabel();
		String labelName = StringHandler.doubleQ(label.getName());

		List<String> labelList = label.getMapping().getValues();
		String columnName = StringHandler.doubleQ(column.getName());
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String dBType=((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName();

		ICartClassfication cartClass=DBPortingFactory.createCartClassificationDB(dBType);
		String whereCondition = ((DBTable) dataSet
				.getDBTable()).getWhereCondition();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
    	if (whereCondition == null || whereCondition.length() == 0)
    	{
    		whereCondition = "";
    	}
    	else
    	{
    		whereCondition = " where "+whereCondition;
    	}
		Statement st = null;
		ResultSet rs = null;

		StringBuffer countSum = new StringBuffer();
		countSum.append("select ").append(columnName).append(
				",(sum(case when ").append(labelName);
		countSum.append("='").append(StringHandler.escQ(labelList.get(0))).append(
				"' then 1 else 0 end))*1.0 sum1,(sum(case when ");
		countSum.append(labelName).append("='").append(StringHandler.escQ(labelList.get(1)))
				.append("' then 1 else 0 end))*1.0 sum2 from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").append(
				columnName);

		StringBuffer countAllSum = new StringBuffer();
		countAllSum.append("select ").append(columnName).append(
				",sum1/(sum1+sum2) probability,");
		countAllSum.append("(sum(sum1) over(order by  sum1/(sum1+sum2), ")
				.append(columnName);
		countAllSum.append(
				")) sum1asc ,(sum(sum2) over(order by sum1/(sum1+sum2) ,")
				.append(columnName);
		countAllSum
				.append(
						")) sum2asc, (sum(sum1) over(order by  sum1/(sum1+sum2) desc, ")
				.append(columnName).append(" desc");
		countAllSum
				.append(
						") - sum1) sum1desc,(sum(sum2) over(order by  sum1/(sum1+sum2) desc , ")
				.append(columnName);
		countAllSum.append(" desc) -sum2)  sum2desc from (").append(countSum)
				.append(") foo");

		StringBuffer countProbability = new StringBuffer();
		cartClass.genarateProbability(columnName, countAllSum, countProbability);

		ArrayList<String> splitArray = new ArrayList<String>();
		double maxChiSquare = Double.NEGATIVE_INFINITY;
		try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug(
					"ChiSquareIndexCriterionDB.getNominalBenefit2Classes():sql="
							+ countProbability);
			rs = st.executeQuery(countProbability.toString());

			ArrayList<String> tempArray = new ArrayList<String>();

			while (rs.next()) {
				double chiSquare = rs.getDouble(3);
//				System.out.println(chiSquare);
				if (chiSquare > maxChiSquare) {
					maxChiSquare = chiSquare;
					if (tempArray.size() != 0) {
						splitArray.addAll(tempArray);
						tempArray.clear();
					}
					splitArray.add(rs.getString(1));
				} else {
					tempArray.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

		bestValues = splitArray;

		return maxChiSquare;
	}


	private double calculateBestBenefit(double[][] weightCounts,
			double bestImpurityReduction, int[] combin, int[] bestCombin) {
		double impurityReduction = getBenefit(weightCounts);
		if (Double.isNaN(bestImpurityReduction)
				|| impurityReduction > bestImpurityReduction) {
			bestImpurityReduction = impurityReduction;
			System.arraycopy(combin, 0, bestCombin, 0, combin.length);
		}
		return bestImpurityReduction;
	}
	
	private void calculte(DataSet dataSet, Column column,
			String labelName, double[][] weightCounts,
			DatabaseConnection databaseConnection, String tableName, String whereCondition,
			int[] combinArray, int oneCount) throws OperatorException {
		StringBuffer whereEqual;
		StringBuffer whereNon;
		whereEqual = new StringBuffer();
		whereNon = new StringBuffer();
		int labelIndex;

		for (int i = 0, addflag = 0, quitflag = 0; i < combinArray.length; i++) {
			if (quitflag == oneCount)
				break;
			if (combinArray[i] != 1)
				continue;
			String columnName = StringHandler.doubleQ(column.getName());
			whereEqual.append(columnName).append("=")
			.append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, StringHandler.escQ(column.getMapping().mapIndex(i))));
			whereNon.append(columnName).append("!=")
			.append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, StringHandler.escQ(column.getMapping().mapIndex(i))));
			if (addflag < oneCount - 1) {
				whereEqual.append(" or ");
				whereNon.append(" and ");
				addflag++;
			}
			quitflag++;

		}

		StringBuilder sql = new StringBuilder("select ");
		sql.append("sum(case when ").append(whereEqual).append(
				" then 1 else 0 end),");
		sql.append("sum(case when ").append(whereNon).append(
				" then 1 else 0 end),");
		sql.append(labelName).append(" from  ").append(tableName).append(" ").append(whereCondition).append("  group by ").append(labelName);

		long countEqual = 0;
		long countNon = 0;
		try {
			Statement st = databaseConnection.createStatement(false);
			itsLogger.debug(
					"ChiSquareSrandardDB.calculte():sql=" + sql);
			ResultSet rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				countEqual = rs.getLong(1);
				countNon = rs.getLong(2);
				String labelValue = rs.getString(3);
				labelIndex = dataSet.getColumns().getLabel().getMapping()
						.getIndex(labelValue);
				weightCounts[0][labelIndex] = countEqual;
				weightCounts[1][labelIndex] = countNon;
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	

	@Override
	public double getNumericalStandard(DataSet dataSet,
			Column column, double splitValue) throws OperatorException {

		cartdb=DBPortingFactory.createCartClassificationDB(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		
		Column labelColumn = dataSet.getColumns().getLabel();
		String labelColumnName = StringHandler.doubleQ(labelColumn
				.getName());
		if (labelColumn.getMapping().size() > Integer
				.parseInt(AlpineDataAnalysisConfig.TREE_LABEL_THRESHOLD)) {
			throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED, labelColumnName, Integer
					.parseInt(AlpineDataAnalysisConfig.TREE_LABEL_THRESHOLD));
		}
		String columnName = StringHandler.doubleQ(column
				.getName());

		double impurityReductionValue = Double.NaN;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String whereCondition = ((DBTable) dataSet
				.getDBTable()).getWhereCondition();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
    	if (whereCondition == null || whereCondition.length() == 0)
    	{
    		whereCondition = "";
    	}
    	else
    	{
    		whereCondition = " where "+whereCondition;
    	}
		Statement st = null;
		ResultSet rs = null;
//		StringBuilder sql = new StringBuilder("");

		
		double distinctRatio = 0.0;
		String dataSourceType = databaseConnection.getProperties().getName();
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dataSourceType);
		
		try {
			st = databaseConnection.createStatement(false);
			distinctRatio = multiDBUtility.getSampleDistinctRatio(st,tableName, columnName, whereCondition);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		itsLogger.debug("DBChiSquareIndexStandard.getNumericalStandard():distinctRatio:"+distinctRatio);
		
		String getNumericalBenefitSql = cartdb.genarateChiSquareSql(labelColumn, labelColumnName,
				columnName, whereCondition, tableName,
				distinctRatio);

		try {
			itsLogger.debug(
					"DBChiSquareIndexCriterion.getNumericalBenefit():sql=" + getNumericalBenefitSql);
			rs = st.executeQuery(getNumericalBenefitSql);
			boolean limitFlag=true;
			if (rs.next()&&limitFlag) {
				bestSplit = rs.getDouble(1);
				impurityReductionValue = rs.getDouble(2);
				limitFlag=false;
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return impurityReductionValue;

	}


	public double getBenefit(double[][] weightCounts) {
		// calculate information amount WITHOUT this column
		double[] rowCWeights = new double[weightCounts[0].length];
		double[] columnRWeights = new double[weightCounts.length];
		for (int l = 0; l < rowCWeights.length; l++) {
			for (int v = 0; v < weightCounts.length; v++) {
				rowCWeights[l] += weightCounts[v][l];
				columnRWeights[v] += weightCounts[v][l];
			}
			if(rowCWeights[l]==0)
			{
				return 0;
			}
		}
		for (int v = 0; v < weightCounts.length; v++) {
			
		if(columnRWeights[v]==0)
		{
			return 0;
		}
		}
		double totalClassWeight = getTotalNumber(rowCWeights);
		totalWeight = totalClassWeight;
		double chiSquareTestValue=0;
		for (int l = 0; l < rowCWeights.length; l++) {
			for (int v = 0; v < columnRWeights.length; v++) {
				double tempE=columnRWeights[v]*rowCWeights[l]/totalClassWeight;
				chiSquareTestValue+= Math.pow((weightCounts[v][l]-tempE),2)/tempE;
			}
		}
		
		return chiSquareTestValue;
	}

	/** Returns the sum of the given weights. */
	public double getTotalNumber(double[] weights) {
		double sum = 0.0d;
		for (double w : weights)
			sum += w;
		return sum;
	}


	public boolean supportsIncrementalCalculation() {
		return true;
	}



	public List<String> getBestValues() {
		return bestValues;
	}

	public void setBestValues(List<String> bestValues) {
		this.bestValues = bestValues;
	}

	public double getBestSplit() {
		return bestSplit;
	}

	public void setBestSplit(double bestSplit) {
		this.bestSplit = bestSplit;
	}

	
}
