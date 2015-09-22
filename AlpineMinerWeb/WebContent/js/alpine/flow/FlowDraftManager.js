/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * FlowDraftManager.js 
 * Author Gary
 * Oct 23, 2012
 */
define(function(){
	var url = alpine.baseURL + "/main/flow.do";
	
	function _checkDraftExist(callback, errCallback){
		ds.get(url + "?method=hasFlowDraft", callback, errCallback);
	}
	
	function _getDraftFlowDTO(callback, errCallback){
		ds.get(url + "?method=openWorkFlowAsDraft", callback, errCallback, null, "FlowDisplayPanelPersonal");
	}
	
	return {
		checkDraftExist: _checkDraftExist,
		getDraftFlowDTO: _getDraftFlowDTO
	};
});