package com.alpine.datamining.operator.tree.cartregression;


public class CartRegressionPostgres implements INumericalSql {

	public String getAvgString(String labelColumnName, String columnName, String tableName, String whereCondition){
		String sql = "";
		sql = (" select ")+(labelColumnName)+(", (case when alpineleadvalue is not null then (alpineleadvalue+ ")+(columnName)+(")/2.0 else ")+(columnName)+(" end) as avg from ( ");
		sql += (" select ")+(labelColumnName)+","+columnName+(", lead(")+(columnName)+(")")+("  over ( order by ")+(columnName)+(") as alpineleadvalue from ")+(tableName)+(" ")+(whereCondition)+") foo ";
		return sql;
	}
	public StringBuilder getVarCountSql(String whereCondition,
			String tableName, String labelColumnName, StringBuffer whereEqual,
			StringBuffer whereNon, String columnName) {
		StringBuilder sql=new StringBuilder("select ");
		sql.append("count(").append(columnName).append("),variance(1.0*").append((labelColumnName));
		sql.append("),sum(case when ").append(whereEqual).append(" then 1 else 0 end),");
		sql.append("variance("+("case when "+whereEqual+(" then ")+(labelColumnName)+(" else null end"))).append("),");
		sql.append("sum(case when ").append(whereNon).append(" then 1 else 0 end),variance(case when ");
		sql.append(whereNon).append(" then ").append(labelColumnName).append(" else null end) from ").append(tableName).append(" ").append(whereCondition);
		return sql;
	}
	public StringBuffer getVarianceSql(String labelColumnNameWithCast,
			String whereCondition, String tableName) {
		StringBuffer sql = new StringBuffer("");
		sql.append("select variance(").append((labelColumnNameWithCast)).append(") from ");
		sql.append(tableName).append(" ").append(whereCondition);
		return sql;
	}

	public StringBuffer getNumericSplitSql(String labelColumnNameWithCast,
			String labelColumnName, String columnName, String whereCondition,
			String tableName, double variance) {
		StringBuffer sql = new StringBuffer();
		sql.append("select avg,").append(variance).append(" - lessvariance");
		sql.append("*lesscount/allcount - morevariance");
		sql.append("*morecount/allcount as impurityreduction");
		sql.append(" from (").append("select avg,lessavg,moreavg");
		sql.append(",lessvariance,morevariance ,lesscount");
		sql.append(",morecount, allcount from (").append("select avg ");
		sql.append(", max(lessavg) lessavg,max(moreavg) ");
		sql.append("moreavg, max(lessvariance) lessvariance,(case when (max(");
		sql.append("morecount)-count(*) - 1) > 0 then (max(moresumproduct) - sum(");
		sql.append(labelColumnNameWithCast).append("*").append(labelColumnNameWithCast).append(") - (max(moresum) - sum(");
		sql.append(labelColumnNameWithCast).append("))*(max(moresum) - sum(").append(labelColumnNameWithCast).append("))*1.0/(max(");
		sql.append("morecount)-count(*))").append(")/(max(morecount)-count(*) - 1) else null end) morevariance");
		sql.append(" ,max(lesscount) lesscount, max(morecount)-count(*) ");
		sql.append("morecount, max(lesscount+morecount)-count(*) allcount from (");	
		sql.append("select ").append(labelColumnName).append(",avg, count(*) over (order by ");
		sql.append("avg ) as lesscount, avg(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg) as lessavg, ").append("variance(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg) as lessvariance,  count(*) over (order by avg desc) as ");
		sql.append("morecount, avg(").append(labelColumnNameWithCast).append(") over (order by avg desc) as ");
		sql.append("moreavg ,  sum(").append(labelColumnNameWithCast).append("*").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg desc) as moresumproduct ,  sum(").append(labelColumnNameWithCast).append(") over (order by ");
		sql.append("avg desc) as moresum from (");
		sql.append(getAvgString(labelColumnName, columnName, tableName, whereCondition));
		sql.append(" ) ").append(" foo0)").append(" foo2  group by avg");	
		sql.append(") ").append(" foo3 left join (select ").append(columnName).append(" from ").append(tableName).append(" ").append(whereCondition).append(") b on foo3.avg");
		sql.append("=b.").append(columnName).append(" where b.").append(columnName);
		sql.append(" is null)").append(" foo5 where morevariance is not null and ");
		sql.append("lessvariance is not null order by impurityreduction desc, avg");
		return sql;
	}
	public String getChangeToLeafSql(String selectSQL, String labelColumnName) {
		String sql = "select avg("+labelColumnName+"),variance("+labelColumnName+"),count("+labelColumnName+") from ("+selectSQL+") foo where "+labelColumnName+" is not null";
		return sql;
	}
}
