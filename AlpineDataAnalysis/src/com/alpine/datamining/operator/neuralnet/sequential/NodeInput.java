/**
 * ClassName NodeInput
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.neuralnet.sequential;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.tools.StringHandler;

/**
 */
public class NodeInput extends NNNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6707683446031638414L;

	private Column column;

	private double columnRange;
	
	private double columnBase;
	
	private boolean normalize;

	private String currentValueSQL;
	
	private String currentValueSQLPrediction;
	
	private HashMap<String,String> TransformMap_columnKey;

	private int dataIndex;
	private String dBType;

	private Column oldColumn;

	public String getdBType() {
		return dBType;
	}
	public void setdBType(String dBType) {
		this.dBType = dBType;
	}
	
	
	public NodeInput(String nodeName,HashMap<String,String> TransformMap_columnKey) {
		super(nodeName, INPUT, INPUT);
		this.TransformMap_columnKey=TransformMap_columnKey;
	}
	public NodeInput(String nodeName)
	{
		super(nodeName, INPUT, INPUT);
	}
	public void setColumn(Column column, Column oldColumn, double columnRange, double columnBase, boolean normalize, int dataIndex) {
		this.column      = column;
		this.oldColumn = oldColumn;
		this.columnRange = columnRange;
		this.columnBase  = columnBase;
		this.normalize      = normalize;
		this.dataIndex = dataIndex;
	}

	public double computeValue(boolean shouldCalculate, double[] row) {
		if (Double.isNaN(currentValue) && shouldCalculate) {
			double value = row[dataIndex];
			if (Double.isNaN(value)) {
				currentValue = 0;
			} else {
				if (normalize) {
					if (columnRange != 0) {
						currentValue = (value - columnBase) / columnRange;
					} else {
						currentValue = value - columnBase;
					}
				} else {
					currentValue = value;
				}
			}
		}
		return currentValue;
	}

	public double computeError(boolean shouldCalculate, double[] row) {
		if (!Double.isNaN(currentValue) && Double.isNaN(currentError) && shouldCalculate) {
			currentError = 0;
			for (int i = 0; i < outputNodes.length; i++) {
				currentError += outputNodes[i].computeError(true, row);
			}
		}
		return currentError;
	}
	
	public String computeValue(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQL && shouldCalculate) {
			String value = column.getName();
			if (null == value){
				currentValueSQL = "0.0";
			} else {
				value=StringHandler.doubleQ(value);
				if (normalize) {
					if (columnRange != 0) {
						currentValueSQL = "("+value+" - "+columnBase+") /" +columnRange;
					} else {
						currentValueSQL = value +"-"+ columnBase;
					}
				} else {
					currentValueSQL = value;
				}
			}
		}
		return "("+currentValueSQL+")";
	}
	
	public String computeValuePrediction(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQLPrediction && shouldCalculate) {
			String columnName = column.getName();
			if(TransformMap_columnKey==null)
			{
				if (null == columnName){
					currentValueSQLPrediction = "0.0";
				} else {
					columnName=StringHandler.doubleQ(columnName);
					if (normalize) {
						if (columnRange != 0) {
							currentValueSQLPrediction = "("+columnName+" - "+columnBase+") /" +columnRange;
						} else {
							currentValueSQLPrediction = columnName +"-"+ columnBase;
						}
					} else {
						currentValueSQLPrediction = columnName;
					}
				}
			}else
			{
				String value=TransformMap_columnKey.get(columnName);
				value=StringHandler.escQ(value);	
				if (null == columnName){
					currentValueSQLPrediction = "0.0";
				} else {
					String[] temp=columnName.split("_");
					String columnname="";
					if(temp.length==2)
					{
						columnname=temp[0];
					}
					else
					{
						for(int i=0;i<temp.length-1;i++)
						{
							if(i!=temp.length-2)
							{columnname+=temp[i]+"_";}
							else
							{
								columnname+=temp[i];
							}						
						}				
					}
					columnname=StringHandler.doubleQ(columnname);
					if (normalize) {
						if (columnRange != 0) {
							currentValueSQLPrediction = "((case when "+columnname+"="+CommonUtility.quoteValue(dBType,oldColumn,value)+" then 1 else 0 end)"+" - "+columnBase+") /" +columnRange;
						} else {
							currentValueSQLPrediction = "((case when "+columnname+"="+CommonUtility.quoteValue(dBType,oldColumn,value)+" then 1 else 0 end)" +"-"+ columnBase+")";
						}
					} else {
						currentValueSQLPrediction = "(case when "+columnname+"="+CommonUtility.quoteValue(dBType,oldColumn,value)+" then 1 else 0 end)";
					}
				}
			}
		}
		return "("+currentValueSQLPrediction+")";
	}

	public Column getColumn() {
		return column;
	}
	public void setcolumn(Column column) {
		this.column = column;
	}
	public double getColumnRange() {
		return columnRange;
	}
	public void setColumnRange(double columnRange) {
		this.columnRange = columnRange;
	}
	public double getColumnBase() {
		return columnBase;
	}
	public void setColumneBase(double columnBase) {
		this.columnBase = columnBase;
	}
	public StringBuffer getTransformValue() {
		StringBuffer buf = new StringBuffer();
		String columnName = column.getName();
		if(TransformMap_columnKey==null)
		{
			if (null == columnName){
				buf.append("0.0");
			} else {
				columnName=StringHandler.doubleQ(columnName);
				buf.append(columnName);
			}
		}else
		{
			String value = null;
			if(dBType.equals(DataSourceInfoNZ.dBType)){
			Iterator<Entry<String, String>> iter = TransformMap_columnKey.entrySet().iterator(); 
				while (iter.hasNext()) { 
				    Map.Entry<String, String> entry = iter.next(); 
				    String key = entry.getKey(); 
				    if(key.trim().equals(column.getName())){
				    	value = entry.getValue();
				    }
				}
			}else{
				value=TransformMap_columnKey.get(columnName);
			}
			if (null == columnName){
				buf.append("0.0");
			} else {
				String[] temp=columnName.split("_");
				String columnname="";
				if(temp.length==2 || temp.length == 1)
				{
					columnname=temp[0];
				}
				else
				{
					for(int i=0;i<temp.length-1;i++)
					{
						if(i!=temp.length-2)
						{columnname+=temp[i]+"_";}
						else
						{
							columnname+=temp[i];
						}						
					}
				}
				columnname=StringHandler.doubleQ(columnname);
				buf.append("(case when ").append(columnname).append("=").append(CommonUtility.quoteValue(dBType,oldColumn,value)).append(" then 1 else 0 end)");
			}
		}
		return buf;
	}
}
