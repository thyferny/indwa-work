/**
 * ClassName  CachedHadoopHDFSFileManagerFactoryImpl
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

import java.util.HashMap;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

public class CachedHadoopHDFSFileManagerFactoryImpl extends
        HadoopHDFSFileManagerFactoryImpl {

    // connection -> file manager
    private HashMap<HadoopConnection, HadoopHDFSFileManager> fileManagerMap = new HashMap<HadoopConnection, HadoopHDFSFileManager>();
 
    @Override
    public HadoopHDFSFileManager getHadoopHDFSFileManager(HadoopConnection connection) throws Exception {
        if (fileManagerMap.containsKey(connection) == false) {
            fileManagerMap.put(connection, super.getHadoopHDFSFileManager(connection));
        }
        return fileManagerMap.get(connection);
    }

}
