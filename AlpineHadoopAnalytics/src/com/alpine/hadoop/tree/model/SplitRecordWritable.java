/**
 * 

* ClassName SplitRecordWritable.java
*
* Version information: 1.00
*
* Date: Nov 28, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.tree.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;


/**
 * @author Shawn
 *
 *  
 */

public class SplitRecordWritable implements WritableComparable<SplitRecordWritable>{
	IntWritable columnIndex;
	BooleanWritable isNumeric;
//	DoubleWritable  dataValue;
	Text 			textValue;
	
	
	public SplitRecordWritable(){
		this.columnIndex=new IntWritable();
		this.isNumeric  =new BooleanWritable(false);
//		this.dataValue = new DoubleWritable();
		this.textValue=new Text("");
	}
	
	
	public SplitRecordWritable(int columnIndex,Text value){
		this.columnIndex=new IntWritable(columnIndex);
		 
			this.isNumeric=new BooleanWritable(false);
//			this.dataValue=new DoubleWritable(0);
			this.textValue=new Text(value);
 
	}
	
	
	public SplitRecordWritable(int columnIndex,Text value,boolean isNumeric ){
		this.columnIndex=new IntWritable(columnIndex);
		 
			this.isNumeric=new BooleanWritable(isNumeric);
//			this.dataValue=new DoubleWritable(0);
			this.textValue=new Text(value);
 
	}
	
//	public SplitRecordWritable(int columnIndex,double value){
//		this.columnIndex=new IntWritable(columnIndex);
//		 
//		this.isNumeric=new BooleanWritable(true);
//			 
////		this.dataValue=new DoubleWritable(value);
//		this.textValue=new Text(" ");
// 
//	}
	
	
	
	@Override
	public void write(DataOutput out) throws IOException {
		columnIndex.write(out);
//		
		isNumeric.write(out);
		 
//			dataValue.write(out);
		 
			textValue.write(out);
		 
		
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		try{
		columnIndex.readFields(in);
		isNumeric.readFields(in);
		 
			
//			dataValue.readFields(in);
		 
			textValue.readFields(in);
		}
		catch(Exception e)
		{
			int fuck=1;
			System.out.println(e);
		}
		
	}
 
//	@Override
//	public int compareTo(SplitRecordWritable o) {
//		if(this.columnIndex.compareTo(o.columnIndex)==0)
//		{
//			if(this.isNumeric.get()==true)
//			{
//				return this.dataValue.compareTo(o.dataValue);
//			}
//			else
//			{
//				return 0;
//			}
//		}
//		else return this.columnIndex.compareTo(o.columnIndex);
//	}
	
	
	@Override
	public boolean equals(Object right) {
		if (right == null)
			return false;
		if (this == right)
			return true;
		if (right instanceof SplitRecordWritable) {
			SplitRecordWritable r = (SplitRecordWritable) right;
//			return r.sortKey == sortKey&&r.group==group;  
			//TODO   will discuss with Peter later
			return r.columnIndex==columnIndex;
		} else {
			return false;
		}
	}
	
	
	
	public IntWritable getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(IntWritable columnIndex) {
		this.columnIndex = columnIndex;
	}
	public BooleanWritable getIsNumeric() {
		return isNumeric;
	}
	public void setIsNumeric(BooleanWritable isNumeric) {
		this.isNumeric = isNumeric;
	}
//	public DoubleWritable getDataValue() {
//		return dataValue;
//	}
//	public void setDataValue(DoubleWritable dataValue) {
//		this.dataValue = dataValue;
//	}
	public Text getTextValue() {
		return textValue;
	}
	public void setTextValue(Text textValue) {
		this.textValue = textValue;
	}


	@Override
	public int compareTo(SplitRecordWritable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	 
}
