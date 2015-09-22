package com.alpine.datamining.api.impl.db.attribute.sampling;

import com.alpine.utility.tools.StringHandler;

public class SamplingMultiDBNZ extends AbstractSamplingMultiDBPGGP {

	@Override
	public String createRandomTable(String tableName, long rowCount) {
		String sqlCreateRand = "CREATE TABLE "+tableName+" (alpine_sample_id BIGINT, rand_order float)" + sqlGenerator.setCreateTableEndingSql(null)+
				"; SELECT alpine_miner_generate_random_table('"+tableName+"',"+rowCount+");";
		return sqlCreateRand;
	}
	
	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName) {
		String sqlCreateSample0 = "CREATE TEMP TABLE "+tempTableName.split("\\.")[tempTableName.split("\\.").length-1]+" AS "
						+ "SELECT *,row_number() over(order by random()) AS alpine_sample_id, random() as rand_order from "+inputTableName+sqlGenerator.setCreateTableEndingSql(null);
		return sqlCreateSample0;
	}
	
	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		String sqlCreateSample0 = "CREATE TABLE "+tempTableName+" AS "
						+ "SELECT *,row_number() over(partition by "+StringHandler.doubleQ(sampleColumn)+" order by random()) AS alpine_sample_id, random() as rand_order from "+inputTableName + sqlGenerator.setCreateTableEndingSql(null);
		return sqlCreateSample0;
	}

	@Override
	public String insertTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		String sqlCreateSample0 = "INSERT INTO "
				+ tempTableName
				+ " SELECT *,row_number() over(partition by "
				+ StringHandler.doubleQ(sampleColumn)
				+ " order by random()) AS alpine_sample_id, random() as rand_order from "
				+ inputTableName + sqlGenerator.setCreateTableEndingSql(null);
		return sqlCreateSample0;
	}

}
