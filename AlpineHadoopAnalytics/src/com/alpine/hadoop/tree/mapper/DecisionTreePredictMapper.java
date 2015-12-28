

package com.alpine.hadoop.tree.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.hadoop.util.DataPretreatUtility;
import com.alpine.hadoop.util.MapReduceHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class DecisionTreePredictMapper extends Mapper<LongWritable, Text, Text, Text> {
	private static Logger itsLogger = Logger
            .getLogger(DecisionTreePredictMapper.class);

	private HadoopTree model;
 
	private Configuration config;
 	ArrayList<String> columnsIds = new ArrayList<String>();

	private String splitor =",";
	private Text outValue=new Text();
	private String[] columnTypes; 
	StringBuffer out = new StringBuffer();
	MapReduceHelper helper;

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		Map<String[],Boolean> allDataWithFlag = helper.getAllDataWithCleanFlag(value, columnsIds);
			
			if(allDataWithFlag!=null){
				for (String[] vec : allDataWithFlag.keySet()) {
 
					if(allDataWithFlag.get(vec))
					{
						try{
						boolean legal = true;
						// format row for input to populateHash
						List<String> featureVec = new ArrayList<String>(vec.length);

						// placeholder for dependant variable
						// featureVec.add(null);
						for(int dataIndex:model.getFeatureDemap())
						{
							if (vec[dataIndex].matches("\\s*")) 
							{
								legal=false;
								break;
							}
							if(!DataPretreatUtility.checkType(vec[dataIndex],columnTypes[dataIndex]))
							{
								legal=false;
								break;
							}
							featureVec.add(vec[dataIndex]);
						}
 
						if(legal==false)
						{
							helper.dirtyAdd(vec);
							continue;
						}

						HadoopTree node = model.predictNode(featureVec);

						out.setLength(0);
						if(node!=null)
						{
							String[] distribution = node
									.predictDistribution(featureVec);
							for (int j = 0; j < distribution.length - 1; j++) 
							{
								out.append(distribution[j]);
								out.append(splitor);
							}

							out.append(distribution[distribution.length - 1]);
						}
						
						if(node!=null)
						{
							outValue.set((helper.generateOutputLine(vec) + splitor
									+ node.getLabel() + splitor
									+ out.toString()));
							context.write(
									outValue , null);
						}else{
							//one column apears new categoric value 
// 							outValue.set(helper.generateOutputLine(vec));
							context.write(value, null);
						}
						}
						catch(Exception e){
							helper.dirtyAdd(vec);
							//skip bad data
						}
					}
					else{
						helper.dirtyAdd(vec);
					}
				}
			}
	}
	
	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}

		protected void setup(Context context) {
			Gson gson = new GsonBuilder().serializeNulls().create();
			helper = new MapReduceHelper(context.getConfiguration(),
					context.getTaskAttemptID());
			config = context.getConfiguration();
			FileSystem fs;
			FSDataInputStream in1 = null;
			
			ByteArrayOutputStream buf1 = null;
		
			
			String modelFile = config.get(DecisionTreeConfigureKeySet.TREE_FILE);
 			try {
			fs = FileSystem.get(config);
			in1 = fs.open(new Path(modelFile));
			
			// read tree model
			buf1 = new ByteArrayOutputStream();
			
			IOUtils.copyBytes(in1, buf1, 4096, false);
			
			String modelJson = buf1.toString();
			
			
			// deserialize string
			this.model = gson.fromJson(modelJson, HadoopTree.class);
 			columnTypes = config
					.get(DecisionTreeConfigureKeySet.COLUMN_TYPES).split(",");
			
 			for(int index:model.getFeatureDemap())
 			{
 				columnsIds.add(index+"");
 			}
 			
 			
 			} catch (Exception e) {
				itsLogger.error("Cannot initialize Splits:", e);
	            throw new IllegalArgumentException("Cannot initialize Split mappings:"); 
			} finally {
				// close streams
				IOUtils.closeStream(in1);
				
				IOUtils.closeStream(buf1);
				
			}
		}
}
