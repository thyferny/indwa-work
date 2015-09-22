package com.alpine.datamining.operator.tree.threshold;

import java.util.Random;

import com.alpine.datamining.db.Column;
import com.alpine.utility.tools.StringHandler;

public class MultiDBSqlOracle implements MultiDBSql {
	
	private String getFloatFromArray="alpine_miner_get_fa_element";

	@Override
	public String generateNumericSql(Column labelColumn,
			String labelColumnName, String columnName,
			String selectSQL, Standard criterion) {
		StringBuilder sql = new StringBuilder("");
        Random random = new Random();
        int randomInt = Math.abs(random.nextInt());
        
        String countAlias = "count"+randomInt;
        String lessAlias = "less"+randomInt;
        String moreAlias = "more"+randomInt;
        String lessAllAlias = "lessall"+randomInt;
        String moreAllAlias = "moreall"+randomInt;
        String columnValueAllAlias = "valueall"+randomInt;
        String allSumAlias = "allsum"+randomInt;
 
        String avgAlias = "avg"+randomInt;
        String infoGainAlias = "infogain"+randomInt;
        
        StringBuilder count =new StringBuilder("floatarray(");
        StringBuilder less = new StringBuilder("floatarray(");
        StringBuilder more = new StringBuilder("floatarray(");
        StringBuilder lessAll = new StringBuilder("");
        StringBuilder moreAll = new StringBuilder("");
        StringBuilder columnValueAll = new StringBuilder("floatarray(");
        StringBuilder allSum = new StringBuilder("");
        StringBuilder entropy =new StringBuilder(" ( ");
        StringBuilder lessConditionalEntropy=new StringBuilder(" ( ");
        	lessConditionalEntropy.append(lessAllAlias).append(")*1.0/(").append(allSumAlias).append(" )*(");
        StringBuilder moreConditionalEntropy = new StringBuilder(" ( ");
        	moreConditionalEntropy.append(moreAllAlias).append(")*1.0/(").append(allSumAlias).append(" )*(");

        for ( int i = 0; i < labelColumn.getMapping().size(); i ++)
        {
        	if (i != 0)
        	{
        		count.append(",");
        		less.append(",");
        		more.append(",");
        		lessAll.append("+");
        		moreAll.append("+");
        		columnValueAll.append(",");
        		allSum.append("+");
        		entropy.append(" + ");
        		lessConditionalEntropy.append("+");
        		moreConditionalEntropy.append("+");
        	}
        	count.append("sum(case when ").append(labelColumnName).append(" = '").append(StringHandler.escQ(labelColumn.getMapping().mapIndex(i))); 
        	count.append("' then 1 else 0 end) ");
        	less.append(" sum(").append(getFloatFromArray).append("(").append(countAlias).append(",").append(i+1).append("))  over( order by ").append(avgAlias).append(") ");
        	more.append(" sum(").append(getFloatFromArray).append("(").append(countAlias).append(",").append(i+1).append("))  over( order by ").append(avgAlias).append(" desc) ");
        	more.append("-").append(getFloatFromArray).append("(").append(countAlias).append(",").append(i+1).append(") ");
        	lessAll.append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append(")");
        	moreAll.append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append(")");
        	columnValueAll.append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append(") + ");
        	columnValueAll.append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append(") ");
        	allSum.append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append(") + ");
        	allSum.append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append(") ");
        	entropy.append("(case when (").append(getFloatFromArray).append("(").append(columnValueAllAlias).append(",").append(i+1).append(")) > 0 then (-1.0*((");
        	entropy.append(getFloatFromArray).append("(").append(columnValueAllAlias).append(",").append(i+1).append("))*1.0/(").append(allSumAlias).append(" )* log(2.0, (");
        	entropy.append(getFloatFromArray).append("(").append(columnValueAllAlias).append(",").append(i+1).append("))*1.0/(").append(allSumAlias).append(")))) else 0 end)");
        	lessConditionalEntropy.append("(case when (").append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append(")) > 0 then -1.0*(");
        	lessConditionalEntropy.append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append("))*1.0/(").append(lessAllAlias).append(") * log(2.0, (");
        	lessConditionalEntropy.append(getFloatFromArray).append("(").append(lessAlias).append(",").append(i+1).append("))*1.0/(").append(lessAllAlias).append(")) else 0 end)");
        	moreConditionalEntropy.append("(case when (").append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append(")) > 0 then -1.0*(");
        	moreConditionalEntropy.append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append("))*1.0/(").append(moreAllAlias).append(") * log(2.0, (");
        	moreConditionalEntropy.append(getFloatFromArray).append("(").append(moreAlias).append(",").append(i+1).append("))*1.0/(").append(moreAllAlias).append(")) else 0 end)");
        }
        
        count.append(") as ").append(countAlias).append(" ");
        less.append(") as ").append(lessAlias).append(" ");
        more.append(") as ").append(moreAlias).append(" ");
        lessAll.append(" as ").append(lessAllAlias).append(" ");
        moreAll.append(" as ").append(moreAllAlias).append(" ");
        columnValueAll.append(") as ").append(columnValueAllAlias).append(" ");
        allSum.append(" as ").append(allSumAlias).append(" ");
        entropy.append(" ) ");
        lessConditionalEntropy.append(") ");
        moreConditionalEntropy.append(") ");

        StringBuilder infomationGain=new StringBuilder("(case when ");
        infomationGain.append(entropy).append("-").append(lessConditionalEntropy).append("-").append(moreConditionalEntropy).append("  < ");
        infomationGain.append(((DBInformationGainStandard)criterion).getMinimalGain()).append("*(").append(entropy).append(") then 0  else (");
        infomationGain.append(entropy).append("-").append(lessConditionalEntropy).append("-").append(moreConditionalEntropy).append(") end )as ");
        infomationGain.append(infoGainAlias).append(" ");

        sql.append("select ").append(avgAlias).append(", ").append(infomationGain).append("  from ( ");
        sql.append("select ").append(avgAlias).append(", ").append(countAlias).append(", ").append(lessAlias).append(" ,").append(moreAlias);
        sql.append(", ").append(lessAll).append(", ").append(moreAll).append(",").append(columnValueAll).append(", ").append(allSum).append(" from( ");
        sql.append("select ").append(avgAlias).append(",").append(countAlias).append(",").append(less).append(",").append(more).append(" from(");
        sql.append("select ").append(avgAlias).append(",").append(count).append(" from (").append("select ").append(labelColumnName);
        sql.append(",").append("avg(").append(columnName).append(")  over ( order by ").append(columnName);
        sql.append(" rows between 0 preceding and 1 following ) as ").append(avgAlias).append(" from ( ").append(selectSQL).append(")");
        sql.append(" foo) ").append(" foo1 group by ").append(avgAlias).append(")  ").append(" foo2) ");
        sql.append(" foo3 where ").append(avgAlias).append(" not in (select ").append(columnName).append(" from (").append(selectSQL);
        sql.append(")  foo4 ))").append(" foo5 order by ").append(infoGainAlias).append(" desc, ").append(avgAlias);
		return sql.toString();
	}

	public StringBuffer getMostLabelIndexSql(String selectSQL,
			String labelcolumnName) {
		StringBuffer sql = new StringBuffer();
		sql.append("select ").append(labelcolumnName).append(
				" label from (select ").append(labelcolumnName).append(
				", count(*) alpine_count from (").append(selectSQL).append(
				") foo group by ").append(labelcolumnName).append(
				" order by alpine_count desc) fooo ");
		sql.append(" where rownum <= 1");
		return sql;
	}

}
