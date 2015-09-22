package com.alpine.datamining.operator.kmeans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.DataSet;

public interface IKmeansDB {
	String generateFunction(DataSet dataSet,boolean maxColumnFlag,int lineThrethhold, int k,
			int maxOptimizationSteps, int maxRuns, String newTableName,
			String temptablename, String id, String tempid, String clusterColumnName, String columnNameList,String columnArrayList,  int columnsSize, int distance);

	String dropTableIfExists(String schemaName, String tableName);
	
	StringBuilder generateCreateCopyTableSql(String tableName,String tempid,String copyTableName);
	
	void dealResult(DataSet dataSet, int columncount,
			boolean maxColumnFlag, ResultSet rs, int i_i,
			ArrayList<String> centerPointForEachCluster, int j,int lineThrethhold)
			throws SQLException;
	boolean isUseArray();
	void setUseArray(boolean useArray);
	public List<Double> getArrayResult(ResultSet rs, int index) throws SQLException;

	void dropTemp(Statement st, String tempTableName, String copyTableName);
}
