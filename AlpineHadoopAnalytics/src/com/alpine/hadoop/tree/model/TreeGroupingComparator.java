package com.alpine.hadoop.tree.model;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
/**
* @author Shawn 
* 
*/
public class TreeGroupingComparator extends WritableComparator {
	protected TreeGroupingComparator() {
		super(SplitRecordWritable.class, true);
	}
	@Override
	// Compare two WritableComparables.
	public int compare(WritableComparable w1, WritableComparable w2) {
		SplitRecordWritable ip1 = (SplitRecordWritable) w1;
		SplitRecordWritable ip2 = (SplitRecordWritable) w2;
		int l = ip1.getColumnIndex().get();
		int r = ip2.getColumnIndex().get();
		int cmp = (l==r ? 0 : (l<r?-1:1));
		return -cmp;//the sort of group   it works
	}
}