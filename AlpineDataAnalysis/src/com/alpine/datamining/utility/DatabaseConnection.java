/**
 * ClassName DatabaseConnection
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.ColumnFactory;
import com.alpine.utility.db.DataSourceInfoNZ;
import org.apache.log4j.Logger;

/**
 * @author Eason
 */
public class DatabaseConnection {
	
	private static final String NUMERIC = "NUMERIC";
	private static final Logger itsLogger = Logger.getLogger(DatabaseConnection.class);
	private String databaseURL;

    private JDBCProperties properties;
    
	private Connection connection;
    

	public DatabaseConnection(String databaseURL, JDBCProperties properties) {
		this.databaseURL = databaseURL;
        this.properties = properties;
		connection = null;
	}
	
	public static DatabaseConnection createDatabaseConnection(Connection connection,String databaseURL,    JDBCProperties properties/*, LoggingHandler logging*/)  {
		DatabaseConnection databaseConnection = new DatabaseConnection(databaseURL, properties);
		databaseConnection.connection=connection;
		return databaseConnection;
	}
	

	public JDBCProperties getProperties() {
		return this.properties;
	}
	

	public Connection getConnection() {
		return connection;
	}
	

	public Statement createStatement(boolean scrollableAndUpdatable) throws SQLException {
		if (connection == null) {
			throw new SQLException("Not connected: could not create a statement for '" + databaseURL + "'.");
		}
		Statement statement = null;
		if (scrollableAndUpdatable)
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		else
			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		return statement;
	}


	public PreparedStatement createPreparedStatement(String sqlString, boolean scrollableAndUpdatable) throws SQLException {
		if (connection == null) {
			throw new SQLException("Could not create a prepared statement for '" + databaseURL + "': not connected.");
		}
		PreparedStatement statement = null;
		if (scrollableAndUpdatable)
			statement = connection.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		else
			statement = connection.prepareStatement(sqlString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		return statement;
	}
	
    public void commit() throws SQLException {
        if ((connection == null) || connection.isClosed()) {
            throw new SQLException("Could not commit: no open connection to database '" + databaseURL + "' !");
        }
        connection.commit();
    }
    
	public void addColumn(Column column, String tableName) throws SQLException {
		Statement statement = createStatement(false);
		boolean exists = false;
		try {
          
			String sql = "SELECT " + properties.getIdentifierQuoteOpen() + column.getName() + properties.getIdentifierQuoteClose() + " FROM " + tableName + " WHERE 0 = 1";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("databaseConnection.addColumn():sql="+sql);
			}
			ResultSet existingResultSet = statement.executeQuery(sql);
            if (existingResultSet.getMetaData().getColumnCount() > 0)
                exists = true;
			existingResultSet.close();
		} catch (SQLException e) {
			// exception will be thrown if the column does not exist
		}
		statement.close();
		
        if (exists) {
        	if(properties.getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
        	{
        		update(column, tableName) ;
        		return ;
        	}
        	removeColumn(column, tableName);
        }
        
        // create new column
		Statement st = null;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
 			if(properties.getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
        		String tempTable = "T"+System.currentTimeMillis();
        		String query = "create table "+tempTable+" as select * , null::"+ (column.isNominal() ? (properties.getVarcharName() + "(256)") : properties.getFloatName())+" "+properties.getIdentifierQuoteOpen() + column.getName() + properties.getIdentifierQuoteClose() + 
				" from "+tableName;
        		if(itsLogger.isDebugEnabled()){
        			itsLogger.debug("databaseConnection.addColumn():sql="+query);
        		}
        		st.execute(query);
        		query = "drop table "+tableName;
        		if(itsLogger.isDebugEnabled()){
        			itsLogger.debug("databaseConnection.addColumn():sql="+query);
        		}
        		st.execute(query);
        		query = "alter table "+tempTable+" rename to "+tableName;
        		if(itsLogger.isDebugEnabled()){
        			itsLogger.debug("databaseConnection.addColumn():sql="+query);
        		}
        		st.execute(query);
        	}else{
        		String query = 
				"ALTER TABLE " + 
				tableName + 
//				" ADD COLUMN " + 
				" ADD " + 
				properties.getIdentifierQuoteOpen() + column.getName() + properties.getIdentifierQuoteClose() + 
				" " + (column.isNominal() ? (properties.getVarcharName() + "(256)") : properties.getFloatName());
        		if(itsLogger.isDebugEnabled()){
        			itsLogger.debug("databaseConnection.addColumn():sql="+query);
        		}
        		st.execute(query);
        	}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

	/**
	 * @param column
	 * @param tableName
	 * @throws SQLException 
	 */
	private void update(Column column, String tableName) throws SQLException {  
		Statement st = null;
    try {
    	st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    	String query = 
    		"update  " + 
    		tableName + 
    		" set " + 
    		properties.getIdentifierQuoteOpen() + column.getName() + properties.getIdentifierQuoteClose()+"= null" ;
    	
		itsLogger.debug("databaseConnection.removeColumn():sql="+query);
    	st.execute(query);
    } catch (SQLException e) {
    	throw e;
    } finally {
    	if (st != null)
            st.close();
    }
	}

	public void removeColumn(Column column, String tableName) throws SQLException {
        Statement st = null;
        try {
        	st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        	String query = 
        		"ALTER TABLE " + 
        		tableName + 
        		" DROP COLUMN " + 
        		properties.getIdentifierQuoteOpen() + column.getName() + properties.getIdentifierQuoteClose() ;
        	if(properties.getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
        	{
        		query= query +" RESTRICT ";
        	}
			itsLogger.debug("databaseConnection.removeColumn():sql="+query);
        	st.execute(query);
        } catch (SQLException e) {
        	throw e;
        } finally {
        	if (st != null)
                st.close();
        }
	}
 
    
	/**
	 * Returns for the given SQL-type the name of the corresponding Type
	 * from  Ontology.
	 */
	public static int getTypeIndex(int sqlType,String sqlTypeName,int decimal) {
		if(sqlTypeName.equalsIgnoreCase("money")){
			return DataType.OTHER;
		}
		switch (sqlType) {
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
				return DataType.INTEGER;

			case Types.DECIMAL:
				if(decimal==0){
					return DataType.INTEGER;
				}else{
					return DataType.REAL;
				}	
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
				return DataType.REAL;

			case Types.NUMERIC:
				if(sqlTypeName.equalsIgnoreCase(NUMERIC)){
					return DataType.NUMERICAL;
				}else if(decimal==0){
					return DataType.INTEGER;
				}else{
					return DataType.NUMERICAL;
				}				
			case Types.BLOB:
			case Types.CLOB:
				return DataType.STRING;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.BINARY:
//			case Types.BIT:
			case Types.LONGVARBINARY:
			case Types.JAVA_OBJECT:
			case Types.STRUCT:
			case Types.VARBINARY:
			case Types.LONGVARCHAR:
				return DataType.NOMINAL;

			case Types.DATE:
				return DataType.DATE;
			case Types.TIME:
				return DataType.TIME;
			case Types.TIMESTAMP:
				return DataType.DATE_TIME;

			case Types.BIT:
			case Types.BOOLEAN:
				return DataType.BOOLEAN;
			case Types.OTHER:
				return DataType.OTHER;
			default:
				return DataType.NOMINAL;
		}
	}

	/**
	 * Creates a list of columns reflecting the result set's column meta
	 * data.
	 */
	public static List<Column> createColumns(ResultSet rs) throws SQLException {
		List<Column> columns = new LinkedList<Column>();

		if (rs == null) {
			throw new IllegalArgumentException("Cannot create columns: ResultSet must not be null!");
		}

		ResultSetMetaData metadata;
		try {
			metadata = rs.getMetaData();
		} catch (NullPointerException npe) {
			throw new RuntimeException("Could not create column list: ResultSet object seems closed.");
		}

		int numberOfColumns = metadata.getColumnCount();

		for (int i = 1; i <= numberOfColumns; i++) {
			String name = metadata.getColumnLabel(i);
			Column column = ColumnFactory.createColumn(name, getTypeIndex(metadata.getColumnType(i),metadata.getColumnTypeName(i),
					metadata.getScale(i)));
			
			columns.add(column);
		}

		return columns;
	}
 
}

