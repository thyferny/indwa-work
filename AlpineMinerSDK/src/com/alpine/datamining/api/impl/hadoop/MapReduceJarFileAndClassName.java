/**
 * ClassName MapReduceJarFileAndClassName.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

/**
 * 
 * @author Eason
 *
 */

public class MapReduceJarFileAndClassName {
	public static final String[] ProductRecommendation = new String []{"ProductRecommendation.jar", "com.alpine.datamining.mapreduce.productrecommendation.ProductRecommendationGroup", "com.alpine.datamining.mapreduce.productrecommendation.ProductRecommendationOutput"};
	public static final String[] KNN = new String []{"KNN.jar", "com.alpine.datamining.mapreduce.knn.LuceneKnnWordSegment", "com.alpine.datamining.mapreduce.knn.KnnCalculateDistance"};
	// just one jar file but different class name
}
