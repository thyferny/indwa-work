
package com.alpine.datamining.operator.tree.threshold;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;


public class DecisionTreeModel extends SingleModel {
    private static Logger itsLogger= Logger.getLogger(DecisionTreeModel.class);
    
	private static final long serialVersionUID = 6047261092664473196L;

	public String[] UPDATE;
    
    private Tree root;
    
	public DecisionTreeModel(DataSet dataSet, Tree root) {
		super(dataSet);
		this.root = root;
	}

	public Tree getRoot() {
		return this.root;
	}
	
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException {

    	StringBuffer valueString = new StringBuffer();
    	Column label = getLabel();
    	StringBuffer [] confidence = new StringBuffer[label.getMapping().size()];
    	for ( int i = 0; i < label.getMapping().size(); i++)
    	{
    		confidence[i] = new StringBuffer();
    	}
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
        getRulesAndConfidence(null, this.root, valueString, confidence);
        StringBuffer sql = new StringBuffer("update ");
        sql.append(tableName).append(" set ");
        sql.append( "\""+predictedLabel.getName()+"\" = ").append(valueString);
        UPDATE=new String[label.getMapping().size()+1];
        UPDATE[0]=predictedLabel.getName();
		for (int i = 0; i < label.getMapping().size(); i++)
		{
			sql.append(",");
			sql.append("\""+dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + label.getMapping().mapIndex(i)).getName()+"\" = ");
			sql.append(confidence[i]);
			UPDATE[i+1]=dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + label.getMapping().mapIndex(i)).getName();
		}
        Statement st = null;
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("TreeModelDB.performPrediction():sql="+sql);
			st.execute(sql.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		return dataSet;
	}
	
    private void getRulesAndConfidence(DevideCond condition, Tree tree, StringBuffer buffer, StringBuffer [] confidence) {
        if (condition != null) {
        	String valueString = condition.getValueString();
            buffer.append(" when \""+condition.getColumnName() + "\" " + condition.getRelation() + " " + valueString+" then ");
            for (int i = 0; i < confidence.length; i++)
            {
            	confidence[i].append(" when \""+condition.getColumnName() + "\" " + condition.getRelation() + " " + valueString+" then ");
            }
        }
        if (!tree.isLeaf()) {
            Iterator<Side> childIterator = tree.childIterator();
            int i = 0;
            while (childIterator.hasNext()) {
            	if ( i == 0 )
            	{
            		buffer.append(" (case ");
            		for ( int j = 0; j < confidence.length; j++)
            		{
            			confidence[j].append(" (case ");
            		}
            	}
                Side edge = childIterator.next();
                getRulesAndConfidence(edge.getCondition(), edge.getChild(), buffer, confidence);
                i++;
            }

            String majorityClass = null;
            int majorityCounter = -1;
            Iterator<String> s = tree.getCounterMap().keySet().iterator();
            int[] counts = new int[getLabel().getMapping().size()];
            for ( i = 0; i < counts.length; i++)
            {
            	counts[i] = 0;
            }
            int sum = 0;
            while (s.hasNext()) {
                String className = s.next();
                int count = tree.getCount(className);
                int index = getLabel().getMapping().getIndex(className);
                counts[index] = count;
                sum += count;
                if (count > majorityCounter) {
                    majorityCounter = count;
                    majorityClass = className;
                }
            }
            String valueString = null;
            if (majorityClass != null)
            {
            	valueString = tree.getLabel();
            }
            else
            {
            	valueString = getLabel().getMapping().mapIndex(0);
            }

            buffer.append(" else ");
            if (getLabel().isNominal())
            {
            	buffer.append("'");
            }
            buffer.append(valueString);
            if (getLabel().isNominal())
            {
            	buffer.append("'");
            }

            buffer.append(" end) ");
            for ( i = 0; i < confidence.length; i++)
            {
        		if (sum == 0)
        		{
        			confidence[i].append(" else 0 end) ");
        		}
        		else
        		{
        			confidence[i].append(" else "+(((double) counts[i]) / sum)+ " end) ");
        		}
            }
        } else {
        	String valueString = tree.getLabel();
            if (getLabel().isNominal())
            {
            	buffer.append(" '");
            }
            buffer.append(valueString);
            if (getLabel().isNominal())
            {
            	buffer.append("' ");
            }
            Iterator<String> s = tree.getCounterMap().keySet().iterator();
            int[] counts = new int[getLabel().getMapping().size()];
            for ( int i = 0; i < counts.length; i++)
            {
            	counts[i] = 0;
            }
            int sum = 0;
            while (s.hasNext()) {
                String className = s.next();
                int count = tree.getCount(className);
                int index = getLabel().getMapping().getIndex(className);
                counts[index] = count;
                sum += count;
            }
            
        	for ( int i = 0; i < confidence.length; i++)
        	{
        		if (sum == 0)
        		{
        			confidence[i].append(0);
        		}
        		else
        		{
        			confidence[i].append((((double) counts[i]) / sum)) ;
        		}
        	}
        }
    }
	
 
    public String toString() {
        return this.root.toString();
    }
    
    public int countLeaf() {
        return this.root.countLeaf();
    }
}
