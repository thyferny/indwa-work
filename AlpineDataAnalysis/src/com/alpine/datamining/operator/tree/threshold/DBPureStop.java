
package com.alpine.datamining.operator.tree.threshold;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class DBPureStop implements Stop {
    private static Logger itsLogger = Logger.getLogger(DBPureStop.class);

    public DBPureStop() {}
    
    public boolean shouldStop(DataSet dataSet, int depth) throws OperatorException {
    	long labelCount = 0; 
        
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelColumnName  = dataSet.getColumns().getLabel().getName();//.getMapping().mapIndex((int)label);//label.getName();
        StringBuffer sql = new StringBuffer();
        sql.append("select max(alpine_count) from (select ").append(StringHandler.doubleQ(labelColumnName)).append(", count(*) alpine_count from (").append(selectSQL).append(") fooo group by ").append(StringHandler.doubleQ(labelColumnName)).append(") foo ");// order by count desc limit 1
        Statement st = null;
        ResultSet rs = null;
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("SingleLabelTerminationDB.shouldStop():sql="+sql);
			rs = st.executeQuery(sql.toString());
			rs.next();
			labelCount = rs.getLong(1);
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
        
        return dataSet.size() == labelCount;
    }    
}
