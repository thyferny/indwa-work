package com.alpine.miner.workflow.operator;

import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.utility.hadoop.HadoopConnection;

public class OperatorInputFileInfo {
	//basic info 
	private String connectionName;
	private String hadoopFileName;
	private String hadoopFileFormat;
	private FileStructureModel columnInfo;
	
	//connection info
	private String hdfsHostname;
	private String hdfsPort;
	private String jobHostname;
	private String jobPort;
	private String version;
 
	private String user;
	private String group;
    private String operatorUUID;

    
	
	private String securityMode = HadoopConnection.SECURITY_MODE_SIMPLE; // simple or kerberos
	private String hdfsPrincipal = null;  
	private String hdfsKeyTab = null;  
	private String mapredPrincipal = null;  	 
	private String mapredKeyTab = null; 
	
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

	public String getOperatorUUID() {
        return operatorUUID;
    }

    public void setOperatorUUID(String operatorUUID) {
        this.operatorUUID = operatorUUID;
    }

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	private boolean isDir;
	
  
	
	
	public OperatorInputFileInfo(){
		
	}
	
	public String getConnectionName() {
		return connectionName;
	}
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
 
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
  
  
 
 
	public OperatorInputFileInfo(String connectionName, String hdfsHostname,
			String hdfsPort, String jobHostname, String jobPort,
			String version, String hadoopFileName, String hadoopFileFormat , String user,String group
			,String securityMode , String hdfsPrincipal ,String hdfsKeyTab ,
			String mapredPrincipal , String mapredKeyTab) {
		super();
		this.connectionName = connectionName;
		this.hdfsHostname = hdfsHostname;
		this.hdfsPort = hdfsPort;
		this.jobHostname = jobHostname;
		this.jobPort = jobPort;
		this.version = version;
		 this.hadoopFileName =hadoopFileName;
		 this.hadoopFileFormat =hadoopFileFormat;
		this.user = user;
		this.group=group;
		this.securityMode = securityMode;
		this.hdfsPrincipal = hdfsPrincipal;  
		this.hdfsKeyTab = hdfsKeyTab;  
		this.mapredPrincipal = mapredPrincipal;  	
		 this.mapredKeyTab = mapredKeyTab; 
	
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
  
	public String getHadoopFileName() {
		return hadoopFileName;
	}

	public void setHadoopFileName(String hadoopFileName) {
		this.hadoopFileName = hadoopFileName;
	}

	public String getHadoopFileFormat() {
		return hadoopFileFormat;
	}

	public void setHadoopFileFormat(String hadoopFileFormat) {
		this.hadoopFileFormat = hadoopFileFormat;
	}

 
	public FileStructureModel getColumnInfo() {
		return columnInfo;
	}

	public void setColumnInfo(FileStructureModel columnInfo) {
		this.columnInfo = columnInfo;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setIsDir(boolean dir) {
		this.isDir= dir;
		
	}
	public boolean isDir() {
		return isDir;
	}
	
	public OperatorInputFileInfo clone (){
		OperatorInputFileInfo clone = new OperatorInputFileInfo(connectionName, hdfsHostname, hdfsPort, jobHostname,
				jobPort, version, hadoopFileName, hadoopFileFormat, user, group
				,  securityMode ,   hdfsPrincipal ,  hdfsKeyTab ,
				  mapredPrincipal ,   mapredKeyTab);
		if(columnInfo!=null){
			 
			try {
				clone.setColumnInfo(columnInfo.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}	 
		}
		return clone;
	}
	
}
