package com.alpine.miner.impls.datasourcemgr.impl.dbconn;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.impl.AbstractDataSourceMgrFileImpl;
import com.alpine.miner.impls.resource.DataSourceInfo;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.IDataSourceInfo;

/**
 * ClassName: DBConnectionMgrFileImpl
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class DBConnectionMgrFileImpl extends AbstractDataSourceMgrFileImpl {

    private static final File ROOT_FOLDER;
    
    private static Persistence store = Persistence.INSTANCE;

    static{
        ROOT_FOLDER = new File(FilePersistence.DBPREFIX);
        if(!ROOT_FOLDER.exists()){
            ROOT_FOLDER.mkdirs();
        }
    }
    
    @Override
    public void saveConnectionConfig(DataSourceInfo config) throws DataSourceMgrException {
    	DbConnectionInfo dbInfo = (DbConnectionInfo) config;
        String fileName = FilePersistence.INSTANCE.generateResourceKey(config) + FilePersistence.INF;
        File file = new File(fileName);
        if(file.exists()){
            throw new DataSourceMgrException(DataSourceMgrException.DataSourceMgrExceptionType.DUPLICATE_NAME);
        }
    	store.storeDbConnectionInfo(dbInfo);
    }

    @Override
    public void updateConnectionConfig(DataSourceInfo config) throws DataSourceMgrException {
    	DbConnectionInfo dbInfo = (DbConnectionInfo) config;
//    	if (store.hasBeenUpdated(dbInfo) == true) {
//			// throw new DataOutOfSyncException(info.getId());
//		}
    	dbInfo.setModifiedTime(System.currentTimeMillis());
		store.storeDbConnectionInfo(dbInfo);
    }

    @Override
    public ResourceInfo loadConnectionConfig(String key) throws DataSourceMgrException {
        File configFile = new File(ROOT_FOLDER, key);
        Properties props = FilePersistence.INSTANCE.readProperties(configFile);
        DbConnectionInfo dbInfo = new DbConnectionInfo();
    	dbInfo.setConnection(new DbConnection(props));
    	FilePersistence.INSTANCE.getResourceFromProperties(dbInfo, props);
        FilePersistence.INSTANCE.generateResourceKey(dbInfo);
        dbInfo.setDatasourceType(DataSourceInfo.DATASOURCE_TYOE_DATABASE);
        return dbInfo;
    }

    @Override
    public boolean testConnection(DataSourceInfo config, Locale locale) throws DataSourceMgrException {
    	DbConnectionInfo dbInfo = (DbConnectionInfo) config;
    	IDataSourceInfo connInfo = DataSourceInfoFactory.createConnectionInfo(dbInfo.getConnection().getDbType());
    	connInfo.setLocale(locale);
    	// in order to initialize url attribute.
		DbConnection targetConn = new DbConnection(
				dbInfo.getConnection().getDbType(), 
				dbInfo.getConnection().getHostname(), 
				dbInfo.getConnection().getPort(), 
				dbInfo.getConnection().getDbname(), 
				dbInfo.getConnection().getDbuser(), 
				dbInfo.getConnection().getPassword(), 
				dbInfo.getConnection().getJdbcDriverFileName(),
				dbInfo.getConnection().getUseSSL());
    	boolean isValidate;
    	try {
    		isValidate = connInfo.checkDBConnection(targetConn);
		} catch (Exception e) {
			throw new DataSourceMgrException(e);
		}
        return isValidate;
    }

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.datasourcemgr.impl.AbstractDataSourceMgrFileImpl#removeConnectionConfig(java.lang.String)
	 */
	@Override
	public void removeConnectionConfig(String key)
			throws DataSourceMgrException {
        File configFile = new File(ROOT_FOLDER, key);
        configFile.delete();
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
		return DataSourceInfo.DATASOURCE_TYOE_DATABASE;
	}
}
