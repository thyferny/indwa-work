/**
 * 

* ClassName LinearRegressionHadoopModel.java
*
* Version information: 1.00
*
* Date: 2012-8-20
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModel;
import com.alpine.datamining.utility.Tools;

/**
 * @author Shawn
 *
 *  
 */

public class LinearRegressionHadoopModel extends AbstractHadoopModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3744318460447231710L;

	private List<String> realColumnNames;
	
	private String[] columnNames;
	
	private String specifyColumn;

	private String errorString;
	
	private List<double[]> residuals;
		
	private HashMap<String,HashMap<String,String>> allTransformMap_valueKey=new HashMap<String,HashMap<String,String>>(); 
	protected HashMap<String, String> interactionColumnExpMap = new HashMap<String, String>();
	private HashMap<String, String[]> interactionColumnColumnMap = new HashMap<String, String[]>();

	private Map<String, String[]> charColumnMap;
	
	public Map<String, String[]> getCharColumnMap() {
		return charColumnMap;
	}
	public void setCharColumnMap(Map<String, String[]> charColumnMap) {
		this.charColumnMap = charColumnMap;
	}

	public HashMap<String, String[]> getInteractionColumnColumnMap() {
		return interactionColumnColumnMap;
	}

	public void setInteractionColumnColumnMap(
			HashMap<String, String[]> interactionColumnColumnMap) {
		this.interactionColumnColumnMap = interactionColumnColumnMap;
	}

	public HashMap<String, String> getInteractionColumnExpMap() {
		return interactionColumnExpMap;
	}

	public void setInteractionColumnExpMap(
			HashMap<String, String> interactionColumnExpMap) {
		this.interactionColumnExpMap = interactionColumnExpMap;
	}

	public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}

	public void setAllTransformMap_valueKey(
			HashMap<String, HashMap<String, String>> allTransformMapValueKey) {
		allTransformMap_valueKey = allTransformMapValueKey;
	}


	protected LinkedHashMap<String,Double> coefficientsMap;
	
	protected Double[] coefficients;
	
	private double[] se;

	private double[] t;

	private double[] p;
	
	private double r2;
	
	private double s;
	protected String predictedLabelName;

 	public LinearRegressionHadoopModel(String [] columnNames,String specifyColumn,Double[] coefficients, Map<String, Double> coefficientmap){
		this.columnNames = columnNames;
		this.specifyColumn=specifyColumn;
		this.coefficients=coefficients;
		this.coefficientsMap=(LinkedHashMap<String, Double>) coefficientmap;
		if (coefficients != null)
		{
			se = new double[coefficients.length];
			t = new double[coefficients.length];
			p = new double[coefficients.length];
			for (int i = 0 ; i < coefficients.length; i++)
			{
				se[i] = Double.NaN;
				t[i] = Double.NaN;
				p[i] = Double.NaN;
			}
		}
	}
	
	public HashMap<String, Double> getCoefficientsMap() {
		return coefficientsMap;
	}

	public void setCoefficientsMap(LinkedHashMap<String, Double> coefficientsMap) {
		this.coefficientsMap = coefficientsMap;
	}

	public String getSpecifyColumn() {
		return specifyColumn;
	}

	public void setSpecifyColumn(String specifyColumn) {
		this.specifyColumn = specifyColumn;
	}
	
	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		if(this.errorString==null)
		{
			this.errorString = errorString;
		}else
		{
			this.errorString += errorString;
		}
		
	}
	
	/**
	 * @return the coefficients
	 */
	public Double[] getCoefficients() {
		return coefficients;
	}

	/**
	 * @param coefficients the coefficients to set
	 */
	public void setCoefficients(Double[] coefficients) {
		this.coefficients = coefficients;
	}

	/**
	 * @return the se
	 */
	public double[] getSe() {
		return se;
	}

	/**
	 * @param se the se to set
	 */
	public void setSe(double[] se) {
		this.se = se;
	}

	/**
	 * @return the t
	 */
	public double[] getT() {
		return t;
	}

	/**
	 * @param t the t to set
	 */
	public void setT(double[] t) {
		this.t = t;
	}

	/**
	 * @return the p
	 */
	public double[] getP() {
		return p;
	}

	/**
	 * @param p the p to set
	 */
	public void setP(double[] p) {
		this.p = p;
	}
	public double getR2() {
		return r2;
	}
	/**
	 * @param r2 the r2 to set
	 */
	public void setR2(double r2) {
		this.r2 = r2;
	}

	/**
	 * @return the s
	 */
	public double getS() {
		return s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(double s) {
		this.s = s;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		if(getErrorString()!=null)
		{
			result.append(getErrorString());
		}
//		result.append(getLabel().getName()+ " = ");//TODO
		for (int i = 0; i < columnNames.length; i++) {
				result.append(getCoefficientString(coefficients[index], first) + " * " + columnNames[i]);
				index++;
				first = false;
		}
		result.append(getCoefficientString(coefficients[coefficients.length - 1], first)+Tools.getLineSeparator());
		result.append(Tools.getLineSeparator());
		result.append("R2: "+getR2());
		result.append(Tools.getLineSeparator());

		if (Double.isNaN(getS()))
		{
			result.append(Tools.getLineSeparator());
			result.append("data size too small!");
			result.append(Tools.getLineSeparator());
			return result.toString();
		}
		result.append("Standard Error: "+getS());
		result.append(Tools.getLineSeparator());
    	result.append("Coefficients:" + Tools.getLineSeparator());
		result.append("Intercept: \t"+coefficients[columnNames.length]+"\tSE: "+se[columnNames.length] +"\tT-statistics: "+ t[columnNames.length] +"\tP-value: "+ p[columnNames.length]+Tools.getLineSeparator());
		for (int i = 0; i < columnNames.length; i++) {
			result.append("coefficient("+columnNames[i]+"): "+coefficients[i]+"\tSE: "+se[i] +"\tT-statistics: "+ t[i] +"\tP-value: "+ p[i]+Tools.getLineSeparator());
		}
		return result.toString();
	}
	public String getPredictedLabelName() {
		return predictedLabelName;
	}

	public void setPredictedLabelName(String predictedLabelName) {
		this.predictedLabelName = predictedLabelName;
	}

	public String getCoefficientString(double coefficient, boolean first) {
		if (!first) {
			if (coefficient >= 0)
				return " + " + Math.abs(coefficient);
			else
				return " - " + Math.abs(coefficient);
		} else {
			if (coefficient >= 0)
				return Double.toString(Math.abs(coefficient));
			else
				return " - " + Math.abs(coefficient);
		}
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	
	public List<String> getRealColumnNames() {
		return realColumnNames;
	}

	public void setRealColumnNames(List<String> realColumnNames) {
		this.realColumnNames = realColumnNames;
	}

	public void addResidual(double[] data){
		if(residuals==null){
			residuals=new ArrayList<double[]>();
		}
		residuals.add(data);
	}
	
	public List<double[]> getResiduals(){
		return residuals;
	}
}
