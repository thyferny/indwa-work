package com.alpine.datamining.operator.kmeans;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

public class KmeansDBOracle implements IKmeansDB {
    private static Logger itsLogger= Logger.getLogger(KmeansDBOracle.class);
    private boolean useArray = false;
	@Override
	public String generateFunction(DataSet dataSet, boolean maxColumnFlag,
			int lineThrethhold, int k, int maxOptimizationSteps, int maxRuns,
			String newTableName, String temptablename, String id,
			String tempid, String clusterColumnName, String columnNameList, String columnArrayList, int columnsSize, int distance) {
		StringBuilder sql=new StringBuilder("");
		if(useArray){
			String[] columnArrays=columnNameList.split(",");
			ArrayList<String> columnList=new ArrayList<String>();
			for(int i=0;i<columnArrays.length;i++){
				columnList.add(columnArrays[i]);
			}
			sql.append("select alpine_miner_kmeans_c_1_5_2('").append(newTableName).append("','").append(temptablename).append("', ");
			sql.append(CommonUtility.array2OracleArray(columnList, CommonUtility.OracleDataType.Varchar2)).append(", ").append(columnArrayList).append(",").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(") from dual");
		}else{
			sql.append("select alpine_miner_kmeans_c_1_5('").append(newTableName).append("','").append(temptablename).append("', varchar2array(");
			sql.append(columnNameList).append("), ").append(columnsSize).append(", '").append(id).append("', ");
			sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
			sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
			sql.append(distance).append(") from dual");
		}
		return sql.toString();
	}

	@Override
	public String dropTableIfExists(String schemaName, String tableName) {
		StringBuilder sb=new StringBuilder();
		sb.append("call proc_droptableifexists('").append(tableName).append("')");
		return sb.toString();
	}

	@Override
	public StringBuilder generateCreateCopyTableSql(String tableName,
			String tempid, String copyTableName) {
		StringBuilder sb_createCopy=new StringBuilder("create table ");
		sb_createCopy.append(copyTableName);
		sb_createCopy.append(" parallel as select ").append(tableName).append(".*,row_number() over (order by 1) ").append(tempid)
		.append(" from ").append(tableName);
		return sb_createCopy;
	}

	@Override
	public void dealResult(DataSet dataSet, int columncount,
			boolean maxColumnFlag, ResultSet rs, int i_i,
			ArrayList<String> centerPointForEachCluster, int j,
			int lineThrethhold) throws SQLException {

		if(useArray){
			Object[] arrayarray = (Object[])rs.getArray(3+i_i).getArray();
			if(arrayarray==null) return;
			for(int m = 0; m < arrayarray.length; m++){
				ResultSet array = ((Array)arrayarray[m]).getResultSet();
				ArrayList<Double> arrayDouble = new ArrayList<Double>();
				while(array.next()){
					arrayDouble.add(array.getInt(1)-1, array.getDouble(2));
				}
//				BigDecimal[] arrayDecimal = (BigDecimal[])array;
				centerPointForEachCluster.add(String.valueOf(arrayDouble.get(j)));
			}
		}else{
			centerPointForEachCluster.add(String.valueOf(rs.getFloat(3
			+ i_i * columncount + j)));// 3+columncount
		}

//		centerPointForEachCluster.add(String.valueOf(((BigDecimal[])rs.getArray(3+i_i).getArray())[j]));
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
		ResultSet resultSet = rs.getArray(index).getResultSet();
		while(resultSet != null && resultSet.next()){
			ret.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
		}
		return ret;
	}

	@Override
	public void dropTemp(Statement st, String tempTableName, String copyTableName) {
		try {
			StringBuilder sb=new StringBuilder();
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("_RANDOM_NEW')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("COPY')");
			st.executeUpdate(sb.toString());
			if(!StringUtil.isEmpty(copyTableName)){
				sb.setLength(0);
				sb.append("call proc_droptemptableifexists('");
				sb.append(copyTableName).append("')");;
				st.executeUpdate(sb.toString());
			}
			sb.setLength(0);
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("RESULT1')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("RESULT2')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("TABLE_NAME_TEMP')");
			st.executeUpdate(sb.toString());
			sb.setLength(0);
			sb.append("call proc_droptemptableifexists('");
			sb.append(tempTableName).append("RESULT3')");
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
