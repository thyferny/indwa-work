/**
 * ClassName SplitOutputMapper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.cluster.util.distance.Distance;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Jeff Dong
 *
 */
public class KMeansPostMapper extends Mapper<LongWritable, Text, LongWritable, DoubleArrayWritable> {
	//last -1 column   -> cluster
	//last column2   ->id (optional)
	
	private int idColumnIndex = -1;//-1 means no id column
	private Distance dm;
	private Map<Long, Writable[]> centroids = new HashMap<Long, Writable[]>();
	private String delimiter= ","; //should always use "," for output
	MapReduceHelper helper;
	DoubleArrayWritable distanceResult = new DoubleArrayWritable();
	private LongWritable outKey  = new LongWritable(-1); 

	@Override
	protected void map(LongWritable key, Text value,Context context)
			throws IOException, InterruptedException {
		Writable[] values = getKmeansColumnValueArray(value, idColumnIndex);
		if(values!=null)
		{
			DoubleArrayWritable daw = new DoubleArrayWritable();
	
			daw.set(values);
			String[] valueArray = value.toString().split(delimiter) ;
			String clusterNumber = 	  valueArray[valueArray.length-1];
			context.write(new LongWritable(Long.parseLong(clusterNumber.trim())), daw);
			
			//handle total distance ...
			
			double totalDistance=0.0;
	
			try {
				// Compute Distance between vec and each other centroid
				for (Map.Entry<Long, Writable[]> cent : this.centroids
						.entrySet()) {
 				 	if(clusterNumber.equals( cent.getKey().toString())){
						Writable[] point = cent.getValue();
						double dist = dm.<Writable> compute(values, point);
						
						totalDistance=totalDistance+dist;
 						
 					 }
				
				}
			} catch (Exception e) {
				System.out.println("Null or non-numeric value encountered! Skipping line " 
						+ key.toString() + " of input file.");
				return;
			}
			
			distanceResult.set(new DoubleWritable[]{new DoubleWritable(totalDistance)});
			context.write(outKey,distanceResult );
		}
	}

	 

	public   Writable[] getKmeansColumnValueArray(Text value,
			int idColumnIndex) {
		try{
			String[] doubleArray = value.toString().split(delimiter);
			int delta = 1;//filter id 
			
			if(idColumnIndex>=0){
				delta = 2;
			}
			int realWidth =doubleArray.length - delta;
			
			Writable[] values = new Writable[realWidth];
			for (int i = 0; i < values.length; i++) {
				values[i] = new DoubleWritable(Double.parseDouble(doubleArray[i].trim()));
			}
			return values;
		}catch (Exception e) {
			return null ;
		}
	}
	
	protected void setup(Context context) {
		
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		idColumnIndex=helper.getDependentId(KmeansConfigureKeySet.ID_FIELD);
		
		
		try {
			String dist = context.getConfiguration().get(
					KmeansConfigureKeySet.DISTANCE_TYPE);
			Class<?> klass = Class.forName(dist);
			this.dm = (Distance) klass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not initialize Distance Measure");
		}
 
		int k = helper.getConfigInt(KmeansConfigureKeySet.K);

		for (int i = 0; i < k; i++) {
			String centroidString = helper.getConfigString(KmeansConfigureKeySet.CENTRIODS
					+ "." + i);
			if(centroidString==null||centroidString.trim().length()==0){
				continue;
			}
			String[] centroidArray = centroidString.split(",");
			Writable[] centroidValueArray = new Writable[centroidArray.length];
			for (int j = 0; j < centroidArray.length; j++) {
				centroidValueArray[j] = new DoubleWritable(
						Double.parseDouble(centroidArray[j]));
			}
			centroids.put(Long.valueOf(i), centroidValueArray);
		}
	 
	 
	}

}
