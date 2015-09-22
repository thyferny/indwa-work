define(["alpine/datasourcemgr/DataSourceDBManager"], function(persistenceManager){
	var constants = {
		EDITOR_DB_CONTENT: "alpine_datasource_db_config_editor_connection_name",
		
		EDITOR_DB_FORM: "alpine_datasource_config_editor_db_form",
        IMPORT_DIALOG_ID:"import_dbDriver_dlg",
			
		EDITOR_INPUT_DB_ENGINE: "alpine_datasource_db_config_editor_dbType",
		EDITOR_INPUT_DB_HOST: "alpine_datasource_db_config_editor_host",
		EDITOR_INPUT_DB_PORT: "alpine_datasource_db_config_editor_port",
		EDITOR_INPUT_DB_DB_NAME: "alpine_datasource_db_config_editor_db_name",
		EDITOR_INPUT_DB_USERNAME: "alpine_datasource_db_config_editor_username",
		EDITOR_INPUT_DB_PASSWORD: "alpine_datasource_db_config_editor_password",
		EDITOR_INPUT_DB_USESSL: "alpine_datasource_db_config_editor_usessl"
	};
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.EDITOR_INPUT_DB_ENGINE), "onChange", function(val){
			changeUseSSLChk(val);
		});
	});
	
	function resetEditorWidgets(){
		dijit.byId(constants.EDITOR_INPUT_DB_HOST).reset();
		dijit.byId(constants.EDITOR_INPUT_DB_PORT).reset();
		dijit.byId(constants.EDITOR_INPUT_DB_DB_NAME).reset();
		dijit.byId(constants.EDITOR_INPUT_DB_USERNAME).reset();
		dijit.byId(constants.EDITOR_INPUT_DB_PASSWORD).reset();
		dijit.byId(constants.EDITOR_INPUT_DB_USESSL).reset();
	}
	
	function fillDataToWidgets(data){
		dijit.byId(constants.EDITOR_INPUT_DB_ENGINE).set("value", data.connection.dbType);
		dijit.byId(constants.EDITOR_INPUT_DB_HOST).set("value", data.connection.hostname);
		dijit.byId(constants.EDITOR_INPUT_DB_PORT).set("value", data.connection.port);
		dijit.byId(constants.EDITOR_INPUT_DB_DB_NAME).set("value", data.connection.dbname);
		dijit.byId(constants.EDITOR_INPUT_DB_USERNAME).set("value", data.connection.dbuser);
		dijit.byId(constants.EDITOR_INPUT_DB_PASSWORD).set("value", data.connection.password);
		dijit.byId(constants.EDITOR_INPUT_DB_USESSL).set("checked", data.connection.useSSL == "true");
	}
	
	function buildConfigInfo(){
		return {
			dbType: dijit.byId(constants.EDITOR_INPUT_DB_ENGINE).get("value"),
			hostname: dijit.byId(constants.EDITOR_INPUT_DB_HOST).get("value"),
			port: dijit.byId(constants.EDITOR_INPUT_DB_PORT).get("value"),
			dbname: dijit.byId(constants.EDITOR_INPUT_DB_DB_NAME).get("value"),
			dbuser: dijit.byId(constants.EDITOR_INPUT_DB_USERNAME).get("value"),
			password: dijit.byId(constants.EDITOR_INPUT_DB_PASSWORD).get("value"),
			useSSL: dijit.byId(constants.EDITOR_INPUT_DB_USESSL).get("checked") + "",
			jdbcDriverFileName: "temporaryDriver.jar"//just for temporary, it could be changed until adding custom jdbcDriver for each configuration.
			/**Will
			jdbcDriverFileName = dijit.byId("createConJDBCInfo").getCurrentContentValue();
			*/
		};
	}
	
	function testConnect(){
		var resourceInfo = {
			connection: buildConfigInfo()
		};
		persistenceManager.testConnectionForConfig(resourceInfo, function(){
			popupComponent.alert(alpine.nls.MSG_TEST_Connenction_OK);
		}, function(msg){
			if(msg){
				msg = alpine.nls.MSG_TEST_Connenction_Error + msg;
			}else{
				msg = alpine.nls.MSG_TEST_Connection_Failure;
			}
			popupComponent.alert(msg);
		}, constants.EDITOR_DB_FORM);
	}
	
	function changeUseSSLChk(val){
		var validUseSSL = val == "Greenplum" || val == "PostgreSQL";
		if(!validUseSSL){
			dijit.byId(constants.EDITOR_INPUT_DB_USESSL).set("checked", false);
		}
		dijit.byId(constants.EDITOR_INPUT_DB_USESSL).set("disabled", !validUseSSL);
	}
	

	
	
	

	
	
/**
 * below code are for upload JDBC driver file.
 */
   dojo.ready(function(){
	   	dojo.connect(dijit.byId('upload_driver_btn_id'),"onClick",function(){doDbDriverUpload(1,constants.IMPORT_DIALOG_ID);});
		dojo.connect(dijit.byId('db_connect_button_import_db_driver'),"onClick",function(){
			dijit.byId('import_dbDriver_dlg').show();
		});
        dijit.byId('import_dbDriver_dlg').titleBar.style.display = "none";
   });
	function upload_dbDriver_error(errorMsg){
		dojo.byId("upload_DBDriver_Error").color="red";
		dojo.byId("upload_DBDriver_Error").innerHTML=errorMsg;
	}
    function doDbDriverUpload(x, callbackPanelId){
		// clear error
		upload_dbDriver_error("");
		var uploadFileName = dojo.byId("dbDriverFile").value; 
	 
		var hasError=false;
		
		if(!uploadFileName||uploadFileName.length==0){
			upload_dbDriver_error(alpine.nls.MSG_Please_selectDBDriver_file);
			hasError = true;
		}
		else {// special case for IE
			if(dojo.isIE&&uploadFileName.lastIndexOf("\\")>-1){
				uploadFileName=uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
			}
		}
		var extend = uploadFileName.substring(uploadFileName.lastIndexOf(".") + 1, uploadFileName.length);
		if(extend != "jar" && extend != "zip"){
			upload_dbDriver_error(alpine.nls.MSG_Upload_Driver_Invalid);
			hasError = true;
		}

       if(hasError==false){

            ds.upload(baseURL + "/main/dataSource/db/manager.do?method=uploadDBDriver","frmIO_dbdriver",
            function(data)
            {
                if(data.error_code){
                    handle_error_result(data);
                } else {
                    popupComponent.alert(alpine.nls.MSG_Upload_Driver_Successful);
                }
                dijit.byId('import_dbDriver_dlg').hide();
            },
            function(data)
            {
                hide_db_progress_db_driver();
//                popupComponent.alert(alpine.nls.MSG_Upload_Driver_Error+":"+res);
            }, callbackPanelId,x);


//			progressBar.showLoadingBar();
//			var td = dojo.io.iframe.send({
//				url: baseURL + "/main/dbconnection.do?method=uploadDBDriver",
//				form: "frmIO_dbdriver",
//				method: "post",
//				content: {fnx:x},
//				timeoutSeconds: 60,
//				preventCache: true,
//				handleAs: "html",
//				handle: function(res, ioArgs){
//					progressBar.closeLoadingBar();
//
//					if(res&&res.body&&res.body.innerHTML){
//
//						var data = res.body.innerHTML.evalJSON();
//						if(data.error_code){
//							handle_error_result(data);
//	 						dijit.byId('import_dbDriver_dlg').hide();
//						}else{
//							popupComponent.alert(alpine.nls.MSG_Upload_Driver_Successful);
//							//Add by Will import OK synchronize dropDownList
////							load_jdbc_jars_info(load_jdbc_jars_info_CallBack);
////							load_jdbc_jars_info(load_jdbc_jars_info_forCreateJDBc_CallBack);
//							dijit.byId('import_dbDriver_dlg').hide();
//						}
//					}
//
//				},
//				error: function (res,ioArgs) {
//					hide_db_progress_db_driver();
//					popupComponent.alert(alpine.nls.MSG_Upload_Driver_Error+":"+res);
//				}
//			});
		}
	}

	function validate(){
		return dijit.byId(constants.EDITOR_DB_FORM).validate();
	}
	
	return {
		resetEditorWidgets: resetEditorWidgets,
		fillDataToWidgets: fillDataToWidgets,
		testConnect: testConnect,
		buildConfigInfo: buildConfigInfo,
		loadDataSourceConfig: persistenceManager.getConnectionConfig,
		saveResourceInfo: persistenceManager.saveConnectionConfig,
		updateResourceInfo: persistenceManager.updateConnectionConfig,
		validate: validate
	};
});