package com.alpine.miner.impls.datasourcemgr.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.IDataSourceConnectionMgr;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEntityInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.security.impl.ProviderFactory;

/**
 * ClassName: AbstractDataSourceConnectionMgrImpl
 * <p/>
 * Data: 5/31/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public abstract class AbstractDataSourceMgrFileImpl implements IDataSourceConnectionMgr {

	/**
	 * child class need to implement this method to return root folder where stored all of configuration file.
	 * @return
	 */
	protected abstract File returnRootFolder();
	
	protected abstract String getDataSourceType(File configFile);
	
	@Override
	public DataSourceCategory getCategory(ResourceType type, String userSignture)
			throws DataSourceMgrException {

        File typeFolder;
        String subPath = type.name();
        String parentKey = "";
        FileFilter filter = null;
        switch(type){
            case Group:
                typeFolder = new File(returnRootFolder(), subPath);
                final String[] groups = ProviderFactory.getAuthenticator("").getUserGroups(userSignture);
                filter = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        for(String group : groups){
                            if(group.equals(pathname.getName())){
                                return true;
                            }
                        }
                        return false;
                    }
                };
                break;
            case Personal:
                subPath += (File.separator + userSignture);
                parentKey = type.name();
            case Public:
                typeFolder = new File(returnRootFolder(), subPath);
                break;
            default:
                throw new UnsupportedOperationException("Unsupport type: " + type);
        }
        if(!typeFolder.exists()){
            typeFolder.mkdirs();
        }
        return (DataSourceCategory) getDataSourceDisplay(typeFolder, parentKey, filter);
    }

    @Override
	public void removeConnectionConfig(String key)
			throws DataSourceMgrException {
        File configFile = new File(returnRootFolder(), key);
        configFile.delete();
	}

	protected final DataSourceDisplayInfo getDataSourceDisplay(File file, String parentKey, FileFilter filter){
        String key = buildKey(file.getName(), parentKey);
        String label = file.getName();
        if(file.isDirectory()){
            DataSourceCategory category = new DataSourceCategory(key, label, parentKey);
            List<DataSourceDisplayInfo> children = category.getSubItems();
            File[] files = filter == null ? file.listFiles() : file.listFiles(filter);
            for(File child : files){
                children.add(getDataSourceDisplay(child, key, null));
            }
            return category;
        }else{
            int extIndex = label.lastIndexOf(".");
            if (extIndex != -1 ) {
                label = label.substring(0,label.lastIndexOf(".")); //get simple file name
            }
            DataSourceEntityInfo entity = new DataSourceEntityInfo(key, label, parentKey);
            entity.setConfigType(getDataSourceType(file));
            return entity;
        }
    }

    protected final String buildKey(String name, String parentKey){
        return parentKey + File.separator + name;
    }

}
