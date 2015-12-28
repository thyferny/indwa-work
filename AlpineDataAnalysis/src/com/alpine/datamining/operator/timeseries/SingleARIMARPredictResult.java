package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.utility.Tools;

public class SingleARIMARPredictResult  implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private double[] trainLastData;
	private Object[] trainLastIDData;
	private double[] predict;
	private double[] se;
	private Object[] IDData;
	private int type;
	private Column idColumn;
	private String groupByValue;
	public double[] getPredict() {
		return predict;
	}
	public void setPredict(double[] predict) {
		this.predict = predict;
	}
	public double[] getSe() {
		return se;
	}
	public void setSe(double[] se) {
		this.se = se;
	}
	public double[] getTrainLastData() {
		return trainLastData;
	}
	public void setTrainLastData(double[] trainLastData) {
		this.trainLastData = trainLastData;
	}
	public Object[] getTrainLastIDData() {
		return trainLastIDData;
	}
	public void setTrainLastIDData(Object[] trainLastIDData) {
		this.trainLastIDData = trainLastIDData;
	}
	public Object[] getIDData() {
		return IDData;
	}
	public void setIDData(Object[] iDData) {
		IDData = iDData;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Column getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(Column idColumn) {
		this.idColumn = idColumn;
	}
	public String getGroupByValue() {
		return groupByValue;
	}
	public void setGroupByValue(String groupByValue) {
		this.groupByValue = groupByValue;
	}
	public String toString(){
		String ret = "";
		ret += "Train last data: ";
		for(int i = 0; i < trainLastData.length; i++){
			ret += " "+trainLastIDData[i] + ":" +trainLastData[i];
		}
		ret += Tools.getLineSeparator();
		ret+="predict: ";
		for(int i = 0 ; i < predict.length; i++){
			ret+=" "+IDData[i] + ":" +predict[i];
		}
		ret+= Tools.getLineSeparator();
		ret+="se:      ";
		for(int i = 0; i < se.length; i++){
			ret += " "+IDData[i] + ":" +se[i];
		}
		ret+= Tools.getLineSeparator();
		return ret;
	}
}
