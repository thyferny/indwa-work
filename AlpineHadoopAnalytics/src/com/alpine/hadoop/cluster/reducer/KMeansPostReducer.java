/**
 * ClassName SplitOutputReducer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author john
 *
 */
public class KMeansPostReducer extends
		Reducer<LongWritable, DoubleArrayWritable, Text, DoubleArrayWritable> {

	@Override
	protected void reduce(LongWritable key,
			Iterable<DoubleArrayWritable> values,
			Context context)
			throws IOException, InterruptedException {
		if(key.get()==-1){
			handleTottalDistance(values,context);
		}
		else{
			handleSplitAndScatter(key, values, context);
		}
	 
	}

	private void handleSplitAndScatter(LongWritable key,
			Iterable<DoubleArrayWritable> values, Context context)
			throws IOException, InterruptedException {
		Configuration configuration = context.getConfiguration();
		
		String[] columns = configuration.get(KmeansConfigureKeySet.COLUMNS).split(",");
		int columnCount=columns.length;
		
		String splitNumber = configuration.get(KmeansConfigureKeySet.SPLIT_VALUE);
		
		int splitValue = Integer.parseInt(splitNumber);
		
		List<Double> minValueList = new ArrayList<Double>();
		List<Double> maxValueList = new ArrayList<Double>();
		for(int i=0;i<columnCount;i++){
			String minMax = configuration.get("alpine.column."+i);
			String[] minMaxArray = minMax.split(",");
			String min=minMaxArray[0];
			String max=minMaxArray[1];
			minValueList.add(Double.parseDouble(min));
			maxValueList.add(Double.parseDouble(max));
		}
		
		//init count list
		List<Long> countList=new ArrayList<Long>();
		for(int i=0;i<splitValue*columnCount;i++){
			countList.add(0l);
		}
		int limitLine = Integer.parseInt(configuration.get(KmeansConfigureKeySet.LIMIT_LINE));

		long scatterCount=0l;
		for(DoubleArrayWritable daw : values) {//for each line
			Writable[] doubleArray=daw.get();
			for(int i=0;i<doubleArray.length;i++){
				double value = ((DoubleWritable)doubleArray[i]).get();
				int intervalNumber=calculateWhichInterval(minValueList.get(i),maxValueList.get(i),splitValue,value);
				int offset = i*splitValue+intervalNumber;
				Long count = countList.get(offset);
				countList.set(offset, count+1);
				
				
			}
			
			//handle scatter point ----------------------
			if(scatterCount<limitLine){
				context.write(new Text(KmeansConfigureKeySet.KEY_SCATTER_POINTS+"_"+key), daw);
				scatterCount++;
			}	
		}
		Writable[] doubleArrayValues=new Writable[countList.size()];
		for(int i=0;i<countList.size();i++){
			doubleArrayValues[i]=new DoubleWritable(countList.get(i));
		}
		DoubleArrayWritable doubleArrayWritable = new DoubleArrayWritable();
		
		doubleArrayWritable.set(doubleArrayValues);
		context.write(new Text(KmeansConfigureKeySet.KEY_SPLIT_OUTPUT+"_"+key), doubleArrayWritable);
	}

	private void handleTottalDistance(Iterable<DoubleArrayWritable> values,
			 Context context) throws IOException, InterruptedException {
		double sum = 0.0;
		long lineNumber = 0;
		for(DoubleArrayWritable vec : values) {
			sum=sum+ ((DoubleWritable)vec.get()[0]).get();
			lineNumber=lineNumber+1;
	    }	
		 
		DoubleArrayWritable sumResult = new DoubleArrayWritable();
		DoubleWritable[] sumArray = new DoubleWritable[]{new DoubleWritable(sum),new DoubleWritable(lineNumber)}; 
		sumResult.set(sumArray) ;
		context.write(new Text(KmeansConfigureKeySet.KEY_TOTAL_DISTANCE), sumResult);
		
	}

	private int calculateWhichInterval(Double min, Double max,
			int splitValue, double value) {
		double step = (max-min)/(splitValue*1.0);
		List<Double> intervalList=new ArrayList<Double>();
		intervalList.add(min);
		double currentValue=min;
		for(int i=0;i<splitValue;i++){
			if(i==splitValue-1){
				intervalList.add(max);
			}else{
				currentValue=currentValue+step;
				intervalList.add(currentValue);
			}	
		}
		for(int i=0;i<intervalList.size();i++){
			currentValue=intervalList.get(i);
			if(i<intervalList.size()-1){
				double nextValue=intervalList.get(i+1);
				if(value<=nextValue&&nextValue>currentValue){
					return i;
				}
			}else{
				return intervalList.size()-2;
			}
		}
		return 0;
	}
}
