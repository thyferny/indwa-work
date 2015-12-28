
package com.alpine.hadoop.timeseries;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

public class StepLineReader extends RecordReader<Text, DoubleWritable>{


	  private CompressionCodecFactory compressionCodecs = null;
	  private long start;
	  private long pos;
	  private long end;
	  private int maxLineLength;
	  private Text key = null;
	  private DoubleWritable value = null;
	private LineReader in;

	  public void initialize(InputSplit genericSplit,
	                         TaskAttemptContext context) throws IOException {
	    FileSplit split = (FileSplit) genericSplit;
	    Configuration job = context.getConfiguration();
	    this.maxLineLength = job.getInt("mapred.linerecordreader.maxlength",
	                                    Integer.MAX_VALUE);
	    start = split.getStart();
	    end = start + split.getLength();
	    final Path file = split.getPath();
	    compressionCodecs = new CompressionCodecFactory(job);
	    final CompressionCodec codec = compressionCodecs.getCodec(file);

	    // open the file and seek to the start of the split
	    FileSystem fs = file.getFileSystem(job);
	    FSDataInputStream fileIn = fs.open(split.getPath());
	    boolean skipFirstLine = false;
	    if (codec != null) {
	      in = new LineReader(codec.createInputStream(fileIn), job);
	      end = Long.MAX_VALUE;
	    } else {
	      if (start != 0) {
	        skipFirstLine = true;
	        --start;
	        fileIn.seek(start);
	      }
//	      in = new LineReader(fileIn, job);
	    }
	    if (skipFirstLine) {  // skip first line and re-establish "start".
	      start += in.readLine(new Text(), 0,
	                           (int)Math.min((long)Integer.MAX_VALUE, end - start));
	    }
	    this.pos = start;
	  }
	  
	  public boolean nextKeyValue() throws IOException {
	    if (key == null) {
	      key = new Text();
	    }
//	    key.set(pos);
	    if (value == null) {
//	      value = new Text();
	    }
	    int newSize = 0;
	    while (pos < end) {
//	      newSize = in.readLine(value, maxLineLength,
//	                            Math.max((int)Math.min(Integer.MAX_VALUE, end-pos),
//	                                     maxLineLength));
	      if (newSize == 0) {
	        break;
	      }
	      pos += newSize;
	      if (newSize < maxLineLength) {
	        break;
	      }
	    }
	    if (newSize == 0) {
	      key = null;
	      value = null;
	      return false;
	    } else {
	      return true;
	    }
	  }

	  @Override
	  public Text getCurrentKey() {
	    return key;
	  }

	  @Override
	  public DoubleWritable getCurrentValue() {
	    return value;
	  }

	  
	  public float getProgress() {
	    if (start == end) {
	      return 0.0f;
	    } else {
	      return Math.min(1.0f, (pos - start) / (float)(end - start));
	    }
	  }
	  
	  public synchronized void close() throws IOException {
	    if (in != null) {
	      in.close(); 
	    }
	  }

}
