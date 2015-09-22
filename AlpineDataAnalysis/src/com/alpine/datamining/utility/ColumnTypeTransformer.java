/**
 * ClassName ColumnTypeTransformer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class ColumnTypeTransformer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7941251036319652748L;
    private static final Logger cttLogger = Logger.getLogger(ColumnTypeTransformer.class);
	/**
	 * 
	 */
	String tempTableName;

	protected boolean transform = false;
	private boolean useArray = false;

	protected HashMap<String, HashMap<String, String>> allTransformMap_valueKey = new HashMap<String, HashMap<String, String>>();

	protected HashMap<String, HashMap<String, String>> allTransformMap_columnKey = new HashMap<String, HashMap<String, String>>();

	public boolean isTransform() {
		return transform;
	}


	public void setTransform(boolean transform) {
		this.transform = transform;
	}


	public DataSet TransformCategoryToNumeric_new(DataSet dataSet)
			throws OperatorException {
		Columns atts = dataSet.getColumns();
		Iterator<Column> atts_i = atts.iterator();
		ArrayList<Column> needToTransform_list = new ArrayList<Column>();
		while (atts_i.hasNext()) {
			Column att = atts_i.next();
			if (!att.isNumerical())
				needToTransform_list.add(att);
		}

		if (needToTransform_list.size() == 0){
			transform = false;
			return dataSet;
		}else{
			transform = true;
		}

		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		String randomName = StringHandler.doubleQ("AlpTmp"+ System.currentTimeMillis());
		String dbType = ((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName();
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dbType);
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
//		if(dbType.equals(DataSourceInfoOracle.dBType)){
//			useArray = false;
//		}
		if (tableName.contains(".")) {
			if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType))
			{
				tempTableName = StringHandler.doubleQ(StringHandler.removeMiddleDoubleQ(tableName.split("\\.")[0]))+"."+randomName;
			}else{
				tempTableName = randomName;
			}
		} else {
			
			tempTableName = randomName;
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Statement st = null;

		ArrayList<String> nullList=countNullColumn(databaseConnection,tableName,atts);
		
		cttLogger.debug(tempTableName);
		StringBuilder sb_create = null;
		if (dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			sb_create =  new StringBuilder("create temporary table ");
		}else{
			sb_create = new StringBuilder("create global temporary table ");
		}

		sb_create.append(tempTableName);
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			sb_create.append(" ON COMMIT PRESERVE ROWS ");
		}
//		sb_create.append(" as (select ");
		sb_create.append(" as (");
		StringBuffer selectSql = new StringBuffer();
		selectSql.append(" select ");
		Iterator<Column> atts_ii = atts.iterator();
//		StringBuilder sb_isNotNull=new StringBuilder();
		while (atts_ii.hasNext()) {
			Column att = atts_ii.next();
			String columnName = StringHandler.doubleQ(att.getName());
			if (att.isNumerical()) {
				selectSql.append(columnName).append(",");
			} else {
//				sb_isNotNull.append(StringHandler.doubleQ(att.getName())).append(" is not null and ");
				HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
				HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
				int count = 0;
				List<String> valueList = att.getMapping().getValues();
				Iterator<String> valueList_i = valueList.iterator();
				if(useArray&&dbType.equals(DataSourceInfoOracle.dBType)){
					ArrayList<String> valueSqlList=new ArrayList<String>();
					while (valueList_i.hasNext()){
						String value = valueList_i.next();
						if (valueList.size()!=1&&count == valueList.size() - 1&&!nullList.contains(columnName)) {
							continue;
						}				
						value = StringHandler.escQ(value);
						String valueQ = CommonUtility.quoteValue(dbType,att,value);
						StringBuilder sb_sql=new StringBuilder();
						sb_sql.append(" case when ").append(columnName).append("=");
						sb_sql.append(valueQ).append(" then 1.0 else 0.0 end ");
						valueSqlList.add(sb_sql.toString());
						
						transformMap_valueKey.put(value, att.getName() + "_"
								+ value);
						transformMap_columnKey.put(att.getName() + "_" + value,
								value);
						count++;
					}
					StringBuffer sbf=CommonUtility.array2OracleArray(valueSqlList, CommonUtility.OracleDataType.Float);
					selectSql.append(sbf).append(" ").append(columnName).append(",");
				}else{
				if (useArray){
					selectSql.append(multiDBUtility.floatArrayHead());				
				}

				while (valueList_i.hasNext()) {
					String value = valueList_i.next();
					if (valueList.size()!=1&&count == valueList.size() - 1&&!nullList.contains(columnName)) {
						continue;
					}
					selectSql.append(" case when ").append(columnName).append(
							"=");
					value = StringHandler.escQ(value);
					String valueQ = CommonUtility.quoteValue(dbType,att,value);
//					String valueQ = null;
//					if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType) && (att.getValueType() == DataType.DATE ||att.getValueType() == DataType.DATE_TIME||att.getValueType() == DataType.TIME)){
//						valueQ = "to_date('"+value+"', 'YYYY-MM-DD HH24:MI:SS')";
//					}else{
//						valueQ = "'"+value+"'";
//					}
					if (!useArray){
						selectSql.append(valueQ).append(
								" then 1.0 else 0.0 end ").append(
								"\"").append(att.getName());
	
						selectSql.append("_").append(value).append("\"")
								.append(",");
					}else{
						selectSql.append(valueQ).append(
						" then 1.0 else 0.0 end ");
	//					sb_create.append(
	//					"\"").append(att.getName());
	
	//					sb_create.append("_").append(value).append("\"");
						selectSql.append(",");
					}

					
					transformMap_valueKey.put(value, att.getName() + "_"
							+ value);
					transformMap_columnKey.put(att.getName() + "_" + value,
							value);
					count++;
				}
				if (useArray){
					selectSql = selectSql.deleteCharAt(selectSql.length() - 1);
//					sb_create.append("] ");
//					sb_create.append("]::int[] ");
					selectSql.append(multiDBUtility.floatArrayTail());

					selectSql.append("\"").append(att.getName()).append("\"");
					selectSql.append(",");
				}
				}
				allTransformMap_valueKey.put(att.getName(),
						transformMap_valueKey);
				allTransformMap_columnKey.put(att.getName(),
						transformMap_columnKey);
			}

		}
		selectSql = selectSql.deleteCharAt(selectSql.length() - 1);
//		sb_isNotNull=sb_isNotNull.delete(sb_isNotNull.length()-4, sb_isNotNull.length()-1);
		Column label = atts.getLabel();
		if (label != null) {
			selectSql.append(",").append(
					StringHandler.doubleQ(label.getName()));
		}
		Column id = atts.getId();
		if (id != null) {
			selectSql.append(",").append(
					StringHandler.doubleQ(id.getName()));
		}

		selectSql.append(" from ").append(tableName);
//		.append(")");
//		IDataSourceInfo  dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(databaseConnection.getProperties().getName());
//		sb_create.append(sqlGenerator.getDistributeString());

		try {
			st = databaseConnection.createStatement(false);
			sb_create.append(selectSql).append(")");
			sb_create.append(sqlGenerator.setCreateTableEndingSql(null));
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				sb_create.append(" ON COMMIT PRESERVE ROWS ");
			}
			cttLogger.debug(
                    "ColumnTypeTransformer.TransformCategoryToNumeric_new():sql="
                            + sb_create);
			st.execute(sb_create.toString());
			
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("insert into ").append(tempTableName);
				insertSql.append(selectSql);
				cttLogger.debug(
                        "ColumnTypeTransformer.TransformCategoryToNumeric_new():sql="
                                + insertSql);
				st.execute(insertSql.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			cttLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		String URL = ((DBTable) dataSet.getDBTable())
				.getUrl();
		String UserName = ((DBTable) dataSet
				.getDBTable()).getUserName();
		String Password = ((DBTable) dataSet
				.getDBTable()).getPassword();
		DataSet newDataSet = null;
		try {
			Table table = DBTable
					.createDatabaseDataTableDB(databaseConnection, URL,
							UserName, Password, tempTableName, null);
			newDataSet = table.createDataSet();
		} catch (SQLException e) {
			cttLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if (label != null) {
			newDataSet.getColumns().setLabel(label);
		}
		if(id!=null)
		{
			newDataSet.getColumns().setId(id);
		}
		return newDataSet;
	}

	public DataSet TransformCategoryToNumericRemain(DataSet dataSet, List<String> transformColumns, List<String> remainColumns)
			throws OperatorException {
		Columns atts = dataSet.getColumns();
		Iterator<Column> atts_i = atts.iterator();
		ArrayList<Column> needToTransform_list = new ArrayList<Column>();
		while (atts_i.hasNext()) {
			Column att = atts_i.next();
			if (!att.isNumerical() && transformColumns.contains(att.getName()))
				needToTransform_list.add(att);
		}

		if (needToTransform_list.size() == 0) {
			transform = false;
			return dataSet;
		} else {
			transform = true;
		}

		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		String randomName = StringHandler.doubleQ("AlpTmp"
				+ System.currentTimeMillis());
		String dbType = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection().getProperties().getName();
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dbType);
		if (tableName.contains(".")) {
			if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				tempTableName = StringHandler.doubleQ(StringHandler
						.removeMiddleDoubleQ(tableName.split("\\.")[0]))
						+ "." + randomName;
			} else {
				tempTableName = randomName;
			}
		} else {

			tempTableName = randomName;
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Statement st = null;

		ArrayList<String> nullList = countNullColumn(databaseConnection,
				tableName, atts);

		cttLogger.debug(tempTableName);
		StringBuilder sbCreate = null;
		if (dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			sbCreate =  new StringBuilder("create temporary table ");
		}else{
			sbCreate = new StringBuilder("create global temporary table ");
		}
		sbCreate.append(tempTableName);
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			sbCreate.append(" ON COMMIT PRESERVE ROWS ");
		}
//		sbCreate.append(" as (select ");
		sbCreate.append(" as (");
		StringBuffer selectSql = new StringBuffer();
		selectSql.append(" select ");

		Iterator<Column> atts_ii = atts.iterator();
		List<String> notTransformColumns = new ArrayList<String>();
		while (atts_ii.hasNext()) {
			Column att = atts_ii.next();
			String columnName = StringHandler.doubleQ(att.getName());
			if (att.isNumerical() || !transformColumns.contains(att.getName())) {
				notTransformColumns.add(att.getName());
				selectSql.append(columnName).append(",");
			} else {
				HashMap<String, String> transformMap_valueKey = new HashMap<String, String>();
				HashMap<String, String> transformMap_columnKey = new HashMap<String, String>();
				int count = 0;
				List<String> valueList = att.getMapping().getValues();
				Iterator<String> valueList_i = valueList.iterator();
				if (useArray && dbType.equals(DataSourceInfoOracle.dBType)) {
					ArrayList<String> valueSqlList = new ArrayList<String>();
					while (valueList_i.hasNext()) {
						String value = valueList_i.next();
						if (valueList.size() != 1
								&& count == valueList.size() - 1
								&& !nullList.contains(columnName)) {
							continue;
						}
						value = StringHandler.escQ(value);
						String valueQ = CommonUtility.quoteValue(dbType, att,
								value);
						StringBuilder sb_sql = new StringBuilder();
						sb_sql.append(" case when ").append(columnName).append(
								"=");
						sb_sql.append(valueQ).append(" then 1.0 else 0.0 end ");
						valueSqlList.add(sb_sql.toString());

						transformMap_valueKey.put(value, att.getName() + "_"
								+ value);
						transformMap_columnKey.put(att.getName() + "_" + value,
								value);
						count++;
					}
					StringBuffer sbf = CommonUtility.array2OracleArray(
							valueSqlList, CommonUtility.OracleDataType.Float);
					selectSql.append(sbf).append(" ").append(columnName)
							.append(",");
				} else {
					if (useArray) {
						selectSql.append(multiDBUtility.floatArrayHead());
					}

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
						if (!useArray) {
							selectSql.append(valueQ).append(
									" then 1.0 else 0.0 end ").append("\"")
									.append(att.getName());

							selectSql.append("_").append(value).append("\"")
									.append(",");
						} else {
							selectSql.append(valueQ).append(
									" then 1.0 else 0.0 end ");
							selectSql.append(",");
						}

						transformMap_valueKey.put(value, att.getName() + "_"
								+ value);
						transformMap_columnKey.put(att.getName() + "_" + value,
								value);
						count++;
					}
					if (useArray) {
						selectSql = selectSql
								.deleteCharAt(selectSql.length() - 1);
						// sb_create.append("] ");
						// sb_create.append("]::int[] ");
						selectSql.append(multiDBUtility.floatArrayTail());

						selectSql.append("\"").append(att.getName()).append(
								"\"");
						selectSql.append(",");
					}
				}
				allTransformMap_valueKey.put(att.getName(),
						transformMap_valueKey);
				allTransformMap_columnKey.put(att.getName(),
						transformMap_columnKey);
			}

		}
		selectSql = selectSql.deleteCharAt(selectSql.length() - 1);
		Iterator<String> remainColumnIter = remainColumns.iterator(); 
		while(remainColumnIter.hasNext()){
			String remainColumn = remainColumnIter.next();
			if(!notTransformColumns.contains(remainColumn)){
				selectSql.append(",").append(StringHandler.doubleQ(remainColumn));
			}
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
//		sbCreate.append(sqlGenerator.getDistributeString());

		try {
			st = databaseConnection.createStatement(false);
			sbCreate.append(selectSql).append(")");
			sbCreate.append(sqlGenerator.setCreateTableEndingSql(null));
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				sbCreate.append("  ON COMMIT PRESERVE ROWS ");
			}
			cttLogger.debug(
                    "ColumnTypeTransformer.TransformCategoryToNumericRemain():sql="
                            + sbCreate);
			st.execute(sbCreate.toString());
			
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("insert into ").append(tempTableName);
				insertSql.append(selectSql);
				cttLogger.debug(
                        "ColumnTypeTransformer.TransformCategoryToNumericRemain():sql="
                                + insertSql);
				st.execute(insertSql.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			cttLogger.error(e.getMessage(), e);
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
			cttLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if (label != null) {
			newDataSet.getColumns().setLabel(label);
		}
		if (id != null) {
			newDataSet.getColumns().setId(id);
		}
		return newDataSet;
	}

	private ArrayList<String> countNullColumn(DatabaseConnection databaseConnection,String tableName,Columns atts) throws OperatorException {
			Iterator<Column> i=atts.iterator();
			StringBuilder sb_countNull=new StringBuilder("select ");
			while(i.hasNext())
			{
				Column att=i.next();
				if(att.isNumerical())continue;
				String columnName=StringHandler.doubleQ(att.getName());
				sb_countNull.append("sum(case when ").append(columnName).append(" is null then 1 else 0 end) ").append(columnName).append(",");
			}
			sb_countNull=sb_countNull.deleteCharAt(sb_countNull.length()-1);
			sb_countNull.append(" from ").append(tableName);
			try {
				Statement st=databaseConnection.createStatement(false);
				cttLogger.debug("ColumnTypeTransformer.countNullColumn():sql=" + sb_countNull.toString());
				ResultSet rs=st.executeQuery(sb_countNull.toString());
				ArrayList<String> nullList=new ArrayList<String>();
				while(rs.next())
				{
					for(int ii=0;ii<rs.getMetaData().getColumnCount();ii++)
					{
						if(rs.getLong(ii+1)!=0)
						{
							nullList.add(StringHandler.doubleQ(rs.getMetaData().getColumnName(ii+1)));
						}
					}
				}
				return nullList;
			} catch (SQLException e) {
				cttLogger.error(e.getMessage(), e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		
	}

	public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}

	public HashMap<String, HashMap<String, String>> getAllTransformMap_columnKey() {
		return allTransformMap_columnKey;
	}


	public boolean isUseArray() {
		return useArray;
	}


	public void setUseArray(boolean useArray) {
		this.useArray = useArray;
	}

}
