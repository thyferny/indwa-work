/**
 * ClassName DBBuildLeaf
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartregression;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.tree.threshold.IBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.utility.DBPortingFactory;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * This class can be used to change an inner tree node into a leaf . This class is for database
 * 
 */
public class DBBuildLeaf
	implements IBuildLeaf
{
    private static Logger itsLogger= Logger.getLogger(DBBuildLeaf.class);

    public void changeToLeaf(Tree node, DataSet dataSet) throws OperatorException {
    	INumericalSql numericalSql = DBPortingFactory.createCartRegressionDB(((DBTable) dataSet
				.getDBTable()).getDatabaseConnection().getProperties().getName());

        Column label = dataSet.getColumns().getLabel();
       String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelColumnName  = StringHandler.doubleQ(label.getName());
        String sql = numericalSql.getChangeToLeafSql(selectSQL,
				labelColumnName);
        Statement st = null;
        ResultSet rs = null;
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("DecisionTreeLeafCreatorDB.changeTreeToLeaf():sql="+sql);
			rs = st.executeQuery(sql);
			while (rs.next())
			{
				double avg = rs.getDouble(1);
				double deviance = rs.getDouble(2);
				long count = rs.getLong(3);
				((RegressionTree)node).setAvg(avg);
				((RegressionTree)node).setDeviance(deviance);
				((RegressionTree)node).setCount(count);
			}
			st.close();
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
    }
}
