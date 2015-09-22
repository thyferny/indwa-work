/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * ImportDataUIHelper.js 
 * Author Gary
 * Aug 7, 2012
 */
define(["alpine/datasourceexplorer/HadoopExplorerUIHelper", 
        "alpine/import/DataFormatUIHelper",
        "alpine/props/HadoopCommonPropertyManager"], function(hadoopExplorerUIHelper, formatUIHelper, hpMgr){
	var constants = {
		REQUEST_URL: baseURL + "/main/importData.do",
		IMPORT_DB_DIALOG: "alpine_import_importData_upload_dialog",
		UPLOAD_BTN: "alpine_datasourceexplorer_import_btn",
		
		UPLOAD_FORM: "alpine_import_importData_upload_form",
		UPLOAD_FILE: "alpine_import_importData_upload_file",
        UPLOAD_FILE_SPAN: "SPAN_alpine_import_importData_upload_file",
		UPLOAD_FILE_TYPE: "alpine_import_importData_upload_type",
		UPLOAD_SIZE_AUTO: "alpine_import_importData_upload_size",
		UPLOAD_SIZE_SAMPLE_SIZE: "alpine_import_importData_upload_sampleSize",
		UPLOAD_FORMAT_AUTO: "alpine_import_importData_upload_format",
		UPLOAD_SUBMIT: "alpine_import_importData_upload_submit",
        UPLOAD_FILE_DISPLAY: "alpine_import_uploaded_file_display",

		IMPORT_HD_DIALOG: "alpine_import_importData_hd_upload_dialog",
        HD_UPLOAD_FILE: "alpine_import_importData_hd_upload_file",
        HD_UPLOAD_SUBMIT: "alpine_import_importData_hd_upload_submit"
	};

	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.UPLOAD_BTN), "onClick", _showUploadDialog);
		dojo.connect(dijit.byId(constants.IMPORT_DB_DIALOG), "onShow", function(){
			dijit.byId(constants.IMPORT_DB_DIALOG).titleBar.style.display='none';
		});
		dojo.connect(dijit.byId(constants.IMPORT_DB_DIALOG), "onHide", function(){
            dojo.byId(constants.UPLOAD_FILE_SPAN).hidden=false;
            dijit.byId(constants.UPLOAD_FORM).reset();
		});
		dojo.connect(dijit.byId(constants.IMPORT_HD_DIALOG), "onHide", function(){
            dijit.byId(constants.HD_UPLOAD_FILE).reset();
		});
		
		dojo.connect(dijit.byId(constants.HD_UPLOAD_SUBMIT), "onClick", function(){
			_doUploadHD();
		});
		
		dojo.connect(dijit.byId(constants.UPLOAD_SIZE_AUTO), "onChange", function(val){
			dijit.byId(constants.UPLOAD_SIZE_SAMPLE_SIZE).set("disabled", val);
		});
		
		dojo.connect(dijit.byId(constants.UPLOAD_SUBMIT), "onClick", _submitData);
	});
	
	function _showUploadDialog(){
		if(hadoopExplorerUIHelper.getCurrentConnKey() != null){
			dijit.byId(constants.IMPORT_HD_DIALOG).titleBar.style.display = "none";
			dijit.byId(constants.IMPORT_HD_DIALOG).show();
		}else{
			dijit.byId(constants.IMPORT_DB_DIALOG).show();
		}
	}
	
	function _validate(){
		var isValid = true;
		
		var fileType = dijit.byId(constants.UPLOAD_FILE_DISPLAY).getUploadFileType();
		if(fileType == null){
			return false;
		}
        // don't validate on the filetype - could be dat, data, txt, etc...
		// isValid &= (fileType.toUpperCase() == "CSV" || fileType.toUpperCase() == "TSV");
		isValid &= dijit.byId(constants.UPLOAD_SIZE_SAMPLE_SIZE).validate();
        return isValid;
	}
	
	function _submitData(){
		var data = {};
		if(!_validate()){
			return;
		}
		data.fileName = dijit.byId(constants.UPLOAD_FILE_DISPLAY).getUploadFileName();
		data.fileType = dijit.byId(constants.UPLOAD_FILE_DISPLAY).getUploadFileType();
		if(dijit.byId(constants.UPLOAD_SIZE_AUTO).get("checked")){
			data.importSize = -1;
		}else{
			data.importSize = dijit.byId(constants.UPLOAD_SIZE_SAMPLE_SIZE).get("value");
		}
		_doUpload(data, _uploadComplete);
	}

    /*
    * args
    *  .dataArray
    *  .fileName
    */
	function _uploadComplete(args){
        dojo.byId(constants.UPLOAD_FILE_SPAN).hidden=false; //find a better way to do this...
        dijit.byId(constants.IMPORT_DB_DIALOG).hide();
		formatUIHelper.startup(args);
		
	}
	
	function _doUpload(data, callback){
		dijit.byId(constants.UPLOAD_SUBMIT).set("label", alpine.nls.import_data_pane_upload_button_UPLOADING);
		dijit.byId(constants.UPLOAD_SUBMIT).set("disabled", true);
		var fileUploader = dijit.byId(constants.UPLOAD_FILE);
		fileUploader.set("uploadUrl", constants.REQUEST_URL + "?method=uploadData");
		var connectID = dojo.connect(fileUploader, "onComplete", function(dataArray){
			dojo.disconnect(connectID);
			dijit.byId(constants.UPLOAD_SUBMIT).set("label", alpine.nls.import_data_pane_upload_button_next);
			dijit.byId(constants.UPLOAD_SUBMIT).set("disabled", false);
            var args = {
                dataArray: dataArray,
                fileName: data.fileName,
                limitSize: data.importSize
            };
            if(callback){
				callback.call(null, args);
			}
		});
		fileUploader.upload(data);
	}
	
	
	
	
	
	function _doUploadHD(){
		var fileUploader = dijit.byId(constants.HD_UPLOAD_FILE);
		if(fileUploader.get("value") == null || fileUploader.get("value").length == 0){
			return;
		}
//		if(/[&|?|=|&|\"|\'|:|\[|\]|\{|\}|,]+/.test(fileUploader.get("value")[0].name)){
//			popupComponent.alert(alpine.nls.import_data_hd_validate_file_name);
//			return;
//		}
		if(!hpMgr.checkHasPermission(hadoopExplorerUIHelper.getCurrentConnKey(), hadoopExplorerUIHelper.getCurrentFolderPath())){
			popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_no_permission);
			return;
		}
		dojo.publish("toasterMessage", [{
			message: alpine.nls.import_data_message_starting,
			type: "message",
			duration: "0"
		}]);
		fileUploader.set("uploadUrl", constants.REQUEST_URL + "?method=uploadDataToHadoop");
		var connectID = dojo.connect(fileUploader, "onComplete", function(data){
			dojo.disconnect(connectID);
			var toasterParams = {};
			if(data.error_code){
				popupComponent.alert(data.message, "");
			}else{
				toasterParams.msg = alpine.nls.import_data_message_success;
				toasterParams.toasterType = "message";
				alpine.datasourceexplorer.DataSourceExplorerUIHelper.refreshCurrentLevel();//refresh data source explorer
	    		dojo.publish("toasterMessage", [{
	    			message: toasterParams.msg,
	    			type: toasterParams.toasterType,
					duration: "10000"
	    		}]);
			}
		});
		fileUploader.upload({
			connectionKey: hadoopExplorerUIHelper.getCurrentConnKey(),
			targetFolder: hadoopExplorerUIHelper.getCurrentFolderPath()
		});
		dijit.byId(constants.IMPORT_HD_DIALOG).hide();
	}
});