package com.alpine.datamining.operator.regressions;

import java.util.HashMap;

public interface LoRModelIfc {

	public abstract double getModelDeviance();

	public abstract void setModelDeviance(double modelDeviance);

	public abstract double getNullDeviance();

	public abstract void setNullDeviance(double nullDeviance);

	public abstract double getChiSquare();

	public abstract void setChiSquare(double chiSquare);

	public abstract long getIteration();

	public abstract void setIteration(long iteration);

	public abstract double[] getBeta();

	public abstract void setBeta(double[] beta);

	public abstract double[] getStandardError();

	public abstract void setStandardError(double[] standardError);

	public abstract double[] getWaldStatistic();

	public abstract void setWaldStatistic(double[] waldStatistic);

	public abstract double[] getzValue();

	public abstract void setzValue(double[] zValue);

	public abstract double[] getpValue();

	public abstract void setpValue(double[] pValue);

	public abstract String[] getColumnNames();

	public abstract void setColumnNames(String[] columnNames);

	public abstract String getGood();

	public abstract void setGood(String good);
	
	public abstract boolean isImprovementStop();
	public abstract void setImprovementStop(boolean improvementStop);

	public abstract HashMap<String, String[]> getInteractionColumnColumnMap();

	public abstract HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey();
	
	public abstract void setInteractionColumnColumnMap(HashMap<String, String[]> map);

	public abstract void setAllTransformMap_valueKey(HashMap<String, HashMap<String, String>>  map);

}