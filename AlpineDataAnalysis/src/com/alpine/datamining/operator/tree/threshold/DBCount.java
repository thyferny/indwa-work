
package com.alpine.datamining.operator.tree.threshold;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class DBCount implements ICount{
    private static Logger itsLogger= Logger.getLogger(DBCount.class);


    static long startTime;
	static long endTime;
	
    public DBCount() {}
    
    public double[][] getNumericalCounts(DataSet dataSet, Column column, double splitValue) throws OperatorException {

    	Column label = dataSet.getColumns().getLabel();
    	int numberOfLabels = label.getMapping().size();
    	double[][] weightCounts = new double[2][numberOfLabels];
    	
    	DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelColumnName  = dataSet.getColumns().getLabel().getName();//.getMapping().mapIndex((int)label);//label.getName();
        String columnName = column.getName();
        String sqlLessEqual = "select \""+labelColumnName+"\", count(*) from ("+selectSQL+") as foo where \""+columnName+"\" <= "+splitValue+"  group by \""+labelColumnName+"\" order by count desc";
        String sqlGreater = "select \""+labelColumnName+"\", count(*) from ("+selectSQL+") as foo where \""+columnName+"\" > "+splitValue+"  group by \""+labelColumnName+"\" order by count desc";

        Statement st = null;
        ResultSet rs = null;
        String labelName = null;
        int labelIndex = 0;
        long count = 0;
       try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("FrequencyCalculatorDB.getNumericalWeightCounts():sql="+sqlLessEqual);
			rs = st.executeQuery(sqlLessEqual);
			while (rs.next())
			{
				labelName = rs.getString(1);
				labelIndex = dataSet.getColumns().getLabel().getMapping().getIndex(labelName);
				count = rs.getLong(2);
				weightCounts[0][labelIndex] = count;
			}
			rs.close();

			itsLogger.debug("FrequencyCalculatorDB.getNumericalWeightCounts():sql="+sqlGreater);
			rs = st.executeQuery(sqlGreater);
			while (rs.next())
			{
				labelName = rs.getString(1);
				labelIndex = dataSet.getColumns().getLabel().getMapping().getIndex(labelName);
				count = rs.getLong(2);
				weightCounts[1][labelIndex] = count;
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
    	return weightCounts;
    }
    
    public double[][] getNominalCounts(DataSet dataSet, Column column) throws OperatorException {
    	Column label = dataSet.getColumns().getLabel();
    	int numberOfLabels = label.getMapping().size();
    	int numberOfValues = column.getMapping().size();
    	
    	double[][] weightCounts = new double[numberOfValues][numberOfLabels];
   	
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelColumneName  = StringHandler.doubleQ(dataSet.getColumns().getLabel().getName());//.getMapping().mapIndex((int)label);//label.getName();
        String columnName = StringHandler.doubleQ(column.getName());
        String sql = "select "+labelColumneName+","+columnName+", count(*) from ("+selectSQL+") foo group by "+labelColumneName+","+columnName+"  having "+columnName+" is not null";//order by count desc 
        Statement st = null;
        ResultSet rs = null;
        String labelName = null;
        String columnValue = null;
        int labelIndex = 0;
        int valueIndex = 0;
        long count = 0;
       try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("FrequencyCalculatorDB.getNominalWeightCounts():sql="+sql);
			rs = st.executeQuery(sql);
			while (rs.next())
			{
				labelName = rs.getString(1);
				columnValue = rs.getString(2);
				count = rs.getLong(3);
				labelIndex = dataSet.getColumns().getLabel().getMapping().getIndex(labelName);
				valueIndex = column.getMapping().getIndex(columnValue);
				weightCounts[valueIndex][labelIndex] = count;
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
   	
    	return weightCounts;
    }
    
    
    public double getTotalCount(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }

	public double[] getLabelCounts(DataSet dataSet) {
		return null;
	}

	@Override
	public double[] getPartitionCount(SplitDataSet splitted) {
		return null;
	}
}
