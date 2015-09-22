/**
 * ClassName WeightOfEvidenceAutoGroupAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQL;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQLDB2;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQLNZ;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQLOracle;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQLPGGP;
import com.alpine.datamining.api.impl.db.trainer.CartTrainer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutWOE;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.tree.cartclassification.CartNorminalDevideCond;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.tree.threshold.Side;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.Resources;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class WeightOfEvidenceAutoGroupAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(WeightOfEvidenceAutoGroupAnalyzer.class);
	
	private static String dbType;
	public AnalyticOutPut doAnalysis(AnalyticSource analyticSource)
			throws AnalysisException {

		AnalyzerOutPutWOE woeResult = new AnalyzerOutPutWOE();
		WeightOfEvidenceConfig woeConfig = (WeightOfEvidenceConfig) analyticSource
				.getAnalyticConfig();

		String dependColumn = woeConfig.getDependentColumn();

		CartConfig cartConfig = new CartConfig();
		String dbSystem = ((DataBaseAnalyticSource) analyticSource)
				.getDataBaseInfo().getSystem();
		String url = ((DataBaseAnalyticSource) analyticSource)
				.getDataBaseInfo().getUrl();
		String userName = ((DataBaseAnalyticSource) analyticSource)
				.getDataBaseInfo().getUserName();
		String password = ((DataBaseAnalyticSource) analyticSource)
				.getDataBaseInfo().getPassword();
		String inputSchema = ((DataBaseAnalyticSource) analyticSource)
				.getTableInfo().getSchema();
		String tableName = ((DataBaseAnalyticSource) analyticSource)
				.getTableInfo().getTableName();
		String useSSL = ((DataBaseAnalyticSource) analyticSource)
				.getDataBaseInfo().getUseSSL();
		DataBaseAnalyticSource tempSource = new DataBaseAnalyticSource(
				dbSystem, url, userName, password, inputSchema, tableName,useSSL);
		tempSource.setConenction(((DataBaseAnalyticSource) analyticSource)
				.getConnection());

		AnalysisWOETable tableResult = new AnalysisWOETable();
		Connection conncetion = ((DataBaseAnalyticSource) analyticSource)
				.getConnection();
		Statement st=null;
		ResultSet rs = null;
		long rowNumber = 0;
		try {
			st = conncetion.createStatement();
			String sql = "select count(*) from "
					+ StringHandler.doubleQ(inputSchema) + "."
					+ StringHandler.doubleQ(tableName);
			
			logger.debug(sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				rowNumber = rs.getLong(1);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			if(e.getErrorCode()==-204){
				throw new AnalysisError(this,AnalysisErrorName.TABLE_NOT_EXIST,woeConfig.getLocale());
			}else{
				throw new AnalysisException(e.getLocalizedMessage());	
			}			
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
			throw new AnalysisException(e.toString());
			}
		}

		int miniForSplit = (int) (rowNumber * AlpineMinerConfig.WOE_mini_ForSplit_Percent) + 1;
		int miniLeafSize = (int) (rowNumber * AlpineMinerConfig.WOE_mini_LeafSize_Percent) + 1;

		cartConfig.setDependentColumn(dependColumn);
		cartConfig.setForceRetrain(Resources.YesOpt);
		cartConfig.setMaximal_depth("10");
		cartConfig.setConfidence("0.25");
		cartConfig.setNumber_of_prepruning_alternatives("3");
		cartConfig.setNo_pruning("true");
		cartConfig.setNo_pre_pruning("false");
		cartConfig.setIsChiSqaure("true");
		cartConfig.setSize_threshold_load_data("1");
		cartConfig.setMinimal_size_for_split(String.valueOf(miniForSplit));// %
		cartConfig.setForWoe(true);

		cartConfig.setMinimal_leaf_size(String.valueOf(miniLeafSize));

		DataSet dataSet;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) analyticSource,
					analyticSource.getAnalyticConfig());
			setSpecifyColumn(dataSet, analyticSource.getAnalyticConfig());
			dataSet.computeAllColumnStatistics();
			Column label = dataSet.getColumns().getLabel();
			if(label==null){
				logger.error(
						SDKLanguagePack.getMessage(SDKLanguagePack.WOE_DEPENDENT_NULL,woeConfig.getLocale()));
				throw new OperatorException(SDKLanguagePack.getMessage(SDKLanguagePack.WOE_DEPENDENT_NULL,woeConfig.getLocale()));
			}else if (label.getMapping().size() > 2) {
				logger.error(
						SDKLanguagePack.getMessage(SDKLanguagePack.WOE_DEPENDENT_2_VALUE,woeConfig.getLocale()));
				throw new OperatorException(SDKLanguagePack.getMessage(SDKLanguagePack.WOE_DEPENDENT_2_VALUE,woeConfig.getLocale()));
			}
			Iterator<Column> iter = dataSet.getColumns().iterator();
			while (iter.hasNext()) {
				Column tempColumn = iter.next();
				if (tempColumn.getName().equalsIgnoreCase(dependColumn)) {
					continue;
				}

				CartTrainer analyzer = new CartTrainer();

				cartConfig.setColumnNames(tempColumn.getName());
				tempSource.setAnalyticConfiguration(cartConfig);
				AnalyzerOutPutTrainModel result = (AnalyzerOutPutTrainModel) analyzer
						.doAnalysis(tempSource);
				DecisionTreeModel tempModel = (DecisionTreeModel) result
						.getEngineModel().getModel();
				Tree tempTree = tempModel.getRoot();
				AnalysisWOEColumnInfo woeInfor = new AnalysisWOEColumnInfo();
				woeInfor.setColumnName(tempColumn.getName());
				List<AnalysisWOENode> ruleList = new ArrayList<AnalysisWOENode>();
				if (tempColumn.isNumerical()) {
					generateNumbernicRule(ruleList, tempTree);
					woeInfor.setInforList(ruleList);
				} else if (tempColumn.isNominal()) {

					generateNominalRule(ruleList, tempTree, tempColumn
							.getMapping().getValues());
					woeInfor.setInforList(ruleList);
				}
				woeInfor.setInforList(ruleList);
				computeWOE(woeInfor, analyticSource, tempColumn,
						tempColumn.isNumerical());
				tableResult.addOneColumnWOE(woeInfor);
			}
		} catch (OperatorException e) {
			logger.error(e.getMessage(),e);
			throw new AnalysisException(e.getLocalizedMessage());
		}
		finally{
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
			throw new AnalysisException(e.toString());
			}
		}
		((AnalyzerOutPutWOE) woeResult).setResultList(tableResult);
		woeResult.setResultList(tableResult);

		return woeResult;
	}

	public AnalyticOutPut computeWOEStatic(AnalyticSource analyticSource)
			throws AnalysisException {

		AnalyzerOutPutWOE woeResult = new AnalyzerOutPutWOE();
		WeightOfEvidenceConfig woeConfig = (WeightOfEvidenceConfig) analyticSource
				.getAnalyticConfig();
				
		String dependColumn = woeConfig.getDependentColumn();
		AnalysisWOETable tableResult = woeConfig.getWOETableInfor();
		List<AnalysisWOEColumnInfo> woeInfoList = tableResult.getDataTableWOE();
		AnalysisWOEColumnInfo woeInfor = woeInfoList.get(0);
		DataSet dataSet;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) analyticSource,
					analyticSource.getAnalyticConfig());
			setSpecifyColumn(dataSet, analyticSource.getAnalyticConfig());
			dataSet.computeAllColumnStatistics();
			Iterator<Column> iter = dataSet.getColumns().iterator();
			while (iter.hasNext()) {
				Column tempColumn = iter.next();
				if (tempColumn.getName().equalsIgnoreCase(dependColumn)) {
					break;
				}
				woeInfor.setColumnName(tempColumn.getName());
				computeWOE(woeInfor, analyticSource, tempColumn,
						tempColumn.isNumerical());
			}
		} catch (OperatorException e) {
			if(e instanceof WrongUsedException){
				if(((WrongUsedException)e).getErrorMessage().contains("DB2 SQL Error: SQLCODE=-204, SQLSTATE=42704")){
					throw new AnalysisError(this,AnalysisErrorName.TABLE_NOT_EXIST,woeConfig.getLocale());
				}else{
					throw new AnalysisException(((WrongUsedException)e).getErrorMessage());
				}			
			}else{
				throw new AnalysisException(e.getMessage());
			}
			
		}
		
		woeResult.setResultList(tableResult);

		return woeResult;
	}

	public void computeWOE(AnalysisWOEColumnInfo woeInfor,
			AnalyticSource analyticSource, Column tempColumn,
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
		 dbType = analyticSource.getDataSourceType();
		
		WOEDataSQL dataWOE = null;
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			dataWOE = new WOEDataSQLOracle();
		} else if (dbType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)
				|| dbType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
			dataWOE = new WOEDataSQLPGGP();
		} else if (dbType.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
			dataWOE = new WOEDataSQLDB2();
		}else if (dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			dataWOE = new WOEDataSQLNZ();
		}

		try {

			double totalGoodNumber = 0, totalBadNumber = 0;
			double tempGoodNumber = 0, tempBadNumber = 0;
			double gini = 0;
			double inforValue = 0;
			String groupInfo = new String();
			st = conncetion.createStatement();
			StringBuffer sumFrequencySQL = new StringBuffer();
			dataWOE.generalTotalSQL(sumFrequencySQL, dependColumn, goodValue);
			sumFrequencySQL.append(" from ").append(
					StringHandler.doubleQ(inputSchema)).append(".").append(
					StringHandler.doubleQ(tableName));

			
			logger.debug("WeightOfEvidenceAutoGroupAnalyzer:computeWOE()"+sumFrequencySQL.toString());
			rs = st.executeQuery(sumFrequencySQL.toString());
			while (rs.next()) {
				totalGoodNumber = rs.getDouble(1);
				totalBadNumber = rs.getDouble(2);
				if (totalGoodNumber == 0) {
					totalGoodNumber = 0.05;
				}
				if (totalBadNumber == 0) {
					totalBadNumber = 0.05;
				}
			}
			double WOE;
			StringBuffer sqlBuffer = new StringBuffer();

			dataWOE.generalPreComputeWOESQL(dependColumn, goodValue, sqlBuffer);
			if (isNumbernic == true) {
				genatrateNumbernicSQL(sqlBuffer, woeInfor, tempColumn);
			} else {
				genatrateNominalSQL(sqlBuffer, woeInfor, tempColumn);
			}
			dataWOE.generalAfterComputeWOESQL(inputSchema, tableName, sqlBuffer);

			logger.debug(sqlBuffer.toString());

			st = conncetion.createStatement();
			rs = st.executeQuery(sqlBuffer.toString());
			while (rs.next()) {
				tempGoodNumber = rs.getDouble(1);
				tempBadNumber = rs.getDouble(2);
				groupInfo = rs.getString(3);
				if (tempGoodNumber == 0) {
					tempGoodNumber = 0.05;
				}
				if (tempBadNumber == 0) {
					tempBadNumber = 0.05;
				}
				WOE = Math.log((tempGoodNumber / totalGoodNumber)
						/ (tempBadNumber / totalBadNumber));
				woeInfor.setWOEValue(groupInfo, WOE);
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
			throw new OperatorException(e.getLocalizedMessage());
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
			throw new OperatorException(e.getLocalizedMessage());
			}
		}

	}

	/**
	 * @param tempTree
	 * @param nominalList
	 * @param choosedColumns
	 */

	private void treeToListNominal(Tree tempTree,
			List<ArrayList<String>> nominalList, List<String> choosedColumns) {
		if (!tempTree.isLeaf()) {
			Iterator<Side> childIterator = tempTree.childIterator();
			boolean firstLoop = true;
		
			List<String> inEdgeString = new ArrayList<String>();
			
			while (childIterator.hasNext()) {
				Side edge = childIterator.next();
				if (firstLoop == true) {
					
					inEdgeString = new ArrayList<String>();
					inEdgeString = ((CartNorminalDevideCond) edge
							.getCondition()).getValueStringList();
					for (String tempString : inEdgeString) {
						
						if (choosedColumns.contains(tempString)) {
							choosedColumns.remove(tempString);
						}
					}
					firstLoop = false;
				}
				if (((CartNorminalDevideCond) edge.getCondition()).isNot() == true) {
					treeToListNominal(edge.getChild(), nominalList,
							choosedColumns);
				} else if (((CartNorminalDevideCond) edge.getCondition())
						.isNot() == false) {
					treeToListNominal(edge.getChild(), nominalList,
							inEdgeString);
				}
			
			}

		} else {
			nominalList.add((ArrayList<String>) choosedColumns);
		}

	}

	private void treeToListNumbernic(Tree tempTree, List<Double> numberList) {

		if (!tempTree.isLeaf()) {
			Iterator<Side> childIterator = tempTree.childIterator();
			while (childIterator.hasNext()) {
				Side edge = childIterator.next();
				double tempNumber = Double.parseDouble(edge.getCondition()
						.getValueString());
				if (!numberList.contains(tempNumber)) {
					numberList.add(tempNumber);
				}
				treeToListNumbernic(edge.getChild(), numberList);
			}

		}

	}

	private void generateNominalRule(List<AnalysisWOENode> ruleList, Tree tempTree,
			List<String> choosedColumns) {
		List<ArrayList<String>> nominalList = new ArrayList<ArrayList<String>>();
		treeToListNominal(tempTree, nominalList, choosedColumns);
		int groupInfor = 1;
		for (ArrayList<String> tempList : nominalList) {
			AnalysisWOENominalNode nominalNode = new AnalysisWOENominalNode();
			nominalNode.setChoosedList(tempList);
			nominalNode.setGroupInfror("Group" + groupInfor);
			groupInfor++;
			ruleList.add(nominalNode);
		}
	}

	private void generateNumbernicRule(List<AnalysisWOENode> ruleList, Tree tempTree) {
		List<Double> numberList = new ArrayList<Double>();
		treeToListNumbernic(tempTree, numberList);
		Collections.sort(numberList);
		double mini = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;
		double preNumber = mini;
		double afterNumber = 0;
		int groupInfor = 1;
		for (double tempNumber : numberList) {
			AnalysisWOENumericNode tempNumberNode = new AnalysisWOENumericNode();
			afterNumber = tempNumber;
			tempNumberNode.setBottom(preNumber);
			tempNumberNode.setUpper(afterNumber);
			tempNumberNode.setGroupInfror("Group" + groupInfor);
			ruleList.add(tempNumberNode);
			groupInfor++;
			preNumber = afterNumber;
		}
		afterNumber = max;
		AnalysisWOENumericNode tempNumberNode = new AnalysisWOENumericNode();
		tempNumberNode.setBottom(preNumber);
		tempNumberNode.setUpper(afterNumber);
		tempNumberNode.setGroupInfror("Group" + groupInfor);
		ruleList.add(tempNumberNode);
	}

	private static void genatrateNominalSQL(StringBuffer sqlBuffer,
			AnalysisWOEColumnInfo woeInfor, Column tempColumn) {
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		sqlBuffer.append(" , ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			List<String> columnValues = ((AnalysisWOENominalNode) tempNode)
					.getChoosedList();
			StringBuffer columnSet = new StringBuffer();
			columnSet.append("(");
			for (String tempString : columnValues) {
				columnSet.append(CommonUtility.quoteValue(dbType, tempColumn, tempString));
				columnSet.append(",");
			}
			columnSet.deleteCharAt(columnSet.length() - 1);
			columnSet.append(")");
			String groupInfor = ((AnalysisWOENominalNode) tempNode).getGroupInfror();
			sqlBuffer.append(" when ").append(
					StringHandler.doubleQ(tempColumn.getName())).append(" in ");
			sqlBuffer.append(columnSet);


			sqlBuffer.append(" then '").append(groupInfor).append("'");
		}
		sqlBuffer.append(" else 'group0' end ) \"AlpineWOEGroup\"");
	}

	private static void genatrateNumbernicSQL(StringBuffer sqlBuffer,
			AnalysisWOEColumnInfo woeInfor, Column tempColumn) {
		List<AnalysisWOENode> woeInforList = woeInfor.getInforList();
		sqlBuffer.append(" , ( case");
		for (AnalysisWOENode tempNode : woeInforList) {
			double upper = ((AnalysisWOENumericNode) tempNode).getUpper();
			double bottom = ((AnalysisWOENumericNode) tempNode).getBottom();
			String groupInfor = ((AnalysisWOENumericNode) tempNode).getGroupInfror();
			sqlBuffer.append(" when ");
			if (upper == Double.POSITIVE_INFINITY && bottom == Double.NEGATIVE_INFINITY) {
				sqlBuffer.append("'").append(groupInfor).append("'").append(
						" = ").append("'").append(groupInfor).append("'")
						.append(" then '").append(groupInfor).append("'");
			}

			else if (upper == Double.POSITIVE_INFINITY) {
				sqlBuffer.append(StringHandler.doubleQ(tempColumn.getName())).append(
						" >").append(bottom).append(" then '").append(
						groupInfor).append("'");
			} else if (bottom == Double.NEGATIVE_INFINITY) {
				sqlBuffer.append(StringHandler.doubleQ(tempColumn.getName())).append(
						"<= ").append(upper).append(" then '").append(
						groupInfor).append("'");
			} else {
				sqlBuffer.append(StringHandler.doubleQ(tempColumn.getName())).append(
						" >").append(bottom).append(" and ").append(
						StringHandler.doubleQ(tempColumn.getName())).append("<= ")
						.append(upper).append(" then '").append(groupInfor)
						.append("'");
			}
		}
		sqlBuffer.append(" end ) \"AlpineWOEGroup\"");
	}

	
	protected void setNumericalLabelCategory(Column label) {
		if (label.isNumerical()) {
			((NumericColumn) label).setCategory(true);
		}
	}
	
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) throws OperatorException {
		((DatabaseSourceParameter) dataSource.getParameter())
				.setLabel(((WeightOfEvidenceConfig) config)
						.getDependentColumn());
	}
}
