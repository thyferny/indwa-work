package com.alpine.datamining.api.impl.hadoop;
import java.util.Map;

import com.google.gson.annotations.Expose;
public class AlpineJob {
	//private String jobConfigString;
	
	@Expose protected String configString;
	@Expose protected Map<String, String> oth;
	
	//All Job parameters
	@Expose  protected String inputPath;
	@Expose  protected String outputPath;
	@Expose  protected String jobName;
	@Expose  protected String counters;
	@Expose  protected String GroupingComparator;
	@Expose  protected String InputFormatClass;
	@Expose  protected String MapOutputKeyClass;
	@Expose  protected String MapOutputValueClass;
	@Expose  protected int NumReduceTasks;
	@Expose  protected String OutputFormatClass;
	@Expose  protected String getMapperClass;
	@Expose  protected String OutputKeyClass;
	@Expose  protected String PartitionerClass;
	@Expose  protected String ReducerClass;
	@Expose  protected String SortComparator;
	@Expose  protected String OutputValueClass;
	@Expose  protected String WorkingDirectory;
	@Expose  protected String CombinerClass;
	@Expose  protected String Jar;
	@Expose  protected String JobID;
	public String getConfigString() {
		return configString;
	}
	public void setConfigString(String configString) {
		this.configString = configString;
	}
	public Map<String, String> getOth() {
		return oth;
	}
	public void setOth(Map<String, String> oth) {
		this.oth = oth;
	}
	public String getInputPath() {
		return inputPath;
	}
	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}
	public String getOutputPath() {
		return outputPath;
	}
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getCounters() {
		return counters;
	}
	public void setCounters(String counters) {
		this.counters = counters;
	}
	public String getGroupingComparator() {
		return GroupingComparator;
	}
	public void setGroupingComparator(String groupingComparator) {
		GroupingComparator = groupingComparator;
	}
	public String getInputFormatClass() {
		return InputFormatClass;
	}
	public void setInputFormatClass(String inputFormatClass) {
		InputFormatClass = inputFormatClass;
	}
	public String getMapOutputKeyClass() {
		return MapOutputKeyClass;
	}
	public void setMapOutputKeyClass(String mapOutputKeyClass) {
		MapOutputKeyClass = mapOutputKeyClass;
	}
	public String getMapOutputValueClass() {
		return MapOutputValueClass;
	}
	public void setMapOutputValueClass(String mapOutputValueClass) {
		MapOutputValueClass = mapOutputValueClass;
	}
	public int getNumReduceTasks() {
		return NumReduceTasks;
	}
	public void setNumReduceTasks(int numReduceTasks) {
		NumReduceTasks = numReduceTasks;
	}
	public String getOutputFormatClass() {
		return OutputFormatClass;
	}
	public void setOutputFormatClass(String outputFormatClass) {
		OutputFormatClass = outputFormatClass;
	}
	public String getGetMapperClass() {
		return getMapperClass;
	}
	public void setGetMapperClass(String getMapperClass) {
		this.getMapperClass = getMapperClass;
	}
	public String getOutputKeyClass() {
		return OutputKeyClass;
	}
	public void setOutputKeyClass(String outputKeyClass) {
		OutputKeyClass = outputKeyClass;
	}
	public String getPartitionerClass() {
		return PartitionerClass;
	}
	public void setPartitionerClass(String partitionerClass) {
		PartitionerClass = partitionerClass;
	}
	public String getReducerClass() {
		return ReducerClass;
	}
	public void setReducerClass(String reducerClass) {
		ReducerClass = reducerClass;
	}
	public String getSortComparator() {
		return SortComparator;
	}
	public void setSortComparator(String sortComparator) {
		SortComparator = sortComparator;
	}
	public String getOutputValueClass() {
		return OutputValueClass;
	}
	public void setOutputValueClass(String outputValueClass) {
		OutputValueClass = outputValueClass;
	}
	public String getWorkingDirectory() {
		return WorkingDirectory;
	}
	public void setWorkingDirectory(String workingDirectory) {
		WorkingDirectory = workingDirectory;
	}
	public String getCombinerClass() {
		return CombinerClass;
	}
	public void setCombinerClass(String combinerClass) {
		CombinerClass = combinerClass;
	}
	public String getJar() {
		return Jar;
	}
	public void setJar(String jar) {
		Jar = jar;
	}
	public String getJobID() {
		return JobID;
	}
	public void setJobID(String jobID) {
		JobID = jobID;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((CombinerClass == null) ? 0 : CombinerClass.hashCode());
		result = prime
				* result
				+ ((GroupingComparator == null) ? 0 : GroupingComparator
						.hashCode());
		result = prime
				* result
				+ ((InputFormatClass == null) ? 0 : InputFormatClass.hashCode());
		result = prime * result + ((Jar == null) ? 0 : Jar.hashCode());
		result = prime * result + ((JobID == null) ? 0 : JobID.hashCode());
		result = prime
				* result
				+ ((MapOutputKeyClass == null) ? 0 : MapOutputKeyClass
						.hashCode());
		result = prime
				* result
				+ ((MapOutputValueClass == null) ? 0 : MapOutputValueClass
						.hashCode());
		result = prime * result + NumReduceTasks;
		result = prime
				* result
				+ ((OutputFormatClass == null) ? 0 : OutputFormatClass
						.hashCode());
		result = prime * result
				+ ((OutputKeyClass == null) ? 0 : OutputKeyClass.hashCode());
		result = prime
				* result
				+ ((OutputValueClass == null) ? 0 : OutputValueClass.hashCode());
		result = prime
				* result
				+ ((PartitionerClass == null) ? 0 : PartitionerClass.hashCode());
		result = prime * result
				+ ((ReducerClass == null) ? 0 : ReducerClass.hashCode());
		result = prime * result
				+ ((SortComparator == null) ? 0 : SortComparator.hashCode());
		result = prime
				* result
				+ ((WorkingDirectory == null) ? 0 : WorkingDirectory.hashCode());
		result = prime * result
				+ ((configString == null) ? 0 : configString.hashCode());
		result = prime * result
				+ ((counters == null) ? 0 : counters.hashCode());
		result = prime * result
				+ ((getMapperClass == null) ? 0 : getMapperClass.hashCode());
		result = prime * result
				+ ((inputPath == null) ? 0 : inputPath.hashCode());
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		result = prime * result
				+ ((outputPath == null) ? 0 : outputPath.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlpineJob other = (AlpineJob) obj;
		if (CombinerClass == null) {
			if (other.CombinerClass != null)
				return false;
		} else if (!CombinerClass.equals(other.CombinerClass))
			return false;
		if (GroupingComparator == null) {
			if (other.GroupingComparator != null)
				return false;
		} else if (!GroupingComparator.equals(other.GroupingComparator))
			return false;
		if (InputFormatClass == null) {
			if (other.InputFormatClass != null)
				return false;
		} else if (!InputFormatClass.equals(other.InputFormatClass))
			return false;
		if (Jar == null) {
			if (other.Jar != null)
				return false;
		} else if (!Jar.equals(other.Jar))
			return false;
		if (JobID == null) {
			if (other.JobID != null)
				return false;
		} else if (!JobID.equals(other.JobID))
			return false;
		if (MapOutputKeyClass == null) {
			if (other.MapOutputKeyClass != null)
				return false;
		} else if (!MapOutputKeyClass.equals(other.MapOutputKeyClass))
			return false;
		if (MapOutputValueClass == null) {
			if (other.MapOutputValueClass != null)
				return false;
		} else if (!MapOutputValueClass.equals(other.MapOutputValueClass))
			return false;
		if (NumReduceTasks != other.NumReduceTasks)
			return false;
		if (OutputFormatClass == null) {
			if (other.OutputFormatClass != null)
				return false;
		} else if (!OutputFormatClass.equals(other.OutputFormatClass))
			return false;
		if (OutputKeyClass == null) {
			if (other.OutputKeyClass != null)
				return false;
		} else if (!OutputKeyClass.equals(other.OutputKeyClass))
			return false;
		if (OutputValueClass == null) {
			if (other.OutputValueClass != null)
				return false;
		} else if (!OutputValueClass.equals(other.OutputValueClass))
			return false;
		if (PartitionerClass == null) {
			if (other.PartitionerClass != null)
				return false;
		} else if (!PartitionerClass.equals(other.PartitionerClass))
			return false;
		if (ReducerClass == null) {
			if (other.ReducerClass != null)
				return false;
		} else if (!ReducerClass.equals(other.ReducerClass))
			return false;
		if (SortComparator == null) {
			if (other.SortComparator != null)
				return false;
		} else if (!SortComparator.equals(other.SortComparator))
			return false;
		if (WorkingDirectory == null) {
			if (other.WorkingDirectory != null)
				return false;
		} else if (!WorkingDirectory.equals(other.WorkingDirectory))
			return false;
		if (configString == null) {
			if (other.configString != null)
				return false;
		} else if (!configString.equals(other.configString))
			return false;
		if (counters == null) {
			if (other.counters != null)
				return false;
		} else if (!counters.equals(other.counters))
			return false;
		if (getMapperClass == null) {
			if (other.getMapperClass != null)
				return false;
		} else if (!getMapperClass.equals(other.getMapperClass))
			return false;
		if (inputPath == null) {
			if (other.inputPath != null)
				return false;
		} else if (!inputPath.equals(other.inputPath))
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		if (outputPath == null) {
			if (other.outputPath != null)
				return false;
		} else if (!outputPath.equals(other.outputPath))
			return false;
		return true;
	}
	
	
	
	

}
