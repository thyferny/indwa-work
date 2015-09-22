/**
 * ClassName  HadoopHDFSFileManagerFactory
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

/**
 * @author John Zhao
 */
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

public interface HadoopHDFSFileManagerFactory {
    public HadoopHDFSFileManagerFactory INSTANCE = new CachedHadoopHDFSFileManagerFactoryImpl();
    public HadoopHDFSFileManager getHadoopHDFSFileManager(HadoopConnection connection) throws Exception;
    HadoopHDFSFileManager getHadoopHDFSFileManager(HadoopConnection connection,boolean isRest) throws Exception;


}
