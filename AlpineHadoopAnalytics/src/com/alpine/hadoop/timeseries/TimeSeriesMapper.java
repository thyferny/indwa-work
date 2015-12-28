
package com.alpine.hadoop.timeseries;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.TimeSeriesKeySet;
import com.alpine.hadoop.util.MapReduceHelper;



public class TimeSeriesMapper extends
		Mapper<LongWritable, Text, LongSort, Text> {

	int piIndex = -1;
	private int groupby;
	private Text valueTxt = new Text();

	private int id;
	private int value;

	private LongSort ls = new LongSort();

	private String timeFormat = "Integer";
	MapReduceHelper helper;

	// private Text nilText = new Text();

	@Override
	public void map(LongWritable key, Text values, Context context)
			throws IOException, InterruptedException {
		List<String[]> lines = helper.getCleanData(values, false);
			if (lines != null) {
				for (String[] columnValues : lines) {
					if (columnValues != null) {
						try {
							long parsedId = parseTextToLong(columnValues[id],
									timeFormat);
							String groupbyValue = "";
							if (groupby == -1) {
								groupbyValue = "1";
							} else {
								groupbyValue = columnValues[groupby];
							}
							ls.set(parsedId, groupbyValue.hashCode());
							valueTxt.set(parsedId + ","
									+ Double.parseDouble(columnValues[value])
									+ "," + groupbyValue);
							context.write(ls, valueTxt);// TODO change txt to
														// others
						} catch (Exception e) {
							e.printStackTrace();
							helper.dirtyAdd(columnValues);
						}
					}
				}
			}
	}

	private Long parseTextToLong(String string, String formatString)
			throws IOException {
		try {
			SimpleDateFormat dateFormator = new SimpleDateFormat(formatString);
			return dateFormator.parse(string).getTime();
		} catch (Exception e) {
			try {
				Double x = Double.parseDouble(string);
				return x.longValue();
			} catch (NumberFormatException e1) {
				throw new IOException(e1.getLocalizedMessage());
			}
		}
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());

		id = helper.getConfigInt(TimeSeriesKeySet.id);
		value = helper.getConfigInt(TimeSeriesKeySet.value);
		groupby = helper.getConfigInt(TimeSeriesKeySet.groupby);
		timeFormat = helper.getConfigString(TimeSeriesKeySet.timeFormat);
		List<Integer> involvedIds=new ArrayList<Integer>();
		involvedIds.add(id);
		involvedIds.add(value);
		helper.setInvolvedColumnIds(involvedIds);
	}

	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}
}
