/**
 * 
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.operator.datasource.DbTableOperatorProperty;
import com.alpine.miner.impls.web.resource.operator.datasource.HadoopOperatorProperty;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: DataSourceExplorerController.java
 * <p/>
 * Data: 2012-6-25
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
@Controller
@RequestMapping("/main/dataSource/explorer.do")
public class DataSourceExplorerController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public DataSourceExplorerController() throws Exception {
		super();
	}

	@RequestMapping(params="method=getAvailableConnections", method=RequestMethod.GET)
	public void getAvailableConnections(HttpServletRequest request, HttpServletResponse response) throws IOException{

		String userSignture = getUserName(request);
		List<DataSourceDisplayInfo> connections = new ArrayList<DataSourceDisplayInfo>();
		for(DataSourceEnum dse : DataSourceEnum.values()){
			DataSourceCategory dsc = null;
			try {
				dsc = dse.getHandler().getCategory(ResourceType.Personal, userSignture);
			} catch (DataSourceMgrException e) {
				generateErrorDTO(response, e, request.getLocale());
				return;
			}
			connections.addAll(dsc.getSubItems());
		}
		ProtocolUtil.sendResponse(response, connections);
	}

	@RequestMapping(params="method=getSchemaByConnection", method=RequestMethod.GET)
	public void getSchemaByConnection(String dbConnectionName, HttpServletRequest request, HttpServletResponse response) throws IOException{
		String userName = getUserName(request);
		String[] schemaNames = null;
		try {
			schemaNames = WebDBResourceManager.getInstance().getSchemaList(userName, dbConnectionName, ResourceType.Personal, true);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
		ProtocolUtil.sendResponse(response, schemaNames);
	}

	@RequestMapping(params="method=getTableViewBySchema", method=RequestMethod.GET)
	public void getTableViewBySchema(String connName, String schemaName, HttpServletRequest request, HttpServletResponse response) throws IOException{
		String userName = getUserName(request);
		String[] tableViewArray = null;
		List<DbTableOperatorProperty> entityList = new ArrayList<DbTableOperatorProperty>();
		try {
			tableViewArray = WebDBResourceManager.getInstance().getTableList(userName, connName, schemaName, ResourceType.Personal, true);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
		for(String name: tableViewArray){
			entityList.add(new DbTableOperatorProperty(connName, schemaName, name));
		}
		ProtocolUtil.sendResponse(response, entityList);
	} 
	
	@RequestMapping(params="method=getTableViewNames", method = RequestMethod.GET)
    public void getTableViewList(String user, String conn, String schema,String resourceType,
            HttpServletRequest request, HttpServletResponse response,
            ModelMap model) throws IOException {
		if (checkUser(user, request, response) == false) {
			return;
		}
		try {
			String[] tableNames;
			if (conn == null || conn.length() == 0||schema==null||schema.length()==0) {
				tableNames = new String[0];
			}else{
				conn=getUTFParamvalue(conn, request) ;
				schema=getUTFParamvalue(schema, request) ;
				tableNames=WebDBResourceManager.getInstance().getTableList(user, conn, schema,super.getResourceType(resourceType));
				ProtocolUtil.sendResponse(response, tableNames);
			}
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
    @RequestMapping(params = "method=getConnSchemaTablesMap", method = RequestMethod.GET)
    // this function is called by find and replace function.
    public void getConnSchemaTablesMap(HttpServletRequest request,
                                       HttpServletResponse response,String resourceType,ModelMap model) throws IOException {
        Map<String,Map<String,List<String>>> connSchemaTablesMap = new HashMap<String, Map<String,List<String>>>();

        String user = getUserName(request);

        String path = ResourceType.Personal.name() + File.separator + user;
        // will use the new API when ready
        List<DbConnectionInfo> connections = rmgr.getDBConnectionsByPath(path);
        if (connections != null) {
            try {
                for (int i = 0; i < connections.size(); i++) {
                    String connectionId = connections.get(i).getId();
                    //
                    Map<String, List<String>> schemaTablesMap = new HashMap<String, List<String>>();
                    String[] schemas = null;
                    try {
                        schemas = WebDBResourceManager.getInstance()
                                .getSchemaList(user, connectionId, super
                                        .getResourceType(resourceType));
                    } catch (Exception e) {

                    }

                    if (null != schemas && schemas.length > 0) {
                        for (int j = 0; j < schemas.length; j++) {
                            List<String> tableGroups = null;
                            String[] tableNames = null;
                            tableNames = WebDBResourceManager.getInstance()
                                    .getTableList(user, connectionId,
                                            schemas[j],
                                            super.getResourceType(resourceType));
                            tableGroups = Arrays.asList(tableNames);
                            schemaTablesMap.put(schemas[j], tableGroups);
                        }
                    }

                    connSchemaTablesMap.put(connectionId, schemaTablesMap);
                }
                ProtocolUtil.sendResponse(response, connSchemaTablesMap);
            } catch (Exception e) {
                generateErrorDTO(response, e, request.getLocale());
            }
        }

    }

	@RequestMapping(params="method=getHadoopFileByPath", method=RequestMethod.GET)
	public void getHadoopFileByPath(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException{
		List<HadoopFile> hadoopFiles = null;
		if(path == null){
			path = HadoopHDFSFileManager.ROOT_PATH;
		}
		List<HadoopOperatorProperty> result = new ArrayList<HadoopOperatorProperty>();
		HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
		try{
			//hadoopFiles = HadoopHDFSFileManager.INSTANCE.getHadoopFiles(path, hci.getConnection(), false);
			hadoopFiles = HadoopHDFSFileManager.INSTANCE.getHadoopFiles(path, hci.getConnection(), false);
		}catch(Exception e){
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			return;
		}
		for(HadoopFile item : hadoopFiles){
			result.add(new HadoopOperatorProperty(item.getConnName(), item.getFullPath(), item.getName(), item.isDir()));
		}
		Collections.sort(result, new Comparator<HadoopOperatorProperty>(){

			@Override
			public int compare(HadoopOperatorProperty o1, HadoopOperatorProperty o2) {
				if(o1.isDir() == o2.isDir()){
					return o1.getFileName().compareTo(o2.getFileName());
				}else if(o1.isDir()){
					return -1;
				}else{
					return 1;
				}
			}
			
		});
		ProtocolUtil.sendResponse(response, result);
	}
}
