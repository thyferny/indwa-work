/**
 * ClassName VariableAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 3, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.VariableConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBin;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinCategory;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinDateTime;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinNumeric;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBDataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Richie Lo
 * 
 */
public class VariableAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(VariableAnalyzer.class);
	
	private AnalysisQuantileFieldsModel quantileModel;

	private AnalysisDerivedFieldsModel derivedFieldsModel;

	private ISqlGeneratorMultiDB sqlGenerator;
	
	private String dbType;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		DatabaseConnection databaseConnection = null;
		try {

			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,
					source.getAnalyticConfig());

			databaseConnection = ((DBTable) dataSet.getDBTable())
					.getDatabaseConnection();
			VariableConfig config = (VariableConfig) source.getAnalyticConfig();

			sqlGenerator = SqlGeneratorMultiDBFactory
					.createConnectionInfo(databaseConnection.getProperties()
							.getName());
			dbType =databaseConnection.getProperties().getName();

			setInputSchema(((DataBaseAnalyticSource) source).getTableInfo()
					.getSchema());
			setInputTable(((DataBaseAnalyticSource) source).getTableInfo()
					.getTableName());

			setDerivedFieldsModel(config.getDerivedModel());
			setQuantileModel(config.getQuantileModel());
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());
			generateStoragePrameterString((DataBaseAnalyticSource) source);

			performOperation(databaseConnection, dataSet);

			DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo();
			AnalyzerOutPutTableObject outPut = getResultTableSampleRow(
					databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			return outPut;

		} catch (Exception e) {
			logger.error(e);
			if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}

	}

	public void performOperation(DatabaseConnection databaseConnection,
			DataSet dataSet) throws AnalysisException, OperatorException {
		// prepare the variable list;
		String varList = buildVariableList();

		String binVarList = buildBinVarList(dataSet.size());

		String outputTableName = getQuotaedTableName(getOutputSchema(),
				getOutputTable());

		String inputTableName = getQuotaedTableName(getInputSchema(),
				getInputTable());

		dropIfExist(dataSet);

		DatabaseUtil.alterParallel(databaseConnection, getOutputType());// for
																		// oracle

		Columns columns = ((DBDataSet) dataSet).getColumns();

		List<String> needNotCreateNewColumnNames = new ArrayList<String>();
		if (quantileModel != null) {
			List<AnalysisQuantileItem> qItems = quantileModel
					.getQuantileItems();
			if (qItems != null && qItems.size() > 0) {
				for (Iterator<AnalysisQuantileItem> iterator = qItems
						.iterator(); iterator.hasNext();) {
					AnalysisQuantileItem quantileItem = iterator.next();
					if (quantileItem.isCreateNewColumn() == false) {
						needNotCreateNewColumnNames.add(quantileItem
								.getColumnName());
					}
				}
			}

		}
		StringBuffer columnNames = new StringBuffer();

		if (getDerivedFieldsModel() != null
				&& getDerivedFieldsModel().getSelectedFieldList() != null
				&& !getDerivedFieldsModel().getSelectedFieldList().isEmpty()) {
			if (needNotCreateNewColumnNames.size() <= 0) {
				columnNames = new StringBuffer("");
			}
			for (Iterator<Column> iterator = columns.iterator(); iterator
					.hasNext();) {
				Column column = iterator.next();
				String columnName = column.getName();
				if (getDerivedFieldsModel().getSelectedFieldList().contains(
						columnName)) {
					if (needNotCreateNewColumnNames.size() > 0
							&& needNotCreateNewColumnNames.contains(columnName)) {
						continue;
					}
					// filter,,,
					if (columnNames.length() > 0) {
						columnNames.append(",");
					}
					columnNames.append(StringHandler.doubleQ(columnName));
				}
			}
		}
		if (getDerivedFieldsModel() != null
				&& ((getDerivedFieldsModel().getSelectedFieldList() == null) || getDerivedFieldsModel()
						.getSelectedFieldList().isEmpty())) {
			columnNames = new StringBuffer("");
			if (varList.length() != 0) {
				varList = varList.substring(1, varList.length());
			} else if (binVarList.length() != 0) {
				binVarList = binVarList.substring(1, binVarList.length());
			}
		}
		if (StringUtil.isEmpty(columnNames.toString())
				&& StringUtil.isEmpty(varList) && binVarList.length() != 0) {
			binVarList = binVarList.substring(1, binVarList.length());
		}
		StringBuilder createSql = new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		StringBuilder insertTable = new StringBuilder();
		createSql.append("CREATE ").append(getOutputType());
		createSql.append(" ").append(outputTableName).append(" ");
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");

		createSql.append(DatabaseUtil.addParallel(databaseConnection,
				getOutputType()));
		createSql.append(" AS ( ");

		if(dbType.equals(DataSourceInfoNZ.dBType)){
			StringBuilder sb=new StringBuilder();
			Iterator<Column> iter = dataSet.getColumns().iterator();
			while(iter.hasNext()){
				Column column = iter.next();
				String columnName=column.getName();
				sb.append(StringHandler.doubleQ(columnName)).append(",");
				sb.append(StringHandler.doubleQ(columnName)).append(" as ").append(StringHandler.doubleQ("alpine_"+columnName)).append(",");
			}
			if(sb.length()>0){
				sb=sb.deleteCharAt(sb.length()-1);
			}
			selectSql.append(" SELECT ").append(columnNames).append(" ");
			selectSql.append(varList).append(binVarList).append(" FROM (");			
			selectSql.append(" SELECT ").append(sb);
			selectSql.append(" FROM ").append(
					inputTableName).append(") foo");	
		}else{
			selectSql.append(" SELECT ").append(columnNames).append(" ");
			selectSql.append(varList).append(binVarList).append(" FROM ").append(
					inputTableName);	
		}

		createSql.append(selectSql).append(" )");
		if (getOutputType().equalsIgnoreCase("table")) {
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			logger.debug("VariableAnalyzer.performOperation():sql="
					+ createSql);
			st.executeUpdate(createSql.toString());
			
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"VariableAnalyzer.performOperation():refreshTableSql=" + insertTable);
			}
			
		} catch (SQLException e) {
			logger.error(e);
			throw new AnalysisException(e);
		} finally {
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}

	}

	/**
	 * @return
	 */
	private List<AnalysisQuantileItem> getAutoGenerateQuantileItem() {
		if (getQuantileModel() != null
				&& getQuantileModel().getQuantileItems() != null
				&& getQuantileModel().getQuantileItems().size() > 0) {
			List<AnalysisQuantileItem> items = new ArrayList<AnalysisQuantileItem>();
			for (Iterator<AnalysisQuantileItem> iterator = getQuantileModel()
					.getQuantileItems().iterator(); iterator.hasNext();) {
				AnalysisQuantileItem quantileItem = iterator.next();
				if (quantileItem.getQuantileType() == AnalysisQuantileItem.TYPE_AVG_ASC) {
					items.add(quantileItem);
				}

			}
			return items;
		}

		return null;
	}

	/**
	 * @return
	 */
	private String buildVariableList() {
		StringBuffer buffer = new StringBuffer();
		if (getDerivedFieldsModel() != null
				&& getDerivedFieldsModel().getDerivedFieldsList() != null
				&& !getDerivedFieldsModel().getDerivedFieldsList().isEmpty()) {
			for (AnalysisDerivedFieldItem item : getDerivedFieldsModel()
					.getDerivedFieldsList()) {
				String sqlExpression = sqlGenerator.getCastDataType(item.getSqlExpression(),item.getDataType());
				buffer.append(",").append(sqlExpression).append(
						" AS ").append(
						StringHandler.doubleQ(item.getResultColumnName()));
			}
		} else {
			return "";
		}
		return buffer.toString();
	}

	private String buildBinVarList(long size) {

		StringBuffer buffer = new StringBuffer();

		if (getQuantileModel() == null
				|| getQuantileModel().getQuantileItems() == null
				|| getQuantileModel().getQuantileItems().size() == 0) {
			return "";
		}
		List<AnalysisQuantileItem> allItems = getQuantileModel()
				.getQuantileItems();
		List<AnalysisQuantileItem> autoIitems = getAutoGenerateQuantileItem();
		if (autoIitems != null && autoIitems.size() > 0) {
			if(dbType.equals(DataSourceInfoDB2.dBType)){
				buffer.append(buildAutoItemsListDB2(autoIitems,size));
			}else if(dbType.equals(DataSourceInfoNZ.dBType)){
				buffer.append(buildAutoItemsListNZ(autoIitems));
			}else{
				buffer.append(buildAutoItemsList(autoIitems));
			}
			
		}
		for (Iterator<AnalysisQuantileItem> iterator = allItems.iterator(); iterator
				.hasNext();) {
			AnalysisQuantileItem quantileItem = iterator.next();
			if (autoIitems != null
					&& autoIitems.contains(quantileItem) == false) {
				buffer.append(buildQuantileItemsList(quantileItem));
			}

		}

		return buffer.toString();
	}

	private StringBuffer buildAutoItemsListNZ(List<AnalysisQuantileItem> autoIitems) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < autoIitems.size(); i++) {
			AnalysisQuantileItem item = autoIitems.get(i);
			if (item.isCreateNewColumn() == true) {
				buffer.append(", (ntile(" + item.getNumberOfBin()
						+ ") OVER (ORDER BY \"" + item.getColumnName()
						+ "\")) AS \"" + item.getColumnName().trim()
						+ "_bin\" ");
			} else {
				buffer.append(", (ntile(" + item.getNumberOfBin()
						+ ") OVER (ORDER BY \"alpine_" + item.getColumnName()
						+ "\")) AS " + item.getColumnName().trim()
						+ " ");
			}
		}
		return buffer;
	}

	/**
	 * @param quantileItem
	 * @return
	 */
	private StringBuffer buildQuantileItemsList(
			AnalysisQuantileItem quantileItem) {
		StringBuffer buffer = new StringBuffer();
		String newColumnName = quantileItem.getNewColumnName();

		String whenCluase = buildQunatileItemWhere(quantileItem);
		if (quantileItem.isCreateNewColumn() == true) {
			buffer.append(", (case " + whenCluase + " end)  as "
					+ StringHandler.doubleQ(newColumnName));
		} else {// use old column name...
			buffer.append(", (case " + whenCluase + " end)  as "
					+ StringHandler.doubleQ(quantileItem.getColumnName()));
		}

		return buffer;
	}

	/**
	 * @param quantileItem
	 * @return
	 */
	private String buildQunatileItemWhere(AnalysisQuantileItem quantileItem) {
		StringBuffer sb = new StringBuffer();
		List<AnalysisQuantileItemBin> binItems = quantileItem.getBins();
		String colName = quantileItem.getColumnName();
		colName = StringHandler.doubleQ(colName);
		AnalysisQuantileItemBin elseBinItem = null;

		for (Iterator<AnalysisQuantileItemBin> iterator = binItems.iterator(); iterator
				.hasNext();) {
			AnalysisQuantileItemBin quantileItemBin = iterator.next();
			int bintype = quantileItemBin.getBinType();
			if (bintype == AnalysisQuantileItemBin.BIN_TYPE_REST_VALUES) {
				elseBinItem = quantileItemBin;
				continue;
			}

			sb.append(" when ");
			// for each bin item 's value. using case ...
			if (quantileItemBin instanceof AnalysisQuantileItemBinCategory) {
				buildCategoryWhen(sb, colName, quantileItemBin, bintype);
			} else if (quantileItemBin instanceof AnalysisQuantileItemBinNumeric) {
				AnalysisQuantileItemBinNumeric numericItemBin = (AnalysisQuantileItemBinNumeric) quantileItemBin;
				buildNumericWhen(sb, colName, bintype, numericItemBin);
			} else if (quantileItemBin instanceof AnalysisQuantileItemBinDateTime) {
				buildDateTimeWhen(sb, colName, quantileItemBin, bintype);
			}

			sb.append(" then " + quantileItemBin.getBinIndex() + " ");
		}

		if (elseBinItem != null) {
			sb.append(" else " + elseBinItem.getBinIndex() + " ");
		}
		return sb.toString();
	}

	/**
	 * @param sb
	 * @param colNmae
	 * @param bintype
	 * @param numericItemBin
	 */
	@SuppressWarnings("unchecked")
	private void buildNumericWhen(StringBuffer sb, String colNmae, int bintype,
			AnalysisQuantileItemBinNumeric numericItemBin) {
		if (bintype == AnalysisQuantileItemBin.BIN_TYPE_RANGE) {

			sb.append(colNmae).append(" >= ").append(
					numericItemBin.getStartFrom()).append(" and ");
			sb.append(colNmae).append(" < ").append(numericItemBin.getEndTo())
					.append(" ");

		} else if (bintype == AnalysisQuantileItemBin.BIN_TYPE_COLLECTION) {
			List binValues = numericItemBin.getValues();
			sb.append(colNmae).append(" in (");

			for (int i = 0; i < binValues.size(); i++) {
				String value = (String) binValues.get(i).toString();
				if (i > 0) {
					sb.append(",");
				}
				sb.append(value);
			}
			sb.append(") ");

		}
	}

	/**
	 * @param sb
	 * @param colName
	 * @param quantileItemBin
	 * @param bintype
	 */
	@SuppressWarnings("unchecked")
	private void buildCategoryWhen(StringBuffer sb, String colName,
			AnalysisQuantileItemBin quantileItemBin, int bintype) {
		List binValues = quantileItemBin.getValues();

		if (bintype == AnalysisQuantileItemBin.BIN_TYPE_COLLECTION
				&& binValues != null && binValues.size() > 0) {
			// in these values
			sb.append(colName).append(" in (");

			for (int i = 0; i < binValues.size(); i++) {
				String value = (String) binValues.get(i);
				if (i > 0) {
					sb.append(",");
				}
				sb.append("'").append(value).append("'");
			}
			sb.append(") ");
		}
	}

	/**
	 * @param sb
	 * @param colNmae
	 * @param quantileItemBin
	 * @param bintype
	 */
	private void buildDateTimeWhen(StringBuffer sb, String colNmae,
			AnalysisQuantileItemBin quantileItemBin, int bintype) {
		if (bintype == AnalysisQuantileItemBin.BIN_TYPE_RANGE) {
			AnalysisQuantileItemBinDateTime dataItem = ((AnalysisQuantileItemBinDateTime) quantileItemBin);
			String startDate = dataItem.getStartDate();
			String startTime = dataItem.getStartTime();
			String endDate = dataItem.getEndDate();
			String endTime = dataItem.getEndTime();

			if (startDate == null) {
				startDate = startTime;
			} else if (startTime != null && startTime.trim().length() > 0) {
				startDate = startDate + " " + startTime;
			}

			if (endDate == null) {
				endDate = endTime;
			} else if (endTime != null && endTime.trim().length() > 0) {
				endDate = endDate + " " + endTime;
			}
			sb.append(colNmae).append(">=").append(
					sqlGenerator.to_date(startDate)).append(" and ");
			sb.append(colNmae).append("<")
					.append(sqlGenerator.to_date(endDate)).append(" ");

		} // time only support range...
	}

	/**
	 * @return
	 */
	private StringBuffer buildAutoItemsList(
			List<AnalysisQuantileItem> autoIitems) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < autoIitems.size(); i++) {
			AnalysisQuantileItem item = autoIitems.get(i);
			if (item.isCreateNewColumn() == true) {
				buffer.append(", (ntile(" + item.getNumberOfBin()
						+ ") OVER (ORDER BY \"" + item.getColumnName()
						+ "\")) AS \"" + item.getColumnName().trim()
						+ "_bin\" ");
			} else {
				buffer.append(", (ntile(" + item.getNumberOfBin()
						+ ") OVER (ORDER BY \"" + item.getColumnName()
						+ "\")) AS \"" + item.getColumnName().trim() + "\" ");
			}
		}
		return buffer;
	}
	
	private StringBuffer buildAutoItemsListDB2(List<AnalysisQuantileItem> autoIitems,long size) {
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < autoIitems.size(); i++) {
			AnalysisQuantileItem item = autoIitems.get(i);
			long n=1;
			if(size%item.getNumberOfBin()==0){
				n=size/item.getNumberOfBin();
			}else{
				n=size/item.getNumberOfBin()+1;
			}
			if (item.isCreateNewColumn() == true) {
				buffer.append(", (row_number() over(order by ").append(StringHandler.doubleQ(item.getColumnName()));
				buffer.append(")-1)/").append(n).append("+1 as ");
				buffer.append(StringHandler.doubleQ(item.getColumnName().trim()+"_bin")).append(" ");
			} else {
				buffer.append(", (row_number() over(order by ").append(StringHandler.doubleQ(item.getColumnName()));
				buffer.append(")-1)/").append(n).append("+1 as ");
				buffer.append(StringHandler.doubleQ(item.getColumnName().trim())).append(" ");
			}
		}
		return buffer;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_ANALYSIS_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_ANALYSIS_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

	/**
	 * @param quantileModel
	 */
	private void setQuantileModel(AnalysisQuantileFieldsModel quantileModel) {
		this.quantileModel = quantileModel;
	}

	public AnalysisQuantileFieldsModel getQuantileModel() {
		return quantileModel;
	}

	public AnalysisDerivedFieldsModel getDerivedFieldsModel() {
		return derivedFieldsModel;
	}

	public void setDerivedFieldsModel(
			AnalysisDerivedFieldsModel derivedFieldsModel) {
		this.derivedFieldsModel = derivedFieldsModel;
	}

}
