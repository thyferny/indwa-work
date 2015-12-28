
package com.alpine.datamining.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;



public class DBDataSet extends AbstractDataSet {


	
	private static final long serialVersionUID = -6688629748189863796L;
    private static final Logger itsLogger = Logger.getLogger(DBDataSet.class);

    
	private Table table;

	
	private Columns columns = new ColumnsImp();
	
	private HashMap<String,Double> average;
	private HashMap<String,Double> variance;
	private HashMap<String,Double> minimum;
	private HashMap<String,Double> maximum;
	private HashMap<String,Double> sum;
	
	public DBDataSet(Table table) {
		this(table, null, null);
	}

	
	public DBDataSet(Table table, List<Column> regularColumns) {
		this(table, regularColumns, null);
	}
	
	
	public DBDataSet(Table table, Map<Column, String> specialColumns) {
		this(table, null, specialColumns);
	}
	
	
	public DBDataSet(Table table, List<Column> regularColumns, Map<Column, String> specialColumns) {
		this.table = table;
		List<Column> regularList = regularColumns;
		if (regularList == null) {
			regularList = new LinkedList<Column>();
			for (int a = 0; a < table.getNumberOfColumns(); a++) {
				Column column = table.getColumn(a);
				if (column != null)
					regularList.add(column);	
			}
		}
		
		for (Column column : regularList) {
			if ((specialColumns == null) || (specialColumns.get(column) == null))
				getColumns().add((column));
		}
		
		if (specialColumns != null) {
			Iterator<Map.Entry<Column, String>> s = specialColumns.entrySet().iterator();
			while (s.hasNext()) {
				Map.Entry<Column, String> entry = s.next();
				getColumns().setSpecialColumn(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public DBDataSet(DBDataSet dataSet) {
		this.table = dataSet.table;
		this.columns = (Columns)dataSet.getColumns().clone();
	}
	
	
	public DBDataSet(DataSet dataSet) {
		this.table = dataSet.getDBTable();
		this.columns = (Columns)dataSet.getColumns().clone();
	}


	public Columns getColumns() {
		return columns;
	}
	

	public Table getDBTable() {
		return table;
	}
	
	public long size() {
		return table.size();
	}
 
	public void computeColumnStatistics(List<Column> columnList) throws WrongUsedException{
		if (columnList.size() == 0) {
			return;
		}

		Statement st = null;
		DatabaseConnection databaseConnection = ((DBTable) getDBTable())
				.getDatabaseConnection();

		String tableName = ((DBTable) getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		String dataSourceType = databaseConnection.getProperties().getName();
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dataSourceType);
		for (Column column : columnList) {
			if (column.isNominal() == false && !column.isCategory())
				continue;
			String columnName = StringHandler.doubleQ(column.getName());

			//category id don't caculate distinct value
			if(columns.getId()!=null&&columns.getId().getName().equals(column.getName()))continue;

			String sql = "select distinct " + columnName + " from " + tableName
					+ " order by " + columnName + " desc";

			try {
				long count = multiDBUtility.getSampleDistinctCount(st, tableName,columnName,null);
				if(count>Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT)))
				{
					throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED,columnName ,Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT)));
				}
				itsLogger.debug("DBDataSet.recalculatecolumnStatistics():sql="+sql);
				ResultSet rs = st.executeQuery(sql);

				while (rs.next()) {
					String value = rs.getString(1);
		        	if (column.isNumerical() && value != null){
		        		value = value.split("\\.")[0];
		        	}
					column.getMapping().mapString(value);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new RuntimeException(e);
			}

		}
	}
	
	public void computeColumnStatistics(Column column) throws WrongUsedException {
		Statement st = null;
		DatabaseConnection databaseConnection = ((DBTable) getDBTable())
				.getDatabaseConnection();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		String tableName = ((DBTable) getDBTable()).getTableName();
		String columnName = StringHandler.doubleQ(column.getName());
		String dataSourceType = databaseConnection.getProperties().getName();
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dataSourceType);
		
		//category id don't caculate distinct value
		if(columns.getId()!=null&&columns.getId().getName().equals(column.getName()))return;
		String sql = "select distinct " + columnName + " from " + tableName
		+ " order by " + columnName + " desc";
		
		try {
			long count = multiDBUtility.getSampleDistinctCount(st, tableName,columnName,null);
			if(count>Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT)))
			{
				throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED,columnName ,Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT)));
			}
			itsLogger.debug("DBDataSet.recalculatecolumnStatistics():sql="+sql);
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String value = rs.getString(1);
				column.getMapping().mapString(value);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
	}
    public void computeStatistics(List<Column> columnList)
    {	if (columnList.size() == 0) {
		return;
	}

	Statement st = null;
	DatabaseConnection databaseConnection = ((DBTable) getDBTable())
			.getDatabaseConnection();

	String tableName = ((DBTable) getDBTable()).getTableName();
	try {
		st = databaseConnection.createStatement(false);
	} catch (SQLException e) {
		itsLogger.error(e.getMessage(),e);
		throw new RuntimeException(e);
	}
		average = new HashMap<String, Double>();
		variance = new HashMap<String, Double>();
		minimum = new HashMap<String, Double>();
		maximum = new HashMap<String, Double>();
		sum = new HashMap<String, Double>();
		ISqlGeneratorMultiDB sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable)
				getDBTable()).getDatabaseConnection().getProperties().getName());

		for (Column column : columnList)

		{
			if (column.isNumerical() == false)
				continue;
			StringBuilder sql = new StringBuilder("select ");
			String columnName = column.getName();
			String newColumnName = sqlGeneratorMultiDB.castToDouble((StringHandler.doubleQ(columnName)));
			
			sql.append("avg(").append(newColumnName).append("),(sum(").append(newColumnName)
					.append("*");
			sql.append(newColumnName).append(")-sum(").append(newColumnName).append(")*sum(")
					.append(newColumnName);
			sql.append(")/ count(*))/(count(*)-1),").append("min(").append(newColumnName).append(
					"),max(").append(newColumnName).append("),sum(").append(newColumnName).append(
					") from ").append(tableName);
			ResultSet rs = null;
			try {
				itsLogger.debug("DBDataSet.calculateStatistics():sql="+sql);
				rs = st.executeQuery(sql.toString());
				while (rs.next()) {
					average.put(columnName, rs.getDouble(1));
					variance.put(columnName, rs.getDouble(2));
					minimum.put(columnName, rs.getDouble(3));
					maximum.put(columnName, rs.getDouble(4));
					sum.put(columnName, rs.getDouble(5));
				}

			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new RuntimeException(e.getLocalizedMessage());
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						throw new RuntimeException(e);
					}
				}
			}

		}
    }
    public double getStatistics(Column column, String statisticsName) {
    	String columnName=column.getName();
    	if(statisticsName.equals(ColumnStats.AVERAGE))
    	{
    		return average.get(columnName);
    	}
    	else if(statisticsName.equals(ColumnStats.VARIANCE))
    	{
        return variance.get(columnName);
    	}
    	else if (statisticsName.equals(ColumnStats.MINIMUM))
    	{
    		return minimum.get(columnName);
    	}
    	else if(statisticsName.equals(ColumnStats.MAXIMUM))
    	{
    		return maximum.get(columnName);
    	}
    	else if(statisticsName.equals(ColumnStats.SUM))
    	{
    		return sum.get(columnName);
    	}
    	else {
    		return 0.0;
    	}
    }

	@Override
	public Data getRow(int i) {
		return null;
	}


	@Override
	public Iterator<Data> iterator() {
		return null;
	}

}
