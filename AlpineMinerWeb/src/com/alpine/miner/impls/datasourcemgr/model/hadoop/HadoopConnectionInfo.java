package com.alpine.miner.impls.datasourcemgr.model.hadoop;

import com.alpine.miner.impls.resource.DataSourceInfo;
import com.alpine.utility.hadoop.HadoopConnection;

/**
 * ClassName: HadoopConnectionInfo
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class HadoopConnectionInfo extends DataSourceInfo {
    private HadoopConnection connection;

	/**
	 * @return the connection
	 */
	public HadoopConnection getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(HadoopConnection connection) {
		this.connection = connection;
	}

}
