define(["alpine/datasourcemgr/DataSourceHadoopManager", "alpine/datasourcemgr/DataSourceDBManager"], function(hadoopPersistence, dbPersistence){
	
	/**
	 * get all of groups
	 * synchronize function
	 */
	function getGroups(callbackpanelid){
		var requestUrl = baseURL + "/main/admin.do?method=getGroupsForUser"; 
		var result;
		ds.get(requestUrl, function(groups){
			result = groups;
		}, null, true,callbackpanelid);
		return result;
	}
	
	/**
	 * get all of categories
	 * synchronize function
	 * @return all of categories
	 */
	function loadAllCategories(callbackpanelid){
		var result;
		ds.get(baseURL + "/main/dataSource/manager.do?method=loadDataSourceCategories", function(data){
			result = data;
		}, null, true, callbackpanelid);
		return result;
	}
	
	function removeConfigInfo(configInfoList, fn, callbackpanelid){
		var dbConfigList = new Array(),
			hadoopConfigList = new Array();
		for(var i = 0;i < configInfoList.length;i++){
			var itemId = toolkit.getValue(configInfoList[i].key);
			switch(toolkit.getValue(configInfoList[i].configType)){
			case "DATABASE": 
				dbConfigList.push(itemId);
				break;
			case "HADOOP":
				hadoopConfigList.push(itemId);
				break;
			}
		}
		if(dbConfigList.length > 0){
			dbPersistence.deleteConfig(dbConfigList, fn, callbackpanelid);
		}
		if(hadoopConfigList.length > 0){
			hadoopPersistence.deleteConfig(hadoopConfigList, fn, callbackpanelid);
		}
	}
	
	return {
		getGroups: getGroups,
		loadAllCategories: loadAllCategories,
		removeConfigInfo: removeConfigInfo
	};
});