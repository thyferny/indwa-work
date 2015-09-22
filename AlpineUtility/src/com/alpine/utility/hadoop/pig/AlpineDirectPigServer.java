package com.alpine.utility.hadoop.pig;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.executionengine.ExecJob;
import org.apache.pig.data.Tuple;

public class AlpineDirectPigServer implements AlpinePigServer{
	
	
	private PigServer pigServer;


	public AlpineDirectPigServer(PigServer pigServer)throws ExecException {
		if(null==pigServer){
			throw new IllegalArgumentException("PigServer must not be null");
		}
		this.pigServer = pigServer;
	}
	
	public AlpineDirectPigServer(ExecType local) throws ExecException {
		pigServer=new PigServer(ExecType.LOCAL);
	}

	@Override
	public void shutdown() {
		pigServer.shutdown();
		
	}

	@Override
	public void registerQuery(String query) throws IOException {
		pigServer.registerQuery(query);
		
	}

	@Override
	public ExecJob store(String outputTempFileName, String fullPathFileName,
			String pigStorageFunction) throws IOException {
		return pigServer.store(outputTempFileName, fullPathFileName, pigStorageFunction);
	}

	@Override
	public Iterator<Tuple> openIterator(String tempFileName) throws IOException {
		return pigServer.openIterator(tempFileName);
	}

}
