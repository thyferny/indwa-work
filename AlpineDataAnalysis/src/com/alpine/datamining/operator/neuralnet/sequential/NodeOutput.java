
package com.alpine.datamining.operator.neuralnet.sequential;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;


public class NodeOutput extends NNNode{
	
	
	private static final long serialVersionUID = -59898700869636570L;

	private Column label;
	
	private int classIndex = 0;
	
	private double labelRange;
	
	private double labelBase;

	private String currentValueSQL;
	
	private String currentValueSQLPrediction;
	
	private int dataIndex;
	

	public NodeOutput(String nodeName, Column label, double labelRange, double labelBase, int dataIndex) {
		super(nodeName, OUTPUT, OUTPUT);
		this.label = label;
		this.labelRange = labelRange;
		this.labelBase = labelBase;
		this.dataIndex = dataIndex;
	}

	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}
	
	public int getClassIndex() {
		return this.classIndex;
	}
	
	public double computeValue(boolean shouldCalculate, double[] row) {
		if (Double.isNaN(currentValue) && shouldCalculate) {
			currentValue = 0;
			for (int i = 0; i < inputNodes.length; i++) {
				currentValue += inputNodes[i].computeValue(true, row);

			}
			if (!label.isNominal()) {
				currentValue = currentValue * labelRange + labelBase;
			}
		}
		return currentValue;
	}

	
	
	public double computeError(boolean shouldCalculate, double[] row) {
		if (!Double.isNaN(currentValue) && Double.isNaN(currentError) && shouldCalculate) {
			if (label.isNominal()) {
				if ((int)row[dataIndex] == classIndex) {
					currentError = 1.0d - currentValue;
				} else {
					currentError = 0.0d - currentValue;
				}
			} else if (!label.isNominal()) {
				if (labelRange == 0.0d) {
					currentError = 0.0d;
				} else {
					currentError = (row[dataIndex] - currentValue) / labelRange;
				}
			}
		}
		return currentError;
	}
	
	public String computeValue(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQL && shouldCalculate) {
			currentValueSQL = "0.0";
			for (int i = 0; i < inputNodes.length; i++) {
				currentValueSQL += "+"+inputNodes[i].computeValue(true, dataSet);

			}
			if (!label.isNominal()) {
				currentValueSQL = "("+currentValueSQL +")*"+ labelRange +"+"+ labelBase;
			}
		}
		return "("+currentValueSQL+")";
	}
	
	public String computeValuePrediction(boolean shouldCalculate, DataSet dataSet) {
		if (null == currentValueSQLPrediction && shouldCalculate) {
			currentValueSQLPrediction = "0.0";
			for (int i = 0; i < inputNodes.length; i++) {
				currentValueSQLPrediction += "+"+inputNodes[i].computeValuePrediction(true, dataSet);

			}
			if (!label.isNominal()) {
				currentValueSQLPrediction = "("+currentValueSQLPrediction +")*"+ labelRange +"+"+ labelBase;
			}
		}
		return "("+currentValueSQLPrediction+")";
	}

	public double getLabelRange() {
		return labelRange;
	}

	public void setLabelRange(double labelRange) {
		this.labelRange = labelRange;
	}

	public double getLabelBase() {
		return labelBase;
	}

	public void setLabelBase(double labelBase) {
		this.labelBase = labelBase;
	}
}
