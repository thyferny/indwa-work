package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;

public interface MultiDBSql {
	
	String generateNumericSql(Column labelColumn,
			String labelColumnName, String columnName,
			String selectSQL, Standard criterion);
	public StringBuffer getMostLabelIndexSql(String selectSQL,
			String labelcolumnName);

}
