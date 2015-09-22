/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * SchedulerManager.js 
 * Author Gary
 * Oct 31, 2012
 */
define(function(){
	
	/**
	 * get all of flows information from user
	 * synchronized function
	 */
	function getFlowFromUser(){
		var result;
		var url = baseURL + "/main/flowCategory.do?method=getAllFlowInfo";
		ds.get(url,function(data){
			result = data;
		}, null, true);	
		return result;
	}
	
	/**
	 * get all of tasks information from user
	 * synchronized function
	 */
	function _getAllTasksFromUser(callback, errCallback, containerId){
		var url = baseURL + "/main/schedulerManage.do?method=loadTaskList";
		ds.get(url,callback, errCallback, false, containerId);	
	}
	
	/**
	 * save trigger information
	 */
	function _saveTaskInfo(taskInfoSet, completeFn, containerId){
		ds.post(baseURL + "/main/schedulerManage.do?method=saveTasks",taskInfoSet,function(data, arg){
			completeFn.call();
		}, null, false, containerId);
	}
	
	return {
		getFlowFromUser: getFlowFromUser,
		getAllTasksFromUser: _getAllTasksFromUser,
		saveTaskInfo: _saveTaskInfo
	};
});