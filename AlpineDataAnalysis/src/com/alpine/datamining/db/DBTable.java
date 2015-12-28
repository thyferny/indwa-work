
package com.alpine.datamining.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;


public class DBTable extends AbstractDataTable {
    private static Logger itsLogger= Logger.getLogger(DBTable.class);

    private static final long serialVersionUID = -7930777621508062375L;

	private transient DatabaseConnection databaseConnection;

	private String tableName;

	private String whereCondition = null;

	private long size = -1;//0;
	
    String url ;


	String userName;
    String password;

    
	public String getUrl() {
		return url;
	}

	
	public String getUserName() {
		return userName;
	}

	
	public String getPassword() {
		return password;
	}
	public DBTable(List<Column> columns, DatabaseConnection databaseConnection,String url, String userName, String password, String tableName, String whereCondition) throws SQLException {
		super(columns);
		this.databaseConnection = databaseConnection;
		this.tableName = tableName;
		this.url = url;
		this.userName = userName;
		this.password = password;
		this.whereCondition = whereCondition;
	}

	
	public static DBTable createDatabaseDataTableDB(DatabaseConnection databaseConnection,String url, String userName, String password, String tableName, String whereCondition) throws SQLException {

		
    	Statement statement = databaseConnection.createStatement(false);
    	StringBuilder selectSQL=new StringBuilder("select * from " );
    	if (whereCondition != null && whereCondition.length() != 0)
    	{

    		selectSQL.append(tableName).append(" where ").append(whereCondition);
    	}
    	else
    	{
    		selectSQL.append(tableName);
    	}
    	StringBuilder sb=new StringBuilder("SELECT * FROM ( ");
    	sb.append(selectSQL).append(")  foo WHERE 0 = 1");
    	itsLogger.debug("DBTable.createDatabasedataTableDB():sql="+sb);
        ResultSet rs = statement.executeQuery(sb.toString());
		List<Column> columns = DatabaseConnection.createColumns(rs);
		rs.close();
		statement.close();
		
		// create database data table
		DBTable table = new DBTable(columns, databaseConnection, url, userName, password, tableName, whereCondition);
		return table;
	}


	public int addColumn(Column column) {
		int index = super.addColumn(column);
		
        // will be invoked by super constructor, hence this check
        if (databaseConnection == null)
			return index;
        
		try {
			databaseConnection.addColumn(column, tableName);
//            resetResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while adding a column '" + column.getName() + "'to database: " + e, e);
		}
		return index;
	}

	public void removeColumn(Column column) {
		super.removeColumn(column);
		try {
			databaseConnection.removeColumn(column, tableName);
		} catch (SQLException e) {
			throw new RuntimeException("Error while removing a column '"+column.getName()+"' from database: " + e, e);
		}
	}

	public long size() {
		if (this.size < 0) {
			try {
				Statement countStatement = this.databaseConnection.createStatement(false);
				String countQuery = "SELECT count(*) FROM ( " + getSQL()+")  foo";
		    	itsLogger.debug("DBTable.size():sql="+countQuery);
				ResultSet countResultSet = countStatement.executeQuery(countQuery);
				countResultSet.next();
				this.size = countResultSet.getLong(1);
				countResultSet.close();
				countStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        return this.size;
	}
    
    public String getTableName()
    {
    	return tableName;
    }
    public void setTableName(String tablename)
    {
    	this.tableName=tablename;
    }
    public String getWhereCondition()
    {
    	return whereCondition;
    }
    public String getSQL()
    {
    	StringBuilder selectsql=new StringBuilder("select * from ");
    	if (whereCondition != null && whereCondition.length() != 0)
    	{
    		selectsql.append(tableName).append(" where ").append(whereCondition);
    	}
    	else
    	{
    		selectsql.append(tableName);
    	}
    	return selectsql.toString();
    }
    public DatabaseConnection getDatabaseConnection()
    {
    	return databaseConnection;
    }


	@Override
	public Row getDataRow(int index) {
		return null;
	}


	@Override
	public RowIterator getDataRowReader() {
		return null;
	}
}
