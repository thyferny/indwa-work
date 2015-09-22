package com.alpine.utility.hadoop.pig;



import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.executionengine.ExecJob;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.relationalOperators.POStore;
import org.apache.pig.data.Tuple;
import org.apache.pig.tools.pigstats.PigStats;

public class AlpinePigExecJob implements ExecJob {
	private Exception pigException;
	ExecJob.JOB_STATUS status;
	public AlpinePigExecJob(Exception err){
		pigException=err;
		if(null==err){
			status=ExecJob.JOB_STATUS.COMPLETED;
		}else{
			status = ExecJob.JOB_STATUS.FAILED;
		}
	}
	@Override
	public void completionNotification(Object arg0) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public String getAlias() throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public Properties getConfiguration() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public Exception getException() {
		return pigException;
	}

	@Override
	public void getLogs(OutputStream arg0) throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public POStore getPOStore() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public Iterator<Tuple> getResults() throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void getSTDError(OutputStream arg0) throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void getSTDOut(OutputStream arg0) throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public PigStats getStatistics() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public JOB_STATUS getStatus() {
		return status;
	}

	@Override
	public boolean hasCompleted() throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void kill() throws ExecException {
		throw new IllegalStateException("Not implemented");
	}

}
