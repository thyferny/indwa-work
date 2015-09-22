/**
 * 

* ClassName LogisticConfigureKeySet.java
*
* Version information: 1.00
*
* Date: Aug 29, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop;

/**
 * @author Peter
 *
 *  
 */
public interface LogisticConfigureKeySet extends AlpineHadoopConfKeySet{
	public static String dependent="alpine.logistic.dependent";
	public static String columns="alpine.logistic.columns";
	public static String columnTyps="alpine.logistic.columnTypes";
	public static String interactionItems="alpine.logistic.interactionItems";
	public static String beta="alpine.logistic.beta";
	public static String dependent_avg="alpine.logistic.dependent.avg";
	public static String coefficients="alpine.logistic.coefficients";
	public static String covariance="alpine.logistic.covariance";
	public static String totalnumber="alpine.logistic.totalnumber";
	public static String linenumber="alpine.logistic.linenumber";
	public static String good="alpine.logistic.good";
	public static String iteratorCount="alpine.logistic.iteratorCount";
	public static String fitness="alpine.logistic.fitness";
	public static String positive="alpine.logistic.positive";
	public static String hessian="alpine.logistic.hessian";
	public static String derivative="alpine.logistic.derivative";
	public static String variance="alpine.logistic.variance";
	public static String max_roc_points="alpine.logist.max_roc_points";
	public static String max_roc_probability="alpine.logist.max_roc_probability";
	public static String min_roc_probability="alpine.logist.min_roc_probability";
	public static String immunity="alpine.logist.immunity";
	public static String bad="alpine.logist.bad";
}

