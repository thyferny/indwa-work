package com.alpine.miner.impls.datasourcemgr.model;

/**
 * ClassName: DataSourceDisplayInfo
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public interface DataSourceDisplayInfo {

    /**
     * return the data source key
     * @return
     */
    String getKey();

    /**
     * return the data source display label text
     * @return
     */
    String getLabel();

    /**
     * return the data source parent's key
     * @return
     */
    String getParentKey();

    /**
     * return true if it is a category
     * @return
     */
    boolean isCategory();
}
