
package com.alpine.datamining.operator.training;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.ColumnFactory;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Table;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.AbstractModel;
import com.alpine.datamining.utility.DataType;
import com.alpine.utility.db.DataSourceInfoDB2;
import org.apache.log4j.Logger;


public abstract class Prediction extends AbstractModel {
	
    private static final Logger itsAbstractLogger = Logger.getLogger(Prediction.class);
    private static final long serialVersionUID = 4690086325473178752L;
	public static final String ALPINE_MINER_CATEGORY_NULL = "alpine_miner_category_null";

    protected Prediction(DataSet trainingDataSet) {
        super(trainingDataSet);
    }
    
	public abstract DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException;


	public DataSet apply(DataSet dataSet) throws OperatorException {
		if (dataSet.size() <= 0 )
		{
			return dataSet;
		}
		Column predictedLabel = createPredictedLabel(dataSet, getLabel());
		reorgTableDB2(dataSet);
		DataSet result = performPrediction(dataSet, predictedLabel);
		reorgTableDB2(dataSet);
        return result;
	}

	protected static void reorgTableDB2(DataSet dataSet) throws OperatorException {
		if(((DBTable) dataSet
				.getDBTable()).getDatabaseConnection().getProperties().getName().equals(DataSourceInfoDB2.dBType)){
			try {
				Statement st = ((DBTable) dataSet
						.getDBTable()).getDatabaseConnection().createStatement(false);
				String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
				String sql="CALL SYSPROC.ADMIN_CMD(' REORG TABLE " + tableName+" ')";
                itsAbstractLogger.debug(sql);
				st.execute(sql);
				st.close();
			} catch (SQLException e) {
                itsAbstractLogger.info(e.getLocalizedMessage());
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}

	
	public Column getLabel() {
		return getTrainingHeader().getColumns().getLabel();
	}
    

	public static Column createPredictedLabel(DataSet dataSet, Column label)throws OperatorException {
		// create and add prediction column
		Column predictedLabel = ColumnFactory.createColumn(label, Column.PREDICTION_NAME);
		predictedLabel.setSpecialName(Column.PREDICTION_NAME);
//		predictedLabel.clearShifts();
		Table table = dataSet.getDBTable();
		reorgTableDB2(dataSet);
		table.addColumn(predictedLabel);
		reorgTableDB2(dataSet);
		dataSet.getColumns().setPredictedLabel(predictedLabel);

		// create and add confidence columns for nominal labels
		if (label.isNominal() || label.isCategory()) {
			Iterator<?> i = predictedLabel.getMapping().getValues().iterator();
			while (i.hasNext()) {
				String value = (String) i.next();
				Column confidence = ColumnFactory.createColumn(Column.CONFIDENCE_NAME + "(" + value + ")", DataType.REAL);
				reorgTableDB2(dataSet);
				table.addColumn(confidence);
				reorgTableDB2(dataSet);
				dataSet.getColumns().setSpecialColumn(confidence, Column.CONFIDENCE_NAME + "_" + value);
			}
		}
		return predictedLabel;
	}

	public String toString() {
		return getName() + " (prediction model for label " + getTrainingHeader().getColumns().getLabel().getName() + ")";
	}


    public static void removePredictedLabel(DataSet dataSet) {
        removePredictedLabel(dataSet, true, true);
    }
    

	public static void removePredictedLabel(DataSet dataSet, boolean removePredictionFromTable, boolean removeConfidencesFromTable) {
		Column predictedLabel = dataSet.getColumns().getPredictedLabel();
		if (predictedLabel != null) { // remove old predicted label
			if (predictedLabel.isNominal()) {
				Iterator<?> i = predictedLabel.getMapping().getValues().iterator();
				while (i.hasNext()) {
					String value = (String) i.next();
					Column currentConfidenceColumn = dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + value);
					if (currentConfidenceColumn != null) {
						dataSet.getColumns().remove(currentConfidenceColumn);
                        if (removeConfidencesFromTable)
                            dataSet.getDBTable().removeColumn(currentConfidenceColumn);
					}
				}
			}
			dataSet.getColumns().remove(predictedLabel);
            if (removePredictionFromTable)
                dataSet.getDBTable().removeColumn(predictedLabel);
		}
	}


    public static void copyPredictedLabel(DataSet source, DataSet destination) {
        Column predictedLabel = source.getColumns().getPredictedLabel();
        if (predictedLabel != null) {
            removePredictedLabel(destination, true, true);
            if (predictedLabel.isNominal()) {
                Iterator<?> i = predictedLabel.getMapping().getValues().iterator();
                while (i.hasNext()) {
                    String value = (String) i.next();
                    Column currentConfidenceColumne = source.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + value);
                    if (currentConfidenceColumne != null) {
                        destination.getColumns().setSpecialColumn(currentConfidenceColumne, Column.CONFIDENCE_NAME + "_" + value);
                    }
                }
            }
            destination.getColumns().setPredictedLabel(predictedLabel);
        }
    }
}
