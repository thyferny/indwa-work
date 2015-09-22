package com.alpine.datamining.api.impl.db.attribute.sampling;

import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;

public abstract class AbstractSamplingMultiDBPGGP implements ISamplingMultiDB {

	ISqlGeneratorMultiDB sqlGenerator;
	
	@Override
	public String generateResultTable(String tableName, String outputType,String tempTableName,
			String tempRandTableName,String columnNames, int tableIndex, long limit,String disjoint, String appendOnlyString, String endingString) {
		String sqlCreateTable;
		if(disjoint!=null&&disjoint.equalsIgnoreCase(Resources.TrueOpt)){
			sqlCreateTable = "CREATE "+outputType+" "+tableName+(outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "")+" AS SELECT  "+columnNames+" FROM "+tempTableName+" AS a, "+tempRandTableName+" AS b "
							+ " WHERE (a.alpine_sample_id = b.alpine_sample_id) "
							+ "ORDER BY b.rand_order "
							+ "LIMIT "+limit+" OFFSET "+(tableIndex * limit)+" ";
		}
		else{
			sqlCreateTable = "CREATE "+outputType+" "+tableName+(outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "")+" AS SELECT  "+columnNames+" FROM "+tempTableName+" AS a, "+tempRandTableName+" AS b "
							+ " WHERE (a.alpine_sample_id = b.alpine_sample_id) "
							+ "ORDER BY b.rand_order LIMIT "+limit;
		}
		if (outputType.equalsIgnoreCase(Resources.TableType)) {
			sqlCreateTable = sqlCreateTable + endingString;
		}
		return sqlCreateTable;
	}
	
	@Override
	public String createRandomTable(String tableName, long rowCount) {
		String sqlCreateRand = "CREATE TABLE "+tableName+" (alpine_sample_id BIGINT, rand_order NUMERIC)   " + sqlGenerator.setCreateTableEndingSql(null)+
				"; SELECT alpine_miner_generate_random_table('"+tableName+"',"+rowCount+");";
		return sqlCreateRand;
	}
	
	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName) {
		String sqlCreateSample0 = "CREATE TEMP TABLE "+tempTableName.split("\\.")[tempTableName.split("\\.").length-1]+" AS "
						+ "SELECT *,row_number() over() AS alpine_sample_id, random() as rand_order from "+inputTableName+sqlGenerator.setCreateTableEndingSql(null);
		return sqlCreateSample0;
	}

	@Override
	public String generateResultTable(String tableName, String outputType,
			String tempTableName, String columnNames, int tableIndex, long limit, long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio,  String disjoint, String appendOnlyString, String endingString,long offset) {
		String sqlCreateTable;
		if(disjoint==null||disjoint.equals(Resources.FalseOpt)){
			if (totalCount > countThreshold)
			{
				double limitRatioUsed = 0;
				if(limit < sampleThreshold){
					limitRatioUsed = sampleLimitRatio;
				}else{
					limitRatioUsed = limitRatio;

				}
				sqlCreateTable= "create "+outputType+" "+tableName + (outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "") +" AS select "+columnNames+" from (SELECT "+columnNames+"," +
						" random() as rand_order from "+tempTableName+") foo   where foo.rand_order <= "+(limit*limitRatioUsed/totalCount)+" limit ";
				sqlCreateTable += limit;
			}else{

					sqlCreateTable= "create "+outputType+" "+tableName+ (outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "") +" AS select "+columnNames+" from (SELECT "+columnNames+"," +
							" random() as rand_order from "+tempTableName+" order by rand_order limit "+limit+") foo";
			}
			sqlCreateTable = sqlCreateTable + endingString; 
		}
		else{
			sqlCreateTable = "CREATE "+outputType+" "+tableName+ (outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "") +" AS SELECT "+columnNames+" FROM "+tempTableName.split("\\.")[tempTableName.split("\\.").length-1]+" AS a "
							+ "ORDER BY a.rand_order,a.alpine_sample_id "
							+ "LIMIT "+limit+" OFFSET "+offset;
			sqlCreateTable = sqlCreateTable + endingString;
		}

		return sqlCreateTable;
	}
	
	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		String sqlCreateSample0 = "CREATE TABLE "+tempTableName+" AS "
						+ "SELECT *,row_number() over(partition by "+StringHandler.doubleQ(sampleColumn)+") AS alpine_sample_id, random() as rand_order from "+inputTableName + sqlGenerator.setCreateTableEndingSql(null);
		return sqlCreateSample0;
	}
	
	@Override
	public String insertTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		StringBuilder sqlCreateSample0 = new StringBuilder();
		sqlCreateSample0.append("INSERT INTO ").append(tempTableName).append(" SELECT *,row_number() over(partition by ")
		.append(StringHandler.doubleQ(sampleColumn)).append(") AS alpine_sample_id, random() as rand_order from ").append(inputTableName);
		return sqlCreateSample0.toString();
	}
	
	@Override
	public String generateResultTable(String tableName, String outputType) {
		String createTable="CREATE "+outputType+" "+tableName;
		return createTable;
	}
	
	@Override
	public String stratifiedSamplingAppendStringUnConsistent(String columnNames,
			String tableName, String sampleColumn, String sampleValue, Long limit, Long offset) {
		int intoffset=Math.round(offset);
		int intlimit=Math.round(limit);
		String result="(SELECT  "+columnNames+" FROM "+tableName+" AS a " +
				" WHERE a."+StringHandler.doubleQ(sampleColumn)+" = "+sampleValue+" " +
				" ORDER BY a.rand_order,a.alpine_sample_id " +
				"LIMIT "+intlimit+" OFFSET "+intoffset+")";
		return result;
	}
	@Override
	public String truncate(String tempTableName){
		StringBuilder sampleTruncate = new StringBuilder();
		sampleTruncate.append("truncate table ").append(tempTableName);
		return sampleTruncate.toString();
	}
	
	@Override
	public String stratifiedSamplingAppendStringUnConsistent(
			String columnNames, String tableName, Long limit, Long offset) {	
		String result="  SELECT  "+columnNames+" FROM "+tableName+" AS a " +
				"ORDER BY a.rand_order,a.alpine_sample_id " +
				"LIMIT "+limit+" OFFSET "+offset+" ";
		return result;
	}
	
	@Override
	public String stratifiedSamplingAppendStringConsistent(
			String columnNames, String tableName, String randTableName,
			String sampleColumn, String sampleValue, Long limit, Long offset) {
		int intlimit=Math.round(limit);
		int intoffset=Math.round(offset);
		String result=" ( SELECT  "+columnNames+" FROM "+tableName+" a, "+randTableName+" b " +
		"WHERE (a.alpine_sample_id = b.alpine_sample_id AND a."+StringHandler.doubleQ(sampleColumn)+" = "+sampleValue+") " +
		"ORDER BY b.rand_order " +
		"LIMIT "+intlimit+" OFFSET "+intoffset+")";
		return result;
	}
	
	@Override
	public String stratifiedSamplingAppendStringConsistent(
			String tableName, String randTableName, Long limit, Long offset, String columnNames) {
		String result=" SELECT  "+columnNames+" FROM "+tableName+" a, "+randTableName+" b " +
				"WHERE (a.alpine_sample_id = b.alpine_sample_id) " +
				"ORDER BY b.rand_order " +
				"LIMIT "+limit+" OFFSET "+offset+" ";
		return result;
	}
	
	@Override
	public StringBuilder generateReplacementTempTable(
			String tempviewName, String inputTable) {
		StringBuilder sb_create=new StringBuilder("create temp table ");
		sb_create.append(tempviewName).append(" as select * ,row_number() over () random_id ");//order by random()
		sb_create.append(" from ").append(inputTable).append(sqlGenerator.setCreateTableEndingSql(null));
		return sb_create;
	}
	
	@Override
	public String generateReplacementResultTable(String tableName, String outputType,
			String resultTableName, String columnNames, long limit, long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio, String appendOnlyString, String endingString) {
		String sqlCreateTable = "";
		sqlCreateTable= "create "+outputType+" "+resultTableName+(outputType.equalsIgnoreCase(Resources.TableType)? appendOnlyString : "")+" AS ";

		long cycle = limit/totalCount;
		for(long i = 0; i < cycle; i++){
			if (i > 0){
				sqlCreateTable+=" union all ";
			}
			sqlCreateTable += "( select "+columnNames+" from "+tableName+" ) ";
		}

		long left = limit - totalCount * cycle;
		String sqlLeft = "";
		if (totalCount > countThreshold)
		{
			double limitRatioUsed = 0;
			if(left < sampleThreshold){
				limitRatioUsed = sampleLimitRatio;
			}else{
				limitRatioUsed = limitRatio;
			}
			sqlLeft = " (select "+columnNames+" from (SELECT "+columnNames+"," +
					" random() as rand_order from "+tableName+") foo where foo.rand_order <= "+(left*limitRatioUsed/totalCount)+" limit ";
			sqlLeft += left+") ";
		}else{
			sqlLeft = " ( select "+columnNames+" from (SELECT "+columnNames+"," +
					" random() as rand_order from "+tableName+" order by rand_order limit "+left+") foo ) ";
		}

		if (cycle > 0){
			sqlCreateTable+=" union all "+sqlLeft;
		}else{
			sqlCreateTable+=sqlLeft;
		}
		 sqlCreateTable = sqlCreateTable + endingString;
		return sqlCreateTable;
	}

	public ISqlGeneratorMultiDB getSqlGenerator() {
		return sqlGenerator;
	}

	public void setSqlGenerator(ISqlGeneratorMultiDB sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}



}
