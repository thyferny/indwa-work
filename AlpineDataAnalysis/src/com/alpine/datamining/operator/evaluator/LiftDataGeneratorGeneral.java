
package com.alpine.datamining.operator.evaluator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


public class LiftDataGeneratorGeneral extends Operator{
    private static final Logger itsLogger = Logger.getLogger(LiftDataGeneratorGeneral.class);

    
	public static final int MAX_LIFT_POINTS = 200;
	EvaluatorParameter para;


	
	public LiftDataGeneratorGeneral() {
		super();
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
			if(model instanceof com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel){
				throw new WrongUsedException(this, AlpineAnalysisErrorName.REGRESSION_EVALUATE,"LIFT");
			}
			dataSet = model.apply(dataSet);
		}
		String targetClass = para.getColumnValue();
		DoubleListData  data = createLiftDataList(dataSet, targetClass);

		if (para.isUseModel()) {
			return new ConsumerProducer[] { dataSet, model, data};
		} else
			return new ConsumerProducer[] {dataSet, data };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class, Model.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { DataSet.class, Model.class, DoubleListData.class };
	}
	


	
	public DoubleListData createLiftDataList(DataSet dataSet, String targetClass)
			throws OperatorException {

		List<double[]> tableData = new LinkedList<double[]>();
		if(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + targetClass)==null)
		{
			throw new WrongUsedException(this, AlpineAnalysisErrorName.NULL_PREDICT_COL);
		}
		String targetClassConfidence =StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + targetClass).getName());
		Column label = dataSet.getColumns().getLabel();
		String labelName = StringHandler.doubleQ(label.getName());
		targetClass=StringHandler.escQ(targetClass);
		DatabaseConnection databaseConnection = null;
		String tableName = null;
		Statement st = null;
		ResultSet rs = null;
//		Column weightAttr = null;
		String weightString = "1.0";
//		weightAttr = dataSet.getColumns().getWeight();
//		if (weightAttr != null) {
//			weightString = StringHandler.doubleQ(weightAttr.getName());
//		}

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
		double sumTotal = 0.0d;
		double pTotal = 0.0d;
		sql.append("select min(").append(targetClassConfidence).append(") , max(").append( targetClassConfidence).append(") , sum(" ).append( weightString ).append( "), sum(case when  " ).append( labelName ).append( " ='"
		).append( targetClass ).append( "' then " ).append( weightString ).append( " else 0 end) from " //+ schemaName + "."
		).append( tableName);
		try {
			itsLogger.debug("LiftDataGeneratorGeneral.createLiftDataList():sql="+sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				min = rs.getDouble(1);
				max = rs.getDouble(2);
				sumTotal = rs.getDouble(3);
				pTotal = rs.getDouble(4);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		if (sumTotal == 0)
		{
			sumTotal += 0.001;
		}
		double rateTotal = pTotal / (sumTotal);
		if (rateTotal == 0)
		{
			rateTotal += 0.00001;
		}
		double diff = (max - min) / MAX_LIFT_POINTS;
		StringBuffer sumArray = new StringBuffer();
		StringBuffer pArray = new StringBuffer();
		for (int i = 0; i < MAX_LIFT_POINTS; i++) {
			
			if (i != 0)
			{
				sumArray.append(",");
				pArray.append(",");
			}
			sumArray.append("sum(case when ").append(targetClassConfidence).append(" >= ").append((max - (i + 1) * diff)).append(" then ").append(weightString).append(" else 0 end)");
			pArray.append("sum(case when ").append(targetClassConfidence).append(" >= ").append((max - (i + 1) * diff)).append(" and ").append(labelName).append(" ='")
			.append(targetClass).append("'").append(" then ").append(weightString).append(" else 0 end)");
		}
		sql = new StringBuffer();
		sql.append("select ").append(sumArray).append(",").append(pArray).append(" from ").append(tableName);
		
		try {
            itsLogger.debug("LiftDataGeneratorGeneral.createLiftDataList():sql="+sql);
			rs = st.executeQuery(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		try {
			while (rs.next()) {
				for (int i = 0; i < MAX_LIFT_POINTS; i++) {
					double sum = 0.0d;
					double p = 0.0d;
					sum = rs.getDouble(i+1);
					p = rs.getDouble(i+MAX_LIFT_POINTS+1);

					if (sum == 0)
					{
						sum += 0.001;
					}
					double ratio = sum/sumTotal;
					double lift = p / (sum) / (rateTotal);
					if ( i == 1)
					{
						if (ratio > 1.0/MAX_LIFT_POINTS)
						{
							tableData.add(new double[] {1.0/MAX_LIFT_POINTS , lift });
						}
					}
					tableData.add(new double[] {ratio , lift });
				}
			}
			tableData.add(new double[] {1.0 , 1.0 });
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return new DoubleListData(tableData);
	}
}
