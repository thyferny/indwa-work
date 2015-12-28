
package com.alpine.datamining.operator.regressions;

import com.alpine.utility.tools.StringHandler;



public class ResidualDataGenaratorDB2 extends ResidualDataGenarator{
	public   StringBuffer getResidualString(String dependColumn,
			String fitColumn, String schemaName, String tableName,
			StringBuffer sbWhere, long count, String maxRows){
		StringBuffer sover = new StringBuffer();
		sover.append("select ").append(StringHandler.doubleQ(fitColumn))
				.append(", alpine_residual ");
		sover.append(" from (select ").append(
				StringHandler.doubleQ(fitColumn)).append(",");
		sover.append(StringHandler.doubleQ(dependColumn)
				+ "-" + StringHandler.doubleQ(fitColumn));
		sover.append(" as alpine_residual,rand()   as alpinelinearrandom from " + schemaName + "." + tableName
				+ " " + sbWhere + " order by alpinelinearrandom FETCH FIRST "+ maxRows+" ROWS ONLY) foo    ") ;
	 
		return sover;
	}

}
