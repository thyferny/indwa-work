/**
 * 
 * ClassName ColumnDistinct.java
 *
 * Version information: 1.00
 *
 * Date: Dec 20, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.utily.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
/**
* @author Peter 
* 
*/
public class ColumnDistinct implements WritableComparable<ColumnDistinct> {
	IntWritable columnId;
	Text value;
 
	
	public ColumnDistinct(){
		this.columnId=new IntWritable();
		this.value=new Text();
 
	}
	public ColumnDistinct(int columnId,String value){
		this.columnId=new IntWritable(columnId);
		this.value=new Text(value);
	}
	
	public void set(int columnId,String value) {
		this.columnId.set(columnId);
		this.value.set(value);
	}
 
	@Override
	public void readFields(DataInput in) throws IOException {
		columnId.readFields(in);
		value.readFields(in);
 
	}
	@Override
	public void write(DataOutput out) throws IOException {
		columnId.write(out);
		value.write(out);
	}
	
	@Override
	public int compareTo(ColumnDistinct o) {
		if(this.columnId.compareTo(o.columnId) ==0)  
            return -this.value.toString().compareTo(o.value.toString());  
        else return this.columnId.compareTo(o.columnId);  
	}
	@Override
	public int hashCode() {
		return  (int) (value.toString().hashCode()%127+columnId.get());
	}
	@Override
	public boolean equals(Object right) {
		if (right == null)
			return false;
		//never use the object id equal,because we will use same object for more java space saving
//		if (this == right)
//			return true;
		if (right instanceof ColumnDistinct) {
			ColumnDistinct r = (ColumnDistinct) right;
			return columnId.get()==r.columnId.get()
					&&value.toString().equals(r.value.toString());//same column same value should be distinct
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return "Column Index:"+columnId.get() + ",Value:" + value.toString();
	}

}


