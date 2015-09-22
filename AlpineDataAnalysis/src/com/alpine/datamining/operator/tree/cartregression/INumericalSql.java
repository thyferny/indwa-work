package com.alpine.datamining.operator.tree.cartregression;


public interface INumericalSql {
	public String getAvgString(String labelColumnName, String columnName, String tableName, String whereCondition);
	public StringBuilder getVarCountSql(String whereCondition,
			String tableName, String labelColumnName, StringBuffer whereEqual,
			StringBuffer whereNon, String columnName);

	public StringBuffer getVarianceSql(String labelColumnNameWithCast,
			String whereCondition, String tableName);


	public StringBuffer getNumericSplitSql(String labelColumnNameWithCast,
			String labelColumnName, String columnName, String whereCondition,
			String tableName, double variance) ;

	public String getChangeToLeafSql(String selectSQL, String labelColumnName);

}
