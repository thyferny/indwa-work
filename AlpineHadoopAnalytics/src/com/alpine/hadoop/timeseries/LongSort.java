
package com.alpine.hadoop.timeseries;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;

public class LongSort implements WritableComparable<LongSort> {
	LongWritable sortKey;
	IntWritable group;
 
	
	public LongSort(){
		this.sortKey=new LongWritable();
		this.group=new IntWritable();
 
	}
	public LongSort(long sortKey,int group){
		this.sortKey=new LongWritable(sortKey);
		this.group=new IntWritable(group);
 
		
	}
	
	public void set(long sortKey,int group) {
		this.sortKey.set(sortKey);
		this.group.set(group);
 
	}
	public IntWritable getGroup() {
		return group;
	}
	public void setGroup(IntWritable group) {
		this.group = group;
	}
	public LongWritable getSortKey() {
		return sortKey;
	}
	public void setSortKey(LongWritable key) {
		this.sortKey = key;
	}
 
	@Override
	public void readFields(DataInput in) throws IOException {
		sortKey.readFields(in);
		group.readFields(in);
 
	}
	@Override
	public void write(DataOutput out) throws IOException {
		sortKey.write(out);
		group.write(out);
 
	}
	
	
 
	
	
	@Override
	public int compareTo(LongSort o) {
		if(this.group.compareTo(o.group) ==0)  
            return -this.sortKey.compareTo(o.sortKey);  
        else return this.group.compareTo(o.group);  
	}
	@Override
	public int hashCode() {
		return  group.get();
	}
	@Override
	public boolean equals(Object right) {
		if (right == null)
			return false;
		if (this == right)
			return true;
		if (right instanceof LongSort) {
			LongSort r = (LongSort) right;
//			return r.sortKey == sortKey&&r.group==group;  
			//TODO   will discuss with Peter later
			return r.group==group;
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return "sortKey:"+sortKey.get() + ",group :" + group.get();
	}

}


