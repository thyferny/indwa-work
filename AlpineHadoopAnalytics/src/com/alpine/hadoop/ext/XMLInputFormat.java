
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
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import com.alpine.hadoop.AlpineHadoopConfKeySet;


public class XMLInputFormat extends TextInputFormat {
	
	public static final String NO_ATTRIBUTE = "no" ;
	public static final String HALF_ATTRIBUTE = "half" ;
	public static final String PURE_ATTRIBUTE = "pure" ;
	

	public static final String START_TAG_KEY = RecordParserFactory.XML_START_TAG_KEY;
	public static final String END_TAG_KEY = RecordParserFactory.XML_END_TAG_KEY;
	private String containerTag = null;
	private String attrMode = null;
	private String jsonDataStructureType = null;
	private String containerJsonPath = null;

	public XMLInputFormat() {

	}

	public XMLInputFormat(String containerTag,String attrMode,String jsonDataStructureType,
			String containerJsonPath) { // trak
																	// traklist
																	// without <
		this.attrMode =attrMode;															// and >
		this.containerTag = containerTag;
		this.jsonDataStructureType=jsonDataStructureType;
		this.containerJsonPath=containerJsonPath;
	}

	@Override
	public RecordReader<LongWritable, Text> createRecordReader(
			InputSplit inputSplit, TaskAttemptContext context) {

		try {
				return new XmlRecordReader(containerTag,attrMode,jsonDataStructureType,
						containerJsonPath);
		} catch ( UnsupportedEncodingException  e) {
			// this can not happen
			e.printStackTrace();
		}
		return null;
	}

	
	public static class XmlRecordReader extends
			RecordReader<LongWritable, Text> {
		private byte[] startTag;
		private byte[] endTag;
		private long start;
		private long end;      

		private LongWritable key = null;
		private Text value = null;
		private FSDataInputStream fsin;
		private DataOutputBuffer buffer = new DataOutputBuffer();
		private InputStream in;
		private boolean isCompressed=false;
		private long index=0;
		private String xmlDataStructureType;
		private String containerXPath;
		private static final String XMLDataStructureType_TAG_KEY=AlpineHadoopConfKeySet.XML_TYPE_TAG_KEY;
		private static final String CONTAINER_XML_PATH_TAG_KEY=AlpineHadoopConfKeySet.XML_CONTAINER_PATH_TYPE_TAG_KEY;
		
		public static final String STRUCTURE_TYPE_STANDARD = "sts";
		public static final String STRUCTURE_TYPE_LINE = "stl";
		
		
		
		public XmlRecordReader(){
			
		}
		
		// this is for pig
		public XmlRecordReader(String containerTagName, 
				String attributeMode,String xmlDataStructureType,
				String containerXPath)
				throws UnsupportedEncodingException {
			if (attributeMode != null) {
				if (attributeMode.equals(NO_ATTRIBUTE)) {

					startTag = new String("<" + containerTagName + ">")
							.getBytes("utf-8");
					endTag = new String("</" + containerTagName + ">")
							.getBytes("utf-8");
				} else {
					startTag = new String("<" + containerTagName + " ")
							.getBytes("utf-8");
					if (attributeMode.equals(HALF_ATTRIBUTE)) {
						endTag = new String("</" + containerTagName + ">")
								.getBytes("utf-8");

					} else { // pure attribute mode
						endTag = new String("/>").getBytes("utf-8");

					}

				}
			}
			this.containerXPath=containerXPath;
			this.xmlDataStructureType=xmlDataStructureType;
		}

		
		
		
		//this is for hadoop
		public void initialize(InputSplit genericSplit,
				TaskAttemptContext context) throws IOException,
				InterruptedException {  
			Configuration jobConf = context.getConfiguration();
			FileSplit split = (FileSplit) genericSplit;
			if (jobConf.get(START_TAG_KEY) != null) {
				startTag = jobConf.get(START_TAG_KEY).getBytes("utf-8");

				
			}

			if (jobConf.get(END_TAG_KEY) != null) {
				endTag = jobConf.get(END_TAG_KEY).getBytes("utf-8");
			}
	 
			if(jobConf.get(XMLDataStructureType_TAG_KEY) != null){
				xmlDataStructureType = jobConf.get(XMLDataStructureType_TAG_KEY);
			}
			if(jobConf.get(CONTAINER_XML_PATH_TAG_KEY) != null){
				containerXPath = jobConf.get(CONTAINER_XML_PATH_TAG_KEY);
			}
			// open the file and seek to the start of the split
			start = split.getStart();
			end = start + split.getLength();
			Path file = split.getPath();
			FileSystem fs = file.getFileSystem(jobConf);
			fsin = fs.open(split.getPath());
			String path=split.getPath().getName();
			if(path!=null&&path.trim().length()!=0&&path.endsWith(".gz")){
				CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(jobConf);
				CompressionCodec codec = compressionCodecs.getCodec(file);
				in = codec.createInputStream(fsin);
				isCompressed=true;
			}else{
				fsin.seek(start);
				if(xmlDataStructureType.equals(STRUCTURE_TYPE_LINE)){
					if(start!=0){
						readUntilReturn(fsin);
					}
				}
			}
		}

		
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

		private boolean readAllData(InputStream in) throws IOException {
			if(isInputStreamAvaliable()){
				if(xmlDataStructureType.equals(STRUCTURE_TYPE_LINE)){
					if(containerXPath!=null&&containerXPath.length()!=0){
						String line = readLineRecord(in);
						if(line!=null){
							line=line.trim();
							key.set(getIndex());
							value.set(line);
							return true;
						}
					}
				}else if(xmlDataStructureType.equals(STRUCTURE_TYPE_STANDARD)){
					if (readUntilMatch(in,startTag, false)) {
						try {
							buffer.write(startTag);
							if (readUntilMatch(in,endTag, true)) {
								key.set(getIndex());
								value.set(buffer.getData(), 0, buffer.getLength());
								return true;
							}
						} finally {
							buffer.reset();
						}
					}
				}
			}
			return false;
		}
		private boolean isInputStreamAvaliable() throws IOException {
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
		
		@Override
		public LongWritable getCurrentKey() {
			return key;
		}

		@Override
		public Text getCurrentValue() {
			return value;
		}

		

		@Override
		public void close() throws IOException {
			fsin.close();
			if(in!=null){
				in.close();
			}
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			if(isCompressed){
				return Math.min(1.0f, (index - start) / (float)(end - start));
			}else{
				return (fsin.getPos() - start) / (float) (end - start);
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
		
		private boolean readUntilMatch(InputStream in,byte[] match, boolean withinBlock)
				throws IOException {
			int i = 0;
			while (true) {
				int b = in.read();
				// end of file:
				if (b == -1){
					return false;
				}
				index++;
				// save to buffer:
				if (withinBlock == true){
					buffer.write(b);
				}

				// check if we're matching:
				if (b == match[i]) {
					i++;
					if (i >= match.length){
						return true;
				}
				} else{
					i = 0;
				}
			}
		}
	}
}