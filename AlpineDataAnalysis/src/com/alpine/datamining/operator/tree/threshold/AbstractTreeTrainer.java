
package com.alpine.datamining.operator.tree.threshold;

import java.sql.SQLException;
import java.util.ArrayList;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Table;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;


public abstract class AbstractTreeTrainer extends Trainer {

	

    public AbstractTreeTrainer() {
        super();
    }


    public Model train(DataSet eSet) throws OperatorException {
    	
    	DataSet dataSet = getNoNullDataSet(eSet);
		dataSet.computeAllColumnStatistics();
    	ConstructTree builder = getTreeBuilder(dataSet); 
    	    	
    	// learn tree
    	Tree root = builder.learnTree(dataSet);
        
        // create and return model
        return new DecisionTreeModel(dataSet, root);
    }

	private DataSet getNoNullDataSet(DataSet eSet) throws WrongUsedException,
			OperatorException {
		DataSet dataSet = (DataSet) eSet.clone();

		String whereCondition = ((DBTable) dataSet
				.getDBTable()).getWhereCondition();
		String notNullArray = "";
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
			if (!first) {
				notNullArray += " and ";
			} else {
				first = false;
			}
			notNullArray += StringHandler.doubleQ(column.getName())
					+ " is not null ";
		}
		notNullArray += " and "
				+ StringHandler.doubleQ(dataSet.getColumns().getLabel()
						.getName()) + " is not null ";
		StringBuffer newWhereCondition = new StringBuffer();

		if (whereCondition != null && whereCondition.length() != 0) {
			newWhereCondition.append(whereCondition).append(notNullArray);
		} else {
			newWhereCondition.append(notNullArray);
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		String url = ((DBTable) dataSet.getDBTable())
				.getUrl();
		String userName = ((DBTable) dataSet
				.getDBTable()).getUserName();
		String password = ((DBTable) dataSet
				.getDBTable()).getPassword();
		ArrayList<Column> regularAttrubtes = new ArrayList<Column>();
		for (Column regularColumn : dataSet.getColumns()) {
			regularAttrubtes.add((Column) regularColumn.clone());
		}
		Table table = null;
		try {
			table = DBTable.createDatabaseDataTableDB(
					databaseConnection, url, userName, password, tableName,
					newWhereCondition.toString());
			Column labelColumn = (Column) dataSet.getColumns()
					.getLabel().clone();
			dataSet = table.createDataSet(labelColumn,
					regularAttrubtes);
			dataSet.computeAllColumnStatistics();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}
    protected abstract ConstructTree getTreeBuilder(DataSet dataSet) throws OperatorException;

	protected Standard createStandard(double minimalGain, boolean loadData){
        Class<?> criterionClass = null;
        if (loadData)
        {
            criterionClass = InformationGainStandard.class;
        }
        else
        {
        	criterionClass = DBInformationGainStandard.class;
        }
            try {
                Standard criterion = (Standard)criterionClass.newInstance();
                if (criterion instanceof StandardI) {
                	((StandardI)criterion).setMinimalGain(minimalGain);
                }
                return criterion;
            } catch (InstantiationException e) {
                if (loadData)
                {
                	return new InformationGainStandard(minimalGain);
                }
                else
                {
                	return new DBInformationGainStandard(minimalGain);
                }
            } catch (IllegalAccessException e) {
                if (loadData)
                {
                	return new InformationGainStandard(minimalGain);
                }
                else
                {
                	return new DBInformationGainStandard(minimalGain);
                }
           }
    }

}
