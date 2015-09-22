package com.alpine.datamining.api.utility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
public class GpN2TUtil extends AbstractN2TUtil {
    private static final Logger itsLogger=Logger.getLogger(GpN2TUtil.class);

    @Override
	public String getDistribution(DatabaseConnection databaseConnection,
			String tableName) throws OperatorException {
		String distributedColumn;
		String schemaName;
		String onlyTableName;
		if (tableName.contains(".")) {
			schemaName = tableName.split("\\.")[0];
			onlyTableName = tableName.split("\\.")[1];
		} else {
			schemaName = "pg_temp_%";
			onlyTableName = tableName;
		}
		StringBuilder sb_distribution = new StringBuilder(
				"select alpine_miner_getdistribution (");
		sb_distribution.append("'").append(onlyTableName).append("','").append(
				schemaName).append("')");

		StringBuilder distributionColumn = new StringBuilder("");
		try {
			Statement st = databaseConnection.createStatement(false);
			itsLogger.debug(
					"IntegerToText.getDistributedColumn():sql="
							+ sb_distribution);
			ResultSet rs = st.executeQuery(sb_distribution.toString());
			rs.next();
			if (rs.getArray(1) != null) {
				Integer[] b = (Integer[]) rs.getArray(1).getArray();
				sb_distribution.setLength(0);
				sb_distribution.append("select * from ").append(tableName);
				itsLogger.debug(
						"IntegerToText.getDistributedColumn():sql="
								+ sb_distribution);
				rs = st.executeQuery(sb_distribution.toString());
				for (int i = 0; i < b.length; i++) {
					distributionColumn.append("\"").append(
							rs.getMetaData().getColumnName(b[i])).append("\"")
							.append(",");
				}
				distributionColumn = distributionColumn
						.deleteCharAt(distributionColumn.length() - 1);

				distributedColumn = " by (" + distributionColumn + ")";
			} else {
				distributedColumn = " randomly ";
			}

		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return distributedColumn;
	}

	@Override
	public void alterColumnType(Statement st,String columnName,String tableName) throws OperatorException, AnalysisError {
		StringBuilder sb_alter=new StringBuilder();
		sb_alter.append("alter table ").append(tableName).append(" alter column ");
		sb_alter.append(columnName).append(" TYPE text;");
		try {
			st.execute(sb_alter.toString());
		} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
		}
	}

}
