package com.alpine.datamining.api.utility;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class OracleN2TUtil extends AbstractN2TUtil {
    private static final Logger itsLogger=Logger.getLogger(OracleN2TUtil.class);

    @Override
	public String getDistribution(DatabaseConnection databaseConnection,
			String tableName) throws OperatorException {
		//nothing for Oracle distribution
		return null;
	}

	@Override
	public void alterColumnType(Statement st,String columnName, String tableName) throws OperatorException {
		ArrayList<String> sqlList=new ArrayList<String>();
		String charType="varchar2(4000)";
		String tempColumnName=StringHandler.doubleQ(columnName.substring(1, columnName.length()-1)+System.currentTimeMillis());
		StringBuilder sb=new StringBuilder("");
		sb.append("alter table ").append(tableName).append(" add ").append(tempColumnName).append(" ").append(charType);
		sqlList.add(sb.toString());
		sb.setLength(0);
		sb.append("update ").append(tableName).append(" set ").append(tempColumnName).append("=").append(columnName);
			sb.append(",").append(columnName).append("=null");
		sqlList.add(sb.toString());
		sb.setLength(0);
		sb.append("alter table ").append(tableName).append(" modify ").append(columnName).append(" ").append(charType);
		sqlList.add(sb.toString());
		sb.setLength(0);
		sb.append("update ").append(tableName).append(" set ").append(columnName).append("=").append(tempColumnName);
			sb.append(",").append(tempColumnName).append("=null");
		sqlList.add(sb.toString());
		sb.setLength(0);
		sb.append("alter table ").append(tableName).append(" drop column ").append(tempColumnName);
		sqlList.add(sb.toString());
//		sb.setLength(0);
//		sb.append("alter table ").append(tableName).append(" modify ").append(columnName).append(" not null");
//		sqlList.add(sb.toString());
		Iterator<String> i_sql=sqlList.iterator();
		try {
			while(i_sql.hasNext())
			{
				String sql_alter=i_sql.next();
	
					st.addBatch(sql_alter);
				
				itsLogger.info("IntegerToText.apply():sql="+sql_alter);
			}
			st.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

	}

}
