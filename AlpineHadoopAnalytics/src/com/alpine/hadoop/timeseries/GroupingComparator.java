/**
 * 
 * ClassName GroupingComparator.java
 *
 * Version information: 1.00
 *
 * Date: Nov 5, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.timeseries;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
/**
* @author Shawn 
* 
*/
public class GroupingComparator extends WritableComparator {
	protected GroupingComparator() {
		super(LongSort.class, true);
	}
	@Override
	// Compare two WritableComparables.
	public int compare(WritableComparable w1, WritableComparable w2) {
		LongSort ip1 = (LongSort) w1;
		LongSort ip2 = (LongSort) w2;
		int l = ip1.getGroup().get();
		int r = ip2.getGroup().get();
		int cmp = (l==r ? 0 : (l<r?-1:1));
		return -cmp;//the sort of group   it works
	}
}
