/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DataSourceHadoopManager
 * Author Gary
 */
define(function(){
	var constants = {
		REQUEST_URL: baseURL + "/main/dataSource/hadoop/manager.do"
	};
	
	/**
	 * test connection with configuration
	 */
	function testConnectionForConfig(configInfo, onSuccess, onFailed, callbackpanelid){
		//progressBar.showLoadingBar();
		var isTestSuccess = false;
		ds.post(constants.REQUEST_URL + "?method=testConnection", configInfo, function(data){
			//progressBar.closeLoadingBar();
			isTestSuccess = data;
			if(isTestSuccess){
				onSuccess.call(null);
			}else{
				onFailed.call(null, data.message);
			}
		}, null, false, callbackpanelid);
	}
	
	/**
	 * save configuration of connection
	 */
	function saveConnectionConfig(configInfo, fn, callbackpanelid){
		//progressBar.showLoadingBar();
		ds.post(constants.REQUEST_URL + "?method=saveConfig", configInfo, function(data){
			//progressBar.closeLoadingBar();
			fn.call(null, data);
		}, null, false, callbackpanelid);
	}
	
	/**
	 * update configuration of connection
	 */
	function updateConnectionConfig(configInfo, fn, callbackpanelid){
		//progressBar.showLoadingBar();
		ds.post(constants.REQUEST_URL + "?method=updateConfig", configInfo, function(data){
			//progressBar.closeLoadingBar();
			fn.call(null, data);
		}, null, false, callbackpanelid);
	}
	
	/**
	 * get configuration information of connection by key
	 * synchronize function
	 */
	function getConnectionConfig(key, callbackpanelid){
		//progressBar.showLoadingBar();
		var result;
		ds.get(constants.REQUEST_URL + "?method=getConnConfig&key=" + key, function(data){
			result = data;
		}, null, true, callbackpanelid);
		//progressBar.closeLoadingBar();
		return result;
	}
	
	/**
	 * get all available versions from server
	 */
	function getAllVersions( callbackpanelid){
		var result;
		ds.get(constants.REQUEST_URL + "?method=getAllVersions", function(data){
			result = data;
		}, null, true, callbackpanelid);
		return result;
	}

	/**
	 * delete configurations of connection
	 */
	function deleteConfig(configIDs, fn, callbackpanelid){
		ds.post(constants.REQUEST_URL + "?method=deleteConfig", configIDs, function(data){
			fn.call(null, data);
		}, null, false, callbackpanelid);
	}
	
	/**
	 * get all personal hadoop connections synchronized method
	 */
	function _getPersonalConnections(){
		var result = null;
		ds.get(constants.REQUEST_URL + "?method=loadPersonalHadoopConnections", function(data){
			result = data;
		}, null, true);
		return result;
	}
	
	return {
		testConnectionForConfig: testConnectionForConfig,
		saveConnectionConfig: saveConnectionConfig,
		updateConnectionConfig: updateConnectionConfig,
		getConnectionConfig: getConnectionConfig,
		getAllVersions: getAllVersions,
		getPersonalConnections: _getPersonalConnections,
		deleteConfig: deleteConfig
	};
});