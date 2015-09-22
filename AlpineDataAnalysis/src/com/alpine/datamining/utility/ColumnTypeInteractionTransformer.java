/**
 * ClassName ColumnTypeInteractionTransformer
 *
 * Version information: 1.00
 *
 * Data: 2011-5-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Table;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class ColumnTypeInteractionTransformer extends
		ColumnTypeTransformer {
    private static final Logger itsLogger = Logger.getLogger(ColumnTypeInteractionTransformer.class);

    private static final long serialVersionUID = 7167040593564321893L;

	private ArrayList<String> columnNames;
	private AnalysisInterActionColumnsModel AnalysisInterActionModel = null;
	private HashMap<String, String> interactionColumnExpMap = new HashMap<String, String>();
	private HashMap<String, String[]> interactionColumnColumnMap = new HashMap<String, String[]>();


//	private HashMap<String, HashMap<String, String>> allTransformInteractionMap_columnKey = new HashMap<String, HashMap<String, String>>();


	public ArrayList<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}


	public HashMap<String, String> getInteractionColumnExpMap() {
		return interactionColumnExpMap;
	}

	public void setInteractionColumnExpMap(
			HashMap<String, String> interactionColumnExpMap) {
		this.interactionColumnExpMap = interactionColumnExpMap;
	}

	public HashMap<String, String[]> getInteractionColumnColumnMap() {
		return interactionColumnColumnMap;
	}

	public void setInteractionColumnColumnMap(
			HashMap<String, String[]> interactionColumnColumnMap) {
		this.interactionColumnColumnMap = interactionColumnColumnMap;
	}

	public AnalysisInterActionColumnsModel getAnalysisInterActionModel() {
		return AnalysisInterActionModel;
	}

	public void setAnalysisInterActionModel(AnalysisInterActionColumnsModel analysisInterActionModel) {
		this.AnalysisInterActionModel = analysisInterActionModel;
	}

	public DataSet TransformCategoryToNumeric_new(DataSet dataSet,String groupByColumn)
			throws OperatorException {
		Columns atts = dataSet.getColumns();
		Iterator<Column> atts_i = atts.iterator();
		ArrayList<Column> needToTransform_list = new ArrayList<Column>();
		while (atts_i.hasNext()) {
			Column att = atts_i.next();
			if (!att.isNumerical())
				needToTransform_list.add(att);
		}
		List<AnalysisInterActionItem> analysisInterActionItems = new ArrayList<AnalysisInterActionItem>();
		if(AnalysisInterActionModel != null){
			analysisInterActionItems = AnalysisInterActionModel.getInterActionItems();
		}
		Collections.sort(analysisInterActionItems,new Comparator<AnalysisInterActionItem>() {
		      @Override
		      public int compare(AnalysisInterActionItem o1, AnalysisInterActionItem o2) {
		        return  Integer.valueOf(o1.getId()).compareTo(Integer.valueOf(o2.getId()));
		      }
		    });
		if (needToTransform_list.size() == 0 && analysisInterActionItems.size() == 0) {
			transform = false;
			return dataSet;
		} else {
			transform = true;
		}
		ArrayList<String> existingColumns = new ArrayList<String>();
		existingColumns.addAll(columnNames);
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		String randomName = StringHandler.doubleQ("AlpTmp"
				+ System.currentTimeMillis());
		String dbType = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection().getProperties().getName();
		
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		// if(dbType.equals(DataSourceInfoOracle.dBType)){
		// useArray = false;
		// }
		if (tableName.contains(".")) {
			if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				tempTableName = StringHandler.doubleQ(StringHandler
						.removeMiddleDoubleQ(tableName.split("\\.")[0]))
						+ "." + randomName;
			}
			else {
				tempTableName = randomName;
			}
		} else {
			tempTableName = randomName;
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Statement st = null;

		ArrayList<String> nullList = new ArrayList<String>();
		if(needToTransform_list.size() != 0){
			nullList = countNullColumn(databaseConnection,
				tableName, atts);
		}
		String alpineNull = "AlpineNull";
		ArrayList<Integer> individualColumnIndex = new ArrayList<Integer>();
		ArrayList<String> newColumnNames = new ArrayList<String>();
		ArrayList<String> newColumnExps = new ArrayList<String>();
//		HashMap<String, String> newColumnExpMap = new HashMap<String, String>();
		if (analysisInterActionItems != null
				&& analysisInterActionItems.size() != 0) {
			for (int i = 0; i < analysisInterActionItems.size(); i++) {
				AnalysisInterActionItem variableInteraction = analysisInterActionItems
						.get(i);
				String firstColumn = variableInteraction.getFirstColumn();
				String interactionType = variableInteraction.getInteractionType();
				String secondColumn = variableInteraction.getSecondColumn();
				if(interactionType.trim().equals("*")){
					existingColumns.add(firstColumn);
					existingColumns.add(secondColumn);
				}
			}
			for (int i = 0; i < analysisInterActionItems.size(); i++) {
				AnalysisInterActionItem variableInteraction = analysisInterActionItems
						.get(i);
				String firstColumn = variableInteraction.getFirstColumn();
				String interactionType = variableInteraction.getInteractionType();
				String secondColumn = variableInteraction.getSecondColumn();
				Column firstAtt = atts.get(firstColumn);
				Column secondAtt = atts.get(secondColumn);
					if (firstAtt.isNumerical()) {
						if (secondAtt.isNumerical()) {
							String newcolumnExp = StringHandler.doubleQ(firstColumn)
							+ "*" + StringHandler.doubleQ(secondColumn);
							String newColumnName = firstColumn + ":" + secondColumn;
							if(!newColumnNames.contains(newColumnName)){
								newColumnExps.add(newcolumnExp);
								newColumnNames.add(newColumnName);
							}
//							newColumnExpMap.put(newColumnName, newcolumnExp);
							interactionColumnExpMap.put(newColumnName, newcolumnExp);
							interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
							if (interactionType.trim().equals("*")) {
								if(!columnNames.contains(firstColumn)){
									if(!newColumnNames.contains(firstColumn)){
										individualColumnIndex.add(newColumnNames.size());
										newColumnExps.add(StringHandler.doubleQ(firstColumn));
										newColumnNames.add(firstColumn);
									}
//									newColumnExpMap.put(firstColumn, StringHandler.doubleQ(firstColumn));

								}
								if(!columnNames.contains(secondColumn)){
									if(!newColumnNames.contains(secondColumn)){
										individualColumnIndex.add(newColumnNames.size());
										newColumnExps.add(StringHandler.doubleQ(secondColumn));
										newColumnNames.add(secondColumn);
									}
//									newColumnExpMap.put(secondColumn, StringHandler.doubleQ(secondColumn));

								}
							}
						} else {
							int count = 0;
							List<String> valueList = secondAtt.getMapping()
									.getValues();
							Iterator<String> valueList_i = valueList.iterator();
							String columnName = StringHandler.doubleQ(secondColumn);
							while (valueList_i.hasNext()) {
								String value = valueList_i.next();
								if (valueList.size() != 1
										&& count == valueList.size() - 1
										&& !nullList.contains(columnName) && existingColumns.contains(firstColumn)) {
									continue;
								}
								String valueQ = CommonUtility.quoteValue(
										dbType, secondAtt, value);
								String newcolumnExp = StringHandler.doubleQ(firstColumn)+"*( case when " + columnName
								+ "=" + valueQ
								+ " then 1.0 else 0.0 end )";
								String newColumnName = firstColumn + ":" + secondColumn + "_"
								+ value;
								if(!newColumnNames.contains(newColumnName)){
									newColumnExps.add(newcolumnExp);
									newColumnNames.add(newColumnName);
								}
//								newColumnExpMap.put(newColumnName, newcolumnExp);

								interactionColumnExpMap.put(newColumnName, newcolumnExp);
								interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
								count++;
							}
							if (nullList.contains(columnName) && !existingColumns.contains(firstColumn)){
								String newcolumnExp = StringHandler.doubleQ(firstColumn)+"*( case when " + columnName
								+ " is null  then 1.0 else 0.0 end )";
								String newColumnName = firstColumn + ":" + secondColumn + "_"
								+ alpineNull;
								if(!newColumnNames.contains(newColumnName)){
									newColumnExps.add(newcolumnExp);
									newColumnNames.add(newColumnName);
								}
								interactionColumnExpMap.put(newColumnName, newcolumnExp);
								interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
							}
							if (interactionType.trim().equals("*")) {
								if(!columnNames.contains(firstColumn)){
									if(!newColumnNames.contains(firstColumn)){
										individualColumnIndex.add(newColumnNames.size());
										newColumnExps.add(StringHandler.doubleQ(firstColumn));
										newColumnNames.add(firstColumn);
									}
	//									newColumnExpMap.put(firstColumn, StringHandler.doubleQ(firstColumn));
								}
								if(!columnNames.contains(secondColumn)){
									HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
									HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
									count = 0;
									valueList = secondAtt.getMapping()
											.getValues();
									valueList_i = valueList.iterator();
									columnName = StringHandler.doubleQ(secondColumn);
									while (valueList_i.hasNext()) {
										String value = valueList_i.next();
										if (valueList.size() != 1
												&& count == valueList.size() - 1
												&& !nullList.contains(columnName)) {
											continue;
										}
										value = StringHandler.escQ(value);
										String valueQ = CommonUtility.quoteValue(
												dbType, secondAtt, value);
										if(!newColumnNames.contains(secondColumn + "_"
												+ value)){
											individualColumnIndex.add(newColumnNames.size());
											newColumnExps.add("( case when " + columnName
													+ "=" + valueQ
													+ " then 1.0 else 0.0 end )");
											newColumnNames.add(secondColumn + "_"
													+ value);
										}
//										newColumnExpMap.put(secondColumn + "_"
//												+ value, "( case when " + columnName
//												+ "=" + valueQ
//												+ " then 1.0 else 0.0 end )");

										transformMap_valueKey.put(value, secondAtt.getName() + "_"
												+ value);
										transformMap_columnKey.put(secondAtt.getName() + "_" + value,
												value);

										count++;
									}
									allTransformMap_valueKey.put(secondAtt.getName(),
											transformMap_valueKey);
									allTransformMap_columnKey.put(secondAtt.getName(),
											transformMap_columnKey);

								}
							}
						}
					} else {
						if (secondAtt.isNumerical()) {
							int count = 0;
							List<String> valueList = firstAtt.getMapping()
									.getValues();
							Iterator<String> valueList_i = valueList.iterator();
							String columnName = StringHandler.doubleQ(firstColumn);
							while (valueList_i.hasNext()) {
								String value = valueList_i.next();
								if (valueList.size() != 1
										&& count == valueList.size() - 1
										&& !nullList.contains(columnName) && existingColumns.contains(secondColumn)) {
									continue;
								}
								value = StringHandler.escQ(value);
								String valueQ = CommonUtility.quoteValue(
										dbType, firstAtt, value);
								String newcolumnExp = StringHandler.doubleQ(secondColumn)+"*( case when " + columnName
								+ "=" + valueQ
								+ " then 1.0 else 0.0 end )";
								String newColumnName = firstColumn + "_"
								+ value+  ":" +secondColumn;
								if(!newColumnNames.contains(newColumnName)){
									newColumnExps.add(newcolumnExp);
									newColumnNames.add(newColumnName);
								}
//								newColumnExpMap.put(newColumnName, newcolumnExp);

								interactionColumnExpMap.put(newColumnName, newcolumnExp);
								interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
								count++;
							}
							if (nullList.contains(columnName) && !existingColumns.contains(secondColumn)){
								String newcolumnExp = StringHandler.doubleQ(secondColumn)+"*( case when " + columnName
								+ " is null  then 1.0 else 0.0 end )";
								String newColumnName = firstColumn + "_"
								+ alpineNull+  ":" +secondColumn;
								if(!newColumnNames.contains(newColumnName)){
									newColumnExps.add(newcolumnExp);
									newColumnNames.add(newColumnName);
								}
								interactionColumnExpMap.put(newColumnName, newcolumnExp);
								interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
							}

							if (interactionType.trim().equals("*")) {
								if(!columnNames.contains(firstColumn)){
									HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
									HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();

									count = 0;
									valueList = firstAtt.getMapping()
											.getValues();
									valueList_i = valueList.iterator();
									columnName = StringHandler.doubleQ(firstColumn);
									while (valueList_i.hasNext()) {
										String value = valueList_i.next();
										if (valueList.size() != 1
												&& count == valueList.size() - 1
												&& !nullList.contains(columnName)) {
											continue;
										}
										value = StringHandler.escQ(value);
										String valueQ = CommonUtility.quoteValue(
												dbType, firstAtt, value);
										if(!newColumnNames.contains(firstColumn + "_"
												+ value)){
											individualColumnIndex.add(newColumnNames.size());
											newColumnExps.add("( case when " + columnName
													+ "=" + valueQ
													+ " then 1.0 else 0.0 end )");
											newColumnNames.add(firstColumn + "_"
													+ value);
										}
//										newColumnExpMap.put(firstColumn + "_"
//												+ value, "( case when " + columnName
//												+ "=" + valueQ
//												+ " then 1.0 else 0.0 end )");

										transformMap_valueKey.put(value, firstAtt.getName() + "_"
												+ value);
										transformMap_columnKey.put(firstAtt.getName() + "_" + value,
												value);

										count++;
									}
									allTransformMap_valueKey.put(firstAtt.getName(),
											transformMap_valueKey);
									allTransformMap_columnKey.put(firstAtt.getName(),
											transformMap_columnKey);

								}
								if(!columnNames.contains(secondColumn)){
									if(!newColumnNames.contains(secondColumn)){
										individualColumnIndex.add(newColumnNames.size());
										newColumnExps.add(StringHandler.doubleQ(secondColumn));
										newColumnNames.add(secondColumn);
									}
//									newColumnExpMap.put(secondColumn, StringHandler.doubleQ(secondColumn));

								}
							}

						} else {
							int count = 0;
							List<String> valueList = firstAtt.getMapping()
									.getValues();
							Iterator<String> valueList_i = valueList.iterator();
							String columnName = StringHandler.doubleQ(firstColumn);
							while (valueList_i.hasNext()) {
								String value = valueList_i.next();
								if (valueList.size() != 1
										&& count == valueList.size() - 1
										&& !nullList.contains(columnName)&& existingColumns.contains(secondColumn)) {
									continue;
								}
								value = StringHandler.escQ(value);
								String valueQ = CommonUtility.quoteValue(
										dbType, firstAtt, value);
								int countR = 0;
								List<String> valueListR = secondAtt.getMapping()
										.getValues();
								Iterator<String> valueListR_i = valueListR.iterator();
								String columnNameR = StringHandler.doubleQ(secondColumn);
								while (valueListR_i.hasNext()) {
									String valueR = valueListR_i.next();
									if (valueListR.size() != 1
											&& countR == valueListR.size() - 1
											&& !nullList.contains(columnNameR)&& existingColumns.contains(firstColumn)) {
										continue;
									}
									valueR = StringHandler.escQ(valueR);
									String valueQR = CommonUtility.quoteValue(
											dbType, secondAtt, valueR);
									String newcolumnExp = " (case when " + columnName
									+ "=" + valueQ
									+ " then 1.0 else 0.0 end )*( case when " + columnNameR
									+ "=" + valueQR
									+ " then 1.0 else 0.0 end )";
									String newColumnName = firstColumn+ "_"
									+ value + ":" + secondColumn + "_"
									+ valueR;
									if(!newColumnNames.contains(newColumnName)){
										newColumnExps.add(newcolumnExp);
										newColumnNames.add(newColumnName);
									}
//									newColumnExpMap.put(newColumnName, newcolumnExp);

									interactionColumnExpMap.put(newColumnName, newcolumnExp);
									interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
									countR++;
								}
								if (nullList.contains(columnNameR) && !existingColumns.contains(firstColumn)){
									String newcolumnExp = " (case when " + columnName
									+ "=" + valueQ
									+ " then 1.0 else 0.0 end )*( case when " + columnNameR
									+ " is null  then 1.0 else 0.0 end )";
									String newColumnName = firstColumn+ "_"
									+ value + ":" + secondColumn + "_"
									+ alpineNull;
									if(!newColumnNames.contains(newColumnName)){
										newColumnExps.add(newcolumnExp);
										newColumnNames.add(newColumnName);
									}
									interactionColumnExpMap.put(newColumnName, newcolumnExp);
									interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
								}
								count++;
							}
							
							if (nullList.contains(columnName) && !existingColumns.contains(secondColumn)){
								int countR = 0;
								List<String> valueListR = secondAtt.getMapping()
										.getValues();
								Iterator<String> valueListR_i = valueListR.iterator();
								String columnNameR = StringHandler.doubleQ(secondColumn);
								while (valueListR_i.hasNext()) {
									String valueR = valueListR_i.next();
									if (valueListR.size() != 1
											&& countR == valueListR.size() - 1
											&& !nullList.contains(columnNameR)&& existingColumns.contains(firstColumn)) {
										continue;
									}
									valueR = StringHandler.escQ(valueR);
									String valueQR = CommonUtility.quoteValue(
											dbType, secondAtt, valueR);
									String newcolumnExp = " (case when " + columnName
									+ " is null "
									+ " then 1.0 else 0.0 end )*( case when " + columnNameR
									+ "=" + valueQR
									+ " then 1.0 else 0.0 end )";
									String newColumnName = firstColumn+ "_"
									+ alpineNull + ":" + secondColumn + "_"
									+ valueR;
									if(!newColumnNames.contains(newColumnName)){
										newColumnExps.add(newcolumnExp);
										newColumnNames.add(newColumnName);
									}
//									newColumnExpMap.put(newColumnName, newcolumnExp);

									interactionColumnExpMap.put(newColumnName, newcolumnExp);
									interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
									countR++;
								}
								if (nullList.contains(columnName) && !existingColumns.contains(firstColumn)){
									String newcolumnExp = " (case when " + columnName
									+ " is null "
									+ " then 1.0 else 0.0 end )*( case when " + columnNameR
									+ " is null  then 1.0 else 0.0 end )";
									String newColumnName = firstColumn+ "_"
									+ alpineNull + ":" + secondColumn + "_"
									+ alpineNull;
									if(!newColumnNames.contains(newColumnName)){
										newColumnExps.add(newcolumnExp);
										newColumnNames.add(newColumnName);
									}
									interactionColumnExpMap.put(newColumnName, newcolumnExp);
									interactionColumnColumnMap.put(newColumnName, new String[]{firstColumn, secondColumn, interactionType});
								}
							}

							if (interactionType.trim().equals("*")) {
								if(!columnNames.contains(firstColumn)){
									HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
									HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
									count = 0;
									valueList = firstAtt.getMapping()
											.getValues();
									valueList_i = valueList.iterator();
									columnName = StringHandler.doubleQ(firstColumn);
									while (valueList_i.hasNext()) {
										String value = valueList_i.next();
										if (valueList.size() != 1
												&& count == valueList.size() - 1
												&& !nullList.contains(columnName)) {
											continue;
										}
										value = StringHandler.escQ(value);
										String valueQ = CommonUtility.quoteValue(
												dbType, firstAtt, value);
										if(!newColumnNames.contains(firstColumn + "_"
												+ value)){
											individualColumnIndex.add(newColumnNames.size());
											newColumnExps.add("( case when " + columnName
													+ "=" + valueQ
													+ " then 1.0 else 0.0 end )");
											newColumnNames.add(firstColumn + "_"
													+ value);
										}
//										newColumnExpMap.put(firstColumn + "_"
//												+ value, "( case when " + columnName
//												+ "=" + valueQ
//												+ " then 1.0 else 0.0 end )");

										transformMap_valueKey.put(value, firstAtt.getName() + "_"
												+ value);
										transformMap_columnKey.put(firstAtt.getName() + "_" + value,
												value);

										count++;
									}
									allTransformMap_valueKey.put(firstAtt.getName(),
											transformMap_valueKey);
									allTransformMap_columnKey.put(firstAtt.getName(),
											transformMap_columnKey);

								}
								if(!columnNames.contains(secondColumn)){
									HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
									HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
									count = 0;
									valueList = secondAtt.getMapping()
											.getValues();
									valueList_i = valueList.iterator();
									columnName = StringHandler.doubleQ(secondColumn);
									while (valueList_i.hasNext()) {
										String value = valueList_i.next();
										if (valueList.size() != 1
												&& count == valueList.size() - 1
												&& !nullList.contains(columnName)) {
											continue;
										}
										value = StringHandler.escQ(value);
										String valueQ = CommonUtility.quoteValue(
												dbType, secondAtt, value);
										if(!newColumnNames.contains(secondColumn + "_"
												+ value)){
											individualColumnIndex.add(newColumnNames.size());
											newColumnExps.add("( case when " + columnName
													+ "=" + valueQ
													+ " then 1.0 else 0.0 end )");
											newColumnNames.add(secondColumn + "_"
													+ value);
										}
//										newColumnExpMap.put(secondColumn + "_"
//												+ value, "( case when " + columnName
//												+ "=" + valueQ
//												+ " then 1.0 else 0.0 end )");

										transformMap_valueKey.put(value, secondAtt.getName() + "_"
												+ value);
										transformMap_columnKey.put(secondAtt.getName() + "_" + value,
												value);

										count++;
									}
									allTransformMap_valueKey.put(secondAtt.getName(),
											transformMap_valueKey);
									allTransformMap_columnKey.put(secondAtt.getName(),
											transformMap_columnKey);
								}
							}
						}
					}
					if(!interactionType.trim().equals("*")){
						existingColumns.add(firstColumn);
						existingColumns.add(secondColumn);
					}
			}
		}
		itsLogger.debug(tempTableName);
		StringBuilder sb_create = null;
		if (dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			sb_create =  new StringBuilder("create temporary table ");
		}else{
			sb_create = new StringBuilder("create global temporary table ");
		}

		// sb_create = new StringBuilder("create ");
		// if (!((DBTable)
		// dataSet.getdataTable()).getDatabaseHandler().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType))
		// {
		// sb_create.append(" temporary ");
		// }
		// sb_create.append(" table ");
		sb_create.append(tempTableName);
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			sb_create.append(" ON COMMIT PRESERVE ROWS ");
		}
//		sb_create.append(" as (select ");
		sb_create.append(" as (");
		StringBuffer selectSql = new StringBuffer();
		selectSql.append(" select ");
		Iterator<Column> atts_ii = atts.iterator();
		// StringBuilder sb_isNotNull=new StringBuilder();
		while (atts_ii.hasNext()) {
			Column att = atts_ii.next();
			String columnName = StringHandler.doubleQ(att.getName());
			if(!columnNames.contains(att.getName()))continue;
			if (att.isNumerical()) {
				selectSql.append(columnName).append(",");
//				selectSql.append(columnName).append(",");
			} else {
				// sb_isNotNull.append(StringHandler.doubleQ(att.getName())).append(" is not null and ");
				HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
				HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
				int count = 0;
				List<String> valueList = att.getMapping().getValues();
				Iterator<String> valueList_i = valueList.iterator();
					while (valueList_i.hasNext()) {
						String value = valueList_i.next();
						if (valueList.size() != 1
								&& count == valueList.size() - 1
								&& !nullList.contains(columnName)) {
							continue;
						}
						selectSql.append(" case when ").append(columnName)
								.append("=");
						value = StringHandler.escQ(value);
						String valueQ = CommonUtility.quoteValue(dbType, att,
								value);
						selectSql.append(valueQ).append(
									" then 1.0 else 0.0 end ").append("\"")
									.append(att.getName());

						selectSql.append("_").append(value).append("\"")
									.append(",");

						transformMap_valueKey.put(value, att.getName() + "_"
								+ value);
						transformMap_columnKey.put(att.getName() + "_" + value,
								value);
						count++;
					}
				allTransformMap_valueKey.put(att.getName(),
						transformMap_valueKey);
				allTransformMap_columnKey.put(att.getName(),
						transformMap_columnKey);
			}

		}
		if(columnNames.size() != 0){
			selectSql = selectSql.deleteCharAt(selectSql.length() - 1);
		}

		boolean first = true;
		for(int i = 0 ; i < newColumnNames.size(); i++){
			if(individualColumnIndex.contains(i)){
				if(!(columnNames.size() == 0 && first)){
					selectSql.append(",");
				}
				if(first){
					first = false;
				}
				selectSql.append(newColumnExps.get(i)).append(" ").append(StringHandler.doubleQ(newColumnNames.get(i)));
			}
		}
		for(int i = 0 ; i < newColumnNames.size(); i++){
			if(!individualColumnIndex.contains(i)){
				if(!(columnNames.size() == 0 && first)){
					selectSql.append(",");
				}
				if(first){
					first = false;
				}
				selectSql.append(newColumnExps.get(i)).append(" ").append(StringHandler.doubleQ(newColumnNames.get(i)));
			}
		}
		if(groupByColumn!=null)
		{
			selectSql.append(",").append(StringHandler.doubleQ(groupByColumn));
		}
		Column label = atts.getLabel();
		if (label != null) {
			selectSql.append(",")
					.append(StringHandler.doubleQ(label.getName()));
		}
		Column id = atts.getId();
		if (id != null) {
			selectSql.append(",").append(StringHandler.doubleQ(id.getName()));
		}

		selectSql.append(" from ").append(tableName);
//		.append(")");
		// IDataSourceInfo dataSourceInfo =
		// DataSourceInfoFactory.createConnectionInfo(databaseConnection.getProperties().getName());

		try {
			st = databaseConnection.createStatement(false);
			sb_create.append(selectSql).append(")");
			sb_create.append(sqlGenerator.setCreateTableEndingSql(null));
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				sb_create.append(" ON COMMIT PRESERVE ROWS ");
			}
			itsLogger.debug(
					"ColumnTypeInteractionTransformer.TransformCategoryToNumeric_new():sql="
							+ sb_create);
			st.execute(sb_create.toString());
			
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("insert into ").append(tempTableName);
				insertSql.append(selectSql);
				itsLogger.debug(
						"ColumnTypeInteractionTransformer.TransformCategoryToNumeric_new():sql="
								+ insertSql);
				st.execute(insertSql.toString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		String URL = ((DBTable) dataSet.getDBTable()).getUrl();
		String UserName = ((DBTable) dataSet.getDBTable()).getUserName();
		String Password = ((DBTable) dataSet.getDBTable()).getPassword();
		DataSet newDataSet = null;
		try {
			Table table = DBTable.createDatabaseDataTableDB(
					databaseConnection, URL, UserName, Password, tempTableName,
					null);
			newDataSet = table.createDataSet();
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if (label != null) {
			newDataSet.getColumns().setLabel(label);
		}
		if (id != null) {
			newDataSet.getColumns().setId(id);
		}
		if(groupByColumn!=null)
		{
			newDataSet.getColumns().remove(newDataSet.getColumns().get(groupByColumn));
		}
		return newDataSet;
	}

	private ArrayList<String> countNullColumn(
			DatabaseConnection databaseConnection, String tableName,
			Columns atts) throws OperatorException {
		Iterator<Column> i = atts.iterator();
		StringBuilder sb_countNull = new StringBuilder("select ");
		while (i.hasNext()) {
			Column att = i.next();
			if (att.isNumerical())
				continue;
			String columnName = StringHandler.doubleQ(att.getName());
			sb_countNull.append("sum(case when ").append(columnName).append(
					" is null then 1 else 0 end) ").append(columnName).append(
					",");
		}
		sb_countNull = sb_countNull.deleteCharAt(sb_countNull.length() - 1);
		sb_countNull.append(" from ").append(tableName);
		try {
			Statement st = databaseConnection.createStatement(false);
			itsLogger.debug(
					"ColumnTypeInteractionTransformer.countNullColumn():sql="
							+ sb_countNull.toString());
			ResultSet rs = st.executeQuery(sb_countNull.toString());
			ArrayList<String> nullList = new ArrayList<String>();
			while (rs.next()) {
				for (int ii = 0; ii < rs.getMetaData().getColumnCount(); ii++) {
					if (rs.getLong(ii + 1) != 0) {
						nullList.add(StringHandler.doubleQ(rs.getMetaData()
								.getColumnName(ii + 1)));
					}
				}
			}
			return nullList;
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

	}
}
