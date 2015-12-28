
package com.alpine.datamining.resources;

import java.util.Locale;
import java.util.ResourceBundle;

public class AlpineDataAnalysisConfig {

	static Locale locale = Locale.getDefault();
	public static ResourceBundle rb = ResourceBundle.getBundle("com.alpine.datamining.resources.alpineDataAnalysisConfig",locale);
	public static final String USE_C_FUNCTION = rb.getString("USE_C_FUNCTION");
	public static final String SIMPLEDATASET_THRESHOLD = rb.getString("SIMPLEDATASET_THRESHOLD");
	public static final String TREE_LABEL_THRESHOLD = rb.getString("TREE_LABEL_THRESHOLD");
	public static final String SE_USE_NEW_BETA = rb.getString("SE_USE_NEW_BETA");
	public static final String MAX_COLUMN = rb.getString("MAX_COLUMN");
	public static final String LR_RERUN = rb.getString("LR_RERUN");
	public static final String CART_DISTINCT_RATIO_THRESHOLD = rb.getString("CART_DISTINCT_RATIO_THRESHOLD");
	public static final int DECIMAL_PRECISION_DIGITS = Integer.parseInt(rb.getString("DECIMAL_PRECISION_DIGITS"));
	public static final int KMEANS_ARRAY_THRESHOLD = Integer.parseInt(rb.getString("KMEANS_ARRAY_THRESHOLD"));
	public static final int ARIMA_MAX_COUNT = Integer.parseInt(rb.getString("ARIMA_MAX_COUNT"));
	public static final int ARIMA_LAST_DATA_COUNT = Integer.parseInt(rb.getString("ARIMA_LAST_DATA_COUNT"));
	public static final int ORACLE_ARRAY_MAX_COUNT = Integer.parseInt(rb.getString("ORACLE_ARRAY_MAX_COUNT"));
	public static final String SVD_FEATURE = rb.getString("SVD_FEATURE");
	public static final double STATISTICS_CHECK_VALUE = Double.parseDouble(rb.getString("STATISTICS_CHECK_VALUE"));
	public static final int DB2_MAX_COLUMN_COUNT = Integer.parseInt(rb.getString("DB2_MAX_COLUMN_COUNT"));
	public static final int NZ_MAX_COLUMN_COUNT = Integer.parseInt(rb.getString("NZ_MAX_COLUMN_COUNT"));
	public static final int ROC_MAX_COLUMN_COUNT = Integer.parseInt(rb.getString("ROC_MAX_COLUMN_COUNT"));
	public static final int ORACLE_FLOAT_SUM_MAX_COUNT = Integer.parseInt(rb.getString("ORACLE_FLOAT_SUM_MAX_COUNT"));
	public static final int NZ_PROCEDURE_SWITCH = Integer.parseInt(rb.getString("NZ_PROCEDURE_SWITCH"));
	public static final int NZ_PROCEDURE_COLUMN_LIMIT = Integer.parseInt(rb.getString("NZ_PROCEDURE_COLUMN_LIMIT"));
	public static final int NZ_ALIAS_NUM = Integer.parseInt(rb.getString("NZ_ALIAS_NUM"));
	public static final int NZ_ALIAS_SWITCH = Integer.parseInt(rb.getString("NZ_ALIAS_SWITCH"));
	public static final int FP_SELECT_COUNT = Integer.parseInt(rb.getString("FP_SELECT_COUNT"));
	public static final String ALPINE_MINER_ID = rb.getString("ALPINE_MINER_ID");
	public static final int LR_NETEZZA_EACH_COUNT = Integer.parseInt(rb.getString("LR_NETEZZA_EACH_COUNT"));
	public static final int NB_GP_COLUMN_COUNT = Integer.parseInt(rb.getString("NB_GP_COLUMN_COUNT"));
	public static final String ALPINE_MINER_EMCLUSTER=rb.getString("ALPINE_MINER_EMCLUSTER");
}
