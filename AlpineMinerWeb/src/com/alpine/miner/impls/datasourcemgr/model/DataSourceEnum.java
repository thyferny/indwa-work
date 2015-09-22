package com.alpine.miner.impls.datasourcemgr.model;

import com.alpine.miner.impls.datasourcemgr.IDataSourceConnectionMgr;
import com.alpine.miner.impls.datasourcemgr.impl.dbconn.DBConnectionMgrFileImpl;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.HadoopConnectionMgrFileImpl;

public enum DataSourceEnum {

	DB_CONNECT(new DBConnectionMgrFileImpl()),
	HADOOP_CONNECT(new HadoopConnectionMgrFileImpl());
	
	private IDataSourceConnectionMgr datasourceMgrHandler;
	
	private DataSourceEnum(IDataSourceConnectionMgr datasourceMgrHandler){
		this.datasourceMgrHandler = datasourceMgrHandler;
	}
	
	public IDataSourceConnectionMgr getHandler(){
		return datasourceMgrHandler;
	}
}
