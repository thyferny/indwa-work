/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ResourceManager.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.editworkflow.flow.FlowDraftManager;
import com.alpine.miner.impls.editworkflow.flow.FlowDraftManager.DraftInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.hadoop.*;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import org.apache.log4j.Logger;

/**
 * @author sam_zang 
 */
public class ResourceFlowManager {
	public static ResourceFlowManager instance = new ResourceFlowManager();
    private static Logger itsLogger = Logger.getLogger(ResourceFlowManager.class);

    static ResourceFlowManager getInstance() {
		return instance;
	}

	private ResourceFlowManager() {
		// flow key-> workflow
		flowCacheMap = new HashMap<String, OperatorWorkFlow>();
		flowInfoTable = new HashMap<String, List<FlowInfo>>();
		rwl = new ReentrantReadWriteLock();
		persistence = new FilePersistence();
	}
	// flow key-> workflow
	private HashMap<String, OperatorWorkFlow> flowCacheMap;
	private HashMap<String, List<FlowInfo>> flowInfoTable;
	private ReentrantReadWriteLock rwl;
	private Persistence persistence;

 

	/**
	 * @param category
	 * @return
	 */
	public List<FlowInfo> getFlowList(String category) {
		List<FlowInfo> list = null;
		rwl.readLock().lock();
		try {
			list = flowInfoTable.get(category);
			if (list != null) {
				return list;
			}
		} finally {
			rwl.readLock().unlock();
		}

		// If it gets here then UseerInfo is not in the cache
		rwl.writeLock().lock();
		try {
			// load DBConnectionInfo from persistence
			list = flowInfoTable.get(category);
			if (list == null) {
				list = persistence.loadFlowInfo(category);
				if (list != null) {
					flowInfoTable.put(category, list);
				}
			}
			if (list != null) {
				return list;
			}
		} finally {
			rwl.writeLock().unlock();
		}

		return new LinkedList<FlowInfo>();
	}

	/**
	 * @param flowInfo
	 * @param locale 
	 * @return
	 * @throws OperationFailedException 
	 */
	public OperatorWorkFlow getFlowData(FlowInfo flowInfo, Locale locale) throws OperationFailedException {
		OperatorWorkFlow flow =   flowCacheMap.get(flowInfo.getKey());
		if (flow != null) {
			return flow;
		}
		// If it gets here then flow is not in the cache
		rwl.writeLock().lock();
		try {
			// load flowInfo from persistence
			flow = persistence.readWorkFlow(flowInfo,locale);
			//currently we only cache the personal flow
			if (flow != null&&flowInfo.getResourceType()==ResourceType.Personal) {
				rebuildWorkflow(flow);
				flowCacheMap.put(flowInfo.getKey(), flow);
			}
 
		} finally {
			rwl.writeLock().unlock();
		}
		return flow;
	}
	
	// called when load workflow. To rebuild workflow information.
	private void rebuildWorkflow(OperatorWorkFlow workflow){
		for(UIOperatorModel operatorModel : workflow.getChildList()){
			if(operatorModel.getOperator() instanceof SubFlowOperator){
				// if exit operator is a hadoop operator we need disconnect between this sub-flow operator and its next operators.
				SubFlowOperator subflowOperator = (SubFlowOperator) operatorModel.getOperator();
				Operator exitOperator = subflowOperator.getExitOperator();
				if(exitOperator instanceof HadoopOperator){
					HadoopOperator hadoopExitOperator = (HadoopOperator) exitOperator;
					OperatorParameter storeParameter = hadoopExitOperator.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults);
					if(storeParameter != null && Boolean.FALSE.equals(Boolean.valueOf((String)storeParameter.getValue()))){
						List<UIConnectionModel> targetConnections = subflowOperator.getOperModel().getTargetConnection();
						for(int i = 0;i < targetConnections.size();i++){
							UIConnectionModel targetConnection = targetConnections.get(i);
							Operator nextOperator = targetConnection.getTarget().getOperator();
							if(nextOperator instanceof HadoopLinearRegressionOperator
									|| nextOperator instanceof CopyToDBOperator
									|| nextOperator instanceof HadoopKmeansOperator
									|| nextOperator instanceof HadoopLogisticRegressionOperator
									|| nextOperator instanceof HadoopLinearRegressionPredictOperator
									|| nextOperator instanceof HadoopLogisticRegressionPredictOperator
                                    || nextOperator instanceof HadoopNaiveBayesPredictOperator
                                    || nextOperator instanceof HadoopNaiveBayesOperator
                                    || nextOperator instanceof HadoopROCOperator
									|| nextOperator instanceof HadoopLiftOperator
                                    || nextOperator instanceof HadoopConfusionOperator
                                    || nextOperator instanceof HadoopGoodnessOfFitOperator
									|| nextOperator instanceof HadoopDecisionTreeOperator
									|| nextOperator instanceof HadoopDecisionTreePredictOperator
									|| nextOperator instanceof HadoopVariableSelectionAnalysisOperator){
								targetConnection.detachSource();
								targetConnection.detachTarget();
								workflow.getConnModelList().remove(targetConnection);
								i--;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Check last modified time stamp. If the cached copy
	 * is newer, throw exception.
	 * 
	 * @param flowInfo
	 * @param flow
	 * @throws Exception 
	 */
	public void updateFlow(FlowInfo flowInfo, OperatorWorkFlow flow) throws Exception {
		if ( flowInfo.getResourceType()==ResourceType.Personal) {
			flowCacheMap.put(persistence.generateResourceKey(flowInfo), flow)  ;
			//we push workflow into draft if it has updated.
			FlowDraftManager.getInstance().pushDraft(flowInfo.getModifiedUser(), flowInfo, flow);
		}
	}

	/**
	 * @param flowInfo
	 * @throws Exception 
	 */
	public void deleteFlow(FlowInfo flowInfo) throws Exception {
		rwl.writeLock().lock();
		try {
			//copy to history--MINERWEB-308
			//flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo) ;
			//FlowVersionManager.INSTANCE.copyFlowToHistory(flowInfo);
			//delete all
			// update cache and persistence
			persistence.deleteFlowInfo(flowInfo);
			FlowVersionManager.INSTANCE.deleteFlowhistory(flowInfo) ;
			flowCacheMap.remove(flowInfo.getKey());
			DraftInfo draftInfo = FlowDraftManager.getInstance().getDraft(flowInfo.getModifiedUser());
			if(draftInfo != null && draftInfo.getFlowInfo().getKey().equals(flowInfo.getKey())){
				//we need clear draft pool when user delete same flow.
				FlowDraftManager.getInstance().clearDraft(flowInfo.getModifiedUser());
			}
			flowInfoTable.clear();	
		} catch ( Exception e) {
			 
			
		 itsLogger.error(e.getMessage(),e);
			e.printStackTrace();
			throw e;
		} finally {
			rwl.writeLock().unlock();
		}
		EventNotifier.sendEvent(flowInfo, EventNotifier.EventType.FlowDelete);
	}

 

	/**
	 * @param flowInfo
	 * @throws IOException 
	 */
	public void saveFlowXMLData(FlowInfo flowInfo) throws  Exception {
		rwl.writeLock().lock();
		try {
			persistence.storeFlowInfoAndData(flowInfo);	
			//it is save, so have to reload the cache
			reloadFlowCache(flowInfo );
			flowInfoTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
		EventNotifier.sendEvent(flowInfo, EventNotifier.EventType.FlowCreate);
	}

	private void reloadFlowCache(FlowInfo flowInfo) throws OperationFailedException {
		if(!flowCacheMap.containsKey(flowInfo.getKey())){
			return;//we only reload flows which has been loaded.
		}
		OperatorWorkFlow flow = persistence.readWorkFlow(flowInfo,Locale.getDefault());
		//currently we only cache the personal flow
		if (flow != null&&flowInfo.getResourceType()==ResourceType.Personal) {
			flowCacheMap.put(flowInfo.getKey(), flow);
		}
		
	}

	/**
	 * @param flowInfo
	 * @throws IOException 
	 */
	public void saveFlowInfoData(FlowInfo flowInfo) throws IOException {
		rwl.writeLock().lock();
		try {
			persistence.storeFlowInfo(
					persistence.generateResourceKey(flowInfo) + FilePersistence.INF, flowInfo);
 
		 
			flowInfoTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
		EventNotifier.sendEvent(flowInfo, EventNotifier.EventType.FlowCreate);
	}
	
	/**
	 * @param flowInfo
	 * @return
	 */
	public boolean flowExists(FlowInfo flowInfo) {
		String filePath = Persistence.INSTANCE.generateResourceKey(flowInfo) + FilePersistence.INF;
		File file = new File(filePath) ;
		return file.exists();
	}


    /**
     * @param src
     * @param dest
     * @return
     * @throws Exception
     */
    public boolean moveFlow(FlowInfo src, FlowInfo dest) throws Exception {

        rwl.writeLock().lock();
        try {

            // update cache and persistence
            persistence.loadXMLFlowData(src);

            dest.setVersion(src.getVersion());

            FlowInfo tempDest = dest;
            tempDest.setXmlString(src.getXmlString());
            tempDest.setModifiedTime(System.currentTimeMillis());


            String comments= src.getComments();
            if(comments==null){
                comments="" ;
            }
            tempDest.setComments(comments);
            persistence.storeFlowInfoAndData(tempDest);
            flowInfoTable.clear();
        } catch (Exception e) {
            throw e;
        } finally {
            rwl.writeLock().unlock();
        }

        //we need to copy over the history
        FlowVersionManager.INSTANCE.copyHistoryToNewName(src,dest);


        deleteFlow(src);


        EventNotifier.sendEvent(dest, EventNotifier.EventType.FlowCreate);

        return true;
    }


    /**
	 * @param src
	 * @param dest
	 * @return
	 * @throws Exception 
	 */
	public boolean copyFlow(FlowInfo src, FlowInfo dest) throws Exception {
	
		rwl.writeLock().lock();
		try {
			
			// update cache and persistence
			
			
			persistence.loadXMLFlowData(src);	
		
			//copy is a new start of version
			//MINERWEB-308 will find the deleted old versio add keep the version...
		 
			FlowInfo tempDest = dest;
			if (flowExists(tempDest) == true) {
				FlowInfo tempDest4Version = FlowVersionManager.INSTANCE.relaodFlowInfo(dest) ;
			 
				int newVersion = Integer.parseInt(tempDest4Version.getVersion()) + 1;

				FlowVersionManager.INSTANCE.copyFlowToHistory(tempDest4Version);
				//update current flow
				tempDest.setVersion(String.valueOf(newVersion));
				

			}else  {
				 if( FlowVersionManager.INSTANCE.hasHistoryVersion(tempDest)==true){
						String version =  FlowVersionManager.INSTANCE.getLatestFlowVersion(tempDest);
						if(version!=null){
							String newVersion = String.valueOf(Integer.parseInt(version) +1);
							tempDest.setVersion(newVersion) ;
						}else{
							tempDest.setVersion(FlowInfo.INIT_VERSION) ;
						}
					 		 
					
				}else{//brand new one
					tempDest.setVersion(FlowInfo.INIT_VERSION);
				}
			}
			tempDest.setXmlString(src.getXmlString());
			tempDest.setModifiedTime(System.currentTimeMillis());
			
			
			String comments= src.getComments();
			if(comments==null){
				comments="" ;
			}
			tempDest.setComments(comments);
			persistence.storeFlowInfoAndData(tempDest);	
			
 
			
			flowInfoTable.clear();	
			//this is for copy and open use...
			dest.setVersion(tempDest.getVersion()) ;
		} catch (Exception e) {
			throw e;
		} finally {
			rwl.writeLock().unlock();
		}
		EventNotifier.sendEvent(dest, EventNotifier.EventType.FlowCreate);

		return true;
	}

 

	/**
	 * @param flowInfo
	 * @throws Exception 
	 */
	public void updateFlowFinish(FlowInfo flowInfo, OperatorWorkFlow flow) throws Exception {
		flowInfo.setModifiedTime(System.currentTimeMillis());
		rwl.writeLock().lock();
		try {
			// update cache and persistence
			
			persistence.saveFlow(flowInfo, flow) ;	 
			flowCacheMap.remove(flowInfo.getKey());
			flowInfoTable.clear();	
		} finally {
			rwl.writeLock().unlock();
		}
		EventNotifier.sendEvent(flowInfo, EventNotifier.EventType.FlowUpdate);
	}

	/**
	 * @param flowInfo
	 * @throws Exception 
	 */
	public void updateFlowCancel(FlowInfo flowInfo) throws Exception {
		flowCacheMap.remove(flowInfo.getKey());
	}
	
	/**
	 * @param flowInfo
	 * @param locale 
	 * @return
	 * @throws OperationFailedException 
	 */
	public OperatorWorkFlow getFlowData4Version(FlowInfo flowInfo,String version, Locale locale) throws OperationFailedException {
		OperatorWorkFlow flow = null;  
	
		// If it gets here then flow is not in the cache
		rwl.readLock().lock();
		try {
			  String path = ResourceManager.getInstance().getFlowVersionPath(flowInfo, Integer.parseInt(version));
			  String fileName = path+ Resources.AFM;
			  // load flowInfo from persistence
			flow = persistence.getFlowData4Path(fileName,flowInfo,locale);
			 
		} finally {
			rwl.readLock().unlock();
		}
		return flow;
	}
	
 

	public void forceClearCache(String flowKey) {
		if(flowCacheMap.containsKey(flowKey)){
			flowCacheMap.remove(flowKey)  ;
		}
		
	}

	// call when user login.
	public void syncSampleFlow(String loginName) throws Exception{
		File userFlowRoot = new File(FilePersistence.FLOWPREFIX + File.separator + ResourceType.Personal.name() + File.separator + loginName);
		String sampleFolder = SysConfigManager.INSTANCE.getSampleFolder();
		if(userFlowRoot.exists() || sampleFolder == null){
			return;
		}
		List<FlowInfo> flowInfoSet = persistence.getFlowInfosFromFolder(FilePersistence.ROOT + File.separator + sampleFolder);
		String[] categories = {loginName + File.separator + "Samples"};
		for(FlowInfo flowInfo : flowInfoSet){
			FlowInfo dest = new FlowInfo(loginName, flowInfo.getId(), ResourceType.Personal);
			dest.setCategories(categories);
			this.copyFlow(flowInfo, dest);
		}
	}
}