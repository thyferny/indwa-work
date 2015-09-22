package com.alpine.miner.impls.datasourcemgr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DataSourceCategory
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class DataSourceCategory implements DataSourceDisplayInfo {

    private String 	key,
            label,
            parentKey;
    private boolean isCategory = true;

    private List subItems = new ArrayList();

    public DataSourceCategory() {}

    public DataSourceCategory(String key, String label, String parentKey){
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

    public List<DataSourceDisplayInfo> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<DataSourceDisplayInfo> subItems) {
        this.subItems = subItems;
    }
}
