/**
 * 
 */
package com.alpine.miner.impls.datasourcemgr.impl.hadoop;

import java.util.List;

import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEntityInfo;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;

/**
 * ClassName: IHadoopConnectionFeatcher.java
 * <p/>
 * Data: 2012-6-7
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public interface IHadoopConnectionFeatcher {
	
	IHadoopConnectionFeatcher INSTANCE = new HadoopConnectionMgrFileImpl();

	/**
	 * retrieve availble hadoop connections by user.
	 * @param userSign
	 * @return
	 */
	List<DataSourceDisplayInfo> getAvailbleHadoopDisplayInfoSet(String userSign);
	
	/**
	 * retrieve Hadoop entity information by login name and connection name
	 * @param userSign
	 * @param connectionName
	 * @return
	 */
	DataSourceEntityInfo getHadoopEntityInfo(String userSign, String connectionName);
	
	/**
	 * get Hadoop connection information by connection key
	 * @param key from DataSourceDisplayInfo
	 * @return
	 */
	HadoopConnectionInfo getHadoopConnection(String key);
}
