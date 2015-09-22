package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.controller.dbmeta.DBMetaDataDTO;
import com.alpine.miner.impls.datasourcemgr.DataSourceMgrException;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.datasourcemgr.model.DataSourceEnum;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.utility.db.*;
import com.alpine.utility.log.LogPoster;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@RequestMapping("/main/dataSource/db/manager.do")
public class DBConnectionMgrController extends AbstractControler {
    private static Logger itsLogger = Logger.getLogger(DBConnectionMgrController.class);

    private static final int ERROR_CODE = -200;
	
	/**
	 * @throws Exception
	 */
	public DBConnectionMgrController() throws Exception {
		super();
	}

	@RequestMapping(params="method=getConnConfig", method=RequestMethod.GET)
	public void getConnConfig(String key, HttpServletRequest request, HttpServletResponse response) throws Exception{
		DbConnectionInfo connConfig = (DbConnectionInfo) DataSourceEnum.DB_CONNECT.getHandler().loadConnectionConfig(key);
		ProtocolUtil.sendResponse(response, connConfig);
	}

	@RequestMapping(params="method=testConnection", method=RequestMethod.POST)
	public void testConnection(HttpServletRequest request, HttpServletResponse response) throws IOException{
		DbConnectionInfo config = ProtocolUtil.getRequest(request, DbConnectionInfo.class);
		try {
			Boolean ableToConnect = DataSourceEnum.DB_CONNECT.getHandler().testConnection(config, request.getLocale());
			ProtocolUtil.sendResponse(response, ableToConnect);
		} catch (DataSourceMgrException e) {
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, e.getMessage()));
		}
	}

	@RequestMapping(params="method=saveConfig", method=RequestMethod.POST)
	public void saveConfig(HttpServletRequest request, HttpServletResponse response) throws IOException{
		DbConnectionInfo config = ProtocolUtil.getRequest(request, DbConnectionInfo.class);
		config.setCreateUser(getUserName(request));
		config.setModifiedUser(getUserName(request));
		try {
			DataSourceEnum.DB_CONNECT.getHandler().saveConnectionConfig(config);
            LogPoster.getInstance().createAndAddEvent(LogPoster.Datasource_Create,config.getConnection().getDbType() , getUserName(request));
			returnSuccess(response);
		} catch (DataSourceMgrException e) {
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString("datasource_config_edit_error_" + e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params="method=updateConfig", method=RequestMethod.POST)
	public void updateConfig(HttpServletRequest request, HttpServletResponse response) throws Exception{
		DbConnectionInfo config = ProtocolUtil.getRequest(request, DbConnectionInfo.class);
		config.setModifiedUser(getUserName(request));
		DataSourceEnum.DB_CONNECT.getHandler().updateConnectionConfig(config);
		returnSuccess(response);
	}

	@RequestMapping(params="method=deleteConfig", method=RequestMethod.POST)
	public void deleteConfig(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String[] keyArray = ProtocolUtil.getRequest(request, String[].class);
		for(String key : keyArray){
			try {
				DataSourceEnum.DB_CONNECT.getHandler().removeConnectionConfig(key);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
			}
		}
		returnSuccess(response);
	}

    @RequestMapping(params = "method=uploadDBDriver", method = RequestMethod.POST)
    public String uploadDBDriver(String user, HttpServletRequest request,
                                 HttpServletResponse response, ModelMap model) throws IOException {

        try{
            if (user == null) {
                user = getUserName(request);
            }
            MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
            Iterator<String> itFileName = req.getFileNames();

            while (itFileName.hasNext()) {
                String fn = itFileName.next();
                //System.out.println("file: " + fn);
                List<MultipartFile> fileList = req.getFiles(fn);

                // 1st, validate all file names before processing.

                for (MultipartFile f : fileList) {
                    String fileName = f.getOriginalFilename();

                    InputStream in = f.getInputStream();

                    String root = FilePersistence.initRoot();
                    root = root+"jdbc_driver"+ File.separator+"Public";

                    File rootDir= new File(root);
                    if(rootDir.exists()==false){
                        rootDir.mkdir();
                    }
                    String path = root+File.separator + fileName ;
                    //this is very important, now only need a folder
                    //the file name are hard coded...
                    com.alpine.utility.db.AlpineUtil.setJarFileDir(root+File.separator );
                    //oracle11.info

                    FileOutputStream out = null;
                    try {
                        out =new FileOutputStream(path);
                        int b;
                        while ((b = in.read( )) != -1){
                            out.write(b);
                        }

                    } finally {
                        if (out != null) {

                            out.flush();
                            out.close();
                        }
                        itsLogger.info("Driver saved:"+path) ;
                    }

                    JDBCDriverInfo info = new JDBCDriverInfo(  getUserName(request),
                            ResourceInfo.ResourceType.Public,  fileName,  fileName  );
                    rmgr.createJDBCDriverInfos(info);

                    returnSuccess(response) ;
                }
            }
        }catch(Exception e){
            generateErrorDTO(response, e, request.getLocale()) ;
        }

        return null;
    }

    @RequestMapping(params = "method=getAllDBConnectionMetadata", method = RequestMethod.GET)
    public void getAllDBConnectionMetadata(HttpServletRequest request,
                                           HttpServletResponse response, ModelMap model) throws IOException {

        try {
            String user = getUserName(request);

            String path = ResourceInfo.ResourceType.Personal.name() + File.separator + user;
            // will use the new API when ready
            List<DbConnectionInfo> connections = rmgr
                    .getDBConnectionsByPath(path);
            List<DBMetaDataDTO> metaDataDTOs = new ArrayList<DBMetaDataDTO>();
            if (connections != null) {
                for(int i =0;i<connections.size();i++){
                    DbConnectionInfo conn = connections.get(i);
                    metaDataDTOs.add(createMetaInfo(conn));
                }
            }

            ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(metaDataDTOs.toArray()));

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }

    @RequestMapping(params = "method=refreshDBMetaData", method = RequestMethod.POST)
    public void refreshDBMetaData(	HttpServletRequest request, HttpServletResponse response,
                                      ModelMap model) throws IOException{
        try{

            DBMetaDataDTO metaData = ProtocolUtil.getRequest(request,DBMetaDataDTO.class);
            DbConnectionInfo dbcInfo = WebDBResourceManager.getInstance().getDBConnection(getUserName(request), metaData.getConnectionName(), ResourceInfo.ResourceType.Personal);
            if(metaData.getType().equals(DBMetaDataDTO.TYPE_DB)){

                DBMetadataManger.INSTANCE.refreshDBConnection(dbcInfo.getConnection(), false,true) ;
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(getDBMetaDataChildren(metaData,getUserName(request),dbcInfo)));
            }
            else if(metaData.getType().equals(DBMetaDataDTO.TYPE_SCHEMA)){
                DBMetadataManger.INSTANCE.refreshSchema( dbcInfo.getConnection(), metaData.getName(),true) ;
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(getDBMetaDataChildren(metaData,getUserName(request),dbcInfo)));
            }
            else if(metaData.getType().equals(DBMetaDataDTO.TYPE_TABLE)){
                DBMetadataManger.INSTANCE.refreshTable( dbcInfo.getConnection(), metaData.getSchemaName(),metaData.getName(),true) ;
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(new String[0]));
            }
            else if(metaData.getType().equals(DBMetaDataDTO.TYPE_VIEWCONTAINER)){
                DBMetadataManger.INSTANCE.refreshSchemaViews( dbcInfo.getConnection(), metaData.getSchemaName(),true) ;
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(getDBMetaDataChildren(metaData,getUserName(request),dbcInfo)));
            }
            else if(metaData.getType().equals(DBMetaDataDTO.TYPE_TABLECONTAINER)){
                DBMetadataManger.INSTANCE.refreshSchemaTables( dbcInfo.getConnection(), metaData.getSchemaName(),true) ;
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(getDBMetaDataChildren(metaData,getUserName(request),dbcInfo)));
            }


        }catch(Exception e){
            generateErrorDTO(response, e, request.getLocale()) ;
        }

    }

    @RequestMapping(params = "method=getDBMetaDataChildren", method = RequestMethod.POST)
    public void getDBMetaDataChildren( HttpServletRequest request, HttpServletResponse response,
                                       ModelMap model) throws IOException{

        try{
            DBMetaDataDTO metaInfo = ProtocolUtil.getRequest(request,DBMetaDataDTO.class);
            DbConnectionInfo dbcInfo = WebDBResourceManager.getInstance().getDBConnection(getUserName(request), metaInfo.getConnectionName(), ResourceInfo.ResourceType.Personal);

            DBMetaDataDTO[] child = getDBMetaDataChildren(metaInfo,getUserName(request),dbcInfo);
            ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(child));
        }catch(Exception e){
            generateErrorDTO(response, e, request.getLocale()) ;
        }
    }

    private DBMetaDataDTO createMetaInfo(DbConnectionInfo conn) {
        DBMetaDataDTO metaDTO= new DBMetaDataDTO(conn.getId(),DBMetaDataDTO.TYPE_DB,conn.getId(),null);
        return metaDTO;
    }

    private DBMetaDataDTO createMetaInfo(DBSchemaMetaInfo schemaInfo) {
        DBMetaDataDTO metaDTO= new DBMetaDataDTO(schemaInfo.getSchemaName(),DBMetaDataDTO.TYPE_SCHEMA,schemaInfo.getConnName(),schemaInfo.getSchemaName());
        return metaDTO;
    }

    private DBMetaDataDTO createMetaInfo(DBTableMetaInfo tableInfo,String tableType) {
        DBMetaDataDTO metaDTO= new DBMetaDataDTO(tableInfo.getTableName(),tableType,tableInfo.getConnectionName(),tableInfo.getSchemaName());

        return metaDTO;
    }

    private DBMetaDataDTO[] getDBMetaDataChildren(DBMetaDataDTO metaData, String userName, DbConnectionInfo dbcInfo ) throws  Exception {
        DBMetaDataDTO[] result = new DBMetaDataDTO[0] ;

        if(metaData.getType().equals(DBMetaDataDTO.TYPE_DB)){
            DataBaseMetaInfo dbInfo = DBMetadataManger.INSTANCE.getDataBaseMetaInfo(dbcInfo.getConnection());
            if(dbInfo!=null){
                List<DBSchemaMetaInfo> schemaList = dbInfo.getSchemaInfos();
                if(schemaList!=null){
                    result = new DBMetaDataDTO[schemaList.size()] ;
                    for(int i=0;i<schemaList.size();i++){
                        result[i] =createMetaInfo(schemaList.get(i)) ;
                    }
                }
            }
        }
        else if(metaData.getType().equals(DBMetaDataDTO.TYPE_SCHEMA)){

            DBSchemaMetaInfo schemaInfo = DBMetadataManger.INSTANCE.getDBSchemaMetaInfo(dbcInfo.getConnection(),metaData.getName());
            if(schemaInfo!=null){
                result = new DBMetaDataDTO[2] ;
                DBTableContainer tableContainer = schemaInfo.getTableContainer();
                DBMetaDataDTO tcMetaDTO = new DBMetaDataDTO("Table",DBMetaDataDTO.TYPE_TABLECONTAINER,metaData.getConnectionName(),schemaInfo.getSchemaName());
                //first time, need load data
                if(tableContainer==null){
                    DBMetadataManger.INSTANCE.refreshSchema(dbcInfo.getConnection(),schemaInfo.getSchemaName(),true);
                }
                if(tableContainer!=null&&tableContainer.getTables()!=null&&tableContainer.getTables().size()>0){
                    List<DBTableMetaInfo> tableList = tableContainer.getTables();
                    addMetaChild(tcMetaDTO, tableList,DBMetaDataDTO.TYPE_TABLE);
                }
                result[0]=tcMetaDTO;

                DBViewContainer viewContainer = schemaInfo.getViewContainer();
                DBMetaDataDTO tvMetaDTO = new DBMetaDataDTO("View",DBMetaDataDTO.TYPE_VIEWCONTAINER,metaData.getConnectionName(),schemaInfo.getSchemaName());
                if(viewContainer!=null&&viewContainer.getTables()!=null&&viewContainer.getTables().size()>0){
                    List<DBTableMetaInfo> viewList = viewContainer.getTables();
                    addMetaChild(tvMetaDTO, viewList,DBMetaDataDTO.TYPE_VIEW);
                }
                result[1]=tvMetaDTO;


            }
        }
        else if(metaData.getType().equals(DBMetaDataDTO.TYPE_TABLECONTAINER)){

            DBSchemaMetaInfo schemaInfo = DBMetadataManger.INSTANCE.getDBSchemaMetaInfo(dbcInfo.getConnection(),metaData.getSchemaName());
            DBTableContainer tableContainer = schemaInfo.getTableContainer();
            if( tableContainer!=null&& tableContainer.getTables()!=null){

                List<DBTableMetaInfo> tableList = tableContainer.getTables();
                result = createDBMetaArray(tableList,DBMetaDataDTO.TYPE_TABLE);
            }


        }else if(metaData.getType().equals(DBMetaDataDTO.TYPE_VIEWCONTAINER)){

            DBSchemaMetaInfo schemaInfo = DBMetadataManger.INSTANCE.getDBSchemaMetaInfo(dbcInfo.getConnection(),metaData.getSchemaName());
            DBViewContainer vc = schemaInfo.getViewContainer();
            if( vc!=null&& vc.getTables()!=null){
                List<DBTableMetaInfo> tableList = vc.getTables();
                result = createDBMetaArray(tableList,DBMetaDataDTO.TYPE_VIEW);
            }

        }
        return result;
    }

    private void addMetaChild(DBMetaDataDTO tcMetaDTO,
                              List<DBTableMetaInfo> tableList, String tableType) {
        List<DBMetaDataDTO> child = new ArrayList<DBMetaDataDTO>();;
        for(int i=0;i<tableList.size();i++){
            child.add(createMetaInfo(tableList.get(i),tableType)) ;
        }
        tcMetaDTO.setChildren(child);
    }

    private DBMetaDataDTO[] createDBMetaArray(List<DBTableMetaInfo> tableList, String tableType) {
        DBMetaDataDTO[] result;
        result = new DBMetaDataDTO[tableList.size()] ;
        for(int i=0;i<tableList.size();i++){
            result[i]=createMetaInfo(tableList.get(i),tableType) ;
        }
        return result;
    }

}
