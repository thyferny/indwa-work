package com.alpine.datamining.operator.tree.cartclassification;

import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.utility.tools.StringHandler;

public class CartClassificationPostgres implements ICartClassfication {

	@Override
	public String generateNumericSql(Column labelColumn,
			String labelColumnName, String columnName,
			String whereCondition, String tableName, double distinctRatio) {
		StringBuilder sql;
		sql = new StringBuilder("");
		StringBuilder count = new StringBuilder("array[");
		StringBuilder less = new StringBuilder("array[");
		StringBuilder more = new StringBuilder("array[");
		StringBuilder lessAll = new StringBuilder("");
		StringBuilder moreAll = new StringBuilder("");
		StringBuilder columnValueAll = new StringBuilder("array[");
		StringBuilder allSum = new StringBuilder("");
		StringBuilder gini = new StringBuilder("(1 + ");
		StringBuilder lessGini = new StringBuilder("(");
		lessGini.append("lessall)*1.0/(allsum )*(1 + ");
		StringBuilder moreGini = new StringBuilder("(");
		moreGini.append("moreall)*1.0/(allsum )*(1 + ");

		for (int i = 0; i < labelColumn.getMapping().size(); i++) {
			if (i != 0) {
				count.append(",");
				less.append(",");
				more.append(",");
				lessAll.append("+");
				moreAll.append("+");
				columnValueAll.append(",");
				allSum.append("+");
				gini.append("+");
				lessGini.append("+");
				moreGini.append("+");

			}
			count.append("sum((case when ").append(labelColumnName).append(
					" = '").append(StringHandler.escQ(labelColumn.getMapping().mapIndex(i)));
			count.append("' then 1 else 0 end)::double precision) ");

			less.append(" sum(count[").append(i + 1).append(
					"])  over( order by avg) ");
			more.append(" sum(count[").append(i + 1).append(
					"])  over( order by avg desc) - ");
			more.append("count[").append(i + 1).append("]");
			lessAll.append("less[").append(i + 1).append("]");
			moreAll.append("more[").append(i + 1).append("]");
			columnValueAll.append("less[").append(i + 1).append("]+more");
			columnValueAll.append("[").append(i + 1).append("]");
			allSum.append("less[").append(i + 1).append("]+more");
			allSum.append("[").append(i + 1).append("]");
			gini.append("(case when allsum > 0 then (-(valueall ");
			gini.append("[").append(i + 1)
					.append("])*1.0/(allsum )*(valueall[");
			gini.append(i + 1).append("])*1.0/(allsum )) else 0 end)");
			lessGini.append("(case when lessall > 0 then -1.0*(less");
			lessGini.append("[").append(i + 1).append("])*1.0/(lessall)*(less");
			lessGini.append("[").append(i + 1).append(
					"])*1.0/(lessall) else 0 end)");
			moreGini.append("(case when moreall > 0 then -1.0*(more");
			moreGini.append("[").append(i + 1).append("])*1.0/(moreall)*(more");
			moreGini.append("[").append(i + 1).append(
					"])*1.0/(moreall) else 0 end)");
		}

		count.append("] as count ");
		less.append("] as less ");
		more.append("] as more ");
		lessAll.append(" as lessall ");
		moreAll.append(" as moreall ");
		columnValueAll.append("] as valueall ");
		allSum.append(" as allsum ");
		gini.append(" ) ");
		lessGini.append(" ) ");
		moreGini.append(" ) ");
		double minimalGain = 0;

		StringBuilder impurityReduction = new StringBuilder("(case when ");
		impurityReduction.append(gini).append("-").append(lessGini).append("-")
				.append(moreGini).append(" < ");
		impurityReduction.append(minimalGain).append("*(").append(gini).append(
				") then 0  else (").append(gini);
		impurityReduction.append("-").append(lessGini).append("-").append(
				moreGini).append(") end )as ");
		impurityReduction.append("impurityReduction ");

		sql.append("select avg, ").append(impurityReduction)
				.append("  from ( ");
		sql.append("select avg, count").append(",less");
		sql.append(" ,more , ").append(lessAll).append(", ").append(moreAll)
				.append(",");
		sql.append(columnValueAll).append(", ").append(allSum).append(
				" from( ").append("select ");
		sql.append("avg, count,").append(less).append(",").append(more).append(
				" ");
		sql.append(" from (");
		double distinctRatioThreshold = Double.parseDouble(AlpineDataAnalysisConfig.CART_DISTINCT_RATIO_THRESHOLD);
		if(distinctRatio < distinctRatioThreshold)
		{
			sql.append("select (case when alpineleadvalue is not null then (alpineleadvalue::float+ ").append(columnName).append(")/2.0 else ").append(columnName).append(" end) as avg"); 
			sql.append(",count").append(" from ( ");			
			sql.append("select  ").append(columnName).append(", lead(").append(columnName).append(")").append("  over ( order by ").append(columnName).append(") as alpineleadvalue  ,count").append(
					" from ( ");
			sql.append("select ").append(columnName);
			sql.append(",").append(count);
			sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").append(columnName).append(" ) as foo1 ) as foo2)  as foo3 ) as foo0)");
		}
		else
		{
			sql.append("select avg,").append(count).append(
					" from ( ");
			sql.append("select ").append(labelColumnName);
	        sql.append(",  (case when alpineleadvalue is not null then (alpineleadvalue::float+ ").append(columnName).append(")/2.0 else ").append(columnName).append(" end) as avg ");
			sql.append(" from (");
			sql.append("select ").append(labelColumnName).append(",");
	        sql.append(columnName).append(", lead(").append(columnName).append(")").append("  over ( order by ").append(columnName).append(") as alpineleadvalue ");
			sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(")as foo0 ) as foo1 group by avg) as foo2)  as foo3 left join ( select ").append(columnName)
					.append(" from  ").append(tableName).append(" ").append(whereCondition).append("  ) as b on foo3.");
			sql.append("avg=b.").append(columnName).append(" where b.")
					.append(columnName);
			sql.append(" is null)");
		}
		sql.append("as foo5 order by impurityReduction desc, avg limit 1; ");
		return sql.toString();
	}

	
	@Override
	public String genarateChiSquareSql(Column labelColumn,
			String labelColumnName, String columnName, String whereCondition,
			String tableName, double distinctRatio) {
		StringBuilder sql;
		sql = new StringBuilder("");
		StringBuilder count = new StringBuilder("array[");
		StringBuilder less = new StringBuilder("array[");
		StringBuilder more = new StringBuilder("array[");
		StringBuilder lessAll = new StringBuilder("");
		StringBuilder moreAll = new StringBuilder("");
		StringBuilder columnValueAll = new StringBuilder("array[");
		StringBuilder allSum = new StringBuilder("");
		StringBuffer valueAll=new StringBuffer();
		StringBuffer lessSum=new StringBuffer();
		StringBuffer moreSum=new StringBuffer();

		for (int i = 0; i < labelColumn.getMapping().size(); i++) {
			if (i != 0) {
				count.append(",");
				less.append(",");
				more.append(",");
				lessAll.append("+");
				moreAll.append("+");
				columnValueAll.append(",");
				allSum.append("+");
				lessSum.append("+");
				moreSum.append("+");
			}
			count.append("sum(case when ").append(labelColumnName).append(
					" = '").append(StringHandler.escQ(labelColumn.getMapping().mapIndex(i)));
			count.append("' then 1 else 0 end) ");

			less.append(" sum(count[").append(i + 1).append(
					"])  over( order by avg) ");
			more.append(" sum(count[").append(i + 1).append(
					"])  over( order by avg desc) - ");
			more.append("count[").append(i + 1).append("]");
			lessAll.append("less[").append(i + 1).append("]");
			moreAll.append("more[").append(i + 1).append("]");
			columnValueAll.append("less[").append(i + 1).append("]+more");
			columnValueAll.append("[").append(i + 1).append("]");
			allSum.append("less[").append(i + 1).append("]+more");
			allSum.append("[").append(i + 1).append("]");
			valueAll.append("   when valueall[").append(i + 1).append("]= 0 then 0 ");
			lessSum.append("power((less[").append(i + 1).append("]-valueall[").append(i + 1).append("]*lessall/allsum),2)/(valueall[").append(i + 1).append("]*lessall/allsum)");
			moreSum.append("power((more[").append(i + 1).append("]-valueall[").append(i + 1).append("]*moreall/allsum),2)/(valueall[").append(i + 1).append("]*moreall/allsum)");
			
		}

		count.append("] as count ");
		less.append("] as less ");
		more.append("] as more ");
		lessAll.append(" as lessall ");
		moreAll.append(" as moreall ");
		columnValueAll.append("] as valueall ");
		allSum.append(" as allsum ");

		StringBuilder chiSqaure = new StringBuilder();
		chiSqaure.append("(case when lessall = 0 then 0   when moreall = 0 then 0  ");
		;
		chiSqaure.append(valueAll);

		chiSqaure.append(" else ").append(lessSum).append(" + ").append(moreSum);


		chiSqaure.append(" end) chiSquare ");

		sql.append("select avg, ").append(chiSqaure)
				.append("  from ( ");
		sql.append("select avg, count").append(",less");
		sql.append(" ,more , ").append(lessAll).append(", ").append(moreAll)
				.append(",");
		sql.append(columnValueAll).append(", ").append(allSum).append(
				" from( ").append("select ");
		sql.append("avg, count,").append(less).append(",").append(more).append(
				" ");
		sql.append(" from (");
		double distinctRatioThreshold = Double.parseDouble(AlpineDataAnalysisConfig.CART_DISTINCT_RATIO_THRESHOLD);
		if(distinctRatio < distinctRatioThreshold)
		{
			sql.append("select (case when alpineleadvalue is not null then (alpineleadvalue::float+ ").append(columnName).append(")/2.0 else ").append(columnName).append(" end) as avg"); 
			sql.append(",count").append(" from ( ");			
			sql.append("select  ").append(columnName).append(", lead(").append(columnName).append(")").append("  over ( order by ").append(columnName).append(") as alpineleadvalue  ,count").append(
					" from ( ");
			sql.append("select ").append(columnName);
			sql.append(",").append(count);
			sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").append(columnName).append(" ) as foo1 ) as foo2)  as foo3 ) as foo0)");
		}
		else
		{
			sql.append("select avg,").append(count).append(
					" from ( ");
			sql.append("select ").append(labelColumnName);
	        sql.append(",  (case when alpineleadvalue is not null then (alpineleadvalue::float+ ").append(columnName).append(")/2.0 else ").append(columnName).append(" end) as avg ");
			sql.append(" from (");
			sql.append("select ").append(labelColumnName).append(",");
	        sql.append(columnName).append(", lead(").append(columnName).append(")").append("  over ( order by ").append(columnName).append(") as alpineleadvalue ");
			sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(")as foo0 ) as foo1 group by avg) as foo2)  as foo3 left join ( select ").append(columnName)
					.append(" from  ").append(tableName).append(" ").append(whereCondition).append("  ) as b on foo3.");
			sql.append("avg=b.").append(columnName).append(" where b.")
					.append(columnName);
			sql.append(" is null)");
		}
	
		sql.append("as foo5 order by chiSquare desc, avg limit 1; ");
		return sql.toString();

	}
	public StringBuffer getNominalGiniSql2Class(String labelName,
			List<String> labelList, String columnName, String whereCondition,
			String tableName) {
		if (whereCondition == null || whereCondition.length() == 0)
    	{
    		whereCondition = "";
    	}
    	else
    	{
    		whereCondition = " where "+whereCondition;
    	}
		StringBuffer countSum = new StringBuffer();
		countSum.append("select ").append(columnName).append(
				",(sum(case when ").append(labelName);
		countSum.append("='").append(StringHandler.escQ(labelList.get(0))).append(
				"' then 1 else 0 end))*1.0 sum1,(sum(case when ");
		countSum.append(labelName).append("='").append(StringHandler.escQ(labelList.get(1)))
				.append("' then 1 else 0 end))*1.0 sum2 from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").append(
				columnName);

		StringBuffer countAllSum = new StringBuffer();
		countAllSum.append("select ").append(columnName).append(
				",sum1/(sum1+sum2) probability,");
		countAllSum.append("(sum(sum1) over(order by  sum1/(sum1+sum2), ")
				.append(columnName);
		countAllSum.append(
				")) sum1asc ,(sum(sum2) over(order by sum1/(sum1+sum2) ,")
				.append(columnName);
		countAllSum
				.append(
						")) sum2asc, (sum(sum1) over(order by  sum1/(sum1+sum2) desc, ")
				.append(columnName).append(" desc");
		countAllSum
				.append(
						") - sum1) sum1desc,(sum(sum2) over(order by  sum1/(sum1+sum2) desc , ")
				.append(columnName);
		countAllSum.append(" desc) -sum2)  sum2desc from (").append(countSum)
				.append(") foo");

		StringBuffer countProbability = new StringBuffer();
		countProbability.append("select ").append(columnName).append(
				",probability, (case when sum1asc + sum2asc !=0 ");
		countProbability
				.append(" and sum1desc + sum2desc != 0 then ( 1- ((sum1asc+sum1desc)*(sum1asc+sum1desc)");
		countProbability
				.append("+ (sum2asc+sum2desc)*(sum2asc+sum2desc))/((sum1asc + sum2asc + sum1desc + sum2desc)*");
		countProbability
				.append("(sum1asc + sum2asc + sum1desc + sum2desc)) )- (sum1asc + sum2asc)/(sum1asc + sum2asc + sum1desc + sum2desc) * ");
		countProbability
				.append("(1- ((sum1asc)*(sum1asc) +(sum2asc)*(sum2asc))/((sum1asc + sum2asc)*(sum1asc + sum2asc)) )");
		countProbability
				.append(" - (sum1desc + sum2desc)/(sum1asc + sum2asc + sum1desc + sum2desc) * (1- ((sum1desc)*(sum1desc) +(sum2desc)*(sum2desc))/");
		countProbability
				.append(" ((sum1desc + sum2desc)*(sum1desc + sum2desc)) )  else null end) as gini from (");
		countProbability.append(countAllSum).append(
				") foo1 order by probability,").append(columnName);
		return countProbability;
	}

	
	@Override
	public void genarateProbability(String columnName,
			StringBuffer countAllSum, StringBuffer countProbability) {{
				countProbability.append("select ").append(columnName).append(
						",probability, (case when sum1asc+sum2asc=0 then 0.0  when sum1desc+sum2desc=0 then 0.0" +
						" else power((sum1asc-(sum1asc+sum2asc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1asc+sum2asc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) " +
						" + power((sum2asc-(sum1asc+sum2asc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2) " +
						"/((sum1asc+sum2asc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc))" +
						"+power((sum1desc-(sum1desc+sum2desc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1desc+sum2desc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) " +
						"+ power((sum2desc-(sum1desc+sum2desc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1desc+sum2desc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) end )"+
						" as chiSquare " +

						"from (");
				countProbability.append(countAllSum).append(
						") foo1 order by probability,").append(columnName);
			}
	}

}
