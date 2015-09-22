/**
 * ClassName WOETrainer.java
 *
 * Version information: 1.00
 *
 * Data: 30 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEAutoGroup;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQL;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSqlFactory;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutWOE;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
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
import com.alpine.datamining.operator.woe.WOEModel;
import com.alpine.utility.db.Resources;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class WOETrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(WOETrainer.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#createNodeMetaInfo
	 * ()
	 */
	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {

		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.WOE_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.WOE_TRAIN_DESCRIPTION, locale));
		return nodeMetaInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#train(com.alpine
	 * .datamining.api.AnalyticSource)
	 */
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		WeightOfEvidenceConfig woeConfig = (WeightOfEvidenceConfig) source
				.getAnalyticConfig();
		String dependentColumn = woeConfig.getDependentColumn();
		String dbType = ((DataBaseAnalyticSource) source).getDataSourceType();

		AnalysisWOETable tableWOEInfor = null;

		try {
			tableWOEInfor = woeConfig.getWOETableInfor().clone();
		} catch (CloneNotSupportedException e1) {
			logger.error(e1.getMessage(),e1);
			throw new AnalysisException(e1);
		}

		DataSet dataSet;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source, source
					.getAnalyticConfig());
			setSpecifyColumn(dataSet, source.getAnalyticConfig());
			dataSet.computeAllColumnStatistics();

			Column label = dataSet.getColumns().getLabel();
			if (label.getMapping().size() > 2) {
				logger.error(
						SDKLanguagePack.getMessage(
								SDKLanguagePack.WOE_DEPENDENT_2_VALUE,
								woeConfig.getLocale()));
				throw new OperatorException(SDKLanguagePack.getMessage(
						SDKLanguagePack.WOE_DEPENDENT_2_VALUE, woeConfig
								.getLocale()));
			}
			WOEDataSQL dataWOE = WOEDataSqlFactory.generalWOEDataSQL(dbType);

			if (tableWOEInfor.getDataTableWOE().isEmpty()) {
				AnalyzerOutPutWOE result = (AnalyzerOutPutWOE) WOEAutoGroup
						.autoGroup(source);
				tableWOEInfor = result.getResultList();

			}

			Iterator<Column> iter = dataSet.getColumns().iterator();

			while (iter.hasNext()) {
				Column tempColumn = iter.next();
				if (tempColumn.getName().equalsIgnoreCase(dependentColumn)) {
					continue;
				}

				AnalysisWOEColumnInfo woeInfor = tableWOEInfor
						.getOneColumnWOE(tempColumn.getName());
				if (woeInfor == null || woeInfor.getInforList() == null
						|| woeInfor.getInforList().isEmpty()) {
					woeInfor = createwoeInfo(tempColumn, source,
							dependentColumn);
					dataWOE.computeWOE(woeInfor, source, tempColumn, tempColumn
							.isNumerical());
					tableWOEInfor.removeOneColumnWOE(tempColumn.getName());
					tableWOEInfor.addOneColumnWOE(woeInfor);
				} else {
					dataWOE.computeWOE(woeInfor, source, tempColumn, tempColumn
							.isNumerical());
				}
			}
		} catch (OperatorException e) {
			logger.error(e);
			throw new AnalysisException(e);
		}

		WOEModel model = new WOEModel(dataSet);
		model.setWOEInfoTable(tableWOEInfor);
		return model;

	}

	/**
	 * @param tempColumn
	 * @param source
	 * @param dependentColumn
	 * @throws AnalysisException
	 * 
	 */
	private AnalysisWOEColumnInfo createwoeInfo(Column tempColumn,
			AnalyticSource source, String dependentColumn)
			throws AnalysisException {

		CartTrainer analyzer = new CartTrainer();
		AnalysisWOEColumnInfo woeInfor = new AnalysisWOEColumnInfo();
		CartConfig cartConfig = new CartConfig();
		long rowNumber = 0;
		Connection conncetion = ((DataBaseAnalyticSource) source)
				.getConnection();
		Statement st = null;
		ResultSet rs = null;
		String inputSchema = ((DataBaseAnalyticSource) source).getTableInfo()
				.getSchema();
		String tableName = ((DataBaseAnalyticSource) source).getTableInfo()
				.getTableName();
		String dbSystem = ((DataBaseAnalyticSource) source).getDataBaseInfo()
				.getSystem();
		String url = ((DataBaseAnalyticSource) source).getDataBaseInfo()
				.getUrl();
		String userName = ((DataBaseAnalyticSource) source).getDataBaseInfo()
				.getUserName();
		String password = ((DataBaseAnalyticSource) source).getDataBaseInfo()
				.getPassword();
		String useSSL = ((DataBaseAnalyticSource) source)
				.getDataBaseInfo().getUseSSL ();
		DataBaseAnalyticSource tempSource = new DataBaseAnalyticSource(
				dbSystem, url, userName, password, inputSchema, tableName,useSSL);
		tempSource.setConenction(conncetion);
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
			st.close();
			int miniForSplit = (int) (rowNumber * AlpineMinerConfig.WOE_mini_ForSplit_Percent) + 1;
			int miniLeafSize = (int) (rowNumber * AlpineMinerConfig.WOE_mini_LeafSize_Percent) + 1;

			cartConfig.setDependentColumn(dependentColumn);
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
			cartConfig.setColumnNames(tempColumn.getName());
			tempSource.setAnalyticConfiguration(cartConfig);
			AnalyzerOutPutTrainModel result;

			result = (AnalyzerOutPutTrainModel) analyzer.doAnalysis(tempSource);

			DecisionTreeModel tempModel = (DecisionTreeModel) result
					.getEngineModel().getModel();
			Tree tempTree = tempModel.getRoot();

			woeInfor.setColumnName(tempColumn.getName());
			List<AnalysisWOENode> ruleList = new ArrayList<AnalysisWOENode>();
			if (tempColumn.isNumerical()) {
				generateNumbernicRule(ruleList, tempTree);
				woeInfor.setInforList(ruleList);
			} else if (tempColumn.isNominal()) {

				generateNominalRule(ruleList, tempTree, tempColumn.getMapping()
						.getValues());
				woeInfor.setInforList(ruleList);
			}
			woeInfor.setInforList(ruleList);
		} catch (SQLException e) {
			logger.error(e);
			throw new AnalysisException(e.getLocalizedMessage());
		}
		return woeInfor;
	}

	private void generateNominalRule(List<AnalysisWOENode> ruleList,
			Tree tempTree, List<String> choosedColumns) {
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

	private void generateNumbernicRule(List<AnalysisWOENode> ruleList,
			Tree tempTree) {
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
