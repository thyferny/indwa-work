/**
 * 

* ClassName LinearConfigureKeySet.java
*
* Version information: 1.00
*
* Date: Aug 29, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop;

/**
 * @author Shawn,Peter
 *
 *  
 */
public interface LinearConfigureKeySet extends AlpineHadoopConfKeySet{
	public static String dependent="alpine.linear.dependent";
	public static String columns="alpine.linear.columns";
	public static String interactionItems="alpine.linear.interactionItems";
	public static String beta="alpine.linear.beta";
	public static String dependent_avg="alpine.linear.dependent.avg";
	public static String coefficients="alpine.linear.coefficients";
	public static String covariance="alpine.linear.covariance";
	public static String totalnumber="alpine.linear.totalnumber";
	public static String linenumber="alpine.linear.linenumber";
	public static String r2="alpine.linear.r2";
	public static String s="alpine.linear.s";
	public static String dof="alpine.linear.dof";
	public static String se="alpine.linear.se";
	public static String t="alpine.linear.t";
	public static String p="alpine.linear.p";
}
