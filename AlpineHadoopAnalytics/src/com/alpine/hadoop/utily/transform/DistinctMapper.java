package com.alpine.hadoop.utily.transform;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.TransformerKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Peter
 * 
 */

public class DistinctMapper extends
		Mapper<LongWritable, Text, ColumnDistinct, Text> {
	
	List<Integer> ids;
	MapReduceHelper helper;
	
	List<String[]> distinct;
	ColumnDistinct idWriter=new ColumnDistinct();
	Text txtWriter=new Text();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		helper.setInvolvedColumnIds(ids);
		List<String[]> columnValuesList = helper.getCleanData(value, false);
		if (columnValuesList != null) {
			try{
			for (String[] columnValues : columnValuesList) {
				for(Integer i: ids){
					idWriter.set(i, columnValues[i]);
					txtWriter.set("");
					context.write(idWriter, txtWriter);
				}
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}
	
	@Override
	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		ids = helper.getColumnIds(TransformerKeySet.ID_FIELDS);
	}
}