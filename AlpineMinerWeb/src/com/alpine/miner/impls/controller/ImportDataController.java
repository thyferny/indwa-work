/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataController.java
 */
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ImportDataConfiguration;
import com.alpine.importdata.ImportDataConfiguration.ColumnStructure;
import com.alpine.importdata.ImportDataService;
import com.alpine.importdata.ImportDataService.ImportHandler;
import com.alpine.importdata.ddl.ICreateParameter;
import com.alpine.importdata.ddl.TableCreator;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.importdata.UploadDataService;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gary
 * Aug 8, 2012
 */

@Controller
@RequestMapping("/main/importData.do")
public class ImportDataController extends AbstractControler {
	
	private static final String uploadName = "importFiles[]";
	
	private static final int ERROR_MAX_SIZE = 100;
	
	private static final String NODE_NAME = "IMPORT_TEMPRARY_FILE";
	public static final String PROGRESS_NODE_NAME = "IMPORT_PROGRESS_NODE_NAME";
	private static final String ABORT_NODE = "ABORT_NODE";
	
	/**
	 * @throws Exception
	 */
	public ImportDataController() throws Exception {
		super();
	}

	@RequestMapping(params = "method=uploadData", method = RequestMethod.POST)
	public void uploadData(HttpServletRequest request, HttpServletResponse response) throws IOException{
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		List<String> dataset;
		try {
			String identifier = UploadDataService.INSTANCE.saveUploadData(req.getFile(uploadName).getInputStream());
			request.getSession().setAttribute(NODE_NAME, identifier);
			dataset = UploadDataService.INSTANCE.loadData(identifier, 100);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
		ProtocolUtil.sendResponse(response, dataset);
	}

	@RequestMapping(params = "method=uploadDataToHadoop", method = RequestMethod.POST)
	public void uploadDataToHadoop(HttpServletRequest request, HttpServletResponse response) throws IOException{
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		MultipartFile uploadFile = req.getFile(uploadName);
		String targetFolder = req.getParameter("targetFolder");
		String connectionKey = req.getParameter("connectionKey");
		HadoopConnectionInfo connectionInfo = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
		try {
			HadoopHDFSFileManager.INSTANCE.writeStreamToFile(uploadFile.getInputStream(), targetFolder + HadoopFile.SEPARATOR + uploadFile.getOriginalFilename(), connectionInfo.getConnection());
			returnSuccess(response);
		} catch (Exception e) {
			String errorMsg = ResourceBundle.getBundle("app", request.getLocale()).getString("import_data_hd_message_error");
			generateErrorDTO(response, errorMsg + e.getMessage(), request.getLocale());
		}
	}
	
	@RequestMapping(params = "method=deleteSimpleData", method = RequestMethod.GET)
	public void deleteSimpleData(String identifier, HttpServletRequest request, HttpServletResponse response) throws IOException{
		if(identifier == null){
			identifier = (String) request.getSession().getAttribute(NODE_NAME);
		}
		UploadDataService.INSTANCE.deleteUploadData(identifier);
		request.getSession().removeAttribute(NODE_NAME);
		returnSuccess(response);
	}

	@RequestMapping(params = "method=abortImport", method = RequestMethod.GET)
	public void abortImport(HttpServletRequest request, HttpServletResponse response) throws IOException{
		request.getSession().setAttribute(ABORT_NODE, "ABORT");//just placeholder
		returnSuccess(response);
	}

	@RequestMapping(params = "method=importDataToDB", method = RequestMethod.POST)
	public void importDataToDB(String connectionName, final HttpServletRequest request, final HttpServletResponse response) throws IOException{
		request.getSession().setAttribute(PROGRESS_NODE_NAME, 0);
		final ImportSummary summary = new ImportSummary();
		ImportDataConfiguration config = ProtocolUtil.getRequest(request, ImportDataConfiguration.class);
		DbConnection connInfo = null;
		InputStream content;
		String identifier;
		DBDataUtil dbd;
		//get Connection first.
		try {
			connInfo = WebDBResourceManager.getInstance().getDBConnection(getUserName(request), connectionName, ResourceType.Personal).getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			generateErrorDTO(response, e.getMessage(), request.getLocale());
			return;
		}
		config.setConnectionInfo(connInfo);
		dbd = new DBDataUtil(connInfo);
		//check table exists
		try {
			boolean talbeIsExist = dbd.isTableExist(config.getSchemaName(), config.getTableName());
			if(!talbeIsExist){
//				String msg = ResourceBundle.getBundle("app", request.getLocale()).getString("import_data_error_table_not_exist");
//				msg = MessageFormat.format(msg, new String[]{config.getTableName()});
//				generateErrorDTO(response, msg, request.getLocale());
//				return;
				createTable(config, connInfo);
			}
		} catch (Exception e1) {
			generateErrorDTO(response, e1.getMessage(), request.getLocale());
			return;
		}
		//get file content stream
		identifier = (String) request.getSession().getAttribute(NODE_NAME);
		try {
			content = UploadDataService.INSTANCE.getDataContent(identifier);
		} catch (Exception e) {
			generateErrorDTO(response, e.getMessage(), request.getLocale());
			return;
		}
		//import data to database
		try {
			ImportDataService.importData(config, content, new ImportHandler(){
				@Override
				public void onCompleteRow(int rowIdx, String[] row) {
					request.getSession().setAttribute(PROGRESS_NODE_NAME, rowIdx);
				}
				@Override
				public void onFailRow(int rowIdx, String[] row, Exception e) {
					if(summary.failureInfoSet.size() < ERROR_MAX_SIZE){
						summary.failureInfoSet.add(new ImportFailureInfo(rowIdx + 1, e.getMessage()));
					}
				}
				@Override
				public void onFailed(Exception e) {
					if(e instanceof SQLException){
                        int i = 0;
                        SQLException sqlException = (SQLException) e;
	                    while(sqlException != null){
                            summary.failureInfoSet.add(new ImportFailureInfo("FailureMsg"+Integer.toString(i), sqlException.getMessage()));
                            sqlException = sqlException.getNextException();
                            ++i;
	                    }
					}else{
						summary.failureInfoSet.add(new ImportFailureInfo(0, e.getMessage()));
					}
                    summary.type = ImportType.ERROR;
				}
                @Override
                public void onAbort(Exception e) {
                    summary.type = ImportType.ABORT;
                }
				@Override
				public boolean isContinue() {
					boolean isContinue = request.getSession().getAttribute(ABORT_NODE) == null;
					return isContinue;
				}
			});
		} catch (Exception e) {
            generateErrorDTO(response, e.getMessage(), request.getLocale());
            return;
		}finally{
			request.getSession().removeAttribute(PROGRESS_NODE_NAME);
		}
		//delete temporary csv file from server.
//		UploadDataService.INSTANCE.deleteUploadData(identifier);
		request.getSession().removeAttribute(ABORT_NODE);
		if(summary.type == null){//not abort or fail
			if(summary.failureInfoSet.size() > 0){
				summary.type = ImportType.WARN;
			}else{
				summary.type = ImportType.MESSAGE;
			}
		}
		try {
			DBMetadataManger.INSTANCE.refreshSchema(connInfo, config.getSchemaName(),true) ;
		} catch (Exception e) {
			generateErrorDTO(response, e.getMessage(), request.getLocale());
			return;
		} 
		ProtocolUtil.sendResponse(response, summary);
	}
	
	private void createTable(final ImportDataConfiguration config, DbConnection connInfo) throws Exception{

		TableCreator.getInstance(connInfo).createTable(new ICreateParameter() {
			@Override
			public String getTableName() {
				return config.getTableName();
			}
			@Override
			public String getSchemaName() {
				return config.getSchemaName();
			}
			@Override
			public List<columnMetaInfo> getColumnMetaList() {
				List<columnMetaInfo> columnList = new ArrayList<columnMetaInfo>();
				List<ColumnStructure> columnStructureList = config.getColumnInfo();
				for(final ColumnStructure cs : columnStructureList){
					if(!cs.isInclude()){
						continue;
					}
					columnList.add(new columnMetaInfo(){
						@Override
						public String getColumnName() {
							return cs.getColumnName();
						}
						@Override
						public DatabaseDataType getColumnType() {
							return cs.getColumnType();
						}
					});
				}
				return columnList;
			}
		});
	}
	
	public static class ImportSummary{
		private ImportType type;
		private List<ImportFailureInfo> failureInfoSet = new ArrayList<ImportFailureInfo>();
	}
	
	public enum ImportType{
		MESSAGE, WARN, ABORT, ERROR
	}
	
	public static class ImportFailureInfo{
		private int rowNumInt;
		private String rowNum;
		private String failureMessage;
		
		public ImportFailureInfo(int rowNumInt, String msg){
			this.rowNum = Integer.toString(rowNumInt);
			this.failureMessage = msg;
		}

        public ImportFailureInfo(String rowNum, String msg){
            this.rowNum = rowNum;
            this.failureMessage = msg;
        }

		public int getRowNumInt() {
			return rowNumInt;
		}
        public String getRowNum() {
			return rowNum;
		}
		public String getFailureMessage() {
			return failureMessage;
		}
	}
}
