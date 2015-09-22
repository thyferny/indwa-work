define(function(){
	var constants = {
		REQUEST_URL: baseURL + "/main/dataSource/hadoop/manager.do?",
		REQUEST_URL4FileStructure: baseURL + "/main/fileStructureManager.do?",
		HADOOP_FILE_EXPLORER_TREE_CONTAINER: "alpine_props_hadoopcommonproperty_fileexplorer_tree_container"
	};
	
	/**
	 * synchronize function to get hadoop files which are child of giving path
	 */
	function _getChildHadoopFileByPath(connectionKey, includeFile, parentPath){
		includeFile = typeof includeFile == "boolean" ? includeFile : true;
		var url = constants.REQUEST_URL4FileStructure + "method=getHadoopFilesByPath&connectionKey=" + connectionKey;
		if(parentPath){
			url += "&path=" + parentPath;
		}
		var result = [];
		ds.get(url, function(hadoopFileList){
			result = hadoopFileList;
		}, null, true, constants.HADOOP_FILE_EXPLORER_TREE_CONTAINER);
		
		if(!includeFile){
			for(var i = 0;i < result.length;i++){
				if(result[i].isDir == false){
					result.splice(i--, 1);
				}
			}
		}
		return result;
	}
	
	function _checkHasPermission(connectionKey, path){
		var hasPermission = false;
		var url = constants.REQUEST_URL + "method=checkPermissionForPath&connectionKey=" + connectionKey + "&path=" + path;
		ds.get(url, function(data){
			hasPermission = data;
		}, null, true, constants.HADOOP_FILE_EXPLORER_TREE_CONTAINER);
		return hasPermission;
	}
	
	return {
		getChildHadoopFileByPath: _getChildHadoopFileByPath,
		checkHasPermission: _checkHasPermission
	};
});