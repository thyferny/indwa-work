/**
 * ClassName PessimisticPrunerDB.java
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.ColumnStats;
import com.alpine.datamining.db.DBDataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.utility.DBPortingFactory;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * This class provides a pruner. It cuts the tree
 * to reduce overfitting.
 * This class is used for data in database.
 */
public class DBPrunePessimistic implements Prune {
    private static Logger itsLogger= Logger.getLogger(DBPrunePessimistic.class);

    private static final double PRUNE_PREFERENCE = 0.001;
    
    private double confidenceLevel;

    private IBuildLeaf leafCreator;
    
    private IBuildLeaf leafCreatorMem;
    
    
    public DBPrunePessimistic(double confidenceLevel, IBuildLeaf leafCreator, IBuildLeaf leafCreatorMem) {
        this.confidenceLevel = confidenceLevel;
        this.leafCreator = leafCreator;
        this.leafCreatorMem = leafCreatorMem;
    }

    public void prune(Tree root) throws OperatorException {
        Iterator<Side> childIterator = root.childIterator();
        while (childIterator.hasNext()) {
            pruneChild(childIterator.next().getChild(), root);
        }
    }
    private boolean loadData(DataSet dataSet)
    {
    	if (dataSet instanceof DBDataSet)
    	{
    		return false;
    	}
    	else
    	{
    		return true;
    	}
    }
    private void pruneChild(Tree currentNode, Tree father) throws OperatorException {
        // going down to fathers of leafs
        if (!currentNode.isLeaf()) {
            Iterator<Side> childIterator = currentNode.childIterator();
            while (childIterator.hasNext()) {
                pruneChild(childIterator.next().getChild(), currentNode);
            }
            if (!childrenHaveChildren(currentNode)) {
                // calculating error estimate for leafs
                double leafsErrorEstimate = 0;
                childIterator = currentNode.childIterator();
                Set<String> classSet = new HashSet<String>();
                while (childIterator.hasNext()) {
                    Tree leafNode = childIterator.next().getChild();
                    DataSet leafDataSet = leafNode.getTrainingSet();
                    classSet.add(leafNode.getLabel());
                    long dataSize = leafDataSet.size();
                    double currentErrorRate = getErrorNumber(leafDataSet, leafDataSet.getColumns().getLabel().getMapping().getIndex(leafNode.getLabel())) / (double) leafDataSet.size();;
                    leafsErrorEstimate += pessimisticErrors(dataSize, currentErrorRate, confidenceLevel)
                            * (((double) dataSize) / currentNode.getTrainingSet().size());
                }

                // calculating error estimate for current node
                DataSet currentNodeDataSet = currentNode.getTrainingSet();
                IBuildLeaf leafCreatorLocal = getConstLeaf(currentNodeDataSet);
                if (classSet.size() <= 1) {
                    currentNode.removeChildren();
                    leafCreatorLocal.changeToLeaf(currentNode, currentNodeDataSet);
                } else {
                    double currentNodeLabel = prunedLabel(currentNodeDataSet);
                    if(currentNodeLabel != -1){
	                    long dataSize = currentNodeDataSet.size();
	                    double currentErrorRate = getErrorNumber(currentNodeDataSet, currentNodeLabel) / (double) currentNodeDataSet.size();
	                    double nodeErrorEstimate = pessimisticErrors(dataSize, currentErrorRate, confidenceLevel);
	                    // if currentNode error level is less than children: prune
	
	                    if (nodeErrorEstimate - PRUNE_PREFERENCE <= leafsErrorEstimate) {
	                        currentNode.removeChildren();
	                        leafCreatorLocal.changeToLeaf(currentNode, currentNodeDataSet);
	                    }
                    }
                }
            }
        }
    }

	/**
	 * @param currentNodeDataSet
	 * @return
	 */
	private IBuildLeaf getConstLeaf(DataSet currentNodeDataSet) {
		IBuildLeaf leafCreatorLocal = null;
		if (loadData(currentNodeDataSet))
		{
			leafCreatorLocal = leafCreatorMem;
		}
		else
		{
			 leafCreatorLocal = leafCreator;
		}
		return leafCreatorLocal;
	}

    private boolean childrenHaveChildren(Tree node) {
        Iterator<Side> iterator = node.childIterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getChild().isLeaf())
                return true;
        }
        return false;
    }

    private long getErrorNumberDB(DataSet dataSet, double label) throws OperatorException {
    	long errors = 0;
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelcolumnName  = dataSet.getColumns().getLabel().getName();//.getMapping().mapIndex((int)label);//label.getName();
        String labelName = dataSet.getColumns().getLabel().getMapping().mapIndex((int)label);
        StringBuffer sql = new StringBuffer();
        itsLogger.debug(label + ":" +labelName);
        sql.append("select count(*) from (").append(selectSQL).append(") foo where ").append(StringHandler.doubleQ(labelcolumnName)).append("<>'").append(labelName).append("'");
        Statement st = null;
        ResultSet rs = null;
        long count = 0; 
       try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("PessimisticPrunerDB.getErrorNumberDB():sql="+sql);
			rs = st.executeQuery(sql.toString());
			rs.next();
			count = rs.getLong(1);
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		errors = count;

        return errors;
    }

    public double prunedLabelDB(DataSet dataSet) throws OperatorException {
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)dataSet.getDBTable()).getSQL();
        String labelcolumnName  = StringHandler.doubleQ(dataSet.getColumns().getLabel().getName());//.getMapping().mapIndex((int)label);//label.getName();
        MultiDBSql multiDBSql=DBPortingFactory.createDecisionTree(databaseConnection.getProperties().getName());
        StringBuffer sql = multiDBSql.getMostLabelIndexSql(selectSQL,
				labelcolumnName);
        Statement st = null;
        ResultSet rs = null;
        String labelName = null;
       try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("PessimisticPrunerDB.prunedLabelDB():sql="+sql);
			rs = st.executeQuery(sql.toString());
			if(rs.next()){
				labelName = rs.getString(1);
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

        double test = dataSet.getColumns().getLabel().getMapping().getIndex(labelName); //= dataSet.getStatistics(labelColumn, ColumnStats.MODE);
        //need statistics
        return test;
    }

    private long getErrorNumber(DataSet dataSet, double label) throws OperatorException {
    	if (loadData(dataSet))
    	{
    		return getErrorNumberMem(dataSet, label);
    	}
    	else
    	{
    		return getErrorNumberDB(dataSet, label);
    	}
    }
    public double prunedLabel(DataSet dataSet) throws OperatorException {
    	if (loadData(dataSet))
    	{
    		return prunedLabelMem(dataSet);
    	}
    	else
    	{
    		return prunedLabelDB(dataSet);
    	}
    }
    private long getErrorNumberMem(DataSet dataSet, double label) {
    	long errors = 0;
        Iterator<Data> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getLabel() != label) {
                errors++;
            }
        }
        return errors;
    }

    public double prunedLabelMem(DataSet dataSet) throws WrongUsedException {
        Column labelColumn = dataSet.getColumns().getLabel();
        dataSet.computeColumnStatistics(labelColumn);
        double test = dataSet.getStatistics(labelColumn, ColumnStats.MODE);
        return test;
    }

    // calculates the pessimistic number of errors, using some confidence level.
    public double pessimisticErrors(double numberOfData, double errorRate, double confidenceLevel) {
        if (errorRate < 1E-6) {
            return errorRate + numberOfData * (1.0 - Math.exp(Math.log(confidenceLevel) / numberOfData));
        } else if (errorRate + 0.5 >= numberOfData) {
            return errorRate + 0.67 * (numberOfData - errorRate);
        } else {
            double coefficient = ErrorFunction.normalInverse(1 - confidenceLevel);
            coefficient *= coefficient;
            double pessimisticRate = (errorRate + 0.5 + coefficient / 2.0d + Math.sqrt(coefficient
                    * ((errorRate + 0.5) * (1 - (errorRate + 0.5) / numberOfData) + coefficient / 4.0d)))
                    / (numberOfData + coefficient);
            return (numberOfData * pessimisticRate);
        }
    }
}
