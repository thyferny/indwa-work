/**
 * ClassName KmeansConfigureKeySet.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-12
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop;


/**
 * @author Jeff Dong
 *
 */
public class KmeansConfigureKeySet implements AlpineHadoopConfKeySet{
	public static final String ID_FIELD = "alpine.kmeans.idColumnIndex";
	public static final String OUTPUT_RANDOM_POINTS = "alpine.kmeans.random_points"; 
	public static final String OUTPUT_CENTER_POINTS = "alpine.kmeans.cent_points"; 

 
	public static final String LIMIT_LINE="alpine.kmeans.limitLine";
	public static final String COLUMNS="alpine.kmeans.columns";
	public static final String DELIMITER="alpine.kmeans.delimiter";
	public static final String K="alpine.kmeans.k";
	public static final String OUTPUT_ASSIGNMENTS="outputAssignments";
	public static final String ITERATION="alpine.kmeans.iteration";
	public static final String CENTRIODS="alpine.kmeans.centroids";
	public static final String DISTANCE_TYPE="alpine.kmeans.distanceMeasure";
	public static final String SPLIT_VALUE="alpine.kmeans.splitValue";
	
	public static final	String KEY_MAX_MIN = "alpine.kmeasn.maxmin";
	public static final String KEY_RANDOM_POINT = "alpine.kmeasn.randomPoints";
	public static final String KEY_SCATTER_POINTS = "alpine.kmeasn.scatterPoints";
	public static final String KEY_SPLIT_OUTPUT =  "alpine.kmeasn.splitOutPut";
	public static final String KEY_TOTAL_DISTANCE = "alpine.kmeasn.totalDistance";
 
}
