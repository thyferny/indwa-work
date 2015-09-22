/**
 * 

* ClassName LogisticRegressionHadoopModel.java
*
* Version information: 1.00
*
* Date: 2012-9-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.models;

import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModel;
import com.alpine.datamining.operator.regressions.LoRModelIfc;

/**
 * @author Peter
 *
 *  
 */

public class LogisticRegressionHadoopModel extends AbstractHadoopModel implements LoRModelIfc{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6726163128694617442L;
	public static final String interceptString = "intercept:lr:alpine";
	public String specifyColumn=null;
	public String getSpecifyColumn() {
		return specifyColumn;
	}
	protected double[] beta = null;
    
    private double[] standardError = null;

	private double[] waldStatistic = null;
    
    private double[] zValue = null;
    
    private double[] pValue = null;
    
	private HashMap<String,HashMap<String,String>> allTransformMap_valueKey=new HashMap<String,HashMap<String,String>>();    
	
	private HashMap<String, String[]> interactionColumnColumnMap = new HashMap<String, String[]>();
	
	private Map<String, String[]> charColumnMap;
	
	public Map<String, String[]> getCharColumnMap() {
		return charColumnMap;
	}
	public void setCharColumnMap(Map<String, String[]> charColumnMap) {
		this.charColumnMap = charColumnMap;
	}
	protected String bad;


    public String getBad() {
		return bad;
	}
	public void setBad(String bad) {
		this.bad = bad;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getModelDeviance()
	 */
    @Override
	public double getModelDeviance() {
		return modelDeviance;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setModelDeviance(double)
	 */
	@Override
	public void setModelDeviance(double modelDeviance) {
		this.modelDeviance = modelDeviance;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getNullDeviance()
	 */
	@Override
	public double getNullDeviance() {
		return nullDeviance;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setNullDeviance(double)
	 */
	@Override
	public void setNullDeviance(double nullDeviance) {
		this.nullDeviance = nullDeviance;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getChiSquare()
	 */
	@Override
	public double getChiSquare() {
		return chiSquare;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setChiSquare(double)
	 */
	@Override
	public void setChiSquare(double chiSquare) {
		this.chiSquare = chiSquare;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getIteration()
	 */
	@Override
	public long getIteration() {
		return iteration;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setIteration(long)
	 */
	@Override
	public void setIteration(long iteration) {
		this.iteration = iteration;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getBeta()
	 */
	@Override
	public double[] getBeta() {
		return beta;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setBeta(double[])
	 */
	@Override
	public void setBeta(double[] beta) {
		this.beta = beta;
	}
	private double modelDeviance = Double.NaN;
    
    private double nullDeviance = Double.NaN;
    
    private double chiSquare = Double.NaN;
    
    private String[] columnNames;
    
	protected String good = null;
    
    private long iteration = 20;
	private boolean improvementStop= false; 


 	public LogisticRegressionHadoopModel(String [] columnNames,String specifyColumn, double[] beta, double[] variance,String goodValue){
 		this.good = goodValue;
        this.beta = beta;
        this.columnNames=columnNames;
        this.specifyColumn=specifyColumn;
        
        standardError = new double[variance.length];
        waldStatistic = new double[variance.length];
        zValue = new double[variance.length];
        pValue = new double[variance.length];
        for (int j = 0; j < beta.length; j++) {
        	standardError[j] = Math.sqrt(variance[j]);
        	waldStatistic[j] = beta[j] * beta[j] / variance[j];
        	zValue[j] = beta[j]/standardError[j];
        	pValue[j] = norm(zValue[j]);
        }
	}
 	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getStandardError()
	 */
 	@Override
	public double[] getStandardError() {
		return standardError;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setStandardError(double[])
	 */
	@Override
	public void setStandardError(double[] standardError) {
		this.standardError = standardError;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getWaldStatistic()
	 */
	@Override
	public double[] getWaldStatistic() {
		return waldStatistic;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setWaldStatistic(double[])
	 */
	@Override
	public void setWaldStatistic(double[] waldStatistic) {
		this.waldStatistic = waldStatistic;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getzValue()
	 */
	@Override
	public double[] getzValue() {
		return zValue;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setzValue(double[])
	 */
	@Override
	public void setzValue(double[] zValue) {
		this.zValue = zValue;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getpValue()
	 */
	@Override
	public double[] getpValue() {
		return pValue;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setpValue(double[])
	 */
	@Override
	public void setpValue(double[] pValue) {
		this.pValue = pValue;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getColumnNames()
	 */
	@Override
	public String[] getColumnNames() {
		return columnNames;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setColumnNames(java.lang.String[])
	 */
	@Override
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#getGood()
	 */
	@Override
	public String getGood() {
		return good;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.models.LoRModelIfc#setGood(java.lang.String)
	 */
	@Override
	public void setGood(String good) {
		this.good = good;
	}
	double norm(double z) {
		z=Math.abs(z);
		double p=1 + z*(0.04986735+ z*(0.02114101+ z*(0.00327763+ z*(0.0000380036+ z*(0.0000488906+ z*0.000005383)))));
		p=p*p; p=p*p; p=p*p;
		return 1/(p*p);
	}

	public boolean isImprovementStop() {
		return improvementStop;
	}
	public void setImprovementStop(boolean improvementStop) {
		this.improvementStop = improvementStop;
		
	    

	}
    public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}
	public void setAllTransformMap_valueKey(
			HashMap<String, HashMap<String, String>> allTransformMapValueKey) {
		allTransformMap_valueKey = allTransformMapValueKey;
	}
	
	public HashMap<String, String[]> getInteractionColumnColumnMap() {
		return interactionColumnColumnMap;
	}

	public void setInteractionColumnColumnMap(
			HashMap<String, String[]> interactionColumnColumnMap) {
		this.interactionColumnColumnMap = interactionColumnColumnMap;
	}
	
	public double[] getOddsArrays()
	{
		double[] dou=new double[beta.length-1];
		for(int i=0;i<beta.length - 1;i++)
		{
			dou[i] = Math.exp(beta[i]);
		}
		return dou;
	}
}
