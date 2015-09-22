/**
 * ClassName CartTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.db.Table;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.operator.tree.cart.CartParameter;
import com.alpine.datamining.operator.tree.cartclassification.CartClassificationTrainer;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionTrainer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;
/** 
 * 
 * @author Eason
 *
 */
public class CartTrainer extends AbstractDBModelTrainer {
    private static final Logger itsLogger = Logger.getLogger(CartTrainer.class);

    private boolean forWoe = false;
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		CartConfig config = (CartConfig)source.getAnalyticConfig();
		if (config.isForWoe()==true){
			forWoe = true;
		}
		try {
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			boolean labelIsNominal = dataSet.getColumns().getLabel().isNominal();
			Operator learner = null;
			
			if (labelIsNominal||(config.isForWoe()==true))
			{
				learner = OperatorUtil.createOperator(CartClassificationTrainer.class);
			}
			else
			{
				learner = OperatorUtil.createOperator(CartRegressionTrainer.class);
			}
			CartParameter parameter = new CartParameter();

//			if(!StringUtil.isEmpty(config.getColumnNames()))
//			{
//				itsLogger.debug("set \"ColumnName\" to "+config.getColumnNames());
//				learner.setParameter("columnname", config.getColumnNames());		
//			}
			if(!StringUtil.isEmpty(config.getMinimal_leaf_size()))
			{
				itsLogger.debug("set \"minimal_leaf_size\" to "+config.getMinimal_leaf_size());
//				learner.setParameter("minimal_leaf_size", config.getMinimal_leaf_size());
				parameter.setMinLeafSize(Integer.parseInt(config.getMinimal_leaf_size()));
			}

			if(!StringUtil.isEmpty(config.getMaximal_depth()))
			{
				itsLogger.debug("set \"maximal_depth\" to "+config.getMaximal_depth());
//				learner.setParameter("maximal_depth", config.getMaximal_depth());
				parameter.setMaxDepth(Integer.parseInt(config.getMaximal_depth()));
			}
//			if(config.getMinimal_gain()!=null)
//			{
//				itsLogger.debug("set \"minimal_gain\" to "+config.getMinimal_gain());
//				learner.setParameter("minimal_gain", config.getMinimal_gain());	
//			}
			if(!StringUtil.isEmpty(config.getMinimal_size_for_split()))
			{
				itsLogger.debug("set \"minimal_size_for_split\" to "+config.getMinimal_size_for_split());
//				learner.setParameter("minimal_size_for_split", config.getMinimal_size_for_split());
				parameter.setSplitMinSize(Integer.parseInt(config.getMinimal_size_for_split()));
			}
			if(!StringUtil.isEmpty(config.getNo_pre_pruning()))
			{
				itsLogger.debug("set \"no_pre_pruning\" to "+config.getNo_pre_pruning());
//				learner.setParameter("no_pre_pruning", config.getNo_pre_pruning());
				parameter.setNoPrePruning(Boolean.parseBoolean(config.getNo_pre_pruning()));
			}

			if(!StringUtil.isEmpty(config.getNumber_of_prepruning_alternatives()))
			{
				itsLogger.debug("set \"number_of_prepruning_alternatives\" to "+config.getNumber_of_prepruning_alternatives());
//				learner.setParameter("number_of_prepruning_alternatives", config.getNumber_of_prepruning_alternatives());	
				parameter.setPrepruningAlternativesNumber(Integer.parseInt(config.getNumber_of_prepruning_alternatives()));
			}
			if (!StringUtil.isEmpty(config.getSize_threshold_load_data()))
			{
				itsLogger.debug("set \"size_threshold_load_data\" to "+config.getSize_threshold_load_data());
//				learner.setParameter("size_threshold_load_data", config.getSize_threshold_load_data());
				parameter.setThresholdLoadData(Integer.parseInt(config.getSize_threshold_load_data()));
			}
			
			if (!StringUtil.isEmpty(config.getIsChiSqaure()))
			{
				itsLogger.debug("set \"isChiSqaure\" to "+config.getIsChiSqaure() );
//				learner.setParameter("size_threshold_load_data", config.getSize_threshold_load_data());
				parameter.setUseChiSquare(Boolean.parseBoolean(config.getIsChiSqaure()));
			}
			
			if (config.isForWoe()==true)
			{
				itsLogger.debug("set \"isForWoe\" to "+config.isForWoe() );
//				learner.setParameter("size_threshold_load_data", config.getSize_threshold_load_data());
				parameter.setForWoe(config.isForWoe());
			}
			
			
			if(((CartConfig)config).getDependentColumn()==null)
			{
				AnalysisError error = new AnalysisError(this,AnalysisErrorName.DependentColumn_Empty,config.getLocale());
				itsLogger.error(error.getMessage(), error);
				throw error;  
			}

			if (labelIsNominal)
			{
				if(!StringUtil.isEmpty(config.getNo_pruning()))
				{
					itsLogger.debug("set \"no_pruning\" to "+config.getNo_pruning());
//					learner.setParameter("no_pruning", config.getNo_pruning());
					parameter.setNoPruning(Boolean.parseBoolean(config.getNo_pruning()));
				}
				if(!StringUtil.isEmpty(config.getConfidence()))
				{
					itsLogger.debug("set \"confidence\" to "+config.getConfidence());
//					learner.setParameter("confidence", config.getConfidence());		
					parameter.setConfidence(Double.parseDouble(config.getConfidence()));
				}
			}
			dataSet = getNoNullDataSet(dataSet);
			dataSet.computeAllColumnStatistics();
			warnTooManyValue(dataSet,Integer.parseInt(AlpineMinerConfig.CART_COMBINE_THRESHOLD),config.getLocale());
			learner.setParameter(parameter);
			Model model = ((Training) learner).train(dataSet); 
			return model;
		} catch (Exception e) {
			itsLogger.error(e) ;
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} 
			else if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}	
	}
		
	}
	
	
	protected void setNumericalLabelCategory(Column label){
		if(forWoe && label.isNumerical())
		{
			((NumericColumn)label).setCategory(true);
		}
	}
	
	

	private DataSet getNoNullDataSet(DataSet eSet) throws WrongUsedException,
			OperatorException {
		DataSet dataSet = (DataSet) eSet.clone();

		String whereCondition = ((DBTable) dataSet.getDBTable())
				.getWhereCondition();
		String notNullArray = "";
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
			if (!first) {
				notNullArray += " and ";
			} else {
				first = false;
			}
			notNullArray += StringHandler.doubleQ(column.getName())
					+ " is not null ";
		}
		notNullArray += " and "
				+ StringHandler.doubleQ(dataSet.getColumns().getLabel()
						.getName()) + " is not null ";
		StringBuffer newWhereCondition = new StringBuffer();

		if (whereCondition != null && whereCondition.length() != 0) {
			newWhereCondition.append(whereCondition).append(notNullArray);
		} else {
			newWhereCondition.append(notNullArray);
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		String url = ((DBTable) dataSet.getDBTable()).getUrl();
		String userName = ((DBTable) dataSet.getDBTable()).getUserName();
		String password = ((DBTable) dataSet.getDBTable()).getPassword();
		ArrayList<Column> regularAttrubtes = new ArrayList<Column>();
		for (Column regularColumn : dataSet.getColumns()) {
			regularAttrubtes.add((Column) regularColumn.clone());
		}
		Table table = null;
		try {
			table = DBTable.createDatabaseDataTableDB(databaseConnection,
					url, userName, password, tableName, newWhereCondition
							.toString());
			Column labelColumn = (Column) dataSet.getColumns()
					.getLabel().clone();
			dataSet = table.createDataSet(labelColumn, regularAttrubtes);
			dataSet.computeAllColumnStatistics();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((CartConfig)config).getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.CART_TREE_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.CART_TREE_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
