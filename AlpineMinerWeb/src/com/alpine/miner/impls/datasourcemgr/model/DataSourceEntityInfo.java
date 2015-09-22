package com.alpine.miner.impls.datasourcemgr.model;

/**
 * ClassName: DataSourceEntityInfo
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class DataSourceEntityInfo implements DataSourceDisplayInfo {
    private String 	key,
            label,
            parentKey;
    private boolean isCategory = false;
    private String configType;

    public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}

	public DataSourceEntityInfo(){}

    public DataSourceEntityInfo(String key, String label, String parentKey){
        this.key = key;
        this.label = label;
        this.parentKey = parentKey;
    }
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getParentKey() {
        return parentKey;
    }

    @Override
    public boolean isCategory() {
        return isCategory;
    }
}
