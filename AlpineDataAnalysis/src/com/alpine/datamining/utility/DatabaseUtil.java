/**
 * ClassName DatabaseUtil
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.Resources;
import org.apache.log4j.Logger;

/**
 * @author Eason
 */
public class DatabaseUtil {
    private static Logger itsLogger= Logger.getLogger(DatabaseUtil.class);

    /**
	 * 
	 */
	
	private static List<JDBCProperties> jdbcProperties = new ArrayList<JDBCProperties>();
    
	public static void init() {
		jdbcProperties = JDBCProperties.getJDBCProperties();
	}

	public static JDBCProperties getJDBCProperties(String name) {
		for (JDBCProperties p : jdbcProperties) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static JDBCProperties getJProperties(String systemName) {
		JDBCProperties result = null;
		for (JDBCProperties properties : jdbcProperties) {
			if (properties.getName().equalsIgnoreCase(systemName)) {
				result = properties;
				break;
			}
		}
		return result;
	}
	
    public static List<JDBCProperties> getJDBCProperties() {
        return jdbcProperties;
    }
    
    public static String[] getDBSystemNames() {
        String[] names = new String[jdbcProperties.size()];
        int counter = 0;
        Iterator<JDBCProperties> i = jdbcProperties.iterator();
        while (i.hasNext()) {
            names[counter++] = i.next().getName();
        }
        return names;
    }
    public static int getDBSystemIndex(String system){
    	String[] systems =  getDBSystemNames();
    	for ( int i = 0; i < systems.length; i++){
    		if(systems[i].equalsIgnoreCase(system)){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public static String addParallel(String dbType){
		return addParallel(dbType,"TABLE");
	}
    
    
	public static String addParallel(DatabaseConnection databaseConnection,
			String type){
		String dbType=databaseConnection.getProperties().getName();
		return addParallel(dbType,type);
	}
	public static String addParallel(String dbType,
			String type){
		if(dbType!=null&&dbType.equals(DataSourceInfoOracle.dBType)
				&&type!=null&&type.equalsIgnoreCase(Resources.OutputTypes[0])){//only table
			return " parallel ";
		}else{
			return " ";
		}
	}
	public static void alterParallel(Connection conn, String dbType) throws OperatorException {
		alterParallel(conn,dbType,"TABLE");
	}
	public static void alterParallel(Connection conn, String dbType,
			String type) throws OperatorException {
		if(dbType!=null&&dbType.equals(DataSourceInfoOracle.dBType)
				&&type!=null&&type.equalsIgnoreCase(Resources.OutputTypes[0])){//only table
			String parallelSql="alter session force parallel dml";
			Statement st = null;
			try {
				st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				itsLogger.debug("DatabaseUtil.alterParallel():sql="+parallelSql);
				st.executeUpdate(parallelSql);
				st.close();
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			} finally {
				try {
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}	
		}
	}
	public static void alterParallel(DatabaseConnection databaseConnection) throws OperatorException {
		alterParallel(databaseConnection,"TABLE");
	}
	public static void alterParallel(DatabaseConnection databaseConnection, 
			String type) throws OperatorException {
		String dbType=databaseConnection.getProperties().getName();
		if(dbType!=null&&dbType.equals(DataSourceInfoOracle.dBType)
				&&type!=null&&type.equalsIgnoreCase(Resources.OutputTypes[0])){//only table
			String parallelSql="alter session force parallel dml";
			Statement st = null;
			try {
				st = databaseConnection.createStatement(false);
				itsLogger.debug("DatabaseUtil.alterParallel():sql="+parallelSql);
				st.executeUpdate(parallelSql);
				st.close();
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			} finally {
				try {
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}	
		}
	}
}
