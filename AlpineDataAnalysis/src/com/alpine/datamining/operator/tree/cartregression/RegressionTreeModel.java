/**
 * ClassName RegressionTreeModel
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartregression;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.Side;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;

/**
 * The tree model is the model created by  cart regression trees.
 * 
 */
public class RegressionTreeModel extends SingleModel {
    private static Logger itsLogger= Logger.getLogger(RegressionTreeModel.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = -441861837293501159L;

	public String[] UPDATE;
    
    private RegressionTree root;
    
	public RegressionTreeModel(DataSet dataSet, RegressionTree root) {
		super(dataSet);
		this.root = root;
	}

	public RegressionTree getRoot() {
		return this.root;
	}
	
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException {

    	StringBuffer valueString = new StringBuffer();
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
        getRulesAndConfidence(null, this.root, valueString);
        StringBuffer set = new StringBuffer( "\""+predictedLabel.getName()+"\"");
        UPDATE=new String[1];
        UPDATE[0]=predictedLabel.getName();
        StringBuffer value = valueString;
        String sql = "update "+tableName+" set "+set+"=("+value + ")";
        Statement st = null;
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("TreeModelDB.performPrediction():sql="+sql);
			st.execute(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		return dataSet;
	}
	
    public void getRulesAndConfidence(DevideCond condition, Tree tree, StringBuffer buffer) {
        if (condition != null) {
            buffer.append(" when \""+condition.getColumnName()+"\""+condition.getRelation()+condition.getValueString()+" then ");
        }
        if (!tree.isLeaf()) {
            Iterator<Side> childIterator = tree.childIterator();
            int i = 0;
            while (childIterator.hasNext()) {
            	if ( i == 0 )
            	{
            		buffer.append(" (case ");
            	}
                Side edge = childIterator.next();
                getRulesAndConfidence(edge.getCondition(), edge.getChild(), buffer);
                i++;
            }
           buffer.append(" else null end) ");

        } else {

        	buffer.append(((RegressionTree)tree).getAvg());
        }
    }
	
 
    public String toString() {
        return this.root.toString();
    }

}
