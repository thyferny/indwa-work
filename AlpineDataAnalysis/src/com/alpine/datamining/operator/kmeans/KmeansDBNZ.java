package com.alpine.datamining.operator.kmeans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.utility.tools.StringHandler;

public class KmeansDBNZ implements IKmeansDB {

	@Override
	public void dealResult(DataSet dataSet, int columncount,
			boolean maxColumnFlag, ResultSet rs, int i_i,
			ArrayList<String> centerPointForEachCluster, int j,
			int lineThrethhold) throws SQLException {
		centerPointForEachCluster.add(String.valueOf(rs.getFloat(3 + i_i
				* columncount + j)));
	}

	@Override
	public String dropTableIfExists(String schemaName, String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("call droptable_if_existsdoubleq(");
		sb.append("'").append(StringHandler.doubleQ(tableName)).append("')");
		return sb.toString();
	}

	@Override
	public void dropTemp(Statement st, String tempTableName , String copyTableName) {
		// TODO Auto-generated method stub
	}

	@Override
	public StringBuilder generateCreateCopyTableSql(String tableName,
			String tempid, String copyTableName) {
		StringBuilder sb_createCopy = new StringBuilder("create temp table ");
		sb_createCopy.append(copyTableName);
		sb_createCopy.append(" as select *,row_number() over (order by random()) ").append(
				tempid).append(" from ").append(tableName);
		return sb_createCopy;
	}

	@Override
	public String generateFunction(DataSet dataSet, boolean maxColumnFlag,
			int lineThrethhold, int k, int maxOptimizationSteps, int maxRuns,
			String newTableName, String temptablename, String id,
			String tempid, String clusterColumnName, String columnNameList,
			String columnArrayList, int columnsSize, int distance) {
		//CALL nza..KMEANS('intable=IRIS, id=ID, target=, k=3,
//		maxiter=20, distance=euclidean, model=ci_km5c, outtable=ci_km5m');
		StringBuilder sb=new StringBuilder();
		sb.append("call nza..KMEANS('intable=").append(newTableName);
		sb.append(",id=").append(id).append(",targer=");
		return null;
	}

	@Override
	public List<Double> getArrayResult(ResultSet rs, int index)
			throws SQLException {
		List<Double> ret = new ArrayList<Double>();
		Number[] array = (Number[]) rs.getArray(index).getArray();
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				ret.add(array[i].doubleValue());
			}
		}
		return ret;
	}

	@Override
	public boolean isUseArray() {
		return false;
	}

	@Override
	public void setUseArray(boolean useArray) {

	}

}
