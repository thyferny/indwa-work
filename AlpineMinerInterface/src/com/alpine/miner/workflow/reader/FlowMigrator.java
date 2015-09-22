/**
 * ClassName FlowMigrator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.xml.XmlDocManager;

public interface FlowMigrator {
	
	public static final String Version_1 = "1.00";
	public static final String Version_111 = "1.1.1";
	public static final String Version_23 = "2.3";
	public static final String Version_234 = "2.3.4";
	public static final String Version_25 = "2.5";
	public static final String Version_3 = "3.0";
	public static final String CURRENT_READ_VERION = Version_3;
	public static final String CURRENT_WRITE_VERION = Version_3; 
	

	List<OperatorParameter> doReadOperatorMigrator(Operator operator,XmlDocManager opTypeXmlManager,
			Element element);
	
	void doSaveOperatorMigrator(Operator operator,Document xmlDoc,Element element,String username, boolean addSuffixToOutput);

}
