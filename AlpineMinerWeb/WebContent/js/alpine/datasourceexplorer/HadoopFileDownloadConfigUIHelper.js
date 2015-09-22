define(["alpine/datasourcemgr/DataSourceHadoopManager",
        "alpine/datasourceexplorer/HadoopServerMapping"], function(hadoopConnMgr, hdServerMapping){
	var constants = {
		DIALOG: "alpine_datasourceexplorer_HadoopFileDownloadConfig_Dialog",
		LABEL_NAME: "alpine_datasourceexplorer_HadoopFileDownloadConfig_name",
		LABEL_OWNER: "alpine_datasourceexplorer_HadoopFileDownloadConfig_owner",
		LABEL_GROUP: "alpine_datasourceexplorer_HadoopFileDownloadConfig_group",
		LABEL_SIZE: "alpine_datasourceexplorer_HadoopFileDownloadConfig_size",
		SUBMIT_BTN: "alpine_datasourceexplorer_HadoopFileDownloadConfig_submit",
		START_LINE_INPUT: "alpine_datasourceexplorer_HadoopFileDownloadConfig_startLine",
		NUMBER_LINE_INPUT: "alpine_datasourceexplorer_HadoopFileDownloadConfig_NumberOfLine",
		FORM: "alpine_datasourceexplorer_HadoopFileDownloadConfig_form",
        TIP_CONTAINER:"alpine_file_download_tip_container",
        TIP_ID:"alpine_file_download_tip"
	};
	var filePath = null;
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.DIALOG), "onShow", function(){
			dijit.byId(constants.START_LINE_INPUT).reset();
			dijit.byId(constants.NUMBER_LINE_INPUT).reset();
		});
		
		dojo.connect(dijit.byId(constants.SUBMIT_BTN), "onClick", function(){
			if(dijit.byId(constants.FORM).validate()){
				_downloadFile(dijit.byId(constants.START_LINE_INPUT).get("value"), dijit.byId(constants.NUMBER_LINE_INPUT).get("value"));
			};
		});
	});
	
	function _downloadFile(startLine, numberOfLine){
		var connKey = alpine.datasourceexplorer.HadoopExplorerUIHelper.getCurrentConnKey();
		//we do check switch here
		if(true){
			dijit.byId(constants.DIALOG).hide();
			_downloadFileDirectly(connKey, startLine, numberOfLine, encodeURIComponent(filePath));
		}else{
			var url = alpine.baseURL + "/main/dataSource/hadoop/manager.do?method=downloadHDFile&connectionKey=" + connKey + "&path=" + encodeURIComponent(filePath) + "&startLine=" + startLine;
			if(numberOfLine != null && numberOfLine != ""){
				url += "&numberOfLine=" + numberOfLine;
			}
			dijit.byId(constants.DIALOG).hide();
			window.location.href = url;
		}
	}
	
	function _downloadFileDirectly(connKey, startLine, numberOfLine, filePath){
		var connectionInfo = hadoopConnMgr.getConnectionConfig(connKey);
		var hadoopVersion = connectionInfo.connection.version;
		var reqUrl = hdServerMapping.getUrl(hadoopVersion);
		reqUrl += "rest/hdfsManager/downloadFromHDFS";
        var hdConnJson = dojox.json.ref.toJson(connectionInfo.connection);
        dojo.byId("alpine_datasourceexplorer_downloadFile_form").action = reqUrl;
        dojo.byId("alpine_datasourceexplorer_downloadFile_form_connectionJson").value = hdConnJson;
        dojo.byId("alpine_datasourceexplorer_downloadFile_form_from").value = startLine;
        dojo.byId("alpine_datasourceexplorer_downloadFile_form_numberOfLine").value = numberOfLine;
        dojo.byId("alpine_datasourceexplorer_downloadFile_form_filePath").value = filePath;
        dojo.byId("alpine_datasourceexplorer_downloadFile_form").submit();
	}

	function _openHadoopFileDownloadConfig(hadoopFileProperty, hdFilePath){
		filePath = hdFilePath;
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
        var displayName = hadoopFileProperty.name;
        if(displayName!=null && displayName.length>25){
            displayName = displayName.substring(0,23)+"...";
        }

        initDialogStatus();

		dojo.byId(constants.LABEL_NAME).innerHTML = displayName;
		dojo.byId(constants.LABEL_NAME).title = hadoopFileProperty.name;
		dojo.byId(constants.LABEL_OWNER).innerHTML = hadoopFileProperty.owner;
		dojo.byId(constants.LABEL_GROUP).innerHTML = hadoopFileProperty.group;
		dojo.byId(constants.LABEL_SIZE).innerHTML = hadoopFileProperty.size;
        if(isExtensionJZ(hadoopFileProperty.name) == true){
            dojo.byId(constants.TIP_ID).innerHTML = "("+alpine.nls.hadoop_data_mgr_download_config_wholedownload_tip+")";
            dojo.byId(constants.TIP_CONTAINER).style.display = "";
            dijit.byId(constants.START_LINE_INPUT).set("disabled",true);
            dijit.byId(constants.NUMBER_LINE_INPUT).set("disabled",true);

        }

	}

    function isExtensionJZ(fileName){
        if(fileName!=null && ""!=fileName){
            if(/.+\.gz$/.test(fileName.toLowerCase()) == true){
                return true;
            }
        }
        return false;
    }

    function initDialogStatus(){
        dojo.byId(constants.TIP_ID).innerHTML = "";
        dojo.byId(constants.TIP_CONTAINER).style.display = "none";
        dijit.byId(constants.START_LINE_INPUT).set("disabled",false);
        dijit.byId(constants.NUMBER_LINE_INPUT).set("disabled",false);
    }

	return {
		openHadoopFileDownloadConfig: _openHadoopFileDownloadConfig
	};
});