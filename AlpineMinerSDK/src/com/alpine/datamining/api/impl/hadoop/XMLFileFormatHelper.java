/**
 * ClassName XMLFileFormatHelper.java
 *
 * Version information: 1.00
 *
 * Date: Oct 30, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.ext.RecordParserFactory;
import com.alpine.hadoop.ext.XMLInputFormat;

/**
 * @author John Zhao
 *
 */
public class XMLFileFormatHelper implements FileFormatHelper {

	private AnalysisXMLFileStructureModel fileStructureModel;
	private HadoopAnalyticSource hadoopSource;
	/**
	 * @param fileStructureModel
	 */
	public XMLFileFormatHelper(AnalysisXMLFileStructureModel fileStructureModel , HadoopAnalyticSource hadoopSource) {
		this.fileStructureModel = fileStructureModel;
		this.hadoopSource = hadoopSource ;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.FileFormatHelper#initHadoopConfig(org.apache.hadoop.conf.Configuration)
	 */
	@Override
	public void initHadoopConfig(Configuration hadoopConf) {
		String attributeMode = fileStructureModel.getAttrMode();
		String containerTagName = fileStructureModel.getContainer();
		String xmlDataStructureType = fileStructureModel.getXmlDataStructureType();
		String containerXPath = fileStructureModel.getContainerXPath();
		String startTag = null;
		String endTag = null;
		if(attributeMode.equals(AnalysisXMLFileStructureModel.NO_ATTRIBUTE)) {
			startTag = new String("<" + containerTagName + ">") ;
			endTag = new String("</" + containerTagName + ">") ;
		}
		else {
			startTag = new String("<" + containerTagName + " ") ;
			if(attributeMode.equals(AnalysisXMLFileStructureModel.HALF_ATTRIBUTE )){
				endTag = new String("</" + containerTagName + ">") ;

			}else{ // pure attribute mode
				endTag = new String("/>") ;

			}

		}
		
		//hadoopConf.set(AlpineHadoopConfKeySet.XML_CONTAINER_TAG_KEY,"<" + fileStructureModel.getContainer()+">");
	 
		hadoopConf.set(AlpineHadoopConfKeySet.XML_START_TAG_KEY,startTag);
		hadoopConf.set(AlpineHadoopConfKeySet.XML_END_TAG_KEY,endTag);
	 
		hadoopConf.set(AlpineHadoopConfKeySet.INPUT_FORMAT_KEY, RecordParserFactory.INPUT_FORMAT_VALUE_XML);
		
		hadoopConf.set(AlpineHadoopConfKeySet.XML_TYPE_TAG_KEY,xmlDataStructureType);
		hadoopConf.set(AlpineHadoopConfKeySet.XML_CONTAINER_PATH_TYPE_TAG_KEY,containerXPath);
		
		List<String> xpathList = fileStructureModel.getxPathList(); 
		hadoopConf.set(AlpineHadoopConfKeySet.XML_XPATH_LIST_KEY, toXPathListString(xpathList)) ;

		
	}

	/**
	 * @param xpathList
	 * @return
	 */
	private String toXPathListString(List<String> xpathList) {
		StringBuilder sb = new StringBuilder();
		for (Iterator iterator = xpathList.iterator(); iterator.hasNext();) {
			String xpath = (String) iterator.next();
			sb.append(xpath).append(AlpineHadoopConfKeySet.VALUE_XPATH_DELIMITER) ;
		}
		if(sb.length()>0){
			sb = sb.deleteCharAt(sb.length()-1) ; 
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.FileFormatHelper#setInputFormatClass(org.apache.hadoop.mapreduce.Job)
	 */
	@Override
	public void setInputFormatClass(Job job) {
		job.setInputFormatClass(XMLInputFormat.class) ;
	}

}
