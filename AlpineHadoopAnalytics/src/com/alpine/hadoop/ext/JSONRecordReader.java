/**
 * ClassName JSONRecordReader.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-5
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Logger;

/**
 * @author Jeff Dong
 *
 */
public class JSONRecordReader extends RecordReader<LongWritable, Text> {
	
	public static Logger itsLogger = Logger.getLogger(JSONRecordReader.class);
	
	public static final String START_TAG_KEY = RecordParserFactory.JSON_START_TAG_KEY;
	
	public static final String JSONDataStructureType_TAG_KEY = RecordParserFactory.JSON_TYPE_TAG_KEY;
	
	public static final String CONTAINER_JSON_PATH_TAG_KEY = RecordParserFactory.JSON_CONTAINER_PATH_TYPE_TAG_KEY;
	
	public static final byte[] LEFT_SQUARE_BRACKET= "[".getBytes();
	
	public static final byte[] RIGHT_SQUARE_BRACKET= "]".getBytes();
	
	public static final byte[] LEFT_BRACE= "{".getBytes();
	
	public static final byte[] RIGHT_BRACE= "}".getBytes();
	
	public static final byte[] DQ= "\"".getBytes();

	public static final String STRUCTURE_TYPE_STANDARD = "sts";
	public static final String STRUCTURE_TYPE_LINE = "stl";
	public static final String STRUCTURE_TYPE_PURE_DATA_ARRAY = "stp";
	public static final String STRUCTURE_TYPE_OBJECT_ARRAY = "sto";
	
	public byte[] startTag;
	public byte[] startTag1;
	public long start;
	public long end;
	public LongWritable key = null;
	public Text value = null;
	public FSDataInputStream fsin;
	private InputStream in;
	public DataOutputBuffer buffer = new DataOutputBuffer();
	private String jsonDataStructureType;
	
	private boolean isInmatch2;
	private boolean isContainerSkiped =false;
	private boolean isCompressed=false;
	private long index=0;
	
	public static boolean isEmpty(String value) {
		if (value==null || value.trim().length()==0) {
			return true;
		} else {
			return false;
		}
	}
	public   String doubleQ(String inputString) {
		if (inputString == null) {
			inputString = "";
		}
		if (!inputString.startsWith("\"") || !inputString.endsWith("\"")) {
			inputString = inputString.replace("\"", "\"\"");
			inputString = "\"" + inputString + "\"";
		}
		return inputString;
	}

	
	public JSONRecordReader(String containerTagName,String jsonDataStructureType, String containerJsonPath)
			throws UnsupportedEncodingException {
		if( isEmpty(containerTagName)==false){
			startTag = ( doubleQ(containerTagName)+":").getBytes("utf-8");
			startTag1 = ( doubleQ(containerTagName)+" :").getBytes("utf-8");
		}
		this.jsonDataStructureType=jsonDataStructureType;
	}
	
	@Override
	public void initialize(InputSplit genericSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		Configuration jobConf = context.getConfiguration();
		FileSplit split = (FileSplit) genericSplit;
		if (jobConf.get(START_TAG_KEY) != null) {
			startTag = ("\""+jobConf.get(START_TAG_KEY)+"\":").getBytes("utf-8");
			startTag1 = ("\""+jobConf.get(START_TAG_KEY)+"\" :").getBytes("utf-8");
		}
		if(jobConf.get(JSONDataStructureType_TAG_KEY) != null){
			jsonDataStructureType = jobConf.get(JSONDataStructureType_TAG_KEY);
		}

		// open the file and seek to the start of the split
		start = split.getStart();
		end = start + split.getLength();
		Path file = split.getPath();
		FileSystem fs = file.getFileSystem(jobConf);
		fsin = fs.open(split.getPath());
		
		String path=split.getPath().getName();
		
		if(isCompressedFile(path)){
			generateCompressedInputStream(jobConf, file);
		}else{
			fsin.seek(start);
			if(jsonDataStructureType.equals(STRUCTURE_TYPE_LINE)){
				if(start!=0){
					readUntilReturn(fsin);
				}
			}
		}
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (key == null) {
			key = new LongWritable();
		}
		if (value == null) {
			value = new Text();
		}
		
		if(isCompressed){
			return readAllData(in);
		}else{
			return readAllData(fsin);
		}
		
	}
	private boolean readAllData(InputStream in) throws IOException, InterruptedException,
	UnsupportedEncodingException {
		if(isInputStreamAvaliable()){
			if(jsonDataStructureType.equals(STRUCTURE_TYPE_LINE)){
				String newStr = readLineRecord(in);
				
				if(newStr!=null&&newStr.isEmpty()==false){
					newStr=newStr.trim();
				}else{
					return false;
				}
				key.set(getIndex());
				value.set(newStr.getBytes());
				return true;
			}
			else if(jsonDataStructureType.equals(STRUCTURE_TYPE_PURE_DATA_ARRAY)){
				if(isContainerSkiped ==false){
					//skipContainerString();
					if(readUntilMatch(in,startTag, startTag1, false)==false){
						return false;
					}
					//now the cursor point to the first '['  of data:[[
					readUntilMatch(in);
					//not start for the second '[' of data:[[
					isContainerSkiped =true ;
				}
				String newStr = readArrayRecord(in,LEFT_SQUARE_BRACKET,RIGHT_SQUARE_BRACKET);
				if(newStr!=null&&newStr.isEmpty()==false){
					newStr=newStr.trim();
				}else{
					return false;
				}
				key.set(getIndex());
				value.set(newStr.getBytes());
				return true;
			}else if(jsonDataStructureType.equals(STRUCTURE_TYPE_OBJECT_ARRAY)){
				if(isContainerSkiped ==false){
					//skipContainerString();
					if(readUntilMatch(in,startTag, startTag1, false)==false){
						return false;
					}
					//now the cursor point to the first '['  of data:[[
					readUntilMatch(in);
					//not start for the second '[' of data:[[
					isContainerSkiped =true ;
				}
				String newStr = readArrayRecord(in,LEFT_BRACE,RIGHT_BRACE);
				if(newStr!=null&&newStr.isEmpty()==false){
					newStr=newStr.trim();
				}else{
					return false;
				}
				key.set(getIndex());
				value.set(newStr.getBytes());
				return true;
			}
			else{
				if (readUntilMatch(in,startTag, startTag1,false)) {
					try {
						int fisrtMatch = readUntilMatch(in);
						if(fisrtMatch==0){
							if(readUntilMatch(in,LEFT_SQUARE_BRACKET,RIGHT_SQUARE_BRACKET,1,0)){
								key.set(getIndex());
								String newStr="["+new String(buffer.getData(),"utf-8").trim()+"]";
								value.set(newStr.getBytes());
								return true;
							}
						}else if(fisrtMatch==1){
							if(readUntilMatch(in,LEFT_BRACE,RIGHT_BRACE,1,0)){
								key.set(getIndex());
								String newStr="{"+new String(buffer.getData(),"utf-8").trim()+"}";
								value.set(newStr.getBytes());
								return true;
							}
						}else if(fisrtMatch==2){
							if(readUntilMatch(in,DQ, true)){
								key.set(getIndex());
								String newStr="\""+new String(buffer.getData(),"utf-8");
								value.set(newStr.getBytes());
								return true;
							}
						}
					} finally {
						buffer.reset();
					}
				}
			}
		}
		return false;
	}

	private void generateCompressedInputStream(Configuration jobConf, Path file)
			throws IOException {
		CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(jobConf);
		CompressionCodec codec = compressionCodecs.getCodec(file);
		in = codec.createInputStream(fsin);
		isCompressed=true;
	}
	
	private boolean isCompressedFile(String path){
		if(path!=null&&path.trim().length()!=0&&path.endsWith(".gz")){
			return true;
		}
		return false;
	}
	private boolean isInputStreamAvaliable() throws IOException{
		if(isCompressed){
			return in.available()!=0;
		}else{
			return fsin.getPos() < end;
		}
	}
	
	private long getIndex() throws IOException{
		if(isCompressed){
			return index;
		}else{
			return fsin.getPos();
		}
	}
	
	private String readArrayRecord(InputStream in,byte[] leftArray,byte[] rightArray) throws IOException{
		DataOutputBuffer buffer = new DataOutputBuffer();
		try {	
			if(readUntilMatch(in,leftArray,rightArray,0,0,buffer)){
				String newStr=new String(buffer.getData(),"utf-8").trim();
				return newStr;
			}
		} finally {
			buffer.close();
		}
		return null;
	}
	
	private boolean readUntilMatch(InputStream in,byte[] leftArray,byte rightArray[],int leftCount,
			int rightCount, DataOutputBuffer buffer) throws IOException{
			boolean beingToRecord=false;
			while (true) {
				int b = in.read();
				// end of file:
				if (b == -1)
					return false;
				index++;
				// check if we're matching:
				if (b == rightArray[0]) {
					rightCount++;
					if(leftCount==rightCount){
						buffer.write(b);
						return true;
					}		
				} else if(b == leftArray[0]){
					leftCount++;	
					beingToRecord=true;
				} 
				if(beingToRecord){
					// save to buffer:
					buffer.write(b);
				}
			}
	}
	private String readLineRecord(InputStream in) throws IOException {
		try {
			while (true) {
				int b = in.read();
				// end of file:
				if (b == -1)
					return null;
				index++;
				if(b == 10){//"\n"
					break;
				}
				// save to buffer:
				buffer.write(b);
			}
			return new String(buffer.getData(),"utf-8").trim();
		} finally{
			buffer.reset();
		}
	}
	
	private void readUntilReturn(InputStream in) throws IOException {
		while (true) {
			int b = in.read();
			// end of file:
			if (b == -1)
				break;
			if (b == 10) {// "\n"
				break;
			}
		}
	}
	
	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if(isCompressed){
			return Math.min(1.0f, (index - start) / (float)(end - start));
		}else{
			return (fsin.getPos() - start) / (float) (end - start);
		}
		
	}

	@Override
	public synchronized void close() throws IOException {
		fsin.close();
		if(in!=null){
			in.close();
		}
	}
	
	//match1 = "field":   match2 = "field" :
	private boolean readUntilMatch(InputStream in,byte[] match1,byte[]match2, boolean withinBlock)
			throws IOException {
		int i = 0;
		while (true) {
			int b = in.read();
			// end of file:
			if (b == -1)
				return false;
			index++;
			// save to buffer:
			if (withinBlock)
				buffer.write(b);

			// check if we're matching:
			if (b == match1[i]||b==match2[i]) {
				if(b<match1.length && b != match1[i]){
					isInmatch2=true;
				}
				i++;
				//match 2 match2
				if(i>=match2.length){
					isInmatch2=false;
					return true;
				}else{//match to match 1
					if(isInmatch2==false&&i>=match1.length){
						isInmatch2=false;
						return true;
					}					
				}	
			} else{
				i = 0;
			}
		}
	}

	private int readUntilMatch(InputStream in) throws IOException{
		while (true) {
			int b = in.read();
			// end of file:
			if (b == -1)
				return -1;
			index++;
			// check if we're matching:
			if (b == LEFT_SQUARE_BRACKET[0]) {
				return 0;	
			} else if(b == LEFT_BRACE[0]){
				return 1;	
			} else if(b == DQ[0]){
				 return 2;
			}
		}
	}
	
	private boolean readUntilMatch(InputStream in,byte[] leftArray,byte rightArray[],int leftCount,int rightCount) throws IOException{
		while (true) {
			int b = in.read();
			// end of file:
			if (b == -1)
				return false;
			index++;
			// check if we're matching:
			if (b == rightArray[0]) {
				rightCount++;
				if(leftCount==rightCount){
						return true;
				}		
			} else if(b == leftArray[0]){
				leftCount++;	
			} 
			// save to buffer:
			buffer.write(b);
		}
	}

	private boolean readUntilMatch(InputStream in,byte[] match, boolean withinBlock)
			throws IOException {
		int i = 0;
		while (true) {
			int b = in.read();
			// end of file:
			if (b == -1)
				return false;
			index++;
			// save to buffer:
			if (withinBlock)
				buffer.write(b);

			// check if we're matching:
			if (b == match[i]) {
				i++;
				if(i>=match.length){
					return true;
				}	
			} else{
				i = 0;
			}
			// see if we've passed the stop point:
			if (!withinBlock && i==0)
				return false;
		}
}
}
