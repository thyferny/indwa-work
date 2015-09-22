/**
* ClassName CartClassificationNZ.java
*
* Version information: 1.00
*
* Data: 29 Dec 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.operator.tree.cartclassification;

import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public class CartClassificationNZ implements ICartClassfication{
	@Override
	public String generateNumericSql(Column labelColumn,
			String labelColumnName, String columnName,
			String whereCondition, String tableName, double distinctRatio) {
		StringBuilder sql;
		sql = new StringBuilder("");
		StringBuilder count = new StringBuilder();
		StringBuilder less = new StringBuilder();
		StringBuilder more = new StringBuilder();
		StringBuilder lessAll = new StringBuilder("");
		StringBuilder moreAll = new StringBuilder("");
		StringBuilder columnValueAll = new StringBuilder();
		StringBuilder allSum = new StringBuilder("");
		StringBuilder gini = new StringBuilder("(1 + ");
		StringBuilder lessGini = new StringBuilder("(");
		lessGini.append("lessall)*1.0/(allsum )*(1 + ");
		StringBuilder moreGini = new StringBuilder("(");
		moreGini.append("moreall)*1.0/(allsum )*(1 + ");
		StringBuffer alpineCount = new StringBuffer();
		StringBuffer alpineLess = new StringBuffer();
		StringBuffer alpineMore = new StringBuffer();

		for (int i = 0; i < labelColumn.getMapping().size(); i++) {
			if (i != 0) {
				alpineCount.append(",");
				alpineLess.append(",");
				alpineMore.append(",");
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
			alpineCount.append("alpine_count").append(i + 1);
			alpineLess.append("alpine_less").append(i + 1);
			alpineMore.append("alpine_more").append(i + 1);

			count.append("(sum(case when ").append(labelColumnName).append(
					" = '").append(StringHandler.escQ(labelColumn.getMapping().mapIndex(i)));
			count.append("' then 1 else 0 end))::double alpine_count").append(i+1);

			less.append(" sum(").append("(alpine_count").append(i + 1).append(
					"))  over( order by avg) alpine_less").append(i + 1);
			more.append(" sum(").append("(alpine_count").append(i + 1).append(
					"))  over( order by avg desc) - ");
			more.append("(alpine_count").append(i + 1).append(") alpine_more").append(i + 1);
			lessAll.append("(alpine_less").append(i + 1).append(")");
			moreAll.append("(alpine_more").append(i + 1).append(")");
			columnValueAll.append("(alpine_less").append(i + 1).append(")+");
			columnValueAll.append("(alpine_more");
			columnValueAll.append(i + 1).append(") valueall").append(i+1);
			allSum.append("(alpine_less").append(i + 1).append(")+");
			allSum.append("(alpine_more");
			allSum.append(i + 1).append(")");
			gini.append("(case when allsum > 0 then (-(");
			gini.append("(valueall");
			gini.append(i + 1).append("))*1.0/(allsum )*(");
			gini.append("(valueall");
			gini.append(i + 1).append("))*1.0/(allsum)) else 0 end)");
			lessGini.append("(case when lessall > 0 then -1.0*(");
			lessGini.append("(alpine_less");
			lessGini.append(i + 1).append("))*1.0/(lessall)*(");
			lessGini.append("(alpine_less");
			lessGini.append(i + 1).append(
					"))*1.0/(lessall) else 0 end)");
			moreGini.append("(case when moreall > 0 then -1.0*(");
			moreGini.append("(alpine_more");
			moreGini.append(i + 1).append("))*1.0/(moreall)*(");
			moreGini.append("(alpine_more");
			moreGini.append(i + 1).append(
					"))*1.0/(moreall) else 0 end)");
		}


		lessAll.append(" as lessall ");
		moreAll.append(" as moreall ");

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
		sql.append("select avg, ").append(alpineCount).append(",").append(alpineLess);
		sql.append(" ,").append(alpineMore).append(" , ").append(lessAll).append(", ").append(moreAll)
				.append(",");
		sql.append(columnValueAll).append(", ").append(allSum).append(
				" from( ").append("select ");
		sql.append("avg, ").append(alpineCount).append(",").append(less).append(",").append(more).append(
				" ");
		sql.append(" from (");
		sql.append("select   (avg((").append(columnName).append(
				")::double)  over ( order by ").append(columnName);
		sql.append(" rows between 0 preceding and 1 following )) as avg ,").append(alpineCount).append(
				" from ( ");
		sql.append("select ").append(columnName);
		sql.append(",").append(count);
		sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").
		append(columnName).append(" ) foo1 ) foo2) foo3 )");
		sql.append(" foo5 order by impurityReduction desc, avg");
		return sql.toString();
	}

	@Override
	public String genarateChiSquareSql(Column labelColumn,
			String labelColumnName, String columnName, String whereCondition,
			String tableName, double distinctRatio) {
		StringBuilder sql;
		sql = new StringBuilder("");
		StringBuilder count = new StringBuilder();
		StringBuilder less = new StringBuilder();
		StringBuilder more = new StringBuilder();
		StringBuilder lessAll = new StringBuilder("");
		StringBuilder moreAll = new StringBuilder("");
		StringBuilder columnValueAll = new StringBuilder();
		StringBuilder allSum = new StringBuilder("");
		
		StringBuffer alpineCount = new StringBuffer();
		StringBuffer alpineLess = new StringBuffer();
		StringBuffer alpineMore = new StringBuffer();
		StringBuffer valueAll=new StringBuffer();
		StringBuffer lessSum=new StringBuffer();
		StringBuffer moreSum=new StringBuffer();

		for (int i = 0; i < labelColumn.getMapping().size(); i++) {
			if (i != 0) {
				alpineCount.append(",");
				alpineLess.append(",");
				alpineMore.append(",");
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
		
		
			alpineCount.append("alpine_count").append(i + 1);
		
			
			alpineLess.append("alpine_less").append(i + 1);
			alpineMore.append("alpine_more").append(i + 1);

			count.append("(sum(case when ").append(labelColumnName).append(
					" = '").append(StringHandler.escQ(labelColumn.getMapping().mapIndex(i)));
			count.append("' then 1 else 0 end))::double alpine_count").append(i+1);

			less.append(" sum(").append("(alpine_count").append(i + 1).append(
					"))  over( order by avg) alpine_less").append(i + 1);
			more.append(" sum(").append("(alpine_count").append(i + 1).append(
					"))  over( order by avg desc) - ");
			more.append("(alpine_count").append(i + 1).append(") alpine_more").append(i + 1);
			lessAll.append("(alpine_less").append(i + 1).append(")");
			moreAll.append("(alpine_more").append(i + 1).append(")");
			columnValueAll.append("(alpine_less").append(i + 1).append(")+");
			columnValueAll.append("(alpine_more");
			columnValueAll.append(i + 1).append(") valueall").append(i+1);
			allSum.append("(alpine_less").append(i + 1).append(")+");
			allSum.append("(alpine_more");
			allSum.append(i + 1).append(")");
			valueAll.append("   when ")
			.append(" valueall").append(i + 1).append(" = 0  then 0.0 ");
			lessSum.append("pow(").append(" ").append("alpine_less").append(i + 1)
			.append("-").append("valueall").append(i + 1).append("*lessall*1.0/allsum ,2)/(")
			.append("valueall").append(i + 1).append("*lessall*1.0/allsum) ");
			moreSum.append("pow(").append("alpine_more").append(i + 1)
			.append("-").append("valueall").append(i + 1).append("*moreall*1.0/allsum ,2)/(")
			.append("valueall").append(i + 1).append("*moreall*1.0/allsum) ");

			
		}


		lessAll.append(" as lessall ");
		moreAll.append(" as moreall ");
		allSum.append(" as allsum ");
	
		

		StringBuilder chiSquare = new StringBuilder("");
		
		chiSquare.append("(case when lessall = 0 then 0.0   when moreall = 0 then 0.0  ");
		;
		chiSquare.append(valueAll);

		chiSquare.append(" else ").append(lessSum).append(" + ").append(moreSum);


		chiSquare.append(" end ) chiSquare ");

		
		

		sql.append("select avg, ").append(chiSquare)
				.append("  from ( ");
		sql.append("select avg, ").append(alpineCount).append(",").append(alpineLess);
		sql.append(" ,").append(alpineMore).append(" , ").append(lessAll).append(", ").append(moreAll)
				.append(",");
		sql.append(columnValueAll).append(", ").append(allSum).append(
				" from( ").append("select ");
		sql.append("avg, ").append(alpineCount).append(",").append(less).append(",").append(more).append(
				" ");
		sql.append(" from (");
		sql.append("select   (avg((").append(columnName).append(
				")::double)  over ( order by ").append(columnName);
		sql.append(" rows between 0 preceding and 1 following )) as avg ,").append(alpineCount).append(
				" from ( ");
		sql.append("select ").append(columnName);
		sql.append(",").append(count);
		sql.append(" from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").
		append(columnName).append(" ) foo1 ) foo2) foo3 )");
		sql.append(" foo5 order by chiSquare desc, avg");
		
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
				",((sum(case when ").append(labelName);
		countSum.append("='").append(StringHandler.escQ(labelList.get(0))).append(
				"' then 1 else 0 end))::double*1.0) sum1,((sum(case when ");
		countSum.append(labelName).append("='").append(StringHandler.escQ(labelList.get(1)))
				.append("' then 1 else 0 end))::double*1.0) sum2 from  ").append(tableName).append(" ").append(whereCondition).append(" group by ").append(
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

	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.tree.cartclassification.ICartClassfication#genarateProbability(java.lang.String, java.lang.StringBuffer, java.lang.StringBuffer)
	 */
	@Override
	public void genarateProbability(String columnName,
			StringBuffer countAllSum, StringBuffer countProbability) {{
				countProbability.append("select ").append(columnName).append(
						",probability, (case when sum1asc+sum2asc=0 then 0.0  when sum1desc+sum2desc=0 then 0.0" +
						" else pow((sum1asc-(sum1asc+sum2asc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1asc+sum2asc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) " +
						" + pow((sum2asc-(sum1asc+sum2asc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2) " +
						"/((sum1asc+sum2asc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc))" +
						"+pow((sum1desc-(sum1desc+sum2desc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1desc+sum2desc)*(sum1asc+sum1desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) " +
						"+ pow((sum2desc-(sum1desc+sum2desc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)),2)" +
						"/((sum1desc+sum2desc)*(sum2asc+sum2desc)/(sum1asc+sum2asc+sum1desc+sum2desc)) end )"+
						" as chiSquare " +

						"from (");
				countProbability.append(countAllSum).append(
						") foo1 order by probability,").append(columnName);
			}
	}

}
