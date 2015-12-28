
package com.alpine.hadoop.ext;

import java.io.UnsupportedEncodingException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;


public class JSONInputFormat extends TextInputFormat {

	public static final String START_TAG_KEY = RecordParserFactory.XML_START_TAG_KEY;
	public static final String END_TAG_KEY = RecordParserFactory.XML_END_TAG_KEY;
	private String containerTag = null;
	private String jsonDataStructureType = null;
	private String containerJsonPath = null;
	public JSONInputFormat() {

	}

	public JSONInputFormat(String containerTag,String jsonDataStructureType, String containerJsonPath) { // trak
																	// traklist
																	// without <
		this.jsonDataStructureType=jsonDataStructureType;												// and >
		this.containerTag = containerTag;
		this.containerJsonPath=containerJsonPath;

	}

	@Override
	public RecordReader<LongWritable, Text> createRecordReader(
			InputSplit inputSplit, TaskAttemptContext context) {

		try {
			return new JSONRecordReader(containerTag,jsonDataStructureType,containerJsonPath);
		} catch (UnsupportedEncodingException e) {
			// this can not happen
			e.printStackTrace();
		}
		return null;
	}
}
