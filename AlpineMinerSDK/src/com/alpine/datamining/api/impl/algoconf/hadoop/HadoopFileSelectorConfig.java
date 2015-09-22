/**
 * ClassName  DBTableSelectorCongif.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.utility.hadoop.HadoopConnection;

/**
 * @author John Zhao
 *
 */
public class HadoopFileSelectorConfig extends AbstractAnalyticConfig {


	public static final String NAME_HD_connetionName =HadoopConnection.KEY_CONNNAME;
	public static final String NAME_HD_fileName ="hadoopFileName";
 
	public static final String NAME_HD_format ="hadoopFileFormat";
	public static final String NAME_HD_fileStructure ="hadoopFileStructure";
	 
	
	public static final String NAME_HD_hdfsHostname= HadoopConnection.KEY_HDFS_HOSTNAME   ;
	public static final String NAME_HD_hdfsPort= HadoopConnection.KEY_HDFS_PORT ;
	
	public static final String NAME_HD_jobHostname= HadoopConnection.KEY_JOB_HOSTNAME   ;
	public static final String NAME_HD_jobPort= HadoopConnection.KEY_JOB_PORT ;
	
	public static final String NAME_HD_version= HadoopConnection.KEY_VERSION ;
	public static final String NAME_HD_user=HadoopConnection.KEY_USERNAME ;
	public static final String NAME_HD_group=HadoopConnection.KEY_GROUPNAME ;
	
	
	public static final String NAME_HD_securityMode = HadoopConnection.KEY_SECURITY_MODE ;
	public static final String NAME_HD_mapredPrincipal =  HadoopConnection.KEY_MAPRED_PRINCIPAL  ;
	public static final String NAME_HD_mapredKeyTab = HadoopConnection.KEY_MAPRED_KEYTAB  ;
	public static final String NAME_HD_hdfsPrincipal= HadoopConnection.KEY_HDFS_PRINCIPAL  ;
	public static final String NAME_HD_hdfsKeyTab = HadoopConnection.KEY_HDFS_KEYTAB  ;

	
	private String hdfsHostname;
	private String hdfsPort;
	private String jobHostname;
	private String jobPort;
	private String hadoopVersion;
	private String groupName; 
	
	
	
	
	public String getGroupName() {
		return groupName;
	}


	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	private String userName ;
	
 	private String connName;
 	private String hadoopFileName;
 	private String hadoopFileFormat;
 	private AnalysisFileStructureModel hadoopFileStructure;
 	 

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(NAME_HD_connetionName);
		parameters.add(NAME_HD_fileName);
		parameters.add(NAME_HD_format);
		
		parameters.add(NAME_HD_hdfsHostname);
		parameters.add(NAME_HD_hdfsPort);
		parameters.add(NAME_HD_jobHostname);
		parameters.add(NAME_HD_jobPort);
		parameters.add(NAME_HD_user);
		parameters.add(NAME_HD_group);
		parameters.add(NAME_HD_version);
		//belowing is for kerberos
		
		parameters.add(NAME_HD_securityMode );
		parameters.add(NAME_HD_mapredPrincipal );
		parameters.add(NAME_HD_mapredKeyTab );
		parameters.add(NAME_HD_hdfsPrincipal);
		parameters.add(NAME_HD_hdfsKeyTab );
	}
	
	public String getSecurityMode() {
		return securityMode;
	}


	public void setSecurityMode(String securityMode) {
		this.securityMode = securityMode;
	}


	public String getHdfsPrincipal() {
		return hdfsPrincipal;
	}


	public void setHdfsPrincipal(String hdfsPrincipal) {
		this.hdfsPrincipal = hdfsPrincipal;
	}


	public String getHdfsKeyTab() {
		return hdfsKeyTab;
	}


	public void setHdfsKeyTab(String hdfsKeyTab) {
		this.hdfsKeyTab = hdfsKeyTab;
	}


	public String getMapredPrincipal() {
		return mapredPrincipal;
	}


	public void setMapredPrincipal(String mapredPrincipal) {
		this.mapredPrincipal = mapredPrincipal;
	}


	public String getMapredKeyTab() {
		return mapredKeyTab;
	}


	public void setMapredKeyTab(String mapredKeyTab) {
		this.mapredKeyTab = mapredKeyTab;
	}


	public HadoopFileSelectorConfig(){
		super();
		setParameterNames(parameters);
	}
	
	private String securityMode = HadoopConnection.SECURITY_MODE_SIMPLE; // simple or kerberos
	private String hdfsPrincipal = null;  
	private String hdfsKeyTab = null;  
	private String mapredPrincipal = null;  	
	private String mapredKeyTab = null;  
	
	public String getConnName() {
		return connName;
	}

	public void setConnName(String connName) {
		this.connName = connName;
	}


	public String getHadoopFileName() {
		return hadoopFileName;
	}

	public void setHadoopFileName(String hadoopFileName) {
		this.hadoopFileName = hadoopFileName;
	}
	
	
	public String getHadoopVersion() {
		return hadoopVersion;
	}


	public void setHadoopVersion(String hadoopVersion) {
		this.hadoopVersion = hadoopVersion;
	}


	public String getHdfsHostname() {
		return hdfsHostname;
	}

	public void setHdfsHostname(String hdfsHostname) {
		this.hdfsHostname = hdfsHostname;
	}

	public String getHdfsPort() {
		return hdfsPort;
	}

	public void setHdfsPort(String hdfsPort) {
		this.hdfsPort = hdfsPort;
	}

	public String getJobHostname() {
		return jobHostname;
	}

	public void setJobHostname(String jobHostname) {
		this.jobHostname = jobHostname;
	}

	public String getJobPort() {
		return jobPort;
	}

	public void setJobPort(String jobPort) {
		this.jobPort = jobPort;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getHadoopFileFormat() {
		return hadoopFileFormat;
	}

	public void setHadoopFileFormat(String hadoopFileFormat) {
		this.hadoopFileFormat = hadoopFileFormat;
	}

	public AnalysisFileStructureModel getHadoopFileStructure() {
		return hadoopFileStructure;
	}

	public void setHadoopFileStructure(
			AnalysisFileStructureModel hadoopFileStructure) {
		this.hadoopFileStructure = hadoopFileStructure;
	}
	
}
