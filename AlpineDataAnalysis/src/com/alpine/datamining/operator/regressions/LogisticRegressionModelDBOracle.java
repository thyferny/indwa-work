
package com.alpine.datamining.operator.regressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.tools.StringHandler;



public class LogisticRegressionModelDBOracle extends LogisticRegressionModelDB {
    
	
	private static final long serialVersionUID = -3965373796731290645L;

	public LogisticRegressionModelDBOracle(DataSet dataSet,DataSet oldDataSet, double[] beta, double[] variance, boolean interceptAdded, String goodValue) {
        super( dataSet, oldDataSet,  beta,  variance, interceptAdded, goodValue);
    }
	protected void appendUpdateSet(DataSet dataSet, StringBuilder sql,
			StringBuilder functionValuesb, String goodColumn, String badColumn,
			StringBuilder predictionStringsb, String predictedLabelName) {
		sql.append(" set ").append(predictedLabelName).append(" = ").append(predictionStringsb)
		.append(",").append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + goodColumn).getName())).append(" = ").append(functionValuesb)
		.append(",").append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + badColumn).getName())).append(" = ").append(" 1.0 - ").append(functionValuesb);
	}
	protected StringBuilder getProbability() {
		StringBuilder probability = null;
		probability = getProbabilityFunction(oldDataSet);

		return probability;
	}

	protected StringBuilder getProbabilityFunction(DataSet dataSet) {
		ArrayList<String> columnNamesList = new ArrayList<String>();
		ArrayList<String> betaList = new ArrayList<String>();
		HashMap<String, Double> betaMap = getBetaMap();
		for (Column column : dataSet.getColumns()) {
			String columnName = StringHandler.doubleQ(column.getName());
			if (column.isNumerical()) {
				if(betaMap.get(column.getName())==null)continue;
				double beta = betaMap.get(column.getName());
				columnNamesList.add(columnName);
				betaList.add(String.valueOf(beta));
			} else {
				HashMap<String, String> TransformMap_valueKey = new HashMap<String, String>();
				TransformMap_valueKey = getAllTransformMap_valueKey().get(
						column.getName());
				if(TransformMap_valueKey==null)continue;
				Iterator<String> valueIterator = TransformMap_valueKey.keySet().iterator();
				String value = null;
				while (valueIterator.hasNext())
				{
					value = valueIterator.next();
					String columnname = TransformMap_valueKey.get(value);
					if (betaMap.get(columnname) == null)
						continue;
					double beta = betaMap.get(columnname);
					value=StringHandler.escQ(value);
					
					columnNamesList.add("(case when "+columnName+"="+CommonUtility.quoteValue(dataSourceInfo.getDBType(),column, value)+" then 1 else 0 end)");
					betaList.add(String.valueOf(beta));
				}
			}
		}
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(betaMap.get(key)!= null){
				columnNamesList.add(String.valueOf(betaMap.get(key)));
				betaList.add(value);
			}
		}

		if (interceptAdded) {
			betaList.add(String.valueOf(betaMap.get(interceptString)));
		}
		StringBuilder probability = new StringBuilder("alpine_miner_lr_ca_pi(");
		probability.append(CommonUtility.array2OracleArray(betaList,CommonUtility.OracleDataType.Float)).append(",").append(CommonUtility.array2OracleArray(columnNamesList,CommonUtility.OracleDataType.Float)).append(",");
		addIntercept(probability);
		probability.append(")");
		return probability;
	}

	protected void addIntercept(StringBuilder probability) {
		if (interceptAdded)
		{
			probability.append("1");
		}
		else
		{
			probability.append("0");
		}
	}
}
	
