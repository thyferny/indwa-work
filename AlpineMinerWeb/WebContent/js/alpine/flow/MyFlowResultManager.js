define(function(){
	function showFlowResultsDialog(initResultListTable) { 
		var requestUrl = baseURL + "/main/flow.do?method=getFlowResultInfoList" ;
		ds.get(requestUrl, initResultListTable);
	};
	
	function perform_delete_flow_result(/*Array*/deleteFlowArray,/*Function*/deleteFlowResultCallBack){
		var requestUrl = baseURL + "/main/flow.do?method=deleteFlowResultInfo";			
		ds.post(requestUrl,deleteFlowArray, deleteFlowResultCallBack,null);
	}
	
	return {
		showFlowResultsDialog : showFlowResultsDialog,
		perform_delete_flow_result:perform_delete_flow_result
	};
});