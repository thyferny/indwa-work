package com.alpine.hadoop.cluster.mapper;
/**
 * 
 * ClassName TextGroupingComparator.java
 *
 * Version information: 1.00
 *
 * Date: Nov 29, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
/**
* @author Peter 
* 
*/
public class TextGroupingComparator extends WritableComparator {
	protected TextGroupingComparator() {
		super(Text.class, true);
	}
	@Override
	// Compare two WritableComparables.
	public int compare(WritableComparable w1, WritableComparable w2) {
		Text ip1 = (Text) w1;
		Text ip2 = (Text) w2;
		return ip1.compareTo(ip2);//the sort of group   it works
	}
}