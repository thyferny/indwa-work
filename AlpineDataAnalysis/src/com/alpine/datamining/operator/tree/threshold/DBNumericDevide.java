
package com.alpine.datamining.operator.tree.threshold;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DBPortingFactory;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoNZ;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class DBNumericDevide {
    private static Logger itsLogger= Logger.getLogger(DBNumericDevide.class);

    private Standard criterion;
    double bestSplitBenefit = Double.NEGATIVE_INFINITY;

    public DBNumericDevide(Standard criterion) {
		this.criterion = criterion;
    }
    
    public double getBestSplit(DataSet inputSet, Column column) throws OperatorException {
        bestSplitBenefit = Double.NEGATIVE_INFINITY;
        Column labelColumn = inputSet.getColumns().getLabel();
        String labelClumnName = StringHandler.doubleQ(labelColumn.getName());
        if(labelColumn.getMapping().size()>Integer.parseInt(AlpineDataAnalysisConfig.TREE_LABEL_THRESHOLD))
        {
        	throw new WrongUsedException(null, AlpineAnalysisErrorName.DISTINCT_NUMBER_EXCEED,labelClumnName ,Integer.parseInt(AlpineDataAnalysisConfig.TREE_LABEL_THRESHOLD));
        }
        String columnName = StringHandler.doubleQ(column.getName());
        double bestSplit = Double.NaN;
        DatabaseConnection databaseConnection = ((DBTable)inputSet.getDBTable()).getDatabaseConnection();
        String selectSQL = ((DBTable)inputSet.getDBTable()).getSQL();
        MultiDBSql dbDTree=DBPortingFactory.createDecisionTree(databaseConnection.getProperties().getName());
        Statement st = null;
        ResultSet rs = null;
        String sql = dbDTree.generateNumericSql(labelColumn,
				labelClumnName, columnName, selectSQL,criterion);
        try {
        	if(databaseConnection.getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
        			{
        		
//    			databaseConnection.getConnection().setAutoCommit(false);
    			st = databaseConnection.createStatement(false);
//    			st.setFetchSize(1);
    			itsLogger.debug("NumericalSplitterDB.getBestSplit():sql="+sql);
    			rs = st.executeQuery(sql);
    			boolean limitFlag=true;
    			if(rs.next()&&limitFlag)
    			{
    				bestSplit = rs.getDouble(1);
    				bestSplitBenefit = rs.getDouble(2);
    				limitFlag=false;
    			}
//    			databaseConnection.getConnection().setAutoCommit(true);
    			
        		
        			}else
        			{
        	
			databaseConnection.getConnection().setAutoCommit(false);
			st = databaseConnection.createStatement(false);
			st.setFetchSize(1);
			itsLogger.debug("NumericalSplitterDB.getBestSplit():sql="+sql);
			rs = st.executeQuery(sql);
			boolean limitFlag=true;
			if(rs.next()&&limitFlag)
			{
				bestSplit = rs.getDouble(1);
				bestSplitBenefit = rs.getDouble(2);
				limitFlag=false;
			}
			databaseConnection.getConnection().setAutoCommit(true);
        			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
        return bestSplit;
    }
    
	public double getBestSplitScore() {
		return bestSplitBenefit;
	}

	public void setBestSplitScore(double bestSplitBenefit) {
		this.bestSplitBenefit = bestSplitBenefit;
	}
}
