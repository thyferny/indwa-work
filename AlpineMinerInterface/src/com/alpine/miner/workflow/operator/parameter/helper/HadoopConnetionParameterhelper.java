package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.hadoop.HadoopConnection;

public class HadoopConnetionParameterhelper extends SingleSelectParameterHelper {

	@Override
	 
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) {
		 
		List<String> result=new ArrayList<String>(); 
		
		//if user is null, will get all public connections
		HadoopConnectionManagerIfc hdManager = HadoopConnectionManagerFactory.INSTANCE.getManager();
		
		List<HadoopConnection> hdConnections=null;
		try {
			hdConnections = hdManager.getAllHadoopConnection(userName, dbType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Iterator<HadoopConnection> iterator = hdConnections.iterator(); iterator.hasNext();) {
			HadoopConnection hdConnectionInfo = iterator.next();
			result.add(hdConnectionInfo.getConnName());
		}
		return result;
	}
}
