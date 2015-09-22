/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * HadoopManagerController
 * Mar 26, 2012
 */
package com.alpine.miner.impls.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceCategory;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceDisplayInfo;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.fs.HadoopHDFSFileManagerFactory;
import com.alpine.utility.log.LogPoster;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/dataSource/hadoop/manager.do")
public class HadoopManagerController extends AbstractControler {

	private static final int ERROR_CODE = -200;


    private static Logger itsLogger = Logger.getLogger(HadoopManagerController.class);

    /**
	 * @throws Exception
	 */
	public HadoopManagerController() throws Exception {
		super();
	}

	@RequestMapping(params="method=getConnConfig", method=RequestMethod.GET)
	public void getConnConfig(String key, HttpServletRequest request, HttpServletResponse response) throws Exception{
		HadoopConnectionInfo connConfig = (HadoopConnectionInfo) DataSourceEnum.HADOOP_CONNECT.getHandler().loadConnectionConfig(key);
		ProtocolUtil.sendResponse(response, connConfig);
	}

	@RequestMapping(params="method=testConnection", method=RequestMethod.POST)
	public void testConnection(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HadoopConnectionInfo config = ProtocolUtil.getRequest(request, HadoopConnectionInfo.class);
		try {
			Boolean ableToConnect = DataSourceEnum.HADOOP_CONNECT.getHandler().testConnection(config, request.getLocale());
			ProtocolUtil.sendResponse(response, ableToConnect);
		} catch (DataSourceMgrException e) {
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, e.getMessage()));
		}
	}

	@RequestMapping(params="method=saveConfig", method=RequestMethod.POST)
	public void saveConfig(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HadoopConnectionInfo config = ProtocolUtil.getRequest(request, HadoopConnectionInfo.class);
		config.setCreateUser(getUserName(request));
		config.setModifiedUser(getUserName(request));
		try {
			DataSourceEnum.HADOOP_CONNECT.getHandler().saveConnectionConfig(config);
            LogPoster.getInstance().createAndAddEvent(LogPoster.Datasource_Create, config.getConnection().getVersion(), getUserName(request));
            returnSuccess(response);
		} catch (DataSourceMgrException e) {
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString("datasource_config_edit_error_" + e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params="method=loadPersonalHadoopConnections", method=RequestMethod.GET)
	public void loadPersonalHadoopConnections(HttpServletRequest request, HttpServletResponse response) throws IOException{
		List<DataSourceDisplayInfo> result = null;
		try {
			DataSourceCategory personalHdConnections = DataSourceEnum.HADOOP_CONNECT.getHandler().getCategory(ResourceType.Personal, getUserName(request));
			result = personalHdConnections.getSubItems();
		} catch (DataSourceMgrException e) {
			generateErrorDTO(response, e, request.getLocale());
		}
		ProtocolUtil.sendResponse(response, result);
	}

	@RequestMapping(params="method=updateConfig", method=RequestMethod.POST)
	public void updateConfig(HttpServletRequest request, HttpServletResponse response) throws Exception{
		HadoopConnectionInfo config = ProtocolUtil.getRequest(request, HadoopConnectionInfo.class);
		config.setModifiedUser(getUserName(request));
		DataSourceEnum.HADOOP_CONNECT.getHandler().updateConnectionConfig(config);
		returnSuccess(response);
	}
	
	@RequestMapping(params="method=deleteConfig", method=RequestMethod.POST)
	public void deleteConfig(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String[] keyArray = ProtocolUtil.getRequest(request, String[].class);
		for(String key : keyArray){
			try {
				DataSourceEnum.HADOOP_CONNECT.getHandler().removeConnectionConfig(key);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
			}
		}
		returnSuccess(response);
	}

	@RequestMapping(params="method=getAllVersions", method=RequestMethod.GET)
	public void getAllVersions(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ProtocolUtil.sendResponse(response, new String[]{HadoopConnection.CURRENT_HADOOP_VERSION});
	}

	@RequestMapping(params="method=getAvailbleHadoopConnList", method=RequestMethod.GET)
	public void getAvailbleHadoopConnList(HttpServletRequest request, HttpServletResponse response) throws IOException{
		List<DataSourceDisplayInfo> hadoopDisplayList = IHadoopConnectionFeatcher.INSTANCE.getAvailbleHadoopDisplayInfoSet(getUserName(request));
		ProtocolUtil.sendResponse(response, hadoopDisplayList);
	}

	@RequestMapping(params="method=checkPermissionForPath", method=RequestMethod.GET)
	public void checkPermissionForPath(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException{
		HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
		boolean hasPermission = false;
		try {
			HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
			hasPermission = fsManager.isPathWritable(hci.getConnection(), path);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		ProtocolUtil.sendResponse(response, hasPermission);
	}

    @RequestMapping(params="method=createSubFolder", method=RequestMethod.GET)
    public void createSubFolder(String connectionKey,String parentPath, String folderName,HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
        	HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
            HadoopHDFSFileManager hdfsFileManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
            if(parentPath == null){
            	parentPath = HadoopHDFSFileManager.ROOT_PATH;
            }
            String folderPath = parentPath + HadoopHDFSFileManager.ROOT_PATH + folderName;
            if(hdfsFileManager.exists(folderPath, hci.getConnection())){
            	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ResourceBundle.getBundle("app", request.getLocale()).getString("same_name_exists")));
            	return;
            }
            hdfsFileManager.createHadoopFolder(folderPath, hci.getConnection());
        }catch (Exception e){
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return ;
        }
        returnSuccess(response);
    }

    @RequestMapping(params="method=deleteResource", method=RequestMethod.GET)
    public void deleteResource(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try{
        	HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
            HadoopHDFSFileManager hdfsFileManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
            if(path == null){
            	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ResourceBundle.getBundle("app", request.getLocale()).getString("hadoop_data_mgr_cannot_found_file")));
            	return;
            }
            boolean success =  hdfsFileManager.deleteHadoopFile(path, hci.getConnection());
            if(success==false){
				throw new Exception("Can not delete file "+path);
			}
        }catch (Exception e){
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return ;
        }
        returnSuccess(response);
    }

    @RequestMapping(params="method=viewHDFileProperty", method=RequestMethod.GET)
    public void viewHDFileProperty(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try{
        	HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
            HadoopHDFSFileManager hdfsFileManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
            if(path == null){
            	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ResourceBundle.getBundle("app", request.getLocale()).getString("hadoop_data_mgr_cannot_found_file")));
            	return;
            }
            HadoopFile hf = hdfsFileManager.getHadoopFile(path, hci.getConnection());
            HadoopFileProperty hp = HadoopFileProperty.newInstance(hf, request.getLocale());
            ProtocolUtil.sendResponse(response, hp);
        }catch (Exception e){
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return ;
        }
    }

    @RequestMapping(params="method=downloadHDFile", method=RequestMethod.GET)
    public void downloadHDFile(String connectionKey, String path, String startLine, String numberOfLine, HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setCharacterEncoding(Persistence.ENCODING);
		response.setContentType("application/octet-stream");
    	try{
        	HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
            HadoopHDFSFileManager hdfsFileManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
            if(path == null){
//            	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ResourceBundle.getBundle("app", request.getLocale()).getString("hadoop_data_mgr_cannot_found_file")));
            	return;
            }
            HadoopFile file = hdfsFileManager.getHadoopFile(path, hci.getConnection());
    		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
    		int numOfLine = numberOfLine == null ? -1 : Integer.parseInt(numberOfLine);
            hdfsFileManager.readHadoopFileToOutputStream(path, hci.getConnection(), Integer.parseInt(startLine) - 1, numOfLine, response.getOutputStream());
        }catch (Exception e){
        	response.setHeader("Content-Disposition", "attachment;filename=Error.txt");
        	OutputStreamWriter printer = new OutputStreamWriter(response.getOutputStream());
        	printer.write(e.getMessage());
        	printer.close();
            return ;
        }
    }

    @RequestMapping(params="method=isHDFileExists", method=RequestMethod.GET)
    public void isHDFileExists(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try{
        	HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
            HadoopHDFSFileManager hdfsFileManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hci.getConnection());
            if(path == null){
            	ProtocolUtil.sendResponse(response, "{result:" + Boolean.FALSE.toString() + "}");
            	return;
            }
            boolean isExists = hdfsFileManager.exists(path, hci.getConnection());
        	ProtocolUtil.sendResponse(response, "{result:" + Boolean.toString(isExists) + "}");
        }catch (Exception e){
        	ProtocolUtil.sendResponse(response, "{result:" + Boolean.FALSE.toString() + "}");
            return ;
        }
    }

	private OperatorWorkFlow readWorkFlow(HttpServletRequest request,HttpServletResponse response, FlowInfo info) throws IOException {
        OperatorWorkFlow flow = null;
        try {
            flow = rmgr.getFlowData(info,request.getLocale());
            if (flow == null) {
                //1: flow not found
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(1));
                return null;
            }
        } catch (OperationFailedException e) {
            ProtocolUtil.sendResponse(response,
                    new ErrorDTO(1, e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return flow;
    }
    
    public static class HadoopFileProperty{
    	private String 	name,
    					owner,
    					group,
    					modificationTime,
    					accessTime,
    					permission,
    					size,
    					blockSize;
    	static HadoopFileProperty newInstance(HadoopFile file, Locale locale){
    		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
    		HadoopFileProperty props = new HadoopFileProperty();
    		props.accessTime = dateFormat.format(new Date(file.getAccessTime()));
    		props.blockSize = formatSize(file.getBlockSize());
    		props.group = file.getGroup();
    		props.modificationTime = file.getModificationTime() == 0 ? "-" : dateFormat.format(new Date(file.getModificationTime()));
    		props.name = file.getName();
    		props.owner = file.getOwner();
    		props.permission = file.getPermission();
    		props.size = formatSize(file.getLength());
    		return props;
    	}
    	
    	private static String formatSize(long fileSize){
    		return fileSize * 1000 / 1048576 / 1000D + "MB";
    	}
    }
    
}
