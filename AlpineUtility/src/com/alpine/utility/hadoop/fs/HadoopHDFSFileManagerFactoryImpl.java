/**
 * ClassName  HadoopHDFSFileManagerFactoryImpl
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

public class HadoopHDFSFileManagerFactoryImpl implements
        HadoopHDFSFileManagerFactory {
    //By default it is REST
    @Override
    public HadoopHDFSFileManager getHadoopHDFSFileManager(HadoopConnection connection) throws Exception {
       return getHadoopHDFSFileManager(connection,true);
    }

    @Override
    public HadoopHDFSFileManager getHadoopHDFSFileManager(HadoopConnection connection, boolean isRest) throws Exception {
        
    	if(isRest&&HadoopRestCallerManager.isRestEnabled()){
    		return new HadoopRestCallerManager(connection);
    	}
    	
        return new VersionedHDFSFileManagerImpl( connection);
    }

}
