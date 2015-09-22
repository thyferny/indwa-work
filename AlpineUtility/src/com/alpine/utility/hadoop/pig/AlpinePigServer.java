package com.alpine.utility.hadoop.pig;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pig.backend.executionengine.ExecJob;
import org.apache.pig.data.Tuple;

public interface AlpinePigServer {
	void shutdown() ;
	void registerQuery(String line) throws IOException ;
	ExecJob store(String outputTempFileName, String fullPathFileName,String iteratorVarName) throws IOException ;
	Iterator<Tuple> openIterator(String tempFileName) throws IOException ;
}