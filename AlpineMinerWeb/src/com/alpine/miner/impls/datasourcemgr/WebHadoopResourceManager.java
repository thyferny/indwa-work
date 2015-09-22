/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WebHadoopResourceManager
 * Mar 29, 2012
 */
package com.alpine.miner.impls.datasourcemgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;


/**
 * @author Gary
 *
 */
public class WebHadoopResourceManager implements HadoopConnectionManagerIfc {
    private static Logger itsLogger = Logger.getLogger(WebHadoopResourceManager.class);

    private static WebHadoopResourceManager instance = new WebHadoopResourceManager();
	
	private IDataSourceConnectionMgr invoker = DataSourceEnum.HADOOP_CONNECT.getHandler();
	
	private WebHadoopResourceManager(){}
	
	public static WebHadoopResourceManager getInstance(){
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#getAllHadoopConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public List<HadoopConnection> getAllHadoopConnection(String userName,
                                                         ResourceType type) throws Exception {
        List<DataSourceDisplayInfo> hadoopConnInfoList = invoker.getCategory(type, userName).getSubItems();
		List<HadoopConnection> hadoopConnections = new ArrayList<HadoopConnection>();
        transforHadoopConnections(hadoopConnInfoList, hadoopConnections);
		return hadoopConnections;
	}
	
	private void transforHadoopConnections(List<DataSourceDisplayInfo> from, List<HadoopConnection> to){
		for(DataSourceDisplayInfo item : from){
			if(item.isCategory()){
                DataSourceCategory category = (DataSourceCategory)item;
                transforHadoopConnections(category.getSubItems(), to);
			}else{
                try {
                    to.add(transforHadoopConnection(item));
                } catch(DataSourceMgrException e) {
                    itsLogger.error(e.getMessage(),e);
                }
			}
		}
	}

    private HadoopConnection transforHadoopConnection(DataSourceDisplayInfo from) throws DataSourceMgrException {
        HadoopConnectionInfo hadoopConnInfo = (HadoopConnectionInfo) invoker.loadConnectionConfig(from.getKey());
        return hadoopConnInfo.getConnection();
    }



	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#readHadoopConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public HadoopConnection readHadoopConnection(String connName, String username)
			throws Exception {
        if(connName == null){
            throw new NullPointerException("connName cannot be null");
        }
        List<HadoopConnection> hcList = getAllHadoopConnection(username, ResourceType.Personal);
        for(HadoopConnection item : hcList){
            if(connName.equals(item.getConnName())){
                return item;
            }
        }
		return null;
	}



    /* (non-Javadoc)
      * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#readHadoopConnection(java.lang.String, java.lang.String, java.lang.String)
      */
	@Override
	public HadoopConnection readHadoopConnection(String connName, String username, ResourceType type) throws Exception {

        if(connName == null){
            throw new NullPointerException("connName cannot be null");
        }
        List<HadoopConnection> hcList = getAllHadoopConnection(username, type);
        for(HadoopConnection item : hcList){
            if(connName.equals(item.getConnName())){
                return item;
            }
        }
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#refreshHadoopConnection(com.alpine.util.hadoop.HadoopConnection, java.lang.String, boolean)
	 */
	@Override
	public boolean refreshHadoopConnection(HadoopConnection connection, String username, boolean isRecursive) {

		return false;
	}

	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#refreshHadoopFiles(com.alpine.util.hadoop.HadoopConnection, java.lang.String, com.alpine.util.hadoop.HadoopFile, boolean)
	 */
	@Override
	public boolean refreshHadoopFiles(HadoopConnection connection, String username, HadoopFile file, boolean isRecursive) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#saveHadoopConnection(java.util.Properties)
	 */
	@Override
	public String saveHadoopConnection(Properties props, String userName) throws Exception {
		HadoopConnection connection = new HadoopConnection(props);
		HadoopConnectionInfo hci = new HadoopConnectionInfo();
		hci.setConnection(connection);
		hci.setResourceType(ResourceType.Personal);
		hci.setId(connection.getConnName());
		hci.setModifiedUser(userName);
		hci.setCreateUser(userName);
		invoker.saveConnectionConfig(hci);
		return connection.getConnName();
	}


	/* (non-Javadoc)
	 * @see com.alpine.util.hadoop.HadoopResourceManagerIfc#updateHadoopConnectionResource(java.io.File, java.util.Properties)
	 */
	@Override
	public boolean updateHadoopConnectionResource(File file, Properties props)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}

