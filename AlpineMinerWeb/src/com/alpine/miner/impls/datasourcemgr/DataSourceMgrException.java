package com.alpine.miner.impls.datasourcemgr;

/**
 * ClassName: DataSourceMgrException
 * <p/>
 * Data: 5/30/12
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class DataSourceMgrException extends Exception {

    public DataSourceMgrException(DataSourceMgrExceptionType type) {
        super(type.name());
    }

    public DataSourceMgrException(Throwable cause) {
        super(cause);
    }

    public static enum DataSourceMgrExceptionType{
        DUPLICATE_NAME;
    }
}
