/**
 * ClassName FPGrowthDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.log.LogUtils;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


/**
 * @author Eason
 */
public class FPGrowthDBColumn extends AbstractFPGrowthDB{
    private static final Logger itsLogger = Logger.getLogger(FPGrowthDBColumn.class);

    DataSet dataSet = null;

	public FPGrowthDBColumn() {
		super();
	}
	
	public ConsumerProducer[] apply(DataSet dataSet, FPGrowthParameter para) throws OperatorException {
		itsLogger.debug(LogUtils.entry("FPGrowthDB", "apply", ""));
		int numericCount = 0;
		int nornimalCount = 0;
		boolean notSupportType = false;
		for(Column column : dataSet.getColumns()){
			if(column.isNumerical())
			{
				numericCount++;
			}else{
				nornimalCount++;
				int valueType = column.getValueType();
				if(DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.TIME)||
					DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.DATE)||
					DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.DATE_TIME)){
					notSupportType = true;
				}
			}
			if(numericCount != 0 && nornimalCount != 0){
				throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.NOT_SUPPORT_DIFF_TYPE_ASSOCIATION, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
			}
			if(notSupportType){
				throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.NOT_SUPPORT_TYPE_ASSOCIATION, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
			}
		}
		this.dataSet = (DataSet) dataSet.clone();
		this.para = para;
		try {
			initParameters(dataSet);
			ItemSets itemSets = new ItemSets(
					dataSet.size(), dataSet);
			itemSets.setPositiveValue(positiveValue);

			databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();

			try {
				// just init it for later use...
				st = databaseConnection.createStatement(false);
				String tempPrifix = Long.toString(System.currentTimeMillis());
				// perform the mining, if necessary, preoject the database
				// first time is 0
				projectedMine(tempPrifix, dataSet, itemSets, 0,
						new ItemSet());

			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage(), e);
			} finally {
				if (st != null) {
					try {
						st.close();
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						throw new OperatorException(e.getLocalizedMessage(), e);
					}
				}
			}

			ConsumerProducer[] res = null;
				res = new ConsumerProducer[] { itemSets };
			itsLogger.debug(LogUtils.exit("FPGrowthDB", "apply", res.toString()));
			return res;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	// count all the items's weight and added into a map
	private Map<Column, Item> getColumnMappingAndColumns(
			DataSet dataSet, String tableName) throws OperatorException,
			SQLException {
		// computing Columns to test, because only boolean column are
		// used
		Map<Column, Item> mapping = new HashMap<Column, Item>();
		int count = 0;
		for (Column column : dataSet.getColumns()) {
			mapping.put(column, new BooleanColumnItem(column));
			count++;

		}
		if (count == 0) {
			return mapping;
		}
		int i = 0;
		Column[] columns = new Column[count];
		for (Column column : dataSet.getColumns()) {
			columns[i] = column;
			i++;
		}
		try {
			ArrayList<Long> freq = getItemsCount(dataSet, tableName);
			for (i = 1; i < dataSet.getColumns().size() + 1; i++) {
				long frequency = 0;
				if (freq.get(i - 1) != null) {
					frequency = freq.get(i - 1);
				}
				mapping.get(columns[i - 1]).increaseFrequency(frequency);
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(
					"getColumnMappingAndColumns error", e);
		} finally {
		}
		return mapping;
	}

	// this will be recrusice invoked in splitMineTree method
	private void projectedMine(String prefix, DataSet dataSet,
			ItemSets itemSets, int recursion,
			ItemSet conditionalItems) throws OperatorException,
			SQLException {

		if (conditionalItems.getNumberOfItems() != 0) {
			itemSets.addFrequentSet(conditionalItems);
		}

		dataSet = (DataSet)dataSet.clone();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();

		// count all the items's weight and added into a map
		Map<Column, Item> itemMapping = getColumnMappingAndColumns(
				dataSet, tableName);
		// if item is not frequent, need remove
		removeNonFrequentItems(itemMapping, dataSet);
		// first time
		if (recursion == 0) {
			intStringItemMapping(itemMapping);// use string as for easy use
		}

		if (true == needSplitTable(dataSet)) {
			splitMineTree(prefix, itemSets, recursion, conditionalItems,
					tableName, itemMapping);
		} else {
			try {
				int count = dataSet.getColumns().size();
				if (count > 0) {
					FPTree tree = getFPTree(dataSet, null, itemMapping);

					mineTree(tree, itemSets, 0, conditionalItems);

				}
				// here the last fptree may be null

			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException("projectedMine error", e);
			}
		}

	}

	private void splitMineTree(String prefix, ItemSets fItemSets,
			int recursion, ItemSet conditionalItems, String tableName,
			Map<Column, Item> itemMapping) throws OperatorException {

		List<BooleanColumnItem> itemSet = new ArrayList<BooleanColumnItem>();

		Iterator<Map.Entry<Column, Item>> it = itemMapping.entrySet()
				.iterator();

		while (it.hasNext()) {
			Map.Entry<Column, Item> entry = it.next();
			if (entry.getValue().getFrequency() >= minTotalSupport) {
				itemSet.add((BooleanColumnItem) entry.getValue());
			}
		}

		Collections.sort(itemSet);
		if (itemSet.size() == 0) {
			return;
		}
		ItemSet topConditionalItems = (ItemSet) conditionalItems
				.clone();
		// first Item
		Item item = stringItemMapping.get(itemSet.get(0).getName());
		topConditionalItems.addItem(item, itemSet.get(0).getFrequency());
		fItemSets.addFrequentSet(topConditionalItems);
		int itemIIndex = 0;
		int itemJIndex = 0;
		BooleanColumnItem itemI = null;
		BooleanColumnItem itemJ = null;
		for (itemIIndex = itemSet.size() - 1; itemIIndex > 0; itemIIndex--) {
			itemI = itemSet.get(itemIIndex);
			// condition Item?
			ItemSet recursiveConditionalItems = (ItemSet) conditionalItems
					.clone();

			Item oldItemI = (BooleanColumnItem) stringItemMapping.get(itemI
					.getName());
			recursiveConditionalItems.addItem(oldItemI, itemI.getFrequency());

			String columnNames = "";
			int itemCount = 0;
			for (itemJIndex = 0; itemJIndex < itemIIndex; itemJIndex++) {
				itemJ = itemSet.get(itemJIndex);

				if (itemCount != 0) {
					columnNames += ",";
				}
				columnNames += StringHandler.doubleQ(itemJ.getName());
				itemCount++;
			}
			Random random = new Random();
			
			StringBuilder newTableName=new StringBuilder();
			newTableName.append("t_").append(itemIIndex).append("_").append(recursion).append("_");
			newTableName.append(Math.abs(random.nextInt())).append(System.currentTimeMillis());
			
			StringBuilder sql=new StringBuilder();
			sql.append("create  view ").append(newTableName).append(" as select ");
			sql.append(columnNames).append(" from ").append(tableName).append(" where ");
			sql.append(StringHandler.doubleQ(itemI.getName()));
			sql.append(getExpression());
			sql.append(getPositiveValueString(itemI.getItem().isNominal()));
			
			try {
				itsLogger.debug("FPGrowthDB.splitMineTree():sql=" + sql);
				st.execute(sql.toString());

				DataSet newDataSetDB = retrieveDataSet(newTableName.toString(),
						true);
				projectedMine(prefix, newDataSetDB, fItemSets,
						recursion + 1, recursiveConditionalItems);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException("splitMineTree error", e);
			}
			sql = new StringBuilder();
			sql.append(" drop view  ").append(newTableName);
			try {
				itsLogger.debug("FPGrowthDB.splitMineTree():sql=" + sql);
				st.execute(sql.toString());
			}catch(Exception e){
				throw new OperatorException(e.getLocalizedMessage());
			}

		}
	}



	private void removeNonFrequentItems(Map<Column, Item> mapping,
			DataSet dataSet) {
		Collection<Column> deleteMappings = new ArrayList<Column>();
		Iterator<Map.Entry<Column, Item>> it = mapping.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Column, Item> entry = it.next();
			if (entry.getValue().getFrequency() < minTotalSupport) {
				deleteMappings.add(entry.getKey());
			}
		}
		for (Column column : deleteMappings) {
			dataSet.getColumns().remove(column);
		}

	}


	/**
	 * Returns a new FPTree, representing the complete DataSet.
	 * 
	 * @param dataSet
	 *            is the dataSet, which shall be represented
	 * @param mapping
	 *            is the mapping of columns of the dataSet to items
	 * @throws SQLException
	 */
	private FPTree getFPTree(DataSet dataSet, double[] positiveIndices,
			Map<Column, Item> mapping) throws SQLException {

		// generate one fptree for a dataSet
		FPTree tree = new FPTree();

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();

		StringBuffer sqlStr = new StringBuffer();// +tableName+";";
		// ItemSet isets=new ItemSet();
		Collection<Item> items = mapping.values();
		List<Item> itemList = new Vector<Item>();
		for (Iterator<Item> iterator = items.iterator(); iterator.hasNext();) {
			Item item = (Item) iterator.next();
			itemList.add(item);
		}
		Collections.sort(itemList);
		StringBuffer groupdByStr = new StringBuffer();

		for (Iterator<Item> iterator = itemList.iterator(); iterator.hasNext();) {
			Item item = (Item) iterator.next();
			// make the column sorted!!!zy
			String itemName = StringHandler.doubleQ(item.toString());
			String caseString = "(case when "+itemName+getExpression()+getPositiveValueString(this.dataSet.getColumns().get(item.toString()).isNominal())+" then 1 else 0 end)"; 
			sqlStr.append(caseString).append(" as ").append(itemName).append(" ,");
			groupdByStr.append(caseString).append(" ,");
		}
		groupdByStr.deleteCharAt(groupdByStr.length() - 1);
		// the count column name when creat the FPTree
		String columnCount = "count";
		sqlStr.append("count(*) as ").append(StringHandler.doubleQ(columnCount));

		sqlStr = sqlStr.append(" from ").append(tableName);

		String sql = "Select " + sqlStr + " Group by " + groupdByStr ;
		itsLogger.debug("FPGrowthDB.getFPTree():sql=" + sql);
		ResultSet res = st.executeQuery(sql);
		List<Item> itemSet = new ArrayList<Item>();

		while (res.next()) {
			itemSet.clear();

			int i = 0;
			for (Column currentColumn : dataSet.getColumns()) {
				int value = res.getInt(currentColumn.getName());
				// true
				if (Double.isNaN(value))
					continue;

				if (value == 1) {
					itemSet.add(mapping.get(currentColumn));
				}
				i++;
			}
			long weight = res.getLong(columnCount);
			// Collections.sort(itemSet);--notneeded because the result is
			// already ok
			// it is very important to count the weight...
			tree.addItemSet(itemSet, weight);
		}
		return tree;
	}



	private boolean needSplitTable(DataSet dataSet) {
		int columnCount = dataSet.getColumns().size();
		return dataSet.size() * columnCount > threshold && columnCount != 1;
	}

	private void intStringItemMapping(Map<Column, Item> itemMapping) {
		Iterator<Map.Entry<Column, Item>> it = itemMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Column, Item> entry = it.next();
			stringItemMapping.put(entry.getKey().getName(),
					(BooleanColumnItem) entry.getValue());
		}
	}

	private ArrayList<Long> getItemsCount(DataSet dataSet, String tableName) throws SQLException {
		ArrayList<Long> freq = new ArrayList<Long>();
		StringBuffer sql = new StringBuffer();
		boolean first = true;
		int resultSetLength = 0;
		int i = 0;
		for (Column column : dataSet.getColumns()) {
			if (first) {
				sql.setLength(0);
				sql.append("select  ");
				resultSetLength = 0;
				first = false;
			} else {
				sql.append(",");
			}
			sql.append(" sum( case when ").append(
					StringHandler.doubleQ(column.getName()));
			sql.append(getExpression()).append(getPositiveValueString(column.isNominal()));
			sql.append(" then 1 else 0 end)");
			resultSetLength++;

			if ((i + 1) % AlpineDataAnalysisConfig.FP_SELECT_COUNT == 0
					|| i + 1 == dataSet.getColumns().size()) {
				sql.append("  from ").append(tableName);
				first = true;

				itsLogger.debug("FPGrowthDB.getColumnMappingAndColumns():sql="
						+ sql);
				ResultSet rs = st.executeQuery(sql.toString());
				if (rs.next()) {
					for (int j = 0; j < resultSetLength; j++) {
						freq.add(rs.getLong(j + 1));
					}
				} else {
					for (int j = 0; j < resultSetLength; j++) {
						freq.add(rs.getLong(0));
					}
				}
				rs.close();
			}
			i++;
		}
		
		return freq;
	}
}
