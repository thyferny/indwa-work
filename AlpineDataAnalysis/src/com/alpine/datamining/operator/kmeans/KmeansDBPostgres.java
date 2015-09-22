package com.alpine.datamining.operator.kmeans;


public class KmeansDBPostgres extends KmeansDBGP {

	@Override
	public StringBuilder generateCreateCopyTableSql(String tableName,
			String tempid, String copyTableName) {
		StringBuilder sb_createCopy=new StringBuilder("create temp table ");
		sb_createCopy.append(copyTableName);
		sb_createCopy.append(" as select *,row_number() over () ").append(tempid)
		.append(" from ").append(tableName);
		return sb_createCopy;
	}
}
