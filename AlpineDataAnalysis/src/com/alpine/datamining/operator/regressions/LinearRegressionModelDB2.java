/**
 * ClassName LinearRegressionModelDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.tools.StringHandler;


/**
 * The model db2 for linear regression.
 *  @author Eason
 */
public class LinearRegressionModelDB2 extends LinearRegressionModelDB {
	

	private static final long serialVersionUID = 3560702652340212186L;

	public LinearRegressionModelDB2(DataSet dataSet, String [] columnNames,String specifyColumn,Double[] coefficients, HashMap<String,Double> coefficientmap) {
		super(dataSet, columnNames,specifyColumn, coefficients, coefficientmap);
	}

	public StringBuffer generatePredictedString(DataSet dataSet) {
		String dbType = ((DBTable) dataSet.getDBTable())
		.getDatabaseConnection().getProperties().getName();
		StringBuffer predictedString = new StringBuffer();
		predictedString.append(coefficients[coefficients.length - 1]);
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> atts_i=atts.iterator();
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(coefficientsMap.get(att.getName())==null)continue;
				double coefficient=coefficientsMap.get(att.getName());
				predictedString.append("+double(").append(coefficient).append(")*").append(columnName);
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(coefficientsMap.get(columnname)==null)continue;
 	 					double coefficient=coefficientsMap.get(columnname);

 	 					predictedString.append("+double(").append(coefficient).append(")*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end)");

 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(coefficientsMap.get(key)!= null){
				predictedString.append("+"+value +"*double(" + coefficientsMap.get(key)+")");
			}
		} 
		return predictedString;
	}
}
