package com.alpine.datamining.api.impl.hadoop;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
public class AlpineJobExt extends AlpineJob{
	public static void main(String[]args) throws IOException{
		Configuration config=new Configuration();
		Job j=new Job(config);
		j.setJobName("NihatJob");
		j.setJarByClass(AlpineJobExt.class);
		
		
	}
	public AlpineJobExt(){
		
	}
	
	protected void buildJsonForConfig(Job job){
		Iterator<Entry<String, String>> it = job.getConfiguration().iterator();
		oth=new HashMap<String,String>();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			String key=entry.getKey();
			String value=entry.getValue();
			System.out.println("Key["+key+"]value["+value+"]");
			oth.put(key,value);
			
		}
		configString=new Gson().toJson(oth);
		
		
		
		
	}

	public AlpineJobExt(Job job,String inputPath,String outpuPath){
		this.inputPath=inputPath;
		this.outputPath=outpuPath;
		Gson ng=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		
		buildJsonForConfig(job);
		Configuration confDup=ng.fromJson(configString,Configuration.class);
		
		
		
		buildJobClassSettings(job);
		
		System.out.println(configString);
		String thisJson=ng.toJson(this);
		AlpineJobExt aj=ng.fromJson(thisJson,AlpineJobExt.class);
		System.out.println("Checking if deserilized objec is the same with the original one["
				+aj.equals(this)+"]");
		System.out.println("JSON object is["+thisJson+"]");
		
	}
	
	
	private void buildJobClassSettings(Job job) {
		jobName=job.getJobName();
		try {
			counters = cleanUp((null==job.getCounters()?"":job.getCounters().toString()));
		} catch (Throwable e) {
			//ignore the counters
		}
		
		GroupingComparator=cleanUp((null==job.getGroupingComparator().getClass()?"":
			job.getGroupingComparator().getClass().toString()));
							
		try {
			InputFormatClass=cleanUp(job.getInputFormatClass()==null?"":
				job.getInputFormatClass().toString());
		} catch (Throwable e) {
			// Ignore grouping
		}
		
		MapOutputKeyClass = cleanUp(null==job.getMapOutputKeyClass()?"":
			job.getMapOutputKeyClass().toString());
		MapOutputValueClass= cleanUp(null==job.getMapOutputValueClass()?"":
			job.getMapOutputValueClass().toString());
		try {
			getMapperClass=cleanUp(null==job.getMapperClass()?"":
				job.getMapperClass().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NumReduceTasks=job.getNumReduceTasks();
		try {
			OutputFormatClass= cleanUp(null==job.getOutputFormatClass()?"":
					job.getOutputFormatClass().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputKeyClass =cleanUp( null==job.getOutputKeyClass()?"":
			job.getOutputKeyClass().toString());
		
		OutputValueClass=cleanUp( null==job.getOutputValueClass()?"":
			job.getOutputKeyClass().toString());
		try {
			PartitionerClass=cleanUp( null==job.getPartitionerClass()?""
					:job.getPartitionerClass().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ReducerClass=cleanUp( null==job.getReducerClass()?"":
				job.getReducerClass().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SortComparator=cleanUp( null==job.getSortComparator()?"":
			job.getSortComparator().getClass().toString());
		try {
			WorkingDirectory=cleanUp( null==job.getWorkingDirectory()?"":
				job.getWorkingDirectory().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			CombinerClass=cleanUp( null==job.getCombinerClass()?"":
				job.getCombinerClass().toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Jar=cleanUp( null==job.getJar()?"":job.getJar().toString());
		JobID=cleanUp(null==job.getJobID()?"":
			job.getJobID().toString());
		
	}
	private String cleanUp(String cName) {
		if(null==cName)
			return cName;
		
		if(cName.startsWith("class ")){
			return cName.substring("class ".length(),cName.length());
		}
		return cName;
		
	}

	

}
