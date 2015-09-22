/**
 * ClassName IWorkFlowReader.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/02
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.util.Locale;

import com.alpine.miner.workflow.operator.OperatorWorkFlow;

public interface IWorkFlowReader {
	
	OperatorWorkFlow doRead(AbstractReaderParameters para,Locale locale) throws Exception;

}
