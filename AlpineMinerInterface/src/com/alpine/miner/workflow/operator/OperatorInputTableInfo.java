
package com.alpine.miner.workflow.operator;

import java.util.ArrayList;
import java.util.List;


public class OperatorInputTableInfo {
	  
 
	@Override
	public int hashCode() {
		return super.hashCode();
//		MINER-1992
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((connectionName == null) ? 0 : connectionName.hashCode());
//		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
//		result = prime * result + ((system == null) ? 0 : system.hashCode());
//		result = prime * result + ((table == null) ? 0 : table.hashCode());
//		result = prime * result
//				+ ((tableType == null) ? 0 : tableType.hashCode());
//		result = prime * result + ((url == null) ? 0 : url.hashCode());
//		result = prime * result
//				+ ((username == null) ? 0 : username.hashCode());
//		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
//		OperatorInputTableInfo other = (OperatorInputTableInfo) obj;
//		if (connectionName == null) {
//			if (other.connectionName != null)
//				return false;
//		} else if (!connectionName.equals(other.connectionName))
//			return false;
//		if (schema == null) {
//			if (other.schema != null)
//				return false;
//		} else if (!schema.equals(other.schema))
//			return false;
//		if (system == null) {
//			if (other.system != null)
//				return false;
//		} else if (!system.equals(other.system))
//			return false;
//		if (table == null) {
//			if (other.table != null)
//				return false;
//		} else if (!table.equals(other.table))
//			return false;
//		if (tableType == null) {
//			if (other.tableType != null)
//				return false;
//		} else if (!tableType.equals(other.tableType))
//			return false;
//		if (url == null) {
//			if (other.url != null)
//				return false;
//		} else if (!url.equals(other.url))
//			return false;
//		if (username == null) {
//			if (other.username != null)
//				return false;
//		} else if (!username.equals(other.username))
//			return false;
		return false;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
 
	//0 columnName ,1 columnType
	private List<String[]> fieldColumns= null; 
	
	public List<String[]> getFieldColumns() {
		return fieldColumns;
	}
	public void setFieldColumns(List<String[]> fieldColumns) {
		this.fieldColumns = fieldColumns;
	}

	public void addFieldColumns(String[] fieldColumn){
		if(fieldColumns==null){
			fieldColumns=new ArrayList<String[]>();
		}
		fieldColumns.add(fieldColumn);
	}
	public String getConnectionName() {
		return connectionName;
	}
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	private String connectionName;
	private String url;
	private String schema ;
	private String table;
	private String tableType;
	private String username;
	private String password;
	private String system;
	private String operatorUUID;
	private String useSSL = "false"; //default value. 

	public void putColumnType(String columnName,String type){
		if(fieldColumns==null){
			fieldColumns= new ArrayList<String[]>();
		}
		fieldColumns.add(new String[]{columnName, type}) ;
	}
	@Override
	public OperatorInputTableInfo clone() throws CloneNotSupportedException {
		OperatorInputTableInfo operatorInputTableInfo=new OperatorInputTableInfo();
		operatorInputTableInfo.setConnectionName(connectionName);
		operatorInputTableInfo.setPassword(password);
		operatorInputTableInfo.setSchema(schema);
		operatorInputTableInfo.setSystem(system);
		operatorInputTableInfo.setTable(table);
		operatorInputTableInfo.setTableType(tableType);
		operatorInputTableInfo.setUrl(url);
		operatorInputTableInfo.setUsername(username);
		operatorInputTableInfo.setUseSSL(useSSL);
		operatorInputTableInfo.setOperatorUUID(operatorUUID);
		List<String[]> newFieldColumns=new ArrayList<String[]>();
		for(String[] fieldColumn:getFieldColumns()){
			newFieldColumns.add(new String[]{fieldColumn[0],fieldColumn[1]});
		}
		operatorInputTableInfo.setFieldColumns(newFieldColumns);
		
		return operatorInputTableInfo;
	}
	
	public void setOperatorUUID(String uuid) {
		this.operatorUUID= uuid;
		
	}
	public String getOperatorUUID() {
		return operatorUUID;
	}
	public String getUseSSL() {
		return useSSL;
	}
	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OperatorInputTableInfo [fieldColumns=");
		builder.append(fieldColumns);
		builder.append(", connectionName=");
		builder.append(connectionName);
		builder.append(", url=");
		builder.append(url);
		builder.append(", schema=");
		builder.append(schema);
		builder.append(", table=");
		builder.append(table);
		builder.append(", tableType=");
		builder.append(tableType);
		builder.append(", username=");
		builder.append(username);
		builder.append(", system=");
		builder.append(system);
		builder.append(", operatorUUID=");
		builder.append(operatorUUID);
		builder.append(", useSSL=");
		builder.append(useSSL);
		builder.append("]");
		return builder.toString();
	}
 
}
