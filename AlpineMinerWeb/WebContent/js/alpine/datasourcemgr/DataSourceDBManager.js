/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DataSourceDBManager
 * Author Gary
 */
define(function(){
	var constants = {
		REQUEST_URL: baseURL + "/main/dataSource/db/manager.do"
	};
	
	/**
	 * test connection with configuration
	 */
	function testConnectionForConfig(configInfo, onSuccess, onFailed, callbackpanelid){
		//progressBar.showLoadingBar();
		var isTestSuccess = false;
		ds.post(constants.REQUEST_URL + "?method=testConnection", configInfo, function(data){
		//	progressBar.closeLoadingBar();
			isTestSuccess = !data.error_code;
			if(isTestSuccess){
				onSuccess.call(null);
			}else{
				onFailed.call(null, data.message);
			}
		},null, false, callbackpanelid);
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
		}, null,false, callbackpanelid);
	}
	
	/**
	 * get all of categories
	 * synchronize function
	 * @return all of categories
	 */
	function getAllCategories(callbackpanelid){
		var result;
		//progressBar.showLoadingBar();
		ds.get(constants.REQUEST_URL + "?method=getAllConnCategories", function(data){
			result = data;
		}, null, true, false, callbackpanelid);
		//progressBar.closeLoadingBar();
		return result;
	}
	
	/**
	 * get configuration information of connection by key
	 * synchronize function
	 */
	function getConnectionConfig(key, callbackpanelid){
		var result;
		//progressBar.showLoadingBar();
		ds.get(constants.REQUEST_URL + "?method=getConnConfig&key=" + key, function(data){
			result = data;
		}, null, true);
		//progressBar.closeLoadingBar();
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
	
	return {
		testConnectionForConfig: testConnectionForConfig,
		saveConnectionConfig: saveConnectionConfig,
		updateConnectionConfig: updateConnectionConfig,
		getAllCategories: getAllCategories,
		getConnectionConfig: getConnectionConfig,
		deleteConfig: deleteConfig
	};
});