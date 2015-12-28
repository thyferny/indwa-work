
package com.alpine.datamining.operator.evaluator;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


public class ROCDataGeneratorGeneral extends Operator implements Serializable {
    private static Logger itsLogger= Logger.getLogger(ROCDataGeneratorGeneral.class);
    
	private static final long serialVersionUID = 8812581936833065822L;

	
	public static final int MAX_ROC_POINTS = 200;

	private double bestThreshold = Double.NaN;
	
	EvaluatorParameter para;
	
	public ROCDataGeneratorGeneral() {
		super();
	}


	
	public double getBestThreshold() {
		return bestThreshold;
	}

	
	public ROCData createROCData(DataSet dataSet, 
			String targetClass) throws OperatorException {

//		Column weightAttr = null;
		String weightString = "1.0";
//		weightAttr = dataSet.getColumns().getWeight();
//		if (weightAttr != null)
//		{
//			weightString = StringHandler.doubleQ(weightAttr.getName());
//		}
//		else
//		{
//			weightString = "1.0";
//		}

		double tp = 0.0d;
		double sum = 0.0d;
		bestThreshold = Double.POSITIVE_INFINITY;

		ROCData rocData = new ROCData();
		if(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + targetClass)==null)
		{
			throw new WrongUsedException(this, AlpineAnalysisErrorName.NULL_PREDICT_COL);
		}
		String targetClassConfidence = StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + targetClass).getName());
		Column label = dataSet.getColumns().getLabel();
		String labelName = StringHandler.doubleQ(label.getName());
		targetClass=StringHandler.escQ(targetClass);
		DatabaseConnection databaseConnection = null;
		String tableName = null;
		Statement st = null;
		ResultSet rs = null;

		databaseConnection = ((DBTable) dataSet.getDBTable())
		.getDatabaseConnection();
		tableName = ((DBTable) dataSet.getDBTable())
		.getTableName();

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		rs = null;
		StringBuffer sql = new StringBuffer();

		double min = 0.0;
		double max = 0.0;

		sql.append("select min(").append(targetClassConfidence).append("),max(").append(targetClassConfidence).append(") from " //+ schemaName + "."
		).append(tableName);
		try {
			itsLogger.debug("ROCDataGeneratorGeneral.createROCData():sql=" + sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				min = rs.getDouble(1);
				max = rs.getDouble(2);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		double diff = (max - min) / MAX_ROC_POINTS;
		StringBuffer sumArray = new StringBuffer();
		StringBuffer tpArray = new StringBuffer();
		for (int i = 0; i < MAX_ROC_POINTS; i++) {
			if (i != 0) {
				sumArray.append(",");
				tpArray.append(",");
			}
			sumArray.append("sum( case when ").append(targetClassConfidence)
			.append(" >= ").append((max - (i + 1) * diff)).append(
			" then ").append(weightString).append(
			" else 0 end)");
			tpArray.append("sum( case when ").append(targetClassConfidence)
			.append(" >= ").append((max - (i + 1) * diff)).append(" and ").append(labelName)
			.append(" ='").append(targetClass).append("'").append(
			" then ").append(weightString).append(" else 0 end)");
		}
		sql = new StringBuffer();
		sql.append(" select ").append(sumArray ).append(",").append( tpArray).append(" from ").append(tableName);
		try {
			itsLogger.debug("ROCDataGeneratorGeneral.createROCData():sql="
                    + sql);
			rs = st.executeQuery(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		try {
			while (rs.next()) {
				for (int i = 0; i < MAX_ROC_POINTS; i++) {
					sum = rs.getDouble(i + 1);
					tp = rs.getDouble(MAX_ROC_POINTS + i + 1);
					rocData.addPoint(new ROCPoint(sum - tp, tp, max - (i + 1)
							* diff));
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		rocData.setTotalPositives(tp);
		rocData.setTotalNegatives(sum - tp);

		return rocData;
	}

	private List<double[]> createROCData(ROCData data) {
		List<double[]> ROCData = new ArrayList<double[]>();
		Iterator<ROCPoint> i = data.iterator();
		int pointCounter = 0;
		int eachPoint = Math.max(1, (int) Math.round((double) data
				.getNumberOfPoints()
				/ (double) MAX_ROC_POINTS));
		ROCData.add(new double[] { 0, 0});
		while (i.hasNext()) {
			ROCPoint point = i.next();
			if ((pointCounter == 0) || ((pointCounter % eachPoint) == 0)
					|| (!i.hasNext())) { // draw only MAX_ROC_POINTS points
				double TN = data.getTotalNegatives();
				if (TN == 0)
				{
					TN += 0.0001;
				}
				double TP = data.getTotalPositives();
				if (TP == 0)
				{
					TP += 0.0001;
				}
				double fpRate = point.getFP()
						/ (TN);
				double tpRate = point.getTP()
						/ (TP);
				ROCData.add(new double[] { fpRate, // x
						tpRate });
			}
			pointCounter++;
		}
		return ROCData;
	}

	public double calculateAUC(ROCData rocData) {
		if (rocData.getNumberOfPoints() == 2) {
			return 0.5;
		}
		// calculate AUC (area under curve)
		double aucSum = 0.0d;
		double[] last = new double[] { 0, 0 };
		Iterator<ROCPoint> i = rocData.iterator();
		while (i.hasNext()) {
			ROCPoint point = i.next();
			double TN = rocData.getTotalNegatives();
			if (TN == 0)
			{
				TN += 0.0001;
			}
			double TP = rocData.getTotalPositives();
			if (TP == 0)
			{
				TP += 0.0001;
			}
			double fpDivN = point.getFP()
					/ TN; // false positives divided by
			// sum of all negatives
			double tpDivP = point.getTP()
					/ TP; // true positives divided by
			// sum of all positives
			if (last != null) {
				aucSum += ((tpDivP - last[1]) * (fpDivN - last[0]) / 2.0d)
						+ (last[1] * (fpDivN - last[0]));
			}
			last = new double[] { fpDivN, tpDivP };
		}

		return aucSum;
	}

	public DoubleListAndDoubleData CreateROCAUCData(ROCData rocData) {
		DoubleListAndDoubleData data = new DoubleListAndDoubleData();
		data.setDoubleList(createROCData(rocData));
		data.setDouble(calculateAUC(rocData));
		return data;
	}

	public ConsumerProducer[] apply() throws OperatorException {
		para = (EvaluatorParameter)getParameter();
        DataSet dataSet;
		dataSet = getInput(DataSet.class);
		if (dataSet.getColumns().getLabel() == null) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.MISS_DEP);
		}
		Model model = null;
		if (para.isUseModel()) {
			model = getInput(Model.class);
			if(model instanceof com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel
					||(model instanceof NNModel&&((NNModel)model).getLabel().isNumerical())){
				throw new WrongUsedException(this, AlpineAnalysisErrorName.REGRESSION_EVALUATE,"ROC");
			}
			dataSet = model.apply(dataSet);
		}

		String targetClass = para.getColumnValue();
		ROCData rocData = createROCData(dataSet, targetClass);
		DoubleListAndDoubleData data = CreateROCAUCData(rocData);

		if (para.isUseModel()) {
			return new ConsumerProducer[] { dataSet, model, data};
		} else
			return new ConsumerProducer[] {dataSet, data };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class, Model.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { DataSet.class, Model.class, ROCData.class };
	}
	
}
