/**
 * ClassName AggregatorReducer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-4
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author john
 * 
 */
public class KMeansInitReducer extends
Reducer<Text, DoubleArrayWritable, Text, DoubleArrayWritable> {

	private int k;  
	private ArrayList<DoubleArrayWritable> points = new ArrayList<DoubleArrayWritable>();

	@Override
	protected void reduce(Text key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		int count = 0;
//		int batchCount = 0;
		
		if("linenumbercount".equals(key.toString())){
			Double sum=0.0;
			for(DoubleArrayWritable next : values) {
				sum=sum+((DoubleWritable)(next.get()[0])).get();
			}
			long linecount=sum.longValue();
			if(linecount<this.k){
				context.write(new Text("error1"), null);
				return;
			}
		}else if(key.toString().equals(KmeansConfigureKeySet.KEY_RANDOM_POINT)) {
			
			try {
				//random points calculation
				for(DoubleArrayWritable next : values) {
					count++;
//					batchCount++;
					
					if(count <= this.k){
						
						// first k elements of iterator
						points.add(next.clone());
						
//						stagingArray.add(next.clone());
//						continue;
					} 
//					else if(batchCount <= this.k) {
						
//						// batch of k-elements from each mapper
//						stagingArray.add(next.clone());
//						continue;
//					} else {
//						
//						// randomly choose one item from each mapper
//						batchCount = 1;
//						collectedArray.add(stagingArray.get(gen.nextInt(stagingArray.size())).clone());
//						stagingArray = new ArrayList<DoubleArrayWritable>();
//						gen = new Random(System.currentTimeMillis());
//						stagingArray.add(next.clone());
//					}
				}	

//				gen = new Random(System.currentTimeMillis());
//				
//				if(collectedArray.size() < k) {
//					collectedArray.addAll(stagingArray);
//				}
//
//				for(int i = 0; i < this.k; i++) {
//					points.set(i, collectedArray.remove(gen.nextInt(collectedArray.size())));
//				}
				
				writeRandomPoint(context);
			} catch (Exception e) {
				context.write(new Text(e.getLocalizedMessage()), null);
			}

			// random points calculation
//			for(DoubleArrayWritable next : values) {
//				count++;
//
//				System.out.println("Randoms :" + ((Writable[]) next.get())[1].toString());
//				//random point --------------------------------------
//				if (points.size() >= this.k) {
//					System.out.println("Count : " + count);
//					if(gen.nextInt(count) == 0){
//						System.out.println("Replacing on count : " + count);
//						points.set(gen.nextInt(this.k), next.clone());
//					}
//				} else {
//					points.add(next.clone());
//				}
//			}
		}
		else {

			try {
				Double min=Double.MAX_VALUE;
				Double max=Double.MIN_VALUE;

				for(DoubleArrayWritable next : values) {

					//max min  --------------------------------------
					Writable[] doubleArray = next.get();

					double testMin = ((DoubleWritable) doubleArray[0]).get();
					double testMax = ((DoubleWritable) doubleArray[1]).get();

					if(testMin<min){
						min = testMin;
					}

					if(testMax>max){
						max = testMax;
					}
				}

				writeMaxMin(context, min, max, key);
			} catch (Exception e) {
				context.write(new Text(e.getLocalizedMessage()), null);
			}
		}
	}


	private void writeRandomPoint(Context context) throws IOException,
	InterruptedException {
		for(int i=0;i<this.k;i++) {
			context.write(new Text(KmeansConfigureKeySet.KEY_RANDOM_POINT+String.valueOf(i)), points.get(i));
		}
	}

	private void writeMaxMin(Context context, double min,
			double max, Text key) throws IOException, InterruptedException {

		DoubleArrayWritable doubleArrayWritable=new DoubleArrayWritable();
		Writable[] doubleArray=new DoubleWritable[2];
		doubleArray[0]=new DoubleWritable(min);
		doubleArray[1]=new DoubleWritable(max);
		doubleArrayWritable.set(doubleArray);
		context.write(key, doubleArrayWritable);
	}

	protected void setup(Context context) {
		this.k = new Integer(context.getConfiguration().get(KmeansConfigureKeySet.K));
	}
}
