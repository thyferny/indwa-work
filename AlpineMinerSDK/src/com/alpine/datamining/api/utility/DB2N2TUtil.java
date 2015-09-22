package com.alpine.datamining.api.utility;

import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
public class DB2N2TUtil extends AbstractN2TUtil {
    private static final Logger itsLogger = Logger.getLogger(DB2N2TUtil.class);

    @Override
	public void alterColumnType(Statement st, String columnName,
			String tableName) throws OperatorException, AnalysisError {
		StringBuilder sb_alter=new StringBuilder();
		sb_alter.append("alter table ").append(tableName).append(" alter column ");
		sb_alter.append(columnName).append(" set DATA TYPE varchar(100)");
		try {
			st.execute(sb_alter.toString());
			StringBuilder sb_reg=new StringBuilder();
			sb_reg.append("call SYSPROC.ADMIN_CMD('reorg table ").append(tableName).append("')");
			st.execute(sb_reg.toString());
		} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
		}
	}

	@Override
	public String getDistribution(DatabaseConnection databaseConnection,
			String tableName) throws OperatorException {
		// TODO Auto-generated method stub
		return null;
	}

}
