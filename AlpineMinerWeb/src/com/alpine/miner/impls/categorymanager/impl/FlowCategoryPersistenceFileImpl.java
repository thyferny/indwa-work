/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * CategoryPersistenceFileImpl
 * Feb 17, 2012
 */
package com.alpine.miner.impls.categorymanager.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence;
import com.alpine.miner.impls.categorymanager.exception.FlowCategoryException;
import com.alpine.miner.impls.categorymanager.exception.FlowCategoryException.ExceptionType;
import com.alpine.miner.impls.categorymanager.model.FlowBasisInfo;
import com.alpine.miner.impls.categorymanager.model.FlowCategory;
import com.alpine.miner.impls.categorymanager.model.FlowDisplayModel;
import com.alpine.miner.impls.flowHistory.FlowHistoryInfo;
import com.alpine.miner.impls.flowHistory.FlowHistoryServiceFactory;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceFlowManager;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.resource.Persistence;
import org.apache.log4j.Logger;

/**
 * @author Gary
 *
 */
public class FlowCategoryPersistenceFileImpl implements IFlowCategoryPersistence {

    private static Logger itsLogger = Logger.getLogger(FlowCategoryPersistenceFileImpl.class);
    private File CATEGORY_ROOT;
	
	private static final String FLOW_SEPARATOR = "/";
	
//	private static final String FLOW_BASIS_INFO_SUFFIX = "?";
	
	public FlowCategoryPersistenceFileImpl(){
		File flowRoot = new File(FilePersistence.FLOWPREFIX + File.separator + ResourceType.Personal.name());
		if(!flowRoot.exists()){
			flowRoot.mkdirs();
		}
		CATEGORY_ROOT = flowRoot;
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.ICategoryPersistence#createCategory(com.alpine.miner.impls.categorymanager.model.Category)
	 */
	@Override
	public void createCategory(final FlowCategory category)throws FlowCategoryException {
		File parentFolder = new File(CATEGORY_ROOT, category.getParentKey());
		if(!parentFolder.exists()){
			parentFolder.mkdirs();
		}else if(!parentFolder.isDirectory()){
			throw new FlowCategoryException(ExceptionType.PARENT_ISNOT_FOLDER);
		}
		String[] duplicateNames = parentFolder.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.equals(category.getName());
			}
		});
		if(duplicateNames.length > 0){
			throw new FlowCategoryException(ExceptionType.NAME_ALREADY_EXISTS);
		}
		new File(parentFolder.getPath() + File.separator + category.getName()).mkdir();
		category.setKey(category.getParentKey() + FLOW_SEPARATOR + category.getName());
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.ICategoryPersistence#getChildren(com.alpine.miner.impls.categorymanager.model.Category)
	 */
	@Override
	public FlowCategory buildRootCategory(String userSign)throws FlowCategoryException {
		FlowCategory rootCategory = new FlowCategory(userSign, userSign);
		fillChildrenCategoryAndFlow(rootCategory);
		return rootCategory;
	}
	

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence#getAllCategory(java.lang.String)
	 */
	@Override
	public List<FlowCategory> getChildrenCategory(String parentKey)
			throws FlowCategoryException {
		File parentFolder = new File(CATEGORY_ROOT + File.separator + parentKey);
		List<FlowCategory> categories = new ArrayList<FlowCategory>();
		File[] categoryFolderArray = parentFolder.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for(File categoryFolder : categoryFolderArray){
			FlowCategory category = new FlowCategory(parentKey + FLOW_SEPARATOR + categoryFolder.getName(), categoryFolder.getName());
			category.setParentKey(parentKey);
			categories.add(category);
		}
		sortDisplayModelList(categories);
		return categories;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.ICategoryPersistence#removeCategory(com.alpine.miner.impls.categorymanager.model.Category)
	 */
	@Override
	public void removeCategory(FlowCategory category)throws FlowCategoryException {
		File directory = new File(CATEGORY_ROOT, category.getKey());
		List<FlowInfo> flowInfoList = ResourceManager.getInstance().getFlowList(ResourceType.Personal.name() + File.separator + category.getKey());
		for(FlowInfo item : flowInfoList){
			try {
				ResourceFlowManager.instance.deleteFlow(item);
			} catch (Exception e) {
				e.printStackTrace();
				throw new FlowCategoryException(ExceptionType.SOME_FLOW_IN_CATEGORY);
			}
			FlowHistoryServiceFactory.getService(item.getModifiedUser()).removeFlowHistory(new FlowHistoryInfo(item));
		}
		deleteFile(directory);
		deleteFile(new File(FilePersistence.FLOW_VERSION_PREFIX + ResourceType.Personal.name() + File.separator + category.getKey()));
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence#moveFlow(com.alpine.miner.impls.categorymanager.model.FlowCategory, com.alpine.miner.impls.categorymanager.model.FlowCategory, com.alpine.miner.impls.web.resource.FlowInfo[])
	 */
	@Override
	public Map<String, FlowDisplayModel> moveFlow(FlowCategory target, FlowDisplayModel[] flowmodelArray)
			throws FlowCategoryException {
		String rootPath = CATEGORY_ROOT.getPath() + File.separator;
		Map<String, FlowDisplayModel> successModelMap = new HashMap<String, FlowDisplayModel>();
		
		for(FlowDisplayModel model : flowmodelArray){
			File 	fromAFM = new File(rootPath + model.getPath() + FilePersistence.AFM),
					toAFM = new File(rootPath + target.getKey() + File.separator + model.getName() + FilePersistence.AFM),
					fromINF = new File(rootPath + model.getPath() + FilePersistence.INF),
					toINF = new File(rootPath + target.getKey() + File.separator + model.getName() + FilePersistence.INF);
			//move afm
			try {
				moveFile(fromAFM, toAFM);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.warn(e.getMessage());
				continue;
			}
			//move inf
			try {
				moveFile(fromINF, toINF);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.warn(e.getMessage());
				//if move inf file failed, then move flow file back.
				try {
					moveFile(toAFM, fromAFM);
				} catch (Exception e1) {
					e1.printStackTrace();
					itsLogger.warn(e1.getMessage());
				}
			}
			//update category field in inf file.
			updateFlowCategory(toINF, target.getKey(), true);
			//move history of flow to same folder with flow.
			moveHistoryOfFlow(model, target);

			FlowBasisInfo movedFlowBasisInfo = new FlowBasisInfo();
			movedFlowBasisInfo.setKey(model.getKey());
			movedFlowBasisInfo.setPath(target.getKey() + FLOW_SEPARATOR + model.getName());
			movedFlowBasisInfo.setParentKey(target.getKey());
			successModelMap.put(model.getKey(), movedFlowBasisInfo);
		}
		return successModelMap;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.ICategoryPersistence#updateCategory(com.alpine.miner.impls.categorymanager.model.Category)
	 */
	@Override
	public void updateCategory(final FlowCategory category)throws FlowCategoryException {
		File parentFolder = new File(CATEGORY_ROOT, category.getParentKey());
		String[] duplicateFile = parentFolder.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.equals(category.getName());
			}
		});
		if(duplicateFile.length != 0){
			throw new FlowCategoryException(ExceptionType.NAME_ALREADY_EXISTS);
		}
		File categoryFolder = new File(CATEGORY_ROOT, category.getKey()),
			 newCategoryFolder = new File(parentFolder, category.getName()),
			 historyFolder = new File(FilePersistence.FLOW_VERSION_PREFIX + ResourceType.Personal.name() + File.separator + category.getKey()),
			 newHistoryFolder = new File(FilePersistence.FLOW_VERSION_PREFIX + ResourceType.Personal.name() + File.separator + category.getParentKey() + File.separator + category.getName());
		boolean isSuccess = categoryFolder.renameTo(newCategoryFolder);
		if(isSuccess){
			if(historyFolder.exists()){
				isSuccess = historyFolder.renameTo(newHistoryFolder);
				if(!isSuccess){
					newCategoryFolder.renameTo(categoryFolder);
					itsLogger.error("the history folder rename failed, and rename " + category.getKey() + " back also failed.");
//					throw new FlowCategoryException(ExceptionType.RENAME_FAILED);
				}
			}
		}else{
			itsLogger.error("Rename " + category.getKey() + " failed.");
		}
		updateBasisModelPath(newCategoryFolder, category.getParentKey() + File.separator + category.getName(), true);
		if(newHistoryFolder.exists()){
			updateBasisModelPath(newHistoryFolder, category.getParentKey() + File.separator + category.getName(), false);
		}
		category.setKey(category.getParentKey() + FLOW_SEPARATOR + category.getName());
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence#getAllFlowInfo(java.lang.String)
	 */
	@Override
	public List<FlowBasisInfo> getAllFlowInfo(String userSign)
			throws FlowCategoryException {
		FlowCategory root = buildRootCategory(userSign);
		List<FlowBasisInfo> result = filterFlow(root);
		sortDisplayModelList(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence#getFlowAbsolutelyPath(java.lang.String)
	 */
	@Override
	public String getFlowAbsolutelyPath(String flowBasisKey) throws Exception {
		String rootPath = CATEGORY_ROOT.getPath() + File.separator;
		File flowAMF = new File(rootPath + flowBasisKey + FilePersistence.AFM);
//		InputStream is = null;
//		ByteArrayOutputStream bos = null;
//		try {
//			is = new FileInputStream(flowAMF);
//			bos = new ByteArrayOutputStream(is.available());
//			byte[] buffer = new byte[1024];
//			int length;
//			while((length = is.read(buffer)) != -1){
//				bos.write(buffer,0,length);
//			}
//		} catch (Exception e) {
//			throw e;
//		}finally{
//			if(is != null)
//				is.close();
//		}
		return flowAMF.getAbsolutePath();
	}
	
	private List<FlowBasisInfo> filterFlow(FlowCategory category){ 
		List<FlowBasisInfo> result = new LinkedList<FlowBasisInfo>();
		List subItems = category.getSubItems();
		for(int i = 0;i < subItems.size();i++){
			Object item = subItems.get(i);
			if(item instanceof FlowCategory){
				List<FlowBasisInfo> models = filterFlow((FlowCategory) item);
				result.addAll(models);
			}else{
				FlowBasisInfo fbi = (FlowBasisInfo) item;
				result.add((FlowBasisInfo) fbi);
			}
		}
		return result;
	}
	
	private void fillChildrenCategoryAndFlow(FlowCategory parentCategory){
		File directory = new File(CATEGORY_ROOT, parentCategory.getKey());
		List<FlowInfo> flowInfoList = ResourceManager.getInstance().getFlowList(ResourceType.Personal.name() + File.separator + parentCategory.getKey());
		List<FlowDisplayModel> flowModelList = new ArrayList<FlowDisplayModel>();//for sort flow.
		
		if(!directory.exists()){
			directory.mkdirs();
		}
		File[] subCategories = directory.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for(File subCategory : subCategories){
			FlowCategory category = new FlowCategory();
			List<FlowDisplayModel> subModelArray = new ArrayList<FlowDisplayModel>();
			category.setSubItems(subModelArray);
			category.setKey(parentCategory.getKey() + FLOW_SEPARATOR + subCategory.getName());//build category key
			category.setName(subCategory.getName());
			category.setParentKey(parentCategory.getKey());
			fillChildrenCategoryAndFlow(category);
			
			parentCategory.getSubItems().add(category);
		}
		sortDisplayModelList(parentCategory.getSubItems());//sort category
		
		
		for(FlowInfo flow : flowInfoList){
			FlowBasisInfo basis = new FlowBasisInfo();
			basis.setInfo(flow);
			basis.setKey(flow.getId() + flow.getCreateTime());
			basis.setPath(parentCategory.getKey() + FLOW_SEPARATOR + flow.getId());
			basis.setName(flow.getId());
			basis.setParentKey(parentCategory.getKey());
			flowModelList.add(basis);
		}
		sortDisplayModelList(flowModelList);
		parentCategory.getSubItems().addAll(flowModelList);
	}
	
	private void deleteFile(File parent){
		if(parent.isDirectory()){
			File[] children = parent.listFiles();
			if(children == null){
				return;
			}
			for(File item : children){
				deleteFile(item);
			}
		}
		parent.delete();
	}
	
	private void moveHistoryOfFlow(FlowDisplayModel flowModel, FlowCategory targetCategory){
		FlowInfo orignInfo = new FlowInfo(),
				 targetInfo = new FlowInfo();
		orignInfo.setCategories(new String[]{flowModel.getParentKey()});
		orignInfo.setResourceType(ResourceType.Personal);
		orignInfo.setId(flowModel.getName());
		targetInfo.setCategories(new String[]{targetCategory.getKey()});
		targetInfo.setResourceType(ResourceType.Personal);
		targetInfo.setId(flowModel.getName());
		
		
		
		File orignVersionFolder = new File(Persistence.INSTANCE.getVersionFolderPath(orignInfo)),
			 targetVersionFolder = new File(Persistence.INSTANCE.getVersionFolderPath(targetInfo));
		boolean isMoved = moveFolder(orignVersionFolder, targetVersionFolder);
		//update categories field with history flow
		if(isMoved){
			updateBasisModelPath(targetVersionFolder, targetCategory.getKey(), false);
		}
	}
	
	
	/**
	 * move orignFolder into targetFolder
	 * @param orignFolder
	 * @param newFolder
	 * @return return true if execute move.
	 */
	private boolean moveFolder(File orignFolder, File newFolder){
		if(newFolder.exists()){
			// if targetVersion folder exist, then remove it.
			deleteFile(newFolder);
//			throw new RuntimeException(newFolder.getAbsolutePath() + " already exists.");
		}
		if(!orignFolder.exists()){
			//have no history
			return false;
		}
		newFolder.mkdirs();
		for(File moveFile : orignFolder.listFiles()){
			File ouputFile = new File(newFolder, moveFile.getName()); 
			try {
				moveFile(moveFile, ouputFile);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.warn(e.getMessage());
			}
		}
		deleteFile(orignFolder);
		return true;
	}
	
	/*
	 * move file
	 */
	private void moveFile(File orignFile, File outputFile) throws Exception{
//		FileInputStream fis = null;
//		FileOutputStream fos = null;
//		try {
//			fis = new FileInputStream(orignFile);
//			if(!outputFile.exists()){
//				outputFile.createNewFile();
//			}
//			fos = new FileOutputStream(outputFile);
//			FileChannel orign = fis.getChannel();
//			orign.transferTo(0, orign.size(), fos.getChannel());
//			
//		} catch (Exception e) {
//			throw e;
//		} finally{
//			try {
//				if(fis != null)
//					fis.close();
//				if(fos != null)
//					fos.close();
//			} catch (IOException e) {
//				//ignore.
//			}
//		}
		FileUtils.copyFile(orignFile, outputFile);
		orignFile.delete();
	}
	
	/*
	 * update flows categories
	 */
	private void updateBasisModelPath(File directory, String path, boolean isFlow){
		File[] subFileArray = directory.listFiles();
		if(subFileArray == null){
			return;
		}
		for(File subFile : subFileArray){
			if(subFile.isDirectory()){
				updateBasisModelPath(subFile, path, isFlow);
			}else if(subFile.getAbsolutePath().endsWith(FilePersistence.INF)){
				updateFlowCategory(subFile, path, isFlow);
			}
		}
	}
	
	private void updateFlowCategory(File flowINF, String targetFlowCategory, boolean isFlow){

		Properties flowProp = FilePersistence.INSTANCE.readProperties(flowINF);
		if(isFlow){
			FlowInfo newFlow = new FlowInfo();
			Persistence.INSTANCE.getResourceFromProperties(newFlow, flowProp);
			
			FlowHistoryServiceFactory.getService(newFlow.getModifiedUser()).removeFlowHistory(new FlowHistoryInfo(newFlow));
			//remove resource type and user folder from category
			newFlow.setCategories(new String[]{targetFlowCategory}); 
			try {
				ResourceFlowManager.instance.saveFlowInfoData(newFlow);
			} catch (IOException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
			}
			//update cache for old flow info
			 
			Persistence.INSTANCE.getResourceFromProperties(newFlow, flowProp);
			//any opened flow will be saved before catehgory update
			ResourceFlowManager.instance.forceClearCache(newFlow.getKey()) ;
			
			
		}else{//flow history, only update the catetory in the file, 
			flowProp.setProperty(ResourceInfo.CATEGORIES, targetFlowCategory);
			OutputStreamWriter oStream = null; 
			try {
				oStream = new OutputStreamWriter(new FileOutputStream(flowINF),Persistence.ENCODING);
				flowProp.store(oStream, "");
			} catch (IOException e) {
				itsLogger.error(e.getMessage(),e);
			} finally {
				if (oStream != null) {
					try {
						oStream.close();
					} catch (IOException e) {
						// ignore close error.
					}
				}
			}
		}
	}
	
	private void sortDisplayModelList(List<? extends FlowDisplayModel> list){
		Collections.sort(list, new Comparator<FlowDisplayModel>() {
			@Override
			public int compare(FlowDisplayModel o1, FlowDisplayModel o2) {
				int parentRes = o1.getParentKey().toLowerCase().compareTo(o2.getParentKey().toLowerCase());
				return parentRes == 0 ? o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()) : parentRes;
			}
		});
	}
	
}
