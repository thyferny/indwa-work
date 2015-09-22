package com.alpine.datamining.operator.kmeans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.MinerInit;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.tools.StringHandler;

public class KmeansDBGP implements IKmeansDB {

	private boolean useArray = false;
	@Override
	public String generateFunction(DataSet dataSet, boolean maxColumnFlag,
			int lineThrethhold, int k, int maxOptimizationSteps, int maxRuns,
			String newTableName, String temptablename, String id,
			String tempid, String clusterColumnName, String columnNameList,String columnArrayList, int columnsSize, int distance) {
		StringBuilder sql=new StringBuilder();
		if (useArray){
			sql.append("select alpine_miner_kmeans_c_array_array('").append(newTableName).append("','").append(temptablename).append("', array[");
			sql.append(columnNameList).append("], ").append("array[").append(columnArrayList).append("],").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(");");
		}else if((MinerInit.isUseCFunction()
				&&maxColumnFlag==false&&dataSet.size()<lineThrethhold))
		{
			sql.append("select alpine_miner_kmeans_c_1_5('").append(newTableName).append("','").append(temptablename).append("', array[");
			sql.append(columnNameList).append("], ").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(");");
		}else if((MinerInit.isUseCFunction()
				&&(maxColumnFlag==true||dataSet.size()>=lineThrethhold)))
		{
			sql.append("select alpine_miner_kmeans_c_array_1_5('").append(newTableName).append("','").append(temptablename).append("', array[");
			sql.append(columnNameList).append("], ").append(columnsSize).append(", '").append(id).append("', ");
//			sql.append("select alpine_miner_kmeans_c_array_array('").append(newTableName).append("','").append(temptablename).append("', array[");
//			sql.append(columnNameList).append("], ").append("array[").append(columnArrayList).append("],").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(");");
		}
		else
		{
			sql.append("select alpine_miner_kmeans_sp_1_5('").append(newTableName).append("','").append(temptablename).append("', array[");
			sql.append(columnNameList).append("], ").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(");");
		}
		return sql.toString();
	}

	@Override
	public String dropTableIfExists(String schemaName, String tableName) {
		StringBuilder sb=new StringBuilder();
		tableName=StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName);
		sb.append("drop table if exists ").append(tableName);
		return sb.toString();
	}

	@Override
	public StringBuilder generateCreateCopyTableSql(String tableName,
			String tempid, String copyTableName) {
		StringBuilder sb_createCopy=new StringBuilder("create temp table ");
		sb_createCopy.append(copyTableName);
		sb_createCopy.append(" as select *,row_number() over () ").append(tempid)
		.append(" from ").append(tableName).append(" distributed randomly");
		return sb_createCopy;
	}

	@Override
	public void dealResult(DataSet dataSet, int columncount,
			boolean maxColumnFlag, ResultSet rs, int i_i,
			ArrayList<String> centerPointForEachCluster, int j,
			int lineThrethhold) throws SQLException {
		if((dataSet.size()<lineThrethhold&&maxColumnFlag==false) && !useArray)
		{
			centerPointForEachCluster.add(String.valueOf(rs.getFloat(3
					+ i_i * columncount + j)));// 3+columncount
		}
		else
		{
			centerPointForEachCluster.add(String.valueOf(((Double[])rs.getArray(3+j).getArray())[i_i]));
		}
		
	}

	@Override
	public boolean isUseArray() {
		return useArray;
	}

	@Override
	public void setUseArray(boolean useArray) {
		this.useArray = useArray;
	}
	public List<Double> getArrayResult(ResultSet rs, int index) throws SQLException{
		List<Double> ret = new ArrayList<Double>();
		Number[] array = (Number[])rs.getArray(index).getArray();
		if(array != null){
			for(int i = 0; i < array.length; i++){
				ret.add(array[i].doubleValue());
			}
		}
		return ret;
	}

	@Override
	public void dropTemp(Statement st, String tempTableName, String copyTableName) {

	}
	
}
