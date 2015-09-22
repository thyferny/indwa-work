/**
 * ClassName DBBuildLeaf
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * This class can be used to change
 *  an inner tree node into a leaf . This class is for database
 * 
 */
public class DBBuildLeaf
	implements IBuildLeaf
{
    private static Logger itsLogger= Logger.getLogger(DBBuildLeaf.class);

    public void changeToLeaf(Tree node, DataSet dataSet) throws OperatorException {
        Column label = dataSet.getColumns().getLabel();
       String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelColumnName  = StringHandler.doubleQ(label.getName());
        String sql = "select "+labelColumnName+", count(*) from ("+selectSQL+") foo group by "+labelColumnName+" having "+labelColumnName+" is not null order by 2 desc, 1 desc";
        Statement st = null;
        ResultSet rs = null;
        String labelName = null;
        Map<String, Long> labelCountMap = new HashMap <String, Long>();//labelValue = 0;
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("DecisionTreeLeafCreatorDB.changeTreeToLeaf():sql="+sql);
			rs = st.executeQuery(sql);
			int i = 0; 
			while (rs.next())
			{
				String tempLabelName = rs.getString(1);
				long count = rs.getLong(2);
				if (i == 0)
				{
					labelName = tempLabelName;
				}
				labelCountMap.put(tempLabelName, new Long(count));
				i++;
			}
			st.close();
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		
         node.setLabel(labelName);
        for (String value : label.getMapping().getValues()) {
        	int count = 0;
        	Long countInteger = labelCountMap.get(value);  
        	if (countInteger!= null)
        	{
        		count = countInteger.intValue();
        	}
        	else
        	{
        		count = 0;
        	}
            node.addCount(value, count);
        }
    }
}
