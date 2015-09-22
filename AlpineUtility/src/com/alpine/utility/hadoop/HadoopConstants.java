/**
 * ClassName  HadoopConstants
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author John Zhao
 */
public class HadoopConstants {
		public static final String VERSION_APACHE_HADOOP_0_20_2 = "Apache Hadoop 0.20.2";
		public static final String VERSION_0_20_2_CDH3_U4 = "Cloudera CDH3 Update 4";
	
		public static final String VERSION_APACHE_0_20_203 = "Apache Hadoop 0.20.203" ;
		public static final String VERSION_APACHE_1_0_2 ="Apache Hadoop 1.0.2";
		public static final String VERSION_APACHE_1_0_4 ="Apache Hadoop 1.0.4";


		public static final String VERSION_CDH_4_1_1 ="Cloudera CDH4.1.1" ;
		public static final String VERSION_GREENPLUM_1_1 ="Greenplum HD 1.1"; 

		public static final String[] SUPPORTED_VERSIONS = new String[]{
			VERSION_APACHE_HADOOP_0_20_2,
			VERSION_APACHE_0_20_203,
			//VERSION_APACHE_1_0_2,
			VERSION_APACHE_1_0_4,
			VERSION_0_20_2_CDH3_U4,
			VERSION_CDH_4_1_1,
			VERSION_GREENPLUM_1_1
		};
		
		public static final HashMap<String, List<String>> JarFileMap = new HashMap<String, List<String>> ();
		public static interface JOB_NAME {
			public static final String Decision_Tree_Predict="Decision_Tree_Predict";
			public static final String TimeSeries_Sort="TimeSeries_Sort";
			public static final String DecisionTree_Depth="DecisionTree_Depth";
			public static final String DecisionTree_Parse_Splits="DecisionTree_Parse_Splits";
			public static final String Goodness_Of_Fit="Goodness_Of_Fit";
			public static final String Kmeans_Post="Kmeans_Post";
			public static final String Kmeans_Init="Kmeans_Init";
			public static final String UNION="Hhadoop_Union";
			public static final String Kmeans_Iteration="Kmeans_Iteration";
			public static final String Kmeans_Output="Kmeans_Output";
			public static final String Max_Min_Job="Max_Min_Job";
			public static final String Lift_DataGenerator="Lift_DataGenerator";
			public static final String LinearRegression_Predictor="LinearRegression_Predictor";
			public static final String LinearRegression_QQ="LinearRegression_QQ";
			public static final String LinearRegression_Staticstics="LinearRegression_Staticstics";
			public static final String LinearRegression_Beta="LinearRegression_Beta";
			public static final String LogisticRegression_Predictor="LogisticRegression_Predictor";
			public static final String LogisticRegression_Iterator="LogisticRegression_Iterator";
			public static final String ROC_DataGenerator="ROC_DataGenerator";
			public static final String Distinct_Job="Distinct_Job";
            public static final String VariableSelection_Beta="VariableSelection_Beta";
            public static final String VariableSelection_R2="VariableSelection_R2";
            public static final String NaiveBayes="NaiveBayes";
            public static final String NaiveBayesPredictor="NaiveBayesPredictor";
            public static final String NaiveBayesConfusion="NaiveBayesConfusion";

        }
		static{
			JarFileMap.put(VERSION_APACHE_HADOOP_0_20_2, Arrays.asList(new String[]{
						"hadoop-0.20.2-core.jar" ,
						"pig-0.10.0-withouthadoop.jar"
				})) ;
			
			JarFileMap.put(VERSION_APACHE_0_20_203, Arrays.asList(new String[]{
					"hadoop-core-0.20.203.0.jar" ,
					"pig-0.10.0-withouthadoop.jar"
			})) ;
			
			JarFileMap.put(VERSION_GREENPLUM_1_1, Arrays.asList(new String[]{
					"hadoop-core-1.0.0+mlnx+gphd+1.1.0.0.jar" ,
					"pig-0.10.0-withouthadoop.jar"
			})) ;
			
			JarFileMap.put(VERSION_APACHE_1_0_2, Arrays.asList(new String[]{
					"hadoop-core-1.0.2.jar" ,
		 			"pig-0.10.0-withouthadoop.jar"
			})) ;
			
			JarFileMap.put(VERSION_APACHE_1_0_4, Arrays.asList(new String[]{
					"hadoop-core-1.0.4.jar" ,
		 			"pig-0.10.0-withouthadoop.jar"
			})) ;
			
			JarFileMap.put(VERSION_0_20_2_CDH3_U4, Arrays.asList(new String[]{
					"hadoop-core-0.20.2-cdh3u4.jar" ,
					"pig-0.8.1-cdh3u4-core-withouthadoop.jar",
					"guava-r09-jarjar.jar"
			})) ;
			
			JarFileMap.put(VERSION_CDH_4_1_1, Arrays.asList(new String[]{
					
					"pig-0.10.0-cdh4.1.1-withouthadoop.jar",
					"hadoop-core-2.0.0-mr1-cdh4.1.1.jar",
					 "guava-11.0.2.jar",
					"hadoop-streaming-2.0.0-mr1-cdh4.1.1.jar", 
					"hadoop-tools-2.0.0-mr1-cdh4.1.1.jar",
					"hadoop-common-2.0.0-cdh4.1.1.jar",
					"hadoop-auth-2.0.0-cdh4.1.1.jar",
					"hadoop-hdfs-2.0.0-cdh4.1.1.jar",			
					"avro-1.7.1.cloudera.2.jar"		,			
					"jackson-core-asl-1.8.8.jar"	,		
					"jackson-mapper-asl-1.8.8.jar",
					"protobuf-java-2.4.0a.jar"

			})) ;
		}
		
		
		public static final String  JAR_APACHE_HADOOP_0_20_2 = "hadoop-0.20.2-core.jar";
		public static final String  JAR_CDH3_U4 ="hadoop-core-0.20.2-cdh3u4.jar"; 
		
		public static final String JAR_APACHE_PIG_0_10_0 = "pig-0.10.0-withouthadoop.jar"; 
		public static final String JAR_CDH3_U4_PIG_0_8_1 = "pig-0.8.1-cdh3u4-core.jar"; 
		
		public static final String CONF_CLASS = "org.apache.hadoop.conf.Configuration" ;
		public static final String FS_CLASS = "org.apache.hadoop.fs.FileSystem" ;
		public static final String PATH_CLASS = "org.apache.hadoop.fs.Path" ;
		public static final String PIGSERVER_CLASS = "org.apache.pig.PigServer";
		public static final String EXEC_TYPE_CLASS = "org.apache.pig.ExecType";
		public static final String DATA_TYPE_CLASS = "org.apache.pig.data.DataType";
		public static final String PIG_TUPLE_CLASS = "org.apache.pig.data.Tuple"; 
		
		
		public static final String PROPERTY_FS_NAME = "fs.default.name";
		public static final String PROPERTY_JOB_UGI = "hadoop.job.ugi"; 

		public static final String HDFS_PREFIX = "hdfs://";
		public static final String COUNT_BADDATA_MR = "baddata.count.mr";
		
		public static String Flow_Call_Back_URL= null;
		
		//different package...
//		public static final String PIG_STORAGE_FUNC = "AlpinePigStorage";
	//	public static final String PIG_STORAGE_FUNC_KEY = "Alpine.PigStorage";

	
}
