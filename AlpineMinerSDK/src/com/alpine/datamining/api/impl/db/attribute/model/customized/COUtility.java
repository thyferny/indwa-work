/**
 * ClassName CustomizedOperatorUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.customized;

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;

public class COUtility {
	
	public static final String OPERATOR = "operator";
	public static final String UDF = "udf";
	public static final String UDF_L = addAngleBrackets(UDF);
	public static final String UDF_SCHEMA = "schema";
	public static final String UDF_NAME = "name";
	public static final String UDF_NAME_L = addAngleBrackets(UDF_NAME);
	public static final String OPERATOR_NAME = "operatorname";
	public static final String OPERATOR_NAME_L = addAngleBrackets(OPERATOR_NAME);
	public static final String LANG = addAngleBrackets("lang");
	public static final String OUTPUT = "output";
	public static final String OUTPUT_L = addAngleBrackets(OUTPUT);
	public static final String COLUMN = "column";
	public static final String COLUMN_L = addAngleBrackets(COLUMN);
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_TYPE_L = addAngleBrackets(COLUMN_TYPE);
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_NAME_L = addAngleBrackets(COLUMN_NAME);
	public static final String PARAMETERS = "parameters";
	public static final String PARAMETERS_L = addAngleBrackets(PARAMETERS);
	public static final String PARAMETER = "parameter";
	public static final String PARAMETER_L = addAngleBrackets(PARAMETER);
	public static final String PARA_NAME = "name";
	public static final String PARA_NAME_L = addAngleBrackets(PARA_NAME);
	public static final String PARA_POSITION = "position";
	public static final String PARA_POSITION_L = addAngleBrackets(PARA_POSITION);
	public static final String PARA_DEFAULT_VALUE = "defaultvalue";
	public static final String PARA_DATA_TYPE = "datatype";
	public static final String PARA_OPTION_VALUE = "optionalvalue";
	public static final String CUMSTOMIZED = "Customized";
 
	
//	public static final String MODEL_SUFFIX = ".cm";
	public static final String OPERATOR_FILE = "customizedoperator"+Resources.minerEdition+".xml";
	
	public static final String CURRENTDIRECTORY=AlpineUtil.getCurrentDirectory()+"configuration"+File.separator;
//	private static final String CURRENTDIRECTORY=System.getProperty("java.io.tmpdir");
//	public static final String MODEL_PATH = CURRENTDIRECTORY+CustomizedOperator.class.getSimpleName()+Resources.Version;
	public static final String XML_PATH = CURRENTDIRECTORY+OPERATOR_FILE;
	
	
	public static String addAngleBrackets(String str){
		StringBuilder sb=new StringBuilder();
		sb.append("<").append(str).append(">");
		return sb.toString();
	}
	public static String xmlToString(Document xmlDoc)
	throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		Locale local = Locale.getDefault();
		String loc="GB2312";
		if (local.equals(Locale.CHINA)) {
			loc="GB2312";
		} else {
			loc="UTF-8";
		}
		
		Source source = new DOMSource(xmlDoc);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, loc);
		transformer.transform(source, result);
		String out=stringWriter.getBuffer().toString();
		return out;
}
}
