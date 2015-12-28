


package com.alpine.datamining.operator.tree.cartregression;


public class CartRegressionNZ implements INumericalSql {
	public String getAvgString(String labelColumnName, String columnName, String tableName, String whereCondition){
		String sql = "";
		sql = (" select ")+(labelColumnName)+(",  avg((")+(columnName)+(")::double)  over ( order by ");
		sql += (columnName)+(" rows between 0 preceding and 1 following ) as avg from ")+(tableName)+(" ")+(whereCondition);
		return sql;
	}
	public StringBuilder getVarCountSql(String whereCondition,
			String tableName, String labelColumnName, StringBuffer whereEqual,
			StringBuffer whereNon, String columnName) {
		StringBuilder sql=new StringBuilder();
//		sql.append("select count, case when count >1 then var/(count-1)*count else 0 end, ");
//		sql.append(" countequal, case when countequal >1 then varequal/(countequal-1)*countequal else 0 end, ");
//		sql.append(" countnon, case when countnon >1 then varnon/(countnon-1)*countnon else 0 end ");
//		sql.append("  from ( ");//TODO  why do this?
		sql.append("select ");
		sql.append("count(").append(columnName).append(") as count ,variance((").append(labelColumnName);
		sql.append(")::double) as var ,sum(case when ").append(whereEqual).append(" then 1 else 0 end) as countequal ,");
		sql.append("variance("+("case when "+whereEqual+(" then ")+(labelColumnName)+(" else null end"))).append(") as varequal,");
		sql.append("sum(case when ").append(whereNon).append(" then 1 else 0 end) as countnon ,variance(case when ");
		sql.append(whereNon).append(" then ").append(labelColumnName).append(" else null end)  as varnon from ").append(tableName).append(" ").append(whereCondition);
//		sql.append(") foo");
		return sql;
	}
	public StringBuffer getVarianceSql(String labelColumnNameWithCast,
			String whereCondition, String tableName) {
		StringBuffer sql = new StringBuffer("");
		sql.append("select case when count > 1 then var /(count - 1) * count end from (");
		sql.append("select count(*) as count, variance(").append((labelColumnNameWithCast)).append(") as var from ");
		sql.append(tableName).append(" ").append(whereCondition).append(") foo ");
		return sql;
	}

	public StringBuffer getNumericSplitSql(String labelColumnNameWithCast,
			String labelColumnName, String columnName, String whereCondition,
			String tableName, double variance) {
		StringBuffer sql = new StringBuffer();
		sql.append("select avg,").append(variance).append(" - lessvariance");
		sql.append("*lesscount/allcount - morevariance");
		sql.append("*morecount/allcount as impurityreduction");
		sql.append(" from (");
		sql.append("select avg,lessavg,moreavg").append(",(case when lesscount > 1 then lessvariance else null end) lessvariance");
		sql.append(",morevariance ,lesscount");
		sql.append(",morecount, allcount from (");
		sql.append("select avg ");
		sql.append(", max(lessavg) lessavg,max(moreavg) ");
		sql.append("moreavg, max(lessvariance) lessvariance,(case when (max(");
		sql.append("morecount)-count(*) - 1) > 0 then (max(moresumproduct) - sum(");
		sql.append(labelColumnNameWithCast).append("*").append(labelColumnNameWithCast).append(") - (max(moresum) - sum(");
		sql.append(labelColumnNameWithCast).append("))*(max(moresum) - sum(").append(labelColumnNameWithCast).append("))*1.0/(max(");
		sql.append("morecount)-count(*))").append(")/(max(morecount)-count(*) - 1) else null end) morevariance");
		sql.append(" ,max(lesscount) lesscount, max(morecount)-count(*) ");
		sql.append("morecount, max(lesscount+morecount)-count(*) allcount from (");	

		sql.append("select ").append(labelColumnName).append(",avg, lesscount,lessavg, ");
		sql.append("(case when lesscount > 1 then lessvariance/(lesscount - 1) * lesscount else 0 end)  as lessvariance,");
		sql.append("morecount, moreavg , moresumproduct ,  moresum from (");

		
		
		sql.append("select ").append(labelColumnName).append(",avg, count(*) over (order by ");
		sql.append("avg ) as lesscount, avg(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg) as lessavg, ").append("variance(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg) as lessvariance,  count(*) over (order by avg desc) as ");
		sql.append("morecount, avg(").append(labelColumnNameWithCast).append(") over (order by avg desc) as ");
		sql.append("moreavg ,  sum(").append(labelColumnNameWithCast).append("*").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg desc) as moresumproduct ,  sum(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg desc) as moresum from (");
		sql.append(getAvgString(labelColumnName, columnName, tableName, whereCondition));
		sql.append(" ) ").append(" foo0) foo1 )").append(" foo2  group by avg");	
		sql.append(") ").append(" foo3 left join (select ").append(columnName).append(" from ").append(tableName).append(" ").append(whereCondition).append(") b on foo3.avg");
		sql.append("=b.").append(columnName).append(" where b.").append(columnName);
		sql.append(" is null)").append(" foo5 where morevariance is not null and ");
		sql.append("lessvariance is not null order by impurityreduction desc, avg");
		return sql;
	}
	public String getChangeToLeafSql(String selectSQL, String labelColumnName) {
		String sql = "select avg, case when count > 1 then variance/(count - 1 ) * count else null end as variance, count from (select avg(("+labelColumnName+")::double) as avg ,variance(("+labelColumnName+")::double) as variance ,count("+labelColumnName+") as count from ("+selectSQL+") foo where "+labelColumnName+" is not null) foo";
		return sql;
	}

}
