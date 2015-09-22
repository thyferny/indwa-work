package com.alpine.datamining.api.resources;

import java.util.Locale;
import java.util.ResourceBundle;

public class AlpineMinerConfig {

	static Locale locale = Locale.getDefault();
	public static ResourceBundle rb = ResourceBundle.getBundle("com.alpine.datamining.api.resources.alpineMinerConfig",locale);
//	public static final String DATA_EXPLORE_ROWS = rb.getString("DATA_EXPLORE_ROWS");
	public static final String FREQUENCY_ANALYSIS_THRESHOLD = rb.getString("FREQUENCY_ANALYSIS_THRESHOLD");
	public static final String HISTOGRAM_ANALYSIS_THRESHOLD = rb.getString("HISTOGRAM_ANALYSIS_THRESHOLD");
	public static final String TABLE_ANALYSIS_THRESHOLD = rb.getString("TABLE_ANALYSIS_THRESHOLD");
	public static final String C2N_WARNING = rb.getString("C2N_WARNING");
	public static final String CART_COMBINE_THRESHOLD = rb.getString("CART_COMBINE_THRESHOLD");
	public static final String PIVOT_DISTINCTVALUE_THRESHOLD = rb.getString("PIVOT_DISTINCTVALUE_THRESHOLD");
	public static final String HADOOP_PIVOT_DISTINCTVALUE_THRESHOLD = rb.getString("HADOOP_PIVOT_DISTINCTVALUE_THRESHOLD");
	public static final String STRATIFIED_SAMPLING_THRESHOLD=rb.getString("STRATIFIED_SAMPLING_THRESHOLD");
	public static final int DECIMAL_PRECISION_DIGITS = Integer.parseInt(rb.getString("DECIMAL_PRECISION_DIGITS"));
	public static final int VALUE_ANALYSIS_EACH_TIME_COLUMNS_COUNT = Integer.parseInt(rb.getString("VALUE_ANALYSIS_EACH_TIME_COLUMNS_COUNT"));
	public static final int VALUE_ANALYSIS_COUNT_DISTINCT_EACH_TIME_COLUMNS_COUNT = Integer.parseInt(rb.getString("VALUE_ANALYSIS_COUNT_DISTINCT_EACH_TIME_COLUMNS_COUNT"));
	public static final double VALUE_ANALYSIS_COUNT_DISTINCT_THRESHOLD = Double.parseDouble(rb.getString("VALUE_ANALYSIS_COUNT_DISTINCT_THRESHOLD"));
	public static final long VALUE_ANALYSIS_COUNT_DISTINCT_COUNT_THRESHOLD = Long.parseLong(rb.getString("VALUE_ANALYSIS_COUNT_DISTINCT_COUNT_THRESHOLD"));
	public static final int VALUE_ANALYSIS_COUNT_DISTINCT_LIMIT = Integer.parseInt(rb.getString("VALUE_ANALYSIS_COUNT_DISTINCT_LIMIT"));
	public static final int VALUE_ANALYSIS_COUNT_ALL_LIMIT = Integer.parseInt(rb.getString("VALUE_ANALYSIS_COUNT_ALL_LIMIT"));
	public static final long RANDOM_SAMPLING_COUNT_THRESHOLD = Long.parseLong(rb.getString("RANDOM_SAMPLING_COUNT_THRESHOLD"));
	public static final double RANDOM_SAMPLING_LIMIT_RATIO = Double.parseDouble(rb.getString("RANDOM_SAMPLING_LIMIT_RATIO"));
	public static final long RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD = Long.parseLong(rb.getString("RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD"));
	public static final double RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO = Double.parseDouble(rb.getString("RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO"));
	public static final long INFORMATIONVALUE_THRESHOLD = Long.parseLong(rb.getString("INFORMATIONVALUE_THRESHOLD"));
	public static final long VARIABLE_SELECTION_THRESHOLD = Long.parseLong(rb.getString("VARIABLE_SELECTION_THRESHOLD"));
	public static final double RECOMMENDATION_EPSILON = Double.parseDouble(rb.getString("RECOMMENDATION_EPSILON"));
	public static final int ADABOOST_SAMPLE = Integer.parseInt(rb.getString("ADABOOST_SAMPLE"));
	public static final int ADABOOST_MAX_DEPENDENT_COUNT = Integer.parseInt(rb.getString("ADABOOST_MAX_DEPENDENT_COUNT"));
	public static final int ADABOOST_SAMPLE_NUMBER=Integer.parseInt(rb.getString("ADABOOST_SAMPLE_NUMBER"));
	public static final double WOE_mini_ForSplit_Percent=Double.parseDouble(rb.getString("WOE_mini_ForSplit_Percent"));
	public static final double WOE_mini_LeafSize_Percent=Double.parseDouble(rb.getString("WOE_mini_LeafSize_Percent"));
	public static final int ASSOCIATION_DB2_COLUMN_LENGTH = Integer.parseInt(rb.getString("ASSOCIATION_DB2_COLUMN_LENGTH"));
	public static final int FP_INSERT_COUNT = Integer.parseInt(rb.getString("FP_INSERT_COUNT"));
	public static final int BOX_WHISKER_THRESHOLD = Integer.parseInt(rb.getString("BOX_WHISKER_THRESHOLD"));
	public static final int HADOOP_LINE_THRESHOLD = Integer.parseInt(rb.getString("HADOOP_LINE_THRESHOLD"));
	public static final int FREQUENCY_ANALYSIS_THRESHOLD_HADOOP = Integer.parseInt(rb.getString("FREQUENCY_ANALYSIS_THRESHOLD_HADOOP"));
}
