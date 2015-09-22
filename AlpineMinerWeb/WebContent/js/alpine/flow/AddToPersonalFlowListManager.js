/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AddToPersonalFlowListManager
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-21
 */


define(function(){
	function load_flow_tree(callBackFunction){
		var url = flowBaseURL + "?method=getFlows" + "&user=" + login + "&type=Group";
		ds.get(url, callBackFunction);
	};
	
	function copyFlowListHandle(/*Number =0(add) =1(add and open)*/ type,/*Array*/copy_flow_list,successCallback,ErrorCallback){
		var url = flowBaseURL + "?method=copyFlowList" 
		+ "&user=" + login
		+ "&type=" + type;
		ds.post(url, copy_flow_list,successCallback,ErrorCallback,true);
	};
	
	function delete_flow_list(/*String*/type,/*Array*/flow_list,/*Function*/callback,/*Function*/error_callback){
		var url = flowBaseURL + "?method=deleteFlowList"
		+ "&user=" + login
		+ "&type=" + type;
		ds.post(url, flow_list, callback, error_callback);
	};
	
	return {
		load_flow_tree:load_flow_tree,
		copyFlowListHandle:copyFlowListHandle,
		delete_flow_list:delete_flow_list
	};
	
});