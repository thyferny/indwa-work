/**
 * ClassName Row.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import java.util.Arrays;

/**
 * @author John Zhao
 * 
 */
public class DataRow {
	int sequence;//row number
	String[] datas =null;

	public DataRow(String[] data){
		this.datas=data;
	}
	public DataRow(){
	}
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String[] getData() {
		return datas;
	}

	public void setData(String[] data) {
		this.datas = data;
	}

	public String getData(int index) {
		if (datas != null) {
			return datas[index];
		} else {
			return null;
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataRow [sequence=");
		builder.append(sequence);
		builder.append(", datas=");
		builder.append(Arrays.toString(datas));
		builder.append("]");
		return builder.toString();
	}
	
}
