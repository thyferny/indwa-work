package com.alpine.datamining.operator.tree.cartclassification;

import java.util.List;

import com.alpine.datamining.db.Column;

public interface ICartClassfication {

	String generateNumericSql(Column labelColumn,
			String labelColumnName, String columnName,
			String whereCondition, String tableName, double distinctRatio);
	StringBuffer getNominalGiniSql2Class(String labelName,
			List<String> labelList, String columnName, String whereCondition,
			String tableName) ;

	
	String genarateChiSquareSql(Column labelColumn,
			String labelColumnName, String columnName,
			String whereCondition, String tableName, double distinctRatio);
	void genarateProbability(String columnName, StringBuffer countAllSum,
			StringBuffer countProbability) ;
	
}
