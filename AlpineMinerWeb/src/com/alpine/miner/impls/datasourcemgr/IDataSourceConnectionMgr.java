package com.alpine.miner.impls.datasourcemgr;

import java.util.Locale;

import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.resource.DataSourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo;

/**
 * ClassName: IDataSourceConnectionMgr
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public interface IDataSourceConnectionMgr {

    /**
     * retrieve category information by giving resource type and user signture
     * @param type
     * @param userSignture required if type = group or personal
     * @return
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    DataSourceCategory getCategory(DataSourceInfo.ResourceType type, String userSignture) throws DataSourceMgrException;

    /**
     * save connection information
     * @param config
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    void saveConnectionConfig(DataSourceInfo config) throws DataSourceMgrException;

    /**
     * update connection information
     * @param config
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    void updateConnectionConfig(DataSourceInfo config) throws DataSourceMgrException;

    /**
     * remove information by giving key <br/>
     * @param key must be from DataSourceDisplayInfo.key
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    void removeConnectionConfig(String key) throws DataSourceMgrException;

    /**
     * load connection information by giving key
     * @param key must be from DataSourceDisplayInfo.key
     * @return
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    ResourceInfo loadConnectionConfig(String key) throws DataSourceMgrException;

    /**
     * test whether able to connect for giving resource
     * @param config
     * @param locale
     * @return
     * @throws com.alpine.miner.impls.datasourcemgr.DataSourceMgrException
     */
    boolean testConnection(DataSourceInfo config, Locale locale) throws DataSourceMgrException;
}
