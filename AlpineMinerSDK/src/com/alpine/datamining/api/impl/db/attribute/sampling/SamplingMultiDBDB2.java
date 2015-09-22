package com.alpine.datamining.api.impl.db.attribute.sampling;

import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;

public class SamplingMultiDBDB2 implements ISamplingMultiDB {

	ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public String createRandomTable(String tableName, long rowCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName) {
		// String sqlCreateSample0
		// ="SELECT "+columnNames+", rand() as rand_order from "+inputTableName;
		String sqlCreateSample0 = "select " + columnNames
				+ ",ROWNUMBER() OVER () AS ROWNUM from " + "(SELECT "
				+ columnNames + ",rand() as rand_order FROM " + inputTableName
				+ " order by rand_order) ";
		return sqlCreateSample0;
	}

	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(inputTableName).append(
				".*, ROWNUMBER() over (partition by ");
		selectSql.append(StringHandler.doubleQ(sampleColumn)).append(
				") AS alpine_sample_id, rand() as rand_order from ");
		selectSql.append(inputTableName);
		return selectSql.toString();
	}

	@Override
	public String insertTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(inputTableName).append(
				".*, ROWNUMBER() over (partition by ");
		selectSql.append(StringHandler.doubleQ(sampleColumn)).append(
				") AS alpine_sample_id, rand() as rand_order from ");
		selectSql.append(inputTableName);
		return selectSql.toString();
	}
	
	@Override
	public String generateReplacementResultTable(String tableName,
			String outputType, String resultTableName, String columnNames,
			long limit, long totalCount, long countThreshold,
			double limitRatio, long sampleThreshold, double sampleLimitRatio, String appendOnlyString, String endingString) {
		StringBuilder selectSql = new StringBuilder();
		long cycle = limit / totalCount;
		for (long i = 0; i < cycle; i++) {
			if (i > 0) {
				selectSql.append(" union all ");
			}
			selectSql.append(" ( select ").append(columnNames).append(" from ")
					.append(tableName).append(" ) ");
		}

		long left = limit - totalCount * cycle;
		String sqlLeft = "";
		StringBuilder leftSelectSql = new StringBuilder();
		if (totalCount > countThreshold) {
			double limitRatioUsed = 0;
			if (left < sampleThreshold) {
				limitRatioUsed = sampleLimitRatio;
			} else {
				limitRatioUsed = limitRatio;
			}
			leftSelectSql.append(" (select ").append(columnNames).append(
					" from (SELECT ").append(columnNames).append(
					", rand() as rand_order from ");
			leftSelectSql.append(tableName).append(
					") foo where foo.rand_order <= ").append(
					left * limitRatioUsed / totalCount);
			if(left==0){
				leftSelectSql.append(" and 1=0 ) ");
			}else{
				leftSelectSql.append(" fetch first ");
				leftSelectSql.append(left).append(" row only )");
			}
			sqlLeft += left + ") ";
		} else {
			leftSelectSql.append(" ( select ").append(columnNames).append(
					" from ( select ").append(columnNames).append(
					",rand() as rand_order from ");
			leftSelectSql.append(tableName);
			if(left==0){
				leftSelectSql.append(" where 1=0 ");	
			}else{
				leftSelectSql.append(
				" order by rand_order ").append(" fetch first ").append(left).append(
				" row only ");	
			}
			leftSelectSql.append(") foo)");
		}

		if (cycle > 0) {
			selectSql.append(" union all ").append(leftSelectSql);
		} else {
			selectSql.append(leftSelectSql);
		}
		return selectSql.toString();
	}

	@Override
	public StringBuilder generateReplacementTempTable(String tempTableName,
			String inputTable) {
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(inputTable);
		selectSql.append(".*,row_number() over () random_id ");
		selectSql.append(" from ").append(inputTable);
		return selectSql;
	}

	@Override
	public String generateResultTable(String tableName, String outputType,
			String tempTableName, String tempRandTableName, String columnNames,
			int tableIndex, long limit, String disjoint, String appendOnlyString, String endingString) {
		String sqlCreateTable;
		if (disjoint != null && disjoint.equals(Resources.TrueOpt)) {
			sqlCreateTable = "select " + columnNames
					+ " from (SELECT  ROWNUMBER() OVER () AS ROWNUM ,"
					+ columnNames + " FROM " + tempTableName + " AS a, "
					+ tempRandTableName + " AS b "
					+ " WHERE (a.alpine_sample_id = b.alpine_sample_id) "
					+ "ORDER BY b.rand_order ) " + "WHERE ROWNUM BETWEEN "
					+ (tableIndex * limit + 1) + " AND "
					+ ((1 + tableIndex) * limit);
		} else {
			sqlCreateTable = " SELECT  " + columnNames + " FROM "
					+ tempTableName + " AS a, " + tempRandTableName + " AS b "
					+ " WHERE (a.alpine_sample_id = b.alpine_sample_id) "
					+ "ORDER BY b.rand_order FETCH FIRST " + limit
					+ " ROWS ONLY ";
		}
		return sqlCreateTable;
	}

	@Override
	public String generateResultTable(String tableName, String outputType,
			String tempTableName, String columnNames, int tableIndex,
			long limit, long totalCount, long countThreshold,
			double limitRatio, long sampleThreshold, double sampleLimitRatio,
			String disjoint, String appendOnlyString, String endingString,long offset) {
		String sqlCreateTable;
		if (disjoint == null || disjoint.equals(Resources.FalseOpt)) {
			if (totalCount > countThreshold) {
				double limitRatioUsed = 0;
				if (limit < sampleThreshold) {
					limitRatioUsed = sampleLimitRatio;
				} else {
					limitRatioUsed = limitRatio;

				}
				sqlCreateTable = "select " + columnNames + " from (SELECT "
						+ columnNames + "," + " rand() as rand_order from "
						+ tempTableName + ") foo where foo.rand_order <= "
						+ (limit * limitRatioUsed / totalCount)
						+ " fetch first  " + limit + " rows only";
			} else {

				sqlCreateTable = "select " + columnNames + " from (SELECT "
						+ columnNames + "," + " rand() as rand_order from "
						+ tempTableName + " order by rand_order fetch first "
						+ limit + " rows only ) foo";
			}
		} else {

			sqlCreateTable = "select "
					+ columnNames
					+ " from (SELECT  ROWNUMBER() OVER () AS ROWNUM,"+columnNames+" from "
					+ tempTableName+")"
					+ " WHERE ROWNUM BETWEEN " + (offset + 1)
					+ " AND " + (offset+limit);
		}
		return sqlCreateTable;
	}

	@Override
	public String generateResultTable(String tableName, String outputType) {
		String createTable = "CREATE " + outputType + " " + tableName;
		return createTable;
	}

	@Override
	public void setSqlGenerator(ISqlGeneratorMultiDB sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	@Override
	public String stratifiedSamplingAppendStringConsistent(String columnNames,
			String tableName, String randTableName, String sampleColumn,
			String sampleValue, Long limit, Long offset) {
		int intlimit=Math.round(limit);
		int intoffset=Math.round(offset);
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(columnNames).append(
				" from ( SELECT  ROWNUMBER() OVER () AS ROWNUM ,");
		selectSql.append(columnNames).append(" From ");
		selectSql.append(tableName).append(" a,").append(randTableName).append(
				" b ");
		selectSql.append(
				" WHERE (a.alpine_sample_id = b.alpine_sample_id and a.")
				.append(StringHandler.doubleQ(sampleColumn));
		selectSql.append(" = ").append(sampleValue).append(")");
		selectSql.append(" ORDER BY b.rand_order )");
		selectSql.append(" WHERE ROWNUM BETWEEN ").append(intoffset + 1).append(
				" and ").append(intoffset + intlimit);
		return selectSql.toString();
	}

	@Override
	public String stratifiedSamplingAppendStringConsistent(String tableName,
			String randTableName, Long limit, Long offset, String columnNames) {
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(columnNames).append(
				" from ( SELECT  ROWNUMBER() OVER () AS ROWNUM ,");
		selectSql.append(columnNames).append(" From ");
		selectSql.append(tableName).append(" a,").append(randTableName).append(
				" b ");
		selectSql.append(" WHERE (a.alpine_sample_id = b.alpine_sample_id) ");
		selectSql.append(" ORDER BY b.rand_order )");
		selectSql.append(" WHERE ROWNUM BETWEEN ").append(offset + 1).append(
				" and ").append(offset + limit);
		return selectSql.toString();
	}

	@Override
	public String stratifiedSamplingAppendStringUnConsistent(
			String columnNames, String tableName, String sampleColumn,
			String sampleValue, Long limit, Long offset) {
		int intoffset=Math.round(offset);
		int intlimit=Math.round(limit);
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(columnNames).append(
				" from ( SELECT  ROWNUMBER() OVER () AS ROWNUM ,");
		selectSql.append(columnNames).append(" From ");
		selectSql.append(tableName).append(" AS a ").append(" WHERE (a.");
		selectSql.append(StringHandler.doubleQ(sampleColumn)).append(" = ")
				.append(sampleValue).append(")");
		selectSql.append(" ORDER BY a.rand_order,a.alpine_sample_id)");
		selectSql.append(" WHERE ROWNUM BETWEEN ").append(intoffset + 1).append(
				" and ").append(intoffset + intlimit);
		return selectSql.toString();
	}

	@Override
	public String stratifiedSamplingAppendStringUnConsistent(
			String columnNames, String tableName, Long limit, Long offset) {
		StringBuilder selectSql = new StringBuilder();
		selectSql.append(" select ").append(columnNames).append(
				" from ( SELECT  ROWNUMBER() OVER () AS ROWNUM ,");
		selectSql.append(columnNames).append(" From ");
		selectSql.append(tableName).append(" AS a ");
		selectSql.append(" ORDER BY a.rand_order,a.alpine_sample_id)");
		selectSql.append(" WHERE ROWNUM BETWEEN ").append(offset + 1).append(
				" and ").append(offset + limit);
		return selectSql.toString();
	}
	
	@Override
	public String truncate(String tempTableName){
		StringBuilder sampleTruncate = new StringBuilder();
		sampleTruncate.append("truncate table ").append(tempTableName).append(" IMMEDIATE");
		return sampleTruncate.toString();
	}

}
