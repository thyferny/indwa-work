/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DataSourceExplorerManager
 * Author Robbie & Gary
 */
define(function(){
	
	var constants = {
			REQUEST_URL: baseURL + "/main/dataSource/explorer.do?",
			HADOOP_MGR_REQUEST_URL: baseURL + "/main/dataSource/hadoop/manager.do?",
            DATASOURCE_PANE: "alpine_datasourceexplorer_display_pane"// let user able to get back if connect spend a lot of time.

    };
	
	/**
	 * get all available database and hadoop connections
	 */
	function _getAvailableConnections(callbackfunction, errorCallbackFn){
		var result = [];
		ds.get(constants.REQUEST_URL + "method=getAvailableConnections", function(data){
			result = data;
            callbackfunction(result);
		}, errorCallbackFn, false, constants.DATASOURCE_PANE);
		;
	}
	
	function _getSchemasByConnection(connName, outercallbackfunction, callbackfunction, errorCallbackFn){
		ds.get(constants.REQUEST_URL + "method=getSchemaByConnection&dbConnectionName=" + connName, function(data){
            var result = [];
            for(var i = 0;i < data.length;i++){
                result[i] = {
                    key: data[i],
                    label: data[i]
                };
            }
            callbackfunction(outercallbackfunction, result);
         }, errorCallbackFn, false,constants.DATASOURCE_PANE);

	}
	
	function _getTableViewBySchema(connName, schemaName, outercallbackfunction, callbackfunction, errorCallbackFn){
		ds.get(constants.REQUEST_URL + "method=getTableViewBySchema&connName=" + connName + "&schemaName=" + schemaName, function(data){
            var result = [];
            for(var i = 0;i < data.length;i++){
            	var item = {
					key: data[i].entityName,
					label: data[i].entityName
				};
        		dojo.safeMixin(item, data[i]);
				result.push(item);
			}
            callbackfunction(outercallbackfunction, result);
		}, errorCallbackFn, false,constants.DATASOURCE_PANE);
	}
	
	function _getNextFileInHadoop(connectionKey, path, outercallbackfunction, callbackfunction, errorCallbackFn){
		var url = constants.REQUEST_URL + "method=getHadoopFileByPath&connectionKey=" + connectionKey;
		if(path){
			url += "&path=" + path;
		}
		ds.get(url, function(data){
            var result = [];
            for(var i = 0;i < data.length;i++){
            	var item = {
					label: data[i].fileName,
					key: data[i].filePath,
					isDir: data[i].isDir
				};
        		dojo.safeMixin(item, data[i]);
				result.push(item);
			}
            callbackfunction(outercallbackfunction, result);

		}, errorCallbackFn, false,constants.DATASOURCE_PANE);
	}
	
	function _createSubFolder(connectionKey, parentPath, folderName, callback, errorCallbackFn){
		var url = constants.HADOOP_MGR_REQUEST_URL + "method=createSubFolder&connectionKey=" + connectionKey + "&folderName=" + folderName;
		if(parentPath){
			url += "&parentPath=" + parentPath;
		}
		ds.get(url, function(data){
			callback.call();
		}, errorCallbackFn, false,constants.DATASOURCE_PANE);
	}
	
	function _deleteResource(connectionKey, path, callback, errCallback){
		var url = constants.HADOOP_MGR_REQUEST_URL + "method=deleteResource";
		var parameters = {
			connectionKey: connectionKey
		};
		if(path){//because, path might be null or undefined. We don't want pass them to server side as String.
			parameters.path = path;
		}
		ds.getWithData(url, parameters, function(data){
			callback.call();
		}, errCallback, false,constants.DATASOURCE_PANE);
	}
	
	function _loadHadoopProperty(connectionKey, path, callback, errCallback){
		var url = constants.HADOOP_MGR_REQUEST_URL + "method=viewHDFileProperty";
		var parameters = {
			connectionKey: connectionKey
		};
		if(path){//because, path might be null or undefined. We don't want pass them to server side as String.
			parameters.path = path;
		}
		ds.getWithData(url, parameters, function(data){
			callback.call(null, data);
		}, errCallback, false,constants.DATASOURCE_PANE);
	}
	
	function _checkHDFileisExists(connectionKey, path, callback, errCallback, loadingPaneId){
		var url = baseURL + "/main/dataSource/hadoop/manager.do?method=isHDFileExists";
		var parameters = {
			connectionKey: connectionKey
		};
		if(path){//because, path might be null or undefined. We don't want pass them to server side as String.
			parameters.path = path;
		}
		ds.getWithData(url, parameters, callback, errCallback, false, loadingPaneId);
	}
	
	return {
		getAvailableConnections: _getAvailableConnections,
		getSchemasByConnection: _getSchemasByConnection,
		getTableViewBySchema: _getTableViewBySchema,
		getNextFileInHadoop: _getNextFileInHadoop,
		deleteResource: _deleteResource,
		loadHadoopProperty: _loadHadoopProperty,
		checkHDFileisExists: _checkHDFileisExists,
		createSubFolder: _createSubFolder
	};
});