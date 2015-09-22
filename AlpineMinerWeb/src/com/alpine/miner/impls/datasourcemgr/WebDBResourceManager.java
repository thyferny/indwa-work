package com.alpine.miner.impls.datasourcemgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.DataBaseMetaInfo;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;

/**
 * ClassName: WebDBResourceManager
 * <p/>
 * Data: 6/6/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class WebDBResourceManager implements DBResourceManagerIfc {
    private static Logger itsLogger = Logger.getLogger(WebDBResourceManager.class);
    private static final WebDBResourceManager INSTANCE = new WebDBResourceManager();

    private IDataSourceConnectionMgr invoker = DataSourceEnum.DB_CONNECT.getHandler();

    private WebDBResourceManager(){

    }

    public static WebDBResourceManager getInstance(){
        return INSTANCE;
    }

    @Override
    public List<DbConnectionInfo> getDBConnectionList(String userLogin) {
    	return getDBConnectionList(userLogin, ResourceType.Personal);
    }
    
    private List<DbConnectionInfo> getDBConnectionList(String userLogin, ResourceType type){
        List<DataSourceDisplayInfo> dbConns;
        List<DbConnectionInfo> dbConnectionList;
        try {
            dbConns = invoker.getCategory(type, userLogin).getSubItems();
        } catch(DataSourceMgrException e) {
            itsLogger.error(e.getMessage(),e);
            return null;
        }
        dbConnectionList = new ArrayList<DbConnectionInfo>();
        transforDBConnections(dbConns, dbConnectionList);
        return dbConnectionList;
    }

    private void transforDBConnections(List<DataSourceDisplayInfo> from, List<DbConnectionInfo> to){
        for(DataSourceDisplayInfo item : from){
            if(item.isCategory()){
                DataSourceCategory category = (DataSourceCategory)item;
                transforDBConnections(category.getSubItems(), to);
            }else{
                try {
                    to.add(transforDBConnection(item));
                } catch(DataSourceMgrException e) {
                    itsLogger.error(e.getMessage(),e);
                }
            }
        }
    }
    private DbConnectionInfo transforDBConnection(DataSourceDisplayInfo from) throws DataSourceMgrException {
        return (DbConnectionInfo) invoker.loadConnectionConfig(from.getKey());
    }

    @Override
    public void createDBConnection(DbConnectionInfo info) throws OperationFailedException {
        Persistence.INSTANCE.storeDbConnectionInfo(info);
    }

    @Override
    public void updateDBConnection(DbConnectionInfo info) throws DataOutOfSyncException {
        Persistence.INSTANCE.storeDbConnectionInfo(info);
    }

    @Override
    public void deleteDBConnection(DbConnectionInfo info) throws Exception {
        Persistence.INSTANCE.deleteDbConnectionInfo(info);
    }

    @Override
    public List<DbConnectionInfo> getDBConnectionListByPath(String path) {
        List<DbConnectionInfo> list = new LinkedList<DbConnectionInfo>();
        if(path.equals("Public")){
            list.addAll(Persistence.INSTANCE.loadDbConnectionInfo(ResourceInfo.ResourceType.Public, ""));
        }
        else if(path.startsWith("Personal")){
            String user=path.substring(path.indexOf(File.separator) +1,path.length()) ;
            list.addAll(Persistence.INSTANCE.loadDbConnectionInfo(ResourceInfo.ResourceType.Personal, user));
        }
        else if(path.startsWith("Group")){
            String group=path.substring(path.indexOf(File.separator) +1,path.length()) ;
            list.addAll(Persistence.INSTANCE.loadDbConnectionInfo(
                    ResourceInfo.ResourceType.Group, group));


        }
        return list;
    }

    @Override
    public List<JDBCDriverInfo> getJDBCDriverInfos() {
        List<JDBCDriverInfo> list = new LinkedList<JDBCDriverInfo>();
        list.addAll(Persistence.INSTANCE.getJDBCDriverInfos( ));
        return list;
    }

    @Override
    public void createJDBCDriverInfos(JDBCDriverInfo info) {
        Persistence.INSTANCE.createJDBCDriverInfo(info) ;
    }

    @Override
    public void initJDBCDriverInfo() {
//		List<JDBCDriverInfo> infos = getJDBCDriverInfos();
        String path = FilePersistence.JDBC_DRIVER_PREFIX
                +   "Public";
        path = path + File.separator  ;
        AlpineUtil.setJarFileDir(path) ;
//		for (Iterator iterator = infos.iterator(); iterator.hasNext();) {
//			JDBCDriverInfo jdbcDriverInfo = (JDBCDriverInfo) iterator.next();
//
//				return;
//		}
    }

    @Override
    public DbConnectionInfo getDBConnection(String userName, String connName, ResourceInfo.ResourceType resourceType) throws Exception {
        if(resourceType==null){
            resourceType = ResourceInfo.ResourceType.Personal;
        }
        List<DbConnectionInfo> conns= getDBConnectionList(userName);
        switch(resourceType){
        case Public:
        	conns = Persistence.INSTANCE.loadDbConnectionInfo(ResourceInfo.ResourceType.Public, "");
        	break;
        case Group:
        	conns = getDBConnectionList(userName, ResourceType.Group);
        	break;
        case Personal:
        	conns = getDBConnectionList(userName);
        }
        if(conns!=null){
            for (Iterator iterator = conns.iterator(); iterator.hasNext();) {
                DbConnectionInfo dbConnectionInfo = (DbConnectionInfo) iterator
                        .next();
                if(dbConnectionInfo.getId().equals(connName)){
                    if(resourceType!=null&&resourceType.equals(dbConnectionInfo.getResourceType())) {
                        return dbConnectionInfo;
                    }
                }
            }
        }
        //not found
        throw new Exception("DB Connection "+connName+" not found in " +resourceType) ;
    }

    @Override
    public String[] getSchemaList(String userName, String dbConnectionName, ResourceInfo.ResourceType dbType) throws Exception {
        return getSchemaList(userName, dbConnectionName, dbType, false);
    }

    public String[] getSchemaList(String userName, String dbConnectionName, ResourceInfo.ResourceType dbType, boolean refresh) throws Exception {
        DbConnectionInfo info = getDBConnection(userName, dbConnectionName,dbType);
        if(refresh){
    		DBMetadataManger.INSTANCE.refreshDBConnection(info.getConnection(), false, true);
        }
        DataBaseMetaInfo dbInfo = DBMetadataManger.INSTANCE.getDataBaseMetaInfo(info.getConnection()) ;
        if(dbInfo!=null){
            List<String> sl = dbInfo.getSchemaNameList();
            if(sl!=null){
                return sl.toArray(new String[sl.size()]);
            }
        }
        return new String[0];
    }

    @Override
    public String[] getTableList(String userName, String dbConnName, String schemaName, ResourceInfo.ResourceType dbType) throws Exception {
        return getTableList(userName, dbConnName, schemaName, dbType, false);
    }
    
    public String[] getTableList(String userName, String dbConnName, String schemaName, ResourceInfo.ResourceType dbType, boolean refresh) throws Exception {
        DbConnectionInfo info = getDBConnection(userName, dbConnName,dbType);
        if(refresh){
            DBMetadataManger.INSTANCE.refreshSchemaTables(info.getConnection(), schemaName, true);
            DBMetadataManger.INSTANCE.refreshSchemaViews(info.getConnection(), schemaName, true);
        }
        List<String> tableList = DBMetadataManger.INSTANCE.getTableAndViewNameList(info.getConnection(),schemaName);
        if(tableList!=null){
            return tableList.toArray(new String[tableList.size()]);
        }

        return new String[0];
    }

    @Override
    public List<TableColumnMetaInfo> loadColumnList(String userName, String connName, String schemaName, String tableName, ResourceInfo.ResourceType resourceType) throws Exception {
        DbConnectionInfo info = getDBConnection(userName, connName,resourceType);
        List<TableColumnMetaInfo> columnInfoList = DBMetadataManger.INSTANCE.getTableColumnInfoList(
                info.getConnection(),   schemaName,   tableName);

        return columnInfoList;
    }
}
