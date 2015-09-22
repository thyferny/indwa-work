/**
* ClassName WOEDataSQL.java
*
* Version information: 1.00
*
* Data: 7 Nov 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.attribute.woe;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public abstract class WOEDataSQL {
    private static final Logger logger = Logger.getLogger(WOEDataSQL.class);

    protected static String preString="WOE";
	private static double insteadZero=0.05;
	protected static String DBType=null;
	
	public static String addPre(String input)
	{
		input=preString+"("+input+")";
		
		return input;
		
		
	}
	
	
	/**
	 * @param dependColumn
	 * @param goodValue
	 * @param sqlBuffer
	 */
	public abstract void generalPreComputeWOESQL(String dependColumn, String goodValue,
			StringBuffer sqlBuffer) ;
	public abstract void generalAfterComputeWOESQL(String dependColumn, String goodValue,
			StringBuffer sqlBuffer) ;
	public abstract void generalTotalSQL(StringBuffer sumFrequencySQL,String dependColumn,String goodValue);
	
	
	/**
	 * @param analyticSource
	 * @param inputSchema
	 * @param tableName
	 * @param outputSchema
	 * @param outputTableName
	 * @param dropIfExist
	 * @param tableWOEInfor
	 * @param conncetion
	 * @param dataSet
	 * @return
	 * @throws AnalysisException
	 * @throws OperatorException
	 * @throws SQLException
	 */
	public abstract AnalysisWOETable createTable(AnalyticSource analyticSource,
			 String inputSchema,
			String tableName, String outputSchema, String outputTableName,
			String dropIfExist, AnalysisWOETable tableWOEInfor,
			Connection conncetion, DataSet dataSet, String appendOnlyString, String endingString) throws AnalysisException,
			OperatorException, SQLException ;
	
	
	protected void creatNominalSQL(StringBuffer sqlBuffer, AnalysisWOEColumnInfo woeInfor,
			Column oneColumn) {
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		if (woeInforList.isEmpty())
		{
			sqlBuffer.append("  0.0 ");
			sqlBuffer.append("  ").append(StringHandler.doubleQ(addPre(oneColumn.getName()))).append(" ");
			return;
		}
		sqlBuffer.append("  ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			List<String> columnValues = ((AnalysisWOENominalNode) tempNode)
					.getChoosedList();
			StringBuffer columnSet = new StringBuffer();
			columnSet.append("(");
			for (String tempString : columnValues) {
				columnSet.append(CommonUtility.quoteValue(DBType, oneColumn, tempString));
				columnSet.append(",");
			}
			columnSet.deleteCharAt(columnSet.length() - 1);
			columnSet.append(")");
			String groupInfor = ((AnalysisWOENominalNode) tempNode).getGroupInfror();
			sqlBuffer.append(" when ").append(StringHandler.doubleQ(oneColumn.getName()))
					.append("  in ");
			sqlBuffer.append(columnSet);
			sqlBuffer.append(" then ").append(woeInfor.getWOEValue(groupInfor));
		}
		sqlBuffer.append(" else 0.0 end ) ").append(StringHandler.doubleQ(addPre(oneColumn.getName()))).append(" ");
	}

	/**
	 * @param sqlBuffer
	 * @param woeInfor
	 * @param tempColumn
	 */
	protected void createNumbernicSQL(StringBuffer sqlBuffer,
			AnalysisWOEColumnInfo woeInfor, Column tempColumn) {
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		if (woeInforList.isEmpty())
		{
			sqlBuffer.append("  0.0 ");
			sqlBuffer.append("  ").append(StringHandler.doubleQ(addPre(tempColumn.getName()))).append(" ");
			return;
		}
		sqlBuffer.append("  ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			double upper = ((AnalysisWOENumericNode) tempNode).getUpper();
			double bottom = ((AnalysisWOENumericNode) tempNode).getBottom();
			String groupInfor = ((AnalysisWOENumericNode) tempNode).getGroupInfror();
			if(upper==Double.POSITIVE_INFINITY&&bottom==Double.NEGATIVE_INFINITY)
			{	sqlBuffer.append(" when '").append(groupInfor).append("'").append(" = ").append("'").append(
					groupInfor ).append("'").append(" then  ").append(woeInfor.getWOEValue(groupInfor)) ;
			}
			else if(upper==Double.POSITIVE_INFINITY)
			{	sqlBuffer.append(" when").append(
						StringHandler.doubleQ(tempColumn.getName())).append(" >").append(
								bottom).append(" then  ").append(woeInfor.getWOEValue(groupInfor));
			}
			else if(bottom==Double.NEGATIVE_INFINITY)
			{
				sqlBuffer.append(" when ").append(
						StringHandler.doubleQ(tempColumn.getName())).append("<= ").append(
								upper).append(" then  ").append(woeInfor.getWOEValue(groupInfor)) ;
			}	
			else
			{
				sqlBuffer.append(" when ").append(
					StringHandler.doubleQ(tempColumn.getName())).append(" >").append(
					bottom).append(" and ").append(
					StringHandler.doubleQ(tempColumn.getName())).append("<= ").append(
					upper).append(" then  ").append(woeInfor.getWOEValue(groupInfor)) ;
			}
		}
		
		sqlBuffer.append(" end ) ").append(StringHandler.doubleQ(addPre(tempColumn.getName()))).append(" ");
	}
	
	
	public void computeWOE(AnalysisWOEColumnInfo woeInfor,
			AnalyticSource analyticSource, Column columnName,
			boolean isNumbernic) throws OperatorException {
		
		WeightOfEvidenceConfig woeConfig = (WeightOfEvidenceConfig) analyticSource
				.getAnalyticConfig();
		String dependColumn = woeConfig.getDependentColumn();
		String goodValue = woeConfig.getGoodValue();
		String inputSchema = ((DataBaseAnalyticSource) analyticSource)
				.getTableInfo().getSchema();
		String tableName = ((DataBaseAnalyticSource) analyticSource)
				.getTableInfo().getTableName();
		Connection conncetion = ((DataBaseAnalyticSource) analyticSource)
				.getConnection();
		Statement st=null;
		ResultSet rs = null;
		try {
			
			double totalGoodNumber = 0, totalBadNumber = 0;
			double tempGoodNumber = 0, tempBadNumber = 0;
			double gini = 0;
			double inforValue = 0;
			String groupInfor = new String();
			st = conncetion.createStatement();
			StringBuffer sumFrequencySQL = new StringBuffer();
			generalTotalSQL(sumFrequencySQL, dependColumn, goodValue);
			sumFrequencySQL.append(" from ").append(
					StringHandler.doubleQ(inputSchema)).append(".").append(
					StringHandler.doubleQ(tableName));

			
			logger.debug(sumFrequencySQL.toString());
			rs = st.executeQuery(sumFrequencySQL.toString());
			while (rs.next()) {
				totalGoodNumber = rs.getDouble(1);
				totalBadNumber = rs.getDouble(2);
				if (totalGoodNumber == 0) {
					totalGoodNumber =insteadZero;
				}
				if (totalBadNumber == 0) {
					totalBadNumber = insteadZero;
				}
			}
			double WOE;
			StringBuffer sqlBuffer = new StringBuffer();

			generalPreComputeWOESQL(dependColumn, goodValue, sqlBuffer);
			if (isNumbernic == true) {
				genatrateNumbernicSQL(sqlBuffer, woeInfor, columnName);
			} else {
				genatrateNominalSQL(sqlBuffer, woeInfor, columnName);
			}
			generalAfterComputeWOESQL(inputSchema, tableName, sqlBuffer);

			logger.debug("WOEDataSQL.computeWOE():sql="+sqlBuffer.toString());

			st = conncetion.createStatement();
			rs = st.executeQuery(sqlBuffer.toString());
			while (rs.next()) {
				tempGoodNumber = rs.getDouble(1);
				tempBadNumber = rs.getDouble(2);
				groupInfor = rs.getString(3);
				if (tempGoodNumber == 0) {
					tempGoodNumber =insteadZero;
				}
				if (tempBadNumber == 0) {
					tempBadNumber = insteadZero;
				}
				WOE = Math.log((tempGoodNumber / totalGoodNumber)
						/ (tempBadNumber / totalBadNumber));
				woeInfor.setWOEValue(groupInfor, WOE);

				inforValue += ((tempGoodNumber / totalGoodNumber) - (tempBadNumber / totalBadNumber))
						* WOE;
				gini += (1 - Math.pow(
						(tempGoodNumber / (tempGoodNumber + tempBadNumber)), 2) - Math
						.pow(
								(tempBadNumber / (tempGoodNumber + tempBadNumber)),
								2))
						* (tempGoodNumber + tempBadNumber)
						/ (totalGoodNumber + totalBadNumber);
			}

			woeInfor.setGini(gini);
			woeInfor.setInforValue(inforValue);

		} catch (SQLException e) {
			logger.debug(e.toString());
			throw new OperatorException(e.toString());
		}finally{
			try {
				if(st != null)
				{
					st.close();
				}
				if(rs!=null)
				{
					rs.close();
				}
			} catch (SQLException e) {
				logger.debug(e.toString());
			throw new OperatorException(e.toString());
			}
		}

	}
	
	private static void genatrateNominalSQL(StringBuffer sqlBuffer,
			AnalysisWOEColumnInfo woeInfor, Column columnName) 
	{
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		if (woeInforList.isEmpty())
		{
			sqlBuffer.append(" , 'group0'  \"AlpineWOEGroup\"");
			return;
		}
		sqlBuffer.append(" , ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			List<String> columnValues = ((AnalysisWOENominalNode) tempNode)
					.getChoosedList();
			StringBuffer columnSet = new StringBuffer();
			columnSet.append("(");
			for (String tempString : columnValues) {
				columnSet.append(CommonUtility.quoteValue(DBType, columnName, tempString));
				columnSet.append(",");
			}
			columnSet.deleteCharAt(columnSet.length() - 1);
			columnSet.append(")");
			String groupInfor = ((AnalysisWOENominalNode) tempNode).getGroupInfror();
			
			
			
			sqlBuffer.append(" when ").append(
					StringHandler.doubleQ(columnName.getName())).append(" in ");
			sqlBuffer.append(columnSet);
			sqlBuffer.append(" then '").append(groupInfor).append("'");
		}
		sqlBuffer.append(" else 'group0' end ) \"AlpineWOEGroup\"");
	}



	/**
	 * @param sqlBuffer
	 * @param woeInfor
	 * @param columnName
	 */
	private static void genatrateNumbernicSQL(StringBuffer sqlBuffer,
			AnalysisWOEColumnInfo woeInfor, Column columnName) {
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		if (woeInforList.isEmpty())
		{
			sqlBuffer.append(" , 'group0'  \"AlpineWOEGroup\"");
			return;
		}
		sqlBuffer.append(" , ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			double upper = ((AnalysisWOENumericNode) tempNode).getUpper();
			double bottom = ((AnalysisWOENumericNode) tempNode).getBottom();
			String groupInfor = ((AnalysisWOENumericNode) tempNode).getGroupInfror();
			sqlBuffer.append(" when ");
			if(upper==Double.POSITIVE_INFINITY&&bottom==Double.NEGATIVE_INFINITY)
			{	sqlBuffer.append("'").append(groupInfor).append("'").append(" = ").append("'").append(
					groupInfor ).append("'").append(" then '").append(groupInfor).append("'");
			}
				
			
			else if(upper==Double.POSITIVE_INFINITY)
			{	sqlBuffer.append(
						StringHandler.doubleQ(columnName.getName())).append(" >").append(
								bottom).append(" then '").append(groupInfor).append("'");
			}
			else if(bottom==Double.NEGATIVE_INFINITY)
			{
				sqlBuffer.append(
						StringHandler.doubleQ(columnName.getName())).append("<= ").append(
								upper).append(" then '").append(groupInfor).append("'");
			}	
			else
			{
				sqlBuffer.append(
					StringHandler.doubleQ(columnName.getName())).append(" >").append(
					bottom).append(" and ").append(
					StringHandler.doubleQ(columnName.getName())).append("<= ").append(
					upper).append(" then '").append(groupInfor).append("'");
			}
		}
		sqlBuffer.append(" end ) \"AlpineWOEGroup\"");
	}
}
