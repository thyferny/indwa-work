/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * FlowCategoryManager
 * Author Gary
 */
define(function(){
	var REQUEST_URL = baseURL + "/main/flowCategory.do";
	
	/**
	 * get data of tree from server
	 */
	function getTreeData(errorFn, callbackpanelid){
		var url = REQUEST_URL + "?method=getFlowByCategoryFromUser",
			treeData;
		ds.get(url, function(data){
			if(data.error_code == -100){
				if(errorFn){
					errorFn.call(null, data);
				}
				treeData = [];
			}else{
				treeData = data;
			}
		}, function(text, arg){
			//ignore.
		}, true, callbackpanelid);
		return treeData;
	}
	
	/**
	 * save new cateogory
	 */
	function saveCategory(category, fn, errorFn, callbackpanelid){
		var url = REQUEST_URL + "?method=saveFlowCategory";
		ds.post(url, category, function(data){
			if(data.error_code == -100){
				if(errorFn){
					errorFn.call(null, data);
				}
			}else{
				fn.call(null, data);
			}
		}, function(text, arg){
			//ignore.
		}, true, callbackpanelid);
	}
	
	/**
	 * move some flow into another category
	 */
	function moveFlow(targetCategory, flowInfoArray, fn, errorFn, callbackpanelid){
		var url = REQUEST_URL + "?method=moveFlowIntoCategory";
		ds.post(url, {
			categoryInfo: targetCategory,
			operateFlowInfoArray: flowInfoArray
		}, function(data){
			if(data.error_code == -100){
				if(errorFn){
					errorFn.call(null, data);
				}
			}else{
				fn.call(null, data);
			}
		}, function(text, arg){
			//ignore.
		}, true,callbackpanelid);
	}
	
	/**
	 * invoke update category request to server
	 */
	function updateCategory(category, fn, errorFn, callbackpanelid){
		var url = REQUEST_URL + "?method=renameFlowCategory";
		ds.post(url, category, function(data){
			if(data.error_code == -100){
				if(errorFn){
					errorFn.call(null, data);
				}
			}else{
				fn.call(null, data);
			}
		}, function(text, arg){
			//ignore.
		}, true, callbackpanelid);
	}
	
	/**
	 * invoke remove category request to server
	 */
	function removeCategory(category, fn, errorFn, callbackpanelid){
		var url = REQUEST_URL + "?method=removeFlowCategory";
		ds.post(url, {
			key: toolkit.getValue(category.key)
		}, function(data){
			if(data.error_code == -100){
				if(errorFn){
					errorFn.call(null, data);
				}
			}else{
				fn.call(data);
			}
		}, null, false, callbackpanelid);
	}
	
	return {
		getTreeData: getTreeData,
		saveCategory: saveCategory,
		moveFlow: moveFlow,
		updateCategory: updateCategory,
		removeCategory: removeCategory
	};
});
