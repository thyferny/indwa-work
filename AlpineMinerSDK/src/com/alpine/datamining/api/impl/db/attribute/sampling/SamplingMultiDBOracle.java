package com.alpine.datamining.api.impl.db.attribute.sampling;


import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;

public class SamplingMultiDBOracle implements ISamplingMultiDB {
	
	ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public String generateResultTable(String tableName, String outputType,
			String tempTableName, String tempRandTableName, String columnNames,
			int tableIndex, long limit,String disjoint, String appendOnlyString, String endingString) {
		String sqlCreateTable;
		if(disjoint!=null&&disjoint.equals(Resources.TrueOpt)){
			sqlCreateTable =
					"CREATE "+outputType+" "+tableName+((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ")
							+" AS select "+columnNames+" from " +
							"(select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tempTableName+" a, "+tempRandTableName+" b "
							+ " WHERE (a.alpine_sample_id = b.alpine_sample_id) "
							+ "ORDER BY b.rand_order ) foo1 where rownum<=("+(tableIndex * limit)+"+"+limit+")) foo2 where foo2.alpine_rownum>"+(tableIndex * limit);
		}else{
			sqlCreateTable ="CREATE "+outputType+" "+tableName+((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ")
			+" AS select "+columnNames+" from (SELECT  "+columnNames+" FROM "+tempTableName+" a, "+tempRandTableName+" b "
							+ " WHERE (a.alpine_sample_id = b.alpine_sample_id)"
							+ "ORDER BY b.rand_order) where rownum<"+(limit+1);
		}
		return sqlCreateTable;
	}

	@Override
	public String createRandomTable(String tableName, long rowCount) {
		String sqlCreateRand = 
				"CREATE TABLE "+tableName+" parallel as (select row_number() over (order by 1) as alpine_sample_id," +
				"DBMS_RANDOM.VALUE as rand_order from dual connect by rownum<="+rowCount+")";
		return sqlCreateRand;
	}
	@Override
	public String createTempTable(String tableName, String columnNames, String inputTableName) {
		String sqlCreateSample0 ="CREATE TABLE "+tableName+" parallel AS "
						+ "SELECT "+columnNames+",row_number() over(order by 1) AS alpine_sample_id, DBMS_RANDOM.VALUE as rand_order from "+inputTableName;
		return sqlCreateSample0;
	}
	
	@Override
	public String generateResultTable(String tableName, String outputType,
			String tempTableName, String columnNames,
			int tableIndex, long limit, long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio, String disjoint, String appendOnlyString, String endingString,long offset) {
		String sqlCreateTable;
		if(disjoint==null||disjoint.equals(Resources.FalseOpt)){
			sqlCreateTable= "create "+outputType+" "+tableName+((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ")
			+" AS select "+columnNames+" from (SELECT "+columnNames+",row_number() over(order by 1) as " +
					" alpine_sample_id,DBMS_RANDOM.VALUE as rand_order from "+tempTableName+" order by rand_order) where rownum<"+(limit+1);
		}
		else{
			sqlCreateTable = "CREATE "+outputType+" "+tableName+((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ")
			+" AS select "+columnNames+" from (select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tempTableName+" a  "
							+ "ORDER BY a.rand_order,a.alpine_sample_id ) " 
							+ "foo1 where rownum<=("+(offset)+"+"+limit+")) foo2 where foo2.alpine_rownum>"+offset;
		}
		return sqlCreateTable;
	}
	
	@Override
	public String createTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		String sqlCreateSample0 = "CREATE TABLE "+tempTableName+" parallel AS "
						+ "SELECT "+columnNames+",row_number() over(partition by "+StringHandler.doubleQ(sampleColumn)+" order by "+StringHandler.doubleQ(sampleColumn)+") AS alpine_sample_id, " +
								"DBMS_RANDOM.VALUE as rand_order from "+inputTableName;
		return sqlCreateSample0;
	}
	
	@Override
	public String insertTempTable(String tempTableName, String columnNames,
			String inputTableName, String sampleColumn) {
		StringBuilder sqlCreateSample0 = new StringBuilder();
		sqlCreateSample0.append("INSERT INTO ").append(tempTableName).append(
				" SELECT ").append(columnNames).append(
				",row_number() over(partition by ").append(
				StringHandler.doubleQ(sampleColumn)).append(" order by ")
				.append(StringHandler.doubleQ(sampleColumn)).append(
						") AS alpine_sample_id, ").append(
						"DBMS_RANDOM.VALUE as rand_order from ").append(
						inputTableName);
		return sqlCreateSample0.toString();
	}
	@Override
	public String truncate(String tempTableName){
		StringBuilder sampleTruncate = new StringBuilder();
		sampleTruncate.append("truncate table ").append(tempTableName);
		return sampleTruncate.toString();
	}
	
	@Override
	public String generateResultTable(String tableName, String outputType) {
		String createTable="CREATE "+outputType+" "+tableName+
		((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ");
		return createTable;
	}
	@Override
	public String stratifiedSamplingAppendStringUnConsistent(
			String columnNames, String tableName, String sampleColumn, String sampleValue, Long limit, Long offset) {
		int intoffset=Math.round(offset);
		int intlimit=Math.round(limit);
		String result="(select "+columnNames+" from (select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tableName+" a "
				+ " WHERE a."+StringHandler.doubleQ(sampleColumn)+" = "+sampleValue+" " 
				+ "ORDER BY a.rand_order,a.alpine_sample_id) " 
				+ "foo1 where rownum<=("+intlimit+"+"+intoffset+")) foo2 where foo2.alpine_rownum>"+intoffset+")";
		return result;
	}
	
	@Override
	public String stratifiedSamplingAppendStringUnConsistent(
			String columnNames, String tableName, Long limit, Long offset) {
		String result="select "+columnNames+" from (select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tableName+" a "
			+ "ORDER BY a.rand_order,a.alpine_sample_id) " 
			+ "foo1 where rownum<=("+limit+"+"+offset+")) foo2 where foo2.alpine_rownum>"+offset;;
		return result;
	}
	@Override
	public String stratifiedSamplingAppendStringConsistent(
			String columnNames, String tableName, String randTableName,
			String sampleColumn, String sampleValue, Long limit, Long offset) {
		int intlimit=Math.round(limit);
		int intoffset=Math.round(offset);
		String result="(select "+columnNames+" from (select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tableName+" a, "+randTableName+" b  "
				+ "WHERE (a.alpine_sample_id = b.alpine_sample_id AND a."+StringHandler.doubleQ(sampleColumn)+" = "+sampleValue+")"
				+ "ORDER BY b.rand_order) " 
				+ "foo1 where rownum<=("+intlimit+"+"+intoffset+")) foo2 where foo2.alpine_rownum>"+intoffset+")";
		return result;
	}
	
	@Override
	public String stratifiedSamplingAppendStringConsistent(
			String tableName, String randTableName, Long limit, Long offset, String columnNames) {
		String result= "select "+columnNames+" from (select rownum alpine_rownum,foo1.* from (SELECT "+columnNames+" FROM "+tableName+" a, "+randTableName+" b  "
		+ "WHERE (a.alpine_sample_id = b.alpine_sample_id)"
		+ "ORDER BY b.rand_order) " 
		+ "foo1 where rownum<=("+limit+"+"+offset+")) foo2 where foo2.alpine_rownum>"+offset;
		
		
		return result;
	}
	
	@Override
	public StringBuilder generateReplacementTempTable(String tempTableName, String inputTable) {
		StringBuilder sb_create=new StringBuilder("create table ");
		sb_create.append(tempTableName).append(" parallel as select ").append(inputTable);
		sb_create.append(".* ,row_number() over (order by DBMS_RANDOM.VALUE) random_id ");
		sb_create.append(" from ").append(inputTable);
		return sb_create;
	}

	public String generateReplacementResultTable(String outputType,
			long sampleRowSize, String columnNames, String tempviewName,
			String resultTableName,long rowCount) {
		String sqlCreateTable= "create "+outputType+" "+resultTableName+((outputType.equalsIgnoreCase(Resources.OutputTypes[0]))?" parallel ":" ")+
		" AS select "+columnNames+" from " +
				"(SELECT ROWNUM,floor("+rowCount+
				"*DBMS_RANDOM.VALUE+1) as value FROM DUAL CONNECT BY LEVEL <"+sampleRowSize+"+1) " +
				" c left join "+tempviewName+" s on s.random_id=c.value";
		return sqlCreateTable;
	}

    @Override
    public String generateReplacementResultTable(String tableName, String outputType,
                                                 String resultTableName, String columnNames, long limit, long totalCount, long countThreshold, double limitRatio, long sampleThreshold, double sampleLimitRatio, String appendOnlyString, String endingString) {
        return generateReplacementResultTable( outputType,
                limit,  columnNames,  tableName,
                resultTableName, totalCount);
    }

	@Override
	public void setSqlGenerator(ISqlGeneratorMultiDB sqlGenerator) {
		this.sqlGenerator=sqlGenerator;
	}

 

}
