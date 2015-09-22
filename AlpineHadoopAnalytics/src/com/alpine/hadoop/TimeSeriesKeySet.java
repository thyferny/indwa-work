package com.alpine.hadoop;

public interface TimeSeriesKeySet extends AlpineHadoopConfKeySet{
	public static String id="alpine.timeseries.id";
	public static String value="alpine.timeseries.value";
	public static String groupby="alpine.timeseries.groupby";
	public static String autoregressive="alpine.timeseries.autoregressive";
	public static String movingaverage="alpine.timeseries.movingaverage";
	public static String integrated="alpine.timeseries.integrated";
	public static String tail="alpine.timeseries.tail";
	public static String dataSize="alpine.timeseries.dataSize";
	public static String lengthOfWindow="alpine.timeseries.lengthofwindow";
	public static String timeFormat="alpine.timeseries.timeformate";
	public static String lastData = "alpine.timeseries.lastData";
}
