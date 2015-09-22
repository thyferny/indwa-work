/**
 * 

* ClassName RocKeySet.java
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
public interface RocKeySet extends AlpineHadoopConfKeySet{
	public static String piIndex="alpinet.roc.piIndex";
	public static String max_roc_points="alpinet.max_roc_points";
	public static String max_roc_probability="alpine.max_roc_probability";
	public static String min_roc_probability="alpinet.min_roc_probability";
	public static String dependent="alpine.logistic.dependent";
	public static String good="alpine.logistic.good";
}

