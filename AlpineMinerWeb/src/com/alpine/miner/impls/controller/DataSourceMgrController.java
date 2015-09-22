package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEntityInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.security.UserInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/main/dataSource/manager.do")
public class DataSourceMgrController extends AbstractControler {

	public DataSourceMgrController() throws Exception {
		super();
	}

	@RequestMapping(params="method=loadDataSourceCategories", method=RequestMethod.GET)
	public void loadDataSourceCategories(HttpServletRequest request, HttpServletResponse response) throws Exception{
		UserInfo user = getUserInfo(request);
		Map<ResourceType, DataSourceCategory> categoriesContainer = new HashMap<ResourceType, DataSourceCategory>();
		for(ResourceType type : ResourceType.values()){
			for(DataSourceEnum dse : DataSourceEnum.values()){
				DataSourceCategory dataSourceCategory;
				try {
					dataSourceCategory = dse.getHandler().getCategory(type, user.getLogin());
				} catch (DataSourceMgrException e) {
					throw e;
				}
				DataSourceCategory typeCategory = categoriesContainer.get(type);
				if(typeCategory == null){
					typeCategory = dataSourceCategory;
					categoriesContainer.put(type, typeCategory);
				}else{
					if(type == ResourceType.Group){
						loopLoadedCategory:
						for(DataSourceDisplayInfo newItem : dataSourceCategory.getSubItems()){// for each all of categories which just has loaded 
							DataSourceCategory newGroup = (DataSourceCategory) newItem;
							for(DataSourceDisplayInfo item : typeCategory.getSubItems()){// for each all of categories which has been load. 
								DataSourceCategory group = (DataSourceCategory) item;
								if(group.getKey().equals(newGroup.getKey())){// if true means there are same category
									classifyDataSource(group, newGroup.getSubItems());
//									group.getSubItems().addAll(newGroup.getSubItems());
									continue loopLoadedCategory;
								}
							}
							// run here, means there was not contained current group in container. then join it in container.
							typeCategory.getSubItems().add(newGroup);
						}
					}else{
						classifyDataSource(typeCategory, dataSourceCategory.getSubItems());
//						typeCategory.getSubItems().addAll(dataSourceCategory.getSubItems());
					}
				}
			}
		}
		ProtocolUtil.sendResponse(response, categoriesContainer);
	}
	
	/**
	 * fill category into categories
	 * @param categories
	 * @param category
	 */
	private void classifyDataSource(DataSourceCategory parent, List<DataSourceDisplayInfo> newlyDataSourceList){
		if(parent.getSubItems().size() > 0 && parent.getSubItems().get(0) instanceof DataSourceEntityInfo){// already has a type of datasource series.
			DataSourceEntityInfo fistDsEntity = (DataSourceEntityInfo) parent.getSubItems().get(0);
			DataSourceCategory dsCategory = new DataSourceCategory(parent.getKey() + "_" + fistDsEntity.getConfigType(), fistDsEntity.getConfigType(), parent.getKey());
			List<DataSourceDisplayInfo> datasourceCategoryList = new ArrayList<DataSourceDisplayInfo>();
			for(DataSourceDisplayInfo datasourceItem : parent.getSubItems()){
				dsCategory.getSubItems().add(datasourceItem);
			}
			datasourceCategoryList.add(dsCategory);
			parent.setSubItems(datasourceCategoryList);
		}
		if(newlyDataSourceList.size() == 0){
			return;
		}
		// now parent has been initialized and fill in a datasource, so just create new datasource category and add it.
		DataSourceEntityInfo fistDsEntity = (DataSourceEntityInfo) newlyDataSourceList.get(0);
		DataSourceCategory dsCategory = new DataSourceCategory(parent.getKey() + "_" + fistDsEntity.getConfigType(), fistDsEntity.getConfigType(), parent.getKey());
		for(DataSourceDisplayInfo newlyDsd : newlyDataSourceList){
			dsCategory.getSubItems().add(newlyDsd);
		}
		parent.getSubItems().add(dsCategory);
	}
}
