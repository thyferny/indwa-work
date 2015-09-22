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
public interface UnionKeySet extends AlpineHadoopConfKeySet{
	// filea,fileb,filec
	public static String union_input_files = "alpine.union.inputFiles";
	public static String union_type = "alpine .union.type";
	public static String union_first_table = "alpine.union.firstTable";
	public static String union_input_column_index = "alpine.union.inputColumns";
	public static String union_input_ids = "alpine.union.ids";
	public static String union_input_real_files = "union.input.real.files";
	public static String union_input_headerline =  "union.input.headerline";
}

