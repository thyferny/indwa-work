package com.alpine.miner.impls.datasourcemgr.impl.hadoop;


import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.impl.AbstractDataSourceMgrFileImpl;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEntityInfo;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.DataSourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

/**
 * ClassName: HadoopConnectionMgrFileImpl
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class HadoopConnectionMgrFileImpl extends AbstractDataSourceMgrFileImpl implements IHadoopConnectionFeatcher {

    private static final File ROOT_FOLDER;

    static{
        ROOT_FOLDER = new File(FilePersistence.HADOOP_PREFIX);
        if(!ROOT_FOLDER.exists()){
            ROOT_FOLDER.mkdirs();
        }
    }

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher#getAvailbleHadoopDisplayInfoSet(java.lang.String)
	 */
	@Override
	public List<DataSourceDisplayInfo> getAvailbleHadoopDisplayInfoSet(
			String userSign) {
		String personalKey = ResourceType.Personal.toString();
		 File personalRootFile = new File(ROOT_FOLDER, personalKey + File.separator + userSign);
		 DataSourceCategory personalHadoopConnCategory = (DataSourceCategory) getDataSourceDisplay(personalRootFile, personalKey, null);
		 return personalHadoopConnCategory.getSubItems();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher#getHadoopConnection(java.lang.String)
	 */
	@Override
	public HadoopConnectionInfo getHadoopConnection(String key) {
		File configFile = new File(ROOT_FOLDER, key);
        return buildConfigInfo(configFile);
	}

    @Override
    public void saveConnectionConfig(DataSourceInfo config) throws DataSourceMgrException {
    	HadoopConnectionInfo hci = (HadoopConnectionInfo) config;
        String fileName = FilePersistence.INSTANCE.generateResourceKey(config) + HadoopConnection.file_suffix;
        File file = new File(fileName);
        if(file.exists()){
            throw new DataSourceMgrException(DataSourceMgrException.DataSourceMgrExceptionType.DUPLICATE_NAME);
        }
        Persistence.INSTANCE.storeHadoopConnectionInfo(hci);
    }

    @Override
    public void updateConnectionConfig(DataSourceInfo config) throws DataSourceMgrException {
    	HadoopConnectionInfo hci = (HadoopConnectionInfo) config;
        hci.setModifiedTime(System.currentTimeMillis());
        Persistence.INSTANCE.storeHadoopConnectionInfo(hci);
    }

    @Override
    public void removeConnectionConfig(String key) throws DataSourceMgrException {
        File configFile = new File(ROOT_FOLDER, key);
        configFile.delete();
    }

    @Override
    public ResourceInfo loadConnectionConfig(String key) throws DataSourceMgrException {
        return getHadoopConnection(key);
    }

    @Override
    public boolean testConnection(DataSourceInfo config, Locale locale) throws DataSourceMgrException {
    	HadoopConnectionInfo hci = (HadoopConnectionInfo) config;
    	try {
			return HadoopHDFSFileManager.INSTANCE.testConnection(hci.getConnection());
		} catch (Exception e) {
			throw new DataSourceMgrException(e );
		}
    }

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher#getHadoopEntityInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public DataSourceEntityInfo getHadoopEntityInfo(String userSign,
			String connectionName) {
		String userKey = super.buildKey(userSign, ResourceType.Personal.toString());
		String key = super.buildKey(connectionName + HadoopConnection.file_suffix, userKey);
		return new DataSourceEntityInfo(key, connectionName, userKey);
	}

	@Override
	protected File returnRootFolder() {
		return ROOT_FOLDER;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.datasourcemgr.impl.AbstractDataSourceMgrFileImpl#getDataSourceType(java.io.File)
	 */
	@Override
	protected String getDataSourceType(File configFile) {
		return DataSourceInfo.DATASOURCE_TYOE_HADDOP;
	}
    
    private HadoopConnectionInfo buildConfigInfo(File file){
    	HadoopConnectionInfo config = new HadoopConnectionInfo();
        Properties props = FilePersistence.INSTANCE.readProperties(file);
        FilePersistence.INSTANCE.getResourceFromProperties(config, props);
        FilePersistence.INSTANCE.generateResourceKey(config);
        HadoopConnection conn = new HadoopConnection(props);
     // match with part of setHadoopProperties in Filepersistence
    //  in order to avoid props name conflict with group name in ResourceInfo
        conn.setGroupName(props.getProperty("Hadoop_" + HadoopConnection.KEY_GROUPNAME));
        config.setConnection(conn);
        config.setDatasourceType(DataSourceInfo.DATASOURCE_TYOE_HADDOP);
        return config;
    }
}
