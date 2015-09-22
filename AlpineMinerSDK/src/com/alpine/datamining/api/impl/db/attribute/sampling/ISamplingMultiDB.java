package com.alpine.datamining.api.impl.db.attribute.sampling;

 

import com.alpine.utility.db.ISqlGeneratorMultiDB;


public interface ISamplingMultiDB {
	
	public abstract String createRandomTable(String tableName, long rowCount);
	public abstract String generateResultTable(String tableName,String outputType,String tempTableName,
			String tempRandTableName,String columnNames,int tableIndex,long sampleRowSize,String disjoint, String appendOnlyString, String endingString);
	public abstract String generateReplacementResultTable(String tableName, String outputType,
			String resultTableName, String columnNames, long sampleRowSize, long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio, String appendOnlyString, String endingString);

	public abstract StringBuilder generateReplacementTempTable(String tempTableName,String inputTable);
	public abstract String createTempTable(String tempTableName,String columnNames, String inputTableName);
	public abstract String createTempTable(String tempTableName,String columnNames, String inputTableName, String sampleColumn);
	public abstract String generateResultTable(String tableName,String outputType,String tempTableName,
			String columnNames,int tableIndex,long limit,long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio, String disjoint, String appendOnlyString, String endingString, long offset);
	public abstract String generateResultTable(String tableName,String outputType);
	public abstract String stratifiedSamplingAppendStringUnConsistent(String columnNames,
			String tableName,String sampleColumn, String sampleValue, Long limit, Long offset);
	public abstract String stratifiedSamplingAppendStringUnConsistent(String columnNames,
			String tableName, Long limit, Long offset);
	public abstract String stratifiedSamplingAppendStringConsistent(String columnNames,
			String tableName,String randTableName, String sampleColumn, String sampleValue, Long limit, Long offset);
	public abstract String stratifiedSamplingAppendStringConsistent(String tableName,
			String randTableName, Long limit, Long offset, String columnNames);
	public void setSqlGenerator(ISqlGeneratorMultiDB sqlGenerator);
	public abstract String insertTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn);
	public abstract String truncate(String tempTableName);

}
