/**
 * ClassName HadoopAnalyticSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.AbstractAnalyticSource;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Eason
 * 
 */
public class HadoopAnalyticSource extends AbstractAnalyticSource {
	public static final String HADOOP = "hadoop";
	private HadoopConnection hadoopInfo;
//	private String path;
	private String fileFormat;
	private String fileName;
	private String sourceAliasName;

	private AnalysisFileStructureModel hadoopFileStructureModel;
	private String inputTempName;
	
	private Map<String,String> variableMap;

	public String getInputTempName() {
		return inputTempName;
	}

	public void setInputTempName(String inputTempName) {
		this.inputTempName = inputTempName;
	}

	public HadoopAnalyticSource(HadoopConnection hadoopInfo) {
		this.hadoopInfo = hadoopInfo;
	}
		
	public HadoopAnalyticSource() {
		
	}

	public String getDataSourceType() {
		return HADOOP;
	}

	public HadoopConnection getHadoopInfo() {
		return hadoopInfo;
	}

	public void setHadoopInfo(HadoopConnection hadoopInfo) {
		this.hadoopInfo = hadoopInfo;
	}

//	public String getPath() {
//		return path;
//	}
//
//	public void setPath(String path) {
//		this.path = path;
//	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	//this is default for hadoop file selector
	public AnalysisFileStructureModel getHadoopFileStructureModel() {
		return hadoopFileStructureModel;
	}

	public void setHadoopFileStructureModel(
			AnalysisFileStructureModel hadoopFileStructureModel) {
		this.hadoopFileStructureModel = hadoopFileStructureModel;
	}

	public Map<String, String> getVariableMap() {
		return variableMap;
	}

	@Override
	public void setSourceInfoByNodeIndex(XmlDocManager opTypeXmlManager,
			Node opNode, int index,Map<String,String> variableMap) {
		this.variableMap=variableMap;
		ArrayList<Node> parameterNodeList;
		ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode,
				"InPutFieldList");
		if (nodes != null && nodes.size() > 0) {
			String connectionName=null;
			String hdfshostname=null;
			String hdfsport=null;
			String jobhostname=null;
			String jobport=null;
			String userName = null;
			String groupName = null;
			String version = null;
			
			String fileName=null;
			String fileFormat=null;
			//-----------kerberos ----------------
			String securityMode = HadoopConnection.SECURITY_MODE_SIMPLE; // simple or kerberos
			  String hdfsPrincipal = null;  
			  String hdfsKeyTab = null;  
			  String mapredPrincipal = null;  	
			  String mapredKeyTab = null; 
			
	 
			

			Node inputNode = nodes.get(index);
			parameterNodeList = opTypeXmlManager.getNodeList(inputNode,
					"Parameter");
			for (Iterator<Node> iterator = parameterNodeList.iterator(); iterator
					.hasNext();) {
				Node pnode = iterator.next();

				if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_CONNNAME)) {
					connectionName = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_HDFS_HOSTNAME)) {
					hdfshostname = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_HDFS_PORT)) {
					hdfsport = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_JOB_HOSTNAME)) {
					jobhostname = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_JOB_PORT)) {
					jobport = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_VERSION)) {
					version = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_USERNAME)) {
					userName = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopConnection.KEY_GROUPNAME)) {
					groupName = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_fileName)) {
					fileName = ((Element) pnode).getAttribute("value");
				} else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_format)) {
					fileFormat = ((Element) pnode).getAttribute("value");
				}//--------kerberros
				else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_securityMode )) {
					securityMode = ((Element) pnode).getAttribute("value");
				}
				else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal )) {
					hdfsPrincipal = ((Element) pnode).getAttribute("value");
				}
				else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab )) {
					hdfsKeyTab = ((Element) pnode).getAttribute("value");
				}
				else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_mapredPrincipal )) {
					mapredPrincipal = ((Element) pnode).getAttribute("value");
				}
				else if (((Element) pnode).getAttribute("key").equals(
						HadoopFileSelectorConfig.NAME_HD_mapredKeyTab )) {
					mapredKeyTab = ((Element) pnode).getAttribute("value");
				}
			}

			List<Node> fileStructureNodeList = opTypeXmlManager.getNodeList(inputNode,
					AnalysisCSVFileStructureModel.TAG_NAME);
			if (fileStructureNodeList != null && fileStructureNodeList.size() > 0) {
				AnalysisCSVFileStructureModel fileStructureModel = AnalysisCSVFileStructureModel
						.fromXMLElement((Element) fileStructureNodeList.get(0));
				setHadoopFileStructureModel(fileStructureModel);
			}else if( opTypeXmlManager.getNodeList(inputNode,
					AnalysisXMLFileStructureModel.TAG_NAME)!=null&&opTypeXmlManager.getNodeList(inputNode,
							AnalysisXMLFileStructureModel.TAG_NAME).size()>0){
				
				AnalysisXMLFileStructureModel fileStructureModel = AnalysisXMLFileStructureModel.fromXMLElement(
						(Element) opTypeXmlManager.getNodeList(inputNode,
								AnalysisXMLFileStructureModel.TAG_NAME).get(0));
				setHadoopFileStructureModel(fileStructureModel);
			}else if( opTypeXmlManager.getNodeList(inputNode,
					AnalysisLogFileStructureModel.TAG_NAME)!=null&&opTypeXmlManager.getNodeList(inputNode,
							AnalysisLogFileStructureModel.TAG_NAME).size()>0){
				
				AnalysisLogFileStructureModel fileStructureModel = AnalysisLogFileStructureModel.fromXMLElement(
						(Element) opTypeXmlManager.getNodeList(inputNode,
								AnalysisLogFileStructureModel.TAG_NAME).get(0));
				setHadoopFileStructureModel(fileStructureModel);
			}else if( opTypeXmlManager.getNodeList(inputNode,
					AnalysisJSONFileStructureModel.TAG_NAME)!=null&&opTypeXmlManager.getNodeList(inputNode,
							AnalysisJSONFileStructureModel.TAG_NAME).size()>0){
				
				AnalysisJSONFileStructureModel fileStructureModel = AnalysisJSONFileStructureModel.fromXMLElement(
						(Element) opTypeXmlManager.getNodeList(inputNode,
								AnalysisJSONFileStructureModel.TAG_NAME).get(0));
				setHadoopFileStructureModel(fileStructureModel);
			}
			 
			
			parameterNodeList = opTypeXmlManager.getNodeList(inputNode,
					"Parameter");
			
//			setPath(path);
			setFileFormat(fileFormat);
			setFileName(fileName);
			setHadoopInfo(
					new HadoopConnection(
					connectionName,
					userName,
					groupName,
					hdfshostname,
					Integer.parseInt(hdfsport),
					version,
					jobhostname,
					Integer.parseInt(jobport),
					securityMode,
					hdfsPrincipal , 
			  hdfsKeyTab ,
			 mapredPrincipal ,  	
			 mapredKeyTab));
		}
	}

	/**
	 * @return
	 */
	public String toReportString() {
	 	StringBuffer sb= new StringBuffer();
	 
		String blank="          ";
		 
		sb.append("Data Source" +blank);
		sb.append(getHadoopInfo().getHDFSUrl()+"\n");
		
		sb.append("Hadoop File" +blank);
		sb.append(getFileName()+"\n");
		
		//TODO:toReportString
		//fix bug :pivotal 41998681  nullpoint here for timeseries 
		if(getHadoopFileStructureModel()!=null){
			sb.append(getHadoopFileStructureModel().toString() +"\n");
		}
		return sb.toString();
	}
	@Override
	public void setNameAlias(String name) {
	  this.sourceAliasName=name;
	}
	@Override
	public String getNameAlias() {
		return sourceAliasName;
	}

 

}
