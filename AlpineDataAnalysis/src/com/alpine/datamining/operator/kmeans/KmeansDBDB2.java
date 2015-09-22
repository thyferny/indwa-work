package com.alpine.datamining.operator.kmeans;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class KmeansDBDB2 implements IKmeansDB {
    private static Logger itsLogger= Logger.getLogger(KmeansDBDB2.class);
	@Override
	public void dealResult(DataSet dataSet, int columncount,
			boolean maxColumnFlag, ResultSet rs, int i_i,
			ArrayList<String> centerPointForEachCluster, int j,
			int lineThrethhold) throws SQLException {
		centerPointForEachCluster.add(String.valueOf(rs.getFloat(3
				+ i_i * columncount + j)));
	}

	@Override
	public String dropTableIfExists(String schemaName, String tableName) {
		StringBuilder sb=new StringBuilder();
		sb.append("call PROC_DROPSCHTABLEIFEXISTS('").append(StringHandler.doubleQ(schemaName));
		sb.append("','").append(StringHandler.doubleQ(tableName)).append("')");
		return sb.toString();
	}

	@Override
	public StringBuilder generateCreateCopyTableSql(String tableName,
			String tempid, String copyTableName) {
		StringBuilder sb_createCopy=new StringBuilder("");
//		sb_createCopy.append(copyTableName);
		sb_createCopy.append(" select ").append(tableName).append(".*,row_number() over () as ").append(tempid)
		.append(" from ").append(tableName);
		return sb_createCopy;
	}

	@Override
	public String generateFunction(DataSet dataSet, boolean maxColumnFlag,
			int lineThrethhold, int k, int maxOptimizationSteps, int maxRuns,
			String newTableName, String temptablename, String id,
			String tempid, String clusterColumnName, String columnNameList,
			String columnArrayList, int columnsSize, int distance) {
		StringBuilder sql=new StringBuilder("");
		sql.append("call alpine_miner_kmeans('").append(newTableName).append("','").append(temptablename).append("',");
		sql.append(columnsSize).append(", '").append(id).append("', ");
		sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
		sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
		sql.append(distance).append("").append(",?,?)");
		
		Object[] outputResult = new Object[2];
		
		try {
			if(!StringUtil.isEmpty(columnArrayList)){
				String columnsArray[]=columnArrayList.split(",");
				Array sqlArray = ((DBTable) dataSet.
						getDBTable()).getDatabaseConnection().getConnection().createArrayOf(
						"VARCHAR", columnsArray);
				CallableStatement stpCall = ((DBTable) dataSet.
						getDBTable()).getDatabaseConnection().getConnection().prepareCall(sql.toString());
				stpCall.setArray(1, sqlArray);
				stpCall.registerOutParameter(2, java.sql.Types.ARRAY);
				stpCall.execute();
				outputResult = (Object[]) stpCall.getArray(2).getArray();
				stpCall.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sql.toString();
	}

	@Override
	public boolean isUseArray() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUseArray(boolean useArray) {
		// TODO Auto-generated method stub

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
		try {
			StringBuilder sb=new StringBuilder();
			sb.append("call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ('");
			sb.append(tempTableName).append("_RANDOM_NEW')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ('");
			sb.append(tempTableName).append("COPY')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ('");
			sb.append(tempTableName).append("RESULT1')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ('");
			sb.append(tempTableName).append("RESULT2')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ('");
			sb.append(tempTableName).append("TABLE_NAME_TEMP')");
			st.executeUpdate(sb.toString());
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
		}finally{
			try {
				st.close();
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
			}
		}
		
	}

	
	
}
