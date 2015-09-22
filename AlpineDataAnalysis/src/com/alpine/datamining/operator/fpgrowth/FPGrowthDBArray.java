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

import java.math.BigDecimal;
import java.sql.Array;
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

import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.log.LogUtils;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


/**
 * @author Eason
 */
public class FPGrowthDBArray extends AbstractFPGrowthDB {
    private static Logger itsLogger = Logger.getLogger(FPGrowthDBArray.class);

    private int arrayLength = 0;
	private IDataSourceInfo dataSourceInfo = null;
	private IMultiDBUtility multiDBUtility ;

	public FPGrowthDBArray() {
		super();
	}

	public ConsumerProducer[] apply(DataSet dataSet, FPGrowthParameter para) throws OperatorException {
		itsLogger.debug(LogUtils.entry("FPGrowthDB", "apply", ""));
		this.para = para;
		dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
		multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
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
				
				getArrayLength(((DBTable) dataSet
						.getDBTable()).getTableName());
				// perform the mining, if necessary, preoject the database
				// first time is 0
				 ArrayList<Integer> arrayIndexes = new ArrayList<Integer>();
				 for(int i = 1; i <= arrayLength; i++){
					 arrayIndexes.add(i);
				 }

				String tempPrifix = Long.toString(System.currentTimeMillis());
				projectedMine(tempPrifix, dataSet, itemSets, 0,
						new ItemSet(), arrayIndexes);

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
	private int getArrayLength(String tableName) throws SQLException{
		String sql = null;
		if (dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType))
		{
			sql = "select max(ALPINE_MINER_ARRAY_COUNT("+StringHandler.doubleQ(columnName)+")) from "+tableName;
		}else{
			 sql = "select max(array_upper("+StringHandler.doubleQ(columnName)+",1)) from "+tableName;
		}
		itsLogger.debug("FPGrowthDB.getArrayLength():sql="+sql);
		ResultSet rs = st.executeQuery(sql);
		if(rs.next()){
			arrayLength = rs.getInt(1);
		}
		return arrayLength;
	}
	// count all the items's weight and added into a map
	private Map<Integer, Item> getColumnMappingAndColumns(
			DataSet dataSet, String tableName, ArrayList<Integer> arrayIndexes, ArrayList<Integer> localArrayIndexes) throws OperatorException,
			SQLException {
		// computing Columns to test, because only boolean column are
		// used
		Map<Integer, Item> mapping = new HashMap<Integer, Item>();
		int count = 0;
		for (int i = 0; i < arrayIndexes.size(); i++) {
			String attrbiuteName = null;

			attrbiuteName = columnName+"["+(arrayIndexes.get(i))+"]";
			mapping.put(i+1, new BooleanColumnItem(attrbiuteName));
			count++;

		}
		if (count == 0) {
			return mapping;
		}

		try {
			ArrayList<Long> freq = getItemsCount(dataSet, tableName,
					localArrayIndexes);
			for (int i = 1; i < arrayIndexes.size() + 1; i++) {
				long frequency = 0;
				if (freq.get(i - 1) != null) {
					frequency = freq.get(i - 1);
				}
				mapping.get(i).increaseFrequency(frequency);
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
			ItemSet conditionalItems,
			 ArrayList<Integer> arrayIndexes) throws OperatorException,
			SQLException {

		if (conditionalItems.getNumberOfItems() != 0) {
			itemSets.addFrequentSet(conditionalItems);
		}

		dataSet = (DataSet)dataSet.clone();
		ArrayList<Integer> oldArrayIndexes = arrayIndexes;
		ArrayList<Integer> oldLocalArrayIndexes = new ArrayList<Integer> ();
		ArrayList<Integer> localArrayIndexes = new ArrayList<Integer> ();

		ArrayList<Integer> newArrayIndexes = new ArrayList<Integer>();
		for(int i = 0; i < arrayIndexes.size(); i++){
			newArrayIndexes.add(arrayIndexes.get(i));
			localArrayIndexes.add(i+1);
			oldLocalArrayIndexes.add(i+1);
		}
		arrayIndexes = newArrayIndexes;
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		// count all the items's weight and added into a map
		Map<Integer, Item> itemMapping = getColumnMappingAndColumns(
				dataSet, tableName,arrayIndexes, localArrayIndexes);
		// if item is not frequent, need remove
		removeNonFrequentItems(itemMapping, dataSet,  arrayIndexes,localArrayIndexes);
		// first time
		if (recursion == 0) {
			intStringItemMapping(itemMapping);// use string as for easy use
		}

		if (true == needSplitTable(dataSet, arrayIndexes)) {
			splitMineTree(prefix, itemSets, recursion, conditionalItems,
					tableName, itemMapping, arrayIndexes, localArrayIndexes);
		} else {
			try {
				int count = arrayIndexes.size();
				if (count > 0) {
					FPTree tree = getFPTree(dataSet, null, itemMapping, arrayIndexes,localArrayIndexes,oldArrayIndexes,oldLocalArrayIndexes);

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
			Map<Integer, Item> itemMapping, ArrayList<Integer> arrayIndexes, ArrayList<Integer> localArrayIndexes) throws OperatorException {

		List<BooleanColumnItem> itemSet = new ArrayList<BooleanColumnItem>();

		Iterator<Map.Entry<Integer, Item>> it = itemMapping.entrySet()
				.iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Item> entry = it.next();
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
		BooleanColumnItem itemI = null;
		for (itemIIndex = itemSet.size() - 1; itemIIndex > 0; itemIIndex--) {
			itemI = itemSet.get(itemIIndex);
			// condition Item?
			ItemSet recursiveConditionalItems = (ItemSet) conditionalItems
					.clone();

			Item oldItemI = (BooleanColumnItem) stringItemMapping.get(itemI
					.getName());
			recursiveConditionalItems.addItem(oldItemI, itemI.getFrequency());
			ArrayList<Integer> newArrayIndexes = new ArrayList<Integer>();
			String columnNames = getColumnNames(arrayIndexes,
					localArrayIndexes, itemSet, itemIIndex, newArrayIndexes);
			Random random = new Random();
			
			StringBuilder newTableName=new StringBuilder();
			newTableName.append("t_").append(itemIIndex).append("_").append(recursion).append("_");
			newTableName.append(Math.abs(random.nextInt())).append(System.currentTimeMillis());
			
			StringBuilder sql=new StringBuilder();
			sql.append("create  view ").append(newTableName).append(" as select ");
			sql.append(columnNames);
			sql.append(" as ").append(StringHandler.doubleQ(columnName));
			sql.append(" from ").append(tableName).append(" where ");
			sql.append(getArrayColumnName(localArrayIndexes.get(arrayIndexes.indexOf(arrayGetIndex(itemI.getName())))));
			sql.append(getExpression());
			sql.append(getPositiveValueString(false));
			
			try {
				itsLogger.debug("FPGrowthDB.splitMineTree():sql=" + sql);
				st.execute(sql.toString());

				DataSet newDataSetDB = retrieveDataSet(newTableName.toString(),
						true);
				projectedMine(prefix, newDataSetDB, fItemSets,
						recursion + 1, recursiveConditionalItems, newArrayIndexes);
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

	private String getColumnNames(ArrayList<Integer> arrayIndexes,
			ArrayList<Integer> localArrayIndexes,
			List<BooleanColumnItem> itemSet, int itemIIndex,
			ArrayList<Integer> newArrayIndexes) {
		BooleanColumnItem itemJ;
		int itemJIndex = 0;
		String columnNames = "";
		if (dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			ArrayList<String> array = new ArrayList<String>();
			for (itemJIndex = 0; itemJIndex < itemIIndex; itemJIndex++) {
				itemJ = itemSet.get(itemJIndex);
				array.add(getArrayColumnName(localArrayIndexes.get(arrayIndexes.indexOf(arrayGetIndex(itemJ.getName())))));
				columnNames = CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float).toString();
				newArrayIndexes.add(arrayGetIndex(itemJ.getName()));
			}
		}
		else{
			columnNames += multiDBUtility.intArrayHead();
			int itemCount = 0;
			for (itemJIndex = 0; itemJIndex < itemIIndex; itemJIndex++) {
				itemJ = itemSet.get(itemJIndex);

				if (itemCount != 0) {
					columnNames += ",";
				}
				columnNames += getArrayColumnName(localArrayIndexes.get(arrayIndexes.indexOf(arrayGetIndex(itemJ.getName()))));
				newArrayIndexes.add(arrayGetIndex(itemJ.getName()));
				itemCount++;
			}
			columnNames += multiDBUtility.intArrayTail();
		}
		return columnNames;
	}

	private void removeNonFrequentItems(Map<Integer, Item> itemMapping,
			DataSet dataSet,ArrayList<Integer> arrayIndexes,ArrayList<Integer> localArrayIndexes) {
		Collection<Integer> deleteMappings = new ArrayList<Integer>();
		Iterator<Map.Entry<Integer, Item>> it = itemMapping.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Item> entry = it.next();
			if (entry.getValue().getFrequency() < minTotalSupport) {
				deleteMappings.add(entry.getKey());
			}
		}
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for (Integer column : deleteMappings) {
			remove.add(arrayIndexes.get(column.intValue() - 1));
			localArrayIndexes.remove(column);
		}
		arrayIndexes.removeAll(remove);
	}


	/**
	 * Returns a new FPTree, representing the complete DataSet.
	 * 
	 * @param dataSet
	 *            is the dataSet, which shall be represented
	 * @param itemMapping
	 *            is the mapping of column of the dataSet to items
	 * @throws SQLException
	 */
	private FPTree getFPTree(DataSet dataSet, double[] positiveIndices,
			Map<Integer, Item> itemMapping,ArrayList<Integer> arrayIndexes,ArrayList<Integer> localArrayIndexes,ArrayList<Integer> oldArrayIndexes,ArrayList<Integer> oldLocalArrayIndexes) throws SQLException {

		// generate one fptree for a dataSet
		FPTree tree = new FPTree();

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();

		// ItemSet isets=new ItemSet();
		Collection<Item> items = itemMapping.values();
		List<Item> itemList = new Vector<Item>();
		for (Iterator<Item> iterator = items.iterator(); iterator.hasNext();) {
			Item item = (Item) iterator.next();
			itemList.add(item);
		}
		Collections.sort(itemList);

		StringBuffer sqlStr = new StringBuffer();// +tableName+";";
		StringBuffer groupByStr = new StringBuffer();
		if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			ArrayList<String> array = new ArrayList<String>();
			boolean first = true;
			for (Iterator<Item> iterator = itemList.iterator(); iterator.hasNext();) {
				if (first){
					first = false;
				}else{
					groupByStr.append(",");
				}
				Item item = (Item) iterator.next();
				String value = "(case when "+getArrayColumnName(oldLocalArrayIndexes.get(oldArrayIndexes.indexOf(arrayGetIndex(item.toString()))))+getExpression()+getPositiveValueString(false)+" then 1 else 0 end)";;
				array.add(value);
				groupByStr.append(value);
			}
			sqlStr = CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Integer);
		}else{
			for (Iterator<Item> iterator = itemList.iterator(); iterator.hasNext();) {
				Item item = (Item) iterator.next();
				// make the column sorted!!!zy
				String itemName = getArrayColumnName(oldLocalArrayIndexes.get(oldArrayIndexes.indexOf(arrayGetIndex(item.toString()))));
				sqlStr.append(" (case when ").append((itemName)).append(getExpression()).append(getPositiveValueString(false)).append(" then 1 else 0 end),");
			}
			sqlStr = sqlStr.deleteCharAt(sqlStr.length() - 1).append(multiDBUtility.stringArrayTail()).insert(0,multiDBUtility.stringArrayHead());
			groupByStr = new StringBuffer(sqlStr);
		}
		// the count column name when creat the FPTree
		String columnCount = "count";
		sqlStr.append(",").append("count(*) as ").append(StringHandler.doubleQ(columnCount));
		sqlStr.append(" from ").append(tableName);
		String sql = "Select " + sqlStr + " Group by " + groupByStr ;
		itsLogger.debug("FPGrowthDB.getFPTree():sql=" + sql);
		ResultSet res = st.executeQuery(sql);
		List<Item> itemSet = new ArrayList<Item>();

		while (res.next()) {
			int[] result = getColumnResult(res, itemList.size());
			itemSet.clear();
			for(Integer index : oldLocalArrayIndexes){
				if(!localArrayIndexes.contains(index)){
					continue;
				}
				int i = 0;
				for (Iterator<Item> iterator = itemList.iterator(); iterator.hasNext();) {
					i++;
					Item item = (Item) iterator.next();
					if (oldLocalArrayIndexes.get(oldArrayIndexes.indexOf(arrayGetIndex(item.toString()))).intValue() == index.intValue()){
						break;
					}
				}
				int value = result[i - 1];
				// true
				if (Double.isNaN(value))
				{	
					continue;
				}
				if (value  == 1) {
					Item item = itemMapping.get(index);
					itemSet.add(item);
				}

			}
			long weight = res.getLong(columnCount);
			// Collections.sort(itemSet);--notneeded because the result is
			// already ok
			// it is very important to count the weight...
			tree.addItemSet(itemSet, weight);
		}
		return tree;
	}

	private int[] getColumnResult(ResultSet res, int length) throws SQLException {
		int[] result = null;
		if (dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType))
		{
			result = new int[length];
			Object [] resResult = (Object[])res.getArray(1).getArray();
			int totalCount = 0;
			if (resResult != null){
				for (int i = 0; i < resResult.length; i++){
					BigDecimal[] array =(BigDecimal[])((Array)resResult[i]).getArray();
					if (array != null){
						for (int j = 0; j < array.length; j++) {
							if (array[j] != null){
								result[totalCount + j] = array[j].intValue();
							}
						}
						totalCount += array.length;
					}
				}
			}
		}else{
			
			Integer[] temp = (Integer[])res.getArray(1).getArray();
			result = new int[temp.length];
			for (int i = 0; i < temp.length; i++){
				result[i] = temp[i];
			}
		}
		return result;
	}

	private String getArrayColumnName(int i) {
		String ret = null;
		if (dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			ret = "alpine_miner_get_faa_element("+StringHandler.doubleQ(columnName)+","+i+")";
		}else{
			ret = StringHandler.doubleQ(columnName)+"["+i+"]";
		}
		return ret;
	}



	private boolean needSplitTable(DataSet dataSet,ArrayList<Integer> arrayIndexes) {
		int columnCount = arrayIndexes.size();
		return dataSet.size() * columnCount > threshold && columnCount != 1;
	}

	private void intStringItemMapping(Map<Integer, Item> itemMapping) {
		Iterator<Map.Entry<Integer, Item>> it = itemMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Item> entry = it.next();
			stringItemMapping.put(((BooleanColumnItem) entry.getValue()).getName(),
					(BooleanColumnItem) entry.getValue());
		}
	}

	private ArrayList<Long> getItemsCount(DataSet dataSet, String tableName,
			ArrayList<Integer> localArrayIndexes) throws SQLException {
		ArrayList<Long> freq = new ArrayList<Long>();
		StringBuffer sql = new StringBuffer();
		boolean first = true;
		int resultSetLength = 0;
		int i = 0;
		for (i = 0; i < localArrayIndexes.size(); i++) {
			if (first) {
				sql.setLength(0);
				sql.append("select  ");
				resultSetLength = 0;
				first = false;
			} else {
				sql.append(",");
			}
			sql.append(" sum( case when ").append(
					getArrayColumnName(localArrayIndexes.get(i)));
			sql.append(getExpression()).append(getPositiveValueString(false));
			sql.append(" then 1 else 0 end)");
			resultSetLength++;

			if ((i + 1) % AlpineDataAnalysisConfig.FP_SELECT_COUNT == 0
					|| i + 1 == localArrayIndexes.size()) {
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
		}
		return freq;
	}
	private int arrayGetIndex(String array){
		int lastIndex = array.lastIndexOf("[");
		int index = Integer.parseInt(array.substring(lastIndex + 1, array.length() - 1));
		return index;
	}

}
